package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import com.example.ui.theme.NexaTextMuted
import com.example.ui.theme.NexaTextPrimary
import com.example.ui.theme.NexaTextSecondary
import kotlinx.coroutines.delay

@Composable
fun AllAppsSection(
    installedApps: List<AppInfoItem>,
    preferences: LauncherPreferences,
    lastFocusedPackage: String?,
    contextMenuVisible: Boolean,
    onUpdateLastFocusedPackage: (String) -> Unit,
    onAppClicked: (AppInfoItem) -> Unit,
    onAppMenuPressed: (AppInfoItem) -> Unit,
    onNavigateRightToMenu: () -> Boolean,
    firstAppFocusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    val activeFont = LocalActiveFontFamily.current
    val requestersByPackage = remember(installedApps.map { it.packageName }) {
        installedApps.associate { app ->
            app.packageName to FocusRequester()
        }
    }

    // Restore focus to the exact same application when closing the MENU context menu
    LaunchedEffect(contextMenuVisible, installedApps.size) {
        if (!contextMenuVisible && installedApps.isNotEmpty()) {
            delay(35)
            val targetPkg = lastFocusedPackage
            val targetRequester = if (targetPkg != null && targetPkg != installedApps.first().packageName) {
                requestersByPackage[targetPkg]
            } else {
                firstAppFocusRequester
            }
            runCatching { targetRequester?.requestFocus() }
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .testTag("all_apps_screen")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "جميع التطبيقات المثبتة (${installedApps.size})",
                        fontFamily = activeFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = (22f * preferences.globalFontScale).sp,
                        color = NexaTextPrimary
                    )
                    Text(
                        text = "اضغط OK للتشغيل • اضغط زر MENU في الريموت (أو ضغطة مطولة) لفتح قائمة خيارات التطبيق",
                        fontFamily = activeFont,
                        fontWeight = FontWeight.Normal,
                        fontSize = (13f * preferences.globalFontScale).sp,
                        color = NexaTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (installedApps.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد تطبيقات مثبتة قابلة للتشغيل حالياً.",
                        fontFamily = activeFont,
                        fontSize = (17f * preferences.globalFontScale).sp,
                        color = NexaTextSecondary
                    )
                }
            } else {
                val spacing = preferences.tileSpacingDp.dp
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 148.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("all_apps_grid"),
                        contentPadding = PaddingValues(bottom = 24.dp, top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(spacing),
                        verticalArrangement = Arrangement.spacedBy(spacing)
                    ) {
                        itemsIndexed(
                            items = installedApps,
                            key = { _, app -> app.packageName }
                        ) { index, app ->
                            val req = if (index == 0) {
                                firstAppFocusRequester
                            } else {
                                requestersByPackage[app.packageName]
                            }
                            Box(contentAlignment = Alignment.Center) {
                                AppTileCard(
                                    app = app,
                                    preferences = preferences,
                                    isFavorite = app.packageName in preferences.favoritePackages,
                                    onClick = { onAppClicked(app) },
                                    onMenuRequested = { onAppMenuPressed(app) },
                                    onFocused = { onUpdateLastFocusedPackage(app.packageName) },
                                    onNavigateRightToMenu = null,
                                    focusRequester = req,
                                    baseWidth = 152.dp,
                                    baseHeight = 100.dp,
                                    testTagPrefix = "all_apps_tile"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
