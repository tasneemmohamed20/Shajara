package com.example.moodlegovapp.presentation.views.offline

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.moodlegovapp.core.DependencyContainer
import com.example.moodlegovapp.data.network.NetworkConfig
import com.example.moodlegovapp.data.offline.db.DownloadState
import com.example.moodlegovapp.data.offline.download.DownloadableFile
import com.example.moodlegovapp.data.offline.download.LargeFileWarning
import com.example.moodlegovapp.ui.theme.AppColors
import kotlinx.coroutines.launch

/**
 * Per-file "cloud" download affordance, matching the Moodle doc's described
 * behaviour: a cloud icon to download a specific resource, a check once it's
 * downloaded, a refresh icon when the server copy is newer, and a confirmation
 * dialog before downloading anything over the Wi-Fi(20MB)/cellular(2MB)
 * large-file threshold.
 */
@Composable
fun DownloadStatusIcon(
    file: DownloadableFile,
    courseId: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val container = remember { DependencyContainer.getInstance(context) }
    val manager = container.fileDownloadManager
    val scope = rememberCoroutineScope()

    var state by remember(file.fileUrl) { mutableStateOf<DownloadState?>(null) }
    var showLargeFileConfirm by remember { mutableStateOf<LargeFileWarning.Confirm?>(null) }
    var isDownloading by remember { mutableStateOf(false) }

    LaunchedEffect(file.fileUrl) {
        state = manager.getState(file.fileUrl)?.state?.let { DownloadState.valueOf(it) }
    }

    fun startDownload() {
        scope.launch {
            isDownloading = true
            manager.download(file, NetworkConfig.WS_TOKEN)
            state = manager.getState(file.fileUrl)?.state?.let { DownloadState.valueOf(it) }
            isDownloading = false
        }
    }

    fun onTap() {
        val warning = manager.checkLargeFile(file.sizeBytes)
        if (warning is LargeFileWarning.Confirm) {
            showLargeFileConfirm = warning
        } else {
            startDownload()
        }
    }

    when {
        isDownloading -> CircularProgressIndicator(modifier = modifier.size(20.dp), strokeWidth = 2.dp)
        state == DownloadState.DOWNLOADED -> Icon(
            Icons.Default.CloudDone,
            contentDescription = "تم التحميل",
            tint = AppColors.Success,
            modifier = modifier.size(20.dp)
        )
        state == DownloadState.UPDATE_AVAILABLE -> Icon(
            Icons.Default.Refresh,
            contentDescription = "يوجد تحديث",
            tint = AppColors.Gold,
            modifier = modifier.size(20.dp).clickable { onTap() }
        )
        else -> Icon(
            Icons.Default.CloudDownload,
            contentDescription = "تحميل للاستخدام دون اتصال",
            tint = AppColors.TextSecondary,
            modifier = modifier.size(20.dp).clickable { onTap() }
        )
    }

    showLargeFileConfirm?.let { warning ->
        val sizeMb = warning.sizeBytes / (1024f * 1024f)
        AlertDialog(
            onDismissRequest = { showLargeFileConfirm = null },
            title = { Text("ملف كبير الحجم") },
            text = {
                val networkNote = if (warning.onWifi) "أنت متصل بشبكة Wi-Fi." else "أنت متصل بشبكة بيانات الجوال، قد يستهلك هذا التحميل من باقتك."
                Text("حجم هذا الملف %.1f ميجابايت. $networkNote هل تريد المتابعة؟".format(sizeMb))
            },
            confirmButton = {
                TextButton(onClick = {
                    showLargeFileConfirm = null
                    startDownload()
                }) { Text("تحميل") }
            },
            dismissButton = {
                TextButton(onClick = { showLargeFileConfirm = null }) { Text("إلغاء") }
            }
        )
    }
}
