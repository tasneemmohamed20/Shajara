package com.example.moodlegovapp.data.offline.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Generic cache row used for any "read" endpoint (courses, sections, resources,
 * assignments, notifications, certificates, badges, profile, stats, leaderboard…).
 *
 * Rather than modelling a dedicated table per Moodle entity (which would require
 * chasing every domain model through Room's type system), we cache the exact
 * Gson-serialized response body keyed by a logical cache key. This mirrors how
 * the official Moodle app's "offline mode" works: it stores the raw web service
 * response and replays it when offline, refreshing it in the background whenever
 * a network call succeeds.
 *
 * [key] examples:
 *  - "enrolled_courses:101"
 *  - "course_contents:55"
 *  - "course_resources:55"
 *  - "assignments:55"
 *  - "notifications:101"
 *  - "user_profile:test.student1"
 */
@Entity(tableName = "cached_responses")
data class CachedResponseEntity(
    @PrimaryKey val key: String,
    val json: String,
    val lastSyncedAt: Long,
    /** Course this entry logically belongs to, for "download whole course" bulk ops. -1 if N/A */
    val courseId: Int = -1
)

/**
 * Tracks a downloadable file (course resource, module content, SCORM/H5P package…)
 * for the offline file manager. Mirrors the Moodle doc's notion of a "large file"
 * threshold and per-file download/refresh state.
 */
@Entity(tableName = "downloaded_files")
data class DownloadedFileEntity(
    @PrimaryKey val fileUrl: String,
    val courseId: Int,
    val fileName: String,
    val mimeType: String?,
    val remoteSizeBytes: Long,
    val remoteTimeModified: Long,
    /** Absolute path on local disk once downloaded, null otherwise */
    val localPath: String?,
    val state: String, // see DownloadState
    val lastDownloadedAt: Long = 0L
)

enum class DownloadState {
    NOT_DOWNLOADED,
    QUEUED,
    DOWNLOADING,
    DOWNLOADED,
    UPDATE_AVAILABLE, // remote timemodified is newer than what we have on disk
    FAILED
}

/**
 * Offline action queue. Any mutating call made while offline (assignment save,
 * finalize, file upload, completion toggle, mark-notification-read…) is appended
 * here and flushed on reconnect by the SyncEngine. The Moodle doc explicitly notes
 * that "actions performed offline are logged... with the synchronisation time, not
 * the time when they happened" — [createdAt] is the original action time we keep
 * for the user's own reference, while [syncedAt] records when it actually went out.
 */
@Entity(tableName = "pending_actions")
data class PendingActionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // see PendingActionType
    val payloadJson: String,
    val createdAt: Long,
    val syncedAt: Long? = null,
    val attemptCount: Int = 0,
    val lastError: String? = null
)

enum class PendingActionType {
    SAVE_SUBMISSION,
    FINALIZE_SUBMISSION,
    UPLOAD_FILE,
    UPDATE_COMPLETION,
    MARK_NOTIFICATION_READ
}
