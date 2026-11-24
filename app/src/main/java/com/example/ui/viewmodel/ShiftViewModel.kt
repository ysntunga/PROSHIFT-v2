package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.engine.ShiftEngine
import com.example.data.model.AppThemeMode
import com.example.data.model.ColleagueProfile
import com.example.data.model.CyclePreset
import com.example.data.model.DayOverride
import com.example.data.model.DutyTelemetry
import com.example.data.model.MatchType
import com.example.data.model.ProfessionGroup
import com.example.data.model.QrAutoSyncReceipt
import com.example.data.model.ShiftDay
import com.example.data.model.ShiftMatchDay
import com.example.data.model.ShiftType
import com.example.data.model.SwapCategory
import com.example.data.model.SwapRequestItem
import com.example.data.model.TimesheetStats
import com.example.data.model.UserProfileSummary
import com.example.data.model.UserSettings
import com.example.data.notification.NotificationDispatchResult
import com.example.data.notification.ShiftNotificationHelper
import com.example.data.notification.ShiftReminderScheduler
import com.example.data.repository.ShiftRepository
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.formatMonthTitleLocalized
import com.example.ui.i18n.localizedName
import com.example.ui.i18n.localizedTitle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class AppScreen {
    ONBOARDING,
    WELCOME,
    WIZARD,
    HOME,
    CALENDAR,
    TIMESHEET,
    SETTINGS,
    PROFILE,
    MATCHING_QR,
    ADD_OVERTIME,
    QR_SCANNER
}

class ShiftViewModel(application: Application) : AndroidViewModel(application) {

    val repository = ShiftRepository(application)

    private val _currentScreen = MutableStateFlow(
        if (repository.userSettings.value.isOnboardingCompleted) AppScreen.HOME else AppScreen.ONBOARDING
    )
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val navigationHistory = ArrayDeque<AppScreen>()
    private val topLevelScreens = setOf(
        AppScreen.HOME,
        AppScreen.CALENDAR,
        AppScreen.MATCHING_QR,
        AppScreen.TIMESHEET,
        AppScreen.SETTINGS
    )

    val userSettings: StateFlow<UserSettings> = repository.userSettings
    val hasExplicitThemeOverride: StateFlow<Boolean> = repository.hasExplicitThemeOverride
    val dayOverrides: StateFlow<Map<String, DayOverride>> = repository.dayOverrides

    private val initialToday = LocalDate.now()
    private val _selectedYear = MutableStateFlow(initialToday.year)
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _selectedMonth = MutableStateFlow(initialToday.monthValue)
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    private val _currentDate = MutableStateFlow(initialToday)
    val currentDate: StateFlow<LocalDate> = _currentDate.asStateFlow()

    private val _dutyTelemetry = MutableStateFlow(repository.calculateDutyTelemetry())
    val dutyTelemetry: StateFlow<DutyTelemetry> = _dutyTelemetry.asStateFlow()

    private val _inspectedDay = MutableStateFlow<ShiftDay?>(null)
    val inspectedDay: StateFlow<ShiftDay?> = _inspectedDay.asStateFlow()

    private val _showBreakOvertimeDialog = MutableStateFlow(false)
    val showBreakOvertimeDialog: StateFlow<Boolean> = _showBreakOvertimeDialog.asStateFlow()

    private val _showShiftNoteDialog = MutableStateFlow(false)
    val showShiftNoteDialog: StateFlow<Boolean> = _showShiftNoteDialog.asStateFlow()

    private val _showOnboardingWizard = MutableStateFlow(false)
    val showOnboardingWizard: StateFlow<Boolean> = _showOnboardingWizard.asStateFlow()

    private val _showPdfPreviewDialog = MutableStateFlow(false)
    val showPdfPreviewDialog: StateFlow<Boolean> = _showPdfPreviewDialog.asStateFlow()

    // Multi-profile state
    val profiles: StateFlow<List<UserProfileSummary>> = repository.profiles
    val activeProfileId: StateFlow<String> = repository.activeProfileId

    // Colleague Matcher State
    val colleagues: StateFlow<List<ColleagueProfile>> = repository.colleagues

    private val _selectedColleague = MutableStateFlow<ColleagueProfile?>(
        repository.colleagues.value.firstOrNull()
    )
    val selectedColleague: StateFlow<ColleagueProfile?> = _selectedColleague.asStateFlow()

    private val _lastQrSyncReceipt = MutableStateFlow<QrAutoSyncReceipt?>(null)
    val lastQrSyncReceipt: StateFlow<QrAutoSyncReceipt?> = _lastQrSyncReceipt.asStateFlow()

