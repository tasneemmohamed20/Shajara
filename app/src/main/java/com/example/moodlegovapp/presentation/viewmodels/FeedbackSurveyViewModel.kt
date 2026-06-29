package com.example.moodlegovapp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.data.repository.QuizFeedbackRepository
import com.google.gson.JsonElement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FeedbackSurveyViewModel(private val repository: QuizFeedbackRepository) : ViewModel() {
    private val _details = MutableStateFlow<JsonElement?>(null)
    val details: StateFlow<JsonElement?> = _details

    private val _page = MutableStateFlow<JsonElement?>(null)
    val page: StateFlow<JsonElement?> = _page

    private val _responses = MutableStateFlow<Map<String, String>>(emptyMap())
    val responses: StateFlow<Map<String, String>> = _responses

    private val _submitted = MutableStateFlow(false)
    val submitted: StateFlow<Boolean> = _submitted

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadDetails(feedbackId: Int) = runRequest { repository.getFeedbackDetails(feedbackId).alsoSuccess { _details.value = it } }
    fun launch(feedbackId: Int) = runRequest { repository.launchFeedback(feedbackId).alsoSuccess { _page.value = it; _submitted.value = false } }
    fun loadPage(feedbackId: Int, page: Int) = runRequest { repository.getFeedbackPage(feedbackId, page).alsoSuccess { _page.value = it; _responses.value = emptyMap() } }
    fun setResponse(name: String, value: String) { _responses.value = _responses.value.toMutableMap().also { it[name] = value } }
    fun submitPage(feedbackId: Int, page: Int, nextPage: Int?) = runRequest {
        repository.submitFeedbackPage(feedbackId, page, _responses.value.filterValues { it.isNotBlank() }).alsoSuccess { json ->
            _page.value = json
            _responses.value = emptyMap()
            if (nextPage != null && nextPage >= 0) loadPage(feedbackId, nextPage) else _submitted.value = true
        }
    }

    private fun runRequest(block: suspend () -> AppResult<JsonElement>) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                when (val r = block()) {
                    is AppResult.Success -> if (r.data.isMoodleException()) _error.value = r.data.moodleMessage()
                    is AppResult.Failure -> _error.value = r.error.errorDescription
                    AppResult.Loading -> Unit
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private inline fun AppResult<JsonElement>.alsoSuccess(onSuccess: (JsonElement) -> Unit): AppResult<JsonElement> {
        if (this is AppResult.Success && !data.isMoodleException()) onSuccess(data)
        return this
    }

    private fun JsonElement.isMoodleException(): Boolean = runCatching { isJsonObject && (asJsonObject.has("exception") || asJsonObject.has("errorcode")) }.getOrDefault(false)
    private fun JsonElement.moodleMessage(): String? = runCatching {
        if (!isJsonObject) null else asJsonObject.get("message")?.asString ?: asJsonObject.get("errorcode")?.asString ?: toString()
    }.getOrNull()
}
