package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.data.model.ChatMessage
import com.example.data.model.ChatSession
import com.example.data.model.MessageSender
import com.example.ui.components.GeminiDrawerSheet
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w420dp-h1200dp")
class GeminiDrawerSheetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun verifyDrawerOptionsAndRecentSessions() {
        var newChatClicked = false
        var dismissClicked = false
        var selectedSessionId: String? = null

        val testSessions = listOf(
            ChatSession(
                id = "s1",
                title = "এআই চ্যাট অ্যাপ UI ডিজাইন",
                messages = listOf(
                    ChatMessage(sender = MessageSender.USER, text = "ডিজাইন নিয়ে বলুন")
                )
            ),
            ChatSession(
                id = "s2",
                title = "আবেগভিত্তিক এআই অ্যাপের নামকরণ ও ...",
                messages = listOf(
                    ChatMessage(sender = MessageSender.USER, text = "নামকরণ কী হবে?")
                )
            ),
            ChatSession(
                id = "s3",
                title = "আত্মিক সম্পর্কের এআই অ্যাপের ধারণা",
                hasUnreadOrActiveDot = true
            ),
            ChatSession(
                id = "s4",
                title = "ইউটিউব ট্রেন্ডিং টপিকসমূহ"
            )
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                GeminiDrawerSheet(
                    sessions = testSessions,
                    currentSessionId = "s1",
                    notebooks = emptyList(),
                    storedEmotionTags = emptyList(),
                    onDismiss = { dismissClicked = true },
                    onNewChat = { newChatClicked = true },
                    onSelectSession = { id -> selectedSessionId = id },
                    onAddNotebook = { _, _ -> },
                    onClearAllHistory = {},
                    onClearEmotionTags = {}
                )
            }
        }

        // 1. Top bar: "Gemini" text removed per user request; close button must exist
        assertTrue(composeTestRule.onAllNodesWithText("Gemini").fetchSemanticsNodes().isEmpty())
        composeTestRule.onNodeWithTag("gemini_drawer_close_button").assertExists()

        // 2. Main menu items
        composeTestRule.onNodeWithText("New chat").assertExists()
        composeTestRule.onNodeWithText("Search chats").assertExists()
        composeTestRule.onNodeWithText("Images").assertExists()
        composeTestRule.onNodeWithText("Videos").assertExists()
        composeTestRule.onNodeWithText("Library").assertExists()

        // 3. Notebooks section
        composeTestRule.onNodeWithText("Notebooks").assertExists()
        composeTestRule.onNodeWithText("New notebook").assertExists()

        // 4. Recents section with individual conversation topics
        composeTestRule.onNodeWithText("Recents").assertExists()
        composeTestRule.onNodeWithText("এআই চ্যাট অ্যাপ UI ডিজাইন").assertExists()
        composeTestRule.onNodeWithText("আবেগভিত্তিক এআই অ্যাপের নামকরণ ও ...").assertExists()
        composeTestRule.onNodeWithText("আত্মিক সম্পর্কের এআই অ্যাপের ধারণা").assertExists()
        composeTestRule.onNodeWithText("ইউটিউব ট্রেন্ডিং টপিকসমূহ").assertExists()

        // 5. User Profile Footer removed per user request
        assertTrue(composeTestRule.onAllNodesWithText("Ttkxfire Adnan").fetchSemanticsNodes().isEmpty())
        assertTrue(composeTestRule.onAllNodesWithText("Upgrade").fetchSemanticsNodes().isEmpty())

        // Test interaction: Clicking a Recent session selects it and dismisses drawer
        composeTestRule.onNodeWithText("আবেগভিত্তিক এআই অ্যাপের নামকরণ ও ...").performClick()
        assertEquals("s2", selectedSessionId)
        assertTrue(dismissClicked)

        // Test interaction: New chat
        composeTestRule.onNodeWithText("New chat").performClick()
        assertTrue(newChatClicked)
    }
}
