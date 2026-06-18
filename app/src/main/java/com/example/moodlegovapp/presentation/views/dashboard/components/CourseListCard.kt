package com.example.moodlegovapp.presentation.views.dashboard.components

import com.example.moodlegovapp.domain.models.Course

import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import androidx.compose.ui.res.stringResource
import com.example.moodlegovapp.R
import com.example.moodlegovapp.ui.theme.SpColors
import com.example.moodlegovapp.ui.theme.SpTypography

@Composable
fun ContinueTrainingSectionCard(
    enrolledCourses: List<Course>,
    onCourseClick: (Course) -> Unit,
    modifier: Modifier = Modifier
) {
    // Master Parent Card Container holding the entire section widget
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF2F5D8A),
                            Color(0xFF1A3550)
                        )
                    ) // Signature Navy Gradient
                )
                .padding(vertical = 24.dp)
        ) {
            // Header Group: Holds 2 Strings and the Action Play Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.dashboard_continue_training),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = stringResource(R.string.dashboard_pick_up),
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 12.sp
                    )
                }

                // Semi-transparent White Play Icon Button Wrapper
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .clickable { /* Handle master action hook */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Resume All Action",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Carousel Section: LazyRow of Nested Cards Peeking Off-Screen
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(enrolledCourses) { course ->
                    InnerCourseItemCard(
                        course = course,
                        onClick = { onCourseClick(course) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun InnerCourseItemCard(
    course: Course,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (course.progress ?: 0) / 100f,
        animationSpec = tween(durationMillis = 800, easing = EaseOut),
        label = "innerCardProgress"
    )

    Card(
        modifier = modifier
            .width(295.dp) // Fixed dimension ensuring clean right-side item peeking
            .wrapContentHeight()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SpColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            // Graphic Banner Title Area
            @OptIn(ExperimentalGlideComposeApi::class)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(145.dp)
            ) {
                // 1. The Composable image goes here, filling the entire parent Box bounds
                if (course.courseImage != null) {
                    GlideImage(
                        model = course.courseImage,
                        contentDescription = course.fullName ?: "Course Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    GlideImage(
                        model = R.drawable.crime,
                        contentDescription = course.fullName ?: "Course Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // 2. Optional: Dark gradient overlay drawn ON TOP of the image so your white text pops cleanly
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))
                            )
                        )
                )

                // 3. Course Title Text Overlay (placed at the bottom-start of the Box stack)
                Text(
                    text = course.fullName ?: "Unknown Course",
                    color = Color.White,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 26.sp,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                )
            }

            // Descriptive Context Block
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Instructor Details Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = SpColors.DarkGray,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Unknown Instructor", // Fallback, not in CourseItem
                        style = SpTypography.caption(),
                        color = SpColors.DarkGray,
                        fontSize = 14.sp
                    )
                }

                // Alert Warning Box Container
                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SpColors.Error.copy(alpha = 0.06f)) // Soft background tint spans the entire row
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Solid Red Circular Wrapper for the Icon
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SpColors.Error),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Due Date Clock",
                            tint = Color.White, // White clock icon inside the red circle
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // 2. Text Group Column (Stacks both strings vertically next to the icon)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.course_card_due_in, "N/A"), // Fallback due date
                            color = SpColors.Error,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "Category ${course.category ?: "Unknown"}", // ID instead of Name
                            color = SpColors.Error,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Smooth Progress Indicators Mapping
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.course_card_progress),
                            style = SpTypography.caption(),
                            color = SpColors.DarkGray
                        )
                        Text(
                            text = "${course.progress ?: 0}%",
                            style = SpTypography.caption(),
                            color = SpColors.DarkBrown,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(50)),
                        color = Color(0xFF1A1A1A),
                        trackColor = SpColors.ProgressBg,
                        gapSize = 0.dp,
                        drawStopIndicator = {}
                    )
                }

                HorizontalDivider(color = SpColors.LightGray.copy(alpha = 0.5f), thickness = 1.dp)

                // Bottom Metadata Info Set
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = SpColors.DarkGray,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "12/18", // Safe static placeholder mapping
                            color = SpColors.DarkGray,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = null,
                            tint = SpColors.Error,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "3 tasks", // Safe static placeholder mapping
                            color = SpColors.Error,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = SpColors.DarkGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}