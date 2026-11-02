package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import com.example.model.BackgroundScaleMode
import com.example.model.BuiltInBackground

@Composable
fun BackgroundCanvas(
    selectedBackground: BuiltInBackground,
    customBitmap: ImageBitmap?,
    scaleMode: BackgroundScaleMode,
    dimOverlay: Float,
    lowEndMode: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF060916))
    ) {
        if (selectedBackground == BuiltInBackground.CUSTOM_IMAGE && customBitmap != null) {
            val contentScale = when (scaleMode) {
                BackgroundScaleMode.FILL_SCREEN -> ContentScale.FillBounds
                BackgroundScaleMode.FIT_IMAGE -> ContentScale.Fit
                BackgroundScaleMode.CENTER_CROP -> ContentScale.Crop
            }
            Image(
                bitmap = customBitmap,
                contentDescription = null,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )
            if (dimOverlay > 0.01f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = dimOverlay.coerceIn(0f, 0.85f)))
                )
            }
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                when (selectedBackground) {
                    BuiltInBackground.DARK_NAVY, BuiltInBackground.CUSTOM_IMAGE -> {
                        // Reference image midnight navy background with top-left & bottom blue-purple waves
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF060A17),
                                    Color(0xFF0B1126),
                                    Color(0xFF070B1A)
                                )
                            )
                        )
                        if (!lowEndMode) {
                            // Top-left blue-purple wave fill
                            val topWave = Path().apply {
                                moveTo(0f, 0f)
                                lineTo(w * 0.42f, 0f)
                                cubicTo(
                                    w * 0.30f, h * 0.14f,
                                    w * 0.14f, h * 0.13f,
                                    0f, h * 0.28f
                                )
                                close()
                            }
                            drawPath(
                                path = topWave,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0x557C3AED),
                                        Color(0x662563EB),
                                        Color(0x110F172A)
                                    ),
                                    start = Offset(0f, 0f),
                                    end = Offset(w * 0.42f, h * 0.28f)
                                )
                            )
                            // Top-left glowing edge stroke
                            val topStroke = Path().apply {
                                moveTo(0f, h * 0.18f)
                                cubicTo(
                                    w * 0.15f, h * 0.15f,
                                    w * 0.28f, h * 0.09f,
                                    w * 0.40f, 0f
                                )
                            }
                            drawPath(
                                path = topStroke,
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFD946EF),
                                        Color(0xFF38BDF8)
                                    )
                                ),
                                style = Stroke(width = 3.5f)
                            )

                            // Bottom blue-purple wave fill
                            val bottomWave = Path().apply {
                                moveTo(0f, h)
                                lineTo(0f, h * 0.90f)
                                cubicTo(
                                    w * 0.25f, h * 0.78f,
                                    w * 0.50f, h * 0.98f,
                                    w, h * 0.85f
                                )
                                lineTo(w, h)
                                close()
                            }
                            drawPath(
                                path = bottomWave,
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0x666D28D9),
                                        Color(0x552563EB),
                                        Color(0x660284C7)
                                    )
                                )
                            )
                            val bottomStroke = Path().apply {
                                moveTo(0f, h * 0.90f)
                                cubicTo(
                                    w * 0.25f, h * 0.78f,
                                    w * 0.50f, h * 0.98f,
                                    w, h * 0.85f
                                )
                            }
                            drawPath(
                                path = bottomStroke,
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFA855F7),
                                        Color(0xFFD946EF),
                                        Color(0xFF38BDF8)
                                    )
                                ),
                                style = Stroke(width = 3.5f)
                            )
                        }
                    }

                    BuiltInBackground.BLUE_PURPLE_GRADIENT -> {
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF1E1B4B),
                                    Color(0xFF172554)
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(w, h)
                            )
                        )
                    }

                    BuiltInBackground.MINIMAL_ABSTRACT -> {
                        drawRect(color = Color(0xFF080D1E))
                        drawCircle(
                            color = Color(0x227C3AED),
                            radius = h * 0.45f,
                            center = Offset(w * 0.2f, h * 0.25f)
                        )
                        drawCircle(
                            color = Color(0x220284C7),
                            radius = h * 0.50f,
                            center = Offset(w * 0.8f, h * 0.8f)
                        )
                    }

                    BuiltInBackground.NATURAL_LANDSCAPE -> {
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF050B18),
                                    Color(0xFF0F1E3A),
                                    Color(0xFF112A46)
                                )
                            )
                        )
                        // Minimalist night mountain ridges
                        val ridgeBack = Path().apply {
                            moveTo(0f, h)
                            lineTo(0f, h * 0.72f)
                            lineTo(w * 0.28f, h * 0.55f)
                            lineTo(w * 0.55f, h * 0.70f)
                            lineTo(w * 0.82f, h * 0.52f)
                            lineTo(w, h * 0.66f)
                            lineTo(w, h)
                            close()
                        }
                        drawPath(ridgeBack, color = Color(0xFF0B162C))
                        val ridgeFront = Path().apply {
                            moveTo(0f, h)
                            lineTo(0f, h * 0.80f)
                            lineTo(w * 0.42f, h * 0.66f)
                            lineTo(w * 0.74f, h * 0.78f)
                            lineTo(w, h * 0.70f)
                            lineTo(w, h)
                            close()
                        }
                        drawPath(ridgeFront, color = Color(0xFF070E1F))
                    }

                    BuiltInBackground.NIGHT_CITY -> {
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF070A18),
                                    Color(0xFF141836),
                                    Color(0xFF090D1E)
                                )
                            )
                        )
                        // Subtle night skyline silhouette at the bottom
                        val buildingWidths = floatArrayOf(0.08f, 0.06f, 0.09f, 0.07f, 0.10f, 0.08f, 0.07f, 0.09f, 0.08f, 0.09f, 0.10f, 0.09f)
                        val buildingHeights = floatArrayOf(0.18f, 0.26f, 0.15f, 0.30f, 0.22f, 0.14f, 0.27f, 0.19f, 0.24f, 0.16f, 0.28f, 0.20f)
                        var currentX = 0f
                        for (i in buildingWidths.indices) {
                            val bw = w * buildingWidths[i]
                            val bh = h * buildingHeights[i]
                            drawRect(
                                color = Color(0xFF060916),
                                topLeft = Offset(currentX, h - bh),
                                size = Size(bw - 4f, bh)
                            )
                            currentX += bw
                        }
                    }
                }
            }
        }
    }
}
