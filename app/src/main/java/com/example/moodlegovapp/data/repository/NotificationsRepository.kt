package com.example.moodlegovapp.data.repository

import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.data.network.datasource.NotificationsDataSource
import com.example.moodlegovapp.domain.models.Notification
import com.example.moodlegovapp.domain.repositoryinterface.NotificationsRepositoryProtocol

/**
 * Notifications Repository that coordinates data sources for notification operations.
 * Future: Add local notification persistence and unread count tracking.
 */
class NotificationsRepository(
    private val notificationsDataSource: NotificationsDataSource
) : NotificationsRepositoryProtocol {

    override suspend fun getNotifications(): AppResult<List<Notification>> {
        return notificationsDataSource.getNotifications()
    }

    override suspend fun markAsRead(notificationId: Int): AppResult<Unit> {
        return notificationsDataSource.markNotificationRead(notificationId)
    }
}

