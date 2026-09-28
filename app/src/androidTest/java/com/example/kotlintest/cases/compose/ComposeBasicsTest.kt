package com.example.kotlintest.cases.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class ComposeBasicsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun clickIncrementsCounter() {
        composeTestRule.setContent {
            var count by remember { mutableStateOf(0) }
            Column {
                Text(text = "$count", modifier = Modifier.testTag("counterText"))
                Button(onClick = { count++ }, modifier = Modifier.testTag("counterButton")) {
                    Text("+1")
                }
            }
        }
        composeTestRule.onNodeWithTag("counterButton").performClick()
        composeTestRule.onNodeWithTag("counterText").assertTextEquals("1")
    }
}
