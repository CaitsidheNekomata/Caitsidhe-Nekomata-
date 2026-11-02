package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
@Entity(tableName = "mood_logs")
data class MoodLog(
    @PrimaryKey
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val catId: String = "",
    val mood: String = "Content",
    val score: Double = 0.8, // 0.0 (very distressed) to 1.0 (blissful)
    val vocalizationCount: Long = 1L,
    @ServerTimestamp
    val createdAt: Timestamp? = null
)
