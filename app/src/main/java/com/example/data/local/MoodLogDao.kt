package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.MoodLog
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodLogDao {
    @Query("SELECT * FROM mood_logs ORDER BY id DESC")
    fun getAllMoodLogs(): Flow<List<MoodLog>>

    @Query("SELECT * FROM mood_logs WHERE catId = :catId ORDER BY id DESC LIMIT 50")
    fun getMoodLogsForCat(catId: String): Flow<List<MoodLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(moodLog: MoodLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(moodLogs: List<MoodLog>)
}
