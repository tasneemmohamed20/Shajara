package com.example.moodlegovapp.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.moodlegovapp.core.domain.models.Course
import com.example.moodlegovapp.ui.theme.AppColors

@Composable
fun CourseCard(
    course: Course,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.Surface)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColors.Navy.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (course.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = course.imageUrl,
                        contentDescription = course.title,
                        modifier = Modifier.size(56.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = course.category.take(3),
                        style = MaterialTheme.typography.labelMedium,
                        color = AppColors.Navy
                    )
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = course.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.TextPrimary
                )
                Text(
                    text = course.instructorName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextSecondary
                )
            }

            Text(
                text = "${course.progress}%",
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.Gold,
                fontWeight = FontWeight.SemiBold
            )
        }

        LinearProgressIndicator(
            progress = { course.progress / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = AppColors.Gold,
            trackColor = AppColors.Border
        )

        if (course.dueIn.isNotEmpty()) {
            Text(
                text = "Due in ${course.dueIn}",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextSecondary
            )
        }
    }
}
