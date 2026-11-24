package com.example.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.example.data.model.ProfessionGroup
import java.util.Locale

@Immutable
data class DepartmentThemeTone(
    val id: String,
    val label: String,
    val seedColor: Color
)

@Immutable
data class DynamicThemePalette(
    val departmentTone: DepartmentThemeTone,
    val isTinted: Boolean,
    val isDark: Boolean = true,
    val background: Color,
    val surfaceDim: Color,
    val surfaceCard: Color,
    val surfaceElevated: Color,
    val surfacePanel: Color,
    val surfaceHighlight: Color,
    val primaryNeon: Color,
    val primaryDeep: Color,
    val accentCyan: Color,
    val borderSubtle: Color,
    val borderMetal: Color,
    val borderGlow: Color,
    val textPrimary: Color = TextPrimary,
    val textSecondary: Color = TextSecondary,
    val textMuted: Color = TextMuted,
    val textOnPrimary: Color = TextInverted,
    val activeChipBg: Color = Color(0xFF082F49),
    val dayShiftBg: Color = Color(0xFF072738),
    val dayShiftBgBottom: Color = SurfaceCard,
    val dayShiftBorder: Color = ShiftGunduz.copy(alpha = 0.65f),
    val dayShiftAccent: Color = AccentCyan,
    val dayShiftText: Color = TextPrimary,
    val eveningShiftBg: Color = Color(0xFF2B1D08),
    val eveningShiftBgBottom: Color = SurfaceCard,
    val eveningShiftBorder: Color = ShiftOvertimeAmber.copy(alpha = 0.65f),
    val eveningShiftAccent: Color = ShiftOvertimeAmber,
    val eveningShiftText: Color = Color(0xFFFEF3C7),
    val nightShiftBg: Color = Color(0xFF1E1B4B),
    val nightShiftBgBottom: Color = SurfaceCard,
    val nightShiftBorder: Color = ShiftGece.copy(alpha = 0.7f),
    val nightShiftAccent: Color = Color(0xFF818CF8),
    val nightShiftText: Color = TextPrimary,
    val reportShiftBg: Color = Color(0xFF3F1329),
    val reportShiftBgBottom: Color = SurfaceCard,
    val reportShiftBorder: Color = ShiftRapor.copy(alpha = 0.7f),
    val reportShiftAccent: Color = Color(0xFFF9A8D4),
    val reportShiftText: Color = TextPrimary,
    val offShiftBg: Color = Color(0xFF151C26),
    val offShiftBorder: Color = Color(0xFF334155),
    val offShiftAccent: Color = Color(0xFF94A3B8),
    val emeraldBg: Color = Color(0xFF062E26),
    val emeraldBorder: Color = Color(0xFF34D399).copy(alpha = 0.65f),
    val emeraldAccent: Color = Color(0xFF34D399),
    val emeraldText: Color = Color(0xFFD1FAE5),
    val dangerAccent: Color = Color(0xFFF87171)
) {
    val brushedSurfaceBrush: Brush
        get() = if (isDark) {
            Brush.verticalGradient(colors = listOf(surfaceCard, background))
        } else {
            Brush.verticalGradient(
                colors = listOf(surfaceCard, lerp(surfaceCard, surfaceDim, 0.42f))
            )
        }

    val elevatedSurfaceBrush: Brush
        get() = if (isDark) {
            Brush.verticalGradient(colors = listOf(surfaceElevated, surfaceCard))
        } else {
            Brush.verticalGradient(colors = listOf(surfaceCard, surfaceElevated))
        }

    val metallicSheenBrush: Brush
        get() = if (isDark) {
            Brush.linearGradient(
                colors = listOf(
                    surfaceHighlight.copy(alpha = 0.55f),
                    surfaceCard,
                    background
                )
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    surfaceCard,
                    surfaceElevated.copy(alpha = 0.65f),
                    surfaceCard
                )
            )
        }

    val metallicBorderBrush: Brush
        get() = if (isDark) {
            Brush.linearGradient(colors = listOf(BorderSpecularTop, borderMetal, borderSubtle))
        } else {
            Brush.linearGradient(colors = listOf(LightBorderSpecularTop, borderMetal.copy(alpha = 0.55f), borderSubtle))
        }

    val actionGradientBrush: Brush
        get() = Brush.horizontalGradient(colors = listOf(primaryDeep, primaryNeon))
}

