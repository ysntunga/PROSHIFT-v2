package com.example.data.model

import java.time.LocalDate

enum class ShiftType(
    val code: String,
    val title: String,
    val shortLabel: String,
    val defaultDuration: Double,
    val defaultStart: String,
    val defaultEnd: String,
    val iconEmoji: String,
    val isWork: Boolean,
    val isNight: Boolean
) {
    GUNDUZ("G", "Gündüz", "12s G", 12.0, "08:00", "20:00", "☀️", true, false),
    GECE("N", "Gece", "12s N", 12.0, "20:00", "08:00", "🌙", true, true),
    IZIN("I", "İzin", "İzin", 0.0, "-", "-", "☕", false, false),
    RAPOR("R", "Rapor", "Rapor", 0.0, "-", "-", "🏥", false, false),
    OZEL("O", "Özel Mesai", "Ek Mesai", 8.0, "08:00", "16:00", "⚡", true, false),
    SABAH("S", "Sabah (8s)", "8s S", 8.0, "08:00", "16:00", "🌅", true, false),
    AKSAM("A", "Akşam (8s)", "8s A", 8.0, "16:00", "24:00", "🌆", true, false),
    GECE8("N8", "Gece (8s)", "8s N", 8.0, "00:00", "08:00", "🌃", true, true),
    NOBET24("24", "24s Nöbet", "24s N", 24.0, "08:00", "08:00", "🛡️", true, true)
}

