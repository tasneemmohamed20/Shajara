package com.example.moodlegovapp.data.repository

import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.data.network.ApiServiceProtocol
import com.example.moodlegovapp.data.offline.OfflineCache
import com.example.moodlegovapp.data.offline.connectivity.ConnectivityObserver
import com.example.moodlegovapp.data.offline.db.PendingActionType
import com.example.moodlegovapp.data.offline.sync.PendingActionQueue
import com.example.moodlegovapp.data.service.DataStoreManager
import com.example.moodlegovapp.domain.models.MoodleAssignment
import com.example.moodlegovapp.domain.models.AssignmentSubmission
import com.example.moodlegovapp.domain.models.Course
import com.example.moodlegovapp.domain.models.CourseSection
import com.example.moodlegovapp.domain.models.CourseModule
import com.google.gson.reflect.TypeToken

/** Payload queued when a completion toggle is made offline. */
data class PendingCompletionUpdate(val activityId: Int, val completed: Boolean)

/**
 * Offline-aware courses repository. GET endpoints are cache-first via
 * [OfflineCache] (see that class's kdoc for the exact strategy). The one
 * mutating call exposed here — [updateActivityCompletion] — is queued via
 * [PendingActionQueue] when there's no connection, matching the Moodle doc's
 * note that "the offline support for completion is only for marking courses
 * or activities completed (when viewed)".
 */
class CoursesRepository(
    private val api: ApiServiceProtocol,
    private val offlineCache: OfflineCache,
    private val connectivity: ConnectivityObserver,
    private val pendingActions: PendingActionQueue,
    private val dataStoreManager: DataStoreManager
) : com.example.moodlegovapp.domain.repositoryinterface.CoursesRepositoryProtocol {

    private fun userId(): Int = dataStoreManager.userIdState.value?.toIntOrNull() ?: 101

    override suspend fun getEnrolledCourses(): AppResult<List<Course>> =
        offlineCache.fetch(
            key = OfflineCache.enrolledCoursesKey(userId()),
            typeToken = object : TypeToken<List<Course>>() {},
            networkCall = { api.getEnrolledCourses() }
        )

    override suspend fun getAllCourses(): AppResult<List<Course>> =
        offlineCache.fetch(
            key = "all_courses",
            typeToken = object : TypeToken<List<Course>>() {},
            networkCall = { api.getAllCourses() }
        )

    override suspend fun getCourseContents(courseId: Int): AppResult<List<CourseSection>> =
        offlineCache.fetch(
            key = OfflineCache.courseContentsKey(courseId),
            courseId = courseId,
            typeToken = object : TypeToken<List<CourseSection>>() {},
            networkCall = { api.getCourseContents(courseId) }
        )

    override suspend fun getCourseModules(courseId: Int): AppResult<List<CourseModule>> =
        offlineCache.fetch(
            key = "course_modules:$courseId",
            courseId = courseId,
            typeToken = object : TypeToken<List<CourseModule>>() {},
            networkCall = { api.getCourseModules(courseId) }
        )

    override suspend fun getCourseModule(cmid: Int) = api.getCourseModule(cmid)

    override suspend fun getCourseModuleByInstance(module: String, instance: Int) = api.getCourseModuleByInstance(module, instance)

    override suspend fun getCourseActivities(courseId: Int) =
        offlineCache.fetch(
            key = "course_activities:$courseId",
            courseId = courseId,
            typeToken = object : TypeToken<List<com.example.moodlegovapp.domain.models.MoodleActivity>>() {},
            networkCall = { api.getCourseActivities(courseId) }
        )

    override suspend fun getCourseCompletionStatus(courseId: Int, userId: Int?) = api.getCourseCompletionStatus(courseId, userId)

    override suspend fun getGradeItems(courseId: Int) = api.getGradeItems(courseId)

    override suspend fun getOverviewCourseGrades(userId: Int?) = api.getOverviewCourseGrades(userId)

    override suspend fun getUserGradesTable(courseId: Int, userId: Int?) = api.getUserGradesTable(courseId, userId)

    override suspend fun getQuizzes(courseId: Int) = api.getQuizzes(courseId)

    override suspend fun getCourseResources(courseId: Int): AppResult<List<com.example.moodlegovapp.domain.models.MoodleResource>> =
        offlineCache.fetch(
            key = OfflineCache.courseResourcesKey(courseId),
            courseId = courseId,
            typeToken = object : TypeToken<List<com.example.moodlegovapp.domain.models.MoodleResource>>() {},
            networkCall = { api.getCourseResources(courseId) }
        )

    override suspend fun getAssignments(courseId: Int): AppResult<List<MoodleAssignment>> =
        offlineCache.fetch(
            key = OfflineCache.assignmentsKey(courseId),
            courseId = courseId,
            typeToken = object : TypeToken<List<MoodleAssignment>>() {},
            networkCall = { api.getAssignments(courseId) }
        )

    override suspend fun getAssignmentDetail(assignmentId: Int): AppResult<MoodleAssignment> =
        offlineCache.fetch(
            key = OfflineCache.assignmentDetailKey(assignmentId),
            typeToken = object : TypeToken<MoodleAssignment>() {},
            networkCall = { api.getAssignmentDetail(assignmentId) }
        )

    override suspend fun submitAssignment(submission: AssignmentSubmission): AppResult<Unit> =
        api.submitAssignment(submission)

    override suspend fun searchCourses(query: String): AppResult<List<Course>> = api.searchCourses(query)

    /**
     * Toggles activity completion. If offline, the change is queued and replayed
     * by SyncEngine on reconnect — the user still sees an optimistic success so
     * the UI doesn't block on connectivity for a "viewed" completion tick.
     */
    override suspend fun updateActivityCompletion(activityId: Int, completed: Boolean): AppResult<Unit> {
        if (!connectivity.isOnlineNow()) {
            pendingActions.enqueue(PendingActionType.UPDATE_COMPLETION, PendingCompletionUpdate(activityId, completed))
            return AppResult.Success(Unit)
        }
        val result = api.updateActivityCompletion(activityId, completed)
        if (result is AppResult.Failure && result.error.isRetryable) {
            pendingActions.enqueue(PendingActionType.UPDATE_COMPLETION, PendingCompletionUpdate(activityId, completed))
            return AppResult.Success(Unit)
        }
        return result
    }
}
