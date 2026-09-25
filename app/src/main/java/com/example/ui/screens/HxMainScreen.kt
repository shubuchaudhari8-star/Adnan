package com.example.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MediaType
import com.example.data.model.MessageSender
import com.example.tts.TtsManager
import com.example.ui.components.BottomInputBar
import com.example.ui.components.ChatMessageCard
import com.example.ui.components.ConversationHistorySheet
import com.example.ui.components.GeminiDrawerSheet
import com.example.ui.viewmodel.HxViewModel
import java.util.*

val HxScreenBackground = Color(0xFF121214)
val HxTopBarBackground = Color(0xFF18181C)
val HxTopIconBtnColor = Color(0xFF2C2C2E)
val HxUserAvatarBlue = Color(0xFF1D72FE)
val HxRedDotColor = Color(0xFFFF3B30)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HxMainScreen(
    viewModel: HxViewModel,
    onShowSplashScreen: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val attachedMedia by viewModel.attachedMedia.collectAsStateWithLifecycle()
    val storedEmotionTags by viewModel.storedEmotionTags.collectAsStateWithLifecycle()
    val chatSessions by viewModel.chatSessions.collectAsStateWithLifecycle()
    val currentSessionId by viewModel.currentSessionId.collectAsStateWithLifecycle()
    val notebooks by viewModel.notebooks.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val showHistorySheet by viewModel.showHistorySheet.collectAsStateWithLifecycle()

    // TextToSpeech Engine for reading empathy responses aloud
    val ttsManager = remember { TtsManager(context) }
    DisposableEffect(ttsManager) {
        onDispose {
            ttsManager.release()
        }
    }
    val isSpeaking by ttsManager.isSpeaking.collectAsStateWithLifecycle()
    val currentlySpeakingId by ttsManager.currentlySpeakingId.collectAsStateWithLifecycle()
    val isAutoSpeakEnabled by ttsManager.isAutoSpeakEnabled.collectAsStateWithLifecycle()

    LaunchedEffect(showHistorySheet) {
        if (showHistorySheet) {
            ttsManager.stop()
        }
    }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    // Auto-scroll to bottom and auto-read aloud AI response if voice is enabled
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
            val lastMsg = chatMessages.last()
            if (lastMsg.sender == MessageSender.COMPANION && isAutoSpeakEnabled) {
                ttsManager.speak(lastMsg.text, lastMsg.id)
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(HxScreenBackground),
        containerColor = HxScreenBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(
                color = HxTopBarBackground,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // বাম পাশে: "HX" ব্র্যান্ড ব্যাজ (থ্রি-ডট আইকন ও নিউ চ্যাট বাটন সম্পূর্ণ হিডেন)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.5f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(enabled = onShowSplashScreen != null) {
                                onShowSplashScreen?.invoke()
                            }
                    ) {
                        Text(
                            text = "HX",
                            color = Color(0xFFA5B4FC),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }

                    // ডান পাশে: ভয়েস মোড টগল বাটন
                    Surface(
                        shape = CircleShape,
                        color = if (isSpeaking) Color(0xFF1D72FE).copy(alpha = 0.3f)
                        else if (isAutoSpeakEnabled) Color(0xFF1E293B)
                        else Color(0xFF1A1A1E),
                        border = BorderStroke(
                            1.dp,
                            if (isSpeaking) Color(0xFF64B5F6)
                            else if (isAutoSpeakEnabled) Color(0xFF1D72FE).copy(alpha = 0.6f)
                            else Color.White.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable {
                                if (isSpeaking) {
                                    ttsManager.stop()
                                } else {
                                    ttsManager.toggleAutoSpeak()
                                }
                            }
                            .testTag("voice_mode_toggle")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Default.StopCircle
                                else if (isAutoSpeakEnabled) Icons.AutoMirrored.Filled.VolumeUp
                                else Icons.AutoMirrored.Filled.VolumeOff,
                                contentDescription = if (isSpeaking) "কথা থামান"
                                else if (isAutoSpeakEnabled) "অটো-ভয়েস চালু"
                                else "ভয়েস চালু করুন",
                                tint = if (isSpeaking) Color(0xFF64B5F6)
                                else if (isAutoSpeakEnabled) Color(0xFF90CAF9)
                                else Color(0xFF8E8E93),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            BottomInputBar(
                inputText = inputText,
                onInputChanged = { text ->
                    if (HxViewModel.isThreeDotsTrigger(text) || HxViewModel.isNewChatTrigger(text)) {
                        ttsManager.stop()
                    }
                    viewModel.onInputTextChanged(text)
                },
                attachedMedia = attachedMedia,
                onMediaSelected = { uri, mediaType, name ->
                    viewModel.attachMedia(uri, mediaType, name)
                },
                onSampleSelected = { mediaType ->
                    viewModel.attachSampleMedia(mediaType)
                },
                onRemoveMedia = { viewModel.clearAttachedMedia() },
                onSendMessage = {
                    ttsManager.stop()
                    viewModel.sendEmpathyMessage()
                },
                isProcessing = isProcessing,
                modifier = Modifier.navigationBarsPadding()
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(HxScreenBackground)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(
                    items = chatMessages,
                    key = { it.id }
                ) { message ->
                        val isThisSpeaking = isSpeaking && currentlySpeakingId == message.id
                        ChatMessageCard(
                            message = message,
                            isSpeaking = isThisSpeaking,
                            onSpeakText = { text ->
                                ttsManager.speak(text, message.id)
                            },
                            onStopSpeak = {
                                ttsManager.stop()
                            }
                        )
                    }

                    if (isProcessing) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xFF26262B),
                                    modifier = Modifier.padding(start = 40.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = HxUserAvatarBlue,
                                            strokeWidth = 2.dp
                                        )
                                        Text(
                                            text = "Human experience process",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFB0B0B8),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

    // ড্রয়ার শিট (স্ক্রিনশটের অপশন ও রিকোয়ারমেন্ট অনুযায়ী)
    if (showHistorySheet) {
        GeminiDrawerSheet(
            sessions = chatSessions,
            currentSessionId = currentSessionId,
            notebooks = notebooks,
            storedEmotionTags = storedEmotionTags,
            onDismiss = { viewModel.closeHistorySheet() },
            onNewChat = {
                viewModel.startNewChat()
                viewModel.closeHistorySheet()
            },
            onSelectSession = { sessionId ->
                viewModel.selectSession(sessionId)
                viewModel.closeHistorySheet()
            },
            onAddNotebook = { title, content -> viewModel.addNotebook(title, content) },
            onClearAllHistory = { viewModel.clearChatHistory() },
            onClearEmotionTags = { viewModel.clearAllEmotionTags() },
            onDeleteSession = { sessionId -> viewModel.deleteSession(sessionId) }
        )
    }
}
