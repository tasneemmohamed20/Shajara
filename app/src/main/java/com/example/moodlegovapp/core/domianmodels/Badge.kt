package com.example.moodlegovapp.core.domianmodels

import com.google.gson.annotations.SerializedName

data class Badge(
    val id: Int,
    val title: String,
    val iconUrl: String,
    val isEarned: Boolean
)
