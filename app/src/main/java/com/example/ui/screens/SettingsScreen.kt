package com.example.ui.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.engine.ShiftEngine
import com.example.data.model.AppThemeMode
import com.example.data.model.CyclePreset
import com.example.data.model.ShiftType
import com.example.data.notification.NotificationDispatchResult
import com.example.data.notification.ShiftNotificationHelper
import com.example.ui.components.CustomCycleBuilderSheet
import com.example.ui.components.NotificationPreferencesCard
import com.example.ui.components.ProShiftBrandHeader
import com.example.ui.components.ShiftTimePickerSheet
import com.example.ui.components.TitaniumDatePickerDialog
import com.example.ui.components.UserAvatarImage
import com.example.ui.components.titaniumBrushedSurface
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalAppLocale
import com.example.ui.i18n.LocalIsEnglish
import com.example.ui.i18n.formatHourUnit
import com.example.ui.i18n.localizedCode
import com.example.ui.i18n.localizedDescription
import com.example.ui.i18n.localizedName
import com.example.ui.i18n.localizedNotice
import com.example.ui.i18n.localizedTitle
import com.example.ui.i18n.trEn
import androidx.compose.ui.platform.LocalConfiguration
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BackgroundTitanium
import com.example.ui.theme.BorderMetal
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DynamicTheme
import com.example.ui.theme.PrimaryNeon
import com.example.ui.theme.ProshiftMonoFont
import com.example.ui.theme.ShiftGunduz
import com.example.ui.theme.ShiftOvertimeAmber
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfacePanel
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.ShiftViewModel
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class SettingsTimeTarget {
    GUNDUZ_START,
    GUNDUZ_END,
    GECE_START,
    GECE_END,
    SABAH_START,
    SABAH_END,
    AKSAM_START,
    AKSAM_END,
    GECE8_START,
    GECE8_END,
    DAILY_REMINDER_TIME
}

