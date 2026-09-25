package com.example.data.repository

import com.example.data.local.dao.EmotionTagDao
import com.example.data.local.entity.EmotionTagEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository abstracting data access for extracted emotion tags stored in Room.
 */
class EmotionTagRepository(private val dao: EmotionTagDao) {

    val allTags: Flow<List<EmotionTagEntity>> = dao.getAllEmotionTags()
    val recentTags: Flow<List<EmotionTagEntity>> = dao.getRecentEmotionTags(50)

    fun getTagsForMessage(messageId: String): Flow<List<EmotionTagEntity>> {
        return dao.getTagsForMessage(messageId)
    }

    fun getTagsByType(tag: String): Flow<List<EmotionTagEntity>> {
        return dao.getTagsByType(tag)
    }

    suspend fun insertTag(tag: EmotionTagEntity): Long {
        return dao.insertTag(tag)
    }

    suspend fun storeExtractedTags(tags: List<EmotionTagEntity>) {
        if (tags.isNotEmpty()) {
            dao.insertTags(tags)
        }
    }

    suspend fun deleteTag(id: Long) {
        dao.deleteTagById(id)
    }

    suspend fun deleteByMessageId(messageId: String) {
        dao.deleteTagsByMessageId(messageId)
    }

    suspend fun clearAllTags() {
        dao.clearAllTags()
    }

    suspend fun getCount(): Int {
        return dao.getCount()
    }
}
