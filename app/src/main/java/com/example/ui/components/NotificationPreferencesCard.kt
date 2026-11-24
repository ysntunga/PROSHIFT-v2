package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserSettings
import com.example.data.notification.ShiftNotificationHelper
import com.example.data.notification.ShiftReminderScheduler
import com.example.data.repository.ShiftRepository
import com.example.ui.i18n.LocalAppLocale
import com.example.ui.i18n.trEn
import com.example.ui.theme.DynamicTheme
import com.example.ui.theme.ProshiftMonoFont
import com.example.ui.theme.ShiftOvertimeAmber
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val LEAD_TIME_PRESETS_MINUTES = listOf(30, 60, 90, 120, 180)

/**
 * Material 3 card for managing all notification preferences and shift reminder alerts:
 * - Permission & system channel readiness status
 * - Pre-shift alert toggle & quick lead-time selector
 * - Daily schedule reminder toggle & customizable reminder time
 * - Night shift quiet mode (daytime rest DND)
 * - Shared day-off alerts for paired colleagues
 * - Live upcoming scheduled alert summary & test notification trigger
 */
@Composable
fun NotificationPreferencesCard(
    settings: UserSettings,
    repository: ShiftRepository,
    onTogglePreShiftAlert: (Boolean) -> Unit,
    onSelectLeadTimeMinutes: (Int) -> Unit,
    onOpenLeadTimeDialog: () -> Unit,
    onToggleDailyReminder: (Boolean) -> Unit,
    onEditDailyReminderTime: () -> Unit,
    onToggleNightDnd: (Boolean) -> Unit,
    onToggleCommonLeaveAlert: (Boolean) -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onOpenSoundSettings: () -> Unit,
    onSendTestNotification: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dyn = DynamicTheme.colors
    val colorScheme = MaterialTheme.colorScheme
    val appLocale = LocalAppLocale.current
    val activeBadgeBg = if (dyn.isDark) Color(0xFF082F49) else dyn.primaryNeon.copy(alpha = 0.14f)

    val hasPermission = ShiftNotificationHelper.hasNotificationPermission(context)
    val systemEnabled = ShiftNotificationHelper.areSystemNotificationsEnabled(context)
    val channelMuted = ShiftNotificationHelper.isAlertChannelMutedOrSilent(context)

    val nextPreShiftLabel = remember(
        settings.preShiftAlertEnabled,
        settings.preShiftAlertMinutes,
        settings.cyclePreset,
        settings.referenceStartDate,
        settings.gunduzStartTime,
        settings.geceStartTime,
        settings.sabahStartTime,
        settings.aksamStartTime,
        settings.gece8StartTime,
        appLocale
    ) {
        if (!settings.preShiftAlertEnabled) {
            null
        } else {
            val nextTrigger = ShiftReminderScheduler.computeNextPreShiftTrigger(
                repository = repository,
                leadMinutes = settings.preShiftAlertMinutes,
                now = LocalDateTime.now()
            )
            nextTrigger?.format(DateTimeFormatter.ofPattern("d MMM, HH:mm", appLocale))
        }
    }

    val nextDailyLabel = remember(
        settings.shiftReminderEnabled,
        settings.shiftReminderTime,
        appLocale
    ) {
        if (!settings.shiftReminderEnabled) {
            null
        } else {
            ShiftReminderScheduler.computeNextDailyTrigger(
                timeText = settings.shiftReminderTime,
                now = LocalDateTime.now()
            ).format(DateTimeFormatter.ofPattern("d MMM, HH:mm", appLocale))
        }
    }

    val activeAlertsCount = listOf(
        settings.preShiftAlertEnabled,
        settings.shiftReminderEnabled,
        settings.nightDndEnabled,
        settings.commonLeaveAlertEnabled
    ).count { it }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .titaniumBrushedSurface(RoundedCornerShape(22.dp), dyn.borderSubtle)
            .testTag("card_notification_preferences"),
        shape = RoundedCornerShape(22.dp),
        color = Color.Transparent
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(activeBadgeBg)
                            .border(1.dp, dyn.primaryNeon.copy(alpha = 0.45f), RoundedCornerShape(11.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (activeAlertsCount > 0) Icons.Filled.NotificationsActive else Icons.Filled.NotificationsOff,
                            contentDescription = trEn("Bildirimler ve Vardiya Uyarıları", "Notifications & Shift Alerts"),
                            tint = dyn.accentCyan,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = trEn("Bildirim & Vardiya Uyarıları", "Notifications & Shift Alerts"),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = dyn.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = trEn(
                                "$activeAlertsCount/4 uyarı tercihi aktif",
                                "$activeAlertsCount/4 alert preferences active"
                            ),
                            fontSize = 11.sp,
                            color = dyn.textSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = dyn.surfaceElevated,
                    border = BorderStroke(1.dp, dyn.accentCyan.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onOpenSoundSettings() }
                        .testTag("btn_notification_sound_settings")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = trEn("Bildirim Sesi Ayarı", "Notification Sound Settings"),
                            tint = dyn.accentCyan,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = trEn("Ses", "Sound"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.accentCyan
                        )
                    }
                }
            }

            // Permission / System Channel Status Banner when attention is needed
            if (!hasPermission || !systemEnabled || channelMuted) {
                Spacer(modifier = Modifier.height(12.dp))
                val isCritical = !hasPermission || !systemEnabled
                val bannerAccent = if (isCritical) colorScheme.error else ShiftOvertimeAmber
                val bannerBg = if (isCritical) {
                    colorScheme.errorContainer.copy(alpha = if (dyn.isDark) 0.38f else 0.75f)
                } else {
                    dyn.eveningShiftBg
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = bannerBg,
                    border = BorderStroke(1.dp, bannerAccent.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            if (!hasPermission) onRequestNotificationPermission() else onOpenSoundSettings()
                        }
                        .testTag("banner_notification_status")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.WarningAmber,
                            contentDescription = null,
                            tint = bannerAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = when {
                                !hasPermission -> trEn(
                                    "Uyarıları alabilmek için bildirim iznine dokunarak onay verin.",
                                    "Tap to grant notification permission so shift alerts can notify you."
                                )
                                !systemEnabled -> trEn(
                                    "Android bildirimleri kapalı. Kanal ayarlarını açmak için dokunun.",
                                    "Android notifications are off. Tap to open notification settings."
                                )
                                else -> trEn(
                                    "Cihaz veya bildirim kanalı sessiz/titreşim modunda.",
                                    "Device or alert channel is currently in silent/vibrate mode."
                                )
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = dyn.textPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Next Scheduled Alerts Summary Strip
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = dyn.surfaceElevated,
                border = BorderStroke(1.dp, dyn.borderSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_next_scheduled_alerts")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = trEn("SONRAKİ ÖN UYARI", "NEXT PRE-SHIFT"),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp,
                            color = dyn.textMuted
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = nextPreShiftLabel ?: trEn("Kapalı", "Paused"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = ProshiftMonoFont,
                            color = if (nextPreShiftLabel != null) dyn.accentCyan else dyn.textMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(28.dp)
                            .background(dyn.borderSubtle)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = trEn("GÜNLÜK HATIRLATICI", "DAILY REMINDER"),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp,
                            color = dyn.textMuted
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = nextDailyLabel ?: trEn("Kapalı", "Paused"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = ProshiftMonoFont,
                            color = if (nextDailyLabel != null) dyn.primaryNeon else dyn.textMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Pre-Shift Alert Toggle
            NotificationPreferenceToggleRow(
                icon = Icons.Filled.Alarm,
                iconTint = dyn.accentCyan,
                title = trEn("Vardiya Öncesi Uyarı", "Pre-Shift Alert"),
                subtitle = trEn(
                    "Vardiyadan ${settings.preShiftAlertMinutes} dk önce hatırlat.",
                    "Remind ${settings.preShiftAlertMinutes} min before shift starts."
                ),
                checked = settings.preShiftAlertEnabled,
                switchTag = "switch_pre_shift_alert",
                onCheckedChange = onTogglePreShiftAlert
            )

            // Quick Lead-Time Chips
            if (settings.preShiftAlertEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(LEAD_TIME_PRESETS_MINUTES) { minutes ->
                        val selected = settings.preShiftAlertMinutes == minutes
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) activeBadgeBg else dyn.surfaceElevated,
                            border = BorderStroke(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) dyn.accentCyan else dyn.borderSubtle
                            ),
                            modifier = Modifier
                                .heightIn(min = 38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSelectLeadTimeMinutes(minutes) }
                                .testTag("chip_lead_time_$minutes")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = trEn("${minutes} dk", "${minutes}m"),
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Black else FontWeight.SemiBold,
                                    fontFamily = ProshiftMonoFont,
                                    color = if (selected) dyn.accentCyan else dyn.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = dyn.borderSubtle.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(12.dp))

            // 2. Daily Shift Reminder Toggle + Editable Reminder Time
            NotificationPreferenceToggleRow(
                icon = Icons.Filled.AccessAlarm,
                iconTint = dyn.primaryNeon,
                title = trEn("Günlük Vardiya Hatırlatıcısı", "Daily Shift Reminder"),
                subtitle = trEn(
                    "Her gün ${settings.shiftReminderTime} saatinde yarınki vardiyanı bildir.",
                    "Daily summary of tomorrow's shift at ${settings.shiftReminderTime}."
                ),
                checked = settings.shiftReminderEnabled,
                switchTag = "switch_daily_shift_reminder",
                onCheckedChange = onToggleDailyReminder
            )

            if (settings.shiftReminderEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = dyn.surfaceElevated,
                    border = BorderStroke(1.dp, dyn.primaryNeon.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onEditDailyReminderTime() }
                        .testTag("btn_daily_reminder_time")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = trEn("GÜNLÜK HATIRLATMA SAATİ", "DAILY REMINDER TIME"),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = dyn.textMuted
                            )
                            Text(
                                text = settings.shiftReminderTime,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = ProshiftMonoFont,
                                color = dyn.primaryNeon
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = trEn("Değiştir", "Change"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = dyn.accentCyan
                            )
                            Icon(
                                imageVector = Icons.Filled.Schedule,
                                contentDescription = trEn("Hatırlatma Saatini Değiştir", "Change Reminder Time"),
                                tint = dyn.accentCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = dyn.borderSubtle.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(12.dp))

            // 3. Night Shift Quiet Mode Toggle
            NotificationPreferenceToggleRow(
                icon = Icons.Filled.DoNotDisturbOn,
                iconTint = dyn.nightShiftAccent,
                title = trEn("Gece Vardiyası Sessiz Mod", "Night Shift Quiet Mode"),
                subtitle = trEn(
                    "Gündüz dinlenmesinde bildirimleri sessize al.",
                    "Silence alerts during daytime rest after night shifts."
                ),
                checked = settings.nightDndEnabled,
                switchTag = "switch_night_dnd",
                onCheckedChange = onToggleNightDnd
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = dyn.borderSubtle.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(12.dp))

            // 4. Shared Day-Off / Colleague Matching Alerts Toggle
            NotificationPreferenceToggleRow(
                icon = Icons.Filled.Groups,
                iconTint = dyn.emeraldAccent,
                title = trEn("Ortak İzin & Eşleşme Uyarıları", "Shared Day-Off Alerts"),
                subtitle = trEn(
                    "Eşleştiğin ekip arkadaşlarınla ortak izin günlerini hatırlat.",
                    "Remind when you and paired colleagues share a day off."
                ),
                checked = settings.commonLeaveAlertEnabled,
                switchTag = "switch_common_leave_alert",
                onCheckedChange = onToggleCommonLeaveAlert
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Action Row: Lead Time Selector + Test Alert Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .heightIn(min = 54.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOpenLeadTimeDialog() }
                        .testTag("btn_alert_lead_time"),
                    shape = RoundedCornerShape(12.dp),
                    color = dyn.surfaceElevated,
                    border = BorderStroke(1.dp, dyn.accentCyan.copy(alpha = 0.45f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = trEn("HATIRLATMA SÜRESİ", "LEAD TIME"),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.textMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = trEn("${settings.preShiftAlertMinutes} dk", "${settings.preShiftAlertMinutes} min"),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = dyn.accentCyan,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = trEn("önce", "before"),
                                fontSize = 11.sp,
                                color = dyn.textSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxHeight()
                        .heightIn(min = 54.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSendTestNotification() }
                        .testTag("btn_test_notification"),
                    shape = RoundedCornerShape(12.dp),
                    color = dyn.surfaceElevated,
                    border = BorderStroke(1.dp, dyn.borderMetal)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = trEn("Test Bildirimi", "Test Alert"),
                            tint = dyn.accentCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = trEn("Test Bildirimi (${settings.shiftReminderTime})", "Test Alert (${settings.shiftReminderTime})"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationPreferenceToggleRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    switchTag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    val dyn = DynamicTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = if (dyn.isDark) 0.16f else 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = dyn.textPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = dyn.textSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(switchTag),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = dyn.primaryNeon,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = dyn.surfacePanel
            )
        )
    }
}
