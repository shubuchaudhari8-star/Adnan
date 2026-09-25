package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.ui.screens.HxSplashScreen
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HxSplashScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun splashScreenRendersAndDismissActionWorks() {
        var dismissed = false

        composeTestRule.setContent {
            MyApplicationTheme {
                HxSplashScreen(
                    onSplashFinished = { dismissed = true }
                )
            }
        }

        // Verify splash screen root is present
        composeTestRule.onNodeWithTag("splash_screen").assertExists()

        // Fast-forward full animation sequence and auto-finish delay
        composeTestRule.mainClock.advanceTimeBy(5000L)
        
        // Check if onSplashFinished was invoked
        assertTrue(dismissed)
    }
}
