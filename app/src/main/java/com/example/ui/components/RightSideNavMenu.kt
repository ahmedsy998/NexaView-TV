package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NavSection
import com.example.ui.theme.LocalActiveFontFamily
import com.example.ui.theme.NexaBlueAccent
import com.example.ui.theme.NexaCyanGlow
import com.example.ui.theme.NexaPanelNavy
import com.example.ui.theme.NexaPurpleAccent
import com.example.ui.theme.NexaTextPrimary
import com.example.ui.theme.NexaTextSecondary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RightSideNavMenu(
    currentSection: NavSection,
    onSelectSection: (NavSection) -> Unit,
    globalFontScale: Float,
    lowEndMode: Boolean,
    onMoveFocusLeftToContent: () -> Boolean,
    menuFocusRequesters: Map<NavSection, FocusRequester>,
    modifier: Modifier = Modifier
) {
    val panelShape = RoundedCornerShape(24.dp)

    Box(
        modifier = modifier
            .width(196.dp)
            .clip(panelShape)
            .background(NexaPanelNavy)
            .border(
                width = 1.dp,
                color = Color(0xFF283554),
                shape = panelShape
            )
            .padding(horizontal = 10.dp, vertical = 12.dp)
            .testTag("right_side_nav_menu")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            NavSection.entries.forEach { section ->
                val isSelected = section == currentSection
                val requester = menuFocusRequesters[section] ?: remember { FocusRequester() }
                RightNavMenuItem(
                    section = section,
                    isSelected = isSelected,
                    globalFontScale = globalFontScale,
                    lowEndMode = lowEndMode,
                    focusRequester = requester,
                    onClick = { onSelectSection(section) },
                    onNavigateLeft = onMoveFocusLeftToContent
                )
            }
        }
    }
}

@Composable
private fun RightNavMenuItem(
    section: NavSection,
    isSelected: Boolean,
    globalFontScale: Float,
    lowEndMode: Boolean,
    focusRequester: FocusRequester,
    onClick: () -> Unit,
    onNavigateLeft: () -> Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val itemShape = RoundedCornerShape(16.dp)
    val activeFont = LocalActiveFontFamily.current

    val backgroundModifier = when {
        isSelected -> Modifier.background(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    NexaPurpleAccent,
                    NexaBlueAccent
                )
            ),
            shape = itemShape
        )
        isFocused -> Modifier.background(
            color = Color(0xFF1E294B),
            shape = itemShape
        )
        else -> Modifier
    }

    val borderModifier = when {
        isFocused -> Modifier.border(
            width = if (lowEndMode) 2.dp else 2.5.dp,
            color = if (lowEndMode) Color.White else NexaCyanGlow,
            shape = itemShape
        )
        isSelected -> Modifier.border(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.45f),
            shape = itemShape
        )
        else -> Modifier
    }

    // Match the reference image layout: icon on the left side of the pill, Arabic label on the right
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clip(itemShape)
                .then(backgroundModifier)
                .then(borderModifier)
                .focusRequester(focusRequester)
                .onKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown) {
                        when (event.key) {
                            Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> {
                                onClick()
                                true
                            }
                            Key.DirectionLeft -> {
                                onNavigateLeft()
                            }
                            else -> false
                        }
                    } else {
                        false
                    }
                }
                .focusable(interactionSource = interactionSource)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Tab,
                    onClick = onClick
                )
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .testTag(section.testTag),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavLineIcon(
                section = section,
                tint = if (isSelected || isFocused) Color.White else NexaTextSecondary,
                modifier = Modifier.size(22.dp)
            )

            Text(
                text = section.arabicTitle,
                fontFamily = activeFont,
                fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium,
                fontSize = (16f * globalFontScale).sp,
                color = if (isSelected || isFocused) NexaTextPrimary else NexaTextSecondary,
                textAlign = TextAlign.Right
            )
        }
    }
}

