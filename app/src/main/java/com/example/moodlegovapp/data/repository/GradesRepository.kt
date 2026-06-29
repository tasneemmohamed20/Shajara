package com.example.moodlegovapp.data.repository

import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.data.network.NetworkCallHandler
import com.example.moodlegovapp.data.network.RetrofitApiService
import com.example.moodlegovapp.data.network.RetryPolicy
import com.example.moodlegovapp.domain.models.GovGradesDto

class GradesRepository(private val retrofit: RetrofitApiService) {
    suspend fun getGrades(): AppResult<GovGradesDto> =
        NetworkCallHandler.safeCall(RetryPolicy.DEFAULT) { retrofit.getGovGrades() }
}
