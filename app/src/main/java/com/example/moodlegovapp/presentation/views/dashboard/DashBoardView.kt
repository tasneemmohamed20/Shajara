package com.example.moodlegovapp.presentation.views.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.example.moodlegovapp.core.DependencyContainer
import com.example.moodlegovapp.domain.models.Course
import com.example.moodlegovapp.domain.models.User
import com.example.moodlegovapp.ui.theme.SpColors
import com.example.moodlegovapp.ui.theme.SpTypography
import com.example.moodlegovapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    assembly: DependencyContainer,
    onCourseClick: (Int) -> Unit
) {
    val vm = remember { assembly.makeDashboardViewModel() }

    // Collect each StateFlow from the ViewModel individually
    val user            by vm.user.collectAsState()
    val enrolledCourses by vm.enrolledCourses.collectAsState()
    val notifications   by vm.notifications.collectAsState()
    val isLoading       by vm.isLoading.collectAsState()
    val errorMessage    by vm.errorMessage.collectAsState()

    // Derived values — computed from VM state, not from VM directly
    val unreadCount = notifications.count { !it.isRead }

    val pullState = rememberPullToRefreshState()

    LaunchedEffect(Unit) { vm.loadAll() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpColors.LightGray)

    ) {
        if (isLoading && user == null) {
            // ── Loading State ─────────────────────────
            DashboardLoadingState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // ── Header ────────────────────────────
                item {
                    DashboardHeader(
                        user        = user,
                        unreadCount = unreadCount
                    )
                }

                // ── Overall Progress ──────────────────
                item {
                    OverallProgressCard(
                        progress = user?.overallProgress ?: 0,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // ── All Training Programs ─────────────
                item {
                    AllCoursesHeader(count = enrolledCourses.size)
                }

                items(enrolledCourses) { course ->
                    CourseListCard(
                        course   = course,
                        onClick  = { onCourseClick(course.id) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                // ── Error Message ─────────────────────
                errorMessage?.let { err ->
                    item {
                        Text(
                            text     = err,
                            style    = SpTypography.bodySecondary(),
                            color    = SpColors.Error,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                // ── Empty state when loaded but no courses ─
                if (!isLoading && enrolledCourses.isEmpty() && errorMessage == null) {
                    item {
                        EmptyStateView(
                            message  = stringResource(R.string.login_welcome_title),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }

    }
}

// ─────────────────────────────────────────────
// DASHBOARD HEADER
// mirrors iOS DashboardHeaderView
// ─────────────────────────────────────────────

@Composable
private fun DashboardHeader(user: User?, unreadCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF2F5D8A), Color(0xFF1A3550))
                )
            )
            .padding(horizontal = 16.dp)
            .padding(top = 52.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar circle
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = user?.fullName ?: "Loading...",
                    style      = SpTypography.titleCard(),
                    color      = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text  = user?.role ?: "",
                    style = SpTypography.caption(),
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            // Notification bell with badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint     = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(SpColors.Error)
                            .align(Alignment.TopEnd)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// OVERALL PROGRESS CARD
// ─────────────────────────────────────────────

@Composable
private fun OverallProgressCard(progress: Int, modifier: Modifier = Modifier) {
    val animatedProgress by animateFloatAsState(
        targetValue    = progress / 100f,
        animationSpec  = tween(durationMillis = 800, easing = EaseOut),
        label          = "progress"
    )

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = SpColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text          = stringResource(R.string.dashboard_overall_progress),
                    style         = SpTypography.labelCategory(),
                    color         = SpColors.DarkGray,
                    letterSpacing = 1.sp
                )
                Text(
                    text       = "$progress%",
                    style      = SpTypography.label(),
                    color      = SpColors.NavyBlue,
                    fontWeight = FontWeight.SemiBold
                )
            }
            LinearProgressIndicator(
                progress   = { animatedProgress },
                modifier   = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color      = SpColors.Gold,
                trackColor = SpColors.ProgressBg
            )
        }
    }
}

// ─────────────────────────────────────────────
// ALL COURSES HEADER
// ─────────────────────────────────────────────

@Composable
private fun AllCoursesHeader(count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text       = stringResource(R.string.dashboard_all_programs),
            style      = SpTypography.titleCard(),
            color      = SpColors.DarkBrown,
            fontWeight = FontWeight.Bold
        )
        Text(
            text  = "$count ${stringResource(R.string.dashboard_enrolled)}",
            style = SpTypography.caption(),
            color = SpColors.DarkGray
        )
    }
}

// ─────────────────────────────────────────────
// COURSE LIST CARD
// mirrors iOS CourseListCard
// ─────────────────────────────────────────────

@Composable
private fun CourseListCard(
    course: Course,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue   = course.progress / 100f,
        animationSpec = tween(800, easing = EaseOut),
        label         = "cardProgress"
    )

    Card(
        modifier  = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = SpColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            // Image area with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF2F5D8A), Color(0xFF1A3550)))
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint     = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text     = course.instructorName,
                            style    = SpTypography.caption(),
                            color    = Color.White.copy(alpha = 0.9f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text  = course.dueIn,
                        style = SpTypography.caption(),
                        color = SpColors.Warning
                    )
                }
            }

            // Course info
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text       = course.title,
                    style      = SpTypography.label(),
                    color      = SpColors.DarkBrown,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LinearProgressIndicator(
                        progress   = { animatedProgress },
                        modifier   = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color      = SpColors.Gold,
                        trackColor = SpColors.ProgressBg
                    )
                    Text(
                        text     = "${course.progress}%",
                        style    = SpTypography.caption(),
                        color    = SpColors.DarkGray,
                        modifier = Modifier.width(36.dp)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// EMPTY & LOADING STATES
// ─────────────────────────────────────────────

@Composable
fun EmptyStateView(message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            Icons.Default.Inbox,
            contentDescription = null,
            tint     = SpColors.DarkGray.copy(alpha = 0.4f),
            modifier = Modifier.size(48.dp)
        )
        Text(
            text  = message,
            style = SpTypography.bodyPrimary(),
            color = SpColors.DarkGray
        )
    }
}

@Composable
fun DashboardLoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color    = SpColors.NavyBlue,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text  = stringResource(R.string.loading),
                style = SpTypography.bodyPrimary(),
                color = SpColors.DarkGray
            )
        }
    }
}