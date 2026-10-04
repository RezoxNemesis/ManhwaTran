package com.rezoxnemesis.muse

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MuseLaunchSmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun launchesIntoMuseRootWithoutCrashing() {
        composeRule.waitForIdle()
        composeRule
            .onNode(hasTestTag("MuseRoot"))
            .assertExists()
    }
}
