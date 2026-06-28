package com.example.moodlegovapp.data.repository

import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.data.network.datasource.NotificationsDataSource
import com.example.moodlegovapp.data.offline.OfflineCache
import com.example.moodlegovapp.data.offline.connectivity.ConnectivityObserver
import com.example.moodlegovapp.data.offline.db.PendingActionType
import com.example.moodlegovapp.data.offline.sync.PendingActionQueue
import com.example.moodlegovapp.data.service.DataStoreManager
import com.example.moodlegovapp.domain.models.Notification
import com.example.moodlegovapp.domain.repositoryinterface.NotificationsRepositoryProtocol
import com.google.gson.reflect.TypeToken

/** Payload queued when "mark as read" happens offline. */
data class PendingNotificationRead(val notificationId: Int)

/**
 * Notifications Repository — cache-first reads, queued "mark as read" when
 * offline ("Notifications" is explicitly listed as offline-available content
 * in the Moodle doc).
 */
class NotificationsRepository(
    private val notificationsDataSource: NotificationsDataSource,
    private val offlineCache: OfflineCache,
    private val connectivity: ConnectivityObserver,
    private val pendingActions: PendingActionQueue,
    private val dataStoreManager: DataStoreManager
) : NotificationsRepositoryProtocol {

    private fun userId(): Int = dataStoreManager.userIdState.value?.toIntOrNull() ?: 101

    override suspend fun getNotifications(): AppResult<List<Notification>> =
        offlineCache.fetch(
            key = OfflineCache.notificationsKey(userId()),
            typeToken = object : TypeToken<List<Notification>>() {},
            networkCall = { notificationsDataSource.getNotifications() }
        )

    override suspend fun getUnreadNotificationCount(): AppResult<Int> = notificationsDataSource.getUnreadNotificationCount()

    override suspend fun getActionEventsByTimesort(from: Long, to: Long, limit: Int) =
        notificationsDataSource.getActionEventsByTimesort(from, to, limit)

    override suspend fun markAsRead(notificationId: Int): AppResult<Unit> {
        if (!connectivity.isOnlineNow()) {
            pendingActions.enqueue(PendingActionType.MARK_NOTIFICATION_READ, PendingNotificationRead(notificationId))
            return AppResult.Success(Unit)
        }
        val result = notificationsDataSource.markNotificationRead(notificationId)
        if (result is AppResult.Failure && result.error.isRetryable) {
            pendingActions.enqueue(PendingActionType.MARK_NOTIFICATION_READ, PendingNotificationRead(notificationId))
            return AppResult.Success(Unit)
        }
        return result
    }
}
