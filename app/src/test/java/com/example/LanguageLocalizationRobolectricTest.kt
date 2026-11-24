package com.example

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.ShiftRepository
import com.example.ui.i18n.AppLanguage
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.ShiftViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LanguageLocalizationRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun appLanguageResolvesSystemLocaleAndManualOverridesCorrectly() {
        val trLocale = Locale.forLanguageTag("tr-TR")
        val enLocale = Locale.US
        val deLocale = Locale.GERMANY

        // Automatic / system mode
        assertFalse(AppLanguage.resolveIsEnglish(AppLanguage.SYSTEM.code, trLocale))
        assertEquals(AppLanguage.TR_LOCALE, AppLanguage.resolveLocale(AppLanguage.SYSTEM.code, trLocale))

        assertTrue(AppLanguage.resolveIsEnglish(AppLanguage.SYSTEM.code, enLocale))
        assertEquals(AppLanguage.EN_LOCALE, AppLanguage.resolveLocale(AppLanguage.SYSTEM.code, enLocale))

        assertTrue(AppLanguage.resolveIsEnglish(AppLanguage.SYSTEM.code, deLocale))

        // Manual overrides ignore system locale
        assertFalse(AppLanguage.resolveIsEnglish(AppLanguage.TURKISH.code, enLocale))
        assertTrue(AppLanguage.resolveIsEnglish(AppLanguage.ENGLISH.code, trLocale))

        // Unknown / blank code safely defaults to SYSTEM
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromCode(null))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromCode("unknown"))
    }

    @Test
    fun repositoryPersistsBacksUpAndResetsAppLanguagePreference() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = ShiftRepository(context)
        repo.resetToDefaults()

        // Default on fresh install is system automatic
        assertEquals(AppLanguage.SYSTEM.code, repo.userSettings.value.appLanguage)

        // Save English and reload from fresh repository instance
        val updated = repo.userSettings.value.copy(appLanguage = AppLanguage.ENGLISH.code)
        repo.updateSettings(updated)
        assertEquals(AppLanguage.ENGLISH.code, ShiftRepository(context).userSettings.value.appLanguage)

        // Full JSON backup & restore preserves appLanguage
        val backupJson = repo.exportFullBackupJson()
        repo.updateSettings(updated.copy(appLanguage = AppLanguage.TURKISH.code))
        assertEquals(AppLanguage.TURKISH.code, repo.userSettings.value.appLanguage)

        val imported = repo.importFullBackupJson(backupJson)
        assertTrue(imported)
        assertEquals(AppLanguage.ENGLISH.code, repo.userSettings.value.appLanguage)

        // Reset restores automatic system language
        repo.resetToDefaults()
        assertEquals(AppLanguage.SYSTEM.code, repo.userSettings.value.appLanguage)
    }

    @Test
    fun settingsLanguageSelectorSwitchesUiLiveBetweenEnglishTurkishAndSystem() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = ShiftViewModel(app)
        viewModel.resetAllData()
        viewModel.completeOnboarding(AppScreen.SETTINGS)

        composeTestRule.setContent {
            MyApplicationTheme(dynamicThemeEnabled = true) {
                ProshiftApp(viewModel = viewModel)
            }
        }
        composeTestRule.waitForIdle()

        assertEquals(AppLanguage.SYSTEM.code, viewModel.userSettings.value.appLanguage)
        composeTestRule.onNodeWithTag("card_app_language").performScrollTo().assertIsDisplayed()

        // Switch to English -> UI updates immediately
        composeTestRule.onNodeWithTag("lang_option_en").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        assertEquals(AppLanguage.ENGLISH.code, viewModel.userSettings.value.appLanguage)
        composeTestRule.onNodeWithText("SETTINGS").assertIsDisplayed()
        composeTestRule.onNodeWithText("Home").assertIsDisplayed()

        // Switch to Turkish -> UI updates immediately
        composeTestRule.onNodeWithTag("lang_option_tr").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        assertEquals(AppLanguage.TURKISH.code, viewModel.userSettings.value.appLanguage)
        composeTestRule.onNodeWithText("AYARLAR").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ana Sayfa").assertIsDisplayed()

        // Switch back to Automatic (System)
        composeTestRule.onNodeWithTag("lang_option_system").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        assertEquals(AppLanguage.SYSTEM.code, viewModel.userSettings.value.appLanguage)
    }
}
