package com.example.moodlegovapp.presentation.views.lesson

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.moodlegovapp.presentation.viewmodels.LessonVideoViewModel
import com.example.moodlegovapp.ui.theme.AppColors

@Composable
fun LessonVideoScreen(cmid: Int, vm: LessonVideoViewModel, onBackClick: () -> Unit) {
    var activeCmid by remember(cmid) { mutableStateOf(cmid) }
    
    val lesson by vm.lesson.collectAsState()
    val loading by vm.isLoading.collectAsState()
    val error by vm.error.collectAsState()
    
    LaunchedEffect(activeCmid) {
        vm.load(activeCmid)
    }

    var selectedScormTab by remember { mutableStateOf("content") }
    val hasContentUrl = !lesson?.contentUrl.isNullOrBlank()
    val hasLaunchUrl = !lesson?.launchUrl.isNullOrBlank()
    
    LaunchedEffect(lesson?.contentUrl, lesson?.launchUrl) {
        selectedScormTab = if (hasContentUrl) "content" else "activity"
    }
    
    val webUrl = when {
        hasContentUrl || hasLaunchUrl -> if (selectedScormTab == "content") lesson?.contentUrl else lesson?.launchUrl
        !lesson?.videoUrl.isNullOrBlank() -> lesson?.videoUrl
        else -> null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 180.dp) // extra padding to avoid overlapping the bottom sticky panel
        ) {
            // Gradient Header (includes video player layout)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppColors.NavyGradient)
                        .padding(horizontal = 20.dp)
                        .padding(top = 16.dp, bottom = 24.dp)
                ) {
                    // Back button (translucent circle container)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .clickable { onBackClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(18.dp))
                    
                    // Module index and Title Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(99.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "MODULE ${lesson?.moduleNumber ?: ""}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Text(
                            text = "  ·  ${lesson?.moduleTitle ?: ""}",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Lesson Title
                    Text(
                        text = lesson?.activityTitle ?: "Lesson",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 34.sp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Metadata Icons Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color(0xFFE5B83B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Video Lesson",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp
                            )
                        }
                        
                        val durationMin = ((lesson?.durationSeconds ?: 0) / 60)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = Color(0xFFE5B83B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${if (durationMin > 0) durationMin else 25} min",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp
                            )
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = Color(0xFFE5B83B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = lesson?.lessonPosition ?: "Lesson 3 of 4",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Header progress bar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val progressPercent = lesson?.moduleProgressPercent ?: 0
                        LinearProgressIndicator(
                            progress = progressPercent / 100f,
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(99.dp)),
                            color = AppColors.Gold,
                            trackColor = Color.White.copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "$progressPercent% done",
                            color = AppColors.GoldLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Error overlay
            error?.let {
                item {
                    ErrorCard(it)
                }
            }
            
            // Video / SCORM card
            item {
                Card(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        // SCORM Tabs
                        if (hasContentUrl || hasLaunchUrl) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF3F4F6), RoundedCornerShape(14.dp))
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ScormTabButton(
                                    title = "Content",
                                    selected = selectedScormTab == "content",
                                    enabled = hasContentUrl,
                                    modifier = Modifier.weight(1f)
                                ) { selectedScormTab = "content" }
                                ScormTabButton(
                                    title = "Activity",
                                    selected = selectedScormTab == "activity",
                                    enabled = hasLaunchUrl,
                                    modifier = Modifier.weight(1f)
                                ) { selectedScormTab = "activity" }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        
                        if (!webUrl.isNullOrBlank()) {
                            Log.i("videoScreen", webUrl)
                            MoodleWebView(url = webUrl, modifier = Modifier.fillMaxWidth().height(480.dp))
                        } else {
                            // Custom Video Player Container matching design
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color(0xFF132A4A)) // sleeker dark navy player background
                            ) {
                                // Top Controls Row: LIVE TRAINING & Full Screen
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val isLive = lesson?.isLiveTraining == true
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(99.dp))
                                            .background(Color.Black.copy(alpha = 0.4f))
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = if (isLive) "LIVE TRAINING" else "VIDEO LESSON",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(99.dp))
                                            .background(Color.Black.copy(alpha = 0.4f))
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                            .clickable { /* Toggle full screen */ },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Fullscreen,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = "Full Screen",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                
                                // Large Play button inside translucent circle (centered)
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .align(Alignment.Center)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.15f))
                                        .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                                        .clickable { /* Play action */ },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        tint = Color.White,
                                        modifier = Modifier.size(34.dp)
                                    )
                                }
                                
                                // Player controls overlay at the bottom
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.BottomCenter)
                                        .background(Color.Black.copy(alpha = 0.2f))
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    // Player timeline with thumb
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "8:42",
                                            color = Color.White,
                                            fontSize = 11.sp
                                        )
                                        
                                        // Custom slider track representation
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(horizontal = 10.dp)
                                                .height(4.dp)
                                                .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                                        ) {
                                            // Completed track (gold)
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(0.35f)
                                                    .fillMaxHeight()
                                                    .background(AppColors.Gold, RoundedCornerShape(2.dp))
                                            )
                                            // Knob/thumb
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.CenterStart)
                                                    .padding(start = 68.dp) // approximate position
                                                    .offset(y = (-4).dp)
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White)
                                            )
                                        }
                                        
                                        Text(
                                            text = "25:00",
                                            color = Color.White,
                                            fontSize = 11.sp
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    // Control buttons: SkipPrevious, SkipNext, Speed, CC, Volume
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.SkipPrevious,
                                                contentDescription = "Previous video",
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp).clickable { }
                                            )
                                            Icon(
                                                imageVector = Icons.Default.SkipNext,
                                                contentDescription = "Next video",
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp).clickable { }
                                            )
                                            
                                            // Speed tag
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color.White.copy(alpha = 0.15f))
                                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                                                    .clickable { }
                                            ) {
                                                Text(
                                                    text = "1x",
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ClosedCaption,
                                                contentDescription = "Captions",
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp).clickable { }
                                            )
                                            Icon(
                                                imageVector = Icons.Default.VolumeUp,
                                                contentDescription = "Volume",
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp).clickable { }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Instructor Card
            item {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Avatar
                            if (!lesson?.instructorAvatarUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = lesson?.instructorAvatarUrl,
                                    contentDescription = "Instructor Avatar",
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE5E7EB)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = lesson?.instructorName?.take(2)?.uppercase() ?: "IN",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column {
                                Text(
                                    text = "INSTRUCTOR",
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = lesson?.instructorName ?: "Instructor",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF1F2937)
                                )
                            }
                        }
                        
                        // Instructor Role Badge
                        val roleStr = lesson?.instructorRole ?: "Module Lead"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(99.dp))
                                .background(Color(0xFFEEF2FF))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = roleStr,
                                color = Color(0xFF4F46E5),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            // About this Lesson Card
            item {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "About this Lesson",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF1F2937)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = lesson?.description ?: "No description provided for this lesson.",
                            color = Color(0xFF6B7280),
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                        
                        // Tag chips
                        val tags = lesson?.tags.orEmpty()
                        if (tags.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                tags.forEach { tag ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(99.dp))
                                            .background(Color(0xFFEEF2FF))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = tag,
                                            color = Color(0xFF4F46E5),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Learning Objectives Card
            item {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Learning Objectives",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val objectives = lesson?.learningObjectives.orEmpty()
                        if (objectives.isNotEmpty()) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                objectives.forEach { objective ->
                                    // Parse by colon or newline to get Title and Subtitle
                                    val parts = objective.split(Regex("[:\n]"), limit = 2)
                                    val title = parts.getOrNull(0)?.trim().orEmpty()
                                    val subtitle = parts.getOrNull(1)?.trim()
                                    
                                    Row(
                                        verticalAlignment = Alignment.Top,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF22C55E),
                                            modifier = Modifier
                                                .size(22.dp)
                                                .padding(top = 1.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = title,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1F2937)
                                            )
                                            if (!subtitle.isNullOrBlank()) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = subtitle,
                                                    color = Color(0xFF6B7280),
                                                    fontSize = 12.sp,
                                                    lineHeight = 16.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "No learning objectives listed.",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
            
            if (loading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AppColors.Navy)
                    }
                }
            }
        }

        // STICKY BOTTOM PANEL
        // Positioned using Alignment.BottomCenter in the root Box container
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .shadow(16.dp, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color.White, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Module Progress
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "MODULE PROGRESS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.weight(1.2f)
                    )

                    val progressPercent = lesson?.moduleProgressPercent ?: 0
                    LinearProgressIndicator(
                        progress = progressPercent / 100f,
                        modifier = Modifier
                            .weight(3f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(99.dp)),
                        color = AppColors.Gold,
                        trackColor = Color(0xFFE5E7EB)
                    )

                    Text(
                        text = "$progressPercent%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B365D),
                        modifier = Modifier
                            .weight(0.8f)
                            .padding(start = 12.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                }

                // 2. Mark as Completed Button
                val isCompleted = lesson?.isCompleted == true
                Button(
                    onClick = {
                        vm.markComplete(activeCmid)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCompleted) Color(0xFF22C55E) else AppColors.Gold,
                        disabledContainerColor = Color(0xFF9CA3AF)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isCompleted) "Completed" else "Mark as Completed",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 3. Previous / Next navigation row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Previous Button
                    val hasPrev = lesson?.hasPrevious == true
                    Button(
                        onClick = {
                            lesson?.previousCmid?.let { activeCmid = it }
                        },
                        enabled = hasPrev,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF3F4F6),
                            disabledContainerColor = Color(0xFFF3F4F6).copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = null
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowLeft,
                                contentDescription = null,
                                tint = if (hasPrev) Color(0xFF4B5563) else Color(0xFF9CA3AF),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Previous",
                                color = if (hasPrev) Color(0xFF4B5563) else Color(0xFF9CA3AF),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Next Lesson Button
                    val hasNext = lesson?.hasNext == true
                    Button(
                        onClick = {
                            lesson?.nextCmid?.let { activeCmid = it }
                        },
                        enabled = hasNext,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFAF7F0), // light beige/gold tint
                            disabledContainerColor = Color(0xFFFAF7F0).copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = null
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Next Lesson",
                                color = if (hasNext) AppColors.Gold else Color(0xFF9CA3AF),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = if (hasNext) AppColors.Gold else Color(0xFF9CA3AF),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScormTabButton(title: String, selected: Boolean, enabled: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected && enabled) AppColors.Navy else Color.Transparent)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            title,
            color = when {
                !enabled -> AppColors.TextSecondary.copy(alpha = 0.45f)
                selected -> Color.White
                else -> AppColors.TextSecondary
            },
            fontWeight = FontWeight.SemiBold
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun MoodleWebView(url: String, modifier: Modifier = Modifier.fillMaxSize()) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                loadUrl(url)
            }
        },
        update = { webView ->
            if (webView.url != url) webView.loadUrl(url)
        }
    )
}

@Composable
private fun ErrorCard(message: String) {
    Card(Modifier.padding(20.dp).fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = AppColors.ErrorBackground)) {
        Text(message, modifier = Modifier.padding(16.dp), color = AppColors.Error)
    }
}
