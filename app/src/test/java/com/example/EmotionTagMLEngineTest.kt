package com.example

import com.example.nlp.EmotionTagMLEngine
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EmotionTagMLEngineTest {

    @Test
    fun testAnxietyExtractionInBengali() = runTest {
        val input = "আমার কাল পরীক্ষা, খুব ভয় এবং দুশ্চিন্তা হচ্ছে, বুক কাঁপছে।"
        val tags = EmotionTagMLEngine.extractEmotionTags(input)

        assertFalse("Tags should not be empty", tags.isEmpty())
        val anxietyTag = tags.find { it.tag == "ANXIETY_FEAR" }
        assertNotNull("Should detect ANXIETY_FEAR tag", anxietyTag)
        assertTrue("Confidence should be high (> 0.6)", anxietyTag!!.confidence >= 0.6f)
        assertTrue("Valence should be negative", anxietyTag.valence < 0f)
    }

    @Test
    fun testJoyAndGratitudeExtraction() = runTest {
        val input = "আমি আজ স্কলারশিপ পেয়েছি! ভীষণ আনন্দ হচ্ছে, পরিবারের সবাই খুব খুশি।"
        val tags = EmotionTagMLEngine.extractEmotionTags(input)

        assertFalse("Tags should not be empty", tags.isEmpty())
        val joyTag = tags.find { it.tag == "JOY_GRATITUDE" }
        assertNotNull("Should detect JOY_GRATITUDE tag", joyTag)
        assertTrue("Valence should be positive", joyTag!!.valence > 0f)
    }

    @Test
    fun testLonelinessExtraction() = runTest {
        val input = "নতুন শহরে এসে নিজেকে খুব একা লাগছে, কেউ পাশে নেই।"
        val tags = EmotionTagMLEngine.extractEmotionTags(input)

        assertFalse("Tags should not be empty", tags.isEmpty())
        val lonelyTag = tags.find { it.tag == "LONELINESS_ISOLATION" }
        assertNotNull("Should detect LONELINESS_ISOLATION tag", lonelyTag)
    }

    @Test
    fun testBurnoutExtractionInEnglish() = runTest {
        val input = "I am completely exhausted from continuous overtime at the office. Pure burnout and no energy."
        val tags = EmotionTagMLEngine.extractEmotionTags(input)

        assertFalse("Tags should not be empty", tags.isEmpty())
        val burnoutTag = tags.find { it.tag == "BURNOUT_EXHAUSTION" }
        assertNotNull("Should detect BURNOUT_EXHAUSTION tag", burnoutTag)
    }
}
