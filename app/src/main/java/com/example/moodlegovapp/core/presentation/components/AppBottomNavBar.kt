package com.example.moodlegovapp.core.presentation.components
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.moodlegovapp.core.presentation.navigation.BottomNavTab
import com.example.moodlegovapp.ui.theme.AppColors
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember


@Composable
fun AppBottomNavBar(
    selectedTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit
) {
    NavigationBar(
        containerColor = AppColors.Surface,
        tonalElevation = 0.dp
    ) {
        BottomNavTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text(tab.label, style = MaterialTheme.typography.bodyMedium) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AppColors.Navy,
                    selectedTextColor = AppColors.Navy,
                    unselectedIconColor = AppColors.TextSecondary,
                    unselectedTextColor = AppColors.TextSecondary,
                    indicatorColor = AppColors.Navy.copy(alpha = 0.08f)
                )
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = AppColors.TextPrimary
        )
        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Navy,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickableWithoutRipple(onAction)
                    .padding(4.dp)
            )
        }
    }
}
@Composable
fun Modifier.clickableWithoutRipple(onClick: () -> Unit): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    return this.clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick
    )
}