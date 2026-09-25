package com.example.ui.components

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathNode
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttachedMedia
import com.example.data.model.MediaType

val HxPrimaryBlue = Color(0xFF1D72FE)
val HxInputBackground = Color(0xFF242429)
val HxTextPlaceholder = Color(0xFF8E8E93)
val HxSurfaceDark = Color(0xFF18181C)

private val UpwardArrowIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "UpwardArrow",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).addPath(
        pathData = listOf(
            PathNode.MoveTo(4f, 12f),
            PathNode.LineTo(5.41f, 13.41f),
            PathNode.LineTo(11f, 7.83f),
            PathNode.VerticalTo(20f),
            PathNode.HorizontalTo(13f),
            PathNode.VerticalTo(7.83f),
            PathNode.LineTo(18.59f, 13.42f),
            PathNode.LineTo(20f, 12f),
            PathNode.LineTo(12f, 4f),
            PathNode.Close
        ),
        fill = SolidColor(Color.White)
    ).build()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomInputBar(
    inputText: String,
    onInputChanged: (String) -> Unit,
    attachedMedia: AttachedMedia?,
    onMediaSelected: (Uri, MediaType, String?) -> Unit,
    onRemoveMedia: () -> Unit,
    onSendMessage: () -> Unit,
    isProcessing: Boolean,
    onSampleSelected: ((MediaType) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showMediaSheet by remember { mutableStateOf(false) }

    // 1. Photo Picker Launcher (Zero-permission Android Photo Picker & fallback)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onMediaSelected(uri, MediaType.IMAGE, "ছবি")
        }
    }
    val photoFallbackLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onMediaSelected(uri, MediaType.IMAGE, "ছবি")
        }
    }

    // 2. Video Picker Launcher
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onMediaSelected(uri, MediaType.VIDEO, "ভিডিও ক্লিপ")
        }
    }
    val videoFallbackLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onMediaSelected(uri, MediaType.VIDEO, "ভিডিও ক্লিপ")
        }
    }

    // 3. Audio Picker Launcher (Standard document/audio picker)
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onMediaSelected(uri, MediaType.AUDIO, "অডিও ক্লিপ")
        }
    }

    // Speech Recognition Launcher
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                val updated = if (inputText.isBlank()) spokenText else "$inputText $spokenText"
                onInputChanged(updated)
                Toast.makeText(context, "ভয়েস গ্রহণ করা হয়েছে", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Microphone Permission Launcher
    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "আপনার মনের অনুভূতি বা প্রশ্ন বলুন...")
                }
                speechRecognizerLauncher.launch(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "ভয়েস ইনপুট সমর্থিত নয়", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "ভয়েস ইনপুটের জন্য মাইক্রোফোন অনুমতি দিন", Toast.LENGTH_SHORT).show()
        }
    }

    fun startVoiceInput() {
        recordAudioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(HxSurfaceDark)
            .testTag("bottom_input_bar_container")
    ) {
        // Media Preview Area if user has attached any photo, video, or audio
        MediaPreviewBar(
            attachedMedia = attachedMedia,
            onRemoveMedia = onRemoveMedia
        )

        // New Unified Input Box Container (Dark rounded card with internal multiline field & bottom action buttons)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF20232D))
                .border(1.dp, Color(0xFF343948), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 1. শীর্ষের মাল্টিলাইন টেক্সট ইনপুট এরিয়া
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .defaultMinSize(minHeight = 36.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    if (inputText.isEmpty()) {
                        Text(
                            text = "মনের অনুভূতি বা প্রশ্ন লিখুন...",
                            color = Color(0xFF8E92A0),
                            fontSize = 15.sp,
                            lineHeight = 20.sp
                        )
                    }
                    BasicTextField(
                        value = inputText,
                        onValueChange = onInputChanged,
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 15.sp,
                            lineHeight = 20.sp
                        ),
                        cursorBrush = SolidColor(HxPrimaryBlue),
                        minLines = 1,
                        maxLines = 6,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("chat_input_text_field")
                    )
                }

                // 2. নিচের বাটন রো (বামে প্লাস, ডানে মাইক এবং সেন্ড বাটন)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 10.dp, bottom = 10.dp, top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // বাম পাশে: প্লাস বাটন (+) মিডিয়া অ্যাটাচমেন্টের জন্য
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF2C303E),
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable { showMediaSheet = true }
                            .testTag("plus_media_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "ফটো, ভিডিও, বা অডিও যুক্ত করুন",
                                tint = Color(0xFFE2E8F0),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // ডান পাশে: ভয়েস/মাইক বাটন এবং সেন্ড (↑) বাটন
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // মাইক বাটন (🎙️)
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF2C303E),
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                            .clickable { startVoiceInput() }
                            .testTag("microphone_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "মুখে বলে পাঠাতে ক্লিক করুন",
                                    tint = Color(0xFFE2E8F0),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // সেন্ড বাটন (↑)
                        val canSend = (inputText.isNotBlank() || attachedMedia != null) && !isProcessing
                        Surface(
                            shape = CircleShape,
                            color = if (canSend) HxPrimaryBlue else Color(0xFF2A2D3A),
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .clickable(enabled = canSend) { onSendMessage() }
                                .testTag("send_message_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isProcessing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = UpwardArrowIcon,
                                        contentDescription = "পাঠান",
                                        tint = if (canSend) Color.White else Color(0xFF6B7280),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // প্লাস বাটনে ক্লিক করলে ৩টি অপশন: ফটো, ভিডিও, অডিও
    if (showMediaSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMediaSheet = false },
            containerColor = Color(0xFF1E1E24),
            contentColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "সংযুক্তি নির্বাচন করুন",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "আপনার অনুভূতি গভীরভাবে বুঝতে ফটো, ভিডিও বা অডিও ক্লিপ ইমপোর্ট করুন:",
                    style = MaterialTheme.typography.bodySmall,
                    color = HxTextPlaceholder
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // ১. ফটো (Photo) অপশন
                    MediaOptionItem(
                        icon = Icons.Default.Image,
                        title = "ফটো",
                        subTitle = "Photo",
                        badgeColor = Color(0xFF388E3C),
                        onClick = {
                            showMediaSheet = false
                            try {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            } catch (e: Exception) {
                                photoFallbackLauncher.launch("image/*")
                            }
                        }
                    )

                    // ২. ভিডিও (Video) অপশন
                    MediaOptionItem(
                        icon = Icons.Default.Videocam,
                        title = "ভিডিও",
                        subTitle = "Video",
                        badgeColor = Color(0xFF8E24AA),
                        onClick = {
                            showMediaSheet = false
                            try {
                                videoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            } catch (e: Exception) {
                                videoFallbackLauncher.launch("video/*")
                            }
                        }
                    )

                    // ৩. অডিও (Audio) অপশন
                    MediaOptionItem(
                        icon = Icons.Default.Audiotrack,
                        title = "অডিও",
                        subTitle = "Audio",
                        badgeColor = Color(0xFFE65100),
                        onClick = {
                            showMediaSheet = false
                            try {
                                audioPickerLauncher.launch("audio/*")
                            } catch (e: Exception) {
                                Toast.makeText(context, "অডিও নির্বাচক খোলা সম্ভব হয়নি", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "তাৎক্ষণিক টেস্ট অপশন (Quick Test Samples):",
                        style = MaterialTheme.typography.labelSmall,
                        color = HxTextPlaceholder
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SuggestionChip(
                            onClick = {
                                showMediaSheet = false
                                onSampleSelected?.invoke(MediaType.IMAGE)
                            },
                            label = { Text("🖼️ নমুনা ফটো", color = Color.White, fontSize = 12.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFF26262B))
                        )
                        SuggestionChip(
                            onClick = {
                                showMediaSheet = false
                                onSampleSelected?.invoke(MediaType.VIDEO)
                            },
                            label = { Text("🎥 নমুনা ভিডিও", color = Color.White, fontSize = 12.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFF26262B))
                        )
                        SuggestionChip(
                            onClick = {
                                showMediaSheet = false
                                onSampleSelected?.invoke(MediaType.AUDIO)
                            },
                            label = { Text("🎵 নমুনা অডিও", color = Color.White, fontSize = 12.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFF26262B))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subTitle: String,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = badgeColor.copy(alpha = 0.2f),
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = badgeColor,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = subTitle,
                style = MaterialTheme.typography.labelSmall,
                color = HxTextPlaceholder
            )
        }
    }
}
