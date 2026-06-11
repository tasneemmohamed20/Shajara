package com.example.moodlegovapp.presentation.views.dashboard.components

import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moodlegovapp.R
import com.example.moodlegovapp.domain.models.UserProfile
import com.example.moodlegovapp.presentation.components.ProgressIndicator
import com.example.moodlegovapp.ui.theme.AppColors
import com.example.moodlegovapp.ui.theme.SpColors

@Composable
fun XpProgressCard(
    userProfile: UserProfile,
    modifier: Modifier = Modifier
) {
    val animatedLevelProgress by animateFloatAsState(
        targetValue = userProfile.xpProgressPercent / 100f,
        animationSpec = tween(durationMillis = 800, easing = EaseOut),
        label = "levelProgressAnim"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = SpColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            SpColors.DarkGray.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Row Setup
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
//                    .background(Color.Cyan),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SpColors.Gold),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painterResource(R.drawable.militarytech),
                            contentDescription = null,
                            tint = SpColors.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Top Performer",
                            color = Color.Black,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${userProfile.performance.averageGrade}% Average Grade",
                            color = SpColors.DarkGray,
                            fontSize = 12.sp
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(AppColors.Navy)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "LVL ${userProfile.level}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "%,d".format(userProfile.totalXP),
                            color = SpColors.Gold,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = " XP",
                            color = SpColors.DarkGray,
                            fontSize = 12.sp,
//                            modifier = Modifier.padding(bottom = 1.dp)
                        )
                    }

                    // Green Weekly XP Label
                    Box(
                        modifier = Modifier
                            .align(Alignment.End)
                            .clip(RoundedCornerShape(50))
                            .background(AppColors.Success.copy(alpha = 0.1f))
                            .padding(horizontal = 2.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+${userProfile.xpToNextLevel} XP this week",
                            color = Color(0xFF2E7D32),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }


            // XP Slider Sub-container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SpColors.DarkGray.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "Level ${userProfile.level}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111111)
                            )
                            Text("➔", fontSize = 12.sp, color = SpColors.DarkGray)
                            Text(
                                "Level ${userProfile.level + 1}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111111)
                            )
                        }

                        Text(
                            text = "${userProfile.xpToNextLevel} XP to Level ${userProfile.level + 1}",
                            color = SpColors.Gold,
                            fontSize = 12.sp,
                            letterSpacing = 0.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    ProgressIndicator(
                        animatedLevelProgress,
                        "",
                        height = 10
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${userProfile.totalXP} / ${userProfile.totalXP + userProfile.xpToNextLevel} XP",
                            color = SpColors.DarkGray,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${userProfile.xpProgressPercent}%",
                            color = Color(0xFF111111),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun CohortRankCard(
    userProfile: UserProfile,
    onRankCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onRankCardClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SpColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            SpColors.DarkGray.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AppColors.Navy),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userProfile.rankNumber.toString(),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
//                modifier = Modifier.weight(1f),
//                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Rank #${userProfile.rankNumber} in cohort",
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.sp
                )
                Text(
                    text = "${userProfile.xpToNextLevel} reach the Top 10",
                    color = SpColors.DarkGray,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.weight(1f)) // <--- Pushes everything after it to the end

            Box(
                modifier = Modifier
                // Remove .align(Alignment.End) from here
            ) {
                Icon(
                    painter = painterResource(R.drawable.icon_back),
                    contentDescription = null,
                    tint = Color(0xFF111111),
                    modifier = Modifier
                        .size(12.dp)
                        .scale(scaleX = -1f, scaleY = 1f)
                )
            }
        }
    }
}

@Composable
fun DetailedMetricsCard(
    userProfile: UserProfile,
    onViewAllBadgesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = SpColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            SpColors.DarkGray.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Badges Horizontal Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dynamically map up to the first 3 earned badges using network URLs
                    userProfile.badges.take(3).forEach { badge ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .border(2.dp, AppColors.Gold, CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            // Using a fallback placeholder if standard network image composables aren't needed
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.MilitaryTech,
                                contentDescription = badge.name,
                                tint = SpColors.Gold,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.clickable { onViewAllBadgesClick() }
                ) {
                    Text(
                        "View All",
                        color = Color(0xFF2F5D8A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        painter = painterResource(R.drawable.icon_back),
                        contentDescription = null,
                        tint = Color(0xFF111111),
                        modifier = Modifier
                            .size(12.dp)
                            .scale(scaleX = -1f, scaleY = 1f)
                    )
                }
            }

            // Next Objective Locked Track
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.LockOpen,
                    null,
                    tint = SpColors.Gold,
                    modifier = Modifier.size(18.dp)
                )
                Row {
                    Text("Next:", color = SpColors.DarkGray, fontSize = 10.sp)

                    Text(
                        "Elite Performer",
                        color = Color(0xFF111111),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("• Complete 2 more courses", color = SpColors.DarkGray, fontSize = 10.sp)
                }
            }

            // Structural Progress bars Checklist Block mapping Nested Performance Metrics
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                PerformanceMetricRow(
                    label = "Overall Progress",
                    percentage = userProfile.performance.overallProgress,
                    barColor = SpColors.Gold,
                    extraLabel = "+50 XP"
                )

                PerformanceMetricRow(
                    label = "Average Grade",
                    percentage = userProfile.performance.averageGrade,
                    barColor = Color(0xFF2E7D32),
                )

                PerformanceMetricRow(
                    label = "Assignment Completion Rate",
                    percentage = userProfile.performance.taskCompletion,
                    barColor = SpColors.Gold,
//                    extraLabel = "+120 XP this week",
                    extraLabelColor = Color(0xFF2E7D32)
                )
            }
        }
    }
}

@Composable
private fun PerformanceMetricRow(
    label: String,
    percentage: Int,
    barColor: Color,
    extraLabel: String? = null,
    extraLabelColor: Color = SpColors.DarkGray
) {
    val animatedProgress by animateFloatAsState(
        targetValue = percentage / 100f,
        animationSpec = tween(800, easing = EaseOut),
        label = "metricBarAnim"
    )

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = Color(0xFF111111),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (extraLabel != null) {
                    Text(
                        text = extraLabel,
                        color = extraLabelColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Thin
                    )
                }
                Text(
                    text = "$percentage%",
                    color = barColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50)),
            color = barColor,
            trackColor = Color(0xFFF5F5F5),
            gapSize = 0.dp,
            drawStopIndicator = {}
        )
    }
}