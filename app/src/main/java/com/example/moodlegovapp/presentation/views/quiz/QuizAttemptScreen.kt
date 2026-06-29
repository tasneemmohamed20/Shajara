package com.example.moodlegovapp.presentation.views.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
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
import com.example.moodlegovapp.presentation.viewmodels.QuizAttemptViewModel
import com.example.moodlegovapp.ui.theme.AppColors
import com.google.gson.JsonElement
import com.example.moodlegovapp.presentation.views.common.toQuizAttemptPageUi

@Composable
fun QuizAttemptScreen(
    quizId: Int,
    cmid: Int = 0,
    vm: QuizAttemptViewModel,
    onBackClick: () -> Unit
) {
    val details by vm.details.collectAsState()
    val attempt by vm.attempt.collectAsState()
    val page by vm.page.collectAsState()
    val activeQuizId by vm.activeQuizId.collectAsState()
    val loading by vm.isLoading.collectAsState()
    val error by vm.error.collectAsState()
    val selectedAnswers by vm.selectedAnswers.collectAsState()

    LaunchedEffect(quizId, cmid) { vm.loadDetails(quizId, cmid) }

    val title = details.textOf("quiz_title", "name", "title") ?: "الاختبار القبلي"
    val course = details.textOf("course_name", "course", "course_title") ?: ""
    val grade = details.textOf("grade", "max_grade", "sumgrades") ?: "100"
    val attempts = details.textOf("attempts_allowed", "attempts", "maxattempts") ?: "غير محدود"
    val previousAttempts = details.listOfObjects("attempts", "previous_attempts", "attempts_history")

    Scaffold(
        bottomBar = {
            if (page == null) {
                Box(Modifier.fillMaxWidth().background(AppColors.Background).padding(20.dp)) {
                    Button(
                        onClick = { vm.start(activeQuizId.takeIf { it > 0 } ?: quizId) },
                        modifier = Modifier.fillMaxWidth().height(58.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Navy),
                        enabled = !loading && error == null
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White)
                        Spacer(Modifier.width(10.dp))
                        Text("ابدأ الاختبار", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        },
        containerColor = AppColors.Background
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            GoldHeader(title = title, onBackClick = onBackClick)

            if (loading && details == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.Navy)
                }
                return@Column
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                error?.let { ErrorCard(cleanMoodleError(it)) }

                if (page != null) {
                    QuizQuestionsContent(page = page!!, selectedAnswers = selectedAnswers, onSelect = vm::selectAnswer, onSubmit = { parsed ->
                        val sequenceValues = parsed.questions.mapNotNull { q ->
                            val name = q.sequenceCheckName
                            val value = q.sequenceCheckValue
                            if (name != null && value != null) name to value else null
                        }.toMap()
                        vm.submitCurrentPage(parsed.attemptId, parsed.page, sequenceValues)
                    })
                } else {
                    QuizSummaryCard(
                        course = course,
                        title = title,
                        grade = grade,
                        attempts = attempts
                    )

                    PreviousAttemptsCard(previousAttempts = previousAttempts)
                }

                if (loading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = AppColors.Navy)
            }
        }
    }
}

@Composable
private fun GoldHeader(title: String, onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .background(
                AppColors.NavyGradient,
                RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
            )
            .padding(horizontal = 24.dp, vertical = 28.dp)
    ) {
        Text(
            text = title,
            modifier = Modifier.align(Alignment.Center),
            color = Color.White,
            fontSize = 25.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(54.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.18f))
                .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
private fun QuizSummaryCard(course: String, title: String, grade: String, attempts: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.End) {
            if (course.isNotBlank()) Text(course, color = AppColors.TextSecondary, fontSize = 17.sp)
            Spacer(Modifier.height(8.dp))
            Text(title, color = AppColors.TextPrimary, fontSize = 26.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End)
            Spacer(Modifier.height(22.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                InfoTile(modifier = Modifier.weight(1f), label = "الدرجة", value = grade, icon = { Icon(Icons.Default.Star, null, tint = AppColors.Navy) })
                InfoTile(modifier = Modifier.weight(1f), label = "المحاولات", value = attempts, icon = { Icon(Icons.Default.Refresh, null, tint = AppColors.Navy) })
            }
        }
    }
}

@Composable
private fun InfoTile(modifier: Modifier, label: String, value: String, icon: @Composable () -> Unit) {
    Row(
        modifier = modifier.background(AppColors.Background, RoundedCornerShape(16.dp)).padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.End) {
            Text(label, color = AppColors.TextSecondary, fontSize = 17.sp)
            Text(value, color = AppColors.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Medium)
        }
        Box(Modifier.size(52.dp).clip(CircleShape).background(AppColors.Navy.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
            icon()
        }
    }
}

