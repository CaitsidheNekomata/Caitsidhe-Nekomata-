package com.example.domain

import com.example.audio.AudioAnalysisResult
import com.example.data.model.CatProfile
import com.example.data.model.Vocalization

data class PostureData(
    val earState: String = "alert_forward", // airplane_flat, swiveling, relaxed
    val tailState: String = "upright_high", // question_mark, twitching_low, puffed, tucked
    val eyeState: String = "normal_focused", // slow_blink, wide_dilated, slitted
    val mouthState: String = "open_meow", // half_open, closed
    val environmentalTrigger: String = "none" // food_bowl, window_birds, closed_door, play_toy, stranger, rest_couch
)

data class TranslationResult(
    val translation: String,
    val emotionalState: String,
    val confidence: Double,
    val rationale: String,
    val suggestedAction: String
)

class TranslationEngine {

    fun translate(
        audio: AudioAnalysisResult,
        posture: PostureData,
        cat: CatProfile?,
        historicalVocalizations: List<Vocalization> = emptyList()
    ): TranslationResult {
        // Adjust baseline pitch based on cat's individual calibration
        val baseline = (cat?.baselinePitchHz ?: 520.0) + (cat?.calibrationOffset ?: 0.0)
        val relativePitchRatio = audio.pitchHz / baseline.coerceAtLeast(200.0)

        // Check if there are user manual tags for this specific cat with similar acoustic traits
        val matchedHistory = historicalVocalizations.firstOrNull { hist ->
            hist.manualEmotionTag != "none" &&
            kotlin.math.abs(hist.pitchHz - audio.pitchHz) < 80.0 &&
            (hist.environmentalTrigger == posture.environmentalTrigger || hist.vocalizationType == audio.vocalizationType)
        }

        // 1. Posture & Environmental Signal Precedence
        if (posture.earState == "airplane_flat" || posture.tailState == "puffed") {
            return TranslationResult(
                translation = "Step back! I am feeling deeply overwhelmed and defensive right now.",
                emotionalState = "Anxious",
                confidence = 0.94,
                rationale = "Flattened 'airplane' ears combined with tense body posture signal sensory overload or defensive arousal.",
                suggestedAction = "Give your cat quiet space. Avoid direct eye contact or attempting to pick them up."
            )
        }

        if (posture.eyeState == "slow_blink" || audio.vocalizationType == "purr") {
            return TranslationResult(
                translation = "I feel completely safe and serene with you. You are my favorite companion.",
                emotionalState = "Affectionate",
                confidence = 0.96,
                rationale = "Slow blinks ('cat kisses') and low purr frequencies communicate profound bonded trust.",
                suggestedAction = "Slowly blink your eyes back at them and offer gentle chin or cheek scratches."
            )
        }

        if (posture.environmentalTrigger == "food_bowl" || matchedHistory?.manualEmotionTag == "hungry") {
            val translation = if (audio.durationMs > 1000L || relativePitchRatio < 0.95) {
                "My food bowl has reached an emergency low! Please dispense wet food without delay."
            } else {
                "Is it mealtime yet? Checking on the status of my culinary provisions."
            }
            return TranslationResult(
                translation = translation,
                emotionalState = "Hungry",
                confidence = 0.92,
                rationale = "Food bowl proximity combined with prolonged vocal envelope indicates a classic feeding solicitation meow.",
                suggestedAction = "Check their food & fresh water bowls, or provide a small healthy treat."
            )
        }

        if (posture.environmentalTrigger == "window_birds" || audio.vocalizationType == "chatter") {
            return TranslationResult(
                translation = "Target acquired out the window! If only this invisible barrier were gone, I'd pounce!",
                emotionalState = "Playful",
                confidence = 0.91,
                rationale = "High-frequency staccato chattering directed at window reflects predatory excitement and hunting frustration.",
                suggestedAction = "Engage in an interactive wand toy play session to redirect that hunting drive."
            )
        }

        if (posture.environmentalTrigger == "closed_door") {
            return TranslationResult(
                translation = "Why is this portal of passage sealed against me? Grant me territory access immediately!",
                emotionalState = "Curious",
                confidence = 0.89,
                rationale = "Facing a closed door with repetitive vocalizations signals feline territory perimeter inspection.",
                suggestedAction = "Open the door or ensure your cat has engaging toys in the current room."
            )
        }

        if (posture.tailState == "question_mark" || audio.vocalizationType == "chirp_trill") {
            return TranslationResult(
                translation = "Oh hello! What are we doing right now? I'm excited you're here!",
                emotionalState = "Happy",
                confidence = 0.93,
                rationale = "Upright tail curved like a question mark paired with ascending trill is the universal feline friendly greeting.",
                suggestedAction = "Greet them in a warm, gentle voice or offer a quick head-butt/hand sniff."
            )
        }

        if (posture.tailState == "twitching_low") {
            return TranslationResult(
                translation = "My patience is wearing thin. Cease current stimulation or I may swipe.",
                emotionalState = "Annoyed",
                confidence = 0.87,
                rationale = "Low twitching or thumping tail tip warns of impending over-stimulation.",
                suggestedAction = "Pause petting or playing; allow your cat to reset their arousal level."
            )
        }

        // Default standard meow based on acoustic pitch ratio
        return if (relativePitchRatio > 1.2) {
            TranslationResult(
                translation = "Hey! Look at me right now, I need your attention!",
                emotionalState = "Playful",
                confidence = 0.85,
                rationale = "High-pitch meow above cat's baseline indicates urgency or energetic play arousal.",
                suggestedAction = "Toss a toy mouse or spend a couple of minutes offering affectionate play."
            )
        } else {
            TranslationResult(
                translation = "Just checking in and stating my presence in the room.",
                emotionalState = "Content",
                confidence = 0.82,
                rationale = "Standard fundamental pitch and balanced duration indicates casual social vocalization.",
                suggestedAction = "Acknowledge them with a friendly Talk Back reply!"
            )
        }
    }
}