@Composable
fun SettingsScreen(
    viewModel: ShiftViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current
    val settings by viewModel.userSettings.collectAsState()

    var showResetDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showCustomBuilder by remember { mutableStateOf(false) }
    var customBuilderPresetTarget by remember { mutableStateOf(CyclePreset.CUSTOM) }
    var showWindowPickerDialog by remember { mutableStateOf(false) }
    var showBackupRestoreDialog by remember { mutableStateOf(false) }
    var restoreJsonInput by remember { mutableStateOf("") }
    var activeTimeTarget by remember { mutableStateOf<SettingsTimeTarget?>(null) }
    var showAllTimeSlices by remember { mutableStateOf(false) }
    var showAllTemplates by remember { mutableStateOf(false) }
    var pendingNotificationAction by remember { mutableStateOf<String?>(null) }

    val isEn = LocalIsEnglish.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        val action = pendingNotificationAction
        pendingNotificationAction = null
        if (granted) {
            when (action) {
                "test" -> {
                    val result = viewModel.testReminderNotification()
                    if (result is NotificationDispatchResult.SystemDisabled) {
                        viewModel.openNotificationSoundSettings()
                    }
                }
                "enable_preshift" -> {
                    viewModel.updateSettings(settings.copy(preShiftAlertEnabled = true))
                }
                "enable_daily" -> {
                    viewModel.updateSettings(settings.copy(shiftReminderEnabled = true))
                }
                "request_only" -> {
                    Toast.makeText(
                        context,
                        trEn(isEn, "Bildirim izni verildi", "Notification permission granted"),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            Toast.makeText(
                context,
                trEn(isEn, "Bildirim izni verilmedi. Android ayarlarından açabilirsiniz.", "Notification permission denied. You can enable it in Android settings."),
                Toast.LENGTH_LONG
            ).show()
        }
    }
    val appLocale = LocalAppLocale.current
    val configuration = LocalConfiguration.current
    val systemLocale = remember(configuration) {
        configuration.locales.let { if (!it.isEmpty) it[0] else Locale.getDefault() } ?: Locale.getDefault()
    }
    val systemIsEn = remember(systemLocale) {
        AppLanguage.resolveIsEnglish(AppLanguage.SYSTEM.code, systemLocale)
    }
    val currentLanguage = remember(settings.appLanguage) {
        AppLanguage.fromCode(settings.appLanguage)
    }
    val hourUnit = formatHourUnit(isEn)

    val activeSequence = remember(settings.cyclePreset, settings.customSequence) {
        ShiftEngine.resolvePattern(settings)
    }
    val has8hShifts = remember(activeSequence) {
        activeSequence.any { it == ShiftType.SABAH || it == ShiftType.AKSAM || it == ShiftType.GECE8 }
    }
    val anchorDateLabel = remember(settings.referenceStartDate, appLocale) {
        settings.referenceStartDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy, EEEE", appLocale))
    }

    val dyn = DynamicTheme.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(dyn.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
            .imePadding()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(bottom = 96.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProShiftBrandHeader(
                sectionLabel = trEn("AYARLAR", "SETTINGS")
            )

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable { viewModel.navigateTo(AppScreen.PROFILE) },
                contentAlignment = Alignment.Center
            ) {
                UserAvatarImage(
                    avatarPath = settings.avatarUriOrPath,
                    contentDescription = trEn("Profil", "Profile"),
                    displayName = settings.employeeName,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, dyn.borderMetal, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        val isLightMode = !dyn.isDark
        val currentThemeMode = if (isLightMode) AppThemeMode.LIGHT else AppThemeMode.DARK
        val activeBadgeBg = if (dyn.isDark) Color(0xFF082F49) else dyn.primaryNeon.copy(alpha = 0.14f)

        // Card -1: TEMA MODU / THEME MODE (Light / Dark)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .titaniumBrushedSurface(RoundedCornerShape(20.dp), dyn.borderSubtle)
                .testTag("card_theme_mode"),
            shape = RoundedCornerShape(20.dp),
            color = Color.Transparent
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(activeBadgeBg)
                            .border(1.dp, dyn.primaryNeon.copy(alpha = 0.45f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isLightMode) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                            contentDescription = trEn("Tema Modu", "Theme Mode"),
                            tint = if (isLightMode) ShiftOvertimeAmber else dyn.accentCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = trEn("Tema Modu", "Theme Mode"),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = dyn.textPrimary
                        )
                        Text(
                            text = if (isLightMode) {
                                trEn("Açık Mod • Gündüz okuması için yüksek netlik", "Light Mode • High clarity for daytime reading")
                            } else {
                                trEn("Koyu Mod • Gece nöbetlerinde göz dostu kontrast", "Dark Mode • Eye-friendly contrast for night shifts")
                            },
                            fontSize = 11.sp,
                            color = dyn.textSecondary
                        )
                    }
                    Switch(
                        checked = isLightMode,
                        onCheckedChange = { lightSelected ->
                            viewModel.updateThemeMode(if (lightSelected) AppThemeMode.LIGHT else AppThemeMode.DARK)
                        },
                        modifier = Modifier.testTag("settings_theme_mode_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = dyn.primaryNeon,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = dyn.surfacePanel
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val themeOptions = listOf(
                        Triple(AppThemeMode.LIGHT, trEn("Açık Mod", "Light"), "theme_option_light"),
                        Triple(AppThemeMode.DARK, trEn("Koyu Mod", "Dark"), "theme_option_dark")
                    )
                    themeOptions.forEach { (mode, label, tag) ->
                        val selected = currentThemeMode == mode
                        val optionIcon = if (mode == AppThemeMode.LIGHT) Icons.Rounded.LightMode else Icons.Rounded.DarkMode
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = if (selected) 1.5.dp else 1.dp,
                                    color = if (selected) dyn.accentCyan else dyn.borderSubtle,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.updateThemeMode(mode) }
                                .testTag(tag),
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) activeBadgeBg else dyn.surfaceElevated
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = optionIcon,
                                    contentDescription = label,
                                    tint = if (selected) dyn.accentCyan else dyn.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Black else FontWeight.SemiBold,
                                    color = if (selected) dyn.accentCyan else dyn.textSecondary,
                                    maxLines = 1
                                )
                                if (selected) {
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = dyn.accentCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Card 0: BİLDİRİM & VARDİYA UYARILARI (Notifications & Shift Reminder Alerts)
        NotificationPreferencesCard(
            settings = settings,
            repository = viewModel.repository,
            onTogglePreShiftAlert = { enabled ->
                if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !ShiftNotificationHelper.hasNotificationPermission(context)) {
                    pendingNotificationAction = "enable_preshift"
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    viewModel.updateSettings(settings.copy(preShiftAlertEnabled = enabled))
                }
            },
            onSelectLeadTimeMinutes = { minutes ->
                viewModel.updateSettings(settings.copy(preShiftAlertMinutes = minutes))
            },
            onOpenLeadTimeDialog = { showWindowPickerDialog = true },
            onToggleDailyReminder = { enabled ->
                if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !ShiftNotificationHelper.hasNotificationPermission(context)) {
                    pendingNotificationAction = "enable_daily"
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    viewModel.updateSettings(settings.copy(shiftReminderEnabled = enabled))
                }
            },
            onEditDailyReminderTime = { activeTimeTarget = SettingsTimeTarget.DAILY_REMINDER_TIME },
            onToggleNightDnd = { enabled ->
                viewModel.updateSettings(settings.copy(nightDndEnabled = enabled))
            },
            onToggleCommonLeaveAlert = { enabled ->
                viewModel.updateSettings(settings.copy(commonLeaveAlertEnabled = enabled))
            },
            onRequestNotificationPermission = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pendingNotificationAction = "request_only"
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    viewModel.openNotificationSoundSettings()
                }
            },
            onOpenSoundSettings = { viewModel.openNotificationSoundSettings() },
            onSendTestNotification = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !ShiftNotificationHelper.hasNotificationPermission(context)) {
                    pendingNotificationAction = "test"
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    val result = viewModel.testReminderNotification()
                    if (result is NotificationDispatchResult.SystemDisabled) {
                        viewModel.openNotificationSoundSettings()
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Card 1: UYGULAMA DİLİ / APP LANGUAGE
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .titaniumBrushedSurface(RoundedCornerShape(20.dp), dyn.borderSubtle)
                .testTag("card_app_language"),
            shape = RoundedCornerShape(20.dp),
            color = Color.Transparent
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(activeBadgeBg)
                            .border(1.dp, dyn.primaryNeon.copy(alpha = 0.45f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Language,
                            contentDescription = trEn("Uygulama Dili", "App Language"),
                            tint = dyn.accentCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = trEn("Uygulama Dili", "App Language"),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = dyn.textPrimary
                        )
                        val systemLangDisplay = if (systemIsEn) "English" else "Türkçe"
                        val langSubtitle = when (currentLanguage) {
                            AppLanguage.SYSTEM -> trEn(
                                "Otomatik (Sistem: $systemLangDisplay)",
                                "Automatic (System: $systemLangDisplay)"
                            )
                            AppLanguage.TURKISH -> trEn("Seçili: Türkçe", "Selected: Türkçe")
                            AppLanguage.ENGLISH -> trEn("Seçili: English", "Selected: English")
                        }
                        Text(
                            text = langSubtitle,
                            fontSize = 11.sp,
                            color = dyn.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val langOptions = listOf(
                        Triple(AppLanguage.SYSTEM, trEn("Otomatik", "Auto"), "lang_option_system"),
                        Triple(AppLanguage.TURKISH, "Türkçe", "lang_option_tr"),
                        Triple(AppLanguage.ENGLISH, "English", "lang_option_en")
                    )
                    langOptions.forEach { (option, label, tag) ->
                        val selected = currentLanguage == option
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = if (selected) 1.5.dp else 1.dp,
                                    color = if (selected) dyn.accentCyan else dyn.borderSubtle,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.updateAppLanguage(option) }
                                .testTag(tag),
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) activeBadgeBg else dyn.surfaceElevated
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (selected) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = dyn.accentCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Black else FontWeight.SemiBold,
                                    color = if (selected) dyn.accentCyan else dyn.textSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Card 1: AKTİF ROTA PARAMETRESİ & SAATLER (Fully Dynamic)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .titaniumBrushedSurface(RoundedCornerShape(20.dp), dyn.borderSubtle),
            shape = RoundedCornerShape(20.dp),
            color = Color.Transparent
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(trEn("AKTİF VARDİYA DÖNGÜSÜ", "ACTIVE SHIFT CYCLE"), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = dyn.accentCyan)
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(dyn.surfaceElevated)
                            .border(1.dp, dyn.borderSubtle, RoundedCornerShape(8.dp))
                            .clickable { viewModel.openOnboardingWizard() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.SwapHoriz, contentDescription = trEn("Kurulum Sihirbazı", "Setup Wizard"), tint = dyn.accentCyan, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(settings.cyclePreset.localizedName(isEn), fontSize = 17.sp, fontWeight = FontWeight.Black, color = dyn.textPrimary)
                Text(settings.cyclePreset.localizedDescription(isEn), fontSize = 11.sp, color = dyn.textSecondary)

                settings.cyclePreset.localizedNotice(isEn)?.let { notice ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (dyn.isDark) Color(0xFF281D0A) else Color(0xFFFFFBEB),
                        border = BorderStroke(1.dp, ShiftOvertimeAmber.copy(alpha = 0.55f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = notice,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (dyn.isDark) Color(0xFFFEF3C7) else Color(0xFF92400E),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Dynamic Step Grid (Full sequence, scrollable if > 4 steps)
                val renderStepPill: @Composable (Int, ShiftType, Modifier) -> Unit = { idx, stepType, itemMod ->
                    val (accent, bg) = when {
                        stepType.isNight -> Color(0xFF6366F1) to (if (dyn.isDark) Color(0xFF1A1A3D) else Color(0xFFEEF2FF))
                        stepType == ShiftType.AKSAM -> ShiftOvertimeAmber to (if (dyn.isDark) Color(0xFF281D0A) else Color(0xFFFFFBEB))
                        stepType.isWork -> dyn.accentCyan to (if (dyn.isDark) Color(0xFF06293D) else Color(0xFFECFEFF))
                        else -> dyn.textSecondary to dyn.surfaceElevated
                    }
                    val hrs = when (stepType) {
                        ShiftType.GUNDUZ -> "${viewModel.repository.calculateDurationHours(settings.gunduzStartTime, settings.gunduzEndTime, stepType).toInt()}$hourUnit"
                        ShiftType.GECE -> "${viewModel.repository.calculateDurationHours(settings.geceStartTime, settings.geceEndTime, stepType).toInt()}$hourUnit"
                        ShiftType.SABAH -> "${viewModel.repository.calculateDurationHours(settings.sabahStartTime, settings.sabahEndTime, stepType).toInt()}$hourUnit"
                        ShiftType.AKSAM -> "${viewModel.repository.calculateDurationHours(settings.aksamStartTime, settings.aksamEndTime, stepType).toInt()}$hourUnit"
                        ShiftType.GECE8 -> "${viewModel.repository.calculateDurationHours(settings.gece8StartTime, settings.gece8EndTime, stepType).toInt()}$hourUnit"
                        ShiftType.IZIN, ShiftType.RAPOR -> trEn(isEn, "İzin", "Off")
                        else -> "${stepType.defaultDuration.toInt()}$hourUnit"
                    }
                    StepPillBox(
                        stepName = trEn(isEn, "GÜN %02d", "DAY %02d").format(idx + 1),
                        title = stepType.localizedTitle(isEn).substringBefore(" ("),
                        duration = hrs,
                        icon = stepType.iconEmoji,
                        accentColor = accent,
                        bgColor = bg,
                        modifier = itemMod
                    )
                }

                if (activeSequence.size <= 4) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        activeSequence.forEachIndexed { idx, stepType ->
                            renderStepPill(idx, stepType, Modifier.weight(1f))
                        }
                    }
                } else {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        itemsIndexed(activeSequence) { idx, stepType ->
                            renderStepPill(idx, stepType, Modifier.width(74.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Interactive DÖNGÜ ÇAPA TARİHİ
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showDatePicker = true }
                        .testTag("btn_settings_anchor_date"),
                    shape = RoundedCornerShape(12.dp),
                    color = dyn.surfaceElevated,
                    border = BorderStroke(1.dp, dyn.accentCyan.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(trEn("Döngü Başlangıç Tarihi", "Cycle Start Date"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = dyn.textMuted)
                            Text(anchorDateLabel, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = dyn.textPrimary)
                        }
                        Icon(Icons.Filled.CalendarMonth, contentDescription = trEn("Tarih Seç", "Pick Date"), tint = dyn.accentCyan, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Interactive Shift Hours Grid (8h Sabah/Akşam/Gece & 12h Gündüz/Gece)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (has8hShifts) trEn("8 SAATLİK VARDİYA SAATLERİ", "8-HOUR SHIFT TIMES") else trEn("VARDİYA SAAT DİLİMLERİ", "SHIFT TIME SLOTS"),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = dyn.accentCyan,
                        letterSpacing = 0.8.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = dyn.surfaceElevated,
                        border = BorderStroke(1.dp, dyn.accentCyan.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showAllTimeSlices = !showAllTimeSlices }
                            .testTag("btn_toggle_all_time_slices")
                    ) {
                        Text(
                            text = if (showAllTimeSlices) trEn("Sadece Aktif", "Active Only") else trEn("Tüm Saatleri Göster", "Show All Times"),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.accentCyan,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (has8hShifts || showAllTimeSlices) {
                    Text(trEn("ÜÇLÜ SİSTEM (SABAH / AKŞAM / GECE 8s)", "3-SHIFT SYSTEM (MORNING / EVENING / NIGHT 8h)"), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = dyn.textSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SettingsTimeTile(
                            label = trEn("Sabah Başlangıç", "Morning Start"),
                            time = settings.sabahStartTime,
                            accent = ShiftGunduz,
                            testTag = "settings_sabah_start",
                            modifier = Modifier.weight(1f),
                            onClick = { activeTimeTarget = SettingsTimeTarget.SABAH_START }
                        )
                        SettingsTimeTile(
                            label = trEn("Sabah Bitiş", "Morning End"),
                            time = settings.sabahEndTime,
                            accent = ShiftGunduz,
                            testTag = "settings_sabah_end",
                            modifier = Modifier.weight(1f),
                            onClick = { activeTimeTarget = SettingsTimeTarget.SABAH_END }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SettingsTimeTile(
                            label = trEn("Akşam Başlangıç", "Evening Start"),
                            time = settings.aksamStartTime,
                            accent = ShiftOvertimeAmber,
                            testTag = "settings_aksam_start",
                            modifier = Modifier.weight(1f),
                            onClick = { activeTimeTarget = SettingsTimeTarget.AKSAM_START }
                        )
                        SettingsTimeTile(
                            label = trEn("Akşam Bitiş", "Evening End"),
                            time = settings.aksamEndTime,
                            accent = ShiftOvertimeAmber,
                            testTag = "settings_aksam_end",
                            modifier = Modifier.weight(1f),
                            onClick = { activeTimeTarget = SettingsTimeTarget.AKSAM_END }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SettingsTimeTile(
                            label = trEn("Gece (8s) Başlangıç", "Night (8h) Start"),
                            time = settings.gece8StartTime,
                            accent = Color(0xFF818CF8),
                            testTag = "settings_gece8_start",
                            modifier = Modifier.weight(1f),
                            onClick = { activeTimeTarget = SettingsTimeTarget.GECE8_START }
                        )
                        SettingsTimeTile(
                            label = trEn("Gece (8s) Bitiş", "Night (8h) End"),
                            time = settings.gece8EndTime,
                            accent = Color(0xFF818CF8),
                            testTag = "settings_gece8_end",
                            modifier = Modifier.weight(1f),
                            onClick = { activeTimeTarget = SettingsTimeTarget.GECE8_END }
                        )
                    }

                    if (showAllTimeSlices) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                if (!has8hShifts || showAllTimeSlices) {
                    Text(trEn("12 SAATLİK SİSTEM (GÜNDÜZ / GECE)", "12-HOUR SYSTEM (DAY / NIGHT)"), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = dyn.textSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SettingsTimeTile(
                            label = trEn("Gündüz Başlangıç", "Day Start"),
                            time = settings.gunduzStartTime,
                            accent = ShiftGunduz,
                            testTag = "settings_gunduz_start",
                            modifier = Modifier.weight(1f),
                            onClick = { activeTimeTarget = SettingsTimeTarget.GUNDUZ_START }
                        )
                        SettingsTimeTile(
                            label = trEn("Gündüz Bitiş", "Day End"),
                            time = settings.gunduzEndTime,
                            accent = ShiftGunduz,
                            testTag = "settings_gunduz_end",
                            modifier = Modifier.weight(1f),
                            onClick = { activeTimeTarget = SettingsTimeTarget.GUNDUZ_END }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SettingsTimeTile(
                            label = trEn("Gece Başlangıç", "Night Start"),
                            time = settings.geceStartTime,
                            accent = Color(0xFF818CF8),
                            testTag = "settings_gece_start",
                            modifier = Modifier.weight(1f),
                            onClick = { activeTimeTarget = SettingsTimeTarget.GECE_START }
                        )
                        SettingsTimeTile(
                            label = trEn("Gece Bitiş", "Night End"),
                            time = settings.geceEndTime,
                            accent = Color(0xFF818CF8),
                            testTag = "settings_gece_end",
                            modifier = Modifier.weight(1f),
                            onClick = { activeTimeTarget = SettingsTimeTarget.GECE_END }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, dyn.borderSubtle, RoundedCornerShape(12.dp))
                        .clickable { showResetDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    color = dyn.surfaceElevated
                ) {
                    Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.Filled.Refresh, contentDescription = trEn("Sıfırla", "Reset"), tint = dyn.accentCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(trEn("Döngüyü Sıfırla veya Yeniden Başlat", "Reset or Restart Cycle"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = dyn.accentCyan)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card 2: Hazır Vardiya Şablonları (Compact + Expandable)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(trEn("Vardiya Şablonları", "Shift Templates"), fontSize = 16.sp, fontWeight = FontWeight.Black, color = dyn.textPrimary)
                    Text(trEn("Tek dokunuşla döngüyü değiştir", "Switch rotation with one tap"), fontSize = 11.sp, color = dyn.textSecondary)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = dyn.surfaceElevated,
                    border = BorderStroke(1.dp, dyn.accentCyan.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showAllTemplates = !showAllTemplates }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (showAllTemplates) trEn("Daha Az", "Show Less") else trEn("Tümünü Gör", "See All"),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.accentCyan
                        )
                        Icon(
                            imageVector = if (showAllTemplates) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = null,
                            tint = dyn.accentCyan,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val activeBadge = trEn("AKTİF", "ACTIVE")
            val dayLabel = trEn("Gün", "Days")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TemplateGridTile(
                    badgeText = if (settings.cyclePreset == CyclePreset.CYCLE_12_36) activeBadge else trEn("STANDART", "STANDARD"),
                    isActive = settings.cyclePreset == CyclePreset.CYCLE_12_36,
                    title = trEn("12/36 Sistemi", "12/36 System"),
                    description = trEn("12s gündüz, 12s gece, 1 gün izin", "12h day, 12h night, 1 day off"),
                    footerText = "3 $dayLabel",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.updateCyclePreset(CyclePreset.CYCLE_12_36) }
                )

                TemplateGridTile(
                    badgeText = if (settings.cyclePreset == CyclePreset.CYCLE_12_24_12_48) activeBadge else trEn("GÜVENLİK", "SECURITY"),
                    isActive = settings.cyclePreset == CyclePreset.CYCLE_12_24_12_48,
                    title = "12/24 - 12/48",
                    description = trEn("Gündüz, izin, gece, 2 gün izin", "Day, off, night, 2 days off"),
                    footerText = "5 $dayLabel",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.updateCyclePreset(CyclePreset.CYCLE_12_24_12_48) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TemplateGridTile(
                    badgeText = if (settings.cyclePreset == CyclePreset.CYCLE_24_48) activeBadge else trEn("24 SAAT", "24 HOUR"),
                    isActive = settings.cyclePreset == CyclePreset.CYCLE_24_48,
                    title = trEn("24/48 Sistemi", "24/48 System"),
                    description = trEn("24s tam nöbet, 2 gün dinlenme", "24h full duty, 2 days rest"),
                    footerText = "3 $dayLabel",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.updateCyclePreset(CyclePreset.CYCLE_24_48) }
                )

                TemplateGridTile(
                    badgeText = if (settings.cyclePreset == CyclePreset.CYCLE_8_2_2_2) activeBadge else trEn("8 SAAT", "8 HOUR"),
                    isActive = settings.cyclePreset == CyclePreset.CYCLE_8_2_2_2,
                    title = trEn("2-2-2 Üçlü Vardiya", "2-2-2 Triple Shift"),
                    description = trEn("2 Sabah, 2 Akşam, 2 Gece (00-08)", "2 Morning, 2 Evening, 2 Night"),
                    footerText = "6 $dayLabel",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.updateCyclePreset(CyclePreset.CYCLE_8_2_2_2) }
                )
            }

            if (showAllTemplates || settings.cyclePreset !in listOf(
                    CyclePreset.CYCLE_12_36,
                    CyclePreset.CYCLE_12_24_12_48,
                    CyclePreset.CYCLE_24_48,
                    CyclePreset.CYCLE_8_2_2_2,
                    CyclePreset.CUSTOM
                )
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TemplateGridTile(
                        badgeText = if (settings.cyclePreset == CyclePreset.CYCLE_8_3) activeBadge else trEn("SANAYİ", "INDUSTRY"),
                        isActive = settings.cyclePreset == CyclePreset.CYCLE_8_3,
                        title = trEn("8s 3 Vardiya", "8h 3-Shift"),
                        description = trEn("Sabah, akşam, gece ve izin", "Morning, evening, night & off"),
                        footerText = "08/16/24",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.updateCyclePreset(CyclePreset.CYCLE_8_3) }
                    )

                    TemplateGridTile(
                        badgeText = if (settings.cyclePreset == CyclePreset.CYCLE_8_2_2_2_2) activeBadge else trEn("8 SAAT", "8 HOUR"),
                        isActive = settings.cyclePreset == CyclePreset.CYCLE_8_2_2_2_2,
                        title = trEn("2-2-2-2 Üçlü + İzin", "2-2-2-2 Triple + Off"),
                        description = trEn("2 Sabah, 2 Akşam, 2 Gece, 2 İzin", "2 Morning, 2 Evening, 2 Night, 2 Off"),
                        footerText = "8 $dayLabel",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.updateCyclePreset(CyclePreset.CYCLE_8_2_2_2_2) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TemplateGridTile(
                        badgeText = if (settings.cyclePreset == CyclePreset.CYCLE_2_2_2) activeBadge else trEn("12 SAAT", "12 HOUR"),
                        isActive = settings.cyclePreset == CyclePreset.CYCLE_2_2_2,
                        title = trEn("2-2-2 (12s)", "2-2-2 (12h)"),
                        description = trEn("2 Gündüz, 2 Gece, 2 İzin", "2 Day, 2 Night, 2 Off"),
                        footerText = "6 $dayLabel",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.updateCyclePreset(CyclePreset.CYCLE_2_2_2) }
                    )

                    TemplateGridTile(
                        badgeText = if (settings.cyclePreset == CyclePreset.CYCLE_4_4) activeBadge else trEn("12 SAAT", "12 HOUR"),
                        isActive = settings.cyclePreset == CyclePreset.CYCLE_4_4,
                        title = trEn("4-4 Sistemi (12s)", "4-4 System (12h)"),
                        description = trEn("4 Gündüz (12s), 4 İzin", "4 Day (12h), 4 Off"),
                        footerText = "8 $dayLabel",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.updateCyclePreset(CyclePreset.CYCLE_4_4) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TemplateGridTile(
                        badgeText = if (settings.cyclePreset == CyclePreset.CYCLE_PITMAN) activeBadge else trEn("14 GÜN", "14 DAYS"),
                        isActive = settings.cyclePreset == CyclePreset.CYCLE_PITMAN,
                        title = "Pitman / 2-2-3",
                        description = trEn("14 günlük 12 saat şablonu", "14-day 12-hour template"),
                        footerText = "14 $dayLabel",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.updateCyclePreset(CyclePreset.CYCLE_PITMAN) }
                    )

                    TemplateGridTile(
                        badgeText = if (settings.cyclePreset == CyclePreset.CYCLE_8_2_2_2_4) activeBadge else trEn("10 GÜN", "10 DAYS"),
                        isActive = settings.cyclePreset == CyclePreset.CYCLE_8_2_2_2_4,
                        title = "Continental 2-2-2-4",
                        description = trEn("2 Sabah, 2 Akşam, 2 Gece, 4 İzin", "2 Morning, 2 Evening, 2 Night, 4 Off"),
                        footerText = "10 $dayLabel",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.updateCyclePreset(CyclePreset.CYCLE_8_2_2_2_4) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TemplateGridTile(
                        badgeText = if (settings.cyclePreset == CyclePreset.CYCLE_SOUTHERN_SWING) activeBadge else trEn("28 GÜN", "28 DAYS"),
                        isActive = settings.cyclePreset == CyclePreset.CYCLE_SOUTHERN_SWING,
                        title = "Southern Swing",
                        description = trEn("7 Sabah, 2 İzin, 7 Akşam, 2 İzin...", "7 Morning, 2 Off, 7 Evening, 2 Off..."),
                        footerText = "28 $dayLabel",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.updateCyclePreset(CyclePreset.CYCLE_SOUTHERN_SWING) }
                    )

                    TemplateGridTile(
                        badgeText = if (settings.cyclePreset == CyclePreset.CYCLE_DUPONT_TEMPLATE) activeBadge else trEn("ÖZEL", "CUSTOM"),
                        isActive = settings.cyclePreset == CyclePreset.CYCLE_DUPONT_TEMPLATE,
                        title = trEn("DuPont (28g)", "DuPont (28d)"),
                        description = trEn("Kuruma göre dizi oluşturun", "Build sequence for your site"),
                        footerText = trEn("Düzenle", "Edit"),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            customBuilderPresetTarget = CyclePreset.CYCLE_DUPONT_TEMPLATE
                            showCustomBuilder = true
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                TemplateGridTile(
                    badgeText = if (settings.cyclePreset == CyclePreset.CYCLE_FIVE_TEAM_8H) activeBadge else trEn("5-EKİP ÖZEL", "5-TEAM CUSTOM"),
                    isActive = settings.cyclePreset == CyclePreset.CYCLE_FIVE_TEAM_8H,
                    title = trEn("5-Ekip 8 Saat Sistemi", "5-Team 8-Hour System"),
                    description = trEn("Ekibinizin rotasyon adımlarını kendiniz girin", "Enter your team rotation steps"),
                    footerText = trEn("Düzenle", "Edit"),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        customBuilderPresetTarget = CyclePreset.CYCLE_FIVE_TEAM_8H
                        showCustomBuilder = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // + Yeni Özel Döngü Oluştur BUILDER Button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, if (settings.cyclePreset == CyclePreset.CUSTOM) dyn.accentCyan else dyn.primaryNeon.copy(alpha = 0.65f), RoundedCornerShape(14.dp))
                    .clickable {
                        customBuilderPresetTarget = CyclePreset.CUSTOM
                        showCustomBuilder = true
                    },
                shape = RoundedCornerShape(14.dp),
                color = if (settings.cyclePreset == CyclePreset.CUSTOM) activeBadgeBg else dyn.surfaceCard
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.AddCircleOutline, contentDescription = trEn("Özel Döngü", "Custom Cycle"), tint = dyn.accentCyan, modifier = Modifier.size(18.dp))
                        Column {
                            Text(trEn("Özel Döngü Oluştur", "Build Custom Cycle"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = dyn.textPrimary)
                            val seqPreview = settings.customSequence.joinToString("-") { it.localizedCode(isEn) }
                            Text(
                                text = if (settings.cyclePreset == CyclePreset.CUSTOM) {
                                    trEn("Dizi: $seqPreview (${settings.customSequence.size} gün)", "Sequence: $seqPreview (${settings.customSequence.size} days)")
                                } else {
                                    trEn("Kendi vardiya sıranızı adım adım oluşturun", "Build your own shift sequence step by step")
                                },
                                fontSize = 10.sp,
                                color = if (settings.cyclePreset == CyclePreset.CUSTOM) dyn.accentCyan else dyn.textSecondary,
                                maxLines = 1
                            )
                        }
                    }
                    Surface(shape = RoundedCornerShape(6.dp), color = dyn.surfaceElevated) {
                        Text(
                            text = if (settings.cyclePreset == CyclePreset.CUSTOM) trEn("DÜZENLE", "EDIT") else trEn("OLUŞTUR", "BUILD"),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (settings.cyclePreset == CyclePreset.CUSTOM) dyn.accentCyan else dyn.textSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Card 4: Yedekleme
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(22.dp),
            color = dyn.surfaceCard
        ) {
            Column(
                modifier = Modifier
                    .border(1.dp, dyn.borderSubtle, RoundedCornerShape(22.dp))
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Lock, contentDescription = trEn("Yedekleme", "Backup"), tint = dyn.accentCyan, modifier = Modifier.size(18.dp))
                    Text(trEn("Yedekleme", "Backup & Restore"), fontSize = 15.sp, fontWeight = FontWeight.Black, color = dyn.textPrimary)
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(trEn("Tüm vardiya ve ayarlarınızı JSON olarak dışa aktarın veya geri yükleyin.", "Export or restore all your shifts and settings as JSON."), fontSize = 11.sp, color = dyn.textSecondary)

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, dyn.borderSubtle, RoundedCornerShape(12.dp))
                            .clickable {
                                val backup = viewModel.repository.exportFullBackupJson()
                                clipboard.setText(AnnotatedString(backup))
                                viewModel.sharePlainText(trEn(isEn, "PROSHIFT JSON Yedek", "PROSHIFT JSON Backup"), backup)
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = dyn.surfaceElevated
                    ) {
                        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(Icons.Filled.CloudDownload, contentDescription = trEn("Yedekle", "Export"), tint = dyn.accentCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(trEn("Dışa Aktar", "Export"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = dyn.textPrimary)
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, dyn.borderSubtle, RoundedCornerShape(12.dp))
                            .clickable {
                                restoreJsonInput = viewModel.repository.exportFullBackupJson()
                                showBackupRestoreDialog = true
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = dyn.surfaceElevated
                    ) {
                        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(Icons.Filled.Restore, contentDescription = trEn("Geri Yükle", "Restore"), tint = dyn.textSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(trEn("Geri Yükle", "Restore"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = dyn.textPrimary)
                        }
                    }
                }
            }
        }
    }

    // Time Pickers for Settings
    activeTimeTarget?.let { target ->
        val (title, init) = when (target) {
            SettingsTimeTarget.GUNDUZ_START -> trEn(isEn, "Gündüz Vardiyası Başlangıcı", "Day Shift Start") to settings.gunduzStartTime
            SettingsTimeTarget.GUNDUZ_END -> trEn(isEn, "Gündüz Vardiyası Bitişi", "Day Shift End") to settings.gunduzEndTime
            SettingsTimeTarget.GECE_START -> trEn(isEn, "Gece Vardiyası Başlangıcı", "Night Shift Start") to settings.geceStartTime
            SettingsTimeTarget.GECE_END -> trEn(isEn, "Gece Vardiyası Bitişi", "Night Shift End") to settings.geceEndTime
            SettingsTimeTarget.SABAH_START -> trEn(isEn, "Sabah Vardiyası Başlangıcı", "Morning Shift Start") to settings.sabahStartTime
            SettingsTimeTarget.SABAH_END -> trEn(isEn, "Sabah Vardiyası Bitişi", "Morning Shift End") to settings.sabahEndTime
            SettingsTimeTarget.AKSAM_START -> trEn(isEn, "Akşam Vardiyası Başlangıcı", "Evening Shift Start") to settings.aksamStartTime
            SettingsTimeTarget.AKSAM_END -> trEn(isEn, "Akşam Vardiyası Bitişi", "Evening Shift End") to settings.aksamEndTime
            SettingsTimeTarget.GECE8_START -> trEn(isEn, "Gece (8s) Vardiyası Başlangıcı", "Night (8h) Shift Start") to settings.gece8StartTime
            SettingsTimeTarget.GECE8_END -> trEn(isEn, "Gece (8s) Vardiyası Bitişi", "Night (8h) Shift End") to settings.gece8EndTime
            SettingsTimeTarget.DAILY_REMINDER_TIME -> trEn(isEn, "Günlük Hatırlatma Saati", "Daily Reminder Time") to settings.shiftReminderTime
        }
        ShiftTimePickerSheet(
            title = title,
            initialTime = init,
            allow24Hour = target != SettingsTimeTarget.DAILY_REMINDER_TIME,
            onDismiss = { activeTimeTarget = null },
            onConfirm = { picked ->
                if (target == SettingsTimeTarget.DAILY_REMINDER_TIME) {
                    viewModel.updateReminderTime(picked)
                } else {
                    val updated = when (target) {
                        SettingsTimeTarget.GUNDUZ_START -> settings.copy(gunduzStartTime = picked)
                        SettingsTimeTarget.GUNDUZ_END -> settings.copy(gunduzEndTime = picked)
                        SettingsTimeTarget.GECE_START -> settings.copy(geceStartTime = picked)
                        SettingsTimeTarget.GECE_END -> settings.copy(geceEndTime = picked)
                        SettingsTimeTarget.SABAH_START -> settings.copy(sabahStartTime = picked)
                        SettingsTimeTarget.SABAH_END -> settings.copy(sabahEndTime = picked)
                        SettingsTimeTarget.AKSAM_START -> settings.copy(aksamStartTime = picked)
                        SettingsTimeTarget.AKSAM_END -> settings.copy(aksamEndTime = picked)
                        SettingsTimeTarget.GECE8_START -> settings.copy(gece8StartTime = picked)
                        SettingsTimeTarget.GECE8_END -> settings.copy(gece8EndTime = picked)
                        SettingsTimeTarget.DAILY_REMINDER_TIME -> settings
                    }
                    viewModel.updateSettings(updated)
                }
            }
        )
    }

    if (showDatePicker) {
        TitaniumDatePickerDialog(
            title = trEn("Döngü Çapa Tarihi", "Cycle Anchor Date"),
            initialDate = settings.referenceStartDate,
            onDismiss = { showDatePicker = false },
            onConfirm = { newDate ->
                viewModel.updateSettings(settings.copy(referenceStartDate = newDate, isAnchorConfigured = true))
            }
        )
    }

    if (showCustomBuilder) {
        val isTemplatePreset = customBuilderPresetTarget == CyclePreset.CYCLE_DUPONT_TEMPLATE ||
            customBuilderPresetTarget == CyclePreset.CYCLE_FIVE_TEAM_8H
        val initSeq = if (isTemplatePreset && settings.cyclePreset != customBuilderPresetTarget) {
            emptyList()
        } else {
            settings.customSequence
        }
        CustomCycleBuilderSheet(
            initialSequence = initSeq,
            allowEmptyInitial = isTemplatePreset,
            titleText = if (isTemplatePreset) customBuilderPresetTarget.localizedName(isEn) else trEn("Özel Döngü Oluşturucu", "Custom Cycle Builder"),
            onDismiss = { showCustomBuilder = false },
            onSave = { seq ->
                viewModel.updateSettings(
                    settings.copy(
                        cyclePreset = customBuilderPresetTarget,
                        customSequence = seq
                    )
                )
            }
        )
    }

    if (showWindowPickerDialog) {
        AlertDialog(
            onDismissRequest = { showWindowPickerDialog = false },
            containerColor = dyn.surfaceCard,
            title = { Text(trEn("Ön Hazırlık Uyarı Süresi", "Pre-Shift Alert Lead Time"), color = dyn.textPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(30, 60, 90, 120, 180).forEach { mins ->
                        val isSel = settings.preShiftAlertMinutes == mins
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) (if (dyn.isDark) Color(0xFF082F49) else dyn.primaryNeon.copy(alpha = 0.14f)) else dyn.surfaceElevated)
                                .border(1.dp, if (isSel) dyn.accentCyan else dyn.borderSubtle, RoundedCornerShape(10.dp))
                                .clickable {
                                    viewModel.updateSettings(settings.copy(preShiftAlertMinutes = mins))
                                    showWindowPickerDialog = false
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(trEn("Vardiyadan $mins dk önce", "$mins min before shift"), color = if (isSel) dyn.accentCyan else dyn.textPrimary, fontWeight = FontWeight.Bold)
                            if (isSel) Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = dyn.accentCyan, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showWindowPickerDialog = false }) { Text(trEn("Kapat", "Close"), color = dyn.textSecondary) }
            }
        )
    }

    if (showBackupRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showBackupRestoreDialog = false },
            containerColor = dyn.surfaceCard,
            title = { Text(trEn("JSON Yedekten Geri Yükle", "Restore from JSON Backup"), color = dyn.textPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(trEn("PROSHIFT JSON yedek metnini yapıştırın:", "Paste your PROSHIFT JSON backup text:"), fontSize = 12.sp, color = dyn.textSecondary)
                    OutlinedTextField(
                        value = restoreJsonInput,
                        onValueChange = { restoreJsonInput = it },
                        minLines = 5,
                        maxLines = 8,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = dyn.primaryNeon,
                            unfocusedBorderColor = dyn.borderMetal,
                            focusedTextColor = dyn.textPrimary,
                            unfocusedTextColor = dyn.textPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ok = viewModel.repository.importFullBackupJson(restoreJsonInput)
                        Toast.makeText(
                            context,
                            if (ok) trEn(isEn, "Yedek başarıyla geri yüklendi", "Backup restored successfully")
                            else trEn(isEn, "Geçersiz JSON yedek formatı", "Invalid JSON backup format"),
                            Toast.LENGTH_SHORT
                        ).show()
                        showBackupRestoreDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = dyn.primaryNeon)
                ) {
                    Text(trEn("Geri Yükle", "Restore"), color = dyn.textOnPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackupRestoreDialog = false }) { Text(trEn("Vazgeç", "Cancel"), color = dyn.textSecondary) }
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = dyn.surfaceCard,
            title = { Text(trEn("Döngüyü Sıfırla veya Yeniden Başlat?", "Reset or Restart Cycle?"), color = dyn.textPrimary, fontWeight = FontWeight.Bold) },
            text = { Text(trEn("Tüm vardiya çapa tarihleri, özel gün kayıtları ve ayarlar varsayılan değerlere dönecektir.", "All shift anchor dates, custom day entries, and settings will reset to defaults."), color = dyn.textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAllData()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = dyn.primaryNeon)
                ) {
                    Text(trEn("Evet, Sıfırla", "Yes, Reset"), color = dyn.textOnPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text(trEn("Vazgeç", "Cancel"), color = dyn.textSecondary) }
            }
        )
    }
}

@Composable
private fun SettingsTimeTile(
    label: String,
    time: String,
    accent: Color,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val dyn = DynamicTheme.colors
    Row(
        modifier = modifier
            .titaniumBrushedSurface(RoundedCornerShape(10.dp), accent.copy(alpha = 0.45f), elevated = true)
            .clickable { onClick() }
            .testTag(testTag)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(label, fontSize = 9.sp, color = dyn.textMuted)
            Text(time, fontSize = 15.sp, fontWeight = FontWeight.Black, fontFamily = ProshiftMonoFont, color = accent)
        }
        Icon(Icons.Filled.Schedule, contentDescription = label, tint = accent, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun StepPillBox(
    stepName: String,
    title: String,
    duration: String,
    icon: String,
    accentColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    val dyn = DynamicTheme.colors
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(stepName, fontSize = 8.sp, fontFamily = ProshiftMonoFont, fontWeight = FontWeight.Bold, color = dyn.textMuted)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = icon, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = dyn.textPrimary, maxLines = 1)
            Text(duration, fontSize = 11.sp, fontFamily = ProshiftMonoFont, fontWeight = FontWeight.Black, color = accentColor)
        }
    }
}

@Composable
private fun TemplateGridTile(
    badgeText: String,
    isActive: Boolean,
    title: String,
    description: String,
    footerText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dyn = DynamicTheme.colors
    val activeChipBg = if (dyn.isDark) Color(0xFF082F49) else dyn.primaryNeon.copy(alpha = 0.14f)
    Surface(
        modifier = modifier
            .height(106.dp)
            .titaniumBrushedSurface(
                shape = RoundedCornerShape(16.dp),
                borderColor = if (isActive) dyn.primaryNeon else dyn.borderSubtle,
                elevated = isActive
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isActive) activeChipBg else dyn.surfaceElevated,
                    border = if (isActive) BorderStroke(1.dp, dyn.primaryNeon) else null
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) dyn.primaryNeon else dyn.textSecondary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Box(modifier = Modifier.size(8.dp).background(if (isActive) dyn.primaryNeon else dyn.textMuted, CircleShape))
            }

            Column {
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.Black, color = dyn.textPrimary, maxLines = 1)
                Spacer(modifier = Modifier.height(2.dp))
                Text(description, fontSize = 10.sp, color = dyn.textSecondary, lineHeight = 12.sp, maxLines = 2)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(footerText, fontSize = 10.sp, fontFamily = ProshiftMonoFont, color = dyn.textSecondary)
                Icon(
                    imageVector = if (isActive) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = if (isActive) trEn("Seçili", "Selected") else trEn("Seç", "Select"),
                    tint = if (isActive) dyn.primaryNeon else dyn.textMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
