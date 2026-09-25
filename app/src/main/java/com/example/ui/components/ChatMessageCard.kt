package com.example.ui.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.ChatMessage
import com.example.data.model.MediaType
import com.example.data.model.MessageSender
import java.text.SimpleDateFormat
import java.util.*

val ChatUserBubbleColor = Color(0xFF1D72FE)
val ChatAiBubbleColor = Color(0xFF26262B)
val ChatTimestampColor = Color(0xFF8E8E93)

@Composable
fun ChatMessageCard(
    message: ChatMessage,
    onSpeakText: ((String) -> Unit)? = null,
    isSpeaking: Boolean = false,
    onStopSpeak: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var isCopied by remember { mutableStateOf(false) }
    var showDetails by remember { mutableStateOf(false) }

    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { timeFormatter.format(Date(message.timestamp)) }

    if (message.sender == MessageSender.USER) {
        // User Message Bubble (Right-aligned, vibrant blue)
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp),
            horizontalAlignment = Alignment.End
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp),
                color = ChatUserBubbleColor,
                modifier = Modifier.widthIn(max = 290.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Attached Media in User Message (Photo / Video / Audio)
                    message.attachedMedia?.let { media ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.2f))
                        ) {
                            when (media.mediaType) {
                                MediaType.IMAGE -> {
                                    val context = LocalContext.current
                                    val imageModel = remember(media.uri, media.base64Data) {
                                        if (media.uri.startsWith("content://") || media.uri.startsWith("file://")) {
                                            Uri.parse(media.uri)
                                        } else if (!media.base64Data.isNullOrBlank()) {
                                            try {
                                                android.util.Base64.decode(media.base64Data, android.util.Base64.DEFAULT)
                                            } catch (e: Exception) {
                                                media.uri
                                            }
                                        } else {
                                            media.uri
                                        }
                                    }
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(imageModel)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = media.displayName ?: "সংযুক্ত ছবি",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                MediaType.VIDEO -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.PlayCircle,
                                                contentDescription = "Video Clip",
                                                tint = Color.White,
                                                modifier = Modifier.size(32.dp)
                                            )
                                            Text(
                                                text = media.displayName ?: "ভিডিও ক্লিপ",
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                                MediaType.AUDIO -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Audiotrack,
                                                contentDescription = "Audio Clip",
                                                tint = Color.White,
                                                modifier = Modifier.size(32.dp)
                                            )
                                            Text(
                                                text = media.displayName ?: "অডিও / ভয়েস বার্তা",
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (message.text.isNotBlank()) {
                        Text(
                            text = message.text,
                            color = Color.White,
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        )
                    }

                    // Extracted ML Emotion Tags (Room-persisted)
                    if (message.extractedEmotionTags.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            message.extractedEmotionTags.take(2).forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.Black.copy(alpha = 0.25f),
                                    border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.35f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Text(
                                            text = "🏷️ ${tag.bengaliLabel}",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "${(tag.confidence * 100).toInt()}%",
                                            color = Color(0xFFFFD54F),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // User Timestamp below the bubble
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = formattedTime,
                color = ChatTimestampColor,
                fontSize = 11.sp,
                modifier = Modifier.padding(end = 4.dp)
            )
        }
    } else {
        // AI / Companion Message Bubble (Left-aligned with Avatar, Dark Charcoal)
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Top
            ) {
                // AI Circular Avatar
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF1E1E24),
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.hx_empathy_logo_1789983171284),
                            contentDescription = "AI Avatar",
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.widthIn(max = 290.dp)) {
                    Surface(
                        shape = RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp),
                        color = ChatAiBubbleColor,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // AI Generated Media Rendering (Images & Videos)
                            message.attachedMedia?.let { media ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.Black.copy(alpha = 0.35f))
                                ) {
                                    when (media.mediaType) {
                                        MediaType.IMAGE -> {
                                            val context = LocalContext.current
                                            val imageModel = remember(media.uri, media.base64Data) {
                                                if (media.uri.startsWith("content://") || media.uri.startsWith("file://")) {
                                                    Uri.parse(media.uri)
                                                } else if (!media.base64Data.isNullOrBlank()) {
                                                    try {
                                                        android.util.Base64.decode(media.base64Data, android.util.Base64.DEFAULT)
                                                    } catch (e: Exception) {
                                                        media.uri
                                                    }
                                                } else {
                                                    media.uri
                                                }
                                            }
                                            Box(modifier = Modifier.fillMaxWidth()) {
                                                AsyncImage(
                                                    model = ImageRequest.Builder(context)
                                                        .data(imageModel)
                                                        .crossfade(true)
                                                        .build(),
                                                    contentDescription = media.displayName,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(200.dp)
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = Color.Black.copy(alpha = 0.7f),
                                                    modifier = Modifier
                                                        .padding(8.dp)
                                                        .align(Alignment.TopStart)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.AutoAwesome,
                                                            contentDescription = null,
                                                            tint = Color(0xFFFFD54F),
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                        Text(
                                                            text = "High Quality AI Image",
                                                            color = Color.White,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        MediaType.VIDEO -> {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(180.dp)
                                                    .background(
                                                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                                            colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                                                        )
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = Color(0xFF1D72FE).copy(alpha = 0.9f),
                                                        modifier = Modifier.size(46.dp)
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Icon(
                                                                imageVector = Icons.Default.PlayArrow,
                                                                contentDescription = "ভিডিও প্লে করুন",
                                                                tint = Color.White,
                                                                modifier = Modifier.size(26.dp)
                                                            )
                                                        }
                                                    }
                                                    Text(
                                                        text = media.displayName,
                                                        color = Color.White,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                        modifier = Modifier.padding(horizontal = 12.dp)
                                                    )
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = Color(0xFF6366F1).copy(alpha = 0.3f),
                                                        border = BorderStroke(0.5.dp, Color(0xFF818CF8))
                                                    ) {
                                                        Text(
                                                            text = "🎬 AI Generated Video (Veo)",
                                                            color = Color(0xFFA5B4FC),
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        MediaType.AUDIO -> {}
                                    }
                                }
                            }

                            Text(
                                text = message.text,
                                color = Color.White,
                                fontSize = 15.sp,
                                lineHeight = 22.sp
                            )

                            // Action icons (TTS & Copy)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (onSpeakText != null) {
                                    if (isSpeaking) {
                                        Text(
                                            text = "পড়া হচ্ছে...",
                                            color = Color(0xFF64B5F6),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(end = 4.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            if (isSpeaking) {
                                                onStopSpeak?.invoke()
                                            } else {
                                                onSpeakText(message.text)
                                            }
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isSpeaking) Icons.Default.StopCircle else Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = if (isSpeaking) "পড়া থামান" else "পড়ে শুনুন",
                                            tint = if (isSpeaking) Color(0xFF64B5F6) else ChatTimestampColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                }

                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(message.text))
                                        isCopied = true
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                        contentDescription = "কপি করুন",
                                        tint = if (isCopied) Color(0xFF4CAF50) else ChatTimestampColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                if (message.emotionResult != null) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = { showDetails = !showDetails },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "বিশ্লেষণ তথ্য",
                                            tint = if (showDetails) Color(0xFF1D72FE) else ChatTimestampColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            // Optional detailed insight dropdown
                            AnimatedVisibility(visible = showDetails) {
                                message.emotionResult?.let { emo ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.Black.copy(alpha = 0.3f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "অনুভূতি: ${emo.primaryEmotion} (${(emo.intensity * 100).toInt()}%)",
                                                color = Color(0xFFFF80AB),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            if (message.extractedEmotionTags.isNotEmpty()) {
                                                Text(
                                                    text = "ML ইমোশন ট্যাগ (Room): " +
                                                        message.extractedEmotionTags.joinToString(", ") { "${it.bengaliLabel} (${(it.confidence * 100).toInt()}%)" },
                                                    color = Color(0xFFA7FFEB),
                                                    fontSize = 11.sp
                                                )
                                            }
                                            emo.visualCues?.let { cues ->
                                                Text(
                                                    text = "মিডিয়া দৃশ্যপট: $cues",
                                                    color = Color(0xFF80D8FF),
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // AI Timestamp below bubble
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = formattedTime,
                        color = ChatTimestampColor,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}
