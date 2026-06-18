package com.example.moodlegovapp.presentation.components

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
import com.example.moodlegovapp.domain.models.Course
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
                if (course.courseImage != null && course.courseImage.isNotEmpty()) {
                    AsyncImage(
                        model = course.courseImage,
                        contentDescription = course.fullName ?: "Course",
                        modifier = Modifier.size(56.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = "CAT", // Fallback for course.category since it's an Int now
                        style = MaterialTheme.typography.labelMedium,
                        color = AppColors.Navy
                    )
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = course.fullName ?: "Unknown Course",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.TextPrimary
                )
                Text(
                    text = "Unknown Instructor",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextSecondary
                )
            }

            Text(
                text = "${course.progress ?: 0}%",
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.Gold,
                fontWeight = FontWeight.SemiBold
            )
        }

        LinearProgressIndicator(
            progress = { (course.progress ?: 0) / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = AppColors.Gold,
            trackColor = AppColors.Border
        )

        // removed course.dueIn logic as we don't have it in CourseItem. We could just skip it or show N/A
        Text(
            text = "Due in N/A",
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextSecondary
        )
    }
}
