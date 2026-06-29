package com.example.moodlegovapp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moodlegovapp.data.network.AppResult
import com.example.moodlegovapp.data.repository.QuizFeedbackRepository
import com.google.gson.JsonElement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class QuizAttemptViewModel(private val repository: QuizFeedbackRepository) : ViewModel() {
    private val _details = MutableStateFlow<JsonElement?>(null)
    val details: StateFlow<JsonElement?> = _details

    private val _attempt = MutableStateFlow<JsonElement?>(null)
    val attempt: StateFlow<JsonElement?> = _attempt

    private val _page = MutableStateFlow<JsonElement?>(null)
    val page: StateFlow<JsonElement?> = _page

    private val _selectedAnswers = MutableStateFlow<Map<String, String>>(emptyMap())
    val selectedAnswers: StateFlow<Map<String, String>> = _selectedAnswers

    private val _activeQuizId = MutableStateFlow(0)
    val activeQuizId: StateFlow<Int> = _activeQuizId

    private val _activeAttemptId = MutableStateFlow<Int?>(null)
    val activeAttemptId: StateFlow<Int?> = _activeAttemptId

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadDetails(quizId: Int, cmid: Int = 0) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _details.value = null
            _page.value = null
            _selectedAnswers.value = emptyMap()
            try {
                val first = repository.getQuizDetails(quizId)
                val firstData = (first as? AppResult.Success)?.data
                if (firstData != null && !firstData.isMoodleException()) {
                    _activeQuizId.value = quizId
                    _details.value = firstData
                    return@launch
                }

                if (cmid > 0 && cmid != quizId) {
                    val second = repository.getQuizDetails(cmid)
                    val secondData = (second as? AppResult.Success)?.data
                    if (secondData != null && !secondData.isMoodleException()) {
                        _activeQuizId.value = cmid
                        _details.value = secondData
                        return@launch
                    }
                    _details.value = null
                    _error.value = secondData?.moodleMessage() ?: firstData?.moodleMessage() ?: (second as? AppResult.Failure)?.error?.errorDescription
                } else {
                    _details.value = null
                    _error.value = firstData?.moodleMessage() ?: (first as? AppResult.Failure)?.error?.errorDescription
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectAnswer(name: String, value: String) {
        _selectedAnswers.value = _selectedAnswers.value.toMutableMap().also { it[name] = value }
    }

    fun start(quizId: Int = _activeQuizId.value) {
        val id = quizId.takeIf { it > 0 } ?: _activeQuizId.value
        runRequest { repository.startQuiz(id).alsoSuccess { json ->
            _attempt.value = json
            _page.value = json
            _selectedAnswers.value = emptyMap()
            _activeAttemptId.value = json.findInt("attemptid") ?: json.findInt("attempt_id") ?: json.findInt("id")
            if (json.findArray("questions") == null) _activeAttemptId.value?.let { loadPage(it, 0) }
        } }
    }

    fun loadPage(attemptId: Int, page: Int) = runRequest {
        repository.getQuizPage(attemptId, page).alsoSuccess {
            _page.value = it
            _selectedAnswers.value = emptyMap()
        }
    }

    fun submitCurrentPage(attemptId: Int, page: Int, sequenceValues: Map<String, String>) = runRequest {
        val payload = _selectedAnswers.value.toMutableMap().apply { putAll(sequenceValues) }
        repository.submitQuiz(attemptId, page, payload).alsoSuccess {
            _attempt.value = it
            repository.reviewQuiz(attemptId, page).alsoSuccess { reviewJson -> _page.value = reviewJson }
        }
    }

    fun review(attemptId: Int, page: Int = 0) = runRequest { repository.reviewQuiz(attemptId, page).alsoSuccess { _page.value = it } }

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

    private fun JsonElement.isMoodleException(): Boolean = runCatching {
        isJsonObject && (asJsonObject.has("exception") || asJsonObject.has("errorcode"))
    }.getOrDefault(false)

    private fun JsonElement.moodleMessage(): String? = runCatching {
        if (!isJsonObject) null else {
            val obj = asJsonObject
            obj.get("message")?.asString ?: obj.get("errorcode")?.asString ?: obj.toString()
        }
    }.getOrNull()

    private fun JsonElement.findInt(key: String): Int? = runCatching {
        when {
            isJsonObject -> asJsonObject.get(key)?.takeIf { !it.isJsonNull }?.asInt
                ?: asJsonObject.entrySet().firstNotNullOfOrNull { it.value.findInt(key) }
            isJsonArray -> asJsonArray.firstNotNullOfOrNull { it.findInt(key) }
            else -> null
        }
    }.getOrNull()

    private fun JsonElement.findArray(key: String): JsonElement? = runCatching {
        when {
            isJsonObject -> asJsonObject.get(key)?.takeIf { it.isJsonArray }
                ?: asJsonObject.entrySet().firstNotNullOfOrNull { it.value.findArray(key) }
            isJsonArray -> asJsonArray.firstNotNullOfOrNull { it.findArray(key) }
            else -> null
        }
    }.getOrNull()
}
