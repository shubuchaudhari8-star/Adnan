package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "experiences")
data class ExperienceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val originalText: String,
    val sanitizedText: String,
    val primaryEmotion: String,
    val secondaryEmotion: String,
    val intensity: Float,
    val valence: String,
    val coreTopic: String,
    val keywords: String, // comma separated
    val embeddingVector: String, // comma separated float values
    val empathyWisdom: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isUserStory: Boolean = false
)
