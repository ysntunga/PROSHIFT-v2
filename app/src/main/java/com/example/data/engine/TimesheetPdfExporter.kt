package com.example.data.engine

import android.content.ClipData
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.ShiftDay
import com.example.data.model.TimesheetStats
import com.example.data.model.UserSettings
import com.example.ui.i18n.formatHourUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.ui.i18n.localizedName
import com.example.ui.i18n.localizedTitle
import com.example.ui.i18n.trEn
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class GeneratedPdfResult(
    val cacheFile: File,
    val fileName: String,
    val sizeBytes: Long,
    val pageCount: Int,
    val downloadsUri: Uri? = null,
    val savedToPublicDownloads: Boolean = false
)

object TimesheetPdfExporter {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN_X = 36f
    private const val RIGHT_X = 559f

    fun buildPdfFileName(year: Int, month: Int, isEn: Boolean): String {
        val period = String.format(Locale.ROOT, "%04d-%02d", year, month)
        return if (isEn) "PROSHIFT_Timesheet_$period.pdf" else "PROSHIFT_Puantaj_$period.pdf"
    }

    fun buildDocCode(year: Int, month: Int): String =
        String.format(Locale.ROOT, "PRSH-%04d-%02d-4857", year, month)

    suspend fun generateTimesheetPdfAsync(
        context: Context,
        settings: UserSettings,
        stats: TimesheetStats,
        monthDays: List<ShiftDay>,
        selectedYear: Int,
        selectedMonth: Int,
        monthTitleLocalized: String,
        includeNotes: Boolean,
        includeBarcode: Boolean,
        includeQr: Boolean,
        isEn: Boolean,
        qrPayload: String,
        saveToDownloads: Boolean = false
    ): GeneratedPdfResult? = withContext(Dispatchers.IO) {
        generateTimesheetPdf(
            context = context,
            settings = settings,
            stats = stats,
            monthDays = monthDays,
            selectedYear = selectedYear,
            selectedMonth = selectedMonth,
            monthTitleLocalized = monthTitleLocalized,
            includeNotes = includeNotes,
            includeBarcode = includeBarcode,
            includeQr = includeQr,
            isEn = isEn,
            qrPayload = qrPayload,
            saveToDownloads = saveToDownloads
        )
    }

    fun generateTimesheetPdf(
        context: Context,
        settings: UserSettings,
        stats: TimesheetStats,
        monthDays: List<ShiftDay>,
        selectedYear: Int,
        selectedMonth: Int,
        monthTitleLocalized: String,
        includeNotes: Boolean,
        includeBarcode: Boolean,
        includeQr: Boolean,
        isEn: Boolean,
        qrPayload: String,
        saveToDownloads: Boolean = false
    ): GeneratedPdfResult? {
        val docCode = buildDocCode(selectedYear, selectedMonth)
        val fileName = buildPdfFileName(selectedYear, selectedMonth, isEn)
        val pdfDir = File(context.cacheDir, "pdfs")
        if (!pdfDir.exists()) pdfDir.mkdirs()
        val cacheFile = File(pdfDir, fileName)

        var doc: PdfDocument? = null
        var qrBitmap: Bitmap? = null
        var barcodeBitmap: Bitmap? = null

        return try {
            val pageCount: Int = try {
                doc = PdfDocument()
                if (includeQr) {
                    qrBitmap = QrCodeCodec.generateQrBitmap(qrPayload.ifBlank { docCode }, sizePx = 180)
                }
                if (includeBarcode) {
                    barcodeBitmap = QrCodeCodec.generateBarcode128Bitmap(docCode, widthPx = 360, heightPx = 68)
                }

                val pages = drawDocumentPages(
                    doc = doc,
                    settings = settings,
                    stats = stats,
                    monthDays = monthDays,
                    monthTitleLocalized = monthTitleLocalized,
                    docCode = docCode,
                    includeNotes = includeNotes,
                    includeBarcode = includeBarcode,
                    includeQr = includeQr,
                    isEn = isEn,
                    qrBitmap = qrBitmap,
                    barcodeBitmap = barcodeBitmap
                )

                FileOutputStream(cacheFile).use { out ->
                    doc.writeTo(out)
                }
                pages
            } catch (_: Throwable) {
                // Fallback pure-Kotlin PDF-1.4 writer for environments where native PdfDocument is unavailable
                writeFallbackPdf14(
                    targetFile = cacheFile,
                    settings = settings,
                    stats = stats,
                    monthDays = monthDays,
                    monthTitleLocalized = monthTitleLocalized,
                    docCode = docCode,
                    includeNotes = includeNotes,
                    includeBarcode = includeBarcode,
                    includeQr = includeQr,
                    isEn = isEn,
                    qrPayload = qrPayload
                )
            }

            if (!cacheFile.exists() || cacheFile.length() <= 0L) return null

            var publicUri: Uri? = null
            var savedPublic = false
            if (saveToDownloads && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                publicUri = copyToPublicDownloadsApi29Plus(context, cacheFile, fileName)
                savedPublic = publicUri != null
            }

            GeneratedPdfResult(
                cacheFile = cacheFile,
                fileName = fileName,
                sizeBytes = cacheFile.length(),
                pageCount = pageCount,
                downloadsUri = publicUri,
                savedToPublicDownloads = savedPublic
            )
        } catch (_: Throwable) {
            null
        } finally {
            runCatching { doc?.close() }
            runCatching { qrBitmap?.recycle() }
            runCatching { barcodeBitmap?.recycle() }
        }
    }

