package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Vocalization
import kotlinx.coroutines.flow.Flow

@Dao
interface VocalizationDao {
    @Query("SELECT * FROM vocalizations ORDER BY id DESC")
    fun getAllVocalizations(): Flow<List<Vocalization>>

    @Query("SELECT * FROM vocalizations WHERE catId = :catId ORDER BY id DESC")
    fun getVocalizationsByCat(catId: String): Flow<List<Vocalization>>

    @Query("SELECT * FROM vocalizations ORDER BY id DESC LIMIT 20")
    fun getRecentVocalizations(): Flow<List<Vocalization>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vocalization: Vocalization)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vocalizations: List<Vocalization>)

    @Update
    suspend fun update(vocalization: Vocalization)

    @Query("UPDATE vocalizations SET userFeedbackTag = :feedback WHERE id = :id")
    suspend fun updateFeedback(id: String, feedback: String)

    @Query("UPDATE vocalizations SET manualEmotionTag = :emotion, manualContextTag = :context WHERE id = :id")
    suspend fun updateManualTags(id: String, emotion: String, context: String)

    @Query("DELETE FROM vocalizations WHERE id = :id")
    suspend fun deleteById(id: String)
}
