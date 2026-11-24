@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.engine.ShiftAnchor
import com.example.data.engine.ShiftEngine
import com.example.data.model.CyclePreset
import com.example.data.model.ProfessionGroup
import com.example.data.model.ShiftType
import com.example.data.model.UserSettings
import com.example.ui.i18n.LocalAppLocale
import com.example.ui.i18n.LocalIsEnglish
import com.example.ui.i18n.localizedCode
import com.example.ui.i18n.localizedDepartments
import com.example.ui.i18n.localizedDescription
import com.example.ui.i18n.localizedName
import com.example.ui.i18n.localizedNotice
import com.example.ui.i18n.localizedTitle
import com.example.ui.i18n.localizedTitles
import com.example.ui.i18n.trEn
import com.example.ui.theme.DynamicTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

private enum class OnboardingTimeTarget {
    GUNDUZ_START,
    GUNDUZ_END,
    GECE_START,
    GECE_END,
    SABAH_START,
    SABAH_END,
    AKSAM_START,
    AKSAM_END,
    GECE8_START,
    GECE8_END
}

private enum class PresetFilter(val labelTr: String, val labelEn: String) {
    ALL("Tümü", "All"),
    HOURS_12("12 Saatlik", "12-Hour"),
    HOURS_8("8 Saatlik", "8-Hour"),
    CUSTOM_24("24s & Özel", "24h & Custom")
}

private fun presetMatchesFilter(preset: CyclePreset, filter: PresetFilter): Boolean = when (filter) {
    PresetFilter.ALL -> true
    PresetFilter.HOURS_12 -> preset in setOf(
        CyclePreset.CYCLE_12_36,
        CyclePreset.CYCLE_2_2_2,
        CyclePreset.CYCLE_4_4,
        CyclePreset.CYCLE_PITMAN,
        CyclePreset.CYCLE_12_24_12_48
    )
    PresetFilter.HOURS_8 -> preset in setOf(
        CyclePreset.CYCLE_8_2_2_2,
        CyclePreset.CYCLE_8_2_2_2_2,
        CyclePreset.CYCLE_8_2_2_2_4,
        CyclePreset.CYCLE_SOUTHERN_SWING,
        CyclePreset.CYCLE_8_3
    )
    PresetFilter.CUSTOM_24 -> preset in setOf(
        CyclePreset.CYCLE_24_48,
        CyclePreset.CYCLE_DUPONT_TEMPLATE,
        CyclePreset.CYCLE_FIVE_TEAM_8H,
        CyclePreset.CUSTOM
    )
}