enum class CyclePreset(
    val id: String,
    val displayName: String,
    val description: String,
    val sequence: List<ShiftType>,
    val hasDefaultPattern: Boolean = true,
    val uiNotice: String? = null
) {
    CYCLE_12_36(
        "12_36",
        "12/36 Sistem",
        "Gündüz, Gece, İzin (12s)",
        listOf(ShiftType.GUNDUZ, ShiftType.GECE, ShiftType.IZIN)
    ),
    CYCLE_2_2_2(
        "2_2_2",
        "2 Gündüz - 2 Gece - 2 İzin",
        "2 Gündüz (12s), 2 Gece (12s), 2 İzin",
        listOf(
            ShiftType.GUNDUZ, ShiftType.GUNDUZ,
            ShiftType.GECE, ShiftType.GECE,
            ShiftType.IZIN, ShiftType.IZIN
        )
    ),
    CYCLE_4_4(
        "4_4",
        "4-4 Sistemi (12s)",
        "4 Gündüz (12s), 4 İzin",
        listOf(
            ShiftType.GUNDUZ, ShiftType.GUNDUZ, ShiftType.GUNDUZ, ShiftType.GUNDUZ,
            ShiftType.IZIN, ShiftType.IZIN, ShiftType.IZIN, ShiftType.IZIN
        )
    ),
    CYCLE_PITMAN(
        "pitman_2_2_3",
        "Pitman / 2-2-3 (12s)",
        "14 günlük döngü: 2G-2İ-3G-2İ-2G-3İ",
        listOf(
            ShiftType.GUNDUZ, ShiftType.GUNDUZ,
            ShiftType.IZIN, ShiftType.IZIN,
            ShiftType.GUNDUZ, ShiftType.GUNDUZ, ShiftType.GUNDUZ,
            ShiftType.IZIN, ShiftType.IZIN,
            ShiftType.GUNDUZ, ShiftType.GUNDUZ,
            ShiftType.IZIN, ShiftType.IZIN, ShiftType.IZIN
        ),
        uiNotice = "Pitman standart şablon — gece bloğu varyantını doğrulayın"
    ),
    CYCLE_12_24_12_48(
        "12_24_12_48",
        "12/24 - 12/48 Sistem",
        "Gündüz, Boş, Gece, Boş",
        listOf(ShiftType.GUNDUZ, ShiftType.IZIN, ShiftType.GECE, ShiftType.IZIN)
    ),
    CYCLE_24_48(
        "24_48",
        "24/48 Nöbet Rejimi",
        "24 Saat Nöbet, 48 Saat İstirahat",
        listOf(ShiftType.NOBET24, ShiftType.IZIN, ShiftType.IZIN)
    ),
    CYCLE_8_2_2_2(
        "8_2_2_2",
        "2-2-2 Üçlü Vardiya (8s)",
        "2 Sabah (08-16), 2 Akşam (16-24), 2 Gece (00-08)",
        listOf(
            ShiftType.SABAH, ShiftType.SABAH,
            ShiftType.AKSAM, ShiftType.AKSAM,
            ShiftType.GECE8, ShiftType.GECE8
        )
    ),
    CYCLE_8_2_2_2_2(
        "8_2_2_2_2",
        "2-2-2-2 Continental (8s)",
        "2 Sabah, 2 Akşam, 2 Gece (00-08), 2 İzin",
        listOf(
            ShiftType.SABAH, ShiftType.SABAH,
            ShiftType.AKSAM, ShiftType.AKSAM,
            ShiftType.GECE8, ShiftType.GECE8,
            ShiftType.IZIN, ShiftType.IZIN
        )
    ),
    CYCLE_8_2_2_2_4(
        "8_2_2_2_4",
        "2-2-2-4 Continental (8s)",
        "2 Sabah, 2 Akşam, 2 Gece (00-08), 4 İzin (10 Gün)",
        listOf(
            ShiftType.SABAH, ShiftType.SABAH,
            ShiftType.AKSAM, ShiftType.AKSAM,
            ShiftType.GECE8, ShiftType.GECE8,
            ShiftType.IZIN, ShiftType.IZIN, ShiftType.IZIN, ShiftType.IZIN
        )
    ),
    CYCLE_SOUTHERN_SWING(
        "southern_swing",
        "Southern Swing (8s, 28 Gün)",
        "7 Sabah, 2 İzin, 7 Akşam, 2 İzin, 7 Gece, 3 İzin",
        listOf(
            ShiftType.SABAH, ShiftType.SABAH, ShiftType.SABAH, ShiftType.SABAH, ShiftType.SABAH, ShiftType.SABAH, ShiftType.SABAH,
            ShiftType.IZIN, ShiftType.IZIN,
            ShiftType.AKSAM, ShiftType.AKSAM, ShiftType.AKSAM, ShiftType.AKSAM, ShiftType.AKSAM, ShiftType.AKSAM, ShiftType.AKSAM,
            ShiftType.IZIN, ShiftType.IZIN,
            ShiftType.GECE8, ShiftType.GECE8, ShiftType.GECE8, ShiftType.GECE8, ShiftType.GECE8, ShiftType.GECE8, ShiftType.GECE8,
            ShiftType.IZIN, ShiftType.IZIN, ShiftType.IZIN
        )
    ),
    CYCLE_8_3(
        "8_3",
        "8 Saatlik 3 Vardiya (1-1-1-1)",
        "Sabah (08-16), Akşam (16-24), Gece (00-08), İzin",
        listOf(ShiftType.SABAH, ShiftType.AKSAM, ShiftType.GECE8, ShiftType.IZIN)
    ),
    CYCLE_DUPONT_TEMPLATE(
        "dupont_12",
        "DuPont (12s, 28 Gün — Özel Düzen)",
        "Varsayılan şablon yok • Kurumunuza göre kendiniz düzenleyin",
        emptyList(),
        hasDefaultPattern = false,
        uiNotice = "DuPont sistemi kurumdan kuruma değişir; düzenleyici ile 28 günlük diziyi oluşturun"
    ),
    CYCLE_FIVE_TEAM_8H(
        "five_team_8",
        "5-Ekip 8 Saat (Özel Düzen)",
        "Varsayılan şablon yok • Ekip rotasyonunuzu kendiniz düzenleyin",
        emptyList(),
        hasDefaultPattern = false,
        uiNotice = "5-Ekip 8 saat sistemi için kurumunuza ait diziyi düzenleyici ile girin"
    ),
    CUSTOM(
        "custom",
        "Özel Döngü Oluşturucu",
        "Kendi belirlediğiniz vardiya dizilimi",
        listOf(ShiftType.GUNDUZ, ShiftType.IZIN, ShiftType.GECE, ShiftType.IZIN),
        hasDefaultPattern = false
    )
}

