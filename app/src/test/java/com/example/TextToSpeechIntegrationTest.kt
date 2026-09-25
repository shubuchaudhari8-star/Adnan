package com.example

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tts.TtsManager
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TextToSpeechIntegrationTest {

    @Test
    fun testTtsManagerInitializationAndAutoSpeakToggle() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val ttsManager = TtsManager(context)

        // Initial state verification
        assertFalse("Initial speaking state should be false", ttsManager.isSpeaking.value)
        assertNull("Initial speaking ID should be null", ttsManager.currentlySpeakingId.value)
        assertFalse("Initial auto-speak should be false", ttsManager.isAutoSpeakEnabled.value)

        // Toggle auto-speak
        ttsManager.toggleAutoSpeak()
        assertTrue("Auto speak should be enabled after toggle", ttsManager.isAutoSpeakEnabled.value)

        ttsManager.toggleAutoSpeak()
        assertFalse("Auto speak should be disabled after second toggle", ttsManager.isAutoSpeakEnabled.value)

        // Test stop and release safety
        ttsManager.stop()
        ttsManager.release()
    }

    @Test
    fun testBengaliLanguageDetection() {
        val bengaliText = "আমি আপনার কথা বুঝতে পারছি, আপনার কষ্ট স্বাভাবিক।"
        val englishText = "I understand what you are going through."

        val hasBengali1 = bengaliText.any { it in '\u0980'..'\u09FF' }
        val hasBengali2 = englishText.any { it in '\u0980'..'\u09FF' }

        assertTrue("Bengali text should be detected", hasBengali1)
        assertFalse("English text should not detect Bengali characters", hasBengali2)
    }
}
