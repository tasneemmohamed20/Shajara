package com.example.moodlegovapp.data.repository

import com.example.moodlegovapp.data.network.ApiServiceProtocol
import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.domain.models.Badge
import com.example.moodlegovapp.domain.models.LeaderboardData
import com.example.moodlegovapp.domain.models.PerformanceOverview
import com.example.moodlegovapp.domain.models.TrainingEvent
import com.example.moodlegovapp.domain.models.TrainingStats
import com.example.moodlegovapp.domain.models.UserProfile
import com.example.moodlegovapp.domain.repositoryinterface.UserRepositoryProtocol

class UserRepository(
    private val api: ApiServiceProtocol
) : UserRepositoryProtocol {

    override suspend fun getUserProfile(): AppResult<UserProfile> = api.getUserProfile()
    override suspend fun getPerformanceOverview(): AppResult<PerformanceOverview> = api.getPerformanceOverview()
    override suspend fun getBadges(): AppResult<List<Badge>> = api.getBadges()
    override suspend fun getLeaderboard(courseId: Int): AppResult<LeaderboardData> = api.getLeaderboard(courseId)
    override suspend fun getTrainingStats(): AppResult<TrainingStats> = api.getTrainingStats()
    override suspend fun getUpcomingEvents(): AppResult<List<TrainingEvent>> = api.getUpcomingEvents()
}
