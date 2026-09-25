package com.example.data.model

import com.example.data.local.entity.EmotionTagEntity

data class EmotionAnalysisResult(
    val primaryEmotion: String,
    val secondaryEmotion: String,
    val intensity: Float, // 0.0 to 1.0
    val valence: String, // Positive, Negative, Neutral, Mixed
    val coreTopic: String,
    val keywords: List<String>,
    val visualCues: String? = null // Cues from photo/video scene
)

data class RedactedEntity(
    val type: String, // Name, Location, Phone, Email, ID
    val originalSnippet: String,
    val replacement: String
)

data class AnonymizationResult(
    val originalText: String,
    val sanitizedText: String,
    val redactedEntities: List<RedactedEntity>,
    val privacyScore: Int // 0 to 100
)

data class VectorMatch(
    val id: Long,
    val sanitizedStory: String,
    val primaryEmotion: String,
    val coreTopic: String,
    val similarityScore: Float, // 0.0 to 1.0 (Cosine Similarity)
    val responseWisdom: String
)

data class EmpathyPipelineResult(
    val inputId: Long = System.currentTimeMillis(),
    val originalInput: String,
    val anonymization: AnonymizationResult,
    val emotionAnalysis: EmotionAnalysisResult,
    val matchedStories: List<VectorMatch>,
    val empathyResponse: String,
    val isAiGenerated: Boolean,
    val attachedMedia: AttachedMedia? = null,
    val extractedEmotionTags: List<EmotionTagEntity> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

data class SystemPromptTemplate(
    val title: String,
    val moduleTag: String,
    val icon: String,
    val description: String,
    val systemPrompt: String,
    val exampleInput: String,
    val jsonOutputSchema: String
)

enum class MediaType {
    IMAGE,
    VIDEO,
    AUDIO
}

data class AttachedMedia(
    val uri: String,
    val mediaType: MediaType,
    val mimeType: String = "image/jpeg",
    val base64Data: String? = null,
    val displayName: String = "Media Attachment"
)

enum class MessageSender {
    USER,
    COMPANION
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val attachedMedia: AttachedMedia? = null,
    val emotionResult: EmotionAnalysisResult? = null,
    val matchedStories: List<VectorMatch> = emptyList(),
    val isPrivacySanitized: Boolean = false,
    val sanitizedText: String? = null,
    val isAiGenerated: Boolean = false,
    val extractedEmotionTags: List<EmotionTagEntity> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatSession(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val messages: List<ChatMessage> = emptyList(),
    val hasUnreadOrActiveDot: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class NotebookEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