    // Swap Pool State (empty by default; no demo contacts)
    private val _swapRequests = MutableStateFlow<List<SwapRequestItem>>(emptyList())
    val swapRequests: StateFlow<List<SwapRequestItem>> = _swapRequests.asStateFlow()

    val monthShiftDays: StateFlow<List<ShiftDay>> = combine(
        _selectedYear,
        _selectedMonth,
        repository.userSettings,
        repository.dayOverrides
    ) { year, month, settings, _ ->
        repository.getMonthDays(year, month, settings)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val weeklyStripDays: StateFlow<List<ShiftDay>> = combine(
        _currentDate,
        repository.userSettings,
        repository.dayOverrides
    ) { today, settings, _ ->
        repository.getRollingWeekFlowDays(
            centerDate = today,
            daysBefore = 7,
            daysAfter = 7,
            settings = settings
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val timesheetStats: StateFlow<TimesheetStats> = combine(
        _selectedYear,
        _selectedMonth,
        repository.userSettings,
        repository.dayOverrides
    ) { year, month, settings, _ ->
        repository.calculateTimesheetStats(year, month, settings)
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        repository.calculateTimesheetStats(initialToday.year, initialToday.monthValue)
    )

    val matchResults: StateFlow<List<ShiftMatchDay>> = combine(
        _selectedColleague,
        _selectedYear,
        _selectedMonth,
        repository.userSettings,
        repository.dayOverrides
    ) { colleague, year, month, settings, _ ->
        if (colleague == null) {
            emptyList()
        } else {
            runCatching {
                repository.calculateShiftMatching(colleague, year, month, settings)
            }.getOrDefault(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        repository.consumeMigrationNotice()?.let { notice ->
            val localizedNotice = if (isEnglish()) {
                "Your shift data was safely migrated to the new version"
            } else {
                notice
            }
            Toast.makeText(application, localizedNotice, Toast.LENGTH_LONG).show()
        }
        ShiftNotificationHelper.ensureChannels(application, isEnglish())
        ShiftReminderScheduler.syncReminders(application, repository)
        viewModelScope.launch {
            while (isActive) {
                val now = LocalDateTime.now()
                val nowDate = now.toLocalDate()
                if (_currentDate.value != nowDate) {
                    _currentDate.value = nowDate
                }
                _dutyTelemetry.value = repository.calculateDutyTelemetry(now)
                delay(1000)
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        val current = _currentScreen.value
        if (current == screen) return

        when {
            screen in topLevelScreens -> {
                navigationHistory.clear()
            }
            screen == AppScreen.ONBOARDING || screen == AppScreen.WELCOME -> {
                navigationHistory.clear()
            }
            else -> {
                if (navigationHistory.lastOrNull() != current) {
                    navigationHistory.addLast(current)
                    while (navigationHistory.size > 20) {
                        navigationHistory.removeFirst()
                    }
                }
            }
        }
        _currentScreen.value = screen
    }

    fun navigateBack(fallback: AppScreen = AppScreen.HOME) {
        val current = _currentScreen.value
        val previous = if (navigationHistory.isNotEmpty()) {
            navigationHistory.removeLast()
        } else if (current != fallback) {
            fallback
        } else {
            AppScreen.HOME
        }
        _currentScreen.value = previous
    }

    fun completeOnboarding(targetScreen: AppScreen = AppScreen.HOME) {
        val current = userSettings.value
        if (!current.isOnboardingCompleted) {
            repository.updateSettings(current.copy(isOnboardingCompleted = true))
        }
        navigationHistory.clear()
        _currentScreen.value = targetScreen
    }

    fun completeOnboardingSetup(
        employeeName: String,
        department: String,
        cyclePreset: CyclePreset,
        customSequence: List<ShiftType>? = null,
        professionGroup: ProfessionGroup? = null,
        title: String? = null,
        referenceStartDate: LocalDate = LocalDate.now(),
        initialCycleOffset: Int = 0
    ) {
        val current = userSettings.value
        val resolvedName = employeeName.trim().ifBlank { current.employeeName }
        val resolvedDepartment = department.trim().ifBlank { current.department }
        val resolvedGroup = professionGroup ?: ProfessionGroup.fromId(current.professionGroup)
        val resolvedTitle = title?.trim()?.takeIf { it.isNotEmpty() } ?: resolvedGroup.titles.first()
        val resolvedSeq = when {
            !customSequence.isNullOrEmpty() -> customSequence
            cyclePreset.hasDefaultPattern && cyclePreset.sequence.isNotEmpty() -> cyclePreset.sequence
            else -> current.customSequence.ifEmpty {
                listOf(ShiftType.GUNDUZ, ShiftType.GECE, ShiftType.IZIN)
            }
        }
        val safeOffset = ShiftEngine.safeModulo(initialCycleOffset.toLong(), resolvedSeq.size.coerceAtLeast(1))

        val updated = current.copy(
            employeeName = resolvedName,
            department = resolvedDepartment,
            professionGroup = resolvedGroup.id,
            title = resolvedTitle,
            cyclePreset = cyclePreset,
            customSequence = resolvedSeq,
            referenceStartDate = referenceStartDate,
            initialCycleOffset = safeOffset,
            isAnchorConfigured = true,
            isOnboardingCompleted = true
        )
        repository.updateSettings(updated)
        ShiftReminderScheduler.syncReminders(getApplication(), repository)
        _dutyTelemetry.value = repository.calculateDutyTelemetry()
        _selectedYear.value = referenceStartDate.year
        _selectedMonth.value = referenceStartDate.monthValue
        _currentScreen.value = AppScreen.HOME
    }

    fun selectMonth(year: Int, month: Int) {
        _selectedYear.value = year
        _selectedMonth.value = month
    }

    fun nextMonth() {
        if (_selectedMonth.value == 12) {
            _selectedYear.value += 1
            _selectedMonth.value = 1
        } else {
            _selectedMonth.value += 1
        }
    }

    fun previousMonth() {
        if (_selectedMonth.value == 1) {
            _selectedYear.value -= 1
            _selectedMonth.value = 12
        } else {
            _selectedMonth.value -= 1
        }
    }

    fun goToToday() {
        val today = LocalDate.now()
        _selectedYear.value = today.year
        _selectedMonth.value = today.monthValue
    }

    fun inspectDay(day: ShiftDay) {
        _inspectedDay.value = day
    }

    fun dismissInspector() {
        _inspectedDay.value = null
    }

    fun openBreakOvertimeDialog() {
        _showBreakOvertimeDialog.value = true
    }

    fun closeBreakOvertimeDialog() {
        _showBreakOvertimeDialog.value = false
    }

    fun openShiftNoteDialog() {
        _showShiftNoteDialog.value = true
    }

    fun closeShiftNoteDialog() {
        _showShiftNoteDialog.value = false
    }

    private fun isEnglish(): Boolean {
        val cfg = getApplication<Application>().resources.configuration
        val sysLocale = if (!cfg.locales.isEmpty) {
            cfg.locales[0]
        } else {
            Locale.getDefault()
        }
        return AppLanguage.resolveIsEnglish(userSettings.value.appLanguage, sysLocale)
    }

    private fun appLocale(): Locale = if (isEnglish()) AppLanguage.EN_LOCALE else AppLanguage.TR_LOCALE

    private fun t(tr: String, en: String): String = if (isEnglish()) en else tr

    fun updateAppLanguage(language: AppLanguage) {
        val current = userSettings.value
        val updated = current.copy(appLanguage = language.code)
        repository.updateSettings(updated)
        _dutyTelemetry.value = repository.calculateDutyTelemetry()
        val msg = when (language) {
            AppLanguage.SYSTEM -> if (isEnglish()) "Language set to Automatic (System)" else "Dil Otomatik (Sistem) olarak ayarlandı"
            AppLanguage.TURKISH -> "Uygulama dili Türkçe olarak ayarlandı"
            AppLanguage.ENGLISH -> "App language set to English"
        }
        runCatching { Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show() }
    }

    fun updateThemeMode(mode: AppThemeMode) {
        val current = userSettings.value
        val wasExplicit = repository.hasExplicitThemeOverride.value
        repository.markThemeModeExplicitlySet()
        if (wasExplicit && AppThemeMode.fromCode(current.themeMode) == mode) return
        repository.updateSettings(current.copy(themeMode = mode.code))
        val msg = when (mode) {
            AppThemeMode.LIGHT -> t("Açık tema etkinleştirildi", "Light mode enabled")
            AppThemeMode.DARK -> t("Koyu tema etkinleştirildi", "Dark mode enabled")
        }
        runCatching { Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show() }
    }

    fun toggleThemeMode() {
        val next = if (AppThemeMode.fromCode(userSettings.value.themeMode) == AppThemeMode.DARK) {
            AppThemeMode.LIGHT
        } else {
            AppThemeMode.DARK
        }
        updateThemeMode(next)
    }

    fun toggleBreak() {
        val isRunning = repository.toggleBreak()
        _dutyTelemetry.value = repository.calculateDutyTelemetry()
        val msg = if (isRunning) t("Mola sayacı başlatıldı", "Break timer started") else t("Mola tamamlandı", "Break completed")
        runCatching { Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show() }
    }

    fun resetBreak() {
        repository.resetBreak()
        _dutyTelemetry.value = repository.calculateDutyTelemetry()
        runCatching { Toast.makeText(getApplication(), t("Mola sayacı sıfırlandı", "Break timer reset"), Toast.LENGTH_SHORT).show() }
    }

    fun openOnboardingWizard() {
        _showOnboardingWizard.value = true
    }

    fun closeOnboardingWizard() {
        _showOnboardingWizard.value = false
    }

    fun openPdfPreviewDialog() {
        _showPdfPreviewDialog.value = true
    }

    fun closePdfPreviewDialog() {
        _showPdfPreviewDialog.value = false
    }

    fun selectColleague(colleague: ColleagueProfile) {
        _selectedColleague.value = colleague
    }

    fun addAndSelectColleague(profile: ColleagueProfile) {
        repository.addOrUpdateColleague(profile)
        _selectedColleague.value = profile
    }

    fun removeColleague(profile: ColleagueProfile) {
        repository.removeColleague(profile)
        val remaining = repository.colleagues.value
        val current = _selectedColleague.value
        if (current == null || (current.qrCodeId == profile.qrCodeId && current.name == profile.name) ||
            remaining.none { it.qrCodeId == current.qrCodeId && it.name == current.name }
        ) {
            _selectedColleague.value = remaining.firstOrNull()
        }
        val receipt = _lastQrSyncReceipt.value
        if (receipt != null && receipt.colleague.qrCodeId == profile.qrCodeId && receipt.colleague.name == profile.name) {
            _lastQrSyncReceipt.value = null
        }
        runCatching {
            Toast.makeText(
                getApplication(),
                t("${profile.name} kişilerden kaldırıldı", "${profile.name} removed from contacts"),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun autoProcessScannedColleague(
        profile: ColleagueProfile,
        sourceLabel: String? = null,
        enableFriendLayer: Boolean = true,
        enableCommonLeaveAlert: Boolean = userSettings.value.commonLeaveAlertEnabled
    ): QrAutoSyncReceipt {
        val resolvedSource = sourceLabel ?: t("QR Tarayıcı", "QR Scanner")
        repository.addOrUpdateColleague(profile)
        _selectedColleague.value = profile

        val currentSettings = repository.userSettings.value
        val updatedSettings = if (
            currentSettings.showFriendOnCalendar != enableFriendLayer ||
            currentSettings.commonLeaveAlertEnabled != enableCommonLeaveAlert ||
            !currentSettings.calendarSyncEnabled
        ) {
            currentSettings.copy(
                showFriendOnCalendar = enableFriendLayer,
                commonLeaveAlertEnabled = enableCommonLeaveAlert,
                calendarSyncEnabled = true
            ).also { repository.updateSettings(it) }
        } else {
            currentSettings
        }

        val matches = repository.calculateShiftMatching(
            colleague = profile,
            year = _selectedYear.value,
            month = _selectedMonth.value,
            userSettings = updatedSettings
        )
        val commonOffCount = matches.count { it.matchType == MatchType.COMMON_OFF }
        val receipt = QrAutoSyncReceipt(
            colleague = profile,
            sourceLabel = resolvedSource,
            timestampMs = System.currentTimeMillis(),
            commonOffCount = commonOffCount,
            appliedAsOwnShift = false
        )
        _lastQrSyncReceipt.value = receipt
        _dutyTelemetry.value = repository.calculateDutyTelemetry()
        return receipt
    }

    fun applyColleagueAsOwnShift(profile: ColleagueProfile): QrAutoSyncReceipt {
        val currentSettings = repository.userSettings.value
        val seq = profile.customSequence?.takeIf { it.isNotEmpty() } ?: profile.cyclePreset.sequence
        val safeSeq = if (seq.isNotEmpty()) seq else currentSettings.customSequence
        val updated = currentSettings.copy(
            cyclePreset = if (profile.customSequence != null && profile.cyclePreset.hasDefaultPattern) {
                CyclePreset.CUSTOM
            } else {
                profile.cyclePreset
            },
            customSequence = safeSeq,
            referenceStartDate = profile.referenceStartDate,
            initialCycleOffset = ShiftEngine.safeModulo(profile.initialCycleOffset.toLong(), safeSeq.size.coerceAtLeast(1)),
            isAnchorConfigured = true,
            calendarSyncEnabled = true
        )
        repository.updateSettings(updated)
        ShiftReminderScheduler.syncReminders(getApplication(), repository)
        _dutyTelemetry.value = repository.calculateDutyTelemetry()
        val receipt = QrAutoSyncReceipt(
            colleague = profile,
            sourceLabel = t("Kendi Takvimine Uygulandı", "Applied to Your Calendar"),
            timestampMs = System.currentTimeMillis(),
            commonOffCount = 0,
            appliedAsOwnShift = true
        )
        _lastQrSyncReceipt.value = receipt
        runCatching {
            Toast.makeText(
                getApplication(),
                t("${profile.name} vardiya döngüsü mevcut takviminize uygulandı", "${profile.name}'s shift cycle applied to your calendar"),
                Toast.LENGTH_LONG
            ).show()
        }
        return receipt
    }

    fun clearQrSyncReceipt() {
        _lastQrSyncReceipt.value = null
    }

    fun switchProfile(profileId: String) {
        if (repository.switchProfile(profileId)) {
            _dutyTelemetry.value = repository.calculateDutyTelemetry()
            val name = repository.userSettings.value.employeeName
            runCatching { Toast.makeText(getApplication(), t("$name profiline geçildi", "Switched to $name"), Toast.LENGTH_SHORT).show() }
        }
    }

    fun createProfile(name: String, department: String, title: String, cyclePreset: CyclePreset) {
        val summary = repository.createProfile(name, department, title, cyclePreset)
        _dutyTelemetry.value = repository.calculateDutyTelemetry()
        runCatching { Toast.makeText(getApplication(), t("${summary.name} profili oluşturuldu", "${summary.name} profile created"), Toast.LENGTH_SHORT).show() }
    }

    fun deleteProfile(profileId: String) {
        if (repository.deleteProfile(profileId)) {
            _dutyTelemetry.value = repository.calculateDutyTelemetry()
            runCatching { Toast.makeText(getApplication(), t("Profil silindi", "Profile deleted"), Toast.LENGTH_SHORT).show() }
        } else {
            runCatching { Toast.makeText(getApplication(), t("En az bir profil kalmalıdır", "At least one profile must remain"), Toast.LENGTH_SHORT).show() }
        }
    }

    fun updateReminderTime(newTime: String) {
        val updated = userSettings.value.copy(
            shiftReminderTime = newTime,
            shiftReminderEnabled = true
        )
        repository.updateSettings(updated)
        ShiftReminderScheduler.syncReminders(getApplication(), repository)
        runCatching { Toast.makeText(getApplication(), t("Bildirim saati $newTime olarak kaydedildi", "Reminder time saved as $newTime"), Toast.LENGTH_SHORT).show() }
    }

    fun acceptSwapRequest(swapId: String) {
        val currentList = _swapRequests.value
        val target = currentList.find { it.id == swapId } ?: return
        val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ROOT)

        if (target.isTransferOnly) {
            repository.saveDayOverride(
                DayOverride(
                    dateIso = target.receiveDate.format(fmt),
                    shiftType = target.receiveShiftType,
                    overtimeHours = target.receiveShiftType.defaultDuration,
                    note = t("Nöbet Devri: ${target.colleagueName}", "Shift Handover: ${target.colleagueName}")
                )
            )
        } else {
            repository.saveDayOverride(
                DayOverride(
                    dateIso = target.giveDate.format(fmt),
                    shiftType = ShiftType.IZIN,
                    overtimeHours = 0.0,
                    note = t("Takas verildi (${target.colleagueName})", "Shift given (${target.colleagueName})")
                )
            )
            repository.saveDayOverride(
                DayOverride(
                    dateIso = target.receiveDate.format(fmt),
                    shiftType = target.receiveShiftType,
                    overtimeHours = 0.0,
                    note = t("Takas alındı (${target.colleagueName})", "Shift taken (${target.colleagueName})")
                )
            )
        }

        _swapRequests.value = currentList.map {
            if (it.id == swapId) {
                it.copy(category = SwapCategory.HISTORY, statusText = t("Onaylandı & Takvime İşlendi", "Approved & Added to Calendar"))
            } else it
        }
        _dutyTelemetry.value = repository.calculateDutyTelemetry()
        runCatching { Toast.makeText(getApplication(), t("${target.colleagueName} takası takvime işlendi", "Swap with ${target.colleagueName} applied"), Toast.LENGTH_SHORT).show() }
    }

    fun proposeCounterSwap(swapId: String, altDate: LocalDate, note: String) {
        val fmt = DateTimeFormatter.ofPattern("d MMM", appLocale())
        _swapRequests.value = _swapRequests.value.map {
            if (it.id == swapId) {
                it.copy(
                    giveDate = altDate,
                    message = if (note.isNotBlank()) note else t("${altDate.format(fmt)} için karşı takas önerildi.", "Counter-swap proposed for ${altDate.format(fmt)}."),
                    statusText = t("Karşı Teklif Gönderildi", "Counter Offer Sent"),
                    timeAgo = t("Az önce", "Just now")
                )
            } else it
        }
        runCatching { Toast.makeText(getApplication(), t("Karşı takas önerisi gönderildi (${altDate.format(fmt)})", "Counter-swap sent (${altDate.format(fmt)})"), Toast.LENGTH_SHORT).show() }
    }

    fun createNewSwapListing(date: LocalDate, shiftType: ShiftType, note: String) {
        val item = SwapRequestItem(
            id = "swap_${System.currentTimeMillis()}",
            colleagueName = userSettings.value.employeeName,
            colleagueRole = userSettings.value.department,
            category = SwapCategory.OPEN_POOL,
            matchScorePercent = 100,
            giveDate = date,
            giveShiftType = shiftType,
            receiveDate = date.plusDays(2),
            receiveShiftType = ShiftType.GUNDUZ,
            message = note.ifBlank { t("Takasa açık vardiya ilanı oluşturuldu.", "Open shift swap listing created.") },
            badgeText = t("Benim İlanım", "My Listing"),
            timeAgo = t("Şimdi yayınlandı", "Posted now"),
            statusText = t("Açık Havuzda", "In Open Pool")
        )
        _swapRequests.value = listOf(item) + _swapRequests.value
        runCatching { Toast.makeText(getApplication(), t("Takas ilanı açık havuza eklendi", "Swap listing added to pool"), Toast.LENGTH_SHORT).show() }
    }

    fun updateDayShift(
        date: LocalDate,
        type: ShiftType,
        overtimeHours: Double,
        note: String,
        startTime: String? = null,
        endTime: String? = null
    ) {
        val dateIso = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ROOT))
        repository.saveDayOverride(
            DayOverride(
                dateIso = dateIso,
                shiftType = type,
                overtimeHours = overtimeHours,
                note = note,
                startTime = startTime,
                endTime = endTime
            )
        )
        _inspectedDay.value = repository.getShiftForDate(date)
        _dutyTelemetry.value = repository.calculateDutyTelemetry()
        ShiftReminderScheduler.syncReminders(getApplication(), repository)
        runCatching { Toast.makeText(getApplication(), t("Vardiya güncellendi", "Shift updated"), Toast.LENGTH_SHORT).show() }
    }

    fun resetDayOverride(date: LocalDate) {
        val dateIso = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ROOT))
        repository.removeDayOverride(dateIso)
        _inspectedDay.value = repository.getShiftForDate(date)
        _dutyTelemetry.value = repository.calculateDutyTelemetry()
        ShiftReminderScheduler.syncReminders(getApplication(), repository)
        runCatching { Toast.makeText(getApplication(), t("Orijinal döngüye sıfırlandı", "Reset to original cycle"), Toast.LENGTH_SHORT).show() }
    }

    fun updateCyclePreset(preset: CyclePreset) {
        val current = userSettings.value
        val updated = current.copy(cyclePreset = preset)
        repository.updateSettings(updated)
        _dutyTelemetry.value = repository.calculateDutyTelemetry()
        ShiftReminderScheduler.syncReminders(getApplication(), repository)
        val presetName = preset.localizedName(isEnglish())
        runCatching { Toast.makeText(getApplication(), t("$presetName aktif edildi", "$presetName activated"), Toast.LENGTH_SHORT).show() }
    }

    fun updateSettings(newSettings: UserSettings) {
        repository.updateSettings(newSettings)
        _dutyTelemetry.value = repository.calculateDutyTelemetry()
        ShiftReminderScheduler.syncReminders(getApplication(), repository)
        runCatching { Toast.makeText(getApplication(), t("Ayarlar kaydedildi", "Settings saved"), Toast.LENGTH_SHORT).show() }
    }

    fun resetAllData() {
        repository.resetToDefaults()
        _selectedColleague.value = repository.colleagues.value.firstOrNull()
        _lastQrSyncReceipt.value = null
        navigationHistory.clear()
        _dutyTelemetry.value = repository.calculateDutyTelemetry()
        ShiftReminderScheduler.syncReminders(getApplication(), repository)
        runCatching {
            Toast.makeText(getApplication(), t("Tüm veriler sıfırlandı", "All data reset"), Toast.LENGTH_SHORT).show()
        }
    }

    fun exportTimesheetText(shareCsv: Boolean = false, includeNotes: Boolean = true) {
        val stats = timesheetStats.value
        val days = monthShiftDays.value
        val settings = userSettings.value
        val en = isEnglish()
        val loc = appLocale()
        val monthTitleLoc = formatMonthTitleLocalized(_selectedYear.value, _selectedMonth.value, loc)

        val content = if (shareCsv) {
            val sb = StringBuilder()
            sb.append(
                if (en) "Date,Day,Shift,Start,End,Work Hours,Overtime,Total,Note\n"
                else "Tarih,Gun,Vardiya,Baslangic,Bitis,Calisma Saati,Ek Mesai,Toplam,Not\n"
            )
            val dayNameFmt = DateTimeFormatter.ofPattern("EEEE", loc)
            for (d in days) {
                val cleanNote = if (includeNotes) d.note else ""
                sb.append("${d.date},${d.date.format(dayNameFmt)},${d.type.localizedTitle(en)},${d.startTime},${d.endTime},${d.baseHours},${d.overtimeHours},${d.totalHours},\"$cleanNote\"\n")
            }
            sb.toString()
        } else {
            val sb = StringBuilder()
            sb.append(if (en) "PROSHIFT TIMESHEET & PAYROLL REPORT\n" else "PROSHIFT PUANTAJ VE BORDRO RAPORU\n")
            sb.append("====================================\n")
            sb.append(if (en) "Period: $monthTitleLoc\n" else "Dönem: $monthTitleLoc\n")
            sb.append(if (en) "Staff: ${settings.employeeName} (${settings.title})\n" else "Personel: ${settings.employeeName} (${settings.title})\n")
            sb.append(if (en) "Department: ${settings.department}\n" else "Birim: ${settings.department}\n")
            sb.append(if (en) "Shift Regime: ${settings.cyclePreset.localizedName(true)}\n" else "Vardiya Rejimi: ${settings.cyclePreset.displayName}\n")
            val activeSeq = ShiftEngine.resolvePattern(settings)
            val has8h = activeSeq.any { it == ShiftType.SABAH || it == ShiftType.AKSAM || it == ShiftType.GECE8 }
            if (has8h) {
                sb.append(
                    if (en) "Hours: Morning ${settings.sabahStartTime}-${settings.sabahEndTime} | Evening ${settings.aksamStartTime}-${settings.aksamEndTime} | Night ${settings.gece8StartTime}-${settings.gece8EndTime}\n"
                    else "Saatler: Sabah ${settings.sabahStartTime}-${settings.sabahEndTime} | Akşam ${settings.aksamStartTime}-${settings.aksamEndTime} | Gece ${settings.gece8StartTime}-${settings.gece8EndTime}\n"
                )
            } else {
                sb.append(
                    if (en) "Hours: Day ${settings.gunduzStartTime}-${settings.gunduzEndTime} | Night ${settings.geceStartTime}-${settings.geceEndTime}\n"
                    else "Saatler: Gündüz ${settings.gunduzStartTime}-${settings.gunduzEndTime} | Gece ${settings.geceStartTime}-${settings.geceEndTime}\n"
                )
            }
            sb.append("------------------------------------\n")
            sb.append(if (en) "• Total Work: ${stats.totalWorkHours.toInt()} Hours\n" else "• Toplam Çalışma: ${stats.totalWorkHours.toInt()} Saat\n")
            sb.append(if (en) "• Monthly Target: ${stats.targetWorkHours.toInt()} Hours\n" else "• Standart Hedef: ${stats.targetWorkHours.toInt()} Saat\n")
            sb.append(if (en) "• Night Hours: ${stats.nightHours.toInt()} Hours\n" else "• Gece Mesaisi: ${stats.nightHours.toInt()} Saat\n")
            sb.append(if (en) "• Overtime (+): ${stats.overtimeHours.toInt()} Hours\n" else "• Fazla Mesai (+): ${stats.overtimeHours.toInt()} Saat\n")
            sb.append(if (en) "• Working Days: ${stats.workDaysCount} Days\n" else "• Çalışılan Gün: ${stats.workDaysCount} Gün\n")
            sb.append(if (en) "• Off Days: ${stats.offDaysCount} Days\n" else "• İzinli Gün: ${stats.offDaysCount} Gün\n")
            if (stats.holidayWorkDaysCount > 0) {
                sb.append(if (en) "• Holiday Duty Days: ${stats.holidayWorkDaysCount} Days\n" else "• Resmi/Dini Tatil Mesaisi: ${stats.holidayWorkDaysCount} Gün\n")
            }
            if (includeNotes) {
                val notes = days.filter { it.note.isNotBlank() }
                if (notes.isNotEmpty()) {
                    sb.append("------------------------------------\n")
                    sb.append(if (en) "SHIFT NOTES & OVERTIME:\n" else "NÖBET NOTLARI & EK MESAİLER:\n")
                    val hUnit = if (en) "h" else "s"
                    notes.forEach { d ->
                        sb.append("• ${d.date}: ${d.note} (+${d.overtimeHours.toInt()}$hUnit)\n")
                    }
                }
            }
            sb.append("------------------------------------\n")
            sb.append(if (en) "ANNUAL LEAVE SUMMARY:\n" else "4857 SK MD. 53 YILLIK İZİN DURUMU:\n")
            sb.append(if (en) "• Seniority: ${stats.seniorityYears} Years\n" else "• Kıdem Yılı: ${stats.seniorityYears} Yıl\n")
            sb.append(if (en) "• Annual Entitlement: ${stats.statutoryLeaveRights} Days\n" else "• Yıllık Kanuni Hak: ${stats.statutoryLeaveRights} Gün\n")
            sb.append(if (en) "• Used Leave: ${stats.usedLeaveDays} Days\n" else "• Kullanılan İzin: ${stats.usedLeaveDays} Gün\n")
            sb.append(if (en) "• Remaining Leave: ${stats.remainingLeaveDays} Days\n" else "• Kalan İzin: ${stats.remainingLeaveDays} Gün\n")
            sb.append("====================================\n")
            sb.toString()
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, content)
            type = if (shareCsv) "text/csv" else "text/plain"
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooserTitle = if (shareCsv) t("CSV Puantajını Paylaş", "Share CSV Timesheet") else t("Puantaj Raporunu Paylaş", "Share Timesheet Report")
        val shareIntent = Intent.createChooser(sendIntent, chooserTitle)
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        getApplication<Application>().startActivity(shareIntent)
    }

    fun sharePlainText(title: String, text: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(sendIntent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        getApplication<Application>().startActivity(chooser)
    }

    fun shareViaWhatsApp() {
        val stats = timesheetStats.value
        val settings = userSettings.value
        val en = isEnglish()
        val monthTitleLoc = formatMonthTitleLocalized(_selectedYear.value, _selectedMonth.value, appLocale())
        val text = if (en) {
            "*PROSHIFT SHIFT SUMMARY*\n" +
                "*${settings.employeeName}* (${settings.department})\n" +
                "*Period:* $monthTitleLoc\n" +
                "*Total Work:* ${stats.totalWorkHours.toInt()} Hours\n" +
                "*Night Hours:* ${stats.nightHours.toInt()} Hours\n" +
                "*Overtime:* ${stats.overtimeHours.toInt()} Hours\n" +
                "*Remaining Leave:* ${stats.remainingLeaveDays} Days\n" +
                "_Generated with PROSHIFT._"
        } else {
            "*PROSHIFT VARDİYA ÖZETİ*\n" +
                "*${settings.employeeName}* (${settings.department})\n" +
                "*Dönem:* $monthTitleLoc\n" +
                "*Toplam Çalışma:* ${stats.totalWorkHours.toInt()} Saat\n" +
                "*Gece Mesaisi:* ${stats.nightHours.toInt()} Saat\n" +
                "*Fazla Mesai:* ${stats.overtimeHours.toInt()} Saat\n" +
                "*Kalan Yıllık İzin:* ${stats.remainingLeaveDays} Gün\n" +
                "_PROSHIFT ile hazırlandı._"
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
            setPackage("com.whatsapp")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            getApplication<Application>().startActivity(sendIntent)
        } catch (_: Exception) {
            sharePlainText(t("WhatsApp ile Paylaş", "Share via WhatsApp"), text)
        }
    }

    fun testReminderNotification(): NotificationDispatchResult {
        val en = isEnglish()
        val result = ShiftNotificationHelper.showTestNotification(
            context = getApplication(),
            settings = userSettings.value,
            telemetry = dutyTelemetry.value,
            isEnglish = en
        )
        val toastMessage = when (result) {
            is NotificationDispatchResult.SentAudible -> result.message
            is NotificationDispatchResult.SentDeviceMuted -> result.message
            is NotificationDispatchResult.PermissionRequired -> result.message
            is NotificationDispatchResult.SystemDisabled -> result.message
        }
        runCatching { Toast.makeText(getApplication(), toastMessage, Toast.LENGTH_LONG).show() }
        return result
    }

    fun openNotificationSoundSettings() {
        ShiftNotificationHelper.openChannelSoundSettings(getApplication(), isEnglish())
    }
}
