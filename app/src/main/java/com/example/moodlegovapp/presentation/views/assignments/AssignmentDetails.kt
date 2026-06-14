package com.example.moodlegovapp.presentation.views.assignments

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Grading
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moodlegovapp.domain.models.AssignmentItem
import com.example.moodlegovapp.domain.models.AssignmentResource
import com.example.moodlegovapp.domain.models.displayName
import com.example.moodlegovapp.domain.models.displayStatus
import com.example.moodlegovapp.domain.models.displayType
import com.example.moodlegovapp.domain.models.isOverdue
import com.example.moodlegovapp.presentation.viewmodels.AssignmentsViewModel
import com.example.moodlegovapp.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentDetailsScreen(
    assignmentsViewModel: AssignmentsViewModel,
    onBackClick: () -> Unit,
    onDownloadResource: (AssignmentResource) -> Unit,
    onDownloadAllResources: (List<AssignmentResource>) -> Unit,
    onContactInstructorClick: () -> Unit,
    onGuideClick: () -> Unit,
    onStartAssignmentClick: (assignmentId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val assignmentState by assignmentsViewModel.selectedAssignment.collectAsState()
    val isLoading by assignmentsViewModel.isDetailLoading.collectAsState()
    val errorMessage by assignmentsViewModel.detailErrorMessage.collectAsState()

    if (isLoading && assignmentState == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AppColors.Navy)
        }
        return
    }

    if (errorMessage != null && assignmentState == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = errorMessage ?: "Error loading details", color = AppColors.Error)
        }
        return
    }

    val assignment = assignmentState ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Assignment details not found.", color = AppColors.TextSecondary)
        }
        return
    }

    var isInstructionsExpanded by remember { mutableStateOf(false) }

    Scaffold(topBar = {
        TopAppBar(
            title = {
            Text(
                "Assignments Details",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }, navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        }, actions = {
            IconButton(onClick = {}) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = Color.White
                )
            }
        }, colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.NavyDark)
        )
    }, containerColor = AppColors.Background, bottomBar = {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.Surface)
                .border(
                    1.dp, AppColors.Border, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Button(
                onClick = { onStartAssignmentClick(assignment.id) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Gold),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Start Assignment",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Top Blue Summary Panel Banner Header
            item {
                AssignmentHeaderBanner(assignment = assignment)
            }

            // 2. Alert Status Timeline Row Card Indicator
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    val isOverdue = assignment.isOverdue()
                    val tintColor = if (isOverdue) AppColors.Error else AppColors.green
                    val bgTint =
                        if (isOverdue) AppColors.ErrorBackground else AppColors.green.copy(alpha = 0.08f)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(bgTint, RoundedCornerShape(20.dp))
                            .border(1.dp, tintColor.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                            .padding(16.dp), verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = tintColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = assignment.dueLabel.orEmpty(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = tintColor
                            )
                            Text(
                                text = assignment.courseName.orEmpty(),
                                fontSize = 13.sp,
                                color = AppColors.TextSecondary
                            )
                        }
                    }
                }
            }

            // 3. Overview and Learning Objectives Container Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .border(1.dp, AppColors.Border, RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        AppColors.GoldLight.copy(alpha = 0.3f), CircleShape
                                    ), contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = AppColors.Gold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Overview",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = assignment.intro.orEmpty(),
                            fontSize = 15.sp,
                            color = AppColors.TextSecondary,
                            lineHeight = 22.sp
                        )

                        assignment.learningObjective?.let { objective ->
                            Spacer(modifier = Modifier.height(16.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(AppColors.Background, RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "LEARNING OBJECTIVE",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.Navy,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                Text(
                                    text = objective,
                                    fontSize = 14.sp,
                                    color = AppColors.TextPrimary,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }

            // 4. Detailed Core Parameters Quad Grid Blocks
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GridParameterCard(
                            title = "ATTEMPTS",
                            value = "${assignment.usedAttempts} / ${assignment.maxAttempts}",
                            icon = Icons.Default.Layers,
                            modifier = Modifier.weight(1f)
                        )
                        GridParameterCard(
                            title = "SUBMISSION",
                            value = assignment.allowedFileTypes?.joinToString(", ")?.uppercase() ?: "ANY",
                            icon = Icons.Default.UploadFile,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GridParameterCard(
                            title = "GRADING",
                            value = "${assignment.maxGrade} Points",
                            icon = Icons.AutoMirrored.Filled.Grading,
                            modifier = Modifier.weight(1f)
                        )
                        GridParameterCard(
                            title = "CUT-OFF DATE",
                            value = "${assignment.gradeWeightPercent}% of Final",
                            icon = Icons.Default.CalendarMonth,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 5. Requirements Expandable Instruction Manual Node Accordion
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .border(1.dp, AppColors.Border, RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isInstructionsExpanded = !isInstructionsExpanded }
                                .padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.AutoMirrored.Filled.List,
                                contentDescription = null,
                                tint = AppColors.TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Detailed Instructions",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = if (isInstructionsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = AppColors.TextSecondary
                            )
                        }

                        AnimatedVisibility(visible = isInstructionsExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                assignment.requirements.orEmpty().forEachIndexed { idx, req ->
                                    Row(verticalAlignment = Alignment.Top) {
                                        Text(
                                            "${idx + 1}. ",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AppColors.TextPrimary
                                        )
                                        Text(
                                            text = req.orEmpty(),
                                            fontSize = 14.sp,
                                            color = AppColors.TextSecondary,
                                            lineHeight = 20.sp
                                        )
                                    }
                                }
                                if (assignment.requireIntegrityStatement) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                AppColors.Background, RoundedCornerShape(12.dp)
                                            )
                                            .padding(12.dp), verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            Icons.Default.ErrorOutline,
                                            contentDescription = null,
                                            tint = AppColors.Navy,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = assignment.integrityStatementText.orEmpty(),
                                            fontSize = 12.sp,
                                            color = AppColors.TextSecondary,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6. Resources Attachment Download Modules Card Container List
            if (!assignment.resources.isNullOrEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .border(1.dp, AppColors.Border, RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Description,
                                        contentDescription = null,
                                        tint = AppColors.TextPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        "Resources",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.TextPrimary
                                    )
                                }
                                Text(
                                    text = "Download All",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.Navy,
                                    modifier = Modifier.clickable {
                                        onDownloadAllResources(
                                            assignment.resources.orEmpty()
                                        )
                                    })
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                assignment.resources.orEmpty().forEach { resource ->
                                    ResourceFileDownloadRow(
                                        resource = resource,
                                        onDownloadClick = { onDownloadResource(resource) })
                                }
                            }
                        }
                    }
                }
            }

            // 7. Dynamic Submission Status Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .border(1.dp, AppColors.Border, RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(AppColors.Background, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (assignment.usedAttempts > 0) Icons.Default.CheckCircle else Icons.Default.UploadFile,
                                contentDescription = null,
                                tint = AppColors.TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                "Status",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextPrimary
                            )
                            Text(
                                text = assignment.displayStatus(),
                                fontSize = 13.sp,
                                color = AppColors.TextSecondary
                            )
                        }
                    }
                }
            }

            // 8. Need Help Support Call Menu Blocks Footer
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .border(1.dp, AppColors.Border, RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.AutoMirrored.Filled.HelpOutline,
                                contentDescription = null,
                                tint = AppColors.TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Need Help?",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        HelpNavigationItemRow(
                            title = "Contact Instructor",
                            icon = Icons.Default.Email,
                            onClick = onContactInstructorClick
                        )
                        HorizontalDivider(
                            color = AppColors.Border, modifier = Modifier.padding(vertical = 4.dp)
                        )
                        HelpNavigationItemRow(
                            title = "Assignment Guide",
                            icon = Icons.Default.Description,
                            onClick = onGuideClick
                        )
                    }
                }
            }
        }
    }
}

