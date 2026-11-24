@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SignalCellularConnectedNoInternet4Bar
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.engine.ShiftAnchor
import com.example.data.engine.ShiftEngine
import com.example.data.model.CyclePreset
import com.example.data.model.ProfessionGroup
import com.example.data.model.ShiftType
import com.example.ui.components.CustomCycleBuilderSheet
import com.example.ui.components.ShiftTimePickerSheet
import com.example.ui.components.TitaniumDatePickerDialog
import com.example.ui.components.UserAvatarImage
import com.example.ui.theme.BorderSpecularTop
import com.example.ui.theme.DynamicTheme
import com.example.ui.i18n.LocalAppLocale
import com.example.ui.i18n.LocalIsEnglish
import com.example.ui.i18n.localizedCode
import com.example.ui.i18n.localizedDepartments
import com.example.ui.i18n.localizedDescription
import com.example.ui.i18n.localizedName
import com.example.ui.i18n.localizedNotice
import com.example.ui.i18n.localizedReason
import com.example.ui.i18n.localizedTitle
import com.example.ui.i18n.localizedTitles
import com.example.ui.i18n.trEn
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.ShiftViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

private enum class WizardTimeTarget {
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

private enum class WizardPresetFilter {
    ALL,
    HOURS_12,
    HOURS_8,
    CUSTOM_24;

