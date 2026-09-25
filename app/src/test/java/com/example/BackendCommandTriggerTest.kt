package com.example

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import com.example.ui.viewmodel.HxViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackendCommandTriggerTest {

    private lateinit var viewModel: HxViewModel

    @Before
    fun setup() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        viewModel = HxViewModel(app)
    }

    @Test
    fun testThreeDotsCommandTrigger() {
        // Initially history sheet is closed
        viewModel.closeHistorySheet()
        assertFalse(viewModel.showHistorySheet.value)

        // 1. Trigger with literal three dots "..."
        viewModel.onInputTextChanged("...")
        assertTrue("Typing '...' must open three dots history sheet", viewModel.showHistorySheet.value)
        assertEquals("Typing '...' must clear input text", "", viewModel.inputText.value)

        // Close it again
        viewModel.closeHistorySheet()
        assertFalse(viewModel.showHistorySheet.value)

        // 2. Trigger with Bengali "তিনটা ডট"
        viewModel.onInputTextChanged("তিনটা ডট")
        assertTrue("Typing 'তিনটা ডট' must open three dots history sheet", viewModel.showHistorySheet.value)
        assertEquals("Typing 'তিনটা ডট' must clear input text", "", viewModel.inputText.value)

        // Close it again
        viewModel.closeHistorySheet()
        assertFalse(viewModel.showHistorySheet.value)

        // 3. Trigger with Bengali "থ্রিডড"
        viewModel.onInputTextChanged("থ্রিডড")
        assertTrue("Typing 'থ্রিডড' must open three dots history sheet", viewModel.showHistorySheet.value)
        assertEquals("Typing 'থ্রিডড' must clear input text", "", viewModel.inputText.value)
    }

    @Test
    fun testNewChatCommandTrigger() {
        // Populate chat messages first
        viewModel.onInputTextChanged("একটি সাধারণ প্রশ্ন")
        viewModel.sendEmpathyMessage()
        assertTrue("Chat should have messages", viewModel.chatMessages.value.isNotEmpty())
        val oldSessionId = viewModel.currentSessionId.value

        // Type "নিউ চ্যাট"
        viewModel.onInputTextChanged("নিউ চ্যাট")

        assertEquals("Input text should be cleared", "", viewModel.inputText.value)
        assertTrue("Messages should be cleared for fresh new chat", viewModel.chatMessages.value.isEmpty())
        assertNotEquals("A brand new session must be created", oldSessionId, viewModel.currentSessionId.value)
        assertFalse("History sheet should remain closed", viewModel.showHistorySheet.value)

        // Test with English "new chat"
        viewModel.onInputTextChanged("অন্য একটি বার্তা")
        viewModel.sendEmpathyMessage()
        val secondSessionId = viewModel.currentSessionId.value

        viewModel.onInputTextChanged("new chat")
        assertEquals("Input text should be cleared", "", viewModel.inputText.value)
        assertTrue("Messages should be cleared for fresh new chat", viewModel.chatMessages.value.isEmpty())
        assertNotEquals("A brand new session must be created", secondSessionId, viewModel.currentSessionId.value)
    }
}
