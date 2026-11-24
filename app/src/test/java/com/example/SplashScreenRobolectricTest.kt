package com.example

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.ShiftRepository
import com.example.ui.components.ProShiftBrandHeader
import com.example.ui.components.ProShiftLogo
import com.example.ui.components.ProShiftLogoVariant
import com.example.ui.components.buildProShiftLogoImageVector
import com.example.ui.components.proShiftLogoColors
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.ProshiftTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.ShiftViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SplashScreenRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var app: Application

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        app.getSharedPreferences(ShiftRepository.PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun proShiftLogoColors_matchExactOfficialDarkAndLightTokens() {
        val dark = proShiftLogoColors(isDark = true)
        assertEquals(Color(0xFFFFFFFF), dark.coreRibbon)
        assertEquals(Color(0xFF22D3EE), dark.dayShift)
        assertEquals(Color(0xFF818CF8), dark.nightShift)

        val light = proShiftLogoColors(isDark = false)
        assertEquals(Color(0xFF0F172A), light.coreRibbon)
        assertEquals(Color(0xFF0891B2), light.dayShift)
        assertEquals(Color(0xFF4F46E5), light.nightShift)
    }

    @Test
    fun proShiftLogoVectors_buildAllVariantsWithExactViewBoxesAndNoBackgroundRect() {
        assertEquals(ProShiftLogoVariant.SYMBOL, ProShiftLogoVariant.fromKey("symbol"))
        assertEquals(ProShiftLogoVariant.RING_SYMBOL, ProShiftLogoVariant.fromKey("ring-symbol"))
        assertEquals(ProShiftLogoVariant.HORIZONTAL, ProShiftLogoVariant.fromKey("horizontal"))
        assertEquals(ProShiftLogoVariant.STACKED, ProShiftLogoVariant.fromKey("stacked"))

        val symbol = buildProShiftLogoImageVector(ProShiftLogoVariant.SYMBOL, isDarkTheme = true)
        assertEquals(256f, symbol.viewportWidth)
        assertEquals(256f, symbol.viewportHeight)
        assertEquals(3, symbol.root.size)

        val ringSymbol = buildProShiftLogoImageVector(ProShiftLogoVariant.RING_SYMBOL, isDarkTheme = false)
        assertEquals(256f, ringSymbol.viewportWidth)
        assertEquals(256f, ringSymbol.viewportHeight)
        assertEquals(3, ringSymbol.root.size)

        val horizontal = buildProShiftLogoImageVector(ProShiftLogoVariant.HORIZONTAL, withRing = true, isDarkTheme = true)
        assertEquals(715f, horizontal.viewportWidth)
        assertEquals(256f, horizontal.viewportHeight)
        assertEquals(5, horizontal.root.size)

        val stacked = buildProShiftLogoImageVector(ProShiftLogoVariant.STACKED, withRing = true, isDarkTheme = false)
        assertEquals(480f, stacked.viewportWidth)
        assertEquals(336f, stacked.viewportHeight)
        assertEquals(2, stacked.root.size)
    }

    @Test
    fun proShiftLogo_rendersAndReactsToLiveThemeToggle() {
        val darkThemeState = mutableStateOf(true)

        composeRule.setContent {
            ProshiftTheme(darkTheme = darkThemeState.value) {
                ProShiftLogo(
                    variant = ProShiftLogoVariant.STACKED,
                    withRing = true,
                    height = 140.dp,
                    modifier = Modifier.testTag("test_stacked_logo")
                )
                ProShiftBrandHeader(
                    sectionLabel = "ANA SAYFA"
                )
            }
        }

        composeRule.onNodeWithTag("test_stacked_logo").assertIsDisplayed()
        composeRule.onNodeWithTag("proshift_brand_header").assertIsDisplayed()

        // Toggle Light Mode instantaneously without remount
        darkThemeState.value = false
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("test_stacked_logo").assertIsDisplayed()
        composeRule.onNodeWithTag("proshift_brand_header").assertIsDisplayed()
    }

    @Test
    fun splashScreen_displaysLogoAndInvokesFinishAfterFadeIn() {
        var finished = false
        composeRule.mainClock.autoAdvance = false

        composeRule.setContent {
            ProshiftTheme(darkTheme = true) {
                SplashScreen(
                    onSplashFinished = { finished = true },
                    fadeInDurationMillis = 500,
                    holdDurationMillis = 400L
                )
            }
        }

        // Initial composition shows splash screen and transparent stacked logo before completion
        composeRule.mainClock.advanceTimeBy(250L)
        composeRule.onNodeWithTag("splash_screen_root").assertIsDisplayed()
        composeRule.onNodeWithTag("splash_app_logo").assertIsDisplayed()
        composeRule.onNodeWithTag("splash_app_tagline").assertIsDisplayed()
        assertFalse(finished)

        // Advance past fade-in + hold duration
        composeRule.mainClock.advanceTimeBy(800L)
        composeRule.waitForIdle()
        assertTrue(finished)
    }

    @Test
    fun proshiftApp_showsSplashBeforeMainContentThenTransitions() {
        val viewModel = ShiftViewModel(app)
        viewModel.completeOnboarding(AppScreen.HOME)
        composeRule.mainClock.autoAdvance = false

        composeRule.setContent {
            ProshiftTheme(darkTheme = false) {
                ProshiftApp(
                    viewModel = viewModel,
                    showSplashOnLaunch = true
                )
            }
        }

        // Splash screen is visible immediately on launch before main content
        composeRule.mainClock.advanceTimeBy(300L)
        composeRule.onNodeWithTag("splash_screen_root").assertIsDisplayed()
        composeRule.onNodeWithTag("splash_app_logo").assertIsDisplayed()
        assertTrue(composeRule.onAllNodesWithTag("nav_home").fetchSemanticsNodes().isEmpty())

        // After splash fade-in + hold + crossfade, splash disappears and main content is shown
        composeRule.mainClock.advanceTimeBy(2000L)
        composeRule.waitForIdle()
        assertTrue(composeRule.onAllNodesWithTag("splash_screen_root").fetchSemanticsNodes().isEmpty())
        composeRule.onNodeWithTag("nav_home").assertIsDisplayed()
    }

    @Test
    fun splashScreen_adaptsToLightAndDarkMaterial3Themes() {
        val darkThemeState = mutableStateOf(false)
        composeRule.mainClock.autoAdvance = false

        composeRule.setContent {
            ProshiftTheme(darkTheme = darkThemeState.value) {
                SplashScreen(
                    onSplashFinished = {},
                    fadeInDurationMillis = 300,
                    holdDurationMillis = 10_000L
                )
            }
        }

        // Light theme splash renders stacked logo and tagline
        composeRule.mainClock.advanceTimeBy(350L)
        composeRule.onNodeWithTag("splash_screen_root").assertIsDisplayed()
        composeRule.onNodeWithTag("splash_app_logo").assertIsDisplayed()
        composeRule.onNodeWithTag("splash_app_tagline").assertIsDisplayed()

        // Switch to Dark theme dynamically
        darkThemeState.value = true
        composeRule.mainClock.advanceTimeBy(100L)
        composeRule.onNodeWithTag("splash_screen_root").assertIsDisplayed()
        composeRule.onNodeWithTag("splash_app_logo").assertIsDisplayed()
        composeRule.onNodeWithTag("splash_app_tagline").assertIsDisplayed()
    }
}
