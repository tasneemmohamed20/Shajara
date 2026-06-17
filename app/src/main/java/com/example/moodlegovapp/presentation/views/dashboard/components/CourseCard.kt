package com.example.moodlegovapp.presentation.views.dashboard.components

import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
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
import com.example.moodlegovapp.R
import com.example.moodlegovapp.domain.models.Course
import com.example.moodlegovapp.ui.theme.SpColors
import com.example.moodlegovapp.ui.theme.SpTypography

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun CourseListCard(
    course: Course,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = course.progress / 100f,
        animationSpec = tween(800, easing = EaseOut),
        label = "cardProgress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp), // Enhanced rounded corners matching the image
        colors = CardDefaults.cardColors(containerColor = SpColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // 1. Top Image Banner Area with Search Overlay & Title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                // Background Course Thumbnail Photo
                GlideImage(
//                    model = course.imageUrl, // Map from your model instance or use a fallback asset
                    model = R.drawable.crime,
                    contentDescription = course.title,
                    contentScale = ContentScale.Crop,
//                    loading = placeholder(Color.LightGray),
//                    failure = placeholder(Color(0xFF1A3550)),
                    modifier = Modifier.fillMaxSize()
                )

                // Translucent dark gradient layer so the white title text is always readable
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f)),
                                startY = 100f
                            )
                        )
                )

                // Horizontal Row for Glassmorphic Search Button and Course Title Text
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Translucent Blur-effect Circular Glass Icon
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = course.title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 2. Lower Informational Details Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Instructor Row Info Layout
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Instructor",
                        tint = SpColors.DarkGray,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = course.instructorName ,
                        style = SpTypography.bodyPrimary(),
                        color = SpColors.DarkGray,
                        fontSize = 12.sp
                    )
                }

                // Alert Warning Metrics Box (Due Text & Percentage Inline)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Due Date alarm",
                            tint = SpColors.Error,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Due in ${ course.dueIn }",
                            color = SpColors.Error,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "${course.progress}%",
                        color = SpColors.Gold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Smooth Golden Progress Tracking Bar Slider
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50)),
                    color = SpColors.Gold,
                    trackColor = SpColors.ProgressBg,
                    gapSize = 0.dp,
                    drawStopIndicator = {}
                )
            }
        }
    }
}