package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.EmotionTagEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Room database operations on extracted emotion tags.
 */
@Dao
interface EmotionTagDao {

    @Query("SELECT * FROM emotion_tags ORDER BY timestamp DESC")
    fun getAllEmotionTags(): Flow<List<EmotionTagEntity>>

    @Query("SELECT * FROM emotion_tags WHERE messageId = :messageId ORDER BY confidence DESC")
    fun getTagsForMessage(messageId: String): Flow<List<EmotionTagEntity>>

    @Query("SELECT * FROM emotion_tags ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentEmotionTags(limit: Int = 50): Flow<List<EmotionTagEntity>>

    @Query("SELECT * FROM emotion_tags WHERE tag = :tag ORDER BY timestamp DESC")
    fun getTagsByType(tag: String): Flow<List<EmotionTagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: EmotionTagEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(tags: List<EmotionTagEntity>)

    @Query("DELETE FROM emotion_tags WHERE id = :id")
    suspend fun deleteTagById(id: Long)

    @Query("DELETE FROM emotion_tags WHERE messageId = :messageId")
    suspend fun deleteTagsByMessageId(messageId: String)

    @Query("DELETE FROM emotion_tags")
    suspend fun clearAllTags()

    @Query("SELECT COUNT(*) FROM emotion_tags")
    suspend fun getCount(): Int
}
