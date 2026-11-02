package com.example.data.remote

import android.content.Context
import com.example.R
import com.example.data.model.CatProfile
import com.example.data.model.MoodLog
import com.example.data.model.Vocalization
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreService(private val context: Context) {
    private val auth get() = Firebase.auth
    private val db: FirebaseFirestore by lazy {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
    }

    val currentUserId: String?
        get() = auth.currentUser?.uid

    val isUserSignedIn: Boolean
        get() = auth.currentUser != null

    val userEmail: String?
        get() = auth.currentUser?.email

    suspend fun syncCat(cat: CatProfile): Boolean {
        val uid = currentUserId ?: return false
        val docRef = db.collection("users").document(uid).collection("cats").document(cat.id)
        return try {
            val payload = hashMapOf<String, Any?>(
                "id" to cat.id,
                "userId" to uid,
                "name" to cat.name,
                "ageYears" to cat.ageYears,
                "breed" to cat.breed,
                "avatarColor" to cat.avatarColor,
                "vocalQuirks" to cat.vocalQuirks,
                "baselinePitchHz" to cat.baselinePitchHz,
                "favoriteTriggers" to cat.favoriteTriggers,
                "calibrationOffset" to cat.calibrationOffset,
                "createdAt" to (cat.createdAt ?: FieldValue.serverTimestamp()),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            true
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            false
        }
    }

    suspend fun syncVocalization(voc: Vocalization): Boolean {
        val uid = currentUserId ?: return false
        val docRef = db.collection("users").document(uid).collection("vocalizations").document(voc.id)
        return try {
            val payload = hashMapOf<String, Any?>(
                "id" to voc.id,
                "userId" to uid,
                "catId" to voc.catId,
                "catName" to voc.catName,
                "pitchHz" to voc.pitchHz,
                "durationMs" to voc.durationMs,
                "volumeDb" to voc.volumeDb,
                "vocalizationType" to voc.vocalizationType,
                "earState" to voc.earState,
                "tailState" to voc.tailState,
                "eyeState" to voc.eyeState,
                "mouthState" to voc.mouthState,
                "environmentalTrigger" to voc.environmentalTrigger,
                "translation" to voc.translation,
                "emotionalState" to voc.emotionalState,
                "confidenceScore" to voc.confidenceScore,
                "userFeedbackTag" to voc.userFeedbackTag,
                "manualEmotionTag" to voc.manualEmotionTag,
                "manualContextTag" to voc.manualContextTag,
                "createdAt" to (voc.createdAt ?: FieldValue.serverTimestamp())
            )
            docRef.set(payload).await()
            true
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            false
        }
    }

    suspend fun syncMoodLog(log: MoodLog): Boolean {
        val uid = currentUserId ?: return false
        val docRef = db.collection("users").document(uid).collection("mood_logs").document(log.id)
        return try {
            val payload = hashMapOf<String, Any?>(
                "id" to log.id,
                "userId" to uid,
                "catId" to log.catId,
                "mood" to log.mood,
                "score" to log.score,
                "vocalizationCount" to log.vocalizationCount,
                "createdAt" to (log.createdAt ?: FieldValue.serverTimestamp())
            )
            docRef.set(payload).await()
            true
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            false
        }
    }

    suspend fun fetchRemoteCats(): List<CatProfile> {
        val uid = currentUserId ?: return emptyList()
        val path = "users/$uid/cats"
        return try {
            val snapshot = db.collection("users").document(uid).collection("cats").get().await()
            snapshot.toObjects(CatProfile::class.java)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            emptyList()
        }
    }

    suspend fun fetchRemoteVocalizations(): List<Vocalization> {
        val uid = currentUserId ?: return emptyList()
        val path = "users/$uid/vocalizations"
        return try {
            val snapshot = db.collection("users").document(uid).collection("vocalizations").get().await()
            snapshot.toObjects(Vocalization::class.java)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            emptyList()
        }
    }
}