@Composable
fun NavLineIcon(
    section: NavSection,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(
            width = w * 0.095f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        when (section) {
            NavSection.HOME -> {
                val path = Path().apply {
                    moveTo(w * 0.14f, h * 0.48f)
                    lineTo(w * 0.50f, h * 0.15f)
                    lineTo(w * 0.86f, h * 0.48f)
                    lineTo(w * 0.86f, h * 0.86f)
                    lineTo(w * 0.60f, h * 0.86f)
                    lineTo(w * 0.60f, h * 0.60f)
                    lineTo(w * 0.40f, h * 0.60f)
                    lineTo(w * 0.40f, h * 0.86f)
                    lineTo(w * 0.14f, h * 0.86f)
                    close()
                }
                drawPath(path, color = tint, style = stroke)
            }

            NavSection.APPS -> {
                val boxSize = w * 0.31f
                val r = CornerRadius(w * 0.06f, w * 0.06f)
                drawRoundRect(tint, Offset(w * 0.14f, h * 0.14f), Size(boxSize, boxSize), r, stroke)
                drawRoundRect(tint, Offset(w * 0.55f, h * 0.14f), Size(boxSize, boxSize), r, stroke)
                drawRoundRect(tint, Offset(w * 0.14f, h * 0.55f), Size(boxSize, boxSize), r, stroke)
                drawRoundRect(tint, Offset(w * 0.55f, h * 0.55f), Size(boxSize, boxSize), r, stroke)
            }

            NavSection.MOVIES -> {
                val playPath = Path().apply {
                    moveTo(w * 0.24f, h * 0.16f)
                    lineTo(w * 0.82f, h * 0.50f)
                    lineTo(w * 0.24f, h * 0.84f)
                    close()
                }
                drawPath(playPath, color = tint, style = stroke)
            }

            NavSection.SERIES -> {
                // TV screen with antennas
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.14f, h * 0.30f),
                    size = Size(w * 0.72f, h * 0.54f),
                    cornerRadius = CornerRadius(w * 0.10f, w * 0.10f),
                    style = stroke
                )
                drawLine(tint, Offset(w * 0.35f, h * 0.12f), Offset(w * 0.50f, h * 0.30f), stroke.width, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.65f, h * 0.12f), Offset(w * 0.50f, h * 0.30f), stroke.width, StrokeCap.Round)
            }

            NavSection.FAVORITES -> {
                val star = Path()
                val cx = w * 0.5f
                val cy = h * 0.52f
                val outerR = w * 0.40f
                val innerR = w * 0.18f
                for (i in 0 until 10) {
                    val angle = Math.toRadians((-90 + i * 36).toDouble())
                    val r = if (i % 2 == 0) outerR else innerR
                    val x = (cx + r * cos(angle)).toFloat()
                    val y = (cy + r * sin(angle)).toFloat()
                    if (i == 0) star.moveTo(x, y) else star.lineTo(x, y)
                }
                star.close()
                drawPath(star, color = tint, style = stroke)
            }

            NavSection.FILES -> {
                val folder = Path().apply {
                    moveTo(w * 0.12f, h * 0.24f)
                    lineTo(w * 0.40f, h * 0.24f)
                    lineTo(w * 0.48f, h * 0.34f)
                    lineTo(w * 0.88f, h * 0.34f)
                    lineTo(w * 0.88f, h * 0.82f)
                    lineTo(w * 0.12f, h * 0.82f)
                    close()
                }
                drawPath(folder, color = tint, style = stroke)
            }

            NavSection.SETTINGS -> {
                val cx = w * 0.5f
                val cy = h * 0.5f
                drawCircle(tint, radius = w * 0.16f, center = Offset(cx, cy), style = stroke)
                drawCircle(tint, radius = w * 0.34f, center = Offset(cx, cy), style = stroke)
                for (i in 0 until 8) {
                    val angle = Math.toRadians((i * 45).toDouble())
                    val x1 = (cx + w * 0.31f * cos(angle)).toFloat()
                    val y1 = (cy + h * 0.31f * sin(angle)).toFloat()
                    val x2 = (cx + w * 0.42f * cos(angle)).toFloat()
                    val y2 = (cy + h * 0.42f * sin(angle)).toFloat()
                    drawLine(tint, Offset(x1, y1), Offset(x2, y2), stroke.width, StrokeCap.Round)
                }
            }
        }
    }
}
