package com.example.moodlegovapp.presentation.views.lesson

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.moodlegovapp.presentation.viewmodels.LessonVideoViewModel
import com.example.moodlegovapp.ui.theme.AppColors

@Composable
fun LessonVideoScreen(cmid: Int, vm: LessonVideoViewModel, onBackClick: () -> Unit) {
    val lesson by vm.lesson.collectAsState()
    val loading by vm.isLoading.collectAsState()
    val error by vm.error.collectAsState()
    LaunchedEffect(cmid) { vm.load(cmid) }

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

    LazyColumn(
        Modifier.fillMaxSize().background(AppColors.Background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Column(Modifier.fillMaxWidth().background(AppColors.NavyGradient).padding(20.dp)) {
                IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                Text("MODULE ${lesson?.moduleNumber ?: ""}  ·  ${lesson?.moduleTitle ?: ""}", color = Color.White.copy(.75f))
                Text(lesson?.activityTitle ?: "Lesson", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                LinearProgressIndicator(
                    progress = (lesson?.moduleProgressPercent ?: 0) / 100f,
                    modifier = Modifier.fillMaxWidth().padding(top = 18.dp).height(6.dp).clip(RoundedCornerShape(99.dp)),
                    color = AppColors.Gold,
                    trackColor = Color.White.copy(.25f)
                )
            }
        }

        error?.let { item { ErrorCard(it) } }

        item {
            Card(
                Modifier.padding(20.dp).fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(Color.White)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (hasContentUrl || hasLaunchUrl) {
                        Row(
                            Modifier.fillMaxWidth().background(AppColors.Background, RoundedCornerShape(14.dp)).padding(6.dp),
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
                    }

                    if (!webUrl.isNullOrBlank()) {
                        MoodleWebView(url = webUrl, modifier = Modifier.fillMaxWidth().height(520.dp))
                    } else {
                        Box(
                            Modifier.fillMaxWidth().height(210.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFF12345A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(Modifier.size(78.dp).clip(CircleShape).background(Color.White.copy(.25f)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(48.dp))
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(Modifier.padding(horizontal = 20.dp, vertical = 8.dp).fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(Color.White)) {
                Column(Modifier.padding(20.dp)) {
                    Text("INSTRUCTOR", color = Color.Gray)
                    Text(lesson?.instructorName ?: "N/A", fontWeight = FontWeight.Bold)
                    Text(lesson?.instructorRole ?: "", color = Color(0xFF2D6CDF))
                }
            }
        }
        item { InfoCard("About this Lesson", lesson?.description ?: "") }
        item {
            Card(Modifier.padding(horizontal = 20.dp, vertical = 8.dp).fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(Color.White)) {
                Column(Modifier.padding(20.dp)) {
                    Text("Learning Objectives", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    lesson?.learningObjectives.orEmpty().forEach {
                        Row(Modifier.padding(vertical = 8.dp)) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF25C46B))
                            Spacer(Modifier.width(12.dp))
                            Text(it)
                        }
                    }
                }
            }
        }
        if (loading) item { Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = AppColors.Navy) } }
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
    Card(Modifier.padding(20.dp).fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(AppColors.ErrorBackground)) {
        Text(message, modifier = Modifier.padding(16.dp), color = AppColors.Error)
    }
}

@Composable
private fun InfoCard(title: String, body: String) {
    Card(Modifier.padding(horizontal = 20.dp, vertical = 8.dp).fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(Color.White)) {
        Column(Modifier.padding(20.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(12.dp))
            Text(body, color = Color.Gray)
        }
    }
}
