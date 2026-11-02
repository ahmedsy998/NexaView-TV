package com.example.model

import androidx.compose.ui.graphics.ImageBitmap

enum class NavSection(val arabicTitle: String, val testTag: String) {
    HOME("الرئيسية", "nav_item_home"),
    APPS("التطبيقات", "nav_item_apps"),
    MOVIES("الأفلام", "nav_item_movies"),
    SERIES("المسلسلات", "nav_item_series"),
    FAVORITES("المفضلة", "nav_item_favorites"),
    FILES("الملفات", "nav_item_files"),
    SETTINGS("الإعدادات", "nav_item_settings")
}

enum class SettingsSection(val arabicTitle: String, val subtitle: String, val testTag: String) {
    ICONS(
        "تخصيص الأيقونات",
        "الشكل، الحجم، الحواف، المسافات، وتوهج التركيز",
        "settings_section_icons"
    ),
    FONTS(
        "الخطوط والخطوط العربية",
        "تغيير نوع الخط العربي الفعلي وحجم النصوص",
        "settings_section_fonts"
    ),
    BACKGROUND(
        "الخلفية والمظهر",
        "خلفيات خفيفة واستيراد صورة من الملفات مع معاينة",
        "settings_section_background"
    ),
    CLOCK(
        "الساعة والتاريخ",
        "صيغة 12/24 ساعة، إظهار أو إخفاء، وتعديل الحجم",
        "settings_section_clock"
    ),
    HOME_APPS(
        "تطبيقات الشاشة الرئيسية",
        "إضافة، إزالة، وإعادة ترتيب تطبيقات الصفين في الرئيسية",
        "settings_section_home_apps"
    ),
    FAVORITES(
        "المفضلة",
        "إدارة التطبيقات والعناصر المفضلة",
        "settings_section_favorites"
    ),
    NAVIGATION(
        "إعدادات التنقل والريموت",
        "خيارات أزرار الريموت، استعادة التركيز، والتنقل الدائري",
        "settings_section_navigation"
    ),
    LOW_END_MODE(
        "وضع الأجهزة الضعيفة",
        "تقليل استهلاك الذاكرة والمعالج وإيقاف التوهج غير الضروري",
        "settings_section_low_end"
    ),
    RESTORE_DEFAULTS(
        "استعادة الإعدادات الافتراضية",
        "إعادة ضبط جميع إعدادات المشغل إلى الوضع الأصلي",
        "settings_section_restore"
    ),
    APP_INFO(
        "معلومات التطبيق",
        "تفاصيل NexaView TV، التوافق، وحالة الذاكرة",
        "settings_section_app_info"
    )
}

enum class IconShapeOption(val arabicLabel: String) {
    ORIGINAL("الأصلي (Original)"),
    SQUARE("مربع (Square)"),
    ROUNDED_SQUARE("مربع بحواف دائرية (Rounded Square)"),
    CIRCLE("دائري (Circle)"),
    ROUNDED_RECTANGLE("مستطيل بحواف دائرية (Rounded Rectangle)")
}

enum class IconSizePreset(val arabicLabel: String, val scaleFactor: Float) {
    EXTRA_SMALL("صغير جداً (Extra Small)", 0.80f),
    SMALL("صغير (Small)", 0.90f),
    MEDIUM("متوسط (Medium)", 1.00f),
    LARGE("كبير (Large)", 1.12f),
    EXTRA_LARGE("كبير جداً (Extra Large)", 1.25f)
}

enum class ArabicFontOption(val fontName: String, val arabicDisplayName: String) {
    CAIRO("Cairo", "خط القاهرة (Cairo - الافتراضي)"),
    AMIRI("Amiri", "خط أميري (Amiri)"),
    TAJAWAL("Tajawal", "خط تجوال (Tajawal)"),
    IBM_PLEX_SANS_ARABIC("IBM Plex Sans Arabic", "خط IBM Plex Sans Arabic"),
    NOTO_SANS_ARABIC("Noto Sans Arabic", "خط Noto Sans Arabic"),
    NOTO_NASKH_ARABIC("Noto Naskh Arabic", "خط Noto Naskh Arabic"),
    READEX_PRO("Readex Pro", "خط Readex Pro"),
    ALMARAI("Almarai", "خط المراعي (Almarai)")
}

