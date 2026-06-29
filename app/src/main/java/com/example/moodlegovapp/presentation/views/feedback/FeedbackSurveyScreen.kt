package com.example.moodlegovapp.presentation.views.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moodlegovapp.presentation.viewmodels.FeedbackSurveyViewModel
import com.example.moodlegovapp.presentation.views.common.FeedbackItemUi
import com.example.moodlegovapp.presentation.views.common.toFeedbackPageUi
import com.example.moodlegovapp.ui.theme.AppColors
import com.google.gson.JsonElement

@Composable
fun FeedbackSurveyScreen(
    feedbackId: Int,
    vm: FeedbackSurveyViewModel,
    onBackClick: () -> Unit
) {
    val details by vm.details.collectAsState()
    val page by vm.page.collectAsState()
    val responses by vm.responses.collectAsState()
    val submitted by vm.submitted.collectAsState()
    val loading by vm.isLoading.collectAsState()
    val error by vm.error.collectAsState()

    LaunchedEffect(feedbackId) { vm.loadDetails(feedbackId) }

    val title = details.textOf("feedback_title", "name", "title") ?: "Survey"
    val course = details.textOf("course_name", "course", "course_title").orEmpty()
    val intro = details.textOf("intro", "description").orEmpty().stripHtml()
    val detailsPage = details.toFeedbackPageUi(feedbackId)
    val activePage = page.toFeedbackPageUi(feedbackId).takeIf { page != null } ?: detailsPage

    Scaffold(
        bottomBar = {
            if (!submitted) {
                Box(Modifier.fillMaxWidth().background(AppColors.Background).padding(20.dp)) {
                    Button(
                        onClick = {
                            if (page == null) vm.launch(feedbackId)
                            else vm.submitPage(feedbackId, activePage.page, activePage.nextPage)
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Navy),
                        enabled = !loading && error == null
                    ) { Text(if (page == null) "Open Survey" else "Submit", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) }
                }
            }
        },
        containerColor = AppColors.Background
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Header(title = title, onBackClick = onBackClick)
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                error?.let { ErrorCard(cleanMoodleError(it)) }

                if (submitted) {
                    SubmittedCard()
                } else if (page == null) {
                    DetailsCard(course = course, title = title, intro = intro)
                }

                if (page != null || detailsPage.items.isNotEmpty()) {
                    SurveyItemsCard(
                        items = activePage.items,
                        responses = responses,
                        onSelect = vm::setResponse
                    )
                }

                if (loading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = AppColors.Navy)
            }
        }
    }
}

@Composable
private fun Header(title: String, onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(AppColors.NavyGradient, RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Text(title, modifier = Modifier.align(Alignment.Center), color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Box(
            modifier = Modifier.align(Alignment.CenterEnd).size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.18f)).clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
    }
}

@Composable
private fun DetailsCard(course: String, title: String, intro: String) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(Color.White)) {
        Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (course.isNotBlank()) Text(course, color = AppColors.TextSecondary, fontSize = 16.sp)
            Text(title, color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, textAlign = TextAlign.End)
            if (intro.isNotBlank()) Text(intro, color = AppColors.TextSecondary, textAlign = TextAlign.End)
        }
    }
}

@Composable
private fun SurveyItemsCard(items: List<FeedbackItemUi>, responses: Map<String, String>, onSelect: (String, String) -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(Color.White)) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (items.isEmpty()) {
                Text("No survey questions found.", color = AppColors.TextSecondary)
            }
            items.forEach { item ->
                if (!item.hasValue || item.type == "label") {
                    Text(item.label, color = AppColors.TextPrimary, fontSize = 18.sp, textAlign = TextAlign.End)
                } else {
                    Text((if (item.required) "* " else "") + item.label, color = AppColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End)
                    item.options.forEach { option ->
                        val selected = responses[item.name] == option.value
                        Row(
                            modifier = Modifier.fillMaxWidth().background(AppColors.Background, RoundedCornerShape(14.dp)).clickable { onSelect(item.name, option.value) }.padding(14.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(option.label, modifier = Modifier.weight(1f), textAlign = TextAlign.End, color = AppColors.TextPrimary)
                            Spacer(Modifier.width(10.dp))
                            Icon(if (selected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked, null, tint = if (selected) AppColors.Navy else AppColors.TextSecondary)
                        }
                    }
                    if (item.options.isEmpty()) {
                        OutlinedTextField(
                            value = responses[item.name].orEmpty(),
                            onValueChange = { onSelect(item.name, it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Answer") },
                            singleLine = item.type.contains("text", ignoreCase = true)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubmittedCard() {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(Color.White)) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.CheckCircle, null, tint = AppColors.Success, modifier = Modifier.size(54.dp))
            Spacer(Modifier.height(12.dp))
            Text("Survey submitted successfully", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(AppColors.ErrorBackground)) {
        Text(message, modifier = Modifier.fillMaxWidth().padding(16.dp), color = AppColors.Error, textAlign = TextAlign.End)
    }
}

private fun cleanMoodleError(message: String): String = when {
    message.contains("notenrolled", ignoreCase = true) || message.contains("not enrolled", ignoreCase = true) ->
        "لا يمكن فتح هذا الاستبيان لأن المستخدم غير مسجل في هذا النشاط أو تم إرسال رقم نشاط غير صحيح."
    message.contains("requirelogin", ignoreCase = true) -> "هذا النشاط يحتاج جلسة تسجيل دخول صالحة."
    else -> message
}

private fun JsonElement?.textOf(vararg keys: String): String? {
    if (this == null) return null
    return runCatching {
        when {
            isJsonObject -> {
                val obj = asJsonObject
                keys.firstNotNullOfOrNull { key -> obj.get(key)?.takeIf { !it.isJsonNull && it.isJsonPrimitive }?.asString }
                    ?: obj.entrySet().firstNotNullOfOrNull { it.value.textOf(*keys) }
            }
            isJsonArray -> asJsonArray.firstNotNullOfOrNull { it.textOf(*keys) }
            isJsonPrimitive -> asString
            else -> null
        }
    }.getOrNull()?.takeIf { it.isNotBlank() }
}

private fun String.stripHtml(): String = replace(Regex("<[^>]*>"), " ")
    .replace("&nbsp;", " ")
    .replace("&quot;", "\"")
    .replace("&#39;", "'")
    .replace(Regex("\\s+"), " ")
    .trim()