// --- Parameter Grid Layout Node Helpers ---
@Composable
private fun GridParameterCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.border(1.dp, AppColors.Border, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AppColors.Navy,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Navy
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// --- Attached File Module Row Helpers ---
@Composable
private fun ResourceFileDownloadRow(resource: AssignmentResource, onDownloadClick: () -> Unit) {
    val isZip = resource.mimeType.orEmpty().contains("zip", ignoreCase = true)
    val circleBg = if (isZip) AppColors.Navy.copy(alpha = 0.08f) else AppColors.ErrorBackground
    val iconColor = if (isZip) AppColors.Navy else AppColors.Error
    val fileIcon = if (isZip) Icons.Default.FolderZip else Icons.Default.Description

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AppColors.Border, RoundedCornerShape(16.dp))
            .padding(12.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(circleBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = fileIcon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = resource.name.orEmpty(),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = resource.fileSizeLabel.orEmpty(),
                fontSize = 12.sp,
                color = AppColors.TextSecondary
            )
        }
        IconButton(onClick = onDownloadClick) {
            Icon(
                imageVector = Icons.Default.FileDownload,
                contentDescription = "Download Resource",
                tint = AppColors.TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// --- Footer Navigation Row Component Helpers ---
@Composable
private fun HelpNavigationItemRow(
    title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(AppColors.Background, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColors.TextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = AppColors.TextSecondary,
            modifier = Modifier.size(16.dp)
        )
    }
}

// --- Top Header Blue Banner Component ---
@Composable
private fun AssignmentHeaderBanner(assignment: AssignmentItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = AppColors.NavyGradient,
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
            .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 28.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(
                            Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(100.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = assignment.displayStatus(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = assignment.displayType(),
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            Text(
                text = assignment.displayName(),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 34.sp
            )
        }
    }
}