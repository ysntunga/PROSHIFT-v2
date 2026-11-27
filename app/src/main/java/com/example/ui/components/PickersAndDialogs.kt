@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CyclePreset
import com.example.data.model.ProfessionGroup
import com.example.data.model.ShiftType
import com.example.data.model.TimesheetStats
import com.example.data.model.UserSettings
import com.example.ui.i18n.LocalAppLocale
import com.example.ui.i18n.LocalIsEnglish
import com.example.ui.i18n.localizedName
import com.example.ui.i18n.localizedTitle
import com.example.ui.i18n.localizedTitles
import com.example.ui.i18n.trEn
import com.example.ui.theme.DynamicTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

fun parseHourMinute(
    timeString: String,
    defaultHour: Int = 8,
    defaultMinute: Int = 0,
    allow24Hour: Boolean = false
): Pair<Int, Int> {
    val parts = timeString.trim().split(":")
    if (parts.size != 2) return defaultHour to defaultMinute
    val maxH = if (allow24Hour) 24 else 23
    val h = parts[0].toIntOrNull()?.coerceIn(0, maxH) ?: defaultHour
    val m = if (h == 24) 0 else parts[1].toIntOrNull()?.coerceIn(0, 59) ?: defaultMinute
    return h to m
}

fun formatHourMinute(hour: Int, minute: Int, allow24Hour: Boolean = false): String {
    val clampedH = hour.coerceIn(0, if (allow24Hour) 24 else 23)
    val clampedM = if (clampedH == 24) 0 else minute.coerceIn(0, 59)
    return String.format(Locale.ROOT, "%02d:%02d", clampedH, clampedM)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShiftTimePickerSheet(
    title: String? = null,
    subtitle: String? = null,
    initialTime: String = "08:00",
    quickPresets: List<String>? = null,
    allow24Hour: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val isEn = LocalIsEnglish.current
    val resolvedTitle = title ?: trEn(isEn, "Saat Seçimi", "Select Time")
    val resolvedSubtitle = subtitle ?: trEn(
        isEn,
        "Kaydırarak veya +/- butonlarıyla saat seçin",
        "Scroll or use +/- buttons to pick a time"
    )
    val maxHour = if (allow24Hour) 24 else 23
    val hourCount = maxHour + 1
    val resolvedPresets = remember(quickPresets, allow24Hour) {
        quickPresets ?: if (allow24Hour) {
            listOf("00:00", "08:00", "16:00", "20:00", "24:00")
        } else {
            listOf("07:00", "08:00", "08:30", "09:00", "16:00", "20:00", "00:00")
        }
    }

    val (initHour, initMinute) = remember(initialTime, allow24Hour) {
        parseHourMinute(initialTime, allow24Hour = allow24Hour)
    }
    var selectedHour by remember(initialTime, allow24Hour) { mutableIntStateOf(initHour) }
    var selectedMinute by remember(initialTime, allow24Hour) { mutableIntStateOf(initMinute) }

    val hourListState = rememberLazyListState(initialFirstVisibleItemIndex = initHour)
    val minuteListState = rememberLazyListState(initialFirstVisibleItemIndex = initMinute)
    val coroutineScope = rememberCoroutineScope()

    val currentCenteredHour by remember {
        derivedStateOf {
            val layout = hourListState.layoutInfo
            val center = layout.viewportStartOffset + (layout.viewportEndOffset - layout.viewportStartOffset) / 2
            layout.visibleItemsInfo.minByOrNull { kotlin.math.abs((it.offset + it.size / 2) - center) }?.index
                ?: selectedHour
        }
    }
    val currentCenteredMinute by remember {
        derivedStateOf {
            val layout = minuteListState.layoutInfo
            val center = layout.viewportStartOffset + (layout.viewportEndOffset - layout.viewportStartOffset) / 2
            layout.visibleItemsInfo.minByOrNull { kotlin.math.abs((it.offset + it.size / 2) - center) }?.index
                ?: selectedMinute
        }
    }

    LaunchedEffect(hourListState.isScrollInProgress, maxHour) {
        if (!hourListState.isScrollInProgress) {
            selectedHour = currentCenteredHour.coerceIn(0, maxHour)
            if (selectedHour == 24 && selectedMinute != 0) {
                selectedMinute = 0
                minuteListState.animateScrollToItem(0)
            }
        }
    }

    LaunchedEffect(minuteListState.isScrollInProgress) {
        if (!minuteListState.isScrollInProgress) {
            if (selectedHour == 24) {
                if (currentCenteredMinute != 0) {
                    selectedMinute = 0
                    minuteListState.animateScrollToItem(0)
                }
            } else {
                selectedMinute = currentCenteredMinute.coerceIn(0, 59)
            }
        }
    }

    LaunchedEffect(selectedHour) {
        if (selectedHour == 24 && selectedMinute != 0) {
            selectedMinute = 0
            minuteListState.animateScrollToItem(0)
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dyn = DynamicTheme.colors

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = dyn.surfaceCard,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(dyn.activeChipBg, RoundedCornerShape(10.dp))
                            .border(1.dp, dyn.borderGlow, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = resolvedTitle,
                            tint = dyn.accentCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(text = resolvedTitle, fontSize = 17.sp, fontWeight = FontWeight.Black, color = dyn.textPrimary)
                        Text(text = resolvedSubtitle, fontSize = 11.sp, color = dyn.textSecondary)
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(dyn.surfacePanel)
                        .border(1.dp, dyn.borderMetal, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = formatHourMinute(selectedHour, selectedMinute, allow24Hour),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = dyn.accentCyan,
                        modifier = Modifier.testTag("time_picker_preview_text")
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(dyn.surfaceDim)
                    .border(1.dp, dyn.borderMetal, RoundedCornerShape(18.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(trEn(isEn, "SAAT", "HOUR"), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = dyn.textMuted)
                        IconButton(
                            onClick = {
                                val next = (selectedHour + hourCount - 1) % hourCount
                                selectedHour = next
                                coroutineScope.launch { hourListState.animateScrollToItem(next) }
                            },
                            modifier = Modifier.size(40.dp).testTag("btn_hour_dec")
                        ) {
                            Icon(Icons.Filled.KeyboardArrowUp, contentDescription = trEn(isEn, "Saat Azalt", "Decrease Hour"), tint = dyn.textSecondary)
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(132.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(dyn.activeChipBg)
                                    .border(1.dp, dyn.accentCyan.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                            )
                            LazyColumn(
                                state = hourListState,
                                flingBehavior = rememberSnapFlingBehavior(lazyListState = hourListState),
                                contentPadding = PaddingValues(vertical = 44.dp),
                                modifier = Modifier.fillMaxWidth().height(132.dp)
                            ) {
                                items((0..maxHour).toList()) { h ->
                                    val isSelected = h == selectedHour
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(44.dp)
                                            .clickable {
                                                selectedHour = h
                                                coroutineScope.launch { hourListState.animateScrollToItem(h) }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = String.format(Locale.ROOT, "%02d", h),
                                            fontSize = if (isSelected) 28.sp else 18.sp,
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                            fontFamily = FontFamily.Monospace,
                                            color = if (isSelected) dyn.textPrimary else dyn.textMuted
                                        )
                                    }
                                }
                            }
                        }

                        IconButton(
                            onClick = {
                                val next = (selectedHour + 1) % hourCount
                                selectedHour = next
                                coroutineScope.launch { hourListState.animateScrollToItem(next) }
                            },
                            modifier = Modifier.size(40.dp).testTag("btn_hour_inc")
                        ) {
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = trEn(isEn, "Saat Artır", "Increase Hour"), tint = dyn.textSecondary)
                        }
                    }

                    Text(
                        text = ":",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = dyn.accentCyan,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(trEn(isEn, "DAKİKA", "MINUTE"), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = dyn.textMuted)
                        IconButton(
                            onClick = {
                                if (selectedHour < 24) {
                                    val next = (selectedMinute + 55) % 60
                                    selectedMinute = next
                                    coroutineScope.launch { minuteListState.animateScrollToItem(next) }
                                }
                            },
                            modifier = Modifier.size(40.dp).testTag("btn_min_dec")
                        ) {
                            Icon(Icons.Filled.KeyboardArrowUp, contentDescription = trEn(isEn, "Dakika Azalt", "Decrease Minute"), tint = dyn.textSecondary)
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(132.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(dyn.activeChipBg)
                                    .border(1.dp, dyn.accentCyan.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                            )
                            LazyColumn(
                                state = minuteListState,
                                flingBehavior = rememberSnapFlingBehavior(lazyListState = minuteListState),
                                contentPadding = PaddingValues(vertical = 44.dp),
                                modifier = Modifier.fillMaxWidth().height(132.dp)
                            ) {
                                items((0..59).toList()) { m ->
                                    val isSelected = m == selectedMinute
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(44.dp)
                                            .clickable {
                                                if (selectedHour < 24) {
                                                    selectedMinute = m
                                                    coroutineScope.launch { minuteListState.animateScrollToItem(m) }
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = String.format(Locale.ROOT, "%02d", m),
                                            fontSize = if (isSelected) 28.sp else 18.sp,
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                            fontFamily = FontFamily.Monospace,
                                            color = if (isSelected) dyn.textPrimary else dyn.textMuted
                                        )
                                    }
                                }
                            }
                        }

                        IconButton(
                            onClick = {
                                if (selectedHour < 24) {
                                    val next = (selectedMinute + 5) % 60
                                    selectedMinute = next
                                    coroutineScope.launch { minuteListState.animateScrollToItem(next) }
                                }
                            },
                            modifier = Modifier.size(40.dp).testTag("btn_min_inc")
                        ) {
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = trEn(isEn, "Dakika Artır", "Increase Minute"), tint = dyn.textSecondary)
                        }
                    }
                }
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(resolvedPresets) { preset ->
                    val (ph, pm) = parseHourMinute(preset, allow24Hour = allow24Hour)
                    val isSel = selectedHour == ph && selectedMinute == pm
                    Box(
                        modifier = Modifier
                            .heightIn(min = 40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSel) dyn.activeChipBg else dyn.surfacePanel)
                            .border(1.dp, if (isSel) dyn.accentCyan else dyn.borderSubtle, RoundedCornerShape(10.dp))
                            .clickable {
                                selectedHour = ph
                                selectedMinute = pm
                                coroutineScope.launch {
                                    hourListState.animateScrollToItem(ph)
                                    minuteListState.animateScrollToItem(pm)
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = preset,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (isSel) dyn.accentCyan else dyn.textPrimary
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onDismiss() }
                        .border(1.dp, dyn.borderMetal, RoundedCornerShape(12.dp)),
                    color = dyn.surfaceElevated,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(trEn(isEn, "Vazgeç", "Cancel"), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = dyn.textSecondary)
                    }
                }
                Surface(
                    modifier = Modifier
                        .weight(1.4f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val chosen = formatHourMinute(selectedHour, selectedMinute, allow24Hour)
                            onConfirm(chosen)
                            onDismiss()
                        }
                        .testTag("btn_confirm_time_picker"),
                    color = Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(Brush.horizontalGradient(listOf(dyn.primaryNeon, dyn.primaryDeep))),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            val formattedSelected = formatHourMinute(selectedHour, selectedMinute, allow24Hour)
                            Text(
                                trEn(isEn, "Saati Uygula ($formattedSelected)", "Apply Time ($formattedSelected)"),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

private enum class DatePickerSubView { DAYS, YEARS, MONTHS }

@Composable
fun TitaniumDatePickerDialog(
    title: String? = null,
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    val isEn = LocalIsEnglish.current
    val appLocale = LocalAppLocale.current
    val resolvedTitle = title ?: trEn(isEn, "Tarih Seçin", "Select Date")
    var displayedYm by remember(initialDate) { mutableStateOf(YearMonth.from(initialDate)) }
    var chosenDate by remember(initialDate) { mutableStateOf(initialDate) }
    var subView by remember { mutableStateOf(DatePickerSubView.DAYS) }

    val yearsRange = remember { (1990..2035).toList() }
    val yearRows = remember(yearsRange) { yearsRange.chunked(3) }
    val selectedYearRowIndex = remember(displayedYm.year) {
        ((displayedYm.year - yearsRange.first()) / 3).coerceIn(0, (yearRows.size - 1).coerceAtLeast(0))
    }
    val yearGridState = rememberLazyListState(initialFirstVisibleItemIndex = selectedYearRowIndex)

    LaunchedEffect(subView, displayedYm.year) {
        if (subView == DatePickerSubView.YEARS) {
            yearGridState.scrollToItem((selectedYearRowIndex - 1).coerceAtLeast(0))
        }
    }

    val monthName = remember(displayedYm, appLocale) {
        displayedYm.atDay(1).format(DateTimeFormatter.ofPattern("MMMM", appLocale)).uppercase(appLocale)
    }
    val monthShortNames = remember(appLocale) {
        (1..12).map { m ->
            YearMonth.of(2026, m).atDay(1).format(DateTimeFormatter.ofPattern("MMMM", appLocale))
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(appLocale) else it.toString() }
        }
    }

    fun applyYearMonth(newYm: YearMonth) {
        val clampedYear = newYm.year.coerceIn(yearsRange.first(), yearsRange.last())
        val targetYm = YearMonth.of(clampedYear, newYm.monthValue)
        displayedYm = targetYm
        chosenDate = targetYm.atDay(chosenDate.dayOfMonth.coerceAtMost(targetYm.lengthOfMonth()))
    }

    val dyn = DynamicTheme.colors

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dyn.surfaceCard,
        title = {
            Column {
                Text(text = resolvedTitle, fontSize = 17.sp, fontWeight = FontWeight.Black, color = dyn.textPrimary)
                Text(
                    text = chosenDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy, EEEE", appLocale)),
                    fontSize = 12.sp,
                    color = dyn.accentCyan,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(dyn.surfaceElevated)
                        .border(
                            1.dp,
                            if (subView == DatePickerSubView.YEARS) dyn.accentCyan else dyn.borderMetal,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { applyYearMonth(displayedYm.minusYears(1)) },
                        modifier = Modifier.size(44.dp).testTag("btn_date_prev_year")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = trEn(isEn, "Önceki Yıl", "Previous Year"), tint = dyn.accentCyan)
                    }

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                subView = if (subView == DatePickerSubView.YEARS) DatePickerSubView.DAYS else DatePickerSubView.YEARS
                            }
                            .testTag("btn_toggle_year_grid"),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = trEn(isEn, "YIL: ${displayedYm.year}", "YEAR: ${displayedYm.year}"),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = dyn.accentCyan
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (subView == DatePickerSubView.YEARS) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = trEn(isEn, "Yıl Listesi", "Year List"),
                            tint = dyn.accentCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { applyYearMonth(displayedYm.plusYears(1)) },
                        modifier = Modifier.size(44.dp).testTag("btn_date_next_year")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = trEn(isEn, "Sonraki Yıl", "Next Year"), tint = dyn.accentCyan)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(dyn.surfacePanel)
                        .border(
                            1.dp,
                            if (subView == DatePickerSubView.MONTHS) dyn.accentCyan else dyn.borderSubtle,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { applyYearMonth(displayedYm.minusMonths(1)) },
                        modifier = Modifier.size(44.dp).testTag("btn_date_prev_month")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = trEn(isEn, "Önceki Ay", "Previous Month"), tint = dyn.textPrimary)
                    }

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                subView = if (subView == DatePickerSubView.MONTHS) DatePickerSubView.DAYS else DatePickerSubView.MONTHS
                            }
                            .testTag("btn_toggle_month_grid"),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(monthName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = dyn.textPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (subView == DatePickerSubView.MONTHS) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = trEn(isEn, "Ay Listesi", "Month List"),
                            tint = dyn.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { applyYearMonth(displayedYm.plusMonths(1)) },
                        modifier = Modifier.size(44.dp).testTag("btn_date_next_month")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = trEn(isEn, "Sonraki Ay", "Next Month"), tint = dyn.textPrimary)
                    }
                }

                when (subView) {
                    DatePickerSubView.YEARS -> {
                        LazyColumn(
                            state = yearGridState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(yearRows) { rowYears ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    rowYears.forEach { yr ->
                                        val isSel = yr == displayedYm.year
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSel) dyn.primaryNeon else dyn.surfaceElevated)
                                                .border(
                                                    1.dp,
                                                    if (isSel) dyn.accentCyan else dyn.borderSubtle,
                                                    RoundedCornerShape(10.dp)
                                                )
                                                .clickable {
                                                    applyYearMonth(YearMonth.of(yr, displayedYm.monthValue))
                                                    subView = DatePickerSubView.DAYS
                                                }
                                                .testTag("year_option_$yr"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "$yr",
                                                fontSize = 13.sp,
                                                fontWeight = if (isSel) FontWeight.Black else FontWeight.SemiBold,
                                                fontFamily = FontFamily.Monospace,
                                                color = if (isSel) dyn.textOnPrimary else dyn.textPrimary
                                            )
                                        }
                                    }
                                    repeat(3 - rowYears.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    DatePickerSubView.MONTHS -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            monthShortNames.chunked(3).forEachIndexed { rIdx, rowMonths ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    rowMonths.forEachIndexed { cIdx, label ->
                                        val mVal = rIdx * 3 + cIdx + 1
                                        val isSel = mVal == displayedYm.monthValue
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(46.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSel) dyn.primaryNeon else dyn.surfaceElevated)
                                                .border(
                                                    1.dp,
                                                    if (isSel) dyn.accentCyan else dyn.borderSubtle,
                                                    RoundedCornerShape(10.dp)
                                                )
                                                .clickable {
                                                    applyYearMonth(YearMonth.of(displayedYm.year, mVal))
                                                    subView = DatePickerSubView.DAYS
                                                }
                                                .testTag("month_option_$mVal"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSel) FontWeight.Black else FontWeight.SemiBold,
                                                color = if (isSel) dyn.textOnPrimary else dyn.textPrimary,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    DatePickerSubView.DAYS -> {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            val weekdayLabels = if (isEn) {
                                listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")
                            } else {
                                listOf("Pt", "Sa", "Ça", "Pe", "Cu", "Ct", "Pz")
                            }
                            weekdayLabels.forEach { wd ->
                                Text(
                                    text = wd,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = dyn.textMuted
                                )
                            }
                        }

                        val firstDayOfMonth = displayedYm.atDay(1)
                        val leadingEmpty = firstDayOfMonth.dayOfWeek.value - 1
                        val totalDays = displayedYm.lengthOfMonth()
                        val cells = leadingEmpty + totalDays
                        val rows = (cells + 6) / 7

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (r in 0 until rows) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    for (c in 0 until 7) {
                                        val cellIndex = r * 7 + c
                                        val dayNum = cellIndex - leadingEmpty + 1
                                        if (dayNum in 1..totalDays) {
                                            val cellDate = displayedYm.atDay(dayNum)
                                            val isSel = cellDate == chosenDate
                                            val isToday = cellDate == LocalDate.now()
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(36.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSel) dyn.primaryNeon else dyn.surfaceElevated)
                                                    .border(
                                                        width = if (isToday && !isSel) 1.dp else 0.dp,
                                                        color = if (isToday && !isSel) dyn.accentCyan else Color.Transparent,
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                    .clickable { chosenDate = cellDate },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "$dayNum",
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSel || isToday) FontWeight.Black else FontWeight.Medium,
                                                    color = if (isSel) dyn.textOnPrimary else dyn.textPrimary
                                                )
                                            }
                                        } else {
                                            Spacer(modifier = Modifier.weight(1f).height(36.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                TextButton(
                    onClick = {
                        val today = LocalDate.now()
                        chosenDate = today
                        displayedYm = YearMonth.from(today)
                        subView = DatePickerSubView.DAYS
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(trEn(isEn, "Bugüne Git", "Go to Today"), color = dyn.accentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(chosenDate)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = dyn.primaryNeon),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(trEn(isEn, "Tarihi Seç", "Select Date"), color = dyn.textOnPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(trEn(isEn, "İptal", "Cancel"), color = dyn.textSecondary)
            }
        }
    )
}

@Composable
fun EditProfileSheet(
    currentSettings: UserSettings,
    onDismiss: () -> Unit,
    onSave: (UserSettings) -> Unit
) {
    val dyn = DynamicTheme.colors
    val context = LocalContext.current
    val isEn = LocalIsEnglish.current
    val appLocale = LocalAppLocale.current
    var name by remember(currentSettings) { mutableStateOf(currentSettings.employeeName) }
    var selectedGroupId by remember(currentSettings) { mutableStateOf(currentSettings.professionGroup) }
    var title by remember(currentSettings) { mutableStateOf(currentSettings.title) }
    var dept by remember(currentSettings) { mutableStateOf(currentSettings.department) }
    var avatarPath by remember(currentSettings) { mutableStateOf(currentSettings.avatarUriOrPath) }
    var wageText by remember(currentSettings) { mutableStateOf(currentSettings.hourlyWage.toInt().toString()) }
    var usedLeave by remember(currentSettings) { mutableIntStateOf(currentSettings.usedLeaveDays) }
    var empDate by remember(currentSettings) { mutableStateOf(currentSettings.employmentStartDate) }
    var birthDate by remember(currentSettings) { mutableStateOf(currentSettings.birthDate) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showBirthDatePicker by remember { mutableStateOf(false) }

    val activeGroup = remember(selectedGroupId) { ProfessionGroup.fromId(selectedGroupId) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = savePickedAvatarToInternalStorage(context, uri)
            if (savedPath != null) {
                avatarPath = savedPath
            } else {
                Toast.makeText(context, trEn(isEn, "Fotoğraf yüklenemedi", "Could not load photo"), Toast.LENGTH_SHORT).show()
            }
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = dyn.surfaceCard,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                .imePadding()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                trEn(isEn, "Personel, Meslek & Bordro Profili", "Staff, Profession & Payroll Profile"),
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = dyn.textPrimary
            )
            Text(
                trEn(
                    isEn,
                    "Profil fotoğrafınız, meslek grubunuz ve ünvanınız tüm kartlarda ve QR eşlemede görünür:",
                    "Your profile photo, profession group, and title appear across all cards and QR matching:"
                ),
                fontSize = 12.sp,
                color = dyn.textSecondary
            )

            // Profile Photo Picker
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = dyn.surfacePanel
            ) {
                Row(
                    modifier = Modifier
                        .border(1.dp, dyn.borderMetal, RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .border(2.dp, dyn.accentCyan, CircleShape)
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                    ) {
                        UserAvatarImage(
                            avatarPath = avatarPath,
                            contentDescription = trEn(isEn, "Profil Fotoğrafı", "Profile Photo"),
                            displayName = name,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(trEn(isEn, "Profil Fotoğrafı", "Profile Photo"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = dyn.textPrimary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                    .border(1.dp, dyn.accentCyan.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                    .testTag("btn_sheet_pick_avatar"),
                                shape = RoundedCornerShape(10.dp),
                                color = dyn.activeChipBg
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Filled.PhotoCamera, contentDescription = null, tint = dyn.accentCyan, modifier = Modifier.size(16.dp))
                                    Text(trEn(isEn, "Fotoğraf Seç", "Choose Photo"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = dyn.accentCyan)
                                }
                            }

                            if (avatarPath.isNotBlank()) {
                                Surface(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { avatarPath = "" }
                                        .border(1.dp, dyn.borderSubtle, RoundedCornerShape(10.dp)),
                                    shape = RoundedCornerShape(10.dp),
                                    color = dyn.surfaceElevated
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Filled.RestartAlt,
                                            contentDescription = trEn(isEn, "Fotoğrafı Kaldır", "Remove Photo"),
                                            tint = dyn.textSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(trEn(isEn, "Ad Soyad", "Full Name"), color = dyn.textSecondary, fontSize = 12.sp) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = dyn.primaryNeon,
                    unfocusedBorderColor = dyn.borderMetal,
                    focusedTextColor = dyn.textPrimary,
                    unfocusedTextColor = dyn.textPrimary
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Profession Group Selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(trEn(isEn, "MESLEK GRUBU", "PROFESSION GROUP"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = dyn.accentCyan, letterSpacing = 0.8.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ProfessionGroup.entries) { group ->
                        val isSelected = selectedGroupId == group.id
                        Surface(
                            modifier = Modifier
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedGroupId = group.id
                                    val isPresetFromAnyGroup = ProfessionGroup.entries.any {
                                        it.titles.contains(title) || it.localizedTitles(true).contains(title)
                                    }
                                    if (title.isBlank() || isPresetFromAnyGroup) {
                                        title = group.localizedTitles(isEn).firstOrNull() ?: title
                                    }
                                }
                                .border(1.dp, if (isSelected) dyn.accentCyan else dyn.borderSubtle, RoundedCornerShape(12.dp))
                                .testTag("sheet_group_${group.id}"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) dyn.activeChipBg else dyn.surfaceElevated
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(group.emoji, fontSize = 14.sp)
                                Text(
                                    text = group.localizedName(isEn),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) dyn.accentCyan else dyn.textPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Quick Title Selector for Selected Group
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    trEn(
                        isEn,
                        "ÜNVAN / GÖREV HIZLI SEÇİM (${activeGroup.localizedName(false).uppercase(appLocale)})",
                        "QUICK TITLE / ROLE (${activeGroup.localizedName(true).uppercase(appLocale)})"
                    ),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = dyn.textMuted,
                    letterSpacing = 0.6.sp
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(activeGroup.localizedTitles(isEn)) { presetTitle ->
                        val isSel = title.equals(presetTitle, ignoreCase = true)
                        Surface(
                            modifier = Modifier
                                .height(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { title = presetTitle }
                                .border(1.dp, if (isSel) dyn.primaryNeon else dyn.borderSubtle, RoundedCornerShape(10.dp)),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) dyn.activeChipBg else dyn.surfacePanel
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = presetTitle,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) dyn.accentCyan else dyn.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(trEn(isEn, "Ünvan / Görev (Özel Yazabilirsiniz)", "Title / Role (Custom allowed)"), color = dyn.textSecondary, fontSize = 12.sp) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = dyn.primaryNeon,
                    unfocusedBorderColor = dyn.borderMetal,
                    focusedTextColor = dyn.textPrimary,
                    unfocusedTextColor = dyn.textPrimary
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = dept,
                onValueChange = { dept = it },
                label = { Text(trEn(isEn, "Birim / Tesis / Şube", "Department / Facility / Unit"), color = dyn.textSecondary, fontSize = 12.sp) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = dyn.primaryNeon,
                    unfocusedBorderColor = dyn.borderMetal,
                    focusedTextColor = dyn.textPrimary,
                    unfocusedTextColor = dyn.textPrimary
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = wageText,
                    onValueChange = { input -> wageText = input.filter { it.isDigit() } },
                    label = { Text(trEn(isEn, "Saatlik Ücret (₺)", "Hourly Rate ($)"), color = dyn.textSecondary, fontSize = 11.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = dyn.primaryNeon,
                        unfocusedBorderColor = dyn.borderMetal,
                        focusedTextColor = dyn.textPrimary,
                        unfocusedTextColor = dyn.textPrimary
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(dyn.surfacePanel)
                        .border(1.dp, dyn.borderMetal, RoundedCornerShape(12.dp))
                        .clickable { showDatePicker = true }
                        .padding(12.dp)
                ) {
                    Text(trEn(isEn, "İşe Giriş (Kıdem)", "Hire Date (Tenure)"), fontSize = 10.sp, color = dyn.textSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = dyn.accentCyan, modifier = Modifier.size(14.dp))
                        Text(
                            empDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.textPrimary
                        )
                    }
                }
            }

            // Birth Date (Optional — age-based minimum leave protection)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(dyn.surfacePanel)
                    .border(1.dp, dyn.borderMetal, RoundedCornerShape(12.dp))
                    .clickable { showBirthDatePicker = true }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(trEn(isEn, "Doğum Tarihi (İsteğe Bağlı)", "Date of Birth (Optional)"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = dyn.textPrimary)
                    Text(
                        trEn(
                            isEn,
                            "50+ yaş ve 18- altı için en az 20 gün yıllık izin koruması",
                            "Minimum 20 days annual leave protection for age 50+ or under 18"
                        ),
                        fontSize = 10.sp,
                        color = dyn.textMuted
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = birthDate?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: trEn(isEn, "Seç", "Select"),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (birthDate != null) dyn.accentCyan else dyn.textSecondary
                    )
                    if (birthDate != null) {
                        Text(
                            text = "✕",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.dangerAccent,
                            modifier = Modifier
                                .clickable { birthDate = null }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Used Leave Stepper
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(dyn.surfacePanel)
                    .border(1.dp, dyn.borderSubtle, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(trEn(isEn, "Kullanılan Yıllık İzin", "Used Annual Leave"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = dyn.textPrimary)
                    Text(
                        trEn(
                            isEn,
                            "4857 SK md. 53 kalan izin hesabından düşülür",
                            "Deducted from your remaining annual leave balance"
                        ),
                        fontSize = 10.sp,
                        color = dyn.textMuted
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(dyn.surfaceElevated)
                            .border(1.dp, dyn.borderMetal, CircleShape)
                            .clickable { if (usedLeave > 0) usedLeave-- },
                        contentAlignment = Alignment.Center
                    ) { Text("-", fontWeight = FontWeight.Black, fontSize = 18.sp, color = dyn.textPrimary) }
                    Text(trEn(isEn, "$usedLeave Gün", "$usedLeave Days"), fontSize = 14.sp, fontWeight = FontWeight.Black, color = dyn.accentCyan)
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(dyn.surfaceElevated)
                            .border(1.dp, dyn.borderMetal, CircleShape)
                            .clickable { if (usedLeave < 45) usedLeave++ },
                        contentAlignment = Alignment.Center
                    ) { Text("+", fontWeight = FontWeight.Black, fontSize = 16.sp, color = dyn.textPrimary) }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onDismiss() }
                        .border(1.dp, dyn.borderMetal, RoundedCornerShape(12.dp)),
                    color = dyn.surfaceElevated
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(trEn(isEn, "İptal", "Cancel"), fontWeight = FontWeight.SemiBold, color = dyn.textSecondary)
                    }
                }
                Surface(
                    modifier = Modifier
                        .weight(1.4f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onSave(
                                currentSettings.copy(
                                    employeeName = name.ifBlank { currentSettings.employeeName },
                                    professionGroup = selectedGroupId,
                                    title = title.ifBlank { currentSettings.title },
                                    department = dept.ifBlank { currentSettings.department },
                                    avatarUriOrPath = avatarPath,
                                    hourlyWage = wageText.toDoubleOrNull()?.coerceIn(50.0, 5000.0) ?: currentSettings.hourlyWage,
                                    usedLeaveDays = usedLeave,
                                    employmentStartDate = empDate,
                                    birthDate = birthDate
                                )
                            )
                            onDismiss()
                        },
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(Brush.horizontalGradient(listOf(dyn.primaryNeon, dyn.primaryDeep))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(trEn(isEn, "Bilgileri Kaydet", "Save Profile"), fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        TitaniumDatePickerDialog(
            title = trEn(isEn, "İşe Başlama Tarihi", "Employment Start Date"),
            initialDate = empDate,
            onDismiss = { showDatePicker = false },
            onConfirm = { empDate = it }
        )
    }

    if (showBirthDatePicker) {
        TitaniumDatePickerDialog(
            title = trEn(isEn, "Doğum Tarihi", "Date of Birth"),
            initialDate = birthDate ?: LocalDate.of(1985, 1, 1),
            onDismiss = { showBirthDatePicker = false },
            onConfirm = { birthDate = it }
        )
    }
}

@Composable
fun CustomCycleBuilderSheet(
    initialSequence: List<ShiftType>,
    allowEmptyInitial: Boolean = false,
    titleText: String? = null,
    onDismiss: () -> Unit,
    onSave: (List<ShiftType>) -> Unit
) {
    val dyn = DynamicTheme.colors
    val isEn = LocalIsEnglish.current
    val resolvedTitle = titleText ?: trEn(isEn, "Özel Döngü Oluşturucu", "Custom Cycle Builder")
    val sequence = remember(initialSequence, allowEmptyInitial) {
        mutableStateListOf<ShiftType>().apply {
            if (allowEmptyInitial) {
                addAll(initialSequence)
            } else {
                addAll(initialSequence.ifEmpty { listOf(ShiftType.GUNDUZ, ShiftType.GECE, ShiftType.IZIN) })
            }
        }
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = dyn.surfaceCard,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                .imePadding()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(resolvedTitle, fontSize = 18.sp, fontWeight = FontWeight.Black, color = dyn.textPrimary)
                    Text(
                        trEn(
                            isEn,
                            "Kurumunuza özgü tekrar eden vardiya sırasını dizin (maks. 28 gün)",
                            "Build your repeating shift sequence step by step (max 28 days)"
                        ),
                        fontSize = 11.sp,
                        color = dyn.textSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(dyn.primaryNeon.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        trEn(isEn, "${sequence.size} Günlük", "${sequence.size} Days"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = dyn.accentCyan
                    )
                }
            }

            // Quick templates & Clear row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            sequence.clear()
                            sequence.addAll(
                                listOf(
                                    ShiftType.SABAH, ShiftType.SABAH,
                                    ShiftType.AKSAM, ShiftType.AKSAM,
                                    ShiftType.GECE8, ShiftType.GECE8
                                )
                            )
                        }
                        .border(1.dp, dyn.accentCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                    color = dyn.activeChipBg
                ) {
                    Text(
                        text = trEn(isEn, "2S-2A-2G (8s)", "2M-2E-2N (8h)"),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = dyn.accentCyan,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            sequence.clear()
                            sequence.addAll(
                                listOf(
                                    ShiftType.SABAH, ShiftType.SABAH,
                                    ShiftType.AKSAM, ShiftType.AKSAM,
                                    ShiftType.GECE8, ShiftType.GECE8,
                                    ShiftType.IZIN, ShiftType.IZIN
                                )
                            )
                        }
                        .border(1.dp, dyn.borderMetal, RoundedCornerShape(8.dp)),
                    color = dyn.surfaceElevated
                ) {
                    Text(
                        text = trEn(isEn, "2S-2A-2G-2İ", "2M-2E-2N-2O"),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = dyn.textPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { sequence.clear() }
                        .border(1.dp, dyn.dangerAccent.copy(alpha = 0.45f), RoundedCornerShape(8.dp)),
                    color = if (dyn.isDark) Color(0xFF28121A) else Color(0xFFFEE2E2)
                ) {
                    Text(
                        text = trEn(isEn, "Temizle", "Clear"),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = dyn.dangerAccent,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }

            // Current sequence chips
            if (sequence.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(dyn.surfacePanel)
                        .border(1.dp, dyn.borderMetal, RoundedCornerShape(14.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = trEn(
                            isEn,
                            "Döngü boş — aşağıdan vardiya adımlarını sırayla ekleyin",
                            "Cycle is empty — add shift steps below in order"
                        ),
                        fontSize = 11.sp,
                        color = dyn.textMuted
                    )
                }
            } else {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(dyn.surfacePanel)
                        .border(1.dp, dyn.borderMetal, RoundedCornerShape(14.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sequence.size) { index ->
                        val st = sequence[index]
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(dyn.surfaceElevated)
                                .border(1.dp, dyn.borderSubtle, RoundedCornerShape(10.dp))
                                .clickable { sequence.removeAt(index) }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(trEn(isEn, "${index + 1}. Gün", "Day ${index + 1}"), fontSize = 8.sp, color = dyn.textMuted)
                            Text(st.iconEmoji, fontSize = 14.sp)
                            Text(st.localizedTitle(isEn).substringBefore(" ("), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = dyn.textPrimary)
                            Text(
                                text = if (st.isWork) "${st.defaultStart.take(2)}-${st.defaultEnd.take(2)}" else trEn(isEn, "İzin", "Off"),
                                fontSize = 8.sp,
                                color = dyn.accentCyan
                            )
                            Text(trEn(isEn, "Kaldır", "Remove"), fontSize = 8.sp, color = dyn.dangerAccent)
                        }
                    }
                }
            }

            Text(
                trEn(isEn, "8 Saatlik Üçlü Vardiya Adımları:", "8-Hour 3-Shift Steps:"),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = dyn.accentCyan
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(ShiftType.SABAH, ShiftType.AKSAM, ShiftType.GECE8).forEach { st ->
                    val hoursTag = "${st.defaultStart.take(2)}-${st.defaultEnd.take(2)}"
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { if (sequence.size < 28) sequence.add(st) }
                            .border(1.dp, dyn.accentCyan.copy(alpha = 0.45f), RoundedCornerShape(10.dp)),
                        color = dyn.activeChipBg
                    ) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text("${st.iconEmoji} + ${st.localizedTitle(isEn).substringBefore(" (")}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = dyn.textPrimary)
                            Text(hoursTag, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = dyn.accentCyan)
                        }
                    }
                }
            }

            Text(
                trEn(isEn, "12 / 24 Saatlik Vardiya & İzin:", "12 / 24-Hour Shift & Off:"),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = dyn.textSecondary
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(ShiftType.GUNDUZ, ShiftType.GECE, ShiftType.NOBET24, ShiftType.IZIN).forEach { st ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { if (sequence.size < 28) sequence.add(st) }
                            .border(1.dp, dyn.borderMetal, RoundedCornerShape(10.dp)),
                        color = dyn.surfaceElevated
                    ) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text(st.iconEmoji, fontSize = 12.sp)
                            Text("+ ${st.localizedTitle(isEn).substringBefore(" (")}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = dyn.textPrimary, maxLines = 1)
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onDismiss() }
                        .border(1.dp, dyn.borderMetal, RoundedCornerShape(12.dp)),
                    color = dyn.surfaceElevated
                ) {
                    Box(contentAlignment = Alignment.Center) { Text(trEn(isEn, "Vazgeç", "Cancel"), color = dyn.textSecondary, fontWeight = FontWeight.SemiBold) }
                }
                val canSave = sequence.isNotEmpty()
                Surface(
                    modifier = Modifier
                        .weight(1.4f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = canSave) {
                            if (canSave) {
                                onSave(sequence.toList())
                                onDismiss()
                            }
                        },
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(
                                if (canSave) Brush.horizontalGradient(listOf(dyn.primaryNeon, dyn.primaryDeep))
                                else Brush.horizontalGradient(listOf(dyn.surfaceElevated, dyn.surfaceElevated))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (canSave) {
                                trEn(isEn, "Özel Döngüyü Uygula", "Apply Custom Cycle")
                            } else {
                                trEn(isEn, "En Az 1 Gün Ekleyin", "Add At Least 1 Day")
                            },
                            fontWeight = FontWeight.Bold,
                            color = if (canSave) Color.White else dyn.textMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LeavePetitionDialog(
    settings: UserSettings,
    stats: TimesheetStats,
    onDismiss: () -> Unit,
    onShare: (String) -> Unit
) {
    val dyn = DynamicTheme.colors
    val isEn = LocalIsEnglish.current
    val appLocale = LocalAppLocale.current
    var startDate by remember { mutableStateOf(LocalDate.now().plusDays(7)) }
    var daysCount by remember { mutableIntStateOf(3.coerceAtMost(stats.remainingLeaveDays.coerceAtLeast(1))) }
    var showDatePicker by remember { mutableStateOf(false) }

    val fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val endDate = startDate.plusDays(daysCount.toLong() - 1)
    val petitionText = if (isEn) {
        """
        TO THE ${settings.department.uppercase(appLocale)} DEPARTMENT

        I am currently employed in your organization as ${settings.title}. I would like to request $daysCount days of paid annual leave from my remaining entitlement (Remaining Leave: ${stats.remainingLeaveDays} days) for the dates listed below.

        Leave Start Date: ${startDate.format(fmt)}
        Leave End Date: ${endDate.format(fmt)}
        Return to Duty Date: ${endDate.plusDays(1).format(fmt)}

        Respectfully submitted for your approval.

        Employee: ${settings.employeeName}
        Title: ${settings.title}
        Date: ${LocalDate.now().format(fmt)}
        """.trimIndent()
    } else {
        """
        ${settings.department.uppercase(appLocale)} MÜDÜRLÜĞÜNE
        
        Kurumunuz bünyesinde ${settings.title} olarak görev yapmaktayım. 4857 Sayılı İş Kanunu'nun 53. maddesi kapsamındaki yıllık ücretli izin hakkımdan (Kalan İzin: ${stats.remainingLeaveDays} gün) aşağıda belirtilen tarihler arasında $daysCount gün yıllık izin kullanmak istiyorum.
        
        İzin Başlangıç Tarihi: ${startDate.format(fmt)}
        İzin Bitiş Tarihi: ${endDate.format(fmt)}
        Göreve Başlama Tarihi: ${endDate.plusDays(1).format(fmt)}
        
        Gereğini arz ederim.
        
        Personel: ${settings.employeeName}
        Ünvan: ${settings.title}
        Tarih: ${LocalDate.now().format(fmt)}
        """.trimIndent()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dyn.surfaceCard,
        title = {
            Column {
                Text(trEn(isEn, "İzin Talep Dilekçesi", "Annual Leave Request"), color = dyn.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Black)
                Text(
                    trEn(isEn, "4857 SK md. 53 Resmi Yıllık İzin Formu", "Official Paid Annual Leave Request Form"),
                    color = dyn.accentCyan,
                    fontSize = 11.sp
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(dyn.surfacePanel)
                            .border(1.dp, dyn.borderMetal, RoundedCornerShape(10.dp))
                            .clickable { showDatePicker = true }
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(trEn(isEn, "Başlangıç", "Start Date"), fontSize = 9.sp, color = dyn.textMuted)
                            Text(startDate.format(fmt), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = dyn.accentCyan)
                        }
                    }
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(dyn.surfacePanel)
                            .border(1.dp, dyn.borderMetal, RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("-", modifier = Modifier.clickable { if (daysCount > 1) daysCount-- }.padding(6.dp), color = dyn.textPrimary, fontWeight = FontWeight.Bold)
                        Text(trEn(isEn, "$daysCount Gün", "$daysCount Days"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = dyn.textPrimary)
                        Text("+", modifier = Modifier.clickable { if (daysCount < 30) daysCount++ }.padding(6.dp), color = dyn.textPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(dyn.surfaceDim)
                        .border(1.dp, dyn.borderSubtle, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(text = petitionText, fontSize = 11.sp, color = dyn.textSecondary, lineHeight = 16.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onShare(petitionText)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = dyn.primaryNeon),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Filled.Share, contentDescription = null, tint = dyn.textOnPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(trEn(isEn, "Paylaş / Gönder", "Share / Send"), color = dyn.textOnPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(trEn(isEn, "Kapat", "Close"), color = dyn.textSecondary) }
        }
    )

    if (showDatePicker) {
        TitaniumDatePickerDialog(
            title = trEn(isEn, "İzin Başlangıç Tarihi", "Leave Start Date"),
            initialDate = startDate,
            onDismiss = { showDatePicker = false },
            onConfirm = { startDate = it }
        )
    }
}

@Composable
fun PinSecurityDialog(
    enabled: Boolean,
    currentPin: String,
    onDismiss: () -> Unit,
    onSave: (Boolean, String) -> Unit
) {
    val dyn = DynamicTheme.colors
    val isEn = LocalIsEnglish.current
    var isEnabled by remember(enabled) { mutableStateOf(enabled) }
    var pin by remember(currentPin) { mutableStateOf(currentPin) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dyn.surfaceCard,
        title = {
            Text(trEn(isEn, "Biyometrik / PIN Kilidi", "Biometric / PIN Lock"), color = dyn.textPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(trEn(isEn, "Açılış PIN Koruması", "Startup PIN Protection"), color = dyn.textSecondary, fontSize = 13.sp)
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = dyn.primaryNeon)
                    )
                }
                OutlinedTextField(
                    value = pin,
                    onValueChange = { input -> pin = input.filter { it.isDigit() }.take(4) },
                    label = { Text(trEn(isEn, "4 Haneli PIN Kodu", "4-Digit PIN Code"), fontSize = 12.sp, color = dyn.textSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = dyn.primaryNeon,
                        unfocusedBorderColor = dyn.borderMetal,
                        focusedTextColor = dyn.textPrimary,
                        unfocusedTextColor = dyn.textPrimary
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanPin = if (pin.length == 4) pin else "1234"
                    onSave(isEnabled, cleanPin)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = dyn.primaryNeon),
                shape = RoundedCornerShape(10.dp)
            ) { Text(trEn(isEn, "Kaydet", "Save"), color = dyn.textOnPrimary, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(trEn(isEn, "İptal", "Cancel"), color = dyn.textSecondary) }
        }
    )
}
