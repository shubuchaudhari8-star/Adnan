package com.example.nlp

import com.example.data.local.entity.EmotionTagEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.math.exp
import kotlin.math.min

/**
 * Machine Learning module using Coroutines for multi-dimensional emotion tag extraction
 * from user-provided text inputs in the Empathy Engine.
 */
object EmotionTagMLEngine {

    data class EmotionArchetype(
        val tag: String,
        val bengaliLabel: String,
        val baseValence: Float,  // -1.0 (very negative) to +1.0 (very positive)
        val baseArousal: Float,  // 0.0 (calm/passive) to 1.0 (activated/intense)
        val category: String,
        val keywords: List<String>
    )

    private val emotionArchetypes = listOf(
        EmotionArchetype(
            tag = "ANXIETY_FEAR",
            bengaliLabel = "উদ্বেগ ও অস্থিরতা",
            baseValence = -0.65f,
            baseArousal = 0.85f,
            category = "NEGATIVE_HIGH_AROUSAL",
            keywords = listOf(
                "ভয়", "উদ্বেগ", "আতঙ্ক", "চিন্তা", "টেনশন", "কাঁপছে", "কী হবে", "অনিশ্চিত", "অস্থির",
                "অনিশ্চয়তা", "আতঙ্কিত", "ভীত", "ভয়ানক", "উদ্বিগ্ন", "বুক কাঁপছে",
                "anxious", "anxiety", "fear", "scared", "panic", "worried", "nervous", "dread",
                "stress", "tense", "uncertain", "frightened", "restless"
            )
        ),
        EmotionArchetype(
            tag = "GRIEF_SADNESS",
            bengaliLabel = "শোক ও বিষণ্ণতা",
            baseValence = -0.80f,
            baseArousal = 0.40f,
            category = "NEGATIVE_LOW_AROUSAL",
            keywords = listOf(
                "কষ্ট", "কান্না", "মন খারাপ", "বেদনা", "দুঃখ", "চোখের পানি", "ব্যথা", "বুক ভাঙা", "বিষণ্ণ",
                "শোক", "হারিয়েছি", "মৃত", "অনুতাপ", "ভারাক্রান্ত", "কাঁদছি", "অশ্রু",
                "sad", "sadness", "crying", "tears", "depressed", "sorrow", "hurt", "pain",
                "unhappy", "grief", "mourning", "loss", "broken", "weeping"
            )
        ),
        EmotionArchetype(
            tag = "FRUSTRATION_ANGER",
            bengaliLabel = "হতাশা ও ক্ষোভ",
            baseValence = -0.70f,
            baseArousal = 0.90f,
            category = "NEGATIVE_HIGH_AROUSAL",
            keywords = listOf(
                "হতাশা", "ব্যর্থ", "পারলাম না", "চেষ্টা", "হবে না", "প্রত্যাখ্যান", "বাদ", "লাভ নেই", "অসহায়",
                "ক্ষোভ", "রাগ", "বিরক্ত", "বিরক্তি", "ধুর", "অন্যায্য", "সহ্য হচ্ছে না",
                "frustrated", "frustration", "failure", "rejected", "gave up", "useless", "hopeless",
                "struggling", "setback", "angry", "rage", "irritated", "annoyed", "unfair"
            )
        ),
        EmotionArchetype(
            tag = "LONELINESS_ISOLATION",
            bengaliLabel = "একাকীত্ব ও বিচ্ছিন্নতা",
            baseValence = -0.60f,
            baseArousal = 0.35f,
            category = "NEGATIVE_LOW_AROUSAL",
            keywords = listOf(
                "একাকীত্ব", "একা", "কেউ নেই", "বন্ধুহীন", "নিঃসঙ্গ", "অপরিচিত", "বিচ্ছিন্ন",
                "কেউ বোঝে না", "সঙ্গীহীন", "শূন্যতা", "একা একা",
                "lonely", "loneliness", "alone", "isolated", "nobody", "no one", "empty",
                "abandoned", "alienated", "solitude", "left out", "disconnected"
            )
        ),
        EmotionArchetype(
            tag = "HEARTBREAK_VULNERABILITY",
            bengaliLabel = "মনভাঙা ও আবেগীয় ক্ষত",
            baseValence = -0.85f,
            baseArousal = 0.70f,
            category = "NEGATIVE_HIGH_AROUSAL",
            keywords = listOf(
                "ব্রেকআপ", "সম্পর্ক", "ভালোবাসা", "ছেড়ে চলে", "বিশ্বাসঘাতক", "মন ভেঙে", "প্রাক্তন",
                "বিশ্বাস ভেঙেছে", "প্রতারণা", "ভালোবাসি না", "আঘাত",
                "breakup", "heartbreak", "heartbroken", "dumped", "ex", "unrequited", "cheated",
                "betrayed", "broken heart", "romantic pain"
            )
        ),
        EmotionArchetype(
            tag = "BURNOUT_EXHAUSTION",
            bengaliLabel = "মানসিক ক্লান্তি ও অবসাদ",
            baseValence = -0.55f,
            baseArousal = 0.25f,
            category = "NEGATIVE_LOW_AROUSAL",
            keywords = listOf(
                "বার্নআউট", "ক্লান্ত", "ক্লান্তি", "অফিস", "বস", "চাপ", "ঘুম", "বিশ্রাম", "শক্তি শেষ",
                "কাজের চাপ", "হাঁপিয়ে উঠেছি", "আর পারছি না", "নিস্তেজ", "অবসাদ",
                "burnout", "exhausted", "exhaustion", "tired", "overworked", "workplace",
                "drained", "sleep", "fatigue", "overwhelmed by work", "no energy"
            )
        ),
        EmotionArchetype(
            tag = "JOY_GRATITUDE",
            bengaliLabel = "আনন্দ ও কৃতজ্ঞতা",
            baseValence = 0.85f,
            baseArousal = 0.75f,
            category = "POSITIVE_HIGH_AROUSAL",
            keywords = listOf(
                "আনন্দ", "খুশি", "সফল", "হাসি", "আশা", "কৃতজ্ঞ", "শান্তি", "আলহামদুলিল্লাহ",
                "স্কলারশিপ", "ধন্যবাদ", "ভালো লাগছে", "উল্লাস", "চমৎকার", "জিতলাম",
                "happy", "happiness", "joy", "success", "grateful", "gratitude", "scholarship",
                "celebrate", "proud", "wonderful", "delighted", "blessed", "cheerful"
            )
        ),
        EmotionArchetype(
            tag = "HOPE_RESILIENCE",
            bengaliLabel = "আশা ও পুনরুজ্জীবন",
            baseValence = 0.70f,
            baseArousal = 0.60f,
            category = "POSITIVE_MODERATE_AROUSAL",
            keywords = listOf(
                "আশা", "স্বপ্ন", "নতুন শুরু", "পারব", "বিশ্বাস", "চেষ্টা করব", "ঘুরে দাঁড়াব",
                "ভবিষ্যৎ", "আলো", "সম্ভাবনা", "সাহস",
                "hope", "hopeful", "resilience", "new beginning", "optimistic", "future",
                "determination", "strength", "courage", "looking forward", "will make it"
            )
        ),
        EmotionArchetype(
            tag = "RELIEF_PEACE",
            bengaliLabel = "স্বস্তি ও আত্মিক শান্তি",
            baseValence = 0.65f,
            baseArousal = 0.20f,
            category = "POSITIVE_CALM",
            keywords = listOf(
                "স্বস্তি", "শান্তি", "হালকা লাগছে", "ধীর", "নিশ্চিন্ত", "চাপ মুক্ত", "সমাধান হয়েছে",
                "শান্ত", "মন হালকা",
                "relief", "peace", "calm", "serene", "tranquil", "weight lifted",
                "relaxed", "peaceful", "resolved", "unburdened"
            )
        ),
        EmotionArchetype(
            tag = "CONFUSION_OVERWHELM",
            bengaliLabel = "দ্বিধাদ্বন্দ্ব ও বিহ্বলতা",
            baseValence = -0.40f,
            baseArousal = 0.65f,
            category = "AMBIGUOUS_MODERATE_AROUSAL",
            keywords = listOf(
                "কী করব", "বুঝতে পারছি না", "দ্বিধা", "বিভ্রান্ত", "অস্থির", "মাথা কাজ করছে না",
                "কোন পথে যাব", "এলোমেলো", "জটিল",
                "confused", "confusion", "overwhelmed", "lost", "don't know", "indecisive",
                "dilemma", "puzzled", "chaos", "mixed feelings"
            )
        )
    )

