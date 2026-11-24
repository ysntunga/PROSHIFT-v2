package com.example

import android.Manifest
import android.app.AlarmManager
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.os.Build
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CyclePreset
import com.example.data.model.DayOverride
import com.example.data.model.ShiftType
import com.example.data.notification.NotificationDispatchResult
import com.example.data.notification.ShiftNotificationHelper
import com.example.data.notification.ShiftReminderScheduler
import com.example.data.repository.ShiftRepository
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.ProshiftTheme
import com.example.ui.viewmodel.ShiftViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NotificationSystemRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var app: Application
    private lateinit var repository: ShiftRepository

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        repository = ShiftRepository(app)
        repository.resetToDefaults()
        shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
    }

    @Test
    fun ensureChannels_createsAudibleHighImportanceChannelAndQuietChannel() {
        ShiftNotificationHelper.ensureChannels(app, isEnglish = false)
        val nm = app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val alertChannel = nm.getNotificationChannel(ShiftNotificationHelper.CHANNEL_ID_ALERTS)
        assertNotNull("Audible alert channel must be registered", alertChannel)
        assertEquals(NotificationManager.IMPORTANCE_HIGH, alertChannel.importance)
        assertNotNull("Alert channel must have a sound URI configured", alertChannel.sound)
        assertTrue("Alert channel must enable vibration", alertChannel.shouldVibrate())

        val quietChannel = nm.getNotificationChannel(ShiftNotificationHelper.CHANNEL_ID_QUIET)
        assertNotNull("Quiet mode channel must be registered", quietChannel)
        assertEquals(NotificationManager.IMPORTANCE_LOW, quietChannel.importance)
        assertNull("Quiet channel must have null sound", quietChannel.sound)
    }

    @Test
    fun showTestNotification_postsRealAndroidNotificationWithAudibleChannel() {
        val audioManager = app.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 5, 0)

        val settings = repository.userSettings.value
        val telemetry = repository.calculateDutyTelemetry()

        val result = ShiftNotificationHelper.showTestNotification(
            context = app,
            settings = settings,
            telemetry = telemetry,
            isEnglish = false
        )

        assertTrue("Expected audible dispatch when ringer normal & permission granted, got $result", result is NotificationDispatchResult.SentAudible)

        val nm = app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val posted = shadowOf(nm).allNotifications
        assertTrue("A real system notification must be posted", posted.isNotEmpty())
        val latest = posted.last()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            assertEquals(ShiftNotificationHelper.CHANNEL_ID_ALERTS, latest.channelId)
        }
        assertNotNull(latest.smallIcon)
    }

    @Test
    fun showTestNotification_reportsDeviceMutedWhenRingerSilent() {
        val audioManager = app.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT

        val result = ShiftNotificationHelper.showTestNotification(
            context = app,
            settings = repository.userSettings.value,
            telemetry = repository.calculateDutyTelemetry(),
            isEnglish = false
        )

        assertTrue("Expected SentDeviceMuted when phone is silent, got $result", result is NotificationDispatchResult.SentDeviceMuted)
    }

    @Test
    fun showTestNotification_requiresPermissionWhenDeniedOnApi34() {
        shadowOf(app).denyPermissions(Manifest.permission.POST_NOTIFICATIONS)

        val result = ShiftNotificationHelper.showTestNotification(
            context = app,
            settings = repository.userSettings.value,
            telemetry = repository.calculateDutyTelemetry(),
            isEnglish = false
        )

        assertTrue("Expected PermissionRequired when POST_NOTIFICATIONS denied, got $result", result is NotificationDispatchResult.PermissionRequired)
    }

    @Test
    fun nightDndMode_detectsDaytimeRestAfterNightShift() {
        val today = LocalDate.of(2026, 5, 10)
        val yesterdayIso = today.minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ROOT))
        repository.saveDayOverride(
            DayOverride(
                dateIso = yesterdayIso,
                shiftType = ShiftType.GECE,
                overtimeHours = 0.0,
                note = "Gece nöbeti"
            )
        )
        repository.updateSettings(repository.userSettings.value.copy(nightDndEnabled = true))

        assertTrue(
            "Daytime rest at 11:00 after night shift should be quiet",
            ShiftNotificationHelper.isDaytimeRestAfterNightShift(repository, LocalDateTime.of(2026, 5, 10, 11, 0))
        )
        assertFalse(
            "Evening at 19:00 should not be quiet",
            ShiftNotificationHelper.isDaytimeRestAfterNightShift(repository, LocalDateTime.of(2026, 5, 10, 19, 0))
        )

        repository.updateSettings(repository.userSettings.value.copy(nightDndEnabled = false))
        assertFalse(
            "When nightDndEnabled is false, daytime should not be quiet",
            ShiftNotificationHelper.isDaytimeRestAfterNightShift(repository, LocalDateTime.of(2026, 5, 10, 11, 0))
        )
    }

    @Test
    fun scheduler_schedulesAlarmsWhenEnabledAndCancelsWhenDisabled() {
        val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val shadowAlarm = shadowOf(alarmManager)

        repository.updateSettings(
            repository.userSettings.value.copy(
                cyclePreset = CyclePreset.CYCLE_12_36,
                preShiftAlertEnabled = true,
                preShiftAlertMinutes = 120,
                shiftReminderEnabled = true,
                shiftReminderTime = "20:00"
            )
        )

        val now = LocalDateTime.of(2026, 5, 10, 10, 0)
        ShiftReminderScheduler.syncReminders(app, repository, now)
        assertTrue("Expected scheduled alarms when reminders enabled", shadowAlarm.scheduledAlarms.size >= 2)

        repository.updateSettings(
            repository.userSettings.value.copy(
                preShiftAlertEnabled = false,
                shiftReminderEnabled = false
            )
        )
        ShiftReminderScheduler.syncReminders(app, repository, now)
        assertEquals("Expected all alarms cancelled when reminders disabled", 0, shadowAlarm.scheduledAlarms.size)
    }

    @Test
    fun settingsScreen_togglesNotificationPreferencesAndUpdatesShiftReminderAlerts() {
        val viewModel = ShiftViewModel(app)

        composeRule.setContent {
            ProshiftTheme(darkTheme = true) {
                SettingsScreen(viewModel = viewModel)
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithTag("card_notification_preferences").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("card_next_scheduled_alerts").performScrollTo().assertIsDisplayed()

        // Pre-shift alert lead-time quick chip updates persisted lead time
        composeRule.onNodeWithTag("chip_lead_time_60").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals(60, viewModel.userSettings.value.preShiftAlertMinutes)

        // Toggle Pre-Shift Alert OFF
        composeRule.onNodeWithTag("switch_pre_shift_alert").performScrollTo().assertIsOn().performClick()
        composeRule.waitForIdle()
        assertFalse(viewModel.userSettings.value.preShiftAlertEnabled)
        composeRule.onNodeWithTag("switch_pre_shift_alert").assertIsOff()

        // Toggle Daily Shift Reminder OFF
        composeRule.onNodeWithTag("btn_daily_reminder_time").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("switch_daily_shift_reminder").performScrollTo().assertIsOn().performClick()
        composeRule.waitForIdle()
        assertFalse(viewModel.userSettings.value.shiftReminderEnabled)
        composeRule.onNodeWithTag("switch_daily_shift_reminder").assertIsOff()

        // Toggle Night Shift Quiet Mode OFF
        composeRule.onNodeWithTag("switch_night_dnd").performScrollTo().assertIsOn().performClick()
        composeRule.waitForIdle()
        assertFalse(viewModel.userSettings.value.nightDndEnabled)

        // Toggle Shared Day-Off Alerts OFF
        composeRule.onNodeWithTag("switch_common_leave_alert").performScrollTo().assertIsOn().performClick()
        composeRule.waitForIdle()
        assertFalse(viewModel.userSettings.value.commonLeaveAlertEnabled)

        // Verify persistence across repository reload
        val reloaded = ShiftRepository(app).userSettings.value
        assertEquals(60, reloaded.preShiftAlertMinutes)
        assertFalse(reloaded.preShiftAlertEnabled)
        assertFalse(reloaded.shiftReminderEnabled)
        assertFalse(reloaded.nightDndEnabled)
        assertFalse(reloaded.commonLeaveAlertEnabled)
    }
}