data class HolidayInfo(
    val name: String,
    val isReligious: Boolean = false,
    val isHalfDay: Boolean = false
)

data class UserProfileSummary(
    val id: String,
    val name: String,
    val department: String,
    val title: String,
    val cyclePresetId: String
)

data class DayOverride(
    val dateIso: String, // yyyy-MM-dd
    val shiftType: ShiftType,
    val overtimeHours: Double = 0.0,
    val note: String = "",
    val startTime: String? = null,
    val endTime: String? = null
)

data class ShiftDay(
    val date: LocalDate,
    val type: ShiftType,
    val startTime: String,
    val endTime: String,
    val baseHours: Double,
    val overtimeHours: Double,
    val totalHours: Double,
    val note: String,
    val isOverride: Boolean,
    val holidayName: String? = null,
    val isHalfDayHoliday: Boolean = false
)

data class DutyTelemetry(
    val isOnDuty: Boolean,
    val currentShiftType: ShiftType?,
    val remainingSeconds: Long,
    val remainingFormatted: String,
    val progressPercent: Float,
    val progressPercentText: String,
    val shiftHoursRange: String,
    val nextShiftTitle: String,
    val nextShiftDetails: String,
    val nextShiftHoursRemaining: String,
    val nextShiftType: ShiftType,
    val isBreakActive: Boolean = false,
    val breakSeconds: Long = 0L,
    val breakFormatted: String = "00:00:00"
)

data class TimesheetStats(
    val monthTitle: String,
    val totalWorkHours: Double,
    val targetWorkHours: Double,
    val nightHours: Double,
    val overtimeHours: Double,
    val workDaysCount: Int,
    val offDaysCount: Int,
    val seniorityYears: Int,
    val statutoryLeaveRights: Int,
    val usedLeaveDays: Int,
    val remainingLeaveDays: Int,
    val holidayWorkDaysCount: Int = 0
)

