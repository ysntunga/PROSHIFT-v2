package com.example

import android.graphics.Color as AndroidColor
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.AppThemeMode
import com.example.ui.components.BreakOvertimeDialog
import com.example.ui.components.DayInspectorDialog
import com.example.ui.components.FloatingDockBottomNav
import com.example.ui.components.ShiftNoteDialog
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalAppLocale
import com.example.ui.i18n.LocalIsEnglish
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.TimesheetScreen
import com.example.ui.theme.DynamicTheme
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.ShiftViewModel
import java.time.LocalDate
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splashScreen.setOnExitAnimationListener { splashScreenView ->
                splashScreenView.remove()
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        window.decorView.post {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                runCatching { setTranslucent(false) }
            }
        }
        setContent {
            val viewModel: ShiftViewModel = viewModel()
            val userSettings by viewModel.userSettings.collectAsState()
            val hasExplicitThemeOverride by viewModel.hasExplicitThemeOverride.collectAsState()
            val systemInDarkTheme = isSystemInDarkTheme()
            val isDarkTheme = if (hasExplicitThemeOverride) {
                AppThemeMode.fromCode(userSettings.themeMode) == AppThemeMode.DARK
            } else {
                systemInDarkTheme
            }
            val view = LocalView.current
            SideEffect {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !isDarkTheme
                insetsController.isAppearanceLightNavigationBars = !isDarkTheme
            }
            MyApplicationTheme(
                department = userSettings.department,
                professionGroupId = userSettings.professionGroup,
                dynamicThemeEnabled = userSettings.isOnboardingCompleted,
                isDarkTheme = isDarkTheme
            ) {
                ProshiftApp(
                    viewModel = viewModel,
                    showSplashOnLaunch = true
                )
            }
        }
    }
}

