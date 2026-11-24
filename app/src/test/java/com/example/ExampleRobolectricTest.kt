package com.example

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.core.view.WindowCompat
import androidx.test.core.app.ApplicationProvider
import com.example.data.engine.QrCodeCodec
import com.example.data.engine.ShiftAnchor
import com.example.data.engine.ShiftEngine
import com.example.data.model.ColleagueProfile
import com.example.data.model.CyclePreset
import com.example.data.model.DayOverride
import com.example.data.model.ProfessionGroup
import com.example.data.model.QrParseResult
import com.example.data.model.ShiftType
import com.example.data.model.UserSettings
import com.example.ui.components.formatHourMinute
import com.example.ui.components.parseHourMinute
import com.example.ui.viewmodel.ShiftViewModel
import com.example.data.repository.ShiftRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.LocalDateTime

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("PROSHIFT", appName)
    }

    @Test
    fun `calculateDurationHours handles daytime, overnight and 24h shifts`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = ShiftRepository(context)
        repo.resetToDefaults()

        assertEquals(12.0, repo.calculateDurationHours("08:00", "20:00", ShiftType.GUNDUZ), 0.01)
        assertEquals(10.5, repo.calculateDurationHours("08:30", "19:00", ShiftType.GUNDUZ), 0.01)
        assertEquals(11.0, repo.calculateDurationHours("21:00", "08:00", ShiftType.GECE), 0.01)
        assertEquals(24.0, repo.calculateDurationHours("08:00", "08:00", ShiftType.NOBET24), 0.01)
    }

    @Test
    fun `custom shift times and initialCycleOffset propagate to daily schedule and telemetry`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = ShiftRepository(context)
        repo.resetToDefaults()

        val refDate = LocalDate.of(2026, 11, 10)
        // CYCLE_12_36 sequence = [GUNDUZ, GECE, IZIN]
        // initialCycleOffset = 1 => refDate becomes GECE
        val customSettings = repo.userSettings.value.copy(
            cyclePreset = CyclePreset.CYCLE_12_36,
            referenceStartDate = refDate,
            initialCycleOffset = 1,
            gunduzStartTime = "09:00",
            gunduzEndTime = "21:00",
            geceStartTime = "21:00",
            geceEndTime = "07:00"
        )
        repo.updateSettings(customSettings)

        val day0 = repo.getShiftForDate(refDate)
        assertEquals(ShiftType.GECE, day0.type)
        assertEquals("21:00", day0.startTime)
        assertEquals("07:00", day0.endTime)
        assertEquals(10.0, day0.baseHours, 0.01)

        val day1 = repo.getShiftForDate(refDate.plusDays(1))
        assertEquals(ShiftType.IZIN, day1.type)
        assertEquals(0.0, day1.baseHours, 0.01)

        val day2 = repo.getShiftForDate(refDate.plusDays(2))
        assertEquals(ShiftType.GUNDUZ, day2.type)
        assertEquals("09:00", day2.startTime)
        assertEquals("21:00", day2.endTime)
        assertEquals(12.0, day2.baseHours, 0.01)

        // Check telemetry during active night shift at 23:00 on refDate
        val activeTelemetry = repo.calculateDutyTelemetry(LocalDateTime.of(2026, 11, 10, 23, 0))
        assertTrue(activeTelemetry.isOnDuty)
        assertEquals(ShiftType.GECE, activeTelemetry.currentShiftType)
        assertEquals("21:00 - 07:00 (10s)", activeTelemetry.shiftHoursRange)
        assertEquals("08:00:00", activeTelemetry.remainingFormatted)

        // Check telemetry during off-duty morning on IZIN day (2026-11-11 10:00) -> next shift is 2026-11-12 09:00
        val offTelemetry = repo.calculateDutyTelemetry(LocalDateTime.of(2026, 11, 11, 10, 0))
        assertFalse(offTelemetry.isOnDuty)
        assertEquals(ShiftType.GUNDUZ, offTelemetry.nextShiftType)
        assertTrue(offTelemetry.nextShiftDetails.contains("09:00 - 21:00"))
    }

    @Test
    fun `export and import backup json roundtrip preserves settings`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = ShiftRepository(context)
        repo.resetToDefaults()

        repo.updateSettings(
            repo.userSettings.value.copy(
                employeeName = "Dr. Ayşe Kaya",
                department = "Kardiyoloji Yoğun Bakım",
                hourlyWage = 480.0,
                gunduzStartTime = "07:30",
                gunduzEndTime = "19:30"
            )
        )

        val backup = repo.exportFullBackupJson()
        repo.resetToDefaults()
        assertEquals("Dr. Selin Yılmaz", repo.userSettings.value.employeeName)

        val restored = repo.importFullBackupJson(backup)
        assertTrue(restored)
        assertEquals("Dr. Ayşe Kaya", repo.userSettings.value.employeeName)
        assertEquals("Kardiyoloji Yoğun Bakım", repo.userSettings.value.department)
        assertEquals("07:30", repo.userSettings.value.gunduzStartTime)
        assertEquals(480.0, repo.userSettings.value.hourlyWage, 0.01)
    }

    @Test
    fun `colleague shift matching detects common off and duty days accurately`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = ShiftRepository(context)
        repo.resetToDefaults()

        assertTrue(repo.colleagues.value.isEmpty())

        val colleague = ColleagueProfile(
            name = "Emre Yılmaz",
            department = "Acil Servis",
            cyclePreset = CyclePreset.CYCLE_12_36,
            referenceStartDate = LocalDate.of(2026, 11, 1),
            qrCodeId = "PRSH-EMRE-TEST"
        )
        repo.addOrUpdateColleague(colleague)

        val refDate = colleague.referenceStartDate
        val shiftOnRef = repo.getColleagueShiftForDate(colleague, refDate)
        assertEquals(colleague.cyclePreset.sequence.first(), shiftOnRef)

        val matches = repo.calculateShiftMatching(colleague, 2026, 11, repo.userSettings.value)
        assertEquals(30, matches.size)
    }

    @Test
    fun `profession group, title and avatar path persist across repository reload and backup`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = ShiftRepository(context)
        repo.resetToDefaults()

        val targetGroup = ProfessionGroup.GUVENLIK
        val chosenTitle = targetGroup.titles.first()
        repo.updateSettings(
            repo.userSettings.value.copy(
                professionGroup = targetGroup.id,
                title = chosenTitle,
                avatarUriOrPath = "preset:kaan"
            )
        )

        val reloadedRepo = ShiftRepository(context)
        assertEquals(targetGroup.id, reloadedRepo.userSettings.value.professionGroup)
        assertEquals(chosenTitle, reloadedRepo.userSettings.value.title)
        assertEquals("preset:kaan", reloadedRepo.userSettings.value.avatarUriOrPath)
        assertEquals(ProfessionGroup.GUVENLIK, ProfessionGroup.fromId(reloadedRepo.userSettings.value.professionGroup))

        val backup = reloadedRepo.exportFullBackupJson()
        reloadedRepo.resetToDefaults()
        assertEquals(ProfessionGroup.SAGLIK.id, reloadedRepo.userSettings.value.professionGroup)

        assertTrue(reloadedRepo.importFullBackupJson(backup))
        assertEquals(targetGroup.id, reloadedRepo.userSettings.value.professionGroup)
        assertEquals(chosenTitle, reloadedRepo.userSettings.value.title)
    }

    @Test
    fun `onboarding completion starts false on fresh install and persists true across reloads`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = ShiftRepository(context)
        repo.resetToDefaults()

        assertFalse(repo.userSettings.value.isOnboardingCompleted)

        repo.updateSettings(repo.userSettings.value.copy(isOnboardingCompleted = true))
        val reloadedRepo = ShiftRepository(context)
        assertTrue(reloadedRepo.userSettings.value.isOnboardingCompleted)
    }

    @Test
    fun `8-hour 2-2-2 triple shift cycle schedules 08-16, 16-24 and 00-08 accurately`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = ShiftRepository(context)
        repo.resetToDefaults()

        val refDate = LocalDate.of(2026, 11, 2)
        // Ensure custom 12h gunduz/gece hours do not corrupt 8-hour SABAH/AKSAM/GECE8 shifts
        repo.updateSettings(
            repo.userSettings.value.copy(
                cyclePreset = CyclePreset.CYCLE_8_2_2_2,
                referenceStartDate = refDate,
                initialCycleOffset = 0,
                gunduzStartTime = "09:00",
                gunduzEndTime = "21:00",
                geceStartTime = "21:00",
                geceEndTime = "09:00"
            )
        )

        val expected = listOf(
            ShiftType.SABAH to ("08:00" to "16:00"),
            ShiftType.SABAH to ("08:00" to "16:00"),
            ShiftType.AKSAM to ("16:00" to "24:00"),
            ShiftType.AKSAM to ("16:00" to "24:00"),
            ShiftType.GECE8 to ("00:00" to "08:00"),
            ShiftType.GECE8 to ("00:00" to "08:00"),
            ShiftType.SABAH to ("08:00" to "16:00")
        )

        expected.forEachIndexed { offsetDays, (expectedType, expectedHours) ->
            val shift = repo.getShiftForDate(refDate.plusDays(offsetDays.toLong()))
            assertEquals("Day $offsetDays shift type", expectedType, shift.type)
            assertEquals("Day $offsetDays start", expectedHours.first, shift.startTime)
            assertEquals("Day $offsetDays end", expectedHours.second, shift.endTime)
            assertEquals("Day $offsetDays baseHours", 8.0, shift.baseHours, 0.01)
        }

        val morningTelemetry = repo.calculateDutyTelemetry(LocalDateTime.of(2026, 11, 2, 10, 30))
        assertTrue(morningTelemetry.isOnDuty)
        assertEquals(ShiftType.SABAH, morningTelemetry.currentShiftType)

        val eveningTelemetry = repo.calculateDutyTelemetry(LocalDateTime.of(2026, 11, 4, 22, 0))
        assertTrue(eveningTelemetry.isOnDuty)
        assertEquals(ShiftType.AKSAM, eveningTelemetry.currentShiftType)

        val nightTelemetry = repo.calculateDutyTelemetry(LocalDateTime.of(2026, 11, 6, 2, 0))
        assertTrue(nightTelemetry.isOnDuty)
        assertEquals(ShiftType.GECE8, nightTelemetry.currentShiftType)
    }

    @Test
    fun `sync payload v2 encodes and decodes custom 8h cycle, offset and colleague persistence`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = ShiftRepository(context)
        repo.resetToDefaults()

        val custom8hSeq = listOf(
            ShiftType.SABAH, ShiftType.SABAH,
            ShiftType.AKSAM, ShiftType.AKSAM,
            ShiftType.GECE8, ShiftType.GECE8
        )
        val settings = repo.userSettings.value.copy(
            employeeName = "Ahmet Çelik",
            department = "Üretim Hattı B",
            cyclePreset = CyclePreset.CUSTOM,
            customSequence = custom8hSeq,
            referenceStartDate = LocalDate.of(2026, 11, 5),
            initialCycleOffset = 2
        )

        val payload = repo.generateSyncPayload(settings)
        val wrapped = "Selam, PROSHIFT vardiya kodum: $payload teşekkürler"
        val parsed = repo.parseSyncPayload(wrapped)

        assertNotNull(parsed)
        val profile = parsed!!
        assertEquals("Ahmet Çelik", profile.name)
        assertEquals("Üretim Hattı B", profile.department)
        assertEquals(CyclePreset.CUSTOM, profile.cyclePreset)
        assertEquals(custom8hSeq, profile.customSequence)
        assertEquals(LocalDate.of(2026, 11, 5), profile.referenceStartDate)
        assertEquals(2, profile.initialCycleOffset)

        // Offset 2 on [SABAH, SABAH, AKSAM, AKSAM, GECE8, GECE8] => refDate is AKSAM
        assertEquals(ShiftType.AKSAM, repo.getColleagueShiftForDate(profile, LocalDate.of(2026, 11, 5)))
        assertEquals(ShiftType.GECE8, repo.getColleagueShiftForDate(profile, LocalDate.of(2026, 11, 7)))

        repo.addOrUpdateColleague(profile)
        val reloaded = ShiftRepository(context)
        val savedColleague = reloaded.colleagues.value.firstOrNull { it.name == "Ahmet Çelik" }
        assertNotNull(savedColleague)
        assertEquals(2, savedColleague!!.initialCycleOffset)
        assertEquals(custom8hSeq, savedColleague.customSequence)
    }

    @Test
    fun `custom 8h Sabah Aksam Gece8 hours and 24_00 support propagate across schedule, backup and telemetry`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = ShiftRepository(context)
        repo.resetToDefaults()

        val refDate = LocalDate.of(2026, 11, 2)
        repo.updateSettings(
            repo.userSettings.value.copy(
                cyclePreset = CyclePreset.CYCLE_8_2_2_2,
                referenceStartDate = refDate,
                initialCycleOffset = 0,
                sabahStartTime = "07:30",
                sabahEndTime = "15:30",
                aksamStartTime = "16:00",
                aksamEndTime = "24:00",
                gece8StartTime = "24:00",
                gece8EndTime = "08:00"
            )
        )

        val day0 = repo.getShiftForDate(refDate)
        assertEquals(ShiftType.SABAH, day0.type)
        assertEquals("07:30", day0.startTime)
        assertEquals("15:30", day0.endTime)
        assertEquals(8.0, day0.baseHours, 0.01)

        val day2 = repo.getShiftForDate(refDate.plusDays(2))
        assertEquals(ShiftType.AKSAM, day2.type)
        assertEquals("16:00", day2.startTime)
        assertEquals("24:00", day2.endTime)
        assertEquals(8.0, day2.baseHours, 0.01)

        val day4 = repo.getShiftForDate(refDate.plusDays(4))
        assertEquals(ShiftType.GECE8, day4.type)
        assertEquals("24:00", day4.startTime)
        assertEquals("08:00", day4.endTime)
        assertEquals(8.0, day4.baseHours, 0.01)

        val geceTelemetry = repo.calculateDutyTelemetry(LocalDateTime.of(2026, 11, 6, 3, 0))
        assertTrue(geceTelemetry.isOnDuty)
        assertEquals(ShiftType.GECE8, geceTelemetry.currentShiftType)
        assertEquals("24:00 - 08:00 (8s)", geceTelemetry.shiftHoursRange)

        val backup = repo.exportFullBackupJson()
        repo.resetToDefaults()
        assertEquals("08:00", repo.userSettings.value.sabahStartTime)
        assertEquals("00:00", repo.userSettings.value.gece8StartTime)

        assertTrue(repo.importFullBackupJson(backup))
        assertEquals("07:30", repo.userSettings.value.sabahStartTime)
        assertEquals("15:30", repo.userSettings.value.sabahEndTime)
        assertEquals("16:00", repo.userSettings.value.aksamStartTime)
        assertEquals("24:00", repo.userSettings.value.aksamEndTime)
        assertEquals("24:00", repo.userSettings.value.gece8StartTime)
        assertEquals("08:00", repo.userSettings.value.gece8EndTime)
    }

    @Test
    fun `picker hour minute helpers support 24_00 when allow24Hour is enabled`() {
        assertEquals(24 to 0, parseHourMinute("24:00", allow24Hour = true))
        assertEquals(24 to 0, parseHourMinute("24:30", allow24Hour = true))
        assertEquals(23 to 30, parseHourMinute("24:30", allow24Hour = false))
        assertEquals("24:00", formatHourMinute(24, 30, allow24Hour = true))
        assertEquals("23:30", formatHourMinute(24, 30, allow24Hour = false))
    }

    @Test
    fun `getColleagueShiftDay resolves 2-2-2 friend schedule with configured 8h hours`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = ShiftRepository(context)
        repo.resetToDefaults()

        val startDate = LocalDate.of(2026, 11, 1)
        val friend = ColleagueProfile(
            name = "Mehmet Demir",
            department = "Üretim",
            cyclePreset = CyclePreset.CYCLE_8_2_2_2,
            referenceStartDate = startDate,
            qrCodeId = "PRSH-TEST-222"
        )
        val settings = repo.userSettings.value.copy(
            sabahStartTime = "08:00",
            sabahEndTime = "16:00",
            aksamStartTime = "16:00",
            aksamEndTime = "24:00",
            gece8StartTime = "24:00",
            gece8EndTime = "08:00"
        )

        val d0 = repo.getColleagueShiftDay(friend, startDate, settings)
        val d1 = repo.getColleagueShiftDay(friend, startDate.plusDays(1), settings)
        val d2 = repo.getColleagueShiftDay(friend, startDate.plusDays(2), settings)
        val d3 = repo.getColleagueShiftDay(friend, startDate.plusDays(3), settings)
        val d4 = repo.getColleagueShiftDay(friend, startDate.plusDays(4), settings)
        val d5 = repo.getColleagueShiftDay(friend, startDate.plusDays(5), settings)

        assertEquals(ShiftType.SABAH, d0.type)
        assertEquals("08:00", d0.startTime)
        assertEquals("16:00", d0.endTime)
        assertEquals(ShiftType.SABAH, d1.type)

        assertEquals(ShiftType.AKSAM, d2.type)
        assertEquals("16:00", d2.startTime)
        assertEquals("24:00", d2.endTime)
        assertEquals(ShiftType.AKSAM, d3.type)

        assertEquals(ShiftType.GECE8, d4.type)
        assertEquals("24:00", d4.startTime)
        assertEquals("08:00", d4.endTime)
        assertEquals(8.0, d4.baseHours, 0.01)
        assertEquals(ShiftType.GECE8, d5.type)
    }

    @Test
    fun `ShiftEngine dayNumber and safeModulo handle negative deltas across leap years`() {
        val anchorDate = LocalDate.of(2026, 3, 1)
        val anchor = ShiftAnchor(anchorDate, index = 1)
        val pattern = listOf(ShiftType.GUNDUZ, ShiftType.GECE, ShiftType.IZIN)

        // Day before anchor (2026-02-28): delta = -1, (-1 + 1) mod 3 = 0 -> GUNDUZ
        assertEquals(ShiftType.GUNDUZ, ShiftEngine.stateFor(LocalDate.of(2026, 2, 28), anchor, pattern))
        // 4 days before anchor: delta = -4, (-4 + 1) = -3 -> mod 3 = 0 -> GUNDUZ
        assertEquals(ShiftType.GUNDUZ, ShiftEngine.stateFor(LocalDate.of(2026, 2, 25), anchor, pattern))
        // 5 days before anchor: (-5 + 1) = -4 -> mod 3 = 2 -> IZIN
        assertEquals(ShiftType.IZIN, ShiftEngine.stateFor(LocalDate.of(2026, 2, 24), anchor, pattern))

        assertEquals(0, ShiftEngine.safeModulo(-6L, 3))
        assertEquals(2, ShiftEngine.safeModulo(-1L, 3))
        assertEquals(1, ShiftEngine.safeModulo(-5L, 3))
        assertEquals(1L, ShiftEngine.dayNumber(2024, 2, 29) - ShiftEngine.dayNumber(2024, 2, 28))
    }

    @Test
    fun `midnight-crossing shift on month boundary is attributed 100 percent to start date`() {
        val settings = UserSettings(
            cyclePreset = CyclePreset.CUSTOM,
            customSequence = listOf(ShiftType.GECE, ShiftType.IZIN),
            referenceStartDate = LocalDate.of(2026, 11, 30),
            initialCycleOffset = 0,
            geceStartTime = "20:00",
            geceEndTime = "08:00"
        )
        // Nov 30 is GECE (12h crossing into Dec 1), Dec 1 is IZIN
        val novDays = ShiftEngine.buildMonth(2026, 11, settings, emptyMap())
        val nov30 = novDays.last()
        assertEquals(LocalDate.of(2026, 11, 30), nov30.date)
        assertEquals(ShiftType.GECE, nov30.type)
        assertEquals(12.0, nov30.baseHours, 0.01)

        val novStats = ShiftEngine.computeMonthStats(novDays, settings, LocalDate.of(2026, 11, 30))
        // In Nov 2026 (30 days), starting 2026-11-30 with GECE (offset even backward):
        // Nov has 15 GECE shifts (days 2,4,...,30 -> 15 * 12 = 180h), all 12h of Nov 30 counted in Nov.
        assertEquals(15, novStats.workDaysCount)
        assertEquals(180.0, novStats.totalWorkHours, 0.01)
        assertEquals(180.0, novStats.nightHours, 0.01)

        // Dec 1 is IZIN and must receive 0h spillover from Nov 30
        val decDays = ShiftEngine.buildMonth(2026, 12, settings, emptyMap())
        assertEquals(ShiftType.IZIN, decDays.first().type)
        assertEquals(0.0, decDays.first().baseHours, 0.01)
    }

    @Test
    fun `annualLeaveDays applies Turkish Labor Law 4857 Art 53 brackets and 50 plus or 18 minus protection`() {
        val asOf = LocalDate.of(2026, 11, 15)
        // < 1 year -> 0 days
        assertEquals(0, ShiftEngine.annualLeaveDays(LocalDate.of(2026, 1, 1), asOf))
        // 1..5 years -> 14 days
        assertEquals(14, ShiftEngine.annualLeaveDays(LocalDate.of(2025, 11, 15), asOf))
        assertEquals(14, ShiftEngine.annualLeaveDays(LocalDate.of(2021, 11, 15), asOf))
        // 6..14 years -> 20 days
        assertEquals(20, ShiftEngine.annualLeaveDays(LocalDate.of(2020, 11, 15), asOf))
        assertEquals(20, ShiftEngine.annualLeaveDays(LocalDate.of(2012, 11, 15), asOf))
        // >= 15 years -> 26 days
        assertEquals(26, ShiftEngine.annualLeaveDays(LocalDate.of(2011, 11, 15), asOf))

        // Age >= 50 with 2 years service -> minimum 20 days
        assertEquals(
            20,
            ShiftEngine.annualLeaveDays(
                hireDate = LocalDate.of(2024, 10, 1),
                asOfDate = asOf,
                birthDate = LocalDate.of(1975, 5, 1)
            )
        )
        // Age <= 18 with 1 year service -> minimum 20 days
        assertEquals(
            20,
            ShiftEngine.annualLeaveDays(
                hireDate = LocalDate.of(2025, 10, 1),
                asOfDate = asOf,
                birthDate = LocalDate.of(2008, 6, 1)
            )
        )
    }

    @Test
    fun `explicit per-year holiday table resolves fixed, half-day and religious holidays`() {
        val jan1 = ShiftEngine.holidayForDate(LocalDate.of(2026, 1, 1))
        assertNotNull(jan1)
        assertFalse(jan1!!.isHalfDay)

        val oct28 = ShiftEngine.holidayForDate(LocalDate.of(2026, 10, 28))
        assertNotNull(oct28)
        assertTrue(oct28!!.isHalfDay)

        val ramazan2026 = ShiftEngine.holidayForDate(LocalDate.of(2026, 3, 20))
        assertNotNull(ramazan2026)
        assertTrue(ramazan2026!!.isReligious)

        val kurban2026 = ShiftEngine.holidayForDate(LocalDate.of(2026, 5, 27))
        assertNotNull(kurban2026)
        assertTrue(kurban2026!!.isReligious)

        assertNull(ShiftEngine.holidayForDate(LocalDate.of(2026, 11, 12)))
    }

    @Test
    fun `shift system catalog presets match exact lengths and no-default rules`() {
        assertEquals(8, CyclePreset.CYCLE_4_4.sequence.size)
        assertEquals(14, CyclePreset.CYCLE_PITMAN.sequence.size)
        assertNotNull(CyclePreset.CYCLE_PITMAN.uiNotice)
        assertEquals(8, CyclePreset.CYCLE_8_2_2_2_2.sequence.size)
        assertEquals(10, CyclePreset.CYCLE_8_2_2_2_4.sequence.size)
        assertEquals(28, CyclePreset.CYCLE_SOUTHERN_SWING.sequence.size)
        assertFalse(CyclePreset.CYCLE_DUPONT_TEMPLATE.hasDefaultPattern)
        assertTrue(CyclePreset.CYCLE_DUPONT_TEMPLATE.sequence.isEmpty())
        assertFalse(CyclePreset.CYCLE_FIVE_TEAM_8H.hasDefaultPattern)
        assertTrue(CyclePreset.CYCLE_FIVE_TEAM_8H.sequence.isEmpty())
    }

    @Test
    fun `v1 to v2 prefs migration preserves legacy settings and overrides with toast notice`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences(ShiftRepository.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        prefs.edit()
            .putString("employee_name", "Kemal Aydın")
            .putString("department", "Güvenlik")
            .putString("cycle_preset", CyclePreset.CYCLE_4_4.id)
            .putString("ref_start_date", "2026-10-01")
            .putFloat("hourly_wage", 412.5f)
            .putString(
                "day_overrides",
                """{"2026-10-05":{"dateIso":"2026-10-05","shiftType":"OZEL","overtimeHours":4.0,"note":"Ek mesai"}}"""
            )
            .commit()

        val migratedRepo = ShiftRepository(context)
        val notice = migratedRepo.consumeMigrationNotice()
        assertNotNull(notice)
        assertNull(migratedRepo.consumeMigrationNotice())
        assertEquals("Kemal Aydın", migratedRepo.userSettings.value.employeeName)
        assertEquals(CyclePreset.CYCLE_4_4, migratedRepo.userSettings.value.cyclePreset)
        assertEquals(412.5, migratedRepo.userSettings.value.hourlyWage, 0.01)
        assertEquals(ShiftType.OZEL, migratedRepo.dayOverrides.value["2026-10-05"]?.shiftType)
        assertEquals(ShiftRepository.PREFS_SCHEMA_VERSION, prefs.getInt("prefs_schema_version", 0))
    }

    @Test
    fun `multi-profile switching isolates settings and overrides and deleted override stays deleted`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = ShiftRepository(context)
        repo.resetToDefaults()

        val mainId = repo.activeProfileId.value
        val overrideDate = "2026-11-18"
        repo.saveDayOverride(DayOverride(overrideDate, ShiftType.RAPOR, 0.0, "Sağlık raporu"))
        assertEquals(1, repo.dayOverrides.value.size)

        val second = repo.createProfile("İkinci İş", "Klinik B", "Nöbetçi", CyclePreset.CYCLE_24_48)
        assertEquals(second.id, repo.activeProfileId.value)
        assertTrue(repo.dayOverrides.value.isEmpty())

        assertTrue(repo.switchProfile(mainId))
        assertEquals(1, repo.dayOverrides.value.size)
        repo.removeDayOverride(overrideDate)
        assertTrue(repo.dayOverrides.value.isEmpty())

        // Reload must NOT resurrect deleted or sample overrides
        val reloaded = ShiftRepository(context)
        assertTrue(reloaded.dayOverrides.value.isEmpty())
    }

    @Test
    fun `backup export and import preserves overrides, birthDate and accepts legacy backups`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = ShiftRepository(context)
        repo.resetToDefaults()

        repo.updateSettings(
            repo.userSettings.value.copy(
                employeeName = "Zeynep Ak",
                birthDate = LocalDate.of(1974, 3, 12)
            )
        )
        repo.saveDayOverride(
            DayOverride(
                dateIso = "2026-11-21",
                shiftType = ShiftType.OZEL,
                overtimeHours = 4.0,
                note = "Nöbet devri +4s",
                startTime = "08:00",
                endTime = "20:00"
            )
        )

        val exported = repo.exportFullBackupJson()
        assertTrue(exported.contains("\"version\": \"5.0\""))

        repo.resetToDefaults()
        assertTrue(repo.dayOverrides.value.isEmpty())
        assertTrue(repo.importFullBackupJson(exported))
        assertEquals(LocalDate.of(1974, 3, 12), repo.userSettings.value.birthDate)
        val restoredOverride = repo.dayOverrides.value["2026-11-21"]
        assertNotNull(restoredOverride)
        assertEquals(4.0, restoredOverride!!.overtimeHours, 0.01)
        assertEquals("08:00", restoredOverride.startTime)

        // Reject invalid/empty JSON
        assertFalse(repo.importFullBackupJson("{}"))

        // Legacy v4.8 backup without schemaVersion or birthDate still imports cleanly
        val legacyBackup = """
            {
              "version": "4.8",
              "settings": {
                "employeeName": "Eski Kullanıcı",
                "department": "Lojistik",
                "cyclePreset": "2_2_2",
                "referenceStartDate": "2026-11-01"
              },
              "overrides": {
                "2026-11-25": {
                  "dateIso": "2026-11-25",
                  "shiftType": "IZIN",
                  "overtimeHours": 0.0,
                  "note": "Eski not"
                }
              }
            }
        """.trimIndent()
        assertTrue(repo.importFullBackupJson(legacyBackup))
        assertEquals("Eski Kullanıcı", repo.userSettings.value.employeeName)
        assertEquals(CyclePreset.CYCLE_2_2_2, repo.userSettings.value.cyclePreset)
        assertEquals(ShiftType.IZIN, repo.dayOverrides.value["2026-11-25"]?.shiftType)
    }

    @Test
    fun `parseSyncPayloadDetailed reports diagnostic failures and v2 success with warnings`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = ShiftRepository(context)

        val emptyRes = repo.parseSyncPayloadDetailed("   ")
        assertTrue(emptyRes is QrParseResult.Failure)
        assertEquals("EMPTY_INPUT", (emptyRes as QrParseResult.Failure).errorCode)

        val schemeRes = repo.parseSyncPayloadDetailed("https://example.com/shift?name=Ali")
        assertTrue(schemeRes is QrParseResult.Failure)
        assertEquals("UNSUPPORTED_SCHEME", (schemeRes as QrParseResult.Failure).errorCode)

        val missingNameRes = repo.parseSyncPayloadDetailed("PROSHIFT://sync?v=2&preset=2_2_2")
        assertTrue(missingNameRes is QrParseResult.Failure)
        assertEquals("MISSING_NAME", (missingNameRes as QrParseResult.Failure).errorCode)

        val missingPresetRes = repo.parseSyncPayloadDetailed("PROSHIFT://sync?v=2&name=Ali+Veli")
        assertTrue(missingPresetRes is QrParseResult.Failure)
        assertEquals("MISSING_PRESET", (missingPresetRes as QrParseResult.Failure).errorCode)

        val warnRes = repo.parseSyncPayloadDetailed(
            "PROSHIFT://sync?v=2&name=Mehmet+Demir&dept=Acil&preset=8_2_2_2&ref=not-a-date&offset=1."
        )
        assertTrue(warnRes is QrParseResult.Success)
        val ok = warnRes as QrParseResult.Success
        assertEquals("v2", ok.payloadVersion)
        assertEquals("Mehmet Demir", ok.colleague.name)
        assertEquals(CyclePreset.CYCLE_8_2_2_2, ok.colleague.cyclePreset)
        assertEquals(1, ok.colleague.initialCycleOffset)
        assertNotNull(ok.warningMessage)

        // Universal URI with short tokens (G,G,A,A,N,N,O,O) and omitted preset
        val shortUriRes = repo.parseSyncPayloadDetailed(
            "PROSHIFT://sync?name=Ay%C5%9Fe+Y%C4%B1lmaz&seq=G,G,A,A,N,N,O,O&ref=2026-11-01"
        )
        assertTrue(shortUriRes is QrParseResult.Success)
        val shortUriOk = shortUriRes as QrParseResult.Success
        assertEquals("Ayşe Yılmaz", shortUriOk.colleague.name)
        assertEquals(CyclePreset.CYCLE_8_2_2_2_2, shortUriOk.colleague.cyclePreset)
        assertEquals(LocalDate.of(2026, 11, 1), shortUriOk.colleague.referenceStartDate)

        // Universal JSON format from Web PROSHIFT
        val jsonRes = repo.parseSyncPayloadDetailed(
            """{"p":"PROSHIFT","name":"Caner Kaya","dept":"Güvenlik","pattern":["G","G","A","A","N","N","O","O"],"ref":"2026-11-05","offset":3}"""
        )
        assertTrue(jsonRes is QrParseResult.Success)
        val jsonOk = jsonRes as QrParseResult.Success
        assertEquals("Caner Kaya", jsonOk.colleague.name)
        assertEquals("Güvenlik", jsonOk.colleague.department)
        assertEquals(CyclePreset.CYCLE_8_2_2_2_2, jsonOk.colleague.cyclePreset)
        assertEquals(LocalDate.of(2026, 11, 5), jsonOk.colleague.referenceStartDate)
        assertEquals(3, jsonOk.colleague.initialCycleOffset)
    }

    @Test
    fun `autoProcessScannedColleague and applyColleagueAsOwnShift update active calendar state`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val repo = ShiftRepository(app)
        repo.resetToDefaults()
        repo.updateSettings(repo.userSettings.value.copy(showFriendOnCalendar = false))

        val viewModel = ShiftViewModel(app)
        val scanned = ColleagueProfile(
            name = "Elif Karahan",
            department = "Üretim Vardiya 2",
            cyclePreset = CyclePreset.CYCLE_8_2_2_2,
            referenceStartDate = LocalDate.of(2026, 11, 2),
            qrCodeId = "PRSH-QR-TEST1",
            initialCycleOffset = 2
        )

        val receipt = viewModel.autoProcessScannedColleague(scanned, sourceLabel = "Canlı Kamera")
        assertEquals("Elif Karahan", receipt.colleague.name)
        assertFalse(receipt.appliedAsOwnShift)
        assertTrue(viewModel.userSettings.value.showFriendOnCalendar)
        assertTrue(viewModel.userSettings.value.calendarSyncEnabled)
        assertEquals("Elif Karahan", viewModel.selectedColleague.value?.name)
        assertEquals(receipt, viewModel.lastQrSyncReceipt.value)

        val ownReceipt = viewModel.applyColleagueAsOwnShift(scanned)
        assertTrue(ownReceipt.appliedAsOwnShift)
        assertEquals(CyclePreset.CYCLE_8_2_2_2, viewModel.userSettings.value.cyclePreset)
        assertEquals(LocalDate.of(2026, 11, 2), viewModel.userSettings.value.referenceStartDate)
        assertEquals(2, viewModel.userSettings.value.initialCycleOffset)
        assertTrue(viewModel.userSettings.value.isAnchorConfigured)
    }

    @Test
    fun `smart onboarding profile creation updates active profile summary and sector preset defaults`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = ShiftRepository(context)
        repo.resetToDefaults()

        val sector = ProfessionGroup.GUVENLIK
        assertEquals(CyclePreset.CYCLE_12_24_12_48, sector.recommendedPreset)
        assertTrue(sector.suggestedDepartments.isNotEmpty())

        repo.updateSettings(
            repo.userSettings.value.copy(
                employeeName = "Murat Demir",
                professionGroup = sector.id,
                title = sector.titles.first(),
                department = sector.suggestedDepartments.first(),
                cyclePreset = sector.recommendedPreset,
                isAnchorConfigured = true,
                isOnboardingCompleted = true
            )
        )

        val reloaded = ShiftRepository(context)
        val activeSummary = reloaded.profiles.value.firstOrNull { it.id == reloaded.activeProfileId.value }
        assertNotNull(activeSummary)
        assertEquals("Murat Demir", activeSummary!!.name)
        assertEquals("Polis Memuru", activeSummary.title)
        assertEquals("Merkez Nokta", activeSummary.department)
        assertEquals(CyclePreset.CYCLE_12_24_12_48.id, activeSummary.cyclePresetId)
        assertTrue(reloaded.userSettings.value.isOnboardingCompleted)
    }

    @Test
    fun `onboarding flow starts at ONBOARDING on fresh launch saves to SharedPreferences and navigates to HOME`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val repo = ShiftRepository(app)
        repo.resetToDefaults()

        val freshVm = ShiftViewModel(app)
        assertEquals(com.example.ui.viewmodel.AppScreen.ONBOARDING, freshVm.currentScreen.value)

        val today = LocalDate.now()
        freshVm.completeOnboardingSetup(
            employeeName = "Zeynep Aksoy",
            department = "Acil Servis",
            cyclePreset = CyclePreset.CYCLE_12_36,
            professionGroup = ProfessionGroup.SAGLIK,
            referenceStartDate = today,
            initialCycleOffset = 1
        )

        assertEquals(com.example.ui.viewmodel.AppScreen.HOME, freshVm.currentScreen.value)
        assertTrue(freshVm.userSettings.value.isOnboardingCompleted)

        val prefs = app.getSharedPreferences(ShiftRepository.PREFS_NAME, Context.MODE_PRIVATE)
        assertEquals("Zeynep Aksoy", prefs.getString("employee_name", null))
        assertEquals("Acil Servis", prefs.getString("department", null))
        assertEquals(CyclePreset.CYCLE_12_36.id, prefs.getString("cycle_preset", null))
        assertTrue(prefs.getBoolean("onboarding_done", false))

        val relaunchedVm = ShiftViewModel(app)
        assertEquals(com.example.ui.viewmodel.AppScreen.HOME, relaunchedVm.currentScreen.value)
        assertEquals("Zeynep Aksoy", relaunchedVm.userSettings.value.employeeName)
        assertEquals("Acil Servis", relaunchedVm.userSettings.value.department)
        assertEquals(CyclePreset.CYCLE_12_36, relaunchedVm.userSettings.value.cyclePreset)
    }

    @Test
    fun `dynamic theme stays untinted before onboarding and tints subtly by department after completion`() {
        val basePalette = com.example.ui.theme.buildDynamicThemePalette(
            department = "İtfaiye İstasyonu",
            professionGroupId = ProfessionGroup.ITFAIYE_AFET.id,
            enabled = false
        )
        assertFalse(basePalette.isTinted)
        assertEquals(com.example.ui.theme.BackgroundTitanium, basePalette.background)
        assertEquals(com.example.ui.theme.PrimaryNeon, basePalette.primaryNeon)

        val firePalette = com.example.ui.theme.buildDynamicThemePalette(
            department = "İtfaiye İstasyonu",
            professionGroupId = ProfessionGroup.ITFAIYE_AFET.id,
            enabled = true
        )
        val securityPalette = com.example.ui.theme.buildDynamicThemePalette(
            department = "Güvenlik Birimi",
            professionGroupId = ProfessionGroup.GUVENLIK.id,
            enabled = true
        )
        val productionPalette = com.example.ui.theme.buildDynamicThemePalette(
            department = "Üretim Hattı",
            professionGroupId = ProfessionGroup.SANAYI_URETIM.id,
            enabled = true
        )

        assertTrue(firePalette.isTinted)
        assertEquals("rescue_amber", firePalette.departmentTone.id)
        assertEquals("tactical_cobalt", securityPalette.departmentTone.id)
        assertEquals("industrial_emerald", productionPalette.departmentTone.id)

        assertTrue(firePalette.background != basePalette.background)
        assertTrue(firePalette.primaryNeon != securityPalette.primaryNeon)
        assertTrue(securityPalette.primaryNeon != productionPalette.primaryNeon)

        // Subtle tint keeps dark Titanium Navy readability intact (low relative luminance)
        assertTrue(firePalette.background.red < 0.12f)
        assertTrue(firePalette.background.green < 0.12f)
        assertTrue(firePalette.background.blue < 0.15f)
    }

    @Test
    fun `release version metadata is incremented for Play Store update and policy compliant`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("PROSHIFT", appName)
        assertTrue(appName.length <= 30)
        assertEquals("com.aistudio.vardiyadongu.tkvswz", BuildConfig.APPLICATION_ID)
        assertEquals(10, BuildConfig.VERSION_CODE)
        assertEquals("2.1.2", BuildConfig.VERSION_NAME)
    }

    @Test
    fun `mainActivity configures edge to edge and transparent dark system bars on Android 15 plus`() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()
        val window = activity.window
        assertNotNull(window)

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        assertFalse(insetsController.isAppearanceLightStatusBars)
        assertFalse(insetsController.isAppearanceLightNavigationBars)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            assertFalse(window.isNavigationBarContrastEnforced)
        }
    }

    @Test
    fun `fresh install has no demo colleagues and upgrade purges legacy demo entries while preserving user contacts`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val prefs = app.getSharedPreferences(ShiftRepository.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().commit()

        val freshRepo = ShiftRepository(app)
        assertTrue(freshRepo.colleagues.value.isEmpty())

        val freshVm = ShiftViewModel(app)
        assertTrue(freshVm.colleagues.value.isEmpty())
        assertNull(freshVm.selectedColleague.value)
        assertTrue(freshVm.swapRequests.value.isEmpty())

        // Simulate an older app version with legacy demo contacts + 1 real user-added contact
        prefs.edit()
            .putBoolean("demo_colleagues_purged", false)
            .putString(
                "colleagues_json",
                """[
                    {"name":"Dr. Kaan Demir","department":"Acil Servis","cyclePreset":"2_2_2","referenceStartDate":"2026-11-01","qrCodeId":"PRSH-KAAN-26"},
                    {"name":"Hemşire Merve Kaya","department":"Yoğun Bakım","cyclePreset":"12_36","referenceStartDate":"2026-11-01","qrCodeId":"PRSH-MERVE-26"},
                    {"name":"Dr. Ahmet Arslan","department":"Acil Poliklinik","cyclePreset":"24_48","referenceStartDate":"2026-11-01","qrCodeId":"PRSH-AHMET-26"},
                    {"name":"Selma Korkmaz","department":"Cerrahi","cyclePreset":"12_36","referenceStartDate":"2026-11-02","qrCodeId":"PRSH-REAL-99"}
                ]"""
            )
            .commit()

        val upgradedRepo = ShiftRepository(app)
        assertEquals(1, upgradedRepo.colleagues.value.size)
        assertEquals("Selma Korkmaz", upgradedRepo.colleagues.value.first().name)
    }

    @Test
    fun `offline ZXing QrCodeCodec roundtrips PROSHIFT sync payload without ML Kit`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = ShiftRepository(context)
        repo.resetToDefaults()

        val payload = repo.generateSyncPayload(
            repo.userSettings.value.copy(
                employeeName = "Burak Özkan",
                department = "Acil Tıp",
                cyclePreset = CyclePreset.CYCLE_8_2_2_2,
                referenceStartDate = LocalDate.of(2026, 11, 3),
                initialCycleOffset = 1
            )
        )

        val qrBitmap = QrCodeCodec.generateQrBitmap(payload, sizePx = 512)
        assertNotNull(qrBitmap)

        val decoded = QrCodeCodec.decodeQrFromBitmap(qrBitmap!!)
        assertEquals(payload, decoded)

        val parsed = repo.parseSyncPayloadDetailed(decoded!!)
        assertTrue(parsed is QrParseResult.Success)
        val ok = parsed as QrParseResult.Success
        assertEquals("Burak Özkan", ok.colleague.name)
        assertEquals(CyclePreset.CYCLE_8_2_2_2, ok.colleague.cyclePreset)
        assertEquals(1, ok.colleague.initialCycleOffset)
    }

    @Test
    fun `navigation backstack and colleague deletion update state safely`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val repo = ShiftRepository(app)
        repo.resetToDefaults()
        repo.updateSettings(repo.userSettings.value.copy(isOnboardingCompleted = true))

        val vm = ShiftViewModel(app)
        assertEquals(com.example.ui.viewmodel.AppScreen.HOME, vm.currentScreen.value)

        vm.navigateTo(com.example.ui.viewmodel.AppScreen.MATCHING_QR)
        assertEquals(com.example.ui.viewmodel.AppScreen.MATCHING_QR, vm.currentScreen.value)

        vm.navigateTo(com.example.ui.viewmodel.AppScreen.QR_SCANNER)
        assertEquals(com.example.ui.viewmodel.AppScreen.QR_SCANNER, vm.currentScreen.value)

        vm.navigateBack(fallback = com.example.ui.viewmodel.AppScreen.HOME)
        assertEquals(com.example.ui.viewmodel.AppScreen.MATCHING_QR, vm.currentScreen.value)

        // Add and delete colleague safely
        val friend = ColleagueProfile(
            name = "Deniz Şahin",
            department = "Radyoloji",
            cyclePreset = CyclePreset.CYCLE_12_36,
            referenceStartDate = LocalDate.of(2026, 11, 1),
            qrCodeId = "PRSH-DENIZ-01"
        )
        vm.addAndSelectColleague(friend)
        assertEquals(1, vm.colleagues.value.size)
        assertEquals("Deniz Şahin", vm.selectedColleague.value?.name)

        vm.removeColleague(friend)
        assertTrue(vm.colleagues.value.isEmpty())
        assertNull(vm.selectedColleague.value)
        assertTrue(vm.matchResults.value.isEmpty())
    }
}


