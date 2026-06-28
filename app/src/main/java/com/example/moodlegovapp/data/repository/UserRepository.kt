package com.example.moodlegovapp.data.repository

import com.example.moodlegovapp.data.network.ApiServiceProtocol
import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.data.offline.OfflineCache
import com.example.moodlegovapp.data.service.DataStoreManager
import com.example.moodlegovapp.domain.models.Badge
import com.example.moodlegovapp.domain.models.LeaderboardData
import com.example.moodlegovapp.domain.models.PerformanceOverview
import com.example.moodlegovapp.domain.models.TrainingEvent
import com.example.moodlegovapp.domain.models.TrainingStats
import com.example.moodlegovapp.domain.models.UserProfile
import com.example.moodlegovapp.domain.repositoryinterface.UserRepositoryProtocol
import com.google.gson.reflect.TypeToken

/**
 * Offline-aware user/profile repository. All reads are cache-first so the
 * profile, badges, stats, leaderboard and upcoming events screens still render
 * something useful while offline (this content is explicitly listed as
 * offline-available in the Moodle doc: "Badges", "Calendar events", etc).
 */
class UserRepository(
    private val api: ApiServiceProtocol,
    private val offlineCache: OfflineCache,
    private val dataStoreManager: DataStoreManager
) : UserRepositoryProtocol {

    private fun userId(): Int = dataStoreManager.userIdState.value?.toIntOrNull() ?: 101

    override suspend fun getUserProfile(): AppResult<UserProfile> =
        offlineCache.fetch(
            key = OfflineCache.userProfileKey(userId().toString()),
            typeToken = object : TypeToken<UserProfile>() {},
            networkCall = { api.getUserProfile() }
        )

    override suspend fun getPerformanceOverview(): AppResult<PerformanceOverview> =
        offlineCache.fetch(
            key = OfflineCache.performanceOverviewKey(userId()),
            typeToken = object : TypeToken<PerformanceOverview>() {},
            networkCall = { api.getPerformanceOverview() }
        )

    override suspend fun getUserPreferences(userId: Int?) =
        offlineCache.fetch(
            key = "user_preferences:${userId ?: userId()}",
            typeToken = object : TypeToken<List<com.example.moodlegovapp.domain.models.UserPreference>>() {},
            networkCall = { api.getUserPreferences(userId) }
        )

    override suspend fun updatePreference(type: String, value: String) = api.updatePreference(type, value)

    override suspend fun updateUserPicture(draftItemId: Long, delete: Boolean) = api.updateUserPicture(draftItemId, delete)

    override suspend fun getBadges(): AppResult<List<Badge>> =
        offlineCache.fetch(
            key = OfflineCache.badgesKey(userId()),
            typeToken = object : TypeToken<List<Badge>>() {},
            networkCall = { api.getBadges() }
        )

    override suspend fun getLeaderboard(courseId: Int): AppResult<LeaderboardData> =
        offlineCache.fetch(
            key = OfflineCache.leaderboardKey(courseId),
            courseId = courseId,
            typeToken = object : TypeToken<LeaderboardData>() {},
            networkCall = { api.getLeaderboard(courseId) }
        )

    override suspend fun getTrainingStats(): AppResult<TrainingStats> =
        offlineCache.fetch(
            key = OfflineCache.trainingStatsKey(userId()),
            typeToken = object : TypeToken<TrainingStats>() {},
            networkCall = { api.getTrainingStats() }
        )

    override suspend fun getUpcomingEvents(): AppResult<List<TrainingEvent>> =
        offlineCache.fetch(
            key = OfflineCache.upcomingEventsKey(userId()),
            typeToken = object : TypeToken<List<TrainingEvent>>() {},
            networkCall = { api.getUpcomingEvents() }
        )
}
