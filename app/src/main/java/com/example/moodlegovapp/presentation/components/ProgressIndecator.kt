package com.example.moodlegovapp.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moodlegovapp.ui.theme.AppColors

@Composable
fun ProgressIndicator(
    progress : Float,
    percentage : String
){
    Row(verticalAlignment = Alignment.CenterVertically) {
        LinearProgressIndicator(
            progress = {progress},
            modifier = Modifier
                .weight(1f)
                .height(4.dp),
            color = AppColors.Gold,
            trackColor = Color.White.copy(alpha = 0.24f),
            gapSize = 0.dp,
            drawStopIndicator = {}
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = percentage,
            color = Color.White,
            fontSize = 12.sp
        )
    }
}