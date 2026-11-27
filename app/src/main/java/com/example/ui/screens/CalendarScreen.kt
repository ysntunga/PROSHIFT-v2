package com.example.ui.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.engine.ShiftEngine
import com.example.data.model.ShiftDay
import com.example.data.model.ShiftType
import com.example.data.notification.ShiftNotificationHelper
import com.example.ui.components.ManualColleagueSheet
import com.example.ui.components.ProShiftBrandHeader
import com.example.ui.components.ShiftTimePickerSheet
import com.example.ui.components.UserAvatarImage
import com.example.ui.components.shiftTypeVector
import com.example.ui.components.titaniumBrushedSurface
import com.example.ui.i18n.LocalAppLocale
import com.example.ui.i18n.LocalIsEnglish
import com.example.ui.i18n.formatHourUnit
import com.example.ui.i18n.localizedCode
import com.example.ui.i18n.localizedName
import com.example.ui.i18n.localizedTitle
import com.example.ui.i18n.trEn
import com.example.ui.theme.DynamicTheme
import com.example.ui.theme.ProshiftMonoFont
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.ShiftViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private enum class CalendarFilter(val trLabel: String, val enLabel: String) {
    ALL("Tümü", "All"),
    DAY("Gündüz", "Day"),
    NIGHT("Gece", "Night"),
    OFF("İzin", "Off"),
    OVERTIME("Mesai", "Overtime");

    fun label(isEn: Boolean): String = if (isEn) enLabel else trLabel
}

