package com.example

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ColleagueProfile
import com.example.data.model.CyclePreset
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.ShiftViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class QrMatchingNavigationRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun tappingPlusAndQrImportOnMatchingScreenOpensQrScannerAndNavigatesBackWithoutCrashing() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = ShiftViewModel(app)
        viewModel.resetAllData()
        viewModel.completeOnboarding(AppScreen.MATCHING_QR)

        composeTestRule.setContent {
            MyApplicationTheme(dynamicThemeEnabled = true) {
                ProshiftApp(viewModel = viewModel)
            }
        }
        composeTestRule.waitForIdle()

        // Empty state is displayed with no demo colleagues
        assertTrue(viewModel.colleagues.value.isEmpty())
        assertNull(viewModel.selectedColleague.value)
        composeTestRule.onNodeWithTag("empty_colleagues_card").performScrollTo().assertIsDisplayed()

        // Tap top '+' button -> opens QR_SCANNER without crashing
        composeTestRule.onNodeWithTag("btn_add_colleague_qr").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        assertEquals(AppScreen.QR_SCANNER, viewModel.currentScreen.value)
        composeTestRule.onNodeWithTag("tab_kamera_tara").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tab_qr_goster").assertIsDisplayed()
        composeTestRule.onNodeWithTag("camera_viewfinder_box").assertIsDisplayed()

        // Switch to "QR Kodumu Göster" tab without crashing
        composeTestRule.onNodeWithTag("tab_qr_goster").performClick()
        composeTestRule.waitForIdle()

        // Navigate back to MATCHING_QR
        composeTestRule.onNodeWithTag("btn_qr_scanner_back").performClick()
        composeTestRule.waitForIdle()
        assertEquals(AppScreen.MATCHING_QR, viewModel.currentScreen.value)

        // Tap main "QR Kod ile Arkadaş Cihazından İçe Aktar" button -> opens QR_SCANNER cleanly
        composeTestRule.onNodeWithTag("empty_colleagues_card").performScrollTo()
        composeTestRule.onNodeWithTag("btn_open_qr_scanner").performClick()
        composeTestRule.waitForIdle()
        assertEquals(AppScreen.QR_SCANNER, viewModel.currentScreen.value)
    }

    @Test
    fun colleaguePickerShowsEmptyStateAndSupportsDeletingAddedColleague() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = ShiftViewModel(app)
        viewModel.resetAllData()
        viewModel.completeOnboarding(AppScreen.MATCHING_QR)

        composeTestRule.setContent {
            MyApplicationTheme(dynamicThemeEnabled = true) {
                ProshiftApp(viewModel = viewModel)
            }
        }
        composeTestRule.waitForIdle()

        // Open colleague picker when empty
        composeTestRule.onNodeWithTag("btn_select_colleague").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("colleague_picker_empty_text").assertIsDisplayed()

        // Add a real colleague and delete via UI button in picker
        val friend = ColleagueProfile(
            name = "Mert Kaya",
            department = "Üretim",
            cyclePreset = CyclePreset.CYCLE_8_2_2_2,
            referenceStartDate = LocalDate.of(2026, 11, 1),
            qrCodeId = "PRSH-MERT-01"
        )
        composeTestRule.runOnIdle {
            viewModel.addAndSelectColleague(friend)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("btn_delete_colleague_PRSH-MERT-01").performClick()
        composeTestRule.waitForIdle()

        assertTrue(viewModel.colleagues.value.isEmpty())
        assertNull(viewModel.selectedColleague.value)
        composeTestRule.onNodeWithTag("colleague_picker_empty_text").assertIsDisplayed()
    }
}
