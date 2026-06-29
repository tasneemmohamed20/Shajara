package com.example.moodlegovapp.data.repository

import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.data.network.NetworkCallHandler
import com.example.moodlegovapp.data.network.RetrofitApiService
import com.example.moodlegovapp.data.network.RetryPolicy
import com.google.gson.JsonElement

class QuizFeedbackRepository(private val retrofit: RetrofitApiService) {
    suspend fun getQuizDetails(quizId: Int): AppResult<JsonElement> =
        NetworkCallHandler.safeCall(RetryPolicy.DEFAULT) { retrofit.getGovQuizDetails(quizId) }

    suspend fun startQuiz(quizId: Int): AppResult<JsonElement> =
        NetworkCallHandler.safeCall(RetryPolicy.DEFAULT) { retrofit.processGovQuiz(action = "start", quizId = quizId) }

    suspend fun getQuizPage(attemptId: Int, page: Int): AppResult<JsonElement> =
        NetworkCallHandler.safeCall(RetryPolicy.DEFAULT) { retrofit.processGovQuiz(action = "page", attemptId = attemptId, page = page) }

    suspend fun submitQuiz(attemptId: Int, page: Int, answers: Map<String, String>): AppResult<JsonElement> =
        NetworkCallHandler.safeCall(RetryPolicy.DEFAULT) {
            retrofit.processGovQuiz(action = "submit", attemptId = attemptId, page = page, encodedAnswers = answers)
        }

    suspend fun reviewQuiz(attemptId: Int, page: Int = 0): AppResult<JsonElement> =
        NetworkCallHandler.safeCall(RetryPolicy.DEFAULT) { retrofit.processGovQuiz(action = "review", attemptId = attemptId, page = page) }

    suspend fun getFeedbackDetails(feedbackId: Int): AppResult<JsonElement> =
        NetworkCallHandler.safeCall(RetryPolicy.DEFAULT) { retrofit.getGovFeedbackDetails(feedbackId) }

    suspend fun launchFeedback(feedbackId: Int): AppResult<JsonElement> =
        NetworkCallHandler.safeCall(RetryPolicy.DEFAULT) { retrofit.submitGovFeedbackAction(action = "launch", feedbackId = feedbackId) }

    suspend fun getFeedbackPage(feedbackId: Int, page: Int): AppResult<JsonElement> =
        NetworkCallHandler.safeCall(RetryPolicy.DEFAULT) { retrofit.submitGovFeedbackAction(action = "page", feedbackId = feedbackId, page = page) }

    suspend fun submitFeedbackPage(feedbackId: Int, page: Int, responses: Map<String, String>): AppResult<JsonElement> =
        NetworkCallHandler.safeCall(RetryPolicy.DEFAULT) {
            retrofit.submitGovFeedbackAction(action = "submit_page", feedbackId = feedbackId, page = page, encodedResponses = responses)
        }
}
