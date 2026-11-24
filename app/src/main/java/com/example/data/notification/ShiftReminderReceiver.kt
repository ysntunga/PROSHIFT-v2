package com.example.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.repository.ShiftRepository
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.localizedNextShiftDetails
import com.example.ui.i18n.localizedTitle
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale

class ShiftReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val repository = ShiftRepository(context.applicationContext)
        val settings = repository.userSettings.value

        val cfg = context.resources.configuration
        val sysLocale = if (!cfg.locales.isEmpty) cfg.locales[0] else Locale.getDefault()
        val isEnglish = AppLanguage.resolveIsEnglish(settings.appLanguage, sysLocale)

        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> {
                ShiftReminderScheduler.syncReminders(context.applicationContext, repository)
            }

            ShiftReminderScheduler.ACTION_PRE_SHIFT_ALERT -> {
                if (settings.preShiftAlertEnabled) {
                    val now = LocalDateTime.now()
                    val telemetry = repository.calculateDutyTelemetry(now)
                    val quiet = ShiftNotificationHelper.isDaytimeRestAfterNightShift(repository, now)
                    val nextTitle = telemetry.nextShiftType.localizedTitle(isEnglish)
                    val nextDetails = telemetry.localizedNextShiftDetails(isEnglish)

                    val title = if (isEnglish) {
                        "Upcoming Shift: $nextTitle"
                    } else {
                        "Yaklaşan Vardiya: $nextTitle"
                    }
                    val body = if (isEnglish) {
                        "$nextDetails • Reminder ${settings.preShiftAlertMinutes} min before shift."
                    } else {
                        "$nextDetails • Vardiyadan ${settings.preShiftAlertMinutes} dk önce hatırlatma."
                    }

                    ShiftNotificationHelper.showScheduledNotification(
                        context = context.applicationContext,
                        notificationId = ShiftNotificationHelper.NOTIFICATION_ID_PRE_SHIFT,
                        title = title,
                        body = body,
                        quiet = quiet,
                        isEnglish = isEnglish
                    )
                }
                ShiftReminderScheduler.syncReminders(
                    context.applicationContext,
                    repository,
                    LocalDateTime.now().plusMinutes(1)
                )
            }

            ShiftReminderScheduler.ACTION_DAILY_REMINDER -> {
                if (settings.shiftReminderEnabled) {
                    val now = LocalDateTime.now()
                    val tomorrowShift = repository.getShiftForDate(LocalDate.now().plusDays(1))
                    val quiet = ShiftNotificationHelper.isDaytimeRestAfterNightShift(repository, now)
                    val shiftLabel = tomorrowShift.type.localizedTitle(isEnglish)
                    val hoursInfo = if (tomorrowShift.type.isWork) {
                        "${tomorrowShift.startTime} - ${tomorrowShift.endTime}"
                    } else {
                        if (isEnglish) "Off duty" else "İzin / Dinlenme"
                    }

                    val title = if (isEnglish) {
                        "Tomorrow's Schedule: $shiftLabel"
                    } else {
                        "Yarınki Vardiya: $shiftLabel"
                    }
                    val body = if (isEnglish) {
                        "PROSHIFT daily reminder (${settings.shiftReminderTime}): $hoursInfo"
                    } else {
                        "PROSHIFT günlük hatırlatıcı (${settings.shiftReminderTime}): $hoursInfo"
                    }

                    ShiftNotificationHelper.showScheduledNotification(
                        context = context.applicationContext,
                        notificationId = ShiftNotificationHelper.NOTIFICATION_ID_DAILY,
                        title = title,
                        body = body,
                        quiet = quiet,
                        isEnglish = isEnglish
                    )
                }
                ShiftReminderScheduler.syncReminders(
                    context.applicationContext,
                    repository,
                    LocalDateTime.now().plusMinutes(1)
                )
            }

            ShiftReminderScheduler.ACTION_COMMON_OFF_REMINDER -> {
                val colleagueName = intent.getStringExtra(ShiftReminderScheduler.EXTRA_COLLEAGUE_NAME).orEmpty()
                val dateIso = intent.getStringExtra(ShiftReminderScheduler.EXTRA_TARGET_DATE_ISO).orEmpty()
                val quiet = ShiftNotificationHelper.isDaytimeRestAfterNightShift(repository, LocalDateTime.now())
                val title = if (isEnglish) "Shared Day Off Reminder" else "Ortak İzin Planı Hatırlatıcısı"
                val body = if (isEnglish) {
                    "You and $colleagueName share a day off on $dateIso."
                } else {
                    "$colleagueName ile $dateIso tarihinde ortak izin gününüz var."
                }
                ShiftNotificationHelper.showScheduledNotification(
                    context = context.applicationContext,
                    notificationId = ShiftNotificationHelper.NOTIFICATION_ID_DAILY + 10,
                    title = title,
                    body = body,
                    quiet = quiet,
                    isEnglish = isEnglish
                )
            }
        }
    }
}
