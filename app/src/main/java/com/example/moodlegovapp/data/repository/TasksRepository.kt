package com.example.moodlegovapp.data.repository

import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.data.network.NetworkCallHandler
import com.example.moodlegovapp.data.network.RetrofitApiService
import com.example.moodlegovapp.data.network.RetryPolicy
import com.example.moodlegovapp.domain.models.GovTasksDto

class TasksRepository(private val retrofit: RetrofitApiService) {
    suspend fun getTasks(): AppResult<GovTasksDto> =
        NetworkCallHandler.safeCall(RetryPolicy.DEFAULT) { retrofit.getGovTasks() }
}
