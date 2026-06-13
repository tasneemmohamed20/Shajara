package com.example.moodlegovapp.presentation.views.Profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Grading
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.moodlegovapp.R
import com.example.moodlegovapp.domain.models.*
import com.example.moodlegovapp.ui.theme.AppColors
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.moodlegovapp.presentation.viewmodels.ProfileViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onLogOutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val response by viewModel.profileResponse.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val context = LocalContext.current
    val userProfile = response?.data

    when {
        userProfile != null -> {
            ProfileContent(
                userProfile = userProfile,
                onViewAllPerformanceClick = {
                    Toast.makeText(context, "Performance overview details screen coming soon", Toast.LENGTH_SHORT).show()
                },
                onViewAllBadgesClick = {
                    Toast.makeText(context, "Badges screen coming soon", Toast.LENGTH_SHORT).show()
                },
                onViewCertificateClick = { url ->
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Cannot open URL: $url", Toast.LENGTH_SHORT).show()
                    }
                },
                onDownloadCertificateClick = { url ->
                    Toast.makeText(context, "Downloading certificate from: $url", Toast.LENGTH_SHORT).show()
                },
                onEditProfileClick = {
                    Toast.makeText(context, "Edit profile screen coming soon", Toast.LENGTH_SHORT).show()
                },
                onLanguageClick = {
                    viewModel.toggleLanguage(context)
                },
                onNotificationToggle = { enabled ->
                    viewModel.toggleNotifications(enabled)
                },
                onChangePasswordClick = {
                    Toast.makeText(context, "Change password screen coming soon", Toast.LENGTH_SHORT).show()
                },
                onLogOutClick = onLogOutClick,
                onTabClick = {},
                modifier = modifier
            )
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
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 1. Top Header Banner with User Card Summary Info
        item {
            ProfileHeaderBanner(userProfile = userProfile)
        }

        // 2. Performance Overview Module Group
        item {
            SectionHeader(title = "Performance Overview", onViewAllClick = onViewAllPerformanceClick)
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PerformanceItemCard(
                    title = "Overall Progress",
                    label = userProfile.performance.overallProgressLabel,
                    value = "${userProfile.performance.overallProgress}%",
                    icon = Icons.Default.Timeline,
                    iconBg = AppColors.Navy.copy(alpha = 0.1f),
                    iconTint = AppColors.Navy
                )
                PerformanceItemCard(
                    title = "Average Grade",
                    label = userProfile.performance.averageGradeLabel,
                    value = "${userProfile.performance.averageGrade}%",
                    icon = Icons.Default.Grading,
                    iconBg = AppColors.Navy.copy(alpha = 0.1f),
                    iconTint = AppColors.Navy
                )
                PerformanceItemCard(
                    title = "Task Completion",
                    label = userProfile.performance.taskCompletionLabel,
                    value = "${userProfile.performance.taskCompletion}%",
                    icon = Icons.Default.AssignmentTurnedIn,
                    iconBg = AppColors.Navy.copy(alpha = 0.1f),
                    iconTint = AppColors.Navy
                )
            }
        }

        // 3. Horizontal Grid Badges Module Row
        item {
            SectionHeader(title = "Badges", onViewAllClick = onViewAllBadgesClick)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                userProfile.badges.take(3).forEach { badge ->
                    BadgeItemWidget(badge = badge, modifier = Modifier.weight(1f))
                }
                if (userProfile.badges.isEmpty()) {
                    Text(
                        text = "No badges earned yet.",
                        color = AppColors.TextSecondary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }

        // 4. Certificates Expandable Dynamic Block List
        item {
            Text(
                text = "Certificates",
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
                text = "Settings",
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
                    SettingsNavigationRow(
                        title = "Edit Profile",
                        icon = Icons.Default.Person,
                        onClick = onEditProfileClick
                    )
                    HorizontalDivider(color = AppColors.Border, modifier = Modifier.padding(horizontal = 20.dp))
                    SettingsNavigationRow(
                        title = "Language",
                        subtitle = userProfile.settings.language,
                        icon = Icons.Default.Language,
                        onClick = onLanguageClick
                    )
                    HorizontalDivider(color = AppColors.Border, modifier = Modifier.padding(horizontal = 20.dp))
                    SettingsToggleRow(
                        title = "Notifications",
                        isChecked = userProfile.settings.notificationsEnabled,
                        icon = Icons.Default.Notifications,
                        onToggleChange = onNotificationToggle
                    )
                }
            }
        }

        // 6. Account Security Items & Destructive Call Action Buttons Footer
        item {
            Text(
                text = "Security",
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
                SettingsNavigationRow(
                    title = "Change Password",
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
                    Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Log Out", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
        }
    }
}

// --- Top Profile Header Segment ---
@Composable
private fun ProfileHeaderBanner(userProfile: UserProfile) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = AppColors.NavyGradient,
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 28.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = {}) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More options", tint = Color.White)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(86.dp)) {
                    AsyncImage(
                        model = userProfile.profileImageUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(76.dp)
                            .align(Alignment.BottomStart)
                            .clip(CircleShape)
                            .border(2.dp, Color.White, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .background(AppColors.Gold, RoundedCornerShape(8.dp))
                            .border(1.dp, Color.White, RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(text = "LVL ${userProfile.level}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = userProfile.fullName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(text = "${userProfile.role} • ${userProfile.batch}", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))

                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(100.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.School, contentDescription = null, tint = AppColors.GoldLight, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Rank #${userProfile.rankNumber}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Progress tracking
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text("TOTAL XP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.6f))
                        Text(text = String.format("%,d XP", userProfile.totalXP), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "${userProfile.xpToNextLevel} XP", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.GoldLight)
                        Text("to Level ${userProfile.level + 1}", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                    }
                }
                LinearProgressIndicator(
                    progress = { userProfile.xpProgressPercent / 100f },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(100.dp)),
                    color = AppColors.Gold,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
            }
        }
    }
}

// --- Section Header Row helper ---
@Composable
private fun SectionHeader(title: String, onViewAllClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
        Text(
            text = "View All",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.Navy,
            modifier = Modifier.clickable(onClick = onViewAllClick)
        )
    }
}

