package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QueryBuilder
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CyclePreset
import com.example.ui.theme.BorderSpecularTop
import com.example.ui.theme.DynamicTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.ShiftViewModel

import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.R
import com.example.ui.components.ProShiftLogo
import com.example.ui.components.ProShiftLogoVariant
import com.example.ui.i18n.LocalIsEnglish
import com.example.ui.i18n.trEn

@Composable
fun WelcomeScreen(
    viewModel: ShiftViewModel,
    modifier: Modifier = Modifier
) {
    val dyn = DynamicTheme.colors
    val context = LocalContext.current
    val settings by viewModel.userSettings.collectAsState()
    val isEn = LocalIsEnglish.current

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
        // Top Bar (Logo, Version, Subtitle & "Atla" Button)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProShiftLogo(
                        variant = ProShiftLogoVariant.HORIZONTAL,
                        withRing = true,
                        height = 34.dp,
                        contentDescription = "PROSHIFT Logo"
                    )
                    Box(
                        modifier = Modifier
                            .background(dyn.accentCyan.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .border(1.dp, dyn.accentCyan.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "v${com.example.BuildConfig.VERSION_NAME}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = dyn.accentCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Text(
                    text = trEn("Türkiye Vardiya & Nöbet Asistanı", "Smart Shift & Duty Assistant"),
                    fontSize = 11.sp,
                    color = dyn.textSecondary
                )
            }

            // Atla (Skip) Button
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { viewModel.completeOnboarding(AppScreen.HOME) }
                    .border(1.dp, dyn.borderMetal, RoundedCornerShape(18.dp))
                    .testTag("btn_welcome_skip"),
                shape = RoundedCornerShape(18.dp),
                color = dyn.surfaceElevated
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
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

        Spacer(modifier = Modifier.height(6.dp))

        // Hero Brand Card with Brushed-Metallic PS PROSHIFT Artwork
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(22.dp),
            color = dyn.surfaceCard
        ) {
            Column(
                modifier = Modifier
                    .border(1.dp, dyn.borderMetal, RoundedCornerShape(22.dp))
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 22.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ProShiftLogo(
                        variant = ProShiftLogoVariant.STACKED,
                        withRing = true,
                        height = 148.dp,
                        contentDescription = trEn("PROSHIFT Karşılama Logosu", "PROSHIFT Welcome Logo")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom 2 Indicator Pills inside Hero Card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Pill 1: DÖNGÜ MOTORU
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, dyn.borderSubtle, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        color = dyn.surfaceElevated
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(dyn.activeChipBg, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Repeat,
                                    contentDescription = null,
                                    tint = dyn.accentCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = trEn("DÖNGÜ MOTORU", "CYCLE ENGINE"),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = dyn.textMuted,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = trEn("2G • 2G • 2İ Sonsuz", "2D • 2N • 2O Infinite"),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = dyn.textPrimary
                                )
                            }
                        }
                    }

                    // Pill 2: AKILLI MESAİ
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, dyn.borderSubtle, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        color = dyn.surfaceElevated
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(dyn.eveningShiftBg, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.QueryBuilder,
                                    contentDescription = null,
                                    tint = dyn.eveningShiftAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = trEn("AKILLI MESAİ", "SMART OVERTIME"),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = dyn.textMuted,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = trEn("Resmi Puantaj & Ek", "Official Timesheet"),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = dyn.textPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Value Proposition
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = null,
                    tint = dyn.accentCyan,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = trEn("HATASIZ NÖBET ALGORİTMASI", "PRECISION SHIFT ENGINE"),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = dyn.accentCyan,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = trEn("Vardiyalarını Kolayca Yönet,\nNöbetini Asla Kaçırma", "Manage Your Shifts Easily,\nNever Miss a Duty"),
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.02).sp,
                color = dyn.textPrimary,
                lineHeight = 28.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = trEn(
                    "Sağlık, emniyet, havacılık ve sanayi personeli için tasarlanmış akıllı döngü motoru. Resmi vardiya listeleri, otomatik fazla mesai hesaplayıcı ve nöbet değişim bildirimleri tek panelde.",
                    "Smart rotation engine built for healthcare, security, aviation, and industry teams. Official shift schedules, automatic overtime tracking, and duty reminders in one place."
                ),
                fontSize = 12.sp,
                color = dyn.textSecondary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Carousel Dots Indicator (1 / 3 Bilgilendirme)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(6.dp)
                        .background(dyn.accentCyan, RoundedCornerShape(3.dp))
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(dyn.borderMetal, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(dyn.borderMetal, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = trEn("1 / 3 Bilgilendirme", "1 / 3 Overview"),
                    fontSize = 10.sp,
                    color = dyn.textMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Popular Shift Templates — Interactive Hızlı Seçim
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = trEn("VARDİYA DÜZENİ HIZLI SEÇİMİ", "QUICK SHIFT PATTERN PICKER"),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = dyn.textMuted,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = trEn("Tümünü Gör & Özel →", "See All & Custom →"),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = dyn.accentCyan,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { viewModel.navigateTo(AppScreen.WIZARD) }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

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
                                color = if (isSelected) dyn.accentCyan else dyn.borderMetal,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .testTag("welcome_quick_${preset.id}"),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) dyn.activeChipBg else dyn.surfaceElevated
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
                                modifier = Modifier.size(15.dp)
                            )
                            Column {
                                Text(
                                    text = shortTitle,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) dyn.accentCyan else dyn.textPrimary,
                                    maxLines = 1
                                )
                                Text(
                                    text = codeHint,
                                    fontSize = 9.sp,
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

        Spacer(modifier = Modifier.height(22.dp))

        // Action Button 1: Hemen Başla (Primary Gradient Button)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable { viewModel.navigateTo(AppScreen.WIZARD) }
                .shadow(12.dp, RoundedCornerShape(14.dp), ambientColor = dyn.primaryNeon)
                .border(1.dp, BorderSpecularTop, RoundedCornerShape(14.dp))
                .testTag("btn_get_started"),
            shape = RoundedCornerShape(14.dp),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(dyn.primaryNeon, dyn.primaryDeep)
                        )
                    ),
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

        // Action Button 2: Mevcut Çizelgemi İçe Aktar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable {
                    Toast.makeText(
                        context,
                        trEn(isEn, "Resmi Çizelge İçe Aktarma Sihirbazı Açılıyor...", "Opening Schedule Import Wizard..."),
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.completeOnboarding(AppScreen.CALENDAR)
                }
                .border(1.dp, dyn.borderMetal, RoundedCornerShape(14.dp)),
            shape = RoundedCornerShape(14.dp),
            color = dyn.surfaceElevated
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Download,
                    contentDescription = trEn("İçe Aktar", "Import"),
                    tint = dyn.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = trEn("Mevcut Çizelgemi İçe Aktar", "Import Existing Schedule"),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = dyn.textPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Security / Offline Privacy Footer
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
                    "Verileriniz cihazınızda şifrelenir • Çevrimdışı (Offline) çalışır",
                    "Your data stays private on-device • Works 100% offline"
                ),
                fontSize = 10.sp,
                color = dyn.textMuted
            )
        }
    }
}
