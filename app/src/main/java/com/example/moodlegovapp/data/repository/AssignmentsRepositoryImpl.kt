package com.example.moodlegovapp.data.repository

import com.example.moodlegovapp.data.network.ApiServiceProtocol
import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.domain.models.AssignmentItem
import com.example.moodlegovapp.domain.models.AssignmentSubmission
import com.example.moodlegovapp.domain.repositoryinterface.IAssignmentsRepository

class AssignmentsRepositoryImpl(
    private val api: ApiServiceProtocol
) : IAssignmentsRepository  {
    override suspend fun getAllAssignments(courseId: Int): AppResult<List<AssignmentItem>> =
        api.getAllUserAssignments(courseId)

    override suspend fun submitAssignment(submission: AssignmentSubmission): AppResult<Unit> =
        api.submitAssignment(submission)
}