package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.EmotionTagEntity
import com.example.data.local.entity.ExperienceEntity
import com.example.data.model.AttachedMedia
import com.example.data.model.ChatMessage
import com.example.data.model.ChatSession
import com.example.data.model.EmpathyPipelineResult
import com.example.data.model.MediaType
import com.example.data.model.MessageSender
import com.example.data.model.NotebookEntry
import com.example.data.repository.EmotionTagRepository
import com.example.data.repository.ExperienceRepository
import com.example.media.GenerationIntent
import com.example.media.MediaGenerationEngine
import com.example.nlp.AnonymizerEngine
import com.example.nlp.EmotionExtractorEngine
import com.example.nlp.EmotionTagMLEngine
import com.example.nlp.VectorEmbeddingEngine
import com.example.remote.GeminiApi
import com.example.util.MediaHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class HxTab {
    PIPELINE_HUB,
    MEMORY_BANK,
    STUDIO_BLUEPRINT
}

class HxViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExperienceRepository
    private val emotionTagRepository: EmotionTagRepository
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _currentPipelineResult = MutableStateFlow<EmpathyPipelineResult?>(null)
    val currentPipelineResult: StateFlow<EmpathyPipelineResult?> = _currentPipelineResult.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _attachedMedia = MutableStateFlow<AttachedMedia?>(null)
    val attachedMedia: StateFlow<AttachedMedia?> = _attachedMedia.asStateFlow()

    private val _activeTab = MutableStateFlow(HxTab.PIPELINE_HUB)
    val activeTab: StateFlow<HxTab> = _activeTab.asStateFlow()

    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val defaultSessions = listOf(
        ChatSession(
            id = "sess_1",
            title = "এআই চ্যাট অ্যাপ UI ডিজাইন",
            messages = listOf(
                ChatMessage(
                    sender = MessageSender.USER,
                    text = "হ্যালো! এআই চ্যাট অ্যাপের আধুনিক UI ডিজাইন কেমন হওয়া উচিত?",
                    timestamp = System.currentTimeMillis() - 120000
                ),
                ChatMessage(
                    sender = MessageSender.COMPANION,
                    text = "একটি আধুনিক এআই চ্যাট অ্যাপের UI ডিজাইনে ডার্ক মিনিমালিস্টিক থিম, কার্ডভিত্তিক ম্যাসেজ লেআউট, স্মুথ অ্যানিমেশন এবং দ্রুত অ্যাকশন ড্রয়ার থাকা অত্যন্ত কার্যকর।",
                    timestamp = System.currentTimeMillis() - 60000,
                    isAiGenerated = true
                )
            )
        ),
        ChatSession(
            id = "sess_2",
            title = "আবেগভিত্তিক এআই অ্যাপের নামকরণ ও ...",
            messages = listOf(
                ChatMessage(
                    sender = MessageSender.USER,
                    text = "আবেগভিত্তিক এআই অ্যাপের জন্য ভালো নাম কী হতে পারে?",
                    timestamp = System.currentTimeMillis() - 3600000
                ),
                ChatMessage(
                    sender = MessageSender.COMPANION,
                    text = "HX (Human Experience), EmpathyPulse, বা SoulAI নামগুলো বেশ অর্থপূর্ণ এবং প্রযুক্তির সাথে মানবিক আবেগের মেলবন্ধন প্রকাশ করে।",
                    timestamp = System.currentTimeMillis() - 3500000,
                    isAiGenerated = true
                )
            )
        ),
        ChatSession(
            id = "sess_3",
            title = "আত্মিক সম্পর্কের এআই অ্যাপের ধারণা",
            messages = listOf(
                ChatMessage(
                    sender = MessageSender.USER,
                    text = "মানুষের সাথে প্রযুক্তির আত্মিক সংযোগ কীভাবে তৈরি করা যায়?",
                    timestamp = System.currentTimeMillis() - 7200000
                ),
                ChatMessage(
                    sender = MessageSender.COMPANION,
                    text = "সহমর্মিতা, সক্রিয় শ্রবণ এবং ব্যবহারকারীর অনুভূতিকে সম্মান জানানোর মাধ্যমে এআই একটি সহৃদয় সঙ্গীতে রূপ নিতে পারে।",
                    timestamp = System.currentTimeMillis() - 7100000,
                    isAiGenerated = true
                )
            ),
            hasUnreadOrActiveDot = true
        ),
        ChatSession(
            id = "sess_4",
            title = "ইউটিউব ট্রেন্ডিং টপিকসমূহ",
            messages = listOf(
                ChatMessage(
                    sender = MessageSender.USER,
                    text = "বর্তমানে টেক ইউটিউব চ্যানেলের জন্য ট্রেন্ডিং টপিকগুলো কী কী?",
                    timestamp = System.currentTimeMillis() - 86400000
                ),
                ChatMessage(
                    sender = MessageSender.COMPANION,
                    text = "অন-ডিভাইস এআই, লোকাল এলএলএম, জেটপ্যাক কম্পোজ ট্রিকস এবং প্রাইভেসি-ফার্স্ট আর্কিটেকচার বর্তমানে সবচেয়ে জনপ্রিয়।",
                    timestamp = System.currentTimeMillis() - 86300000,
                    isAiGenerated = true
                )
            )
        )
    )

    private val _chatSessions = MutableStateFlow<List<ChatSession>>(
        listOf(
            ChatSession(
                id = "sess_${System.currentTimeMillis()}",
                title = "নতুন চ্যাট",
                messages = emptyList(),
                hasUnreadOrActiveDot = false
            )
        ) + defaultSessions
    )
    val chatSessions: StateFlow<List<ChatSession>> = _chatSessions.asStateFlow()

    private val _currentSessionId = MutableStateFlow(_chatSessions.value.first().id)
    val currentSessionId: StateFlow<String> = _currentSessionId.asStateFlow()

    private val _notebooks = MutableStateFlow<List<NotebookEntry>>(
        listOf(
            NotebookEntry(title = "এআই অ্যাপ ডিজাইন নোট", content = "ডার্ক থিম, জেমিনি স্টাইল ড্রয়ার এবং ইউজার হিস্ট্রি মডিউল।"),
            NotebookEntry(title = "প্রাইভেসি আর্কিটেকচার", content = "অন-ডিভাইস ফিল্টার ও এনক্রিপ্টেড ডাটা স্টোরেজ।")
        )
    )
    val notebooks: StateFlow<List<NotebookEntry>> = _notebooks.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    val allExperiences: StateFlow<List<ExperienceEntity>>
    val storedEmotionTags: StateFlow<List<EmotionTagEntity>>

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = ExperienceRepository(db.experienceDao())
        emotionTagRepository = EmotionTagRepository(db.emotionTagDao())
        allExperiences = repository.allExperiences.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        storedEmotionTags = emotionTagRepository.allTags.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        viewModelScope.launch {
            repository.getAllExperiencesSync()
        }
    }

    private val _showHistorySheet = MutableStateFlow(false)
    val showHistorySheet: StateFlow<Boolean> = _showHistorySheet.asStateFlow()

    fun openHistorySheet() {
        _showHistorySheet.value = true
    }

    fun closeHistorySheet() {
        _showHistorySheet.value = false
    }

    companion object {
        fun isNewChatTrigger(text: String): Boolean {
            val clean = text.trim().lowercase()
            return clean == "নিউ চ্যাট" ||
                    clean == "নিউচ্যাট" ||
                    clean == "নতুন চ্যাট" ||
                    clean == "নতুনচ্যাট" ||
                    clean == "new chat" ||
                    clean == "newchat" ||
                    clean == "/new" ||
                    clean == "/newchat"
        }

        fun isThreeDotsTrigger(text: String): Boolean {
            val clean = text.trim().lowercase()
            if (clean == "..." || clean == "…" || clean == ". . ." || clean.matches(Regex("""^\.{3,}$"""))) {
                return true
            }
            return clean == "থ্রিডড" ||
                    clean == "থ্রি ডট" ||
                    clean == "থ্রিডট" ||
                    clean == "থ্রি-ডট" ||
                    clean == "তিনটা ডট" ||
                    clean == "তিনটি ডট" ||
                    clean == "৩টি ডট" ||
                    clean == "৩টা ডট" ||
                    clean == "three dots" ||
                    clean == "three dot" ||
                    clean == "threedots" ||
                    clean == "threedot" ||
                    clean == "dot dot dot" ||
                    clean == "/menu" ||
                    clean == "/history"
        }
    }

    fun onInputTextChanged(text: String) {
        if (isThreeDotsTrigger(text)) {
            _inputText.value = ""
            _showHistorySheet.value = true
            _statusMessage.value = "থ্রি-ডট ফাংশন ও হিস্ট্রি ওপেন হয়েছে"
            return
        }
        if (isNewChatTrigger(text)) {
            _inputText.value = ""
            _showHistorySheet.value = false
            startNewChat()
            _statusMessage.value = "সম্পূর্ণ চ্যাট মুছে নতুন চ্যাট ওপেন হয়েছে"
            return
        }
        _inputText.value = text
    }

    fun setActiveTab(tab: HxTab) {
        _activeTab.value = tab
    }

    fun setFilter(emotion: String) {
        _selectedFilter.value = emotion
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun loadSample(text: String) {
        _inputText.value = text
    }

    /**
     * Synchronizes current in-memory chat messages and intelligent title into the active session.
     */
    private fun syncCurrentSession(userTextForTitle: String? = null) {
        val currentId = _currentSessionId.value
        val msgs = _chatMessages.value
        _chatSessions.value = _chatSessions.value.map { sess ->
            if (sess.id == currentId) {
                val updatedTitle = if ((sess.title == "নতুন চ্যাট" || sess.title.isBlank()) && !userTextForTitle.isNullOrBlank()) {
                    userTextForTitle.take(30) + if (userTextForTitle.length > 30) "..." else ""
                } else sess.title
                sess.copy(
                    title = updatedTitle,
                    messages = msgs
                )
            } else sess
        }
    }

    fun startNewChat(title: String = "নতুন চ্যাট") {
        // 1. Sync current active session before switching
        syncCurrentSession()

        val currentId = _currentSessionId.value
        val currentSession = _chatSessions.value.find { it.id == currentId }

        // If current session is already brand new and empty, reuse it
        if (currentSession != null && currentSession.messages.isEmpty() && _chatMessages.value.isEmpty()) {
            _inputText.value = ""
            _attachedMedia.value = null
            _statusMessage.value = "নতুন চ্যাট প্রস্তুত"
            return
        }

        // 2. Create fresh independent session
        val newSession = ChatSession(
            id = "sess_${System.currentTimeMillis()}",
            title = title,
            messages = emptyList(),
            hasUnreadOrActiveDot = false
        )
        _chatSessions.value = listOf(newSession) + _chatSessions.value
        _currentSessionId.value = newSession.id
        _chatMessages.value = emptyList()
        _inputText.value = ""
        _attachedMedia.value = null
        _statusMessage.value = "নতুন চ্যাট শুরু হয়েছে"
    }

    fun selectSession(sessionId: String) {
        if (sessionId == _currentSessionId.value) return

        // 1. Save current session messages before switching
        syncCurrentSession()

        // 2. Switch to target session
        val session = _chatSessions.value.find { it.id == sessionId } ?: return
        _currentSessionId.value = sessionId
        _chatMessages.value = session.messages
        _inputText.value = ""
        _attachedMedia.value = null

        // Clear unread dot if present
        if (session.hasUnreadOrActiveDot) {
            _chatSessions.value = _chatSessions.value.map {
                if (it.id == sessionId) it.copy(hasUnreadOrActiveDot = false) else it
            }
        }
        _statusMessage.value = "\"${session.title}\" লোড করা হয়েছে"
    }

    fun deleteSession(sessionId: String) {
        val remaining = _chatSessions.value.filter { it.id != sessionId }
        if (remaining.isEmpty()) {
            val fresh = ChatSession(
                id = "sess_${System.currentTimeMillis()}",
                title = "নতুন চ্যাট",
                messages = emptyList(),
                hasUnreadOrActiveDot = false
            )
            _chatSessions.value = listOf(fresh)
            _currentSessionId.value = fresh.id
            _chatMessages.value = emptyList()
        } else {
            _chatSessions.value = remaining
            if (_currentSessionId.value == sessionId) {
                val next = remaining.first()
                _currentSessionId.value = next.id
                _chatMessages.value = next.messages
            }
        }
        _statusMessage.value = "চ্যাটটি মুছে ফেলা হয়েছে"
    }

    fun clearAllChatHistory() {
        val fresh = ChatSession(
            id = "sess_${System.currentTimeMillis()}",
            title = "নতুন চ্যাট",
            messages = emptyList(),
            hasUnreadOrActiveDot = false
        )
        _chatSessions.value = listOf(fresh)
        _currentSessionId.value = fresh.id
        _chatMessages.value = emptyList()
        _inputText.value = ""
        _attachedMedia.value = null
        _statusMessage.value = "সকল পূর্ববর্তী চ্যাট মুছে ফেলা হয়েছে"
    }

    fun addNotebook(title: String, content: String) {
        val entry = NotebookEntry(title = title, content = content)
        _notebooks.value = listOf(entry) + _notebooks.value
        _statusMessage.value = "নোটবুক যুক্ত করা হয়েছে: $title"
    }

    fun attachMedia(uri: Uri, mediaType: MediaType, displayName: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val processed = MediaHelper.processUri(
                context = context,
                uri = uri,
                explicitType = mediaType,
                customDisplayName = displayName
            )
            _attachedMedia.value = processed
            _statusMessage.value = "${processed.displayName} সফলভাবে যুক্ত হয়েছে"
        }
    }

    fun attachSampleMedia(type: MediaType) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val media = when (type) {
                MediaType.IMAGE -> MediaHelper.createSamplePhoto(context)
                MediaType.AUDIO -> MediaHelper.createSampleAudio(context)
                MediaType.VIDEO -> MediaHelper.createSampleVideo(context)
            }
            _attachedMedia.value = media
            _statusMessage.value = "${media.displayName} সফলভাবে যুক্ত হয়েছে"
        }
    }

    fun attachMediaUri(uri: Uri, displayName: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val mimeType = context.contentResolver.getType(uri) ?: ""
            val mediaType = when {
                mimeType.startsWith("video/") -> MediaType.VIDEO
                mimeType.startsWith("audio/") -> MediaType.AUDIO
                else -> MediaType.IMAGE
            }
            val processed = MediaHelper.processUri(
                context = context,
                uri = uri,
                explicitType = mediaType,
                customDisplayName = displayName
            )
            _attachedMedia.value = processed
        }
    }

    fun clearAttachedMedia() {
        _attachedMedia.value = null
    }

    fun sendEmpathyMessage() {
        val text = _inputText.value.trim()
        val media = _attachedMedia.value
        if (text.isBlank() && media == null) return

        if (isThreeDotsTrigger(text)) {
            _inputText.value = ""
            _showHistorySheet.value = true
            _statusMessage.value = "থ্রি-ডট ফাংশন ও হিস্ট্রি ওপেন হয়েছে"
            return
        }
        if (isNewChatTrigger(text)) {
            _inputText.value = ""
            _showHistorySheet.value = false
            startNewChat()
            _statusMessage.value = "সম্পূর্ণ চ্যাট মুছে নতুন চ্যাট ওপেন হয়েছে"
            return
        }

        // Clear input field and attached media preview immediately
        _inputText.value = ""
        _attachedMedia.value = null

        val userMsgId = java.util.UUID.randomUUID().toString()
        val anonymization = AnonymizerEngine.anonymize(text)

        // Add user message to chat stream and sync session immediately
        val initialUserMsg = ChatMessage(
            id = userMsgId,
            sender = MessageSender.USER,
            text = text,
            attachedMedia = media,
            isPrivacySanitized = true,
            sanitizedText = anonymization.sanitizedText
        )
        _chatMessages.value = _chatMessages.value + initialUserMsg
        syncCurrentSession(userTextForTitle = text)

        val generationIntent = MediaGenerationEngine.detectIntent(text, media)
        if (generationIntent != GenerationIntent.NORMAL_CHAT) {
            viewModelScope.launch {
                _isProcessing.value = true
                val result = when (generationIntent) {
                    GenerationIntent.IMAGE_GENERATION -> {
                        _statusMessage.value = "উচ্চমানের ছবি তৈরি করা হচ্ছে..."
                        MediaGenerationEngine.generateImage(getApplication(), text)
                    }
                    GenerationIntent.TEXT_TO_VIDEO -> {
                        _statusMessage.value = "ভিডিও তৈরি করা হচ্ছে..."
                        MediaGenerationEngine.generateVideoFromText(getApplication(), text)
                    }
                    GenerationIntent.IMAGE_TO_VIDEO -> {
                        _statusMessage.value = "ছবি থেকে ভিডিও তৈরি করা হচ্ছে..."
                        MediaGenerationEngine.generateVideoFromImage(getApplication(), media!!, text)
                    }
                    else -> null
                }

                if (result != null) {
                    val aiMsg = ChatMessage(
                        sender = MessageSender.COMPANION,
                        text = result.summaryText,
                        attachedMedia = result.media,
                        isAiGenerated = true
                    )
                    _chatMessages.value = _chatMessages.value + aiMsg
                    syncCurrentSession()
                    _statusMessage.value = "মিডিয়া তৈরি সম্পন্ন হয়েছে"
                }
                _isProcessing.value = false
            }
            return
        }

        viewModelScope.launch {
            _isProcessing.value = true

            // ML Module: Multi-dimensional emotion tag extraction with Coroutines (Dispatchers.Default)
            val extractedTags = EmotionTagMLEngine.extractEmotionTags(
                text = if (anonymization.sanitizedText.isNotBlank()) anonymization.sanitizedText else text,
                messageId = userMsgId
            )

            // Store extracted emotion tags in Room asynchronously via Coroutines (Dispatchers.IO)
            if (extractedTags.isNotEmpty()) {
                emotionTagRepository.storeExtractedTags(extractedTags)
                // Update user message with extracted tags
                _chatMessages.value = _chatMessages.value.map {
                    if (it.id == userMsgId) it.copy(extractedEmotionTags = extractedTags) else it
                }
                syncCurrentSession()
            }

            try {
                // High-speed local processing (<2ms): Memory bank lookup & NLP Emotion Extraction
                val storedExperiences = repository.getAllExperiencesSync()
                val emotion = EmotionExtractorEngine.extractEmotionLocally(anonymization.sanitizedText)

                // High-speed Vector Matching (<1ms)
                val queryVector = VectorEmbeddingEngine.generateEmbedding(anonymization.sanitizedText, emotion)
                val matches = VectorEmbeddingEngine.findTopMatches(queryVector, storedExperiences, topK = 3)

                // High-Speed Direct AI Generation (1.5-2.5s via Flash-Lite / Direct response)
                val isAi = GeminiApi.isApiKeyConfigured()
                val empathyResponse = GeminiApi.generateRAGEmpathyResponse(
                    userText = anonymization.sanitizedText,
                    emotion = emotion,
                    matchedStories = matches,
                    attachedMedia = media
                )

                val result = EmpathyPipelineResult(
                    originalInput = text,
                    anonymization = anonymization,
                    emotionAnalysis = emotion,
                    matchedStories = matches,
                    empathyResponse = empathyResponse,
                    isAiGenerated = isAi,
                    attachedMedia = media,
                    extractedEmotionTags = extractedTags
                )
                _currentPipelineResult.value = result

                // Immediately append Companion message to chat stream so user sees response instantly
                val companionMsg = ChatMessage(
                    sender = MessageSender.COMPANION,
                    text = empathyResponse,
                    emotionResult = emotion,
                    matchedStories = matches,
                    isAiGenerated = isAi,
                    extractedEmotionTags = extractedTags
                )
                _chatMessages.value = _chatMessages.value + companionMsg
                syncCurrentSession()

                // Asynchronously persist to Room Vector Memory Bank without blocking UI
                if (anonymization.sanitizedText.isNotBlank()) {
                    val newEntity = ExperienceEntity(
                        originalText = "[PRIVACY MASKED]",
                        sanitizedText = anonymization.sanitizedText,
                        primaryEmotion = emotion.primaryEmotion,
                        secondaryEmotion = emotion.secondaryEmotion,
                        intensity = emotion.intensity,
                        valence = emotion.valence,
                        coreTopic = emotion.coreTopic,
                        keywords = emotion.keywords.joinToString(", "),
                        embeddingVector = queryVector.joinToString(",") { it.toString() },
                        empathyWisdom = empathyResponse.take(200) + "...",
                        isUserStory = true
                    )
                    launch(Dispatchers.IO) {
                        repository.insertExperience(newEntity)
                    }
                }
            } catch (e: Exception) {
                val errorMsg = ChatMessage(
                    sender = MessageSender.COMPANION,
                    text = "আপনার অনুভূতি শুনতে শুনতে কিছুটা সমস্যা হয়েছে, তবে আমি আপনার সাথে আছি। চাইলে আবার লিখতে পারেন।"
                )
                _chatMessages.value = _chatMessages.value + errorMsg
            } finally {
                _isProcessing.value = false
                // Sync current active session's messages and title
                val currentId = _currentSessionId.value
                _chatSessions.value = _chatSessions.value.map { sess ->
                    if (sess.id == currentId) {
                        val updatedTitle = if ((sess.title == "নতুন চ্যাট" || sess.title.isBlank()) && text.isNotBlank()) {
                            text.take(28) + if (text.length > 28) "..." else ""
                        } else sess.title
                        sess.copy(
                            title = updatedTitle,
                            messages = _chatMessages.value
                        )
                    } else sess
                }
            }
        }
    }

    fun deleteExperience(id: Long) {
        viewModelScope.launch {
            repository.deleteExperience(id)
        }
    }

    fun clearUserStories() {
        viewModelScope.launch {
            repository.clearUserStories()
            _statusMessage.value = "আপনার শেয়ার করা অভিজ্ঞতাগুলো মুছে ফেলা হয়েছে।"
        }
    }

    fun deleteEmotionTag(id: Long) {
        viewModelScope.launch {
            emotionTagRepository.deleteTag(id)
        }
    }

    fun clearAllEmotionTags() {
        viewModelScope.launch {
            emotionTagRepository.clearAllTags()
            _statusMessage.value = "সংরক্ষিত সকল ইমোশন ট্যাগ মুছে ফেলা হয়েছে।"
        }
    }

    fun clearChatHistory() {
        _chatMessages.value = emptyList()
        _inputText.value = ""
        _attachedMedia.value = null
        _statusMessage.value = "আগের কথোপকথনের হিস্ট্রি মুছে ফেলা হয়েছে"
    }
}
