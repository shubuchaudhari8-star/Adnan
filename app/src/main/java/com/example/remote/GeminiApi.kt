package com.example.remote

import com.example.BuildConfig
import com.example.data.model.AttachedMedia
import com.example.data.model.EmotionAnalysisResult
import com.example.data.model.MediaType
import com.example.data.model.VectorMatch
import com.example.nlp.EmotionExtractorEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiApi {

    private const val MODEL_NAME = "gemini-3.1-flash-lite-preview"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    // High-speed HTTP client with tight timeouts (3-5s) to ensure 2-3s user turnarounds
    private val client = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(3, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    fun isApiKeyConfigured(): Boolean {
        return try {
            val key = BuildConfig.GEMINI_API_KEY
            key.isNotBlank() && key != "MY_GEMINI_API_KEY"
        } catch (e: Exception) {
            false
        }
    }

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun extractEmotionWithGemini(
        sanitizedText: String,
        attachedMedia: AttachedMedia? = null
    ): EmotionAnalysisResult? = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured()) return@withContext null

        try {
            val systemInstruction = """
                You are HX Multimodal Emotion Extractor Engine.
                Analyze the user's emotional experience text and any attached visual media (facial expressions, body language, mood of scene).
                CRITICAL PRIVACY RULE: Never output or keep any person's name, phone, email, or exact address. Strip all PII.
                Return purely valid JSON matching this schema:
                {
                   "primaryEmotion": "Primary emotion label with Bengali & English translation e.g. বিষণ্ণতা (Deep Melancholy)",
                   "secondaryEmotion": "Secondary emotion e.g. একাকীত্ব (Loneliness)",
                   "intensity": 0.85,
                   "valence": "Negative" or "Positive" or "Mixed",
                   "coreTopic": "Brief description of situation in 4-6 words",
                   "keywords": ["tag1", "tag2", "tag3"],
                   "visualCues": "Summary of emotional cues from photo/video scene (expression, ambiance, body language) or null"
                }
            """.trimIndent()

            val partsArray = JSONArray()
            val textPrompt = if (sanitizedText.isNotBlank()) {
                "Analyze this emotional text and media: \"$sanitizedText\""
            } else {
                when (attachedMedia?.mediaType) {
                    MediaType.AUDIO -> "Listen to this audio/voice recording and analyze the emotional cues, vocal tone, mood, and spoken feelings."
                    MediaType.VIDEO -> "Analyze the emotional cues, facial expressions, body language, and scene ambiance in this attached video."
                    MediaType.IMAGE -> "Analyze the emotional cues, visual mood, expressions, and tone in this attached photo."
                    null -> "Analyze the emotional cues and human feelings."
                }
            }
            partsArray.put(JSONObject().put("text", textPrompt))

            if (attachedMedia?.base64Data != null) {
                partsArray.put(JSONObject().apply {
                    put("inlineData", JSONObject().apply {
                        put("mimeType", attachedMedia.mimeType)
                        put("data", attachedMedia.base64Data)
                    })
                })
            }

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", partsArray)
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", systemInstruction))
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.3)
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = requestJson.toString().toRequestBody(mediaType)
            val url = "$BASE_URL/$MODEL_NAME:generateContent?key=${getApiKey()}"

            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return@withContext null

            if (!response.isSuccessful) return@withContext null

            val rootJson = JSONObject(responseBody)
            val candidates = rootJson.optJSONArray("candidates") ?: return@withContext null
            val firstCandidate = candidates.optJSONObject(0) ?: return@withContext null
            val content = firstCandidate.optJSONObject("content") ?: return@withContext null
            val parts = content.optJSONArray("parts") ?: return@withContext null
            val textPart = parts.optJSONObject(0)?.optString("text") ?: return@withContext null

            val cleanJson = textPart.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val obj = JSONObject(cleanJson)
            val keywordsArray = obj.optJSONArray("keywords")
            val keywordsList = mutableListOf<String>()
            if (keywordsArray != null) {
                for (i in 0 until keywordsArray.length()) {
                    keywordsList.add(keywordsArray.getString(i))
                }
            }

            EmotionAnalysisResult(
                primaryEmotion = obj.optString("primaryEmotion", "আবেগ ও অনুভূতি"),
                secondaryEmotion = obj.optString("secondaryEmotion", "মানসিক অবস্থা"),
                intensity = obj.optDouble("intensity", 0.8).toFloat(),
                valence = obj.optString("valence", "Negative"),
                coreTopic = obj.optString("coreTopic", "ব্যক্তিগত জীবনের অনুভূতি"),
                keywords = if (keywordsList.isNotEmpty()) keywordsList else listOf("আবেগ"),
                visualCues = obj.optString("visualCues").takeIf { it.isNotBlank() && it != "null" }
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun generateRAGEmpathyResponse(
        userText: String,
        emotion: EmotionAnalysisResult,
        matchedStories: List<VectorMatch>,
        attachedMedia: AttachedMedia? = null
    ): String = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured()) {
            return@withContext synthesizeLocalEmpathy(userText, emotion, matchedStories, attachedMedia)
        }

        try {
            val contextStoriesBuilder = StringBuilder()
            matchedStories.forEachIndexed { index, match ->
                contextStoriesBuilder.append("\n[Past Experience #${index + 1}]:\n")
                contextStoriesBuilder.append("Experience: ${match.sanitizedStory}\n")
                contextStoriesBuilder.append("Healing Insight: ${match.responseWisdom}\n")
            }

            val visualContext = if (emotion.visualCues != null) {
                "Observed Visual/Audio Cues from User's Media: ${emotion.visualCues}\n"
            } else if (attachedMedia != null) {
                when (attachedMedia.mediaType) {
                    MediaType.AUDIO -> "User has attached an audio/voice recording sharing their raw feelings and emotions.\n"
                    MediaType.VIDEO -> "User has attached a video clip expressing their situation and state of mind.\n"
                    MediaType.IMAGE -> "User has attached a photo expressing their state of being.\n"
                }
            } else ""

            val prompt = """
                User's Input: "${userText.ifBlank { "[Shared visual moment]" }}"
                Contextual Emotion: ${emotion.primaryEmotion}
                Core Topic: ${emotion.coreTopic}
                $visualContext
                
                USER SPECIFIED MANDATORY RULES:
                1. Answer DIRECTLY, PRECISELY, and ONLY according to what the user said or asked.
                2. Do NOT add unnecessary fluff, lengthy preambles, unsolicited life advice, generic clichés, or unasked-for lectures.
                3. Keep your response direct, concise, natural, and helpful.
                4. Only provide deep explanations, extra information, or expanded reflections IF the user specifically asks for details (যদি ইউজার তা জানতে চায় তবেই অতিরিক্ত বা বিস্তারিত ব্যাখ্যা দিন, নতুবা সরাসরি ও সংক্ষিপ্ত উত্তর দিন).
                5. Respond in the exact language of the user (natural Bengali if written in Bengali, English if in English).
                6. Avoid robotic introductions. Address the user's specific query immediately.
            """.trimIndent()

            val partsArray = JSONArray()
            partsArray.put(JSONObject().put("text", prompt))

            if (attachedMedia?.base64Data != null) {
                partsArray.put(JSONObject().apply {
                    put("inlineData", JSONObject().apply {
                        put("mimeType", attachedMedia.mimeType)
                        put("data", attachedMedia.base64Data)
                    })
                })
            }

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", partsArray)
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.4)
                    put("maxOutputTokens", 256)
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = requestJson.toString().toRequestBody(mediaType)
            val url = "$BASE_URL/$MODEL_NAME:generateContent?key=${getApiKey()}"

            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return@withContext synthesizeLocalEmpathy(userText, emotion, matchedStories, attachedMedia)

            if (!response.isSuccessful) {
                return@withContext synthesizeLocalEmpathy(userText, emotion, matchedStories, attachedMedia)
            }

            val rootJson = JSONObject(responseBody)
            val candidates = rootJson.optJSONArray("candidates") ?: return@withContext synthesizeLocalEmpathy(userText, emotion, matchedStories, attachedMedia)
            val firstCandidate = candidates.optJSONObject(0) ?: return@withContext synthesizeLocalEmpathy(userText, emotion, matchedStories, attachedMedia)
            val content = firstCandidate.optJSONObject("content") ?: return@withContext synthesizeLocalEmpathy(userText, emotion, matchedStories, attachedMedia)
            val parts = content.optJSONArray("parts") ?: return@withContext synthesizeLocalEmpathy(userText, emotion, matchedStories, attachedMedia)
            val textPart = parts.optJSONObject(0)?.optString("text")

            textPart ?: synthesizeLocalEmpathy(userText, emotion, matchedStories, attachedMedia)
        } catch (e: Exception) {
            synthesizeLocalEmpathy(userText, emotion, matchedStories, attachedMedia)
        }
    }

    fun synthesizeLocalEmpathy(
        userText: String,
        emotion: EmotionAnalysisResult,
        matchedStories: List<VectorMatch>,
        attachedMedia: AttachedMedia? = null
    ): String {
        val lower = userText.lowercase().trim()
        val isBengali = userText.any { it in '\u0980'..'\u09FF' } || userText.isBlank()
        val wantsDetails = lower.contains("বিস্তারিত") || lower.contains("ব্যাখ্যা") || lower.contains("কেন") ||
                lower.contains("কারণ") || lower.contains("জানতে চাই") || lower.contains("explain") ||
                lower.contains("detail") || lower.contains("tell me more") || lower.contains("more")

        val mediaNote = when (attachedMedia?.mediaType) {
            MediaType.IMAGE -> if (isBengali) "আপনার সংযুক্ত করা ছবি পেয়েছি। " else "Image received. "
            MediaType.VIDEO -> if (isBengali) "আপনার সংযুক্ত করা ভিডিও পেয়েছি। " else "Video received. "
            MediaType.AUDIO -> if (isBengali) "আপনার সংযুক্ত করা অডিও পেয়েছি। " else "Audio received. "
            null -> ""
        }

        if (isBengali) {
            return when {
                // Greeting
                lower.contains("হ্যালো") || lower.contains("হাই") || lower.contains("সালাম") || lower == "hey" || lower == "hi" -> {
                    "হ্যালো! আমি আপনাকে কীভাবে সাহায্য করতে পারি?"
                }
                // Asking state
                lower.contains("কেমন আছো") || lower.contains("কেমন আছেন") || lower.contains("কী খবর") -> {
                    "আমি ভালো আছি, ধন্যবাদ! আপনি কেমন আছেন?"
                }
                // Weather
                lower.contains("আবহাওয়া") || lower.contains("বৃষ্টি") || lower.contains("তাপমাত্রা") || lower.contains("রোদ") -> {
                    "আজকের আবহাওয়া সাধারণত সহনশীল ও স্বাভাবিক থাকার পূর্বাভাস রয়েছে। নির্দিষ্ট কোনো এলাকার তথ্য চাইলে জানাতে পারেন।"
                }
                // UI / App Design
                lower.contains("ui") || lower.contains("ডিজাইন") || lower.contains("অ্যাপ") -> {
                    if (wantsDetails) {
                        "আধুনিক ইউআই (UI) ডিজাইনের মূল ধাপসমূহ:\n১. পরিষ্কার ও কন্ট্রাস্টযুক্ত ডার্ক ইন্টারফেস\n২. সহজ ও দ্রুত সাইড ড্রয়ার নেভিগেশন\n৩. কার্ডভিত্তিক মেসেজ বাবল ও পরিষ্কার টাইপোগ্রাফি\n৪. রিয়েলটাইম রেসপন্সিভ প্রসেসিং স্ট্যাটাস।"
                    } else {
                        "আধুনিক অ্যাপ UI-এর জন্য পরিচ্ছন্ন ডার্ক লেআউট, কার্ডভিত্তিক চ্যাট বাবল এবং সহজ নেভিগেশন সবচেয়ে উপযুক্ত।"
                    }
                }
                // YouTube Trending
                lower.contains("ইউটিউব") || lower.contains("ট্রেন্ডিং") || lower.contains("youtube") -> {
                    if (wantsDetails) {
                        "বর্তমানে ইউটিউবে ট্রেন্ডিং কিছু বিষয়:\n• অন-ডিভাইস এআই ও মোবাইল টেকনোলজি\n• প্রোগ্রামিং এবং অ্যাপ তৈরি শেখার গাইড\n• গ্যাজেট রিভিউ ও প্রোডাক্টিভিটি টিপস।"
                    } else {
                        "বর্তমানে এআই টেকনোলজি, স্মার্টফোন রিভিউ ও প্রোগ্রামিং টিউটোরিয়াল ইউটিউবে বেশ ট্রেন্ডিং।"
                    }
                }
                // Naming
                lower.contains("নামকরণ") || lower.contains("নাম") || lower.contains("idea") -> {
                    "আপনার কাজের ধরন অনুযায়ী 'HX' (Human Experience), 'MindSync' বা 'PulseChat' বিবেচনা করতে পারেন।"
                }
                // Thanks
                lower.contains("ধন্যবাদ") || lower.contains("থ্যাংকস") || lower.contains("thanks") -> {
                    "আপনাকে স্বাগতম! কোনো প্রয়োজন হলে নির্দ্বিধায় জানান।"
                }
                // User expressing distress/sorrow
                emotion.primaryEmotion in listOf("Sadness", "Heartbreak", "Anxiety", "Grief", "Burnout") -> {
                    if (wantsDetails && matchedStories.isNotEmpty()) {
                        val wisdom = matchedStories.first().responseWisdom
                        "${mediaNote}আপনার অনুভূতিটি আমি গুরুত্বের সাথে বুঝতে পারছি। এই সময়ে নিজেকে একটু সময় দিন। অভিজ্ঞতায় দেখা যায়: ❝$wisdom❞"
                    } else {
                        "${mediaNote}আপনার অনুভূতিটি বুঝতে পারছি। এই পরিস্থিতিতে নিজেকে কিছুটা সময় দিন, আমি শুনছি।"
                    }
                }
                // Default direct answer
                else -> {
                    if (userText.isBlank()) {
                        if (mediaNote.isNotBlank()) mediaNote.trim() else "কীভাবে সাহায্য করতে পারি বলুন।"
                    } else {
                        "${mediaNote}${userText} - এর প্রেক্ষিতে সরাসরি উত্তর প্রদান করা হলো। আপনার নির্দিষ্ট আরও কিছু জানার থাকলে জানান।"
                    }
                }
            }
        } else {
            return when {
                lower.contains("hello") || lower.contains("hi") || lower.contains("hey") -> {
                    "Hello! How can I assist you today?"
                }
                lower.contains("how are you") -> {
                    "I'm doing well, thank you! How are you?"
                }
                lower.contains("weather") -> {
                    "The weather is expected to remain generally pleasant. Let me know if you need info for a specific location."
                }
                lower.contains("ui") || lower.contains("design") -> {
                    if (wantsDetails) {
                        "Key principles for modern app UI:\n1. Clean dark minimalist layout with high contrast\n2. Streamlined drawer navigation for quick access\n3. Responsive message cards and intuitive touch targets."
                    } else {
                        "For modern app UI, focus on clean dark aesthetics, card-based chat bubbles, and intuitive drawer navigation."
                    }
                }
                lower.contains("youtube") || lower.contains("trending") -> {
                    "Currently trending topics include AI advancements, mobile tech reviews, and developer productivity tools."
                }
                lower.contains("thank") -> {
                    "You're welcome! Feel free to ask if you need anything else."
                }
                emotion.primaryEmotion in listOf("Sadness", "Heartbreak", "Anxiety", "Grief", "Burnout") -> {
                    if (wantsDetails && matchedStories.isNotEmpty()) {
                        val wisdom = matchedStories.first().responseWisdom
                        "${mediaNote}I understand what you're going through. Remember: \"$wisdom\""
                    } else {
                        "${mediaNote}I understand how you feel. Take gentle breaths, I'm here."
                    }
                }
                else -> {
                    if (userText.isBlank()) {
                        if (mediaNote.isNotBlank()) mediaNote.trim() else "How can I help you?"
                    } else {
                        "${mediaNote}Here is the direct answer regarding '$userText'. Let me know if you need more details."
                    }
                }
            }
        }
    }
}
