package com.example.moodlegovapp.presentation.views.auth

import android.app.Activity
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moodlegovapp.R
import com.example.moodlegovapp.presentation.components.ProgressIndicator
import com.example.moodlegovapp.presentation.viewmodels.LoginViewModel
import com.example.moodlegovapp.ui.theme.AppColors
import com.example.moodlegovapp.ui.theme.SpTypography


@Composable
fun LoginStepOneView(
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    onContinueClicked: () -> Unit,
    vm: LoginViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(AppColors.NavyGradient)
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    bottom = 16.dp
                ) // Reduced bottom padding from 32.dp to 16.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween // Helps dynamically distribute items if space allows
            ) {
                Column {
                    // Integrated custom LoginHeader component
                    LoginHeader(
                        showBackButton = showBackButton,
                        onBackClick = onBackClick,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp)) // Reduced from 24.dp

                    // Welcome Text
                    Text(
                        text = stringResource(R.string.login_step1_welcome_title),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 28.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp)) // Reduced from 12.dp

                    Text(
                        text = stringResource(R.string.login_step1_welcome_subtitle),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }

                // Pushed to the very bottom of the 30% box dynamically
                Column {
                    // Progress Indicator Section
                    Text(
                        text = stringResource(R.string.auth_step_1),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    ProgressIndicator(0.5f, "50%")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Language Selection Card Component
        LanguageSelectionCard(vm = vm)

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

// ─────────────────────────────────────────────────────────────
// LanguageSelectionCard
// ─────────────────────────────────────────────────────────────

@Composable
fun LanguageSelectionCard(vm: LoginViewModel) {
    val context = LocalContext.current
    val activity = context as? Activity

    // Collect VM state
    val selectedLanguage by vm.selectedLanguage.collectAsState()
    val pendingLanguage by vm.pendingLanguage.collectAsState()

    LaunchedEffect(Unit) {
        vm.syncSelectedLanguageFromSystem()
    }

    // ── Restart-confirmation dialog ────────────────────────────
    pendingLanguage?.let { pending ->
        AlertDialog(
            onDismissRequest = { vm.onLanguageDialogDismiss() },
            title = {
                Text(
                    text = stringResource(R.string.lang_dialog_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(text = stringResource(R.string.lang_dialog_message))
            },
            confirmButton = {
                TextButton(
                    onClick = { activity?.let { vm.applyLanguage(it) } }
                ) {
                    Text(
                        text = stringResource(R.string.lang_dialog_ok),
                        color = AppColors.Gold,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.onLanguageDialogDismiss() }) {
                    Text(
                        text = stringResource(R.string.lang_dialog_cancel),
                        color = AppColors.Navy
                    )
                }
            }
        )
    }

    // ── Card UI ────────────────────────────────────────────────
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
                    painter = painterResource(id = R.drawable.language_icon),
                    contentDescription = "Language",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.login_step1_language_label),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Segmented Toggle Control
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(50))
                    .background(AppColors.Surface)
            ) {
                // English Toggle Option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(if (selectedLanguage == LoginViewModel.LANGUAGE_ENGLISH) AppColors.Gold else Color.Transparent)
                        .clickable {
                            vm.onLanguageTapped(LoginViewModel.LANGUAGE_ENGLISH)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.login_step1_lang_english),
                        color = if (selectedLanguage == LoginViewModel.LANGUAGE_ENGLISH) Color.White else AppColors.Navy,
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
                        .background(if (selectedLanguage == LoginViewModel.LANGUAGE_ARABIC) AppColors.Gold else Color.Transparent)
                        .clickable {
                            vm.onLanguageTapped(LoginViewModel.LANGUAGE_ARABIC)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.login_step1_lang_arabic),
                        color = if (selectedLanguage == LoginViewModel.LANGUAGE_ARABIC) Color.White else AppColors.Navy,
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
                .height(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, AppColors.Border, RoundedCornerShape(16.dp))
                .background(AppColors.Surface)
                // Adjusted vertical padding so inner components (like the 36dp icon) don't clip
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(AppColors.Success),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.lock_icon),
                    contentDescription = "Security",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.login_step1_security_title),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.TextPrimary,
                    lineHeight = 16.sp
                )
                Text(
                    text = stringResource(R.string.login_step1_security_subtitle),
                    fontSize = 10.sp,
                    color = AppColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Visible,
                    lineHeight = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Main Action Button
        Button(
            onClick = onContinueClicked,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Gold)
        ) {
            Text(
                text = stringResource(R.string.login_step1_continue),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

