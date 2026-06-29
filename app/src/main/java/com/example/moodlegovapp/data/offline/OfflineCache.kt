package com.example.moodlegovapp.data.offline

import com.example.moodlegovapp.data.network.AppError
import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.data.offline.connectivity.ConnectivityObserver
import com.example.moodlegovapp.data.offline.db.CachedResponseDao
import com.example.moodlegovapp.data.offline.db.CachedResponseEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Cache-first data access used by every repository to become offline-aware
 * without duplicating the same "try network, fall back to disk" logic.
 *
 * Strategy (mirrors the Moodle app doc):
 *  - If online: call the network. On success, persist the raw JSON to Room
 *    under [key] and return it. On failure, fall back to whatever is cached
 *    (so a flaky connection doesn't blank out a screen that has cached data).
 *  - If offline: skip the network call entirely and serve the cache directly,
 *    avoiding the doc-mentioned costs of failed network attempts (battery,
 *    timeouts) when we already know there's no connection.
 *  - If neither network nor cache is available: propagate the underlying error.
 */
class OfflineCache(
    private val dao: CachedResponseDao,
    private val connectivity: ConnectivityObserver,
    private val gson: Gson = Gson()
) {

    suspend fun <T> fetch(
        key: String,
        courseId: Int = -1,
        typeToken: TypeToken<T>,
        networkCall: suspend () -> AppResult<T>
    ): AppResult<T> {
        if (!connectivity.isOnlineNow()) {
            return readCache(key, typeToken) ?: AppResult.Failure(
                AppError.NetworkError("لا يوجد اتصال بالإنترنت ولا توجد بيانات محفوظة مسبقاً")
            )
        }

        return when (val result = networkCall()) {
            is AppResult.Success -> {
                writeCache(key, courseId, result.data)
                result
            }
            is AppResult.Failure -> {
                readCache(key, typeToken) ?: result
            }
            is AppResult.Loading -> result
        }
    }

    suspend fun <T> readCache(key: String, typeToken: TypeToken<T>): AppResult<T>? {
        val entity = dao.get(key) ?: return null
        return try {
            val data: T = gson.fromJson(entity.json, typeToken.type)
            AppResult.Success(data)
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun <T> writeCache(key: String, courseId: Int, data: T) {
        dao.put(
            CachedResponseEntity(
                key = key,
                json = gson.toJson(data),
                lastSyncedAt = System.currentTimeMillis(),
                courseId = courseId
            )
        )
    }

    suspend fun isStale(key: String, maxAgeMillis: Long): Boolean {
        val entity = dao.get(key) ?: return true
        return System.currentTimeMillis() - entity.lastSyncedAt > maxAgeMillis
    }

    suspend fun lastSyncedAt(key: String): Long? = dao.get(key)?.lastSyncedAt

    suspend fun clearAll() = dao.clearAll()

    companion object {
        // Cache key builders — centralised so repositories and the sync engine agree.
        fun enrolledCoursesKey(userId: Int) = "enrolled_courses:$userId"
        fun courseContentsKey(courseId: Int) = "course_contents:$courseId"
        fun courseResourcesKey(courseId: Int) = "course_resources:$courseId"
        fun assignmentsKey(courseId: Int) = "assignments:$courseId"
        fun assignmentDetailKey(assignmentId: Int) = "assignment_detail:$assignmentId"
        fun notificationsKey(userId: Int) = "notifications:$userId"
        fun certificatesKey(userId: Int) = "certificates:$userId"
        fun badgesKey(userId: Int) = "badges:$userId"
        fun leaderboardKey(courseId: Int) = "leaderboard:$courseId"
        fun trainingStatsKey(userId: Int) = "training_stats:$userId"
        fun upcomingEventsKey(userId: Int) = "upcoming_events:$userId"
        fun userProfileKey(username: String) = "user_profile:$username"
        fun performanceOverviewKey(userId: Int) = "performance_overview:$userId"
    }
}
