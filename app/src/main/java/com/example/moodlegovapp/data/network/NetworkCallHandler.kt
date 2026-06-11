package com.example.moodlegovapp.data.network

import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Centralized network call handler with comprehensive error mapping.
 * Converts HTTP responses and exceptions into AppError sealed classes.
 */
object NetworkCallHandler {

    /**
     * Execute a network call with proper error handling and retry logic.
     * @param retryPolicy Policy defining retry behavior
     * @param call Suspend function returning Retrofit Response
     * @return AppResult with Success, Failure, or appropriate error state
     */
    suspend inline fun <T> safeCall(
        retryPolicy: RetryPolicy = RetryPolicy.DEFAULT,
        crossinline call: suspend () -> Response<T>
    ): AppResult<T> {
        return retryPolicy.execute {
            executeCall { call() }
        }
    }

    /**
     * Execute a network call without retry logic.
     * @param call Suspend function returning Retrofit Response
     * @return AppResult with Success or Failure
     */
    suspend inline fun <T> executeCall(
        crossinline call: suspend () -> Response<T>
    ): AppResult<T> {
        return try {
            val response = call()
            when {
                response.isSuccessful -> {
                    val body = response.body()
                    if (body != null) {
                        AppResult.Success(body)
                    } else {
                        AppResult.Failure(AppError.DecodingError)
                    }
                }
                response.code() == 400 -> {
                    val errorBody = response.errorBody()?.string() ?: "Bad request"
                    AppResult.Failure(AppError.BadRequest(errorBody))
                }
                response.code() == 401 -> AppResult.Failure(AppError.Unauthorized)
                response.code() == 403 -> AppResult.Failure(AppError.Forbidden)
                response.code() == 404 -> AppResult.Failure(AppError.NotFound)
                response.code() == 422 -> {
                    // Handle validation errors (Unprocessable Entity)
                    AppResult.Failure(AppError.ValidationError(emptyMap()))
                }
                response.code() in 500..599 -> {
                    val message = response.message()
                    AppResult.Failure(AppError.ServerError(response.code(), message))
                }
                response.code() == 503 -> AppResult.Failure(AppError.ServiceUnavailable)
                else -> AppResult.Failure(AppError.ServerError(response.code(), response.message()))
            }
        } catch (e: SocketTimeoutException) {
            AppResult.Failure(AppError.Timeout)
        } catch (e: IOException) {
            AppResult.Failure(AppError.NetworkError(e.localizedMessage ?: "Network error", e))
        } catch (e: Exception) {
            AppResult.Failure(AppError.NetworkError(e.localizedMessage ?: "Unknown error", e))
        }
    }
}


