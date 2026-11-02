package com.example.ui.components

import android.view.KeyEvent
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppInfoItem
import com.example.ui.theme.LocalActiveFontFamily
import com.example.ui.theme.NexaBlueAccent
import com.example.ui.theme.NexaCyanGlow
import com.example.ui.theme.NexaMidnightBlue
import com.example.ui.theme.NexaPurpleAccent
import com.example.ui.theme.NexaTextMuted
import com.example.ui.theme.NexaTextPrimary
import com.example.ui.theme.NexaTextSecondary

@Composable
fun AppContextMenuDialog(
    app: AppInfoItem,
    globalFontScale: Float,
    onAppInfoSelected: () -> Unit,
    onAppSettingsSelected: () -> Unit,
    onCancelSelected: () -> Unit
) {
    val firstOptionFocusRequester = remember { FocusRequester() }
    val activeFont = LocalActiveFontFamily.current

    LaunchedEffect(app.packageName) {
        runCatching { firstOptionFocusRequester.requestFocus() }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.72f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onCancelSelected
                )
                .testTag("app_context_menu_overlay"),
            contentAlignment = Alignment.Center
        ) {
            val cardShape = RoundedCornerShape(22.dp)
            Column(
                modifier = Modifier
                    .widthIn(min = 320.dp, max = 420.dp)
                    .clip(cardShape)
                    .background(NexaMidnightBlue)
                    .border(1.5.dp, NexaCyanGlow.copy(alpha = 0.7f), cardShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .padding(22.dp)
                    .testTag("app_context_menu_dialog"),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = app.label,
                    fontFamily = activeFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = (20f * globalFontScale).sp,
                    color = NexaTextPrimary
                )
                Text(
                    text = app.packageName,
                    fontFamily = activeFont,
                    fontWeight = FontWeight.Normal,
                    fontSize = (12.5f * globalFontScale).sp,
                    color = NexaTextMuted
                )

                Spacer(modifier = Modifier.height(16.dp))

                ContextMenuOptionButton(
                    label = "معلومات التطبيق",
                    testTag = "context_option_app_info",
                    globalFontScale = globalFontScale,
                    focusRequester = firstOptionFocusRequester,
                    onClick = onAppInfoSelected,
                    onBackOrEscape = onCancelSelected
                )

                Spacer(modifier = Modifier.height(10.dp))

                ContextMenuOptionButton(
                    label = "ضبط التطبيق",
                    testTag = "context_option_app_settings",
                    globalFontScale = globalFontScale,
                    focusRequester = null,
                    onClick = onAppSettingsSelected,
                    onBackOrEscape = onCancelSelected
                )

                Spacer(modifier = Modifier.height(10.dp))

                ContextMenuOptionButton(
                    label = "إلغاء",
                    testTag = "context_option_cancel",
                    globalFontScale = globalFontScale,
                    focusRequester = null,
                    onClick = onCancelSelected,
                    onBackOrEscape = onCancelSelected
                )
            }
        }
    }
}

@Composable
private fun ContextMenuOptionButton(
    label: String,
    testTag: String,
    globalFontScale: Float,
    focusRequester: FocusRequester?,
    onClick: () -> Unit,
    onBackOrEscape: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val activeFont = LocalActiveFontFamily.current
    val shape = RoundedCornerShape(14.dp)

    val bgModifier = if (isFocused) {
        Modifier.background(
            brush = Brush.horizontalGradient(
                colors = listOf(NexaPurpleAccent, NexaBlueAccent)
            ),
            shape = shape
        )
    } else {
        Modifier.background(Color(0xFF17223B), shape = shape)
    }

    val borderModifier = if (isFocused) {
        Modifier.border(2.dp, Color.White, shape)
    } else {
        Modifier.border(1.dp, Color(0xFF283552), shape)
    }

    val reqModifier = if (focusRequester != null) {
        Modifier.focusRequester(focusRequester)
    } else {
        Modifier
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(shape)
            .then(bgModifier)
            .then(borderModifier)
            .then(reqModifier)
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    when (event.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_DPAD_CENTER,
                        KeyEvent.KEYCODE_ENTER,
                        KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                            onClick()
                            true
                        }
                        KeyEvent.KEYCODE_BACK,
                        KeyEvent.KEYCODE_ESCAPE -> {
                            onBackOrEscape()
                            true
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
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = label,
            fontFamily = activeFont,
            fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium,
            fontSize = (16f * globalFontScale).sp,
            color = if (isFocused) NexaTextPrimary else NexaTextSecondary,
            textAlign = TextAlign.Right
        )
    }
}
