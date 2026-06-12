package com.example.moodlegovapp.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// ─────────────────────────────────────────────
// SP COLORS — mirrors iOS SPColor exactly
// ─────────────────────────────────────────────

object SpColors {
    val NavyBlue      = Color(0xFF1A3550)
    val NavyBlueLight = Color(0xFF2F5D8A)
    val Gold          = Color(0xFFC9A84C)
    val GoldLight     = Color(0xFFE8C97A)

    val White         = Color(0xFFFFFFFF)
    val LightGray     = Color(0xFFF4F5F7)
    val BorderColor   = Color(0xFFE2E6EA)
    val DarkGray      = Color(0xFF6B7280)
    val DarkBrown     = Color(0xFF1A1A2E)

    val Error         = Color(0xFFEF4444)
    val Success       = Color(0xFF22C55E)
    val Warning       = Color(0xFFF59E0B)
    val Info          = Color(0xFF3B82F6)

    val ProgressBg    = Color(0xFFE5E7EB)
    val ProgressFill  = Color(0xFFC9A84C)
    val blackLabel = Color(0xFF1A1A1A)
}

// ─────────────────────────────────────────────
// SP TYPOGRAPHY — mirrors iOS SPFont exactly
// ─────────────────────────────────────────────

object SpTypography {

    // mirrors iOS SPFont.headingXL()
    fun headingXL() = TextStyle(
        fontSize   = 28.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 34.sp
    )

    // mirrors iOS SPFont.headingL()
    fun headingL() = TextStyle(
        fontSize   = 20.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 26.sp
    )

    // mirrors iOS SPFont.titleCard()
    fun titleCard() = TextStyle(
        fontSize   = 16.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 22.sp
    )

    // mirrors iOS SPFont.bodyPrimary()
    fun bodyPrimary() = TextStyle(
        fontSize   = 15.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 22.sp
    )

    // mirrors iOS SPFont.bodySecondary()
    fun bodySecondary() = TextStyle(
        fontSize   = 13.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 18.sp
    )

    // mirrors iOS SPFont.label()
    fun label() = TextStyle(
        fontSize   = 14.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 20.sp
    )

    // mirrors iOS SPFont.labelCategory()
    fun labelCategory() = TextStyle(
        fontSize   = 11.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    )

    // mirrors iOS SPFont.caption()
    fun caption() = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 16.sp
    )

    // mirrors iOS SPFont.small()
    fun small() = TextStyle(
        fontSize   = 11.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 14.sp
    )
}