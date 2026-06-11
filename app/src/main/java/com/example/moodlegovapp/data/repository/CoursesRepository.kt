package com.example.moodlegovapp.data.repository

import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.data.network.datasource.ActivityDataSource
import com.example.moodlegovapp.data.network.datasource.AssignmentsDataSource
import com.example.moodlegovapp.data.network.datasource.CoursesDataSource
import com.example.moodlegovapp.data.network.datasource.SearchDataSource
import com.example.moodlegovapp.domain.models.Assignment
import com.example.moodlegovapp.domain.models.AssignmentSubmission
import com.example.moodlegovapp.domain.models.Course
import com.example.moodlegovapp.domain.models.CourseModule
import com.example.moodlegovapp.domain.models.CourseResource
import com.example.moodlegovapp.domain.repositoryinterface.CoursesRepositoryProtocol

/**
 * Courses Repository that coordinates data sources for course-related operations.
 * Handles course fetching, searching, assignments, and activity tracking.
 * Future: Add caching, pagination, and smart data synchronization.
 */
class CoursesRepository(
    private val coursesDataSource: CoursesDataSource,
    private val assignmentsDataSource: AssignmentsDataSource,
    private val searchDataSource: SearchDataSource,
    private val activityDataSource: ActivityDataSource
) : CoursesRepositoryProtocol {

    override suspend fun getEnrolledCourses(): AppResult<List<Course>> {
        return coursesDataSource.getEnrolledCourses()
    }

    override suspend fun getCourseDetail(courseId: Int): AppResult<Course> {
        return coursesDataSource.getCourseDetail(courseId)
    }

    override suspend fun getCourseModules(courseId: Int): AppResult<List<CourseModule>> {
        return coursesDataSource.getCourseModules(courseId)
    }

    override suspend fun getCourseResources(courseId: Int): AppResult<List<CourseResource>> {
        return coursesDataSource.getCourseResources(courseId)
    }

    override suspend fun getAssignments(courseId: Int): AppResult<List<Assignment>> {
        return assignmentsDataSource.getAssignments(courseId)
    }

    override suspend fun getAssignmentDetail(assignmentId: Int): AppResult<Assignment> {
        return assignmentsDataSource.getAssignmentDetail(assignmentId)
    }

    override suspend fun submitAssignment(submission: AssignmentSubmission): AppResult<Unit> {
        return assignmentsDataSource.submitAssignment(submission)
    }

    override suspend fun searchCourses(query: String): AppResult<List<Course>> {
        return searchDataSource.searchCourses(query)
    }

    override suspend fun updateActivityCompletion(activityId: Int, completed: Boolean): AppResult<Unit> {
        return activityDataSource.updateActivityCompletion(activityId, completed)
    }
}