@Composable
private fun PreviousAttemptsCard(previousAttempts: List<JsonElement>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.End) {
            Text("المحاولات السابقة", color = AppColors.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(16.dp))
            val attempts = previousAttempts.ifEmpty { emptyList() }
            if (attempts.isEmpty()) {
                AttemptRow(number = "--", status = "لا توجد محاولات", action = "")
            } else {
                attempts.forEachIndexed { index, item ->
                    val id = item.textOf("attempt", "attemptid", "id") ?: "${index + 1}"
                    val state = item.textOf("state", "status") ?: "Finished"
                    AttemptRow(number = "#$id", status = state, action = "مراجعة")
                    if (index != attempts.lastIndex) Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun AttemptRow(number: String, status: String, action: String) {
    Row(
        modifier = Modifier.fillMaxWidth().background(AppColors.Background, RoundedCornerShape(14.dp)).padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(action, color = AppColors.Navy, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Column(horizontalAlignment = Alignment.End) {
            Text(number, color = AppColors.TextPrimary, fontSize = 22.sp)
            Text(status.replaceFirstChar { it.uppercase() }, color = AppColors.TextSecondary, fontSize = 17.sp)
        }
    }
}

@Composable
private fun QuizQuestionsContent(
    page: JsonElement,
    selectedAnswers: Map<String, String>,
    onSelect: (String, String) -> Unit,
    onSubmit: (com.example.moodlegovapp.presentation.views.common.QuizAttemptPageUi) -> Unit
) {
    val parsed = page.toQuizAttemptPageUi()
    val status = parsed.state ?: "inprogress"

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(status.replaceFirstChar { it.uppercase() }, color = AppColors.Navy, fontSize = 18.sp)
        Text("الصفحة ${parsed.page + 1}", color = AppColors.TextSecondary, fontSize = 18.sp)
    }

    if (parsed.questions.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Text(
                "لا توجد أسئلة في هذه الصفحة.",
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                color = AppColors.TextSecondary,
                textAlign = TextAlign.End
            )
        }
    }

    parsed.questions.forEach { question ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.End) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${question.maxMark ?: ""} نقطة", color = AppColors.Navy, fontSize = 17.sp)
                    Text("السؤال ${question.number}", color = AppColors.TextSecondary, fontSize = 18.sp)
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = question.text,
                    color = AppColors.TextPrimary,
                    fontSize = 20.sp,
                    textAlign = TextAlign.End,
                    lineHeight = 28.sp
                )
                Spacer(Modifier.height(18.dp))
                if (question.options.isEmpty()) {
                    Text(
                        question.status.ifBlank { "لا توجد اختيارات متاحة لهذا السؤال." },
                        color = AppColors.TextSecondary,
                        fontSize = 17.sp,
                        textAlign = TextAlign.End
                    )
                } else {
                    question.options.forEachIndexed { index, answer ->
                        val answerName = question.answerName
                        val selected = answerName != null && selectedAnswers[answerName] == answer.value
                        AnswerRow(
                            text = answer.label,
                            selected = selected,
                            onClick = { if (answerName != null) onSelect(answerName, answer.value) }
                        )
                        if (index != question.options.lastIndex) Spacer(Modifier.height(10.dp))
                    }
                }
            }
        }
    }

    if (parsed.questions.any { it.options.isNotEmpty() }) {
        Button(
            onClick = { onSubmit(parsed) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Navy)
        ) {
            Text("إرسال الإجابات", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun AnswerRow(text: String, selected: Boolean = false, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().background(AppColors.Background, RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Text(text, color = AppColors.TextPrimary, fontSize = 18.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
        Spacer(Modifier.width(12.dp))
        Icon(if (selected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked, contentDescription = null, tint = if (selected) AppColors.Navy else AppColors.TextSecondary, modifier = Modifier.size(28.dp))
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
        "لا يمكن فتح هذا الاختبار لأن المستخدم غير مسجل في هذا النشاط أو تم إرسال رقم نشاط غير صحيح."
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

private fun JsonElement?.listOfObjects(vararg keys: String): List<JsonElement> {
    if (this == null) return emptyList()
    return runCatching {
        if (!isJsonObject) return@runCatching emptyList<JsonElement>()
        val obj = asJsonObject
        keys.firstNotNullOfOrNull { key ->
            obj.get(key)?.takeIf { it.isJsonArray }?.asJsonArray?.toList()
        } ?: emptyList()
    }.getOrDefault(emptyList())
}

private fun String.stripHtml(): String = replace(Regex("<[^>]*>"), " ")
    .replace("&nbsp;", " ")
    .replace("&quot;", "\"")
    .replace("&#39;", "'")
    .replace(Regex("\\s+"), " ")
    .trim()
