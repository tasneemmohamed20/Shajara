package com.example.moodlegovapp.core.domianmodels

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    val data: T?,
    val error: String?,
    val success: Boolean
)
