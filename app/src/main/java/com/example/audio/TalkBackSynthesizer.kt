package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.sin

data class TalkBackOption(
    val id: String,
    val phrase: String,
    val catInterpretation: String,
    val acousticType: String,
    val iconEmoji: String
)

class TalkBackSynthesizer {

    val predefinedResponses = listOf(
        TalkBackOption(
            id = "hello",
            phrase = "Hello there!",
            catInterpretation = "Friendly high-pitched trill (Greeting)",
            acousticType = "Trill Chirp",
            iconEmoji = "👋"
        ),
        TalkBackOption(
            id = "hear_you",
            phrase = "I hear you!",
            catInterpretation = "Soft dual-harmonic acknowledgement (Validation)",
            acousticType = "Soft Mew",
            iconEmoji = "👂"
        ),
        TalkBackOption(
            id = "coming",
            phrase = "Okay, I'm coming!",
            catInterpretation = "Reassuring descending stepping whistle (Attention acknowledged)",
            acousticType = "Stepping Glide",
            iconEmoji = "🏃"
        ),
        TalkBackOption(
            id = "dinner",
            phrase = "Dinner is served!",
            catInterpretation = "Upbeat rhythmic chirp harmonic (Resource reward cue)",
            acousticType = "Food Call Chirp",
            iconEmoji = "🥣"
        ),
        TalkBackOption(
            id = "calm",
            phrase = "Calm down, all is safe",
            catInterpretation = "Low rhythmic soothing purr vibration (Comfort)",
            acousticType = "Purr Harmonic Drone",
            iconEmoji = "🧘"
        ),
        TalkBackOption(
            id = "play",
            phrase = "Let's play!",
            catInterpretation = "Rapid double squeak staccato (Play solicitation)",
            acousticType = "Double Chirrup",
            iconEmoji = "🧶"
        )
    )

    suspend fun playSynthesizedResponse(option: TalkBackOption) = withContext(Dispatchers.Default) {
        val sampleRate = 22050
        val durationSeconds = when (option.id) {
            "hello" -> 0.45
            "hear_you" -> 0.40
            "coming" -> 0.60
            "dinner" -> 0.50
            "calm" -> 0.90
            "play" -> 0.40
            else -> 0.50
        }

        val numSamples = (durationSeconds * sampleRate).toInt()
        val generatedSnd = ShortArray(numSamples)

        // Generate synthetic waveform tailored to friendly feline vocal frequencies
        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            // Apply Hanning/cosine window envelope for smooth non-clicking attack/release
            val envelope = (0.5 * (1 - kotlin.math.cos(2 * PI * progress))).coerceIn(0.0, 1.0)

            val sampleVal: Double = when (option.id) {
                "hello" -> {
                    // Gentle rising frequency sweep from 580Hz to 760Hz
                    val freq = 580.0 + (180.0 * progress)
                    sin(2 * PI * freq * i / sampleRate) * 0.75 +
                    sin(4 * PI * freq * i / sampleRate) * 0.25 // subtle 2nd harmonic
                }
                "hear_you" -> {
                    // Soft warm dual tone at 500Hz + 520Hz creating soothing acoustic beating
                    val f1 = 500.0
                    val f2 = 525.0
                    (sin(2 * PI * f1 * i / sampleRate) + sin(2 * PI * f2 * i / sampleRate)) * 0.45
                }
                "coming" -> {
                    // Friendly descending glide 650Hz -> 480Hz
                    val freq = 650.0 - (170.0 * progress)
                    sin(2 * PI * freq * i / sampleRate) * 0.8
                }
                "dinner" -> {
                    // Double pulse chirrup
                    val freq = 740.0 + 80.0 * sin(4 * PI * progress)
                    sin(2 * PI * freq * i / sampleRate) * 0.85
                }
                "calm" -> {
                    // Low gentle purr vibration (28Hz amplitude modulation of 95Hz fundamental)
                    val carrier = sin(2 * PI * 95.0 * i / sampleRate)
                    val purrModulation = 0.5 + 0.5 * sin(2 * PI * 26.0 * i / sampleRate)
                    carrier * purrModulation * 0.9
                }
                "play" -> {
                    // Rapid staccato chirps
                    val chirpEnvelope = sin(4 * PI * progress).coerceAtLeast(0.0)
                    val freq = 820.0 + 200.0 * progress
                    sin(2 * PI * freq * i / sampleRate) * chirpEnvelope * 0.85
                }
                else -> {
                    sin(2 * PI * 550.0 * i / sampleRate) * 0.7
                }
            }

            // Convert to 16-bit PCM short
            val pcm = (sampleVal * envelope * 24000.0).toInt().coerceIn(-32767, 32767)
            generatedSnd[i] = pcm.toShort()
        }

        try {
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(numSamples * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(generatedSnd, 0, numSamples)
            audioTrack.play()
            // Wait for duration to complete before releasing
            kotlinx.coroutines.delay((durationSeconds * 1000).toLong() + 50)
            audioTrack.stop()
            audioTrack.release()
        } catch (e: Exception) {
            Log.e("TalkBackSynthesizer", "Error generating talkback audio", e)
        }
    }
}
