package com.example.moodlegovapp.data.repository

import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.data.network.datasource.BadgesDataSource
import com.example.moodlegovapp.data.network.datasource.EventsDataSource
import com.example.moodlegovapp.data.network.datasource.LeaderboardDataSource
import com.example.moodlegovapp.data.network.datasource.StatsDataSource
import com.example.moodlegovapp.data.network.datasource.UserDataSource
import com.example.moodlegovapp.domain.models.Badge
import com.example.moodlegovapp.domain.models.LeaderboardEntry
import com.example.moodlegovapp.domain.models.PerformanceOverview
import com.example.moodlegovapp.domain.models.TrainingEvent
import com.example.moodlegovapp.domain.models.TrainingStats
import com.example.moodlegovapp.domain.models.User
import com.example.moodlegovapp.domain.repositoryinterface.UserRepositoryProtocol

/**
 * User Repository that coordinates data sources for user-related operations.
 * Future: Add caching strategies and data synchronization logic.
 */
class UserRepository(
    private val userDataSource: UserDataSource,
    private val badgesDataSource: BadgesDataSource,
    private val leaderboardDataSource: LeaderboardDataSource,
    private val eventsDataSource: EventsDataSource,
    private val statsDataSource: StatsDataSource
) : UserRepositoryProtocol {

    override suspend fun getUserProfile(): AppResult<User> {
        return userDataSource.getUserProfile()
    }

    override suspend fun getPerformanceOverview(): AppResult<PerformanceOverview> {
        return userDataSource.getPerformanceOverview()
    }

    override suspend fun getBadges(): AppResult<List<Badge>> {
        return badgesDataSource.getBadges()
    }

    override suspend fun getLeaderboard(courseId: Int): AppResult<List<LeaderboardEntry>> {
        return leaderboardDataSource.getLeaderboard(courseId)
    }

    override suspend fun getTrainingStats(): AppResult<TrainingStats> {
        return statsDataSource.getTrainingStats()
    }

    override suspend fun getUpcomingEvents(): AppResult<List<TrainingEvent>> {
        return eventsDataSource.getUpcomingEvents()
    }
}
