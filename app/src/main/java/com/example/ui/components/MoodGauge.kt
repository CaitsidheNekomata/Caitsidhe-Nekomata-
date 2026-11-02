package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CatCardBorderDark
import com.example.ui.theme.MoodAffectionate
import com.example.ui.theme.MoodAnnoyed
import com.example.ui.theme.MoodAnxious
import com.example.ui.theme.MoodContent
import com.example.ui.theme.MoodHappy
import com.example.ui.theme.MoodHungry
import com.example.ui.theme.MoodPlayful

val MoodAffectionateColor = Color(0xFFE91E63)

@Composable
fun MoodGauge(
    score: Double,
    mood: String,
    emoji: String,
    modifier: Modifier = Modifier,
    size: Dp = 140.dp
) {
    val animatedScore by animateFloatAsState(
        targetValue = score.toFloat().coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "moodScore"
    )

    val moodColor = when (mood) {
        "Affectionate" -> MoodAffectionate
        "Happy" -> MoodHappy
        "Playful" -> MoodPlayful
        "Content" -> MoodContent
        "Hungry" -> MoodHungry
        "Annoyed" -> MoodAnnoyed
        "Anxious" -> MoodAnxious
        else -> MoodContent
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = 12.dp.toPx()
            val arcSize = size.toPx() - strokeWidth
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

            // Background Track
            drawArc(
                color = CatCardBorderDark.copy(alpha = 0.5f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active Colored Arc
            val activeSweep = 270f * animatedScore
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(moodColor.copy(alpha = 0.7f), moodColor)
                ),
                startAngle = 135f,
                sweepAngle = activeSweep,
                useCenter = false,
                topLeft = topLeft,
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = emoji,
                fontSize = (size.value * 0.24f).sp
            )
            Text(
                text = mood,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = moodColor
            )
            Text(
                text = "${(animatedScore * 100).toInt()}% Wellness",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
