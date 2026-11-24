package com.example.ui.components

import android.os.Build
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.data.model.ShiftDay
import com.example.data.model.ShiftType
import java.io.File
import java.io.FileOutputStream
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import com.example.ui.theme.ProshiftMonoFont
import com.example.ui.theme.TitaniumBrushedBrush
import com.example.ui.theme.TitaniumElevatedBrush
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BackgroundTitanium
import com.example.ui.theme.BorderGlow
import com.example.ui.theme.BorderMetal
import com.example.ui.theme.BorderSpecularTop
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DynamicTheme
import com.example.ui.theme.PrimaryDeep
import com.example.ui.theme.PrimaryMetallic
import com.example.ui.theme.PrimaryNeon
import com.example.ui.theme.ShiftGece
import com.example.ui.theme.ShiftGeceBg
import com.example.ui.theme.ShiftGeceBorder
import com.example.ui.theme.ShiftGunduz
import com.example.ui.theme.ShiftGunduzBg
import com.example.ui.theme.ShiftGunduzBorder
import com.example.ui.theme.ShiftIzin
import com.example.ui.theme.ShiftIzinBg
import com.example.ui.theme.ShiftIzinBorder
import com.example.ui.theme.ShiftOvertimeAmber
import com.example.ui.theme.ShiftOvertimeBg
import com.example.ui.theme.ShiftOvertimeBorder
import com.example.ui.theme.ShiftRapor
import com.example.ui.theme.ShiftRaporBg
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.SurfacePanel
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.i18n.LocalAppLocale
import com.example.ui.i18n.LocalIsEnglish
import com.example.ui.i18n.formatHourUnit
import com.example.ui.i18n.localizedShortLabel
import com.example.ui.i18n.localizedTitle
import com.example.ui.i18n.trEn
import com.example.ui.viewmodel.AppScreen
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

fun shiftTypeVector(type: ShiftType): ImageVector = when (type) {
    ShiftType.GUNDUZ -> Icons.Rounded.WbSunny
    ShiftType.GECE, ShiftType.GECE8 -> Icons.Rounded.NightsStay
    ShiftType.IZIN -> Icons.Rounded.LocalCafe
    ShiftType.RAPOR -> Icons.Rounded.LocalHospital
    ShiftType.OZEL -> Icons.Rounded.Bolt
    ShiftType.SABAH, ShiftType.AKSAM -> Icons.Rounded.WbTwilight
    ShiftType.NOBET24 -> Icons.Rounded.Shield
}

@Composable
fun Modifier.titaniumBrushedSurface(
    shape: Shape = RoundedCornerShape(16.dp),
    borderColor: Color = DynamicTheme.colors.borderSubtle,
    borderWidth: Dp = 1.dp,
    elevated: Boolean = false
): Modifier = this
    .clip(shape)
    .background(
        if (elevated) DynamicTheme.colors.elevatedSurfaceBrush else DynamicTheme.colors.brushedSurfaceBrush,
        shape
    )
    .border(
        width = borderWidth,
        brush = Brush.verticalGradient(
            colors = listOf(
                borderColor.copy(alpha = (borderColor.alpha * 1.45f).coerceAtMost(0.95f)),
                borderColor,
                borderColor.copy(alpha = (borderColor.alpha * 0.55f).coerceAtLeast(0.08f))
            )
        ),
        shape = shape
    )

@Composable
fun Modifier.ultraThinFrostedSurface(
    shape: Shape = RoundedCornerShape(32.dp),
    borderWidth: Dp = 1.dp
): Modifier = this
    .clip(shape)
    .background(
        brush = Brush.verticalGradient(
            colors = listOf(
                DynamicTheme.colors.surfaceElevated.copy(alpha = 0.72f),
                DynamicTheme.colors.surfaceCard.copy(alpha = 0.84f)
            )
        ),
        shape = shape
    )
    .border(
        width = borderWidth,
        brush = Brush.verticalGradient(
            colors = listOf(
                DynamicTheme.colors.borderMetal,
                DynamicTheme.colors.borderSubtle,
                DynamicTheme.colors.primaryNeon.copy(alpha = 0.08f)
            )
        ),
        shape = shape
    )

