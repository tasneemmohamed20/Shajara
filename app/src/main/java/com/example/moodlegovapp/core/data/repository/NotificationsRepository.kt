package com.example.moodlegovapp.core.data.repository

import com.example.moodlegovapp.core.data.network.ApiServiceProtocol
import com.example.moodlegovapp.core.data.network.AppResult
import com.example.moodlegovapp.core.domain.repositoryinterface.NotificationsRepositoryProtocol
import com.example.moodlegovapp.core.domain.models.Notification
class NotificationsRepository(
    private val api: ApiServiceProtocol
) : NotificationsRepositoryProtocol {

    override suspend fun getNotifications(): AppResult<List<Notification>> = api.getNotifications()
    override suspend fun markAsRead(notificationId: Int): AppResult<Unit>     = api.markNotificationRead(notificationId)
}