@Composable
fun ProshiftApp(
    viewModel: ShiftViewModel = viewModel(),
    showSplashOnLaunch: Boolean = false
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val inspectedDay by viewModel.inspectedDay.collectAsState()
    val showBreakDialog by viewModel.showBreakOvertimeDialog.collectAsState()
    val showNoteDialog by viewModel.showShiftNoteDialog.collectAsState()
    val showOnboarding by viewModel.showOnboardingWizard.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()
    val configuration = LocalConfiguration.current
    val systemLocale = remember(configuration) {
        if (!configuration.locales.isEmpty) {
            configuration.locales[0]
        } else {
            Locale.getDefault()
        }
    }
    val isEnglish = remember(userSettings.appLanguage, systemLocale) {
        AppLanguage.resolveIsEnglish(userSettings.appLanguage, systemLocale)
    }
    val appLocale = remember(isEnglish) {
        if (isEnglish) AppLanguage.EN_LOCALE else AppLanguage.TR_LOCALE
    }

    val showPdfPreview by viewModel.showPdfPreviewDialog.collectAsState()
    var showSplash by rememberSaveable(showSplashOnLaunch) { mutableStateOf(showSplashOnLaunch) }

    CompositionLocalProvider(
        LocalIsEnglish provides isEnglish,
        LocalAppLocale provides appLocale
    ) {
        Crossfade(
            targetState = showSplash,
            animationSpec = tween(durationMillis = 350),
            label = "splash_to_main_crossfade"
        ) { isShowingSplash ->
            if (isShowingSplash) {
                SplashScreen(
                    onSplashFinished = { showSplash = false }
                )
            } else {
                // Handle back button via safe navigation history stack
                BackHandler(enabled = currentScreen != AppScreen.HOME && currentScreen != AppScreen.WELCOME && currentScreen != AppScreen.ONBOARDING) {
                    viewModel.navigateBack(fallback = AppScreen.HOME)
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DynamicTheme.colors.background)
                ) {
            // Screen Content
            when (currentScreen) {
                AppScreen.ONBOARDING -> com.example.ui.screens.OnboardingScreen(viewModel = viewModel)
                AppScreen.WELCOME -> com.example.ui.screens.WelcomeScreen(viewModel = viewModel)
                AppScreen.WIZARD -> com.example.ui.screens.WizardScreen(viewModel = viewModel)
                AppScreen.HOME -> HomeScreen(viewModel = viewModel)
                AppScreen.CALENDAR -> CalendarScreen(viewModel = viewModel)
                AppScreen.TIMESHEET -> TimesheetScreen(viewModel = viewModel)
                AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                AppScreen.PROFILE -> com.example.ui.screens.ProfileScreen(viewModel = viewModel)
                AppScreen.MATCHING_QR -> com.example.ui.screens.QrMatchingScreen(viewModel = viewModel)
                AppScreen.ADD_OVERTIME -> com.example.ui.screens.AddOvertimeScreen(viewModel = viewModel)
                AppScreen.QR_SCANNER -> com.example.ui.screens.QrScannerScreen(viewModel = viewModel)
            }

            // Floating Dock Bottom Navigation (Shown on main navigation tabs)
            if (currentScreen != AppScreen.ONBOARDING && currentScreen != AppScreen.WELCOME && currentScreen != AppScreen.WIZARD && currentScreen != AppScreen.ADD_OVERTIME && currentScreen != AppScreen.QR_SCANNER) {
                FloatingDockBottomNav(
                    currentScreen = currentScreen,
                    onNavigate = { screen -> viewModel.navigateTo(screen) },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            // PDF Preview Sheet
            if (showPdfPreview) {
                com.example.ui.components.PdfPreviewSheet(
                    viewModel = viewModel,
                    onDismiss = { viewModel.closePdfPreviewDialog() }
                )
            }

            // Day Inspector Dialog
            inspectedDay?.let { day ->
                DayInspectorDialog(
                    day = day,
                    settings = userSettings,
                    onDismiss = { viewModel.dismissInspector() },
                    onSave = { type, overtime, note, startTime, endTime ->
                        viewModel.updateDayShift(day.date, type, overtime, note, startTime, endTime)
                    },
                    onReset = {
                        viewModel.resetDayOverride(day.date)
                    }
                )
            }

            // Break / Overtime Dialog
            if (showBreakDialog) {
                BreakOvertimeDialog(
                    onDismiss = { viewModel.closeBreakOvertimeDialog() },
                    onSaveOvertime = { extraHours ->
                        val today = LocalDate.now()
                        val existing = viewModel.repository.getShiftForDate(today)
                        val targetType = if (existing.type.isWork) existing.type else com.example.data.model.ShiftType.GUNDUZ
                        val defaultNote = if (isEnglish) "Overtime added (+${extraHours.toInt()}h)" else "Ek mesai eklendi (+${extraHours.toInt()}s)"
                        val noteText = if (existing.note.isNotBlank()) existing.note else defaultNote
                        viewModel.updateDayShift(
                            date = today,
                            type = targetType,
                            overtimeHours = extraHours,
                            note = noteText,
                            startTime = existing.startTime.takeIf { it != "-" },
                            endTime = existing.endTime.takeIf { it != "-" }
                        )
                    }
                )
            }

            // Shift Note Dialog
            if (showNoteDialog) {
                val today = LocalDate.now()
                val existingToday = viewModel.repository.getShiftForDate(today)
                ShiftNoteDialog(
                    initialNote = existingToday.note,
                    onDismiss = { viewModel.closeShiftNoteDialog() },
                    onSave = { note ->
                        viewModel.updateDayShift(
                            date = today,
                            type = existingToday.type,
                            overtimeHours = existingToday.overtimeHours,
                            note = note,
                            startTime = existingToday.startTime.takeIf { it != "-" },
                            endTime = existingToday.endTime.takeIf { it != "-" }
                        )
                    }
                )
            }

                    // Onboarding Wizard Dialog
                    if (showOnboarding) {
                        com.example.ui.components.OnboardingWizardDialog(
                            currentSettings = userSettings,
                            onDismiss = { viewModel.closeOnboardingWizard() },
                            onFinish = { updated ->
                                viewModel.updateSettings(updated)
                            }
                        )
                    }
                }
            }
        }
    }
}