@Composable
fun FloatingDockBottomNav(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val dockShape = RoundedCornerShape(32.dp)
    val dyn = DynamicTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
            .padding(horizontal = 14.dp)
            .padding(bottom = 12.dp, top = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .shadow(elevation = 24.dp, shape = dockShape, ambientColor = dyn.primaryNeon.copy(alpha = 0.25f))
                .ultraThinFrostedSurface(shape = dockShape)
        ) {
            // GPU-accelerated frosted reflection sheen behind crisp foreground navigation items
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            Modifier.blur(18.dp, edgeTreatment = BlurredEdgeTreatment(dockShape))
                        } else {
                            Modifier
                        }
                    )
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                dyn.primaryNeon.copy(alpha = 0.09f),
                                dyn.surfaceHighlight.copy(alpha = 0.24f),
                                dyn.primaryDeep.copy(alpha = 0.13f)
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DockNavItem(
                    title = trEn("Ana Sayfa", "Home"),
                    isSelected = currentScreen == AppScreen.HOME,
                    selectedIcon = Icons.Filled.Home,
                    unselectedIcon = Icons.Outlined.Home,
                    testTag = "nav_home",
                    onClick = { onNavigate(AppScreen.HOME) }
                )
                DockNavItem(
                    title = trEn("Takvim", "Calendar"),
                    isSelected = currentScreen == AppScreen.CALENDAR,
                    selectedIcon = Icons.Filled.CalendarMonth,
                    unselectedIcon = Icons.Outlined.CalendarMonth,
                    testTag = "nav_calendar",
                    onClick = { onNavigate(AppScreen.CALENDAR) }
                )
                DockNavItem(
                    title = trEn("Eşleşme", "Match"),
                    isSelected = currentScreen == AppScreen.MATCHING_QR || currentScreen == AppScreen.QR_SCANNER,
                    selectedIcon = Icons.Filled.People,
                    unselectedIcon = Icons.Outlined.People,
                    testTag = "nav_matching",
                    onClick = { onNavigate(AppScreen.MATCHING_QR) }
                )
                DockNavItem(
                    title = trEn("Puantaj", "Payroll"),
                    isSelected = currentScreen == AppScreen.TIMESHEET,
                    selectedIcon = Icons.Filled.QueryStats,
                    unselectedIcon = Icons.Outlined.QueryStats,
                    testTag = "nav_timesheet",
                    onClick = { onNavigate(AppScreen.TIMESHEET) }
                )
                DockNavItem(
                    title = trEn("Ayarlar", "Settings"),
                    isSelected = currentScreen == AppScreen.SETTINGS || currentScreen == AppScreen.PROFILE,
                    selectedIcon = Icons.Filled.Settings,
                    unselectedIcon = Icons.Outlined.Settings,
                    testTag = "nav_settings",
                    onClick = { onNavigate(AppScreen.SETTINGS) }
                )
            }
        }
    }
}

@Composable
private fun DockNavItem(
    title: String,
    isSelected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    val dyn = DynamicTheme.colors
    val tintColor by animateColorAsState(
        targetValue = if (isSelected) dyn.primaryNeon else dyn.textMuted,
        label = "tint"
    )
    val pillShape = RoundedCornerShape(22.dp)

    Box(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .then(
                if (isSelected) {
                    Modifier
                        .clip(pillShape)
                        .background(
                            Brush.verticalGradient(
                                colors = if (dyn.isDark) {
                                    listOf(dyn.primaryDeep.copy(alpha = 0.34f), dyn.surfaceElevated.copy(alpha = 0.9f))
                                } else {
                                    listOf(dyn.primaryNeon.copy(alpha = 0.14f), dyn.surfaceElevated.copy(alpha = 0.95f))
                                }
                            )
                        )
                        .border(1.dp, dyn.primaryNeon.copy(alpha = 0.45f), pillShape)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                } else {
                    Modifier
                        .clip(pillShape)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                }
            )
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isSelected) selectedIcon else unselectedIcon,
                contentDescription = title,
                tint = tintColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 9.5.sp,
                maxLines = 1,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = tintColor
            )
        }
    }
}

