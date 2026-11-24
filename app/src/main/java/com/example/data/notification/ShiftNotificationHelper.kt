package com.example.data.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.DutyTelemetry
import com.example.data.model.ShiftType
import com.example.data.model.UserSettings
import com.example.data.repository.ShiftRepository
import com.example.ui.i18n.localizedNextShiftDetails
import com.example.ui.i18n.localizedTitle
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicInteger

sealed class NotificationDispatchResult {
    data class SentAudible(val message: String) : NotificationDispatchResult()
    data class SentDeviceMuted(val message: String) : NotificationDispatchResult()
    data class PermissionRequired(val message: String) : NotificationDispatchResult()
    data class SystemDisabled(val message: String) : NotificationDispatchResult()
}

object ShiftNotificationHelper {
    const val CHANNEL_ID_ALERTS = "proshift_shift_alerts_v2"
    const val CHANNEL_ID_QUIET = "proshift_shift_quiet_v2"

    const val NOTIFICATION_ID_PRE_SHIFT = 2101
    const val NOTIFICATION_ID_DAILY = 2102
    private const val NOTIFICATION_ID_TEST_BASE = 2200

    private val VIBRATION_PATTERN = longArrayOf(0L, 250L, 150L, 250L)
    private val testNotificationCounter = AtomicInteger(0)

    private fun resolveNotificationSoundUri(): android.net.Uri {
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: Settings.System.DEFAULT_NOTIFICATION_URI
    }

    fun ensureChannels(context: Context, isEnglish: Boolean = false) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        val soundUri = resolveNotificationSoundUri()
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val alertChannel = NotificationChannel(
            CHANNEL_ID_ALERTS,
            if (isEnglish) "Shift & Duty Alerts" else "Vardiya & Nöbet Uyarıları",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = if (isEnglish) {
                "Audible reminders before upcoming shifts and daily schedule alerts."
            } else {
                "Yaklaşan vardiya öncesi sesli uyarılar ve günlük nöbet hatırlatıcıları."
            }
            setSound(soundUri, audioAttributes)
            enableVibration(true)
            vibrationPattern = VIBRATION_PATTERN
            enableLights(true)
        }

        val quietChannel = NotificationChannel(
            CHANNEL_ID_QUIET,
            if (isEnglish) "Night Shift Quiet Mode" else "Gece Vardiyası Sessiz Mod",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = if (isEnglish) {
                "Silent notifications during daytime recovery after night shifts."
            } else {
                "Gece vardiyası sonrası gündüz dinlenmesinde sessiz bildirimler."
            }
            setSound(null, null)
            enableVibration(false)
        }

