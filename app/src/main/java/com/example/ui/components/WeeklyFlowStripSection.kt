package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ShiftDay
import com.example.data.model.ShiftType
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
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

/**
 * Horizontally scrollable Weekly Flow carousel ("Haftalık Akış") placed directly beneath
 * the active shift countdown card. Automatically centers the active day (today) on launch
 * and snaps cleanly as the user swipes left or right across past and upcoming shifts.
 */
@Composable
fun WeeklyFlowStripSection(
    days: List<ShiftDay>,
    todayDate: LocalDate,
    onInspectDay: (ShiftDay) -> Unit,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier,
    headerHorizontalPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    val isEn = LocalIsEnglish.current
    val appLocale = LocalAppLocale.current
    val hUnit = formatHourUnit(isEn)
    val dyn = DynamicTheme.colors
    val coroutineScope = rememberCoroutineScope()

    val todayIndex = remember(days, todayDate) {
        val exact = days.indexOfFirst { it.date == todayDate }
        if (exact >= 0) exact else (days.size / 2).coerceAtLeast(0)
    }

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = todayIndex,
        initialFirstVisibleItemScrollOffset = 0
    )

    // Keep today centered whenever the underlying date window updates
    LaunchedEffect(todayIndex, days.size) {
        if (days.isNotEmpty() && todayIndex in days.indices) {
            listState.scrollToItem(index = todayIndex, scrollOffset = 0)
        }
    }

    val snapFlingBehavior = rememberSnapFlingBehavior(
        lazyListState = listState,
        snapPosition = SnapPosition.Center
    )

    val centeredIndex by remember(days) {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty() || days.isEmpty()) {
                todayIndex.coerceIn(0, (days.size - 1).coerceAtLeast(0))
            } else {
                val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                visibleItems.minByOrNull { item ->
                    abs((item.offset + item.size / 2) - viewportCenter)
                }?.index?.coerceIn(0, days.lastIndex) ?: todayIndex
            }
        }
    }

    val isTodayCentered by remember(todayIndex) {
        derivedStateOf { centeredIndex == todayIndex }
    }

    val monthFmt = remember(appLocale) { DateTimeFormatter.ofPattern("MMM", appLocale) }
    val dayAbbrFmt = remember(appLocale) { DateTimeFormatter.ofPattern("EEE", appLocale) }

    val visibleRangeText by remember(days, centeredIndex, monthFmt, isEn) {
        derivedStateOf {
            if (days.isEmpty()) {
                if (isEn) "This Week" else "Bu Hafta"
            } else {
                val centerDay = days[centeredIndex.coerceIn(0, days.lastIndex)].date
                val startDay = centerDay.minusDays(3)
                val endDay = centerDay.plusDays(3)
                if (startDay.month == endDay.month) {
                    "${startDay.dayOfMonth} - ${endDay.dayOfMonth} ${endDay.format(monthFmt)}"
                } else {
                    "${startDay.dayOfMonth} ${startDay.format(monthFmt)} - ${endDay.dayOfMonth} ${endDay.format(monthFmt)}"
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_flow_section")
    ) {
        // Section Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = headerHorizontalPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.CalendarViewWeek,
                    contentDescription = trEn("Haftalık Akış", "Weekly Flow"),
                    tint = dyn.accentCyan,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = trEn("Haftalık Akış", "Weekly Flow"),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = dyn.textPrimary
                )
                Box(
                    modifier = Modifier
                        .background(dyn.surfacePanel, RoundedCornerShape(999.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = visibleRangeText,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = dyn.textSecondary
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedVisibility(
                    visible = !isTodayCentered && days.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        onClick = {
                            coroutineScope.launch {
                                listState.animateScrollToItem(index = todayIndex, scrollOffset = 0)
                            }
                        },
                        shape = RoundedCornerShape(999.dp),
                        color = dyn.primaryNeon.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, dyn.primaryNeon.copy(alpha = 0.45f)),
                        modifier = Modifier.testTag("weekly_flow_today_button")
                    ) {
                        Text(
                            text = trEn("Bugün", "Today"),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.primaryNeon,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    text = trEn("Tümü →", "All →"),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = dyn.accentCyan,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onViewAllClick)
                        .padding(horizontal = 4.dp, vertical = 6.dp)
                        .testTag("home_weekly_all_button")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontally Scrollable Carousel with Active Day Centered
        val itemWidth = 70.dp
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val sidePadding = ((maxWidth - itemWidth) / 2f).coerceAtLeast(8.dp)

            LazyRow(
                state = listState,
                flingBehavior = snapFlingBehavior,
                contentPadding = PaddingValues(horizontal = sidePadding),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weekly_flow_lazy_row")
            ) {
                itemsIndexed(
                    items = days,
                    key = { _, day -> day.date.toEpochDay() }
                ) { index, day ->
                    val isToday = day.date == todayDate
                    val isCentered = index == centeredIndex
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

                    val cardModifier = Modifier
                        .width(itemWidth)
                        .heightIn(min = if (isToday) 104.dp else 94.dp)
                        .then(
                            if (isToday) {
                                Modifier.shadow(
                                    elevation = 10.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    ambientColor = dyn.primaryNeon,
                                    spotColor = dyn.primaryNeon
                                )
                            } else {
                                Modifier
                            }
                        )
                        .titaniumBrushedSurface(
                            shape = RoundedCornerShape(16.dp),
                            borderColor = when {
                                isToday -> dyn.primaryNeon
                                isCentered -> dyn.accentCyan.copy(alpha = 0.65f)
                                isEveningShift -> ShiftOvertimeAmber.copy(alpha = 0.50f)
                                isNightShift -> ShiftGece.copy(alpha = 0.42f)
                                isDayShift -> ShiftGunduz.copy(alpha = 0.42f)
                                else -> dyn.borderSubtle
                            },
                            borderWidth = if (isToday) 1.8.dp else if (isCentered) 1.3.dp else 1.dp,
                            elevated = isToday || isCentered
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onInspectDay(day) }
                        .testTag(if (isToday) "weekly_flow_today_card" else "weekly_flow_day_${day.date}")

                    Surface(
                        modifier = cardModifier,
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Transparent
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = if (isToday) 10.dp else 8.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = dayName,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isToday) dyn.primaryNeon else dyn.textSecondary
                                )
                                if (isToday) {
                                    Spacer(modifier = Modifier.width(3.dp))
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
                                fontSize = if (isToday) 18.sp else 16.sp,
                                fontFamily = ProshiftMonoFont,
                                fontWeight = FontWeight.Black,
                                color = if (isToday || day.type.isWork) dyn.textPrimary else dyn.textSecondary
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Icon(
                                imageVector = shiftTypeVector(day.type),
                                contentDescription = day.type.localizedTitle(isEn),
                                tint = cellAccentColor,
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(modifier = Modifier.height(5.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when {
                                    isEveningShift -> ShiftOvertimeAmber.copy(alpha = 0.22f)
                                    isDayShift -> ShiftGunduz.copy(alpha = 0.20f)
                                    isNightShift -> ShiftGece.copy(alpha = 0.20f)
                                    else -> dyn.surfaceElevated
                                }
                            ) {
                                Text(
                                    text = badge,
                                    fontSize = 9.sp,
                                    fontFamily = ProshiftMonoFont,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        isEveningShift -> ShiftOvertimeAmber
                                        isDayShift -> ShiftGunduz
                                        isNightShift -> if (dyn.isDark) Color(0xFFA5B4FC) else Color(0xFF4F46E5)
                                        else -> dyn.textMuted
                                    },
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
