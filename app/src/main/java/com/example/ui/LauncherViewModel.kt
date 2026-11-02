package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppRepository
import com.example.data.BackgroundAndMediaHelper
import com.example.data.ConnectivityObserver
import com.example.data.PreferencesManager
import com.example.model.AppInfoItem
import com.example.model.ArabicFontOption
import com.example.model.BackgroundScaleMode
import com.example.model.BuiltInBackground
import com.example.model.IconShapeOption
import com.example.model.IconSizePreset
import com.example.model.InternetStatus
import com.example.model.LauncherPreferences
import com.example.model.LocalMediaEntry
import com.example.model.MediaCategory
import com.example.model.NavSection
import com.example.model.SettingsSection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    val preferencesManager = PreferencesManager(application)
    val appRepository = AppRepository(application, viewModelScope)
    val connectivityObserver = ConnectivityObserver(application)
    val mediaHelper = BackgroundAndMediaHelper(application)

    val preferences: StateFlow<LauncherPreferences> = preferencesManager.preferences
    val installedApps: StateFlow<List<AppInfoItem>> = appRepository.installedApps
    val isLoadingApps: StateFlow<Boolean> = appRepository.isLoading
    val internetStatus: StateFlow<InternetStatus> = connectivityObserver.status

    private val _currentSection = MutableStateFlow(NavSection.HOME)
    val currentSection: StateFlow<NavSection> = _currentSection.asStateFlow()

    private val _sectionBackStack = ArrayDeque<NavSection>()

    private val _activeSettingsSection = MutableStateFlow(SettingsSection.ICONS)
    val activeSettingsSection: StateFlow<SettingsSection> = _activeSettingsSection.asStateFlow()

    private val _contextMenuApp = MutableStateFlow<AppInfoItem?>(null)
    val contextMenuApp: StateFlow<AppInfoItem?> = _contextMenuApp.asStateFlow()

    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

    private val _customBackgroundBitmap = MutableStateFlow<ImageBitmap?>(null)
    val customBackgroundBitmap: StateFlow<ImageBitmap?> = _customBackgroundBitmap.asStateFlow()

    private val _pendingPreviewBackgroundUri = MutableStateFlow<String?>(null)
    val pendingPreviewBackgroundUri: StateFlow<String?> = _pendingPreviewBackgroundUri.asStateFlow()

    private val _pendingPreviewBackgroundBitmap = MutableStateFlow<ImageBitmap?>(null)
    val pendingPreviewBackgroundBitmap: StateFlow<ImageBitmap?> = _pendingPreviewBackgroundBitmap.asStateFlow()

    private val _discoveredSystemVideos = MutableStateFlow<List<LocalMediaEntry>>(emptyList())
    val discoveredSystemVideos: StateFlow<List<LocalMediaEntry>> = _discoveredSystemVideos.asStateFlow()

    // Focus tracking so we focus YouTube on initial startup without stealing focus during navigation
    var initialStartupFocusDone: Boolean = false
    var lastFocusedHomePackage: String? = null
    var lastFocusedAllAppsPackage: String? = null

    val homeScreenApps: StateFlow<List<AppInfoItem>> = combine(
        installedApps,
        preferences
    ) { allInstalled, prefs ->
        buildHomeAppsList(allInstalled, prefs)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val favoriteApps: StateFlow<List<AppInfoItem>> = combine(
        installedApps,
        preferences
    ) { allInstalled, prefs ->
        allInstalled.filter { it.packageName in prefs.favoritePackages }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        val currentPrefs = preferences.value
        appRepository.registerPackageReceiver()
        connectivityObserver.startObserving()
        appRepository.refreshInstalledApps(
            includeSelf = currentPrefs.includeSelfInAppList,
            lowEndMode = currentPrefs.lowEndDeviceMode,
            forceReload = true
        )
        if (!currentPrefs.customBackgroundUri.isNullOrBlank()) {
            loadPersistedCustomBackground(
                uriString = currentPrefs.customBackgroundUri,
                lowEndMode = currentPrefs.lowEndDeviceMode
            )
        }
        refreshLocalMedia()
    }

    override fun onCleared() {
        super.onCleared()
        appRepository.unregisterPackageReceiver()
        connectivityObserver.stopObserving()
    }

    private fun buildHomeAppsList(
        allInstalled: List<AppInfoItem>,
        prefs: LauncherPreferences
    ): List<AppInfoItem> {
        val installedByPkg = allInstalled.associateBy { it.packageName }
        val result = ArrayList<AppInfoItem>()
        val seenPackages = HashSet<String>()

        if (prefs.homeAppPackages.isNotEmpty()) {
            // Respect user's explicit saved order, excluding uninstalled or hidden apps
            for (pkg in prefs.homeAppPackages) {
                if (pkg in prefs.hiddenHomePackages) continue
                val item = installedByPkg[pkg] ?: continue
                if (seenPackages.add(pkg)) {
                    result.add(item)
                }
            }
        } else {
            // Default automatic home list: place YouTube first when installed, then up to 9 apps
            val youtubeApp = allInstalled.firstOrNull { it.isYouTube && it.packageName !in prefs.hiddenHomePackages }
            if (youtubeApp != null && seenPackages.add(youtubeApp.packageName)) {
                result.add(youtubeApp)
            }
            for (app in allInstalled) {
                if (result.size >= 9) break
                if (app.packageName in prefs.hiddenHomePackages) continue
                if (seenPackages.add(app.packageName)) {
                    result.add(app)
                }
            }
        }

        if (prefs.showAllAppsTileOnHome) {
            result.add(AppRepository.createAllAppsShortcutTile())
        }

        return result
    }

    fun selectNavSection(section: NavSection) {
        val current = _currentSection.value
        if (current != section) {
            _sectionBackStack.addLast(current)
            if (_sectionBackStack.size > 10) {
                _sectionBackStack.removeFirst()
            }
            _currentSection.value = section
        }
    }

    fun navigateBack(): Boolean {
        if (_contextMenuApp.value != null) {
            _contextMenuApp.value = null
            return true
        }
        if (_pendingPreviewBackgroundUri.value != null) {
            cancelBackgroundPreview()
            return true
        }
        if (_currentSection.value != NavSection.HOME) {
            val previous = if (_sectionBackStack.isNotEmpty()) {
                _sectionBackStack.removeLast()
            } else {
                NavSection.HOME
            }
            _currentSection.value = previous
            return true
        }
        return false
    }

    fun selectSettingsSection(section: SettingsSection) {
        _activeSettingsSection.value = section
    }

    fun onAppTileClicked(app: AppInfoItem) {
        if (app.isAllAppsShortcut) {
            selectNavSection(NavSection.APPS)
            return
        }
        val result = appRepository.launchApp(app)
        if (result.isFailure) {
            showMessage("تعذر فتح التطبيق (${app.label}). قد يكون غير مثبت أو معطلاً.")
            appRepository.refreshInstalledApps(
                includeSelf = preferences.value.includeSelfInAppList,
                lowEndMode = preferences.value.lowEndDeviceMode,
                forceReload = true
            )
        }
    }

    fun openContextMenuForApp(app: AppInfoItem) {
        if (app.isAllAppsShortcut) return
        lastFocusedAllAppsPackage = app.packageName
        _contextMenuApp.value = app
    }

    fun dismissContextMenu() {
        _contextMenuApp.value = null
    }

    fun openSystemAppInfoFromContextMenu(app: AppInfoItem) {
        _contextMenuApp.value = null
        val opened = appRepository.openSystemAppInfo(app.packageName)
        if (!opened) {
            showMessage("تعذر فتح شاشة معلومات النظام لهذا التطبيق.")
        }
    }

    fun openAppSpecificSettingsFromContextMenu(app: AppInfoItem) {
        _contextMenuApp.value = null
        val opened = appRepository.openAppSpecificSettings(app.packageName)
        if (!opened) {
            showMessage("لا تتوفر إعدادات مستقلة لهذا التطبيق.")
        }
    }

    fun showMessage(msg: String) {
        _statusBannerMessage.value = msg
    }

    fun clearMessage() {
        _statusBannerMessage.value = null
    }

    // Home Screen Application Customization
    fun addAppToHomeScreen(packageName: String) {
        preferencesManager.update { current ->
            val currentList = if (current.homeAppPackages.isEmpty()) {
                homeScreenApps.value.filter { !it.isAllAppsShortcut }.map { it.packageName }
            } else {
                current.homeAppPackages
            }
            val updatedList = (currentList + packageName).distinct()
            current.copy(
                homeAppPackages = updatedList,
                hiddenHomePackages = current.hiddenHomePackages - packageName
            )
        }
        showMessage("تمت إضافة التطبيق إلى الشاشة الرئيسية.")
    }

    fun removeAppFromHomeScreen(packageName: String) {
        preferencesManager.update { current ->
            val currentList = if (current.homeAppPackages.isEmpty()) {
                homeScreenApps.value.filter { !it.isAllAppsShortcut }.map { it.packageName }
            } else {
                current.homeAppPackages
            }
            current.copy(
                homeAppPackages = currentList.filter { it != packageName },
                hiddenHomePackages = current.hiddenHomePackages + packageName
            )
        }
        showMessage("تمت إزالة التطبيق من الشاشة الرئيسية (دون إلغاء تثبيته).")
    }

    fun moveHomeAppOrder(packageName: String, directionDelta: Int) {
        preferencesManager.update { current ->
            val list = (if (current.homeAppPackages.isEmpty()) {
                homeScreenApps.value.filter { !it.isAllAppsShortcut }.map { it.packageName }
            } else {
                current.homeAppPackages
            }).toMutableList()

            val idx = list.indexOf(packageName)
            if (idx >= 0) {
                val targetIdx = (idx + directionDelta).coerceIn(0, list.lastIndex)
                if (targetIdx != idx) {
                    val item = list.removeAt(idx)
                    list.add(targetIdx, item)
                }
            }
            current.copy(homeAppPackages = list)
        }
    }

    fun toggleFavoriteApp(packageName: String) {
        preferencesManager.update { current ->
            val updatedFavs = if (packageName in current.favoritePackages) {
                current.favoritePackages - packageName
            } else {
                current.favoritePackages + packageName
            }
            current.copy(favoritePackages = updatedFavs)
        }
    }

    fun toggleFavoriteMedia(uriString: String) {
        preferencesManager.update { current ->
            val updatedFavs = if (uriString in current.favoriteMediaUris) {
                current.favoriteMediaUris - uriString
            } else {
                current.favoriteMediaUris + uriString
            }
            current.copy(favoriteMediaUris = updatedFavs)
        }
    }

    fun restoreDefaultHomeLayout() {
        preferencesManager.restoreDefaultHomeLayout()
        showMessage("تمت استعادة التخطيط الافتراضي للشاشة الرئيسية.")
    }

    // Icon Customization
    fun updateIconShape(shape: IconShapeOption) {
        preferencesManager.update { it.copy(iconShape = shape) }
    }

    fun updateIconSizePreset(preset: IconSizePreset) {
        preferencesManager.update {
            it.copy(iconSizePreset = preset, customIconScale = preset.scaleFactor)
        }
    }

    fun updateCustomIconScale(scale: Float) {
        preferencesManager.update { it.copy(customIconScale = scale.coerceIn(0.65f, 1.45f)) }
    }

    fun updateShowAppLabels(show: Boolean) {
        preferencesManager.update { it.copy(showAppLabels = show) }
    }

    fun updateAppLabelFontSize(sizeSp: Float) {
        preferencesManager.update { it.copy(appLabelFontSizeSp = sizeSp.coerceIn(11f, 22f)) }
    }

    fun updateTileSpacing(spacingDp: Float) {
        preferencesManager.update { it.copy(tileSpacingDp = spacingDp.coerceIn(6f, 32f)) }
    }

    fun updateTileCornerRadius(radiusDp: Float) {
        preferencesManager.update { it.copy(tileCornerRadiusDp = radiusDp.coerceIn(0f, 44f)) }
    }

    fun updateTileBackgroundOpacity(opacity: Float) {
        preferencesManager.update { it.copy(tileBackgroundOpacity = opacity.coerceIn(0.25f, 1.0f)) }
    }

    fun updateEnableFocusGlow(enabled: Boolean) {
        preferencesManager.update { it.copy(enableFocusGlow = enabled) }
    }

    fun updateFocusBorderThickness(thicknessDp: Float) {
        preferencesManager.update { it.copy(focusBorderThicknessDp = thicknessDp.coerceIn(1f, 6f)) }
    }

    // Typography
    fun updateSelectedFont(font: ArabicFontOption) {
        preferencesManager.update { it.copy(selectedFont = font) }
    }

    fun updateGlobalFontScale(scale: Float) {
        preferencesManager.update { it.copy(globalFontScale = scale.coerceIn(0.80f, 1.35f)) }
    }

    // Clock & Date
    fun updateUse24HourFormat(use24Hour: Boolean) {
        preferencesManager.update { it.copy(use24HourFormat = use24Hour) }
    }

    fun updateShowClock(show: Boolean) {
        preferencesManager.update { it.copy(showClock = show) }
    }

    fun updateShowDate(show: Boolean) {
        preferencesManager.update { it.copy(showDate = show) }
    }

    fun updateClockFontSize(sizeSp: Float) {
        preferencesManager.update { it.copy(clockFontSizeSp = sizeSp.coerceIn(18f, 48f)) }
    }

    fun updateDateFontSize(sizeSp: Float) {
        preferencesManager.update { it.copy(dateFontSizeSp = sizeSp.coerceIn(11f, 24f)) }
    }

    // Background & Custom File Import
    fun selectBuiltInBackground(bg: BuiltInBackground) {
        preferencesManager.update { it.copy(selectedBackground = bg) }
    }

    fun updateBackgroundScaleMode(mode: BackgroundScaleMode) {
        preferencesManager.update { it.copy(backgroundScaleMode = mode) }
    }

    fun updateBackgroundDimOverlay(dim: Float) {
        preferencesManager.update { it.copy(backgroundDimOverlay = dim.coerceIn(0f, 0.75f)) }
    }

    fun onCustomBackgroundFilePicked(uri: Uri?) {
        if (uri == null) {
            showMessage("تم إلغاء اختيار صورة الخلفية.")
            return
        }
        viewModelScope.launch {
            mediaHelper.takePersistableReadPermission(uri)
            val uriString = uri.toString()
            val decodeResult = mediaHelper.decodeCustomBackground(
                uriString = uriString,
                lowEndMode = preferences.value.lowEndDeviceMode
            )
            decodeResult.onSuccess { bitmap ->
                _pendingPreviewBackgroundUri.value = uriString
                _pendingPreviewBackgroundBitmap.value = bitmap
                showMessage("معاينة الخلفية جاهزة. اضغط على «تطبيق الخلفية الآن» لاعتمادها.")
            }.onFailure { error ->
                showMessage(error.message ?: "تعذر قراءة الصورة المختارة. يرجى اختيار ملف JPEG أو PNG أو WebP صالح.")
            }
        }
    }

    fun applyPreviewedCustomBackground() {
        val previewUri = _pendingPreviewBackgroundUri.value ?: return
        val previewBitmap = _pendingPreviewBackgroundBitmap.value ?: return
        _customBackgroundBitmap.value = previewBitmap
        _pendingPreviewBackgroundUri.value = null
        _pendingPreviewBackgroundBitmap.value = null
        preferencesManager.update {
            it.copy(
                selectedBackground = BuiltInBackground.CUSTOM_IMAGE,
                customBackgroundUri = previewUri
            )
        }
        showMessage("تم تطبيق الخلفية المخصصة وحفظها بنجاح.")
    }

    fun cancelBackgroundPreview() {
        _pendingPreviewBackgroundUri.value = null
        _pendingPreviewBackgroundBitmap.value = null
    }

    fun resetToDefaultBackground() {
        _pendingPreviewBackgroundUri.value = null
        _pendingPreviewBackgroundBitmap.value = null
        _customBackgroundBitmap.value = null
        mediaHelper.clearBackgroundCache()
        preferencesManager.update {
            it.copy(
                selectedBackground = BuiltInBackground.DARK_NAVY,
                customBackgroundUri = null,
                backgroundScaleMode = BackgroundScaleMode.CENTER_CROP,
                backgroundDimOverlay = 0.20f
            )
        }
        showMessage("تمت استعادة الخلفية الافتراضية.")
    }

    private fun loadPersistedCustomBackground(uriString: String, lowEndMode: Boolean) {
        viewModelScope.launch {
            val result = mediaHelper.decodeCustomBackground(uriString, lowEndMode)
            result.onSuccess { bitmap ->
                _customBackgroundBitmap.value = bitmap
            }.onFailure {
                // Fall back to default navy background if persisted file was deleted externally
                preferencesManager.update {
                    it.copy(
                        selectedBackground = BuiltInBackground.DARK_NAVY,
                        customBackgroundUri = null
                    )
                }
                showMessage("تعذر الوصول إلى ملف الخلفية السابق؛ تمت العودة للخلفية الافتراضية.")
            }
        }
    }

    // Low-End Device Mode
    fun updateLowEndDeviceMode(enabled: Boolean) {
        preferencesManager.update {
            it.copy(
                lowEndDeviceMode = enabled,
                enableFocusGlow = if (enabled) false else it.enableFocusGlow
            )
        }
        appRepository.refreshInstalledApps(
            includeSelf = preferences.value.includeSelfInAppList,
            lowEndMode = enabled,
            forceReload = true
        )
        val bgUri = preferences.value.customBackgroundUri
        if (!bgUri.isNullOrBlank()) {
            mediaHelper.clearBackgroundCache()
            loadPersistedCustomBackground(bgUri, enabled)
        }
        showMessage(
            if (enabled) {
                "تم تفعيل وضع الأجهزة الضعيفة لتقليل استهلاك الذاكرة والمعالج."
            } else {
                "تم إيقاف وضع الأجهزة الضعيفة."
            }
        )
    }

    // Navigation & Remote Options
    fun updateWrapAroundNavigation(enabled: Boolean) {
        preferencesManager.update { it.copy(wrapAroundNavigation = enabled) }
    }

    fun updateRestoreFocusOnReturn(enabled: Boolean) {
        preferencesManager.update { it.copy(restoreFocusOnReturn = enabled) }
    }

    fun updateSupportAlternativeMenuKeys(enabled: Boolean) {
        preferencesManager.update { it.copy(supportAlternativeMenuKeys = enabled) }
    }

    fun updateShowAllAppsTileOnHome(enabled: Boolean) {
        preferencesManager.update { it.copy(showAllAppsTileOnHome = enabled) }
    }

    fun updateIncludeSelfInAppList(enabled: Boolean) {
        preferencesManager.update { it.copy(includeSelfInAppList = enabled) }
        appRepository.refreshInstalledApps(
            includeSelf = enabled,
            lowEndMode = preferences.value.lowEndDeviceMode,
            forceReload = true
        )
    }

    // Restore All Defaults
    fun restoreAllDefaults() {
        _pendingPreviewBackgroundUri.value = null
        _pendingPreviewBackgroundBitmap.value = null
        _customBackgroundBitmap.value = null
        mediaHelper.clearBackgroundCache()
        preferencesManager.restoreDefaults()
        appRepository.refreshInstalledApps(
            includeSelf = false,
            lowEndMode = false,
            forceReload = true
        )
        showMessage("تمت استعادة جميع الإعدادات الافتراضية لـ NexaView TV.")
    }

    // Local Media (Movies, Series, Files)
    fun refreshLocalMedia() {
        viewModelScope.launch {
            _discoveredSystemVideos.value = mediaHelper.queryAccessibleLocalVideos()
        }
    }

    fun onLocalMediaFilesPicked(uris: List<Uri>, targetCategory: MediaCategory) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            val resolvedEntries = ArrayList<LocalMediaEntry>()
            for (uri in uris) {
                val entry = mediaHelper.resolveDocumentMetadata(uri, targetCategory)
                if (entry != null) {
                    resolvedEntries.add(entry)
                }
            }
            if (resolvedEntries.isNotEmpty()) {
                preferencesManager.update { current ->
                    val merged = (resolvedEntries + current.configuredMediaItems)
                        .distinctBy { it.uriString }
                    current.copy(configuredMediaItems = merged)
                }
                showMessage("تمت إضافة ${resolvedEntries.size} ملف إلى قسم ${targetCategory.arabicTitle}.")
            } else {
                showMessage("تعذر قراءة الملفات المختارة.")
            }
        }
    }

    fun removeConfiguredMediaItem(uriString: String) {
        preferencesManager.update { current ->
            current.copy(
                configuredMediaItems = current.configuredMediaItems.filter { it.uriString != uriString },
                favoriteMediaUris = current.favoriteMediaUris - uriString
            )
        }
    }

    fun openMediaEntry(entry: LocalMediaEntry) {
        val result = mediaHelper.openLocalMediaFile(entry)
        if (result.isFailure) {
            showMessage("لا يوجد مشغل وسائط مثبت لفتح هذا الملف (${entry.title}).")
        }
    }
}
