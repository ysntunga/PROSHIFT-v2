package com.example.ui.components

import android.os.Build
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DutyTelemetry
import com.example.data.model.ShiftDay
import com.example.data.model.ShiftType
import com.example.data.model.TimesheetStats
import com.example.ui.i18n.LocalAppLocale
import com.example.ui.i18n.LocalIsEnglish
import com.example.ui.i18n.formatHourUnit
import com.example.ui.i18n.formatPercent
import com.example.ui.i18n.localizedNextShiftDetails
import com.example.ui.i18n.localizedNextShiftHoursRemaining
import com.example.ui.i18n.localizedNextShiftTitle
import com.example.ui.i18n.localizedShiftHoursRange
import com.example.ui.i18n.localizedTitle
import com.example.ui.i18n.trEn
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BorderSpecularTop
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DynamicTheme
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.ProshiftMonoFont
import com.example.ui.theme.ShiftGece
import com.example.ui.theme.ShiftGunduz
import com.example.ui.theme.ShiftOvertimeAmber
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class UpcomingShiftTimer(
    val shiftDay: ShiftDay,
    val startDateTime: LocalDateTime,
    val secondsUntilStart: Long,
    val proximityProgress: Float
)

fun computeUpcomingShiftTimers(
    days: List<ShiftDay>,
    now: LocalDateTime = LocalDateTime.now(),
    maxCount: Int = 3
): List<UpcomingShiftTimer> {
    if (days.isEmpty() || maxCount <= 0) return emptyList()
    val maxWindowSeconds = 72L * 3600L // 72h visual proximity window

    return days
        .asSequence()
        .filter { it.type.isWork }
        .mapNotNull { day ->
            val startTime = parseShiftStartTime(day.startTime) ?: return@mapNotNull null
            val startDateTime = LocalDateTime.of(day.date, startTime)
            val secondsUntil = Duration.between(now, startDateTime).seconds
            if (secondsUntil <= 0L) return@mapNotNull null
            val clamped = secondsUntil.coerceAtMost(maxWindowSeconds)
            val proximity = (1f - (clamped.toFloat() / maxWindowSeconds.toFloat())).coerceIn(0.08f, 1f)
            UpcomingShiftTimer(
                shiftDay = day,
                startDateTime = startDateTime,
                secondsUntilStart = secondsUntil,
                proximityProgress = proximity
            )
        }
        .sortedBy { it.secondsUntilStart }
        .take(maxCount)
        .toList()
}

fun formatUpcomingCountdown(secondsUntilStart: Long, isEnglish: Boolean): String {
    val safeSeconds = secondsUntilStart.coerceAtLeast(0L)
    val days = safeSeconds / 86_400L
    val hours = (safeSeconds % 86_400L) / 3_600L
    val minutes = (safeSeconds % 3_600L) / 60L
    val seconds = safeSeconds % 60L

    return when {
        days > 0L -> if (isEnglish) "${days}d ${hours}h" else "${days}g ${hours}s"
        hours > 0L -> if (isEnglish) "${hours}h ${String.format(Locale.ROOT, "%02d", minutes)}m" else "${hours}s ${String.format(Locale.ROOT, "%02d", minutes)}dk"
        else -> if (isEnglish) {
            "${minutes}m ${String.format(Locale.ROOT, "%02d", seconds)}s"
        } else {
            "${minutes}dk ${String.format(Locale.ROOT, "%02d", seconds)}sn"
        }
    }
}

