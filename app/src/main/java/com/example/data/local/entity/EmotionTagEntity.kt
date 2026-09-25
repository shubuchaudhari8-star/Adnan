package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing an emotion tag extracted by the machine learning module
 * from user-provided text inputs for the empathy engine.
 */
@Entity(tableName = "emotion_tags")
data class EmotionTagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val messageId: String = "",
    val inputSnippet: String,
    val tag: String,
    val bengaliLabel: String,
    val confidence: Float,
    val valence: Float,
    val arousal: Float,
    val category: String,
    val extractedBy: String = "ON_DEVICE_ML_ENGINE",
    val timestamp: Long = System.currentTimeMillis()
)
