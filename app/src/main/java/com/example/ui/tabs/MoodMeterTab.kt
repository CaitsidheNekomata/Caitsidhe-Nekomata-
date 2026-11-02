package com.example.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.components.ExportDataDialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CatProfile
import com.example.data.repository.CatRepository
import com.example.domain.MoodMeterEngine
import com.example.ui.components.MoodGauge
import com.example.ui.components.MoodTrendChart
import com.example.ui.theme.CatCardBorderDark
import com.example.ui.theme.CatPrimaryAmber
import com.example.ui.theme.CatSecondaryTeal
import com.example.ui.theme.CatSurfaceDark
import com.example.ui.theme.CatSurfaceVariantDark
import com.example.ui.theme.MoodAffectionate
import com.example.ui.theme.MoodHappy
import com.example.ui.theme.MoodHungry
import com.example.ui.theme.MoodPlayful

@Composable
fun MoodMeterTab(
    selectedCat: CatProfile?,
    repository: CatRepository,
    moodMeterEngine: MoodMeterEngine
) {
    val recentVocs by repository.recentVocalizations.collectAsState(initial = emptyList())
    val moodLogs by (selectedCat?.let { repository.getMoodLogsForCat(it.id) }
        ?: repository.getMoodLogsForCat("mochi_default")).collectAsState(initial = emptyList())

    val moodOverview = moodMeterEngine.computeMoodOverview(recentVocs, moodLogs)

    var showExportDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("mood_meter_tab"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Mood Meter",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Real-time emotional wellness for ${selectedCat?.name ?: "Cat"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (selectedCat != null) {
                        IconButton(
                            onClick = { showExportDialog = true },
                            modifier = Modifier.testTag("export_mood_csv_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Export CSV",
                                tint = CatPrimaryAmber
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(CatSurfaceVariantDark)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = "24h Active",
                                tint = CatSecondaryTeal,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "24h Monitor",
                                style = MaterialTheme.typography.labelSmall,
                                color = CatSecondaryTeal,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Primary Mood Overview Card with Gauge
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CatSurfaceDark),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CatCardBorderDark)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MoodGauge(
                        score = moodOverview.score,
                        mood = moodOverview.currentMood,
                        emoji = moodOverview.emoji,
                        size = 160.dp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = moodOverview.summaryText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tip Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(CatSurfaceVariantDark)
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "Tip",
                                tint = CatPrimaryAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = moodOverview.actionableTip,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 24-Hour Trend Chart
        item {
            MoodTrendChart(points = moodOverview.hourlyTrend)
        }

        // Emotion Breakdown Bars
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CatSurfaceDark),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CatCardBorderDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Vocalization Emotion Distribution",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    EmotionBar(label = "Happy / Content", percentage = 0.55f, color = MoodHappy)
                    Spacer(modifier = Modifier.height(8.dp))
                    EmotionBar(label = "Affectionate", percentage = 0.20f, color = MoodAffectionate)
                    Spacer(modifier = Modifier.height(8.dp))
                    EmotionBar(label = "Playful", percentage = 0.15f, color = MoodPlayful)
                    Spacer(modifier = Modifier.height(8.dp))
                    EmotionBar(label = "Hungry Demands", percentage = 0.10f, color = MoodHungry)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(60.dp)) }
    }

    if (showExportDialog && selectedCat != null) {
        ExportDataDialog(
            cat = selectedCat,
            vocalizations = recentVocs,
            moodLogs = moodLogs,
            onDismiss = { showExportDialog = false }
        )
    }
}

@Composable
fun EmotionBar(
    label: String,
    percentage: Float,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${(percentage * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = CatSurfaceVariantDark
        )
    }
}
