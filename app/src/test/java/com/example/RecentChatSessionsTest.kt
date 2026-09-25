package com.example

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import com.example.ui.viewmodel.HxViewModel
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecentChatSessionsTest {

    @Test
    fun testSeparateChatSessionsSavedIndependentlyInRecents() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = HxViewModel(app)

        // 1. First conversation: Talk about "আমি একটি নতুন এআই অ্যাপ বানাতে চাই"
        viewModel.startNewChat("প্রথম আলোচনা")
        val session1Id = viewModel.currentSessionId.value

        // Simulate sending a message in session 1
        viewModel.onInputTextChanged("আমি একটি নতুন এআই অ্যাপ বানাতে চাই")
        viewModel.sendEmpathyMessage()

        val session1 = viewModel.chatSessions.value.find { it.id == session1Id }
        assertNotNull("Session 1 should exist in Recents", session1)
        assertTrue("Session 1 should have messages", session1!!.messages.isNotEmpty())

        // 2. Click 'New chat' to start a completely new session
        viewModel.startNewChat("দ্বিতীয় আলোচনা")
        val session2Id = viewModel.currentSessionId.value
        assertNotEquals("New session must have a different ID", session1Id, session2Id)
        assertTrue("New chat screen should be empty", viewModel.chatMessages.value.isEmpty())

        // Simulate sending a message in session 2
        viewModel.onInputTextChanged("আজকের আবহাওয়া কেমন?")
        viewModel.sendEmpathyMessage()

        val session2 = viewModel.chatSessions.value.find { it.id == session2Id }
        assertNotNull("Session 2 should exist in Recents", session2)
        assertTrue("Session 2 should have messages", session2!!.messages.isNotEmpty())

        // 3. Verify user can switch back to session 1 and retrieve all past messages
        viewModel.selectSession(session1Id)
        assertEquals("Current session ID should be session 1", session1Id, viewModel.currentSessionId.value)
        val loadedMessages1 = viewModel.chatMessages.value
        assertTrue("Session 1 messages must be restored", loadedMessages1.any { it.text.contains("এআই অ্যাপ") })

        // 4. Verify user can switch to session 2 and retrieve its messages
        viewModel.selectSession(session2Id)
        assertEquals("Current session ID should be session 2", session2Id, viewModel.currentSessionId.value)
        val loadedMessages2 = viewModel.chatMessages.value
        assertTrue("Session 2 messages must be restored", loadedMessages2.any { it.text.contains("আবহাওয়া") })
    }
}
