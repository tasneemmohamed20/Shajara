package com.example.moodlegovapp.presentation.views.Profile

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moodlegovapp.R
import com.example.moodlegovapp.domain.models.UserProfile
import com.example.moodlegovapp.presentation.viewmodels.ProfileViewModel
import com.example.moodlegovapp.presentation.views.Profile.components.BadgeItemWidget
import com.example.moodlegovapp.presentation.views.Profile.components.PerformanceItemCard
import com.example.moodlegovapp.presentation.views.Profile.components.ProfileCertificateCard
import com.example.moodlegovapp.presentation.views.Profile.components.ProfileHeaderBanner
import com.example.moodlegovapp.presentation.views.Profile.components.SectionHeader
import com.example.moodlegovapp.presentation.views.Profile.components.SettingsRow
import com.example.moodlegovapp.presentation.views.Profile.components.SettingsToggleRow
import com.example.moodlegovapp.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onLogOutClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showBadgesSheet by remember { mutableStateOf(false) }
    var showLanguageSheet by remember { mutableStateOf(false) }

    val response by viewModel.profileResponse.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val context = LocalContext.current
    val userProfile = response?.data

    val downloadMessage = stringResource(
        id = R.string.profile_downloading_certificate,
    )
    val editProfileMessage = stringResource(
        id = R.string.profile_edit_coming_soon,
    )
    val changePasswordMessage = stringResource(
        id = R.string.profile_change_password_coming_soon,
    )
    val performanceOverviewMessage = stringResource(
        id = R.string.profile_performance_overview_coming_soon,
    )
    val urlErrorMessage = stringResource(
        id = R.string.profile_cannot_open_url,
    )

    when {
        userProfile != null -> {
            ProfileContent(
                userProfile = userProfile,
                onViewAllPerformanceClick = {
                    Toast.makeText(
                        context, performanceOverviewMessage, Toast.LENGTH_SHORT
                    ).show()
                },
                onViewAllBadgesClick = {
                    showBadgesSheet = true
                },
                onViewCertificateClick = { url ->
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        Toast.makeText(
                            context, urlErrorMessage, Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                onDownloadCertificateClick = { url ->
                    Toast.makeText(
                        context, downloadMessage, Toast.LENGTH_SHORT
                    ).show()
                },
                onEditProfileClick = {
                    Toast.makeText(
                        context, editProfileMessage, Toast.LENGTH_SHORT
                    ).show()
                },
                onLanguageClick = {
                    showLanguageSheet = true
                },
                onNotificationToggle = { enabled ->
                    viewModel.toggleNotifications(enabled)
                },
                onChangePasswordClick = {
                    Toast.makeText(
                        context, changePasswordMessage, Toast.LENGTH_SHORT
                    ).show()
                },
                onLogOutClick = onLogOutClick,
                onTabClick = {},
                modifier = modifier,
                onBackClick = onBackClick
            )

            if (showBadgesSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showBadgesSheet = false }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.profile_all_badges),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        LazyColumn {
                            items(userProfile.badges) { badge ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(AppColors.Surface, CircleShape)
                                            .border(1.dp, AppColors.Border, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = badge.name,
                                            tint = AppColors.Gold,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = badge.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = AppColors.TextPrimary
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }

            if (showLanguageSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showLanguageSheet = false }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.profile_select_language),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        val isEn = userProfile.settings.language.lowercase() == "en"
                        val isAr = userProfile.settings.language.lowercase() == "ar"

                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setLanguage(context, "en")
                                showLanguageSheet = false
                            }
                            .padding(vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(R.string.profile_english),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isEn) AppColors.Gold else AppColors.TextPrimary
                            )
                            if (isEn) {
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = AppColors.Gold
                                )
                            }
                        }

                        HorizontalDivider(color = AppColors.Border)

                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setLanguage(context, "ar")
                                showLanguageSheet = false
                            }
                            .padding(vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(R.string.profile_arabic),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isAr) AppColors.Gold else AppColors.TextPrimary
                            )
                            if (isAr) {
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = AppColors.Gold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

            }
        }

        isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.Navy)
            }
        }

        errorMessage != null -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(AppColors.Background)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = errorMessage!!,
                    color = AppColors.TextSecondary,
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.loadAll() },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Navy)
                ) {
                    Text(stringResource(R.string.error_retry), color = Color.White)
                }
            }
        }

        else -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.Navy)
            }
        }
    }
}

