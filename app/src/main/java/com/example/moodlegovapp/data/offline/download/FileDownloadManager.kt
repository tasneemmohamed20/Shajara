package com.example.moodlegovapp.data.offline.download

import android.content.Context
import com.example.moodlegovapp.data.offline.connectivity.ConnectivityObserver
import com.example.moodlegovapp.data.offline.connectivity.NetworkType
import com.example.moodlegovapp.data.offline.db.DownloadState
import com.example.moodlegovapp.data.offline.db.DownloadedFileDao
import com.example.moodlegovapp.data.offline.db.DownloadedFileEntity
import com.example.moodlegovapp.data.offline.db.OfflineDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.security.MessageDigest

/** A single file the UI may want to download for offline use. */
data class DownloadableFile(
    val courseId: Int,
    val fileUrl: String,
    val fileName: String,
    val mimeType: String? = null,
    val sizeBytes: Long = 0L,
    val timeModified: Long = 0L
)

sealed class LargeFileWarning {
    object None : LargeFileWarning()
    data class Confirm(val sizeBytes: Long, val thresholdBytes: Long, val onWifi: Boolean) : LargeFileWarning()
}

/**
 * Downloads and tracks course files for offline use — the "cloud icon" feature
 * described in the Moodle offline doc. Mirrors these specific behaviours:
 *
 *  - Large file thresholds: Wi-Fi >= 20MB, cellular/data >= 2MB. Callers should
 *    call [checkLargeFile] before [download] and surface a confirmation dialog
 *    if it returns [LargeFileWarning.Confirm] ("The user gets alerted when
 *    downloading large files").
 *  - "If a resource has been updated on the server, a refresh icon is shown":
 *    [DownloadState.UPDATE_AVAILABLE] is computed by comparing the remote
 *    timeModified against what's stored locally (see [reconcileState]).
 *  - Files live under the app's private storage (no extra runtime permission
 *    needed on API 26+), namespaced by course so "delete course downloads"
 *    is a simple directory + DB wipe.
 */