enum class ProfessionGroup(
    val id: String,
    val displayName: String,
    val emoji: String,
    val titles: List<String>,
    val recommendedPreset: CyclePreset,
    val suggestedDepartments: List<String>,
    val recommendationReason: String
) {
    SAGLIK(
        id = "saglik",
        displayName = "Sağlık & Klinik",
        emoji = "🩺",
        titles = listOf(
            "Acil Tıp Uzmanı",
            "Hemşire",
            "Paramedik / ATT",
            "Asistan Hekim",
            "Ebe",
            "Anestezi Teknikeri",
            "Radyoloji Teknisyeni",
            "Laborant"
        ),
        recommendedPreset = CyclePreset.CYCLE_12_36,
        suggestedDepartments = listOf("Acil Servis", "Yoğun Bakım", "Ameliyathane", "112 KKM"),
        recommendationReason = "Sağlık ve hastane ekiplerinde en yaygın 12/36 nöbet düzeni"
    ),
    GUVENLIK(
        id = "guvenlik",
        displayName = "Güvenlik & Kolluk",
        emoji = "🛡️",
        titles = listOf(
            "Polis Memuru",
            "Komiser",
            "Jandarma Personeli",
            "Özel Güvenlik Görevlisi",
            "İnfaz Koruma Memuru",
            "Gümrük Muhafaza"
        ),
        recommendedPreset = CyclePreset.CYCLE_12_24_12_48,
        suggestedDepartments = listOf("Merkez Nokta", "Site Güvenliği", "Devriye Ekibi", "Kampüs Güvenlik"),
        recommendationReason = "Güvenlik ve kolluk birimleri için standart 12/24 - 12/48 düzeni"
    ),
    ITFAIYE_AFET(
        id = "itfaiye",
        displayName = "İtfaiye & Afet",
        emoji = "🚒",
        titles = listOf(
            "İtfaiye Eri",
            "İtfaiye Çavuşu",
            "AFAD Arama Kurtarma",
            "112 KKM Operatörü"
        ),
        recommendedPreset = CyclePreset.CYCLE_24_48,
        suggestedDepartments = listOf("Merkez İstasyon", "Müdahale Ekibi", "AFAD Ekibi", "112 KKM"),
        recommendationReason = "İtfaiye ve arama kurtarma ekipleri için 24/48 tam nöbet sistemi"
    ),
    SANAYI_URETIM(
        id = "sanayi",
        displayName = "Sanayi & Üretim",
        emoji = "⚙️",
        titles = listOf(
            "Vardiya Amiri",
            "Üretim Operatörü",
            "CNC Teknisyeni",
            "Kalite Kontrol Uzmanı",
            "Bakım Mühendisi"
        ),
        recommendedPreset = CyclePreset.CYCLE_8_2_2_2,
        suggestedDepartments = listOf("Üretim Hattı", "Bakım Atölyesi", "Kalite Kontrol", "CNC Operasyon"),
        recommendationReason = "Fabrika ve üretim hatları için 8 saatlik 2-2-2 üçlü vardiya"
    ),
    LOJISTIK_ULASIM(
        id = "lojistik",
        displayName = "Lojistik & Ulaşım",
        emoji = "✈️",
        titles = listOf(
            "Havalimanı Yer Hizmetleri",
            "Hava Trafik / Kule",
            "Demiryolu Hareket Memuru",
            "Lojistik Vardiya Şefi",
            "Ağır Vasıta / Kaptan"
        ),
        recommendedPreset = CyclePreset.CYCLE_8_2_2_2_4,
        suggestedDepartments = listOf("Havalimanı Apron", "Operasyon Merkezi", "Depo & Sevkiyat", "Hat Kontrol"),
        recommendationReason = "Ulaşım ve lojistik operasyonları için 2-2-2-4 Continental düzeni"
    ),
    OTEL_HIZMET(
        id = "hizmet",
        displayName = "Turizm & Hizmet",
        emoji = "🏨",
        titles = listOf(
            "Gece Müdürü / Resepsiyon",
            "Çağrı Merkezi Uzmanı",
            "Veri Merkezi Operatörü",
            "Teknik Destek Nöbetçisi"
        ),
        recommendedPreset = CyclePreset.CYCLE_8_3,
        suggestedDepartments = listOf("Ön Büro / Resepsiyon", "Çağrı Merkezi", "Veri Merkezi", "Teknik Destek"),
        recommendationReason = "7/24 hizmet ve operasyon merkezleri için 8 saatlik 3 vardiya"
    );

    companion object {
        fun fromId(id: String): ProfessionGroup =
            entries.find { it.id == id } ?: SAGLIK
    }
}

