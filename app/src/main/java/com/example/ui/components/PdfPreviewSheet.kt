package com.example.ui.components

import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.engine.GeneratedPdfResult
import com.example.data.engine.QrCodeCodec
import com.example.data.engine.TimesheetPdfExporter
import com.example.ui.i18n.LocalAppLocale
import com.example.ui.i18n.LocalIsEnglish
import com.example.ui.i18n.formatHourUnit
import com.example.ui.i18n.formatMonthTitleLocalized
import com.example.ui.i18n.localizedTitle
import com.example.ui.i18n.trEn
import com.example.ui.theme.BorderSpecularTop
import com.example.ui.theme.DynamicTheme
import com.example.ui.viewmodel.ShiftViewModel
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfPreviewSheet(
    viewModel: ShiftViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val isEn = LocalIsEnglish.current
    val appLocale = LocalAppLocale.current
    val hrUnit = formatHourUnit(isEn)
    val stats by viewModel.timesheetStats.collectAsState()
    val settings by viewModel.userSettings.collectAsState()
    val monthDays by viewModel.monthShiftDays.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val localizedMonthTitle = remember(selectedYear, selectedMonth, appLocale) {
        formatMonthTitleLocalized(selectedYear, selectedMonth, appLocale)
    }

    var includeNotes by remember { mutableStateOf(true) }
    var includeBarcode by remember { mutableStateOf(false) }
    var includeQr by remember { mutableStateOf(true) }
    var zoomLevel by remember { mutableFloatStateOf(100f) }
    var lastGeneratedPdf by remember { mutableStateOf<GeneratedPdfResult?>(null) }
    var pendingSafSourceFile by remember { mutableStateOf<File?>(null) }

    val scale = (zoomLevel / 100f).coerceIn(0.8f, 1.35f)
    val dayShiftsCount = remember(monthDays) { monthDays.count { it.type.isWork && !it.type.isNight } }
    val nightShiftsCount = remember(monthDays) { monthDays.count { it.type.isWork && it.type.isNight } }
    val dayHoursTotal = remember(stats) {
        (stats.totalWorkHours - stats.nightHours - stats.overtimeHours).toInt().coerceAtLeast(0)
    }
    val docCode = remember(selectedYear, selectedMonth) {
        TimesheetPdfExporter.buildDocCode(selectedYear, selectedMonth)
    }
    val periodChip = remember(selectedYear, selectedMonth) {
        String.format(Locale.ROOT, "%02d/%02d", selectedMonth, selectedYear % 100)
    }
    val activeNotes = remember(monthDays) { monthDays.filter { it.note.isNotBlank() } }
    val qrPayload = remember(docCode, settings.employeeName, stats.totalWorkHours) {
        "PROSHIFT|$docCode|${settings.employeeName}|${stats.totalWorkHours.toInt()}h"
    }
    val previewQrBitmap = remember(qrPayload, includeQr) {
        if (includeQr) QrCodeCodec.generateQrBitmap(qrPayload, sizePx = 160)?.asImageBitmap() else null
    }
    val previewBarcodeBitmap = remember(docCode, includeBarcode) {
        if (includeBarcode) QrCodeCodec.generateBarcode128Bitmap(docCode, widthPx = 320, heightPx = 56)?.asImageBitmap() else null
    }

    // Zero-permission SAF "Save As PDF" launcher for Android 7.0–9.0 (API 24–28)
    val createPdfDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { targetUri: Uri? ->
        val src = pendingSafSourceFile
        pendingSafSourceFile = null
        if (targetUri != null && src != null) {
            val ok = TimesheetPdfExporter.copyFileToUri(context, src, targetUri)
            if (ok) {
                lastGeneratedPdf = lastGeneratedPdf?.copy(downloadsUri = targetUri, savedToPublicDownloads = true)
                Toast.makeText(
                    context,
                    trEn(isEn, "PDF seçtiğiniz konuma kaydedildi", "PDF saved to selected location"),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    context,
                    trEn(isEn, "PDF dosyaya yazılamadı", "Could not write PDF file"),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    val scope = rememberCoroutineScope()

    suspend fun generateCurrentPdf(saveToDownloads: Boolean): GeneratedPdfResult? {
        val result = TimesheetPdfExporter.generateTimesheetPdfAsync(
            context = context,
            settings = settings,
            stats = stats,
            monthDays = monthDays,
            selectedYear = selectedYear,
            selectedMonth = selectedMonth,
            monthTitleLocalized = localizedMonthTitle,
            includeNotes = includeNotes,
            includeBarcode = includeBarcode,
            includeQr = includeQr,
            isEn = isEn,
            qrPayload = qrPayload,
            saveToDownloads = saveToDownloads
        )
        if (result != null) {
            lastGeneratedPdf = result
        }
        return result
    }

    val dyn = DynamicTheme.colors

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = dyn.surfaceCard,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .background(dyn.borderMetal, RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 28.dp)
                .testTag("pdf_preview_sheet")
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "A4 PDF EXPORT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = dyn.textMuted,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = trEn("Resmi Nöbet Çizelgesi — Önizleme", "Official Shift Timesheet — Preview"),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = dyn.textPrimary
                    )
                    Text(
                        text = trEn("$localizedMonthTitle Dönemi • SGK & İK Standart Format", "$localizedMonthTitle Period • Standard HR Format"),
                        fontSize = 12.sp,
                        color = dyn.textSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { onDismiss() }
                        .testTag("btn_close_pdf_preview"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(dyn.surfaceElevated, CircleShape)
                            .border(1.dp, dyn.borderSubtle, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = trEn("Kapat", "Close"),
                            tint = dyn.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Document Preview Plate
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, dyn.borderMetal, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = dyn.surfaceCard
            ) {
                Column(modifier = Modifier.padding((14 * scale).dp)) {
                    // Document Top Banner
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ProShiftLogo(
                                variant = ProShiftLogoVariant.SYMBOL,
                                height = (15 * scale).dp,
                                contentDescription = null
                            )
                            Text(
                                text = trEn("PROSHIFT RESMİ PUANTAJ", "PROSHIFT OFFICIAL TIMESHEET"),
                                fontSize = (10 * scale).sp,
                                fontWeight = FontWeight.Bold,
                                color = dyn.textPrimary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(dyn.activeChipBg, RoundedCornerShape(6.dp))
                                .border(1.dp, dyn.dayShiftBorder, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = periodChip,
                                fontSize = (9 * scale).sp,
                                fontWeight = FontWeight.Bold,
                                color = dyn.accentCyan,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Staff & Department Box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(dyn.surfaceElevated, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = trEn("PERSONEL", "STAFF"), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = dyn.textMuted)
                            Text(
                                text = settings.employeeName,
                                fontSize = (12 * scale).sp,
                                fontWeight = FontWeight.Bold,
                                color = dyn.textPrimary
                            )
                        }
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Text(text = trEn("ÜNVAN & BİRİM", "TITLE & DEPT"), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = dyn.textMuted)
                            Text(
                                text = "${settings.title} • ${settings.department}",
                                fontSize = (10 * scale).sp,
                                color = dyn.textSecondary,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Summary Table Headers
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = trEn("KALEM AÇIKLAMASI", "LINE ITEM"), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = dyn.textMuted)
                        Text(text = trEn("SÜRE / STATÜ", "HOURS / STATUS"), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = dyn.textMuted)
                    }

                    // Row 1: Day Base Shifts
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).background(dyn.dayShiftAccent, CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = trEn("Gündüz / Normal Mesai ($dayShiftsCount Vardiya)", "Day / Regular Shifts ($dayShiftsCount Shifts)"),
                                fontSize = (11.5f * scale).sp,
                                color = dyn.textPrimary
                            )
                        }
                        Text(
                            text = trEn("${dayHoursTotal}s Taban", "${dayHoursTotal}h Base"),
                            fontSize = (11.5f * scale).sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.textPrimary
                        )
                    }

                    // Row 2: Night Shifts
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).background(dyn.nightShiftAccent, CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = trEn("Gece Nöbeti ($nightShiftsCount Nöbet • md. 69)", "Night Shifts ($nightShiftsCount Shifts)"),
                                fontSize = (11.5f * scale).sp,
                                color = dyn.textPrimary
                            )
                        }
                        Text(
                            text = "${stats.nightHours.toInt()}$hrUnit",
                            fontSize = (11.5f * scale).sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.nightShiftAccent
                        )
                    }

                    // Row 3: Overtime
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).background(dyn.eveningShiftAccent, CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = trEn("Onaylı Fazla Mesai (+%50)", "Approved Overtime (+50%)"),
                                fontSize = (11.5f * scale).sp,
                                color = dyn.textPrimary
                            )
                        }
                        Text(
                            text = "+${stats.overtimeHours.toInt()}$hrUnit",
                            fontSize = (11.5f * scale).sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.eveningShiftAccent
                        )
                    }

                    // Compact Daily Table Preview (shows real days that will be written to A4 PDF)
                    if (monthDays.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(dyn.surfaceDim, RoundedCornerShape(8.dp))
                                .border(1.dp, dyn.borderSubtle, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = trEn("GÜNLÜK ÇİZELGE ÖNİZLEME", "DAILY TABLE PREVIEW"),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = dyn.accentCyan
                                )
                                Text(
                                    text = trEn("${monthDays.size} gün dahil", "${monthDays.size} days included"),
                                    fontSize = 8.sp,
                                    color = dyn.textMuted
                                )
                            }
                            monthDays.take(4).forEach { day ->
                                val range = if (day.type.isWork) "${day.startTime}-${day.endTime}" else "-"
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${day.date} • ${day.type.localizedTitle(isEn)}",
                                        fontSize = (9.5f * scale).sp,
                                        color = dyn.textSecondary,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "$range (${day.totalHours.toInt()}$hrUnit)",
                                        fontSize = (9.5f * scale).sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = dyn.textPrimary
                                    )
                                }
                            }
                            if (monthDays.size > 4) {
                                Text(
                                    text = trEn(
                                        "… ve ${monthDays.size - 4} gün daha tam A4 çizelgede yer alır",
                                        "… plus ${monthDays.size - 4} more days in full A4 report"
                                    ),
                                    fontSize = 8.5.sp,
                                    color = dyn.textMuted
                                )
                            }
                        }
                    }

                    if (includeNotes && activeNotes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(dyn.surfaceElevated, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text(trEn("VARDİYA NOTLARI (${activeNotes.size})", "SHIFT NOTES (${activeNotes.size})"), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = dyn.accentCyan)
                            activeNotes.take(2).forEach { noteDay ->
                                Text(
                                    text = "• ${noteDay.date}: ${noteDay.note}",
                                    fontSize = 10.sp,
                                    color = dyn.textSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Total Accrued Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(dyn.activeChipBg, RoundedCornerShape(10.dp))
                            .border(1.dp, dyn.dayShiftBorder, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = trEn("TAHAKKUK TOPLAMI", "TOTAL ACCRUED"),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = dyn.textSecondary
                        )
                        Text(
                            text = trEn("${stats.totalWorkHours.toInt()} Saat", "${stats.totalWorkHours.toInt()} Hours"),
                            fontSize = (16 * scale).sp,
                            fontWeight = FontWeight.Black,
                            color = dyn.accentCyan
                        )
                    }

                    // Live Code-128 Barcode Preview when enabled
                    if (includeBarcode && previewBarcodeBitmap != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .border(1.dp, dyn.borderSubtle, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = previewBarcodeBitmap,
                                contentDescription = trEn("Belge Barkodu", "Document Barcode"),
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(34.dp)
                                    .testTag("pdf_preview_barcode_image")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Document Bottom Metadata with real QR & Zoom pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (includeQr && previewQrBitmap != null) {
                                Surface(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .border(1.dp, dyn.borderSubtle, RoundedCornerShape(6.dp)),
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.White
                                ) {
                                    Image(
                                        bitmap = previewQrBitmap,
                                        contentDescription = trEn("Belge Doğrulama QR Kodu", "Document Verification QR Code"),
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(3.dp)
                                            .testTag("pdf_preview_qr_image")
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = docCode,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = dyn.textPrimary
                                )
                                Text(
                                    text = if (includeBarcode) trEn("Barkod & Belge Kodu Aktif", "Barcode & Doc ID Enabled") else trEn("4857 SK Standart Çıktı", "Standard HR Export"),
                                    fontSize = 9.sp,
                                    color = dyn.textSecondary
                                )
                            }
                        }

                        // Accessible Zoom Pill
                        Surface(
                            modifier = Modifier.border(1.dp, dyn.borderMetal, RoundedCornerShape(20.dp)),
                            shape = RoundedCornerShape(20.dp),
                            color = dyn.surfaceElevated
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 36.dp, height = 36.dp)
                                        .clickable { if (zoomLevel > 75f) zoomLevel -= 25f }
                                        .testTag("pdf_zoom_out"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "-", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = dyn.textSecondary)
                                }
                                Text(
                                    text = "${zoomLevel.toInt()}%",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = dyn.accentCyan,
                                    modifier = Modifier.testTag("pdf_zoom_value")
                                )
                                Box(
                                    modifier = Modifier
                                        .size(width = 36.dp, height = 36.dp)
                                        .clickable { if (zoomLevel < 125f) zoomLevel += 25f }
                                        .testTag("pdf_zoom_in"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "+", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = dyn.textSecondary)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Document Options Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = trEn("BELGE SEÇENEKLERİ", "DOCUMENT OPTIONS"),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = dyn.textMuted,
                    letterSpacing = 0.5.sp
                )
                val activeCount = (if (includeNotes) 1 else 0) + (if (includeBarcode) 1 else 0) + (if (includeQr) 1 else 0)
                Text(
                    text = trEn("$activeCount Aktif Parametre", "$activeCount Active Options"),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = dyn.accentCyan
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Options Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { includeNotes = !includeNotes }
                        .border(1.dp, if (includeNotes) dyn.accentCyan else dyn.borderSubtle, RoundedCornerShape(10.dp))
                        .testTag("pdf_toggle_notes"),
                    shape = RoundedCornerShape(10.dp),
                    color = if (includeNotes) dyn.activeChipBg else dyn.surfaceElevated
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (includeNotes) trEn("✓ Notlar", "✓ Notes") else trEn("○ Notlar", "○ Notes"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (includeNotes) dyn.accentCyan else dyn.textSecondary
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { includeBarcode = !includeBarcode }
                        .border(1.dp, if (includeBarcode) dyn.accentCyan else dyn.borderSubtle, RoundedCornerShape(10.dp))
                        .testTag("pdf_toggle_barcode"),
                    shape = RoundedCornerShape(10.dp),
                    color = if (includeBarcode) dyn.activeChipBg else dyn.surfaceElevated
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (includeBarcode) trEn("✓ Barkod", "✓ Barcode") else trEn("○ Barkod", "○ Barcode"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (includeBarcode) dyn.accentCyan else dyn.textSecondary
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { includeQr = !includeQr }
                        .border(1.dp, if (includeQr) dyn.accentCyan else dyn.borderSubtle, RoundedCornerShape(10.dp))
                        .testTag("pdf_toggle_qr"),
                    shape = RoundedCornerShape(10.dp),
                    color = if (includeQr) dyn.activeChipBg else dyn.surfaceElevated
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (includeQr) trEn("✓ QR Kod", "✓ QR Code") else trEn("○ QR Kod", "○ QR Code"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (includeQr) dyn.accentCyan else dyn.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Inline Generated PDF Ready Banner (immediately visible above action button)
            val generated = lastGeneratedPdf
            if (generated != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, dyn.emeraldBorder, RoundedCornerShape(12.dp))
                        .testTag("pdf_ready_banner"),
                    shape = RoundedCornerShape(12.dp),
                    color = dyn.emeraldBg
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = dyn.emeraldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = generated.fileName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = dyn.textPrimary,
                                    maxLines = 1
                                )
                                val kb = (generated.sizeBytes / 1024).coerceAtLeast(1)
                                val locText = if (generated.savedToPublicDownloads) {
                                    trEn("İndirilenler/PROSHIFT • $kb KB", "Downloads/PROSHIFT • $kb KB")
                                } else {
                                    trEn("${generated.pageCount} sayfa • $kb KB", "${generated.pageCount} page(s) • $kb KB")
                                }
                                Text(text = locText, fontSize = 9.5.sp, color = dyn.emeraldAccent)
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .height(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    val uri = generated.downloadsUri
                                        ?: TimesheetPdfExporter.getShareablePdfUri(context, generated.cacheFile)
                                    if (uri != null) {
                                        TimesheetPdfExporter.openPdfInViewer(context, uri, isEn)
                                    }
                                }
                                .border(1.dp, dyn.emeraldBorder, RoundedCornerShape(8.dp))
                                .testTag("btn_open_generated_pdf"),
                            shape = RoundedCornerShape(8.dp),
                            color = dyn.surfaceCard
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = dyn.emeraldAccent, modifier = Modifier.size(14.dp))
                                Text(trEn("Aç", "Open"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = dyn.emeraldAccent)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Primary Download Button -> Generates real A4 PDF & saves to Downloads
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable {
                        scope.launch {
                            val isQPlus = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                            val result = generateCurrentPdf(saveToDownloads = isQPlus)
                            if (result != null) {
                                val kb = (result.sizeBytes / 1024).coerceAtLeast(1)
                                if (isQPlus) {
                                    val msg = if (result.savedToPublicDownloads) {
                                        trEn(
                                            isEn,
                                            "İndirilenler/PROSHIFT/${result.fileName} (${kb} KB) kaydedildi",
                                            "Saved to Downloads/PROSHIFT/${result.fileName} (${kb} KB)"
                                        )
                                    } else {
                                        trEn(
                                            isEn,
                                            "${result.fileName} (${kb} KB) hazır — aşağıdan açabilir veya paylaşabilirsiniz",
                                            "${result.fileName} (${kb} KB) ready — open or share below"
                                        )
                                    }
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                } else {
                                    pendingSafSourceFile = result.cacheFile
                                    runCatching { createPdfDocumentLauncher.launch(result.fileName) }
                                        .onFailure {
                                            Toast.makeText(
                                                context,
                                                trEn(isEn, "${result.fileName} (${kb} KB) oluşturuldu", "${result.fileName} (${kb} KB) created"),
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    trEn(isEn, "PDF oluşturulamadı, metin dışa aktarılıyor", "Could not create PDF, exporting text instead"),
                                    Toast.LENGTH_SHORT
                                ).show()
                                viewModel.exportTimesheetText(shareCsv = false, includeNotes = includeNotes)
                            }
                        }
                    }
                    .shadow(12.dp, RoundedCornerShape(14.dp), ambientColor = dyn.primaryNeon)
                    .border(1.dp, BorderSpecularTop, RoundedCornerShape(14.dp))
                    .testTag("btn_download_pdf"),
                shape = RoundedCornerShape(14.dp),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.horizontalGradient(listOf(dyn.primaryNeon, dyn.primaryDeep))),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Download,
                                contentDescription = trEn("İndir", "Download"),
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = trEn("PDF Çizelgeyi Oluştur & Kaydet", "Generate & Save PDF Report"),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "A4 PDF",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Actions Bar (Print PDF, Share PDF, WhatsApp Summary, HR CSV)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            scope.launch {
                                val result = generateCurrentPdf(saveToDownloads = false)
                                if (result != null) {
                                    val printed = TimesheetPdfExporter.printPdfFile(
                                        context = context,
                                        file = result.cacheFile,
                                        jobName = result.fileName,
                                        isEn = isEn
                                    )
                                    if (!printed) {
                                        viewModel.exportTimesheetText(shareCsv = false, includeNotes = includeNotes)
                                    }
                                } else {
                                    viewModel.exportTimesheetText(shareCsv = false, includeNotes = includeNotes)
                                }
                            }
                        }
                        .border(1.dp, dyn.borderMetal, RoundedCornerShape(12.dp))
                        .testTag("btn_print_pdf"),
                    shape = RoundedCornerShape(12.dp),
                    color = dyn.surfaceElevated
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Filled.Print, contentDescription = trEn("Yazdır", "Print"), tint = dyn.accentCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = trEn("Yazdır", "Print"), fontSize = 11.sp, color = dyn.textPrimary)
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            scope.launch {
                                val result = generateCurrentPdf(saveToDownloads = false)
                                val uri = result?.let { TimesheetPdfExporter.getShareablePdfUri(context, it.cacheFile) }
                                if (uri != null) {
                                    TimesheetPdfExporter.sharePdfFile(
                                        context = context,
                                        uri = uri,
                                        title = trEn(isEn, "PDF Puantajı Paylaş", "Share PDF Timesheet"),
                                        isEn = isEn
                                    )
                                } else {
                                    viewModel.exportTimesheetText(shareCsv = false, includeNotes = includeNotes)
                                }
                            }
                        }
                        .border(1.dp, dyn.borderMetal, RoundedCornerShape(12.dp))
                        .testTag("btn_share_pdf"),
                    shape = RoundedCornerShape(12.dp),
                    color = dyn.surfaceElevated
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Filled.Share, contentDescription = trEn("PDF Paylaş", "Share PDF"), tint = dyn.accentCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = trEn("PDF Paylaş", "Share PDF"), fontSize = 11.sp, color = dyn.textPrimary)
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.shareViaWhatsApp() }
                        .border(1.dp, dyn.borderMetal, RoundedCornerShape(12.dp))
                        .testTag("btn_whatsapp_pdf"),
                    shape = RoundedCornerShape(12.dp),
                    color = dyn.surfaceElevated
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "💬", fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "WhatsApp", fontSize = 11.sp, color = dyn.textPrimary)
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.exportTimesheetText(shareCsv = true, includeNotes = includeNotes) }
                        .border(1.dp, dyn.borderMetal, RoundedCornerShape(12.dp))
                        .testTag("btn_csv_pdf"),
                    shape = RoundedCornerShape(12.dp),
                    color = dyn.surfaceElevated
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Filled.Email, contentDescription = trEn("İK CSV", "HR CSV"), tint = dyn.accentCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = trEn("İK CSV", "HR CSV"), fontSize = 11.sp, color = dyn.textPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Security,
                    contentDescription = trEn("Güvenlik", "Security"),
                    tint = dyn.textMuted,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = trEn("Tam Çevrimdışı PDF Üretimi • Veriler Cihaz Dışına Çıkmaz", "100% Offline PDF Generation • Data Never Leaves Your Device"),
                    fontSize = 10.sp,
                    color = dyn.textMuted
                )
            }
        }
    }
}
