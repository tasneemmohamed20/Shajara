package com.gov.moodleapp.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.*
import com.example.moodlegovapp.R
import com.example.moodlegovapp.core.DependencyContainer
import com.example.moodlegovapp.presentation.viewmodels.LoginViewModel
import com.example.moodlegovapp.presentation.views.auth.components.LoginHeader
import com.example.moodlegovapp.ui.theme.AppColors.Navy
import com.example.moodlegovapp.ui.theme.AppColors.NavyDark
import com.example.moodlegovapp.ui.theme.AppColors.NavyGradient
import com.example.moodlegovapp.ui.theme.SpColors
import com.example.moodlegovapp.ui.theme.SpTypography

@Composable
fun LoginStepTwoView(
    onLoginSuccess: () -> Unit, assembly: DependencyContainer,
    onBackClicked: () -> Unit
) {
    val vm: LoginViewModel = remember { assembly.makeLoginViewModel() }

    // Collect each StateFlow from the ViewModel individually
    val username by vm.username.collectAsState()
    val password by vm.password.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val errorMessage by vm.errorMessage.collectAsState()

    // Password visibility is purely UI state — ViewModel doesn't own it
    var isPasswordVisible by remember { mutableStateOf(false) }

    var rememberMe by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // Observe login success: when loading finishes with no error, fire callback.
    // Use a LaunchedEffect keyed on isLoading so it triggers on each loading→false transition.
    val prevLoading = remember { mutableStateOf(false) }
    LaunchedEffect(isLoading) {
        if (prevLoading.value && !isLoading && errorMessage == null) {
            onLoginSuccess()
        }
        prevLoading.value = isLoading
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpColors.LightGray)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header Banner ─────────────────────────────
            LoginHeaderBanner(onBackClicked)

            // ── Scrollable Form Area ──────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
//                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
//                RoleCard()

                FormCard(
                    username = username,
                    password = password,
                    isPasswordVisible = isPasswordVisible,
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    rememberMe = rememberMe,
                    onUsernameChange = vm::onUsernameChange,
                    onPasswordChange = vm::onPasswordChange,
                    onTogglePassword = { isPasswordVisible = !isPasswordVisible },
                    onRememberMe = { rememberMe = !rememberMe },
                    onForgotPassword = { /* TODO */ },
                    onLogin = {
                        focusManager.clearFocus()
                        vm.login()
                    })

//                SecureNotice()

                SupportLink()
            }
        }
    }
}

// ─────────────────────────────────────────────
// HEADER BANNER
// ─────────────────────────────────────────────

@Composable
private fun LoginHeaderBanner(
    onBackClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(
                NavyGradient
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            LoginHeader(
                showBackButton = true,
                onBackClick = onBackClicked
            )

            Column(
                modifier = Modifier.padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.login_welcome_title),
                    style = SpTypography.headingXL(),
                    color = Color.White
                )
                Text(
                    text = stringResource(R.string.login_welcome_subtitle),
                    style = SpTypography.bodyPrimary(),
                    color = Color.White.copy(alpha = 0.75f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.auth_step_2),
                        style = SpTypography.labelCategory(),
                        color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "100%",
                        style = SpTypography.labelCategory(),
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .background(SpColors.Gold)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// ROLE CARD
// ─────────────────────────────────────────────

@Composable
private fun RoleCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(NavyDark)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f)), contentAlignment = Alignment.Center
        ) {
            //replace image
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = stringResource(R.string.login_logging_as),
                style = SpTypography.bodySecondary(),
                color = Color.White.copy(alpha = 0.75f)
            )
            Text(
                text = stringResource(R.string.login_role_participant),
                style = SpTypography.titleCard(),
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }

        OutlinedButton(
            onClick = { },
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.5.dp, Color.White),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 7.dp)
        ) {
            Text(
                text = stringResource(R.string.login_change),
                style = SpTypography.label(),
                color = Color.White
            )
        }
    }
}

// ─────────────────────────────────────────────
// FORM CARD
// All state passed as plain values — no LoginUiState wrapper
// ─────────────────────────────────────────────

