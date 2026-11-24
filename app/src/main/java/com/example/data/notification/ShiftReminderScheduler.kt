package com.example.data.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.data.repository.ShiftRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object ShiftReminderScheduler {
    const val ACTION_PRE_SHIFT_ALERT = "com.example.action.PRE_SHIFT_ALERT"
    const val ACTION_DAILY_REMINDER = "com.example.action.DAILY_REMINDER"
    const val ACTION_COMMON_OFF_REMINDER = "com.example.action.COMMON_OFF_REMINDER"
    const val EXTRA_COLLEAGUE_NAME = "extra_colleague_name"
    const val EXTRA_TARGET_DATE_ISO = "extra_target_date_iso"

    private const val REQUEST_CODE_PRE_SHIFT = 3101
    private const val REQUEST_CODE_DAILY = 3102
    private const val REQUEST_CODE_COMMON_OFF_BASE = 3200

    fun scheduleCommonOffReminder(
        context: Context,
        targetDate: LocalDate,
        colleagueName: String,
        now: LocalDateTime = LocalDateTime.now()
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val dayBeforeMorning = LocalDateTime.of(targetDate.minusDays(1), LocalTime.of(10, 0))
        val targetMorning = LocalDateTime.of(targetDate, LocalTime.of(9, 0))
        val triggerTime = when {
            dayBeforeMorning.isAfter(now) -> dayBeforeMorning
            targetMorning.isAfter(now) -> targetMorning
            else -> now.plusMinutes(1)
        }
        val requestCode = REQUEST_CODE_COMMON_OFF_BASE + ((targetDate.dayOfYear + colleagueName.hashCode()).and(0x3FF))
        val intent = Intent(context, ShiftReminderReceiver::class.java).apply {
            action = ACTION_COMMON_OFF_REMINDER
            putExtra(EXTRA_COLLEAGUE_NAME, colleagueName)
            putExtra(EXTRA_TARGET_DATE_ISO, targetDate.toString())
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAtMillis = triggerTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        runCatching {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    fun syncReminders(
        context: Context,
        repository: ShiftRepository,
        now: LocalDateTime = LocalDateTime.now()
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val settings = repository.userSettings.value

        // 1. Pre-shift alert (e.g. 120 min before next upcoming work shift)
        if (settings.preShiftAlertEnabled) {
            val triggerTime = computeNextPreShiftTrigger(repository, settings.preShiftAlertMinutes, now)
            if (triggerTime != null) {
                scheduleInexactAlarm(
                    context = context,
                    alarmManager = alarmManager,
                    action = ACTION_PRE_SHIFT_ALERT,
                    requestCode = REQUEST_CODE_PRE_SHIFT,
                    triggerAtMillis = triggerTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                )
            } else {
                cancelAlarm(context, alarmManager, ACTION_PRE_SHIFT_ALERT, REQUEST_CODE_PRE_SHIFT)
            }
        } else {
            cancelAlarm(context, alarmManager, ACTION_PRE_SHIFT_ALERT, REQUEST_CODE_PRE_SHIFT)
        }

        // 2. Daily reminder at shiftReminderTime (e.g. 20:00)
        if (settings.shiftReminderEnabled) {
            val dailyTrigger = computeNextDailyTrigger(settings.shiftReminderTime, now)
            scheduleInexactAlarm(
                context = context,
                alarmManager = alarmManager,
                action = ACTION_DAILY_REMINDER,
                requestCode = REQUEST_CODE_DAILY,
                triggerAtMillis = dailyTrigger.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
        } else {
            cancelAlarm(context, alarmManager, ACTION_DAILY_REMINDER, REQUEST_CODE_DAILY)
        }
    }

    internal fun computeNextPreShiftTrigger(
        repository: ShiftRepository,
        leadMinutes: Int,
        now: LocalDateTime = LocalDateTime.now()
    ): LocalDateTime? {
        val safeLead = leadMinutes.coerceIn(5, 720).toLong()
        val today: LocalDate = now.toLocalDate()
        for (offset in 0L..14L) {
            val date = today.plusDays(offset)
            val shiftDay = repository.getShiftForDate(date)
            if (!shiftDay.type.isWork) continue
            val startTime = parseTimeOrNull(shiftDay.startTime) ?: continue
            val shiftStart = LocalDateTime.of(date, startTime)
            val alertTime = shiftStart.minusMinutes(safeLead)
            if (alertTime.isAfter(now)) {
                return alertTime
            }
        }
        return null
    }

    internal fun computeNextDailyTrigger(
        timeText: String,
        now: LocalDateTime = LocalDateTime.now()
    ): LocalDateTime {
        val parsed = parseTimeOrNull(timeText) ?: LocalTime.of(20, 0)
        val todayCandidate = LocalDateTime.of(now.toLocalDate(), parsed)
        return if (todayCandidate.isAfter(now)) todayCandidate else todayCandidate.plusDays(1)
    }

    private fun parseTimeOrNull(raw: String): LocalTime? {
        val parts = raw.trim().split(":")
        if (parts.size != 2) return null
        val hour = parts[0].toIntOrNull() ?: return null
        val minute = parts[1].toIntOrNull() ?: return null
        if (hour !in 0..23 || minute !in 0..59) return null
        return LocalTime.of(hour, minute)
    }

    private fun scheduleInexactAlarm(
        context: Context,
        alarmManager: AlarmManager,
        action: String,
        requestCode: Int,
        triggerAtMillis: Long
    ) {
        val pendingIntent = buildPendingIntent(context, action, requestCode)
        runCatching {
            if (ShiftNotificationHelper.canScheduleExactAlarms(context)) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        }
    }

    private fun cancelAlarm(
        context: Context,
        alarmManager: AlarmManager,
        action: String,
        requestCode: Int
    ) {
        val pendingIntent = buildPendingIntent(context, action, requestCode)
        runCatching { alarmManager.cancel(pendingIntent) }
    }

    private fun buildPendingIntent(context: Context, action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, ShiftReminderReceiver::class.java).apply {
            this.action = action
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
