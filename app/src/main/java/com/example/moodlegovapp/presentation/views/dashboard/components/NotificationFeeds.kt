package com.example.moodlegovapp.presentation.views.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moodlegovapp.R
import com.example.moodlegovapp.ui.theme.SpColors
import com.example.moodlegovapp.ui.theme.SpTypography

enum class SchedulePeriod(val displayName: String) {
    TODAY("Today"), WEEK("This Week"), MONTH("This Month")
}

data class ScheduleEvent(
    val id: Int,
    val title: String,
    val type: String,
    val category: String,
    val timeAndDate: String,
    val location: String,
    val instructor: String,
    val iconRes: Int
)

@Composable
fun TrainingScheduleSection(
    currentPeriod: SchedulePeriod,
    onPeriodChange: (SchedulePeriod) -> Unit,
    upcomingEventsList: List<ScheduleEvent>,
    onEventClick: (ScheduleEvent) -> Unit,
    onGoToCalendarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .border(1.dp, SpColors.DarkGray.copy(alpha = 0.2f), RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = SpColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painterResource(id = R.drawable.calender_icon),
                        contentDescription = null,
                        tint = SpColors.DarkGray,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Training Schedule",
                        style = SpTypography.titleCard(),
                        color = Color(0xFF1A1A1A),
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.clickable { onGoToCalendarClick() }
                ) {
                    Text(
                        text = "Go to Calendar",
                        style = SpTypography.caption(),
                        color = Color(0xFF1A3550),
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFF1A3550),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Tabs Selector
            PeriodTabSelector(
                selectedPeriod = currentPeriod,
                onPeriodSelected = onPeriodChange
            )

            // Divider Line Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = SpColors.DarkGray.copy(alpha = 0.2f))
                Text(
                    text = "UPCOMING EVENTS",
                    color = Color(0xFF2F5D8A),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = SpColors.DarkGray.copy(alpha = 0.2f))
            }

            // Inner Cards Stacked Container
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                upcomingEventsList.forEach { singleEvent ->
                    ScheduleEventCard(
                        event = singleEvent,
                        onClick = { onEventClick(singleEvent) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PeriodTabSelector(
    selectedPeriod: SchedulePeriod,
    onPeriodSelected: (SchedulePeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFF4F5F7))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SchedulePeriod.values().forEach { period ->
            val isSelected = selectedPeriod == period
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) SpColors.Gold else Color.Transparent)
                    .clickable { onPeriodSelected(period) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = period.displayName,
                    color = if (isSelected) Color.White else SpColors.DarkGray,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ScheduleEventCard(
    event: ScheduleEvent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, SpColors.DarkGray.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SpColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2F5D8A)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = event.iconRes),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = event.title,
                    color = Color(0xFF111111),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp
                )
                Text(
                    text = event.type.uppercase(),
                    color = Color(0xFF2F5D8A),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(text = event.category, style = SpTypography.caption(), color = SpColors.DarkGray)
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1.1f, fill = false)
                    ) {
                        Icon(Icons.Default.Schedule, null, tint = SpColors.DarkGray, modifier = Modifier.size(16.dp))
                        Text(event.timeAndDate, style = SpTypography.caption(), color = SpColors.DarkGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(0.9f, fill = false)
                    ) {
                        Icon(Icons.Default.LocationOn, null, tint = SpColors.DarkGray, modifier = Modifier.size(16.dp))
                        Text(event.location, style = SpTypography.caption(), color = SpColors.DarkGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Person, null, tint = SpColors.DarkGray, modifier = Modifier.size(16.dp))
                    Text(event.instructor, style = SpTypography.caption(), color = SpColors.DarkGray)
                }
            }
        }
    }
}