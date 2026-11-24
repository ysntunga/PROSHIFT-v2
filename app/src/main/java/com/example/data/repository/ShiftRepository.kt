package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.engine.ShiftAnchor
import com.example.data.engine.ShiftEngine
import com.example.data.model.ColleagueProfile
import com.example.data.model.CyclePreset
import com.example.data.model.DayOverride
import com.example.data.model.DutyTelemetry
import com.example.data.model.MatchType
import com.example.data.model.ProfessionGroup
import com.example.data.model.ShiftDay
import com.example.data.model.ShiftMatchDay
import com.example.data.model.ShiftType
import com.example.data.model.TimesheetStats
import com.example.data.model.UserProfileSummary
import com.example.data.model.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class ShiftRepository(context: Context) {

    companion object {
        const val PREFS_NAME = "proshift_prefs"
        const val PREFS_SCHEMA_VERSION = 2
        private const val DEFAULT_PROFILE_ID = "profile_main"
        private val LEGACY_DEMO_COLLEAGUE_IDS = setOf(
            "PRSH-KAAN-26",
            "PRSH-MERVE-26",
            "PRSH-AHMET-26"
        )
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ROOT)

    var lastMigrationNotice: String? = null
        private set

    init {
        migrateIfNeeded()
    }

    private val _activeProfileId = MutableStateFlow(loadActiveProfileId())
    val activeProfileId: StateFlow<String> = _activeProfileId.asStateFlow()

    private val _userSettings = MutableStateFlow(loadSettings())
    val userSettings: StateFlow<UserSettings> = _userSettings.asStateFlow()

    private val _dayOverrides = MutableStateFlow(loadOverrides())
    val dayOverrides: StateFlow<Map<String, DayOverride>> = _dayOverrides.asStateFlow()

    private val _profiles = MutableStateFlow(loadProfileSummaries())
    val profiles: StateFlow<List<UserProfileSummary>> = _profiles.asStateFlow()

    private val _colleagues = MutableStateFlow(loadColleagues())
    val colleagues: StateFlow<List<ColleagueProfile>> = _colleagues.asStateFlow()

    private val _hasExplicitThemeOverride = MutableStateFlow(prefs.getBoolean("theme_mode_explicitly_set", false))
    val hasExplicitThemeOverride: StateFlow<Boolean> = _hasExplicitThemeOverride.asStateFlow()

    fun markThemeModeExplicitlySet() {
        prefs.edit().putBoolean("theme_mode_explicitly_set", true).commit()
        _hasExplicitThemeOverride.value = true
    }

    // Break stopwatch state
    private var isBreakRunning = false
    private var breakStartTime: LocalDateTime? = null
    private var accumulatedBreakSeconds = 0L

    fun consumeMigrationNotice(): String? {
        val notice = lastMigrationNotice
        lastMigrationNotice = null
        return notice
    }

    private fun migrateIfNeeded() {
        val currentVersion = prefs.getInt("prefs_schema_version", 0)
        if (currentVersion >= PREFS_SCHEMA_VERSION) return

        val hasExistingUserData = prefs.contains("cycle_preset") ||
            prefs.contains("employee_name") ||
            prefs.contains("ref_start_date") ||
            prefs.contains("day_overrides")

        if (!hasExistingUserData) {
            // Fresh install: just stamp current schema version without wiping anything
            prefs.edit().putInt("prefs_schema_version", PREFS_SCHEMA_VERSION).commit()
            return
        }

        try {
            val legacySettings = readFlatSettingsFromPrefs()
            val legacyOverrides = readOverridesFromJsonString(prefs.getString("day_overrides", null))
            val activeId = prefs.getString("active_profile_id", DEFAULT_PROFILE_ID) ?: DEFAULT_PROFILE_ID

            val profileSummary = UserProfileSummary(
                id = activeId,
                name = legacySettings.employeeName,
                department = legacySettings.department,
                title = legacySettings.title,
                cyclePresetId = legacySettings.cyclePreset.id
            )

            // Write migrated v2 keys while keeping all legacy keys intact for rollback safety
            val editor = prefs.edit()
            editor.putString("active_profile_id", activeId)
            editor.putString("profile_${activeId}_settings", serializeSettings(legacySettings).toString())
            editor.putString("profile_${activeId}_overrides", serializeOverrides(legacyOverrides).toString())
            editor.putString("profiles_index_json", serializeProfileSummaries(listOf(profileSummary)).toString())
            editor.putString("hourly_wage_exact", legacySettings.hourlyWage.toString())
            editor.putBoolean("anchor_configured", legacySettings.isAnchorConfigured)
            editor.putInt("prefs_schema_version", PREFS_SCHEMA_VERSION)

            if (editor.commit()) {
                lastMigrationNotice = "Vardiya verileriniz yeni sürüme (v$PREFS_SCHEMA_VERSION) güvenle aktarıldı"
            }
        } catch (_: Exception) {
            // Never delete old data on migration failure; next launch will retry safely
        }
    }

    private fun loadActiveProfileId(): String =
        prefs.getString("active_profile_id", DEFAULT_PROFILE_ID)?.takeIf { it.isNotBlank() } ?: DEFAULT_PROFILE_ID

    private fun loadProfileSummaries(): List<UserProfileSummary> {
        val raw = prefs.getString("profiles_index_json", null)
        if (!raw.isNullOrBlank()) {
            try {
                val arr = JSONArray(raw)
                val list = mutableListOf<UserProfileSummary>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        UserProfileSummary(
                            id = obj.optString("id", DEFAULT_PROFILE_ID),
                            name = obj.optString("name", "Dr. Selin Yılmaz"),
                            department = obj.optString("department", "Acil Servis"),
                            title = obj.optString("title", "Acil Tıp Uzmanı"),
                            cyclePresetId = obj.optString("cyclePresetId", CyclePreset.CYCLE_12_36.id)
                        )
                    )
                }
                if (list.isNotEmpty()) return list
            } catch (_: Exception) {
                // Fallback below
            }
        }
        val current = _userSettings.value
        return listOf(
            UserProfileSummary(
                id = _activeProfileId.value,
                name = current.employeeName,
                department = current.department,
                title = current.title,
                cyclePresetId = current.cyclePreset.id
            )
        )
    }

    private fun saveProfileSummaries(list: List<UserProfileSummary>) {
        prefs.edit()
            .putString("profiles_index_json", serializeProfileSummaries(list).toString())
            .commit()
        _profiles.value = list
    }

    private fun serializeProfileSummaries(list: List<UserProfileSummary>): JSONArray {
        val arr = JSONArray()
        list.forEach { p ->
            arr.put(
                JSONObject().apply {
                    put("id", p.id)
                    put("name", p.name)
                    put("department", p.department)
                    put("title", p.title)
                    put("cyclePresetId", p.cyclePresetId)
                }
            )
        }
        return arr
    }

    private fun readFlatSettingsFromPrefs(): UserSettings {
        val presetId = prefs.getString("cycle_preset", CyclePreset.CYCLE_12_36.id) ?: CyclePreset.CYCLE_12_36.id
        val preset = CyclePreset.entries.find { it.id == presetId } ?: CyclePreset.CYCLE_12_36

        val refDateStr = prefs.getString("ref_start_date", "2026-11-01") ?: "2026-11-01"
        val refDate = try {
            LocalDate.parse(refDateStr)
        } catch (_: Exception) {
            LocalDate.of(2026, 11, 1)
        }

        val empDateStr = prefs.getString("emp_start_date", "2020-07-01") ?: "2020-07-01"
        val empDate = try {
            LocalDate.parse(empDateStr)
        } catch (_: Exception) {
            LocalDate.of(2020, 7, 1)
        }

        val birthDateStr = prefs.getString("birth_date", null)
        val birthDate = if (!birthDateStr.isNullOrBlank()) {
            try { LocalDate.parse(birthDateStr) } catch (_: Exception) { null }
        } else null

        val customSeqRaw = prefs.getString("custom_sequence", null)
        val customSeq = if (!customSeqRaw.isNullOrEmpty()) {
            try {
                val array = JSONArray(customSeqRaw)
                val list = mutableListOf<ShiftType>()
                for (i in 0 until array.length()) {
                    val code = array.getString(i)
                    ShiftType.entries.find { it.name == code }?.let { list.add(it) }
                }
                if (list.isNotEmpty()) list else preset.sequence
            } catch (_: Exception) {
                preset.sequence
            }
        } else {
            preset.sequence
        }

        val wageExact = prefs.getString("hourly_wage_exact", null)?.toDoubleOrNull()
            ?: prefs.getFloat("hourly_wage", 355f).toDouble()

        val hasRefOrOnboarding = prefs.contains("anchor_configured") ||
            prefs.contains("ref_start_date") ||
            prefs.getBoolean("onboarding_done", false)

        return UserSettings(
            employeeName = prefs.getString("employee_name", "Dr. Selin Yılmaz") ?: "Dr. Selin Yılmaz",
            department = prefs.getString("department", "İstanbul Acil Servis & 112 KKM") ?: "İstanbul Acil Servis & 112 KKM",
            professionGroup = prefs.getString("profession_group", ProfessionGroup.SAGLIK.id) ?: ProfessionGroup.SAGLIK.id,
            title = prefs.getString("title", "Acil Tıp Uzmanı") ?: "Acil Tıp Uzmanı",
            avatarUriOrPath = prefs.getString("avatar_path", "") ?: "",
            cyclePreset = preset,
            referenceStartDate = refDate,
            customSequence = customSeq,
            initialCycleOffset = prefs.getInt("initial_cycle_offset", 0),
            employmentStartDate = empDate,
            usedLeaveDays = prefs.getInt("used_leave_days", 6),
            shiftReminderEnabled = prefs.getBoolean("reminder_enabled", true),
            shiftReminderTime = prefs.getString("reminder_time", "20:00") ?: "20:00",
            preShiftAlertEnabled = prefs.getBoolean("pre_shift_alert_enabled", true),
            preShiftAlertMinutes = prefs.getInt("pre_shift_alert_minutes", 120),
            nightDndEnabled = prefs.getBoolean("night_dnd_enabled", true),
            calendarSyncEnabled = prefs.getBoolean("calendar_sync_enabled", true),
            autoHolidaysEnabled = prefs.getBoolean("auto_holidays_enabled", true),
            showFriendOnCalendar = prefs.getBoolean("show_friend_on_calendar", true),
            colorLayerStyle = prefs.getInt("color_layer_style", 1),
            commonLeaveAlertEnabled = prefs.getBoolean("common_leave_alert_enabled", true),
            hourlyWage = wageExact,
            isPinSecurityEnabled = prefs.getBoolean("pin_security_enabled", false),
            pinCode = prefs.getString("pin_code", "1234") ?: "1234",
            gunduzStartTime = prefs.getString("gunduz_start", "08:00") ?: "08:00",
            gunduzEndTime = prefs.getString("gunduz_end", "20:00") ?: "20:00",
            geceStartTime = prefs.getString("gece_start", "20:00") ?: "20:00",
            geceEndTime = prefs.getString("gece_end", "08:00") ?: "08:00",
            sabahStartTime = prefs.getString("sabah_start", "08:00") ?: "08:00",
            sabahEndTime = prefs.getString("sabah_end", "16:00") ?: "16:00",
            aksamStartTime = prefs.getString("aksam_start", "16:00") ?: "16:00",
            aksamEndTime = prefs.getString("aksam_end", "24:00") ?: "24:00",
            gece8StartTime = prefs.getString("gece8_start", "00:00") ?: "00:00",
            gece8EndTime = prefs.getString("gece8_end", "08:00") ?: "08:00",
            birthDate = birthDate,
            isAnchorConfigured = if (prefs.contains("anchor_configured")) {
                prefs.getBoolean("anchor_configured", false)
            } else {
                hasRefOrOnboarding
            },
            isOnboardingCompleted = if (prefs.contains("onboarding_done")) {
                prefs.getBoolean("onboarding_done", false)
            } else {
                prefs.contains("cycle_preset") ||
                    prefs.contains("employee_name") ||
                    prefs.contains("ref_start_date")
            },
            appLanguage = prefs.getString("app_language", "system") ?: "system",
            themeMode = prefs.getString("app_theme_mode", com.example.data.model.AppThemeMode.DARK.code)
                ?: com.example.data.model.AppThemeMode.DARK.code
        )
    }

    private fun loadSettings(profileId: String = loadActiveProfileId()): UserSettings {
        val profileJson = prefs.getString("profile_${profileId}_settings", null)
        if (!profileJson.isNullOrBlank()) {
            try {
                return deserializeSettings(JSONObject(profileJson), UserSettings(isAnchorConfigured = false))
            } catch (_: Exception) {
                // Fall back to flat prefs
            }
        }
        return readFlatSettingsFromPrefs()
    }

    private fun defaultColleagues(): List<ColleagueProfile> = emptyList()

    private fun loadColleagues(): List<ColleagueProfile> {
        if (!prefs.contains("colleagues_json")) {
            if (!prefs.getBoolean("demo_colleagues_purged", false)) {
                prefs.edit().putBoolean("demo_colleagues_purged", true).commit()
            }
            return emptyList()
        }
        val raw = prefs.getString("colleagues_json", null) ?: return emptyList()
        val parsed = parseColleaguesJson(raw) ?: return emptyList()
        if (!prefs.getBoolean("demo_colleagues_purged", false)) {
            val cleaned = parsed.filterNot { it.qrCodeId in LEGACY_DEMO_COLLEAGUE_IDS }
            prefs.edit()
                .putString("colleagues_json", serializeColleagues(cleaned).toString())
                .putBoolean("demo_colleagues_purged", true)
                .commit()
            return cleaned
        }
        return parsed
    }

    private fun parseColleaguesJson(raw: String): List<ColleagueProfile>? {
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<ColleagueProfile>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val presetId = obj.optString("presetId", CyclePreset.CYCLE_12_36.id)
                val preset = CyclePreset.entries.find { it.id == presetId } ?: CyclePreset.CYCLE_12_36
                val refDate = try {
                    LocalDate.parse(obj.optString("refDate", "2026-11-01"))
                } catch (_: Exception) {
                    LocalDate.of(2026, 11, 1)
                }
                val seqArr = obj.optJSONArray("customSequence")
                val customSeq = if (seqArr != null) {
                    (0 until seqArr.length()).mapNotNull { idx ->
                        ShiftType.entries.find { it.name == seqArr.getString(idx) }
                    }.takeIf { it.isNotEmpty() }
                } else null
                list.add(
                    ColleagueProfile(
                        name = obj.optString("name", "Mesai Arkadaşı"),
                        department = obj.optString("department", "Acil Servis"),
                        cyclePreset = preset,
                        referenceStartDate = refDate,
                        qrCodeId = obj.optString("qrCodeId", "PRSH-P2P"),
                        customSequence = customSeq,
                        initialCycleOffset = obj.optInt("initialCycleOffset", 0)
                    )
                )
            }
            list
        } catch (_: Exception) {
            null
        }
    }

    fun addOrUpdateColleague(profile: ColleagueProfile) {
        val current = _colleagues.value.toMutableList()
        val existingIdx = current.indexOfFirst {
            it.name.equals(profile.name, ignoreCase = true) ||
                (profile.qrCodeId.isNotBlank() && it.qrCodeId == profile.qrCodeId)
        }
        if (existingIdx >= 0) {
            current[existingIdx] = profile
        } else {
            current.add(0, profile)
        }
        saveColleagues(current)
    }

    fun removeColleague(profile: ColleagueProfile) {
        val current = _colleagues.value.filterNot {
            it.qrCodeId == profile.qrCodeId && it.name == profile.name
        }
        saveColleagues(current)
    }

    private fun serializeColleagues(list: List<ColleagueProfile>): JSONArray {
        val arr = JSONArray()
        list.forEach { col ->
            val obj = JSONObject()
            obj.put("name", col.name)
            obj.put("department", col.department)
            obj.put("presetId", col.cyclePreset.id)
            obj.put("refDate", col.referenceStartDate.format(dateFormatter))
            obj.put("qrCodeId", col.qrCodeId)
            obj.put("initialCycleOffset", col.initialCycleOffset)
            col.customSequence?.let { seq ->
                val sArr = JSONArray()
                seq.forEach { sArr.put(it.name) }
                obj.put("customSequence", sArr)
            }
            arr.put(obj)
        }
        return arr
    }

    private fun saveColleagues(list: List<ColleagueProfile>) {
        prefs.edit().putString("colleagues_json", serializeColleagues(list).toString()).commit()
        _colleagues.value = list
    }

    private fun loadOverrides(profileId: String = loadActiveProfileId()): Map<String, DayOverride> {
        val profileJson = prefs.getString("profile_${profileId}_overrides", null)
        if (profileJson != null) {
            return readOverridesFromJsonString(profileJson)
        }
        val legacyJson = prefs.getString("day_overrides", null)
        return readOverridesFromJsonString(legacyJson)
    }

    private fun readOverridesFromJsonString(jsonStr: String?): Map<String, DayOverride> {
        if (jsonStr.isNullOrBlank()) return emptyMap()
        return try {
            val json = JSONObject(jsonStr)
            val map = mutableMapOf<String, DayOverride>()
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val obj = json.getJSONObject(key)
                val typeName = obj.optString("shiftType", ShiftType.GUNDUZ.name)
                val type = ShiftType.entries.find { it.name == typeName } ?: ShiftType.GUNDUZ
                val overtime = obj.optDouble("overtimeHours", 0.0)
                val note = obj.optString("note", "")
                val start = if (obj.has("startTime") && !obj.isNull("startTime")) obj.getString("startTime") else null
                val end = if (obj.has("endTime") && !obj.isNull("endTime")) obj.getString("endTime") else null
                map[key] = DayOverride(key, type, overtime, note, start, end)
            }
            map
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private fun serializeOverrides(map: Map<String, DayOverride>): JSONObject {
        val json = JSONObject()
        map.forEach { (key, value) ->
            val obj = JSONObject()
            obj.put("dateIso", value.dateIso)
            obj.put("shiftType", value.shiftType.name)
            obj.put("overtimeHours", value.overtimeHours)
            obj.put("note", value.note)
            value.startTime?.let { obj.put("startTime", it) }
            value.endTime?.let { obj.put("endTime", it) }
            json.put(key, obj)
        }
        return json
    }

    private fun saveOverrides(map: Map<String, DayOverride>) {
        val serialized = serializeOverrides(map).toString()
        val activeId = _activeProfileId.value
        prefs.edit()
            .putString("profile_${activeId}_overrides", serialized)
            .putString("day_overrides", serialized)
            .commit()
        _dayOverrides.value = map
    }

    // Multi-profile management
    fun switchProfile(profileId: String): Boolean {
        val exists = _profiles.value.any { it.id == profileId }
        if (!exists) return false
        prefs.edit().putString("active_profile_id", profileId).commit()
        _activeProfileId.value = profileId
        val loadedSettings = loadSettings(profileId)
        val loadedOverrides = loadOverrides(profileId)
        persistFlatSettings(loadedSettings)
        prefs.edit().putString("day_overrides", serializeOverrides(loadedOverrides).toString()).commit()
        _userSettings.value = loadedSettings
        _dayOverrides.value = loadedOverrides
        return true
    }

    fun createProfile(
        name: String,
        department: String,
        title: String,
        cyclePreset: CyclePreset = CyclePreset.CYCLE_12_36
    ): UserProfileSummary {
        val newId = "profile_${UUID.randomUUID().toString().take(8)}"
        val baseSettings = _userSettings.value.copy(
            employeeName = name.ifBlank { "Yeni Profil" },
            department = department.ifBlank { "Vardiya Birimi" },
            title = title.ifBlank { "Vardiya Personeli" },
            cyclePreset = cyclePreset,
            customSequence = cyclePreset.sequence.ifEmpty {
                listOf(ShiftType.GUNDUZ, ShiftType.GECE, ShiftType.IZIN)
            },
            initialCycleOffset = 0,
            usedLeaveDays = 0,
            isAnchorConfigured = false,
            isOnboardingCompleted = true
        )
        val summary = UserProfileSummary(
            id = newId,
            name = baseSettings.employeeName,
            department = baseSettings.department,
            title = baseSettings.title,
            cyclePresetId = baseSettings.cyclePreset.id
        )
        val updatedProfiles = _profiles.value + summary
        prefs.edit()
            .putString("profile_${newId}_settings", serializeSettings(baseSettings).toString())
            .putString("profile_${newId}_overrides", "{}")
            .putString("active_profile_id", newId)
            .commit()
        saveProfileSummaries(updatedProfiles)
        _activeProfileId.value = newId
        persistFlatSettings(baseSettings)
        prefs.edit().putString("day_overrides", "{}").commit()
        _userSettings.value = baseSettings
        _dayOverrides.value = emptyMap()
        return summary
    }

    fun deleteProfile(profileId: String): Boolean {
        val current = _profiles.value
        if (current.size <= 1) return false
        val remaining = current.filterNot { it.id == profileId }
        if (remaining.size == current.size) return false

        prefs.edit()
            .remove("profile_${profileId}_settings")
            .remove("profile_${profileId}_overrides")
            .commit()
        saveProfileSummaries(remaining)

        if (_activeProfileId.value == profileId) {
            switchProfile(remaining.first().id)
        }
        return true
    }

    fun toggleBreak(): Boolean {
        if (!isBreakRunning) {
            isBreakRunning = true
            breakStartTime = LocalDateTime.now()
        } else {
            isBreakRunning = false
            breakStartTime?.let {
                accumulatedBreakSeconds += Duration.between(it, LocalDateTime.now()).seconds
            }
            breakStartTime = null
        }
        return isBreakRunning
    }

    fun resetBreak() {
        isBreakRunning = false
        breakStartTime = null
        accumulatedBreakSeconds = 0L
    }

    private fun persistFlatSettings(newSettings: UserSettings) {
        val jsonArray = JSONArray()
        newSettings.customSequence.forEach { jsonArray.put(it.name) }

        val editor = prefs.edit()
            .putString("employee_name", newSettings.employeeName)
            .putString("department", newSettings.department)
            .putString("profession_group", newSettings.professionGroup)
            .putString("title", newSettings.title)
            .putString("avatar_path", newSettings.avatarUriOrPath)
            .putString("cycle_preset", newSettings.cyclePreset.id)
            .putString("ref_start_date", newSettings.referenceStartDate.format(dateFormatter))
            .putString("emp_start_date", newSettings.employmentStartDate.format(dateFormatter))
            .putInt("initial_cycle_offset", newSettings.initialCycleOffset)
            .putInt("used_leave_days", newSettings.usedLeaveDays)
            .putBoolean("reminder_enabled", newSettings.shiftReminderEnabled)
            .putString("reminder_time", newSettings.shiftReminderTime)
            .putBoolean("pre_shift_alert_enabled", newSettings.preShiftAlertEnabled)
            .putInt("pre_shift_alert_minutes", newSettings.preShiftAlertMinutes)
            .putBoolean("night_dnd_enabled", newSettings.nightDndEnabled)
            .putBoolean("calendar_sync_enabled", newSettings.calendarSyncEnabled)
            .putBoolean("auto_holidays_enabled", newSettings.autoHolidaysEnabled)
            .putBoolean("show_friend_on_calendar", newSettings.showFriendOnCalendar)
            .putInt("color_layer_style", newSettings.colorLayerStyle)
            .putBoolean("common_leave_alert_enabled", newSettings.commonLeaveAlertEnabled)
            .putFloat("hourly_wage", newSettings.hourlyWage.toFloat())
            .putString("hourly_wage_exact", newSettings.hourlyWage.toString())
            .putBoolean("pin_security_enabled", newSettings.isPinSecurityEnabled)
            .putString("pin_code", newSettings.pinCode)
            .putString("gunduz_start", newSettings.gunduzStartTime)
            .putString("gunduz_end", newSettings.gunduzEndTime)
            .putString("gece_start", newSettings.geceStartTime)
            .putString("gece_end", newSettings.geceEndTime)
            .putString("sabah_start", newSettings.sabahStartTime)
            .putString("sabah_end", newSettings.sabahEndTime)
            .putString("aksam_start", newSettings.aksamStartTime)
            .putString("aksam_end", newSettings.aksamEndTime)
            .putString("gece8_start", newSettings.gece8StartTime)
            .putString("gece8_end", newSettings.gece8EndTime)
            .putBoolean("anchor_configured", newSettings.isAnchorConfigured)
            .putBoolean("onboarding_done", newSettings.isOnboardingCompleted)
            .putString("app_language", newSettings.appLanguage)
            .putString("app_theme_mode", newSettings.themeMode)
            .putString("custom_sequence", jsonArray.toString())
            .putInt("prefs_schema_version", PREFS_SCHEMA_VERSION)

        if (newSettings.birthDate != null) {
            editor.putString("birth_date", newSettings.birthDate.format(dateFormatter))
        } else {
            editor.remove("birth_date")
        }
        editor.commit()
    }

    fun updateSettings(newSettings: UserSettings) {
        val activeId = _activeProfileId.value
        persistFlatSettings(newSettings)
        prefs.edit()
            .putString("profile_${activeId}_settings", serializeSettings(newSettings).toString())
            .commit()

        val updatedSummaries = _profiles.value.map { p ->
            if (p.id == activeId) {
                p.copy(
                    name = newSettings.employeeName,
                    department = newSettings.department,
                    title = newSettings.title,
                    cyclePresetId = newSettings.cyclePreset.id
                )
            } else p
        }
        saveProfileSummaries(updatedSummaries)
        _userSettings.value = newSettings
    }

    fun saveDayOverride(override: DayOverride) {
        val current = _dayOverrides.value.toMutableMap()
        current[override.dateIso] = override
        saveOverrides(current)
    }

    fun removeDayOverride(dateIso: String) {
        val current = _dayOverrides.value.toMutableMap()
        current.remove(dateIso)
        saveOverrides(current)
    }

    fun resetToDefaults() {
        prefs.edit().clear().commit()
        prefs.edit()
            .putInt("prefs_schema_version", PREFS_SCHEMA_VERSION)
            .putBoolean("demo_colleagues_purged", true)
            .commit()
        _activeProfileId.value = DEFAULT_PROFILE_ID
        val freshSettings = loadSettings(DEFAULT_PROFILE_ID)
        _userSettings.value = freshSettings
        _dayOverrides.value = emptyMap()
        _colleagues.value = emptyList()
        _profiles.value = listOf(
            UserProfileSummary(
                id = DEFAULT_PROFILE_ID,
                name = freshSettings.employeeName,
                department = freshSettings.department,
                title = freshSettings.title,
                cyclePresetId = freshSettings.cyclePreset.id
            )
        )
        resetBreak()
    }

    fun parseTimeSafe(timeStr: String?, fallback: LocalTime): LocalTime =
        ShiftEngine.parseTimeSafe(timeStr, fallback)

    fun calculateDurationHours(startStr: String, endStr: String, type: ShiftType): Double =
        ShiftEngine.calculateDurationHours(startStr, endStr, type)

    fun resolveDefaultTimes(type: ShiftType, settings: UserSettings = _userSettings.value): Pair<String, String> =
        ShiftEngine.resolveDefaultTimes(type, settings)

    fun getShiftForDate(date: LocalDate, settings: UserSettings = _userSettings.value): ShiftDay =
        ShiftEngine.dayShiftFor(date, settings, _dayOverrides.value)

    fun getMonthDays(year: Int, month: Int, settings: UserSettings = _userSettings.value): List<ShiftDay> =
        ShiftEngine.buildMonth(year, month, settings, _dayOverrides.value)

    fun getWeekDays(centerDate: LocalDate, settings: UserSettings = _userSettings.value): List<ShiftDay> {
        val dayOfWeek = centerDate.dayOfWeek.value
        val monday = centerDate.minusDays((dayOfWeek - 1).toLong())
        return (0..6).map { i -> getShiftForDate(monday.plusDays(i.toLong()), settings) }
    }

    fun getRollingWeekFlowDays(
        centerDate: LocalDate,
        daysBefore: Int = 7,
        daysAfter: Int = 7,
        settings: UserSettings = _userSettings.value
    ): List<ShiftDay> {
        return (-daysBefore..daysAfter).map { offset ->
            getShiftForDate(centerDate.plusDays(offset.toLong()), settings)
        }
    }

    private fun shiftWindowForDate(day: ShiftDay): Pair<LocalDateTime, LocalDateTime>? {
        if (!day.type.isWork) return null
        val startLocal = parseTimeSafe(day.startTime, parseTimeSafe(day.type.defaultStart, LocalTime.of(8, 0)))
        val endLocal = parseTimeSafe(day.endTime, parseTimeSafe(day.type.defaultEnd, LocalTime.of(20, 0)))
        val startDt = LocalDateTime.of(day.date, startLocal)
        val crossesMidnight = day.type == ShiftType.NOBET24 || !endLocal.isAfter(startLocal)
        val endDt = if (crossesMidnight) {
            LocalDateTime.of(day.date.plusDays(1), endLocal)
        } else {
            LocalDateTime.of(day.date, endLocal)
        }
        return startDt to endDt
    }

    fun calculateDutyTelemetry(now: LocalDateTime = LocalDateTime.now()): DutyTelemetry {
        val today = now.toLocalDate()
        val yesterday = today.minusDays(1)
        val tomorrow = today.plusDays(1)

        val todayShift = getShiftForDate(today)
        val yesterdayShift = getShiftForDate(yesterday)

        var isOnDuty = false
        var activeShift: ShiftDay? = null
        var shiftStartTime = LocalDateTime.of(today, LocalTime.of(8, 0))
        var shiftEndTime = LocalDateTime.of(today, LocalTime.of(20, 0))

        shiftWindowForDate(yesterdayShift)?.let { (yStart, yEnd) ->
            if (!now.isBefore(yStart) && now.isBefore(yEnd)) {
                isOnDuty = true
                activeShift = yesterdayShift
                shiftStartTime = yStart
                shiftEndTime = yEnd
            }
        }

        if (!isOnDuty) {
            shiftWindowForDate(todayShift)?.let { (tStart, tEnd) ->
                if (!now.isBefore(tStart) && now.isBefore(tEnd)) {
                    isOnDuty = true
                    activeShift = todayShift
                    shiftStartTime = tStart
                    shiftEndTime = tEnd
                }
            }
        }

        var nextShift: ShiftDay = todayShift
        var nextShiftStartDt: LocalDateTime = LocalDateTime.of(tomorrow, parseTimeSafe(_userSettings.value.gunduzStartTime, LocalTime.of(8, 0)))
        var foundNext = false
        for (offset in 0..14) {
            val candidateDate = today.plusDays(offset.toLong())
            val candidateShift = getShiftForDate(candidateDate)
            val window = shiftWindowForDate(candidateShift)
            if (window != null && window.first.isAfter(now)) {
                nextShift = candidateShift
                nextShiftStartDt = window.first
                foundNext = true
                break
            }
        }
        if (!foundNext) {
            nextShift = getShiftForDate(tomorrow)
        }

        val remainingSeconds: Long
        val progressPercent: Float
        val currentShiftType: ShiftType?

        val currentActive = activeShift
        if (isOnDuty && currentActive != null) {
            currentShiftType = currentActive.type
            val totalSeconds = Duration.between(shiftStartTime, shiftEndTime).seconds.coerceAtLeast(1)
            val elapsedSeconds = Duration.between(shiftStartTime, now).seconds.coerceIn(0, totalSeconds)
            remainingSeconds = (totalSeconds - elapsedSeconds).coerceAtLeast(0)
            progressPercent = (elapsedSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
        } else {
            currentShiftType = if (todayShift.type.isWork) todayShift.type else ShiftType.IZIN
            remainingSeconds = Duration.between(now, nextShiftStartDt).seconds.coerceAtLeast(0)
            val windowHoursSeconds = (24 * 3600).toFloat()
            progressPercent = (1f - (remainingSeconds / windowHoursSeconds)).coerceIn(0.08f, 0.96f)
        }

        val hours = remainingSeconds / 3600
        val minutes = (remainingSeconds % 3600) / 60
        val seconds = remainingSeconds % 60
        val formattedTime = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        val percentText = "%${(progressPercent * 100).toInt()}"

        val nextHours = Duration.between(now, nextShiftStartDt).toHours().coerceAtLeast(0)
        val nextMins = (Duration.between(now, nextShiftStartDt).toMinutes() % 60).coerceAtLeast(0)
        val nextShiftHoursRemaining = if (nextHours == 0L) {
            "${nextMins.coerceAtLeast(1)} dk kaldı"
        } else {
            "${nextHours}s kaldı"
        }

        val nextShiftTitle = when (nextShift.date) {
            today -> "Bugün"
            tomorrow -> "Yarın"
            else -> nextShift.date.format(DateTimeFormatter.ofPattern("d MMMM", Locale.forLanguageTag("tr-TR")))
        }

        val activeOrNextShift = activeShift ?: todayShift
        val activeHoursInt = activeOrNextShift.baseHours.toInt()
        val shiftHoursRange = if (activeOrNextShift.type.isWork) {
            "${activeOrNextShift.startTime} - ${activeOrNextShift.endTime} (${activeHoursInt}s)"
        } else {
            "İzin Günü • Sonraki: ${nextShift.startTime}"
        }

        val nextShiftDetails = "${nextShift.startTime} - ${nextShift.endTime} (${nextShift.type.title})"

        val currentBreakSeconds = if (isBreakRunning && breakStartTime != null) {
            accumulatedBreakSeconds + Duration.between(breakStartTime, now).seconds
        } else {
            accumulatedBreakSeconds
        }
        val bH = currentBreakSeconds / 3600
        val bM = (currentBreakSeconds % 3600) / 60
        val bS = currentBreakSeconds % 60
        val bFmt = String.format(Locale.US, "%02d:%02d:%02d", bH, bM, bS)

        return DutyTelemetry(
            isOnDuty = isOnDuty,
            currentShiftType = currentShiftType,
            remainingSeconds = remainingSeconds,
            remainingFormatted = formattedTime,
            progressPercent = progressPercent,
            progressPercentText = percentText,
            shiftHoursRange = shiftHoursRange,
            nextShiftTitle = nextShiftTitle,
            nextShiftDetails = nextShiftDetails,
            nextShiftHoursRemaining = nextShiftHoursRemaining,
            nextShiftType = nextShift.type,
            isBreakActive = isBreakRunning,
            breakSeconds = currentBreakSeconds,
            breakFormatted = bFmt
        )
    }

    fun calculateTimesheetStats(
        year: Int,
        month: Int,
        settings: UserSettings = _userSettings.value,
        asOfDate: LocalDate = LocalDate.now()
    ): TimesheetStats {
        val days = getMonthDays(year, month, settings)
        return ShiftEngine.computeMonthStats(days, settings, asOfDate)
    }

    fun getColleagueShiftForDate(
        colleague: ColleagueProfile,
        date: LocalDate
    ): ShiftType {
        val seq = colleague.customSequence?.takeIf { it.isNotEmpty() } ?: colleague.cyclePreset.sequence
        return ShiftEngine.stateFor(
            date = date,
            anchor = ShiftAnchor(colleague.referenceStartDate, colleague.initialCycleOffset),
            pattern = seq
        )
    }

    fun getColleagueShiftDay(
        colleague: ColleagueProfile,
        date: LocalDate,
        settings: UserSettings = _userSettings.value
    ): ShiftDay {
        val type = getColleagueShiftForDate(colleague, date)
        val (startTime, endTime) = ShiftEngine.resolveDefaultTimes(type, settings)
        val baseHours = if (type.isWork) ShiftEngine.calculateDurationHours(startTime, endTime, type) else 0.0
        val holiday = if (settings.autoHolidaysEnabled) ShiftEngine.holidayForDate(date) else null
        return ShiftDay(
            date = date,
            type = type,
            startTime = startTime,
            endTime = endTime,
            baseHours = baseHours,
            overtimeHours = 0.0,
            totalHours = baseHours,
            note = "${colleague.name} vardiyası",
            isOverride = false,
            holidayName = holiday?.name,
            isHalfDayHoliday = holiday?.isHalfDay == true
        )
    }

    fun calculateShiftMatching(
        colleague: ColleagueProfile,
        year: Int,
        month: Int,
        userSettings: UserSettings = _userSettings.value
    ): List<ShiftMatchDay> {
        val userDays = getMonthDays(year, month, userSettings)
        val results = mutableListOf<ShiftMatchDay>()

        for (userDay in userDays) {
            val date = userDay.date
            val colleagueShift = getColleagueShiftForDate(colleague, date)

            val matchType: MatchType
            val note: String

            if (!userDay.type.isWork && !colleagueShift.isWork) {
                matchType = MatchType.COMMON_OFF
                note = "Tam gün ortak izin"
            } else if (userDay.type.isWork && colleagueShift.isWork && userDay.type == colleagueShift) {
                matchType = MatchType.COMMON_DUTY
                note = "Aynı vardiya birlikte nöbet (${userDay.type.title})"
            } else if (!userDay.type.isWork && colleagueShift.isWork) {
                matchType = MatchType.SWAP_OPPORTUNITY
                note = "Sen izinlisin, ${colleague.name} nöbette (Takas fırsatı)"
            } else if (userDay.type.isWork && !colleagueShift.isWork) {
                matchType = MatchType.SWAP_OPPORTUNITY
                note = "Sen nöbettesin, ${colleague.name} izinli (Zıt vardiya)"
            } else {
                matchType = MatchType.SWAP_OPPORTUNITY
                note = "Farklı vardiya rotasyonu (${userDay.type.title} / ${colleagueShift.title})"
            }

            results.add(
                ShiftMatchDay(
                    date = date,
                    userShift = userDay.type,
                    colleagueShift = colleagueShift,
                    matchType = matchType,
                    note = note
                )
            )
        }
        return results
    }

    fun generateSyncPayload(settings: UserSettings = _userSettings.value): String {
        val enc = { v: String -> java.net.URLEncoder.encode(v, "UTF-8") }
        val sb = StringBuilder("PROSHIFT://sync?")
            .append("v=2")
            .append("&name=").append(enc(settings.employeeName))
            .append("&dept=").append(enc(settings.department))
            .append("&preset=").append(enc(settings.cyclePreset.id))
            .append("&ref=").append(settings.referenceStartDate.format(dateFormatter))
            .append("&offset=").append(settings.initialCycleOffset)
        val seq = ShiftEngine.resolvePattern(settings)
        if ((settings.cyclePreset == CyclePreset.CUSTOM || !settings.cyclePreset.hasDefaultPattern) && seq.isNotEmpty()) {
            sb.append("&seq=").append(enc(seq.joinToString(",") { it.name }))
        }
        return sb.toString()
    }

    fun parseSyncPayload(raw: String): ColleagueProfile? =
        (parseSyncPayloadDetailed(raw) as? com.example.data.model.QrParseResult.Success)?.colleague

    fun parseSyncPayloadDetailed(raw: String): com.example.data.model.QrParseResult {
        val trimmed = raw.trim()
        val preview = trimmed.replace('\n', ' ').take(90)
        if (trimmed.isEmpty()) {
            return com.example.data.model.QrParseResult.Failure(
                errorCode = "EMPTY_INPUT",
                errorMessage = "QR kodu veya senkron bağlantısı boş.",
                rawPreview = ""
            )
        }

        // 1. JSON Format (Web PROSHIFT / Cross-Platform Sync)
        val jsonCandidate = extractJsonObjectCandidate(trimmed)
        if (jsonCandidate != null) {
            return parseJsonSyncPayload(jsonCandidate, preview)
        }

        // 2. PROSHIFT://sync? URI Schema
        val startIdx = trimmed.indexOf("PROSHIFT://sync?", ignoreCase = true)
        if (startIdx < 0) {
            return com.example.data.model.QrParseResult.Failure(
                errorCode = "UNSUPPORTED_SCHEME",
                errorMessage = "Geçersiz format: PROSHIFT URI veya JSON verisi bulunamadı.",
                rawPreview = preview
            )
        }

        val uriSlice = trimmed.substring(startIdx)
            .lineSequence()
            .firstOrNull()
            ?.trim()
            ?.substringBefore(" ")
            ?.trimEnd('.', ',', ';', ')', '!')
            ?: return com.example.data.model.QrParseResult.Failure(
                errorCode = "MALFORMED_URI",
                errorMessage = "Senkron URI ayrıştırılamadı.",
                rawPreview = preview
            )

        val query = uriSlice.substringAfter("?", "")
        if (query.isBlank()) {
            return com.example.data.model.QrParseResult.Failure(
                errorCode = "EMPTY_QUERY",
                errorMessage = "QR bağlantısında vardiya parametreleri eksik.",
                rawPreview = preview
            )
        }

        val params = mutableMapOf<String, String>()
        query.split("&").forEach { pair ->
            val idx = pair.indexOf('=')
            if (idx > 0) {
                val key = pair.substring(0, idx).lowercase(Locale.ROOT)
                val value = try {
                    java.net.URLDecoder.decode(pair.substring(idx + 1), "UTF-8")
                } catch (_: Exception) {
                    pair.substring(idx + 1).replace("_", " ")
                }
                params[key] = value
            }
        }

        val name = params["name"]?.trim()?.takeIf { it.isNotEmpty() }
            ?: return com.example.data.model.QrParseResult.Failure(
                errorCode = "MISSING_NAME",
                errorMessage = "Personel adı ('name') parametresi eksik.",
                rawPreview = preview
            )

        val rawSeqTokens = params["seq"]
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            .orEmpty()
        val customSeq = parseShiftSequenceTokens(rawSeqTokens)
        val presetId = params["preset"]?.trim()?.takeIf { it.isNotEmpty() }
        if (presetId == null && customSeq == null) {
            return com.example.data.model.QrParseResult.Failure(
                errorCode = "MISSING_PRESET",
                errorMessage = "Vardiya düzeni ('preset' veya 'seq') parametresi eksik.",
                rawPreview = preview
            )
        }

        val dept = params["dept"]?.trim()?.ifEmpty { "Vardiya Ekibi" } ?: "Vardiya Ekibi"
        val refRaw = params["ref"]?.trim().orEmpty()
        val offset = params["offset"]?.toIntOrNull() ?: 0
        val payloadVersion = params["v"]?.takeIf { it.isNotBlank() }?.let { "v$it" } ?: "v1"

        return buildResolvedColleagueResult(
            name = name,
            department = dept,
            presetId = presetId,
            customSeq = customSeq,
            refRaw = refRaw,
            offset = offset,
            payloadVersion = payloadVersion,
            normalizedSource = uriSlice
        )
    }

    private fun extractJsonObjectCandidate(trimmed: String): JSONObject? {
        val start = trimmed.indexOf('{')
        val end = trimmed.lastIndexOf('}')
        if (start < 0 || end <= start) return null
        val slice = trimmed.substring(start, end + 1)
        return try {
            val obj = JSONObject(slice)
            val proto = obj.optString("p", obj.optString("app", obj.optString("protocol", ""))).trim()
            val hasProshiftProto = proto.equals("PROSHIFT", ignoreCase = true)
            val hasShiftFields = obj.has("pattern") || obj.has("seq") || obj.has("sequence") || obj.has("cycle") || obj.has("preset") || obj.has("presetId")
            val hasNameField = obj.has("name") || obj.has("n") || obj.has("employeeName") || obj.has("user") || obj.has("ad")
            if (hasProshiftProto || (hasShiftFields && hasNameField)) obj else null
        } catch (_: Exception) {
            null
        }
    }

    private fun parseJsonSyncPayload(
        obj: JSONObject,
        preview: String
    ): com.example.data.model.QrParseResult {
        val name = listOf("name", "n", "employeeName", "user", "ad")
            .firstNotNullOfOrNull { key -> obj.optString(key, "").trim().takeIf { it.isNotEmpty() } }
            ?: return com.example.data.model.QrParseResult.Failure(
                errorCode = "MISSING_NAME",
                errorMessage = "JSON verisinde personel adı ('name') eksik.",
                rawPreview = preview
            )

        val rawSeqTokens = extractJsonSequenceTokens(obj)
        val customSeq = parseShiftSequenceTokens(rawSeqTokens)
        val presetId = listOf("preset", "presetId", "system", "regime")
            .firstNotNullOfOrNull { key -> obj.optString(key, "").trim().takeIf { it.isNotEmpty() } }

        if (presetId == null && customSeq == null) {
            return com.example.data.model.QrParseResult.Failure(
                errorCode = "MISSING_PRESET",
                errorMessage = "JSON verisinde vardiya dizisi ('pattern' veya 'preset') eksik.",
                rawPreview = preview
            )
        }

        val dept = listOf("dept", "d", "department", "birim")
            .firstNotNullOfOrNull { key -> obj.optString(key, "").trim().takeIf { it.isNotEmpty() } }
            ?: "Vardiya Ekibi"

        val refRaw = listOf("ref", "r", "refDate", "referenceStartDate", "anchorDate", "startDate", "start")
            .firstNotNullOfOrNull { key -> obj.optString(key, "").trim().takeIf { it.isNotEmpty() } }
            .orEmpty()

        val offset = when {
            obj.has("offset") -> obj.optInt("offset", 0)
            obj.has("o") -> obj.optInt("o", 0)
            obj.has("anchorIndex") -> obj.optInt("anchorIndex", 0)
            obj.has("idx") -> obj.optInt("idx", 0)
            obj.has("initialCycleOffset") -> obj.optInt("initialCycleOffset", 0)
            else -> 0
        }

        val versionRaw = listOf("v", "version")
            .firstNotNullOfOrNull { key -> obj.optString(key, "").trim().takeIf { it.isNotEmpty() } }
        val payloadVersion = if (versionRaw != null) "v$versionRaw-json" else "json"

        val canonicalUri = buildString {
            val enc = { v: String -> java.net.URLEncoder.encode(v, "UTF-8") }
            append("PROSHIFT://sync?v=2")
            append("&name=").append(enc(name))
            append("&dept=").append(enc(dept))
            if (presetId != null) append("&preset=").append(enc(presetId))
            if (refRaw.isNotEmpty()) append("&ref=").append(enc(refRaw))
            append("&offset=").append(offset)
            if (customSeq != null) append("&seq=").append(enc(customSeq.joinToString(",") { it.name }))
        }

        return buildResolvedColleagueResult(
            name = name,
            department = dept,
            presetId = presetId,
            customSeq = customSeq,
            refRaw = refRaw,
            offset = offset,
            payloadVersion = payloadVersion,
            normalizedSource = canonicalUri
        )
    }

    private fun extractJsonSequenceTokens(obj: JSONObject): List<String> {
        val keys = listOf("pattern", "seq", "sequence", "cycle")
        for (key in keys) {
            val arr = obj.optJSONArray(key)
            if (arr != null && arr.length() > 0) {
                val out = mutableListOf<String>()
                for (i in 0 until arr.length()) {
                    val item = arr.optString(i, "").trim()
                    if (item.isNotEmpty()) out.add(item)
                }
                if (out.isNotEmpty()) return out
            }
            val str = obj.optString(key, "").trim()
            if (str.isNotEmpty() && !str.startsWith("[")) {
                val split = str.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                if (split.isNotEmpty()) return split
            }
        }
        return emptyList()
    }

    private fun parseShiftSequenceTokens(rawTokens: List<String>): List<ShiftType>? {
        if (rawTokens.isEmpty()) return null
        val trUpper = Locale.forLanguageTag("tr-TR")
        val normalized = rawTokens.map { it.trim().uppercase(trUpper) }.filter { it.isNotEmpty() }
        if (normalized.isEmpty()) return null

        val has8hMarker = normalized.any { token ->
            token in setOf("S", "SABAH", "M", "MORNING", "A", "AKSAM", "AKŞAM", "E", "EVENING", "N8", "GECE8")
        }

        val resolved = normalized.mapNotNull { token ->
            ShiftType.entries.find { it.name.uppercase(Locale.ROOT) == token } ?: when (token) {
                "O", "OFF", "I", "İ", "IZIN", "İZİN", "BOS", "BOŞ", "TATIL", "TATİL" -> ShiftType.IZIN
                "G", "GUNDUZ", "GÜNDÜZ", "D", "DAY" -> if (has8hMarker) ShiftType.SABAH else ShiftType.GUNDUZ
                "S", "SABAH", "M", "MORNING" -> ShiftType.SABAH
                "A", "AKSAM", "AKŞAM", "E", "EVENING" -> ShiftType.AKSAM
                "N8", "GECE8" -> ShiftType.GECE8
                "N", "GECE", "NIGHT" -> if (has8hMarker) ShiftType.GECE8 else ShiftType.GECE
                "24", "24S", "NOBET24", "NÖBET24" -> ShiftType.NOBET24
                "R", "RAPOR" -> ShiftType.RAPOR
                "OZEL", "ÖZEL" -> ShiftType.OZEL
                else -> null
            }
        }
        return resolved.takeIf { it.isNotEmpty() }
    }

    private fun buildResolvedColleagueResult(
        name: String,
        department: String,
        presetId: String?,
        customSeq: List<ShiftType>?,
        refRaw: String,
        offset: Int,
        payloadVersion: String,
        normalizedSource: String
    ): com.example.data.model.QrParseResult.Success {
        val matchedById = presetId?.let { id -> CyclePreset.entries.find { it.id == id } }
        val matchedBySeq = if (matchedById == null && customSeq != null) {
            CyclePreset.entries.find { it.hasDefaultPattern && it.sequence == customSeq }
        } else null

        val warnings = mutableListOf<String>()
        val preset = when {
            matchedById != null -> matchedById
            matchedBySeq != null -> matchedBySeq
            customSeq != null -> CyclePreset.CUSTOM
            else -> {
                warnings.add("Bilinmeyen şablon '$presetId' için varsayılan döngü uygulandı")
                CyclePreset.CYCLE_12_36
            }
        }

        val refDate = if (refRaw.isNotEmpty()) {
            try {
                LocalDate.parse(refRaw)
            } catch (_: Exception) {
                warnings.add("Geçersiz başlangıç tarihi ('$refRaw'); bugün varsayıldı")
                LocalDate.now()
            }
        } else {
            warnings.add("Başlangıç tarihi eksik; bugün varsayıldı")
            LocalDate.now()
        }

        val finalCustomSeq = if (preset == CyclePreset.CUSTOM || (customSeq != null && preset.sequence != customSeq)) {
            customSeq
        } else null

        val finalPreset = if (finalCustomSeq != null && preset.hasDefaultPattern) CyclePreset.CUSTOM else preset
        val stableHash = kotlin.math.abs(normalizedSource.hashCode()).toString(16).uppercase(Locale.ROOT)
        val colleague = ColleagueProfile(
            name = name,
            department = department,
            cyclePreset = finalPreset,
            referenceStartDate = refDate,
            qrCodeId = "PRSH-QR-$stableHash",
            customSequence = finalCustomSeq,
            initialCycleOffset = offset
        )

        return com.example.data.model.QrParseResult.Success(
            colleague = colleague,
            payloadVersion = payloadVersion,
            normalizedUri = normalizedSource,
            warningMessage = warnings.takeIf { it.isNotEmpty() }?.joinToString(" • ")
        )
    }

    private fun serializeSettings(s: UserSettings): JSONObject = JSONObject().apply {
        put("employeeName", s.employeeName)
        put("department", s.department)
        put("professionGroup", s.professionGroup)
        put("title", s.title)
        put("avatarUriOrPath", s.avatarUriOrPath)
        put("cyclePreset", s.cyclePreset.id)
        put("referenceStartDate", s.referenceStartDate.format(dateFormatter))
        put("employmentStartDate", s.employmentStartDate.format(dateFormatter))
        s.birthDate?.let { put("birthDate", it.format(dateFormatter)) }
        put("usedLeaveDays", s.usedLeaveDays)
        put("gunduzStartTime", s.gunduzStartTime)
        put("gunduzEndTime", s.gunduzEndTime)
        put("geceStartTime", s.geceStartTime)
        put("geceEndTime", s.geceEndTime)
        put("sabahStartTime", s.sabahStartTime)
        put("sabahEndTime", s.sabahEndTime)
        put("aksamStartTime", s.aksamStartTime)
        put("aksamEndTime", s.aksamEndTime)
        put("gece8StartTime", s.gece8StartTime)
        put("gece8EndTime", s.gece8EndTime)
        put("shiftReminderEnabled", s.shiftReminderEnabled)
        put("shiftReminderTime", s.shiftReminderTime)
        put("preShiftAlertEnabled", s.preShiftAlertEnabled)
        put("preShiftAlertMinutes", s.preShiftAlertMinutes)
        put("nightDndEnabled", s.nightDndEnabled)
        put("calendarSyncEnabled", s.calendarSyncEnabled)
        put("autoHolidaysEnabled", s.autoHolidaysEnabled)
        put("showFriendOnCalendar", s.showFriendOnCalendar)
        put("colorLayerStyle", s.colorLayerStyle)
        put("commonLeaveAlertEnabled", s.commonLeaveAlertEnabled)
        put("hourlyWage", s.hourlyWage)
        put("initialCycleOffset", s.initialCycleOffset)
        put("isAnchorConfigured", s.isAnchorConfigured)
        put("isOnboardingCompleted", s.isOnboardingCompleted)
        put("appLanguage", s.appLanguage)
        put("themeMode", s.themeMode)
        put("customSequence", JSONArray(s.customSequence.map { it.name }))
    }

    private fun deserializeSettings(sObj: JSONObject, cur: UserSettings): UserSettings {
        val preset = CyclePreset.entries.find { it.id == sObj.optString("cyclePreset") } ?: cur.cyclePreset
        val refDate = try { LocalDate.parse(sObj.optString("referenceStartDate")) } catch (_: Exception) { cur.referenceStartDate }
        val empDate = try { LocalDate.parse(sObj.optString("employmentStartDate")) } catch (_: Exception) { cur.employmentStartDate }
        val birthDate = if (sObj.has("birthDate") && !sObj.isNull("birthDate")) {
            try { LocalDate.parse(sObj.optString("birthDate")) } catch (_: Exception) { cur.birthDate }
        } else cur.birthDate

        val seqArr = sObj.optJSONArray("customSequence")
        val customSeq = if (seqArr != null) {
            (0 until seqArr.length()).mapNotNull { idx ->
                ShiftType.entries.find { it.name == seqArr.optString(idx) }
            }.takeIf { it.isNotEmpty() } ?: cur.customSequence
        } else cur.customSequence

        return cur.copy(
            employeeName = sObj.optString("employeeName", cur.employeeName),
            department = sObj.optString("department", cur.department),
            professionGroup = sObj.optString("professionGroup", cur.professionGroup),
            title = sObj.optString("title", cur.title),
            avatarUriOrPath = sObj.optString("avatarUriOrPath", cur.avatarUriOrPath),
            cyclePreset = preset,
            customSequence = customSeq,
            initialCycleOffset = sObj.optInt("initialCycleOffset", cur.initialCycleOffset),
            referenceStartDate = refDate,
            employmentStartDate = empDate,
            birthDate = birthDate,
            usedLeaveDays = sObj.optInt("usedLeaveDays", cur.usedLeaveDays),
            gunduzStartTime = sObj.optString("gunduzStartTime", cur.gunduzStartTime),
            gunduzEndTime = sObj.optString("gunduzEndTime", cur.gunduzEndTime),
            geceStartTime = sObj.optString("geceStartTime", cur.geceStartTime),
            geceEndTime = sObj.optString("geceEndTime", cur.geceEndTime),
            sabahStartTime = sObj.optString("sabahStartTime", cur.sabahStartTime),
            sabahEndTime = sObj.optString("sabahEndTime", cur.sabahEndTime),
            aksamStartTime = sObj.optString("aksamStartTime", cur.aksamStartTime),
            aksamEndTime = sObj.optString("aksamEndTime", cur.aksamEndTime),
            gece8StartTime = sObj.optString("gece8StartTime", cur.gece8StartTime),
            gece8EndTime = sObj.optString("gece8EndTime", cur.gece8EndTime),
            shiftReminderEnabled = sObj.optBoolean("shiftReminderEnabled", cur.shiftReminderEnabled),
            shiftReminderTime = sObj.optString("shiftReminderTime", cur.shiftReminderTime),
            preShiftAlertEnabled = sObj.optBoolean("preShiftAlertEnabled", cur.preShiftAlertEnabled),
            preShiftAlertMinutes = sObj.optInt("preShiftAlertMinutes", cur.preShiftAlertMinutes),
            nightDndEnabled = sObj.optBoolean("nightDndEnabled", cur.nightDndEnabled),
            calendarSyncEnabled = sObj.optBoolean("calendarSyncEnabled", cur.calendarSyncEnabled),
            autoHolidaysEnabled = sObj.optBoolean("autoHolidaysEnabled", cur.autoHolidaysEnabled),
            showFriendOnCalendar = sObj.optBoolean("showFriendOnCalendar", cur.showFriendOnCalendar),
            colorLayerStyle = sObj.optInt("colorLayerStyle", cur.colorLayerStyle),
            commonLeaveAlertEnabled = sObj.optBoolean("commonLeaveAlertEnabled", cur.commonLeaveAlertEnabled),
            hourlyWage = sObj.optDouble("hourlyWage", cur.hourlyWage),
            isAnchorConfigured = sObj.optBoolean("isAnchorConfigured", true),
            isOnboardingCompleted = true,
            appLanguage = sObj.optString("appLanguage", cur.appLanguage),
            themeMode = sObj.optString(
                "themeMode",
                prefs.getString("app_theme_mode", cur.themeMode) ?: cur.themeMode
            )
        )
    }

    fun exportFullBackupJson(): String {
        val s = _userSettings.value
        val root = JSONObject()
        root.put("schemaVersion", PREFS_SCHEMA_VERSION)
        root.put("version", "5.0")
        root.put("exportedAt", LocalDateTime.now().toString())
        root.put("settings", serializeSettings(s))
        root.put("overrides", serializeOverrides(_dayOverrides.value))
        root.put("colleagues", serializeColleagues(_colleagues.value))
        return root.toString(2)
    }

    fun importFullBackupJson(rawJson: String): Boolean {
        return try {
            val root = JSONObject(rawJson)
            val sObj = root.optJSONObject("settings")
            val hasSettings = sObj != null || root.has("employeeName") || root.has("cyclePreset")
            val overridesObj = root.optJSONObject("overrides")
            val colleaguesArr = root.optJSONArray("colleagues")
            if (!hasSettings && overridesObj == null && colleaguesArr == null) {
                return false
            }

            if (sObj != null) {
                val restoredSettings = deserializeSettings(sObj, _userSettings.value)
                updateSettings(restoredSettings)
            } else if (root.has("employeeName") || root.has("cyclePreset")) {
                // Legacy flat-object backup compatibility
                val restoredSettings = deserializeSettings(root, _userSettings.value)
                updateSettings(restoredSettings)
            }

            // Restore overrides (if present in backup; supports v4.8 and v5.0+)
            if (overridesObj != null) {
                val restoredOverrides = readOverridesFromJsonString(overridesObj.toString())
                saveOverrides(restoredOverrides)
            }

            // Restore colleagues if present
            if (colleaguesArr != null) {
                parseColleaguesJson(colleaguesArr.toString())?.let { saveColleagues(it) }
            }
            true
        } catch (_: Exception) {
            false
        }
    }
}
