@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.engine.ShiftAnchor
import com.example.data.engine.ShiftEngine
import com.example.data.model.CyclePreset
import com.example.data.model.ProfessionGroup
import com.example.data.model.ShiftType
import com.example.ui.components.CustomCycleBuilderSheet
import com.example.ui.components.ProShiftLogo
import com.example.ui.components.ProShiftLogoVariant
import com.example.ui.theme.BorderSpecularTop
import com.example.ui.theme.DepartmentColorResolver
import com.example.ui.theme.DynamicTheme
import com.example.ui.i18n.LocalAppLocale
import com.example.ui.i18n.LocalIsEnglish
import com.example.ui.i18n.localizedCode
import com.example.ui.i18n.localizedDescription
import com.example.ui.i18n.localizedName
import com.example.ui.i18n.localizedTitle
import com.example.ui.i18n.localizedTitles
import com.example.ui.i18n.trEn
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.ShiftViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private data class QuickDepartmentOption(
    val trLabel: String,
    val enLabel: String,
    val group: ProfessionGroup
) {
    fun label(isEn: Boolean): String = if (isEn) enLabel else trLabel
}

private enum class OnboardingPresetFilter {
    ALL,
    HOURS_12,
    HOURS_8,
    CUSTOM_24;

    fun label(isEn: Boolean): String = when (this) {
        ALL -> if (isEn) "All (14)" else "Tümü (14)"
        HOURS_12 -> if (isEn) "12-Hour" else "12 Saatlik"
        HOURS_8 -> if (isEn) "8-Hour" else "8 Saatlik"
        CUSTOM_24 -> if (isEn) "24h & Custom" else "24s & Özel"
    }
}

private data class OnboardingPresetCardData(
    val preset: CyclePreset,
    val title: String,
    val subtitle: String,
    val badge: String,
    val filter: OnboardingPresetFilter
)

