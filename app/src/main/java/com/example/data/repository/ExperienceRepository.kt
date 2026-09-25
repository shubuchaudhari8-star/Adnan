package com.example.data.repository

import com.example.data.local.PreseededData
import com.example.data.local.dao.ExperienceDao
import com.example.data.local.entity.ExperienceEntity
import kotlinx.coroutines.flow.Flow

class ExperienceRepository(private val dao: ExperienceDao) {

    val allExperiences: Flow<List<ExperienceEntity>> = dao.getAllExperiences()

    fun getExperiencesByEmotion(emotion: String): Flow<List<ExperienceEntity>> {
        return if (emotion.isBlank() || emotion == "All") {
            dao.getAllExperiences()
        } else {
            dao.getExperiencesByEmotion(emotion)
        }
    }

    suspend fun insertExperience(experience: ExperienceEntity): Long {
        return dao.insertExperience(experience)
    }

    suspend fun getAllExperiencesSync(): List<ExperienceEntity> {
        val list = dao.getAllExperiencesSync()
        if (list.isEmpty()) {
            val seeds = PreseededData.getSeedExperiences()
            dao.insertAll(seeds)
            return seeds
        }
        return list
    }

    suspend fun deleteExperience(id: Long) {
        dao.deleteById(id)
    }

    suspend fun clearUserStories() {
        dao.clearUserStories()
    }
}
