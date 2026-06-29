package com.example.moodlegovapp.data.repository

import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.data.network.NetworkCallHandler
import com.example.moodlegovapp.data.network.RetrofitApiService
import com.example.moodlegovapp.data.network.RetryPolicy
import com.example.moodlegovapp.domain.models.ActivityCompletionUpdateResult
import com.example.moodlegovapp.domain.models.GovLessonDto

class LessonRepository(private val retrofit: RetrofitApiService) {
    suspend fun getLesson(cmid: Int): AppResult<GovLessonDto> =
        NetworkCallHandler.safeCall(RetryPolicy.DEFAULT) { retrofit.getGovLesson(cmid) }
    suspend fun markComplete(cmid: Int): AppResult<ActivityCompletionUpdateResult> =
        NetworkCallHandler.safeCall(RetryPolicy.DEFAULT) { retrofit.updateActivityCompletionRemote(cmid, 1) }
}
