package com.example.ui.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.data.model.CyclePreset
import com.example.data.model.DutyTelemetry
import com.example.data.model.MatchType
import com.example.data.model.ProfessionGroup
import com.example.data.model.QrCameraPermissionState
import com.example.data.model.ShiftMatchDay
import com.example.data.model.ShiftType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class AppLanguage(val code: String) {
    SYSTEM("system"),
    TURKISH("tr"),
    ENGLISH("en");

    companion object {
        val TR_LOCALE: Locale = Locale.forLanguageTag("tr-TR")
        val EN_LOCALE: Locale = Locale.ENGLISH

        fun fromCode(code: String?): AppLanguage =
            entries.find { it.code.equals(code?.trim(), ignoreCase = true) } ?: SYSTEM

        fun resolveIsEnglish(code: String?, systemLocale: Locale = Locale.getDefault()): Boolean {
            return when (fromCode(code)) {
                TURKISH -> false
                ENGLISH -> true
                SYSTEM -> !systemLocale.language.lowercase(Locale.ROOT).startsWith("tr")
            }
        }

        fun resolveLocale(code: String?, systemLocale: Locale = Locale.getDefault()): Locale =
            if (resolveIsEnglish(code, systemLocale)) EN_LOCALE else TR_LOCALE
    }
}

val LocalIsEnglish = staticCompositionLocalOf { false }
val LocalAppLocale = staticCompositionLocalOf { AppLanguage.TR_LOCALE }

@Composable
fun trEn(tr: String, en: String): String = if (LocalIsEnglish.current) en else tr

fun trEn(isEn: Boolean, tr: String, en: String): String = if (isEn) en else tr

fun ShiftType.localizedTitle(isEn: Boolean): String = if (!isEn) title else when (this) {
    ShiftType.GUNDUZ -> "Day"
    ShiftType.GECE -> "Night"
    ShiftType.IZIN -> "Off"
    ShiftType.RAPOR -> "Sick Leave"
    ShiftType.OZEL -> "Overtime"
    ShiftType.SABAH -> "Morning (8h)"
    ShiftType.AKSAM -> "Evening (8h)"
    ShiftType.GECE8 -> "Night (8h)"
    ShiftType.NOBET24 -> "24h Duty"
}

fun ShiftType.localizedShortLabel(isEn: Boolean): String = if (!isEn) shortLabel else when (this) {
    ShiftType.GUNDUZ -> "12h D"
    ShiftType.GECE -> "12h N"
    ShiftType.IZIN -> "Off"
    ShiftType.RAPOR -> "Sick"
    ShiftType.OZEL -> "OT"
    ShiftType.SABAH -> "8h M"
    ShiftType.AKSAM -> "8h E"
    ShiftType.GECE8 -> "8h N"
    ShiftType.NOBET24 -> "24h D"
}

fun ShiftType.localizedCode(isEn: Boolean): String = if (!isEn) code else when (this) {
    ShiftType.GUNDUZ -> "D"
    ShiftType.GECE -> "N"
    ShiftType.IZIN -> "O"
    ShiftType.RAPOR -> "S"
    ShiftType.OZEL -> "OT"
    ShiftType.SABAH -> "M"
    ShiftType.AKSAM -> "E"
    ShiftType.GECE8 -> "N8"
    ShiftType.NOBET24 -> "24"
}

fun CyclePreset.localizedName(isEn: Boolean): String = if (!isEn) displayName else when (this) {
    CyclePreset.CYCLE_12_36 -> "12/36 System"
    CyclePreset.CYCLE_2_2_2 -> "2 Day - 2 Night - 2 Off"
    CyclePreset.CYCLE_4_4 -> "4-4 System (12h)"
    CyclePreset.CYCLE_PITMAN -> "Pitman / 2-2-3 (12h)"
    CyclePreset.CYCLE_12_24_12_48 -> "12/24 - 12/48 System"
    CyclePreset.CYCLE_24_48 -> "24/48 Duty Cycle"
    CyclePreset.CYCLE_8_2_2_2 -> "2-2-2 Triple Shift (8h)"
    CyclePreset.CYCLE_8_2_2_2_2 -> "2-2-2-2 Continental (8h)"
    CyclePreset.CYCLE_8_2_2_2_4 -> "2-2-2-4 Continental (8h)"
    CyclePreset.CYCLE_SOUTHERN_SWING -> "Southern Swing (8h, 28d)"
    CyclePreset.CYCLE_8_3 -> "8-Hour 3-Shift (1-1-1-1)"
    CyclePreset.CYCLE_DUPONT_TEMPLATE -> "DuPont (12h, 28d — Custom)"
    CyclePreset.CYCLE_FIVE_TEAM_8H -> "5-Team 8-Hour (Custom)"
    CyclePreset.CUSTOM -> "Custom Cycle Builder"
}

