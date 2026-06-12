package com.example.moodlegovapp.presentation.views.coursedetails

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moodlegovapp.R
import com.example.moodlegovapp.domain.models.*
import com.example.moodlegovapp.presentation.components.ProgressIndicator
import com.example.moodlegovapp.presentation.viewmodels.CourseDetailViewModel
import com.example.moodlegovapp.ui.theme.AppColors

@Composable
fun CourseOverviewScreen(
    vm: CourseDetailViewModel,
    onBackClick: () -> Unit,
    onReviewAssignmentClick: (assignmentId: Int) -> Unit,
    onActivityClick: (activityId: Int) -> Unit,
    onResourcesClick: (url: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val courseDetail by vm.courseDetail.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val errorMessage by vm.errorMessage.collectAsState()

    LaunchedEffect(vm) { vm.load() }

    when {
        isLoading && courseDetail == null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.Navy)
            }
            return
        }

        courseDetail == null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = errorMessage ?: "Course not found",
                    color = AppColors.TextSecondary
                )
            }
            return
        }
    }

    val course = courseDetail!!
    val completedModules = remember(course.modules) { course.modules.filter { it.isCompleted } }
    val activeModules = remember(course.modules) { course.modules.filter { !it.isCompleted } }
    var expandedModuleId by remember(course.id, activeModules) {
        mutableStateOf(activeModules.firstOrNull { !it.isLocked }?.id)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            CourseHeaderBanner(course = course, onBackClick = onBackClick)
        }

        course.nextRequiredAssignment?.let { assignment ->
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Text(
                            text = "NEXT REQUIRED ASSIGNMENT",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextSecondary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        NextAssignmentCard(
                            assignment = assignment,
                            onReviewClick = { onReviewAssignmentClick(assignment.id) }
                        )
                    }
                }
            }

            // 3. Horizontal Mini Module Slider — completed modules only
            if (completedModules.isNotEmpty()) {
                item {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Completed Modules", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                            Text("${completedModules.size} Modules", fontSize = 14.sp, color = AppColors.Navy)
                        }
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(completedModules, key = { it.id }) { module ->
                                HorizontalMiniModuleCard(
                                    module = module,
                                    isSelected = false,
                                    onClick = { }
                                )
                            }
                        }
                    }
                }
            }

            // 4. Expandable accordion — all modules except completed
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Course Modules", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                    Text("${activeModules.size} Modules", fontSize = 14.sp, color = AppColors.Navy)
                }
            }

            items(activeModules, key = { it.id }) { module ->
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    ExpandableModuleAccordion(
                        module = module,
                        isExpanded = expandedModuleId == module.id,
                        onHeaderClick = {
                            if (!module.isLocked) {
                                expandedModuleId = if (expandedModuleId == module.id) null else module.id
                            }
                        },
                        onActivityClick = onActivityClick
                    )
                }
            }

            // 5. Shared Course Resources Callout Footer Widget
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    CourseResourcesCard(
                        resources = course.courseResources,
                        onClick = { course.courseResources.url?.let(onResourcesClick) }
                    )
                }
            }
        }
}

// --- Header Blue Segment Component ---
@Composable
private fun CourseHeaderBanner(
    course: CourseDetail,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = AppColors.NavyGradient,
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
            .padding(horizontal = 24.dp, vertical = 28.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f))
                .clickable(onClick = onBackClick)
                .align(Alignment.TopStart),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.icon_back),
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(
            modifier = Modifier.padding(top = 52.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Tag Badge
            Box(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(100.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(text = course.status, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.White)
            }

            // Title
            Text(
                text = course.fullName,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 34.sp
            )

            // Dynamic Subtitle Meta Rows

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = course.instructor,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${course.startDate} - ${course.endDate}",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        if (course.hasCertificate) {
                            Spacer(modifier = Modifier.width(16.dp))
                            Icon(
                                Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = AppColors.GoldLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Certificate",
                                fontSize = 14.sp,
                                color = AppColors.GoldLight
                            )
                        }
                    }
                }


            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.1f),
                    contentColor = Color.Transparent,
                ),
//                border = BorderStroke(1.dp, Color.White)
            ){
                    Column(modifier = Modifier.padding(all = 8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "OVERALL COURSE PROGRESS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        ProgressIndicator(
                            progress = course.overallProgress / 100f,
                            percentage = "${course.overallProgress}%",
                        )
                    }
                }

        }
    }
}

