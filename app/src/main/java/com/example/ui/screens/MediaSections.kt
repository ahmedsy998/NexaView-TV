package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppInfoItem
import com.example.model.LauncherPreferences
import com.example.model.LocalMediaEntry
import com.example.model.MediaCategory
import com.example.ui.components.AppTileCard
import com.example.ui.theme.LocalActiveFontFamily
import com.example.ui.theme.NexaBlueAccent
import com.example.ui.theme.NexaCyanGlow
import com.example.ui.theme.NexaPanelNavy
import com.example.ui.theme.NexaPurpleAccent
import com.example.ui.theme.NexaTextMuted
import com.example.ui.theme.NexaTextPrimary
import com.example.ui.theme.NexaTextSecondary

@Composable
fun MoviesOrSeriesSection(
    isSeries: Boolean,
    mediaItems: List<LocalMediaEntry>,
    favoriteUris: Set<String>,
    globalFontScale: Float,
    onPickMediaFiles: () -> Unit,
    onRefreshMedia: () -> Unit,
    onOpenMedia: (LocalMediaEntry) -> Unit,
    onToggleFavoriteMedia: (String) -> Unit,
    onRemoveMedia: (String) -> Unit,
    firstActionFocusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    val activeFont = LocalActiveFontFamily.current
    val sectionTitle = if (isSeries) "المسلسلات المحلية" else "الأفلام المحلية"
    val sectionSubtitle = if (isSeries) {
        "يعرض ملفات المسلسلات والحلقات المتوفرة محلياً على جهاز التلفاز أو المضافة عبر منتقي الملفات الرسمي."
    } else {
        "يعرض ملفات الأفلام والفيديو المتوفرة محلياً على جهاز التلفاز أو المضافة عبر منتقي الملفات الرسمي."
    }
    val emptyTitle = if (isSeries) {
        "لا توجد حلقات أو مسلسلات محلية مضافة حالياً"
    } else {
        "لا توجد ملفات أفلام محلية مضافة حالياً"
    }
    val addButtonLabel = if (isSeries) {
        "إضافة ملفات مسلسلات من التخزين المحلي"
    } else {
        "إضافة ملفات أفلام من التخزين المحلي"
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .testTag(if (isSeries) "series_screen" else "movies_screen")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sectionTitle,
                        fontFamily = activeFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = (22f * globalFontScale).sp,
                        color = NexaTextPrimary
                    )
                    Text(
                        text = sectionSubtitle,
                        fontFamily = activeFont,
                        fontSize = (13f * globalFontScale).sp,
                        color = NexaTextMuted
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RemoteActionChip(
                        label = addButtonLabel,
                        onClick = onPickMediaFiles,
                        globalFontScale = globalFontScale,
                        focusRequester = firstActionFocusRequester,
                        testTag = if (isSeries) "add_series_files_btn" else "add_movies_files_btn"
                    )
                    RemoteActionChip(
                        label = "تحديث الفحص",
                        onClick = onRefreshMedia,
                        globalFontScale = globalFontScale,
                        testTag = if (isSeries) "refresh_series_btn" else "refresh_movies_btn"
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (mediaItems.isEmpty()) {
                EmptyStateCard(
                    title = emptyTitle,
                    description = "لا يقوم NexaView TV بإنشاء محتوى وهمي أو الاتصال بخوادم خارجية. يمكنك اختيار ملفات الفيديو المخزنة على جهازك أو وحدة USB عبر زر «$addButtonLabel» أعلاه.",
                    globalFontScale = globalFontScale
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(mediaItems, key = { it.uriString }) { item ->
                        MediaFileRowCard(
                            entry = item,
                            isFavorite = item.uriString in favoriteUris,
                            globalFontScale = globalFontScale,
                            onOpen = { onOpenMedia(item) },
                            onToggleFavorite = { onToggleFavoriteMedia(item.uriString) },
                            onRemove = { onRemoveMedia(item.uriString) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FavoritesSection(
    favoriteApps: List<AppInfoItem>,
    allInstalledApps: List<AppInfoItem>,
    favoriteMediaItems: List<LocalMediaEntry>,
    preferences: LauncherPreferences,
    onAppClicked: (AppInfoItem) -> Unit,
    onToggleFavoriteApp: (String) -> Unit,
    onOpenMedia: (LocalMediaEntry) -> Unit,
    onToggleFavoriteMedia: (String) -> Unit,
    firstItemFocusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    val activeFont = LocalActiveFontFamily.current

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .testTag("favorites_screen")
        ) {
            Text(
                text = "المفضلة (${favoriteApps.size + favoriteMediaItems.size})",
                fontFamily = activeFont,
                fontWeight = FontWeight.Bold,
                fontSize = (22f * preferences.globalFontScale).sp,
                color = NexaTextPrimary
            )
            Text(
                text = "التطبيقات والملفات المحلية المميزة بنجمة للوصول السريع.",
                fontFamily = activeFont,
                fontSize = (13f * preferences.globalFontScale).sp,
                color = NexaTextMuted
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (favoriteApps.isEmpty() && favoriteMediaItems.isEmpty()) {
                EmptyStateCard(
                    title = "قائمة المفضلة فارغة حالياً",
                    description = "اختر أي تطبيق من القائمة أدناه لإضافته إلى المفضلة بضغطة واحدة بالريموت.",
                    globalFontScale = preferences.globalFontScale
                )
                Spacer(modifier = Modifier.height(12.dp))
            } else if (favoriteApps.isNotEmpty()) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 148.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 150.dp)
                            .testTag("favorite_apps_grid"),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(favoriteApps, key = { it.packageName }) { app ->
                            AppTileCard(
                                app = app,
                                preferences = preferences,
                                isFavorite = true,
                                onClick = { onAppClicked(app) },
                                baseWidth = 144.dp,
                                baseHeight = 92.dp,
                                testTagPrefix = "fav_tile"
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(
                text = "إدارة التطبيقات المفضلة (اضغط OK للإضافة أو الإزالة من المفضلة):",
                fontFamily = activeFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = (15f * preferences.globalFontScale).sp,
                color = NexaTextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(allInstalledApps.size) { index ->
                    val app = allInstalledApps[index]
                    val isFav = app.packageName in preferences.favoritePackages
                    val interactionSource = remember { MutableInteractionSource() }
                    val isFocused by interactionSource.collectIsFocusedAsState()
                    val rowShape = RoundedCornerShape(14.dp)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(rowShape)
                            .background(if (isFocused) Color(0xFF1E294B) else NexaPanelNavy)
                            .border(
                                width = if (isFocused) 2.dp else 1.dp,
                                color = if (isFocused) NexaCyanGlow else Color(0xFF283552),
                                shape = rowShape
                            )
                            .then(if (index == 0) Modifier.focusRequester(firstItemFocusRequester) else Modifier)
                            .focusable(interactionSource = interactionSource)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = { onToggleFavoriteApp(app.packageName) }
                            )
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .testTag("fav_toggle_${app.packageName}"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = app.label,
                                fontFamily = activeFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = (15f * preferences.globalFontScale).sp,
                                color = NexaTextPrimary
                            )
                            Text(
                                text = app.packageName,
                                fontFamily = activeFont,
                                fontSize = (12f * preferences.globalFontScale).sp,
                                color = NexaTextMuted
                            )
                        }

                        Text(
                            text = if (isFav) "★ في المفضلة (إزالة)" else "☆ إضافة للمفضلة",
                            fontFamily = activeFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = (14f * preferences.globalFontScale).sp,
                            color = if (isFav) NexaCyanGlow else NexaTextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilesSection(
    configuredFiles: List<LocalMediaEntry>,
    favoriteUris: Set<String>,
    globalFontScale: Float,
    onPickAnyFiles: () -> Unit,
    onOpenFile: (LocalMediaEntry) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onRemoveFile: (String) -> Unit,
    firstActionFocusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    val activeFont = LocalActiveFontFamily.current

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .testTag("files_screen")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "الملفات والمستندات المحلية",
                        fontFamily = activeFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = (22f * globalFontScale).sp,
                        color = NexaTextPrimary
                    )
                    Text(
                        text = "استعراض وفتح الوسائط والمستندات المحلية عبر منتقي ملفات أندرويد الرسمي بدون أذونات تخزين واسعة.",
                        fontFamily = activeFont,
                        fontSize = (13f * globalFontScale).sp,
                        color = NexaTextMuted
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                RemoteActionChip(
                    label = "استعراض وإضافة ملفات من الجهاز",
                    onClick = onPickAnyFiles,
                    globalFontScale = globalFontScale,
                    focusRequester = firstActionFocusRequester,
                    testTag = "pick_local_files_btn"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (configuredFiles.isEmpty()) {
                EmptyStateCard(
                    title = "لا توجد ملفات أو مستندات مضافة بعد",
                    description = "اضغط على زر «استعراض وإضافة ملفات من الجهاز» لفتح منتقي ملفات النظام الرسمي واختيار ملفات الفيديو أو الصوت أو الصور أو المستندات.",
                    globalFontScale = globalFontScale
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(configuredFiles, key = { it.uriString }) { item ->
                        MediaFileRowCard(
                            entry = item,
                            isFavorite = item.uriString in favoriteUris,
                            globalFontScale = globalFontScale,
                            onOpen = { onOpenFile(item) },
                            onToggleFavorite = { onToggleFavorite(item.uriString) },
                            onRemove = { onRemoveFile(item.uriString) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaFileRowCard(
    entry: LocalMediaEntry,
    isFavorite: Boolean,
    globalFontScale: Float,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRemove: () -> Unit
) {
    val activeFont = LocalActiveFontFamily.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val shape = RoundedCornerShape(16.dp)

    val sizeKb = (entry.sizeBytes / 1024L).coerceAtLeast(1L)
    val sizeText = if (sizeKb >= 1024L) {
        "${sizeKb / 1024L} ميجابايت"
    } else {
        "$sizeKb كيلوبايت"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (isFocused) Color(0xFF1E294B) else NexaPanelNavy)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) NexaCyanGlow else Color(0xFF283552),
                shape = shape
            )
            .focusable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onOpen
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.title,
                fontFamily = activeFont,
                fontWeight = FontWeight.Bold,
                fontSize = (16f * globalFontScale).sp,
                color = NexaTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${entry.category.arabicTitle} • ${entry.mimeType} • $sizeText",
                fontFamily = activeFont,
                fontSize = (12.5f * globalFontScale).sp,
                color = NexaTextMuted
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RemoteActionChip(
                label = "فتح",
                onClick = onOpen,
                globalFontScale = globalFontScale
            )
            RemoteActionChip(
                label = if (isFavorite) "★ مفضل" else "☆ تفضيل",
                onClick = onToggleFavorite,
                globalFontScale = globalFontScale
            )
            RemoteActionChip(
                label = "حذف من القائمة",
                onClick = onRemove,
                globalFontScale = globalFontScale
            )
        }
    }
}

@Composable
fun RemoteActionChip(
    label: String,
    onClick: () -> Unit,
    globalFontScale: Float,
    focusRequester: FocusRequester? = null,
    testTag: String? = null
) {
    val activeFont = LocalActiveFontFamily.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val shape = RoundedCornerShape(14.dp)

    Box(
        modifier = Modifier
            .heightIn(min = 48.dp)
            .clip(shape)
            .background(
                brush = if (isFocused) {
                    Brush.horizontalGradient(listOf(NexaPurpleAccent, NexaBlueAccent))
                } else {
                    Brush.horizontalGradient(listOf(Color(0xFF1E293B), Color(0xFF172036)))
                },
                shape = shape
            )
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) Color.White else Color(0xFF334155),
                shape = shape
            )
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .focusable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = activeFont,
            fontWeight = FontWeight.Bold,
            fontSize = (13.5f * globalFontScale).sp,
            color = NexaTextPrimary
        )
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    description: String,
    globalFontScale: Float
) {
    val activeFont = LocalActiveFontFamily.current
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(NexaPanelNavy)
            .border(1.dp, Color(0xFF283552), shape)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontFamily = activeFont,
            fontWeight = FontWeight.Bold,
            fontSize = (18f * globalFontScale).sp,
            color = NexaTextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            fontFamily = activeFont,
            fontSize = (14f * globalFontScale).sp,
            color = NexaTextSecondary
        )
    }
}
