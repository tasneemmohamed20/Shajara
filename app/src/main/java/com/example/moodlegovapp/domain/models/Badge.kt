package com.example.moodlegovapp.domain.models

data class Badge(
    val id: Int,
    val title: String,
    val iconUrl: String,
    val isEarned: Boolean
)
