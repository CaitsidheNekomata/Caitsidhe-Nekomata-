package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.model.CatProfile
import com.example.data.model.MoodLog
import com.example.data.model.Vocalization
import com.example.data.remote.FirestoreService
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class CatRepository(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val catDao = database.catDao()
    private val vocDao = database.vocalizationDao()
    private val moodDao = database.moodLogDao()
    private val firestoreService = FirestoreService(context)

    val cats: Flow<List<CatProfile>> = catDao.getAllCats()
    val vocalizations: Flow<List<Vocalization>> = vocDao.getAllVocalizations()
    val recentVocalizations: Flow<List<Vocalization>> = vocDao.getRecentVocalizations()

    fun getVocalizationsForCat(catId: String): Flow<List<Vocalization>> = vocDao.getVocalizationsByCat(catId)
    fun getMoodLogsForCat(catId: String): Flow<List<MoodLog>> = moodDao.getMoodLogsForCat(catId)

    suspend fun initializeDefaultDataIfEmpty() {
        // Pre-populate if empty
        val existing = catDao.getCatById("mochi_default")
        if (existing == null) {
            val mochi = CatProfile(
                id = "mochi_default",
                userId = firestoreService.currentUserId ?: "local_user",
                name = "Mochi",
                ageYears = 3.0,
                breed = "Scottish Fold",
                avatarColor = "#FF8A50",
                vocalQuirks = "High chirps for attention; soft purrs when sleeping on laps.",
                baselinePitchHz = 540.0,
                favoriteTriggers = listOf("Churu Treats", "Cardboard Box", "Sunbeam"),
                calibrationOffset = 0.0,
                createdAt = Timestamp.now()
            )
            val luna = CatProfile(
                id = "luna_default",
                userId = firestoreService.currentUserId ?: "local_user",
                name = "Luna",
                ageYears = 1.5,
                breed = "Russian Blue",
                avatarColor = "#4DD0E1",
                vocalQuirks = "Raspy meow when bowl is half-empty; long melodic greeting trills.",
                baselinePitchHz = 610.0,
                favoriteTriggers = listOf("Wet Food Pouch", "Laser Dot", "Bird TV"),
                calibrationOffset = 15.0,
                createdAt = Timestamp.now()
            )
            catDao.insertAllCats(listOf(mochi, luna))

            // Seed initial historical vocalizations for immediate richness
            val nowSeconds = System.currentTimeMillis() / 1000
            val starterVocs = listOf(
                Vocalization(
                    id = "voc_init_1",
                    userId = firestoreService.currentUserId ?: "local_user",
                    catId = "mochi_default",
                    catName = "Mochi",
                    pitchHz = 580.0,
                    durationMs = 620L,
                    volumeDb = -12.0,
                    vocalizationType = "chirp_trill",
                    earState = "alert_forward",
                    tailState = "question_mark",
                    eyeState = "slow_blink",
                    mouthState = "half_open",
                    environmentalTrigger = "rest_couch",
                    translation = "Hello human friend! I am very glad you're in the room.",
                    emotionalState = "Affectionate",
                    confidenceScore = 0.94,
                    userFeedbackTag = "accurate",
                    manualEmotionTag = "greeting",
                    manualContextTag = "on couch",
                    createdAt = Timestamp(nowSeconds - 3600, 0)
                ),
                Vocalization(
                    id = "voc_init_2",
                    userId = firestoreService.currentUserId ?: "local_user",
                    catId = "luna_default",
                    catName = "Luna",
                    pitchHz = 490.0,
                    durationMs = 1200L,
                    volumeDb = -8.0,
                    vocalizationType = "demanding_yowl",
                    earState = "swiveling",
                    tailState = "upright_high",
                    eyeState = "wide_dilated",
                    mouthState = "open_meow",
                    environmentalTrigger = "food_bowl",
                    translation = "My bowl has reached unacceptable levels of vacancy. Rectify now!",
                    emotionalState = "Hungry",
                    confidenceScore = 0.91,
                    userFeedbackTag = "accurate",
                    manualEmotionTag = "hungry",
                    manualContextTag = "at food bowl",
                    createdAt = Timestamp(nowSeconds - 7200, 0)
                ),
                Vocalization(
                    id = "voc_init_3",
                    userId = firestoreService.currentUserId ?: "local_user",
                    catId = "mochi_default",
                    catName = "Mochi",
                    pitchHz = 710.0,
                    durationMs = 380L,
                    volumeDb = -15.0,
                    vocalizationType = "chatter",
                    earState = "alert_forward",
                    tailState = "twitching_low",
                    eyeState = "wide_dilated",
                    mouthState = "half_open",
                    environmentalTrigger = "window_birds",
                    translation = "Fascinating feathered specimen detected out the window! Intensely curious.",
                    emotionalState = "Playful",
                    confidenceScore = 0.89,
                    userFeedbackTag = "accurate",
                    manualEmotionTag = "playful",
                    manualContextTag = "at window",
                    createdAt = Timestamp(nowSeconds - 14400, 0)
                )
            )
            vocDao.insertAll(starterVocs)

            // Seed initial 24h mood logs for trend demonstration
            val starterMoods = mutableListOf<MoodLog>()
            val moodsList = listOf("Happy", "Content", "Playful", "Hungry", "Affectionate", "Content", "Happy")
            val scoresList = listOf(0.85, 0.78, 0.90, 0.65, 0.92, 0.80, 0.88)
            for (i in 0..6) {
                starterMoods.add(
                    MoodLog(
                        id = "mood_init_$i",
                        userId = firestoreService.currentUserId ?: "local_user",
                        catId = "mochi_default",
                        mood = moodsList[i],
                        score = scoresList[i],
                        vocalizationCount = (i + 1).toLong(),
                        createdAt = Timestamp(nowSeconds - ((6 - i) * 3600 * 3), 0)
                    )
                )
            }
            moodDao.insertAll(starterMoods)
        }
    }

    suspend fun saveCat(cat: CatProfile) {
        val catToSave = if (cat.id.isBlank()) {
            cat.copy(
                id = UUID.randomUUID().toString(),
                userId = firestoreService.currentUserId ?: "local_user",
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
        } else {
            cat.copy(
                userId = firestoreService.currentUserId ?: "local_user",
                updatedAt = Timestamp.now()
            )
        }
        catDao.insertCat(catToSave)
        if (firestoreService.isUserSignedIn) {
            firestoreService.syncCat(catToSave)
        }
    }

    suspend fun deleteCat(catId: String) {
        catDao.deleteCatById(catId)
    }

    suspend fun recordVocalization(voc: Vocalization): Vocalization {
        val fullVoc = voc.copy(
            id = if (voc.id.isBlank()) UUID.randomUUID().toString() else voc.id,
            userId = firestoreService.currentUserId ?: "local_user",
            createdAt = voc.createdAt ?: Timestamp.now()
        )
        vocDao.insert(fullVoc)
        if (firestoreService.isUserSignedIn) {
            firestoreService.syncVocalization(fullVoc)
        }
        return fullVoc
    }

    suspend fun updateFeedback(id: String, feedback: String) {
        vocDao.updateFeedback(id, feedback)
    }

    suspend fun updateManualTags(id: String, emotion: String, context: String) {
        vocDao.updateManualTags(id, emotion, context)
    }

    suspend fun recordMoodLog(log: MoodLog) {
        val fullLog = log.copy(
            id = if (log.id.isBlank()) UUID.randomUUID().toString() else log.id,
            userId = firestoreService.currentUserId ?: "local_user",
            createdAt = log.createdAt ?: Timestamp.now()
        )
        moodDao.insert(fullLog)
        if (firestoreService.isUserSignedIn) {
            firestoreService.syncMoodLog(fullLog)
        }
    }

    suspend fun syncAllWithCloud(): String {
        if (!firestoreService.isUserSignedIn) {
            return "Please sign in with Google to enable cloud sync."
        }
        // Pull remote cats
        val remoteCats = firestoreService.fetchRemoteCats()
        if (remoteCats.isNotEmpty()) {
            catDao.insertAllCats(remoteCats)
        }
        val remoteVocs = firestoreService.fetchRemoteVocalizations()
        if (remoteVocs.isNotEmpty()) {
            vocDao.insertAll(remoteVocs)
        }
        return "Synchronized ${remoteCats.size} cats and ${remoteVocs.size} meows with cloud!"
    }
}