object DepartmentColorResolver {
    private val trLocale = Locale.forLanguageTag("tr-TR")

    private val toneClinicalCyan = DepartmentThemeTone(
        id = "clinical_cyan",
        label = "Klinik Turkuaz",
        seedColor = Color(0xFF06B6D4)
    )
    private val toneTacticalCobalt = DepartmentThemeTone(
        id = "tactical_cobalt",
        label = "Taktik Kobalt",
        seedColor = Color(0xFF3B82F6)
    )
    private val toneRescueAmber = DepartmentThemeTone(
        id = "rescue_amber",
        label = "Kurtarma Kehribarı",
        seedColor = Color(0xFFF59E0B)
    )
    private val toneIndustrialEmerald = DepartmentThemeTone(
        id = "industrial_emerald",
        label = "Endüstriyel Zümrüt",
        seedColor = Color(0xFF10B981)
    )
    private val toneLogisticsIndigo = DepartmentThemeTone(
        id = "logistics_indigo",
        label = "Operasyon İndigo",
        seedColor = Color(0xFF818CF8)
    )
    private val toneHospitalityRose = DepartmentThemeTone(
        id = "hospitality_rose",
        label = "Hizmet Gül Kurusu",
        seedColor = Color(0xFFEC4899)
    )

    fun resolveDepartmentTone(
        department: String,
        professionGroupId: String = ProfessionGroup.SAGLIK.id
    ): DepartmentThemeTone {
        val normalized = department.trim().lowercase(trLocale)
        if (normalized.isNotEmpty()) {
            when {
                normalized.containsAny(
                    "acil", "yoğun", "yogun", "hastane", "klinik", "112", "kkm",
                    "ameliyat", "hemşire", "hemsire", "doktor", "tıp", "tip", "sağlık", "saglik",
                    "poliklinik", "triyaj", "ebe", "kardiyoloji"
                ) -> return toneClinicalCyan

                normalized.containsAny(
                    "güvenlik", "guvenlik", "emniyet", "polis", "zabıta", "zabita",
                    "devriye", "kampüs", "kampus", "merkez nokta", "bekçi", "bekci", "nizamiye"
                ) -> return toneTacticalCobalt

                normalized.containsAny(
                    "itfaiye", "afad", "yangın", "yangin", "kurtarma", "afet", "arama", "istasyon"
                ) -> return toneRescueAmber

                normalized.containsAny(
                    "üretim", "uretim", "fabrika", "sanayi", "atölye", "atolye",
                    "bakım", "bakim", "kalite", "cnc", "tesis", "enerji", "teknik"
                ) -> return toneIndustrialEmerald

                normalized.containsAny(
                    "havaliman", "apron", "lojistik", "depo", "sevkiyat", "ulaşım", "ulasim",
                    "operasyon", "kargo", "liman", "hat kontrol"
                ) -> return toneLogisticsIndigo

                normalized.containsAny(
                    "otel", "çağrı", "cagri", "resepsiyon", "ön büro", "on buro",
                    "hizmet", "destek", "veri merkezi", "müşteri", "musteri"
                ) -> return toneHospitalityRose
            }
        }

        return when (ProfessionGroup.fromId(professionGroupId)) {
            ProfessionGroup.SAGLIK -> toneClinicalCyan
            ProfessionGroup.GUVENLIK -> toneTacticalCobalt
            ProfessionGroup.ITFAIYE_AFET -> toneRescueAmber
            ProfessionGroup.SANAYI_URETIM -> toneIndustrialEmerald
            ProfessionGroup.LOJISTIK_ULASIM -> toneLogisticsIndigo
            ProfessionGroup.OTEL_HIZMET -> toneHospitalityRose
        }
    }

    private fun String.containsAny(vararg tokens: String): Boolean =
        tokens.any { this.contains(it) }
}