    fun label(isEn: Boolean): String = when (this) {
        ALL -> if (isEn) "All" else "Tümü"
        HOURS_12 -> if (isEn) "12-Hour" else "12 Saatlik"
        HOURS_8 -> if (isEn) "8-Hour" else "8 Saatlik"
        CUSTOM_24 -> if (isEn) "24h & Custom" else "24s & Özel"
    }
}

private fun wizardPresetCategory(preset: CyclePreset): WizardPresetFilter = when (preset) {
    CyclePreset.CYCLE_12_36,
    CyclePreset.CYCLE_2_2_2,
    CyclePreset.CYCLE_4_4,
    CyclePreset.CYCLE_PITMAN,
    CyclePreset.CYCLE_12_24_12_48 -> WizardPresetFilter.HOURS_12

    CyclePreset.CYCLE_8_2_2_2,
    CyclePreset.CYCLE_8_2_2_2_2,
    CyclePreset.CYCLE_8_2_2_2_4,
    CyclePreset.CYCLE_SOUTHERN_SWING,
    CyclePreset.CYCLE_8_3 -> WizardPresetFilter.HOURS_8

    CyclePreset.CYCLE_24_48,
    CyclePreset.CYCLE_DUPONT_TEMPLATE,
    CyclePreset.CYCLE_FIVE_TEAM_8H,
    CyclePreset.CUSTOM -> WizardPresetFilter.CUSTOM_24
}

@Composable
fun WizardScreen(
    viewModel: ShiftViewModel,
    modifier: Modifier = Modifier
) {
    val dyn = DynamicTheme.colors
    val context = LocalContext.current
    val isEn = LocalIsEnglish.current
    val appLocale = LocalAppLocale.current
    val currentSettings by viewModel.userSettings.collectAsState()

    val isFirstTimeSetup = !currentSettings.isOnboardingCompleted
    var wizardStep by remember { mutableIntStateOf(1) } // 1: Profil, 2: Düzen, 3: Başlangıç & Saatler, 4: Önizleme & Onay

    var selectedGroup by remember(currentSettings) { mutableStateOf(ProfessionGroup.fromId(currentSettings.professionGroup)) }
    var employeeName by remember(currentSettings) {
        mutableStateOf(
            if (isFirstTimeSetup && currentSettings.employeeName == "Dr. Selin Yılmaz") "" else currentSettings.employeeName
        )
    }
    var selectedTitle by remember(currentSettings) {
        mutableStateOf(
            if (isFirstTimeSetup && currentSettings.title == "Acil Tıp Uzmanı") {
                ProfessionGroup.fromId(currentSettings.professionGroup).titles.first()
            } else {
                currentSettings.title
            }
        )
    }
    var department by remember(currentSettings) {
        mutableStateOf(
            if (isFirstTimeSetup && currentSettings.department == "İstanbul Acil Servis & 112 KKM") "" else currentSettings.department
        )
    }
    var userManuallyPickedPreset by remember { mutableStateOf(false) }

    var chosenPreset by remember(currentSettings) { mutableStateOf(currentSettings.cyclePreset) }
    var customSeq by remember(currentSettings) { mutableStateOf(currentSettings.customSequence) }
    var startDate by remember(currentSettings) { mutableStateOf(currentSettings.referenceStartDate) }
    var initialOffset by remember(currentSettings) { mutableIntStateOf(currentSettings.initialCycleOffset) }

    var gunduzStart by remember(currentSettings) { mutableStateOf(currentSettings.gunduzStartTime) }
    var gunduzEnd by remember(currentSettings) { mutableStateOf(currentSettings.gunduzEndTime) }
    var geceStart by remember(currentSettings) { mutableStateOf(currentSettings.geceStartTime) }
    var geceEnd by remember(currentSettings) { mutableStateOf(currentSettings.geceEndTime) }
    var sabahStart by remember(currentSettings) { mutableStateOf(currentSettings.sabahStartTime) }
    var sabahEnd by remember(currentSettings) { mutableStateOf(currentSettings.sabahEndTime) }
    var aksamStart by remember(currentSettings) { mutableStateOf(currentSettings.aksamStartTime) }
    var aksamEnd by remember(currentSettings) { mutableStateOf(currentSettings.aksamEndTime) }
    var gece8Start by remember(currentSettings) { mutableStateOf(currentSettings.gece8StartTime) }
    var gece8End by remember(currentSettings) { mutableStateOf(currentSettings.gece8EndTime) }

    var ruleHolidays by remember(currentSettings) { mutableStateOf(currentSettings.autoHolidaysEnabled) }
    var ruleNotifications by remember(currentSettings) { mutableStateOf(currentSettings.shiftReminderEnabled) }
    var ruleOffline by remember { mutableStateOf(true) }

    var activeTimeTarget by remember { mutableStateOf<WizardTimeTarget?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showOffsetDialog by remember { mutableStateOf(false) }
    var showCustomBuilder by remember { mutableStateOf(false) }

    val activeSequence = remember(chosenPreset, customSeq) {
        if (chosenPreset == CyclePreset.CUSTOM || !chosenPreset.hasDefaultPattern) {
            customSeq.ifEmpty { listOf(ShiftType.GUNDUZ, ShiftType.GECE, ShiftType.IZIN) }
        } else {
            chosenPreset.sequence
        }
    }
    val has8hShifts = remember(activeSequence) {
        activeSequence.any { it == ShiftType.SABAH || it == ShiftType.AKSAM || it == ShiftType.GECE8 }
    }
    val isOnly24h = remember(activeSequence) {
        activeSequence.any { it == ShiftType.NOBET24 } &&
            activeSequence.none { it == ShiftType.GUNDUZ || it == ShiftType.GECE || it == ShiftType.SABAH }
    }
    var activeFilter by remember { mutableStateOf(WizardPresetFilter.ALL) }
    val safeOffset = ShiftEngine.safeModulo(initialOffset.toLong(), activeSequence.size)
    val firstShiftType = activeSequence[safeOffset]

    // Live 7-day preview calculation via pure ShiftEngine
    val previewDays = remember(startDate, activeSequence, safeOffset, appLocale) {
        val dayFmt = DateTimeFormatter.ofPattern("EEE", appLocale)
        val anchor = ShiftAnchor(startDate, safeOffset)
        (0 until 7).map { offset ->
            val date = startDate.plusDays(offset.toLong())
            val type = ShiftEngine.stateFor(date, anchor, activeSequence)
            val shortDay = date.format(dayFmt).uppercase(appLocale).take(3)
            Triple(shortDay, date.dayOfMonth.toString(), type)
        }
    }

    // Live 365-day annual projection via pure ShiftEngine
    val yearlyProjection = remember(startDate, activeSequence, safeOffset) {
        var gunduzCount = 0
        var geceCount = 0
        var workCount = 0
        var offCount = 0
        val anchor = ShiftAnchor(startDate, safeOffset)
        for (d in 0 until 365) {
            val type = ShiftEngine.stateFor(startDate.plusDays(d.toLong()), anchor, activeSequence)
            if (type.isWork) {
                workCount++
                if (type.isNight) geceCount++ else gunduzCount++
            } else {
                offCount++
            }
        }
        listOf(workCount, gunduzCount, geceCount, offCount)
    }

    BackHandler {
        if (wizardStep > 1) {
            wizardStep -= 1
        } else {
            viewModel.navigateTo(AppScreen.HOME)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(dyn.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
            .imePadding()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(bottom = 28.dp)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(dyn.surfaceElevated, CircleShape)
                        .border(1.dp, dyn.borderSubtle, CircleShape)
                        .clickable {
                            if (wizardStep > 1) wizardStep -= 1 else viewModel.navigateTo(AppScreen.HOME)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = trEn(isEn, "Geri", "Back"),
                        tint = dyn.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = trEn(isEn, "AKILLI KURULUM", "SMART SETUP"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = dyn.accentCyan,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = when (wizardStep) {
                            1 -> trEn(isEn, "1. Profil & Meslek Bilgileri", "1. Profile & Profession")
                            2 -> trEn(isEn, "2. Vardiya Döngü Düzeni", "2. Shift Cycle Pattern")
                            3 -> trEn(isEn, "3. Başlangıç & Vardiya Saatleri", "3. Anchor Date & Shift Hours")
                            else -> trEn(isEn, "4. Önizleme & Onay", "4. Preview & Confirm")
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = dyn.textPrimary
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(dyn.activeChipBg, RoundedCornerShape(12.dp))
                        .border(1.dp, dyn.dayShiftBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = trEn(isEn, "ADIM $wizardStep/4", "STEP $wizardStep/4"),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = dyn.accentCyan
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(dyn.surfaceElevated, CircleShape)
                        .border(1.dp, dyn.borderSubtle, CircleShape)
                        .clickable {
                            Toast.makeText(
                                context,
                                trEn(
                                    isEn,
                                    "Profilinizi girin; sektörünüze uygun vardiya düzeni otomatik önerilir.",
                                    "Enter your profile; a matching shift pattern for your sector will be recommended."
                                ),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = trEn(isEn, "Yardım", "Help"),
                        tint = dyn.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, dyn.primaryNeon, CircleShape)
                ) {
                    UserAvatarImage(
                        avatarPath = currentSettings.avatarUriOrPath,
                        contentDescription = trEn(isEn, "Profil", "Profile"),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Step Navigation Bar (01 PROFİL | 02 DÜZEN | 03 BAŞLANGIÇ | 04 ONAY)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            WizardStepTab(
                label = trEn(isEn, "01 PROFİL", "01 PROFILE"),
                isActive = wizardStep == 1,
                isCompleted = wizardStep > 1,
                modifier = Modifier.weight(1f),
                onClick = { wizardStep = 1 }
            )
            WizardStepTab(
                label = trEn(isEn, "02 DÜZEN", "02 PATTERN"),
                isActive = wizardStep == 2,
                isCompleted = wizardStep > 2,
                modifier = Modifier.weight(1f),
                onClick = { wizardStep = 2 }
            )
            WizardStepTab(
                label = trEn(isEn, "03 SAAT", "03 HOURS"),
                isActive = wizardStep == 3,
                isCompleted = wizardStep > 3,
                modifier = Modifier.weight(1f),
                onClick = { wizardStep = 3 }
            )
            WizardStepTab(
                label = trEn(isEn, "04 ONAY", "04 REVIEW"),
                isActive = wizardStep == 4,
                isCompleted = false,
                modifier = Modifier.weight(1f),
                onClick = { wizardStep = 4 }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // STEP 1: AKILLI PROFİL OLUŞTURMA
        if (wizardStep == 1) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = trEn(isEn, "Profilini Oluştur", "Create Your Profile"),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = dyn.textPrimary
                )
                Text(
                    text = trEn(
                        isEn,
                        "Bilgilerinizi girin; sektörünüze göre profiliniz ve vardiya öneriniz otomatik hazırlansın.",
                        "Enter your details to auto-tailor your profile and shift recommendation."
                    ),
                    fontSize = 12.sp,
                    color = dyn.textSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = employeeName,
                    onValueChange = { employeeName = it },
                    label = { Text(trEn(isEn, "Ad Soyad", "Full Name"), fontSize = 11.sp, color = dyn.textSecondary) },
                    placeholder = { Text(trEn(isEn, "Örn: Ahmet Yılmaz", "e.g. Alex Carter"), fontSize = 13.sp, color = dyn.textMuted) },
                    leadingIcon = {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = dyn.accentCyan, modifier = Modifier.size(18.dp))
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = dyn.accentCyan,
                        unfocusedBorderColor = dyn.borderMetal,
                        focusedTextColor = dyn.textPrimary,
                        unfocusedTextColor = dyn.textPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wizard_input_name"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = trEn(isEn, "SEKTÖR / MESLEK GRUBU", "SECTOR / PROFESSION"),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = dyn.textMuted,
                    letterSpacing = 0.6.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                ProfessionGroup.entries.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pair.forEach { group ->
                            val isSel = selectedGroup == group
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        val oldRecommended = selectedGroup.recommendedPreset
                                        val wasKnownPresetDept = ProfessionGroup.entries.any { g ->
                                            g.suggestedDepartments.contains(department) || g.localizedDepartments(true).contains(department)
                                        }
                                        val wasPresetTitle = selectedTitle.isBlank() || ProfessionGroup.entries.any { g ->
                                            g.titles.contains(selectedTitle) || g.localizedTitles(true).contains(selectedTitle)
                                        }
                                        selectedGroup = group
                                        if (wasPresetTitle) {
                                            selectedTitle = group.localizedTitles(isEn).first()
                                        }
                                        if (department.isBlank() || wasKnownPresetDept) {
                                            department = group.localizedDepartments(isEn).first()
                                        }
                                        if (!userManuallyPickedPreset || chosenPreset == oldRecommended) {
                                            chosenPreset = group.recommendedPreset
                                            initialOffset = 0
                                        }
                                    }
                                    .border(1.dp, if (isSel) dyn.accentCyan else dyn.borderSubtle, RoundedCornerShape(12.dp))
                                    .testTag("wizard_sector_${group.id}"),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSel) dyn.activeChipBg else dyn.surfaceCard
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(text = group.emoji, fontSize = 16.sp)
                                    Text(
                                        text = group.localizedName(isEn),
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSel) dyn.accentCyan else dyn.textPrimary,
                                        maxLines = 2,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = trEn(isEn, "ÜNVAN / GÖREV", "TITLE / ROLE"),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = dyn.textMuted,
                    letterSpacing = 0.6.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                val localizedWizardTitles = remember(selectedGroup, isEn) { selectedGroup.localizedTitles(isEn) }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    itemsIndexed(localizedWizardTitles) { idx, roleTitle ->
                        val rawTitle = selectedGroup.titles.getOrNull(idx)
                        val isSel = selectedTitle.equals(roleTitle, ignoreCase = true) ||
                            (rawTitle != null && selectedTitle.equals(rawTitle, ignoreCase = true))
                        Surface(
                            modifier = Modifier
                                .height(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedTitle = roleTitle }
                                .border(1.dp, if (isSel) dyn.primaryNeon else dyn.borderSubtle, RoundedCornerShape(10.dp)),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) dyn.activeChipBg else dyn.surfaceElevated
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSel) Icons.Filled.Check else Icons.Filled.Badge,
                                    contentDescription = null,
                                    tint = if (isSel) dyn.accentCyan else dyn.textMuted,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = roleTitle,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) dyn.accentCyan else dyn.textSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val localizedWizardDepts = remember(selectedGroup, isEn) { selectedGroup.localizedDepartments(isEn) }
                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text(trEn(isEn, "Birim / Kurum", "Unit / Organization"), fontSize = 11.sp, color = dyn.textSecondary) },
                    placeholder = {
                        Text(
                            trEn(isEn, "Örn: ${localizedWizardDepts.first()}", "e.g. ${localizedWizardDepts.first()}"),
                            fontSize = 13.sp,
                            color = dyn.textMuted
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Filled.WorkOutline, contentDescription = null, tint = dyn.accentCyan, modifier = Modifier.size(18.dp))
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = dyn.accentCyan,
                        unfocusedBorderColor = dyn.borderMetal,
                        focusedTextColor = dyn.textPrimary,
                        unfocusedTextColor = dyn.textPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wizard_input_department"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(localizedWizardDepts) { deptPreset ->
                        val isSel = department.equals(deptPreset, ignoreCase = true)
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { department = deptPreset }
                                .border(1.dp, if (isSel) dyn.accentCyan else dyn.borderSubtle, RoundedCornerShape(8.dp)),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) dyn.activeChipBg else dyn.surfacePanel
                        ) {
                            Text(
                                text = deptPreset,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSel) dyn.accentCyan else dyn.textSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Smart Recommendation Preview Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, dyn.accentCyan.copy(alpha = 0.45f), RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    color = dyn.activeChipBg
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = dyn.accentCyan, modifier = Modifier.size(15.dp))
                                Text(
                                    text = trEn(isEn, "AKILLI VARDİYA ÖNERİSİ", "SMART SHIFT RECOMMENDATION"),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = dyn.accentCyan,
                                    letterSpacing = 0.6.sp
                                )
                            }
                            Text(
                                text = trEn(isEn, "Hızlı Başlat →", "Quick Start →"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = dyn.eveningShiftAccent,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        chosenPreset = selectedGroup.recommendedPreset
                                        initialOffset = 0
                                        wizardStep = 3
                                    }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                    .testTag("btn_wizard_smart_express")
                            )
                        }
                        Text(
                            text = selectedGroup.recommendedPreset.localizedName(isEn),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = dyn.textPrimary
                        )
                        Text(
                            text = selectedGroup.localizedReason(isEn),
                            fontSize = 11.sp,
                            color = dyn.textSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { wizardStep = 2 }
                        .shadow(12.dp, RoundedCornerShape(14.dp), ambientColor = dyn.primaryNeon)
                        .border(1.dp, BorderSpecularTop, RoundedCornerShape(14.dp))
                        .testTag("btn_wizard_step1_next"),
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
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(trEn(isEn, "Devam Et — Vardiya Düzeni", "Continue — Shift Pattern"), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // STEP 2: DÜZEN SEÇİMİ
        if (wizardStep == 2) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = trEn(isEn, "Çalışma Düzenini Seç", "Choose Shift Pattern"),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = dyn.textPrimary
                )
                Text(
                    text = trEn(
                        isEn,
                        "${selectedGroup.localizedName(false)} için önerilen düzen hazır; isterseniz farklı bir döngü seçebilirsiniz.",
                        "Recommended pattern for ${selectedGroup.localizedName(true)} is ready; or pick any cycle below."
                    ),
                    fontSize = 12.sp,
                    color = dyn.textSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(WizardPresetFilter.entries) { filter ->
                        val isSel = activeFilter == filter
                        Surface(
                            modifier = Modifier
                                .height(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { activeFilter = filter }
                                .border(
                                    1.dp,
                                    if (isSel) dyn.accentCyan else dyn.borderSubtle,
                                    RoundedCornerShape(10.dp)
                                )
                                .testTag("wizard_filter_${filter.name}"),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) dyn.activeChipBg else dyn.surfaceElevated
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = filter.label(isEn),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) dyn.accentCyan else dyn.textSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val allWizardPresets = CyclePreset.entries.map { preset ->
                    Triple(preset, preset.localizedName(isEn), preset.localizedDescription(isEn))
                }.filter { (preset, _, _) ->
                    activeFilter == WizardPresetFilter.ALL || wizardPresetCategory(preset) == activeFilter
                }.sortedByDescending { (preset, _, _) ->
                    preset == selectedGroup.recommendedPreset
                }

                allWizardPresets.forEach { (preset, title, desc) ->
                    val isSelected = chosenPreset == preset
                    val isRecommended = preset == selectedGroup.recommendedPreset
                    val seqPreview = if (preset == CyclePreset.CUSTOM || !preset.hasDefaultPattern) {
                        if (isSelected && customSeq.isNotEmpty()) customSeq else preset.sequence
                    } else {
                        preset.sequence
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                chosenPreset = preset
                                userManuallyPickedPreset = true
                                initialOffset = 0
                                if (preset == CyclePreset.CUSTOM || !preset.hasDefaultPattern) {
                                    showCustomBuilder = true
                                }
                            }
                            .border(
                                1.dp,
                                if (isSelected) dyn.accentCyan else dyn.borderSubtle,
                                RoundedCornerShape(14.dp)
                            )
                            .testTag("wizard_preset_${preset.id}"),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) dyn.activeChipBg else dyn.surfaceCard
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) dyn.accentCyan else dyn.textPrimary
                                        )
                                        if (isRecommended) {
                                            Box(
                                                modifier = Modifier
                                                    .background(dyn.accentCyan.copy(alpha = 0.16f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                                            ) {
                                                Text(trEn(isEn, "ÖNERİLEN", "RECOMMENDED"), fontSize = 8.sp, fontWeight = FontWeight.Black, color = dyn.accentCyan)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if ((preset == CyclePreset.CUSTOM || !preset.hasDefaultPattern) && isSelected && customSeq.isNotEmpty()) {
                                            customSeq.joinToString(" → ") { it.localizedTitle(isEn).substringBefore(" (") }
                                        } else desc,
                                        fontSize = 11.sp,
                                        color = dyn.textSecondary,
                                        lineHeight = 14.sp
                                    )
                                }
                                if (preset == CyclePreset.CUSTOM || !preset.hasDefaultPattern) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = dyn.activeChipBg,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                chosenPreset = preset
                                                showCustomBuilder = true
                                            }
                                            .border(1.dp, dyn.accentCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Filled.Tune, contentDescription = null, tint = dyn.accentCyan, modifier = Modifier.size(13.dp))
                                            Text(trEn(isEn, "Düzenle", "Edit"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = dyn.accentCyan)
                                        }
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .border(1.5.dp, if (isSelected) dyn.accentCyan else dyn.textMuted, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Box(modifier = Modifier.size(10.dp).background(dyn.accentCyan, CircleShape))
                                        }
                                    }
                                }
                            }

                            if (seqPreview.isNotEmpty()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    seqPreview.take(8).forEach { st ->
                                        val c = when {
                                            st == ShiftType.AKSAM -> dyn.eveningShiftAccent
                                            st.isNight -> dyn.nightShiftAccent
                                            st.isWork -> dyn.dayShiftAccent
                                            else -> dyn.textMuted
                                        }
                                        Box(
                                            modifier = Modifier
                                                .background(c.copy(alpha = 0.16f), RoundedCornerShape(5.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(st.localizedCode(isEn), fontSize = 9.sp, fontWeight = FontWeight.Black, color = c)
                                        }
                                    }
                                    if (seqPreview.size > 8) {
                                        Text("+${seqPreview.size - 8}${trEn(isEn, "g", "d")}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = dyn.textMuted)
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(trEn(isEn, "${seqPreview.size} Gün", "${seqPreview.size} Days"), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = dyn.textMuted)
                                }
                            }

                            val localizedNotice = preset.localizedNotice(isEn)
                            if (isSelected && localizedNotice != null) {
                                Text(
                                    text = "⚠️ $localizedNotice",
                                    fontSize = 10.sp,
                                    color = dyn.eveningShiftText
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        modifier = Modifier
                            .weight(0.8f)
                            .height(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { wizardStep = 1 }
                            .border(1.dp, dyn.borderMetal, RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        color = dyn.surfaceElevated
                    ) {
                        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = trEn(isEn, "Geri", "Back"), tint = dyn.textSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(trEn(isEn, "Geri", "Back"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = dyn.textPrimary)
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(2f)
                            .height(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { wizardStep = 3 }
                            .shadow(12.dp, RoundedCornerShape(14.dp), ambientColor = dyn.primaryNeon)
                            .border(1.dp, BorderSpecularTop, RoundedCornerShape(14.dp))
                            .testTag("btn_wizard_step2_next"),
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
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(trEn(isEn, "Devam Et — Başlangıç & Saatler", "Continue — Anchor & Hours"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        // STEP 3: BAŞLANGIÇ TARİHİ & VARDİYA SAATLERİ
        if (wizardStep == 3) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = trEn(isEn, "Başlangıç & Saat Ayarları", "Start Date & Shift Hours"),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = dyn.textPrimary
                )
                Text(
                    text = trEn(
                        isEn,
                        "Döngünüzün başlangıç tarihini, ilk nöbet adımını ve çalışma saatlerinizi belirleyin.",
                        "Set your cycle anchor date, starting shift step, and working hours."
                    ),
                    fontSize = 12.sp,
                    color = dyn.textSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Card: Döngü Başlangıcı & İlk Vardiya (Tek Dokunuş Pratik Seçim)
                val today = remember { LocalDate.now() }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = dyn.surfaceCard
                ) {
                    Column(
                        modifier = Modifier
                            .border(1.dp, dyn.borderMetal, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = trEn(isEn, "Çapa Tarihi & İlk Vardiya Adımı", "Anchor Date & First Shift Step"),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = dyn.textPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .background(dyn.accentCyan.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                    .border(1.dp, dyn.accentCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(trEn(isEn, "TEK DOKUNUŞ", "1-TAP"), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = dyn.accentCyan)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 1-Tap Date Shortcut Chips
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val datePresets = listOf(
                                trEn(isEn, "Bugün", "Today") to today,
                                trEn(isEn, "Yarın", "Tomorrow") to today.plusDays(1),
                                trEn(isEn, "Pazartesi", "Monday") to today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
                                trEn(isEn, "Ayın 1'i", "1st of Month") to today.withDayOfMonth(1)
                            )
                            items(datePresets) { (lbl, dateVal) ->
                                val isSel = startDate == dateVal
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { startDate = dateVal }
                                        .border(1.dp, if (isSel) dyn.accentCyan else dyn.borderSubtle, RoundedCornerShape(8.dp)),
                                    color = if (isSel) dyn.activeChipBg else dyn.surfaceElevated
                                ) {
                                    Text(
                                        text = lbl,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) dyn.accentCyan else dyn.textSecondary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Başlangıç Tarihi (Interactive Date Picker)
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { showDatePicker = true }
                                    .border(1.dp, dyn.borderMetal, RoundedCornerShape(10.dp))
                                    .testTag("btn_wizard_anchor_date"),
                                shape = RoundedCornerShape(10.dp),
                                color = dyn.surfaceElevated
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(trEn(isEn, "BAŞLANGIÇ TARİHİ", "START DATE"), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = dyn.textMuted)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = startDate.format(DateTimeFormatter.ofPattern("d MMM yyyy", appLocale)),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = dyn.textPrimary
                                        )
                                        Icon(Icons.Filled.CalendarMonth, contentDescription = trEn(isEn, "Tarih Seç", "Pick Date"), tint = dyn.accentCyan, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            // O Günkü Görev (Interactive Cycle Step Selector Dialog button)
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { showOffsetDialog = true }
                                    .border(1.dp, dyn.borderMetal, RoundedCornerShape(10.dp))
                                    .testTag("btn_wizard_first_duty"),
                                shape = RoundedCornerShape(10.dp),
                                color = dyn.surfaceElevated
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(trEn(isEn, "O GÜNKÜ GÖREV", "DUTY ON THAT DAY"), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = dyn.textMuted)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${safeOffset + 1}. ${firstShiftType.localizedTitle(isEn)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = dyn.accentCyan
                                        )
                                        Icon(Icons.Filled.UnfoldMore, contentDescription = trEn(isEn, "Görev Seç", "Select Shift"), tint = dyn.textSecondary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = trEn(
                                isEn,
                                "${startDate.format(DateTimeFormatter.ofPattern("d MMM", appLocale))} günü döngünün kaçıncı adımındasınız?",
                                "Which cycle step are you on ${startDate.format(DateTimeFormatter.ofPattern("MMM d", appLocale))}?"
                            ),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = dyn.textSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Inline Horizontal Cycle Step Cards (Direct tap, no popup required)
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
                                        .clip(RoundedCornerShape(9.dp))
                                        .clickable { initialOffset = idx }
                                        .border(1.dp, if (isSel) stepAccent else dyn.borderSubtle, RoundedCornerShape(9.dp))
                                        .testTag("wizard_inline_offset_$idx"),
                                    color = if (isSel) stepAccent.copy(alpha = 0.2f) else dyn.surfacePanel
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(trEn(isEn, "${idx + 1}. Gün", "Day ${idx + 1}"), fontSize = 8.sp, color = dyn.textMuted)
                                        Text(
                                            text = "${shiftType.iconEmoji} ${shiftType.localizedTitle(isEn).substringBefore(" (")}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) stepAccent else dyn.textPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Card: Vardiya Saatleri (1-Tap Quick Presets + Interactive Time Pickers)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = dyn.surfaceCard
                ) {
                    Column(
                        modifier = Modifier
                            .border(1.dp, dyn.borderMetal, RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Filled.Schedule, contentDescription = null, tint = dyn.accentCyan, modifier = Modifier.size(16.dp))
                                Text(trEn(isEn, "Vardiya Saat Dilimleri", "Shift Time Windows"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = dyn.textPrimary)
                            }
                            Text(trEn(isEn, "Hazır & Çark", "Preset & Picker"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = dyn.accentCyan)
                        }

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

                            Text(trEn(isEn, "SABAH VARDİYASI (8s)", "MORNING SHIFT (8h)"), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = dyn.dayShiftAccent)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                WizardTimeChip(
                                    label = trEn(isEn, "Sabah Başlangıç", "Morning Start"),
                                    time = sabahStart,
                                    accentColor = dyn.dayShiftAccent,
                                    testTag = "wizard_sabah_start",
                                    modifier = Modifier.weight(1f),
                                    onClick = { activeTimeTarget = WizardTimeTarget.SABAH_START }
                                )
                                WizardTimeChip(
                                    label = trEn(isEn, "Sabah Bitiş", "Morning End"),
                                    time = sabahEnd,
                                    accentColor = dyn.dayShiftAccent,
                                    testTag = "wizard_sabah_end",
                                    modifier = Modifier.weight(1f),
                                    onClick = { activeTimeTarget = WizardTimeTarget.SABAH_END }
                                )
                            }

                            Text(trEn(isEn, "AKŞAM VARDİYASI (8s)", "EVENING SHIFT (8h)"), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = dyn.eveningShiftAccent)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                WizardTimeChip(
                                    label = trEn(isEn, "Akşam Başlangıç", "Evening Start"),
                                    time = aksamStart,
                                    accentColor = dyn.eveningShiftAccent,
                                    testTag = "wizard_aksam_start",
                                    modifier = Modifier.weight(1f),
                                    onClick = { activeTimeTarget = WizardTimeTarget.AKSAM_START }
                                )
                                WizardTimeChip(
                                    label = trEn(isEn, "Akşam Bitiş", "Evening End"),
                                    time = aksamEnd,
                                    accentColor = dyn.eveningShiftAccent,
                                    testTag = "wizard_aksam_end",
                                    modifier = Modifier.weight(1f),
                                    onClick = { activeTimeTarget = WizardTimeTarget.AKSAM_END }
                                )
                            }

                            Text(trEn(isEn, "GECE VARDİYASI (8s)", "NIGHT SHIFT (8h)"), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = dyn.nightShiftAccent)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                WizardTimeChip(
                                    label = trEn(isEn, "Gece (8s) Başlangıç", "Night (8h) Start"),
                                    time = gece8Start,
                                    accentColor = dyn.nightShiftAccent,
                                    testTag = "wizard_gece8_start",
                                    modifier = Modifier.weight(1f),
                                    onClick = { activeTimeTarget = WizardTimeTarget.GECE8_START }
                                )
                                WizardTimeChip(
                                    label = trEn(isEn, "Gece (8s) Bitiş", "Night (8h) End"),
                                    time = gece8End,
                                    accentColor = dyn.nightShiftAccent,
                                    testTag = "wizard_gece8_end",
                                    modifier = Modifier.weight(1f),
                                    onClick = { activeTimeTarget = WizardTimeTarget.GECE8_END }
                                )
                            }
                        } else if (isOnly24h) {
                            WizardTimeChip(
                                label = trEn(isEn, "24s Nöbet Teslim / Başlangıç Saati", "24h Shift Handover / Start Time"),
                                time = gunduzStart,
                                accentColor = dyn.accentCyan,
                                testTag = "wizard_nobet24_start",
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { activeTimeTarget = WizardTimeTarget.GUNDUZ_START }
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

                            // Gündüz row
                            Text(trEn(isEn, "GÜNDÜZ VARDİYASI", "DAY SHIFT"), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = dyn.dayShiftAccent)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                WizardTimeChip(
                                    label = trEn(isEn, "Gündüz Başlangıç", "Day Start"),
                                    time = gunduzStart,
                                    accentColor = dyn.dayShiftAccent,
                                    testTag = "wizard_gunduz_start",
                                    modifier = Modifier.weight(1f),
                                    onClick = { activeTimeTarget = WizardTimeTarget.GUNDUZ_START }
                                )
                                WizardTimeChip(
                                    label = trEn(isEn, "Gündüz Bitiş", "Day End"),
                                    time = gunduzEnd,
                                    accentColor = dyn.dayShiftAccent,
                                    testTag = "wizard_gunduz_end",
                                    modifier = Modifier.weight(1f),
                                    onClick = { activeTimeTarget = WizardTimeTarget.GUNDUZ_END }
                                )
                            }

                            // Gece row
                            Text(trEn(isEn, "GECE VARDİYASI (12 SAAT)", "NIGHT SHIFT (12 HOUR)"), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = dyn.nightShiftAccent)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                WizardTimeChip(
                                    label = trEn(isEn, "Gece Başlangıç", "Night Start"),
                                    time = geceStart,
                                    accentColor = dyn.nightShiftAccent,
                                    testTag = "wizard_gece_start",
                                    modifier = Modifier.weight(1f),
                                    onClick = { activeTimeTarget = WizardTimeTarget.GECE_START }
                                )
                                WizardTimeChip(
                                    label = trEn(isEn, "Gece Bitiş", "Night End"),
                                    time = geceEnd,
                                    accentColor = dyn.nightShiftAccent,
                                    testTag = "wizard_gece_end",
                                    modifier = Modifier.weight(1f),
                                    onClick = { activeTimeTarget = WizardTimeTarget.GECE_END }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Dynamic 7-Day Live Preview Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(trEn(isEn, "7 GÜNLÜK CANLI ÖNİZLEME", "7-DAY LIVE PREVIEW"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = dyn.textMuted)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.Shield, contentDescription = null, tint = dyn.accentCyan, modifier = Modifier.size(12.dp))
                        Text(trEn(isEn, "DÖNGÜ SİMÜLATÖRÜ", "CYCLE SIMULATOR"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = dyn.accentCyan)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    previewDays.forEach { (dayName, dateNum, type) ->
                        val color = when {
                            type == ShiftType.AKSAM -> dyn.eveningShiftAccent
                            type.isNight -> dyn.nightShiftAccent
                            type.isWork -> dyn.dayShiftAccent
                            else -> dyn.offShiftAccent
                        }
                        val previewHoursLabel = when (type) {
                            ShiftType.SABAH -> "${sabahStart.take(2)}-${sabahEnd.take(2)}"
                            ShiftType.AKSAM -> "${aksamStart.take(2)}-${aksamEnd.take(2)}"
                            ShiftType.GECE8 -> "${gece8Start.take(2)}-${gece8End.take(2)}"
                            else -> type.localizedCode(isEn)
                        }
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, dyn.borderSubtle, RoundedCornerShape(8.dp)),
                            shape = RoundedCornerShape(8.dp),
                            color = dyn.surfaceCard
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = dayName, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = dyn.textMuted)
                                Text(text = dateNum, fontSize = 12.sp, fontWeight = FontWeight.Black, color = dyn.textPrimary)
                                Spacer(modifier = Modifier.height(3.dp))
                                Box(
                                    modifier = Modifier
                                        .background(color.copy(alpha = 0.2f), RoundedCornerShape(3.dp))
                                        .padding(horizontal = 3.dp, vertical = 1.dp)
                                ) {
                                    Text(text = previewHoursLabel, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = color, maxLines = 1)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        modifier = Modifier
                            .weight(0.8f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { wizardStep = 2 }
                            .border(1.dp, dyn.borderMetal, RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        color = dyn.surfaceElevated
                    ) {
                        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = trEn(isEn, "Geri", "Back"), tint = dyn.textSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(trEn(isEn, "Geri", "Back"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = dyn.textPrimary)
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(2f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { wizardStep = 4 }
                            .border(1.dp, BorderSpecularTop, RoundedCornerShape(14.dp))
                            .testTag("btn_wizard_next"),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.Transparent
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.horizontalGradient(listOf(dyn.primaryNeon, dyn.primaryDeep))),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(trEn(isEn, "Devam Et — Önizleme", "Continue — Preview"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // STEP 4: ÖNİZLEME & ONAY
        if (wizardStep == 4) {
            val totalWorkDays = yearlyProjection[0]
            val gunduzCount = yearlyProjection[1]
            val geceCount = yearlyProjection[2]
            val offDaysCount = yearlyProjection[3]
            val previewTitle = selectedTitle.ifBlank { selectedGroup.localizedTitles(isEn).first() }
            val previewName = employeeName.trim().ifBlank { previewTitle }
            val previewDept = department.trim().ifBlank { selectedGroup.localizedDepartments(isEn).first() }

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                // Hero Plate: Döngünüz Hazır!
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = dyn.surfaceCard
                ) {
                    Column(
                        modifier = Modifier
                            .border(1.dp, dyn.dayShiftBorder, RoundedCornerShape(18.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .background(dyn.accentCyan.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(trEn(isEn, "MİLİMETRİK PLANLAMA", "PRECISION PLANNING"), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = dyn.accentCyan)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(dyn.eveningShiftAccent.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(trEn(isEn, "OTOMATİK", "AUTOMATIC"), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = dyn.eveningShiftAccent)
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(trEn(isEn, "Döngünüz Hazır!", "Your Cycle is Ready!"), fontSize = 22.sp, fontWeight = FontWeight.Black, color = dyn.textPrimary)
                            }
                            Surface(
                                modifier = Modifier.size(38.dp).border(1.dp, dyn.dayShiftBorder, RoundedCornerShape(10.dp)),
                                shape = RoundedCornerShape(10.dp),
                                color = dyn.activeChipBg
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = dyn.accentCyan, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = trEn(
                                isEn,
                                "Seçtiğiniz parametrelerle 365 günlük tüm yıllık takviminiz milisaniyeler içinde hatasız oluşturulacak.",
                                "Your full 365-day annual schedule will be generated instantly with your chosen parameters."
                            ),
                            fontSize = 11.sp,
                            color = dyn.textSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Auto-Created Profile Summary Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = dyn.surfaceCard
                ) {
                    Row(
                        modifier = Modifier
                            .border(1.dp, dyn.borderMetal, RoundedCornerShape(16.dp))
                            .padding(14.dp),
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
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(dyn.activeChipBg)
                                    .border(1.dp, dyn.accentCyan, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(selectedGroup.emoji, fontSize = 18.sp)
                            }
                            Column {
                                Text(previewName, fontSize = 14.sp, fontWeight = FontWeight.Black, color = dyn.textPrimary)
                                Text("$previewTitle • $previewDept", fontSize = 11.sp, color = dyn.textSecondary)
                            }
                        }
                        Text(
                            text = trEn(isEn, "DÜZENLE", "EDIT"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.accentCyan,
                            modifier = Modifier
                                .clickable { wizardStep = 1 }
                                .padding(horizontal = 4.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Summary card of chosen config
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = dyn.surfaceCard
                ) {
                    Column(
                        modifier = Modifier
                            .border(1.dp, dyn.borderMetal, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Filled.Tune, contentDescription = null, tint = dyn.accentCyan, modifier = Modifier.size(14.dp))
                                Text(trEn(isEn, "SEÇİLEN VARDİYA DÜZENİ", "SELECTED SHIFT PATTERN"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = dyn.textMuted)
                            }
                            Text(
                                text = trEn(isEn, "DÜZENLE", "EDIT"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = dyn.accentCyan,
                                modifier = Modifier.clickable { wizardStep = 3 }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = dyn.activeChipBg
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.Shield, contentDescription = null, tint = dyn.accentCyan, modifier = Modifier.size(16.dp))
                                Text(chosenPreset.localizedName(isEn), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = dyn.textPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(trEn(isEn, "Başlangıç Tarihi", "Start Date"), fontSize = 11.sp, color = dyn.textSecondary)
                            Text(
                                startDate.format(DateTimeFormatter.ofPattern("d MMM yyyy (EEE)", appLocale)),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = dyn.textPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(modifier = Modifier.fillMaxWidth().clickable { showOffsetDialog = true }, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(trEn(isEn, "İlk Görev Adımı", "First Shift Step"), fontSize = 11.sp, color = dyn.textSecondary)
                            Text("${safeOffset + 1}. ${firstShiftType.localizedTitle(isEn)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = dyn.accentCyan)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(modifier = Modifier.clickable { wizardStep = 3 }) {
                            Text(trEn(isEn, "Vardiya Saat Dilimleri (Dokun & Değiştir)", "Shift Time Windows (Tap to Edit)"), fontSize = 11.sp, color = dyn.textSecondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (has8hShifts) {
                                    trEn(
                                        isEn,
                                        "Sabah $sabahStart-$sabahEnd | Akşam $aksamStart-$aksamEnd | Gece $gece8Start-$gece8End",
                                        "Morning $sabahStart-$sabahEnd | Evening $aksamStart-$aksamEnd | Night $gece8Start-$gece8End"
                                    )
                                } else {
                                    trEn(
                                        isEn,
                                        "$gunduzStart - $gunduzEnd (Gündüz)  |  $geceStart - $geceEnd (Gece)",
                                        "$gunduzStart - $gunduzEnd (Day)  |  $geceStart - $geceEnd (Night)"
                                    )
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = dyn.accentCyan
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Dynamic 365-Day Yearly Projection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(trEn(isEn, "YILLIK DÖNGÜ PROJEKSİYONU", "ANNUAL CYCLE PROJECTION"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = dyn.textMuted)
                    Text(trEn(isEn, "365 GÜN HESAPLANDI", "365 DAYS COMPUTED"), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = dyn.accentCyan)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        modifier = Modifier.weight(1f).border(1.dp, dyn.borderSubtle, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        color = dyn.surfaceCard
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(trEn(isEn, "YILLIK NÖBET", "ANNUAL SHIFTS"), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = dyn.textMuted)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(trEn(isEn, "$totalWorkDays Nöbet", "$totalWorkDays Shifts"), fontSize = 18.sp, fontWeight = FontWeight.Black, color = dyn.textPrimary)
                            Text(trEn(isEn, "$gunduzCount Gündüz • $geceCount Gece", "$gunduzCount Day • $geceCount Night"), fontSize = 10.sp, color = dyn.textSecondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                val gRatio = (gunduzCount.toFloat() / totalWorkDays.coerceAtLeast(1)).coerceIn(0.1f, 0.9f)
                                Box(modifier = Modifier.weight(gRatio).height(4.dp).background(dyn.dayShiftAccent, RoundedCornerShape(2.dp)))
                                Spacer(modifier = Modifier.width(2.dp))
                                Box(modifier = Modifier.weight(1f - gRatio).height(4.dp).background(dyn.nightShiftAccent, RoundedCornerShape(2.dp)))
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f).border(1.dp, dyn.borderSubtle, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        color = dyn.surfaceCard
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(trEn(isEn, "İZİN & İSTİRAHAT", "OFF & REST DAYS"), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = dyn.textMuted)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(trEn(isEn, "$offDaysCount Gün", "$offDaysCount Days"), fontSize = 18.sp, fontWeight = FontWeight.Black, color = dyn.accentCyan)
                            Text(trEn(isEn, "Kesintisiz Dinlenme", "Full Rest Days"), fontSize = 10.sp, color = dyn.textSecondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { offDaysCount / 365f },
                                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                color = dyn.accentCyan,
                                trackColor = dyn.surfacePanel
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Smart Rules
                Text(trEn(isEn, "ÖN YÜKLÜ AKILLI KURALLAR", "PRELOADED SMART RULES"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = dyn.textMuted)
                Spacer(modifier = Modifier.height(8.dp))

                RuleCheckTile(
                    title = trEn(isEn, "Resmi Tatiller Otomatik İşaretlensin", "Auto-Mark Public Holidays"),
                    subtitle = trEn(isEn, "Ulusal resmi tatiller & dini bayram mesaileri", "National & public holiday overtime highlights"),
                    checked = ruleHolidays,
                    icon = Icons.Filled.Flag,
                    onCheckedChange = { ruleHolidays = it }
                )
                RuleCheckTile(
                    title = trEn(isEn, "Nöbet Devir-Teslim Bildirimleri", "Shift Handover Notifications"),
                    subtitle = trEn(isEn, "Vardiya başlamadan önce otomatik hatırlatıcı", "Automatic reminder before shift starts"),
                    checked = ruleNotifications,
                    icon = Icons.Filled.Notifications,
                    onCheckedChange = { ruleNotifications = it }
                )
                RuleCheckTile(
                    title = trEn(isEn, "Offline Yerel Veritabanı Koruması", "Offline Local Storage Protection"),
                    subtitle = trEn(isEn, "İnternet olmadan dahi takvim ve alarmlar çalışır", "Calendar and alarms work completely offline"),
                    checked = ruleOffline,
                    icon = Icons.Filled.SignalCellularConnectedNoInternet4Bar,
                    onCheckedChange = { ruleOffline = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action Buttons
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        modifier = Modifier
                            .weight(0.8f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { wizardStep = 3 }
                            .border(1.dp, dyn.borderMetal, RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        color = dyn.surfaceElevated
                    ) {
                        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = trEn(isEn, "Geri", "Back"), tint = dyn.textSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(trEn(isEn, "Geri", "Back"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = dyn.textPrimary)
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(2f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                viewModel.updateSettings(
                                    currentSettings.copy(
                                        employeeName = previewName,
                                        professionGroup = selectedGroup.id,
                                        title = previewTitle,
                                        department = previewDept,
                                        cyclePreset = chosenPreset,
                                        customSequence = customSeq,
                                        referenceStartDate = startDate,
                                        initialCycleOffset = safeOffset,
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
                                        autoHolidaysEnabled = ruleHolidays,
                                        shiftReminderEnabled = ruleNotifications,
                                        isAnchorConfigured = true,
                                        isOnboardingCompleted = true
                                    )
                                )
                                viewModel.selectMonth(startDate.year, startDate.monthValue)
                                Toast.makeText(
                                    context,
                                    trEn(isEn, "Profiliniz ve 365 Günlük Takviminiz Oluşturuldu!", "Your profile and 365-day calendar are ready!"),
                                    Toast.LENGTH_LONG
                                ).show()
                                viewModel.navigateTo(AppScreen.CALENDAR)
                            }
                            .shadow(12.dp, RoundedCornerShape(14.dp), ambientColor = dyn.primaryNeon)
                            .border(1.dp, BorderSpecularTop, RoundedCornerShape(14.dp))
                            .testTag("btn_create_calendar_finish"),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.Transparent
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.horizontalGradient(listOf(dyn.primaryNeon, dyn.primaryDeep))),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(trEn(isEn, "Profili ve Takvimi Oluştur", "Create Profile & Calendar"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Time Picker Modal (Real interactive scrolling wheel + steppers + presets)
    activeTimeTarget?.let { target ->
        val (title, init) = when (target) {
            WizardTimeTarget.GUNDUZ_START -> trEn(isEn, "Gündüz Nöbeti Başlangıcı", "Day Shift Start") to gunduzStart
            WizardTimeTarget.GUNDUZ_END -> trEn(isEn, "Gündüz Nöbeti Bitişi", "Day Shift End") to gunduzEnd
            WizardTimeTarget.GECE_START -> trEn(isEn, "Gece Nöbeti Başlangıcı", "Night Shift Start") to geceStart
            WizardTimeTarget.GECE_END -> trEn(isEn, "Gece Nöbeti Bitişi", "Night Shift End") to geceEnd
            WizardTimeTarget.SABAH_START -> trEn(isEn, "Sabah Vardiyası Başlangıcı", "Morning Shift Start") to sabahStart
            WizardTimeTarget.SABAH_END -> trEn(isEn, "Sabah Vardiyası Bitişi", "Morning Shift End") to sabahEnd
            WizardTimeTarget.AKSAM_START -> trEn(isEn, "Akşam Vardiyası Başlangıcı", "Evening Shift Start") to aksamStart
            WizardTimeTarget.AKSAM_END -> trEn(isEn, "Akşam Vardiyası Bitişi", "Evening Shift End") to aksamEnd
            WizardTimeTarget.GECE8_START -> trEn(isEn, "Gece (8s) Vardiyası Başlangıcı", "Night (8h) Shift Start") to gece8Start
            WizardTimeTarget.GECE8_END -> trEn(isEn, "Gece (8s) Vardiyası Bitişi", "Night (8h) Shift End") to gece8End
        }
        ShiftTimePickerSheet(
            title = title,
            initialTime = init,
            allow24Hour = true,
            onDismiss = { activeTimeTarget = null },
            onConfirm = { picked ->
                when (target) {
                    WizardTimeTarget.GUNDUZ_START -> gunduzStart = picked
                    WizardTimeTarget.GUNDUZ_END -> gunduzEnd = picked
                    WizardTimeTarget.GECE_START -> geceStart = picked
                    WizardTimeTarget.GECE_END -> geceEnd = picked
                    WizardTimeTarget.SABAH_START -> sabahStart = picked
                    WizardTimeTarget.SABAH_END -> sabahEnd = picked
                    WizardTimeTarget.AKSAM_START -> aksamStart = picked
                    WizardTimeTarget.AKSAM_END -> aksamEnd = picked
                    WizardTimeTarget.GECE8_START -> gece8Start = picked
                    WizardTimeTarget.GECE8_END -> gece8End = picked
                }
            }
        )
    }

    // Anchor Date Picker
    if (showDatePicker) {
        TitaniumDatePickerDialog(
            title = trEn(isEn, "Döngü Başlangıç Tarihi", "Cycle Start Date"),
            initialDate = startDate,
            onDismiss = { showDatePicker = false },
            onConfirm = { startDate = it }
        )
    }

    // First-Day Cycle Offset Selector Dialog
    if (showOffsetDialog) {
        AlertDialog(
            onDismissRequest = { showOffsetDialog = false },
            containerColor = dyn.surfaceCard,
            title = {
                Text(trEn(isEn, "Başlangıç Günü Nöbet Adımı", "Starting Shift Step"), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = dyn.textPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = trEn(
                            isEn,
                            "${startDate.format(DateTimeFormatter.ofPattern("d MMM yyyy", appLocale))} tarihinde döngünün hangi adımında olduğunuzu seçin:",
                            "Select which cycle step you are on ${startDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy", appLocale))}:"
                        ),
                        fontSize = 12.sp,
                        color = dyn.textSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    activeSequence.forEachIndexed { idx, shiftType ->
                        val selected = idx == safeOffset
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) dyn.activeChipBg else dyn.surfacePanel)
                                .border(1.dp, if (selected) dyn.accentCyan else dyn.borderSubtle, RoundedCornerShape(10.dp))
                                .clickable {
                                    initialOffset = idx
                                    showOffsetDialog = false
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                trEn(isEn, "${idx + 1}. Gün: ${shiftType.localizedTitle(false)}", "Day ${idx + 1}: ${shiftType.localizedTitle(true)}"),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) dyn.accentCyan else dyn.textPrimary
                            )
                            if (selected) {
                                Icon(Icons.Filled.Check, contentDescription = trEn(isEn, "Seçili", "Selected"), tint = dyn.accentCyan, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showOffsetDialog = false }) {
                    Text(trEn(isEn, "Kapat", "Close"), color = dyn.accentCyan)
                }
            }
        )
    }

    // Custom Cycle Builder
    if (showCustomBuilder) {
        val isTemplatePreset = chosenPreset == CyclePreset.CYCLE_DUPONT_TEMPLATE ||
            chosenPreset == CyclePreset.CYCLE_FIVE_TEAM_8H
        CustomCycleBuilderSheet(
            initialSequence = if (isTemplatePreset && currentSettings.cyclePreset != chosenPreset) emptyList() else customSeq,
            allowEmptyInitial = isTemplatePreset,
            titleText = if (isTemplatePreset) chosenPreset.localizedName(isEn) else trEn(isEn, "Özel Döngü Oluşturucu", "Custom Cycle Builder"),
            onDismiss = { showCustomBuilder = false },
            onSave = { seq ->
                customSeq = seq
                initialOffset = 0
            }
        )
    }
}

@Composable
private fun WizardStepTab(
    label: String,
    isActive: Boolean,
    isCompleted: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val dyn = DynamicTheme.colors
    Column(modifier = modifier.clickable { onClick() }.padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (isCompleted) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = dyn.accentCyan, modifier = Modifier.size(12.dp))
            }
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive || isCompleted) dyn.accentCyan else dyn.textSecondary,
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(if (isActive || isCompleted) dyn.accentCyan else dyn.surfacePanel, RoundedCornerShape(2.dp))
        )
    }
}

@Composable
private fun WizardTimeChip(
    label: String,
    time: String,
    accentColor: Color,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val dyn = DynamicTheme.colors
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(dyn.surfaceElevated)
            .border(1.dp, accentColor.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .testTag(testTag)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(label, fontSize = 9.sp, color = dyn.textMuted)
            Text(time, fontSize = 16.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = accentColor)
        }
        Icon(Icons.Filled.Schedule, contentDescription = label, tint = accentColor, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun RuleCheckTile(
    title: String,
    subtitle: String,
    checked: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onCheckedChange: (Boolean) -> Unit
) {
    val dyn = DynamicTheme.colors
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
            .border(1.dp, dyn.borderSubtle, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = dyn.surfaceCard
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    colors = CheckboxDefaults.colors(checkedColor = dyn.accentCyan, checkmarkColor = dyn.textOnPrimary, uncheckedColor = dyn.textMuted)
                )
                Column {
                    Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = dyn.textPrimary)
                    Text(subtitle, fontSize = 10.sp, color = dyn.textSecondary)
                }
            }
            Icon(icon, contentDescription = null, tint = dyn.textMuted, modifier = Modifier.size(16.dp))
        }
    }
}
