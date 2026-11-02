package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.FontFamily
import com.example.model.ArabicFontOption

val LocalActiveFontFamily = staticCompositionLocalOf<FontFamily> {
    NexaTypography.getFontFamily(ArabicFontOption.CAIRO)
}

private val NexaDarkColorScheme = darkColorScheme(
    primary = NexaVioletAccent,
    onPrimary = NexaTextPrimary,
    secondary = NexaBlueAccent,
    onSecondary = NexaTextPrimary,
    tertiary = NexaCyanGlow,
    background = NexaDeepNavy,
    onBackground = NexaTextPrimary,
    surface = NexaMidnightBlue,
    onSurface = NexaTextPrimary,
    surfaceVariant = NexaTileDark,
    onSurfaceVariant = NexaTextSecondary
)

@Composable
fun NexaViewTVTheme(
    selectedFont: ArabicFontOption = ArabicFontOption.CAIRO,
    fontScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val activeFontFamily = remember(selectedFont) {
        NexaTypography.getFontFamily(selectedFont)
    }
    val typography = remember(selectedFont, fontScale) {
        NexaTypography.buildTypography(selectedFont, fontScale)
    }

    CompositionLocalProvider(LocalActiveFontFamily provides activeFontFamily) {
        MaterialTheme(
            colorScheme = NexaDarkColorScheme,
            typography = typography,
            content = content
        )
    }
}
