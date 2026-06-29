package com.example.moodlegovapp.presentation.views.common

import android.os.Build
import android.text.Html
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser

data class QuizAttemptPageUi(
    val attemptId: Int,
    val page: Int,
    val nextPage: Int?,
    val state: String?,
    val questions: List<QuizQuestionUi>
)

data class QuizQuestionUi(
    val slot: Int,
    val number: String,
    val status: String,
    val maxMark: String?,
    val html: String,
    val text: String,
    val answerName: String?,
    val sequenceCheckName: String?,
    val sequenceCheckValue: String?,
    val options: List<QuizOptionUi>
)

data class QuizOptionUi(val id: String, val label: String, val value: String)

data class FeedbackPageUi(
    val feedbackId: Int,
    val page: Int,
    val nextPage: Int?,
    val hasNextPage: Boolean,
    val items: List<FeedbackItemUi>
)

data class FeedbackItemUi(
    val id: String,
    val name: String,
    val label: String,
    val type: String,
    val required: Boolean,
    val hasValue: Boolean,
    val options: List<FeedbackOptionUi>
)

data class FeedbackOptionUi(val id: String, val label: String, val value: String)

fun JsonElement?.asDecodedResultObject(): JsonObject? {
    val obj = this?.asObjectOrNull() ?: return null
    val rawResult = obj.getStringOrNull("result")
    if (!rawResult.isNullOrBlank()) {
        runCatching { JsonParser.parseString(rawResult).asObjectOrNull() }.getOrNull()?.let { return it }
    }
    return obj
}

fun JsonElement?.toQuizAttemptPageUi(): QuizAttemptPageUi {
    val wrapper = this?.asObjectOrNull()
    val result = this.asDecodedResultObject()
    val attempt = result?.getObjectOrNull("attempt")
    val questionArray = result?.getArrayOrNull("questions") ?: wrapper?.getArrayOrNull("questions")

    val questions = questionArray?.mapIndexedNotNull { index, item ->
        val q = item.asObjectOrNull() ?: return@mapIndexedNotNull null
        val html = q.getStringOrNull("html").orEmpty()
        val answerName = html.firstAnswerInputName()
        val sequenceName = html.firstRegexGroup("name=\\\"([^\\\"]*:sequencecheck)\\\"")
        val sequenceValue = html.firstRegexGroup("name=\\\"[^\\\"]*:sequencecheck\\\"[^>]*value=\\\"([^\\\"]+)\\\"")
        val qText = html.extractMoodleQuestionText()
            ?: q.getStringOrNull("questiontext")?.stripHtmlEntities()
            ?: q.getStringOrNull("text")?.stripHtmlEntities()
            ?: html.stripHtmlEntities()

        QuizQuestionUi(
            slot = q.getIntOrNull("slot") ?: index + 1,
            number = q.getStringOrNull("number") ?: q.getStringOrNull("questionnumber") ?: "${index + 1}",
            status = q.getStringOrNull("status").orEmpty(),
            maxMark = q.getStringOrNull("maxmark") ?: q.getStringOrNull("mark") ?: q.getStringOrNull("grade"),
            html = html,
            text = qText,
            answerName = answerName,
            sequenceCheckName = sequenceName,
            sequenceCheckValue = sequenceValue,
            options = html.parseQuizOptions().ifEmpty { q.parseJsonOptions() }
        )
    }.orEmpty()

    return QuizAttemptPageUi(
        attemptId = wrapper?.getIntOrNull("attemptid") ?: attempt?.getIntOrNull("id") ?: result?.getIntOrNull("attemptid") ?: 0,
        page = wrapper?.getIntOrNull("page") ?: attempt?.getIntOrNull("currentpage") ?: result?.getIntOrNull("page") ?: 0,
        nextPage = result?.getIntOrNull("nextpage") ?: wrapper?.getIntOrNull("nextpage"),
        state = attempt?.getStringOrNull("state") ?: result?.getStringOrNull("state") ?: wrapper?.getStringOrNull("state"),
        questions = questions
    )
}