        manager.createNotificationChannel(alertChannel)
        manager.createNotificationChannel(quietChannel)
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun canScheduleExactAlarms(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager
            return alarmManager?.canScheduleExactAlarms() ?: true
        }
        return true
    }

    fun areSystemNotificationsEnabled(context: Context): Boolean {
        val compat = NotificationManagerCompat.from(context)
        if (!compat.areNotificationsEnabled()) return false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            val channel = manager?.getNotificationChannel(CHANNEL_ID_ALERTS)
            if (channel != null && channel.importance == NotificationManager.IMPORTANCE_NONE) {
                return false
            }
        }
        return true
    }

    fun isAlertChannelMutedOrSilent(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            val channel = manager?.getNotificationChannel(CHANNEL_ID_ALERTS)
            if (channel != null && (channel.importance < NotificationManager.IMPORTANCE_DEFAULT || channel.sound == null)) {
                return true
            }
        }
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return false
        val ringerMuted = audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL
        val notifVolumeZero = runCatching {
            audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION) == 0
        }.getOrDefault(false)
        return ringerMuted || notifVolumeZero
    }

    fun isDaytimeRestAfterNightShift(
        repository: ShiftRepository,
        now: LocalDateTime = LocalDateTime.now()
    ): Boolean {
        val settings = repository.userSettings.value
        if (!settings.nightDndEnabled) return false
        val hour = now.hour
        if (hour !in 8..17) return false

        val todayShift = repository.getShiftForDate(now.toLocalDate())
        val yesterdayShift = repository.getShiftForDate(now.toLocalDate().minusDays(1))
        val nightTypes = setOf(ShiftType.GECE, ShiftType.GECE8, ShiftType.NOBET24)
        return yesterdayShift.type in nightTypes || todayShift.type in nightTypes
    }

    fun showTestNotification(
        context: Context,
        settings: UserSettings,
        telemetry: DutyTelemetry,
        isEnglish: Boolean
    ): NotificationDispatchResult {
        ensureChannels(context, isEnglish)

        if (!hasNotificationPermission(context)) {
            return NotificationDispatchResult.PermissionRequired(
                if (isEnglish) "Notification permission is required to send alerts."
                else "Bildirim sesi ve uyarısı için bildirim izni gereklidir."
            )
        }

        if (!areSystemNotificationsEnabled(context)) {
            return NotificationDispatchResult.SystemDisabled(
                if (isEnglish) "Notifications are turned off in Android settings."
                else "Android ayarlarında PROSHIFT bildirimleri kapalı."
            )
        }

        val nextTitle = telemetry.nextShiftType.localizedTitle(isEnglish)
        val nextDetails = telemetry.localizedNextShiftDetails(isEnglish)
        val title = if (isEnglish) {
            "PROSHIFT • Alert Test (${settings.shiftReminderTime})"
        } else {
            "PROSHIFT • Bildirim Testi (${settings.shiftReminderTime})"
        }
        val body = if (isEnglish) {
            "Next shift: $nextTitle ($nextDetails) • Pre-shift alert: ${settings.preShiftAlertMinutes} min before."
        } else {
            "Sıradaki vardiya: $nextTitle ($nextDetails) • Ön uyarı: ${settings.preShiftAlertMinutes} dk önce."
        }

        val notificationId = NOTIFICATION_ID_TEST_BASE + (testNotificationCounter.incrementAndGet() % 50)
        val manager = NotificationManagerCompat.from(context)
        // Cancel previous test notification to avoid sound throttling on consecutive taps
        if (notificationId > NOTIFICATION_ID_TEST_BASE) {
            manager.cancel(notificationId - 1)
        }

        val notification = buildNotification(
            context = context,
            channelId = CHANNEL_ID_ALERTS,
            title = title,
            body = body,
            quiet = false
        )

        try {
            manager.notify(notificationId, notification)
        } catch (_: SecurityException) {
            return NotificationDispatchResult.PermissionRequired(
                if (isEnglish) "Notification permission is required to send alerts."
                else "Bildirim göndermek için bildirim izni gereklidir."
            )
        }

        return if (isAlertChannelMutedOrSilent(context)) {
            NotificationDispatchResult.SentDeviceMuted(
                if (isEnglish) "Test alert sent (device or channel is currently silent/vibrate)."
                else "Test bildirimi gönderildi (telefon veya bildirim kanalı sessiz/titreşim modunda)."
            )
        } else {
            NotificationDispatchResult.SentAudible(
                if (isEnglish) "Test notification sent with sound!"
                else "Sesli test bildirimi gönderildi!"
            )
        }
    }

    fun showScheduledNotification(
        context: Context,
        notificationId: Int,
        title: String,
        body: String,
        quiet: Boolean,
        isEnglish: Boolean
    ): Boolean {
        ensureChannels(context, isEnglish)
        if (!hasNotificationPermission(context) || !areSystemNotificationsEnabled(context)) {
            return false
        }
        val channelId = if (quiet) CHANNEL_ID_QUIET else CHANNEL_ID_ALERTS
        val notification = buildNotification(
            context = context,
            channelId = channelId,
            title = title,
            body = body,
            quiet = quiet
        )
        return try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
            true
        } catch (_: SecurityException) {
            false
        }
    }

    fun openChannelSoundSettings(context: Context, isEnglish: Boolean = false) {
        ensureChannels(context, isEnglish)
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                putExtra(Settings.EXTRA_CHANNEL_ID, CHANNEL_ID_ALERTS)
            }
        } else {
            Intent("android.settings.APP_NOTIFICATION_SETTINGS").apply {
                putExtra("app_package", context.packageName)
                putExtra("app_uid", context.applicationInfo.uid)
            }
        }.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching {
            context.startActivity(intent)
        }.onFailure {
            val fallback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            } else {
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    android.net.Uri.fromParts("package", context.packageName, null)
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            runCatching { context.startActivity(fallback) }
        }
    }

    private fun buildNotification(
        context: Context,
        channelId: String,
        title: String,
        body: String,
        quiet: Boolean
    ): android.app.Notification {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification_proshift)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (quiet) {
            builder
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setSilent(true)
        } else {
            val defaultSound = resolveNotificationSoundUri()
            builder
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(defaultSound)
                .setVibrate(VIBRATION_PATTERN)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
        }

        return builder.build()
    }
}