@Composable
fun ShiftBadge(
    type: ShiftType,
    modifier: Modifier = Modifier,
    overtimeHours: Double = 0.0
) {
    val dyn = DynamicTheme.colors
    val (bgColor, borderColor, textColor) = when (type) {
        ShiftType.GUNDUZ, ShiftType.SABAH -> Triple(dyn.dayShiftBg, dyn.dayShiftBorder, dyn.dayShiftAccent)
        ShiftType.GECE, ShiftType.GECE8, ShiftType.NOBET24 -> Triple(dyn.nightShiftBg, dyn.nightShiftBorder, dyn.nightShiftAccent)
        ShiftType.IZIN -> Triple(dyn.offShiftBg, dyn.offShiftBorder, dyn.offShiftAccent)
        ShiftType.RAPOR -> Triple(dyn.reportShiftBg, dyn.reportShiftBorder, dyn.reportShiftAccent)
        ShiftType.OZEL, ShiftType.AKSAM -> Triple(dyn.eveningShiftBg, dyn.eveningShiftBorder, dyn.eveningShiftAccent)
    }

    val isEn = LocalIsEnglish.current
    val hUnit = formatHourUnit(isEn)
    Row(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = shiftTypeVector(type),
            contentDescription = type.localizedTitle(isEn),
            tint = textColor,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = if (overtimeHours > 0) "+${overtimeHours.toInt()}$hUnit ${type.localizedTitle(isEn)}" else type.localizedShortLabel(isEn),
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DayInspectorDialog(
    day: ShiftDay,
    settings: com.example.data.model.UserSettings? = null,
    onDismiss: () -> Unit,
    onSave: (ShiftType, Double, String, String?, String?) -> Unit,
    onReset: () -> Unit
) {
    fun defaultHoursFor(t: ShiftType): Pair<String, String> = when (t) {
        ShiftType.GUNDUZ -> (settings?.gunduzStartTime ?: t.defaultStart) to (settings?.gunduzEndTime ?: t.defaultEnd)
        ShiftType.GECE -> (settings?.geceStartTime ?: t.defaultStart) to (settings?.geceEndTime ?: t.defaultEnd)
        ShiftType.SABAH -> (settings?.sabahStartTime ?: t.defaultStart) to (settings?.sabahEndTime ?: t.defaultEnd)
        ShiftType.AKSAM -> (settings?.aksamStartTime ?: t.defaultStart) to (settings?.aksamEndTime ?: t.defaultEnd)
        ShiftType.GECE8 -> (settings?.gece8StartTime ?: t.defaultStart) to (settings?.gece8EndTime ?: t.defaultEnd)
        ShiftType.NOBET24 -> (settings?.gunduzStartTime ?: t.defaultStart) to (settings?.gunduzStartTime ?: t.defaultEnd)
        else -> t.defaultStart to t.defaultEnd
    }

    var selectedType by remember(day) { mutableStateOf(day.type) }
    var startTimeText by remember(day) { mutableStateOf(day.startTime) }
    var endTimeText by remember(day) { mutableStateOf(day.endTime) }
    var overtime by remember(day) { mutableDoubleStateOf(day.overtimeHours) }
    var noteText by remember(day) { mutableStateOf(day.note) }
    var pickingStart by remember { mutableStateOf<Boolean?>(null) }

    val dyn = DynamicTheme.colors
    val isEn = LocalIsEnglish.current
    val appLocale = LocalAppLocale.current
    val hUnit = formatHourUnit(isEn)
    val dateFmt = remember(day.date, appLocale) {
        day.date.format(DateTimeFormatter.ofPattern("d MMMM yyyy, EEEE", appLocale))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dyn.surfaceCard,
        title = {
            Column {
                Text(
                    text = trEn("Vardiya Düzenle", "Edit Shift"),
                    color = dyn.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateFmt,
                    color = dyn.accentCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                if (!day.holidayName.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .background(dyn.emeraldBg, RoundedCornerShape(6.dp))
                            .border(1.dp, dyn.emeraldBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (day.isHalfDayHoliday) "🏛️ ${day.holidayName} (${trEn("Yarım Gün", "Half Day")})" else "🏛️ ${day.holidayName}",
                            color = dyn.emeraldAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = trEn("Vardiya Türü Seçin:", "Select Shift Type:"),
                    color = dyn.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // Shift Type Selection Chips (2 rows: 12s/İzin & 8s/24s)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        listOf(ShiftType.GUNDUZ, ShiftType.GECE, ShiftType.IZIN, ShiftType.RAPOR),
                        listOf(ShiftType.SABAH, ShiftType.AKSAM, ShiftType.GECE8, ShiftType.NOBET24)
                    ).forEach { rowTypes ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowTypes.forEach { t ->
                                val isChosen = selectedType == t
                                val (defS, defE) = defaultHoursFor(t)
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isChosen) (if (dyn.isDark) dyn.primaryDeep else dyn.primaryNeon.copy(alpha = 0.14f)) else dyn.surfaceElevated,
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            1.dp,
                                            if (isChosen) dyn.primaryNeon else dyn.borderSubtle,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            selectedType = t
                                            startTimeText = defS
                                            endTimeText = defE
                                        }
                                        .padding(vertical = 8.dp, horizontal = 2.dp),
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = shiftTypeVector(t),
                                            contentDescription = t.localizedTitle(isEn),
                                            tint = if (isChosen) dyn.accentCyan else dyn.textSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = t.localizedTitle(isEn).substringBefore(" ("),
                                            fontSize = 10.sp,
                                            fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isChosen) dyn.textPrimary else dyn.textSecondary,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = if (t.isWork) "${defS.take(2)}-${defE.take(2)}" else trEn("Boş", "Off"),
                                            fontSize = 8.sp,
                                            color = if (isChosen) dyn.accentCyan else dyn.textMuted,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (selectedType.isWork) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = trEn("Vardiya Saatleri (Dokun & Değiştir):", "Shift Hours (Tap to Edit):"),
                        color = dyn.textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(dyn.surfaceElevated)
                                .border(1.dp, dyn.borderMetal, RoundedCornerShape(10.dp))
                                .clickable { pickingStart = true }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Column {
                                Text(trEn("Başlangıç", "Start"), fontSize = 9.sp, color = dyn.textMuted)
                                Text(
                                    text = startTimeText.ifBlank { selectedType.defaultStart },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = dyn.accentCyan
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(dyn.surfaceElevated)
                                .border(1.dp, dyn.borderMetal, RoundedCornerShape(10.dp))
                                .clickable { pickingStart = false }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Column {
                                Text(trEn("Bitiş", "End"), fontSize = 9.sp, color = dyn.textMuted)
                                Text(
                                    text = endTimeText.ifBlank { selectedType.defaultEnd },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = dyn.accentCyan
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Overtime Quick Adder
                Text(
                    text = trEn("Fazla Mesai Saati:", "Overtime Hours:"),
                    color = dyn.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(0.0, 2.0, 4.0, 6.0, 8.0).forEach { hrs ->
                        val isSel = overtime == hrs
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) ShiftOvertimeAmber.copy(alpha = 0.25f) else dyn.surfaceElevated)
                                .border(
                                    1.dp,
                                    if (isSel) ShiftOvertimeAmber else dyn.borderSubtle,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { overtime = hrs }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (hrs == 0.0) trEn("Yok", "None") else "+${hrs.toInt()}$hUnit",
                                color = if (isSel) ShiftOvertimeAmber else dyn.textSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Shift Note input
                Text(
                    text = trEn("Nöbet Notu / Hatırlatıcı:", "Shift Note / Reminder:"),
                    color = dyn.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    placeholder = { Text(trEn("Örn: İcap nöbeti, vizit kontrolü...", "e.g. On-call duty, handover note..."), color = dyn.textMuted, fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = dyn.primaryNeon,
                        unfocusedBorderColor = dyn.borderMetal,
                        focusedTextColor = dyn.textPrimary,
                        unfocusedTextColor = dyn.textPrimary,
                        cursorColor = dyn.primaryNeon
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val savedStart = if (selectedType.isWork) startTimeText.takeIf { it != "-" && it.isNotBlank() } else null
                    val savedEnd = if (selectedType.isWork) endTimeText.takeIf { it != "-" && it.isNotBlank() } else null
                    onSave(selectedType, overtime, noteText, savedStart, savedEnd)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = dyn.primaryNeon),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(trEn("Kaydet", "Save"), color = dyn.textOnPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row {
                if (day.isOverride) {
                    TextButton(onClick = {
                        onReset()
                        onDismiss()
                    }) {
                        Text(trEn("Sıfırla", "Reset"), color = Color(0xFFF87171))
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text(trEn("İptal", "Cancel"), color = dyn.textSecondary)
                }
            }
        }
    )

    pickingStart?.let { isStart ->
        ShiftTimePickerSheet(
            title = if (isStart) trEn("Vardiya Başlangıç Saati", "Shift Start Time") else trEn("Vardiya Bitiş Saati", "Shift End Time"),
            initialTime = if (isStart) startTimeText.ifBlank { selectedType.defaultStart } else endTimeText.ifBlank { selectedType.defaultEnd },
            allow24Hour = true,
            onDismiss = { pickingStart = null },
            onConfirm = { picked ->
                if (isStart) startTimeText = picked else endTimeText = picked
            }
        )
    }
}

@Composable
fun BreakOvertimeDialog(
    onDismiss: () -> Unit,
    onSaveOvertime: (Double) -> Unit
) {
    val dyn = DynamicTheme.colors
    var selectedExtra by remember { mutableDoubleStateOf(2.0) }
    val hUnit = formatHourUnit(LocalIsEnglish.current)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dyn.surfaceCard,
        title = {
            Text(
                text = trEn("Mola & Ek Mesai Girişi", "Break & Overtime Entry"),
                color = dyn.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = trEn("Bugünkü nöbetinize ek mesai süresi ekleyin veya mola durumunuzu kaydedin:", "Add overtime hours to today's shift or record your break:"),
                    color = dyn.textSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1.0, 2.0, 3.0, 4.0, 6.0).forEach { hrs ->
                        val isSel = selectedExtra == hrs
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) (if (dyn.isDark) dyn.primaryDeep else dyn.primaryNeon.copy(alpha = 0.14f)) else dyn.surfaceElevated)
                                .border(1.dp, if (isSel) dyn.primaryNeon else dyn.borderSubtle, RoundedCornerShape(10.dp))
                                .clickable { selectedExtra = hrs }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+${hrs.toInt()}$hUnit",
                                color = if (isSel) dyn.accentCyan else dyn.textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveOvertime(selectedExtra)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = dyn.primaryNeon),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(trEn("Mesaiyi Kaydet", "Save Overtime"), color = dyn.textOnPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(trEn("Vazgeç", "Cancel"), color = dyn.textSecondary)
            }
        }
    )
}

@Composable
fun ShiftNoteDialog(
    initialNote: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val dyn = DynamicTheme.colors
    var note by remember { mutableStateOf(initialNote) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dyn.surfaceCard,
        title = {
            Text(
                text = trEn("Nöbet Notu", "Shift Note"),
                color = dyn.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = trEn("Bu nöbete dair devir bilgisi veya kişisel notunuzu girin:", "Enter handover details or personal notes for this shift:"),
                    color = dyn.textSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text(trEn("Örn: İlaç dolabı kontrol edildi, devir teslim alındı...", "e.g. Equipment checked, handover completed..."), color = dyn.textMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = dyn.primaryNeon,
                        unfocusedBorderColor = dyn.borderMetal,
                        focusedTextColor = dyn.textPrimary,
                        unfocusedTextColor = dyn.textPrimary
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(note)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = dyn.primaryNeon),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(trEn("Notu Kaydet", "Save Note"), color = dyn.textOnPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(trEn("Kapat", "Close"), color = dyn.textSecondary)
            }
        }
    )
}

@Composable
fun UserAvatarImage(
    avatarPath: String,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val customBitmap = remember(avatarPath) {
        if (avatarPath.isNotBlank() && !avatarPath.startsWith("preset:")) {
            try {
                val file = File(avatarPath)
                if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
            } catch (_: Exception) {
                null
            }
        } else null
    }

    if (customBitmap != null) {
        Image(
            bitmap = customBitmap.asImageBitmap(),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        val drawableId = when (avatarPath) {
            "preset:kaan" -> R.drawable.doctor_kaan_avatar_1794486160219
            else -> R.drawable.proshift_user_avatar_1794484704001
        }
        Image(
            painter = painterResource(id = drawableId),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    }
}

fun savePickedAvatarToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val decoded = inputStream.use { BitmapFactory.decodeStream(it) } ?: return null
        val maxDim = 512
        val scale = minOf(
            maxDim.toFloat() / decoded.width.coerceAtLeast(1),
            maxDim.toFloat() / decoded.height.coerceAtLeast(1),
            1f
        )
        val targetW = (decoded.width * scale).toInt().coerceAtLeast(1)
        val targetH = (decoded.height * scale).toInt().coerceAtLeast(1)
        val scaled = if (scale < 1f) {
            Bitmap.createScaledBitmap(decoded, targetW, targetH, true)
        } else {
            decoded
        }
        context.filesDir.listFiles()?.forEach { f ->
            if (f.name.startsWith("user_profile_avatar_") && f.name.endsWith(".jpg")) {
                f.delete()
            }
        }
        val outFile = File(context.filesDir, "user_profile_avatar_${System.currentTimeMillis()}.jpg")
        FileOutputStream(outFile).use { fos ->
            scaled.compress(Bitmap.CompressFormat.JPEG, 90, fos)
        }
        if (scaled !== decoded) decoded.recycle()
        outFile.absolutePath
    } catch (_: Exception) {
        null
    }
}
