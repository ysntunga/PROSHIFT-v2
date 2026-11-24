@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.ui.components

import android.widget.Toast
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ColleagueProfile
import com.example.data.model.CyclePreset
import com.example.data.model.ShiftType
import com.example.ui.i18n.LocalAppLocale
import com.example.ui.i18n.LocalIsEnglish
import com.example.ui.i18n.localizedCode
import com.example.ui.i18n.localizedDescription
import com.example.ui.i18n.localizedName
import com.example.ui.i18n.localizedTitle
import com.example.ui.i18n.trEn
import com.example.ui.theme.DynamicTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ManualColleagueSheet(
    onDismiss: () -> Unit,
    onSave: (ColleagueProfile) -> Unit
) {
    val context = LocalContext.current
    val isEn = LocalIsEnglish.current
    val appLocale = LocalAppLocale.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var selectedPreset by remember { mutableStateOf(CyclePreset.CYCLE_8_2_2_2) }
    var customSeq by remember {
        mutableStateOf(
            listOf(
                ShiftType.SABAH, ShiftType.SABAH,
                ShiftType.AKSAM, ShiftType.AKSAM,
                ShiftType.GECE8, ShiftType.GECE8
            )
        )
    }
    var referenceDate by remember { mutableStateOf(LocalDate.now()) }
    var cycleOffset by remember { mutableIntStateOf(0) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showCustomBuilder by remember { mutableStateOf(false) }

    val usesCustomSeq = selectedPreset == CyclePreset.CUSTOM || !selectedPreset.hasDefaultPattern
    val activeSeq = remember(selectedPreset, customSeq) {
        if (selectedPreset == CyclePreset.CUSTOM || !selectedPreset.hasDefaultPattern) customSeq else selectedPreset.sequence
    }
    val safeOffset = ((cycleOffset % activeSeq.size.coerceAtLeast(1)) + activeSeq.size.coerceAtLeast(1)) % activeSeq.size.coerceAtLeast(1)

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
                .imePadding()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column {
                Text(
                    text = trEn("Arkadaşını Elle Ekle", "Add Colleague Manually"),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = dyn.textPrimary
                )
                Text(
                    text = trEn("Kamerasız veya ikinci cihaz olmadan arkadaşının vardiya düzenini tanımla", "Define your colleague's shift rotation without a camera or second device"),
                    fontSize = 11.sp,
                    color = dyn.textSecondary
                )
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(trEn("Arkadaşın Adı Soyadı *", "Colleague's Full Name *"), fontSize = 11.sp, color = dyn.textSecondary) },
                placeholder = { Text(trEn("Örn: Emre Kaya", "e.g. Alex Taylor"), color = dyn.textMuted) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = dyn.accentCyan,
                    unfocusedBorderColor = dyn.borderMetal,
                    focusedTextColor = dyn.textPrimary,
                    unfocusedTextColor = dyn.textPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_manual_friend_name"),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = department,
                onValueChange = { department = it },
                label = { Text(trEn("Birim / Bölüm (İsteğe bağlı)", "Department / Unit (Optional)"), fontSize = 11.sp, color = dyn.textSecondary) },
                placeholder = { Text(trEn("Örn: Montaj Hattı / Acil Servis", "e.g. Assembly Line / Emergency Dept"), color = dyn.textMuted) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = dyn.accentCyan,
                    unfocusedBorderColor = dyn.borderMetal,
                    focusedTextColor = dyn.textPrimary,
                    unfocusedTextColor = dyn.textPrimary
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Text(
                text = trEn("Çalışma Düzeni Seç:", "Select Shift Pattern:"),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = dyn.accentCyan
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                CyclePreset.entries.forEach { preset ->
                    val isSelected = selectedPreset == preset
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                selectedPreset = preset
                                cycleOffset = 0
                                if (preset == CyclePreset.CUSTOM || !preset.hasDefaultPattern) {
                                    showCustomBuilder = true
                                }
                            }
                            .border(
                                1.dp,
                                if (isSelected) dyn.accentCyan else dyn.borderSubtle,
                                RoundedCornerShape(10.dp)
                            ),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) dyn.activeChipBg else dyn.surfaceElevated
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = preset.localizedName(isEn),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) dyn.accentCyan else dyn.textPrimary
                                )
                                Text(
                                    text = if (preset == CyclePreset.CUSTOM || (!preset.hasDefaultPattern && isSelected && customSeq.isNotEmpty())) {
                                        customSeq.joinToString("-") { it.localizedCode(isEn) }
                                    } else {
                                        preset.localizedDescription(isEn)
                                    },
                                    fontSize = 10.sp,
                                    color = dyn.textSecondary,
                                    maxLines = 1
                                )
                            }
                            if (preset == CyclePreset.CUSTOM || !preset.hasDefaultPattern) {
                                Icon(
                                    imageVector = Icons.Filled.Tune,
                                    contentDescription = trEn("Özel Döngü Düzenle", "Edit Custom Cycle"),
                                    tint = dyn.accentCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = trEn("Seçili", "Selected"),
                                    tint = dyn.accentCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Start date row
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { showDatePicker = true }
                    .border(1.dp, dyn.borderMetal, RoundedCornerShape(10.dp)),
                color = dyn.surfacePanel
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(trEn("Döngü Başlangıç Tarihi", "Cycle Start Date"), fontSize = 9.sp, color = dyn.textMuted)
                        Text(
                            text = referenceDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", appLocale)),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.accentCyan
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = trEn("Tarih Seç", "Select Date"),
                        tint = dyn.accentCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = trEn("Seçilen Tarihteki İlk Vardiyası:", "First Shift on Selected Date:"),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = dyn.textSecondary
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                itemsIndexed(activeSeq) { idx, shiftType ->
                    val isSel = idx == safeOffset
                    val stepAccent = when {
                        shiftType == ShiftType.AKSAM -> dyn.eveningShiftAccent
                        shiftType.isNight -> dyn.nightShiftAccent
                        shiftType.isWork -> dyn.dayShiftAccent
                        else -> dyn.textSecondary
                    }
                    val stepSelBg = when {
                        shiftType == ShiftType.AKSAM -> dyn.eveningShiftBg
                        shiftType.isNight -> dyn.nightShiftBg
                        shiftType.isWork -> dyn.dayShiftBg
                        else -> dyn.activeChipBg
                    }
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { cycleOffset = idx }
                            .border(1.dp, if (isSel) stepAccent else dyn.borderSubtle, RoundedCornerShape(8.dp)),
                        color = if (isSel) stepSelBg else dyn.surfaceElevated
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(trEn("${idx + 1}. Gün", "Day ${idx + 1}"), fontSize = 8.sp, color = dyn.textMuted)
                            Text(
                                text = "${shiftType.iconEmoji} ${shiftType.localizedTitle(isEn).substringBefore(" (")}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) stepAccent else dyn.textPrimary
                            )
                            Text(
                                text = if (shiftType.isWork) "${shiftType.defaultStart.take(2)}-${shiftType.defaultEnd.take(2)}" else trEn("İstirahat", "Rest"),
                                fontSize = 8.sp,
                                color = if (isSel) stepAccent else dyn.textMuted
                            )
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
                    Box(contentAlignment = Alignment.Center) {
                        Text(trEn("İptal", "Cancel"), color = dyn.textSecondary, fontWeight = FontWeight.SemiBold)
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1.4f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val cleanName = name.trim()
                            if (cleanName.isEmpty()) {
                                Toast.makeText(context, trEn(isEn, "Lütfen arkadaşınızın adını yazın", "Please enter your colleague's name"), Toast.LENGTH_SHORT).show()
                                return@clickable
                            }
                            val profile = ColleagueProfile(
                                name = cleanName,
                                department = department.trim().ifEmpty { trEn(isEn, "Vardiya Ekibi", "Shift Team") },
                                cyclePreset = selectedPreset,
                                referenceStartDate = referenceDate,
                                qrCodeId = "PRSH-MAN-${System.currentTimeMillis()}",
                                customSequence = if (usesCustomSeq) customSeq else null,
                                initialCycleOffset = safeOffset
                            )
                            onSave(profile)
                            onDismiss()
                        }
                        .testTag("btn_save_manual_friend"),
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(Brush.horizontalGradient(listOf(dyn.primaryNeon, dyn.primaryDeep))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(trEn("Arkadaşı Ekle", "Add Colleague"), fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        TitaniumDatePickerDialog(
            title = trEn("Arkadaşın Döngü Başlangıcı", "Colleague's Cycle Start"),
            initialDate = referenceDate,
            onDismiss = { showDatePicker = false },
            onConfirm = { referenceDate = it }
        )
    }

    if (showCustomBuilder) {
        CustomCycleBuilderSheet(
            initialSequence = if (!selectedPreset.hasDefaultPattern && selectedPreset != CyclePreset.CUSTOM) emptyList() else customSeq,
            allowEmptyInitial = !selectedPreset.hasDefaultPattern && selectedPreset != CyclePreset.CUSTOM,
            titleText = if (!selectedPreset.hasDefaultPattern && selectedPreset != CyclePreset.CUSTOM) {
                trEn("${selectedPreset.localizedName(false)} Düzenle", "Edit ${selectedPreset.localizedName(true)}")
            } else {
                trEn("Özel Vardiya Döngüsü Oluştur", "Create Custom Shift Cycle")
            },
            onDismiss = { showCustomBuilder = false },
            onSave = { seq ->
                customSeq = seq
                cycleOffset = 0
            }
        )
    }
}
