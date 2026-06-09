package com.example.moodlegovapp.core.data.network


sealed class AppResult<out T> {
    data class Success<T>(val data: T)   : AppResult<T>()
    data class Failure(val error: AppError) : AppResult<Nothing>()
    object Loading                        : AppResult<Nothing>()
}

sealed class AppError {
    data class NetworkError(val message: String) : AppError()
    object Unauthorized                          : AppError()
    object NotFound                              : AppError()
    data class ServerError(val code: Int)        : AppError()
    object DecodingError                         : AppError()
    object Unknown                               : AppError()

    val errorDescription: String get() = when (this) {
        is NetworkError  -> message
        is Unauthorized  -> "انتهت الجلسة، يرجى تسجيل الدخول مجدداً"
        is NotFound      -> "البيانات غير موجودة"
        is ServerError   -> "خطأ في الخادم: $code"
        is DecodingError -> "فشل في قراءة البيانات"
        is Unknown       -> "حدث خطأ غير متوقع"
    }
}


inline fun <T, R> AppResult<T>.mapSuccess(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Failure -> AppResult.Failure(error)
    is AppResult.Loading -> AppResult.Loading
}

fun <T> AppResult<T>.getOrNull(): T? = (this as? AppResult.Success)?.data

fun <T> AppResult<T>.isSuccess(): Boolean = this is AppResult.Success
