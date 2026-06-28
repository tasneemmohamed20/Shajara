package com.example.moodlegovapp.domain.models

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val username: String,
    val password: String,
    val service: String = "moodle_mobile_app"
)

data class AuthToken(

    @SerializedName("token")
    val token: String,
    @SerializedName("privatetoken")
    val privateToken: String? = null
)