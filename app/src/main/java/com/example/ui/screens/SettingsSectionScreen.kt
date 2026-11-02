package com.example.ui.screens

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppInfoItem
import com.example.model.ArabicFontOption
import com.example.model.BackgroundScaleMode
import com.example.model.BuiltInBackground
import com.example.model.IconShapeOption
import com.example.model.IconSizePreset
import com.example.model.LauncherPreferences
import com.example.model.SettingsSection
import com.example.ui.LauncherViewModel
import com.example.ui.theme.LocalActiveFontFamily
import com.example.ui.theme.NexaBlueAccent
import com.example.ui.theme.NexaCyanGlow
import com.example.ui.theme.NexaPanelNavy
import com.example.ui.theme.NexaPurpleAccent
import com.example.ui.theme.NexaTextMuted
import com.example.ui.theme.NexaTextPrimary
import com.example.ui.theme.NexaTextSecondary
import com.example.ui.theme.NexaTypography
import kotlin.math.roundToInt

@Composable
fun SettingsSectionScreen(
    viewModel: LauncherViewModel,
    preferences: LauncherPreferences,
    activeSubSection: SettingsSection,
    installedApps: List<AppInfoItem>,
    homeApps: List<AppInfoItem>,
    pendingPreviewBitmap: ImageBitmap?,
    onPickCustomBackgroundFromFiles: () -> Unit,
    firstSettingFocusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    val activeFont = LocalActiveFontFamily.current

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag("settings_screen"),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Right-side (in RTL start) Settings Sub-Sections column
            LazyColumn(
                modifier = Modifier
                    .width(245.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(NexaPanelNavy)
                    .border(1.dp, Color(0xFF283552), RoundedCornerShape(20.dp))
                    .padding(10.dp)
                    .testTag("settings_categories_list"),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(SettingsSection.entries.size) { index ->
                    val section = SettingsSection.entries[index]
                    val isSelected = section == activeSubSection
                    val interactionSource = remember { MutableInteractionSource() }
                    val isFocused by interactionSource.collectIsFocusedAsState()
                    val itemShape = RoundedCornerShape(14.dp)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clip(itemShape)
                            .background(
                                brush = when {
                                    isSelected -> Brush.horizontalGradient(
                                        listOf(NexaPurpleAccent, NexaBlueAccent)
                                    )
                                    isFocused -> Brush.horizontalGradient(
                                        listOf(Color(0xFF24345A), Color(0xFF1B2644))
                                    )
                                    else -> Brush.horizontalGradient(
                                        listOf(Color.Transparent, Color.Transparent)
                                    )
                                },
                                shape = itemShape
                            )
                            .border(
                                width = if (isFocused) 2.dp else 0.dp,
                                color = if (isFocused) Color.White else Color.Transparent,
                                shape = itemShape
                            )
                            .then(if (index == 0) Modifier.focusRequester(firstSettingFocusRequester) else Modifier)
                            .focusable(interactionSource = interactionSource)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = { viewModel.selectSettingsSection(section) }
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag(section.testTag)
                    ) {
                        Text(
                            text = section.arabicTitle,
                            fontFamily = activeFont,
                            fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium,
                            fontSize = (14.5f * preferences.globalFontScale).sp,
                            color = if (isSelected || isFocused) NexaTextPrimary else NexaTextSecondary
                        )
                    }
                }
            }

            // Active Settings Detail Pane
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .testTag("settings_detail_pane"),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 28.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(NexaPanelNavy)
                            .border(1.dp, Color(0xFF283552), RoundedCornerShape(18.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = activeSubSection.arabicTitle,
                            fontFamily = activeFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = (20f * preferences.globalFontScale).sp,
                            color = NexaTextPrimary
                        )
                        Text(
                            text = activeSubSection.subtitle,
                            fontFamily = activeFont,
                            fontSize = (13f * preferences.globalFontScale).sp,
                            color = NexaTextMuted
                        )
                    }
                }

                when (activeSubSection) {
                    SettingsSection.ICONS -> {
                        item {
                            SettingsOptionSelectorCard(
                                title = "شكل بلاطة وأيقونة التطبيق",
                                options = IconShapeOption.entries,
                                selectedOption = preferences.iconShape,
                                optionLabel = { it.arabicLabel },
                                optionTestTag = { "icon_shape_${it.name.lowercase()}" },
                                globalFontScale = preferences.globalFontScale,
                                onSelect = { viewModel.updateIconShape(it) }
                            )
                        }
                        item {
                            SettingsOptionSelectorCard(
                                title = "أحجام الأيقونات الجاهزة (Size Presets)",
                                options = IconSizePreset.entries,
                                selectedOption = preferences.iconSizePreset,
                                optionLabel = { it.arabicLabel },
                                optionTestTag = { "icon_preset_${it.name.lowercase()}" },
                                globalFontScale = preferences.globalFontScale,
                                onSelect = { viewModel.updateIconSizePreset(it) }
                            )
                        }
                        item {
                            SettingsSliderAndStepperRow(
                                title = "شريط التحكم الدقيق بحجم الأيقونة",
                                valueText = "${(preferences.customIconScale * 100).roundToInt()}%",
                                value = preferences.customIconScale,
                                valueRange = 0.65f..1.45f,
                                stepDelta = 0.05f,
                                globalFontScale = preferences.globalFontScale,
                                onValueChange = { viewModel.updateCustomIconScale(it) },
                                testTag = "slider_icon_scale"
                            )
                        }
                        item {
                            SettingsToggleRow(
                                title = "إظهار أسماء التطبيقات أسفل الأيقونات",
                                subtitle = "إظهار أو إخفاء تسمية التطبيق داخل البلاطة",
                                checked = preferences.showAppLabels,
                                globalFontScale = preferences.globalFontScale,
                                onCheckedChange = { viewModel.updateShowAppLabels(it) },
                                testTag = "toggle_show_app_labels"
                            )
                        }
                        item {
                            SettingsSliderAndStepperRow(
                                title = "حجم خط اسم التطبيق",
                                valueText = "${preferences.appLabelFontSizeSp.roundToInt()} sp",
                                value = preferences.appLabelFontSizeSp,
                                valueRange = 11f..22f,
                                stepDelta = 1f,
                                globalFontScale = preferences.globalFontScale,
                                onValueChange = { viewModel.updateAppLabelFontSize(it) }
                            )
                        }
                        item {
                            SettingsSliderAndStepperRow(
                                title = "المسافة بين البلاطات (Tile Spacing)",
                                valueText = "${preferences.tileSpacingDp.roundToInt()} dp",
                                value = preferences.tileSpacingDp,
                                valueRange = 6f..32f,
                                stepDelta = 2f,
                                globalFontScale = preferences.globalFontScale,
                                onValueChange = { viewModel.updateTileSpacing(it) }
                            )
                        }
                        item {
                            SettingsSliderAndStepperRow(
                                title = "استدارة الحواف (Corner Radius)",
                                valueText = "${preferences.tileCornerRadiusDp.roundToInt()} dp",
                                value = preferences.tileCornerRadiusDp,
                                valueRange = 0f..44f,
                                stepDelta = 2f,
                                globalFontScale = preferences.globalFontScale,
                                onValueChange = { viewModel.updateTileCornerRadius(it) }
                            )
                        }
                        item {
                            SettingsSliderAndStepperRow(
                                title = "شفافية خلفية البلاطة (Background Opacity)",
                                valueText = "${(preferences.tileBackgroundOpacity * 100).roundToInt()}%",
                                value = preferences.tileBackgroundOpacity,
                                valueRange = 0.25f..1.0f,
                                stepDelta = 0.05f,
                                globalFontScale = preferences.globalFontScale,
                                onValueChange = { viewModel.updateTileBackgroundOpacity(it) }
                            )
                        }
                        item {
                            SettingsToggleRow(
                                title = "تفعيل توهج التركيز (Focus Glow)",
                                subtitle = "إضافة هالة ضوئية زرقاء وبنفسجية حول التطبيق المحدد بالريموت",
                                checked = preferences.enableFocusGlow,
                                globalFontScale = preferences.globalFontScale,
                                onCheckedChange = { viewModel.updateEnableFocusGlow(it) },
                                testTag = "toggle_focus_glow"
                            )
                        }
                        item {
                            SettingsSliderAndStepperRow(
                                title = "سُمك إطار التركيز (Focus Border Thickness)",
                                valueText = "${preferences.focusBorderThicknessDp} dp",
                                value = preferences.focusBorderThicknessDp,
                                valueRange = 1f..6f,
                                stepDelta = 0.5f,
                                globalFontScale = preferences.globalFontScale,
                                onValueChange = { viewModel.updateFocusBorderThickness(it) }
                            )
                        }
                    }

                    SettingsSection.FONTS -> {
                        item {
                            SettingsOptionSelectorCard(
                                title = "اختر الخط العربي للمشغل (تغيير فعلي لعائلة الخط)",
                                options = ArabicFontOption.entries,
                                selectedOption = preferences.selectedFont,
                                optionLabel = { it.arabicDisplayName },
                                optionFontFamily = { NexaTypography.getFontFamily(it) },
                                optionTestTag = { "font_option_${it.name.lowercase()}" },
                                globalFontScale = preferences.globalFontScale,
                                onSelect = { viewModel.updateSelectedFont(it) }
                            )
                        }
                        item {
                            SettingsSliderAndStepperRow(
                                title = "مقياس حجم الخط العام في الواجهة",
                                valueText = "${(preferences.globalFontScale * 100).roundToInt()}%",
                                value = preferences.globalFontScale,
                                valueRange = 0.80f..1.35f,
                                stepDelta = 0.05f,
                                globalFontScale = preferences.globalFontScale,
                                onValueChange = { viewModel.updateGlobalFontScale(it) },
                                testTag = "slider_global_font_scale"
                            )
                        }
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(NexaPanelNavy)
                                    .border(1.dp, NexaCyanGlow.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                                    .testTag("font_live_preview_card")
                            ) {
                                Text(
                                    text = "معاينة حية للخط المختار: ${preferences.selectedFont.arabicDisplayName}",
                                    fontFamily = activeFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = (18f * preferences.globalFontScale).sp,
                                    color = NexaCyanGlow
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "NexaView TV — مشغل تلفاز أندرويد خفيف وسريع يدعم اللغة العربية وتنسيق RTL بالكامل (0123456789).",
                                    fontFamily = activeFont,
                                    fontSize = (15.5f * preferences.globalFontScale).sp,
                                    color = NexaTextPrimary
                                )
                            }
                        }
                    }

                    SettingsSection.BACKGROUND -> {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                RemoteActionChip(
                                    label = "اختيار خلفية من الملفات (JPEG / PNG / WebP)",
                                    onClick = onPickCustomBackgroundFromFiles,
                                    globalFontScale = preferences.globalFontScale,
                                    testTag = "btn_pick_background_from_files"
                                )
                                RemoteActionChip(
                                    label = "استعادة الخلفية الافتراضية",
                                    onClick = { viewModel.resetToDefaultBackground() },
                                    globalFontScale = preferences.globalFontScale,
                                    testTag = "btn_reset_default_background"
                                )
                            }
                        }

                        if (pendingPreviewBitmap != null) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF17223B))
                                        .border(2.dp, NexaCyanGlow, RoundedCornerShape(16.dp))
                                        .padding(16.dp)
                                        .testTag("custom_background_preview_box")
                                ) {
                                    Text(
                                        text = "معاينة الخلفية المختارة قبل التطبيق:",
                                        fontFamily = activeFont,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = (16f * preferences.globalFontScale).sp,
                                        color = NexaTextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Image(
                                        bitmap = pendingPreviewBitmap,
                                        contentDescription = "معاينة الخلفية",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(150.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        RemoteActionChip(
                                            label = "تطبيق الخلفية الآن",
                                            onClick = { viewModel.applyPreviewedCustomBackground() },
                                            globalFontScale = preferences.globalFontScale,
                                            testTag = "btn_apply_preview_background"
                                        )
                                        RemoteActionChip(
                                            label = "إلغاء المعاينة",
                                            onClick = { viewModel.cancelBackgroundPreview() },
                                            globalFontScale = preferences.globalFontScale,
                                            testTag = "btn_cancel_preview_background"
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            SettingsOptionSelectorCard(
                                title = "الخلفيات الثابتة الخفيفة المدمجة",
                                options = BuiltInBackground.entries.filter {
                                    it != BuiltInBackground.CUSTOM_IMAGE || preferences.customBackgroundUri != null
                                },
                                selectedOption = preferences.selectedBackground,
                                optionLabel = { it.arabicLabel },
                                optionTestTag = { "bg_preset_${it.name.lowercase()}" },
                                globalFontScale = preferences.globalFontScale,
                                onSelect = { viewModel.selectBuiltInBackground(it) }
                            )
                        }

                        item {
                            SettingsOptionSelectorCard(
                                title = "طريقة عرض وتحجيم صورة الخلفية المخصصة",
                                options = BackgroundScaleMode.entries,
                                selectedOption = preferences.backgroundScaleMode,
                                optionLabel = { it.arabicLabel },
                                optionTestTag = { "bg_scale_${it.name.lowercase()}" },
                                globalFontScale = preferences.globalFontScale,
                                onSelect = { viewModel.updateBackgroundScaleMode(it) }
                            )
                        }

                        item {
                            SettingsSliderAndStepperRow(
                                title = "درجة تعتيم الخلفية للحفاظ على وضوح التطبيقات",
                                valueText = "${(preferences.backgroundDimOverlay * 100).roundToInt()}%",
                                value = preferences.backgroundDimOverlay,
                                valueRange = 0f..0.75f,
                                stepDelta = 0.05f,
                                globalFontScale = preferences.globalFontScale,
                                onValueChange = { viewModel.updateBackgroundDimOverlay(it) }
                            )
                        }
                    }

                    SettingsSection.CLOCK -> {
                        item {
                            SettingsToggleRow(
                                title = "استخدام نظام 24 ساعة",
                                subtitle = if (preferences.use24HourFormat) "الحالي: صيغة 24 ساعة (مثال 20:45)" else "الحالي: صيغة 12 ساعة (مثال 08:45 PM)",
                                checked = preferences.use24HourFormat,
                                globalFontScale = preferences.globalFontScale,
                                onCheckedChange = { viewModel.updateUse24HourFormat(it) },
                                testTag = "toggle_24h_clock"
                            )
                        }
                        item {
                            SettingsToggleRow(
                                title = "إظهار الساعة في الشريط العلوي",
                                checked = preferences.showClock,
                                globalFontScale = preferences.globalFontScale,
                                onCheckedChange = { viewModel.updateShowClock(it) },
                                testTag = "toggle_show_clock"
                            )
                        }
                        item {
                            SettingsToggleRow(
                                title = "إظهار التاريخ العربي في الشريط العلوي",
                                checked = preferences.showDate,
                                globalFontScale = preferences.globalFontScale,
                                onCheckedChange = { viewModel.updateShowDate(it) },
                                testTag = "toggle_show_date"
                            )
                        }
                        item {
                            SettingsSliderAndStepperRow(
                                title = "حجم خط الساعة",
                                valueText = "${preferences.clockFontSizeSp.roundToInt()} sp",
                                value = preferences.clockFontSizeSp,
                                valueRange = 18f..48f,
                                stepDelta = 2f,
                                globalFontScale = preferences.globalFontScale,
                                onValueChange = { viewModel.updateClockFontSize(it) }
                            )
                        }
                        item {
                            SettingsSliderAndStepperRow(
                                title = "حجم خط التاريخ",
                                valueText = "${preferences.dateFontSizeSp.roundToInt()} sp",
                                value = preferences.dateFontSizeSp,
                                valueRange = 11f..24f,
                                stepDelta = 1f,
                                globalFontScale = preferences.globalFontScale,
                                onValueChange = { viewModel.updateDateFontSize(it) }
                            )
                        }
                    }

                    SettingsSection.HOME_APPS -> {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                RemoteActionChip(
                                    label = "استعادة الترتيب الافتراضي للشاشة الرئيسية",
                                    onClick = { viewModel.restoreDefaultHomeLayout() },
                                    globalFontScale = preferences.globalFontScale,
                                    testTag = "btn_restore_default_home_apps"
                                )
                            }
                        }
                        item {
                            SettingsToggleRow(
                                title = "إظهار بلاطة «كل التطبيقات» في الشاشة الرئيسية",
                                checked = preferences.showAllAppsTileOnHome,
                                globalFontScale = preferences.globalFontScale,
                                onCheckedChange = { viewModel.updateShowAllAppsTileOnHome(it) },
                                testTag = "toggle_all_apps_tile_on_home"
                            )
                        }

                        val currentHomeRealApps = homeApps.filter { !it.isAllAppsShortcut }
                        val homePkgSet = currentHomeRealApps.map { it.packageName }.toSet()

                        items(installedApps, key = { "home_cfg_${it.packageName}" }) { app ->
                            val isOnHome = app.packageName in homePkgSet
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(NexaPanelNavy)
                                    .border(1.dp, Color(0xFF283552), RoundedCornerShape(14.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = app.label,
                                            fontFamily = activeFont,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = (15f * preferences.globalFontScale).sp,
                                            color = NexaTextPrimary
                                        )
                                        Text(
                                            text = if (isOnHome) "معروض في الشاشة الرئيسية" else "مخفي من الشاشة الرئيسية",
                                            fontFamily = activeFont,
                                            fontSize = (12f * preferences.globalFontScale).sp,
                                            color = if (isOnHome) NexaCyanGlow else NexaTextMuted
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (isOnHome) {
                                            RemoteActionChip(
                                                label = "تقديم ▲",
                                                onClick = { viewModel.moveHomeAppOrder(app.packageName, -1) },
                                                globalFontScale = preferences.globalFontScale
                                            )
                                            RemoteActionChip(
                                                label = "تأخير ▼",
                                                onClick = { viewModel.moveHomeAppOrder(app.packageName, 1) },
                                                globalFontScale = preferences.globalFontScale
                                            )
                                            RemoteActionChip(
                                                label = "إزالة من الرئيسية",
                                                onClick = { viewModel.removeAppFromHomeScreen(app.packageName) },
                                                globalFontScale = preferences.globalFontScale
                                            )
                                        } else {
                                            RemoteActionChip(
                                                label = "إضافة للرئيسية",
                                                onClick = { viewModel.addAppToHomeScreen(app.packageName) },
                                                globalFontScale = preferences.globalFontScale
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    SettingsSection.FAVORITES -> {
                        items(installedApps, key = { "fav_cfg_${it.packageName}" }) { app ->
                            val isFav = app.packageName in preferences.favoritePackages
                            SettingsToggleRow(
                                title = app.label,
                                subtitle = app.packageName,
                                checked = isFav,
                                globalFontScale = preferences.globalFontScale,
                                onCheckedChange = { viewModel.toggleFavoriteApp(app.packageName) }
                            )
                        }
                    }

                    SettingsSection.NAVIGATION -> {
                        item {
                            SettingsToggleRow(
                                title = "استعادة التركيز السابق عند العودة من الإعدادات أو القوائم",
                                subtitle = "يعيد المؤشر تلقائياً إلى آخر تطبيق كان محدداً",
                                checked = preferences.restoreFocusOnReturn,
                                globalFontScale = preferences.globalFontScale,
                                onCheckedChange = { viewModel.updateRestoreFocusOnReturn(it) }
                            )
                        }
                        item {
                            SettingsToggleRow(
                                title = "دعم أزرار الريموت البديلة لفتح قائمة التطبيق (INFO / GUIDE / BOOKMARK)",
                                subtitle = "مفيد لأجهزة التلفاز التي لا يحتوي الريموت الخاص بها على زر MENU مستقل",
                                checked = preferences.supportAlternativeMenuKeys,
                                globalFontScale = preferences.globalFontScale,
                                onCheckedChange = { viewModel.updateSupportAlternativeMenuKeys(it) }
                            )
                        }
                        item {
                            SettingsToggleRow(
                                title = "إظهار NexaView TV نفسه ضمن قائمة كل التطبيقات",
                                subtitle = "الافتراضي: مستبعد من قائمة التطبيقات العادية لتجنب التكرار",
                                checked = preferences.includeSelfInAppList,
                                globalFontScale = preferences.globalFontScale,
                                onCheckedChange = { viewModel.updateIncludeSelfInAppList(it) }
                            )
                        }
                    }

                    SettingsSection.LOW_END_MODE -> {
                        item {
                            SettingsToggleRow(
                                title = "تفعيل وضع الأجهزة الضعيفة (Low-End Device Mode)",
                                subtitle = "يوقف توهج الحواف غير الضروري، يختصر زمن الانتقالات، ويقلل استهلاك الذاكرة للأجهزة ذات 1GB / 2GB RAM",
                                checked = preferences.lowEndDeviceMode,
                                globalFontScale = preferences.globalFontScale,
                                onCheckedChange = { viewModel.updateLowEndDeviceMode(it) },
                                testTag = "toggle_low_end_device_mode"
                            )
                        }
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(NexaPanelNavy)
                                    .border(1.dp, Color(0xFF283552), RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "ما الذي يقوم به وضع الأجهزة الضعيفة؟",
                                    fontFamily = activeFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = (16f * preferences.globalFontScale).sp,
                                    color = NexaCyanGlow
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "• إيقاف تأثيرات التوهج والظلال المتعددة الطبقات.\n" +
                                        "• تقليل دقة فك ترميز الخلفيات والأيقونات إلى الحجم المحافظ (RGB_565) لتوفير الذاكرة العشوائية.\n" +
                                        "• إلغاء الحركات الانتقالية لإعطاء استجابة فورية لأزرار الريموت.\n" +
                                        "• الحفاظ الكامل على مراقبة حالة الواي فاي، تشغيل التطبيقات، الخطوط العربية، وتخطيط الصفين.",
                                    fontFamily = activeFont,
                                    fontSize = (13.5f * preferences.globalFontScale).sp,
                                    color = NexaTextSecondary
                                )
                            }
                        }
                    }

                    SettingsSection.RESTORE_DEFAULTS -> {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(NexaPanelNavy)
                                    .border(1.dp, Color(0xFF283552), RoundedCornerShape(16.dp))
                                    .padding(18.dp)
                            ) {
                                Text(
                                    text = "إعادة ضبط المصنع لإعدادات NexaView TV",
                                    fontFamily = activeFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = (17f * preferences.globalFontScale).sp,
                                    color = NexaTextPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "سيؤدي هذا الخيار إلى استعادة الخط الافتراضي (Cairo)، الخلفية الكحلية الأصلية، أشكال الأيقونات، وترتيب الشاشة الرئيسية دون حذف أي تطبيق مثبت على جهازك.",
                                    fontFamily = activeFont,
                                    fontSize = (14f * preferences.globalFontScale).sp,
                                    color = NexaTextSecondary
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                RemoteActionChip(
                                    label = "استعادة جميع الإعدادات الافتراضية الآن",
                                    onClick = { viewModel.restoreAllDefaults() },
                                    globalFontScale = preferences.globalFontScale,
                                    testTag = "btn_confirm_restore_defaults"
                                )
                            }
                        }
                    }

                    SettingsSection.APP_INFO -> {
                        item {
                            SystemAndLauncherInfoCard(globalFontScale = preferences.globalFontScale)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SystemAndLauncherInfoCard(globalFontScale: Float) {
    val context = LocalContext.current
    val activeFont = LocalActiveFontFamily.current

    val memSummary = remember {
        runCatching {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val info = ActivityManager.MemoryInfo()
            am?.getMemoryInfo(info)
            val totalMb = info.totalMem / (1024L * 1024L)
            val availMb = info.availMem / (1024L * 1024L)
            "إجمالي الذاكرة (RAM): $totalMb ميجابايت • المتاح حالياً: $availMb ميجابايت"
        }.getOrDefault("معلومات الذاكرة غير متاحة")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NexaPanelNavy)
            .border(1.dp, Color(0xFF283552), RoundedCornerShape(16.dp))
            .padding(18.dp)
            .testTag("launcher_app_info_card"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "اسم التطبيق: NexaView TV",
            fontFamily = activeFont,
            fontWeight = FontWeight.Bold,
            fontSize = (17f * globalFontScale).sp,
            color = NexaTextPrimary
        )
        Text(
            text = "معرّف الحزمة (Application ID): com.ahmedsalam.nexaviewtv",
            fontFamily = activeFont,
            fontSize = (14f * globalFontScale).sp,
            color = NexaCyanGlow
        )
        Text(
            text = "الإصدار: 1.0 • الحد الأدنى المدعوم: Android 7.0 (API 24) • النظام الحالي: Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            fontFamily = activeFont,
            fontSize = (13.5f * globalFontScale).sp,
            color = NexaTextSecondary
        )
        Text(
            text = memSummary,
            fontFamily = activeFont,
            fontSize = (13.5f * globalFontScale).sp,
            color = NexaTextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "ملاحظات التوافق وأجهزة التلفاز:\n" +
                "• يدعم المشغل فئات HOME و DEFAULT و LEANBACK_LAUNCHER للعمل كواجهة رئيسية لأجهزة Android TV و TV Boxes.\n" +
                "• بعض الشركات المصنعة قد تقيد تغيير المشغل الافتراضي تلقائياً من داخل النظام؛ يمكن تعيين NexaView TV كمشغل افتراضي من إعدادات النظام (التطبيقات الافتراضية -> تطبيق الشاشة الرئيسية).\n" +
                "• في أجهزة الريموت التي لا تحتوي على زر MENU مخصص، يمكن فتح قائمة التطبيق عبر الضغطة المطولة على زر OK أو عبر أزرار INFO / GUIDE.",
            fontFamily = activeFont,
            fontSize = (13f * globalFontScale).sp,
            color = NexaTextMuted
        )
    }
}
