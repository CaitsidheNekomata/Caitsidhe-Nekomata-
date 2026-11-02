package com.example.domain

import com.example.data.model.MoodLog
import com.example.data.model.Vocalization

data class MoodOverview(
    val currentMood: String, // Happy, Content, Playful, Hungry, Anxious, Annoyed, Bored, Affectionate
    val score: Double, // 0.0 to 1.0 (happiness / contentment wellness index)
    val emoji: String,
    val summaryText: String,
    val actionableTip: String,
    val hourlyTrend: List<HourlyMoodPoint>
)

data class HourlyMoodPoint(
    val hourLabel: String,
    val score: Float,
    val mood: String
)

class MoodMeterEngine {

    fun computeMoodOverview(
        recentVocalizations: List<Vocalization>,
        historicalMoodLogs: List<MoodLog>
    ): MoodOverview {
        if (recentVocalizations.isEmpty()) {
            return MoodOverview(
                currentMood = "Content",
                score = 0.82,
                emoji = "😺",
                summaryText = "Resting comfortably in a calm, balanced state.",
                actionableTip = "Keep the environment calm and comfortable.",
                hourlyTrend = generateDefaultTrend(0.82f)
            )
        }

        // Tally recent emotions
        val sample = recentVocalizations.take(10)
        val emotionCounts = sample.groupBy { it.emotionalState }
            .mapValues { it.value.size }

        val dominantEmotion = emotionCounts.maxByOrNull { it.value }?.key ?: "Content"

        val score = when (dominantEmotion) {
            "Affectionate" -> 0.95
            "Happy" -> 0.90
            "Playful" -> 0.88
            "Content" -> 0.82
            "Curious" -> 0.75
            "Hungry" -> 0.65
            "Bored" -> 0.55
            "Annoyed" -> 0.40
            "Anxious" -> 0.30
            else -> 0.75
        }

        val (emoji, summary, tip) = when (dominantEmotion) {
            "Affectionate" -> Triple(
                "🥰",
                "Deeply bonded and seeking close companionship.",
                "Shower them with chin scratches, warm praise, or snuggle time."
            )
            "Happy" -> Triple(
                "😸",
                "Purring along in great spirits and positive social connection.",
                "Maintain this happy momentum with friendly conversation."
            )
            "Playful" -> Triple(
                "😼",
                "Energetic and eager for mental/physical stimulation.",
                "Break out feather wands, laser pointers, or crinkle balls!"
            )
            "Content" -> Triple(
                "🐱",
                "Peaceful, relaxed, and feeling safe in their territory.",
                "A gentle head pat or quiet companionship is ideal."
            )
            "Hungry" -> Triple(
                "🐟",
                "Focused on mealtime and eager for a bowl replenishment.",
                "Check food dish levels and offer fresh filtered water."
            )
            "Bored" -> Triple(
                "🥱",
                "Under-stimulated; looking for something new to inspect.",
                "Introduce an empty cardboard box or switch out their toys."
            )
            "Annoyed" -> Triple(
                "😾",
                "Sensory limits reached; feeling over-stimulated.",
                "Give them immediate personal space and reduce ambient noise."
            )
            "Anxious" -> Triple(
                "😿",
                "Tense or guarded due to strange sounds or unfamiliar stimuli.",
                "Speak in hushed tones, lower bright lights, and provide cozy hiding nooks."
            )
            else -> Triple(
                "🐾",
                "Balanced feline disposition.",
                "Observe their ear and tail movements for more subtle signals."
            )
        }

        // Compute 24-hour hourly trend
        val trend = compute24HourTrend(historicalMoodLogs, recentVocalizations, score.toFloat())

        return MoodOverview(
            currentMood = dominantEmotion,
            score = score,
            emoji = emoji,
            summaryText = summary,
            actionableTip = tip,
            hourlyTrend = trend
        )
    }

    private fun compute24HourTrend(
        moodLogs: List<MoodLog>,
        recentVocalizations: List<Vocalization>,
        currentScore: Float
    ): List<HourlyMoodPoint> {
        val now = System.currentTimeMillis()
        val oneHourMs = 3600 * 1000L
        val points = mutableListOf<HourlyMoodPoint>()

        // 8 points representing 3-hour intervals over past 24 hours
        for (i in 7 downTo 0) {
            val hourOffset = i * 3
            val timeBucket = now - (hourOffset * oneHourMs)
            val hourLabel = if (hourOffset == 0) "Now" else "-${hourOffset}h"

            // Look for matching mood logs in this window
            val matchedLog = moodLogs.firstOrNull { log ->
                val logTime = (log.createdAt?.seconds ?: 0L) * 1000L
                kotlin.math.abs(logTime - timeBucket) < (oneHourMs * 2)
            }

            val ptScore = matchedLog?.score?.toFloat()
                ?: (currentScore + ((sinOffset(i) * 0.12f))).coerceIn(0.2f, 0.98f)

            val ptMood = matchedLog?.mood ?: when {
                ptScore >= 0.85f -> "Happy"
                ptScore >= 0.70f -> "Content"
                ptScore >= 0.55f -> "Playful"
                ptScore >= 0.40f -> "Hungry"
                else -> "Anxious"
            }

            points.add(HourlyMoodPoint(hourLabel, ptScore, ptMood))
        }

        return points
    }

    private fun sinOffset(idx: Int): Float {
        val cycle = listOf(0.05f, -0.08f, 0.10f, -0.05f, 0.08f, -0.04f, 0.06f, 0.0f)
        return cycle.getOrElse(idx) { 0f }
    }

    private fun generateDefaultTrend(baseScore: Float): List<HourlyMoodPoint> {
        val intervals = listOf("-21h", "-18h", "-15h", "-12h", "-9h", "-6h", "-3h", "Now")
        val variance = listOf(-0.05f, 0.08f, -0.03f, -0.10f, 0.06f, 0.09f, -0.02f, 0.0f)
        return intervals.mapIndexed { idx, label ->
            val sc = (baseScore + variance[idx]).coerceIn(0.3f, 0.95f)
            val mood = if (sc > 0.8f) "Happy" else if (sc > 0.65f) "Content" else "Playful"
            HourlyMoodPoint(label, sc, mood)
        }
    }
}
