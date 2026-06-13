package com.example.moodlegovapp.presentation.views.Profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moodlegovapp.R
import com.example.moodlegovapp.domain.models.UserBadge
import com.example.moodlegovapp.ui.theme.AppColors

@Composable
fun BadgeItemWidget(
    badge: UserBadge, isGoldStyle: Boolean, // Determines if it uses the Gold highlight or Grey tint
    modifier: Modifier = Modifier
) {
    // 1. Dynamic Color Profiles matching the image asset styling
    val badgeBgColor =
        if (isGoldStyle) AppColors.GoldLight.copy(alpha = 0.4f) else AppColors.TextSecondary.copy(
            alpha = 0.15f
        )
    val badgeIconColor = if (isGoldStyle) AppColors.Gold else AppColors.TextSecondary


    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp) // Perfect spacing from image
    ) {
        // Rounded Circle Icon Canvas
        Box(
            modifier = Modifier
                .size(64.dp) // Upsized slightly to match the bold profile look
                .background(badgeBgColor, CircleShape), contentAlignment = Alignment.Center
        ) {
            Icon(
                painterResource(id = R.drawable.shield),
                contentDescription = badge.name,
                tint = badgeIconColor,
                modifier = Modifier.size(28.dp)
            )
        }

        // Label Typography
        Text(
            text = badge.name,
            fontSize = 15.sp, // Upgraded font size slightly for readability
            fontWeight = FontWeight.Normal,
            color = AppColors.TextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 20.sp
        )
    }
}