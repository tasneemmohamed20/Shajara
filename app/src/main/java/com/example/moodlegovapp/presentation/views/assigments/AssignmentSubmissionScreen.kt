package com.example.moodlegovapp.presentation.views.assigments
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moodlegovapp.domain.models.AssignmentSubmissionFinalize
import com.example.moodlegovapp.presentation.viewmodels.AssignmentsViewModel
import com.example.moodlegovapp.ui.theme.AppColors
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentSubmissionScreen(
    assignmentId: Int,           // add this parameter
    viewModel: AssignmentsViewModel,
    onBackClick: () -> Unit,
    onSubmissionSuccess: () -> Unit
) {
    val assignment by viewModel.selectedAssignment.collectAsState()
    val isLoadingDetail by viewModel.isLoadingDetail.collectAsState()
    val submissionStatus by viewModel.submissionStatus.collectAsState()
    val uploadedFile by viewModel.uploadedFile.collectAsState()
    val submissionResult by viewModel.submissionResult.collectAsState()

    var isIntegrityChecked by remember { mutableStateOf(false) }
    var commentsText by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("File Upload") }

    // Trigger fetch when screen opens
    LaunchedEffect(assignmentId) {
        viewModel.fetchAssignmentDetail(assignmentId)
    }

    LaunchedEffect(submissionResult) {
        if (submissionResult is AssignmentsViewModel.SubmissionResult.Submitted) {
            onSubmissionSuccess()
            viewModel.clearSubmissionResult()
        }
    }

    // Show loader instead of blank screen
    if (isLoadingDetail || assignment == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AppColors.Navy)
        }
        return
    }

    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppColors.Background)
                    .padding(20.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.finalizeSubmission(
                            AssignmentSubmissionFinalize(
                                assignmentId = assignment!!.id,
                                submissionId = 9001, // Mock ID passed here
                                acceptIntegrityStatement = isIntegrityChecked,
                                comments = commentsText
                            )
                        )
                    },
                    enabled = isIntegrityChecked,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Gold,
                        disabledContainerColor = AppColors.Gold.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    if (submissionResult is AssignmentsViewModel.SubmissionResult.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Review & Submit", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        },
        containerColor = AppColors.Background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                SubmissionHeader(
                    courseName = assignment!!.courseName ?: "COURSE",
                    title = assignment!!.name,
                    onBackClick = onBackClick
                )
            }

            // Timer & Deadline Card
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .background(AppColors.Surface, RoundedCornerShape(24.dp))
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(48.dp).background(AppColors.Gold.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = AppColors.Gold, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("TIME REMAINING", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppColors.TextSecondary)
                        Text(submissionStatus?.timeRemaining ?: "N/A", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("DEADLINE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppColors.TextSecondary)
                        Text(submissionStatus?.deadline ?: "N/A", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                    }
                }
            }

            // Tabs
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text("Submission Type", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TypeTab(title = "File Upload", icon = Icons.Default.UploadFile, isSelected = selectedTab == "File Upload", modifier = Modifier.weight(1f)) { selectedTab = "File Upload" }
                        TypeTab(title = "Text Entry", icon = Icons.Default.Keyboard, isSelected = selectedTab == "Text Entry", modifier = Modifier.weight(1f)) { selectedTab = "Text Entry" }
                    }
                }
            }

            // Upload Area
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text("Upload File", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AppColors.Border, RoundedCornerShape(24.dp))
                            .background(AppColors.Surface, RoundedCornerShape(24.dp))
                            .clickable { /* Trigger File Picker Intent Here */ }
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(modifier = Modifier.size(48.dp).background(AppColors.Navy, CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.White)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Tap to browse or drag file", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("PDF, DOCX, JPG (Max 25MB)", fontSize = 12.sp, color = AppColors.TextSecondary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { /* Trigger File Picker */ }, colors = ButtonDefaults.buttonColors(containerColor = AppColors.Navy)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Browse Files")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Uploaded File
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).border(1.dp, AppColors.Border, RoundedCornerShape(16.dp)).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).background(AppColors.ErrorBackground, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = AppColors.Error, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(uploadedFile?.file?.fileName ?: "crime_scene_report_v...", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                            Text("${uploadedFile?.file?.fileSizeLabel ?: "2.4 MB"} • Uploaded", fontSize = 12.sp, color = AppColors.TextSecondary)
                        }
                    }
                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = AppColors.TextSecondary, modifier = Modifier.clickable { viewModel.clearUpload() })
                }
            }

            // Requirements & Integrity
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Card(colors = CardDefaults.cardColors(containerColor = AppColors.Surface), shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = AppColors.Gold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("REQUIREMENTS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppColors.Gold)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            assignment?.requirements?.forEach { req ->
                                Text("•  $req", fontSize = 13.sp, color = AppColors.TextSecondary, modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().background(AppColors.Surface, RoundedCornerShape(16.dp)).padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isIntegrityChecked,
                            onCheckedChange = { isIntegrityChecked = it },
                            colors = CheckboxDefaults.colors(checkedColor = AppColors.Gold, checkmarkColor = Color.White)
                        )
                        Text(
                            "I confirm this submission follows academy integrity rules and is my original work.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.TextPrimary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Comments
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text("Comments (Optional)", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = AppColors.TextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = commentsText,
                        onValueChange = { commentsText = it },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        placeholder = { Text("Add any notes for your instructor...", color = AppColors.TextSecondary, fontSize = 14.sp) },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = AppColors.Surface,
                            unfocusedContainerColor = AppColors.Surface,
                            unfocusedBorderColor = AppColors.Border,
                            focusedBorderColor = AppColors.Navy
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun SubmissionHeader(courseName: String, title: String, onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(brush = AppColors.NavyGradient, shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .padding(28.dp)
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(0.15f)).clickable(onClick = onBackClick), contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Box(modifier = Modifier.background(AppColors.Gold.copy(0.2f), RoundedCornerShape(100.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = AppColors.Gold, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("72:45:29", color = AppColors.Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(0.15f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(courseName.uppercase(), fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White, lineHeight = 34.sp)
        }
    }
}

@Composable
fun TypeTab(title: String, icon: ImageVector, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val borderColor = if (isSelected) AppColors.Gold else AppColors.Border
    val bgColor = if (isSelected) AppColors.Surface else Color.Transparent

    Box(
        modifier = modifier
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .background(bgColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = AppColors.Gold,
                modifier = Modifier.align(Alignment.TopEnd).padding(end = 8.dp, top = 8.dp).size(16.dp)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = if (isSelected) AppColors.Gold else AppColors.TextSecondary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isSelected) AppColors.TextPrimary else AppColors.TextSecondary)
        }
    }
}