@Composable
private fun ProfileContent(
    userProfile: UserProfile,
    onViewAllPerformanceClick: () -> Unit,
    onViewAllBadgesClick: () -> Unit,
    onViewCertificateClick: (url: String) -> Unit,
    onDownloadCertificateClick: (url: String) -> Unit,
    onEditProfileClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onBackClick: () -> Unit,
    onNotificationToggle: (Boolean) -> Unit,
    onChangePasswordClick: () -> Unit,
    onLogOutClick: () -> Unit,
    onTabClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Header Banner with User Card Summary Info
        item {
            ProfileHeaderBanner(userProfile = userProfile, onBackClick)
        }

        // 2. Performance Overview Module Group
        item {
            SectionHeader(
                title = stringResource(R.string.profile_performance_overview),
                onViewAllClick = onViewAllPerformanceClick
            )
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PerformanceItemCard(
                    title = stringResource(R.string.profile_overall_progress),
                    label = userProfile.performance.overallProgressLabel,
                    value = "${userProfile.performance.overallProgress}%",
                    icon = R.drawable.progress_icon,
                    iconBg = AppColors.Navy,
                    iconTint = Color.White
                )
                PerformanceItemCard(
                    title = stringResource(R.string.profile_average_grade),
                    label = userProfile.performance.averageGradeLabel,
                    value = "${userProfile.performance.averageGrade}%",
                    icon = R.drawable.avg_grade,
                    iconBg = AppColors.Navy,
                    iconTint = Color.White
                )
                PerformanceItemCard(
                    title = stringResource(R.string.profile_task_completion),
                    label = userProfile.performance.taskCompletionLabel,
                    value = "${userProfile.performance.taskCompletion}%",
                    icon = R.drawable.task_completion,
                    iconBg = AppColors.Navy,
                    iconTint = Color.White
                )
            }
        }

        // 3. Horizontal Grid Badges Module Row
//        item {
//            SectionHeader(title = stringResource(R.string.profile_badges), onViewAllClick = onViewAllBadgesClick)
//            Spacer(modifier = Modifier.height(12.dp))
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 20.dp),
//                horizontalArrangement = Arrangement.spacedBy(16.dp)
//            ) {
//                userProfile.badges.take(3).forEach { badge ->
//                    BadgeItemWidget(badge = badge, modifier = Modifier.weight(1f))
//                }
//                if (userProfile.badges.isEmpty()) {
//                    Text(
//                        text = stringResource(R.string.profile_no_badges),
//                        color = AppColors.TextSecondary,
//                        fontSize = 14.sp,
//                        modifier = Modifier.padding(vertical = 8.dp)
//                    )
//                }
//            }
//        }

        item {
            SectionHeader(
                title = stringResource(R.string.profile_badges),
                onViewAllClick = onViewAllBadgesClick
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Wrap the badges row inside a stylized layout Card container matching the image spec
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .border(1.dp, AppColors.Border, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 24.dp, vertical = 24.dp
                        ), // Comfortable internal padding
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    if (userProfile.badges.isEmpty()) {
                        Text(
                            text = stringResource(R.string.profile_no_badges),
                            color = AppColors.TextSecondary,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        userProfile.badges.take(3).forEachIndexed { index, badge ->
                            BadgeItemWidget(
                                badge = badge,
                                // First item (index 0) gets the Gold highlight style; others get Grey
                                isGoldStyle = index == 0, modifier = Modifier.weight(1f)
                            )
                        }

                        // If there are fewer than 3 items, add invisible Spacers to maintain alignment
                        val emptySlots = 3 - userProfile.badges.take(3).size
                        repeat(emptySlots) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // 4. Certificates Expandable Dynamic Block List
        item {
            Text(
                text = stringResource(R.string.profile_certificates),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
        items(userProfile.certificates, key = { it.id }) { certificate ->
            ProfileCertificateCard(
                certificate = certificate,
                onViewClick = onViewCertificateClick,
                onDownloadClick = onDownloadCertificateClick,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        // 5. App Settings Controls Menu Block
        item {
            Text(
                text = stringResource(R.string.profile_settings),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .border(1.dp, AppColors.Border, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column {
                    SettingsRow(
                        title = stringResource(R.string.profile_edit_profile),
                        icon = Icons.Outlined.Person,
                        onClick = onEditProfileClick
                    )
                    HorizontalDivider(
                        color = AppColors.Border, modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    SettingsRow(
                        title = stringResource(R.string.profile_language),
                        subtitle = userProfile.settings.language,
                        icon = Icons.Outlined.Language,
                        onClick = onLanguageClick
                    )
                    HorizontalDivider(
                        color = AppColors.Border, modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    SettingsToggleRow(
                        title = stringResource(R.string.profile_notifications),
                        isChecked = userProfile.settings.notificationsEnabled,
                        icon = Icons.Outlined.Notifications,
                        onToggleChange = onNotificationToggle
                    )
                }
            }
        }

        // 6. Account Security Items & Destructive Call Action Buttons Footer
        item {
            Text(
                text = stringResource(R.string.profile_security),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .border(1.dp, AppColors.Border, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                shape = RoundedCornerShape(24.dp)
            ) {
                SettingsRow(
                    title = stringResource(R.string.profile_change_password),
                    icon = Icons.Default.Security,
                    onClick = onChangePasswordClick
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onLogOutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Gold),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.profile_log_out),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
