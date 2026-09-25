package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Schema
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ArchitectureBlueprint
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.EmpathyRose
import com.example.ui.theme.NeuralViolet
import com.example.ui.theme.PrivacyEmerald

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchitectureStudioScreen(modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val clipboardManager = LocalClipboardManager.current
    var copiedIndex by remember { mutableIntStateOf(-1) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "💻 গুগল এআই স্টুডিও ও সিস্টেম আর্কিটেকচার",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "গুগল এআই স্টুডিওর নিখুঁত System Prompt এবং পুরো ফ্লো-টি বাস্তবায়নের জন্য পাইথন (Python) ব্যাকএন্ড কোডের পূর্ণ কাঠামো।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Sub Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("১. System Prompts 📝", fontSize = 13.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("২. Python কোড 🐍", fontSize = 13.sp) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("৩. আর্কিটেকচার ফ্লো 🔄", fontSize = 13.sp) }
            )
        }

        // Content
        when (selectedTab) {
            0 -> {
                // System Prompts Tab
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(ArchitectureBlueprint.SYSTEM_PROMPTS.size) { index ->
                        val template = ArchitectureBlueprint.SYSTEM_PROMPTS[index]
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                        Text(template.icon, fontSize = 20.sp)
                                        Column {
                                            Text(
                                                text = template.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = template.moduleTag,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = NeuralViolet
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(template.systemPrompt))
                                            copiedIndex = index
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (copiedIndex == index) Icons.Default.Check else Icons.Default.ContentCopy,
                                            contentDescription = "Copy Prompt",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Text(
                                    text = template.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "System Prompt (Google AI Studio):",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = EmpathyRose
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = template.systemPrompt,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.background,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "JSON Structured Schema / Example:",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = PrivacyEmerald
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = template.jsonOutputSchema,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Python Backend Tab
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "সম্পূর্ণ পাইথন স্ক্রিপ্ট (ChromaDB + Gemini + RAG):",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        FilledTonalButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(ArchitectureBlueprint.PYTHON_CODE_SNIPPET))
                                copiedIndex = 99
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                if (copiedIndex == 99) Icons.Default.Check else Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (copiedIndex == 99) "কপি হয়েছে!" else "কপি করুন", fontSize = 12.sp)
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCanvas),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        LazyColumn(modifier = Modifier.padding(14.dp)) {
                            item {
                                Text(
                                    text = ArchitectureBlueprint.PYTHON_CODE_SNIPPET,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    color = Color(0xFFE2E8F0)
                                )
                            }
                        }
                    }
                }
            }

            2 -> {
                // Architecture Flow Diagram Tab
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val steps = listOf(
                        Triple("১. User Input (আবেগ প্রকাশ)", "ইউজার তার মনের অনুভূতির কথা লিখবে। যেকোনো ব্যক্তিগত তথ্য অন্তর্ভুক্ত থাকলেও সিস্টেমে পৌঁছাবে।", EmpathyRose),
                        Triple("২. Anonymizer & Privacy Filter 🛡️", "নাম, ফোন নম্বর, স্থান ইত্যাদি স্বয়ংক্রিয়ভাবে [REDACTED_*] ট্যাগ দ্বারা মুছে ফেলে ১০০% পরিচয় গোপন করা হয়।", PrivacyEmerald),
                        Triple("৩. Emotion Extractor 🧠", "Gemini 3.5 Flash JSON structured output দ্বারা মূল আবেগ, তীব্রতা, মনোভাব ও কি-ওয়ার্ড আলাদা করা হয়।", NeuralViolet),
                        Triple("৪. Vector Experience Store (RAG) 🗄️", "Gemini Embedding Model দ্বারা কথাগুলোকে ভেক্টরে রূপান্তর করে ChromaDB/Room-এ কোসাইন সিমিলারিটি সার্চ করা হয়।", Color(0xFFF59E0B)),
                        Triple("৫. Empathic Generation 💬", "মেমোরি ব্যাংক থেকে পাওয়া অতীত ৩-৪টি অভিজ্ঞতা রেফারেন্স হিসেবে নিয়ে মানবিক ও সহানুভূতিশীল সান্ত্বনা তৈরি হয়।", Color(0xFF0EA5E9))
                    )

                    items(steps.size) { i ->
                        val (title, desc, color) = steps[i]
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = color.copy(alpha = 0.15f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${i + 1}",
                                            fontWeight = FontWeight.Bold,
                                            color = color
                                        )
                                    }
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = color
                                    )
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 18.sp
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
