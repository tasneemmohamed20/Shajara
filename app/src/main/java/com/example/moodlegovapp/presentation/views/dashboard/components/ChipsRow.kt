package com.example.moodlegovapp.presentation.views.dashboard.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moodlegovapp.ui.theme.AppColors

@Composable
fun TrainingFilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showIcon: Boolean = false
) {
    Surface(
        modifier = modifier
            .height(44.dp) // Set height matching the image proportions
            .clip(CircleShape)
            .clickable { onClick() },
        shape = CircleShape,
        color = if (isSelected) AppColors.Gold else Color.Transparent,
        border = if (isSelected) null else BorderStroke(1.dp, AppColors.Border)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Display the play icon only when selected (as seen on the "Active" chip)
            if (isSelected && showIcon) {
                Icon(
                    imageVector = Icons.Filled.PlayCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = text,
                color = if (isSelected) Color.White else AppColors.TextSecondary,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                letterSpacing = 0.2.sp
            )
        }
    }
}


@Composable
fun TrainingFilterRow(
    selectedFilter: TrainingFilter,
    onFilterChange: (TrainingFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp), // Even gaps matching your design
        verticalAlignment = Alignment.CenterVertically
    ) {
        TrainingFilter.values().forEach { filter ->
            TrainingFilterChip(
                text = filter.displayName,
                isSelected = selectedFilter == filter,
                // Only show the play icon on the "Active" option when selected
                showIcon = filter == TrainingFilter.ACTIVE,
                onClick = { onFilterChange(filter) }
            )
        }
    }
}

enum class TrainingFilter(val displayName: String) {
    ACTIVE("Active"),
    NEW("New"),
    COMPLETED("Completed")
}