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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.moodlegovapp.R
import com.example.moodlegovapp.core.DependencyContainer
import com.example.moodlegovapp.domain.models.Course
import com.example.moodlegovapp.domain.models.NotificationType
import com.example.moodlegovapp.domain.models.User
import com.example.moodlegovapp.presentation.components.ProgressIndicator
import com.example.moodlegovapp.presentation.views.dashboard.components.ContinueTrainingSectionCard
import com.example.moodlegovapp.presentation.views.dashboard.components.DashboardMetricsRow
import com.example.moodlegovapp.presentation.views.dashboard.components.ScheduleEvent
import com.example.moodlegovapp.presentation.views.dashboard.components.SchedulePeriod
import com.example.moodlegovapp.presentation.views.dashboard.components.TrainingFilter
import com.example.moodlegovapp.presentation.views.dashboard.components.TrainingFilterRow
import com.example.moodlegovapp.presentation.views.dashboard.components.TrainingScheduleSection
import com.example.moodlegovapp.ui.theme.AppColors
import com.example.moodlegovapp.ui.theme.SpColors
import com.example.moodlegovapp.ui.theme.SpTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    assembly: DependencyContainer, onCourseClick: (Int) -> Unit
) {
    val vm = remember { assembly.makeDashboardViewModel() }

    // Collect each StateFlow from the ViewModel individually
    val user by vm.user.collectAsState()
    val enrolledCourses by vm.enrolledCourses.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val errorMessage by vm.errorMessage.collectAsState()
    val unreadCount by vm.unreadCount.collectAsState()
    val profileUrl = "https://m.media-amazon.com/images/I/615JjV818kL._AC_SL1500_.jpg"
    var searchQuery by remember { mutableStateOf("") }
    var currentFilter by remember { mutableStateOf(TrainingFilter.ACTIVE) }
    val pullState = rememberPullToRefreshState()
    var activeSchedulePeriod by remember { mutableStateOf(SchedulePeriod.TODAY) }

    val dueActivities = enrolledCourses.size - (user?.overallProgress ?: 10)
    val rawNotifications by vm.notifications.collectAsState()

    // Map database notifications into explicit view components models cleanly
    val formattedEvents = remember(rawNotifications) {
        rawNotifications.map { notif ->
            val resourceId = when (notif.type) {
                NotificationType.ASSIGNMENT -> R.drawable.ic_tasks
                NotificationType.CERTIFICATE -> R.drawable.ic_completed
                NotificationType.ACHIEVEMENT -> R.drawable.ic_completed
                NotificationType.COURSE -> R.drawable.ic_courses
                NotificationType.SCHEDULE -> R.drawable.notification_icon
            }
            ScheduleEvent(
                id = notif.id,
                title = notif.title,
                type = notif.type.name.replace('_', ' '),
                category = notif.message,
                timeAndDate = "Today, 10:00 AM", // Replace with raw time format if needed
                location = "Room 204",            // Fallback context mapping
                instructor = "Lt. Al-Nuaimi",     // Fallback context mapping
                iconRes = resourceId
            )
        }
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
                            onCourseClick(selectedCourse.id)
                        }, modifier = Modifier.padding(16.dp)
                    )
                }

                item {
                    DashboardMetricsRow(
                        activeCoursesCount = enrolledCourses.size,
                        dueActivitiesCount = dueActivities,
                        completedCount = user?.overallProgress ?: 10,
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
                    AllCoursesHeader(count = enrolledCourses.size)
                }

                items(enrolledCourses) { course ->
                    CourseListCard(
                        course = course,
                        onClick = { onCourseClick(course.id) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
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

// ─────────────────────────────────────────────
// DASHBOARD HEADER
// mirrors iOS DashboardHeaderView
// ─────────────────────────────────────────────

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun DashboardHeader(user: User?, unreadCount: Int, profileUrl: String) {
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
                        model = profileUrl,
                        contentDescription = "User Profile Picture",
                        modifier = Modifier.size(48.dp),
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user?.fullName ?: "Loading...",
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
                progress = user?.overallProgress ?: 0,
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // The vertical navy blue indicator line
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(Color(0xFF1A3550))
            )

            Text(
                text = stringResource(R.string.dashboard_all_programs),
                style = SpTypography.titleCard(),
                color = SpColors.DarkBrown,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = "$count ${stringResource(R.string.dashboard_enrolled)}",
            style = SpTypography.caption(),
            color = SpColors.DarkGray.copy(alpha = 0.8f) // Soft gray tint matching the design asset
        )
    }
}

// ─────────────────────────────────────────────
// COURSE LIST CARD
// mirrors iOS CourseListCard
// ─────────────────────────────────────────────

@Composable
private fun CourseListCard(
    course: Course, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = course.progress / 100f,
        animationSpec = tween(800, easing = EaseOut),
        label = "cardProgress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SpColors.White),
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
                    .padding(16.dp), contentAlignment = Alignment.BottomStart
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = course.instructorName,
                            style = SpTypography.caption(),
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = course.dueIn,
                        style = SpTypography.caption(),
                        color = SpColors.Warning
                    )
                }
            }

            // Course info
            Column(
                modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = course.title,
                    style = SpTypography.label(),
                    color = SpColors.DarkBrown,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = SpColors.Gold,
                        trackColor = SpColors.ProgressBg
                    )
                    Text(
                        text = "${course.progress}%",
                        style = SpTypography.caption(),
                        color = SpColors.DarkGray,
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
    placeholder: String = "Search training programs..."
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