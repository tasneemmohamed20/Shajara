package com.example.moodlegovapp.presentation.views.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.moodlegovapp.domain.models.LeaderboardEntry
import com.example.moodlegovapp.domain.models.LeaderboardResponse
import com.example.moodlegovapp.ui.theme.AppColors

@Composable
fun DashboardLeaderboardWidget(
    response: LeaderboardResponse?,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(AppColors.Surface, shape = RoundedCornerShape(28.dp))
            .border(1.dp, AppColors.Border, shape = RoundedCornerShape(28.dp))
            .padding(20.dp)
    ) {
        // --- Header Section ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Leaderboard",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )
            TextButton(
                onClick = onViewAllClick,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "View All",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.Navy,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "View All Leaderboard",
                        tint = AppColors.Navy,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // --- Layout States Handling ---
        when {
            response?.data == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.Navy)
                }
            }
            else -> {
                val data = response.data
                val topThree = data.leaderboard.filter { it.rank <= 3 }
                val remainingUsers = data.leaderboard.filter { it.rank > 3 }
                val currentUserEntry = data.leaderboard.find { it.isCurrentUser }

                // Main List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(topThree, key = { it.userId }) { entry ->
                        TopRankCard(entry = entry)
                    }

                    if (topThree.isNotEmpty() && remainingUsers.isNotEmpty()) {
                        item {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = AppColors.Border
                            )
                        }
                    }

                    items(remainingUsers, key = { it.userId }) { entry ->
                        StandardRankRow(entry = entry)
                    }
                }

                // Sticky Current User Bottom Footer Frame
                currentUserEntry?.let { entry ->
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = AppColors.Border
                    )
                    CurrentUserCard(entry = entry)
                }
            }
        }
    }
}

// --- Internal Layout Variations ---

@Composable
private fun TopRankCard(entry: LeaderboardEntry) {
    val rankColor = when (entry.rank) {
        1 -> AppColors.Gold
        2 -> AppColors.Navy
        else -> AppColors.darkBlue
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AppColors.Border, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Image Profile with Rank Badge Badge Overlay
        Box(modifier = Modifier.size(64.dp)) {
            AsyncImage(
                model = entry.profileImageUrl,
                contentDescription = entry.fullName,
                modifier = Modifier
                    .size(58.dp)
                    .align(Alignment.BottomStart)
                    .clip(CircleShape)
                    .border(1.dp, AppColors.Border, CircleShape),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(rankColor, CircleShape)
                    .align(Alignment.TopEnd),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = entry.rank.toString(),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Label Info Columns
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.fullName,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextPrimary
            )
            entry.course?.let {
                Text(
                    text = it,
                    fontSize = 14.sp,
                    color = AppColors.TextSecondary
                )
            }
        }

        // XP Counters
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = String.format("%,d", entry.xp),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (entry.rank == 1) AppColors.Gold else AppColors.darkBlue
            )
            Text(
                text = " XP",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.TextSecondary,
                modifier = Modifier.padding(bottom = 3.dp, start = 2.dp)
            )
        }
    }
}

@Composable
private fun StandardRankRow(entry: LeaderboardEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank Index Circle Indicator
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(AppColors.Background, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = entry.rank.toString(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextSecondary
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Avatar
        AsyncImage(
            model = entry.profileImageUrl,
            contentDescription = entry.fullName,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(14.dp))

        // Name Tag Layout
        Text(
            text = entry.fullName,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.TextPrimary,
            modifier = Modifier.weight(1f)
        )

        // Point Metric
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = String.format("%,d", entry.xp),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )
            Text(
                text = " XP",
                fontSize = 11.sp,
                color = AppColors.TextSecondary,
                modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
            )
        }
    }
}

@Composable
private fun CurrentUserCard(entry: LeaderboardEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AppColors.Border, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(AppColors.Navy, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = entry.rank.toString(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "You are ranked #${entry.rank}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )
            Text(
                text = "Keep going to reach top 10!",
                fontSize = 13.sp,
                color = AppColors.TextSecondary
            )
        }

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = String.format("%,d", entry.xp),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gold
            )
            Text(
                text = " XP",
                fontSize = 11.sp,
                color = AppColors.TextSecondary,
                modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
            )
        }
    }
}