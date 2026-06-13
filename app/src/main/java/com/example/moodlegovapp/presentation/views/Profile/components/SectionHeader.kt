package com.example.moodlegovapp.presentation.views.Profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moodlegovapp.ui.theme.AppColors

@Composable
fun SectionHeader(title: String, onViewAllClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextPrimary
        )
        TextButton(onClick = onViewAllClick) {
            Text(
                text = androidx.compose.ui.res.stringResource(com.example.moodlegovapp.R.string.profile_view_all),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.Navy
            )
        }
    }
}
