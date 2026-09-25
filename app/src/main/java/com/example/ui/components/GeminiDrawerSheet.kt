package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.EmotionTagEntity
import com.example.data.model.ChatSession
import com.example.data.model.NotebookEntry

/**
 * Full-height Gemini drawer sheet matching the screenshot layout exactly:
 * - Top Bar: "Gemini" + "✕" Close icon
 * - Primary Actions: New chat, Search chats, Images, Videos, Library
 * - Notebooks section: "New notebook"
 * - Recents section: Recent chat items with active state pill and blue dot indicator
 * - Bottom Footer: Avatar "T", "Ttkxfire Adnan", "Upgrade" pill button, and Settings gear
 */
@Composable
fun GeminiDrawerSheet(
    sessions: List<ChatSession>,
    currentSessionId: String,
    notebooks: List<NotebookEntry>,
    storedEmotionTags: List<EmotionTagEntity>,
    onDismiss: () -> Unit,
    onNewChat: () -> Unit,
    onSelectSession: (String) -> Unit,
    onAddNotebook: (title: String, content: String) -> Unit,
    onClearAllHistory: () -> Unit,
    onClearEmotionTags: () -> Unit,
    onDeleteSession: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var showNewNotebookDialog by remember { mutableStateOf(false) }
    var showUpgradeDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showLibraryDialog by remember { mutableStateOf(false) }
    var showMediaFilterDialog by remember { mutableStateOf<String?>(null) } // "Images" or "Videos"

    val filteredSessions = remember(searchQuery, sessions) {
        if (searchQuery.isBlank()) {
            sessions
        } else {
            sessions.filter { it.title.contains(searchQuery, ignoreCase = true) }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF0E0E10))
                .testTag("gemini_drawer_sheet"),
            color = Color(0xFF0E0E10),
            contentColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // TOP HEADER: Close button "✕" (Gemini label removed per user request)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("gemini_drawer_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "বন্ধ করুন",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Inline Search Bar (expands when Search chats is tapped)
                AnimatedVisibility(
                    visible = isSearchActive,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        placeholder = { Text("চ্যাট খুঁজুন...", color = Color(0xFF8E8E93)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFF8E8E93)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "মুছুন",
                                        tint = Color(0xFF8E8E93)
                                    )
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF1C1C20),
                            unfocusedContainerColor = Color(0xFF1C1C20),
                            focusedBorderColor = Color(0xFF1D72FE),
                            unfocusedBorderColor = Color(0xFF33333A)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { isSearchActive = false })
                    )
                }

                // SCROLLABLE CONTENT (Primary Options + Notebooks + Recents)
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Item 1: New chat
                    item {
                        DrawerActionItem(
                            icon = Icons.Default.Edit,
                            label = "New chat",
                            onClick = {
                                onNewChat()
                                onDismiss()
                            }
                        )
                    }

                    // Item 2: Search chats
                    item {
                        DrawerActionItem(
                            icon = Icons.Default.Search,
                            label = "Search chats",
                            onClick = {
                                isSearchActive = !isSearchActive
                            }
                        )
                    }

                    // Item 3: Images
                    item {
                        DrawerActionItem(
                            icon = Icons.Default.Image,
                            label = "Images",
                            onClick = {
                                showMediaFilterDialog = "Images"
                            }
                        )
                    }

                    // Item 4: Videos
                    item {
                        DrawerActionItem(
                            icon = Icons.Default.Videocam,
                            label = "Videos",
                            onClick = {
                                showMediaFilterDialog = "Videos"
                            }
                        )
                    }

                    // Item 5: Library
                    item {
                        DrawerActionItem(
                            icon = Icons.Default.Widgets,
                            label = "Library",
                            onClick = {
                                showLibraryDialog = true
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // SECTION: Notebooks
                    item {
                        Text(
                            text = "Notebooks",
                            color = Color(0xFF8E8E93),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    item {
                        DrawerActionItem(
                            icon = Icons.Default.Add,
                            label = "New notebook",
                            onClick = {
                                showNewNotebookDialog = true
                            }
                        )
                    }

                    // Render notebooks if any exist
                    items(notebooks) { nb ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF16161A),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Book,
                                    contentDescription = null,
                                    tint = Color(0xFFA5B4FC),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = nb.title,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // SECTION: Recents
                    item {
                        Text(
                            text = "Recents",
                            color = Color(0xFF8E8E93),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    if (filteredSessions.isEmpty()) {
                        item {
                            Text(
                                text = "কোনো পূর্ববর্তী চ্যাট নেই",
                                color = Color(0xFF52525B),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }

                    // Recents list matching the user requirement
                    items(filteredSessions, key = { it.id }) { session ->
                        val isSelected = session.id == currentSessionId

                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = if (isSelected) Color(0xFF26262B) else Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .clickable {
                                    onSelectSession(session.id)
                                    onDismiss()
                                }
                                .testTag("recent_chat_item_${session.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Chat,
                                        contentDescription = null,
                                        tint = if (isSelected) Color(0xFF64B5F6) else Color(0xFF8E8E93),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = session.title,
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (session.messages.isNotEmpty()) {
                                            Text(
                                                text = "${session.messages.size}টি বার্তা",
                                                color = Color(0xFF71717A),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                if (onDeleteSession != null && session.id != currentSessionId) {
                                    IconButton(
                                        onClick = { onDeleteSession(session.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "চ্যাট মুছুন",
                                            tint = Color(0xFF71717A),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                } else if (session.hasUnreadOrActiveDot) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF1D72FE),
                                        modifier = Modifier.size(8.dp)
                                    ) {}
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
    }

    // DIALOG: New Notebook
    if (showNewNotebookDialog) {
        var nbTitle by remember { mutableStateOf("") }
        var nbContent by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showNewNotebookDialog = false },
            containerColor = Color(0xFF1C1C20),
            titleContentColor = Color.White,
            textContentColor = Color.White,
            title = { Text("নতুন নোটবুক তৈরি করুন") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nbTitle,
                        onValueChange = { nbTitle = it },
                        label = { Text("নোটবুকের শিরোনাম") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF1D72FE),
                            unfocusedBorderColor = Color(0xFF3A3A40)
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = nbContent,
                        onValueChange = { nbContent = it },
                        label = { Text("বিস্তারিত বিবরণ বা আইডিয়া") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF1D72FE),
                            unfocusedBorderColor = Color(0xFF3A3A40)
                        ),
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nbTitle.isNotBlank()) {
                            onAddNotebook(nbTitle, nbContent)
                            showNewNotebookDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D72FE))
                ) {
                    Text("সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewNotebookDialog = false }) {
                    Text("বাতিল", color = Color(0xFF8E8E93))
                }
            }
        )
    }

    // DIALOG: Upgrade
    if (showUpgradeDialog) {
        AlertDialog(
            onDismissRequest = { showUpgradeDialog = false },
            containerColor = Color(0xFF1C1C20),
            titleContentColor = Color.White,
            textContentColor = Color.White,
            icon = {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFFFFD54F),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = { Text("Gemini Advanced / HX Ultra", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("অ্যাডভান্সড প্ল্যানে রয়েছে:", color = Color(0xFFA5B4FC), fontWeight = FontWeight.SemiBold)
                    Text("• দ্রুততম এআই এমপ্যাথি রেসপন্স ও আনলিমিটেড মাল্টিমোডাল বিশ্লেষণ")
                    Text("• উন্নত অন-ডিভাইস প্রাইভেসি ও ফুল এইচডি ভিডিও অনুভূতি ডিটেকশন")
                    Text("• ক্লাউড এবং লোকাল মেমোরি ব্যাংকের দ্বিমুখী সিনক্রোনাইজেশন")
                }
            },
            confirmButton = {
                Button(
                    onClick = { showUpgradeDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D72FE))
                ) {
                    Text("সাবস্ক্রাইব করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpgradeDialog = false }) {
                    Text("পরে দেখব", color = Color(0xFF8E8E93))
                }
            }
        )
    }

    // DIALOG: Library
    if (showLibraryDialog) {
        AlertDialog(
            onDismissRequest = { showLibraryDialog = false },
            containerColor = Color(0xFF1C1C20),
            titleContentColor = Color.White,
            textContentColor = Color.White,
            title = { Text("Gemini Library & Templates") },
            text = {
                Column(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF26262B),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("🧠 এমপ্যাথি ইঞ্জিন টেমপ্লেট", fontWeight = FontWeight.Bold, color = Color(0xFF64B5F6))
                            Text("আবেগপ্রবণ বার্তা বিশ্লেষণ এবং উপযুক্ত সহানুভূতিশীল প্রতিক্রিয়া তৈরির প্রম্পট।", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF26262B),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("🏷️ সংরক্ষিত Room ML ট্যাগ (${storedEmotionTags.size}টি)", fontWeight = FontWeight.Bold, color = Color(0xFFA7FFEB))
                            Text("ব্যবহারকারীর অনুভূতিভিত্তিক নিষ্কাশিত সকল ট্যাগ লোকাল ডাটাবেসে সুরক্ষিত।", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLibraryDialog = false }) {
                    Text("ঠিক আছে", color = Color(0xFF1D72FE))
                }
            }
        )
    }

    // DIALOG: Media Filter (Images / Videos)
    if (showMediaFilterDialog != null) {
        val type = showMediaFilterDialog ?: "Images"
        AlertDialog(
            onDismissRequest = { showMediaFilterDialog = null },
            containerColor = Color(0xFF1C1C20),
            titleContentColor = Color.White,
            textContentColor = Color.White,
            title = { Text("$type গ্যালারি ও ফিল্টার") },
            text = {
                Text(
                    text = "আপনার পূর্ববর্তী চ্যাটে যুক্ত হওয়া সকল $type এবং মিডিয়া বিশ্লেষণ এখানে প্রদর্শিত হবে। নতুন মিডিয়া যুক্ত করতে চ্যাট ইনপুটের ক্যামেরা/গ্যালারি বাটন ব্যবহার করুন।",
                    color = Color.White.copy(alpha = 0.9f)
                )
            },
            confirmButton = {
                Button(
                    onClick = { showMediaFilterDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D72FE))
                ) {
                    Text("ঠিক আছে")
                }
            }
        )
    }

    // DIALOG: Settings
    if (showSettingsDialog) {
        var confirmClearAll by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            containerColor = Color(0xFF1C1C20),
            titleContentColor = Color.White,
            textContentColor = Color.White,
            title = { Text("অ্যাপ সেটিংস") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("অ্যাকাউন্ট: Ttkxfire Adnan", fontWeight = FontWeight.Medium, color = Color(0xFFA5B4FC))
                    Text("সংরক্ষিত ইমোশন ট্যাগ: ${storedEmotionTags.size}টি", color = Color(0xFF8E8E93), fontSize = 13.sp)

                    HorizontalDivider(color = Color(0xFF33333A))

                    Button(
                        onClick = {
                            onClearEmotionTags()
                            showSettingsDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A3A42)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("সকল Room ইমোশন ট্যাগ মুছুন")
                    }

                    Button(
                        onClick = { confirmClearAll = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30).copy(alpha = 0.25f)),
                        border = BorderStroke(1.dp, Color(0xFFFF3B30)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("সম্পূর্ণ চ্যাট হিস্ট্রি রিসেট করুন", color = Color(0xFFFF5252))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("বন্ধ করুন", color = Color(0xFF8E8E93))
                }
            }
        )

        if (confirmClearAll) {
            AlertDialog(
                onDismissRequest = { confirmClearAll = false },
                title = { Text("সব চ্যাট মুছে ফেলতে চান?") },
                text = { Text("আপনার সব অধিবেশন ও কথোপকথন মুছে ফেলা হবে।") },
                confirmButton = {
                    TextButton(onClick = {
                        confirmClearAll = false
                        showSettingsDialog = false
                        onClearAllHistory()
                        onDismiss()
                    }) {
                        Text("হ্যাঁ, মুছুন", color = Color(0xFFFF3B30))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { confirmClearAll = false }) {
                        Text("বাতিল")
                    }
                }
            )
        }
    }
}

@Composable
private fun DrawerActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = label,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
