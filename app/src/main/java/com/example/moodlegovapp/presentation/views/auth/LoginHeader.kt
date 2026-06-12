package com.example.moodlegovapp.presentation.views.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush


@Composable
fun LoginHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF2F5D8A), Color(0xFF1A3550))
                ),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {

            Text(
                "Welcome",
                color = Color.White,
                fontSize = 28.sp
            )

            Text(
                "Sign in to continue",
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}