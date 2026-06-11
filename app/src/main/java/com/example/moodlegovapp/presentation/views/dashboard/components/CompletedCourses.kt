package com.example.moodlegovapp.presentation.views.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moodlegovapp.R
import com.example.moodlegovapp.domain.models.UserCertificate
import com.example.moodlegovapp.ui.theme.AppColors

private const val MAX_VISIBLE_COURSES = 3

@Composable
fun CompletedCourses(
    certificates: List<UserCertificate>?,
    onViewCertificateClick: (url: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val visibleCertificates = certificates?.take(MAX_VISIBLE_COURSES).orEmpty()

    if (visibleCertificates.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No completed courses yet",
                fontSize = 14.sp,
                color = AppColors.TextSecondary
            )
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        visibleCertificates.forEach { certificate ->
            CompletedCourseCard(
                certificate = certificate,
                onViewCertificateClick = onViewCertificateClick
            )
        }
    }
}

@Composable
fun CompletedCourseCard(
    certificate: UserCertificate,
    onViewCertificateClick: (url: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isAvailable = certificate.isAvailable
    val primaryStatusColor = if (isAvailable) AppColors.green else AppColors.TextSecondary
    val lightStatusBg = primaryStatusColor.copy(alpha = 0.12f)
    val statusIconBg = primaryStatusColor.copy(alpha = 0.15f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AppColors.Surface, shape = RoundedCornerShape(28.dp))
            .border(1.dp, AppColors.Border, shape = RoundedCornerShape(28.dp))
            .padding(vertical = 24.dp, horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier
//                .weight(1f)
                .padding(end = 12.dp)) {
                Text(
                    text = certificate.courseName.orEmpty(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    certificate.instructorName?.takeIf { it.isNotBlank() }?.let { instructor ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Instructor",
                                tint = AppColors.TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = instructor,
                                fontSize = 14.sp,
                                color = AppColors.TextSecondary
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Completed date",
                            tint = AppColors.TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = certificate.completedAtFormatted.orEmpty(),
                            fontSize = 14.sp,
                            color = AppColors.TextSecondary
                        )
                    }
                }

                if (!isAvailable && !certificate.pendingMessage.isNullOrEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Pending notice",
                            tint = AppColors.Error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = certificate.pendingMessage.orEmpty(),
                            fontSize = 13.sp,
                            color = AppColors.Error
                        )
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(statusIconBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painterResource(R.drawable.check_mark),
                        contentDescription = "Status Indicator",
                        tint = primaryStatusColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp),
            color = AppColors.Border
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(lightStatusBg)
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = certificate.approvalStatus.orEmpty(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = primaryStatusColor
                )
            }

            Button(
                onClick = { certificate.viewUrl?.let { onViewCertificateClick(it) } },
                enabled = isAvailable && certificate.viewUrl != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Navy,
                    contentColor = Color.White,
                    disabledContainerColor = AppColors.Background,
                    disabledContentColor = AppColors.TextSecondary
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "View Certificate",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
