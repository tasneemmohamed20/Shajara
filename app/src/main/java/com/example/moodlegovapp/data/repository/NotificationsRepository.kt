package com.example.moodlegovapp.data.repository

import com.example.moodlegovapp.data.network.ApiServiceProtocol
import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.domain.repositoryinterface.NotificationsRepositoryProtocol
import com.example.moodlegovapp.domain.models.Notification

class NotificationsRepository(
    private val api: ApiServiceProtocol
) : NotificationsRepositoryProtocol {

    override suspend fun getNotifications(): AppResult<List<Notification>> = api.getNotifications()
    override suspend fun markAsRead(notificationId: Int): AppResult<Unit> = api.markNotificationRead(notificationId)
}

