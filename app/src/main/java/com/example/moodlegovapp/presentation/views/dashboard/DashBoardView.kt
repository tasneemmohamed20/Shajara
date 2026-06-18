package com.example.moodlegovapp.presentation.views.dashboard

import android.util.Log
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.moodlegovapp.R
import com.example.moodlegovapp.core.DependencyContainer
import com.example.moodlegovapp.domain.models.UserProfile
import com.example.moodlegovapp.presentation.components.ProgressIndicator
import com.example.moodlegovapp.presentation.views.dashboard.components.CohortRankCard
import com.example.moodlegovapp.presentation.views.dashboard.components.CompletedCourseCard
import com.example.moodlegovapp.presentation.views.dashboard.components.ContinueTrainingSectionCard
import com.example.moodlegovapp.presentation.views.dashboard.components.CourseListCard
import com.example.moodlegovapp.presentation.views.dashboard.components.DashboardLeaderboardWidget
import com.example.moodlegovapp.presentation.views.dashboard.components.DashboardMetricsRow
import com.example.moodlegovapp.presentation.views.dashboard.components.DetailedMetricsCard
import com.example.moodlegovapp.presentation.views.dashboard.components.ScheduleEvent
import com.example.moodlegovapp.presentation.views.dashboard.components.SchedulePeriod
import com.example.moodlegovapp.presentation.views.dashboard.components.SectionHeader
import com.example.moodlegovapp.presentation.views.dashboard.components.TrainingFilter
import com.example.moodlegovapp.presentation.views.dashboard.components.TrainingFilterRow
import com.example.moodlegovapp.presentation.views.dashboard.components.TrainingScheduleSection
import com.example.moodlegovapp.presentation.views.dashboard.components.XpProgressCard
import com.example.moodlegovapp.ui.theme.AppColors
import com.example.moodlegovapp.ui.theme.SpColors
import com.example.moodlegovapp.ui.theme.SpTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    assembly: DependencyContainer,
    onCourseClick: (Int, String, Int) -> Unit,
    onLeaderboardClick: () -> Unit,

) {
    val vm = remember { assembly.makeDashboardViewModel() }

    // Collect each StateFlow from the ViewModel individually
    val user by vm.user.collectAsState()
    val enrolledCourses by vm.enrolledCourses.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val errorMessage by vm.errorMessage.collectAsState()
    val unreadCount by vm.unreadCount.collectAsState()
    val profileUrl = R.drawable.avatar
    var searchQuery by remember { mutableStateOf("") }
    var currentFilter by remember { mutableStateOf(TrainingFilter.ACTIVE) }
    val pullState = rememberPullToRefreshState()
    var activeSchedulePeriod by remember { mutableStateOf(SchedulePeriod.TODAY) }

    val dueActivities = enrolledCourses.size - (user?.performance?.overallProgress ?: 10)
    val rawNotifications by vm.notifications.collectAsState()

    val formattedEvents = remember(rawNotifications) {
        rawNotifications.map { notif ->
            val resourceId = when (notif.iconType.lowercase()) {
                "assignment"  -> R.drawable.ic_tasks
                "certificate" -> R.drawable.ic_completed
                "achievement" -> R.drawable.ic_completed
                "course"      -> R.drawable.ic_courses
                else          -> R.drawable.notification_icon
            }
            val timeAndDate = when {
                notif.sessionDate.isNotBlank() && notif.sessionTime.isNotBlank() ->
                    "${notif.sessionDate} · ${notif.sessionTime}"
                notif.sessionDate.isNotBlank() -> notif.sessionDate
                else -> notif.createdAtFormatted
            }
            ScheduleEvent(
                id          = notif.id,
                title       = notif.title,
                type        = notif.notificationType,
                category    = notif.shortBody.ifBlank { notif.body },
                timeAndDate = timeAndDate,
                location    = notif.location,
                instructor  = "Maj. Ahmed Al-Mansouri",
                iconRes     = resourceId
            )
        }
    }

    val leaderboardData by vm.leaderboard.collectAsState()
    val completedCertificates = remember(user?.certificates) {
        user?.certificates?.take(3).orEmpty()
    }

    val onLeaderboardClick = {
        // Navigate to full leaderboard screen
        // e.g., navController.navigate("leaderboard")
    }

    LaunchedEffect(Unit) {
        vm.loadAll()
        user?.let { Log.i("dashboard", it.profileImageUrl) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpColors.LightGray)

    ) {
        if (isLoading && user == null) {
            DashboardLoadingState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // ── Header ────────────────────────────
                item {
                    DashboardHeader(
                        user = user, unreadCount = unreadCount, profileUrl = profileUrl
                    )
                }

                item {
                    TrainingSearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                    )
                }

                item {
                    TrainingFilterRow(
                        selectedFilter = currentFilter, onFilterChange = { clickedFilter ->
                            currentFilter = clickedFilter
                            // TODO: Trigger any viewmodel filter updates here
                        })
                }

                item {
                    ContinueTrainingSectionCard(
                        enrolledCourses = enrolledCourses, onCourseClick = { selectedCourse ->
                            onCourseClick(selectedCourse.id, selectedCourse.fullName ?: "", selectedCourse.progress ?: 0)
                        }, modifier = Modifier.padding(16.dp)
                    )
                }

                item {
                    DashboardMetricsRow(
                        activeCoursesCount = enrolledCourses.size,
                        dueActivitiesCount = dueActivities,
                        completedCount = user?.performance?.overallProgress ?: 10,
//                        modifier =  Modifier.padding(16.dp)
                    )
                }


                item {
                    TrainingScheduleSection(
                        currentPeriod = activeSchedulePeriod,
                        onPeriodChange = { activeSchedulePeriod = it },
                        upcomingEventsList = formattedEvents,
                        onEventClick = { },
                        onGoToCalendarClick = { /* Handle Navigation Intent */ },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // ── All Training Programs ─────────────
                item {
                    SectionHeader(title = stringResource(R.string.dashboard_all_programs), count = enrolledCourses.size)
                }

                items(enrolledCourses) { course ->
                    CourseListCard(
                        course = course,
                        onClick = { onCourseClick(course.id, course.fullName ?: "", course.progress ?: 0) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                item {
                    SectionHeader(stringResource(R.string.dashboard_achievements))
                }

                user?.let { userProfile -> // Ensures your UserProfile data isn't null before rendering
                    item {
                        XpProgressCard(
                            userProfile = userProfile,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    item {
                        Spacer(Modifier.height(16.dp))
                        CohortRankCard(
                            userProfile = userProfile,
                            onRankCardClick = {
                                // Navigate to your Leaderboard screen here
                                // e.g., navController.navigate("leaderboard")
                            },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    item {
                        Spacer(Modifier.height(16.dp))
                        DetailedMetricsCard(
                            userProfile = userProfile,
                            onViewAllBadgesClick = {
                                // Navigate to all badges grid
                            },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                item {
                    Spacer(Modifier.height(16.dp))
                    DashboardLeaderboardWidget(
                        leaderboard = leaderboardData,
                        isLoading = isLoading && leaderboardData == null,
                        onViewAllClick = onLeaderboardClick,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                }

                item {
                    SectionHeader(
                        title = stringResource(R.string.completed),
                        count = user?.certificates?.size,
                        color = AppColors.Success
                    )
                }

                if (completedCertificates.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.dashboard_no_completed),
                            style = SpTypography.bodySecondary(),
                            color = AppColors.TextSecondary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                } else {
                    items(completedCertificates, key = { it.id }) { certificate ->
                        CompletedCourseCard(
                            certificate = certificate,
                            onViewCertificateClick = { /* Open certificate URL */ },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
                // ── Error Message ─────────────────────
                errorMessage?.let { err ->
                    item {
                        Text(
                            text = err,
                            style = SpTypography.bodySecondary(),
                            color = SpColors.Error,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                // ── Empty state when loaded but no courses ─
                if (!isLoading && enrolledCourses.isEmpty() && errorMessage == null) {
                    item {
                        EmptyStateView(
                            message = stringResource(R.string.login_welcome_title),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }

    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun DashboardHeader(user: UserProfile?, unreadCount: Int, profileUrl: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF2F5D8A), Color(0xFF1A3550))
                )
            )
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 16.dp)
    ) {
        Column {
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
                        .border(2.dp, AppColors.Gold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    @OptIn(ExperimentalGlideComposeApi::class) GlideImage(
                        model = user?.profileImageUrl,
                        contentDescription = "User Profile Picture",
                        modifier = Modifier.size(48.dp),
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user?.fullName ?: stringResource(R.string.dashboard_loading_name),
                        style = SpTypography.titleCard(),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = user?.role ?: "",
                        style = SpTypography.caption(),
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                Box(
                    modifier = Modifier.wrapContentSize()
                ) {
                    // 1. Main Circular Icon Button
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .clickable { /* Handle click */ }, contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painterResource(R.drawable.notification_icon),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(AppColors.Gold)
                                .align(Alignment.TopEnd)
                                .offset(x = (1).dp, y = (-1).dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            OverallProgressCard(
                progress = user?.performance?.overallProgress ?: 0,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────
// OVERALL PROGRESS CARD
// ─────────────────────────────────────────────

@Composable
private fun OverallProgressCard(progress: Int, modifier: Modifier = Modifier) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress / 100f,
        animationSpec = tween(durationMillis = 800, easing = EaseOut),
        label = "progress"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f),
            contentColor = Color.Transparent,
        ),
        border = BorderStroke(1.dp, Color.White)
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(
                text = stringResource(R.string.dashboard_overall_progress),
                style = SpTypography.bodySecondary(),
                color = SpColors.White,
                letterSpacing = 1.sp
            )


            ProgressIndicator(animatedProgress, "$progress%")
        }
    }
}


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
            tint = SpColors.DarkGray.copy(alpha = 0.4f),
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = message, style = SpTypography.bodyPrimary(), color = SpColors.DarkGray
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
                color = SpColors.NavyBlue, modifier = Modifier.size(48.dp)
            )
            Text(
                text = stringResource(R.string.loading),
                style = SpTypography.bodyPrimary(),
                color = SpColors.DarkGray
            )
        }
    }
}

@Composable
fun TrainingSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(R.string.dashboard_search_hint)
) {
    val focusManager = LocalFocusManager.current

//    Spacer(Modifier.height(16.dp))
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp) // Perfect proportions matching the image
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, AppColors.Border, RoundedCornerShape(14.dp))
            .background(AppColors.Surface)
            .padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        // Search Icon
        Icon(
            painterResource(R.drawable.search_icon),
            contentDescription = "Search Icon",
            tint = AppColors.TextSecondary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Text Field Input Area
        Box(
            modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart
        ) {
            // Placeholder management
            if (query.isEmpty()) {
                Text(
                    text = placeholder,
                    color = AppColors.TextSecondary,
                    fontSize = 15.sp,
                    letterSpacing = 0.3.sp
                )
            }

            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = TextStyle(
                    color = AppColors.TextPrimary, fontSize = 15.sp, letterSpacing = 0.3.sp
                ),
                singleLine = true,
                cursorBrush = SolidColor(AppColors.Navy),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus() // Closes keyboard on search click
                    }),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}