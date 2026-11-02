package com.example.ui.components

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.InternetStatus
import com.example.ui.theme.LocalActiveFontFamily
import com.example.ui.theme.NexaTextPrimary
import com.example.ui.theme.NexaTextSecondary
import com.example.ui.theme.NexaWarningAmber
import com.example.ui.theme.NexaWifiRedSlash
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TopStatusBar(
    internetStatus: InternetStatus,
    use24HourFormat: Boolean,
    showClock: Boolean,
    showDate: Boolean,
    clockFontSizeSp: Float,
    dateFontSizeSp: Float,
    globalFontScale: Float,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                currentTimeMillis = System.currentTimeMillis()
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        context.registerReceiver(receiver, filter)
        onDispose {
            runCatching { context.unregisterReceiver(receiver) }
        }
    }

    val activeFont = LocalActiveFontFamily.current

    val formattedTime = remember(currentTimeMillis, use24HourFormat) {
        val pattern = if (use24HourFormat) "HH:mm" else "hh:mm a"
        SimpleDateFormat(pattern, Locale.ENGLISH).format(Date(currentTimeMillis))
    }

    val formattedArabicDate = remember(currentTimeMillis) {
        val arabicLocale = Locale.forLanguageTag("ar")
        SimpleDateFormat("EEEE، d MMMM", arabicLocale).format(Date(currentTimeMillis))
    }

    // Visual layout matches the reference image: positioned at top-right of the screen,
    // with clock + Arabic date on the far right and the enlarged Wi-Fi icon beside it.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 14.dp)
                .testTag("top_status_bar"),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            EnlargedWifiIndicator(
                status = internetStatus,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("enlarged_wifi_indicator")
                    .semantics {
                        contentDescription = internetStatus.arabicDescription
                    }
            )

            if (showClock || showDate) {
                Spacer(modifier = Modifier.width(42.dp))
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    if (showClock) {
                        Text(
                            text = formattedTime,
                            fontFamily = activeFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = (clockFontSizeSp * globalFontScale).sp,
                            color = NexaTextPrimary,
                            modifier = Modifier.testTag("status_clock_text")
                        )
                    }
                    if (showDate) {
                        Text(
                            text = formattedArabicDate,
                            fontFamily = activeFont,
                            fontWeight = FontWeight.Medium,
                            fontSize = (dateFontSizeSp * globalFontScale).sp,
                            color = NexaTextSecondary,
                            modifier = Modifier.testTag("status_date_text")
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EnlargedWifiIndicator(
    status: InternetStatus,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val centerX = w * 0.5f
        val baseCenterY = h * 0.80f

        val waveColor = when (status) {
            InternetStatus.VALIDATED_INTERNET -> Color.White
            InternetStatus.CONNECTED_NO_INTERNET -> Color(0xFFE2E8F0)
            InternetStatus.DISCONNECTED -> Color(0xFF94A3B8)
        }

        val strokeWidth = w * 0.095f

        // Outer Wi-Fi arc
        val outerR = w * 0.44f
        drawArc(
            color = waveColor,
            startAngle = 222f,
            sweepAngle = 96f,
            useCenter = false,
            topLeft = Offset(centerX - outerR, baseCenterY - outerR),
            size = Size(outerR * 2f, outerR * 2f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Middle Wi-Fi arc
        val midR = w * 0.285f
        drawArc(
            color = waveColor,
            startAngle = 222f,
            sweepAngle = 96f,
            useCenter = false,
            topLeft = Offset(centerX - midR, baseCenterY - midR),
            size = Size(midR * 2f, midR * 2f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Bottom Wi-Fi dot
        drawCircle(
            color = waveColor,
            radius = strokeWidth * 0.68f,
            center = Offset(centerX, baseCenterY - w * 0.04f)
        )

        // Status overlay when internet is unavailable or unvalidated
        when (status) {
            InternetStatus.DISCONNECTED -> {
                // Bold diagonal red slash across the Wi-Fi symbol
                drawLine(
                    color = Color(0xFF060916),
                    start = Offset(w * 0.16f, h * 0.18f),
                    end = Offset(w * 0.84f, h * 0.86f),
                    strokeWidth = strokeWidth * 1.6f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = NexaWifiRedSlash,
                    start = Offset(w * 0.16f, h * 0.18f),
                    end = Offset(w * 0.84f, h * 0.86f),
                    strokeWidth = strokeWidth * 1.05f,
                    cap = StrokeCap.Round
                )
            }
            InternetStatus.CONNECTED_NO_INTERNET -> {
                // Diagonal red/amber slash indicating no internet access
                drawLine(
                    color = NexaWifiRedSlash,
                    start = Offset(w * 0.18f, h * 0.20f),
                    end = Offset(w * 0.82f, h * 0.84f),
                    strokeWidth = strokeWidth * 0.95f,
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = NexaWarningAmber,
                    radius = strokeWidth * 0.65f,
                    center = Offset(w * 0.82f, h * 0.24f)
                )
            }
            InternetStatus.VALIDATED_INTERNET -> Unit
        }
    }
}
