package com.example

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.data.model.AttachedMedia
import com.example.data.model.MediaType
import com.example.media.GenerationIntent
import com.example.media.MediaGenerationEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MediaGenerationEngineTest {

    @Test
    fun testImageGenerationIntentDetection() {
        val bengaliPrompt1 = "আমাকে একটি সূর্যাস্তের ছবি তৈরি করে দাও"
        val intent1 = MediaGenerationEngine.detectIntent(bengaliPrompt1, null)
        assertEquals(GenerationIntent.IMAGE_GENERATION, intent1)

        val bengaliPrompt2 = "একটি বিড়ালের ছবি আঁকো"
        val intent2 = MediaGenerationEngine.detectIntent(bengaliPrompt2, null)
        assertEquals(GenerationIntent.IMAGE_GENERATION, intent2)

        val englishPrompt = "generate an image of a futuristic neon city"
        val intent3 = MediaGenerationEngine.detectIntent(englishPrompt, null)
        assertEquals(GenerationIntent.IMAGE_GENERATION, intent3)

        val promptSlash = "/image a calm river in the morning"
        val intent4 = MediaGenerationEngine.detectIntent(promptSlash, null)
        assertEquals(GenerationIntent.IMAGE_GENERATION, intent4)
    }

    @Test
    fun testTextToVideoIntentDetection() {
        val bengaliPrompt = "একটি বৃষ্টির ভিডিও তৈরি করে দাও"
        val intent1 = MediaGenerationEngine.detectIntent(bengaliPrompt, null)
        assertEquals(GenerationIntent.TEXT_TO_VIDEO, intent1)

        val englishPrompt = "generate video of ocean waves crashing on rocks"
        val intent2 = MediaGenerationEngine.detectIntent(englishPrompt, null)
        assertEquals(GenerationIntent.TEXT_TO_VIDEO, intent2)

        val promptSlash = "/video flying through clouds"
        val intent3 = MediaGenerationEngine.detectIntent(promptSlash, null)
        assertEquals(GenerationIntent.TEXT_TO_VIDEO, intent3)
    }

    @Test
    fun testImageToVideoIntentDetection() {
        val dummyImage = AttachedMedia(
            uri = "content://media/external/images/media/1",
            mediaType = MediaType.IMAGE,
            displayName = "Source Photo"
        )

        val bengaliPrompt = "এই ছবি থেকে একটি ভিডিও তৈরি করে দাও"
        val intent1 = MediaGenerationEngine.detectIntent(bengaliPrompt, dummyImage)
        assertEquals(GenerationIntent.IMAGE_TO_VIDEO, intent1)

        val englishPrompt = "animate this image into a video clip"
        val intent2 = MediaGenerationEngine.detectIntent(englishPrompt, dummyImage)
        assertEquals(GenerationIntent.IMAGE_TO_VIDEO, intent2)
    }

    @Test
    fun testNormalChatNotMistakenForMedia() {
        val chatPrompt = "আজকে আমার মনটা খুব খারাপ লাগছে"
        val intent = MediaGenerationEngine.detectIntent(chatPrompt, null)
        assertEquals(GenerationIntent.NORMAL_CHAT, intent)
    }

    @Test
    fun testExtractCleanPrompt() {
        val rawPrompt = "আমাকে একটি সূর্যাস্তের সুন্দর দৃশ্য এর ছবি তৈরি করে দাও"
        val extracted = MediaGenerationEngine.extractPrompt(rawPrompt)
        assertTrue("Extracted prompt should contain core subject", extracted.contains("সূর্যাস্ত"))
        assertFalse("Extracted prompt should strip suffix boilerplate", extracted.endsWith("ছবি তৈরি করে দাও"))
    }

    @Test
    fun testGenerateImageExecution() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val result = MediaGenerationEngine.generateImage(app, "পাহাড় এবং নদী")
        assertTrue("Generation should succeed", result.success)
        assertNotNull("Generated media should exist", result.media)
        assertEquals(MediaType.IMAGE, result.media?.mediaType)
        assertNotNull("Base64 data or URI must be populated", result.media?.base64Data ?: result.media?.uri)
    }

    @Test
    fun testGenerateVideoExecution() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val result = MediaGenerationEngine.generateVideoFromText(app, "সমুদ্রের ঢেউ")
        assertTrue("Video generation should succeed", result.success)
        assertNotNull("Generated media should exist", result.media)
        assertEquals(MediaType.VIDEO, result.media?.mediaType)
    }
}
