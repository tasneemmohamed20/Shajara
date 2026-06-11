package com.example.moodlegovapp.data.network

import android.util.Log
import kotlin.math.min

/**
 * Retry policy with exponential backoff for network operations.
 * Follows best practices for handling transient failures.
 */
data class RetryPolicy(
    val maxRetries: Int = 3,
    val initialDelayMs: Long = 100,
    val maxDelayMs: Long = 30_000,
    val backoffMultiplier: Double = 2.0,
    val isRetryableError: (AppError) -> Boolean = { it.isRetryable }
) {
    companion object {
        val DEFAULT = RetryPolicy()
        val AGGRESSIVE = RetryPolicy(maxRetries = 5, initialDelayMs = 50)
        val CONSERVATIVE = RetryPolicy(maxRetries = 1, initialDelayMs = 200)
    }

    suspend fun <T> execute(operation: suspend () -> AppResult<T>): AppResult<T> {
        var lastError: AppError? = null
        var delay = initialDelayMs

        repeat(maxRetries + 1) { attempt ->
            try {
                val result = operation()
                if (result is AppResult.Success) {
                    return result
                }

                if (result is AppResult.Failure) {
                    lastError = result.error
                    if (!isRetryableError(result.error) || attempt == maxRetries) {
                        return result
                    }

                    if (attempt < maxRetries) {
                        Log.d("RetryPolicy", "Retrying after ${delay}ms (attempt ${attempt + 1}/$maxRetries)")
                        kotlinx.coroutines.delay(delay)
                        delay = min((delay * backoffMultiplier).toLong(), maxDelayMs)
                    }
                }
            } catch (e: Exception) {
                lastError = AppError.NetworkError(e.localizedMessage ?: "Unknown error", e)
                if (attempt < maxRetries) {
                    Log.d("RetryPolicy", "Exception during attempt ${attempt + 1}, retrying...")
                    kotlinx.coroutines.delay(delay)
                    delay = min((delay * backoffMultiplier).toLong(), maxDelayMs)
                }
            }
        }

        return AppResult.Failure(lastError ?: AppError.Unknown)
    }
}