fun CyclePreset.localizedDescription(isEn: Boolean): String = if (!isEn) description else when (this) {
    CyclePreset.CYCLE_12_36 -> "Day, Night, Off (12h)"
    CyclePreset.CYCLE_2_2_2 -> "2 Day (12h), 2 Night (12h), 2 Off"
    CyclePreset.CYCLE_4_4 -> "4 Day (12h), 4 Off"
    CyclePreset.CYCLE_PITMAN -> "14-day cycle: 2D-2O-3D-2O-2D-3O"
    CyclePreset.CYCLE_12_24_12_48 -> "Day, Off, Night, Off"
    CyclePreset.CYCLE_24_48 -> "24h Duty, 48h Rest"
    CyclePreset.CYCLE_8_2_2_2 -> "2 Morning (08-16), 2 Evening (16-24), 2 Night (00-08)"
    CyclePreset.CYCLE_8_2_2_2_2 -> "2 Morning, 2 Evening, 2 Night (00-08), 2 Off"
    CyclePreset.CYCLE_8_2_2_2_4 -> "2 Morning, 2 Evening, 2 Night (00-08), 4 Off (10 Days)"
    CyclePreset.CYCLE_SOUTHERN_SWING -> "7 Morning, 2 Off, 7 Evening, 2 Off, 7 Night, 3 Off"
    CyclePreset.CYCLE_8_3 -> "Morning (08-16), Evening (16-24), Night (00-08), Off"
    CyclePreset.CYCLE_DUPONT_TEMPLATE -> "No default pattern • Customize for your workplace"
    CyclePreset.CYCLE_FIVE_TEAM_8H -> "No default pattern • Customize your team rotation"
    CyclePreset.CUSTOM -> "Your own custom shift sequence"
}

fun CyclePreset.localizedNotice(isEn: Boolean): String? = if (!isEn) uiNotice else when (this) {
    CyclePreset.CYCLE_PITMAN -> "Standard Pitman template — verify your night block variant"
    CyclePreset.CYCLE_DUPONT_TEMPLATE -> "DuPont varies by site; build your 28-day sequence in the editor"
    CyclePreset.CYCLE_FIVE_TEAM_8H -> "Enter your workplace 5-team 8-hour sequence in the editor"
    else -> uiNotice
}

fun ProfessionGroup.localizedName(isEn: Boolean): String = if (!isEn) displayName else when (this) {
    ProfessionGroup.SAGLIK -> "Healthcare & Clinical"
    ProfessionGroup.GUVENLIK -> "Security & Law"
    ProfessionGroup.ITFAIYE_AFET -> "Fire & Rescue"
    ProfessionGroup.SANAYI_URETIM -> "Industry & Manufacturing"
    ProfessionGroup.LOJISTIK_ULASIM -> "Logistics & Aviation"
    ProfessionGroup.OTEL_HIZMET -> "Hospitality & Services"
}

fun ProfessionGroup.localizedReason(isEn: Boolean): String = if (!isEn) recommendationReason else when (this) {
    ProfessionGroup.SAGLIK -> "Most common 12/36 shift schedule for clinical & hospital teams"
    ProfessionGroup.GUVENLIK -> "Standard 12/24 - 12/48 rotation for security and patrol units"
    ProfessionGroup.ITFAIYE_AFET -> "Full 24/48 duty system for fire & emergency rescue teams"
    ProfessionGroup.SANAYI_URETIM -> "8-hour 2-2-2 triple shift for factory and production lines"
    ProfessionGroup.LOJISTIK_ULASIM -> "2-2-2-4 Continental schedule for aviation and logistics"
    ProfessionGroup.OTEL_HIZMET -> "8-hour 3-shift schedule for 24/7 operations and service desks"
}

fun ProfessionGroup.localizedTitles(isEn: Boolean): List<String> = if (!isEn) titles else when (this) {
    ProfessionGroup.SAGLIK -> listOf(
        "Emergency Physician",
        "Registered Nurse",
        "Paramedic / EMT",
        "Resident Doctor",
        "Midwife",
        "Anesthesia Technician",
        "Radiology Technician",
        "Lab Technician"
    )
    ProfessionGroup.GUVENLIK -> listOf(
        "Police Officer",
        "Inspector",
        "Gendarmerie",
        "Security Officer",
        "Corrections Officer",
        "Customs Enforcement"
    )
    ProfessionGroup.ITFAIYE_AFET -> listOf(
        "Firefighter",
        "Fire Sergeant",
        "Search & Rescue",
        "911 / 112 Dispatcher"
    )
    ProfessionGroup.SANAYI_URETIM -> listOf(
        "Shift Supervisor",
        "Production Operator",
        "CNC Technician",
        "Quality Control Specialist",
        "Maintenance Engineer"
    )
    ProfessionGroup.LOJISTIK_ULASIM -> listOf(
        "Airport Ground Services",
        "Air Traffic Controller",
        "Rail Dispatcher",
        "Logistics Shift Lead",
        "Heavy Fleet / Captain"
    )
    ProfessionGroup.OTEL_HIZMET -> listOf(
        "Night Manager / Reception",
        "Call Center Specialist",
        "Data Center Operator",
        "On-Call Support Engineer"
    )
}

