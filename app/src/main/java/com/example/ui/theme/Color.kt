package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================================
// PROSHIFT — Brushed Metallic Blue Design System Tokens
// ============================================================================

// Dark Mode — Brushed Titanium Navy Surfaces
val SurfaceContainerLowestDark = Color(0xFF050B13)
val BackgroundTitanium = Color(0xFF070E18)
val SurfaceDim = Color(0xFF091423)
val SurfaceCard = Color(0xFF0C1625)
val SurfaceElevated = Color(0xFF111F33)
val SurfacePanel = Color(0xFF172942)
val SurfaceHighlight = Color(0xFF1F3656)

// Light Mode — Brushed Silver Steel Blue Surfaces
val LightBackgroundMetallic = Color(0xFFEEF4FA)
val LightSurfaceDim = Color(0xFFE2ECF6)
val LightSurfaceCard = Color(0xFFFFFFFF)
val LightSurfaceSheenBottom = Color(0xFFF3F8FE)
val LightSurfaceElevated = Color(0xFFEAF2FA)
val LightSurfacePanel = Color(0xFFDCE8F6)
val LightSurfaceHighlight = Color(0xFFC8DDF5)

// Dark Mode — Neon & Anodized Cobalt Blue Accents
val PrimaryNeon = Color(0xFF38BDF8)
val PrimaryMetallic = Color(0xFF60A5FA)
val PrimaryDeep = Color(0xFF2563EB)
val CobaltBlue = Color(0xFF2563EB)
val AccentCyan = Color(0xFF22D3EE)
val AccentCyanGlow = Color(0x3838BDF8)
val TertiaryContainerDark = Color(0xFF1E3A8A)
val OnTertiaryContainerDark = Color(0xFFDBEAFE)

// Light Mode — Brushed Sapphire & Machined Steel Blue Accents
val LightPrimaryNeon = Color(0xFF0284C7)
val LightPrimaryDeep = Color(0xFF1D4ED8)
val LightAccentCyan = Color(0xFF0369A1)
val TertiaryContainerLight = Color(0xFFDBEAFE)
val OnTertiaryContainerLight = Color(0xFF1E3A8A)

// Shift Status Colors
val ShiftGunduz = Color(0xFF06B6D4)
val ShiftGunduzBg = Color(0x2906B6D4)
val ShiftGunduzBorder = Color(0x6606B6D4)

val ShiftGece = Color(0xFF6366F1)
val ShiftGeceBg = Color(0x296366F1)
val ShiftGeceBorder = Color(0x666366F1)

val ShiftIzin = Color(0xFF334155)
val ShiftIzinBg = Color(0x40334155)
val ShiftIzinBorder = Color(0x3394A3B8)

val ShiftOvertimeAmber = Color(0xFFF59E0B)
val ShiftOvertimeBg = Color(0x29F59E0B)
val ShiftOvertimeBorder = Color(0x66F59E0B)

val ShiftRapor = Color(0xFFEC4899)
val ShiftRaporBg = Color(0x29EC4899)

// Typography & Text — Dark Mode
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)
val TextInverted = Color(0xFF030712)

// Typography & Text — Light Mode
val LightTextPrimary = Color(0xFF0B192C)
val LightTextSecondary = Color(0xFF3E546E)
val LightTextMuted = Color(0xFF607691)

// Semantic Error & Status Tokens
val ErrorDark = Color(0xFFF87171)
val OnErrorDark = Color(0xFF450A0A)
val ErrorContainerDark = Color(0xFF7F1D1D)
val OnErrorContainerDark = Color(0xFFFEE2E2)

val ErrorLight = Color(0xFFDC2626)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFEE2E2)
val OnErrorContainerLight = Color(0xFF7F1D1D)

// Metallic Specular Bevels & Borders — Dark Mode
val BorderSubtle = Color(0x2693C5FD)
val BorderMetal = Color(0x4793C5FD)
val BorderSpecularTop = Color(0x59FFFFFF)
val BorderGlow = Color(0x7338BDF8)

// Metallic Specular Bevels & Borders — Light Mode
val LightBorderSubtle = Color(0xFFC5D5E8)
val LightBorderMetal = Color(0xFF8EA7C4)
val LightBorderSpecularTop = Color(0xCCFFFFFF)

val EmeraldGlow = Color(0xFF10B981)
val WhatsAppGreen = Color(0xFF25D366)

// Reusable Brushed Metallic Gradients — Dark Mode
val TitaniumBrushedBrush = Brush.verticalGradient(
    colors = listOf(SurfaceCard, BackgroundTitanium)
)
val TitaniumElevatedBrush = Brush.verticalGradient(
    colors = listOf(SurfaceElevated, SurfaceCard)
)
val TitaniumActionGradient = Brush.horizontalGradient(
    colors = listOf(PrimaryDeep, PrimaryNeon)
)
val DarkMetallicBorderBrush = Brush.linearGradient(
    colors = listOf(BorderSpecularTop, BorderMetal, BorderSubtle)
)

// Reusable Brushed Metallic Gradients — Light Mode
val LightMetallicBrushedBrush = Brush.verticalGradient(
    colors = listOf(LightSurfaceCard, LightSurfaceSheenBottom)
)
val LightMetallicElevatedBrush = Brush.verticalGradient(
    colors = listOf(LightSurfaceCard, LightSurfaceElevated)
)
val LightMetallicActionGradient = Brush.horizontalGradient(
    colors = listOf(LightPrimaryDeep, LightPrimaryNeon)
)
val LightMetallicBorderBrush = Brush.linearGradient(
    colors = listOf(LightBorderSpecularTop, LightBorderMetal.copy(alpha = 0.55f), LightBorderSubtle)
)
