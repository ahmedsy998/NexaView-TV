package com.example.ui.screens

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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalActiveFontFamily
import com.example.ui.theme.NexaBlueAccent
import com.example.ui.theme.NexaCyanGlow
import com.example.ui.theme.NexaPanelNavy
import com.example.ui.theme.NexaPurpleAccent
import com.example.ui.theme.NexaTextMuted
import com.example.ui.theme.NexaTextPrimary
import com.example.ui.theme.NexaTextSecondary
import com.example.ui.theme.NexaVioletAccent

@Composable
fun SettingsToggleRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    globalFontScale: Float,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String? = null
) {
    val activeFont = LocalActiveFontFamily.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val shape = RoundedCornerShape(16.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 54.dp)
            .clip(shape)
            .background(if (isFocused) Color(0xFF1E294B) else NexaPanelNavy)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) NexaCyanGlow else Color(0xFF283552),
                shape = shape
            )
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    when (event.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_DPAD_CENTER,
                        KeyEvent.KEYCODE_ENTER,
                        KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                            onCheckedChange(!checked)
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
                onClick = { onCheckedChange(!checked) }
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = activeFont,
                fontWeight = FontWeight.Bold,
                fontSize = (15.5f * globalFontScale).sp,
                color = NexaTextPrimary
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    fontFamily = activeFont,
                    fontSize = (12.5f * globalFontScale).sp,
                    color = NexaTextMuted
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = NexaVioletAccent,
                uncheckedThumbColor = NexaTextSecondary,
                uncheckedTrackColor = Color(0xFF1E293B)
            )
        )
    }
}

@Composable
fun SettingsSliderAndStepperRow(
    title: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    stepDelta: Float,
    globalFontScale: Float,
    onValueChange: (Float) -> Unit,
    testTag: String? = null
) {
    val activeFont = LocalActiveFontFamily.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val shape = RoundedCornerShape(16.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (isFocused) Color(0xFF1E294B) else NexaPanelNavy)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) NexaCyanGlow else Color(0xFF283552),
                shape = shape
            )
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    when (event.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_DPAD_LEFT -> {
                            onValueChange((value - stepDelta).coerceIn(valueRange.start, valueRange.endInclusive))
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            onValueChange((value + stepDelta).coerceIn(valueRange.start, valueRange.endInclusive))
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
            .focusable(interactionSource = interactionSource)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontFamily = activeFont,
                fontWeight = FontWeight.Bold,
                fontSize = (15f * globalFontScale).sp,
                color = NexaTextPrimary
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RemoteActionChip(
                    label = "−",
                    onClick = {
                        onValueChange((value - stepDelta).coerceIn(valueRange.start, valueRange.endInclusive))
                    },
                    globalFontScale = globalFontScale
                )
                Text(
                    text = valueText,
                    fontFamily = activeFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = (14.5f * globalFontScale).sp,
                    color = NexaCyanGlow
                )
                RemoteActionChip(
                    label = "+",
                    onClick = {
                        onValueChange((value + stepDelta).coerceIn(valueRange.start, valueRange.endInclusive))
                    },
                    globalFontScale = globalFontScale
                )
            }
        }

        Slider(
            value = value.coerceIn(valueRange.start, valueRange.endInclusive),
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = NexaCyanGlow,
                activeTrackColor = NexaVioletAccent,
                inactiveTrackColor = Color(0xFF334155)
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> SettingsOptionSelectorCard(
    title: String,
    options: List<T>,
    selectedOption: T,
    optionLabel: (T) -> String,
    optionFontFamily: ((T) -> FontFamily)? = null,
    optionTestTag: ((T) -> String)? = null,
    globalFontScale: Float,
    onSelect: (T) -> Unit
) {
    val activeFont = LocalActiveFontFamily.current
    val cardShape = RoundedCornerShape(16.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(NexaPanelNavy)
            .border(1.dp, Color(0xFF283552), cardShape)
            .padding(16.dp)
    ) {
        Text(
            text = title,
            fontFamily = activeFont,
            fontWeight = FontWeight.Bold,
            fontSize = (15.5f * globalFontScale).sp,
            color = NexaTextPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            options.forEach { option ->
                val isSelected = option == selectedOption
                val interactionSource = remember { MutableInteractionSource() }
                val isFocused by interactionSource.collectIsFocusedAsState()
                val chipShape = RoundedCornerShape(14.dp)
                val itemFont = optionFontFamily?.invoke(option) ?: activeFont
                val tag = optionTestTag?.invoke(option)

                Box(
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clip(chipShape)
                        .background(
                            brush = when {
                                isSelected -> Brush.horizontalGradient(
                                    listOf(NexaPurpleAccent, NexaBlueAccent)
                                )
                                isFocused -> Brush.horizontalGradient(
                                    listOf(Color(0xFF25355A), Color(0xFF1E294B))
                                )
                                else -> Brush.horizontalGradient(
                                    listOf(Color(0xFF172036), Color(0xFF11182C))
                                )
                            },
                            shape = chipShape
                        )
                        .border(
                            width = if (isFocused) 2.dp else 1.dp,
                            color = when {
                                isFocused -> Color.White
                                isSelected -> NexaCyanGlow
                                else -> Color(0xFF334155)
                            },
                            shape = chipShape
                        )
                        .onKeyEvent { event ->
                            if (event.type == KeyEventType.KeyDown &&
                                (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                                    event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER ||
                                    event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)
                            ) {
                                onSelect(option)
                                true
                            } else {
                                false
                            }
                        }
                        .focusable(interactionSource = interactionSource)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { onSelect(option) }
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .then(if (tag != null) Modifier.testTag(tag) else Modifier),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = optionLabel(option),
                        fontFamily = itemFont,
                        fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium,
                        fontSize = (14f * globalFontScale).sp,
                        color = if (isSelected || isFocused) NexaTextPrimary else NexaTextSecondary
                    )
                }
            }
        }
    }
}
