package com.example

import com.example.data.model.EmotionAnalysisResult
import com.example.remote.GeminiApi
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ResponseDirectnessTest {

    @Test
    fun testGreetingResponseIsDirectWithoutFluff() {
        val dummyEmotion = EmotionAnalysisResult(
            primaryEmotion = "Neutral",
            secondaryEmotion = "Calm",
            intensity = 0.2f,
            valence = "Neutral",
            coreTopic = "Greeting",
            keywords = emptyList()
        )

        val response = GeminiApi.synthesizeLocalEmpathy(
            userText = "হ্যালো কেমন আছেন?",
            emotion = dummyEmotion,
            matchedStories = emptyList()
        )

        // Should directly greet without unnecessary melodrama
        assertTrue(response.contains("হ্যালো") || response.contains("ভালো আছি"))
        assertFalse(response.contains("সবচেয়ে ভারী অনুভূতি"))
        assertFalse(response.contains("অন্ধকারের পরেই ভোরের আলো"))
    }

    @Test
    fun testTechnicalQuestionIsDirect() {
        val dummyEmotion = EmotionAnalysisResult(
            primaryEmotion = "Neutral",
            secondaryEmotion = "Curious",
            intensity = 0.2f,
            valence = "Neutral",
            coreTopic = "UI Design",
            keywords = emptyList()
        )

        val response = GeminiApi.synthesizeLocalEmpathy(
            userText = "ইউআই ডিজাইন কেমন হওয়া উচিত?",
            emotion = dummyEmotion,
            matchedStories = emptyList()
        )

        // Directly talks about UI design
        assertTrue(response.contains("UI") || response.contains("ডিজাইন"))
        assertFalse(response.contains("সবচেয়ে ভারী অনুভূতি"))
    }
}