@Composable
fun OnboardingWizardDialog(
    currentSettings: UserSettings,
    onDismiss: () -> Unit,
    onFinish: (UserSettings) -> Unit
) {
    val isEn = LocalIsEnglish.current
    val appLocale = LocalAppLocale.current
    val dyn = DynamicTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var step by remember { mutableIntStateOf(1) }
    var activeFilter by remember { mutableStateOf(PresetFilter.ALL) }

    var chosenPreset by remember { mutableStateOf(currentSettings.cyclePreset) }
    var customSeq by remember {
        mutableStateOf(
            currentSettings.customSequence.ifEmpty {
                listOf(ShiftType.GUNDUZ, ShiftType.GECE, ShiftType.IZIN)
            }
        )
    }
    var refDate by remember { mutableStateOf(currentSettings.referenceStartDate) }
    var initialOffset by remember { mutableIntStateOf(currentSettings.initialCycleOffset) }

    var gunduzStart by remember { mutableStateOf(currentSettings.gunduzStartTime) }
    var gunduzEnd by remember { mutableStateOf(currentSettings.gunduzEndTime) }
    var geceStart by remember { mutableStateOf(currentSettings.geceStartTime) }
    var geceEnd by remember { mutableStateOf(currentSettings.geceEndTime) }
    var sabahStart by remember { mutableStateOf(currentSettings.sabahStartTime) }
    var sabahEnd by remember { mutableStateOf(currentSettings.sabahEndTime) }
    var aksamStart by remember { mutableStateOf(currentSettings.aksamStartTime) }
    var aksamEnd by remember { mutableStateOf(currentSettings.aksamEndTime) }
    var gece8Start by remember { mutableStateOf(currentSettings.gece8StartTime) }
    var gece8End by remember { mutableStateOf(currentSettings.gece8EndTime) }
    val isFreshProfile = !currentSettings.isOnboardingCompleted && currentSettings.employeeName == "Dr. Selin Yılmaz"
    var selectedGroup by remember { mutableStateOf(ProfessionGroup.fromId(currentSettings.professionGroup)) }
    var employeeName by remember { mutableStateOf(if (isFreshProfile) "" else currentSettings.employeeName) }
    var department by remember { mutableStateOf(if (isFreshProfile) "" else currentSettings.department) }

    var activeTimeTarget by remember { mutableStateOf<OnboardingTimeTarget?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showCustomBuilder by remember { mutableStateOf(false) }

    val activeSequence = remember(chosenPreset, customSeq) {
        if (chosenPreset == CyclePreset.CUSTOM || !chosenPreset.hasDefaultPattern) {
            customSeq.ifEmpty { listOf(ShiftType.GUNDUZ, ShiftType.GECE, ShiftType.IZIN) }
        } else {
            chosenPreset.sequence
        }
    }
    val safeOffset = ShiftEngine.safeModulo(initialOffset.toLong(), activeSequence.size)
    val has8hShifts = remember(activeSequence) {
        activeSequence.any { it == ShiftType.SABAH || it == ShiftType.AKSAM || it == ShiftType.GECE8 }
    }
    val isOnly24h = remember(activeSequence) {
        activeSequence.any { it == ShiftType.NOBET24 } &&
            activeSequence.none { it == ShiftType.GUNDUZ || it == ShiftType.GECE || it == ShiftType.SABAH }
    }

    val filteredPresets = remember(activeFilter) {
        CyclePreset.entries.filter { presetMatchesFilter(it, activeFilter) }
    }

    val previewDays = remember(refDate, activeSequence, safeOffset, appLocale) {
        val dayFmt = DateTimeFormatter.ofPattern("EEE", appLocale)
        val anchor = ShiftAnchor(refDate, safeOffset)
        (0 until 7).map { offset ->
            val d = refDate.plusDays(offset.toLong())
            val t = ShiftEngine.stateFor(d, anchor, activeSequence)
            Triple(d.format(dayFmt).uppercase(appLocale).take(3), d.dayOfMonth.toString(), t)
        }
    }

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
                .navigationBarsPadding()
                .padding(horizontal = 18.dp)
                .padding(bottom = 20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = trEn("HIZLI KURULUM SİHİRBAZI", "QUICK SETUP WIZARD"),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = dyn.accentCyan,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = when (step) {
                            1 -> trEn("1. Vardiya Düzenini Seç", "1. Select Shift Rotation")
                            2 -> trEn("2. Başlangıç Günü & Saatler", "2. Start Day & Hours")
                            else -> trEn("3. Önizleme & Onay", "3. Preview & Confirm")
                        },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = dyn.textPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .background(dyn.activeChipBg, RoundedCornerShape(10.dp))
                            .border(1.dp, dyn.accentCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(trEn("Adım $step / 3", "Step $step / 3"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = dyn.accentCyan)
                    }
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(dyn.surfaceElevated)
                            .border(1.dp, dyn.borderSubtle, CircleShape)
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = trEn("Kapat", "Close"), tint = dyn.textSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Interactive Progress Pills
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(
                    1 to trEn("Düzen", "Cycle"),
                    2 to trEn("Tarih & Saat", "Date & Time"),
                    3 to trEn("Onay", "Confirm")
                ).forEach { (s, lbl) ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { step = s }
                    ) {
                        Text(
                            text = "0$s $lbl",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (step >= s) dyn.accentCyan else dyn.textMuted,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(if (step >= s) dyn.primaryNeon else dyn.surfaceElevated)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Scrollable Body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when (step) {
                    1 -> {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 2.dp)
                        ) {
                            items(PresetFilter.entries) { filter ->
                                val isSelected = activeFilter == filter
                                Surface(
                                    modifier = Modifier
                                        .height(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { activeFilter = filter }
                                        .border(
                                            1.dp,
                                            if (isSelected) dyn.accentCyan else dyn.borderSubtle,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .testTag("quick_wizard_filter_${filter.name}"),
                                    color = if (isSelected) dyn.activeChipBg else dyn.surfaceElevated
                                ) {
                                    Box(
                                        modifier = Modifier.padding(horizontal = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isEn) filter.labelEn else filter.labelTr,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) dyn.accentCyan else dyn.textSecondary
                                        )
                                    }
                                }
                            }
                        }

                        filteredPresets.forEach { preset ->
                            val isChosen = chosenPreset == preset
                            val previewSeq = if (preset == CyclePreset.CUSTOM || !preset.hasDefaultPattern) {
                                if (isChosen && customSeq.isNotEmpty()) customSeq else preset.sequence
                            } else {
                                preset.sequence
                            }
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isChosen) dyn.activeChipBg else dyn.surfaceElevated,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(
                                        1.dp,
                                        if (isChosen) dyn.accentCyan else dyn.borderSubtle,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        chosenPreset = preset
                                        initialOffset = 0
                                        if (preset == CyclePreset.CUSTOM || !preset.hasDefaultPattern) {
                                            showCustomBuilder = true
                                        }
                                    }
                                    .testTag("quick_wizard_preset_${preset.id}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = preset.localizedName(isEn),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isChosen) dyn.accentCyan else dyn.textPrimary
                                            )
                                            Text(
                                                text = preset.localizedDescription(isEn),
                                                fontSize = 10.sp,
                                                color = dyn.textSecondary,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        if (preset == CyclePreset.CUSTOM || !preset.hasDefaultPattern) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = dyn.surfaceCard,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .border(1.dp, dyn.accentCyan.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        chosenPreset = preset
                                                        showCustomBuilder = true
                                                    }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(Icons.Filled.Tune, contentDescription = null, tint = dyn.accentCyan, modifier = Modifier.size(13.dp))
                                                    Text(trEn("Düzenle", "Edit"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = dyn.accentCyan)
                                                }
                                            }
                                        } else if (isChosen) {
                                            Icon(Icons.Filled.Check, contentDescription = trEn("Seçili", "Selected"), tint = dyn.accentCyan, modifier = Modifier.size(18.dp))
                                        }
                                    }

                                    if (previewSeq.isNotEmpty()) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            previewSeq.take(8).forEach { st ->
                                                val badgeColor = when {
                                                    st == ShiftType.AKSAM -> dyn.eveningShiftAccent
                                                    st.isNight -> dyn.nightShiftAccent
                                                    st.isWork -> dyn.dayShiftAccent
                                                    else -> dyn.textMuted
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .background(badgeColor.copy(alpha = 0.16f), RoundedCornerShape(5.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = st.localizedCode(isEn),
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = badgeColor
                                                    )
                                                }
                                            }
                                            if (previewSeq.size > 8) {
                                                Text(trEn("+${previewSeq.size - 8}g", "+${previewSeq.size - 8}d"), fontSize = 9.sp, color = dyn.textMuted, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.weight(1f))
                                            Text(trEn("${previewSeq.size} Günlük", "${previewSeq.size}-Day"), fontSize = 9.sp, color = dyn.textMuted)
                                        }
                                    }

                                    val notice = preset.localizedNotice(isEn)
                                    if (isChosen && notice != null) {
                                        Text(
                                            text = "⚠️ $notice",
                                            fontSize = 10.sp,
                                            color = dyn.eveningShiftText
                                        )
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        val today = remember { LocalDate.now() }
                        Text(trEn("Döngü Çapa Tarihi (Tek Dokunuş)", "Cycle Anchor Date (Quick Select)"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = dyn.accentCyan)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val quickDates = listOf(
                                trEn(isEn, "Bugün", "Today") to today,
                                trEn(isEn, "Yarın", "Tomorrow") to today.plusDays(1),
                                trEn(isEn, "Pazartesi", "Monday") to today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
                                trEn(isEn, "Ayın 1'i", "1st of Month") to today.withDayOfMonth(1)
                            )
                            items(quickDates) { (label, d) ->
                                val isSel = refDate == d
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { refDate = d }
                                        .border(1.dp, if (isSel) dyn.accentCyan else dyn.borderSubtle, RoundedCornerShape(8.dp)),
                                    color = if (isSel) dyn.activeChipBg else dyn.surfaceElevated
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) dyn.accentCyan else dyn.textSecondary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                    )
                                }
                            }
                            item {
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { showDatePicker = true }
                                        .border(1.dp, dyn.borderMetal, RoundedCornerShape(8.dp)),
                                    color = dyn.surfacePanel
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Filled.CalendarMonth, contentDescription = trEn("Tarih Seç", "Select Date"), tint = dyn.accentCyan, modifier = Modifier.size(13.dp))
                                        Text(
                                            text = refDate.format(DateTimeFormatter.ofPattern("d MMM", appLocale)),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = dyn.textPrimary
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = trEn(
                                "${refDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", appLocale))} Günü İlk Vardiyanız:",
                                "Your First Shift on ${refDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", appLocale))}:"
                            ),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.textPrimary
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            itemsIndexed(activeSequence) { idx, shiftType ->
                                val isSel = idx == safeOffset
                                val stepAccent = when {
                                    shiftType == ShiftType.AKSAM -> dyn.eveningShiftAccent
                                    shiftType.isNight -> dyn.nightShiftAccent
                                    shiftType.isWork -> dyn.dayShiftAccent
                                    else -> dyn.textSecondary
                                }
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { initialOffset = idx }
                                        .border(1.dp, if (isSel) stepAccent else dyn.borderSubtle, RoundedCornerShape(10.dp))
                                        .testTag("quick_wizard_offset_$idx"),
                                    color = if (isSel) stepAccent.copy(alpha = 0.16f) else dyn.surfaceElevated
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(trEn("${idx + 1}. Gün", "Day ${idx + 1}"), fontSize = 8.sp, color = dyn.textMuted)
                                        Text(
                                            text = "${shiftType.iconEmoji} ${shiftType.localizedTitle(isEn).substringBefore(" (")}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) stepAccent else dyn.textPrimary
                                        )
                                    }
                                }
                            }
                        }

                        // Quick Hour Presets + Pickers
                        Text(trEn("Vardiya Saatleri & Hızlı Şablonlar", "Shift Hours & Quick Presets"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = dyn.accentCyan)
                        if (has8hShifts) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                val presets8 = listOf(
                                    "08-16 / 16-24 / 00-08" to listOf("08:00", "16:00", "16:00", "24:00", "00:00", "08:00"),
                                    "07-15 / 15-23 / 23-07" to listOf("07:00", "15:00", "15:00", "23:00", "23:00", "07:00"),
                                    "06-14 / 14-22 / 22-06" to listOf("06:00", "14:00", "14:00", "22:00", "22:00", "06:00")
                                )
                                items(presets8) { (label, hrs) ->
                                    val isCurrent = sabahStart == hrs[0] && aksamStart == hrs[2] && gece8Start == hrs[4]
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                sabahStart = hrs[0]; sabahEnd = hrs[1]
                                                aksamStart = hrs[2]; aksamEnd = hrs[3]
                                                gece8Start = hrs[4]; gece8End = hrs[5]
                                            }
                                            .border(1.dp, if (isCurrent) dyn.accentCyan else dyn.borderSubtle, RoundedCornerShape(8.dp)),
                                        color = if (isCurrent) dyn.activeChipBg else dyn.surfaceElevated
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCurrent) dyn.accentCyan else dyn.textSecondary,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TimePickerTile(label = trEn("Sabah Baş.", "Morning Start"), time = sabahStart, modifier = Modifier.weight(1f)) { activeTimeTarget = OnboardingTimeTarget.SABAH_START }
                                TimePickerTile(label = trEn("Sabah Bit.", "Morning End"), time = sabahEnd, modifier = Modifier.weight(1f)) { activeTimeTarget = OnboardingTimeTarget.SABAH_END }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TimePickerTile(label = trEn("Akşam Baş.", "Evening Start"), time = aksamStart, accent = dyn.eveningShiftAccent, modifier = Modifier.weight(1f)) { activeTimeTarget = OnboardingTimeTarget.AKSAM_START }
                                TimePickerTile(label = trEn("Akşam Bit.", "Evening End"), time = aksamEnd, accent = dyn.eveningShiftAccent, modifier = Modifier.weight(1f)) { activeTimeTarget = OnboardingTimeTarget.AKSAM_END }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TimePickerTile(label = trEn("Gece (8s) Baş.", "Night (8h) Start"), time = gece8Start, accent = dyn.nightShiftAccent, modifier = Modifier.weight(1f)) { activeTimeTarget = OnboardingTimeTarget.GECE8_START }
                                TimePickerTile(label = trEn("Gece (8s) Bit.", "Night (8h) End"), time = gece8End, accent = dyn.nightShiftAccent, modifier = Modifier.weight(1f)) { activeTimeTarget = OnboardingTimeTarget.GECE8_END }
                            }
                        } else if (isOnly24h) {
                            TimePickerTile(
                                label = trEn("24s Nöbet Başlangıç Saati", "24h Duty Start Time"),
                                time = gunduzStart,
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { activeTimeTarget = OnboardingTimeTarget.GUNDUZ_START }
                            )
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                val presets12 = listOf(
                                    "08-20 / 20-08" to listOf("08:00", "20:00", "20:00", "08:00"),
                                    "07-19 / 19-07" to listOf("07:00", "19:00", "19:00", "07:00"),
                                    "09-21 / 21-09" to listOf("09:00", "21:00", "21:00", "09:00")
                                )
                                items(presets12) { (label, hrs) ->
                                    val isCurrent = gunduzStart == hrs[0] && geceStart == hrs[2]
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                gunduzStart = hrs[0]; gunduzEnd = hrs[1]
                                                geceStart = hrs[2]; geceEnd = hrs[3]
                                            }
                                            .border(1.dp, if (isCurrent) dyn.accentCyan else dyn.borderSubtle, RoundedCornerShape(8.dp)),
                                        color = if (isCurrent) dyn.activeChipBg else dyn.surfaceElevated
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCurrent) dyn.accentCyan else dyn.textSecondary,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TimePickerTile(label = trEn("Gündüz Baş.", "Day Start"), time = gunduzStart, modifier = Modifier.weight(1f)) { activeTimeTarget = OnboardingTimeTarget.GUNDUZ_START }
                                TimePickerTile(label = trEn("Gündüz Bit.", "Day End"), time = gunduzEnd, modifier = Modifier.weight(1f)) { activeTimeTarget = OnboardingTimeTarget.GUNDUZ_END }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TimePickerTile(label = trEn("Gece Baş.", "Night Start"), time = geceStart, accent = dyn.nightShiftAccent, modifier = Modifier.weight(1f)) { activeTimeTarget = OnboardingTimeTarget.GECE_START }
                                TimePickerTile(label = trEn("Gece Bit.", "Night End"), time = geceEnd, accent = dyn.nightShiftAccent, modifier = Modifier.weight(1f)) { activeTimeTarget = OnboardingTimeTarget.GECE_END }
                            }
                        }

                        // Live 7-Day Preview
                        Text(trEn("7 Günlük Canlı Önizleme", "Live 7-Day Preview"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = dyn.textMuted)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            previewDays.forEach { (dName, dNum, st) ->
                                val c = when {
                                    st == ShiftType.AKSAM -> dyn.eveningShiftAccent
                                    st.isNight -> dyn.nightShiftAccent
                                    st.isWork -> dyn.dayShiftAccent
                                    else -> dyn.textMuted
                                }
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(1.dp, dyn.borderSubtle, RoundedCornerShape(8.dp)),
                                    shape = RoundedCornerShape(8.dp),
                                    color = dyn.surfaceElevated
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(dName, fontSize = 8.sp, color = dyn.textMuted)
                                        Text(dNum, fontSize = 11.sp, fontWeight = FontWeight.Black, color = dyn.textPrimary)
                                        Text(st.localizedCode(isEn), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = c)
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        Text(trEn("MESLEK GRUBU", "PROFESSION SECTOR"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = dyn.textMuted)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(ProfessionGroup.entries) { group ->
                                val isSel = selectedGroup == group
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(9.dp))
                                        .clickable {
                                            selectedGroup = group
                                            if (department.isBlank() || ProfessionGroup.entries.any { it.suggestedDepartments.contains(department) || it.localizedDepartments(true).contains(department) }) {
                                                department = group.localizedDepartments(isEn).first()
                                            }
                                        }
                                        .border(1.dp, if (isSel) dyn.accentCyan else dyn.borderSubtle, RoundedCornerShape(9.dp)),
                                    color = if (isSel) dyn.activeChipBg else dyn.surfaceElevated
                                ) {
                                    Text(
                                        text = "${group.emoji} ${group.localizedName(isEn)}",
                                        fontSize = 10.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSel) dyn.accentCyan else dyn.textSecondary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = employeeName,
                            onValueChange = { employeeName = it },
                            label = { Text(trEn("Ad Soyad", "Full Name"), fontSize = 11.sp, color = dyn.textSecondary) },
                            placeholder = { Text(trEn("Örn: Ahmet Yılmaz", "e.g. Jordan Smith"), fontSize = 12.sp, color = dyn.textMuted) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = dyn.accentCyan,
                                unfocusedBorderColor = dyn.borderMetal,
                                focusedTextColor = dyn.textPrimary,
                                unfocusedTextColor = dyn.textPrimary
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = department,
                            onValueChange = { department = it },
                            label = { Text(trEn("Birim / Bölüm", "Department / Unit"), fontSize = 11.sp, color = dyn.textSecondary) },
                            placeholder = { Text(trEn("Örn: ${selectedGroup.localizedDepartments(false).first()}", "e.g. ${selectedGroup.localizedDepartments(true).first()}"), fontSize = 12.sp, color = dyn.textMuted) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = dyn.accentCyan,
                                unfocusedBorderColor = dyn.borderMetal,
                                focusedTextColor = dyn.textPrimary,
                                unfocusedTextColor = dyn.textPrimary
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = dyn.surfaceElevated,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, dyn.dayShiftBorder, RoundedCornerShape(14.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(dyn.activeChipBg, CircleShape)
                                            .border(1.dp, dyn.accentCyan, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.Check, contentDescription = null, tint = dyn.accentCyan, modifier = Modifier.size(16.dp))
                                    }
                                    Text(trEn("Kurulum Özeti Hazır", "Setup Summary Ready"), fontSize = 14.sp, fontWeight = FontWeight.Black, color = dyn.textPrimary)
                                }
                                Text(trEn("Vardiya Sistemi: ${chosenPreset.localizedName(false)}", "Shift System: ${chosenPreset.localizedName(true)}"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = dyn.accentCyan)
                                Text(
                                    trEn(
                                        "Çapa: ${refDate.format(DateTimeFormatter.ofPattern("d MMM yyyy", appLocale))} • ${safeOffset + 1}. Gün (${activeSequence[safeOffset].localizedTitle(false)})",
                                        "Anchor: ${refDate.format(DateTimeFormatter.ofPattern("d MMM yyyy", appLocale))} • Day ${safeOffset + 1} (${activeSequence[safeOffset].localizedTitle(true)})"
                                    ),
                                    fontSize = 11.sp,
                                    color = dyn.textSecondary
                                )
                                Text(
                                    text = when {
                                        has8hShifts -> trEn("Sabah: $sabahStart-$sabahEnd | Akşam: $aksamStart-$aksamEnd | Gece: $gece8Start-$gece8End", "Morning: $sabahStart-$sabahEnd | Evening: $aksamStart-$aksamEnd | Night: $gece8Start-$gece8End")
                                        isOnly24h -> trEn("24s Nöbet Başlangıç: $gunduzStart", "24h Duty Start: $gunduzStart")
                                        else -> trEn("Gündüz: $gunduzStart-$gunduzEnd | Gece: $geceStart-$geceEnd", "Day: $gunduzStart-$gunduzEnd | Night: $geceStart-$geceEnd")
                                    },
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = dyn.textPrimary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer Navigation
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    modifier = Modifier
                        .weight(0.9f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { if (step > 1) step -= 1 else onDismiss() }
                        .border(1.dp, dyn.borderMetal, RoundedCornerShape(12.dp)),
                    color = dyn.surfaceElevated
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = dyn.textSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (step > 1) trEn("Geri", "Back") else trEn("Vazgeç", "Cancel"), fontWeight = FontWeight.Bold, color = dyn.textSecondary, fontSize = 13.sp)
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1.7f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            if (step < 3) {
                                step += 1
                            } else {
                                val resolvedTitle = if (ProfessionGroup.entries.any { it.titles.contains(currentSettings.title) || it.localizedTitles(true).contains(currentSettings.title) }) {
                                    selectedGroup.localizedTitles(isEn).first()
                                } else {
                                    currentSettings.title
                                }
                                onFinish(
                                    currentSettings.copy(
                                        cyclePreset = chosenPreset,
                                        customSequence = customSeq,
                                        referenceStartDate = refDate,
                                        initialCycleOffset = safeOffset,
                                        professionGroup = selectedGroup.id,
                                        title = resolvedTitle,
                                        employeeName = employeeName.trim().ifBlank { resolvedTitle },
                                        department = department.trim().ifBlank { selectedGroup.localizedDepartments(isEn).first() },
                                        gunduzStartTime = gunduzStart,
                                        gunduzEndTime = gunduzEnd,
                                        geceStartTime = geceStart,
                                        geceEndTime = geceEnd,
                                        sabahStartTime = sabahStart,
                                        sabahEndTime = sabahEnd,
                                        aksamStartTime = aksamStart,
                                        aksamEndTime = aksamEnd,
                                        gece8StartTime = gece8Start,
                                        gece8EndTime = gece8End,
                                        isAnchorConfigured = true,
                                        isOnboardingCompleted = true
                                    )
                                )
                                onDismiss()
                            }
                        }
                        .testTag("btn_quick_wizard_next"),
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(Brush.horizontalGradient(listOf(dyn.primaryNeon, dyn.primaryDeep))),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = if (step < 3) trEn("Devam Et", "Continue") else trEn("Kaydet ve Uygula", "Save & Apply"),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        TitaniumDatePickerDialog(
            title = trEn("Döngü Başlangıç Tarihi", "Cycle Start Date"),
            initialDate = refDate,
            onDismiss = { showDatePicker = false },
            onConfirm = { refDate = it }
        )
    }

    if (showCustomBuilder) {
        val isTemplate = chosenPreset == CyclePreset.CYCLE_DUPONT_TEMPLATE ||
            chosenPreset == CyclePreset.CYCLE_FIVE_TEAM_8H
        CustomCycleBuilderSheet(
            initialSequence = if (isTemplate && currentSettings.cyclePreset != chosenPreset) emptyList() else customSeq,
            allowEmptyInitial = isTemplate,
            titleText = if (isTemplate) chosenPreset.localizedName(isEn) else trEn("Özel Vardiya Döngüsü Oluştur", "Create Custom Shift Cycle"),
            onDismiss = { showCustomBuilder = false },
            onSave = { seq ->
                customSeq = seq
                initialOffset = 0
            }
        )
    }

    activeTimeTarget?.let { target ->
        val (pickerTitle, currentVal) = when (target) {
            OnboardingTimeTarget.GUNDUZ_START -> trEn("Gündüz / Nöbet Başlangıç Saati", "Day / Duty Start Time") to gunduzStart
            OnboardingTimeTarget.GUNDUZ_END -> trEn("Gündüz Bitiş Saati", "Day End Time") to gunduzEnd
            OnboardingTimeTarget.GECE_START -> trEn("Gece Başlangıç Saati", "Night Start Time") to geceStart
            OnboardingTimeTarget.GECE_END -> trEn("Gece Bitiş Saati", "Night End Time") to geceEnd
            OnboardingTimeTarget.SABAH_START -> trEn("Sabah Başlangıç Saati", "Morning Start Time") to sabahStart
            OnboardingTimeTarget.SABAH_END -> trEn("Sabah Bitiş Saati", "Morning End Time") to sabahEnd
            OnboardingTimeTarget.AKSAM_START -> trEn("Akşam Başlangıç Saati", "Evening Start Time") to aksamStart
            OnboardingTimeTarget.AKSAM_END -> trEn("Akşam Bitiş Saati", "Evening End Time") to aksamEnd
            OnboardingTimeTarget.GECE8_START -> trEn("Gece (8s) Başlangıç Saati", "Night (8h) Start Time") to gece8Start
            OnboardingTimeTarget.GECE8_END -> trEn("Gece (8s) Bitiş Saati", "Night (8h) End Time") to gece8End
        }
        ShiftTimePickerSheet(
            title = pickerTitle,
            initialTime = currentVal,
            allow24Hour = true,
            onDismiss = { activeTimeTarget = null },
            onConfirm = { newTime ->
                when (target) {
                    OnboardingTimeTarget.GUNDUZ_START -> gunduzStart = newTime
                    OnboardingTimeTarget.GUNDUZ_END -> gunduzEnd = newTime
                    OnboardingTimeTarget.GECE_START -> geceStart = newTime
                    OnboardingTimeTarget.GECE_END -> geceEnd = newTime
                    OnboardingTimeTarget.SABAH_START -> sabahStart = newTime
                    OnboardingTimeTarget.SABAH_END -> sabahEnd = newTime
                    OnboardingTimeTarget.AKSAM_START -> aksamStart = newTime
                    OnboardingTimeTarget.AKSAM_END -> aksamEnd = newTime
                    OnboardingTimeTarget.GECE8_START -> gece8Start = newTime
                    OnboardingTimeTarget.GECE8_END -> gece8End = newTime
                }
            }
        )
    }
}

@Composable
private fun TimePickerTile(
    label: String,
    time: String,
    accent: Color = DynamicTheme.colors.accentCyan,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val dyn = DynamicTheme.colors
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(dyn.surfacePanel)
            .border(1.dp, dyn.borderMetal, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = label, fontSize = 9.sp, color = dyn.textMuted)
            Text(
                text = time,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = accent
            )
        }
        Icon(Icons.Filled.Schedule, contentDescription = label, tint = dyn.textSecondary, modifier = Modifier.size(16.dp))
    }
}
