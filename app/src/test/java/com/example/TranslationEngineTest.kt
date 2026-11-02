package com.example

import com.example.audio.AudioAnalysisResult
import com.example.audio.TalkBackSynthesizer
import com.example.data.model.CatProfile
import com.example.data.model.MoodLog
import com.example.data.model.Vocalization
import com.example.domain.MoodMeterEngine
import com.example.domain.PostureData
import com.example.domain.TranslationEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TranslationEngineTest {

    private val engine = TranslationEngine()
    private val moodEngine = MoodMeterEngine()
    private val talkBack = TalkBackSynthesizer()

    private val testCat = CatProfile(
        id = "cat_1",
        name = "Luna",
        baselinePitchHz = 540.0,
        calibrationOffset = 10.0
    )

    @Test
    fun testHungryMeowAtFoodBowl() {
        val audio = AudioAnalysisResult(
            pitchHz = 480.0,
            durationMs = 1400L,
            volumeDb = -10.0,
            vocalizationType = "demanding_yowl",
            confidence = 0.92
        )
        val posture = PostureData(environmentalTrigger = "food_bowl")
        val result = engine.translate(audio, posture, testCat)

        assertEquals("Hungry", result.emotionalState)
        assertTrue(result.translation.contains("food bowl", ignoreCase = true))
        assertTrue(result.confidence > 0.8)
    }

    @Test
    fun testAffectionatePurrSlowBlink() {
        val audio = AudioAnalysisResult(
            pitchHz = 40.0,
            durationMs = 2000L,
            volumeDb = -25.0,
            vocalizationType = "purr",
            confidence = 0.95
        )
        val posture = PostureData(eyeState = "slow_blink")
        val result = engine.translate(audio, posture, testCat)

        assertEquals("Affectionate", result.emotionalState)
        assertTrue(result.confidence >= 0.9)
    }

    @Test
    fun testAnxiousAirplaneEars() {
        val audio = AudioAnalysisResult(
            pitchHz = 820.0,
            durationMs = 600L,
            volumeDb = -8.0,
            vocalizationType = "standard_meow",
            confidence = 0.88
        )
        val posture = PostureData(earState = "airplane_flat")
        val result = engine.translate(audio, posture, testCat)

        assertEquals("Anxious", result.emotionalState)
        assertTrue(result.suggestedAction.contains("space", ignoreCase = true))
    }

    @Test
    fun testMoodMeter24HourOverview() {
        val vocs = listOf(
            Vocalization(emotionalState = "Happy"),
            Vocalization(emotionalState = "Happy"),
            Vocalization(emotionalState = "Playful")
        )
        val overview = moodEngine.computeMoodOverview(vocs, emptyList())
        assertEquals("Happy", overview.currentMood)
        assertTrue(overview.score >= 0.85)
        assertEquals(8, overview.hourlyTrend.size)
    }

    @Test
    fun testTalkBackPredefinedOptions() {
        val options = talkBack.predefinedResponses
        assertTrue(options.size >= 6)
        val helloOption = options.firstOrNull { it.id == "hello" }
        assertNotNull(helloOption)
        assertEquals("Hello there!", helloOption?.phrase)
    }

    @Test
    fun testDataExporterCsvGeneration() {
        val vocs = listOf(
            Vocalization(
                id = "voc_123",
                catId = "cat_1",
                catName = "Luna",
                pitchHz = 540.0,
                durationMs = 800L,
                translation = "Give me wet food, please!",
                emotionalState = "Hungry"
            )
        )
        val moods = listOf(
            MoodLog(
                id = "mood_456",
                catId = "cat_1",
                mood = "Happy",
                score = 0.90
            )
        )

        val csv = com.example.export.DataExporter.generateCatDataCsv(testCat, vocs, moods)

        assertTrue(csv.contains("MEOWLINGO - FELINE VOCAL & MOOD EXPORT"))
        assertTrue(csv.contains("Profile,Name,Luna"))
        assertTrue(csv.contains("MEOW VOCALIZATION & TRANSLATION HISTORY"))
        assertTrue(csv.contains("voc_123"))
        assertTrue(csv.contains("Give me wet food, please!"))
        assertTrue(csv.contains("24-HOUR & HISTORICAL MOOD LOGS"))
        assertTrue(csv.contains("mood_456"))

        val fileName = com.example.export.DataExporter.getDefaultFileName("Luna")
        assertTrue(fileName.startsWith("Luna_meow_history_"))
        assertTrue(fileName.endsWith(".csv"))
    }
}
