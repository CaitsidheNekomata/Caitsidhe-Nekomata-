package com.example.ui.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.CatAudioAnalyzer
import com.example.audio.TalkBackSynthesizer
import com.example.data.model.CatProfile
import com.example.data.model.Vocalization
import com.example.data.repository.CatRepository
import com.example.domain.PostureData
import com.example.domain.TranslationEngine
import com.example.domain.TranslationResult
import com.example.ui.components.ManualTaggingDialog
import com.example.ui.components.TalkBackDialog
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.CatCardBorderDark
import com.example.ui.theme.CatPrimaryAmber
import com.example.ui.theme.CatSecondaryTeal
import com.example.ui.theme.CatSurfaceDark
import com.example.ui.theme.CatSurfaceVariantDark
import com.example.ui.theme.MoodHappy
import com.example.ui.theme.MoodHungry
import com.example.ui.theme.WaveformCoral
import com.example.ui.theme.WaveformCyan
import kotlinx.coroutines.launch

@Composable
fun TranslatorTab(
    cats: List<CatProfile>,
    selectedCat: CatProfile?,
    onSelectCat: (CatProfile) -> Unit,
    audioAnalyzer: CatAudioAnalyzer,
    translationEngine: TranslationEngine,
    talkBackSynthesizer: TalkBackSynthesizer,
    repository: CatRepository,
    onNavigateToCamera: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val isListening by audioAnalyzer.isListening.collectAsState()
    val waveform by audioAnalyzer.liveWaveform.collectAsState()
    val currentPitch by audioAnalyzer.currentPitchHz.collectAsState()
    val currentDb by audioAnalyzer.currentRmsDb.collectAsState()
    val recentVocs by repository.recentVocalizations.collectAsState(initial = emptyList())

    var activePosture by remember { mutableStateOf(PostureData()) }
    var currentTranslation by remember { mutableStateOf<TranslationResult?>(null) }
    var latestVocalizationRecord by remember { mutableStateOf<Vocalization?>(null) }

    var showTalkBackDialog by remember { mutableStateOf(false) }
    var showManualTaggingDialog by remember { mutableStateOf(false) }

    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Cat Selection Pill Header
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Translating for:",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (selectedCat != null) {
                    Text(
                        text = "Baseline: ${selectedCat.baselinePitchHz.toInt()} Hz",
                        style = MaterialTheme.typography.labelSmall,
                        color = CatSecondaryTeal
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(cats) { cat ->
                    val isSelected = cat.id == selectedCat?.id
                    val catColor = try {
                        Color(android.graphics.Color.parseColor(cat.avatarColor))
                    } catch (e: Exception) {
                        CatPrimaryAmber
                    }
                    Card(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onSelectCat(cat) }
                            .testTag("cat_selector_${cat.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) CatSurfaceVariantDark else CatSurfaceDark
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, catColor) else null,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(catColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cat.name.take(1),
                                    fontWeight = FontWeight.Bold,
                                    color = CatSurfaceDark,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = cat.breed,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Selected Cat Vocal Quirks Banner
        if (selectedCat != null && selectedCat.vocalQuirks.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CatSurfaceVariantDark.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("✨", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${selectedCat.name}'s Quirk: ${selectedCat.vocalQuirks}",
                            style = MaterialTheme.typography.bodySmall,
                            color = CatSecondaryTeal
                        )
                    }
                }
            }
        }

        // Live Audio Visualizer Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CatSurfaceDark),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CatCardBorderDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isListening) WaveformCoral else Color.Gray)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isListening) "LISTENING FOR MEOWS..." else "MICROPHONE READY",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isListening) WaveformCoral else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Telemetry Badge
                        Text(
                            text = "${currentPitch.toInt()} Hz • ${currentDb.toInt()} dB",
                            style = MaterialTheme.typography.labelSmall,
                            color = WaveformCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    WaveformVisualizer(
                        waveform = waveform,
                        isListening = isListening,
                        currentPitchHz = currentPitch
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Microphone Action & Quick Test Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Big Mic Button
                        Button(
                            onClick = {
                                if (isListening) {
                                    audioAnalyzer.stopListening()
                                } else {
                                    audioAnalyzer.startListening { result ->
                                        val translation = translationEngine.translate(
                                            result,
                                            activePosture,
                                            selectedCat,
                                            recentVocs
                                        )
                                        currentTranslation = translation
                                        coroutineScope.launch {
                                            val savedVoc = repository.recordVocalization(
                                                Vocalization(
                                                    catId = selectedCat?.id ?: "unknown",
                                                    catName = selectedCat?.name ?: "Cat",
                                                    pitchHz = result.pitchHz,
                                                    durationMs = result.durationMs,
                                                    volumeDb = result.volumeDb,
                                                    vocalizationType = result.vocalizationType,
                                                    earState = activePosture.earState,
                                                    tailState = activePosture.tailState,
                                                    eyeState = activePosture.eyeState,
                                                    mouthState = activePosture.mouthState,
                                                    environmentalTrigger = activePosture.environmentalTrigger,
                                                    translation = translation.translation,
                                                    emotionalState = translation.emotionalState,
                                                    confidenceScore = translation.confidence
                                                )
                                            )
                                            latestVocalizationRecord = savedVoc
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isListening) WaveformCoral else CatPrimaryAmber
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("toggle_listening_button")
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = if (isListening) "Stop Listening" else "Start Listening",
                                tint = CatSurfaceDark,
                                modifier = Modifier
                                    .size(24.dp)
                                    .then(if (isListening) Modifier.scale(pulseScale) else Modifier)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isListening) "Listening Active..." else "Listen & Translate",
                                fontWeight = FontWeight.Bold,
                                color = CatSurfaceDark
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Camera Posture Button
                        IconButton(
                            onClick = onNavigateToCamera,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(CatSurfaceVariantDark)
                                .border(1.dp, CatCardBorderDark, RoundedCornerShape(16.dp))
                                .testTag("camera_posture_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Open Posture Camera",
                                tint = CatSecondaryTeal
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preset Simulation Row for Instant Testing
                    Text(
                        text = "Or test with simulated meow sound:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val presets = listOf("Hungry Dinner Yowl", "Greeting Trill", "Playful Chirp", "Affectionate Purr", "Anxious Squeak")
                        items(presets) { preset ->
                            SuggestionChip(
                                onClick = {
                                    audioAnalyzer.triggerSimulatedVocalization(preset) { result ->
                                        val translation = translationEngine.translate(
                                            result,
                                            activePosture,
                                            selectedCat,
                                            recentVocs
                                        )
                                        currentTranslation = translation
                                        coroutineScope.launch {
                                            val savedVoc = repository.recordVocalization(
                                                Vocalization(
                                                    catId = selectedCat?.id ?: "unknown",
                                                    catName = selectedCat?.name ?: "Cat",
                                                    pitchHz = result.pitchHz,
                                                    durationMs = result.durationMs,
                                                    volumeDb = result.volumeDb,
                                                    vocalizationType = result.vocalizationType,
                                                    earState = activePosture.earState,
                                                    tailState = activePosture.tailState,
                                                    eyeState = activePosture.eyeState,
                                                    mouthState = activePosture.mouthState,
                                                    environmentalTrigger = activePosture.environmentalTrigger,
                                                    translation = translation.translation,
                                                    emotionalState = translation.emotionalState,
                                                    confidenceScore = translation.confidence
                                                )
                                            )
                                            latestVocalizationRecord = savedVoc
                                        }
                                    }
                                },
                                label = { Text(preset, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.testTag("simulate_meow_${preset.take(6)}")
                            )
                        }
                    }
                }
            }
        }

        // Translation Result Card
        if (currentTranslation != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("translation_result_card"),
                    colors = CardDefaults.cardColors(containerColor = CatSurfaceDark),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, CatPrimaryAmber)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TRANSLATION",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = CatPrimaryAmber
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(CatSecondaryTeal.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = currentTranslation!!.emotionalState,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = CatSecondaryTeal
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${(currentTranslation!!.confidence * 100).toInt()}% match",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Large Human Readable Translation Bubble
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(CatSurfaceVariantDark)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "\"${currentTranslation!!.translation}\"",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 24.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Rationale & Tip
                        Text(
                            text = "Acoustic Cue: ${currentTranslation!!.rationale}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "💡 Pet Parent Tip: ${currentTranslation!!.suggestedAction}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MoodHappy
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Talk Back & Tagging Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = { showTalkBackDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = CatSecondaryTeal),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("open_talk_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RecordVoiceOver,
                                    contentDescription = "Talk Back",
                                    tint = CatSurfaceDark,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Talk Back", color = CatSurfaceDark, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            OutlinedButton(
                                onClick = { showManualTaggingDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("open_manual_tagging_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = "Tag & Refine",
                                    tint = CatPrimaryAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Tag & Refine", color = CatPrimaryAmber, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Feedback Rating
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Was this translation accurate?",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row {
                                IconButton(
                                    onClick = {
                                        latestVocalizationRecord?.let {
                                            coroutineScope.launch {
                                                repository.updateFeedback(it.id, "accurate")
                                            }
                                        }
                                    },
                                    modifier = Modifier.size(32.dp).testTag("feedback_accurate_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ThumbUp,
                                        contentDescription = "Accurate",
                                        tint = MoodHappy,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        latestVocalizationRecord?.let {
                                            coroutineScope.launch {
                                                repository.updateFeedback(it.id, "inaccurate")
                                            }
                                        }
                                    },
                                    modifier = Modifier.size(32.dp).testTag("feedback_inaccurate_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ThumbDown,
                                        contentDescription = "Inaccurate",
                                        tint = WaveformCoral,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent Meow History List
        item {
            Text(
                text = "Recent Vocalization Log",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(recentVocs.take(5)) { voc ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CatSurfaceDark),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CatCardBorderDark)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                when (voc.emotionalState) {
                                    "Hungry" -> MoodHungry.copy(alpha = 0.2f)
                                    "Affectionate", "Happy" -> MoodHappy.copy(alpha = 0.2f)
                                    else -> CatSecondaryTeal.copy(alpha = 0.2f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (voc.emotionalState) {
                                "Hungry" -> "🐟"
                                "Playful" -> "🧶"
                                "Affectionate" -> "🥰"
                                "Anxious" -> "😿"
                                "Annoyed" -> "😾"
                                else -> "😺"
                            },
                            fontSize = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = voc.catName,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = CatPrimaryAmber
                            )
                            Text(
                                text = "${voc.pitchHz.toInt()} Hz • ${voc.durationMs}ms",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "\"${voc.translation}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (voc.manualEmotionTag != "none") {
                            Text(
                                text = "🏷️ Tagged: ${voc.manualEmotionTag} (${voc.manualContextTag})",
                                style = MaterialTheme.typography.labelSmall,
                                color = CatSecondaryTeal
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(60.dp)) }
    }

    if (showTalkBackDialog) {
        TalkBackDialog(
            synthesizer = talkBackSynthesizer,
            onDismiss = { showTalkBackDialog = false }
        )
    }

    if (showManualTaggingDialog && latestVocalizationRecord != null) {
        ManualTaggingDialog(
            vocalization = latestVocalizationRecord!!,
            catName = selectedCat?.name ?: "Cat",
            onSaveTags = { emotion, context ->
                coroutineScope.launch {
                    repository.updateManualTags(latestVocalizationRecord!!.id, emotion, context)
                }
            },
            onDismiss = { showManualTaggingDialog = false }
        )
    }
}
