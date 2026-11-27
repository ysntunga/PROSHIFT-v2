package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CyclePreset
import com.example.ui.components.ProShiftLogo
import com.example.ui.components.ProShiftLogoVariant
import com.example.ui.i18n.trEn
import com.example.ui.theme.DynamicTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.ShiftViewModel

@Composable
fun WelcomeScreen(
    viewModel: ShiftViewModel,
    modifier: Modifier = Modifier
) {
    val dyn = DynamicTheme.colors
    val settings by viewModel.userSettings.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(dyn.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
            .imePadding()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(bottom = 28.dp)
    ) {
        // Minimal Top Bar: version label + Skip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "v${com.example.BuildConfig.VERSION_NAME}",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = dyn.textMuted,
                fontFamily = FontFamily.Monospace
            )

            Surface(
                onClick = { viewModel.completeOnboarding(AppScreen.HOME) },
                modifier = Modifier
                    .heightIn(min = 40.dp)
                    .testTag("btn_welcome_skip"),
                shape = RoundedCornerShape(18.dp),
                color = dyn.surfaceElevated,
                border = BorderStroke(1.dp, dyn.borderSubtle)
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = trEn("Atla", "Skip"),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = dyn.textSecondary
                    )
                }
            }
        }

        // Open, transparent hero brand mark directly on canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 18.dp, bottom = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            ProShiftLogo(
                variant = ProShiftLogoVariant.STACKED,
                withRing = true,
                height = 148.dp,
                contentDescription = trEn("PROSHIFT Karşılama Logosu", "PROSHIFT Welcome Logo")
            )
        }

        // Value Proposition
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = trEn("VARDİYA & NÖBET TAKİBİ", "SHIFT & DUTY TRACKING"),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = dyn.accentCyan,
                letterSpacing = 0.8.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = trEn("Vardiyalarını Kolayca Yönet,\nNöbetini Asla Kaçırma", "Manage Your Shifts Easily,\nNever Miss a Duty"),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.02).sp,
                color = dyn.textPrimary,
                lineHeight = 30.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = trEn(
                    "Sağlık, güvenlik, havacılık ve üretim ekipleri için vardiya döngüleri, otomatik fazla mesai puantajı ve nöbet hatırlatıcıları tek yerde.",
                    "Shift cycles, automatic overtime timesheets, and duty reminders in one place for healthcare, security, aviation, and operations teams."
                ),
                fontSize = 13.sp,
                color = dyn.textSecondary,
                lineHeight = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Popular Shift Templates — Interactive Quick Selection
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = trEn("HIZLI VARDİYA DÜZENİ", "QUICK SHIFT PATTERN"),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = dyn.textMuted,
                    letterSpacing = 0.6.sp
                )
                Text(
                    text = trEn("Tümü & Özel →", "All & Custom →"),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = dyn.accentCyan,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { viewModel.navigateTo(AppScreen.WIZARD) }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val quickPresets = listOf(
                Triple(CyclePreset.CYCLE_12_36, trEn("12/36 Sağlık", "12/36 Healthcare"), trEn("G • N • İ (12s)", "D • N • O (12h)")),
                Triple(CyclePreset.CYCLE_8_2_2_2, trEn("2-2-2 Üçlü (8s)", "2-2-2 Triple (8h)"), trEn("S-S-A-A-G-G", "M-M-E-E-N-N")),
                Triple(CyclePreset.CYCLE_2_2_2, trEn("2G-2G-2İ (12s)", "2D-2N-2O (12h)"), trEn("G-G-N-N-İ-İ", "D-D-N-N-O-O")),
                Triple(CyclePreset.CYCLE_8_2_2_2_2, trEn("2-2-2-2 (8s)", "2-2-2-2 (8h)"), trEn("2S-2A-2G-2İ", "2M-2E-2N-2O")),
                Triple(CyclePreset.CYCLE_4_4, trEn("4-4 Sistemi", "4-4 System"), trEn("4G • 4İ (12s)", "4D • 4O (12h)")),
                Triple(CyclePreset.CYCLE_PITMAN, "Pitman 2-2-3", trEn("14g Döngü", "14d Cycle")),
                Triple(CyclePreset.CYCLE_24_48, trEn("24/48 Nöbet", "24/48 Duty"), trEn("24s • 48s", "24h • 48h")),
                Triple(CyclePreset.CYCLE_8_2_2_2_4, trEn("2-2-2-4 (10g)", "2-2-2-4 (10d)"), trEn("2S-2A-2G-4İ", "2M-2E-2N-4O")),
                Triple(CyclePreset.CYCLE_SOUTHERN_SWING, "Southern Swing", trEn("28g (8s)", "28d (8h)")),
                Triple(CyclePreset.CUSTOM, trEn("Özel / DuPont", "Custom / DuPont"), trEn("Kendi Dizini Kur", "Build Your Own"))
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickPresets) { (preset, shortTitle, codeHint) ->
                    val isSelected = settings.cyclePreset == preset
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.updateCyclePreset(preset)
                                if (preset == CyclePreset.CUSTOM || !preset.hasDefaultPattern) {
                                    viewModel.navigateTo(AppScreen.WIZARD)
                                }
                            }
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) dyn.accentCyan else dyn.borderSubtle,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .testTag("welcome_quick_${preset.id}"),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) dyn.activeChipBg else dyn.surfaceCard
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = when {
                                    isSelected -> Icons.Filled.Check
                                    preset == CyclePreset.CUSTOM -> Icons.Filled.Tune
                                    else -> Icons.Filled.Add
                                },
                                contentDescription = null,
                                tint = dyn.accentCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Column {
                                Text(
                                    text = shortTitle,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) dyn.accentCyan else dyn.textPrimary,
                                    maxLines = 1
                                )
                                Text(
                                    text = codeHint,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = dyn.textSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        // Action Button 1: Hemen Başla
        Surface(
            onClick = { viewModel.navigateTo(AppScreen.WIZARD) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(52.dp)
                .testTag("btn_get_started"),
            shape = RoundedCornerShape(14.dp),
            color = dyn.primaryDeep
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = trEn("Hemen Başla", "Get Started"),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = trEn("Başla", "Start"),
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action Button 2: Takvimi İncele
        Surface(
            onClick = { viewModel.completeOnboarding(AppScreen.CALENDAR) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
            color = dyn.surfaceElevated,
            border = BorderStroke(1.dp, dyn.borderSubtle)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = trEn("Takvim", "Calendar"),
                    tint = dyn.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = trEn("Takvimi Doğrudan Aç", "Open Calendar Directly"),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = dyn.textPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Privacy Footer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = trEn("Kilit", "Lock"),
                tint = dyn.textMuted,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = trEn(
                    "Verileriniz yalnızca cihazınızda saklanır • Çevrimdışı çalışır",
                    "Data stays private on your device • Works offline"
                ),
                fontSize = 11.sp,
                color = dyn.textMuted
            )
        }
    }
}
