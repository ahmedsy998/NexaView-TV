package com.example.ui.components

import android.view.KeyEvent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppInfoItem
import com.example.model.IconShapeOption
import com.example.model.LauncherPreferences
import com.example.ui.theme.LocalActiveFontFamily
import com.example.ui.theme.NexaCyanGlow
import com.example.ui.theme.NexaMagentaGlow
import com.example.ui.theme.NexaTextPrimary

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppTileCard(
    app: AppInfoItem,
    preferences: LauncherPreferences,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onMenuRequested: (() -> Unit)? = null,
    onFocused: (() -> Unit)? = null,
    onNavigateRightToMenu: (() -> Boolean)? = null,
    focusRequester: FocusRequester? = null,
    baseWidth: Dp = 158.dp,
    baseHeight: Dp = 104.dp,
    testTagPrefix: String = "app_tile",
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val activeFont = LocalActiveFontFamily.current

    LaunchedEffect(isFocused) {
        if (isFocused) {
            onFocused?.invoke()
        }
    }

    val effectiveScale = preferences.customIconScale.coerceIn(0.65f, 1.45f)
    val tileShape: Shape = remember(preferences.iconShape, preferences.tileCornerRadiusDp) {
        when (preferences.iconShape) {
            IconShapeOption.ORIGINAL -> RoundedCornerShape(preferences.tileCornerRadiusDp.dp)
            IconShapeOption.SQUARE -> RoundedCornerShape(4.dp)
            IconShapeOption.ROUNDED_SQUARE -> RoundedCornerShape(preferences.tileCornerRadiusDp.dp)
            IconShapeOption.CIRCLE -> CircleShape
            IconShapeOption.ROUNDED_RECTANGLE -> RoundedCornerShape(preferences.tileCornerRadiusDp.dp)
        }
    }

    val tileWidth = when (preferences.iconShape) {
        IconShapeOption.SQUARE, IconShapeOption.ROUNDED_SQUARE, IconShapeOption.CIRCLE ->
            baseHeight * effectiveScale * 1.05f
        else -> baseWidth * effectiveScale
    }
    val tileHeight = baseHeight * effectiveScale

    val animDuration = if (preferences.lowEndDeviceMode) 0 else 120
    val focusScale by animateFloatAsState(
        targetValue = if (isFocused && !preferences.lowEndDeviceMode) 1.05f else 1.0f,
        animationSpec = tween(durationMillis = animDuration),
        label = "tileFocusScale"
    )

    val opacity = preferences.tileBackgroundOpacity.coerceIn(0.25f, 1.0f)
    val primaryTileColor = Color(app.tileColorHex).copy(alpha = opacity)
    val secondaryTileColor = Color(app.tileSecondaryColorHex).copy(alpha = opacity)

    val showGlow = isFocused && preferences.enableFocusGlow && !preferences.lowEndDeviceMode
    val cornerRadiusPx = preferences.tileCornerRadiusDp

    val focusBorderModifier = if (isFocused) {
        Modifier.border(
            width = preferences.focusBorderThicknessDp.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White,
                    NexaCyanGlow,
                    NexaMagentaGlow
                )
            ),
            shape = tileShape
        )
    } else {
        Modifier.border(
            width = 1.dp,
            color = Color(0xFF283552).copy(alpha = 0.75f),
            shape = tileShape
        )
    }

    val glowModifier = if (showGlow) {
        Modifier.drawBehind {
            val spread = 8.dp.toPx()
            val radius = if (preferences.iconShape == IconShapeOption.CIRCLE) {
                size.minDimension / 2f
            } else {
                cornerRadiusPx.dp.toPx() + 4.dp.toPx()
            }
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        NexaMagentaGlow.copy(alpha = 0.55f),
                        NexaCyanGlow.copy(alpha = 0.55f)
                    )
                ),
                topLeft = Offset(-spread / 2f, -spread / 2f),
                size = Size(size.width + spread, size.height + spread),
                cornerRadius = CornerRadius(radius, radius),
                style = Stroke(width = spread)
            )
        }
    } else {
        Modifier
    }

    val requesterModifier = if (focusRequester != null) {
        Modifier.focusRequester(focusRequester)
    } else {
        Modifier
    }

    val iconClipShape = when (preferences.iconShape) {
        IconShapeOption.CIRCLE -> CircleShape
        IconShapeOption.SQUARE -> RoundedCornerShape(4.dp)
        else -> RoundedCornerShape(10.dp)
    }

    Box(
        modifier = modifier
            .size(width = tileWidth, height = tileHeight)
            .scale(focusScale)
            .then(glowModifier)
            .clip(tileShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(primaryTileColor, secondaryTileColor)
                ),
                shape = tileShape
            )
            .then(focusBorderModifier)
            .then(requesterModifier)
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    val nativeCode = event.nativeKeyEvent.keyCode
                    when {
                        nativeCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                            nativeCode == KeyEvent.KEYCODE_ENTER ||
                            nativeCode == KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                            onClick()
                            true
                        }
                        onMenuRequested != null && isMenuKeyCode(
                            nativeCode,
                            preferences.supportAlternativeMenuKeys
                        ) -> {
                            onMenuRequested()
                            true
                        }
                        nativeCode == KeyEvent.KEYCODE_DPAD_RIGHT && onNavigateRightToMenu != null -> {
                            onNavigateRightToMenu()
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
            .focusable(interactionSource = interactionSource)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = { onMenuRequested?.invoke() }
            )
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .testTag("${testTagPrefix}_${app.packageName}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val iconSize = (if (preferences.showAppLabels) 42.dp else 56.dp) * effectiveScale

            if (app.isAllAppsShortcut) {
                AllAppsGridSymbol(
                    modifier = Modifier.size(iconSize * 0.85f)
                )
            } else if (app.iconBitmap != null) {
                Image(
                    bitmap = app.iconBitmap,
                    contentDescription = app.label,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(iconSize)
                        .clip(iconClipShape)
                )
            } else {
                // Clean vector fallback emblem if an installed app lacks a bitmap icon
                FallbackAppMonogram(
                    label = app.label,
                    isYouTube = app.isYouTube,
                    modifier = Modifier.size(iconSize)
                )
            }

            if (preferences.showAppLabels) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = app.label,
                    fontFamily = activeFont,
                    fontWeight = if (isFocused) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = (preferences.appLabelFontSizeSp * preferences.globalFontScale).sp,
                    color = NexaTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }

        if (isFavorite && !app.isAllAppsShortcut) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(NexaCyanGlow)
            )
        }
    }
}

