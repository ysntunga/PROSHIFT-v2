package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ShiftType
import com.example.ui.components.LeavePetitionDialog
import com.example.ui.components.ProShiftBrandHeader
import com.example.ui.components.UserAvatarImage
import com.example.ui.components.shiftTypeVector
import com.example.ui.components.titaniumBrushedSurface
import com.example.ui.i18n.LocalAppLocale
import com.example.ui.i18n.LocalIsEnglish
import com.example.ui.i18n.formatHourUnit
import com.example.ui.i18n.formatMonthTitleLocalized
import com.example.ui.i18n.formatPercent
import com.example.ui.i18n.trEn
import com.example.ui.theme.DynamicTheme
import com.example.ui.theme.ProshiftMonoFont
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.ShiftViewModel
import java.time.LocalDate
import java.time.Period

@Composable
fun TimesheetScreen(
    viewModel: ShiftViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.timesheetStats.collectAsState()
    val settings by viewModel.userSettings.collectAsState()
    val monthDays by viewModel.monthShiftDays.collectAsState()

    var includeNotesAndBreaks by remember { mutableStateOf(true) }
    var showPetitionDialog by remember { mutableStateOf(false) }
    var showPayrollInfoDialog by remember { mutableStateOf(false) }

    val selectedYear by viewModel.selectedYear.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val isEn = LocalIsEnglish.current
    val appLocale = LocalAppLocale.current
    val hourUnit = formatHourUnit(isEn)
    val today = remember { LocalDate.now() }
    val localizedMonthTitle = remember(selectedYear, selectedMonth, appLocale) {
        formatMonthTitleLocalized(selectedYear, selectedMonth, appLocale).uppercase(appLocale)
    }

    val specialHours = remember(monthDays) {
        monthDays.filter { it.type == ShiftType.OZEL }.sumOf { it.totalHours }.toInt()
    }
    val baseHours = (stats.totalWorkHours - stats.overtimeHours - specialHours).toInt().coerceAtLeast(0)
    val servicePeriod = remember(settings.employmentStartDate, today) {
        Period.between(settings.employmentStartDate, today)
    }
    val seniorityYears = servicePeriod.years.coerceAtLeast(0)
    val seniorityMonths = servicePeriod.months.coerceAtLeast(0)
    val seniorityBracket = when {
        seniorityYears < 1 -> trEn("(1 Yıldan Az)", "(Under 1 Year)")
        seniorityYears in 1..5 && stats.statutoryLeaveRights >= 20 -> trEn("(1-5 Yıl • 50+ Yaş Koruması)", "(1–5 Yrs • 50+ Protection)")
        seniorityYears in 1..5 -> trEn("(1 - 5 Yıl Aralığı)", "(1 – 5 Years)")
        seniorityYears in 6..14 -> trEn("(6 - 14 Yıl Aralığı)", "(6 – 14 Years)")
        else -> trEn("(15+ Yıl Aralığı)", "(15+ Years)")
    }
    val leaveUsageRatio = if (stats.statutoryLeaveRights > 0) {
        (stats.usedLeaveDays.toFloat() / stats.statutoryLeaveRights.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val leaveUsagePercent = (leaveUsageRatio * 100).toInt()

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
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProShiftBrandHeader(
                sectionLabel = trEn("PUANTAJ", "PAYROLL")
            )

            // User Avatar Circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
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

        Spacer(modifier = Modifier.height(4.dp))

        // Period Switcher and SGK Tag Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Month Selector Pill with < > navigation
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, dyn.borderSubtle, RoundedCornerShape(10.dp)),
                shape = RoundedCornerShape(10.dp),
                color = dyn.surfaceCard
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.previousMonth() },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_timesheet_prev_month")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = trEn("Önceki Ay", "Previous Month"),
                            tint = dyn.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = localizedMonthTitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = dyn.textPrimary,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier
                            .clickable { viewModel.goToToday() }
                            .padding(horizontal = 4.dp)
                    )
                    IconButton(
                        onClick = { viewModel.nextMonth() },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_timesheet_next_month")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = trEn("Sonraki Ay", "Next Month"),
                            tint = dyn.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Payroll Esaslar Pill (opens info dialog directly)
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, dyn.dayShiftBorder, RoundedCornerShape(10.dp))
                    .clickable { showPayrollInfoDialog = true },
                shape = RoundedCornerShape(10.dp),
                color = dyn.activeChipBg
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = trEn("Puantaj Esasları", "Payroll Rules"),
                        tint = dyn.dayShiftAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = trEn("4857 SK", "RULES"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = dyn.dayShiftAccent
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3 Stat Tiles in a Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Tile 1: TOPLAM
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .titaniumBrushedSurface(RoundedCornerShape(16.dp), dyn.borderSubtle),
                shape = RoundedCornerShape(16.dp),
                color = Color.Transparent
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = trEn("TOPLAM", "TOTAL"),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.textMuted,
                            letterSpacing = 0.5.sp
                        )
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = trEn("Toplam Saat", "Total Hours"),
                            tint = dyn.dayShiftAccent,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${stats.totalWorkHours.toInt()}",
                            fontSize = 22.sp,
                            fontFamily = ProshiftMonoFont,
                            fontWeight = FontWeight.Black,
                            color = dyn.textPrimary
                        )
                        Text(
                            text = hourUnit,
                            fontSize = 12.sp,
                            color = dyn.textSecondary,
                            modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .background(dyn.dayShiftBg, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = trEn("HEDEF: ${stats.targetWorkHours.toInt()}s", "TARGET: ${stats.targetWorkHours.toInt()}h"),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.dayShiftAccent
                        )
                    }
                }
            }

            // Tile 2: GECE
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .titaniumBrushedSurface(RoundedCornerShape(16.dp), dyn.borderSubtle),
                shape = RoundedCornerShape(16.dp),
                color = Color.Transparent
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = trEn("GECE", "NIGHT"),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.textMuted,
                            letterSpacing = 0.5.sp
                        )
                        Icon(
                            imageVector = shiftTypeVector(ShiftType.GECE),
                            contentDescription = trEn("Gece Vardiyası", "Night Shift"),
                            tint = dyn.nightShiftAccent,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${stats.nightHours.toInt()}",
                            fontSize = 22.sp,
                            fontFamily = ProshiftMonoFont,
                            fontWeight = FontWeight.Black,
                            color = dyn.textPrimary
                        )
                        Text(
                            text = hourUnit,
                            fontSize = 12.sp,
                            color = dyn.textSecondary,
                            modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .background(dyn.nightShiftBg, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = trEn("%50 EK PRİM", "+50% NIGHT"),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.nightShiftAccent
                        )
                    }
                }
            }

            // Tile 3: FAZLA M.
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .titaniumBrushedSurface(RoundedCornerShape(16.dp), dyn.borderSubtle),
                shape = RoundedCornerShape(16.dp),
                color = Color.Transparent
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = trEn("FAZLA M.", "OVERTIME"),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.textMuted,
                            letterSpacing = 0.5.sp
                        )
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = trEn("Fazla Mesai", "Overtime"),
                            tint = dyn.eveningShiftAccent,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "+${stats.overtimeHours.toInt()}",
                            fontSize = 22.sp,
                            fontFamily = ProshiftMonoFont,
                            fontWeight = FontWeight.Black,
                            color = dyn.eveningShiftAccent
                        )
                        Text(
                            text = hourUnit,
                            fontSize = 12.sp,
                            color = dyn.textSecondary,
                            modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .background(dyn.eveningShiftBg, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = trEn("1.5X KATSAYI", "1.5X RATE"),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.eveningShiftAccent
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card 2: Tahmini Hak Ediş Tablosu
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .titaniumBrushedSurface(RoundedCornerShape(20.dp), dyn.borderSubtle),
            shape = RoundedCornerShape(20.dp),
            color = Color.Transparent
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(16.dp)
                                .background(dyn.accentCyan, RoundedCornerShape(2.dp))
                        )
                        Text(
                            text = trEn("Tahmini Hak Ediş Tablosu", "Estimated Payroll Breakdown"),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.textPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(dyn.surfaceElevated)
                            .border(1.dp, dyn.borderSubtle, CircleShape)
                            .clickable { showPayrollInfoDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = trEn("Bordro Hesaplama Bilgisi", "Payroll Calculation Info"),
                            tint = dyn.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Item 1: Taban Çalışma
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = trEn("Taban Çalışma (${stats.targetWorkHours.toInt()}s Standart)", "Base Hours (${stats.targetWorkHours.toInt()}h Standard)"),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.textPrimary
                        )
                        Text(
                            text = trEn("Gündüz & standart vardiya saati", "Day & standard shift hours"),
                            fontSize = 10.sp,
                            color = dyn.textSecondary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${baseHours}$hourUnit",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.textPrimary
                        )
                        Text(
                            text = trEn("${stats.workDaysCount} Nöbet", "${stats.workDaysCount} Shifts"),
                            fontSize = 10.sp,
                            color = dyn.textMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Item 2: Gece Nöbeti Primi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = trEn("Gece Nöbeti Primi", "Night Shift Allowance"),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.textPrimary
                        )
                        Text(
                            text = trEn("${settings.geceStartTime} - ${settings.geceEndTime} arası döngü", "${settings.geceStartTime} - ${settings.geceEndTime} rotation"),
                            fontSize = 10.sp,
                            color = dyn.textSecondary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${stats.nightHours.toInt()}$hourUnit",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.textPrimary
                        )
                        Text(
                            text = trEn("+%50 Fark", "+50% Rate"),
                            fontSize = 10.sp,
                            color = dyn.accentCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Item 3: Haftalık Fazla Çalışma
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = trEn("Ek Mesai & Çağrı Süresi", "Overtime & On-Call Hours"),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.textPrimary
                        )
                        Text(
                            text = trEn("Aylık girilen ek nöbet saatleri", "Extra overtime logged this month"),
                            fontSize = 10.sp,
                            color = dyn.textSecondary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "+${stats.overtimeHours.toInt()}$hourUnit",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.eveningShiftAccent
                        )
                        Text(
                            text = trEn("%150 Zamlı", "150% Rate"),
                            fontSize = 10.sp,
                            color = dyn.eveningShiftAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Item 4: Özel Mesai / İzinli Günler
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = trEn("Özel / Bayram Nöbeti", "Special / Holiday Duty"),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.textPrimary
                        )
                        Text(
                            text = trEn("${stats.offDaysCount} İzin günü / Özel görevler", "${stats.offDaysCount} Off days / Special duties"),
                            fontSize = 10.sp,
                            color = dyn.textSecondary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${specialHours}$hourUnit",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.textPrimary
                        )
                        Text(
                            text = trEn("%100 Çift Yevmiye", "2.0x Holiday Pay"),
                            fontSize = 10.sp,
                            color = dyn.accentCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Container Bottom Bar: Toplam Puantaj Süresi
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
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Calculate,
                                contentDescription = null,
                                tint = dyn.dayShiftAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = trEn("Toplam Puantaj Süresi", "Total Timesheet Hours"),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = dyn.dayShiftText
                            )
                        }

                        Text(
                            text = trEn("${stats.totalWorkHours.toInt()} Saat", "${stats.totalWorkHours.toInt()} Hours"),
                            fontSize = 18.sp,
                            fontFamily = ProshiftMonoFont,
                            fontWeight = FontWeight.Black,
                            color = dyn.dayShiftAccent
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card 3: Resmi Çıktı & Dışa Aktarma
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .titaniumBrushedSurface(RoundedCornerShape(20.dp), dyn.borderSubtle),
            shape = RoundedCornerShape(20.dp),
            color = Color.Transparent
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = null,
                        tint = dyn.accentCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = trEn("Çıktı & Paylaşım", "Export & Share"),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = dyn.textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Toggle Row: Nöbet Notları ve Molaları Dahil Et
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .titaniumBrushedSurface(RoundedCornerShape(12.dp), dyn.borderSubtle, elevated = true)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.EditNote,
                            contentDescription = null,
                            tint = dyn.accentCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = trEn("Notları ve molaları dahil et", "Include notes and breaks"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = dyn.textPrimary
                        )
                    }

                    Switch(
                        checked = includeNotesAndBreaks,
                        onCheckedChange = { includeNotesAndBreaks = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = dyn.textOnPrimary,
                            checkedTrackColor = dyn.primaryNeon,
                            uncheckedThumbColor = dyn.textMuted,
                            uncheckedTrackColor = dyn.surfacePanel
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Button 1: Resmi Çizelge (PDF)
                Surface(
                    onClick = { viewModel.openPdfPreviewDialog() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_official_pdf"),
                    shape = RoundedCornerShape(12.dp),
                    color = dyn.primaryDeep
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PictureAsPdf,
                                    contentDescription = "PDF",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = trEn("PDF İndir (Resmi Çizelge)", "Download PDF (Official Sheet)"),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Icon(
                                imageVector = Icons.Filled.Download,
                                contentDescription = trEn("İndir", "Download"),
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Button 2: Excel / CSV
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .titaniumBrushedSurface(RoundedCornerShape(12.dp), dyn.borderSubtle, elevated = true)
                        .clickable {
                            viewModel.exportTimesheetText(
                                shareCsv = true,
                                includeNotes = includeNotesAndBreaks
                            )
                        }
                        .testTag("btn_export_xlsx"),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.TableChart,
                                contentDescription = "Excel",
                                tint = dyn.accentCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = trEn("Excel / CSV Dökümü", "Excel / CSV Export"),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = dyn.textPrimary
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.Download,
                            contentDescription = trEn("İndir", "Download"),
                            tint = dyn.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Button 3: WhatsApp ile Gönder
                Surface(
                    onClick = { viewModel.shareViaWhatsApp() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .border(1.dp, dyn.emeraldBorder, RoundedCornerShape(12.dp))
                        .testTag("btn_share_whatsapp_timesheet"),
                    shape = RoundedCornerShape(12.dp),
                    color = dyn.emeraldBg
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .background(WhatsAppGreen, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Text(
                                text = trEn("WhatsApp ile Gönder", "Share via WhatsApp"),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = dyn.textPrimary
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = trEn("Gönder", "Send"),
                            tint = dyn.emeraldAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card 4: Yıllık Ücretli İzin Takibi
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .titaniumBrushedSurface(RoundedCornerShape(20.dp), dyn.borderSubtle),
            shape = RoundedCornerShape(20.dp),
            color = Color.Transparent
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.BeachAccess,
                        contentDescription = null,
                        tint = dyn.accentCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = trEn("Yıllık İzin Takibi", "Annual Leave Tracker"),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = dyn.textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Service duration sub-row
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, dyn.borderSubtle, RoundedCornerShape(10.dp)),
                    shape = RoundedCornerShape(10.dp),
                    color = dyn.surfaceElevated
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                imageVector = Icons.Filled.WorkOutline,
                                contentDescription = null,
                                tint = dyn.accentCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = trEn("Hizmet Süresi: ", "Tenure: "),
                                fontSize = 11.sp,
                                color = dyn.textSecondary
                            )
                            Text(
                                text = trEn("$seniorityYears Yıl, $seniorityMonths Ay", "${seniorityYears}y ${seniorityMonths}m"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = dyn.textPrimary
                            )
                        }

                        Text(
                            text = seniorityBracket,
                            fontSize = 10.sp,
                            color = dyn.textMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Usage Progress Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = trEn("KULLANIM ORANI", "USAGE RATE"),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = dyn.textMuted,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = formatPercent(leaveUsagePercent, isEn),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = dyn.accentCyan
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                LinearProgressIndicator(
                    progress = { leaveUsageRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = dyn.primaryNeon,
                    trackColor = dyn.surfacePanel
                )

                Spacer(modifier = Modifier.height(14.dp))

                val daysUnit = trEn(" Gün", " d")
                // 3 Metric Boxes: YILLIK HAK, KULLANILAN, KALAN BAKİYE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Box 1: YILLIK HAK
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, dyn.borderSubtle, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        color = dyn.surfaceElevated
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = trEn("YILLIK HAK", "ANNUAL"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = dyn.textMuted)
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${stats.statutoryLeaveRights}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = dyn.textPrimary
                                )
                                Text(
                                    text = daysUnit,
                                    fontSize = 11.sp,
                                    color = dyn.textSecondary,
                                    modifier = Modifier.padding(bottom = 1.dp)
                                )
                            }
                        }
                    }

                    // Box 2: KULLANILAN
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, dyn.borderSubtle, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        color = dyn.surfaceElevated
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = trEn("KULLANILAN", "USED"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = dyn.textMuted)
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${stats.usedLeaveDays}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = dyn.textPrimary
                                )
                                Text(
                                    text = daysUnit,
                                    fontSize = 11.sp,
                                    color = dyn.textSecondary,
                                    modifier = Modifier.padding(bottom = 1.dp)
                                )
                            }
                        }
                    }

                    // Box 3: KALAN BAKİYE (Highlighted Cyan Card)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, dyn.dayShiftBorder, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        color = dyn.activeChipBg
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = trEn("KALAN BAKİYE", "REMAINING"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = dyn.dayShiftAccent)
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${stats.remainingLeaveDays}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = dyn.dayShiftText
                                )
                                Text(
                                    text = daysUnit,
                                    fontSize = 11.sp,
                                    color = dyn.dayShiftAccent,
                                    modifier = Modifier.padding(bottom = 1.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Button: İzin Talep Dilekçesi Hazırla
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showPetitionDialog = true }
                        .border(1.dp, dyn.borderSubtle, RoundedCornerShape(12.dp))
                        .testTag("btn_prepare_leave_petition"),
                    shape = RoundedCornerShape(12.dp),
                    color = dyn.surfaceElevated
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Description,
                            contentDescription = null,
                            tint = dyn.accentCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = trEn("İzin Dilekçesi Hazırla", "Prepare Leave Request"),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.textPrimary
                        )
                    }
                }
            }
        }
    }

    if (showPetitionDialog) {
        LeavePetitionDialog(
            settings = settings,
            stats = stats,
            onDismiss = { showPetitionDialog = false },
            onShare = { petitionText ->
                viewModel.sharePlainText(
                    trEn(isEn, "Yıllık İzin Talep Dilekçesi", "Annual Leave Request"),
                    petitionText
                )
            }
        )
    }

    if (showPayrollInfoDialog) {
        AlertDialog(
            onDismissRequest = { showPayrollInfoDialog = false },
            containerColor = dyn.surfaceCard,
            title = { Text(trEn("4857 SK Puantaj Esasları", "Payroll Calculation Rules"), color = dyn.textPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = trEn(
                        "• Gece Çalışması (md. 69): Saat ${settings.geceStartTime} - ${settings.geceEndTime} aralığındaki gece vardiyaları başladığı güne işlenir.\n\n" +
                            "• Ek Mesai (md. 41): Vardiya dışı ek çalışma ve tatil mesaileri (${stats.holidayWorkDaysCount} gün tatil nöbeti) ayrı hesaplanır.\n\n" +
                            "• Yıllık İzin (md. 53): 1–5 yıl kıdeme 14 gün, 6–14 yıl kıdeme 20 gün, 15+ yıl kıdeme 26 gün; 50 yaş üstü ve 18 yaş altı çalışanlara en az 20 gün hak tanınır.",
                        "• Night Work: Night shifts between ${settings.geceStartTime} and ${settings.geceEndTime} are recorded on their start date.\n\n" +
                            "• Overtime: Extra duty and public holiday work (${stats.holidayWorkDaysCount} holiday shifts) are tracked separately at premium rates.\n\n" +
                            "• Annual Leave: Entitlement scales with tenure (1–5 yrs: 14d, 6–14 yrs: 20d, 15+ yrs: 26d; min 20d for ages 50+ or under 18)."
                    ),
                    color = dyn.textSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showPayrollInfoDialog = false }) {
                    Text(trEn("Anladım", "Got It"), color = dyn.accentCyan, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
