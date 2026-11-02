package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppInfoItem
import com.example.model.LauncherPreferences
import com.example.ui.components.AppTileCard
import com.example.ui.theme.LocalActiveFontFamily
import com.example.ui.theme.NexaTextSecondary
import kotlinx.coroutines.delay

@Composable
fun HomeScreenSection(
    homeApps: List<AppInfoItem>,
    preferences: LauncherPreferences,
    initialStartupFocusDone: Boolean,
    lastFocusedPackage: String?,
    onMarkInitialFocusDone: () -> Unit,
    onUpdateLastFocusedPackage: (String) -> Unit,
    onAppClicked: (AppInfoItem) -> Unit,
    onAppContextMenu: (AppInfoItem) -> Unit,
    onNavigateRightToMenu: () -> Boolean,
    firstTileFocusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    val activeFont = LocalActiveFontFamily.current
    val requestersByPackage = remember(homeApps.map { it.packageName }) {
        homeApps.associate { app ->
            app.packageName to FocusRequester()
        }
    }

    // Focus logic:
    // 1. On initial startup, focus installed YouTube app (or first available app if YouTube is not installed).
    // 2. When returning to Home from another section, restore previous focus if enabled.
    // 3. Never repeatedly steal focus while the user is navigating.
    LaunchedEffect(homeApps.size, initialStartupFocusDone) {
        if (homeApps.isEmpty()) return@LaunchedEffect
        if (!initialStartupFocusDone) {
            delay(40)
            val targetApp = homeApps.firstOrNull { it.isYouTube } ?: homeApps.first()
            val requester = if (targetApp == homeApps.first()) {
                firstTileFocusRequester
            } else {
                requestersByPackage[targetApp.packageName] ?: firstTileFocusRequester
            }
            runCatching {
                requester.requestFocus()
                onUpdateLastFocusedPackage(targetApp.packageName)
                onMarkInitialFocusDone()
            }
        } else if (preferences.restoreFocusOnReturn && lastFocusedPackage != null) {
            delay(30)
            val targetApp = homeApps.firstOrNull { it.packageName == lastFocusedPackage }
            if (targetApp != null) {
                val requester = if (targetApp == homeApps.first()) {
                    firstTileFocusRequester
                } else {
                    requestersByPackage[targetApp.packageName]
                }
                runCatching { requester?.requestFocus() }
            }
        }
    }

    // Match the reference image left-to-right 2-row application grid to the left of the right-side navigation panel
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        BoxWithConstraints(
            modifier = modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
                .testTag("home_two_row_grid_container"),
            contentAlignment = Alignment.CenterStart
        ) {
            if (homeApps.isEmpty()) {
                Text(
                    text = "جاري فحص التطبيقات المثبتة...",
                    fontFamily = activeFont,
                    fontWeight = FontWeight.Medium,
                    fontSize = (18f * preferences.globalFontScale).sp,
                    color = NexaTextSecondary,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                val spacing = preferences.tileSpacingDp.dp
                // Compute adaptive tile dimensions so 5 columns x 2 rows fit comfortably on 16:9 720p/1080p/4K TVs
                val availableWidth = maxWidth
                val availableHeight = maxHeight
                val adaptiveTileWidth = ((availableWidth - spacing * 4 - 16.dp) / 5f)
                    .coerceIn(112.dp, 184.dp)
                val adaptiveTileHeight = ((availableHeight - spacing - 24.dp) / 2f)
                    .coerceIn(78.dp, 118.dp)

                val gridHeight = (adaptiveTileHeight * preferences.customIconScale * 2f) + spacing + 24.dp

                // Split into 2 explicit horizontal rows when <= 10 items, or use LazyHorizontalGrid(GridCells.Fixed(2))
                // Using LazyHorizontalGrid with GridCells.Fixed(2) guarantees strictly 2 horizontal rows with smooth scrolling
                if (homeApps.size <= 10) {
                    val row1Apps = homeApps.take(5)
                    val row2Apps = homeApps.drop(5).take(5)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_two_rows_static"),
                        verticalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("home_grid_row_1"),
                            horizontalArrangement = Arrangement.spacedBy(spacing)
                        ) {
                            row1Apps.forEachIndexed { index, app ->
                                val isRightmostInRow = index == row1Apps.lastIndex
                                val req = if (index == 0) {
                                    firstTileFocusRequester
                                } else {
                                    requestersByPackage[app.packageName]
                                }
                                AppTileCard(
                                    app = app,
                                    preferences = preferences,
                                    isFavorite = app.packageName in preferences.favoritePackages,
                                    onClick = { onAppClicked(app) },
                                    onMenuRequested = if (!app.isAllAppsShortcut) {
                                        { onAppContextMenu(app) }
                                    } else {
                                        null
                                    },
                                    onFocused = { onUpdateLastFocusedPackage(app.packageName) },
                                    onNavigateRightToMenu = if (isRightmostInRow) onNavigateRightToMenu else null,
                                    focusRequester = req,
                                    baseWidth = adaptiveTileWidth,
                                    baseHeight = adaptiveTileHeight,
                                    testTagPrefix = "home_tile"
                                )
                            }
                        }

                        if (row2Apps.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("home_grid_row_2"),
                                horizontalArrangement = Arrangement.spacedBy(spacing)
                            ) {
                                row2Apps.forEachIndexed { index, app ->
                                    val isRightmostInRow = index == row2Apps.lastIndex
                                    val req = requestersByPackage[app.packageName]
                                    AppTileCard(
                                        app = app,
                                        preferences = preferences,
                                        isFavorite = app.packageName in preferences.favoritePackages,
                                        onClick = { onAppClicked(app) },
                                        onMenuRequested = if (!app.isAllAppsShortcut) {
                                            { onAppContextMenu(app) }
                                        } else {
                                            null
                                        },
                                        onFocused = { onUpdateLastFocusedPackage(app.packageName) },
                                        onNavigateRightToMenu = if (isRightmostInRow) onNavigateRightToMenu else null,
                                        focusRequester = req,
                                        baseWidth = adaptiveTileWidth,
                                        baseHeight = adaptiveTileHeight,
                                        testTagPrefix = "home_tile"
                                    )
                                }
                            }
                        }
                    }
                } else {
                    LazyHorizontalGrid(
                        rows = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(gridHeight)
                            .testTag("home_two_rows_scrollable"),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(spacing),
                        verticalArrangement = Arrangement.spacedBy(spacing)
                    ) {
                        itemsIndexed(
                            items = homeApps,
                            key = { _, app -> app.packageName }
                        ) { index, app ->
                            val req = if (index == 0) {
                                firstTileFocusRequester
                            } else {
                                requestersByPackage[app.packageName]
                            }
                            val isLastColumn = index >= homeApps.size - 2
                            Box(contentAlignment = Alignment.Center) {
                                AppTileCard(
                                    app = app,
                                    preferences = preferences,
                                    isFavorite = app.packageName in preferences.favoritePackages,
                                    onClick = { onAppClicked(app) },
                                    onMenuRequested = if (!app.isAllAppsShortcut) {
                                        { onAppContextMenu(app) }
                                    } else {
                                        null
                                    },
                                    onFocused = { onUpdateLastFocusedPackage(app.packageName) },
                                    onNavigateRightToMenu = if (isLastColumn) onNavigateRightToMenu else null,
                                    focusRequester = req,
                                    baseWidth = adaptiveTileWidth,
                                    baseHeight = adaptiveTileHeight,
                                    testTagPrefix = "home_tile"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
