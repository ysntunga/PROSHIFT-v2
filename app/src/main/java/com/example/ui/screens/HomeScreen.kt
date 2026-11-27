package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.ShiftDay
import com.example.ui.components.ProShiftBrandHeader
import com.example.ui.components.ShiftDashboardOverview
import com.example.ui.components.UserAvatarImage
import com.example.ui.i18n.trEn
import com.example.ui.theme.DynamicTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.ShiftViewModel
import java.time.LocalDateTime

@Composable
fun HomeScreen(
    viewModel: ShiftViewModel,
    modifier: Modifier = Modifier
) {
    val telemetry by viewModel.dutyTelemetry.collectAsState()
    val settings by viewModel.userSettings.collectAsState()
    val weeklyDays by viewModel.weeklyStripDays.collectAsState()
    val timesheetStats by viewModel.timesheetStats.collectAsState()
    val dyn = DynamicTheme.colors

    val currentNow = remember(telemetry.remainingSeconds) { LocalDateTime.now() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(dyn.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
            .imePadding()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(bottom = 96.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProShiftBrandHeader(
                sectionLabel = trEn("ANA SAYFA", "HOME")
            )

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable { viewModel.navigateTo(AppScreen.PROFILE) }
                    .testTag("home_profile_avatar_button"),
                contentAlignment = Alignment.Center
            ) {
                UserAvatarImage(
                    avatarPath = settings.avatarUriOrPath,
                    contentDescription = trEn("Kullanıcı Profili", "User Profile"),
                    displayName = settings.employeeName,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, dyn.borderMetal, CircleShape)
                )
            }
        }

        // Dashboard Overview Component (Live countdown dial, weekly flow strip, metrics duo cards)
        ShiftDashboardOverview(
            telemetry = telemetry,
            weeklyDays = weeklyDays,
            timesheetStats = timesheetStats,
            onBreakOvertimeClick = { viewModel.openBreakOvertimeDialog() },
            onShiftNoteClick = { viewModel.openShiftNoteDialog() },
            onNextShiftClick = { viewModel.navigateTo(AppScreen.CALENDAR) },
            onMonthlyHoursClick = { viewModel.navigateTo(AppScreen.ADD_OVERTIME) },
            onTimesheetClick = { viewModel.navigateTo(AppScreen.TIMESHEET) },
            onInspectDay = { day: ShiftDay -> viewModel.inspectDay(day) },
            now = currentNow
        )
    }
}