class FileDownloadManager private constructor(
    private val context: Context,
    private val dao: DownloadedFileDao,
    private val connectivity: ConnectivityObserver
) {
    private val client = OkHttpClient.Builder().build()

    companion object {
        const val WIFI_LARGE_FILE_THRESHOLD = 20L * 1024 * 1024 // 20MB
        const val CELLULAR_LARGE_FILE_THRESHOLD = 2L * 1024 * 1024 // 2MB

        @Volatile private var instance: FileDownloadManager? = null

        fun getInstance(context: Context): FileDownloadManager =
            instance ?: synchronized(this) {
                instance ?: FileDownloadManager(
                    context.applicationContext,
                    OfflineDatabase.getInstance(context).downloadedFileDao(),
                    ConnectivityObserver.getInstance(context)
                ).also { instance = it }
            }
    }

    private fun courseDir(courseId: Int): File =
        File(context.filesDir, "offline_files/course_$courseId").apply { mkdirs() }

    private fun localFileFor(courseId: Int, fileUrl: String, fileName: String): File {
        val hash = MessageDigest.getInstance("MD5").digest(fileUrl.toByteArray())
            .joinToString("") { "%02x".format(it) }
            .take(10)
        // Keep the original extension/name for nicer "open with" behaviour, prefixed to avoid collisions.
        return File(courseDir(courseId), "${hash}_$fileName")
    }

    fun observeForCourse(courseId: Int): Flow<List<DownloadedFileEntity>> = dao.observeForCourse(courseId)

    suspend fun getState(fileUrl: String): DownloadedFileEntity? = withContext(Dispatchers.IO) {
        dao.get(fileUrl)
    }

    /**
     * Per the Moodle doc: "If the user is connected to Wi-Fi: large file >= 20MB.
     * If connected to a data network: large file >= 2MB." Returns the warning the
     * caller should show (or [LargeFileWarning.None] if no confirmation is needed).
     */
    fun checkLargeFile(sizeBytes: Long): LargeFileWarning {
        val onWifi = connectivity.networkType.value == NetworkType.WIFI
        val threshold = if (onWifi) WIFI_LARGE_FILE_THRESHOLD else CELLULAR_LARGE_FILE_THRESHOLD
        return if (sizeBytes >= threshold) {
            LargeFileWarning.Confirm(sizeBytes, threshold, onWifi)
        } else {
            LargeFileWarning.None
        }
    }

    /**
     * Downloads [file] to local storage. Caller is expected to have already
     * resolved any [LargeFileWarning] with the user. Token is appended the same
     * way the rest of the app appends it for pluginfile.php links.
     */
    suspend fun download(file: DownloadableFile, wsToken: String): Result<File> = withContext(Dispatchers.IO) {
        dao.upsert(
            DownloadedFileEntity(
                fileUrl = file.fileUrl,
                courseId = file.courseId,
                fileName = file.fileName,
                mimeType = file.mimeType,
                remoteSizeBytes = file.sizeBytes,
                remoteTimeModified = file.timeModified,
                localPath = null,
                state = DownloadState.DOWNLOADING.name
            )
        )

        try {
            val urlWithToken = if (file.fileUrl.contains("?")) "${file.fileUrl}&token=$wsToken"
                                else "${file.fileUrl}?token=$wsToken"

            val request = Request.Builder().url(urlWithToken).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                markFailed(file.fileUrl)
                return@withContext Result.failure(Exception("HTTP ${response.code}"))
            }

            val target = localFileFor(file.courseId, file.fileUrl, file.fileName)
            response.body?.byteStream()?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            } ?: run {
                markFailed(file.fileUrl)
                return@withContext Result.failure(Exception("Empty response body"))
            }

            dao.upsert(
                DownloadedFileEntity(
                    fileUrl = file.fileUrl,
                    courseId = file.courseId,
                    fileName = file.fileName,
                    mimeType = file.mimeType,
                    remoteSizeBytes = file.sizeBytes,
                    remoteTimeModified = file.timeModified,
                    localPath = target.absolutePath,
                    state = DownloadState.DOWNLOADED.name,
                    lastDownloadedAt = System.currentTimeMillis()
                )
            )
            Result.success(target)
        } catch (e: Exception) {
            markFailed(file.fileUrl)
            Result.failure(e)
        }
    }

    private suspend fun markFailed(fileUrl: String) {
        dao.get(fileUrl)?.let { dao.update(it.copy(state = DownloadState.FAILED.name)) }
    }

    /** Bulk-download every file in a section/course — the "download complete course" cloud icon. */
    suspend fun downloadAll(files: List<DownloadableFile>, wsToken: String): List<Result<File>> =
        files.map { download(it, wsToken) }

    /**
     * Compares each known file's remote timeModified against what's on disk and
     * flips the state to UPDATE_AVAILABLE where the server copy is newer — this is
     * what should drive the "refresh icon" mentioned in the Moodle doc.
     */
    suspend fun reconcileState(remoteFiles: List<DownloadableFile>) = withContext(Dispatchers.IO) {
        remoteFiles.forEach { remote ->
            val local = dao.get(remote.fileUrl)
            if (local != null && local.state == DownloadState.DOWNLOADED.name &&
                remote.timeModified > local.remoteTimeModified
            ) {
                dao.update(local.copy(state = DownloadState.UPDATE_AVAILABLE.name, remoteTimeModified = remote.timeModified))
            }
        }
    }

    suspend fun deleteCourseDownloads(courseId: Int) = withContext(Dispatchers.IO) {
        courseDir(courseId).deleteRecursively()
        dao.deleteForCourse(courseId)
    }

    suspend fun totalDownloadedBytes(): Long = withContext(Dispatchers.IO) { dao.totalDownloadedBytes() }
}
