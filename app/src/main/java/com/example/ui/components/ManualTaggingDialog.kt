package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Vocalization
import com.example.ui.theme.CatCardBorderDark
import com.example.ui.theme.CatPrimaryAmber
import com.example.ui.theme.CatSecondaryTeal
import com.example.ui.theme.CatSurfaceDark
import com.example.ui.theme.CatSurfaceVariantDark

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ManualTaggingDialog(
    vocalization: Vocalization,
    catName: String,
    onSaveTags: (emotion: String, context: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedEmotion by remember {
        mutableStateOf(if (vocalization.manualEmotionTag != "none") vocalization.manualEmotionTag else "hungry")
    }
    var selectedContext by remember {
        mutableStateOf(if (vocalization.manualContextTag != "none") vocalization.manualContextTag else "at food bowl")
    }

    val emotions = listOf(
        "hungry" to "🐟 Hungry",
        "playful" to "🧶 Playful",
        "annoyed" to "😾 Annoyed",
        "greeting" to "👋 Greeting",
        "affectionate" to "🥰 Affectionate",
        "anxious" to "😿 Anxious"
    )

    val contexts = listOf(
        "at food bowl" to "🥣 At Food Bowl",
        "at door" to "🚪 At Door",
        "seeing another cat" to "🐈 Seeing Another Cat",
        "on human lap" to "🛋️ On Lap / Bed",
        "window birds" to "🪟 Window Birds",
        "near litter box" to "📦 Near Box"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .testTag("manual_tagging_dialog"),
            color = CatSurfaceDark,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Model Refinement",
                            tint = CatPrimaryAmber
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Refine Model for $catName",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "Tagging this meow refines $catName's individual vocal translation profile.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Emotion Tags
                Text(
                    text = "Specific Emotion:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = CatSecondaryTeal
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    emotions.forEach { (key, label) ->
                        val isSelected = selectedEmotion == key
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) CatPrimaryAmber else CatSurfaceVariantDark
                                )
                                .clickable { selectedEmotion = key }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("tag_emotion_$key")
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) CatSurfaceDark else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Context Tags
                Text(
                    text = "Environmental Context:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = CatSecondaryTeal
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    contexts.forEach { (key, label) ->
                        val isSelected = selectedContext == key
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) CatSecondaryTeal else CatSurfaceVariantDark
                                )
                                .clickable { selectedContext = key }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("tag_context_$key")
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) CatSurfaceDark else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            onSaveTags(selectedEmotion, selectedContext)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CatPrimaryAmber),
                        modifier = Modifier.testTag("save_tags_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save and refine",
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text("Save & Refine", color = CatSurfaceDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
