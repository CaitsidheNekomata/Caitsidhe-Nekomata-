package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.HourlyMoodPoint
import com.example.ui.theme.CatCardBorderDark
import com.example.ui.theme.CatPrimaryAmber
import com.example.ui.theme.CatSecondaryTeal
import com.example.ui.theme.CatSurfaceDark

@Composable
fun MoodTrendChart(
    points: List<HourlyMoodPoint>,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CatSurfaceDark)
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "24-Hour Emotional Trend",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Hourly Indices",
                    style = MaterialTheme.typography.labelSmall,
                    color = CatSecondaryTeal
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val paddingBottom = 20.dp.toPx()
                val chartHeight = canvasHeight - paddingBottom

                val pointCount = points.size
                val stepX = canvasWidth / (pointCount - 1).coerceAtLeast(1)

                val linePath = Path()
                val fillPath = Path()

                val coords = points.mapIndexed { idx, pt ->
                    val x = idx * stepX
                    // Y: high score (1.0) is near top (0), low score (0.0) near bottom (chartHeight)
                    val y = (1f - pt.score) * chartHeight
                    Offset(x, y)
                }

                // Grid lines (0.25, 0.50, 0.75)
                listOf(0.25f, 0.5f, 0.75f).forEach { ratio ->
                    val gy = ratio * chartHeight
                    drawLine(
                        color = CatCardBorderDark.copy(alpha = 0.35f),
                        start = Offset(0f, gy),
                        end = Offset(canvasWidth, gy),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Smooth line using cubic beziers
                linePath.moveTo(coords[0].x, coords[0].y)
                fillPath.moveTo(coords[0].x, chartHeight)
                fillPath.lineTo(coords[0].x, coords[0].y)

                for (i in 0 until coords.size - 1) {
                    val p0 = coords[i]
                    val p1 = coords[i + 1]
                    val controlX1 = (p0.x + p1.x) / 2f
                    val controlY1 = p0.y
                    val controlX2 = (p0.x + p1.x) / 2f
                    val controlY2 = p1.y

                    linePath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                    fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                }

                fillPath.lineTo(coords.last().x, chartHeight)
                fillPath.close()

                // Draw gradient area under curve
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            CatSecondaryTeal.copy(alpha = 0.35f),
                            CatPrimaryAmber.copy(alpha = 0.05f)
                        ),
                        startY = 0f,
                        endY = chartHeight
                    )
                )

                // Draw stroke line
                drawPath(
                    path = linePath,
                    brush = Brush.horizontalGradient(
                        listOf(CatPrimaryAmber, CatSecondaryTeal)
                    ),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw dots at points
                coords.forEach { pt ->
                    drawCircle(
                        color = CatSurfaceDark,
                        radius = 5.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = CatSecondaryTeal,
                        radius = 3.dp.toPx(),
                        center = pt
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // X-Axis Hour Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                points.forEach { pt ->
                    Text(
                        text = pt.hourLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