@Composable
private fun NextAssignmentCard(assignment: NextAssignment, onReviewClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AppColors.Border, RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(AppColors.ErrorBackground, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(painterResource(R.drawable.missing_file), contentDescription = null, tint = AppColors.Error, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = assignment.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                    Text(text = assignment.dueLabel, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.Error)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onReviewClick,
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Gold),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Review Assignment", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// --- Top Horizontal Module Pill List Cards ---
@Composable
private fun HorizontalMiniModuleCard(module: CourseModule, isSelected: Boolean, onClick: () -> Unit) {
    val borderColor = if (isSelected) AppColors.green else AppColors.Border
    Box(
        modifier = Modifier
            .size(width = 200.dp, height = 130.dp)
            .background(AppColors.Surface, RoundedCornerShape(20.dp))
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(enabled = !module.isLocked, onClick = onClick)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(), // Allow column to occupy the entire fixed height
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top row (Short name & Icons)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = module.shortName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (module.isLocked) AppColors.TextSecondary else AppColors.green
                )
                if (module.status.lowercase() == "completed") {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AppColors.green, modifier = Modifier.size(18.dp))
                } else if (module.isLocked) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = AppColors.TextSecondary, modifier = Modifier.size(16.dp))
                }
            }

            // Module Name (Will dynamically handle 1 or 2 lines without altering the card size)
            Text(
                text = module.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // This weight spacer pushes the progress bar to the absolute bottom of your fixed box
            Spacer(modifier = Modifier.weight(1f))

            LinearProgressIndicator(
                progress = { module.progressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(100.dp)),
                color = AppColors.green,
                trackColor = AppColors.Border
            )
        }
    }
}

// --- Expandable Accordion Item Node Component ---
@Composable
private fun ExpandableModuleAccordion(
    module: CourseModule,
    isExpanded: Boolean,
    onHeaderClick: () -> Unit,
    onActivityClick: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AppColors.Border, RoundedCornerShape(24.dp))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !module.isLocked, onClick = onHeaderClick)
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${module.shortName}  •  ${module.totalActivities} Activities",
                        fontSize = 12.sp,
                        color = AppColors.TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = module.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (module.isLocked) AppColors.TextSecondary else AppColors.TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))

                // End Metric/Locked Status handles
                when {
                    module.isLocked -> Icon(Icons.Default.Lock, contentDescription = "Locked", tint = AppColors.TextSecondary)
                    else -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${module.progressPercent}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppColors.Navy, modifier = Modifier.padding(end = 8.dp))
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = AppColors.TextSecondary
                            )
                        }
                    }
                }
            }

            // Expanding sub-item details stream
            AnimatedVisibility(visible = isExpanded && !module.isLocked) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppColors.Background.copy(alpha = 0.5f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    module.activities?.forEach { activity ->
                        ActivityRowItem(activity = activity, onClick = { onActivityClick(activity.id) })
                    }
                }
            }
        }
    }
}

// --- Nested Dynamic Individual Activities Row Layouts ---
@Composable
private fun ActivityRowItem(activity: ModuleActivity, onClick: () -> Unit) {
    val isCompleted = activity.status.lowercase() == "completed"
    val isExam = activity.type.lowercase() == "exam"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isExam) AppColors.Surface else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = if (isExam) 1.dp else 0.dp,
                color = if (isExam) AppColors.Border else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Status Icon circle configurations
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = when {
                        isCompleted -> AppColors.green.copy(alpha = 0.15f)
                        isExam -> AppColors.ErrorBackground
                        else -> AppColors.Navy.copy(alpha = 0.1f)
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                isCompleted -> {
                    Icon(
                        painter = painterResource(id = R.drawable.check_mark),
                        contentDescription = "Completed",
                        tint = AppColors.green,
                        modifier = Modifier.size(16.dp)
                    )
                }
                isExam -> {
                    Icon(
                        painter = painterResource(id = R.drawable.assignment),
                        contentDescription = "Assignment Exam",
                        tint = AppColors.Error,
                        modifier = Modifier.size(16.dp)
                    )
                }
                else -> {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Activity",
                        tint = AppColors.Navy,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Center labels
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = activity.name,
                fontSize = 15.sp,
                fontWeight = if (isExam) FontWeight.Bold else FontWeight.Medium,
                color = AppColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))

            // Subtitle status display logic configurations
            val subtitleText = buildString {
                append(activity.type.replaceFirstChar { it.uppercase() })
                if (!activity.duration.isNullOrEmpty()) {
                    append("  •  ${activity.duration}")
                }
                if (!activity.dueLabel.isNullOrEmpty()) {
                    append("  •  ${activity.dueLabel}")
                }
            }
            Text(
                text = subtitleText,
                fontSize = 12.sp,
                color = if (isExam && !isCompleted) AppColors.Error else AppColors.TextSecondary,
                fontWeight = if (isExam) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

// --- Core Shared Media Resources Callout Box ---
@Composable
private fun CourseResourcesCard(resources: CourseResources, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.Surface, RoundedCornerShape(24.dp))
            .border(1.dp, AppColors.Border, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(AppColors.Background, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Folder, contentDescription = null, tint = AppColors.TextSecondary, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = resources.label, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
            Text(text = resources.description, fontSize = 13.sp, color = AppColors.TextSecondary)
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = AppColors.TextPrimary, modifier = Modifier.size(18.dp))
    }
}