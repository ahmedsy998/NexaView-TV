package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.ArabicFontOption
import com.example.model.BackgroundScaleMode
import com.example.model.BuiltInBackground
import com.example.model.IconShapeOption
import com.example.model.IconSizePreset
import com.example.model.LauncherPreferences
import com.example.model.LocalMediaEntry
import com.example.model.MediaCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _preferences = MutableStateFlow(loadFromDisk())
    val preferences: StateFlow<LauncherPreferences> = _preferences.asStateFlow()

    private fun loadFromDisk(): LauncherPreferences {
        val default = LauncherPreferences()
        val shapeName = prefs.getString(KEY_ICON_SHAPE, default.iconShape.name) ?: default.iconShape.name
        val sizePresetName = prefs.getString(KEY_ICON_SIZE_PRESET, default.iconSizePreset.name)
            ?: default.iconSizePreset.name
        val fontName = prefs.getString(KEY_SELECTED_FONT, default.selectedFont.name)
            ?: default.selectedFont.name
        val bgName = prefs.getString(KEY_SELECTED_BG, default.selectedBackground.name)
            ?: default.selectedBackground.name
        val scaleModeName = prefs.getString(KEY_BG_SCALE_MODE, default.backgroundScaleMode.name)
            ?: default.backgroundScaleMode.name

        val homeOrderStr = prefs.getString(KEY_HOME_APP_PACKAGES, "") ?: ""
        val homePackages = if (homeOrderStr.isBlank()) {
            emptyList()
        } else {
            homeOrderStr.split("|").filter { it.isNotBlank() }.distinct()
        }

        val hiddenSet = prefs.getStringSet(KEY_HIDDEN_HOME_PACKAGES, emptySet())?.toSet() ?: emptySet()
        val favSet = prefs.getStringSet(KEY_FAVORITE_PACKAGES, emptySet())?.toSet() ?: emptySet()
        val favMediaSet = prefs.getStringSet(KEY_FAVORITE_MEDIA_URIS, emptySet())?.toSet() ?: emptySet()
        val mediaJsonStr = prefs.getString(KEY_CONFIGURED_MEDIA, "[]") ?: "[]"

        return LauncherPreferences(
            iconShape = runCatching { IconShapeOption.valueOf(shapeName) }.getOrDefault(default.iconShape),
            iconSizePreset = runCatching { IconSizePreset.valueOf(sizePresetName) }.getOrDefault(default.iconSizePreset),
            customIconScale = prefs.getFloat(KEY_CUSTOM_ICON_SCALE, default.customIconScale).coerceIn(0.65f, 1.45f),
            showAppLabels = prefs.getBoolean(KEY_SHOW_APP_LABELS, default.showAppLabels),
            appLabelFontSizeSp = prefs.getFloat(KEY_APP_LABEL_SIZE, default.appLabelFontSizeSp).coerceIn(11f, 22f),
            tileSpacingDp = prefs.getFloat(KEY_TILE_SPACING, default.tileSpacingDp).coerceIn(6f, 32f),
            tileCornerRadiusDp = prefs.getFloat(KEY_TILE_CORNER_RADIUS, default.tileCornerRadiusDp).coerceIn(0f, 44f),
            tileBackgroundOpacity = prefs.getFloat(KEY_TILE_BG_OPACITY, default.tileBackgroundOpacity).coerceIn(0.25f, 1.0f),
            enableFocusGlow = prefs.getBoolean(KEY_ENABLE_FOCUS_GLOW, default.enableFocusGlow),
            focusBorderThicknessDp = prefs.getFloat(KEY_FOCUS_BORDER_THICKNESS, default.focusBorderThicknessDp).coerceIn(1f, 6f),
            selectedFont = runCatching { ArabicFontOption.valueOf(fontName) }.getOrDefault(default.selectedFont),
            globalFontScale = prefs.getFloat(KEY_GLOBAL_FONT_SCALE, default.globalFontScale).coerceIn(0.80f, 1.35f),
            selectedBackground = runCatching { BuiltInBackground.valueOf(bgName) }.getOrDefault(default.selectedBackground),
            customBackgroundUri = prefs.getString(KEY_CUSTOM_BG_URI, null),
            backgroundScaleMode = runCatching { BackgroundScaleMode.valueOf(scaleModeName) }.getOrDefault(default.backgroundScaleMode),
            backgroundDimOverlay = prefs.getFloat(KEY_BG_DIM_OVERLAY, default.backgroundDimOverlay).coerceIn(0f, 0.75f),
            use24HourFormat = prefs.getBoolean(KEY_USE_24_HOUR, default.use24HourFormat),
            showClock = prefs.getBoolean(KEY_SHOW_CLOCK, default.showClock),
            showDate = prefs.getBoolean(KEY_SHOW_DATE, default.showDate),
            clockFontSizeSp = prefs.getFloat(KEY_CLOCK_FONT_SIZE, default.clockFontSizeSp).coerceIn(18f, 48f),
            dateFontSizeSp = prefs.getFloat(KEY_DATE_FONT_SIZE, default.dateFontSizeSp).coerceIn(11f, 24f),
            homeAppPackages = homePackages,
            hiddenHomePackages = hiddenSet,
            favoritePackages = favSet,
            favoriteMediaUris = favMediaSet,
            showAllAppsTileOnHome = prefs.getBoolean(KEY_SHOW_ALL_APPS_TILE, default.showAllAppsTileOnHome),
            includeSelfInAppList = prefs.getBoolean(KEY_INCLUDE_SELF, default.includeSelfInAppList),
            wrapAroundNavigation = prefs.getBoolean(KEY_WRAP_NAVIGATION, default.wrapAroundNavigation),
            restoreFocusOnReturn = prefs.getBoolean(KEY_RESTORE_FOCUS, default.restoreFocusOnReturn),
            supportAlternativeMenuKeys = prefs.getBoolean(KEY_ALT_MENU_KEYS, default.supportAlternativeMenuKeys),
            lowEndDeviceMode = prefs.getBoolean(KEY_LOW_END_MODE, default.lowEndDeviceMode),
            configuredMediaItems = parseMediaItems(mediaJsonStr)
        )
    }

    fun update(transform: (LauncherPreferences) -> LauncherPreferences) {
        val updated = transform(_preferences.value)
        _preferences.value = updated
        saveToDisk(updated)
    }

    fun restoreDefaults() {
        prefs.edit().clear().apply()
        _preferences.value = LauncherPreferences()
    }

    fun restoreDefaultHomeLayout() {
        update {
            it.copy(
                homeAppPackages = emptyList(),
                hiddenHomePackages = emptySet(),
                showAllAppsTileOnHome = true
            )
        }
    }

    private fun saveToDisk(state: LauncherPreferences) {
        prefs.edit().apply {
            putString(KEY_ICON_SHAPE, state.iconShape.name)
            putString(KEY_ICON_SIZE_PRESET, state.iconSizePreset.name)
            putFloat(KEY_CUSTOM_ICON_SCALE, state.customIconScale)
            putBoolean(KEY_SHOW_APP_LABELS, state.showAppLabels)
            putFloat(KEY_APP_LABEL_SIZE, state.appLabelFontSizeSp)
            putFloat(KEY_TILE_SPACING, state.tileSpacingDp)
            putFloat(KEY_TILE_CORNER_RADIUS, state.tileCornerRadiusDp)
            putFloat(KEY_TILE_BG_OPACITY, state.tileBackgroundOpacity)
            putBoolean(KEY_ENABLE_FOCUS_GLOW, state.enableFocusGlow)
            putFloat(KEY_FOCUS_BORDER_THICKNESS, state.focusBorderThicknessDp)
            putString(KEY_SELECTED_FONT, state.selectedFont.name)
            putFloat(KEY_GLOBAL_FONT_SCALE, state.globalFontScale)
            putString(KEY_SELECTED_BG, state.selectedBackground.name)
            if (state.customBackgroundUri != null) {
                putString(KEY_CUSTOM_BG_URI, state.customBackgroundUri)
            } else {
                remove(KEY_CUSTOM_BG_URI)
            }
            putString(KEY_BG_SCALE_MODE, state.backgroundScaleMode.name)
            putFloat(KEY_BG_DIM_OVERLAY, state.backgroundDimOverlay)
            putBoolean(KEY_USE_24_HOUR, state.use24HourFormat)
            putBoolean(KEY_SHOW_CLOCK, state.showClock)
            putBoolean(KEY_SHOW_DATE, state.showDate)
            putFloat(KEY_CLOCK_FONT_SIZE, state.clockFontSizeSp)
            putFloat(KEY_DATE_FONT_SIZE, state.dateFontSizeSp)
            putString(KEY_HOME_APP_PACKAGES, state.homeAppPackages.distinct().joinToString("|"))
            putStringSet(KEY_HIDDEN_HOME_PACKAGES, state.hiddenHomePackages)
            putStringSet(KEY_FAVORITE_PACKAGES, state.favoritePackages)
            putStringSet(KEY_FAVORITE_MEDIA_URIS, state.favoriteMediaUris)
            putBoolean(KEY_SHOW_ALL_APPS_TILE, state.showAllAppsTileOnHome)
            putBoolean(KEY_INCLUDE_SELF, state.includeSelfInAppList)
            putBoolean(KEY_WRAP_NAVIGATION, state.wrapAroundNavigation)
            putBoolean(KEY_RESTORE_FOCUS, state.restoreFocusOnReturn)
            putBoolean(KEY_ALT_MENU_KEYS, state.supportAlternativeMenuKeys)
            putBoolean(KEY_LOW_END_MODE, state.lowEndDeviceMode)
            putString(KEY_CONFIGURED_MEDIA, encodeMediaItems(state.configuredMediaItems))
            apply()
        }
    }

    private fun parseMediaItems(jsonStr: String): List<LocalMediaEntry> {
        return runCatching {
            val array = JSONArray(jsonStr)
            val result = ArrayList<LocalMediaEntry>(array.length())
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val catName = obj.optString("category", MediaCategory.DOCUMENT_OR_FILE.name)
                val category = runCatching { MediaCategory.valueOf(catName) }
                    .getOrDefault(MediaCategory.DOCUMENT_OR_FILE)
                result.add(
                    LocalMediaEntry(
                        uriString = obj.getString("uri"),
                        title = obj.optString("title", "ملف محلي"),
                        mimeType = obj.optString("mimeType", "*/*"),
                        sizeBytes = obj.optLong("sizeBytes", 0L),
                        category = category,
                        addedTimestamp = obj.optLong("addedTimestamp", 0L)
                    )
                )
            }
            result
        }.getOrDefault(emptyList())
    }

    private fun encodeMediaItems(items: List<LocalMediaEntry>): String {
        val array = JSONArray()
        for (item in items) {
            val obj = JSONObject().apply {
                put("uri", item.uriString)
                put("title", item.title)
                put("mimeType", item.mimeType)
                put("sizeBytes", item.sizeBytes)
                put("category", item.category.name)
                put("addedTimestamp", item.addedTimestamp)
            }
            array.put(obj)
        }
        return array.toString()
    }

    companion object {
        private const val PREFS_NAME = "nexaview_tv_prefs"
        private const val KEY_ICON_SHAPE = "icon_shape"
        private const val KEY_ICON_SIZE_PRESET = "icon_size_preset"
        private const val KEY_CUSTOM_ICON_SCALE = "custom_icon_scale"
        private const val KEY_SHOW_APP_LABELS = "show_app_labels"
        private const val KEY_APP_LABEL_SIZE = "app_label_size"
        private const val KEY_TILE_SPACING = "tile_spacing"
        private const val KEY_TILE_CORNER_RADIUS = "tile_corner_radius"
        private const val KEY_TILE_BG_OPACITY = "tile_bg_opacity"
        private const val KEY_ENABLE_FOCUS_GLOW = "enable_focus_glow"
        private const val KEY_FOCUS_BORDER_THICKNESS = "focus_border_thickness"
        private const val KEY_SELECTED_FONT = "selected_font"
        private const val KEY_GLOBAL_FONT_SCALE = "global_font_scale"
        private const val KEY_SELECTED_BG = "selected_bg"
        private const val KEY_CUSTOM_BG_URI = "custom_bg_uri"
        private const val KEY_BG_SCALE_MODE = "bg_scale_mode"
        private const val KEY_BG_DIM_OVERLAY = "bg_dim_overlay"
        private const val KEY_USE_24_HOUR = "use_24_hour"
        private const val KEY_SHOW_CLOCK = "show_clock"
        private const val KEY_SHOW_DATE = "show_date"
        private const val KEY_CLOCK_FONT_SIZE = "clock_font_size"
        private const val KEY_DATE_FONT_SIZE = "date_font_size"
        private const val KEY_HOME_APP_PACKAGES = "home_app_packages"
        private const val KEY_HIDDEN_HOME_PACKAGES = "hidden_home_packages"
        private const val KEY_FAVORITE_PACKAGES = "favorite_packages"
        private const val KEY_FAVORITE_MEDIA_URIS = "favorite_media_uris"
        private const val KEY_SHOW_ALL_APPS_TILE = "show_all_apps_tile"
        private const val KEY_INCLUDE_SELF = "include_self"
        private const val KEY_WRAP_NAVIGATION = "wrap_navigation"
        private const val KEY_RESTORE_FOCUS = "restore_focus"
        private const val KEY_ALT_MENU_KEYS = "alt_menu_keys"
        private const val KEY_LOW_END_MODE = "low_end_mode"
        private const val KEY_CONFIGURED_MEDIA = "configured_media"
    }
}