@Composable
fun OnboardingScreen(
    viewModel: ShiftViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val currentSettings by viewModel.userSettings.collectAsState()
    val dyn = DynamicTheme.colors

    val isFreshProfile = !currentSettings.isOnboardingCompleted

    var employeeName by remember(currentSettings.employeeName, isFreshProfile) {
        mutableStateOf(
            if (isFreshProfile && currentSettings.employeeName == "Dr. Selin Yılmaz") ""
            else currentSettings.employeeName
        )
    }
    var department by remember(currentSettings.department, isFreshProfile) {
        mutableStateOf(
            if (isFreshProfile && currentSettings.department == "İstanbul Acil Servis & 112 KKM") ""
            else currentSettings.department
        )
    }
    var selectedGroup by remember(currentSettings.professionGroup) {
        mutableStateOf(ProfessionGroup.fromId(currentSettings.professionGroup))
    }
    var selectedPreset by remember(currentSettings.cyclePreset) {
        mutableStateOf(currentSettings.cyclePreset)
    }
    var customSeq by remember(currentSettings.customSequence) {
        mutableStateOf(currentSettings.customSequence)
    }
    var activeFilter by remember { mutableStateOf(OnboardingPresetFilter.ALL) }
    var showCustomBuilder by remember { mutableStateOf(false) }
    var initialOffset by remember { mutableIntStateOf(0) }
    var showValidationError by remember { mutableStateOf(false) }

    val isEn = LocalIsEnglish.current
    val appLocale = LocalAppLocale.current
    val quickDepartments = remember {
        listOf(
            QuickDepartmentOption("Acil Servis", "Emergency Room", ProfessionGroup.SAGLIK),
            QuickDepartmentOption("Yoğun Bakım", "Intensive Care", ProfessionGroup.SAGLIK),
            QuickDepartmentOption("Güvenlik Birimi", "Security Unit", ProfessionGroup.GUVENLIK),
            QuickDepartmentOption("İtfaiye İstasyonu", "Fire Station", ProfessionGroup.ITFAIYE_AFET),
            QuickDepartmentOption("Üretim Hattı", "Production Line", ProfessionGroup.SANAYI_URETIM),
            QuickDepartmentOption("Havalimanı / Lojistik", "Airport / Logistics", ProfessionGroup.LOJISTIK_ULASIM),
            QuickDepartmentOption("Çağrı Merkezi", "Call Center", ProfessionGroup.OTEL_HIZMET)
        )
    }

    val presetOptions = remember(isEn) {
        listOf(
            OnboardingPresetCardData(
                preset = CyclePreset.CYCLE_12_36,
                title = CyclePreset.CYCLE_12_36.localizedName(isEn),
                subtitle = CyclePreset.CYCLE_12_36.localizedDescription(isEn),
                badge = trEn(isEn, "POPÜLER", "POPULAR"),
                filter = OnboardingPresetFilter.HOURS_12
            ),
            OnboardingPresetCardData(
                preset = CyclePreset.CYCLE_2_2_2,
                title = CyclePreset.CYCLE_2_2_2.localizedName(isEn),
                subtitle = CyclePreset.CYCLE_2_2_2.localizedDescription(isEn),
                badge = trEn(isEn, "12 SAAT", "12 HOUR"),
                filter = OnboardingPresetFilter.HOURS_12
            ),
            OnboardingPresetCardData(
                preset = CyclePreset.CYCLE_12_24_12_48,
                title = CyclePreset.CYCLE_12_24_12_48.localizedName(isEn),
                subtitle = CyclePreset.CYCLE_12_24_12_48.localizedDescription(isEn),
                badge = trEn(isEn, "GÜVENLİK", "SECURITY"),
                filter = OnboardingPresetFilter.HOURS_12
            ),
            OnboardingPresetCardData(
                preset = CyclePreset.CYCLE_4_4,
                title = CyclePreset.CYCLE_4_4.localizedName(isEn),
                subtitle = CyclePreset.CYCLE_4_4.localizedDescription(isEn),
                badge = trEn(isEn, "12 SAAT", "12 HOUR"),
                filter = OnboardingPresetFilter.HOURS_12
            ),
            OnboardingPresetCardData(
                preset = CyclePreset.CYCLE_PITMAN,
                title = CyclePreset.CYCLE_PITMAN.localizedName(isEn),
                subtitle = CyclePreset.CYCLE_PITMAN.localizedDescription(isEn),
                badge = trEn(isEn, "14 GÜN", "14 DAYS"),
                filter = OnboardingPresetFilter.HOURS_12
            ),
            OnboardingPresetCardData(
                preset = CyclePreset.CYCLE_8_2_2_2,
                title = CyclePreset.CYCLE_8_2_2_2.localizedName(isEn),
                subtitle = CyclePreset.CYCLE_8_2_2_2.localizedDescription(isEn),
                badge = trEn(isEn, "8 SAAT", "8 HOUR"),
                filter = OnboardingPresetFilter.HOURS_8
            ),
            OnboardingPresetCardData(
                preset = CyclePreset.CYCLE_8_2_2_2_2,
                title = CyclePreset.CYCLE_8_2_2_2_2.localizedName(isEn),
                subtitle = CyclePreset.CYCLE_8_2_2_2_2.localizedDescription(isEn),
                badge = trEn(isEn, "8 SAAT", "8 HOUR"),
                filter = OnboardingPresetFilter.HOURS_8
            ),
            OnboardingPresetCardData(
                preset = CyclePreset.CYCLE_8_2_2_2_4,
                title = CyclePreset.CYCLE_8_2_2_2_4.localizedName(isEn),
                subtitle = CyclePreset.CYCLE_8_2_2_2_4.localizedDescription(isEn),
                badge = trEn(isEn, "10 GÜN", "10 DAYS"),
                filter = OnboardingPresetFilter.HOURS_8
            ),
            OnboardingPresetCardData(
                preset = CyclePreset.CYCLE_SOUTHERN_SWING,
                title = CyclePreset.CYCLE_SOUTHERN_SWING.localizedName(isEn),
                subtitle = CyclePreset.CYCLE_SOUTHERN_SWING.localizedDescription(isEn),
                badge = trEn(isEn, "28 GÜN", "28 DAYS"),
                filter = OnboardingPresetFilter.HOURS_8
            ),
            OnboardingPresetCardData(
                preset = CyclePreset.CYCLE_8_3,
                title = CyclePreset.CYCLE_8_3.localizedName(isEn),
                subtitle = CyclePreset.CYCLE_8_3.localizedDescription(isEn),
                badge = trEn(isEn, "8 SAAT", "8 HOUR"),
                filter = OnboardingPresetFilter.HOURS_8
            ),
            OnboardingPresetCardData(
                preset = CyclePreset.CYCLE_24_48,
                title = CyclePreset.CYCLE_24_48.localizedName(isEn),
                subtitle = CyclePreset.CYCLE_24_48.localizedDescription(isEn),
                badge = trEn(isEn, "24 SAAT", "24 HOUR"),
                filter = OnboardingPresetFilter.CUSTOM_24
            ),
            OnboardingPresetCardData(
                preset = CyclePreset.CYCLE_DUPONT_TEMPLATE,
                title = CyclePreset.CYCLE_DUPONT_TEMPLATE.localizedName(isEn),
                subtitle = CyclePreset.CYCLE_DUPONT_TEMPLATE.localizedDescription(isEn),
                badge = trEn(isEn, "ŞABLON", "TEMPLATE"),
                filter = OnboardingPresetFilter.CUSTOM_24
            ),
            OnboardingPresetCardData(
                preset = CyclePreset.CYCLE_FIVE_TEAM_8H,
                title = CyclePreset.CYCLE_FIVE_TEAM_8H.localizedName(isEn),
                subtitle = CyclePreset.CYCLE_FIVE_TEAM_8H.localizedDescription(isEn),
                badge = trEn(isEn, "ŞABLON", "TEMPLATE"),
                filter = OnboardingPresetFilter.CUSTOM_24
            ),
            OnboardingPresetCardData(
                preset = CyclePreset.CUSTOM,
                title = CyclePreset.CUSTOM.localizedName(isEn),
                subtitle = CyclePreset.CUSTOM.localizedDescription(isEn),
                badge = trEn(isEn, "ÖZEL", "CUSTOM"),
                filter = OnboardingPresetFilter.CUSTOM_24
            )
        )
    }

    val filteredPresets = remember(presetOptions, activeFilter) {
        presetOptions.filter {
            activeFilter == OnboardingPresetFilter.ALL || it.filter == activeFilter
        }
    }

    val activeSequence = remember(selectedPreset, customSeq) {
        if (selectedPreset == CyclePreset.CUSTOM || !selectedPreset.hasDefaultPattern) {
            customSeq.ifEmpty { listOf(ShiftType.GUNDUZ, ShiftType.GECE, ShiftType.IZIN) }
        } else {
            selectedPreset.sequence
        }
    }
    val safeOffset = remember(initialOffset, activeSequence) {
        ShiftEngine.safeModulo(initialOffset.toLong(), activeSequence.size)
    }
    val today = remember { LocalDate.now() }
    val previewDays = remember(today, activeSequence, safeOffset, appLocale) {
        val dayFmt = DateTimeFormatter.ofPattern("EEE", appLocale)
        val anchor = ShiftAnchor(today, safeOffset)
        (0 until 7).map { delta ->
            val date = today.plusDays(delta.toLong())
            val type = ShiftEngine.stateFor(date, anchor, activeSequence)
            Triple(
                date.format(dayFmt).uppercase(appLocale).take(3),
                date.dayOfMonth.toString(),
                type
            )
        }
    }

    val trimmedName = employeeName.trim()
    val trimmedDepartment = department.trim()
    val isNameMissing = trimmedName.isEmpty()
    val isDepartmentMissing = trimmedDepartment.isEmpty()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(dyn.background)
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(scrollState)
                .navigationBarsPadding()
                .padding(bottom = 28.dp)
        ) {
            // Top Brand & Skip Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProShiftLogo(
                            variant = ProShiftLogoVariant.HORIZONTAL,
                            withRing = true,
                            height = 34.dp,
                            contentDescription = "PROSHIFT Logo"
                        )
                        Box(
                            modifier = Modifier
                                .background(dyn.accentCyan.copy(alpha = 0.16f), RoundedCornerShape(5.dp))
                                .border(1.dp, dyn.accentCyan.copy(alpha = 0.4f), RoundedCornerShape(5.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = trEn(isEn, "KURULUM", "SETUP"),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = dyn.accentCyan,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    Text(
                        text = trEn(isEn, "Kişisel Vardiya & Nöbet Asistanı", "Personal Shift & Roster Assistant"),
                        fontSize = 11.sp,
                        color = dyn.textSecondary
                    )
                }

                Surface(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable {
                            viewModel.completeOnboarding(AppScreen.HOME)
                        }
                        .border(1.dp, dyn.borderMetal, RoundedCornerShape(18.dp))
                        .testTag("onboarding_skip_button"),
                    shape = RoundedCornerShape(18.dp),
                    color = dyn.surfaceElevated
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = trEn(isEn, "Atla", "Skip"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = dyn.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Headline Intro Card
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = dyn.accentCyan,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = trEn(isEn, "İLK KURULUM SİHİRBAZI", "QUICK SETUP WIZARD"),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = dyn.accentCyan,
                        letterSpacing = 0.8.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = trEn(isEn, "Vardiya Profilini\n30 Saniyede Hazırla", "Set Up Your Shift\nProfile in 30 Seconds"),
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 29.sp,
                    color = dyn.textPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = trEn(
                        isEn,
                        "Adını, departmanını ve çalıştığın vardiya sistemini seç; tüm takvimin ve canlı nöbet sayacın otomatik kurulsun.",
                        "Enter your name, department, and shift system to automatically build your calendar and live duty timer."
                    ),
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = dyn.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 1: Name & Department Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .border(1.dp, dyn.borderMetal, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                color = dyn.surfaceCard
            ) {
                Column(
                    modifier = Modifier
                        .background(dyn.brushedSurfaceBrush)
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .background(dyn.primaryDeep.copy(alpha = 0.22f), CircleShape)
                                .border(1.dp, dyn.primaryNeon.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "1",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = dyn.accentCyan
                            )
                        }
                        Text(
                            text = trEn(isEn, "Personel & Departman Bilgileri", "Staff & Department Info"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = employeeName,
                        onValueChange = {
                            employeeName = it
                            if (showValidationError && it.trim().isNotEmpty()) {
                                showValidationError = false
                            }
                        },
                        label = { Text(trEn(isEn, "Adınız Soyadınız", "Full Name"), fontSize = 11.sp, color = dyn.textSecondary) },
                        placeholder = { Text(trEn(isEn, "Örn: Ahmet Yılmaz", "e.g. Alex Carter"), fontSize = 13.sp, color = dyn.textMuted) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = dyn.accentCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        isError = showValidationError && isNameMissing,
                        supportingText = if (showValidationError && isNameMissing) {
                            {
                                Text(
                                    text = trEn(isEn, "Lütfen adınızı girin", "Please enter your name"),
                                    fontSize = 11.sp,
                                    color = dyn.eveningShiftAccent
                                )
                            }
                        } else null,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = dyn.accentCyan,
                            unfocusedBorderColor = dyn.borderMetal,
                            errorBorderColor = dyn.eveningShiftAccent,
                            focusedTextColor = dyn.textPrimary,
                            unfocusedTextColor = dyn.textPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_name_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = department,
                        onValueChange = {
                            department = it
                            if (showValidationError && it.trim().isNotEmpty()) {
                                showValidationError = false
                            }
                        },
                        label = { Text(trEn(isEn, "Departman / Birim", "Department / Unit"), fontSize = 11.sp, color = dyn.textSecondary) },
                        placeholder = { Text(trEn(isEn, "Örn: Acil Servis, Güvenlik, Üretim...", "e.g. Emergency Room, Security, Production..."), fontSize = 13.sp, color = dyn.textMuted) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.WorkOutline,
                                contentDescription = null,
                                tint = dyn.accentCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        isError = showValidationError && isDepartmentMissing,
                        supportingText = if (showValidationError && isDepartmentMissing) {
                            {
                                Text(
                                    text = trEn(isEn, "Lütfen çalıştığınız departmanı veya birimi yazın", "Please enter your department or unit"),
                                    fontSize = 11.sp,
                                    color = dyn.eveningShiftAccent
                                )
                            }
                        } else null,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = dyn.accentCyan,
                            unfocusedBorderColor = dyn.borderMetal,
                            errorBorderColor = dyn.eveningShiftAccent,
                            focusedTextColor = dyn.textPrimary,
                            unfocusedTextColor = dyn.textPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_department_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = trEn(isEn, "HIZLI DEPARTMAN SEÇİMİ", "QUICK DEPARTMENT"),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = dyn.textMuted,
                        letterSpacing = 0.6.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(quickDepartments) { option ->
                            val chipLabel = option.label(isEn)
                            val isSel = department.equals(option.trLabel, ignoreCase = true) ||
                                department.equals(option.enLabel, ignoreCase = true)
                            Surface(
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        department = chipLabel
                                        selectedGroup = option.group
                                        showValidationError = false
                                    }
                                    .border(
                                        width = 1.dp,
                                        color = if (isSel) dyn.accentCyan else dyn.borderSubtle,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .testTag("onboarding_dept_chip_${option.trLabel}"),
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) dyn.activeChipBg else dyn.surfaceElevated
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isSel) Icons.Filled.Check else Icons.Filled.Badge,
                                        contentDescription = null,
                                        tint = if (isSel) dyn.accentCyan else dyn.textMuted,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = chipLabel,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSel) dyn.accentCyan else dyn.textSecondary
                                    )
                                }
                            }
                        }
                    }

                    val previewTone = remember(department, selectedGroup) {
                        DepartmentColorResolver.resolveDepartmentTone(department, selectedGroup.id)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(previewTone.seedColor.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                            .border(1.dp, previewTone.seedColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(previewTone.seedColor, CircleShape)
                        )
                        Text(
                            text = trEn(isEn, "Kurulum sonrası tema tonu: ${previewTone.label}", "Post-setup theme accent: ${previewTone.label}"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = dyn.textPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Section 2: Shift System Selection Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .border(1.dp, dyn.borderMetal, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                color = dyn.surfaceCard
            ) {
                Column(
                    modifier = Modifier
                        .background(dyn.brushedSurfaceBrush)
                        .padding(16.dp)
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
                                    .size(26.dp)
                                    .background(dyn.primaryDeep.copy(alpha = 0.22f), CircleShape)
                                    .border(1.dp, dyn.primaryNeon.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "2",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = dyn.accentCyan
                                )
                            }
                            Text(
                                text = trEn(isEn, "Vardiya Sistemini Seç", "Choose Shift System"),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = dyn.textPrimary
                            )
                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.navigateTo(AppScreen.WIZARD) }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                .testTag("onboarding_advanced_wizard_button"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Tune,
                                contentDescription = trEn(isEn, "Özel Döngü", "Custom Cycle"),
                                tint = dyn.accentCyan,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = trEn(isEn, "Özel / Saat", "Custom / Hours"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = dyn.accentCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(OnboardingPresetFilter.entries) { filter ->
                            val isFilterSelected = activeFilter == filter
                            Surface(
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { activeFilter = filter }
                                    .border(
                                        1.dp,
                                        if (isFilterSelected) dyn.accentCyan else dyn.borderSubtle,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .testTag("onboarding_filter_${filter.name}"),
                                shape = RoundedCornerShape(10.dp),
                                color = if (isFilterSelected) dyn.activeChipBg else dyn.surfaceElevated
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = filter.label(isEn),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isFilterSelected) dyn.accentCyan else dyn.textSecondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        filteredPresets.forEach { item ->
                            val isSelected = selectedPreset == item.preset
                            val isCustomPreset = item.preset == CyclePreset.CUSTOM || !item.preset.hasDefaultPattern
                            val cardSequence = if (isCustomPreset) {
                                if (isSelected) activeSequence else customSeq
                            } else {
                                item.preset.sequence
                            }
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        selectedPreset = item.preset
                                        initialOffset = 0
                                        if (isCustomPreset) {
                                            showCustomBuilder = true
                                        }
                                    }
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) dyn.accentCyan else dyn.borderSubtle,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .testTag("onboarding_preset_${item.preset.id}"),
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) dyn.activeChipBg else dyn.surfaceElevated
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 12.dp),
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
                                                .background(
                                                    if (isSelected) dyn.primaryNeon.copy(alpha = 0.18f) else dyn.surfacePanel,
                                                    RoundedCornerShape(10.dp)
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isSelected) dyn.accentCyan else dyn.borderSubtle,
                                                    RoundedCornerShape(10.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = when {
                                                    isSelected -> Icons.Filled.Check
                                                    isCustomPreset -> Icons.Filled.Tune
                                                    else -> Icons.Filled.Repeat
                                                },
                                                contentDescription = null,
                                                tint = if (isSelected) dyn.accentCyan else dyn.textSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = item.title,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) dyn.accentCyan else dyn.textPrimary
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .background(
                                                            if (isSelected) dyn.accentCyan.copy(alpha = 0.18f) else dyn.surfacePanel,
                                                            RoundedCornerShape(4.dp)
                                                        )
                                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = item.badge,
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSelected) dyn.accentCyan else dyn.textMuted,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = if (isSelected && isCustomPreset) {
                                                    trEn(
                                                        isEn,
                                                        "Aktif Dizi (${cardSequence.size} gün) • Düzenlemek için dokun",
                                                        "Active Cycle (${cardSequence.size} days) • Tap to edit"
                                                    )
                                                } else {
                                                    item.subtitle
                                                },
                                                fontSize = 11.sp,
                                                color = dyn.textSecondary
                                            )
                                        }
                                    }

                                    // Compact sequence tokens preview
                                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                        cardSequence.take(4).forEach { shiftType ->
                                            val tokenColor = when {
                                                shiftType == ShiftType.IZIN -> dyn.textMuted
                                                shiftType == ShiftType.AKSAM -> dyn.eveningShiftAccent
                                                shiftType.isNight -> dyn.nightShiftAccent
                                                else -> dyn.dayShiftAccent
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .background(tokenColor.copy(alpha = 0.16f), RoundedCornerShape(5.dp))
                                                    .border(1.dp, tokenColor.copy(alpha = 0.4f), RoundedCornerShape(5.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = shiftType.localizedCode(isEn),
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = tokenColor
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Live Today Step Picker + 7-day cycle preview
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, dyn.borderSubtle, RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        color = dyn.surfacePanel
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = trEn(isEn, "BUGÜN HANGİ VARDİYADASIN?", "WHICH SHIFT ARE YOU ON TODAY?"),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = dyn.textMuted,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = selectedPreset.localizedName(isEn),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = dyn.accentCyan
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(activeSequence.indices.take(8).toList()) { idx ->
                                    val shiftType = activeSequence[idx]
                                    val isOffsetSelected = idx == safeOffset
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { initialOffset = idx }
                                            .border(
                                                1.dp,
                                                if (isOffsetSelected) dyn.accentCyan else dyn.borderSubtle,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .testTag("onboarding_offset_$idx"),
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isOffsetSelected) dyn.activeChipBg else dyn.surfaceCard
                                    ) {
                                        Text(
                                            text = trEn(
                                                isEn,
                                                "${idx + 1}. Gün: ${shiftType.localizedTitle(false)}",
                                                "Day ${idx + 1}: ${shiftType.localizedTitle(true)}"
                                            ),
                                            fontSize = 10.sp,
                                            fontWeight = if (isOffsetSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isOffsetSelected) dyn.accentCyan else dyn.textSecondary,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                previewDays.forEachIndexed { index, (dayShort, dayNum, shiftType) ->
                                    val shiftColor = when {
                                        shiftType == ShiftType.IZIN -> dyn.offShiftAccent
                                        shiftType == ShiftType.AKSAM -> dyn.eveningShiftAccent
                                        shiftType.isNight -> dyn.nightShiftAccent
                                        else -> dyn.dayShiftAccent
                                    }
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                if (index == 0) dyn.activeChipBg else dyn.surfaceCard,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .border(
                                                1.dp,
                                                if (index == 0) dyn.accentCyan else dyn.borderSubtle,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(vertical = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = dayShort,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (index == 0) dyn.accentCyan else dyn.textMuted
                                        )
                                        Text(
                                            text = dayNum,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = dyn.textPrimary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = shiftType.localizedCode(isEn),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (shiftType == ShiftType.IZIN) dyn.textSecondary else shiftColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(visible = showValidationError && (isNameMissing || isDepartmentMissing)) {
                Text(
                    text = trEn(isEn, "Devam etmek için adınızı ve departmanınızı doldurun.", "Please enter your name and department to continue."),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = dyn.eveningShiftAccent,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Primary Action Button -> Save to SharedPreferences & Navigate to HomeScreen
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        if (isNameMissing || isDepartmentMissing) {
                            showValidationError = true
                            coroutineScope.launch {
                                scrollState.animateScrollTo(0)
                            }
                            Toast.makeText(
                                context,
                                trEn(isEn, "Lütfen adınızı ve departmanınızı girin", "Please enter your name and department"),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            viewModel.completeOnboardingSetup(
                                employeeName = trimmedName,
                                department = trimmedDepartment,
                                cyclePreset = selectedPreset,
                                customSequence = if (!selectedPreset.hasDefaultPattern) activeSequence else null,
                                professionGroup = selectedGroup,
                                title = selectedGroup.localizedTitles(isEn).first(),
                                referenceStartDate = today,
                                initialCycleOffset = safeOffset
                            )
                            Toast.makeText(
                                context,
                                trEn(isEn, "Hoş geldin $trimmedName! Takvimin hazırlandı.", "Welcome $trimmedName! Your schedule is ready."),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    .shadow(12.dp, RoundedCornerShape(16.dp), ambientColor = dyn.primaryNeon)
                    .border(1.dp, if (dyn.isDark) BorderSpecularTop else Color.Transparent, RoundedCornerShape(16.dp))
                    .testTag("onboarding_complete_button"),
                shape = RoundedCornerShape(16.dp),
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
                        Text(
                            text = trEn(isEn, "Kurulumu Tamamla ve Başla", "Complete Setup & Start"),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = trEn(isEn, "Başla", "Start"),
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = trEn(isEn, "Güvenlik", "Security"),
                    tint = dyn.textMuted,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = trEn(
                        isEn,
                        "Bilgileriniz cihazınızda güvenle saklanır • Çevrimdışı çalışır",
                        "Your data is stored safely on your device • Works offline"
                    ),
                    fontSize = 10.sp,
                    color = dyn.textMuted
                )
            }
        }

        if (showCustomBuilder) {
            val isTemplatePreset = selectedPreset == CyclePreset.CYCLE_DUPONT_TEMPLATE ||
                selectedPreset == CyclePreset.CYCLE_FIVE_TEAM_8H
            CustomCycleBuilderSheet(
                initialSequence = if (isTemplatePreset && currentSettings.cyclePreset != selectedPreset) emptyList() else customSeq,
                allowEmptyInitial = isTemplatePreset,
                titleText = if (isTemplatePreset) selectedPreset.localizedName(isEn) else trEn(isEn, "Özel Döngü Oluşturucu", "Custom Cycle Builder"),
                onDismiss = { showCustomBuilder = false },
                onSave = { seq ->
                    customSeq = seq
                    initialOffset = 0
                }
            )
        }
    }
}
