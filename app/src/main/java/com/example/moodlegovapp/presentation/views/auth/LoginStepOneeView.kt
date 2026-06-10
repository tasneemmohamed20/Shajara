package com.example.moodlegovapp.presentation.views.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moodlegovapp.R
import com.example.moodlegovapp.ui.theme.AppColors
import com.example.moodlegovapp.ui.theme.SpTypography


@Composable
fun LoginStepOneView(
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    onContinueClicked:() -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        // Dark Navy Header Container (Using your premium gradient)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(AppColors.NavyGradient)
                .padding(bottom = 32.dp, start = 24.dp, end = 24.dp)
        ) {
            Column {
                // Integrated custom LoginHeader component
                LoginHeader(
                    showBackButton = showBackButton,
                    onBackClick = onBackClick,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Welcome Text
                Text(
                    text = "Welcome to\nTraining Platform",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 34.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Choose your preferred language to continue and review important platform information.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Progress Indicator Section
                Text(
                    text = "STEP 1 OF 2",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { 0.5f },
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(50)),
                        color = AppColors.Gold,
                        trackColor = Color.White.copy(alpha = 0.24f),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "50%",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Language Selection Card Component
        LanguageSelectionCard()

        // Pushes the security section and button to the bottom
        Spacer(modifier = Modifier.weight(1f))

        // Bottom Security and Action Component
        BottomActionSection(onContinueClicked)
    }
}

@Composable
fun LoginHeader(
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.login_academy_name)
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 52.dp),
        horizontalArrangement = if (showBackButton) Arrangement.SpaceBetween else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showBackButton) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .clickable { onBackClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_back),
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
        }

        // Logo and Title Group
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.shield),
                    contentDescription = null,
                    tint = AppColors.Gold,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = title,
                style = SpTypography.label(),
                color = Color.White,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun LanguageSelectionCard() {
    var selectedLanguage by remember { mutableStateOf("English") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Navy)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_back), // Swap out with a language drawable when ready
                    contentDescription = "Language",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Language",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Segmented Toggle Control
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(50))
                    .background(AppColors.Surface)
            ) {
                // English Toggle Option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(if (selectedLanguage == "English") AppColors.Gold else Color.Transparent)
                        .clickable { selectedLanguage = "English" },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "English",
                        color = if (selectedLanguage == "English") Color.White else AppColors.Navy,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Arabic Toggle Option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(if (selectedLanguage == "Arabic") AppColors.Gold else Color.Transparent)
                        .clickable { selectedLanguage = "Arabic" },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "العربية",
                        color = if (selectedLanguage == "Arabic") Color.White else AppColors.Navy,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun BottomActionSection(
    onContinueClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        // Security Banner Info Box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, AppColors.Border, RoundedCornerShape(16.dp))
                .background(AppColors.Surface)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AppColors.Success),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.shield), // Swap out with a lock asset when ready
                    contentDescription = "Security",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "SECURITY & PRIVACY",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.TextPrimary
                )
                Text(
                    text = "Encrypted and monitored by SP IT Security",
                    fontSize = 12.sp,
                    color = AppColors.TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Main Action Button
        Button(
            onClick = onContinueClicked,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Gold)
        ) {
            Text(
                text = "Continue",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