    fun copyFileToUri(context: Context, sourceFile: File, targetUri: Uri): Boolean {
        return try {
            context.contentResolver.openOutputStream(targetUri)?.use { out ->
                FileInputStream(sourceFile).use { input ->
                    input.copyTo(out)
                }
            } != null
        } catch (_: Throwable) {
            false
        }
    }

    fun getShareablePdfUri(context: Context, file: File): Uri? {
        return runCatching {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        }.getOrNull()
    }

    fun openPdfInViewer(context: Context, uri: Uri, isEn: Boolean): Boolean {
        return try {
            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(viewIntent)
            true
        } catch (_: Throwable) {
            // Fallback to system share sheet when no PDF viewer app is installed
            sharePdfFile(
                context = context,
                uri = uri,
                title = trEn(isEn, "PDF Görüntüle / Paylaş", "Open / Share PDF"),
                isEn = isEn
            )
        }
    }

    fun sharePdfFile(context: Context, uri: Uri, title: String, isEn: Boolean): Boolean {
        return try {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                clipData = ClipData.newUri(context.contentResolver, title, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(sendIntent, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            true
        } catch (_: Throwable) {
            runCatching {
                Toast.makeText(
                    context,
                    trEn(isEn, "PDF paylaşım ekranı açılamadı", "Could not open PDF share sheet"),
                    Toast.LENGTH_SHORT
                ).show()
            }
            false
        }
    }

    fun printPdfFile(context: Context, file: File, jobName: String, isEn: Boolean): Boolean {
        return try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                ?: return false
            val adapter = object : PrintDocumentAdapter() {
                override fun onLayout(
                    oldAttributes: PrintAttributes?,
                    newAttributes: PrintAttributes,
                    cancellationSignal: CancellationSignal?,
                    callback: LayoutResultCallback,
                    extras: Bundle?
                ) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback.onLayoutCancelled()
                        return
                    }
                    val info = PrintDocumentInfo.Builder(file.name)
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                        .build()
                    callback.onLayoutFinished(info, true)
                }

                override fun onWrite(
                    pages: Array<out PageRange>,
                    destination: ParcelFileDescriptor,
                    cancellationSignal: CancellationSignal?,
                    callback: WriteResultCallback
                ) {
                    try {
                        FileInputStream(file).use { input ->
                            FileOutputStream(destination.fileDescriptor).use { output ->
                                input.copyTo(output)
                            }
                        }
                        if (cancellationSignal?.isCanceled == true) {
                            callback.onWriteCancelled()
                        } else {
                            callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                        }
                    } catch (e: Throwable) {
                        callback.onWriteFailed(e.localizedMessage ?: "Write error")
                    }
                }
            }
            printManager.print(jobName, adapter, PrintAttributes.Builder().build())
            true
        } catch (_: Throwable) {
            runCatching {
                Toast.makeText(
                    context,
                    trEn(isEn, "Yazdırma servisi kullanılamıyor", "Print service is unavailable"),
                    Toast.LENGTH_SHORT
                ).show()
            }
            false
        }
    }

    private fun copyToPublicDownloadsApi29Plus(
        context: Context,
        sourceFile: File,
        fileName: String
    ): Uri? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
        return try {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/PROSHIFT")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return null
            val copied = resolver.openOutputStream(uri)?.use { out: OutputStream ->
                FileInputStream(sourceFile).use { input ->
                    input.copyTo(out)
                }
                true
            } ?: false

            if (copied) {
                val doneValues = ContentValues().apply {
                    put(MediaStore.Downloads.IS_PENDING, 0)
                }
                resolver.update(uri, doneValues, null, null)
                uri
            } else {
                resolver.delete(uri, null, null)
                null
            }
        } catch (_: Throwable) {
            null
        }
    }

    private fun drawDocumentPages(
        doc: PdfDocument,
        settings: UserSettings,
        stats: TimesheetStats,
        monthDays: List<ShiftDay>,
        monthTitleLocalized: String,
        docCode: String,
        includeNotes: Boolean,
        includeBarcode: Boolean,
        includeQr: Boolean,
        isEn: Boolean,
        qrBitmap: Bitmap?,
        barcodeBitmap: Bitmap?
    ): Int {
        val headerBgPaint = Paint().apply { color = android.graphics.Color.rgb(15, 33, 58) }
        val accentPaint = Paint().apply {
            color = android.graphics.Color.rgb(34, 211, 238)
            strokeWidth = 2f
        }
        val whiteTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val whiteSubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(203, 213, 225)
            textSize = 9.5f
        }
        val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(15, 23, 42)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(30, 41, 59)
            textSize = 9f
        }
        val mutedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(100, 116, 139)
            textSize = 8.2f
        }
        val zebraPaint = Paint().apply { color = android.graphics.Color.rgb(244, 247, 251) }
        val summaryBgPaint = Paint().apply { color = android.graphics.Color.rgb(239, 246, 255) }
        val linePaint = Paint().apply {
            color = android.graphics.Color.rgb(203, 213, 225)
            strokeWidth = 0.8f
        }

        val hrUnit = formatHourUnit(isEn)
        var pageNum = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create())
        var canvas = page.canvas

        // Top Dark Header Banner
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 66f, headerBgPaint)
        canvas.drawLine(0f, 66f, PAGE_WIDTH.toFloat(), 66f, accentPaint)
        canvas.drawText(
            trEn(isEn, "PROSHIFT RESMİ NÖBET VE PUANTAJ ÇİZELGESİ", "PROSHIFT OFFICIAL SHIFT & TIMESHEET REPORT"),
            MARGIN_X,
            30f,
            whiteTitlePaint
        )
        canvas.drawText(
            trEn(
                isEn,
                "Dönem: $monthTitleLocalized  •  Belge No: $docCode  •  4857 SK Standart Format",
                "Period: $monthTitleLocalized  •  Doc ID: $docCode  •  Standard HR Format"
            ),
            MARGIN_X,
            49f,
            whiteSubPaint
        )

        var y = 84f
        // Staff & Summary Box
        canvas.drawRect(MARGIN_X, y - 12f, RIGHT_X, y + 40f, summaryBgPaint)
        canvas.drawText(
            trEn(
                isEn,
                "Personel: ${settings.employeeName} (${settings.title})  •  Birim: ${settings.department}",
                "Staff: ${settings.employeeName} (${settings.title})  •  Dept: ${settings.department}"
            ),
            MARGIN_X + 8f,
            y + 2f,
            sectionPaint
        )
        canvas.drawText(
            trEn(
                isEn,
                "Vardiya Düzeni: ${settings.cyclePreset.localizedName(false)}  •  Tarih: ${LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}",
                "Shift Cycle: ${settings.cyclePreset.localizedName(true)}  •  Generated: ${LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}"
            ),
            MARGIN_X + 8f,
            y + 17f,
            bodyPaint
        )
        canvas.drawText(
            trEn(
                isEn,
                "Toplam: ${stats.totalWorkHours.toInt()}s  |  Gece: ${stats.nightHours.toInt()}s  |  Ek Mesai: +${stats.overtimeHours.toInt()}s  |  Tatil: ${stats.holidayWorkDaysCount}g  |  Kalan İzin: ${stats.remainingLeaveDays}g",
                "Total: ${stats.totalWorkHours.toInt()}h  |  Night: ${stats.nightHours.toInt()}h  |  Overtime: +${stats.overtimeHours.toInt()}h  |  Holiday: ${stats.holidayWorkDaysCount}d  |  Leave Left: ${stats.remainingLeaveDays}d"
            ),
            MARGIN_X + 8f,
            y + 32f,
            sectionPaint
        )

        y += 56f

        fun drawTableHeader(targetCanvas: Canvas, yPos: Float): Float {
            targetCanvas.drawText(trEn(isEn, "TARİH", "DATE"), MARGIN_X + 4f, yPos, sectionPaint)
            targetCanvas.drawText(trEn(isEn, "VARDİYA", "SHIFT"), 126f, yPos, sectionPaint)
            targetCanvas.drawText(trEn(isEn, "SAAT", "HOURS"), 226f, yPos, sectionPaint)
            targetCanvas.drawText(trEn(isEn, "SÜRE", "DUR."), 314f, yPos, sectionPaint)
            if (includeNotes) {
                targetCanvas.drawText(trEn(isEn, "NOT / AÇIKLAMA", "NOTE / REMARKS"), 376f, yPos, sectionPaint)
            }
            targetCanvas.drawLine(MARGIN_X, yPos + 5f, RIGHT_X, yPos + 5f, linePaint)
            return yPos + 17f
        }

        y = drawTableHeader(canvas, y)
        val rowHeight = 16.5f
        val footerNeededHeight = if (includeQr || includeBarcode) 108f else 64f
        val maxRowYOnSinglePage = PAGE_HEIGHT - 42f

        for ((index, day) in monthDays.withIndex()) {
            if (y > maxRowYOnSinglePage) {
                doc.finishPage(page)
                pageNum++
                page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create())
                canvas = page.canvas
                canvas.drawText(
                    trEn(isEn, "PROSHIFT PUANTAJ — $monthTitleLocalized (Sayfa $pageNum)", "PROSHIFT TIMESHEET — $monthTitleLocalized (Page $pageNum)"),
                    MARGIN_X,
                    32f,
                    sectionPaint
                )
                y = drawTableHeader(canvas, 52f)
            }

            if (index % 2 == 1) {
                canvas.drawRect(MARGIN_X, y - 11.5f, RIGHT_X, y + 4.5f, zebraPaint)
            }

            canvas.drawText(day.date.toString(), MARGIN_X + 4f, y, bodyPaint)
            canvas.drawText(day.type.localizedTitle(isEn).take(16), 126f, y, bodyPaint)
            val range = if (day.type.isWork) "${day.startTime}-${day.endTime}" else "-"
            canvas.drawText(range, 226f, y, bodyPaint)
            val hoursLabel = if (day.overtimeHours > 0) {
                "${day.totalHours.toInt()}$hrUnit (+${day.overtimeHours.toInt()})"
            } else {
                "${day.totalHours.toInt()}$hrUnit"
            }
            canvas.drawText(hoursLabel, 314f, y, bodyPaint)
            if (includeNotes) {
                val noteDisplay = buildString {
                    if (day.holidayName != null) append("[${day.holidayName}] ")
                    if (day.note.isNotBlank()) append(day.note)
                }.trim()
                if (noteDisplay.isNotBlank()) {
                    canvas.drawText(noteDisplay.take(34), 376f, y, mutedPaint)
                }
            }
            y += rowHeight
        }

        // If footer does not fit on current page, open a fresh page cleanly
        if (y + footerNeededHeight > PAGE_HEIGHT - 20f) {
            doc.finishPage(page)
            pageNum++
            page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum).create())
            canvas = page.canvas
            y = 48f
        } else {
            y += 10f
        }

        canvas.drawLine(MARGIN_X, y, RIGHT_X, y, linePaint)
        y += 16f

        // Signature lines
        canvas.drawText(trEn(isEn, "Personel İmza:", "Employee Signature:"), MARGIN_X, y, sectionPaint)
        canvas.drawLine(MARGIN_X + 86f, y + 2f, 240f, y + 2f, linePaint)

        canvas.drawText(trEn(isEn, "Birim Amiri Onay:", "Supervisor Approval:"), 260f, y, sectionPaint)
        canvas.drawLine(368f, y + 2f, RIGHT_X, y + 2f, linePaint)
        y += 16f

        // Verification QR & Barcode Block
        if (includeQr || includeBarcode) {
            var leftCursorX = MARGIN_X
            if (includeQr && qrBitmap != null) {
                val qrSize = 68
                val destRect = Rect(
                    leftCursorX.toInt(),
                    y.toInt(),
                    leftCursorX.toInt() + qrSize,
                    y.toInt() + qrSize
                )
                canvas.drawBitmap(qrBitmap, null, destRect, null)
                leftCursorX += qrSize + 14f
            }

            if (includeBarcode && barcodeBitmap != null) {
                val bw = 180
                val bh = 38
                val destRect = Rect(
                    leftCursorX.toInt(),
                    (y + 4f).toInt(),
                    leftCursorX.toInt() + bw,
                    (y + 4f).toInt() + bh
                )
                canvas.drawBitmap(barcodeBitmap, null, destRect, null)
                canvas.drawText(docCode, leftCursorX + 4f, y + bh + 15f, mutedPaint)
            } else {
                canvas.drawText(
                    trEn(isEn, "Belge Doğrulama Kodu: $docCode", "Document Verification ID: $docCode"),
                    leftCursorX,
                    y + 20f,
                    mutedPaint
                )
                canvas.drawText(
                    trEn(isEn, "PROSHIFT Çevrimdışı Puantaj Motoru ile üretilmiştir.", "Generated offline by PROSHIFT Timesheet Engine."),
                    leftCursorX,
                    y + 34f,
                    mutedPaint
                )
            }
        } else {
            canvas.drawText(
                trEn(isEn, "Belge Kodu: $docCode  •  PROSHIFT Çevrimdışı Puantaj", "Document ID: $docCode  •  PROSHIFT Offline Timesheet"),
                MARGIN_X,
                y + 10f,
                mutedPaint
            )
        }

        doc.finishPage(page)
        return pageNum
    }

    private fun writeFallbackPdf14(
        targetFile: File,
        settings: UserSettings,
        stats: TimesheetStats,
        monthDays: List<ShiftDay>,
        monthTitleLocalized: String,
        docCode: String,
        includeNotes: Boolean,
        includeBarcode: Boolean,
        includeQr: Boolean,
        isEn: Boolean,
        qrPayload: String
    ): Int {
        val hrUnit = formatHourUnit(isEn)
        val lines = mutableListOf<String>()
        lines += trEn(isEn, "PROSHIFT RESMI NOBET VE PUANTAJ CIZELGESI", "PROSHIFT OFFICIAL SHIFT & TIMESHEET REPORT")
        lines += trEn(
            isEn,
            "Donem: $monthTitleLocalized | Belge No: $docCode | 4857 SK",
            "Period: $monthTitleLocalized | Doc ID: $docCode | Standard HR"
        )
        lines += trEn(
            isEn,
            "Personel: ${settings.employeeName} (${settings.title}) | Birim: ${settings.department}",
            "Staff: ${settings.employeeName} (${settings.title}) | Dept: ${settings.department}"
        )
        lines += trEn(
            isEn,
            "Toplam: ${stats.totalWorkHours.toInt()}s | Gece: ${stats.nightHours.toInt()}s | Ek Mesai: +${stats.overtimeHours.toInt()}s | Tatil: ${stats.holidayWorkDaysCount}g",
            "Total: ${stats.totalWorkHours.toInt()}h | Night: ${stats.nightHours.toInt()}h | Overtime: +${stats.overtimeHours.toInt()}h | Holiday: ${stats.holidayWorkDaysCount}d"
        )
        lines += "--------------------------------------------------------------------------"
        lines += trEn(
            isEn,
            "TARIH        | VARDIYA          | SAAT        | SURE   | NOT",
            "DATE         | SHIFT            | HOURS       | DUR.   | NOTE"
        )

        monthDays.forEach { day ->
            val range = if (day.type.isWork) "${day.startTime}-${day.endTime}" else "-"
            val dur = if (day.overtimeHours > 0) {
                "${day.totalHours.toInt()}$hrUnit(+${day.overtimeHours.toInt()})"
            } else {
                "${day.totalHours.toInt()}$hrUnit"
            }
            val notePart = if (includeNotes) {
                buildString {
                    if (day.holidayName != null) append("[${day.holidayName}] ")
                    if (day.note.isNotBlank()) append(day.note)
                }.trim().take(28)
            } else {
                ""
            }
            lines += "${day.date} | ${day.type.localizedTitle(isEn).take(16).padEnd(16)} | ${range.padEnd(11)} | ${dur.padEnd(6)} | $notePart"
        }

        lines += "--------------------------------------------------------------------------"
        lines += trEn(
            isEn,
            "Personel Imza: ___________________   Birim Amiri Onay: ___________________",
            "Employee Signature: _______________  Supervisor Approval: ________________"
        )
        if (includeBarcode) lines += "BARCODE-128: $docCode"
        if (includeQr) lines += "QR-VERIFY: ${qrPayload.ifBlank { docCode }.take(80)}"

        val linesPerPage = 44
        val pageChunks = lines.chunked(linesPerPage).ifEmpty { listOf(emptyList()) }
        val pageCount = pageChunks.size

        val stream = ByteArrayOutputStream()
        fun writeAscii(text: String) {
            stream.write(text.toByteArray(Charsets.ISO_8859_1))
        }

        writeAscii("%PDF-1.4\n")
        val totalObjects = 3 + pageCount * 2
        val offsets = IntArray(totalObjects + 1)

        fun startObject(id: Int) {
            offsets[id] = stream.size()
            writeAscii("$id 0 obj\n")
        }

        // 1: Catalog
        startObject(1)
        writeAscii("<< /Type /Catalog /Pages 2 0 R >>\nendobj\n")

        // 2: Pages
        val kids = (0 until pageCount).joinToString(" ") { "${4 + it * 2} 0 R" }
        startObject(2)
        writeAscii("<< /Type /Pages /Kids [$kids] /Count $pageCount >>\nendobj\n")

        // 3: Type1 Helvetica Font
        startObject(3)
        writeAscii("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n")

        // Page + Content Stream objects
        pageChunks.forEachIndexed { idx, chunk ->
            val pageObjId = 4 + idx * 2
            val contentObjId = pageObjId + 1

            startObject(pageObjId)
            writeAscii(
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 $PAGE_WIDTH $PAGE_HEIGHT] " +
                    "/Resources << /Font << /F1 3 0 R >> >> /Contents $contentObjId 0 R >>\nendobj\n"
            )

            val contentBuilder = StringBuilder()
            contentBuilder.append("BT\n/F1 9 Tf\n15 TL\n36 806 Td\n")
            chunk.forEachIndexed { lineIdx, rawLine ->
                if (lineIdx > 0) contentBuilder.append("T*\n")
                contentBuilder.append("(").append(escapePdfAscii(rawLine)).append(") Tj\n")
            }
            contentBuilder.append("ET\n")
            val contentBytes = contentBuilder.toString().toByteArray(Charsets.ISO_8859_1)

            startObject(contentObjId)
            writeAscii("<< /Length ${contentBytes.size} >>\nstream\n")
            stream.write(contentBytes)
            writeAscii("\nendstream\nendobj\n")
        }

        val xrefOffset = stream.size()
        writeAscii("xref\n0 ${totalObjects + 1}\n")
        writeAscii("0000000000 65535 f \n")
        for (id in 1..totalObjects) {
            writeAscii(String.format(Locale.ROOT, "%010d 00000 n \n", offsets[id]))
        }
        writeAscii("trailer\n<< /Size ${totalObjects + 1} /Root 1 0 R >>\nstartxref\n$xrefOffset\n%%EOF\n")

        FileOutputStream(targetFile).use { out ->
            out.write(stream.toByteArray())
        }
        return pageCount
    }

    private fun escapePdfAscii(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            val mapped = when (ch) {
                'Ç' -> "C"
                'ç' -> "c"
                'Ğ' -> "G"
                'ğ' -> "g"
                'İ' -> "I"
                'ı' -> "i"
                'Ö' -> "O"
                'ö' -> "o"
                'Ş' -> "S"
                'ş' -> "s"
                'Ü' -> "U"
                'ü' -> "u"
                '•' -> "-"
                '—', '–' -> "-"
                '\\' -> "\\\\"
                '(' -> "\\("
                ')' -> "\\)"
                else -> if (ch.code in 32..126) ch.toString() else " "
            }
            sb.append(mapped)
        }
        return sb.toString()
    }
}
