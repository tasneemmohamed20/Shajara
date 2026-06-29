package com.example.moodlegovapp.data.repository

import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.data.network.NetworkCallHandler
import com.example.moodlegovapp.data.network.RetrofitApiService
import com.example.moodlegovapp.data.network.RetryPolicy
import com.example.moodlegovapp.domain.models.GovCourseResourcesDto

class CourseResourcesRepository(private val retrofit: RetrofitApiService) {
    suspend fun getResources(courseId: Int): AppResult<GovCourseResourcesDto> =
        NetworkCallHandler.safeCall(RetryPolicy.DEFAULT) { retrofit.getGovCourseResources(courseId) }
}