// --- Overview Metric Layout Card ---
@Composable
private fun PerformanceItemCard(
    title: String,
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconTint: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, AppColors.Border, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp).background(iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                Text(text = label, fontSize = 13.sp, color = AppColors.TextSecondary)
            }
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
        }
    }
}

// --- Badge Item layout Box ---
@Composable
private fun BadgeItemWidget(badge: UserBadge, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.size(56.dp).background(AppColors.GoldLight.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Default.FolderSpecial, contentDescription = null, tint = AppColors.Gold, modifier = Modifier.size(24.dp))
        }
        Text(
            text = badge.name,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.TextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// --- Dynamic Certificate Row/Pending Block Card ---
@Composable
private fun ProfileCertificateCard(
    certificate: UserCertificate,
    onViewClick: (String) -> Unit,
    onDownloadClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().border(1.dp, AppColors.Border, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = certificate.status.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (certificate.isAvailable) AppColors.Gold else AppColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = certificate.courseName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (certificate.isAvailable) Icons.Default.Check else Icons.Default.HourglassEmpty,
                    contentDescription = null,
                    tint = AppColors.TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (certificate.isAvailable) certificate.completedAtFormatted else "Completed ${certificate.completedAtFormatted}",
                    fontSize = 13.sp,
                    color = AppColors.TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (certificate.isAvailable) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { certificate.viewUrl?.let { onViewClick(it) } },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Navy),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("View Certificate", fontSize = 13.sp, color = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                    Button(
                        onClick = { certificate.downloadUrl?.let { onDownloadClick(it) } },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Background),
                        shape = RoundedCornerShape(14.dp),
//                        border = BoxStroke(1.dp, AppColors.Border)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = AppColors.TextSecondary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download", fontSize = 13.sp, color = AppColors.TextSecondary)
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AppColors.Border, RoundedCornerShape(100.dp))
                        .background(AppColors.Background)
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = AppColors.TextSecondary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = certificate.pendingMessage ?: "Available after final approval",
                            fontSize = 13.sp,
                            color = AppColors.TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// --- Menu Interaction Rows helper ---
@Composable
private fun SettingsNavigationRow(title: String, subtitle: String? = null, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(36.dp).background(AppColors.Navy.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = null, tint = AppColors.Navy, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
            if (subtitle != null) {
                Text(text = subtitle, fontSize = 12.sp, color = AppColors.TextSecondary)
            }
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = AppColors.TextSecondary, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun SettingsToggleRow(title: String, isChecked: Boolean, icon: androidx.compose.ui.graphics.vector.ImageVector, onToggleChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(36.dp).background(AppColors.Navy.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = null, tint = AppColors.Navy, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
        Switch(
            checked = isChecked,
            onCheckedChange = onToggleChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AppColors.Navy,
                uncheckedThumbColor = AppColors.TextSecondary,
                uncheckedTrackColor = AppColors.Border
            )
        )
    }
}

// --- Shared Footer Active Tab Navigation Panel ---
@Composable
private fun ProfileBottomNavigationBar(activeTab: String, onTabClick: (String) -> Unit) {
    NavigationBar(
        containerColor = AppColors.Surface,
        tonalElevation = 8.dp,
        modifier = Modifier.border(1.dp, AppColors.Border, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        val tabs = listOf("courses", "tasks", "grades", "profile")
        val labels = listOf("Courses", "Tasks", "Grades", "Profile")
        val icons = listOf(Icons.Default.School, Icons.Default.AssignmentTurnedIn, Icons.Default.BarChart, Icons.Default.Person)

        tabs.forEachIndexed { idx, tab ->
            val isSelected = activeTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabClick(tab) },
                label = { Text(text = labels[idx], fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                icon = { Icon(imageVector = icons[idx], contentDescription = labels[idx]) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AppColors.Navy,
                    selectedTextColor = AppColors.Navy,
                    indicatorColor = AppColors.Navy.copy(alpha = 0.1f),
                    unselectedIconColor = AppColors.TextSecondary,
                    unselectedTextColor = AppColors.TextSecondary
                )
            )
        }
    }
}