private fun isMenuKeyCode(keyCode: Int, allowAlternatives: Boolean): Boolean {
    if (keyCode == KeyEvent.KEYCODE_MENU) return true
    if (!allowAlternatives) return false
    return keyCode == KeyEvent.KEYCODE_INFO ||
        keyCode == KeyEvent.KEYCODE_BOOKMARK ||
        keyCode == KeyEvent.KEYCODE_SETTINGS ||
        keyCode == KeyEvent.KEYCODE_GUIDE ||
        keyCode == KeyEvent.KEYCODE_BUTTON_Y
}

@Composable
private fun AllAppsGridSymbol(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cell = w * 0.36f
        val r = CornerRadius(w * 0.08f, w * 0.08f)
        val stroke = Stroke(width = w * 0.085f)
        drawRoundRect(Color.White, Offset(w * 0.10f, h * 0.10f), Size(cell, cell), r, stroke)
        drawRoundRect(Color.White, Offset(w * 0.54f, h * 0.10f), Size(cell, cell), r, stroke)
        drawRoundRect(Color.White, Offset(w * 0.10f, h * 0.54f), Size(cell, cell), r, stroke)
        drawRoundRect(Color.White, Offset(w * 0.54f, h * 0.54f), Size(cell, cell), r, stroke)
    }
}

@Composable
private fun FallbackAppMonogram(
    label: String,
    isYouTube: Boolean,
    modifier: Modifier = Modifier
) {
    val activeFont = LocalActiveFontFamily.current
    if (isYouTube) {
        Canvas(modifier = modifier) {
            val w = size.width
            val h = size.height
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(w * 0.08f, h * 0.20f),
                size = Size(w * 0.84f, h * 0.60f),
                cornerRadius = CornerRadius(w * 0.18f, w * 0.18f)
            )
            val playPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.40f, h * 0.35f)
                lineTo(w * 0.66f, h * 0.50f)
                lineTo(w * 0.40f, h * 0.65f)
                close()
            }
            drawPath(playPath, color = Color(0xFFDC2626))
        }
    } else {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label.take(2).uppercase(),
                fontFamily = activeFont,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
            )
        }
    }
}