fun buildDynamicThemePalette(
    department: String,
    professionGroupId: String = ProfessionGroup.SAGLIK.id,
    enabled: Boolean = true,
    isDark: Boolean = true
): DynamicThemePalette {
    val tone = DepartmentColorResolver.resolveDepartmentTone(department, professionGroupId)
    val seed = tone.seedColor

    if (!isDark) {
        val lightActiveChipBg = Color(0xFFE0F2FE)
        val lightDayBg = Color(0xFFE0F2FE)
        val lightDayBgBottom = Color(0xFFF0F9FF)
        val lightDayBorder = Color(0xFF7DD3FC)
        val lightDayAccent = Color(0xFF0369A1)
        val lightDayText = Color(0xFF0C4A6E)
        val lightEveningBg = Color(0xFFFEF3C7)
        val lightEveningBgBottom = Color(0xFFFFFBEB)
        val lightEveningBorder = Color(0xFFFCD34D)
        val lightEveningAccent = Color(0xFFB45309)
        val lightEveningText = Color(0xFF78350F)
        val lightNightBg = Color(0xFFE0E7FF)
        val lightNightBgBottom = Color(0xFFEEF2FF)
        val lightNightBorder = Color(0xFFA5B4FC)
        val lightNightAccent = Color(0xFF4338CA)
        val lightNightText = Color(0xFF312E81)
        val lightReportBg = Color(0xFFFCE7F3)
        val lightReportBgBottom = Color(0xFFFDF2F8)
        val lightReportBorder = Color(0xFFF9A8D4)
        val lightReportAccent = Color(0xFFBE185D)
        val lightReportText = Color(0xFF831843)
        val lightOffBg = Color(0xFFF8FAFC)
        val lightOffBorder = Color(0xFFCBD5E1)
        val lightOffAccent = Color(0xFF475569)
        val lightEmeraldBg = Color(0xFFDCFCE7)
        val lightEmeraldBorder = Color(0xFF6EE7B7)
        val lightEmeraldAccent = Color(0xFF047857)
        val lightEmeraldText = Color(0xFF064E3B)
        val lightDangerAccent = Color(0xFFDC2626)

        if (!enabled) {
            return DynamicThemePalette(
                departmentTone = tone,
                isTinted = false,
                isDark = false,
                background = LightBackgroundMetallic,
                surfaceDim = LightSurfaceDim,
                surfaceCard = LightSurfaceCard,
                surfaceElevated = LightSurfaceElevated,
                surfacePanel = LightSurfacePanel,
                surfaceHighlight = LightSurfaceHighlight,
                primaryNeon = LightPrimaryNeon,
                primaryDeep = LightPrimaryDeep,
                accentCyan = LightAccentCyan,
                borderSubtle = LightBorderSubtle,
                borderMetal = LightBorderMetal,
                borderGlow = LightPrimaryNeon.copy(alpha = 0.35f),
                textPrimary = LightTextPrimary,
                textSecondary = LightTextSecondary,
                textMuted = LightTextMuted,
                textOnPrimary = Color.White,
                activeChipBg = lightActiveChipBg,
                dayShiftBg = lightDayBg,
                dayShiftBgBottom = lightDayBgBottom,
                dayShiftBorder = lightDayBorder,
                dayShiftAccent = lightDayAccent,
                dayShiftText = lightDayText,
                eveningShiftBg = lightEveningBg,
                eveningShiftBgBottom = lightEveningBgBottom,
                eveningShiftBorder = lightEveningBorder,
                eveningShiftAccent = lightEveningAccent,
                eveningShiftText = lightEveningText,
                nightShiftBg = lightNightBg,
                nightShiftBgBottom = lightNightBgBottom,
                nightShiftBorder = lightNightBorder,
                nightShiftAccent = lightNightAccent,
                nightShiftText = lightNightText,
                reportShiftBg = lightReportBg,
                reportShiftBgBottom = lightReportBgBottom,
                reportShiftBorder = lightReportBorder,
                reportShiftAccent = lightReportAccent,
                reportShiftText = lightReportText,
                offShiftBg = lightOffBg,
                offShiftBorder = lightOffBorder,
                offShiftAccent = lightOffAccent,
                emeraldBg = lightEmeraldBg,
                emeraldBorder = lightEmeraldBorder,
                emeraldAccent = lightEmeraldAccent,
                emeraldText = lightEmeraldText,
                dangerAccent = lightDangerAccent
            )
        }

        val tintedPrimary = lerp(LightPrimaryNeon, seed, 0.30f)
        val tintedDeep = lerp(LightPrimaryDeep, seed, 0.28f)
        val tintedAccent = lerp(LightAccentCyan, seed, 0.26f)
        return DynamicThemePalette(
            departmentTone = tone,
            isTinted = true,
            isDark = false,
            background = lerp(LightBackgroundMetallic, seed, 0.03f),
            surfaceDim = lerp(LightSurfaceDim, seed, 0.04f),
            surfaceCard = LightSurfaceCard,
            surfaceElevated = lerp(LightSurfaceElevated, seed, 0.05f),
            surfacePanel = lerp(LightSurfacePanel, seed, 0.065f),
            surfaceHighlight = lerp(LightSurfaceHighlight, seed, 0.085f),
            primaryNeon = tintedPrimary,
            primaryDeep = tintedDeep,
            accentCyan = tintedAccent,
            borderSubtle = lerp(LightBorderSubtle, seed, 0.14f),
            borderMetal = lerp(LightBorderMetal, seed, 0.20f),
            borderGlow = tintedAccent.copy(alpha = 0.36f),
            textPrimary = LightTextPrimary,
            textSecondary = LightTextSecondary,
            textMuted = LightTextMuted,
            textOnPrimary = Color.White,
            activeChipBg = lerp(lightActiveChipBg, seed, 0.08f),
            dayShiftBg = lightDayBg,
            dayShiftBgBottom = lightDayBgBottom,
            dayShiftBorder = lightDayBorder,
            dayShiftAccent = lightDayAccent,
            dayShiftText = lightDayText,
            eveningShiftBg = lightEveningBg,
            eveningShiftBgBottom = lightEveningBgBottom,
            eveningShiftBorder = lightEveningBorder,
            eveningShiftAccent = lightEveningAccent,
            eveningShiftText = lightEveningText,
            nightShiftBg = lightNightBg,
            nightShiftBgBottom = lightNightBgBottom,
            nightShiftBorder = lightNightBorder,
            nightShiftAccent = lightNightAccent,
            nightShiftText = lightNightText,
            reportShiftBg = lightReportBg,
            reportShiftBgBottom = lightReportBgBottom,
            reportShiftBorder = lightReportBorder,
            reportShiftAccent = lightReportAccent,
            reportShiftText = lightReportText,
            offShiftBg = lightOffBg,
            offShiftBorder = lightOffBorder,
            offShiftAccent = lightOffAccent,
            emeraldBg = lightEmeraldBg,
            emeraldBorder = lightEmeraldBorder,
            emeraldAccent = lightEmeraldAccent,
            emeraldText = lightEmeraldText,
            dangerAccent = lightDangerAccent
        )
    }

    if (!enabled) {
        return DynamicThemePalette(
            departmentTone = tone,
            isTinted = false,
            isDark = true,
            background = BackgroundTitanium,
            surfaceDim = SurfaceDim,
            surfaceCard = SurfaceCard,
            surfaceElevated = SurfaceElevated,
            surfacePanel = SurfacePanel,
            surfaceHighlight = SurfaceHighlight,
            primaryNeon = PrimaryNeon,
            primaryDeep = PrimaryDeep,
            accentCyan = AccentCyan,
            borderSubtle = BorderSubtle,
            borderMetal = BorderMetal,
            borderGlow = BorderGlow,
            textPrimary = TextPrimary,
            textSecondary = TextSecondary,
            textMuted = TextMuted,
            textOnPrimary = TextInverted
        )
    }

    val tintedNeon = lerp(PrimaryNeon, seed, 0.48f)
    val tintedDeep = lerp(PrimaryDeep, seed, 0.44f)
    val tintedCyan = lerp(AccentCyan, seed, 0.54f)
    val tintedBorderBase = lerp(Color(0xFF93C5FD), seed, 0.38f)
    val tintedCard = lerp(SurfaceCard, seed, 0.075f)

    return DynamicThemePalette(
        departmentTone = tone,
        isTinted = true,
        isDark = true,
        background = lerp(BackgroundTitanium, seed, 0.055f),
        surfaceDim = lerp(SurfaceDim, seed, 0.065f),
        surfaceCard = tintedCard,
        surfaceElevated = lerp(SurfaceElevated, seed, 0.095f),
        surfacePanel = lerp(SurfacePanel, seed, 0.115f),
        surfaceHighlight = lerp(SurfaceHighlight, seed, 0.135f),
        primaryNeon = tintedNeon,
        primaryDeep = tintedDeep,
        accentCyan = tintedCyan,
        borderSubtle = tintedBorderBase.copy(alpha = 0.18f),
        borderMetal = tintedBorderBase.copy(alpha = 0.30f),
        borderGlow = tintedCyan.copy(alpha = 0.46f),
        textPrimary = TextPrimary,
        textSecondary = TextSecondary,
        textMuted = TextMuted,
        textOnPrimary = TextInverted,
        dayShiftBgBottom = tintedCard,
        eveningShiftBgBottom = tintedCard,
        nightShiftBgBottom = tintedCard,
        reportShiftBgBottom = tintedCard
    )
}