enum class BuiltInBackground(val arabicLabel: String) {
    DARK_NAVY("كحلي داكن مع موجات ضوئية (الافتراضي)"),
    BLUE_PURPLE_GRADIENT("تدرج أزرق وبنفسجي هادئ"),
    MINIMAL_ABSTRACT("تجريدي هندسي بسيط"),
    NATURAL_LANDSCAPE("طبيعة ليلية هادئة"),
    NIGHT_CITY("أفق مدينة ليلية"),
    CUSTOM_IMAGE("خلفية مخصصة من الملفات")
}

enum class BackgroundScaleMode(val arabicLabel: String) {
    FILL_SCREEN("ملء الشاشة (Fill Screen)"),
    FIT_IMAGE("ملائمة الصورة (Fit Image)"),
    CENTER_CROP("قص مركزي (Center Crop)")
}

enum class InternetStatus(val arabicDescription: String) {
    VALIDATED_INTERNET("متصل بالإنترنت"),
    CONNECTED_NO_INTERNET("متصل بالشبكة بدون إنترنت"),
    DISCONNECTED("غير متصل بالإنترنت")
}

enum class MediaCategory(val arabicTitle: String) {
    MOVIE("فيلم"),
    SERIES("مسلسل"),
    DOCUMENT_OR_FILE("ملف")
}

data class LocalMediaEntry(
    val uriString: String,
    val title: String,
    val mimeType: String,
    val sizeBytes: Long,
    val category: MediaCategory,
    val addedTimestamp: Long = System.currentTimeMillis()
)

data class AppInfoItem(
    val packageName: String,
    val activityName: String? = null,
    val label: String,
    val iconBitmap: ImageBitmap? = null,
    val isAllAppsShortcut: Boolean = false,
    val isLeanback: Boolean = false,
    val hasAppPreferencesActivity: Boolean = false,
    val tileColorHex: Long = 0xFF151E32L,
    val tileSecondaryColorHex: Long = 0xFF0F172AL
) {
    val isYouTube: Boolean
        get() = !isAllAppsShortcut && (
            packageName.contains("youtube", ignoreCase = true) ||
                label.contains("YouTube", ignoreCase = true) ||
                label.contains("يوتيوب", ignoreCase = true)
            )
}

data class LauncherPreferences(
    // Icon customization
    val iconShape: IconShapeOption = IconShapeOption.ROUNDED_RECTANGLE,
    val iconSizePreset: IconSizePreset = IconSizePreset.MEDIUM,
    val customIconScale: Float = 1.0f,
    val showAppLabels: Boolean = true,
    val appLabelFontSizeSp: Float = 15f,
    val tileSpacingDp: Float = 16f,
    val tileCornerRadiusDp: Float = 20f,
    val tileBackgroundOpacity: Float = 0.92f,
    val enableFocusGlow: Boolean = true,
    val focusBorderThicknessDp: Float = 3.0f,

    // Typography
    val selectedFont: ArabicFontOption = ArabicFontOption.CAIRO,
    val globalFontScale: Float = 1.0f,

    // Background
    val selectedBackground: BuiltInBackground = BuiltInBackground.DARK_NAVY,
    val customBackgroundUri: String? = null,
    val backgroundScaleMode: BackgroundScaleMode = BackgroundScaleMode.CENTER_CROP,
    val backgroundDimOverlay: Float = 0.20f,

    // Clock & Date
    val use24HourFormat: Boolean = true,
    val showClock: Boolean = true,
    val showDate: Boolean = true,
    val clockFontSizeSp: Float = 32f,
    val dateFontSizeSp: Float = 14f,

    // Home Apps & Favorites
    val homeAppPackages: List<String> = emptyList(),
    val hiddenHomePackages: Set<String> = emptySet(),
    val favoritePackages: Set<String> = emptySet(),
    val favoriteMediaUris: Set<String> = emptySet(),
    val showAllAppsTileOnHome: Boolean = true,
    val includeSelfInAppList: Boolean = false,

    // Remote & Navigation
    val wrapAroundNavigation: Boolean = false,
    val restoreFocusOnReturn: Boolean = true,
    val supportAlternativeMenuKeys: Boolean = true,

    // Low-end device mode
    val lowEndDeviceMode: Boolean = false,

    // User-configured local media entries
    val configuredMediaItems: List<LocalMediaEntry> = emptyList()
)
