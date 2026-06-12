package com.example.moodlegovapp.presentation.views.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.res.stringResource
import com.example.moodlegovapp.R
import com.example.moodlegovapp.domain.models.LeaderboardData
import com.example.moodlegovapp.domain.models.LeaderboardEntry
import com.example.moodlegovapp.ui.theme.AppColors

@Composable
fun DashboardLeaderboardWidget(
    leaderboard: LeaderboardData?,
    isLoading: Boolean,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AppColors.Surface, shape = RoundedCornerShape(28.dp))
            .border(1.dp, AppColors.Border, shape = RoundedCornerShape(28.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.dashboard_leaderboard),
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
                        text = stringResource(R.string.dashboard_view_all),
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

        when {
            isLoading && leaderboard == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.Navy)
                }
            }

            leaderboard == null || leaderboard.leaderboard.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.dashboard_no_leaderboard),
                        fontSize = 14.sp,
                        color = AppColors.TextSecondary
                    )
                }
            }

            else -> {
                val topThree = leaderboard.leaderboard.filter { it.rank <= 3 }
                val currentUserEntry = leaderboard.leaderboard.find { it.isCurrentUser }
                val remainingUsers = leaderboard.leaderboard.filter { it.rank > 3 && !it.isCurrentUser }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    topThree.forEach { entry ->
                        TopRankCard(entry = entry)
                    }

                    if (topThree.isNotEmpty() && remainingUsers.isNotEmpty()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = AppColors.Border
                        )
                    }

                    remainingUsers.forEach { entry ->
                        StandardRankRow(entry = entry)
                    }
                }

                currentUserEntry?.takeIf { it.rank > 3 }?.let { entry ->
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = AppColors.Border
                    )
                    CurrentUserCard(
                        entry = entry,
                        totalParticipants = leaderboard.totalParticipants
                    )
                }
            }
        }
    }
}

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
            .border(2.dp, AppColors.Border, RoundedCornerShape(20.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(48.dp)
        ) {
            AsyncImage(
//                model = entry.profileImageUrl,
                model = "https://m.media-amazon.com/images/I/615JjV818kL._AC_SL1500_.jpg",
                contentDescription = entry.fullName,
                modifier = Modifier
                    .size(40.dp)
                    // Align it to the bottom-left so the badge can neatly anchor over the top-right
                    .align(Alignment.BottomStart)
                    .clip(CircleShape)
                    .border(1.dp, AppColors.Border, CircleShape),
                contentScale = ContentScale.Crop
            )

            // The Rank Badge
            Box(
                modifier = Modifier
                    .size(18.dp) // Slightly decreased badge size from 22.dp to match the smaller 40.dp avatar scale balance
                    .background(rankColor, CircleShape)
                    .align(Alignment.TopEnd),
                contentAlignment = Alignment.Center // Center ensures text is perfectly dead-center vertically and horizontally
            ) {
                Text(
                    text = entry.rank.toString(),
                    color = Color.White,
                    fontSize = 10.sp, // Dropped to 10.sp so digits don't clip inside the 18.dp container
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.fullName,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            entry.course?.let {
                Text(
                    text = it,
                    fontSize = 10.sp,
                    color = AppColors.TextSecondary
                )
            }
        }

        Text(
            text = "${String.format("%,d", entry.xp)} XP",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (entry.rank == 1) AppColors.Gold else AppColors.darkBlue
        )
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
        Box(
            modifier = Modifier
                .size(20.dp)
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

        AsyncImage(
//            model = entry.profileImageUrl,
            model = "https://m.media-amazon.com/images/I/615JjV818kL._AC_SL1500_.jpg",
            contentDescription = entry.fullName,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = entry.fullName,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.TextPrimary,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "${String.format("%,d", entry.xp)} XP",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextPrimary
        )
    }
}

@Composable
private fun CurrentUserCard(
    entry: LeaderboardEntry,
    totalParticipants: Int
) {
    val rankMessage = when {
        entry.rank <= 10 -> stringResource(R.string.leaderboard_you_top10)
        else -> stringResource(R.string.leaderboard_keep_going)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AppColors.Border, RoundedCornerShape(20.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(AppColors.Navy, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = entry.rank.toString(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.leaderboard_you_ranked, entry.rank),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )
            Text(
                text = rankMessage,
                fontSize = 11.sp,
                color = AppColors.TextSecondary
            )

        }

        Text(
            text = "${String.format("%,d", entry.xp)} XP",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.Gold
        )
    }
}