val LocalDynamicTheme = staticCompositionLocalOf {
    buildDynamicThemePalette(
        department = "",
        professionGroupId = ProfessionGroup.SAGLIK.id,
        enabled = false,
        isDark = true
    )
}

object DynamicTheme {
    val colors: DynamicThemePalette
        @Composable
        @ReadOnlyComposable
        get() = LocalDynamicTheme.current
}

@Composable
fun DynamicTheme(
    department: String,
    professionGroupId: String = ProfessionGroup.SAGLIK.id,
    enabled: Boolean = true,
    isDarkTheme: Boolean = true,
    colorSchemeOverride: ColorScheme? = null,
    content: @Composable () -> Unit
) {
    val target = buildDynamicThemePalette(
        department = department,
        professionGroupId = professionGroupId,
        enabled = enabled,
        isDark = isDarkTheme
    )

    val animSpec = tween<Color>(durationMillis = 350)
    val background by animateColorAsState(target.background, animSpec, label = "dynBg")
    val surfaceDim by animateColorAsState(target.surfaceDim, animSpec, label = "dynDim")
    val surfaceCard by animateColorAsState(target.surfaceCard, animSpec, label = "dynCard")
    val surfaceElevated by animateColorAsState(target.surfaceElevated, animSpec, label = "dynElevated")
    val surfacePanel by animateColorAsState(target.surfacePanel, animSpec, label = "dynPanel")
    val surfaceHighlight by animateColorAsState(target.surfaceHighlight, animSpec, label = "dynHighlight")
    val primaryNeon by animateColorAsState(target.primaryNeon, animSpec, label = "dynNeon")
    val primaryDeep by animateColorAsState(target.primaryDeep, animSpec, label = "dynDeep")
    val accentCyan by animateColorAsState(target.accentCyan, animSpec, label = "dynCyan")
    val borderSubtle by animateColorAsState(target.borderSubtle, animSpec, label = "dynBorderSubtle")
    val borderMetal by animateColorAsState(target.borderMetal, animSpec, label = "dynBorderMetal")
    val borderGlow by animateColorAsState(target.borderGlow, animSpec, label = "dynBorderGlow")
    val textPrimary by animateColorAsState(target.textPrimary, animSpec, label = "dynTextPrimary")
    val textSecondary by animateColorAsState(target.textSecondary, animSpec, label = "dynTextSecondary")
    val textMuted by animateColorAsState(target.textMuted, animSpec, label = "dynTextMuted")
    val textOnPrimary by animateColorAsState(target.textOnPrimary, animSpec, label = "dynTextOnPrimary")

    val animatedPalette = target.copy(
        isDark = isDarkTheme,
        background = background,
        surfaceDim = surfaceDim,
        surfaceCard = surfaceCard,
        surfaceElevated = surfaceElevated,
        surfacePanel = surfacePanel,
        surfaceHighlight = surfaceHighlight,
        primaryNeon = primaryNeon,
        primaryDeep = primaryDeep,
        accentCyan = accentCyan,
        borderSubtle = borderSubtle,
        borderMetal = borderMetal,
        borderGlow = borderGlow,
        textPrimary = textPrimary,
        textSecondary = textSecondary,
        textMuted = textMuted,
        textOnPrimary = textOnPrimary
    )

    val dynamicColorScheme = colorSchemeOverride ?: buildBrushedMetallicColorScheme(animatedPalette)

    CompositionLocalProvider(LocalDynamicTheme provides animatedPalette) {
        MaterialTheme(
            colorScheme = dynamicColorScheme,
            typography = Typography,
            shapes = ProshiftShapes,
            content = content
        )
    }
}