@Composable
fun CalendarScreen(
    viewModel: ShiftViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedYear by viewModel.selectedYear.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val days by viewModel.monthShiftDays.collectAsState()
    val settings by viewModel.userSettings.collectAsState()
    val colleagues by viewModel.colleagues.collectAsState()
    val selectedColleague by viewModel.selectedColleague.collectAsState()
    val lastQrSyncReceipt by viewModel.lastQrSyncReceipt.collectAsState()

    var reminderToggle by remember(settings.shiftReminderEnabled) { mutableStateOf(settings.shiftReminderEnabled) }
    var activeFilter by remember { mutableStateOf(CalendarFilter.ALL) }
    var showReminderTimePicker by remember { mutableStateOf(false) }
    var viewingFriendCalendar by remember { mutableStateOf(false) }
    var showColleagueChooser by remember { mutableStateOf(false) }
    var showManualAddSheet by remember { mutableStateOf(false) }

    val calendarNotifPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            reminderToggle = true
            viewModel.updateSettings(settings.copy(shiftReminderEnabled = true))
        }
    }

    LaunchedEffect(selectedColleague) {
        if (selectedColleague == null) {
            viewingFriendCalendar = false
        }
    }

    val activeColleague = selectedColleague
    val isViewingFriend = viewingFriendCalendar && activeColleague != null

    val infiniteTransition = rememberInfiniteTransition(label = "calPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "calPulseAlpha"
    )

    val isEn = LocalIsEnglish.current
    val appLocale = LocalAppLocale.current
    val hourUnit = formatHourUnit(isEn)
    val dayShort = if (isEn) "d" else "G"

    val today by viewModel.currentDate.collectAsState()
    val monthDate = remember(selectedYear, selectedMonth) { LocalDate.of(selectedYear, selectedMonth, 1) }
    val monthTitle = remember(monthDate, appLocale) {
        monthDate.format(DateTimeFormatter.ofPattern("MMMM yyyy", appLocale))
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(appLocale) else it.toString() }
    }

    val displayedDays = remember(days, isViewingFriend, activeColleague, settings) {
        if (isViewingFriend) {
            days.map { viewModel.repository.getColleagueShiftDay(activeColleague, it.date, settings) }
        } else {
            days
        }
    }

    // Dynamic calculations from live `displayedDays`
    val morningOrDayDays = remember(displayedDays) {
        displayedDays.filter { it.type.isWork && !it.type.isNight && it.type != ShiftType.AKSAM }
    }
    val aksamDays = remember(displayedDays) { displayedDays.filter { it.type == ShiftType.AKSAM } }
    val geceDays = remember(displayedDays) { displayedDays.filter { it.type.isWork && it.type.isNight } }
    val izinDays = remember(displayedDays) { displayedDays.filter { !it.type.isWork } }

    val morningOrDayHours = remember(morningOrDayDays) { morningOrDayDays.sumOf { it.totalHours }.toInt() }
    val aksamHours = remember(aksamDays) { aksamDays.sumOf { it.totalHours }.toInt() }
    val geceHours = remember(geceDays) { geceDays.sumOf { it.totalHours }.toInt() }
    val offOvertimeHours = remember(izinDays) { izinDays.sumOf { it.overtimeHours }.toInt() }
    val displayedTotalWorkHours = remember(displayedDays) { displayedDays.sumOf { it.totalHours }.toInt() }

    val activePreset = if (isViewingFriend) activeColleague.cyclePreset else settings.cyclePreset
    val activeSeq = if (isViewingFriend) {
        activeColleague.customSequence?.takeIf { it.isNotEmpty() } ?: activeColleague.cyclePreset.sequence
    } else {
        ShiftEngine.resolvePattern(settings)
    }
    val has8hShifts = remember(displayedDays, activeSeq) {
        activeSeq.any { it == ShiftType.SABAH || it == ShiftType.AKSAM || it == ShiftType.GECE8 } ||
            displayedDays.any { it.type == ShiftType.SABAH || it.type == ShiftType.AKSAM || it.type == ShiftType.GECE8 }
    }

    // Build dynamic 7-column calendar grid rows from `displayedDays`
    val gridRows = remember(displayedDays, selectedYear, selectedMonth, today, isViewingFriend, activeColleague, settings, isEn) {
        val leadingEmptyCount = (monthDate.dayOfWeek.value - 1).coerceIn(0, 6)
        val prevMonthDate = monthDate.minusMonths(1)
        val prevMonthLength = prevMonthDate.lengthOfMonth()

        val cells = mutableListOf<CalDayItem>()
        for (i in 0 until leadingEmptyCount) {
            val prevDayNum = prevMonthLength - leadingEmptyCount + 1 + i
            val prevDate = prevMonthDate.withDayOfMonth(prevDayNum)
            val prevShift = if (isViewingFriend) {
                viewModel.repository.getColleagueShiftDay(activeColleague, prevDate, settings)
            } else {
                viewModel.repository.getShiftForDate(prevDate, settings)
            }
            cells.add(
                CalDayItem(
                    dayNum = prevDayNum.toString(),
                    shiftLabel = formatCalendarCellLabel(prevShift, isEn),
                    type = CellType.INACTIVE,
                    date = prevDate,
                    shiftDay = prevShift
                )
            )
        }
        displayedDays.forEach { shiftDay ->
            val cellType = when {
                shiftDay.date == today -> CellType.TODAY
                shiftDay.type == ShiftType.RAPOR -> CellType.REPORT
                shiftDay.type == ShiftType.AKSAM -> CellType.EVENING
                shiftDay.type.isNight -> CellType.NIGHT
                shiftDay.type.isWork -> CellType.DAY
                else -> CellType.OFF
            }
            val friendShift = if (!isViewingFriend && settings.showFriendOnCalendar && activeColleague != null) {
                viewModel.repository.getColleagueShiftForDate(activeColleague, shiftDay.date)
            } else null
            cells.add(
                CalDayItem(
                    dayNum = shiftDay.date.dayOfMonth.toString(),
                    shiftLabel = formatCalendarCellLabel(shiftDay, isEn),
                    type = cellType,
                    date = shiftDay.date,
                    shiftDay = shiftDay,
                    friendShift = friendShift
                )
            )
        }
        while (cells.size % 7 != 0) {
            cells.add(CalDayItem("", "", CellType.EMPTY, null, null))
        }
        cells.chunked(7)
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
            .padding(bottom = 96.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProShiftBrandHeader(
                sectionLabel = trEn("TAKVİM", "CALENDAR")
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Sleek Friend Matching / Colleague Sync quick icon button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .titaniumBrushedSurface(
                            shape = CircleShape,
                            borderColor = dyn.borderSubtle,
                            elevated = true
                        )
                        .clickable { viewModel.navigateTo(AppScreen.MATCHING_QR) }
                        .testTag("btn_calendar_open_matching"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.People,
                        contentDescription = trEn("Arkadaş Eşleşme", "Shift Matching"),
                        tint = dyn.accentCyan,
                        modifier = Modifier.size(21.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { viewModel.navigateTo(AppScreen.PROFILE) },
                    contentAlignment = Alignment.Center
                ) {
                    UserAvatarImage(
                        avatarPath = settings.avatarUriOrPath,
                        contentDescription = trEn("Profil", "Profile"),
                        displayName = settings.employeeName,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, dyn.borderMetal, CircleShape)
                    )
                }
            }
        }

        // Clean Month Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = monthTitle,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = dyn.textPrimary
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = dyn.surfaceCard,
                    modifier = Modifier
                        .height(38.dp)
                        .border(1.dp, dyn.borderSubtle, RoundedCornerShape(10.dp))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(34.dp).clickable { viewModel.previousMonth() }.testTag("btn_prev_month"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.ChevronLeft, contentDescription = trEn("Önceki Ay", "Previous Month"), tint = dyn.textSecondary, modifier = Modifier.size(18.dp))
                        }
                        Box(
                            modifier = Modifier.size(34.dp).clickable { viewModel.nextMonth() }.testTag("btn_next_month"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.ChevronRight, contentDescription = trEn("Sonraki Ay", "Next Month"), tint = dyn.textSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = dyn.activeChipBg,
                    modifier = Modifier
                        .height(38.dp)
                        .border(1.dp, dyn.dayShiftBorder, RoundedCornerShape(10.dp))
                        .clickable { viewModel.goToToday() }
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(trEn("Bugün", "Today"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = dyn.dayShiftAccent)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = dyn.surfaceCard,
                    modifier = Modifier
                        .size(38.dp)
                        .border(1.dp, dyn.borderSubtle, RoundedCornerShape(10.dp))
                        .clickable {
                            viewModel.inspectDay(viewModel.repository.getShiftForDate(LocalDate.now()))
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Add, contentDescription = trEn("Bugüne Vardiya Ekle", "Add Shift Today"), tint = dyn.accentCyan, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (!settings.isAnchorConfigured && !viewingFriendCalendar) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { viewModel.navigateTo(AppScreen.WIZARD) }
                    .testTag("banner_anchor_setup_prompt"),
                shape = RoundedCornerShape(14.dp),
                color = dyn.eveningShiftBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, dyn.eveningShiftBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = trEn("Çapa Tarihi Ayarlanmadı", "Anchor Date Not Set"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = dyn.eveningShiftAccent
                        )
                        Text(
                            text = trEn("Döngünüzün başlangıç tarihini ve ilk nöbetini seçmek için dokunun.", "Tap to pick your cycle start date and first shift."),
                            fontSize = 10.sp,
                            color = dyn.eveningShiftText
                        )
                    }
                    Text(
                        text = trEn("Kur →", "Set →"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = dyn.eveningShiftAccent
                    )
                }
            }
        }

        lastQrSyncReceipt?.let { receipt ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 10.dp)
                    .testTag("banner_qr_auto_sync_receipt"),
                shape = RoundedCornerShape(14.dp),
                color = dyn.emeraldBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, dyn.emeraldBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (receipt.appliedAsOwnShift) {
                                trEn("QR Vardiyası Takviminize Uygulandı", "QR Shift Applied to Your Calendar")
                            } else {
                                trEn("QR Takvime İşlendi: ${receipt.colleague.name}", "QR Synced: ${receipt.colleague.name}")
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = dyn.emeraldAccent
                        )
                        Text(
                            text = if (receipt.appliedAsOwnShift) {
                                trEn(
                                    "${receipt.colleague.cyclePreset.localizedName(false)} aktif takviminiz olarak ayarlandı.",
                                    "${receipt.colleague.cyclePreset.localizedName(true)} is now your active calendar."
                                )
                            } else {
                                trEn(
                                    "${receipt.colleague.cyclePreset.localizedName(false)} • ${receipt.commonOffCount} ortak izin katmanda görünür",
                                    "${receipt.colleague.cyclePreset.localizedName(true)} • ${receipt.commonOffCount} shared off days shown"
                                )
                            },
                            fontSize = 10.sp,
                            color = dyn.emeraldText
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (!receipt.appliedAsOwnShift) {
                            Text(
                                text = trEn("Gör", "View"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = dyn.emeraldAccent,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        viewingFriendCalendar = true
                                        viewModel.clearQrSyncReceipt()
                                    }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = trEn("Kapat", "Close"),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.emeraldText,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { viewModel.clearQrSyncReceipt() }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Calendar Owner Switcher (Benim Takvimim vs Arkadaşım)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { viewingFriendCalendar = false }
                    .border(
                        1.dp,
                        if (!viewingFriendCalendar) dyn.dayShiftAccent else dyn.borderSubtle,
                        RoundedCornerShape(12.dp)
                    )
                    .testTag("btn_calendar_mode_mine"),
                shape = RoundedCornerShape(12.dp),
                color = if (!viewingFriendCalendar) dyn.activeChipBg else dyn.surfaceCard
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = trEn("Benim Takvimim", "My Calendar"),
                        tint = if (!viewingFriendCalendar) dyn.dayShiftAccent else dyn.textSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = trEn("Benim Takvimim", "My Calendar"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (!viewingFriendCalendar) dyn.dayShiftAccent else dyn.textSecondary,
                        maxLines = 1
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        if (activeColleague == null) {
                            showColleagueChooser = true
                        } else if (isViewingFriend) {
                            showColleagueChooser = true
                        } else {
                            viewingFriendCalendar = true
                        }
                    }
                    .border(
                        1.dp,
                        if (isViewingFriend) dyn.eveningShiftAccent else dyn.borderSubtle,
                        RoundedCornerShape(12.dp)
                    )
                    .testTag("btn_calendar_mode_friend"),
                shape = RoundedCornerShape(12.dp),
                color = if (isViewingFriend) dyn.eveningShiftBg else dyn.surfaceCard
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.People,
                        contentDescription = trEn("Arkadaşımın Takvimi", "Friend Calendar"),
                        tint = if (isViewingFriend) dyn.eveningShiftAccent else dyn.textSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = activeColleague?.let {
                            it.name.substringBefore(" ") + if (isViewingFriend) " ▼" else ""
                        } ?: trEn("Arkadaş Ekle", "Add Friend"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isViewingFriend) dyn.eveningShiftAccent else dyn.textSecondary,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Active Schedule Info Card (Benim Vardiyam veya Arkadaşım)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .titaniumBrushedSurface(
                    shape = RoundedCornerShape(20.dp),
                    borderColor = if (isViewingFriend) dyn.eveningShiftBorder else dyn.borderSubtle
                ),
            shape = RoundedCornerShape(20.dp),
            color = Color.Transparent
        ) {
            Column(
                modifier = Modifier.padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(if (isViewingFriend) dyn.eveningShiftAccent else dyn.accentCyan, CircleShape)
                        )
                        Text(
                            text = if (isViewingFriend) {
                                "${activeColleague.name} • ${activePreset.localizedName(isEn)}"
                            } else {
                                "${activePreset.localizedName(isEn)} • ${displayedTotalWorkHours}$hourUnit"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.textPrimary,
                            maxLines = 1
                        )
                    }
                    if (isViewingFriend) {
                        Text(
                            text = trEn("Değiştir", "Change"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.eveningShiftAccent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { showColleagueChooser = true }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    } else {
                        Text(
                            text = trEn("+ Mesai", "+ Overtime"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.eveningShiftAccent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { viewModel.navigateTo(AppScreen.ADD_OVERTIME) }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (has8hShifts || aksamDays.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Sabah Box
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = dyn.dayShiftBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, dyn.dayShiftBorder)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Icon(shiftTypeVector(ShiftType.SABAH), contentDescription = trEn("Sabah", "Morning"), tint = dyn.dayShiftAccent, modifier = Modifier.size(11.dp))
                                    Text(trEn("Sabah", "Morning"), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = dyn.dayShiftAccent, maxLines = 1)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${morningOrDayDays.size}$dayShort", fontSize = 9.sp, color = dyn.dayShiftAccent)
                                    Text("${morningOrDayHours}$hourUnit", fontSize = 12.sp, fontFamily = ProshiftMonoFont, fontWeight = FontWeight.Black, color = dyn.dayShiftText)
                                }
                            }
                        }

                        // Akşam Box
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = dyn.eveningShiftBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, dyn.eveningShiftBorder)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Icon(shiftTypeVector(ShiftType.AKSAM), contentDescription = trEn("Akşam", "Evening"), tint = dyn.eveningShiftAccent, modifier = Modifier.size(11.dp))
                                    Text(trEn("Akşam", "Evening"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = dyn.eveningShiftAccent, maxLines = 1)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${aksamDays.size}$dayShort", fontSize = 10.sp, color = dyn.eveningShiftAccent)
                                    Text("${aksamHours}$hourUnit", fontSize = 12.sp, fontFamily = ProshiftMonoFont, fontWeight = FontWeight.Black, color = dyn.eveningShiftText)
                                }
                            }
                        }

                        // Gece Box
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = dyn.nightShiftBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, dyn.nightShiftBorder)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Icon(shiftTypeVector(ShiftType.GECE), contentDescription = trEn("Gece", "Night"), tint = dyn.nightShiftAccent, modifier = Modifier.size(11.dp))
                                    Text(trEn("Gece", "Night"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = dyn.nightShiftAccent, maxLines = 1)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${geceDays.size}$dayShort", fontSize = 10.sp, color = dyn.nightShiftAccent)
                                    Text("${geceHours}$hourUnit", fontSize = 12.sp, fontFamily = ProshiftMonoFont, fontWeight = FontWeight.Black, color = dyn.nightShiftText)
                                }
                            }
                        }

                        // İzin Box
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = dyn.offShiftBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, dyn.offShiftBorder)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Icon(shiftTypeVector(ShiftType.IZIN), contentDescription = trEn("İzin", "Off"), tint = dyn.offShiftAccent, modifier = Modifier.size(11.dp))
                                    Text(trEn("İzin", "Off"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = dyn.offShiftAccent, maxLines = 1)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${izinDays.size}$dayShort", fontSize = 10.sp, color = dyn.textMuted)
                                    Text(
                                        text = if (offOvertimeHours > 0) "+${offOvertimeHours}$hourUnit" else "—",
                                        fontSize = 12.sp,
                                        fontFamily = ProshiftMonoFont,
                                        fontWeight = FontWeight.Bold,
                                        color = if (offOvertimeHours > 0) dyn.eveningShiftAccent else dyn.offShiftAccent
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Gündüz Box
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = dyn.dayShiftBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, dyn.dayShiftBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Icon(shiftTypeVector(ShiftType.GUNDUZ), contentDescription = trEn("Gündüz", "Day"), tint = dyn.dayShiftAccent, modifier = Modifier.size(12.dp))
                                        Text(trEn("Gündüz", "Day"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = dyn.dayShiftAccent)
                                    }
                                    Text("(${morningOrDayDays.size}$dayShort)", fontSize = 10.sp, color = dyn.dayShiftAccent)
                                }
                                Text("${morningOrDayHours}$hourUnit", fontSize = 14.sp, fontFamily = ProshiftMonoFont, fontWeight = FontWeight.Black, color = dyn.dayShiftText)
                            }
                        }

                        // Gece Box
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = dyn.nightShiftBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, dyn.nightShiftBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Icon(shiftTypeVector(ShiftType.GECE), contentDescription = trEn("Gece", "Night"), tint = dyn.nightShiftAccent, modifier = Modifier.size(12.dp))
                                        Text(trEn("Gece", "Night"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = dyn.nightShiftAccent)
                                    }
                                    Text("(${geceDays.size}$dayShort)", fontSize = 10.sp, color = dyn.nightShiftAccent)
                                }
                                Text("${geceHours}$hourUnit", fontSize = 14.sp, fontFamily = ProshiftMonoFont, fontWeight = FontWeight.Black, color = dyn.nightShiftText)
                            }
                        }

                        // İzin Box
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = dyn.offShiftBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, dyn.offShiftBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Icon(shiftTypeVector(ShiftType.IZIN), contentDescription = trEn("İzin", "Off"), tint = dyn.offShiftAccent, modifier = Modifier.size(12.dp))
                                        Text(trEn("İzin", "Off"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = dyn.offShiftAccent)
                                    }
                                    Text("(${izinDays.size}$dayShort)", fontSize = 10.sp, color = dyn.textMuted)
                                }
                                Text(
                                    text = if (offOvertimeHours > 0) "+${offOvertimeHours}$hourUnit" else "—",
                                    fontSize = 14.sp,
                                    fontFamily = ProshiftMonoFont,
                                    fontWeight = FontWeight.Bold,
                                    color = if (offOvertimeHours > 0) dyn.eveningShiftAccent else dyn.offShiftAccent
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal Filter Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(CalendarFilter.entries) { filter ->
                val isSel = activeFilter == filter
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (isSel) dyn.activeChipBg else dyn.surfaceCard,
                    modifier = Modifier
                        .border(
                            1.dp,
                            if (isSel) dyn.primaryNeon else dyn.borderSubtle,
                            RoundedCornerShape(999.dp)
                        )
                        .clip(RoundedCornerShape(999.dp))
                        .clickable { activeFilter = filter }
                        .testTag("cal_filter_${filter.name.lowercase()}")
                ) {
                    Text(
                        text = filter.label(isEn),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) dyn.primaryNeon else dyn.textSecondary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Minimal Legend Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendDotLabel(color = dyn.dayShiftAccent, label = if (has8hShifts) trEn("Sabah", "Morning") else trEn("Gündüz", "Day"))
            if (has8hShifts || aksamDays.isNotEmpty()) {
                LegendDotLabel(color = dyn.eveningShiftAccent, label = trEn("Akşam", "Evening"))
            }
            LegendDotLabel(color = dyn.nightShiftAccent, label = trEn("Gece", "Night"))
            if (settings.autoHolidaysEnabled) {
                LegendDotLabel(color = dyn.emeraldAccent, label = trEn("Tatil", "Holiday"))
            }
            if (settings.showFriendOnCalendar && !isViewingFriend && activeColleague != null) {
                LegendDotLabel(color = dyn.emeraldAccent, label = activeColleague.name.substringBefore(" "))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Dynamic 7-Column Calendar Grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val weekdays = if (isEn) {
                    listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
                } else {
                    listOf("PZT", "SAL", "ÇAR", "PER", "CUM", "CMT", "PAZ")
                }
                weekdays.forEachIndexed { idx, name ->
                    val isWeekend = idx >= 5
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            text = name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isWeekend) dyn.accentCyan else dyn.textSecondary
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                gridRows.forEach { rowDays ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        rowDays.forEach { item ->
                            if (item.type == CellType.EMPTY) {
                                Box(modifier = Modifier.weight(1f).height(62.dp))
                            } else {
                                val dimmedByFilter = item.shiftDay != null && when (activeFilter) {
                                    CalendarFilter.ALL -> false
                                    CalendarFilter.DAY -> !item.shiftDay.type.isWork || item.shiftDay.type.isNight
                                    CalendarFilter.NIGHT -> !item.shiftDay.type.isWork || !item.shiftDay.type.isNight
                                    CalendarFilter.OFF -> item.shiftDay.type.isWork
                                    CalendarFilter.OVERTIME -> !item.shiftDay.isOverride && item.shiftDay.overtimeHours <= 0 && item.shiftDay.note.isBlank()
                                }
                                CalendarGridPillCell(
                                    item = item,
                                    pulseAlpha = pulseAlpha,
                                    dimmed = dimmedByFilter,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        if (item.date != null) {
                                            if (isViewingFriend) {
                                                val friendDay = item.shiftDay
                                                if (friendDay != null) {
                                                    val hoursInfo = if (friendDay.type.isWork) {
                                                        "${friendDay.startTime} - ${friendDay.endTime}"
                                                    } else {
                                                        trEn(isEn, "İzin / İstirahat", "Off / Rest")
                                                    }
                                                    val fmt = DateTimeFormatter.ofPattern("d MMM", appLocale)
                                                    Toast.makeText(
                                                        context,
                                                        "${activeColleague.name} • ${item.date.format(fmt)}: ${friendDay.type.localizedTitle(isEn)} ($hoursInfo)",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            } else {
                                                viewModel.inspectDay(viewModel.repository.getShiftForDate(item.date))
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Compact Single-Row Vardiya Hatırlatıcısı Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .titaniumBrushedSurface(
                    shape = RoundedCornerShape(16.dp),
                    borderColor = dyn.borderSubtle
                ),
            shape = RoundedCornerShape(16.dp),
            color = Color.Transparent
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Filled.Notifications, contentDescription = trEn("Hatırlatıcı", "Reminder"), tint = dyn.accentCyan, modifier = Modifier.size(18.dp))
                    Column {
                        Text(trEn("Vardiya Hatırlatıcısı", "Shift Reminder"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = dyn.textPrimary)
                        Text(trEn("Günlük bildirim saati", "Daily alert time"), fontSize = 10.sp, color = dyn.textSecondary)
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = dyn.surfaceElevated,
                        modifier = Modifier
                            .border(1.dp, dyn.dayShiftBorder, RoundedCornerShape(8.dp))
                            .clickable { showReminderTimePicker = true }
                            .testTag("btn_calendar_reminder_time")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = settings.shiftReminderTime,
                                fontSize = 12.sp,
                                fontFamily = ProshiftMonoFont,
                                fontWeight = FontWeight.Bold,
                                color = dyn.accentCyan
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = trEn("Saati Değiştir", "Change Time"), tint = dyn.accentCyan, modifier = Modifier.size(15.dp))
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = if (reminderToggle) dyn.primaryNeon else dyn.surfacePanel,
                        modifier = Modifier
                            .clickable {
                                val nextEnabled = !reminderToggle
                                if (nextEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !ShiftNotificationHelper.hasNotificationPermission(context)) {
                                    calendarNotifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    reminderToggle = nextEnabled
                                    viewModel.updateSettings(settings.copy(shiftReminderEnabled = nextEnabled))
                                }
                            }
                            .testTag("btn_calendar_reminder_toggle")
                    ) {
                        Text(
                            text = if (reminderToggle) trEn("AÇIK", "ON") else trEn("KAPALI", "OFF"),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (reminderToggle) dyn.textOnPrimary else dyn.textSecondary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }
    }

    if (showReminderTimePicker) {
        ShiftTimePickerSheet(
            title = trEn("Hatırlatma Bildirim Saati", "Reminder Alert Time"),
            subtitle = trEn("Vardiya öncesi günlük alarm saatini seçin", "Select your daily pre-shift reminder time"),
            initialTime = settings.shiftReminderTime,
            quickPresets = listOf("07:00", "12:00", "15:00", "18:00", "20:00", "22:00"),
            onDismiss = { showReminderTimePicker = false },
            onConfirm = { newTime ->
                viewModel.updateReminderTime(newTime)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !ShiftNotificationHelper.hasNotificationPermission(context)) {
                    calendarNotifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        )
    }

    if (showColleagueChooser) {
        AlertDialog(
            onDismissRequest = { showColleagueChooser = false },
            containerColor = dyn.surfaceCard,
            title = {
                Text(
                    text = trEn("Takvimi Gösterilecek Arkadaş", "Select Friend Calendar"),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = dyn.textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (colleagues.isEmpty()) {
                        Text(
                            text = trEn(
                                "Henüz kayıtlı arkadaş yok. QR kod ile veya elle arkadaş ekleyebilirsiniz.",
                                "No saved colleagues yet. Add someone via QR code or manually."
                            ),
                            fontSize = 12.sp,
                            color = dyn.textSecondary,
                            lineHeight = 17.sp
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 260.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            colleagues.forEach { col ->
                                val isSel = activeColleague != null &&
                                    col.qrCodeId == activeColleague.qrCodeId &&
                                    col.name == activeColleague.name
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSel) dyn.activeChipBg else dyn.surfaceElevated)
                                        .border(1.dp, if (isSel) dyn.accentCyan else dyn.borderSubtle, RoundedCornerShape(10.dp))
                                        .clickable {
                                            viewModel.selectColleague(col)
                                            viewingFriendCalendar = true
                                            showColleagueChooser = false
                                        }
                                        .padding(start = 12.dp, top = 6.dp, bottom = 6.dp, end = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(col.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isSel) dyn.accentCyan else dyn.textPrimary)
                                        Text("${col.department} • ${col.cyclePreset.localizedName(isEn)}", fontSize = 10.sp, color = dyn.textSecondary, maxLines = 1)
                                    }
                                    if (isSel) {
                                        Icon(Icons.Filled.Check, contentDescription = trEn("Seçili", "Selected"), tint = dyn.accentCyan, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = { viewModel.removeColleague(col) },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.DeleteOutline,
                                            contentDescription = trEn("${col.name} sil", "Remove ${col.name}"),
                                            tint = dyn.dangerAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                showColleagueChooser = false
                                viewModel.navigateTo(AppScreen.QR_SCANNER)
                            }
                            .border(1.dp, dyn.dayShiftBorder, RoundedCornerShape(10.dp)),
                        color = dyn.activeChipBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.QrCodeScanner, contentDescription = null, tint = dyn.dayShiftAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(trEn("QR ile Arkadaş Ekle", "Add Friend via QR"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = dyn.dayShiftAccent)
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                showColleagueChooser = false
                                showManualAddSheet = true
                            }
                            .border(1.dp, dyn.eveningShiftBorder, RoundedCornerShape(10.dp)),
                        color = dyn.eveningShiftBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, tint = dyn.eveningShiftAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(trEn("Yeni Arkadaş Elle Ekle", "Add Friend Manually"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = dyn.eveningShiftAccent)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showColleagueChooser = false }) {
                    Text(trEn("Kapat", "Close"), color = dyn.textSecondary)
                }
            }
        )
    }

    if (showManualAddSheet) {
        ManualColleagueSheet(
            onDismiss = { showManualAddSheet = false },
            onSave = { profile ->
                viewModel.addAndSelectColleague(profile)
                viewingFriendCalendar = true
                Toast.makeText(
                    context,
                    trEn(isEn, "${profile.name} takvimi açıldı", "Opened ${profile.name}'s calendar"),
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }
}

private fun formatCalendarCellLabel(shiftDay: ShiftDay, isEn: Boolean): String {
    val h = formatHourUnit(isEn)
    return when {
        shiftDay.overtimeHours > 0 -> "+${shiftDay.overtimeHours.toInt()}$h ${shiftDay.type.localizedCode(isEn)}"
        shiftDay.type == ShiftType.SABAH -> "${if (isEn) "M" else "S"} ${shiftDay.startTime.take(2)}-${shiftDay.endTime.take(2)}"
        shiftDay.type == ShiftType.AKSAM -> "${if (isEn) "E" else "A"} ${shiftDay.startTime.take(2)}-${shiftDay.endTime.take(2)}"
        shiftDay.type == ShiftType.GECE8 -> "${if (isEn) "N" else "G"} ${shiftDay.startTime.take(2)}-${shiftDay.endTime.take(2)}"
        shiftDay.type == ShiftType.NOBET24 -> if (isEn) "24h Duty" else "24s Nöbet"
        shiftDay.type.isWork -> "${shiftDay.type.localizedTitle(isEn).substringBefore(" (").take(6)} ${shiftDay.baseHours.toInt()}$h"
        else -> shiftDay.type.localizedTitle(isEn)
    }
}

@Composable
private fun LegendDotLabel(color: Color, label: String) {
    val dyn = DynamicTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(modifier = Modifier.size(7.dp).background(color, CircleShape))
        Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = dyn.textSecondary)
    }
}

private enum class CellType {
    DAY,
    EVENING,
    NIGHT,
    OFF,
    REPORT,
    TODAY,
    INACTIVE,
    EMPTY
}

private data class CalDayItem(
    val dayNum: String,
    val shiftLabel: String,
    val type: CellType,
    val date: LocalDate? = null,
    val shiftDay: ShiftDay? = null,
    val friendShift: ShiftType? = null
)

@Composable
private fun CalendarGridPillCell(
    item: CalDayItem,
    pulseAlpha: Float,
    dimmed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dyn = DynamicTheme.colors
    val (bgBrush, borderColor, textColor, badgeTextColor) = when (item.type) {
        CellType.TODAY -> Quad(
            Brush.verticalGradient(listOf(dyn.primaryNeon, dyn.primaryDeep)),
            dyn.primaryNeon.copy(alpha = pulseAlpha),
            if (dyn.isDark) Color(0xFF030712) else Color.White,
            if (dyn.isDark) Color(0xFF030712) else Color(0xFFE0F2FE)
        )
        CellType.DAY -> Quad(
            Brush.verticalGradient(listOf(dyn.dayShiftBg, dyn.dayShiftBgBottom)),
            dyn.dayShiftBorder,
            dyn.dayShiftText,
            dyn.dayShiftAccent
        )
        CellType.EVENING -> Quad(
            Brush.verticalGradient(listOf(dyn.eveningShiftBg, dyn.eveningShiftBgBottom)),
            dyn.eveningShiftBorder,
            dyn.eveningShiftText,
            dyn.eveningShiftAccent
        )
        CellType.NIGHT -> Quad(
            Brush.verticalGradient(listOf(dyn.nightShiftBg, dyn.nightShiftBgBottom)),
            dyn.nightShiftBorder,
            dyn.nightShiftText,
            dyn.nightShiftAccent
        )
        CellType.REPORT -> Quad(
            Brush.verticalGradient(listOf(dyn.reportShiftBg, dyn.reportShiftBgBottom)),
            dyn.reportShiftBorder,
            dyn.reportShiftText,
            dyn.reportShiftAccent
        )
        CellType.OFF -> Quad(
            Brush.verticalGradient(listOf(dyn.surfaceCard, dyn.offShiftBg)),
            dyn.offShiftBorder,
            dyn.textPrimary,
            dyn.offShiftAccent
        )
        CellType.INACTIVE -> Quad(
            Brush.verticalGradient(listOf(dyn.background, dyn.background)),
            dyn.borderSubtle.copy(alpha = if (dyn.isDark) 0.45f else 0.65f),
            dyn.textMuted,
            dyn.textMuted
        )
        CellType.EMPTY -> Quad(
            Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent)),
            Color.Transparent,
            Color.Transparent,
            Color.Transparent
        )
    }

    val overtimeHours = item.shiftDay?.overtimeHours ?: 0.0
    val hasSpecialNoteOrOverride = item.shiftDay != null && (item.shiftDay.isOverride || item.shiftDay.note.isNotBlank())
    val isCommonOffWithFriend = item.shiftDay != null && item.friendShift != null && !item.shiftDay.type.isWork && !item.friendShift.isWork
    val friendBarColor = when {
        isCommonOffWithFriend -> dyn.emeraldAccent
        item.friendShift == ShiftType.AKSAM -> dyn.eveningShiftAccent
        item.friendShift?.isNight == true -> dyn.nightShiftAccent
        item.friendShift?.isWork == true -> dyn.dayShiftAccent
        else -> dyn.textMuted
    }

    Surface(
        modifier = modifier
            .height(64.dp)
            .alpha(if (dimmed) 0.28f else 1f)
            .clip(RoundedCornerShape(12.dp))
            .background(bgBrush, RoundedCornerShape(12.dp))
            .border(
                width = if (item.type == CellType.TODAY) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = item.type != CellType.EMPTY, onClick = onClick)
            .testTag("cal_cell_${item.date?.dayOfMonth ?: item.dayNum}"),
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 2.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center
        ) {
            if (!item.shiftDay?.holidayName.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 3.dp, top = 1.dp)
                        .size(6.dp)
                        .background(dyn.emeraldAccent, CircleShape)
                )
            }
            if (overtimeHours > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(dyn.eveningShiftAccent, RoundedCornerShape(4.dp))
                        .padding(horizontal = 3.dp, vertical = 1.dp)
                ) {
                    val isEnCell = LocalIsEnglish.current
                    Text(
                        text = "+${overtimeHours.toInt()}${formatHourUnit(isEnCell)}",
                        fontSize = 8.5.sp,
                        fontFamily = ProshiftMonoFont,
                        fontWeight = FontWeight.Black,
                        color = if (dyn.isDark) Color(0xFF030712) else Color.White
                    )
                }
            } else if (hasSpecialNoteOrOverride) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 3.dp, top = 1.dp)
                        .size(6.dp)
                        .background(dyn.eveningShiftAccent, CircleShape)
                )
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.dayNum,
                    fontSize = 14.sp,
                    fontFamily = ProshiftMonoFont,
                    fontWeight = if (item.type == CellType.INACTIVE) FontWeight.Normal else FontWeight.Black,
                    color = textColor
                )
                Text(
                    text = item.shiftLabel,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeTextColor,
                    maxLines = 1
                )
                if (item.friendShift != null && item.type != CellType.INACTIVE && item.type != CellType.EMPTY) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                            .height(3.5.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(friendBarColor)
                    )
                }
            }
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
