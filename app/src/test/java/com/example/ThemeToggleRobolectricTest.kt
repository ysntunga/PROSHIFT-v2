package com.example

import android.app.Application
import android.content.Context
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AppThemeMode
import com.example.data.model.ProfessionGroup
import com.example.data.repository.ShiftRepository
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalAppLocale
import com.example.ui.i18n.LocalIsEnglish
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.BrushedMetallicBlueDarkColorScheme
import com.example.ui.theme.BrushedMetallicBlueLightColorScheme
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ProshiftTheme
import com.example.ui.theme.buildDynamicThemePalette
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
class ThemeToggleRobolectricTest {

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
    fun repository_persistsThemeModeAcrossReloads() {
        val repo1 = ShiftRepository(app)
        assertEquals(AppThemeMode.DARK.code, repo1.userSettings.value.themeMode)

        repo1.updateSettings(repo1.userSettings.value.copy(themeMode = AppThemeMode.LIGHT.code))
        assertEquals(AppThemeMode.LIGHT.code, repo1.userSettings.value.themeMode)

        // Fresh repository instance simulates app restart reading from local SharedPreferences
        val repo2 = ShiftRepository(app)
        assertEquals(AppThemeMode.LIGHT.code, repo2.userSettings.value.themeMode)

        repo2.updateSettings(repo2.userSettings.value.copy(themeMode = AppThemeMode.DARK.code))
        val repo3 = ShiftRepository(app)
        assertEquals(AppThemeMode.DARK.code, repo3.userSettings.value.themeMode)
    }

    @Test
    fun buildDynamicThemePalette_providesDistinctContrastingLightAndDarkPalettes() {
        val darkPalette = buildDynamicThemePalette(
            department = "Acil Servis",
            professionGroupId = ProfessionGroup.SAGLIK.id,
            enabled = true,
            isDark = true
        )
        val lightPalette = buildDynamicThemePalette(
            department = "Acil Servis",
            professionGroupId = ProfessionGroup.SAGLIK.id,
            enabled = true,
            isDark = false
        )

        assertTrue(darkPalette.isDark)
        assertFalse(lightPalette.isDark)

        // Dark mode: dark background, light text
        assertTrue(darkPalette.background.luminance() < 0.15f)
        assertTrue(darkPalette.textPrimary.luminance() > 0.7f)

        // Light mode: bright background/cards, dark text
        assertTrue(lightPalette.background.luminance() > 0.75f)
        assertTrue(lightPalette.surfaceCard.luminance() > 0.9f)
        assertTrue(lightPalette.textPrimary.luminance() < 0.15f)
    }

    @Test
    fun settingsScreen_themeToggleUpdatesViewModelAndPersistsLocally() {
        val viewModel = ShiftViewModel(app)
        viewModel.updateAppLanguage(AppLanguage.ENGLISH)

        composeRule.setContent {
            val userSettings by viewModel.userSettings.collectAsState()
            val isDarkTheme = AppThemeMode.fromCode(userSettings.themeMode) == AppThemeMode.DARK

            CompositionLocalProvider(
                LocalIsEnglish provides true,
                LocalAppLocale provides AppLanguage.EN_LOCALE
            ) {
                MyApplicationTheme(
                    department = userSettings.department,
                    professionGroupId = userSettings.professionGroup,
                    dynamicThemeEnabled = userSettings.isOnboardingCompleted,
                    isDarkTheme = isDarkTheme
                ) {
                    SettingsScreen(viewModel = viewModel)
                }
            }
        }

        composeRule.waitForIdle()

        // Default is Dark Mode -> Light switch is OFF
        composeRule.onNodeWithTag("card_theme_mode").assertIsDisplayed()
        composeRule.onNodeWithTag("settings_theme_mode_switch").assertIsDisplayed().assertIsOff()
        composeRule.onNodeWithTag("theme_option_light").assertIsDisplayed()
        composeRule.onNodeWithTag("theme_option_dark").assertIsDisplayed()

        // Toggle switch ON -> Light Mode
        composeRule.onNodeWithTag("settings_theme_mode_switch").performClick()
        composeRule.waitForIdle()

        assertEquals(AppThemeMode.LIGHT.code, viewModel.userSettings.value.themeMode)
        composeRule.onNodeWithTag("settings_theme_mode_switch").assertIsOn()
        assertEquals(AppThemeMode.LIGHT.code, ShiftRepository(app).userSettings.value.themeMode)

        // Click Dark option tile -> switches back to Dark Mode and persists
        composeRule.onNodeWithTag("theme_option_dark").performClick()
        composeRule.waitForIdle()

        assertEquals(AppThemeMode.DARK.code, viewModel.userSettings.value.themeMode)
        composeRule.onNodeWithTag("settings_theme_mode_switch").assertIsOff()
        assertEquals(AppThemeMode.DARK.code, ShiftRepository(app).userSettings.value.themeMode)
    }

    @Test
    fun proshiftTheme_appliesBrushedMetallicBlueMaterial3ColorSchemes() {
        // Static Brushed Metallic Blue M3 ColorScheme verification
        assertTrue(BrushedMetallicBlueDarkColorScheme.background.luminance() < 0.05f)
        assertTrue(BrushedMetallicBlueDarkColorScheme.onBackground.luminance() > 0.75f)
        assertTrue(BrushedMetallicBlueDarkColorScheme.surfaceContainerLowest.luminance() <= BrushedMetallicBlueDarkColorScheme.surfaceContainerHighest.luminance())

        assertTrue(BrushedMetallicBlueLightColorScheme.background.luminance() > 0.8f)
        assertTrue(BrushedMetallicBlueLightColorScheme.surface.luminance() > 0.95f)
        assertTrue(BrushedMetallicBlueLightColorScheme.onBackground.luminance() < 0.1f)
        assertTrue(BrushedMetallicBlueLightColorScheme.primary.blue > BrushedMetallicBlueLightColorScheme.primary.red)

        var darkModeState by mutableStateOf(true)
        var capturedScheme: ColorScheme? = null

        composeRule.setContent {
            ProshiftTheme(
                darkTheme = darkModeState,
                dynamicColor = false,
                dynamicThemeEnabled = false
            ) {
                capturedScheme = MaterialTheme.colorScheme
            }
        }

        composeRule.waitForIdle()
        val darkScheme = requireNotNull(capturedScheme)
        assertEquals(BrushedMetallicBlueDarkColorScheme.background, darkScheme.background)
        assertEquals(BrushedMetallicBlueDarkColorScheme.primary, darkScheme.primary)
        assertEquals(BrushedMetallicBlueDarkColorScheme.surfaceContainer, darkScheme.surfaceContainer)
        assertEquals(BrushedMetallicBlueDarkColorScheme.outline, darkScheme.outline)

        // Switch to Brushed Metallic Blue Light Mode
        darkModeState = false
        composeRule.waitForIdle()

        val lightScheme = requireNotNull(capturedScheme)
        assertEquals(BrushedMetallicBlueLightColorScheme.background, lightScheme.background)
        assertEquals(BrushedMetallicBlueLightColorScheme.surface, lightScheme.surface)
        assertEquals(BrushedMetallicBlueLightColorScheme.primary, lightScheme.primary)
        assertEquals(BrushedMetallicBlueLightColorScheme.onBackground, lightScheme.onBackground)
        assertEquals(BrushedMetallicBlueLightColorScheme.surfaceContainerHigh, lightScheme.surfaceContainerHigh)
    }
}
