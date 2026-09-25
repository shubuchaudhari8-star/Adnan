package com.example.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * TextToSpeech engine manager providing human-like voice synthesis
 * for the empathy engine companion responses.
 */
class TtsManager(context: Context) {

    private val TAG = "TtsManager"

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _currentlySpeakingId = MutableStateFlow<String?>(null)
    val currentlySpeakingId: StateFlow<String?> = _currentlySpeakingId.asStateFlow()

    private val _isAutoSpeakEnabled = MutableStateFlow(false)
    val isAutoSpeakEnabled: StateFlow<Boolean> = _isAutoSpeakEnabled.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                configureHumanLikeVoice()
            } else {
                Log.w(TAG, "TextToSpeech initialization failed with status: $status")
            }
        }

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
                _currentlySpeakingId.value = utteranceId
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
                _currentlySpeakingId.value = null
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
                _currentlySpeakingId.value = null
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                _isSpeaking.value = false
                _currentlySpeakingId.value = null
            }
        })
    }

    private fun configureHumanLikeVoice() {
        tts?.let { engine ->
            // Warm, natural pitch and slightly relaxed conversational pacing
            engine.setPitch(1.0f)
            engine.setSpeechRate(0.95f)

            // Attempt Bengali locale default
            val bnLocale = Locale.forLanguageTag("bn-BD")
            val result = engine.setLanguage(bnLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                engine.setLanguage(Locale.US)
            }
        }
    }

    /**
     * Speaks the given text aloud.
     * Automatically adjusts language to Bengali if Bengali characters are present.
     */
    fun speak(text: String, utteranceId: String) {
        if (!isInitialized || text.isBlank()) return

        tts?.let { engine ->
            val hasBengali = text.any { it in '\u0980'..'\u09FF' }
            if (hasBengali) {
                val bnLocale = Locale.forLanguageTag("bn-BD")
                val res = engine.setLanguage(bnLocale)
                if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                    engine.setLanguage(Locale.forLanguageTag("bn"))
                }
            } else {
                engine.setLanguage(Locale.US)
            }

            engine.setPitch(1.0f)
            engine.setSpeechRate(0.95f)

            // Flush queue and speak immediately
            engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        }
    }

    /**
     * Stops any ongoing speech.
     */
    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
        _currentlySpeakingId.value = null
    }

    fun toggleAutoSpeak() {
        val next = !_isAutoSpeakEnabled.value
        _isAutoSpeakEnabled.value = next
        if (!next) {
            stop()
        }
    }

    fun release() {
        try {
            stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing TTS", e)
        }
    }
}
