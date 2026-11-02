package com.example

import com.example.data.AppRepository
import com.example.model.AppInfoItem
import com.example.model.ArabicFontOption
import com.example.model.BackgroundScaleMode
import com.example.model.BuiltInBackground
import com.example.model.IconShapeOption
import com.example.model.IconSizePreset
import com.example.model.InternetStatus
import com.example.model.LauncherPreferences
import com.example.model.MediaCategory
import com.example.model.NavSection
import com.example.model.SettingsSection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {

    @Test
    fun verifyNavigationMenuExactArabicOrder() {
        val titles = NavSection.entries.map { it.arabicTitle }
        assertEquals(
            listOf(
                "الرئيسية",
                "التطبيقات",
                "الأفلام",
                "المسلسلات",
                "المفضلة",
                "الملفات",
                "الإعدادات"
            ),
            titles
        )
    }

    @Test
    fun verifySettingsSectionsExactArabicTitles() {
        val titles = SettingsSection.entries.map { it.arabicTitle }
        assertEquals(
            listOf(
                "تخصيص الأيقونات",
                "الخطوط والخطوط العربية",
                "الخلفية والمظهر",
                "الساعة والتاريخ",
                "تطبيقات الشاشة الرئيسية",
                "المفضلة",
                "إعدادات التنقل والريموت",
                "وضع الأجهزة الضعيفة",
                "استعادة الإعدادات الافتراضية",
                "معلومات التطبيق"
            ),
            titles
        )
    }

    @Test
    fun verifyYouTubeDetectionAndAllAppsShortcut() {
        val ytTv = AppInfoItem(
            packageName = "com.google.android.youtube.tv",
            label = "YouTube"
        )
        val vlc = AppInfoItem(
            packageName = "org.videolan.vlc",
            label = "VLC"
        )
        val allAppsShortcut = AppRepository.createAllAppsShortcutTile()

        assertTrue(ytTv.isYouTube)
        assertFalse(vlc.isYouTube)
        assertFalse(allAppsShortcut.isYouTube)
        assertTrue(allAppsShortcut.isAllAppsShortcut)
        assertEquals("كل التطبيقات", allAppsShortcut.label)
    }

    @Test
    fun verifyDefaultPreferencesAndArabicFonts() {
        val defaults = LauncherPreferences()
        assertEquals(ArabicFontOption.CAIRO, defaults.selectedFont)
        assertEquals(IconShapeOption.ROUNDED_RECTANGLE, defaults.iconShape)
        assertEquals(IconSizePreset.MEDIUM, defaults.iconSizePreset)
        assertEquals(BuiltInBackground.DARK_NAVY, defaults.selectedBackground)
        assertEquals(BackgroundScaleMode.CENTER_CROP, defaults.backgroundScaleMode)
        assertFalse(defaults.lowEndDeviceMode)
        assertTrue(defaults.showClock)
        assertTrue(defaults.showDate)
        assertTrue(defaults.use24HourFormat)

        val fontNames = ArabicFontOption.entries.map { it.fontName }
        assertEquals(
            listOf(
                "Cairo",
                "Amiri",
                "Tajawal",
                "IBM Plex Sans Arabic",
                "Noto Sans Arabic",
                "Noto Naskh Arabic",
                "Readex Pro",
                "Almarai"
            ),
            fontNames
        )
    }

    @Test
    fun verifyInternetStatusAndMediaCategories() {
        assertEquals(3, InternetStatus.entries.size)
        assertEquals("متصل بالإنترنت", InternetStatus.VALIDATED_INTERNET.arabicDescription)
        assertEquals("غير متصل بالإنترنت", InternetStatus.DISCONNECTED.arabicDescription)
        assertEquals("فيلم", MediaCategory.MOVIE.arabicTitle)
        assertEquals("مسلسل", MediaCategory.SERIES.arabicTitle)
        assertEquals("ملف", MediaCategory.DOCUMENT_OR_FILE.arabicTitle)
    }
}
