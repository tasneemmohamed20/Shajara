package com.example.moodlegovapp.core.domain.models

data class Badge(
    val id: Int,
    val title: String,
    val iconUrl: String,
    val isEarned: Boolean
)
