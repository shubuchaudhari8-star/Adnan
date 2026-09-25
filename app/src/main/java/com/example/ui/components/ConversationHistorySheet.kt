package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.EmotionTagEntity
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationHistorySheet(
    messages: List<ChatMessage>,
    storedEmotionTags: List<EmotionTagEntity> = emptyList(),
    onDismiss: () -> Unit,
    onClearHistory: () -> Unit,
    onClearEmotionTags: (() -> Unit)? = null,
    onDeleteEmotionTag: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var showConfirmClearChat by remember { mutableStateOf(false) }
    var showConfirmClearTags by remember { mutableStateOf(false) }
    var selectedSheetTab by remember { mutableIntStateOf(0) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1C20),
        contentColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with Red Dot Icon and Clear Option
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Red Dot Icon as requested
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFF3B30),
                        modifier = Modifier.size(16.dp)
                    ) {}

                    Column {
                        Text(
                            text = if (selectedSheetTab == 0) "আগের কথোপকথনের হিস্ট্রি" else "ML ইমোশন ট্যাগ সংগ্রহ (Room DB)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (selectedSheetTab == 0) "আপনার পূর্ববর্তী সকল প্রশ্ন ও আলাপচারিতা" else "টেক্সট থেকে ML ও Coroutines দ্বারা নিষ্কাশিত সংরক্ষিত ট্যাগ",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF8E8E93)
                        )
                    }
                }

                if (selectedSheetTab == 0 && messages.isNotEmpty()) {
                    IconButton(
                        onClick = { showConfirmClearChat = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "হিস্ট্রি মুছুন",
                            tint = Color(0xFFFF453A),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                } else if (selectedSheetTab == 1 && storedEmotionTags.isNotEmpty() && onClearEmotionTags != null) {
                    IconButton(
                        onClick = { showConfirmClearTags = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "সকল ট্যাগ মুছুন",
                            tint = Color(0xFFFF453A),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Tab Selector: Chats vs Stored Emotion Tags
            TabRow(
                selectedTabIndex = selectedSheetTab,
                containerColor = Color(0xFF26262B),
                contentColor = Color.White
            ) {
                Tab(
                    selected = selectedSheetTab == 0,
                    onClick = { selectedSheetTab = 0 },
                    text = { Text("আলাপচারিতা (${messages.size})", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedSheetTab == 1,
                    onClick = { selectedSheetTab = 1 },
                    text = { Text("ইমোশন ট্যাগ (${storedEmotionTags.size})", fontSize = 12.sp) }
                )
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            if (selectedSheetTab == 0) {
                // Chats List
                if (messages.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                tint = Color(0xFF8E8E93),
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                text = "কোনো পূর্ববর্তী কথোপকথন নেই",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF8E8E93)
                            )
                        }
                    }
                } else {
                    val timeFormatter = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(messages.reversed(), key = { it.id }) { msg ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (msg.sender == MessageSender.USER) Color(0xFF1D72FE).copy(alpha = 0.15f) else Color(0xFF26262B),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        clipboardManager.setText(AnnotatedString(msg.text))
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = if (msg.sender == MessageSender.USER) Color(0xFF1D72FE) else Color(0xFF4CAF50),
                                                modifier = Modifier.size(8.dp)
                                            ) {}
                                            Text(
                                                text = if (msg.sender == MessageSender.USER) "আপনি (User)" else "HX",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (msg.sender == MessageSender.USER) Color(0xFF64B5F6) else Color(0xFF81C784)
                                            )
                                        }

                                        Text(
                                            text = timeFormatter.format(Date(msg.timestamp)),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF8E8E93),
                                            fontSize = 10.sp
                                        )
                                    }

                                    if (msg.attachedMedia != null) {
                                        Text(
                                            text = "📎 ${msg.attachedMedia.displayName}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFFFFB74D)
                                        )
                                    }

                                    Text(
                                        text = msg.text,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.9f),
                                        maxLines = 3
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Emotion Tags List (from Room DB)
                if (storedEmotionTags.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tag,
                                contentDescription = null,
                                tint = Color(0xFF8E8E93),
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                text = "কোনো ইমোশন ট্যাগ সংরক্ষিত নেই",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF8E8E93)
                            )
                            Text(
                                text = "চ্যাটে যেকোনো অনুভূতি লিখে পাঠালে ML মডিউল ট্যাগ নিষ্কাশন করে Room ডাটাবেসে সেভ করবে।",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF64B5F6)
                            )
                        }
                    }
                } else {
                    val timeFormatter = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(storedEmotionTags, key = { it.id }) { tag ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF26262B),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFF1D72FE).copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "🏷️ ${tag.bengaliLabel}",
                                                    color = Color(0xFF80D8FF),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFFFFD54F).copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "${(tag.confidence * 100).toInt()}% ML",
                                                    color = Color(0xFFFFD54F),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        if (onDeleteEmotionTag != null) {
                                            IconButton(
                                                onClick = { onDeleteEmotionTag(tag.id) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "ট্যাগ মুছুন",
                                                    tint = Color(0xFF8E8E93),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = "\"${tag.inputSnippet}\"",
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 12.sp,
                                        maxLines = 2
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "ক্যাটাগরি: ${tag.category}",
                                            color = Color(0xFFB0BEC5),
                                            fontSize = 10.sp
                                        )
                                        Text(
                                            text = timeFormatter.format(Date(tag.timestamp)),
                                            color = Color(0xFF8E8E93),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showConfirmClearChat) {
        AlertDialog(
            onDismissRequest = { showConfirmClearChat = false },
            title = { Text("হিস্ট্রি মুছে ফেলতে চান?") },
            text = { Text("আপনার আগের সব কথোপকথন মুছে ফেলা হবে।") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmClearChat = false
                        onClearHistory()
                        onDismiss()
                    }
                ) {
                    Text("হ্যাঁ, মুছুন", color = Color(0xFFFF3B30))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClearChat = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    if (showConfirmClearTags && onClearEmotionTags != null) {
        AlertDialog(
            onDismissRequest = { showConfirmClearTags = false },
            title = { Text("সকল ইমোশন ট্যাগ মুছবেন?") },
            text = { Text("Room ডাটাবেস থেকে সকল নিষ্কাশিত ইমোশন ট্যাগ মুছে ফেলা হবে।") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmClearTags = false
                        onClearEmotionTags()
                    }
                ) {
                    Text("হ্যাঁ, মুছুন", color = Color(0xFFFF3B30))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClearTags = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}
