package com.example.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.sqrt

data class AudioAnalysisResult(
    val pitchHz: Double,
    val durationMs: Long,
    val volumeDb: Double,
    val vocalizationType: String,
    val confidence: Double
)

class CatAudioAnalyzer(private val coroutineScope: CoroutineScope) {
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null

    private val _isListening = MutableStateFlow(false)
    val isListening = _isListening.asStateFlow()

    private val _liveWaveform = MutableStateFlow(FloatArray(40) { 0f })
    val liveWaveform = _liveWaveform.asStateFlow()

    private val _currentRmsDb = MutableStateFlow(-60f)
    val currentRmsDb = _currentRmsDb.asStateFlow()

    private val _currentPitchHz = MutableStateFlow(0.0)
    val currentPitchHz = _currentPitchHz.asStateFlow()

    private val _lastDetectedAudio = MutableStateFlow<AudioAnalysisResult?>(null)
    val lastDetectedAudio = _lastDetectedAudio.asStateFlow()

    private val sampleRate = 16000
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast(2048)

    @SuppressLint("MissingPermission")
    fun startListening(onMeowDetected: ((AudioAnalysisResult) -> Unit)? = null) {
        if (_isListening.value) return
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )
            audioRecord?.startRecording()
            _isListening.value = true

            recordingJob = coroutineScope.launch(Dispatchers.Default) {
                val audioBuffer = ShortArray(bufferSize)
                var vocalizationStartTime: Long = 0
                var peakRmsDb = -60.0
                var pitchSum = 0.0
                var pitchCount = 0

                while (isActive && _isListening.value) {
                    val readSamples = audioRecord?.read(audioBuffer, 0, bufferSize) ?: -1
                    if (readSamples > 0) {
                        // 1. Calculate RMS Volume
                        var sumSquared = 0.0
                        for (i in 0 until readSamples) {
                            val sample = audioBuffer[i]
                            sumSquared += sample * sample
                        }
                        val rms = sqrt(sumSquared / readSamples)
                        val rmsDb = if (rms > 1.0) 20 * log10(rms / 32767.0) else -60.0
                        _currentRmsDb.value = rmsDb.toFloat()

                        // 2. Generate Waveform preview (sub-sample to 40 bars)
                        val step = (readSamples / 40).coerceAtLeast(1)
                        val wave = FloatArray(40)
                        for (i in 0 until 40) {
                            val idx = (i * step).coerceAtMost(readSamples - 1)
                            wave[i] = (abs(audioBuffer[idx].toInt()) / 32767.0f).coerceIn(0.05f, 1f)
                        }
                        _liveWaveform.value = wave

                        // 3. Estimate fundamental pitch using Zero-Crossing Rate & Peak Autocorrelation
                        val pitch = estimatePitch(audioBuffer, readSamples, sampleRate)
                        if (pitch in 100.0..3000.0) {
                            _currentPitchHz.value = pitch
                        }

                        // 4. Vocalization onset & offset detection
                        // Normal room ambient is usually < -35 dB. Meow sound is typically > -30 dB
                        if (rmsDb > -30.0) {
                            if (vocalizationStartTime == 0L) {
                                vocalizationStartTime = System.currentTimeMillis()
                                peakRmsDb = rmsDb
                                pitchSum = 0.0
                                pitchCount = 0
                            } else {
                                if (rmsDb > peakRmsDb) peakRmsDb = rmsDb
                            }
                            if (pitch > 150.0) {
                                pitchSum += pitch
                                pitchCount++
                            }
                        } else {
                            if (vocalizationStartTime != 0L) {
                                val duration = System.currentTimeMillis() - vocalizationStartTime
                                // Valid cat vocalization duration: between 180ms and 3500ms
                                if (duration in 180..3500) {
                                    val avgPitch = if (pitchCount > 0) pitchSum / pitchCount else 520.0
                                    val type = classifyVocalization(avgPitch, duration, peakRmsDb)
                                    val result = AudioAnalysisResult(
                                        pitchHz = avgPitch,
                                        durationMs = duration,
                                        volumeDb = peakRmsDb,
                                        vocalizationType = type,
                                        confidence = 0.88
                                    )
                                    _lastDetectedAudio.value = result
                                    onMeowDetected?.invoke(result)
                                }
                                vocalizationStartTime = 0L
                                peakRmsDb = -60.0
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("CatAudioAnalyzer", "Failed to start AudioRecord", e)
            _isListening.value = false
        }
    }

    fun stopListening() {
        _isListening.value = false
        recordingJob?.cancel()
        recordingJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            Log.e("CatAudioAnalyzer", "Error releasing AudioRecord", e)
        }
        audioRecord = null
    }

    // High quality pitch estimation via simplified autocorrelation
    private fun estimatePitch(buffer: ShortArray, length: Int, sampleRate: Int): Double {
        val minPeriod = sampleRate / 2500 // Max ~2500Hz
        val maxPeriod = sampleRate / 150  // Min ~150Hz
        var bestCorrelation = 0.0
        var bestPeriod = 0

        for (period in minPeriod until maxPeriod.coerceAtMost(length / 2)) {
            var correlation = 0.0
            for (i in 0 until (length - period)) {
                correlation += buffer[i].toDouble() * buffer[i + period].toDouble()
            }
            if (correlation > bestCorrelation) {
                bestCorrelation = correlation
                bestPeriod = period
            }
        }
        return if (bestPeriod > 0) sampleRate.toDouble() / bestPeriod else 0.0
    }

    private fun classifyVocalization(pitchHz: Double, durationMs: Long, volumeDb: Double): String {
        return when {
            pitchHz in 650.0..1800.0 && durationMs < 500L -> "chirp_trill"
            pitchHz in 350.0..950.0 && durationMs > 1300L && volumeDb > -18.0 -> "demanding_yowl"
            pitchHz in 20.0..150.0 -> "purr"
            pitchHz > 900.0 && durationMs < 350L -> "chatter"
            volumeDb > -12.0 && pitchHz < 300.0 -> "hiss_growl"
            durationMs < 450L && volumeDb < -25.0 -> "soft_mew"
            else -> "standard_meow"
        }
    }

    // Simulation trigger for testing without a live cat nearby
    fun triggerSimulatedVocalization(presetName: String, onDetected: (AudioAnalysisResult) -> Unit) {
        val simulated = when (presetName) {
            "Greeting Trill" -> AudioAnalysisResult(680.0, 420L, -14.0, "chirp_trill", 0.95)
            "Hungry Dinner Yowl" -> AudioAnalysisResult(480.0, 1600L, -8.0, "demanding_yowl", 0.93)
            "Playful Chirp" -> AudioAnalysisResult(850.0, 310L, -16.0, "chatter", 0.91)
            "Affectionate Purr" -> AudioAnalysisResult(45.0, 2200L, -28.0, "purr", 0.96)
            "Anxious Squeak" -> AudioAnalysisResult(920.0, 520L, -11.0, "standard_meow", 0.89)
            else -> AudioAnalysisResult(530.0, 780L, -12.0, "standard_meow", 0.92)
        }
        _lastDetectedAudio.value = simulated
        onDetected(simulated)
    }
}