fun JsonElement?.toFeedbackPageUi(fallbackFeedbackId: Int): FeedbackPageUi {
    val wrapper = this?.asObjectOrNull()
    val result = this.asDecodedResultObject()
    val itemArray = result?.getArrayOrNull("items") ?: result?.getArrayOrNull("questions") ?: wrapper?.getArrayOrNull("items")
    val items = itemArray?.mapIndexedNotNull { index, item ->
        val obj = item.asObjectOrNull() ?: return@mapIndexedNotNull null
        val itemId = obj.getIntOrNull("id") ?: index + 1
        val html = obj.getStringOrNull("html").orEmpty()
        val presentation = obj.getStringOrNull("presentation") ?: html
        val type = obj.getStringOrNull("typ") ?: obj.getStringOrNull("type") ?: html.detectFeedbackType()
        val name = obj.getStringOrNull("name") ?: html.firstRegexGroup("name=\\\"([^\\\"]+)\\\"") ?: "item_$itemId"
        val rawLabel = obj.getStringOrNull("label") ?: obj.getStringOrNull("text") ?: html.extractFeedbackLabel()
        val label = when {
            !rawLabel.isNullOrBlank() -> rawLabel.stripHtmlEntities()
            type == "label" -> presentation.stripHtmlEntities()
            else -> name.stripHtmlEntities()
        }
        FeedbackItemUi(
            id = "$itemId",
            name = name,
            label = label,
            type = type,
            required = obj.getBooleanOrNull("required") ?: false,
            hasValue = obj.getBooleanOrNull("hasvalue") ?: (type != "label"),
            options = presentation.parseFeedbackOptions()
        )
    }.orEmpty()
    val hasNextPage = result?.getBooleanOrNull("hasnextpage") ?: false
    return FeedbackPageUi(
        feedbackId = wrapper?.getIntOrNull("feedbackid") ?: result?.getIntOrNull("feedbackid") ?: fallbackFeedbackId,
        page = wrapper?.getIntOrNull("page") ?: result?.getIntOrNull("gopage") ?: 0,
        nextPage = result?.getIntOrNull("nextpage") ?: if (hasNextPage) ((wrapper?.getIntOrNull("page") ?: 0) + 1) else null,
        hasNextPage = hasNextPage,
        items = items
    )
}

private fun String.parseQuizOptions(): List<QuizOptionUi> {
    val inputLabelRegex = Regex(
        "<input[^>]+type=\\\"(?:radio|checkbox)\\\"[^>]*>\\s*<label[^>]*>(.*?)</label>",
        setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
    )
    return inputLabelRegex.findAll(this).mapIndexedNotNull { index, match ->
        val input = match.value.substringBefore("<label")
        val name = input.firstRegexGroup("name=\\\"([^\\\"]+)\\\"")
        val value = input.firstRegexGroup("value=\\\"([^\\\"]+)\\\"") ?: "$index"
        val id = input.firstRegexGroup("id=\\\"([^\\\"]+)\\\"") ?: "$name-$value"
        val label = match.groupValues.getOrNull(1)?.stripHtmlEntities().orEmpty()
        val isFlagControl = name?.contains("flagged", ignoreCase = true) == true ||
            id.contains("flagged", ignoreCase = true) ||
            label.equals("Flag question", ignoreCase = true) ||
            label.contains("وضع علامة", ignoreCase = true)
        val isAnswerControl = name?.contains("_answer", ignoreCase = true) == true
        if (label.isBlank() || isFlagControl || !isAnswerControl) null else QuizOptionUi(id = id, label = label, value = value)
    }.distinctBy { it.value to it.label }.toList()
}

