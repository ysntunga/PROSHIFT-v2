package com.example

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.data.engine.QrCodeCodec
import com.example.data.engine.TimesheetPdfExporter
import com.example.data.repository.ShiftRepository
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.ShiftViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PdfPreviewAndExportRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun generatesValidPdfWithQrAndBarcodeAndFileProviderUriFor31DayMonth() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = ShiftRepository(context)
        repo.resetToDefaults()
        val settings = repo.userSettings.value
        val monthDays = repo.getMonthDays(2026, 10, settings) // 31 days
        val stats = repo.calculateTimesheetStats(2026, 10, settings)

        assertEquals(31, monthDays.size)
        assertNotNull(QrCodeCodec.generateBarcode128Bitmap("PRSH-2026-10-4857"))

        val trResult = TimesheetPdfExporter.generateTimesheetPdf(
            context = context,
            settings = settings,
            stats = stats,
            monthDays = monthDays,
            selectedYear = 2026,
            selectedMonth = 10,
            monthTitleLocalized = "Ekim 2026",
            includeNotes = true,
            includeBarcode = true,
            includeQr = true,
            isEn = false,
            qrPayload = "PROSHIFT|PRSH-2026-10-4857",
            saveToDownloads = false
        )

        assertNotNull(trResult)
        val generated = trResult!!
        assertEquals("PROSHIFT_Puantaj_2026-10.pdf", generated.fileName)
        assertTrue(generated.cacheFile.exists())
        assertTrue(generated.sizeBytes > 0L)
        assertTrue(generated.pageCount >= 1)
        val trHeader = generated.cacheFile.readBytes().take(5).toByteArray().decodeToString()
        assertEquals("%PDF-", trHeader)

        // Verify FileProvider produces a valid content:// URI
        val shareUri = TimesheetPdfExporter.getShareablePdfUri(context, generated.cacheFile)
        assertNotNull(shareUri)
        assertTrue(shareUri.toString().startsWith("content://${context.packageName}.fileprovider/"))

        // Verify English export filename too
        val enResult = TimesheetPdfExporter.generateTimesheetPdf(
            context = context,
            settings = settings,
            stats = stats,
            monthDays = monthDays,
            selectedYear = 2026,
            selectedMonth = 10,
            monthTitleLocalized = "October 2026",
            includeNotes = true,
            includeBarcode = false,
            includeQr = false,
            isEn = true,
            qrPayload = "PROSHIFT|PRSH-2026-10-4857",
            saveToDownloads = false
        )
        assertNotNull(enResult)
        assertEquals("PROSHIFT_Timesheet_2026-10.pdf", enResult!!.fileName)
        assertTrue(enResult.cacheFile.exists())
        assertTrue(enResult.sizeBytes > 0L)
        val enHeader = enResult.cacheFile.readBytes().take(5).toByteArray().decodeToString()
        assertEquals("%PDF-", enHeader)
    }

    @Test
    fun pdfPreviewSheetControlsToggleQrBarcodeZoomAndGeneratePdfCleanly() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = ShiftViewModel(app)
        viewModel.resetAllData()
        viewModel.completeOnboarding(AppScreen.TIMESHEET)
        viewModel.openPdfPreviewDialog()

        composeTestRule.setContent {
            MyApplicationTheme(dynamicThemeEnabled = true) {
                ProshiftApp(viewModel = viewModel)
            }
        }
        composeTestRule.waitForIdle()

        // Preview sheet and default QR code image are shown
        composeTestRule.onNodeWithTag("pdf_preview_sheet").assertIsDisplayed()
        composeTestRule.onNodeWithTag("pdf_preview_qr_image").performScrollTo().assertIsDisplayed()

        // Toggle Barcode ON -> real Code-128 barcode preview image appears
        composeTestRule.onNodeWithTag("pdf_toggle_barcode").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("pdf_preview_barcode_image").performScrollTo().assertIsDisplayed()

        // Zoom controls work accurately (100% -> 125% -> 100% -> 75%)
        composeTestRule.onNodeWithTag("pdf_zoom_value").performScrollTo().assertTextEquals("100%")
        composeTestRule.onNodeWithTag("pdf_zoom_in").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("pdf_zoom_value").assertTextEquals("125%")
        composeTestRule.onNodeWithTag("pdf_zoom_out").performClick()
        composeTestRule.onNodeWithTag("pdf_zoom_out").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("pdf_zoom_value").assertTextEquals("75%")

        // Generate & Save PDF button creates the PDF and shows the Ready Banner with Open button
        composeTestRule.onNodeWithTag("btn_download_pdf").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("pdf_ready_banner").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_open_generated_pdf").performScrollTo().assertIsDisplayed()
    }
}
