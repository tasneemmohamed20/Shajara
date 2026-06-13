package com.example.moodlegovapp.presentation.views.Profile.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moodlegovapp.R
import com.example.moodlegovapp.domain.models.UserCertificate
import com.example.moodlegovapp.ui.theme.AppColors

@Composable
fun ProfileCertificateCard(
    certificate: UserCertificate,
    onViewClick: (String) -> Unit,
    onDownloadClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        shape = RoundedCornerShape(24.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left Side Border / Gold Bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(if (certificate.isAvailable) AppColors.Gold else AppColors.Border)
            )
            Column(
                modifier = Modifier
                    .padding(vertical = 20.dp, horizontal = 15.dp)
                    .weight(1f)
            ) {
                Text(
                    text = certificate.status.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (certificate.isAvailable) AppColors.Gold else AppColors.TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = certificate.courseName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = if (certificate.isAvailable) R.drawable.done_icon else R.drawable.pending_icon),
                        contentDescription = null,
                        tint = AppColors.TextSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (certificate.isAvailable) certificate.completedAtFormatted else "${stringResource(R.string.completed)} ${certificate.completedAtFormatted}",
                        fontSize = 12.sp,
                        color = AppColors.TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))

                if (certificate.isAvailable) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { certificate.viewUrl?.let { onViewClick(it) } },
//                        modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Navy),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(stringResource(R.string.profile_view_certificate), fontSize = 10.sp, color = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Button(
                            onClick = { certificate.downloadUrl?.let { onDownloadClick(it) } },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Background),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, AppColors.Border)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(stringResource(R.string.profile_download), fontSize = 12.sp, color = AppColors.TextSecondary)
                            }
                        }
                    }
                } else {
                    HorizontalDivider(
                        color = AppColors.Border,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppColors.Background, RoundedCornerShape(100.dp))
                            .drawBehind {
                                drawRoundRect(
                                    color = AppColors.Border,
                                    style = Stroke(
                                        width = 2.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(
                                            floatArrayOf(
                                                10f,
                                                10f
                                            ), 0f
                                        )
                                    ),
                                    cornerRadius = CornerRadius(100.dp.toPx(), 100.dp.toPx())
                                )
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.HourglassEmpty,
                                contentDescription = null,
                                tint = AppColors.TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = certificate.pendingMessage
                                    ?: "Available after final approval",
                                fontSize = 13.sp,
                                color = AppColors.TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}