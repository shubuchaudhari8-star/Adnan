package com.example.nlp

import com.example.data.model.EmotionAnalysisResult
import org.json.JSONObject

object EmotionExtractorEngine {

    // Bengali & English emotion keyword groups
    private val sadnessKeywords = listOf(
        "কষ্ট", "কান্না", "মন খারাপ", "বেদনা", "দুঃখ", "চোখের পানি", "ব্যথা", "বুক ভাঙা", "বিষণ্ণ",
        "sad", "crying", "tears", "depressed", "sorrow", "hurt", "pain", "unhappy", "grief"
    )

    private val frustrationKeywords = listOf(
        "হতাশা", "ব্যর্থ", "পারলাম না", "চেষ্টা", "হবে না", "প্রত্যাখ্যান", "বাদ", "লাভ নেই", "অসহায়",
        "frustrated", "failure", "rejected", "gave up", "useless", "hopeless", "struggling", "setback"
    )

    private val lonelinessKeywords = listOf(
        "একাকীত্ব", "একা", "কেউ নেই", "বন্ধুহীন", "নিঃসঙ্গ", "অপরিচিত", "বিচ্ছিন্ন",
        "lonely", "alone", "isolated", "nobody", "no one", "empty", "abandoned"
    )

    private val anxietyKeywords = listOf(
        "ভয়", "উদ্বেগ", "আতঙ্ক", "চিন্তা", "টেনশন", "কাঁপছে", "কী হবে", "অনিশ্চিত", "অস্থির",
        "anxious", "anxiety", "fear", "scared", "panic", "worried", "nervous", "dread"
    )

    private val heartbreakKeywords = listOf(
        "ব্রেকআপ", "সম্পর্ক", "ভালোবাসা", "ছেড়ে চলে", "বিশ্বাসঘাতক", "মন ভেঙে", "প্রাক্তন",
        "breakup", "heartbreak", "heartbroken", "dumped", "ex", "unrequited", "cheated"
    )

    private val burnoutKeywords = listOf(
        "বার্নআউট", "ক্লান্ত", "ক্লান্তি", "অফিস", "বস", "চাপ", "ঘুম", "বিশ্রাম", "শক্তি শেষ",
        "burnout", "exhausted", "tired", "overworked", "workplace", "drained", "sleep"
    )

    private val joyKeywords = listOf(
        "আনন্দ", "খুশি", "সফল", "হাসি", "আশা", "কৃতজ্ঞ", "শান্তি", "আলহামদুলিল্লাহ", "স্কলারশিপ",
        "happy", "joy", "success", "grateful", "scholarship", "peace", "hope", "proud"
    )

    fun extractEmotionLocally(text: String): EmotionAnalysisResult {
        val lower = text.lowercase()
        val detectedKeywords = mutableListOf<String>()

        fun countMatches(keywords: List<String>): Int {
            var count = 0
            for (k in keywords) {
                if (lower.contains(k)) {
                    count++
                    detectedKeywords.add(k)
                }
            }
            return count
        }

        val sadnessScore = countMatches(sadnessKeywords)
        val frustrationScore = countMatches(frustrationKeywords)
        val lonelinessScore = countMatches(lonelinessKeywords)
        val anxietyScore = countMatches(anxietyKeywords)
        val heartbreakScore = countMatches(heartbreakKeywords)
        val burnoutScore = countMatches(burnoutKeywords)
        val joyScore = countMatches(joyKeywords)

        val scores = listOf(
            "হতাশা (Frustration)" to frustrationScore,
            "মনভাঙা (Heartbreak)" to heartbreakScore,
            "একাকীত্ব (Loneliness)" to lonelinessScore,
            "উদ্বেগ ও আতঙ্ক (Anxiety & Fear)" to anxietyScore,
            "মানসিক ক্লান্তি (Burnout)" to burnoutScore,
            "দুঃখ ও বিষণ্ণতা (Sadness & Grief)" to sadnessScore,
            "আনন্দ ও আশা (Joy & Hope)" to joyScore
        ).sortedByDescending { it.second }

        val primary = if (scores.first().second > 0) scores[0].first else "মানসিক বিভ্রান্তি ও দ্বিধা (Inner Turmoil)"
        val secondary = if (scores.size > 1 && scores[1].second > 0) scores[1].first else "অব্যক্ত চাপ (Unspoken Strain)"

        val totalMatches = sadnessScore + frustrationScore + lonelinessScore + anxietyScore + heartbreakScore + burnoutScore + joyScore
        val intensity = when {
            totalMatches >= 4 -> 0.92f
            totalMatches >= 2 -> 0.85f
            totalMatches == 1 -> 0.75f
            else -> 0.65f
        }

        val valence = if (joyScore > (sadnessScore + frustrationScore + lonelinessScore + anxietyScore)) {
            "Positive"
        } else if (joyScore > 0 && totalMatches > joyScore) {
            "Mixed"
        } else {
            "Negative"
        }

        val coreTopic = when {
            heartbreakScore > 0 -> "সম্পর্কের বিচ্ছেদ ও মানসিক আঘাত (Relationship Trauma)"
            frustrationScore > 0 || burnoutScore > 0 -> "ক্যারিয়ার, কাজ ও ভবিষ্যতের অনিশ্চয়তা (Career & Workplace Strain)"
            lonelinessScore > 0 -> "সামাজিক বিচ্ছিন্নতা ও একাকীত্ব (Social Isolation)"
            anxietyScore > 0 -> "স্বাস্থ্য, পরিবার বা জীবনের সংকট (Health & Uncertainty Anxiety)"
            joyScore > 0 -> "জীবনের ব্যক্তিগত সাফল্য ও আশা (Personal Milestone & Hope)"
            else -> "ব্যক্তিগত আবেগ ও আত্ম-উপলব্ধি (Personal Emotional State)"
        }

        return EmotionAnalysisResult(
            primaryEmotion = primary,
            secondaryEmotion = secondary,
            intensity = intensity,
            valence = valence,
            coreTopic = coreTopic,
            keywords = detectedKeywords.distinct().take(6)
        )
    }

    fun parseGeminiJsonResponse(jsonStr: String, fallbackText: String): EmotionAnalysisResult {
        return try {
            val cleanJson = jsonStr.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val obj = JSONObject(cleanJson)
            val keywordsArray = obj.optJSONArray("keywords")
            val keywordsList = mutableListOf<String>()
            if (keywordsArray != null) {
                for (i in 0 until keywordsArray.length()) {
                    keywordsList.add(keywordsArray.getString(i))
                }
            }
            EmotionAnalysisResult(
                primaryEmotion = obj.optString("primaryEmotion", "হতাশা ও বিভ্রান্তি"),
                secondaryEmotion = obj.optString("secondaryEmotion", "মানসিক চাপ"),
                intensity = obj.optDouble("intensity", 0.8).toFloat(),
                valence = obj.optString("valence", "Negative"),
                coreTopic = obj.optString("coreTopic", "ব্যক্তিগত সংকট"),
                keywords = if (keywordsList.isNotEmpty()) keywordsList else listOf("আবেগ", "অনুভূতি")
            )
        } catch (e: Exception) {
            extractEmotionLocally(fallbackText)
        }
    }
}
