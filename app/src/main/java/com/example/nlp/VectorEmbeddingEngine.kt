package com.example.nlp

import com.example.data.local.entity.ExperienceEntity
import com.example.data.model.EmotionAnalysisResult
import com.example.data.model.VectorMatch
import kotlin.math.sqrt

object VectorEmbeddingEngine {

    fun generateEmbedding(text: String, emotion: EmotionAnalysisResult): List<Float> {
        val lower = text.lowercase()
        val vector = FloatArray(16) { 0f }

        // Dim 0: Valence
        vector[0] = when (emotion.valence) {
            "Positive" -> 0.85f
            "Mixed" -> 0.1f
            else -> -0.85f
        }

        // Dim 1: Intensity / Arousal
        vector[1] = emotion.intensity

        // Dim 2: Career / Workplace
        if (lower.contains("কাজ") || lower.contains("চাকরি") || lower.contains("অফিস") ||
            lower.contains("ইন্টারভিউ") || lower.contains("career") || lower.contains("job") ||
            lower.contains("work") || lower.contains("startup") || lower.contains("বস")
        ) {
            vector[2] = 0.95f
        }

        // Dim 3: Relationship / Heartbreak
        if (lower.contains("ব্রেকআপ") || lower.contains("সম্পর্ক") || lower.contains("ভালোবাসা") ||
            lower.contains("বন্ধু") || lower.contains("প্রেম") || lower.contains("breakup") ||
            lower.contains("relationship") || lower.contains("love")
        ) {
            vector[3] = 0.95f
        }

        // Dim 4: Loneliness / Social Isolation
        if (lower.contains("একা") || lower.contains("একাকীত্ব") || lower.contains("কেউ নেই") ||
            lower.contains("lonely") || lower.contains("alone") || lower.contains("isolated")
        ) {
            vector[4] = 0.95f
        }

        // Dim 5: Grief / Permanent Loss
        if (lower.contains("বাবা") || lower.contains("মা") || lower.contains("মৃত্যু") ||
            lower.contains("হারিয়ে") || lower.contains("grief") || lower.contains("passed away")
        ) {
            vector[5] = 0.9f
        }

        // Dim 6: Anxiety / Fear of Future
        if (lower.contains("ভয়") || lower.contains("উদ্বেগ") || lower.contains("আতঙ্ক") ||
            lower.contains("কাঁপছে") || lower.contains("anxiety") || lower.contains("fear") ||
            lower.contains("panic")
        ) {
            vector[6] = 0.95f
        }

        // Dim 7: Anger / Betrayal
        if (lower.contains("রাগ") || lower.contains("ক্ষোভ") || lower.contains("বিশ্বাসঘাতক") ||
            lower.contains("angry") || lower.contains("betrayed")
        ) {
            vector[7] = 0.85f
        }

        // Dim 8: Hope / Resilience
        if (lower.contains("আশা") || lower.contains("বিশ্বাস") || lower.contains("চেষ্টা করব") ||
            lower.contains("hope") || lower.contains("recover") || emotion.valence == "Positive"
        ) {
            vector[8] = 0.8f
        } else {
            vector[8] = 0.2f
        }

        // Dim 9: Guilt / Self-blame
        if (lower.contains("দোষ") || lower.contains("আমার ভুল") || lower.contains("guilt") ||
            lower.contains("my fault")
        ) {
            vector[9] = 0.85f
        }

        // Dim 10: Burnout / Physical Draining
        if (lower.contains("ক্লান্ত") || lower.contains("বার্নআউট") || lower.contains("বিছানা") ||
            lower.contains("শরীর") || lower.contains("exhausted") || lower.contains("burnout")
        ) {
            vector[10] = 0.95f
        }

        // Dim 11: Academic Pressure
        if (lower.contains("পরীক্ষা") || lower.contains("স্কলারশিপ") || lower.contains("মাস্টার্স") ||
            lower.contains("পড়াশোনা") || lower.contains("exam") || lower.contains("study")
        ) {
            vector[11] = 0.9f
        }

        // Dim 12: Family Strain
        if (lower.contains("পরিবার") || lower.contains("বাবা-মা") || lower.contains("সংসার") ||
            lower.contains("family")
        ) {
            vector[12] = 0.85f
        }

        // Dim 13: Financial Burden
        if (lower.contains("টাকা") || lower.contains("লোন") || lower.contains("অর্থ") ||
            lower.contains("financial") || lower.contains("savings") || lower.contains("money")
        ) {
            vector[13] = 0.9f
        }

        // Dim 14: Nostalgia / Longing
        if (lower.contains("স্মৃতি") || lower.contains("অতীত") || lower.contains("আগের দিন") ||
            lower.contains("miss") || lower.contains("nostalgia")
        ) {
            vector[14] = 0.8f
        }

        // Dim 15: Acceptance
        vector[15] = if (emotion.intensity < 0.7f) 0.5f else 0.15f

        // Normalize vector to unit length
        var sumSquares = 0f
        for (v in vector) sumSquares += v * v
        val magnitude = if (sumSquares > 0f) sqrt(sumSquares) else 1f

        return vector.map { it / magnitude }
    }

    fun parseVector(vectorStr: String): List<Float> {
        return try {
            vectorStr.split(",").map { it.trim().toFloat() }
        } catch (e: Exception) {
            List(16) { 0f }
        }
    }

    fun cosineSimilarity(vecA: List<Float>, vecB: List<Float>): Float {
        if (vecA.isEmpty() || vecB.isEmpty() || vecA.size != vecB.size) return 0f
        var dot = 0f
        var normA = 0f
        var normB = 0f
        for (i in vecA.indices) {
            val a = vecA[i]
            val b = vecB[i]
            dot += a * b
            normA += a * a
            normB += b * b
        }
        val denom = sqrt(normA) * sqrt(normB)
        if (denom == 0f) return 0f
        val cos = dot / denom
        // Scale cosine similarity (-1 to 1) into (0.0 to 1.0) with slight boost for positive correlation
        return ((cos + 1f) / 2f).coerceIn(0f, 1f)
    }

    fun findTopMatches(
        queryVector: List<Float>,
        storedExperiences: List<ExperienceEntity>,
        topK: Int = 3
    ): List<VectorMatch> {
        if (storedExperiences.isEmpty()) return emptyList()

        return storedExperiences.map { exp ->
            val expVec = parseVector(exp.embeddingVector)
            val score = cosineSimilarity(queryVector, expVec)
            VectorMatch(
                id = exp.id,
                sanitizedStory = exp.sanitizedText,
                primaryEmotion = exp.primaryEmotion,
                coreTopic = exp.coreTopic,
                similarityScore = score,
                responseWisdom = exp.empathyWisdom
            )
        }
            .sortedByDescending { it.similarityScore }
            .take(topK)
    }
}
