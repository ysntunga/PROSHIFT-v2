package com.example.data.engine

import com.example.data.model.CyclePreset
import com.example.data.model.DayOverride
import com.example.data.model.HolidayInfo
import com.example.data.model.ShiftDay
import com.example.data.model.ShiftType
import com.example.data.model.TimesheetStats
import com.example.data.model.UserSettings
import java.time.LocalDate
import java.time.LocalTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Locale

data class ShiftAnchor(
    val date: LocalDate,
    val index: Int
)

/**
 * Pure, timezone- and DST-independent shift scheduling, holiday, and labor-law calculation engine.
 * Contains zero Android framework UI or storage dependencies.
 */
object ShiftEngine {

    private val isoDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ROOT)

    /**
     * Timezone- and DST-independent calendar day number (days since 1970-01-01 UTC).
     */
    fun dayNumber(date: LocalDate): Long = date.toEpochDay()

    fun dayNumber(year: Int, month: Int, dayOfMonth: Int): Long =
        LocalDate.of(year, month, dayOfMonth).toEpochDay()

    /**
     * Euclidean modulo that always returns a non-negative index in [0, length - 1].
     */
    fun safeModulo(value: Long, length: Int): Int {
        val len = length.coerceAtLeast(1)
        val mod = value % len.toLong()
        return if (mod < 0L) (mod + len.toLong()).toInt() else mod.toInt()
    }

    /**
     * Resolves the base ShiftType for [date] from [anchor] and repeating [pattern].
     */
    fun stateFor(
        date: LocalDate,
        anchor: ShiftAnchor,
        pattern: List<ShiftType>
    ): ShiftType {
        if (pattern.isEmpty()) return ShiftType.IZIN
        val deltaDays = dayNumber(date) - dayNumber(anchor.date)
        val idx = safeModulo(deltaDays + anchor.index.toLong(), pattern.size)
        return pattern[idx]
    }

    fun resolvePattern(settings: UserSettings): List<ShiftType> =
        if (settings.cyclePreset == CyclePreset.CUSTOM || !settings.cyclePreset.hasDefaultPattern) {
            settings.customSequence.ifEmpty { settings.cyclePreset.sequence }
        } else {
            settings.cyclePreset.sequence
        }

    fun resolveDefaultTimes(type: ShiftType, settings: UserSettings): Pair<String, String> =
        when (type) {
            ShiftType.GUNDUZ -> settings.gunduzStartTime to settings.gunduzEndTime
            ShiftType.GECE -> settings.geceStartTime to settings.geceEndTime
            ShiftType.SABAH -> settings.sabahStartTime to settings.sabahEndTime
            ShiftType.AKSAM -> settings.aksamStartTime to settings.aksamEndTime
            ShiftType.GECE8 -> settings.gece8StartTime to settings.gece8EndTime
            ShiftType.NOBET24 -> settings.gunduzStartTime to settings.gunduzStartTime
            else -> type.defaultStart to type.defaultEnd
        }

    fun parseTimeSafe(timeStr: String?, fallback: LocalTime): LocalTime {
        if (timeStr.isNullOrBlank() || timeStr == "-") return fallback
        return try {
            val normalized = timeStr.trim()
            if (normalized == "24:00") LocalTime.MIDNIGHT
            else LocalTime.parse(normalized, DateTimeFormatter.ofPattern("H:mm"))
        } catch (_: Exception) {
            fallback
        }
    }

    fun calculateDurationHours(startStr: String, endStr: String, type: ShiftType): Double {
        if (!type.isWork) return 0.0
        if (type == ShiftType.NOBET24 && startStr == endStr) return 24.0
        val start = parseTimeSafe(startStr, parseTimeSafe(type.defaultStart, LocalTime.of(8, 0)))
        val end = parseTimeSafe(endStr, parseTimeSafe(type.defaultEnd, LocalTime.of(20, 0)))
        val startMin = start.hour * 60 + start.minute
        var endMin = end.hour * 60 + end.minute
        if (endMin <= startMin) {
            endMin += 24 * 60
        }
        val diffHours = (endMin - startMin) / 60.0
        return if (diffHours in 0.5..24.0) diffHours else type.defaultDuration
    }

    /**
     * Explicit per-year fixed & religious Turkish holiday lookup table (never algorithmically guessed).
     */
    private val religiousHolidaysByYear: Map<Int, Map<String, HolidayInfo>> = mapOf(
        2024 to mapOf(
            "04-09" to HolidayInfo("Ramazan Bayramı Arifesi", isReligious = true, isHalfDay = true),
            "04-10" to HolidayInfo("Ramazan Bayramı 1. Gün", isReligious = true),
            "04-11" to HolidayInfo("Ramazan Bayramı 2. Gün", isReligious = true),
            "04-12" to HolidayInfo("Ramazan Bayramı 3. Gün", isReligious = true),
            "06-15" to HolidayInfo("Kurban Bayramı Arifesi", isReligious = true, isHalfDay = true),
            "06-16" to HolidayInfo("Kurban Bayramı 1. Gün", isReligious = true),
            "06-17" to HolidayInfo("Kurban Bayramı 2. Gün", isReligious = true),
            "06-18" to HolidayInfo("Kurban Bayramı 3. Gün", isReligious = true),
            "06-19" to HolidayInfo("Kurban Bayramı 4. Gün", isReligious = true)
        ),
        2025 to mapOf(
            "03-29" to HolidayInfo("Ramazan Bayramı Arifesi", isReligious = true, isHalfDay = true),
            "03-30" to HolidayInfo("Ramazan Bayramı 1. Gün", isReligious = true),
            "03-31" to HolidayInfo("Ramazan Bayramı 2. Gün", isReligious = true),
            "04-01" to HolidayInfo("Ramazan Bayramı 3. Gün", isReligious = true),
            "06-05" to HolidayInfo("Kurban Bayramı Arifesi", isReligious = true, isHalfDay = true),
            "06-06" to HolidayInfo("Kurban Bayramı 1. Gün", isReligious = true),
            "06-07" to HolidayInfo("Kurban Bayramı 2. Gün", isReligious = true),
            "06-08" to HolidayInfo("Kurban Bayramı 3. Gün", isReligious = true),
            "06-09" to HolidayInfo("Kurban Bayramı 4. Gün", isReligious = true)
        ),
        2026 to mapOf(
            "03-19" to HolidayInfo("Ramazan Bayramı Arifesi", isReligious = true, isHalfDay = true),
            "03-20" to HolidayInfo("Ramazan Bayramı 1. Gün", isReligious = true),
            "03-21" to HolidayInfo("Ramazan Bayramı 2. Gün", isReligious = true),
            "03-22" to HolidayInfo("Ramazan Bayramı 3. Gün", isReligious = true),
            "05-26" to HolidayInfo("Kurban Bayramı Arifesi", isReligious = true, isHalfDay = true),
            "05-27" to HolidayInfo("Kurban Bayramı 1. Gün", isReligious = true),
            "05-28" to HolidayInfo("Kurban Bayramı 2. Gün", isReligious = true),
            "05-29" to HolidayInfo("Kurban Bayramı 3. Gün", isReligious = true),
            "05-30" to HolidayInfo("Kurban Bayramı 4. Gün", isReligious = true)
        ),
        2027 to mapOf(
            "03-08" to HolidayInfo("Ramazan Bayramı Arifesi", isReligious = true, isHalfDay = true),
            "03-09" to HolidayInfo("Ramazan Bayramı 1. Gün", isReligious = true),
            "03-10" to HolidayInfo("Ramazan Bayramı 2. Gün", isReligious = true),
            "03-11" to HolidayInfo("Ramazan Bayramı 3. Gün", isReligious = true),
            "05-15" to HolidayInfo("Kurban Bayramı Arifesi", isReligious = true, isHalfDay = true),
            "05-16" to HolidayInfo("Kurban Bayramı 1. Gün", isReligious = true),
            "05-17" to HolidayInfo("Kurban Bayramı 2. Gün", isReligious = true),
            "05-18" to HolidayInfo("Kurban Bayramı 3. Gün", isReligious = true),
            "05-19" to HolidayInfo("Kurban Bayramı 4. Gün", isReligious = true)
        ),
        2028 to mapOf(
            "02-26" to HolidayInfo("Ramazan Bayramı Arifesi", isReligious = true, isHalfDay = true),
            "02-27" to HolidayInfo("Ramazan Bayramı 1. Gün", isReligious = true),
            "02-28" to HolidayInfo("Ramazan Bayramı 2. Gün", isReligious = true),
            "02-29" to HolidayInfo("Ramazan Bayramı 3. Gün", isReligious = true),
            "05-04" to HolidayInfo("Kurban Bayramı Arifesi", isReligious = true, isHalfDay = true),
            "05-05" to HolidayInfo("Kurban Bayramı 1. Gün", isReligious = true),
            "05-06" to HolidayInfo("Kurban Bayramı 2. Gün", isReligious = true),
            "05-07" to HolidayInfo("Kurban Bayramı 3. Gün", isReligious = true),
            "05-08" to HolidayInfo("Kurban Bayramı 4. Gün", isReligious = true)
        ),
        2029 to mapOf(
            "02-13" to HolidayInfo("Ramazan Bayramı Arifesi", isReligious = true, isHalfDay = true),
            "02-14" to HolidayInfo("Ramazan Bayramı 1. Gün", isReligious = true),
            "02-15" to HolidayInfo("Ramazan Bayramı 2. Gün", isReligious = true),
            "02-16" to HolidayInfo("Ramazan Bayramı 3. Gün", isReligious = true),
            "04-24" to HolidayInfo("Kurban Bayramı 1. Gün", isReligious = true),
            "04-25" to HolidayInfo("Kurban Bayramı 2. Gün", isReligious = true),
            "04-26" to HolidayInfo("Kurban Bayramı 3. Gün", isReligious = true),
            "04-27" to HolidayInfo("Kurban Bayramı 4. Gün", isReligious = true)
        ),
        2030 to mapOf(
            "02-03" to HolidayInfo("Ramazan Bayramı Arifesi", isReligious = true, isHalfDay = true),
            "02-04" to HolidayInfo("Ramazan Bayramı 1. Gün", isReligious = true),
            "02-05" to HolidayInfo("Ramazan Bayramı 2. Gün", isReligious = true),
            "02-06" to HolidayInfo("Ramazan Bayramı 3. Gün", isReligious = true),
            "04-12" to HolidayInfo("Kurban Bayramı Arifesi", isReligious = true, isHalfDay = true),
            "04-13" to HolidayInfo("Kurban Bayramı 1. Gün", isReligious = true),
            "04-14" to HolidayInfo("Kurban Bayramı 2. Gün", isReligious = true),
            "04-15" to HolidayInfo("Kurban Bayramı 3. Gün", isReligious = true),
            "04-16" to HolidayInfo("Kurban Bayramı 4. Gün", isReligious = true)
        )
    )

    private val fixedHolidaysByMonthDay: Map<String, HolidayInfo> = mapOf(
        "01-01" to HolidayInfo("Yılbaşı", isReligious = false),
        "04-23" to HolidayInfo("23 Nisan Ulusal Egemenlik ve Çocuk Bayramı", isReligious = false),
        "05-01" to HolidayInfo("1 Mayıs Emek ve Dayanışma Günü", isReligious = false),
        "05-19" to HolidayInfo("19 Mayıs Atatürk'ü Anma, Gençlik ve Spor Bayramı", isReligious = false),
        "07-15" to HolidayInfo("15 Temmuz Demokrasi ve Millî Birlik Günü", isReligious = false),
        "08-30" to HolidayInfo("30 Ağustos Zafer Bayramı", isReligious = false),
        "10-28" to HolidayInfo("Cumhuriyet Bayramı Arifesi", isReligious = false, isHalfDay = true),
        "10-29" to HolidayInfo("29 Ekim Cumhuriyet Bayramı", isReligious = false)
    )

    fun holidayForDate(date: LocalDate): HolidayInfo? {
        val mmDd = String.format(Locale.ROOT, "%02d-%02d", date.monthValue, date.dayOfMonth)
        val fixed = fixedHolidaysByMonthDay[mmDd]
        val religious = religiousHolidaysByYear[date.year]?.get(mmDd)
        return when {
            fixed != null && religious != null -> HolidayInfo(
                name = "${fixed.name} & ${religious.name}",
                isReligious = religious.isReligious,
                isHalfDay = fixed.isHalfDay && religious.isHalfDay
            )
            fixed != null -> fixed
            else -> religious
        }
    }

    /**
     * Resolves [date] shift day: resolves base pattern via [anchor], then overlays [overrides] map.
     * Night shifts crossing midnight are always attributed 100% to their START date ([date]).
     */
    fun dayShiftFor(
        date: LocalDate,
        settings: UserSettings,
        overrides: Map<String, DayOverride>
    ): ShiftDay {
        val dateIso = date.format(isoDateFormatter)
        val override = overrides[dateIso]
        val pattern = resolvePattern(settings)
        val anchor = ShiftAnchor(settings.referenceStartDate, settings.initialCycleOffset)

        val type: ShiftType
        val isOverride: Boolean
        val overtimeHours: Double
        val note: String
        val startTime: String
        val endTime: String

        if (override != null) {
            type = override.shiftType
            isOverride = true
            overtimeHours = override.overtimeHours
            note = override.note
            val (defStart, defEnd) = resolveDefaultTimes(type, settings)
            startTime = override.startTime ?: defStart
            endTime = override.endTime ?: defEnd
        } else {
            type = stateFor(date, anchor, pattern)
            isOverride = false
            overtimeHours = 0.0
            note = ""
            val (defStart, defEnd) = resolveDefaultTimes(type, settings)
            startTime = defStart
            endTime = defEnd
        }

        val holiday = if (settings.autoHolidaysEnabled) holidayForDate(date) else null
        val baseHours = if (type.isWork) calculateDurationHours(startTime, endTime, type) else 0.0
        val totalHours = baseHours + overtimeHours

        return ShiftDay(
            date = date,
            type = type,
            startTime = startTime,
            endTime = endTime,
            baseHours = baseHours,
            overtimeHours = overtimeHours,
            totalHours = totalHours,
            note = note,
            isOverride = isOverride,
            holidayName = holiday?.name,
            isHalfDayHoliday = holiday?.isHalfDay == true
        )
    }

    fun buildMonth(
        year: Int,
        month: Int,
        settings: UserSettings,
        overrides: Map<String, DayOverride>
    ): List<ShiftDay> {
        val firstDay = LocalDate.of(year, month, 1)
        val length = firstDay.lengthOfMonth()
        return (1..length).map { day ->
            dayShiftFor(LocalDate.of(year, month, day), settings, overrides)
        }
    }

    /**
     * Turkish Labor Law No. 4857 Art. 53 statutory annual leave days:
     * - < 1 year: 0 days
     * - 1..5 years inclusive: 14 days
     * - 6..14 years inclusive: 20 days
     * - >= 15 years: 26 days
     * - Employees aged <= 18 or >= 50 with >= 1 year of service receive at least 20 days.
     */
    fun annualLeaveDays(
        hireDate: LocalDate?,
        asOfDate: LocalDate = LocalDate.now(),
        birthDate: LocalDate? = null
    ): Int {
        if (hireDate == null || asOfDate.isBefore(hireDate)) return 0
        val seniorityYears = Period.between(hireDate, asOfDate).years.coerceAtLeast(0)
        if (seniorityYears < 1) return 0

        val baseDays = when {
            seniorityYears in 1..5 -> 14
            seniorityYears in 6..14 -> 20
            else -> 26
        }

        if (birthDate != null && !asOfDate.isBefore(birthDate)) {
            val age = Period.between(birthDate, asOfDate).years
            if (age >= 50 || age <= 18) {
                return maxOf(baseDays, 20)
            }
        }
        return baseDays
    }

    /**
     * Computes monthly payroll & leave statistics from resolved [days].
     * All shifts starting inside the month (even if crossing midnight into the next month) are counted in full here.
     */
    fun computeMonthStats(
        days: List<ShiftDay>,
        settings: UserSettings,
        asOfDate: LocalDate = LocalDate.now()
    ): TimesheetStats {
        var totalWork = 0.0
        var nightWork = 0.0
        var totalOvertime = 0.0
        var workDays = 0
        var offDays = 0
        var holidayWorkDays = 0

        for (d in days) {
            if (d.type.isWork) {
                workDays++
                totalWork += d.totalHours
                if (d.type.isNight) {
                    nightWork += d.baseHours
                }
                totalOvertime += d.overtimeHours
                if (d.holidayName != null) {
                    holidayWorkDays++
                }
            } else {
                offDays++
                if (d.overtimeHours > 0) {
                    totalOvertime += d.overtimeHours
                    totalWork += d.overtimeHours
                }
            }
        }

        val firstDate = days.firstOrNull()?.date ?: LocalDate.of(asOfDate.year, asOfDate.monthValue, 1)
        val monthName = firstDate.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("tr-TR")))
        val targetHours = 180.0
        val seniorityYears = Period.between(settings.employmentStartDate, asOfDate).years.coerceAtLeast(0)
        val statutoryRights = annualLeaveDays(
            hireDate = settings.employmentStartDate,
            asOfDate = asOfDate,
            birthDate = settings.birthDate
        )
        val used = settings.usedLeaveDays
        val remaining = (statutoryRights - used).coerceAtLeast(0)

        return TimesheetStats(
            monthTitle = monthName,
            totalWorkHours = totalWork,
            targetWorkHours = targetHours,
            nightHours = nightWork,
            overtimeHours = totalOvertime,
            workDaysCount = workDays,
            offDaysCount = offDays,
            seniorityYears = seniorityYears,
            statutoryLeaveRights = statutoryRights,
            usedLeaveDays = used,
            remainingLeaveDays = remaining,
            holidayWorkDaysCount = holidayWorkDays
        )
    }
}
