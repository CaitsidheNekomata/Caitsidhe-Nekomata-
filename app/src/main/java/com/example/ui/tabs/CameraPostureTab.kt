package com.example.ui.tabs

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.audio.AudioAnalysisResult
import com.example.audio.CatAudioAnalyzer
import com.example.data.model.CatProfile
import com.example.data.model.Vocalization
import com.example.data.repository.CatRepository
import com.example.domain.PostureData
import com.example.domain.TranslationEngine
import com.example.domain.TranslationResult
import com.example.ui.theme.CatAubergineDark
import com.example.ui.theme.CatCardBorderDark
import com.example.ui.theme.CatPrimaryAmber
import com.example.ui.theme.CatSecondaryTeal
import com.example.ui.theme.CatSurfaceDark
import com.example.ui.theme.CatSurfaceVariantDark
import com.example.ui.theme.MoodHappy
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CameraPostureTab(
    selectedCat: CatProfile?,
    audioAnalyzer: CatAudioAnalyzer,
    translationEngine: TranslationEngine,
    repository: CatRepository
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var earState by remember { mutableStateOf("alert_forward") }
    var tailState by remember { mutableStateOf("upright_high") }
    var eyeState by remember { mutableStateOf("slow_blink") }
    var mouthState by remember { mutableStateOf("open_meow") }
    var envTrigger by remember { mutableStateOf("food_bowl") }

    var cameraStarted by remember { mutableStateOf(false) }
    var cameraError by remember { mutableStateOf(false) }
    var correlationResult by remember { mutableStateOf<TranslationResult?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Camera Posture Correlation",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Multi-modal audio + posture translation HUD",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CatSecondaryTeal.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "AR Vision",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CatSecondaryTeal
                    )
                }
            }
        }

        // Camera Viewport with AR Overlay
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black)
                    .border(2.dp, CatCardBorderDark, RoundedCornerShape(20.dp))
            ) {
                if (!cameraError) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                try {
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = Preview.Builder().build().also {
                                        it.surfaceProvider = previewView.surfaceProvider
                                    }
                                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                                    cameraStarted = true
                                } catch (e: Exception) {
                                    Log.e("CameraPostureTab", "Camera initialization failed", e)
                                    cameraError = true
                                }
                            }, ContextCompat.getMainExecutor(ctx))
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // If camera unavailable (emulator or permissions), display sleek posture simulation viewfinder
                if (cameraError || !cameraStarted) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(CatSurfaceDark, CatAubergineDark)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Camera viewfinder",
                                tint = CatSecondaryTeal,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Live Posture Scanner Ready",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Select visible cues below to correlate with meow",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // AR Overlays on Camera
                // Top Telemetry Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "EARS: $earState",
                            style = MaterialTheme.typography.labelSmall,
                            color = CatPrimaryAmber
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "TAIL: $tailState",
                            style = MaterialTheme.typography.labelSmall,
                            color = CatSecondaryTeal
                        )
                    }
                }

                // Reticle in center
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.Center)
                        .border(1.dp, CatSecondaryTeal.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                )
            }
        }

        // Visual Behavioral Feature Selectors
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CatSurfaceDark),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CatCardBorderDark)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Detected Physical Signals & Posture",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = CatSecondaryTeal
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 1. Ear Posture
                    Text("Ear Posture:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            "alert_forward" to "👂 Alert Forward",
                            "airplane_flat" to "✈️ Airplane Flat",
                            "swiveling" to "📡 Swiveling"
                        ).forEach { (key, label) ->
                            PostureChip(label = label, isSelected = earState == key) { earState = key }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Tail Movement
                    Text("Tail Movement:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            "upright_high" to "⬆️ Upright High",
                            "question_mark" to "❓ Question Mark",
                            "twitching_low" to "⚡ Low Twitch",
                            "puffed" to "🦔 Puffed Bottle"
                        ).forEach { (key, label) ->
                            PostureChip(label = label, isSelected = tailState == key) { tailState = key }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. Eye Expression
                    Text("Eye & Facial Expression:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            "slow_blink" to "😌 Slow Blink",
                            "wide_dilated" to "👀 Wide Dilated",
                            "normal_focused" to "👁️ Focused"
                        ).forEach { (key, label) ->
                            PostureChip(label = label, isSelected = eyeState == key) { eyeState = key }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4. Environmental Trigger
                    Text("Environmental Trigger:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            "food_bowl" to "🥣 Food Bowl",
                            "window_birds" to "🪟 Window Birds",
                            "closed_door" to "🚪 Closed Door",
                            "play_toy" to "🧶 Play Toy",
                            "rest_couch" to "🛋️ Couch / Bed"
                        ).forEach { (key, label) ->
                            PostureChip(label = label, isSelected = envTrigger == key) { envTrigger = key }
                        }
                    }
                }
            }
        }

        // Correlate & Translate Action Button
        item {
            Button(
                onClick = {
                    val posture = PostureData(
                        earState = earState,
                        tailState = tailState,
                        eyeState = eyeState,
                        mouthState = mouthState,
                        environmentalTrigger = envTrigger
                    )
                    // Combine with audio analysis
                    val mockAudio = AudioAnalysisResult(
                        pitchHz = selectedCat?.baselinePitchHz ?: 540.0,
                        durationMs = 850L,
                        volumeDb = -12.0,
                        vocalizationType = "standard_meow",
                        confidence = 0.92
                    )
                    val result = translationEngine.translate(mockAudio, posture, selectedCat)
                    correlationResult = result

                    coroutineScope.launch {
                        repository.recordVocalization(
                            Vocalization(
                                catId = selectedCat?.id ?: "unknown",
                                catName = selectedCat?.name ?: "Cat",
                                pitchHz = mockAudio.pitchHz,
                                durationMs = mockAudio.durationMs,
                                volumeDb = mockAudio.volumeDb,
                                vocalizationType = mockAudio.vocalizationType,
                                earState = earState,
                                tailState = tailState,
                                eyeState = eyeState,
                                mouthState = mouthState,
                                environmentalTrigger = envTrigger,
                                translation = result.translation,
                                emotionalState = result.emotionalState,
                                confidenceScore = result.confidence
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CatPrimaryAmber),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("correlate_posture_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Correlate Posture",
                    tint = CatSurfaceDark
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Correlate Posture & Translate Meow",
                    fontWeight = FontWeight.Bold,
                    color = CatSurfaceDark,
                    fontSize = 16.sp
                )
            }
        }

        // Correlated Result Output
        if (correlationResult != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("posture_correlation_result"),
                    colors = CardDefaults.cardColors(containerColor = CatSurfaceDark),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, CatSecondaryTeal)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "HOLISTIC TRANSLATION",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = CatSecondaryTeal
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CatPrimaryAmber.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = correlationResult!!.emotionalState,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CatPrimaryAmber
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "\"${correlationResult!!.translation}\"",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 24.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Posture Insight: ${correlationResult!!.rationale}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Action: ${correlationResult!!.suggestedAction}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MoodHappy
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(60.dp)) }
    }
}

@Composable
fun PostureChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) CatSecondaryTeal else CatSurfaceVariantDark)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) CatSurfaceDark else MaterialTheme.colorScheme.onSurface
        )
    }
}
