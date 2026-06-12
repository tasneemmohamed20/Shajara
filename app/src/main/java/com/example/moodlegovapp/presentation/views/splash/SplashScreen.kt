package com.example.moodlegovapp.presentation.views.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.moodlegovapp.R
import com.example.moodlegovapp.ui.theme.AppColors
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    session: com.example.moodlegovapp.data.session.AppSession,
    onSplashFinished: (isAuthenticated: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(key1 = true) {
        delay(2000)
        // Check credentials/auth token state
        onSplashFinished(session.isAuthenticated)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.NavyGradient),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.shield),
            contentDescription = "Shield Icon",
            modifier = Modifier.size(120.dp)
        )
    }
}
