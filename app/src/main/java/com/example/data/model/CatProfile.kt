package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
@Entity(tableName = "cat_profiles")
data class CatProfile(
    @PrimaryKey
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val ageYears: Double = 2.0,
    val breed: String = "Domestic Shorthair",
    val avatarColor: String = "#FF8A50",
    val vocalQuirks: String = "Loves high-pitched greeting chirps; raspy demands near dinnertime.",
    val baselinePitchHz: Double = 520.0,
    val favoriteTriggers: List<String> = listOf("Wet Food", "Window Birds", "Laser Pointer"),
    val calibrationOffset: Double = 0.0,
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
)
