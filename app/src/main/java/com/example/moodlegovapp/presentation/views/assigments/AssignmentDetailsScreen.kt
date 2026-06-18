package com.example.moodlegovapp.presentation.views.assigments
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moodlegovapp.presentation.viewmodels.AssignmentsViewModel
import com.example.moodlegovapp.ui.theme.AppColors

@Composable
fun AssignmentDetailsScreen(
    courseId: Int,
    assignmentId: Int,
    viewModel: AssignmentsViewModel,
    onBackClick: () -> Unit,
    onStartAssignmentClick: (assignmentId: Int) -> Unit
) {
    val assignment by viewModel.selectedAssignment.collectAsState()
    val submissionStatus by viewModel.submissionStatus.collectAsState()
    val isLoading by viewModel.isLoadingDetail.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState() // NEW: Collect the error message
    val courseResources by viewModel.courseResources.collectAsState()

    LaunchedEffect(courseId, assignmentId) {
        viewModel.fetchAssignmentDetail(courseId, assignmentId)
        viewModel.fetchCourseResources(courseId)
    }

    // 1. Show loader ONLY if it's actively loading and we don't have data yet
    if (isLoading && assignment == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AppColors.Navy)
        }
        return
    }

    // 2. Show error text if loading finished but we still have no data
    if (!isLoading && assignment == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = AppColors.Error, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage ?: "Could not load assignment details.",
                    color = AppColors.TextSecondary,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.fetchAssignmentDetail(courseId, assignmentId) }) {
                    Text("Retry")
                }
            }
        }
        return
    }

    val currentAssignment = assignment!!

    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppColors.Background)
                    .padding(20.dp)
            ) {
                Button(
                    onClick = { onStartAssignmentClick(currentAssignment.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Gold),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Start Assignment", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        },
        containerColor = AppColors.Background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
//                .padding(paddingValues),
//            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AssignmentHeader(
                    title = currentAssignment.name,
                    courseName = "Course ${currentAssignment.course}",
                    status = "Pending", // Needs separate status API
                    onBackClick = onBackClick
                )
            }

            // Due Date Banner
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .background(AppColors.ErrorBackground, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = AppColors.Error, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        val dueStr = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(currentAssignment.dueDate * 1000L))
                        Text("Due: $dueStr", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppColors.Error)
                        Text(currentAssignment.name, fontSize = 12.sp, color = AppColors.Error.copy(alpha = 0.8f))
                    }
                }
            }

            // Overview
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.Surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(32.dp).background(AppColors.Gold.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.AdsClick, contentDescription = null, tint = AppColors.Gold, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Overview", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(currentAssignment.intro ?: "", fontSize = 14.sp, color = AppColors.TextSecondary, lineHeight = 20.sp)

                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, AppColors.Border, RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Text("LEARNING OBJECTIVE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppColors.Navy)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Objectives from Intro", fontSize = 13.sp, color = AppColors.TextPrimary)
                            }
                        }
                    }
                }
            }

            // Grid Stats
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(modifier = Modifier.weight(1f), icon = Icons.Default.Refresh, title = "ATTEMPTS", value = "0 / ${currentAssignment.maxAttempts}")
                        val fileTypes = currentAssignment.configs.find { it.plugin == "file" && it.name == "filetypeslist" }?.value ?: ""
                        StatCard(modifier = Modifier.weight(1f), icon = Icons.Default.Description, title = "SUBMISSION", value = fileTypes.ifBlank { "Any" })
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(modifier = Modifier.weight(1f), icon = Icons.Default.Star, title = "GRADING", value = "${currentAssignment.grade} Points")
                        StatCard(modifier = Modifier.weight(1f), icon = Icons.Default.Scale, title = "CUT-OFF DATE", value = if (currentAssignment.cutoffDate > 0) java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date(currentAssignment.cutoffDate * 1000L)) else "None")
                    }
                }
            }

            // Resources
//            if (currentAssignment.introAttachments.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = AppColors.Surface)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Folder, contentDescription = null, tint = AppColors.Navy, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Attachments", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            val context = androidx.compose.ui.platform.LocalContext.current
                            val allFiles = courseResources.flatMap { it.contentFiles }
                            allFiles.forEach { file ->
                                val name = file.filename
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .border(1.dp, AppColors.Border, RoundedCornerShape(16.dp))
                                        .clickable {
                                            val token = "b4cd92a9bbb816fc54ae1a43a01d1dcc"
                                            val url = file.fileurl
                                            val finalUrl = if (url.contains("?")) "$url&token=$token" else "$url?token=$token"
                                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(finalUrl))
                                            context.startActivity(intent)
                                        }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(40.dp).background(AppColors.Background, CircleShape), contentAlignment = Alignment.Center) {
                                            Icon(if (name.endsWith(".zip")) Icons.Default.Archive else Icons.Default.PictureAsPdf, contentDescription = null, tint = AppColors.Error, modifier = Modifier.size(20.dp))
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                    Icon(Icons.Default.Download, contentDescription = null, tint = AppColors.Navy)
                                }
                            }
                        }
                    }
                }
//            }

            // Status
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).background(AppColors.Surface, RoundedCornerShape(24.dp)).padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(40.dp).background(AppColors.Background, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AssignmentLate, contentDescription = null, tint = AppColors.TextSecondary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Status", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                        Text(submissionStatus?.status?.replace("_", " ")?.replaceFirstChar { it.uppercase() } ?: "Not submitted yet", fontSize = 13.sp, color = AppColors.TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String) {
    Column(
        modifier = modifier.background(AppColors.Surface, RoundedCornerShape(20.dp)).padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = AppColors.Navy, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppColors.Navy)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
    }
}

@Composable
fun AssignmentHeader(title: String, courseName: String, status: String, onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(brush = AppColors.NavyGradient, shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .padding(horizontal = 24.dp, vertical = 28.dp)
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(0.15f)).clickable(onClick = onBackClick), contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Text("Assignments Details", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(0.15f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.background(Color.White.copy(0.2f), RoundedCornerShape(100.dp)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                    Text(status.uppercase(), fontSize = 11.sp, color = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(courseName, fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(title, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White, lineHeight = 34.sp)
        }
    }
}