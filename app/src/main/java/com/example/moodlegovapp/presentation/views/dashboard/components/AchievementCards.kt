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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moodlegovapp.domain.models.UserProfile
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(SpColors.Gold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MilitaryTech,
                            contentDescription = null,
                            tint = SpColors.Gold,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = userProfile.rank, // e.g., "Top Performer"
                            color = Color(0xFF111111),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${userProfile.performance.averageGrade}% Average Grade",
                            color = SpColors.DarkGray,
                            fontSize = 14.sp
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1A3550))
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
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = " XP",
                            color = SpColors.DarkGray,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }
            }

            // Green Weekly XP Label
            Box(
                modifier = Modifier
                    .align(Alignment.End)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFE8F8EF))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = userProfile.performance.taskCompletionLabel, // e.g., "+120 XP this week"
                    color = Color(0xFF2E7D32),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // XP Slider Sub-container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SpColors.DarkGray.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "Level ${userProfile.level}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111111)
                            )
                            Text("➔", fontSize = 12.sp, color = SpColors.DarkGray)
                            Text(
                                "Level ${userProfile.level + 1}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111111)
                            )
                        }

                        Text(
                            text = "${userProfile.xpToNextLevel} XP to Level ${userProfile.level + 1}",
                            color = SpColors.Gold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    LinearProgressIndicator(
                        progress = { animatedLevelProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(50)),
                        color = SpColors.Gold,
                        trackColor = Color(0xFFF5F5F5),
                        gapSize = 0.dp,
                        drawStopIndicator = {}
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${userProfile.totalXP} XP Total",
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
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2F5D8A)),
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
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Rank #${userProfile.rankNumber} in cohort",
                    color = Color(0xFF111111),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Keep advancing to reach the Top 10",
                    color = SpColors.DarkGray,
                    fontSize = 14.sp
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                contentDescription = null,
                tint = Color(0xFF111111),
                modifier = Modifier.size(24.dp)
            )
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
            modifier = Modifier.padding(20.dp),
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
                                .background(SpColors.Gold.copy(alpha = 0.15f)),
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
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowRight,
                        null,
                        tint = Color(0xFF2F5D8A),
                        modifier = Modifier.size(16.dp)
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
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Next Badge Target:", color = SpColors.DarkGray, fontSize = 14.sp)
                    Text(
                        "Elite Performer",
                        color = Color(0xFF111111),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Structural Progress bars Checklist Block mapping Nested Performance Metrics
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                PerformanceMetricRow(
                    label = "Overall Progress",
                    percentage = userProfile.performance.overallProgress,
                    barColor = SpColors.Gold,
                    extraLabel = userProfile.performance.overallProgressLabel
                )

                PerformanceMetricRow(
                    label = "Average Grade",
                    percentage = userProfile.performance.averageGrade,
                    barColor = Color(0xFF2E7D32),
                    extraLabel = userProfile.performance.averageGradeLabel
                )

                PerformanceMetricRow(
                    label = "Assignment Completion Rate",
                    percentage = userProfile.performance.taskCompletion,
                    barColor = SpColors.Gold,
                    extraLabel = userProfile.performance.taskCompletionLabel,
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
                fontSize = 15.sp,
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
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = "$percentage%",
                    color = barColor,
                    fontSize = 15.sp,
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