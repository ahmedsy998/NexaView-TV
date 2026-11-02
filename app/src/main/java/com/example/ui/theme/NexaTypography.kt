package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.ArabicFontOption

object NexaTypography {

    private val cairoFamily: FontFamily by lazy {
        runCatching {
            FontFamily(
                Font(R.font.cairo, FontWeight.Normal),
                Font(R.font.cairo, FontWeight.Medium),
                Font(R.font.cairo, FontWeight.Bold)
            )
        }.getOrDefault(FontFamily.SansSerif)
    }

    private val amiriFamily: FontFamily by lazy {
        runCatching {
            FontFamily(
                Font(R.font.amiri, FontWeight.Normal),
                Font(R.font.amiri, FontWeight.Medium),
                Font(R.font.amiri, FontWeight.Bold)
            )
        }.getOrDefault(FontFamily.Serif)
    }

    private val tajawalFamily: FontFamily by lazy {
        runCatching {
            FontFamily(
                Font(R.font.tajawal, FontWeight.Normal),
                Font(R.font.tajawal, FontWeight.Medium),
                Font(R.font.tajawal, FontWeight.Bold)
            )
        }.getOrDefault(FontFamily.SansSerif)
    }

    private val ibmPlexSansArabicFamily: FontFamily by lazy {
        runCatching {
            FontFamily(
                Font(R.font.ibm_plex_sans_arabic, FontWeight.Normal),
                Font(R.font.ibm_plex_sans_arabic, FontWeight.Medium),
                Font(R.font.ibm_plex_sans_arabic, FontWeight.Bold)
            )
        }.getOrDefault(FontFamily.SansSerif)
    }

    private val notoSansArabicFamily: FontFamily by lazy {
        runCatching {
            FontFamily(
                Font(R.font.noto_sans_arabic, FontWeight.Normal),
                Font(R.font.noto_sans_arabic, FontWeight.Medium),
                Font(R.font.noto_sans_arabic, FontWeight.Bold)
            )
        }.getOrDefault(FontFamily.SansSerif)
    }

    private val notoNaskhArabicFamily: FontFamily by lazy {
        runCatching {
            FontFamily(
                Font(R.font.noto_naskh_arabic, FontWeight.Normal),
                Font(R.font.noto_naskh_arabic, FontWeight.Medium),
                Font(R.font.noto_naskh_arabic, FontWeight.Bold)
            )
        }.getOrDefault(FontFamily.Serif)
    }

    private val readexProFamily: FontFamily by lazy {
        runCatching {
            FontFamily(
                Font(R.font.readex_pro, FontWeight.Normal),
                Font(R.font.readex_pro, FontWeight.Medium),
                Font(R.font.readex_pro, FontWeight.Bold)
            )
        }.getOrDefault(FontFamily.SansSerif)
    }

    private val almaraiFamily: FontFamily by lazy {
        runCatching {
            FontFamily(
                Font(R.font.almarai, FontWeight.Normal),
                Font(R.font.almarai, FontWeight.Medium),
                Font(R.font.almarai, FontWeight.Bold)
            )
        }.getOrDefault(FontFamily.SansSerif)
    }

    fun getFontFamily(option: ArabicFontOption): FontFamily {
        return when (option) {
            ArabicFontOption.CAIRO -> cairoFamily
            ArabicFontOption.AMIRI -> amiriFamily
            ArabicFontOption.TAJAWAL -> tajawalFamily
            ArabicFontOption.IBM_PLEX_SANS_ARABIC -> ibmPlexSansArabicFamily
            ArabicFontOption.NOTO_SANS_ARABIC -> notoSansArabicFamily
            ArabicFontOption.NOTO_NASKH_ARABIC -> notoNaskhArabicFamily
            ArabicFontOption.READEX_PRO -> readexProFamily
            ArabicFontOption.ALMARAI -> almaraiFamily
        }
    }

    fun buildTypography(fontOption: ArabicFontOption, scale: Float = 1.0f): Typography {
        val family = getFontFamily(fontOption)
        val safeScale = scale.coerceIn(0.80f, 1.35f)
        return Typography(
            displayLarge = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.Bold,
                fontSize = (38f * safeScale).sp,
                lineHeight = (46f * safeScale).sp
            ),
            displayMedium = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.Bold,
                fontSize = (32f * safeScale).sp,
                lineHeight = (40f * safeScale).sp
            ),
            headlineLarge = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.Bold,
                fontSize = (26f * safeScale).sp,
                lineHeight = (34f * safeScale).sp
            ),
            headlineMedium = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.SemiBold,
                fontSize = (22f * safeScale).sp,
                lineHeight = (30f * safeScale).sp
            ),
            titleLarge = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.Bold,
                fontSize = (19f * safeScale).sp,
                lineHeight = (26f * safeScale).sp
            ),
            titleMedium = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.Medium,
                fontSize = (16f * safeScale).sp,
                lineHeight = (22f * safeScale).sp
            ),
            bodyLarge = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.Normal,
                fontSize = (15f * safeScale).sp,
                lineHeight = (22f * safeScale).sp
            ),
            bodyMedium = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.Normal,
                fontSize = (13.5f * safeScale).sp,
                lineHeight = (19f * safeScale).sp
            ),
            labelLarge = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.SemiBold,
                fontSize = (14f * safeScale).sp,
                lineHeight = (20f * safeScale).sp
            ),
            labelMedium = TextStyle(
                fontFamily = family,
                fontWeight = FontWeight.Medium,
                fontSize = (12f * safeScale).sp,
                lineHeight = (16f * safeScale).sp
            )
        )
    }
}