@Composable
private fun FormCard(
    username: String,                       // from vm.username
    password: String,                       // from vm.password
    isPasswordVisible: Boolean,             // local UI state in LoginScreen
    isLoading: Boolean,                     // from vm.isLoading
    errorMessage: String?,                  // from vm.errorMessage
    rememberMe: Boolean,
    onUsernameChange: (String) -> Unit,     // vm::onUsernameChange
    onPasswordChange: (String) -> Unit,     // vm::onPasswordChange
    onTogglePassword: () -> Unit,           // flips local isPasswordVisible
    onRememberMe: () -> Unit,
    onForgotPassword: () -> Unit,
    onLogin: () -> Unit                     // calls vm.login()
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = Color.Black.copy(alpha = 0.06f)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(SpColors.White)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── Username Field ────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(R.string.login_username),
                style = SpTypography.label(),
                color = SpColors.DarkBrown
            )
            OutlinedTextField(
                value = username,
                onValueChange = onUsernameChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        stringResource(R.string.login_username_placeholder),
                        style = SpTypography.bodyPrimary(),
                        color = SpColors.DarkGray.copy(alpha = 0.5f)
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = SpColors.DarkGray,
                        modifier = Modifier.size(18.dp)
                    )
                },
                textStyle = SpTypography.bodyPrimary().copy(color = SpColors.DarkBrown),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text, imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SpColors.NavyBlue,
                    unfocusedBorderColor = SpColors.BorderColor,
                    focusedContainerColor = SpColors.White,
                    unfocusedContainerColor = SpColors.White
                ))
        }

        // ── Password Field ────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(R.string.login_password),
                style = SpTypography.label(),
                color = SpColors.DarkBrown
            )
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = SpColors.DarkGray,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = onTogglePassword) {
                        Icon(
                            if (isPasswordVisible) Icons.Default.Visibility
                            else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = SpColors.DarkGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                textStyle = SpTypography.bodyPrimary().copy(color = SpColors.DarkBrown),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password, imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { onLogin() }),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SpColors.NavyBlue,
                    unfocusedBorderColor = SpColors.BorderColor,
                    focusedContainerColor = SpColors.White,
                    unfocusedContainerColor = SpColors.White
                )
            )
        }

        // ── Error Message ─────────────────────
        AnimatedVisibility(visible = errorMessage != null) {
            errorMessage?.let { err ->
                Text(
                    text = err, style = SpTypography.bodySecondary(), color = SpColors.Error
                )
            }
        }

        // ── Remember Me + Forgot Password ─────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.clickable(onClick = onRememberMe),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .border(
                            1.5.dp,
                            if (rememberMe) SpColors.NavyBlue else SpColors.BorderColor,
                            RoundedCornerShape(4.dp)
                        )
                        .background(
                            if (rememberMe) SpColors.NavyBlue.copy(alpha = 0.1f)
                            else Color.Transparent
                        ), contentAlignment = Alignment.Center
                ) {
                    if (rememberMe) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = SpColors.NavyBlue,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.login_remember_me),
                    style = SpTypography.bodySecondary(),
                    color = SpColors.DarkGray
                )
            }

            TextButton(onClick = onForgotPassword) {
                Text(
                    text = stringResource(R.string.login_forgot_password),
                    style = SpTypography.bodySecondary(),
                    color = Navy
                )
            }
        }

        // ── Login Button ──────────────────────
        Button(
            onClick = onLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !isLoading,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SpColors.Gold,
                disabledContainerColor = SpColors.Gold.copy(alpha = 0.6f)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = stringResource(R.string.login_button),
                    style = SpTypography.headingL(),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// SECURE NOTICE
// ─────────────────────────────────────────────

@Composable
private fun SecureNotice() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SpColors.White)
            .border(1.dp, SpColors.BorderColor, RoundedCornerShape(14.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .border(1.5.dp, SpColors.NavyBlue.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "?",
                style = SpTypography.label(),
                color = SpColors.NavyBlue,
                fontWeight = FontWeight.SemiBold
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(R.string.login_secure_title),
                style = SpTypography.label(),
                color = SpColors.DarkBrown
            )
            Text(
                text = stringResource(R.string.login_secure_body),
                style = SpTypography.bodySecondary(),
                color = SpColors.DarkGray
            )
        }
    }
}

// ─────────────────────────────────────────────
// SUPPORT LINK
// ─────────────────────────────────────────────

@Composable
private fun SupportLink() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.login_need_help),
            style = SpTypography.bodySecondary(),
            color = SpColors.DarkGray
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = stringResource(R.string.login_contact_support),
            style = SpTypography.bodySecondary().copy(
                textDecoration = TextDecoration.Underline
            ),
            color = Navy,
            modifier = Modifier.clickable { })
    }
}