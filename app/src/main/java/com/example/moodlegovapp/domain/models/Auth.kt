package com.example.moodlegovapp.domain.models

data class LoginRequest(
    val username: String,
    val password: String,
    val service: String = "moodle_mobile_app"
)

data class AuthToken(
    val token: String,
    val privateToken: String?
)