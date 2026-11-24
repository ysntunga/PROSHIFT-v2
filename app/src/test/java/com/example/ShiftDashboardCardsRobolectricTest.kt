package com.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.data.model.DutyTelemetry
import com.example.data.model.ShiftDay
import com.example.data.model.ShiftType
import com.example.data.model.TimesheetStats
import com.example.ui.components.ShiftDashboardOverview
import com.example.ui.components.computeUpcomingShiftTimers
import com.example.ui.components.formatUpcomingCountdown
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalAppLocale
import com.example.ui.i18n.LocalIsEnglish
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDateTime

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ShiftDashboardCardsRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun computeUpcomingShiftTimers_filtersPastAndOffDaysAndSortsByStart() {
        val now = LocalDateTime.of(2026, 11, 23, 10, 0)
        val today = now.toLocalDate()
        val days = listOf(
            ShiftDay(today, ShiftType.GUNDUZ, "08:00", "20:00", 12.0, 0.0, 12.0, "", false),
            ShiftDay(today, ShiftType.GECE, "20:00", "08:00", 12.0, 0.0, 12.0, "", false),
            ShiftDay(today.plusDays(1), ShiftType.IZIN, "-", "-", 0.0, 0.0, 0.0, "", false),
            ShiftDay(today.plusDays(2), ShiftType.GUNDUZ, "08:00", "20:00", 12.0, 0.0, 12.0, "", false),
            ShiftDay(today.plusDays(3), ShiftType.AKSAM, "16:00", "24:00", 8.0, 0.0, 8.0, "", false)
        )

        val timers = computeUpcomingShiftTimers(days = days, now = now, maxCount = 3)
        assertEquals(3, timers.size)
        assertEquals(ShiftType.GECE, timers[0].shiftDay.type)
        assertEquals(10L * 3600L, timers[0].secondsUntilStart)
        assertEquals("10h 00m", formatUpcomingCountdown(timers[0].secondsUntilStart, isEnglish = true))
        assertEquals("10s 00dk", formatUpcomingCountdown(timers[0].secondsUntilStart, isEnglish = false))
        assertEquals("1d 22h", formatUpcomingCountdown(timers[1].secondsUntilStart, isEnglish = true))
    }

    @Test
    fun shiftDashboardOverview_placesScrollableWeeklyFlowBelowCurrentShiftAndRemovesUpcomingTimers() {
        val now = LocalDateTime.of(2026, 11, 23, 10, 0)
        val today = now.toLocalDate()
        val sampleDays = (-7..7).map { offset ->
            val date = today.plusDays(offset.toLong())
            val type = when (Math.floorMod(offset, 3)) {
                0 -> ShiftType.GUNDUZ
                1 -> ShiftType.GECE
                else -> ShiftType.IZIN
            }
            ShiftDay(
                date = date,
                type = type,
                startTime = if (type.isWork) "08:00" else "-",
                endTime = if (type.isWork) "20:00" else "-",
                baseHours = if (type.isWork) 12.0 else 0.0,
                overtimeHours = 0.0,
                totalHours = if (type.isWork) 12.0 else 0.0,
                note = "",
                isOverride = false
            )
        }
        val telemetry = DutyTelemetry(
            isOnDuty = true,
            currentShiftType = ShiftType.GUNDUZ,
            remainingSeconds = 36_000L,
            remainingFormatted = "10:00:00",
            progressPercent = 0.25f,
            progressPercentText = "%25",
            shiftHoursRange = "08:00 - 20:00 (12 saat)",
            nextShiftTitle = "Bugün Gece",
            nextShiftDetails = "20:00 - 08:00",
            nextShiftHoursRemaining = "10s kaldı",
            nextShiftType = ShiftType.GECE
        )
        val stats = TimesheetStats(
            monthTitle = "Kasım 2026",
            totalWorkHours = 120.0,
            targetWorkHours = 180.0,
            nightHours = 48.0,
            overtimeHours = 12.0,
            workDaysCount = 10,
            offDaysCount = 12,
            seniorityYears = 3,
            statutoryLeaveRights = 14,
            usedLeaveDays = 4,
            remainingLeaveDays = 10
        )

        var breakClicked = false
        var noteClicked = false
        var inspectedShift: ShiftDay? = null

        composeRule.setContent {
            MyApplicationTheme(dynamicThemeEnabled = false) {
                CompositionLocalProvider(
                    LocalIsEnglish provides true,
                    LocalAppLocale provides AppLanguage.EN_LOCALE
                ) {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        ShiftDashboardOverview(
                            telemetry = telemetry,
                            weeklyDays = sampleDays,
                            timesheetStats = stats,
                            now = now,
                            onBreakOvertimeClick = { breakClicked = true },
                            onShiftNoteClick = { noteClicked = true },
                            onNextShiftClick = {},
                            onMonthlyHoursClick = {},
                            onTimesheetClick = {},
                            onInspectDay = { inspectedShift = it }
                        )
                    }
                }
            }
        }

        composeRule.waitForIdle()

        // Upcoming Shift Timers card is removed
        assertTrue(composeRule.onAllNodesWithTag("dashboard_upcoming_timers_card").fetchSemanticsNodes().isEmpty())

        // Measure all three sections in the same scroll frame to verify exact vertical order:
        // Current Shift Card -> Weekly Flow Strip -> Metrics Duo Row
        val currentBounds = composeRule.onNodeWithTag("dashboard_current_shift_card").getUnclippedBoundsInRoot()
        val weeklyBounds = composeRule.onNodeWithTag("weekly_flow_section").getUnclippedBoundsInRoot()
        val metricsBounds = composeRule.onNodeWithTag("dashboard_next_shift_card").getUnclippedBoundsInRoot()

        assertTrue(weeklyBounds.top >= currentBounds.bottom)
        assertTrue(metricsBounds.top >= weeklyBounds.bottom)

        // Verify scrollable LazyRow and active today card are displayed, horizontally centered, and clickable
        val rowBounds = composeRule.onNodeWithTag("weekly_flow_lazy_row").performScrollTo().assertIsDisplayed().getUnclippedBoundsInRoot()
        val todayBounds = composeRule.onNodeWithTag("weekly_flow_today_card").assertIsDisplayed().getUnclippedBoundsInRoot()
        val rowCenterX = (rowBounds.left.value + rowBounds.right.value) / 2f
        val todayCenterX = (todayBounds.left.value + todayBounds.right.value) / 2f
        assertTrue("Expected today card centered (rowCenter=$rowCenterX, todayCenter=$todayCenterX)", kotlin.math.abs(rowCenterX - todayCenterX) <= 4f)

        composeRule.onNodeWithTag("weekly_flow_today_card").performClick()
        assertEquals(today, inspectedShift?.date)

        composeRule.onNodeWithTag("dashboard_break_overtime_button").performScrollTo().performClick()
        composeRule.onNodeWithTag("dashboard_shift_note_button").performScrollTo().performClick()
        composeRule.onNodeWithTag("dashboard_monthly_hours_card").performScrollTo().assertIsDisplayed()

        assertTrue(breakClicked)
        assertTrue(noteClicked)
    }
}
