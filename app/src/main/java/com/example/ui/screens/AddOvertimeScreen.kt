package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ShiftType
import com.example.ui.theme.DynamicTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.ShiftViewModel
import java.time.LocalDate

import com.example.ui.components.ShiftTimePickerSheet
import com.example.ui.components.TitaniumDatePickerDialog
import com.example.ui.components.UserAvatarImage
import com.example.ui.components.formatHourMinute
import com.example.ui.components.parseHourMinute
import com.example.ui.i18n.LocalAppLocale
import com.example.ui.i18n.LocalIsEnglish
import com.example.ui.i18n.formatHourUnit
import com.example.ui.i18n.localizedTitle
import com.example.ui.i18n.trEn
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun AddOvertimeScreen(
    viewModel: ShiftViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.userSettings.collectAsState()

    BackHandler {
        viewModel.navigateTo(AppScreen.HOME)
    }

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }

    var selectedModeIndex by remember { mutableIntStateOf(0) } // 0: Uzatma, 1: İzin Günü, 2: İcap, 3: Bayram
    var startTimeStr by remember(settings.gunduzEndTime, selectedModeIndex) {
        mutableStateOf(if (selectedModeIndex == 0) settings.gunduzEndTime else settings.gunduzStartTime)
    }
    var selectedDurationMinutes by remember { mutableIntStateOf(240) } // +4s default

    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    fun recomputeEndFromDuration(start: String, mins: Int): String {
        val (sh, sm) = parseHourMinute(start)
        val totalEnd = (sh * 60 + sm + mins) % (24 * 60)
        return formatHourMinute(totalEnd / 60, totalEnd % 60)
    }

    fun recomputeDurationFromTimes(start: String, end: String): Int {
        val (sh, sm) = parseHourMinute(start)
        val (eh, em) = parseHourMinute(end)
        val sMin = sh * 60 + sm
        var eMin = eh * 60 + em
        if (eMin <= sMin) eMin += 24 * 60
        return (eMin - sMin).coerceIn(30, 24 * 60)
    }

    // Initialize endTimeStr once on mode or settings start change
    val currentEndStr = remember(startTimeStr, selectedDurationMinutes) {
        recomputeEndFromDuration(startTimeStr, selectedDurationMinutes)
    }

    val durationHoursDecimal = selectedDurationMinutes / 60.0
    val selectedDurationHours = kotlin.math.round(durationHoursDecimal).toInt().coerceAtLeast(1)

    val isEn = LocalIsEnglish.current
    val appLocale = LocalAppLocale.current
    val hourUnit = formatHourUnit(isEn)

    val baseDayShift = remember(selectedDate, settings) {
        viewModel.repository.getShiftForDate(selectedDate, settings)
    }
    val dateFmt = remember(appLocale) { DateTimeFormatter.ofPattern("d MMMM yyyy", appLocale) }
    val crossesMidnight = remember(startTimeStr, currentEndStr) {
        val (sh, sm) = parseHourMinute(startTimeStr)
        val (eh, em) = parseHourMinute(currentEndStr)
        (eh * 60 + em) <= (sh * 60 + sm)
    }

    val dyn = DynamicTheme.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(dyn.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
            .imePadding()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(bottom = 32.dp)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(dyn.surfaceElevated, CircleShape)
                    .border(1.dp, dyn.borderSubtle, CircleShape)
                    .clickable { viewModel.navigateTo(AppScreen.HOME) }
                    .testTag("btn_back_overtime"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = trEn("Geri Dön", "Go Back"),
                    tint = dyn.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = trEn("Mesai Ekle", "Add Overtime"),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = dyn.textPrimary
            )

            // User Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, dyn.primaryNeon, CircleShape)
                    .clickable { viewModel.navigateTo(AppScreen.PROFILE) }
            ) {
                UserAvatarImage(
                    avatarPath = settings.avatarUriOrPath,
                    contentDescription = trEn("Profil", "Profile"),
                    displayName = settings.employeeName,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Card 1: Asıl Görev Özeti (Date selectable via TitaniumDatePickerDialog)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { showDatePicker = true }
                .testTag("card_overtime_date_picker"),
            shape = RoundedCornerShape(16.dp),
            color = dyn.surfaceCard
        ) {
            Column(
                modifier = Modifier
                    .border(1.dp, dyn.borderSubtle, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = dyn.dayShiftBg,
                            modifier = Modifier
                                .size(36.dp)
                                .border(1.dp, dyn.dayShiftBorder, RoundedCornerShape(10.dp))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = baseDayShift.type.iconEmoji, fontSize = 18.sp)
                            }
                        }

                        Column {
                            Text(
                                text = trEn("ASIL GÖREV ÖZETİ (TARİH SEÇ)", "BASE SHIFT SUMMARY (PICK DATE)"),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = dyn.textMuted,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = if (baseDayShift.type.isWork) {
                                    "${baseDayShift.startTime} – ${baseDayShift.endTime} ${baseDayShift.type.localizedTitle(isEn)}"
                                } else {
                                    trEn("${baseDayShift.type.localizedTitle(false)} Günü", "${baseDayShift.type.localizedTitle(true)} Day")
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = dyn.textPrimary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(dyn.dayShiftBg, RoundedCornerShape(6.dp))
                            .border(1.dp, dyn.dayShiftBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (baseDayShift.type.isWork) trEn("VARDİYA", "SHIFT") else trEn("BOŞ GÜN", "DAY OFF"),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.dayShiftAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Icon(
                            imageVector = Icons.Filled.WorkOutline,
                            contentDescription = trEn("Birim", "Department"),
                            tint = dyn.textSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = settings.department,
                            fontSize = 11.sp,
                            color = dyn.textSecondary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(imageVector = Icons.Filled.CalendarMonth, contentDescription = trEn("Tarih Değiştir", "Change Date"), tint = dyn.accentCyan, modifier = Modifier.size(13.dp))
                        Text(
                            text = selectedDate.format(dateFmt),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.accentCyan
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 2: Mesai / Görev Modu
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Tune,
                        contentDescription = null,
                        tint = dyn.textMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = trEn("MESAİ / GÖREV MODU", "OVERTIME / DUTY MODE"),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = dyn.textMuted,
                        letterSpacing = 0.5.sp
                    )
                }
                val modeLabel = when (selectedModeIndex) {
                    0 -> trEn("UZATMA", "EXTENSION")
                    1 -> trEn("İZİN GÜNÜ", "DAY OFF")
                    2 -> trEn("İCAP", "ON-CALL")
                    else -> trEn("BAYRAM", "HOLIDAY")
                }
                Text(
                    text = trEn("SEÇİLDİ: $modeLabel", "SELECTED: $modeLabel"),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = dyn.accentCyan
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2x2 Modes Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Mode 0: Vardiya Uzatma
                val isSelected0 = selectedModeIndex == 0
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { selectedModeIndex = 0 }
                        .border(
                            1.dp,
                            if (isSelected0) dyn.dayShiftAccent else dyn.borderSubtle,
                            RoundedCornerShape(14.dp)
                        ),
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected0) dyn.activeChipBg else dyn.surfaceCard
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Schedule,
                                contentDescription = null,
                                tint = if (isSelected0) dyn.dayShiftAccent else dyn.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (isSelected0) dyn.dayShiftAccent else dyn.borderMetal,
                                        CircleShape
                                    )
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = trEn("Vardiya Uzatma", "Shift Extension"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.textPrimary
                        )
                        Text(
                            text = trEn("+4s Nöbet Devamı", "+4h Shift Continuation"),
                            fontSize = 10.sp,
                            color = dyn.textSecondary
                        )
                    }
                }

                // Mode 1: İzin Günü Mesaisi
                val isSelected1 = selectedModeIndex == 1
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { selectedModeIndex = 1 }
                        .border(
                            1.dp,
                            if (isSelected1) dyn.dayShiftAccent else dyn.borderSubtle,
                            RoundedCornerShape(14.dp)
                        ),
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected1) dyn.activeChipBg else dyn.surfaceCard
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CalendarMonth,
                                contentDescription = null,
                                tint = if (isSelected1) dyn.dayShiftAccent else dyn.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (isSelected1) dyn.dayShiftAccent else dyn.borderMetal,
                                        CircleShape
                                    )
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = trEn("İzin Günü Mesaisi", "Day-Off Overtime"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.textPrimary
                        )
                        Text(
                            text = trEn("Tam Nöbet (8-24s)", "Full Shift (8-24h)"),
                            fontSize = 10.sp,
                            color = dyn.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Mode 2: İcap / Acil Çağrı
                val isSelected2 = selectedModeIndex == 2
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { selectedModeIndex = 2 }
                        .border(
                            1.dp,
                            if (isSelected2) dyn.dayShiftAccent else dyn.borderSubtle,
                            RoundedCornerShape(14.dp)
                        ),
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected2) dyn.activeChipBg else dyn.surfaceCard
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = null,
                                tint = if (isSelected2) dyn.dayShiftAccent else dyn.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (isSelected2) dyn.dayShiftAccent else dyn.borderMetal,
                                        CircleShape
                                    )
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = trEn("İcap / Acil Çağrı", "On-Call / Standby"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.textPrimary
                        )
                        Text(
                            text = trEn("On-Call Bekleme", "Emergency Standby"),
                            fontSize = 10.sp,
                            color = dyn.textSecondary
                        )
                    }
                }

                // Mode 3: Resmi Tatil / Bayram
                val isSelected3 = selectedModeIndex == 3
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { selectedModeIndex = 3 }
                        .border(
                            1.dp,
                            if (isSelected3) dyn.dayShiftAccent else dyn.borderSubtle,
                            RoundedCornerShape(14.dp)
                        ),
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected3) dyn.activeChipBg else dyn.surfaceCard
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.EventAvailable,
                                contentDescription = trEn("Resmi Tatil", "Public Holiday"),
                                tint = dyn.eveningShiftAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .background(dyn.eveningShiftBg, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "2.0x",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = dyn.eveningShiftAccent
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = trEn("Resmi Tatil / Bayram", "Public Holiday"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.textPrimary
                        )
                        Text(
                            text = trEn("%100 Zamlı Tarife", "100% Holiday Rate"),
                            fontSize = 10.sp,
                            color = dyn.textSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 3: Mesai Saat Aralığı
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            color = dyn.surfaceCard
        ) {
            Column(
                modifier = Modifier
                    .border(1.dp, dyn.borderSubtle, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = null,
                            tint = dyn.accentCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = trEn("MESAİ SAAT ARALIĞI", "OVERTIME TIME RANGE"),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.textMuted,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = trEn("Giriş: Vardiya Bitişi", "Start: End of Shift"),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = dyn.accentCyan
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Time Blocks (Interactive Başlangıç & Bitiş Pickers)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Başlangıç (Tappable ShiftTimePickerSheet)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showStartTimePicker = true }
                            .border(1.dp, dyn.dayShiftBorder, RoundedCornerShape(12.dp))
                            .testTag("btn_overtime_start_time"),
                        shape = RoundedCornerShape(12.dp),
                        color = dyn.surfaceElevated
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = trEn("BAŞLANGIÇ (SEÇ)", "START (PICK)"), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = dyn.textMuted)
                                Icon(
                                    imageVector = Icons.Filled.Schedule,
                                    contentDescription = trEn("Başlangıç Saati Seç", "Pick Start Time"),
                                    tint = dyn.accentCyan,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = startTimeStr,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = dyn.textPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(text = trEn("Dokun & Ayarla", "Tap to Edit"), fontSize = 9.sp, color = dyn.accentCyan)
                        }
                    }

                    // Bitiş (Tappable ShiftTimePickerSheet)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showEndTimePicker = true }
                            .border(1.dp, dyn.dayShiftBorder, RoundedCornerShape(12.dp))
                            .testTag("btn_overtime_end_time"),
                        shape = RoundedCornerShape(12.dp),
                        color = dyn.surfaceElevated
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (crossesMidnight) trEn("BİTİŞ (ERTESİ GÜN)", "END (NEXT DAY)") else trEn("BİTİŞ SAATİ", "END TIME"),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = dyn.textMuted
                                )
                                Icon(
                                    imageVector = Icons.Filled.Schedule,
                                    contentDescription = trEn("Bitiş Saati Seç", "Pick End Time"),
                                    tint = dyn.accentCyan,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = currentEndStr,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = dyn.accentCyan,
                                    fontFamily = FontFamily.Monospace
                                )
                                if (crossesMidnight) {
                                    Box(
                                        modifier = Modifier
                                            .background(dyn.dayShiftBg, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isEn) "+1D" else "+1G",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = dyn.dayShiftAccent
                                        )
                                    }
                                }
                            }
                            Text(text = trEn("Dokun & Ayarla", "Tap to Edit"), fontSize = 9.sp, color = dyn.textMuted)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Presets Pills (+2s, +4s, +6s, +8s)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(2, 4, 6, 8).forEach { hrs ->
                        val isSelected = selectedDurationMinutes == hrs * 60
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedDurationMinutes = hrs * 60 }
                                .border(
                                    1.dp,
                                    if (isSelected) dyn.primaryNeon else dyn.borderSubtle,
                                    RoundedCornerShape(8.dp)
                                ),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) dyn.primaryNeon else dyn.surfaceElevated
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+${hrs}$hourUnit",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) dyn.textOnPrimary else dyn.textPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Net Duration Bar
                val netH = selectedDurationMinutes / 60
                val netM = selectedDurationMinutes % 60
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, dyn.dayShiftBorder, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    color = dyn.activeChipBg
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(dyn.dayShiftAccent.copy(alpha = 0.16f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Schedule,
                                    contentDescription = null,
                                    tint = dyn.dayShiftAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = trEn("NET MESAİ SÜRESİ", "NET OVERTIME DURATION"),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = dyn.textMuted
                                )
                                Text(
                                    text = if (isEn) {
                                        String.format(Locale.ROOT, "%02dh %02dm", netH, netM)
                                    } else {
                                        String.format(Locale.ROOT, "%02ds %02ddk", netH, netM)
                                    },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = dyn.dayShiftText
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = trEn("ARALIK", "RANGE"),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = dyn.dayShiftAccent
                            )
                            Text(
                                text = "$startTimeStr - $currentEndStr",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = dyn.textSecondary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Save Button (Primary Hardware Gradient)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(50.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable {
                    val modeTag = when (selectedModeIndex) {
                        0 -> trEn(isEn, "Vardiya Uzatma", "Shift Extension")
                        1 -> trEn(isEn, "İzin Günü Mesaisi", "Day-Off Overtime")
                        2 -> trEn(isEn, "İcap / On-Call", "On-Call / Standby")
                        else -> trEn(isEn, "Resmi Tatil / Bayram Mesaisi", "Public Holiday Overtime")
                    }
                    val resolvedType = when {
                        selectedModeIndex == 0 && baseDayShift.type.isWork -> baseDayShift.type
                        selectedModeIndex == 1 || selectedModeIndex == 3 -> ShiftType.OZEL
                        else -> if (baseDayShift.type.isWork) baseDayShift.type else ShiftType.OZEL
                    }
                    val finalNote = "$modeTag (+${selectedDurationHours}$hourUnit • $startTimeStr-$currentEndStr)"
                    viewModel.updateDayShift(
                        date = selectedDate,
                        type = resolvedType,
                        overtimeHours = durationHoursDecimal,
                        note = finalNote,
                        startTime = if (baseDayShift.type.isWork) baseDayShift.startTime else startTimeStr,
                        endTime = if (baseDayShift.type.isWork) baseDayShift.endTime else currentEndStr
                    )
                    Toast.makeText(
                        context,
                        trEn(
                            isEn,
                            "Mesai Kaydedildi: +${selectedDurationHours}s (${selectedDate.format(dateFmt)})",
                            "Overtime Saved: +${selectedDurationHours}h (${selectedDate.format(dateFmt)})"
                        ),
                        Toast.LENGTH_LONG
                    ).show()
                    viewModel.navigateTo(AppScreen.TIMESHEET)
                }
                .testTag("btn_save_overtime"),
            shape = RoundedCornerShape(14.dp),
            color = dyn.primaryDeep
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.EditNote,
                        contentDescription = trEn("Kaydet", "Save"),
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = trEn("Mesaiyi Kaydet ve Çizelgeye İşle", "Save Overtime & Add to Timesheet"),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Cancel / Back Button
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable { viewModel.navigateTo(AppScreen.HOME) }
                .border(1.dp, dyn.borderSubtle, RoundedCornerShape(14.dp)),
            shape = RoundedCornerShape(14.dp),
            color = dyn.surfaceElevated
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = trEn("İptal", "Cancel"),
                    tint = dyn.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = trEn("İşlemi İptal Et / Geri Dön", "Cancel / Go Back"),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = dyn.textSecondary
                )
            }
        }
    }

    if (showDatePicker) {
        TitaniumDatePickerDialog(
            title = trEn("Mesai Günü Seçin", "Select Overtime Date"),
            initialDate = selectedDate,
            onDismiss = { showDatePicker = false },
            onConfirm = { selectedDate = it }
        )
    }

    if (showStartTimePicker) {
        ShiftTimePickerSheet(
            title = trEn("Mesai Başlangıç Saati", "Overtime Start Time"),
            subtitle = trEn("Fazla mesai veya ek nöbetin başlama saati", "Start time for overtime or extra duty"),
            initialTime = startTimeStr,
            quickPresets = listOf("08:00", "16:00", "18:00", "20:00", "22:00", "00:00"),
            onDismiss = { showStartTimePicker = false },
            onConfirm = { picked ->
                startTimeStr = picked
            }
        )
    }

    if (showEndTimePicker) {
        ShiftTimePickerSheet(
            title = trEn("Mesai Bitiş Saati", "Overtime End Time"),
            subtitle = trEn("Bitiş saati net mesai süresini otomatik hesaplar", "End time automatically calculates net overtime"),
            initialTime = currentEndStr,
            quickPresets = listOf("20:00", "22:00", "00:00", "02:00", "04:00", "08:00"),
            onDismiss = { showEndTimePicker = false },
            onConfirm = { picked ->
                selectedDurationMinutes = recomputeDurationFromTimes(startTimeStr, picked)
            }
        )
    }
}
