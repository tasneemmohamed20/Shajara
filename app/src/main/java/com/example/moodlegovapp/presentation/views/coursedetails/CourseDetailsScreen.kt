package com.example.moodlegovapp.presentation.views.coursedetails

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moodlegovapp.R
import com.example.moodlegovapp.domain.models.CourseModule
import com.example.moodlegovapp.domain.models.CourseSection
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
    val courseSections by vm.courseSections.collectAsState()
    val courseResources by vm.courseResources.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val errorMessage by vm.errorMessage.collectAsState()

    LaunchedEffect(vm) { vm.load() }

    when {
        isLoading && courseSections.isEmpty() -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.Navy)
            }
            return
        }

        courseSections.isEmpty() && errorMessage != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = errorMessage ?: stringResource(R.string.course_detail_not_found),
                    color = AppColors.TextSecondary
                )
            }
            return
        }
    }

    var expandedSectionId by remember(courseSections) {
        mutableStateOf(courseSections.firstOrNull()?.id)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            CourseHeaderBanner(
                courseName = vm.courseName,
                progress = vm.progress,
                onBackClick = onBackClick
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.course_detail_course_modules),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
                Text(
                    stringResource(R.string.course_detail_modules_count, courseSections.size),
                    fontSize = 14.sp,
                    color = AppColors.Navy
                )
            }
        }

        items(courseSections, key = { it.id }) { section ->
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                ExpandableSectionAccordion(
                    section = section,
                    isExpanded = expandedSectionId == section.id,
                    onHeaderClick = {
                        expandedSectionId =
                            if (expandedSectionId == section.id) null else section.id
                    },
                    courseResources = courseResources,
                    onResourcesClick = onResourcesClick,
                    onActivityClick = onActivityClick
                )
            }
        }
    }
}

// --- Header Blue Segment Component ---
@Composable
private fun CourseHeaderBanner(
    courseName: String,
    progress: Int,
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
            // Title
            Text(
                text = courseName,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 34.sp
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.1f),
                    contentColor = Color.Transparent,
                ),
            ) {
                Column(modifier = Modifier.padding(all = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            stringResource(R.string.course_detail_overall_progress),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    ProgressIndicator(
                        progress = progress / 100f,
                        percentage = "${progress}%",
                    )
                }
            }

        }
    }
}


// --- Expandable Accordion Item Node Component ---
@Composable
private fun ExpandableSectionAccordion(
    section: CourseSection,
    isExpanded: Boolean,
    onHeaderClick: () -> Unit,
    courseResources: List<com.example.moodlegovapp.domain.models.MoodleResource>,
    onResourcesClick: (String) -> Unit,
    onActivityClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onHeaderClick)
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Determine icon based on section type or name
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(AppColors.Surface, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "Section Icon",
                        tint = AppColors.Navy,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = section.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(
                            R.string.course_detail_activities_count,
                            section.modules.size
                        ),
                        fontSize = 13.sp,
                        color = AppColors.TextSecondary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = AppColors.TextSecondary
                )
            }

            // Expanding sub-item details stream
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppColors.Background.copy(alpha = 0.5f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    section.modules.forEach { module ->
                        val resourceUrl = courseResources.find { it.id == module.instance }?.contentFiles?.firstOrNull()?.fileurl
                        ActivityRowItem(
                            module = module, 
                            onClick = { 
                                if (module.modName.lowercase() == "resource" && resourceUrl != null) {
                                    onResourcesClick(resourceUrl)
                                } else {
                                    onActivityClick(module.instance)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

// --- Nested Dynamic Individual Activities Row Layouts ---
@Composable
private fun ActivityRowItem(module: CourseModule, onClick: () -> Unit) {
    val isCompleted = module.completionData?.state == 1
    val isExam = module.modName.lowercase() == "assign" || module.modName.lowercase() == "quiz"

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
        Column(modifier = Modifier.weight(1f).clickable(onClick = onClick)) {
            Text(
                text = module.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = module.modName.replaceFirstChar { it.uppercase() },
                fontSize = 12.sp,
                color = AppColors.TextSecondary
            )
        }
    }
}