data class UserSettings(
    val employeeName: String = "Dr. Selin Yılmaz",
    val department: String = "İstanbul Acil Servis & 112 KKM",
    val professionGroup: String = ProfessionGroup.SAGLIK.id,
    val title: String = "Acil Tıp Uzmanı",
    val avatarUriOrPath: String = "",
    val cyclePreset: CyclePreset = CyclePreset.CYCLE_12_36,
    val referenceStartDate: LocalDate = LocalDate.of(2026, 11, 1),
    val customSequence: List<ShiftType> = listOf(ShiftType.GUNDUZ, ShiftType.GECE, ShiftType.IZIN),
    val initialCycleOffset: Int = 0,
    val employmentStartDate: LocalDate = LocalDate.of(2020, 7, 1),
    val usedLeaveDays: Int = 6,
    val shiftReminderEnabled: Boolean = true,
    val shiftReminderTime: String = "20:00",
    val preShiftAlertEnabled: Boolean = true,
    val preShiftAlertMinutes: Int = 120,
    val nightDndEnabled: Boolean = true,
    val calendarSyncEnabled: Boolean = true,
    val autoHolidaysEnabled: Boolean = true,
    val showFriendOnCalendar: Boolean = true,
    val colorLayerStyle: Int = 1,
    val commonLeaveAlertEnabled: Boolean = true,
    val hourlyWage: Double = 355.0,
    val isPinSecurityEnabled: Boolean = false,
    val pinCode: String = "1234",
    val gunduzStartTime: String = "08:00",
    val gunduzEndTime: String = "20:00",
    val geceStartTime: String = "20:00",
    val geceEndTime: String = "08:00",
    val sabahStartTime: String = "08:00",
    val sabahEndTime: String = "16:00",
    val aksamStartTime: String = "16:00",
    val aksamEndTime: String = "24:00",
    val gece8StartTime: String = "00:00",
    val gece8EndTime: String = "08:00",
    val birthDate: LocalDate? = null,
    val isAnchorConfigured: Boolean = true,
    val isOnboardingCompleted: Boolean = false,
    val appLanguage: String = "system",
    val themeMode: String = AppThemeMode.DARK.code
)

enum class AppThemeMode(val code: String) {
    DARK("dark"),
    LIGHT("light");

    companion object {
        fun fromCode(code: String?): AppThemeMode = when (code?.trim()?.lowercase()) {
            LIGHT.code -> LIGHT
            else -> DARK
        }
    }
}

enum class MatchType {
    COMMON_OFF,       // İkisi de İzinli
    COMMON_DUTY,      // İkisi de Aynı Nöbette
    SWAP_OPPORTUNITY  // Biri İzinli, Biri Nöbette (Takas Fırsatı)
}

data class ShiftMatchDay(
    val date: LocalDate,
    val userShift: ShiftType,
    val colleagueShift: ShiftType,
    val matchType: MatchType,
    val note: String
)

data class ColleagueProfile(
    val name: String,
    val department: String,
    val cyclePreset: CyclePreset,
    val referenceStartDate: LocalDate,
    val qrCodeId: String = "PRSH-P2P",
    val customSequence: List<ShiftType>? = null,
    val initialCycleOffset: Int = 0
)

enum class SwapCategory {
    INCOMING,
    OPEN_POOL,
    HISTORY
}

data class SwapRequestItem(
    val id: String,
    val colleagueName: String,
    val colleagueRole: String,
    val category: SwapCategory,
    val matchScorePercent: Int,
    val giveDate: LocalDate,
    val giveShiftType: ShiftType,
    val receiveDate: LocalDate,
    val receiveShiftType: ShiftType,
    val message: String,
    val badgeText: String,
    val timeAgo: String,
    val isTransferOnly: Boolean = false,
    val statusText: String = "Bekliyor"
)

sealed class QrParseResult {
    data class Success(
        val colleague: ColleagueProfile,
        val payloadVersion: String,
        val normalizedUri: String,
        val warningMessage: String? = null
    ) : QrParseResult()

    data class Failure(
        val errorCode: String,
        val errorMessage: String,
        val rawPreview: String
    ) : QrParseResult()
}

data class QrAutoSyncReceipt(
    val colleague: ColleagueProfile,
    val sourceLabel: String,
    val timestampMs: Long,
    val commonOffCount: Int,
    val appliedAsOwnShift: Boolean = false
)

enum class QrCameraPermissionState(val label: String) {
    GRANTED("VERİLDİ"),
    NOT_REQUESTED("İZİN BEKLİYOR"),
    RATIONALE_NEEDED("REDDEDİLDİ (TEKRAR İSTENEBİLİR)"),
    PERMANENTLY_DENIED("KALICI RED (AYARLAR GEREKLİ)")
}

