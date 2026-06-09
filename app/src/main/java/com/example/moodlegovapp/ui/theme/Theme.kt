package com.example.moodlegovapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AppColorScheme = lightColorScheme(
    primary = AppColors.Navy,
    onPrimary = AppColors.Surface,
    secondary = AppColors.Gold,
    onSecondary = AppColors.TextPrimary,
    background = AppColors.Background,
    onBackground = AppColors.TextPrimary,
    surface = AppColors.Surface,
    onSurface = AppColors.TextPrimary,
    outline = AppColors.Border,
    error = AppColors.Error
)

@Composable
fun MoodleGovAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}