private fun String.firstAnswerInputName(): String? {
    val inputRegex = Regex("<input[^>]+type=\\\"(?:radio|checkbox)\\\"[^>]*>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
    return inputRegex.findAll(this).mapNotNull { match ->
        val input = match.value
        input.firstRegexGroup("name=\\\"([^\\\"]+)\\\"")?.takeIf { name ->
            !name.contains("flagged", ignoreCase = true) && name.contains("_answer", ignoreCase = true)
        }
    }.firstOrNull()
}

private fun String.extractMoodleQuestionText(): String? {
    val patterns = listOf(
        "<div[^>]*class=\\\"[^\\\"]*qtext[^\\\"]*\\\"[^>]*>(.*?)</div>",
        "<div[^>]*class='[^']*qtext[^']*'[^>]*>(.*?)</div>",
        "<div[^>]*id=\\\"questiontext[^\\\"]*\\\"[^>]*>(.*?)</div>"
    )
    return patterns.firstNotNullOfOrNull { pattern ->
        firstRegexGroup(pattern)?.stripHtmlEntities()?.takeIf { it.isNotBlank() }
    }
}

private fun JsonObject.parseJsonOptions(): List<QuizOptionUi> {
    val arr = getArrayOrNull("answers") ?: getArrayOrNull("options") ?: getArrayOrNull("choices") ?: return emptyList()
    return arr.mapIndexedNotNull { index, item ->
        val obj = item.asObjectOrNull() ?: return@mapIndexedNotNull null
        val label = obj.getStringOrNull("text") ?: obj.getStringOrNull("answer") ?: obj.getStringOrNull("label") ?: return@mapIndexedNotNull null
        val value = obj.getStringOrNull("value") ?: obj.getStringOrNull("id") ?: "$index"
        val cleanLabel = label.stripHtmlEntities()
        if (cleanLabel.equals("Flag question", true)) null else QuizOptionUi(id = value, label = cleanLabel, value = value)
    }
}

private fun String.parseFeedbackOptions(): List<FeedbackOptionUi> {
    val middle = firstRegexGroup(">>>>>(.*?)<<<<<") ?: this
    val moodlePresentation = middle.split("|").mapNotNull { part ->
        val segments = part.trim().split("####")
        if (segments.size < 2) return@mapNotNull null
        val value = segments.first().replace("r", "").trim()
        val label = segments.drop(1).joinToString("####").stripHtmlEntities()
        if (value.isBlank() || label.isBlank()) null else FeedbackOptionUi(value, label, value)
    }
    if (moodlePresentation.isNotEmpty()) return moodlePresentation

    val optionRegex = Regex("<option[^>]*value=\\\"([^\\\"]*)\\\"[^>]*>(.*?)</option>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
    val selectOptions = optionRegex.findAll(this).mapNotNull { match ->
        val value = match.groupValues.getOrNull(1).orEmpty()
        val label = match.groupValues.getOrNull(2).orEmpty().stripHtmlEntities()
        if (value.isBlank() || label.isBlank()) null else FeedbackOptionUi(value, label, value)
    }.toList()
    if (selectOptions.isNotEmpty()) return selectOptions

    val radioRegex = Regex("<input[^>]+type=\\\"(?:radio|checkbox)\\\"[^>]*>\\s*<label[^>]*>(.*?)</label>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
    return radioRegex.findAll(this).mapIndexedNotNull { index, match ->
        val input = match.value.substringBefore("<label")
        val name = input.firstRegexGroup("name=\\\"([^\\\"]+)\\\"")
        val value = input.firstRegexGroup("value=\\\"([^\\\"]+)\\\"") ?: "$index"
        val label = match.groupValues.getOrNull(1).orEmpty().stripHtmlEntities()
        val isFlagControl = name?.contains("flagged", ignoreCase = true) == true || label.equals("Flag question", true)
        if (label.isBlank() || isFlagControl) null else FeedbackOptionUi(value, label, value)
    }.toList()
}

private fun String.detectFeedbackType(): String = when {
    contains("<select", ignoreCase = true) -> "multichoice"
    contains("type=\"radio\"", ignoreCase = true) || contains("type=\"checkbox\"", ignoreCase = true) -> "multichoice"
    contains("<textarea", ignoreCase = true) -> "textarea"
    contains("<input", ignoreCase = true) -> "textfield"
    else -> "label"
}

private fun String.extractFeedbackLabel(): String? =
    firstRegexGroup("<label[^>]*>(.*?)</label>")?.stripHtmlEntities()?.takeIf { it.isNotBlank() }
        ?: firstRegexGroup("<div[^>]*class=\\\"[^\\\"]*(?:item|text|label)[^\\\"]*\\\"[^>]*>(.*?)</div>")?.stripHtmlEntities()?.takeIf { it.isNotBlank() }

fun String.stripHtmlEntities(): String {
    val decoded = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY).toString()
    } else {
        @Suppress("DEPRECATION") Html.fromHtml(this).toString()
    }
    return decoded.replace('\u00A0', ' ').replace(Regex("\\s+"), " ").trim()
}

fun String.firstRegexGroup(pattern: String): String? =
    Regex(pattern, setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        .find(this)?.groupValues?.getOrNull(1)

fun JsonElement.asObjectOrNull(): JsonObject? = runCatching { if (isJsonObject) asJsonObject else null }.getOrNull()
fun JsonObject.getObjectOrNull(key: String): JsonObject? = get(key)?.asObjectOrNull()
fun JsonObject.getArrayOrNull(key: String) = runCatching { get(key)?.takeIf { it.isJsonArray }?.asJsonArray }.getOrNull()
fun JsonObject.getStringOrNull(key: String): String? = runCatching { get(key)?.takeIf { !it.isJsonNull && it.isJsonPrimitive }?.asString }.getOrNull()?.takeIf { it.isNotBlank() }
fun JsonObject.getIntOrNull(key: String): Int? = runCatching { get(key)?.takeIf { !it.isJsonNull && it.isJsonPrimitive }?.asInt }.getOrNull()
fun JsonObject.getBooleanOrNull(key: String): Boolean? = runCatching { get(key)?.takeIf { !it.isJsonNull && it.isJsonPrimitive }?.asBoolean }.getOrNull()
