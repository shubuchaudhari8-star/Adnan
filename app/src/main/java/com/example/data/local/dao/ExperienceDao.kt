package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.ExperienceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExperienceDao {
    @Query("SELECT * FROM experiences ORDER BY timestamp DESC")
    fun getAllExperiences(): Flow<List<ExperienceEntity>>

    @Query("SELECT * FROM experiences WHERE primaryEmotion = :emotion ORDER BY timestamp DESC")
    fun getExperiencesByEmotion(emotion: String): Flow<List<ExperienceEntity>>

    @Query("SELECT * FROM experiences")
    suspend fun getAllExperiencesSync(): List<ExperienceEntity>

    @Query("SELECT COUNT(*) FROM experiences")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExperience(experience: ExperienceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(experiences: List<ExperienceEntity>)

    @Query("DELETE FROM experiences WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM experiences WHERE isUserStory = 1")
    suspend fun clearUserStories()
}
