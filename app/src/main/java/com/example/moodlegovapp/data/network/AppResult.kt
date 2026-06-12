package com.example.moodlegovapp.data.network


sealed class AppResult<out T> {
    data class Success<T>(val data: T)   : AppResult<T>()
    data class Failure(val error: AppError) : AppResult<Nothing>()
    object Loading                        : AppResult<Nothing>()
}

sealed class AppError {
    data class NetworkError(val message: String, val exception: Throwable? = null) : AppError()
    object Unauthorized                          : AppError()
    object Forbidden                             : AppError()
    object NotFound                              : AppError()
    data class BadRequest(val message: String)   : AppError()
    data class ServerError(val code: Int, val message: String = "") : AppError()
    object ServiceUnavailable                    : AppError()
    object Timeout                               : AppError()
    object DecodingError                         : AppError()
    data class ValidationError(val fieldErrors: Map<String, String>) : AppError()
    object Unknown                               : AppError()

    val errorDescription: String get() = when (this) {
        is NetworkError     -> message
        is Unauthorized     -> "انتهت الجلسة، يرجى تسجيل الدخول مجدداً"
        is Forbidden        -> "ليس لديك صلاحيات للوصول لهذا المورد"
        is NotFound         -> "البيانات غير موجودة"
        is BadRequest       -> message
        is ServerError      -> "خطأ في الخادم: $code ${if (message.isNotEmpty()) "- $message" else ""}"
        is ServiceUnavailable -> "الخدمة غير متاحة حالياً"
        is Timeout          -> "انتهت المهلة الزمنية للطلب"
        is DecodingError    -> "فشل في قراءة البيانات"
        is ValidationError  -> fieldErrors.values.joinToString(", ")
        is Unknown          -> "حدث خطأ غير متوقع"
    }

    val isRetryable: Boolean get() = when (this) {
        is NetworkError, is Timeout, is ServiceUnavailable -> true
        is ServerError -> code in 500..599
        else -> false
    }

    val isCritical: Boolean get() = when (this) {
        is Unauthorized, is Forbidden, is ValidationError -> true
        else -> false
    }
}


inline fun <T, R> AppResult<T>.mapSuccess(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Failure -> AppResult.Failure(error)
    is AppResult.Loading -> AppResult.Loading
}

fun <T> AppResult<T>.getOrNull(): T? = (this as? AppResult.Success)?.data

fun <T> AppResult<T>.isSuccess(): Boolean = this is AppResult.Success

fun <T> AppResult<T>.isFailure(): Boolean = this is AppResult.Failure

fun <T> AppResult<T>.getErrorOrNull(): AppError? = (this as? AppResult.Failure)?.error

