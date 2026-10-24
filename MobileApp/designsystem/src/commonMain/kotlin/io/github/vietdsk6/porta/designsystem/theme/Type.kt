package io.github.vietdsk6.porta.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.github.vietdsk6.porta.designsystem.generated.resources.UiRes
import io.github.vietdsk6.porta.designsystem.generated.resources.manrope_variable
import io.github.vietdsk6.porta.designsystem.generated.resources.newsreader_variable
import org.jetbrains.compose.resources.Font

private val uiFontFamily
    @Composable get() =
        FontFamily(
            Font(
                UiRes.font.manrope_variable,
                FontWeight.Normal,
                FontStyle.Normal,
            ),
            Font(
                UiRes.font.manrope_variable,
                FontWeight.Medium,
                FontStyle.Normal,
            ),
            Font(
                UiRes.font.manrope_variable,
                FontWeight.SemiBold,
                FontStyle.Normal,
            ),
            Font(
                UiRes.font.manrope_variable,
                FontWeight.Bold,
                FontStyle.Normal,
            ),
        )

private val editorialFontFamily
    @Composable get() =
        FontFamily(
            Font(
                UiRes.font.newsreader_variable,
                FontWeight.SemiBold,
                FontStyle.Normal,
            ),
        )

/**
 * App typography values
 * @param h2: Used for example app name in splash screen
 * @param h3: h3 can be used in screens like big title, where it is like one part of screen.
 * For example, in Forgot Password screen or OnBoarding Screen title, this big text can use h3
 *
 * @param h4: Most used title. For example toolbar title, card title, dialog title, etc.
 * @param h5: Mostly used in section titles
 */
@Immutable
class AppTypography(
    val h1: TextStyle,
    val h2: TextStyle,
    val h3: TextStyle,
    val h4: TextStyle,
    val h5: TextStyle,
    val h6: TextStyle,
    val bodyExtraLarge: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val bodySmall: TextStyle,
    val bodyExtraSmall: TextStyle,
)

// Default Material 3 typography values
private val baseline = Typography()

val MaterialThemAppTypography
    @Composable
    get() =
        Typography(
            displayLarge = baseline.displayLarge.copy(fontFamily = editorialFontFamily),
            displayMedium = baseline.displayMedium.copy(fontFamily = editorialFontFamily),
            displaySmall = baseline.displaySmall.copy(fontFamily = editorialFontFamily),
            headlineLarge = baseline.headlineLarge.copy(fontFamily = editorialFontFamily),
            headlineMedium = baseline.headlineMedium.copy(fontFamily = editorialFontFamily),
            headlineSmall = baseline.headlineSmall.copy(fontFamily = editorialFontFamily),
            titleLarge = baseline.titleLarge.copy(fontFamily = editorialFontFamily),
            titleMedium = baseline.titleMedium.copy(fontFamily = uiFontFamily),
            titleSmall = baseline.titleSmall.copy(fontFamily = uiFontFamily),
            bodyLarge = baseline.bodyLarge.copy(fontFamily = uiFontFamily),
            bodyMedium = baseline.bodyMedium.copy(fontFamily = uiFontFamily),
            bodySmall = baseline.bodySmall.copy(fontFamily = uiFontFamily),
            labelLarge = baseline.labelLarge.copy(fontFamily = uiFontFamily),
            labelMedium = baseline.labelMedium.copy(fontFamily = uiFontFamily),
            labelSmall = baseline.labelSmall.copy(fontFamily = uiFontFamily),
        )

val appTypography
    @Composable get() =
        AppTypography(
            h1 =
            TextStyle(
                fontFamily = editorialFontFamily,
                letterSpacing = 0.sp,
                fontWeight = FontWeight.SemiBold,
                fontSize = 48.sp,
                lineHeight = 67.sp,
            ),
            h2 =
            TextStyle(
                letterSpacing = 0.sp,
                fontFamily = editorialFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 40.sp,
                lineHeight = 56.sp,
            ),
            h3 =
            TextStyle(
                letterSpacing = 0.sp,
                fontFamily = editorialFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 32.sp,
                lineHeight = 45.sp,
            ),
            h4 =
            TextStyle(
                letterSpacing = 0.sp,
                fontFamily = editorialFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 29.sp,
                lineHeight = 34.sp,
            ),
            h5 =
            TextStyle(
                letterSpacing = 0.sp,
                fontFamily = editorialFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                lineHeight = 26.sp,
            ),
            h6 =
            TextStyle(
                letterSpacing = 0.sp,
                fontFamily = uiFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                lineHeight = 24.sp,
            ),
            bodyExtraLarge =
            TextStyle(
                fontFamily = uiFontFamily,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.2.sp,
                fontSize = 18.sp,
                lineHeight = 29.sp,
            ),
            bodyLarge =
            TextStyle(
                fontFamily = uiFontFamily,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.2.sp,
                fontSize = 16.sp,
                lineHeight = 26.sp,
            ),
            bodyMedium =
            TextStyle(
                fontFamily = uiFontFamily,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.2.sp,
                fontSize = 15.sp,
                lineHeight = 23.sp,
            ),
            bodySmall =
            TextStyle(
                fontFamily = uiFontFamily,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.2.sp,
                fontSize = 13.sp,
                lineHeight = 19.sp,
            ),
            bodyExtraSmall =
            TextStyle(
                fontFamily = uiFontFamily,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.2.sp,
                fontSize = 12.sp,
                lineHeight = 17.sp,
            ),
        )