fun ProfessionGroup.localizedDepartments(isEn: Boolean): List<String> = if (!isEn) suggestedDepartments else when (this) {
    ProfessionGroup.SAGLIK -> listOf("Emergency Dept", "Intensive Care", "Operating Room", "Dispatch Center")
    ProfessionGroup.GUVENLIK -> listOf("Central Post", "Site Security", "Patrol Unit", "Campus Security")
    ProfessionGroup.ITFAIYE_AFET -> listOf("Central Station", "Response Unit", "Rescue Team", "Dispatch Center")
    ProfessionGroup.SANAYI_URETIM -> listOf("Production Line", "Maintenance Shop", "Quality Control", "CNC Ops")
    ProfessionGroup.LOJISTIK_ULASIM -> listOf("Airport Apron", "Operations Center", "Warehouse & Dispatch", "Line Control")
    ProfessionGroup.OTEL_HIZMET -> listOf("Front Desk", "Call Center", "Data Center", "Tech Support")
}

fun QrCameraPermissionState.localizedLabel(isEn: Boolean): String = if (!isEn) label else when (this) {
    QrCameraPermissionState.GRANTED -> "GRANTED"
    QrCameraPermissionState.NOT_REQUESTED -> "PENDING PERMISSION"
    QrCameraPermissionState.RATIONALE_NEEDED -> "DENIED (CAN REQUEST AGAIN)"
    QrCameraPermissionState.PERMANENTLY_DENIED -> "PERMANENTLY DENIED (OPEN SETTINGS)"
}

fun DutyTelemetry.localizedShiftHoursRange(isEn: Boolean): String {
    if (!isEn) return shiftHoursRange
    return if (shiftHoursRange.startsWith("İzin Günü")) {
        shiftHoursRange.replace("İzin Günü • Sonraki:", "Off Day • Next:")
    } else {
        shiftHoursRange.replace(Regex("\\((\\d+)s\\)"), "($1h)")
    }
}

fun DutyTelemetry.localizedNextShiftTitle(isEn: Boolean): String {
    if (!isEn) return nextShiftTitle
    return when (nextShiftTitle) {
        "Bugün" -> "Today"
        "Yarın" -> "Tomorrow"
        else -> {
            // Translate Turkish month names in "d MMMM"
            val trToEnMonths = mapOf(
                "Ocak" to "Jan", "Şubat" to "Feb", "Mart" to "Mar", "Nisan" to "Apr",
                "Mayıs" to "May", "Haziran" to "Jun", "Temmuz" to "Jul", "Ağustos" to "Aug",
                "Eylül" to "Sep", "Ekim" to "Oct", "Kasım" to "Nov", "Aralık" to "Dec"
            )
            var out = nextShiftTitle
            trToEnMonths.forEach { (trM, enM) -> out = out.replace(trM, enM) }
            out
        }
    }
}

fun DutyTelemetry.localizedNextShiftDetails(isEn: Boolean): String {
    if (!isEn) return nextShiftDetails
    val rangePart = nextShiftDetails.substringBefore(" (")
    return "$rangePart (${nextShiftType.localizedTitle(true)})"
}

fun DutyTelemetry.localizedNextShiftHoursRemaining(isEn: Boolean): String {
    if (!isEn) return nextShiftHoursRemaining
    return nextShiftHoursRemaining
        .replace(" dk kaldı", "m left")
        .replace("s kaldı", "h left")
}

fun ShiftMatchDay.localizedNote(colleagueName: String, isEn: Boolean): String {
    if (!isEn) return note
    return when {
        !userShift.isWork && !colleagueShift.isWork -> "Full day off together"
        userShift.isWork && colleagueShift.isWork && userShift == colleagueShift ->
            "Same shift on duty together (${userShift.localizedTitle(true)})"
        !userShift.isWork && colleagueShift.isWork ->
            "You're off, $colleagueName is on duty (Swap opportunity)"
        userShift.isWork && !colleagueShift.isWork ->
            "You're on duty, $colleagueName is off (Opposite shift)"
        else ->
            "Different shift rotation (${userShift.localizedTitle(true)} / ${colleagueShift.localizedTitle(true)})"
    }
}

fun formatMonthTitleLocalized(year: Int, month: Int, locale: Locale): String {
    val d = LocalDate.of(year, month, 1)
    return d.format(DateTimeFormatter.ofPattern("MMMM yyyy", locale))
}

fun formatHourUnit(isEn: Boolean): String = if (isEn) "h" else "s"
fun formatCurrencyUnit(isEn: Boolean): String = if (isEn) "$/h" else "₺/sa"
fun formatPercent(pct: Int, isEn: Boolean): String = if (isEn) "$pct%" else "%$pct"
