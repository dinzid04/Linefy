package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

// Clean local TrueType font families installed via font-util
val PlusJakartaSans: FontFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Thin),
    Font(R.font.plus_jakarta_sans, FontWeight.ExtraLight),
    Font(R.font.plus_jakarta_sans, FontWeight.Light),
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold),
    Font(R.font.plus_jakarta_sans, FontWeight.ExtraBold),
    Font(R.font.plus_jakarta_sans, FontWeight.Black)
)

val SpaceGrotesk: FontFamily = FontFamily(
    Font(R.font.space_grotesk, FontWeight.Normal),
    Font(R.font.space_grotesk, FontWeight.Medium),
    Font(R.font.space_grotesk, FontWeight.SemiBold),
    Font(R.font.space_grotesk, FontWeight.Bold)
)

val SfProDisplayBold: FontFamily = PlusJakartaSans
val SFProDisplay: FontFamily = PlusJakartaSans
val SfProDisplay: FontFamily = PlusJakartaSans

val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = SfProDisplayBold,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 42.sp,
        letterSpacing = (-0.5).sp,
        color = TextPrimaryWhite
    ),
    displayMedium = TextStyle(
        fontFamily = SfProDisplayBold,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.3).sp,
        color = TextPrimaryWhite
    ),
    displaySmall = TextStyle(
        fontFamily = SfProDisplayBold,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        color = TextPrimaryWhite
    ),
    headlineLarge = TextStyle(
        fontFamily = SfProDisplayBold,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.3).sp,
        color = TextPrimaryWhite
    ),
    headlineMedium = TextStyle(
        fontFamily = SfProDisplayBold,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.2).sp,
        color = TextPrimaryWhite
    ),
    headlineSmall = TextStyle(
        fontFamily = SfProDisplayBold,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        color = TextPrimaryWhite
    ),
    titleLarge = TextStyle(
        fontFamily = SfProDisplayBold,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        color = TextPrimaryWhite
    ),
    titleMedium = TextStyle(
        fontFamily = SfProDisplayBold,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        color = TextPrimaryWhite
    ),
    titleSmall = TextStyle(
        fontFamily = SfProDisplayBold,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = TextPrimaryWhite
    ),
    bodyLarge = TextStyle(
        fontFamily = SfProDisplayBold,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        color = TextPrimaryWhite
    ),
    bodyMedium = TextStyle(
        fontFamily = SfProDisplayBold,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = TextSecondaryGrey
    ),
    bodySmall = TextStyle(
        fontFamily = SfProDisplayBold,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = TextSecondaryGrey
    ),
    labelLarge = TextStyle(
        fontFamily = SfProDisplayBold,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.2.sp,
        color = TextPrimaryWhite
    ),
    labelMedium = TextStyle(
        fontFamily = SfProDisplayBold,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.4.sp,
        color = TextSecondaryGrey
    ),
    labelSmall = TextStyle(
        fontFamily = SfProDisplayBold,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 13.sp,
        letterSpacing = 0.5.sp,
        color = TextSecondaryGrey
    )
)

val Typography = AppTypography
