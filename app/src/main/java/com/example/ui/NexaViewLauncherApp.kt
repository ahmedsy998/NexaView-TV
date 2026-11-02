package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.MediaCategory
import com.example.model.NavSection
import com.example.ui.components.AppContextMenuDialog
import com.example.ui.components.BackgroundCanvas
import com.example.ui.components.RightSideNavMenu
import com.example.ui.components.TopStatusBar
import com.example.ui.screens.AllAppsSection
import com.example.ui.screens.FavoritesSection
import com.example.ui.screens.FilesSection
import com.example.ui.screens.HomeScreenSection
import com.example.ui.screens.MoviesOrSeriesSection
import com.example.ui.screens.SettingsSectionScreen
import com.example.ui.theme.LocalActiveFontFamily
import com.example.ui.theme.NexaCyanGlow
import com.example.ui.theme.NexaMidnightBlue
import com.example.ui.theme.NexaTextPrimary
import com.example.ui.theme.NexaViewTVTheme
import kotlinx.coroutines.delay

@Composable
fun NexaViewLauncherApp(
    viewModel: LauncherViewModel
) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val currentSection by viewModel.currentSection.collectAsStateWithLifecycle()
    val activeSettingsSection by viewModel.activeSettingsSection.collectAsStateWithLifecycle()
    val homeApps by viewModel.homeScreenApps.collectAsStateWithLifecycle()
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    val favoriteApps by viewModel.favoriteApps.collectAsStateWithLifecycle()
    val internetStatus by viewModel.internetStatus.collectAsStateWithLifecycle()
    val contextMenuApp by viewModel.contextMenuApp.collectAsStateWithLifecycle()
    val statusBannerMessage by viewModel.statusBannerMessage.collectAsStateWithLifecycle()
    val customBackgroundBitmap by viewModel.customBackgroundBitmap.collectAsStateWithLifecycle()
    val pendingPreviewBitmap by viewModel.pendingPreviewBackgroundBitmap.collectAsStateWithLifecycle()
    val discoveredVideos by viewModel.discoveredSystemVideos.collectAsStateWithLifecycle()

    // Official Android Document Pickers
    val backgroundPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        viewModel.onCustomBackgroundFilePicked(uri)
    }

    val moviesPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        viewModel.onLocalMediaFilesPicked(uris, MediaCategory.MOVIE)
    }

    val seriesPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        viewModel.onLocalMediaFilesPicked(uris, MediaCategory.SERIES)
    }

    val filesPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        viewModel.onLocalMediaFilesPicked(uris, MediaCategory.DOCUMENT_OR_FILE)
    }

    // FocusRequesters for smooth D-pad transitions between content area and right-side menu
    val firstContentFocusRequester = remember { FocusRequester() }
    val menuFocusRequesters = remember {
        NavSection.entries.associateWith { FocusRequester() }
    }

    BackHandler(
        enabled = contextMenuApp != null ||
            pendingPreviewBitmap != null ||
            currentSection != NavSection.HOME
    ) {
        viewModel.navigateBack()
    }

    LaunchedEffect(statusBannerMessage) {
        if (statusBannerMessage != null) {
            delay(4500)
            viewModel.clearMessage()
        }
    }

    NexaViewTVTheme(
        selectedFont = preferences.selectedFont,
        fontScale = preferences.globalFontScale
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("nexaview_root")
        ) {
            // 1. Background Layer (Static preset or imported custom image)
            BackgroundCanvas(
                selectedBackground = preferences.selectedBackground,
                customBitmap = customBackgroundBitmap,
                scaleMode = preferences.backgroundScaleMode,
                dimOverlay = preferences.backgroundDimOverlay,
                lowEndMode = preferences.lowEndDeviceMode
            )

            // 2. Main TV Screen Frame (Top Status Bar + Left Content Area + Permanent Right-Side Menu)
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                ) {
                    // Top Status Bar: ONLY enlarged Wi-Fi indicator, clock, and Arabic date.
                    // No banner, no logo, no launcher title, no Bluetooth or notification icons.
                    TopStatusBar(
                        internetStatus = internetStatus,
                        use24HourFormat = preferences.use24HourFormat,
                        showClock = preferences.showClock,
                        showDate = preferences.showDate,
                        clockFontSizeSp = preferences.clockFontSizeSp,
                        dateFontSizeSp = preferences.dateFontSizeSp,
                        globalFontScale = preferences.globalFontScale
                    )

                    // Main Body: Content on the LEFT, Permanent Vertical Navigation Menu on the RIGHT
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(end = 22.dp, bottom = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Content Area
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .testTag("main_content_area")
                        ) {
                            when (currentSection) {
                                NavSection.HOME -> {
                                    HomeScreenSection(
                                        homeApps = homeApps,
                                        preferences = preferences,
                                        initialStartupFocusDone = viewModel.initialStartupFocusDone,
                                        lastFocusedPackage = viewModel.lastFocusedHomePackage,
                                        onMarkInitialFocusDone = {
                                            viewModel.initialStartupFocusDone = true
                                        },
                                        onUpdateLastFocusedPackage = { pkg ->
                                            viewModel.lastFocusedHomePackage = pkg
                                        },
                                        onAppClicked = { app ->
                                            viewModel.onAppTileClicked(app)
                                        },
                                        onAppContextMenu = { app ->
                                            viewModel.openContextMenuForApp(app)
                                        },
                                        onNavigateRightToMenu = {
                                            runCatching {
                                                menuFocusRequesters[currentSection]?.requestFocus()
                                                true
                                            }.getOrDefault(false)
                                        },
                                        firstTileFocusRequester = firstContentFocusRequester
                                    )
                                }

                                NavSection.APPS -> {
                                    AllAppsSection(
                                        installedApps = installedApps,
                                        preferences = preferences,
                                        lastFocusedPackage = viewModel.lastFocusedAllAppsPackage,
                                        contextMenuVisible = contextMenuApp != null,
                                        onUpdateLastFocusedPackage = { pkg ->
                                            viewModel.lastFocusedAllAppsPackage = pkg
                                        },
                                        onAppClicked = { app ->
                                            viewModel.onAppTileClicked(app)
                                        },
                                        onAppMenuPressed = { app ->
                                            viewModel.openContextMenuForApp(app)
                                        },
                                        onNavigateRightToMenu = {
                                            runCatching {
                                                menuFocusRequesters[currentSection]?.requestFocus()
                                                true
                                            }.getOrDefault(false)
                                        },
                                        firstAppFocusRequester = firstContentFocusRequester
                                    )
                                }

                                NavSection.MOVIES -> {
                                    val movieItems = remember(
                                        preferences.configuredMediaItems,
                                        discoveredVideos
                                    ) {
                                        (preferences.configuredMediaItems + discoveredVideos)
                                            .filter { it.category == MediaCategory.MOVIE }
                                            .distinctBy { it.uriString }
                                    }
                                    MoviesOrSeriesSection(
                                        isSeries = false,
                                        mediaItems = movieItems,
                                        favoriteUris = preferences.favoriteMediaUris,
                                        globalFontScale = preferences.globalFontScale,
                                        onPickMediaFiles = {
                                            runCatching {
                                                moviesPickerLauncher.launch(arrayOf("video/*"))
                                            }.onFailure {
                                                viewModel.showMessage("تعذر فتح منتقي الملفات على هذا الجهاز.")
                                            }
                                        },
                                        onRefreshMedia = { viewModel.refreshLocalMedia() },
                                        onOpenMedia = { viewModel.openMediaEntry(it) },
                                        onToggleFavoriteMedia = { viewModel.toggleFavoriteMedia(it) },
                                        onRemoveMedia = { viewModel.removeConfiguredMediaItem(it) },
                                        firstActionFocusRequester = firstContentFocusRequester
                                    )
                                }

                                NavSection.SERIES -> {
                                    val seriesItems = remember(
                                        preferences.configuredMediaItems,
                                        discoveredVideos
                                    ) {
                                        (preferences.configuredMediaItems + discoveredVideos)
                                            .filter { it.category == MediaCategory.SERIES }
                                            .distinctBy { it.uriString }
                                    }
                                    MoviesOrSeriesSection(
                                        isSeries = true,
                                        mediaItems = seriesItems,
                                        favoriteUris = preferences.favoriteMediaUris,
                                        globalFontScale = preferences.globalFontScale,
                                        onPickMediaFiles = {
                                            runCatching {
                                                seriesPickerLauncher.launch(arrayOf("video/*"))
                                            }.onFailure {
                                                viewModel.showMessage("تعذر فتح منتقي الملفات على هذا الجهاز.")
                                            }
                                        },
                                        onRefreshMedia = { viewModel.refreshLocalMedia() },
                                        onOpenMedia = { viewModel.openMediaEntry(it) },
                                        onToggleFavoriteMedia = { viewModel.toggleFavoriteMedia(it) },
                                        onRemoveMedia = { viewModel.removeConfiguredMediaItem(it) },
                                        firstActionFocusRequester = firstContentFocusRequester
                                    )
                                }

                                NavSection.FAVORITES -> {
                                    val favMedia = remember(
                                        preferences.configuredMediaItems,
                                        discoveredVideos,
                                        preferences.favoriteMediaUris
                                    ) {
                                        (preferences.configuredMediaItems + discoveredVideos)
                                            .distinctBy { it.uriString }
                                            .filter { it.uriString in preferences.favoriteMediaUris }
                                    }
                                    FavoritesSection(
                                        favoriteApps = favoriteApps,
                                        allInstalledApps = installedApps,
                                        favoriteMediaItems = favMedia,
                                        preferences = preferences,
                                        onAppClicked = { viewModel.onAppTileClicked(it) },
                                        onToggleFavoriteApp = { viewModel.toggleFavoriteApp(it) },
                                        onOpenMedia = { viewModel.openMediaEntry(it) },
                                        onToggleFavoriteMedia = { viewModel.toggleFavoriteMedia(it) },
                                        firstItemFocusRequester = firstContentFocusRequester
                                    )
                                }

                                NavSection.FILES -> {
                                    val allFiles = remember(
                                        preferences.configuredMediaItems,
                                        discoveredVideos
                                    ) {
                                        (preferences.configuredMediaItems + discoveredVideos)
                                            .distinctBy { it.uriString }
                                    }
                                    FilesSection(
                                        configuredFiles = allFiles,
                                        favoriteUris = preferences.favoriteMediaUris,
                                        globalFontScale = preferences.globalFontScale,
                                        onPickAnyFiles = {
                                            runCatching {
                                                filesPickerLauncher.launch(arrayOf("*/*"))
                                            }.onFailure {
                                                viewModel.showMessage("تعذر فتح منتقي الملفات على هذا الجهاز.")
                                            }
                                        },
                                        onOpenFile = { viewModel.openMediaEntry(it) },
                                        onToggleFavorite = { viewModel.toggleFavoriteMedia(it) },
                                        onRemoveFile = { viewModel.removeConfiguredMediaItem(it) },
                                        firstActionFocusRequester = firstContentFocusRequester
                                    )
                                }

                                NavSection.SETTINGS -> {
                                    SettingsSectionScreen(
                                        viewModel = viewModel,
                                        preferences = preferences,
                                        activeSubSection = activeSettingsSection,
                                        installedApps = installedApps,
                                        homeApps = homeApps,
                                        pendingPreviewBitmap = pendingPreviewBitmap,
                                        onPickCustomBackgroundFromFiles = {
                                            runCatching {
                                                backgroundPickerLauncher.launch(
                                                    arrayOf("image/jpeg", "image/png", "image/webp", "image/*")
                                                )
                                            }.onFailure {
                                                viewModel.showMessage("تعذر فتح منتقي الصور على هذا الجهاز.")
                                            }
                                        },
                                        firstSettingFocusRequester = firstContentFocusRequester
                                    )
                                }
                            }
                        }

                        // Permanent Right-Side Vertical Navigation Menu
                        RightSideNavMenu(
                            currentSection = currentSection,
                            onSelectSection = { section ->
                                viewModel.selectNavSection(section)
                            },
                            globalFontScale = preferences.globalFontScale,
                            lowEndMode = preferences.lowEndDeviceMode,
                            onMoveFocusLeftToContent = {
                                runCatching {
                                    firstContentFocusRequester.requestFocus()
                                    true
                                }.getOrDefault(false)
                            },
                            menuFocusRequesters = menuFocusRequesters
                        )
                    }
                }
            }

            // 3. Application Context Menu Dialog (Opened via remote MENU button)
            val targetApp = contextMenuApp
            if (targetApp != null) {
                AppContextMenuDialog(
                    app = targetApp,
                    globalFontScale = preferences.globalFontScale,
                    onAppInfoSelected = {
                        viewModel.openSystemAppInfoFromContextMenu(targetApp)
                    },
                    onAppSettingsSelected = {
                        viewModel.openAppSpecificSettingsFromContextMenu(targetApp)
                    },
                    onCancelSelected = {
                        viewModel.dismissContextMenu()
                    }
                )
            }

            // 4. Arabic Notification / Status Feedback Banner
            val bannerMsg = statusBannerMessage
            if (bannerMsg != null) {
                val activeFont = LocalActiveFontFamily.current
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 32.dp, vertical = 20.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(NexaMidnightBlue.copy(alpha = 0.96f))
                            .border(1.5.dp, NexaCyanGlow, RoundedCornerShape(16.dp))
                            .clickable { viewModel.clearMessage() }
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                            .testTag("status_feedback_banner")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = bannerMsg,
                                fontFamily = activeFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = (15f * preferences.globalFontScale).sp,
                                color = NexaTextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
