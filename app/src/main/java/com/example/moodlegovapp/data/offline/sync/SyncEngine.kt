package com.example.moodlegovapp.data.offline.sync

import android.content.Context
import android.util.Log
import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.data.offline.connectivity.ConnectivityObserver
import com.example.moodlegovapp.data.offline.connectivity.NetworkType
import com.example.moodlegovapp.data.offline.db.PendingActionEntity
import com.example.moodlegovapp.data.offline.db.PendingActionType
import com.example.moodlegovapp.data.repository.AppDependencies
import com.example.moodlegovapp.data.repository.PendingCompletionUpdate
import com.example.moodlegovapp.data.repository.PendingNotificationRead
import com.example.moodlegovapp.data.service.DataStoreManager
import com.example.moodlegovapp.domain.models.AssignmentSubmission
import com.example.moodlegovapp.domain.models.AssignmentSubmissionFinalize
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "SyncEngine"

sealed class SyncStatus {
    object Idle : SyncStatus()
    object Syncing : SyncStatus()
    data class Done(val succeeded: Int, val failed: Int, val at: Long) : SyncStatus()
    data class Error(val message: String) : SyncStatus()
}

/**
 * Coordinates synchronization between the local Room cache/queue and the
 * Moodle server. Mirrors the "Synchronisation" section of the offline doc:
 *
 *  - "Actions performed offline are logged and stored... with the
 *    synchronisation time, not the time when they happened" → each
 *    [PendingActionEntity] is stamped with `syncedAt` only once it's actually
 *    flushed here, never backdated to `createdAt`.
 *  - "There are two ways of synchronising data: Automatic (every 10 minutes)
 *    and Manual" → [PeriodicSyncWorker] drives the automatic path via
 *    WorkManager; [syncNow] is what a manual "sync" button/pull-to-refresh
 *    should call directly.
 *  - "Users can decide if they want to allow synchronisation only when
 *    connected to Wi-Fi" → [syncNow] checks [DataStoreManager.syncWifiOnlyState]
 *    before doing any network work unless [force] is set (e.g. user tapped a
 *    manual sync button explicitly).
 */
class SyncEngine(
    private val context: Context,
    private val deps: AppDependencies = AppDependencies.getInstance(context)
) {
    private val connectivity: ConnectivityObserver = ConnectivityObserver.getInstance(context)
    private val pendingActions = deps.pendingActionQueue
    private val dataStoreManager: DataStoreManager = deps.dataStoreManager

    private val _status = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val status: StateFlow<SyncStatus> = _status.asStateFlow()

    /**
     * Runs one synchronization pass: flush the pending action queue, then let
     * already-open screens naturally refresh their caches on next read (since
     * every repository call is itself network-first when online).
     *
     * @param force bypass the "Wi-Fi only" user preference (used for an explicit
     *   manual "Sync now" tap, matching how the official app still allows a
     *   manual sync over cellular even if auto-sync is Wi-Fi-restricted).
     */
    suspend fun syncNow(force: Boolean = false): SyncStatus {
        if (!dataStoreManager.offlineModeEnabledState.value) {
            // Admin/user disabled offline mode entirely — nothing to reconcile.
            return SyncStatus.Idle.also { _status.value = it }
        }
        if (!connectivity.isOnlineNow()) {
            return SyncStatus.Error("لا يوجد اتصال بالإنترنت").also { _status.value = it }
        }
        if (!force && dataStoreManager.syncWifiOnlyState.value && connectivity.networkType.value != NetworkType.WIFI) {
            return SyncStatus.Idle.also { _status.value = it }
        }

        _status.value = SyncStatus.Syncing
        var succeeded = 0
        var failed = 0

        for (action in pendingActions.getUnsynced()) {
            val result = runCatching { replay(action) }
            if (result.isSuccess && result.getOrDefault(false)) {
                pendingActions.markSynced(action)
                succeeded++
            } else {
                pendingActions.markFailed(action, result.exceptionOrNull()?.message ?: "فشلت المزامنة")
                failed++
            }
        }

        dataStoreManager.markSyncedNow()
        val done = SyncStatus.Done(succeeded, failed, System.currentTimeMillis())
        _status.value = done
        Log.d(TAG, "Sync complete: $succeeded ok, $failed failed")
        return done
    }

    /** Replays a single queued action against the real repositories. Returns true on success. */
    private suspend fun replay(action: PendingActionEntity): Boolean {
        return when (PendingActionType.valueOf(action.type)) {
            PendingActionType.SAVE_SUBMISSION -> {
                val payload = pendingActions.decode(action, AssignmentSubmission::class.java)
                deps.assignmentsRepository.saveSubmission(payload).isSyncSuccess()
            }
            PendingActionType.FINALIZE_SUBMISSION -> {
                val payload = pendingActions.decode(action, AssignmentSubmissionFinalize::class.java)
                deps.assignmentsRepository.finalizeSubmission(payload).isSyncSuccess()
            }
            PendingActionType.UPLOAD_FILE -> {
                val payload = pendingActions.decode(action, PendingFileUpload::class.java)
                val bytes = android.util.Base64.decode(payload.fileBytesBase64, android.util.Base64.NO_WRAP)
                deps.assignmentsRepository.uploadFile(payload.assignmentId, payload.fileName, bytes).isSyncSuccess()
            }
            PendingActionType.UPDATE_COMPLETION -> {
                val payload = pendingActions.decode(action, PendingCompletionUpdate::class.java)
                deps.coursesRepository.updateActivityCompletion(payload.activityId, payload.completed).isSyncSuccess()
            }
            PendingActionType.MARK_NOTIFICATION_READ -> {
                val payload = pendingActions.decode(action, PendingNotificationRead::class.java)
                deps.notificationsRepository.markAsRead(payload.notificationId).isSyncSuccess()
            }
        }
    }

    private fun <T> AppResult<T>.isSyncSuccess(): Boolean = this is AppResult.Success

    companion object {
        @Volatile private var instance: SyncEngine? = null

        fun getInstance(context: Context): SyncEngine =
            instance ?: synchronized(this) {
                instance ?: SyncEngine(context.applicationContext).also { instance = it }
            }
    }
}
