package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.Nightlight
import androidx.compose.material.icons.rounded.QueryBuilder
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DutyTelemetry
import com.example.data.model.ShiftDay
import com.example.data.model.ShiftType
import com.example.data.model.TimesheetStats
import com.example.ui.i18n.LocalAppLocale
import com.example.ui.i18n.LocalIsEnglish
import com.example.ui.i18n.formatHourUnit
import com.example.ui.i18n.localizedTitle
import com.example.ui.i18n.trEn
import com.example.ui.theme.DynamicTheme
import com.example.ui.theme.ProshiftMonoFont
import com.example.ui.theme.ShiftGece
import com.example.ui.theme.ShiftGunduz
import com.example.ui.theme.ShiftOvertimeAmber
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


@Composable
fun ModernShiftDashboardOverview(
    telemetry: DutyTelemetry,
    weeklyDays: List<ShiftDay>,
    timesheetStats: TimesheetStats,
    onBreakOvertimeClick: () -> Unit,
    onShiftNoteClick: () -> Unit,
    onNextShiftClick: () -> Unit,
    onMonthlyHoursClick: () -> Unit,
    onTimesheetClick: () -> Unit,
    onInspectDay: (ShiftDay) -> Unit,
    modifier: Modifier = Modifier,
    now: LocalDateTime = LocalDateTime.now()
) {
    val dyn = DynamicTheme.colors
    val isEn = LocalIsEnglish.current
    val hUnit = formatHourUnit(isEn)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. HERO SHIFT STATUS CARD
        HeroShiftTelemetryCard(
            telemetry = telemetry,
            onBreakOvertimeClick = onBreakOvertimeClick,
            onShiftNoteClick = onShiftNoteClick
        )

        // 2. QUICK ACTION CHIPS (Mola, Fazla Mesai, Nöbet Notu)
        QuickActionBar(
            telemetry = telemetry,
            onBreakOvertimeClick = onBreakOvertimeClick,
            onShiftNoteClick = onShiftNoteClick,
            onMonthlyHoursClick = onMonthlyHoursClick
        )

        // 3. CENTERED WEEKLY FLOW STRIP
        WeeklyFlowStripSection(
            days = weeklyDays,
            todayDate = now.toLocalDate(),
            onInspectDay = onInspectDay,
            onViewAllClick = onNextShiftClick
        )

        // 4. STATS GLANCE ROW (Bu Ayki Çalışma, Gece Mesaisi, Kalan İzin)
        MonthlyStatsSummaryRow(
            stats = timesheetStats,
            hUnit = hUnit,
            onTimesheetClick = onTimesheetClick
        )

        // 5. UPCOMING SHIFT TEASER (Sıradaki Nöbet)
        NextShiftTeaserCard(
            telemetry = telemetry,
            onClick = onNextShiftClick
        )
    }
}

