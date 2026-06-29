package com.example.moodlegovapp.presentation.views.offline

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.moodlegovapp.core.DependencyContainer
import com.example.moodlegovapp.data.offline.sync.SyncStatus
import com.example.moodlegovapp.ui.theme.AppColors
import kotlinx.coroutines.launch

/**
 * Thin status banner mirroring the Moodle doc's "the user will see a message
 * indicating there is data pending synchronisation". Shows:
 *  - an offline pill when there's no connection at all
 *  - a "N items pending sync" pill (tap to force a manual sync) when online but
 *    there's a queued action backlog
 *
 * Drop this at the top of any screen's Column — it collapses to nothing when
 * online with an empty queue, so it's safe to always include.
 */
@Composable
fun OfflineStatusBanner(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val container = remember { DependencyContainer.getInstance(context) }
    val scope = rememberCoroutineScope()

    val isOnline by container.connectivityObserver.isOnline.collectAsState()
    val pendingCount by container.pendingActionQueue.observePendingCount().collectAsState(initial = 0)
    val syncStatus by container.syncEngine(context).status.collectAsState()

    val visible = !isOnline || pendingCount > 0

    AnimatedVisibility(visible = visible, enter = expandVertically(), exit = shrinkVertically()) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(if (!isOnline) AppColors.ErrorBackground else AppColors.GoldLight)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .let { base ->
                    if (isOnline && pendingCount > 0) {
                        base.clickable { scope.launch { container.syncEngine(context).syncNow(force = true) } }
                    } else base
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when {
                !isOnline -> {
                    Icon(Icons.Default.CloudOff, contentDescription = null, tint = AppColors.Error)
                    Text("لا يوجد اتصال بالإنترنت — يتم العرض من البيانات المحفوظة", color = AppColors.Error)
                }
                syncStatus is SyncStatus.Syncing -> {
                    CircularProgressIndicator(modifier = Modifier.padding(2.dp), strokeWidth = 2.dp)
                    Text("جارٍ المزامنة...", color = AppColors.TextPrimary)
                }
                pendingCount > 0 -> {
                    Icon(Icons.Default.Sync, contentDescription = null, tint = AppColors.NavyDark)
                    Text("لديك $pendingCount عملية بانتظار المزامنة — اضغط للمزامنة الآن", color = AppColors.NavyDark)
                }
            }
        }
    }
}
