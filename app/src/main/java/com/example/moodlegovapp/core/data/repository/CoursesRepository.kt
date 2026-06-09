package com.example.moodlegovapp.core.data.repository
import com.example.moodlegovapp.core.data.network.ApiServiceProtocol
import com.example.moodlegovapp.core.data.network.AppResult
import com.example.moodlegovapp.core.domain.models.*
import com.example.moodlegovapp.core.domain.repositoryinterface.CoursesRepositoryProtocol

class CoursesRepository(
    private val api: ApiServiceProtocol
) : CoursesRepositoryProtocol {

    override suspend fun getEnrolledCourses(): AppResult<List<Course>> = api.getEnrolledCourses()
    override suspend fun getCourseDetail(courseId: Int): AppResult<Course>            = api.getCourseDetail(courseId)
    override suspend fun getCourseModules(courseId: Int): AppResult<List<CourseModule>> = api.getCourseModules(courseId)
    override suspend fun getCourseResources(courseId: Int): AppResult<List<CourseResource>> = api.getCourseResources(courseId)
    override suspend fun getAssignments(courseId: Int): AppResult<List<Assignment>>   = api.getAssignments(courseId)
    override suspend fun getAssignmentDetail(assignmentId: Int): AppResult<Assignment> = api.getAssignmentDetail(assignmentId)
    override suspend fun submitAssignment(submission: AssignmentSubmission): AppResult<Unit> = api.submitAssignment(submission)
    override suspend fun searchCourses(query: String): AppResult<List<Course>>        = api.searchCourses(query)
    override suspend fun updateActivityCompletion(activityId: Int, completed: Boolean): AppResult<Unit> = api.updateActivityCompletion(activityId, completed)
}