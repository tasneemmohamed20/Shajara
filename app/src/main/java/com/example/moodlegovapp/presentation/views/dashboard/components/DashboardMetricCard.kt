package com.example.moodlegovapp.presentation.views.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moodlegovapp.R
import com.example.moodlegovapp.ui.theme.AppColors
import com.example.moodlegovapp.ui.theme.SpColors

@Composable
fun MetricCard(
    backgroundColor: Color,
    cardIcon: Int,
    iconColor: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(0.85f)
            .height(122.dp)
            .width(108.dp),
//        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SpColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painterResource(cardIcon),
                    contentDescription = "course metric",
                    tint = iconColor,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                color = SpColors.blackLabel,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )


            Text(
                text = label,
                color = SpColors.DarkGray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


@Composable
fun DashboardMetricsRow(
    activeCoursesCount: Int,
    dueActivitiesCount: Int,
    completedCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp), // Leaves an even gap between all cards
        verticalAlignment = Alignment.CenterVertically
    ) {
        MetricCard(
            value = activeCoursesCount.toString(),
            label = stringResource(R.string.metric_courses_active),
            cardIcon = R.drawable.ic_courses,
            iconColor = Color.White,
            backgroundColor = AppColors.Navy,
            modifier = Modifier.weight(1f)
        )

        // 2. Activities Due Card
        MetricCard(
            value = dueActivitiesCount.toString(),
            label = stringResource(R.string.metric_activities_due),
            cardIcon = R.drawable.ic_tasks,
            iconColor = SpColors.Error,
            backgroundColor = SpColors.Error.copy(alpha = 0.1f),
            modifier = Modifier.weight(1f)
        )

        // 3. Completed Card
        MetricCard(
            value = completedCount.toString(),
            label = stringResource(R.string.metric_completed),
            cardIcon = R.drawable.ic_completed,
            iconColor = Color(0xFF2E7D32),
            backgroundColor = AppColors.green,
            modifier = Modifier.weight(1f)
        )
    }
}