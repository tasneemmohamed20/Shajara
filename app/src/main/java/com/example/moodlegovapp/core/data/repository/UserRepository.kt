package com.example.moodlegovapp.core.data.repository

import android.util.Log
import com.example.moodlegovapp.core.data.network.ApiServiceProtocol
import com.example.moodlegovapp.core.data.network.AppError
import com.example.moodlegovapp.core.data.network.AppResult
import com.example.moodlegovapp.core.domain.models.Badge
import com.example.moodlegovapp.core.domain.models.LeaderboardEntry
import com.example.moodlegovapp.core.domain.models.PerformanceOverview
import com.example.moodlegovapp.core.domain.models.TrainingEvent
import com.example.moodlegovapp.core.domain.models.TrainingStats
import com.example.moodlegovapp.core.domain.models.User
import com.example.moodlegovapp.core.domain.repositoryinterface.UserRepositoryProtocol

class UserRepository(
    private val api: ApiServiceProtocol
) : UserRepositoryProtocol {

    override suspend fun getUserProfile(): AppResult<User>                            = api.getUserProfile()
    override suspend fun getPerformanceOverview(): AppResult<PerformanceOverview>     = api.getPerformanceOverview()
    override suspend fun getBadges(): AppResult<List<Badge>>                          = api.getBadges()
    override suspend fun getLeaderboard(courseId: Int): AppResult<List<LeaderboardEntry>> = api.getLeaderboard(courseId)
    override suspend fun getTrainingStats(): AppResult<TrainingStats>                 = api.getTrainingStats()
    override suspend fun getUpcomingEvents(): AppResult<List<TrainingEvent>>          = api.getUpcomingEvents()
}
