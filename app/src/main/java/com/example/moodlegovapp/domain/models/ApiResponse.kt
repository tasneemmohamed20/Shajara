package com.example.moodlegovapp.domain.models

data class ApiResponse<T>(
    val data: T?,
    val error: String?,
    val success: Boolean
)