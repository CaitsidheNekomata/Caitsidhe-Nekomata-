package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CatAubergineDark
import com.example.ui.theme.CatSurfaceVariantDark
import com.example.ui.theme.WaveformCoral
import com.example.ui.theme.WaveformCyan
import com.example.ui.theme.WaveformGold

@Composable
fun WaveformVisualizer(
    waveform: FloatArray,
    isListening: Boolean,
    currentPitchHz: Double,
    modifier: Modifier = Modifier
) {
    val animatedPitch by animateFloatAsState(
        targetValue = currentPitchHz.toFloat(),
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "pitch"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CatAubergineDark)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val barCount = waveform.size.coerceAtLeast(1)
            val canvasWidth = size.width
            val canvasHeight = size.height
            val barSpacing = 4.dp.toPx()
            val totalSpacing = barSpacing * (barCount - 1)
            val barWidth = ((canvasWidth - totalSpacing) / barCount).coerceAtLeast(2f)
            val centerY = canvasHeight / 2f

            // Baseline center guide line
            drawLine(
                color = CatSurfaceVariantDark,
                start = Offset(0f, centerY),
                end = Offset(canvasWidth, centerY),
                strokeWidth = 1.dp.toPx()
            )

            val brush = Brush.verticalGradient(
                colors = if (isListening) {
                    if (animatedPitch > 600f) {
                        listOf(WaveformCoral, WaveformGold, WaveformCyan)
                    } else {
                        listOf(WaveformCyan, WaveformGold, WaveformCyan)
                    }
                } else {
                    listOf(Color(0xFF5C5478), Color(0xFF38314F))
                }
            )

            for (i in 0 until barCount) {
                val amp = if (isListening) waveform[i].coerceIn(0.08f, 1f) else 0.08f
                val barHeight = (amp * (canvasHeight * 0.85f)).coerceAtLeast(6f)
                val x = i * (barWidth + barSpacing)
                val top = centerY - (barHeight / 2f)

                drawRoundRect(
                    brush = brush,
                    topLeft = Offset(x, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                )
            }
        }
    }
}
