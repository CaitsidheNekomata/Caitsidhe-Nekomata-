package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
@Entity(tableName = "vocalizations")
data class Vocalization(
    @PrimaryKey
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val catId: String = "",
    val catName: String = "",
    val pitchHz: Double = 500.0,
    val durationMs: Long = 750L,
    val volumeDb: Double = -14.0,
    val vocalizationType: String = "standard_meow", // chirp_trill, purr, demanding_yowl, hiss, chatter, soft_mew
    val earState: String = "alert_forward", // airplane_flat, swiveling, relaxed
    val tailState: String = "upright_high", // question_mark, twitching_low, puffed, tucked
    val eyeState: String = "slow_blink", // wide_dilated, slitted, normal_focused
    val mouthState: String = "open_meow", // half_open, closed
    val environmentalTrigger: String = "food_bowl", // window_birds, closed_door, play_toy, stranger, rest_couch
    val translation: String = "",
    val emotionalState: String = "Content", // Happy, Hungry, Playful, Anxious, Bored, Annoyed, Affectionate
    val confidenceScore: Double = 0.88,
    val userFeedbackTag: String = "unrated", // accurate, close, inaccurate
    val manualEmotionTag: String = "none", // hungry, playful, annoyed, greeting
    val manualContextTag: String = "none", // at food bowl, at door, seeing another cat
    @ServerTimestamp
    val createdAt: Timestamp? = null
)