@Composable
private fun HeroShiftTelemetryCard(
    telemetry: DutyTelemetry,
    onBreakOvertimeClick: () -> Unit,
    onShiftNoteClick: () -> Unit
) {
    val dyn = DynamicTheme.colors
    val isEn = LocalIsEnglish.current
    val isOnDuty = telemetry.isOnDuty

    // Animated breathing glow for active duty
    val infiniteTransition = rememberInfiniteTransition(label = "duty_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val cardBorderColor = if (isOnDuty) {
        dyn.primaryNeon.copy(alpha = pulseAlpha)
    } else {
        dyn.borderMetal
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isOnDuty) 16.dp else 6.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = if (isOnDuty) dyn.primaryNeon.copy(alpha = 0.35f) else Color.Transparent,
                spotColor = if (isOnDuty) dyn.accentCyan.copy(alpha = 0.45f) else Color.Transparent
            )
            .titaniumBrushedSurface(
                shape = RoundedCornerShape(24.dp),
                borderColor = cardBorderColor,
                borderWidth = if (isOnDuty) 1.5.dp else 1.dp,
                elevated = true
            ),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header: Duty badge & Shift Name
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
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isOnDuty) dyn.emeraldAccent else dyn.textMuted)
                    )
                    Text(
                        text = if (isOnDuty) trEn("GÖREVDE (AKTİF NÖBET)", "ON DUTY (ACTIVE)") else trEn("İSTİRAHATTE / SERBEST", "OFF DUTY"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isOnDuty) dyn.emeraldAccent else dyn.textSecondary,
                        letterSpacing = 1.sp
                    )
                }

                telemetry.currentShiftType?.let { sType ->
                    ShiftBadge(type = sType)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Center: Circular Progress & Countdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isOnDuty) trEn("Nöbetin Bitmesine:", "Shift Ends In:") else trEn("Sıradaki Nöbete:", "Next Shift In:"),
                        fontSize = 12.sp,
                        color = dyn.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = telemetry.remainingFormatted,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = ProshiftMonoFont,
                        color = if (isOnDuty) dyn.primaryNeon else dyn.textPrimary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isOnDuty) telemetry.nextShiftDetails else telemetry.nextShiftTitle,
                        fontSize = 12.sp,
                        color = dyn.textMuted,
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Modern Circular Progress
                Box(
                    modifier = Modifier.size(76.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        color = dyn.surfaceElevated,
                        strokeWidth = 7.dp,
                        trackColor = Color.Transparent,
                        strokeCap = StrokeCap.Round
                    )
                    val progressValue = (telemetry.progressPercent / 100f).coerceIn(0f, 1f)
                    CircularProgressIndicator(
                        progress = { progressValue },
                        modifier = Modifier.fillMaxSize(),
                        color = if (isOnDuty) dyn.primaryNeon else dyn.accentCyan,
                        strokeWidth = 7.dp,
                        trackColor = Color.Transparent,
                        strokeCap = StrokeCap.Round
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "%${telemetry.progressPercent.toInt()}",
                            fontSize = 15.sp,
                            fontFamily = ProshiftMonoFont,
                            fontWeight = FontWeight.Black,
                            color = dyn.textPrimary
                        )
                        Text(
                            text = trEn("Tamam", "Done"),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(dyn.surfaceElevated, RoundedCornerShape(12.dp))
                    .border(1.dp, dyn.borderSubtle, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.QueryBuilder,
                        contentDescription = null,
                        tint = dyn.accentCyan,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = telemetry.shiftHoursRange,
                        fontSize = 12.sp,
                        fontFamily = ProshiftMonoFont,
                        fontWeight = FontWeight.Bold,
                        color = dyn.textPrimary
                    )
                }

                if (telemetry.isBreakActive) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(ShiftOvertimeAmber)
                        )
                        Text(
                            text = trEn("Mola: ${telemetry.breakFormatted}", "Break: ${telemetry.breakFormatted}"),
                            fontSize = 11.sp,
                            fontFamily = ProshiftMonoFont,
                            fontWeight = FontWeight.Bold,
                            color = ShiftOvertimeAmber
                        )
                    }
                } else {
                    Text(
                        text = trEn("Kalan: ${telemetry.nextShiftHoursRemaining}", "Left: ${telemetry.nextShiftHoursRemaining}"),
                        fontSize = 11.sp,
                        color = dyn.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionBar(
    telemetry: DutyTelemetry,
    onBreakOvertimeClick: () -> Unit,
    onShiftNoteClick: () -> Unit,
    onMonthlyHoursClick: () -> Unit
) {
    val dyn = DynamicTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ActionGlassButton(
            title = trEn("Ek Mesai", "Overtime"),
            subtitle = trEn("+Saat Ekle", "+Add Hours"),
            icon = Icons.Rounded.AddCircleOutline,
            iconTint = ShiftOvertimeAmber,
            onClick = onBreakOvertimeClick,
            modifier = Modifier.weight(1f),
            testTag = "home_quick_action_overtime"
        )

        ActionGlassButton(
            title = trEn("Mola", "Break"),
            subtitle = if (telemetry.isBreakActive) telemetry.breakFormatted else trEn("Sayaç", "Timer"),
            icon = Icons.Rounded.LocalCafe,
            iconTint = if (telemetry.isBreakActive) ShiftOvertimeAmber else dyn.accentCyan,
            onClick = onBreakOvertimeClick,
            modifier = Modifier.weight(1f),
            testTag = "home_quick_action_break"
        )

        ActionGlassButton(
            title = trEn("Nöbet Notu", "Shift Note"),
            subtitle = trEn("Not / Devir", "Note / Handover"),
            icon = Icons.Rounded.EditNote,
            iconTint = dyn.primaryNeon,
            onClick = onShiftNoteClick,
            modifier = Modifier.weight(1f),
            testTag = "home_quick_action_note"
        )
    }
}

@Composable
private fun ActionGlassButton(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    val dyn = DynamicTheme.colors
    Surface(
        modifier = modifier
            .titaniumBrushedSurface(
                shape = RoundedCornerShape(16.dp),
                borderColor = dyn.borderMetal,
                elevated = false
            )
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(19.dp)
                )
            }
            Spacer(modifier = Modifier.height(7.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = dyn.textPrimary,
                maxLines = 1
            )
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = dyn.textMuted,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun WeeklyFlowStrip(
    weeklyDays: List<ShiftDay>,
    now: LocalDateTime,
    onInspectDay: (ShiftDay) -> Unit,
    onAllClick: () -> Unit
) {
    val dyn = DynamicTheme.colors
    val isEn = LocalIsEnglish.current
    val appLocale = LocalAppLocale.current
    val hUnit = formatHourUnit(isEn)

    val weekRangeText = remember(weeklyDays, appLocale, isEn) {
        if (weeklyDays.isNotEmpty()) {
            val monthFmt = DateTimeFormatter.ofPattern("MMMM", appLocale)
            "${weeklyDays.first().date.dayOfMonth} - ${weeklyDays.last().date.dayOfMonth} ${weeklyDays.last().date.format(monthFmt)}"
        } else {
            if (isEn) "This Week" else "Bu Hafta"
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
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
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(dyn.accentCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarViewWeek,
                        contentDescription = trEn("Haftalık", "Weekly"),
                        tint = dyn.accentCyan,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Text(
                    text = trEn("Haftalık Akış", "Weekly Flow"),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = dyn.textPrimary
                )
                Box(
                    modifier = Modifier
                        .background(dyn.surfaceElevated, RoundedCornerShape(999.dp))
                        .border(1.dp, dyn.borderSubtle, RoundedCornerShape(999.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = weekRangeText,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        color = dyn.textSecondary
                    )
                }
            }

            Text(
                text = trEn("Tümü →", "All →"),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = dyn.accentCyan,
                modifier = Modifier
                    .clickable(onClick = onAllClick)
                    .padding(vertical = 4.dp, horizontal = 2.dp)
                    .testTag("home_weekly_all_button")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        val todayDate = remember(now.toLocalDate()) { LocalDate.now() }
        val dayAbbrFmt = remember(appLocale) { DateTimeFormatter.ofPattern("EEE", appLocale) }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            weeklyDays.forEach { day ->
                val isToday = day.date == todayDate
                val isEveningShift = day.type == ShiftType.AKSAM
                val isDayShift = day.type.isWork && !day.type.isNight && !isEveningShift
                val isNightShift = day.type.isWork && day.type.isNight
                val dayName = day.date.format(dayAbbrFmt).uppercase(appLocale).replace(".", "").take(3)
                val dateNum = String.format(Locale.ROOT, "%02d", day.date.dayOfMonth)
                val badge = when {
                    day.type == ShiftType.SABAH || day.type == ShiftType.AKSAM || day.type == ShiftType.GECE8 ->
                        "${day.startTime.take(2)}-${day.endTime.take(2)}"
                    day.type.isWork -> "${day.totalHours.toInt()}$hUnit"
                    else -> day.type.localizedTitle(isEn)
                }
                val cellAccentColor = when {
                    isEveningShift -> ShiftOvertimeAmber
                    isDayShift -> dyn.accentCyan
                    isNightShift -> Color(0xFF6366F1)
                    else -> dyn.textMuted
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .then(
                            if (isToday) Modifier.shadow(12.dp, RoundedCornerShape(16.dp), ambientColor = dyn.primaryNeon, spotColor = dyn.primaryNeon)
                            else Modifier
                        )
                        .titaniumBrushedSurface(
                            shape = RoundedCornerShape(16.dp),
                            borderColor = when {
                                isToday -> dyn.primaryNeon
                                isEveningShift -> ShiftOvertimeAmber.copy(alpha = 0.55f)
                                isNightShift -> ShiftGece.copy(alpha = 0.45f)
                                isDayShift -> ShiftGunduz.copy(alpha = 0.45f)
                                else -> dyn.borderSubtle
                            },
                            borderWidth = if (isToday) 1.5.dp else 1.dp,
                            elevated = isToday
                        )
                        .clickable { onInspectDay(day) },
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Transparent
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = dayName,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isToday) dyn.primaryNeon else dyn.textSecondary
                            )
                            if (isToday) {
                                Spacer(modifier = Modifier.width(2.dp))
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .background(dyn.primaryNeon, CircleShape)
                                    )
                            }
                        }

                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = dateNum,
                            fontSize = 16.sp,
                            fontFamily = ProshiftMonoFont,
                            fontWeight = FontWeight.Black,
                            color = if (isToday || day.type.isWork) dyn.textPrimary else dyn.textSecondary
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Icon(
                            imageVector = shiftTypeVector(day.type),
                            contentDescription = day.type.localizedTitle(isEn),
                            tint = cellAccentColor,
                            modifier = Modifier.size(15.dp)
                        )

                        Spacer(modifier = Modifier.height(5.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when {
                                isEveningShift -> ShiftOvertimeAmber.copy(alpha = 0.22f)
                                isDayShift -> ShiftGunduz.copy(alpha = 0.2f)
                                isNightShift -> ShiftGece.copy(alpha = 0.2f)
                                else -> dyn.surfaceElevated
                            }
                        ) {
                            Text(
                                text = badge,
                                fontSize = 8.5.sp,
                                fontFamily = ProshiftMonoFont,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    isEveningShift -> ShiftOvertimeAmber
                                    isDayShift -> ShiftGunduz
                                    isNightShift -> if (dyn.isDark) Color(0xFFA5B4FC) else Color(0xFF4F46E5)
                                    else -> dyn.textMuted
                                },
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlyStatsSummaryRow(
    stats: TimesheetStats,
    hUnit: String,
    onTimesheetClick: () -> Unit
) {
    val dyn = DynamicTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTimesheetClick),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatMiniCard(
            title = trEn("Toplam Çalışma", "Total Work"),
            value = "${stats.totalWorkHours.toInt()}$hUnit",
            accentColor = dyn.primaryNeon,
            modifier = Modifier.weight(1f)
        )
        StatMiniCard(
            title = trEn("Gece Mesaisi", "Night Hours"),
            value = "${stats.nightHours.toInt()}$hUnit",
            accentColor = Color(0xFF818CF8),
            modifier = Modifier.weight(1f)
        )
        StatMiniCard(
            title = trEn("Kalan Yıllık İzin", "Leave Left"),
            value = "${stats.remainingLeaveDays} ${trEn("Gün", "Days")}",
            accentColor = dyn.emeraldAccent,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatMiniCard(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val dyn = DynamicTheme.colors
    Box(
        modifier = modifier
            .titaniumBrushedSurface(
                shape = RoundedCornerShape(14.dp),
                borderColor = dyn.borderSubtle
            )
            .padding(vertical = 10.dp, horizontal = 10.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 9.5.sp,
                color = dyn.textSecondary,
                maxLines = 1,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontFamily = ProshiftMonoFont,
                fontWeight = FontWeight.Black,
                color = accentColor
            )
        }
    }
}

@Composable
private fun NextShiftTeaserCard(
    telemetry: DutyTelemetry,
    onClick: () -> Unit
) {
    val dyn = DynamicTheme.colors

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .titaniumBrushedSurface(
                shape = RoundedCornerShape(16.dp),
                borderColor = dyn.borderMetal
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(dyn.accentCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = shiftTypeVector(telemetry.nextShiftType),
                        contentDescription = null,
                        tint = dyn.accentCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = trEn("Sıradaki Nöbet & Vardiya", "Upcoming Shift"),
                        fontSize = 10.5.sp,
                        color = dyn.textMuted,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${telemetry.nextShiftTitle} • ${telemetry.nextShiftDetails}",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = dyn.textPrimary
                    )
                }
            }

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = dyn.textSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
