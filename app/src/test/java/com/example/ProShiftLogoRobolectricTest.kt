package com.example

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.example.ui.components.ProShiftLogo
import com.example.ui.components.ProShiftLogoColors
import com.example.ui.components.ProShiftLogoVariant
import com.example.ui.components.proShiftLogoColors
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ProShiftLogoRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun material3ThemeAwareColors_switchBetweenLightAndDarkSchemes() {
        val isDarkState = mutableStateOf(false)
        var captured: ProShiftLogoColors? = null

        composeRule.setContent {
            MaterialTheme(
                colorScheme = if (isDarkState.value) darkColorScheme() else lightColorScheme()
            ) {
                captured = proShiftLogoColors(isDarkState.value)
                ProShiftLogo(
                    variant = "horizontal",
                    height = 34.dp,
                    modifier = Modifier.testTag("ui_proshift_logo_theme")
                )
            }
        }

        composeRule.waitForIdle()
        assertEquals(Color(0xFF0F172A), captured?.coreRibbon)
        assertEquals(Color(0xFF0891B2), captured?.dayShift)
        assertEquals(Color(0xFF4F46E5), captured?.nightShift)
        composeRule.onNodeWithTag("ui_proshift_logo_theme").assertIsDisplayed()

        // Switch MaterialTheme to Dark ColorScheme
        isDarkState.value = true
        composeRule.waitForIdle()
        assertEquals(Color(0xFFFFFFFF), captured?.coreRibbon)
        assertEquals(Color(0xFF22D3EE), captured?.dayShift)
        assertEquals(Color(0xFF818CF8), captured?.nightShift)
        composeRule.onNodeWithTag("ui_proshift_logo_theme").assertIsDisplayed()
    }

    @Test
    fun proShiftLogo_rendersAllFourVariantsOnCanvas() {
        composeRule.setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Column {
                    ProShiftLogo(
                        variant = "symbol",
                        size = 36.dp,
                        modifier = Modifier.testTag("logo_symbol")
                    )
                    ProShiftLogo(
                        variant = "ring-symbol",
                        size = 36.dp,
                        modifier = Modifier.testTag("logo_ring_symbol")
                    )
                    ProShiftLogo(
                        variant = ProShiftLogoVariant.HORIZONTAL,
                        withRing = true,
                        height = 34.dp,
                        modifier = Modifier.testTag("logo_horizontal")
                    )
                    ProShiftLogo(
                        variant = ProShiftLogoVariant.STACKED,
                        withRing = true,
                        height = 140.dp,
                        modifier = Modifier.testTag("logo_stacked")
                    )
                }
            }
        }

        composeRule.onNodeWithTag("logo_symbol").assertIsDisplayed()
        composeRule.onNodeWithTag("logo_ring_symbol").assertIsDisplayed()
        composeRule.onNodeWithTag("logo_horizontal").assertIsDisplayed()
        composeRule.onNodeWithTag("logo_stacked").assertIsDisplayed()
    }
}