private fun parseShiftStartTime(raw: String): LocalTime? {
    val parts = raw.trim().split(":")
    if (parts.size != 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    if (hour !in 0..23 || minute !in 0..59) return null
    return LocalTime.of(hour, minute)
}

@Composable
fun ShiftDashboardOverview(
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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp)
            .testTag("shift_dashboard_overview"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CurrentShiftStatusCard(
            telemetry = telemetry,
            onBreakOvertimeClick = onBreakOvertimeClick,
            onShiftNoteClick = onShiftNoteClick,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        WeeklyFlowStripSection(
            days = weeklyDays,
            todayDate = now.toLocalDate(),
            onInspectDay = onInspectDay,
            onViewAllClick = onNextShiftClick,
            headerHorizontalPadding = 16.dp
        )

        ShiftMetricsDuoRow(
            telemetry = telemetry,
            timesheetStats = timesheetStats,
            onNextShiftClick = onNextShiftClick,
            onMonthlyHoursClick = onMonthlyHoursClick,
            onTimesheetClick = onTimesheetClick,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun CurrentShiftStatusCard(
    telemetry: DutyTelemetry,
    onBreakOvertimeClick: () -> Unit,
    onShiftNoteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEn = LocalIsEnglish.current
    val dyn = DynamicTheme.colors

    val infiniteTransition = rememberInfiniteTransition(label = "dashboardStatusPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dashboardPulseAlpha"
    )

    val statusChipColor = if (telemetry.isOnDuty) EmeraldGlow else dyn.primaryNeon
    val activeShiftType = telemetry.currentShiftType ?: ShiftType.GUNDUZ

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dashboard_current_shift_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(dyn.brushedSurfaceBrush)
                .border(1.dp, dyn.borderSubtle, RoundedCornerShape(22.dp))
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            // Top Status Row: Active state badge + shift type pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = statusChipColor.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, statusChipColor.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(statusChipColor.copy(alpha = 0.55f + 0.45f * pulseAlpha), CircleShape)
                        )
                        Text(
                            text = if (telemetry.isOnDuty) trEn("ŞU AN GÖREVDE", "ON DUTY NOW") else trEn("DİNLENMEDE", "OFF DUTY"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusChipColor
                        )
                    }
                }

                val shiftBadgeColor = when {
                    activeShiftType == ShiftType.AKSAM -> ShiftOvertimeAmber
                    activeShiftType.isNight -> ShiftGece
                    activeShiftType.isWork -> dyn.accentCyan
                    else -> dyn.textSecondary
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = shiftBadgeColor.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, shiftBadgeColor.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = shiftTypeVector(activeShiftType),
                            contentDescription = activeShiftType.localizedTitle(isEn),
                            tint = shiftBadgeColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = activeShiftType.localizedTitle(isEn),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = shiftBadgeColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Animated Circular Progress Dial
            val animatedProgress by animateFloatAsState(
                targetValue = telemetry.progressPercent.coerceIn(0f, 1f),
                animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
                label = "dashboardDialProgress"
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(178.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(182.dp)
                        .then(
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                Modifier.blur(24.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                            } else {
                                Modifier
                            }
                        )
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    dyn.primaryNeon.copy(alpha = 0.17f * (0.75f + 0.25f * pulseAlpha)),
                                    dyn.primaryDeep.copy(alpha = 0.10f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )

                Canvas(modifier = Modifier.size(176.dp)) {
                    val strokeWidth = 11.dp.toPx()
                    val pad = strokeWidth / 2f
                    val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                    val topLeft = Offset(pad, pad)
                    val sweepAngle = 360f * animatedProgress

                    drawArc(
                        color = dyn.surfaceElevated.copy(alpha = 0.9f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    if (sweepAngle > 0.5f) {
                        drawArc(
                            color = dyn.primaryNeon.copy(alpha = 0.20f * (0.7f + 0.3f * pulseAlpha)),
                            startAngle = -90f,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth * 1.9f, cap = StrokeCap.Round)
                        )

                        val gradientBrush = Brush.linearGradient(
                            colors = listOf(dyn.primaryDeep, dyn.primaryNeon, dyn.accentCyan),
                            start = Offset(size.width * 0.5f, 0f),
                            end = Offset(size.width * 0.5f, size.height)
                        )
                        drawArc(
                            brush = gradientBrush,
                            startAngle = -90f,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth + 1.5f, cap = StrokeCap.Round)
                        )

                        val endRad = (-90f + sweepAngle) * (PI / 180.0)
                        val radius = arcSize.width / 2f
                        val tipX = center.x + (radius * cos(endRad)).toFloat()
                        val tipY = center.y + (radius * sin(endRad)).toFloat()
                        drawCircle(
                            color = dyn.accentCyan.copy(alpha = 0.36f * pulseAlpha),
                            radius = strokeWidth * 0.9f,
                            center = Offset(tipX, tipY)
                        )
                        drawCircle(
                            color = dyn.textPrimary,
                            radius = strokeWidth * 0.34f,
                            center = Offset(tipX, tipY)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (telemetry.isOnDuty) trEn("KALAN VARDİYA SÜRESİ", "SHIFT TIME REMAINING") else trEn("SONRAKİ VARDİYAYA", "UNTIL NEXT SHIFT"),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        color = dyn.textSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = telemetry.remainingFormatted,
                        fontSize = 32.sp,
                        fontFamily = ProshiftMonoFont,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.02).sp,
                        color = dyn.textPrimary,
                        modifier = Modifier.testTag("dashboard_primary_timer_text")
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(dyn.accentCyan, CircleShape)
                        )
                        val pctText = formatPercent((telemetry.progressPercent * 100).toInt(), isEn)
                        Text(
                            text = "${activeShiftType.localizedTitle(isEn)} • $pctText ${trEn("Dolu", "Done")}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.accentCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val localizedRange = telemetry.localizedShiftHoursRange(isEn)
            val intervalParts = localizedRange.split(" (")
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "${activeShiftType.localizedTitle(isEn)} ${trEn("Vardiyası", "Shift")} • ${intervalParts.firstOrNull() ?: localizedRange}",
                    fontSize = 13.sp,
                    color = dyn.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (intervalParts.size > 1) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = dyn.accentCyan.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, dyn.accentCyan.copy(alpha = 0.45f))
                    ) {
                        Text(
                            text = "(${intervalParts[1]}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.accentCyan,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onBreakOvertimeClick)
                        .shadow(10.dp, RoundedCornerShape(14.dp), ambientColor = dyn.primaryNeon)
                        .testTag("dashboard_break_overtime_button"),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(
                                if (telemetry.isBreakActive) {
                                    Brush.horizontalGradient(
                                        colors = listOf(ShiftOvertimeAmber, Color(0xFFB45309))
                                    )
                                } else {
                                    dyn.actionGradientBrush
                                }
                            )
                            .border(1.dp, BorderSpecularTop, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (telemetry.isBreakActive) Icons.Filled.PlayCircle else Icons.Filled.PauseCircle,
                                contentDescription = trEn("Mola", "Break"),
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (telemetry.isBreakActive) "${trEn("Mola", "Break")}: ${telemetry.breakFormatted}" else trEn("Mola / Mesai", "Break / OT"),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1
                            )
                        }
                    }
                }

                OutlinedCard(
                    onClick = onShiftNoteClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("dashboard_shift_note_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = dyn.surfaceElevated.copy(alpha = 0.88f)
                    ),
                    border = BorderStroke(1.dp, dyn.borderSubtle)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.EditNote,
                            contentDescription = trEn("Nöbet Notu", "Shift Note"),
                            tint = dyn.accentCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = trEn("Nöbet Notu", "Shift Note"),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.textPrimary,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShiftMetricsDuoRow(
    telemetry: DutyTelemetry,
    timesheetStats: TimesheetStats,
    onNextShiftClick: () -> Unit,
    onMonthlyHoursClick: () -> Unit,
    onTimesheetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEn = LocalIsEnglish.current
    val appLocale = LocalAppLocale.current
    val hUnit = formatHourUnit(isEn)
    val dyn = DynamicTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val nextTypeColor = if (telemetry.nextShiftType.isNight) ShiftGece else dyn.accentCyan
        Card(
            onClick = onNextShiftClick,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .testTag("dashboard_next_shift_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = dyn.surfaceCard),
            border = BorderStroke(1.dp, dyn.borderSubtle)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(dyn.brushedSurfaceBrush)
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = trEn("SONRAKİ", "NEXT"),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = dyn.textSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = nextTypeColor.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, nextTypeColor.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = shiftTypeVector(telemetry.nextShiftType),
                                contentDescription = telemetry.nextShiftType.localizedTitle(isEn),
                                tint = nextTypeColor,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = telemetry.nextShiftType.localizedTitle(isEn).uppercase(appLocale),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = nextTypeColor,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Column {
                    Text(
                        text = telemetry.localizedNextShiftTitle(isEn),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = dyn.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = telemetry.localizedNextShiftDetails(isEn),
                        fontSize = 11.sp,
                        fontFamily = ProshiftMonoFont,
                        color = dyn.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = trEn("Kalan Zaman", "Remaining"),
                        fontSize = 10.sp,
                        color = dyn.textMuted
                    )
                    Text(
                        text = telemetry.localizedNextShiftHoursRemaining(isEn),
                        fontSize = 10.sp,
                        fontFamily = ProshiftMonoFont,
                        fontWeight = FontWeight.Bold,
                        color = dyn.accentCyan
                    )
                }
            }
        }

        val monthRatio = if (timesheetStats.targetWorkHours > 0) {
            (timesheetStats.totalWorkHours / timesheetStats.targetWorkHours).toFloat().coerceIn(0f, 1f)
        } else 0f
        val animatedMonthRatio by animateFloatAsState(
            targetValue = monthRatio,
            animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
            label = "dashboardMonthProgress"
        )

        Card(
            onClick = onMonthlyHoursClick,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .testTag("dashboard_monthly_hours_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = dyn.surfaceCard),
            border = BorderStroke(1.dp, dyn.borderSubtle)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(dyn.brushedSurfaceBrush)
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Speed,
                            contentDescription = trEn("Aylık Kota", "Monthly Target"),
                            tint = dyn.textSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = trEn("BU AYKİ MESAİ", "MONTHLY HOURS"),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.textSecondary,
                            letterSpacing = 0.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = formatPercent((animatedMonthRatio * 100).toInt(), isEn),
                        fontSize = 11.sp,
                        fontFamily = ProshiftMonoFont,
                        fontWeight = FontWeight.Black,
                        color = dyn.accentCyan
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${timesheetStats.totalWorkHours.toInt()}",
                            fontSize = 20.sp,
                            fontFamily = ProshiftMonoFont,
                            fontWeight = FontWeight.Black,
                            color = dyn.textPrimary
                        )
                        Text(
                            text = " / ${timesheetStats.targetWorkHours.toInt()}$hUnit",
                            fontSize = 11.sp,
                            fontFamily = ProshiftMonoFont,
                            color = dyn.textSecondary,
                            modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { animatedMonthRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = dyn.primaryNeon,
                        trackColor = dyn.surfaceElevated
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = trEn("Ek Mesai", "Overtime"),
                            tint = ShiftOvertimeAmber,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "+${timesheetStats.overtimeHours.toInt()}$hUnit ${trEn("Ek Mesai", "OT")}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ShiftOvertimeAmber,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = trEn("Puantaj", "Payroll"),
                        fontSize = 10.sp,
                        color = dyn.textMuted,
                        modifier = Modifier.clickable(onClick = onTimesheetClick)
                    )
                }
            }
        }
    }
}

@Composable
fun UpcomingShiftTimersCard(
    upcomingTimers: List<UpcomingShiftTimer>,
    now: LocalDateTime,
    onInspectDay: (ShiftDay) -> Unit,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEn = LocalIsEnglish.current
    val appLocale = LocalAppLocale.current
    val hUnit = formatHourUnit(isEn)
    val dyn = DynamicTheme.colors
    val dateFmt = remember(appLocale) { DateTimeFormatter.ofPattern("EEE, d MMM", appLocale) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dashboard_upcoming_timers_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = dyn.surfaceCard),
        border = BorderStroke(1.dp, dyn.borderSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(dyn.brushedSurfaceBrush)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            .size(30.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(dyn.primaryNeon.copy(alpha = 0.14f))
                            .border(1.dp, dyn.primaryNeon.copy(alpha = 0.35f), RoundedCornerShape(9.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Timer,
                            contentDescription = trEn("Yaklaşan Vardiya Sayaçları", "Upcoming Shift Timers"),
                            tint = dyn.accentCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Column {
                        Text(
                            text = trEn("Yaklaşan Vardiya Sayaçları", "Upcoming Shift Timers"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = dyn.textPrimary
                        )
                        Text(
                            text = trEn("Sıradaki nöbetlere canlı geri sayım", "Live countdown to upcoming shifts"),
                            fontSize = 10.sp,
                            color = dyn.textSecondary
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onViewAllClick)
                        .padding(horizontal = 6.dp, vertical = 6.dp)
                        .testTag("dashboard_upcoming_view_all"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = trEn("Takvim", "Schedule"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = dyn.accentCyan
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = dyn.accentCyan,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            if (upcomingTimers.isEmpty()) {
                OutlinedCard(
                    onClick = onViewAllClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dashboard_upcoming_empty_state"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = dyn.surfaceElevated.copy(alpha = 0.65f)
                    ),
                    border = BorderStroke(1.dp, dyn.borderSubtle)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.EventAvailable,
                            contentDescription = trEn("Planlanmış Vardiya Yok", "No Upcoming Shifts"),
                            tint = EmeraldGlow,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = trEn("Yakın 7 günde planlı nöbet yok", "No shifts scheduled in the next 7 days"),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = dyn.textPrimary
                            )
                            Text(
                                text = trEn("Takvimden yeni vardiya veya döngü ayarlayın.", "Open Calendar to customize your shift cycle."),
                                fontSize = 11.sp,
                                color = dyn.textSecondary
                            )
                        }
                    }
                }
            } else {
                upcomingTimers.forEachIndexed { index, item ->
                    val shift = item.shiftDay
                    val accent = when {
                        shift.type == ShiftType.AKSAM -> ShiftOvertimeAmber
                        shift.type.isNight -> ShiftGece
                        else -> ShiftGunduz
                    }
                    val relativeDayLabel = when (shift.date) {
                        now.toLocalDate() -> trEn("Bugün", "Today")
                        now.toLocalDate().plusDays(1) -> trEn("Yarın", "Tomorrow")
                        else -> shift.date.format(dateFmt)
                    }
                    val countdownText = formatUpcomingCountdown(item.secondsUntilStart, isEn)

                    OutlinedCard(
                        onClick = { onInspectDay(shift) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                            .testTag("upcoming_shift_timer_item_$index"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = dyn.surfaceElevated.copy(alpha = if (index == 0) 0.90f else 0.65f)
                        ),
                        border = BorderStroke(
                            width = if (index == 0) 1.2.dp else 1.dp,
                            color = if (index == 0) accent.copy(alpha = 0.55f) else dyn.borderSubtle
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(accent.copy(alpha = 0.16f))
                                            .border(1.dp, accent.copy(alpha = 0.38f), RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = shiftTypeVector(shift.type),
                                            contentDescription = shift.type.localizedTitle(isEn),
                                            tint = accent,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }

                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = shift.type.localizedTitle(isEn),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = dyn.textPrimary,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "• $relativeDayLabel",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = accent,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Text(
                                            text = "${shift.startTime} - ${shift.endTime} (${shift.totalHours.toInt()}$hUnit)",
                                            fontSize = 11.sp,
                                            fontFamily = ProshiftMonoFont,
                                            color = dyn.textSecondary,
                                            maxLines = 1
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(9.dp),
                                    color = accent.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, accent.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Schedule,
                                            contentDescription = trEn("Kalan Süre", "Time Left"),
                                            tint = accent,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = countdownText,
                                            fontSize = 11.sp,
                                            fontFamily = ProshiftMonoFont,
                                            fontWeight = FontWeight.Black,
                                            color = dyn.textPrimary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            LinearProgressIndicator(
                                progress = { item.proximityProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = accent,
                                trackColor = dyn.background.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}
