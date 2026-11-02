package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CatProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface CatDao {
    @Query("SELECT * FROM cat_profiles ORDER BY name ASC")
    fun getAllCats(): Flow<List<CatProfile>>

    @Query("SELECT * FROM cat_profiles WHERE id = :id LIMIT 1")
    suspend fun getCatById(id: String): CatProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCat(cat: CatProfile)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCats(cats: List<CatProfile>)

    @Update
    suspend fun updateCat(cat: CatProfile)

    @Query("DELETE FROM cat_profiles WHERE id = :id")
    suspend fun deleteCatById(id: String)
}