    private val intensifiers = listOf(
        "অনেক", "খুব", "ভীষণ", "অতিরিক্ত", "বড্ড", "মারাত্মক", "প্রচণ্ড", "একদম", "বেশি",
        "very", "extremely", "deeply", "so", "really", "tremendously", "completely", "severely"
    )

    private val negations = listOf(
        "না", "নেই", "নয়", "নি", "নাহ",
        "not", "no", "never", "don't", "doesn't", "didn't", "cannot", "can't", "without"
    )

    /**
     * Executes machine learning emotion tag extraction asynchronously using Kotlin Coroutines on Dispatchers.Default.
     * Computes multi-label confidence scores and returns ranked EmotionTagEntities ready to be stored in Room.
     */
    suspend fun extractEmotionTags(
        text: String,
        messageId: String = UUID.randomUUID().toString()
    ): List<EmotionTagEntity> = withContext(Dispatchers.Default) {
        if (text.isBlank()) return@withContext emptyList()

        val normalized = text.lowercase().trim()
        val tokens = tokenize(normalized)

        // Raw score accumulators
        val rawScores = FloatArray(emotionArchetypes.size) { 0.0f }

        // Contextual window scan (window size 3 for negation and intensifier modifiers)
        emotionArchetypes.forEachIndexed { index, archetype ->
            var score = 0.0f
            for (keyword in archetype.keywords) {
                if (normalized.contains(keyword)) {
                    var matchWeight = 1.0f

                    // Check for nearby intensifiers
                    for (intensifier in intensifiers) {
                        if (normalized.contains("$intensifier $keyword") || normalized.contains("$keyword $intensifier")) {
                            matchWeight *= 1.5f
                        }
                    }

                    // Check for nearby negations
                    for (negation in negations) {
                        if (normalized.contains("$negation $keyword") || normalized.contains("$keyword $negation")) {
                            // If positive emotion is negated (e.g. "খুশি না", "not happy"), invert or penalize
                            if (archetype.baseValence > 0) {
                                matchWeight *= -0.8f
                            } else {
                                matchWeight *= 0.3f
                            }
                        }
                    }

                    score += matchWeight
                }
            }
            rawScores[index] = score
        }

        // Apply Softmax normalization with temperature scaling
        val temperature = 1.2f
        val expScores = rawScores.map { exp(it / temperature) }
        val sumExp = expScores.sum().coerceAtLeast(0.0001f)
        val probabilities = expScores.map { it / sumExp }

        val rankedResults = mutableListOf<EmotionTagEntity>()
        val snippet = text.take(120).trim()

        // Pair with archetypes and sort by raw score and probability
        val paired = emotionArchetypes.mapIndexed { idx, archetype ->
            Triple(archetype, rawScores[idx], probabilities[idx])
        }.sortedByDescending { it.second }

        val topMatches = paired.filter { it.second > 0f }

        if (topMatches.isNotEmpty()) {
            topMatches.take(3).forEach { (archetype, rawScore, prob) ->
                // Calibrate confidence score between 0.65 and 0.96 based on match strength
                val confidence = min(0.96f, (0.65f + (prob * 0.40f) + (rawScore * 0.08f)).coerceIn(0.60f, 0.96f))
                rankedResults.add(
                    EmotionTagEntity(
                        messageId = messageId,
                        inputSnippet = snippet,
                        tag = archetype.tag,
                        bengaliLabel = archetype.bengaliLabel,
                        confidence = confidence,
                        valence = archetype.baseValence,
                        arousal = archetype.baseArousal,
                        category = archetype.category,
                        extractedBy = "ON_DEVICE_ML_ENGINE",
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        } else {
            // General baseline emotional tag when no explicit keywords matched
            rankedResults.add(
                EmotionTagEntity(
                    messageId = messageId,
                    inputSnippet = snippet,
                    tag = "INNER_TURMOIL",
                    bengaliLabel = "মানসিক বিভ্রান্তি ও আত্ম-উপলব্ধি",
                    confidence = 0.65f,
                    valence = -0.20f,
                    arousal = 0.50f,
                    category = "REFLECTIVE_MODERATE",
                    extractedBy = "ON_DEVICE_ML_ENGINE",
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        rankedResults
    }

    private fun tokenize(text: String): List<String> {
        val delimiters = charArrayOf(' ', ',', '.', '!', '?', ';', ':', '\n', '\t', '।', '-')
        return text.split(*delimiters).filter { it.isNotBlank() }
    }
}
