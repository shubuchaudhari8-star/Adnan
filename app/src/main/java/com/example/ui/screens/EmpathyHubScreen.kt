package com.example.ui.screens

import android.net.Uri
import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttachedMedia
import com.example.data.model.ChatMessage
import com.example.tts.TtsManager
import com.example.ui.components.BottomInputBar
import com.example.ui.components.ChatMessageCard
import com.example.ui.theme.EmpathyRose
import java.util.*

@Composable
fun EmpathyHubScreen(
    inputText: String,
    onInputChanged: (String) -> Unit,
    attachedMedia: AttachedMedia?,
    onMediaSelected: (Uri) -> Unit,
    onRemoveMedia: () -> Unit,
    isProcessing: Boolean,
    chatMessages: List<ChatMessage>,
    onSendMessage: () -> Unit,
    onLoadSample: (String) -> Unit,
    onClearChatHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()

    // TextToSpeech manager for reading responses aloud
    val ttsManager = remember { TtsManager(context) }
    DisposableEffect(ttsManager) {
        onDispose {
            ttsManager.release()
        }
    }

    val sampleScenarios = listOf(
        "আজকে অফিসে বসের অপ্রত্যাশিত বকুনি খেলাম, খুব একা লাগছে...",
        "দীর্ঘদিনের সম্পর্কটা হুট করে শেষ হয়ে গেল, নিঃশ্বাস ভারী হয়ে আসছে...",
        "নতুন শহরে কাউকে চিনি না, সন্ধ্যায় ঘরে বসে কান্না পাচ্ছে...",
        "Rejected after 3 interview rounds, losing faith in myself..."
    )

    // Auto-scroll to the bottom when new message arrives
    LaunchedEffect(chatMessages.size, isProcessing) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("empathy_hub_screen")
    ) {
        // Quick Starters Header (Horizontal Scroll)
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "নমুনা:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                sampleScenarios.forEach { sample ->
                    SuggestionChip(
                        onClick = { onLoadSample(sample) },
                        label = { Text(sample.take(24) + "...", fontSize = 11.sp) },
                        modifier = Modifier.height(28.dp)
                    )
                }

                if (chatMessages.size > 2) {
                    IconButton(
                        onClick = onClearChatHistory,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.DeleteSweep,
                            contentDescription = "Clear Chat",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // মেসেজ ও রেসপন্স স্ট্রিম (Main Feed Area) 💬
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .testTag("chat_messages_stream"),
            contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chatMessages, key = { it.id }) { msg ->
                ChatMessageCard(
                    message = msg,
                    onSpeakText = { textToSpeak ->
                        ttsManager.speak(textToSpeak, msg.id)
                    },
                    onStopSpeak = {
                        ttsManager.stop()
                    }
                )
            }

            // Processing Indicator Card
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
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.padding(start = 44.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = EmpathyRose,
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "আপনার অনুভূতি ও সংকেত গভীর সহমর্মিতায় অনুধাবন করা হচ্ছে...",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // ইনপুট বার (Bottom Input Bar) ⌨️ with Media Preview Area 🔍
        BottomInputBar(
            inputText = inputText,
            onInputChanged = onInputChanged,
            attachedMedia = attachedMedia,
            onMediaSelected = { uri, _, _ -> onMediaSelected(uri) },
            onRemoveMedia = onRemoveMedia,
            onSendMessage = onSendMessage,
            isProcessing = isProcessing
        )
    }
}
