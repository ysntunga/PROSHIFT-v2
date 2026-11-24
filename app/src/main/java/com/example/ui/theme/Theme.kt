package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.ProfessionGroup

/**
 * Full Material 3 Dark ColorScheme for the PROSHIFT "Brushed Metallic Blue" design system.
 * Uses deep anodized titanium navy surfaces paired with electric sky and cobalt accents.
 */
val BrushedMetallicBlueDarkColorScheme: ColorScheme = darkColorScheme(
    primary = PrimaryNeon,
    onPrimary = TextInverted,
    primaryContainer = SurfacePanel,
    onPrimaryContainer = PrimaryNeon,
    inversePrimary = LightPrimaryNeon,
    secondary = AccentCyan,
    onSecondary = TextInverted,
    secondaryContainer = SurfaceElevated,
    onSecondaryContainer = AccentCyan,
    tertiary = PrimaryDeep,
    onTertiary = Color.White,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    background = BackgroundTitanium,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    surfaceTint = PrimaryMetallic,
    surfaceDim = SurfaceDim,
    surfaceBright = SurfaceHighlight,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceCard,
    surfaceContainer = SurfaceElevated,
    surfaceContainerHigh = SurfacePanel,
    surfaceContainerHighest = SurfaceHighlight,
    inverseSurface = LightSurfaceCard,
    inverseOnSurface = LightTextPrimary,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    outline = BorderMetal,
    outlineVariant = BorderSubtle,
    scrim = Color(0x99000000)
)

/**
 * Full Material 3 Light ColorScheme for the PROSHIFT "Brushed Metallic Blue" design system.
 * Uses frosted silver-steel blue surfaces, crisp white machined cards, and sapphire/cobalt accents.
 */
val BrushedMetallicBlueLightColorScheme: ColorScheme = lightColorScheme(
    primary = LightPrimaryNeon,
    onPrimary = Color.White,
    primaryContainer = LightSurfacePanel,
    onPrimaryContainer = LightPrimaryDeep,
    inversePrimary = PrimaryNeon,
    secondary = LightAccentCyan,
    onSecondary = Color.White,
    secondaryContainer = LightSurfaceElevated,
    onSecondaryContainer = LightAccentCyan,
    tertiary = LightPrimaryDeep,
    onTertiary = Color.White,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    background = LightBackgroundMetallic,
    onBackground = LightTextPrimary,
    surface = LightSurfaceCard,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = LightTextSecondary,
    surfaceTint = LightPrimaryNeon,
    surfaceDim = LightSurfaceDim,
    surfaceBright = LightSurfaceCard,
    surfaceContainerLowest = LightSurfaceCard,
    surfaceContainerLow = LightSurfaceSheenBottom,
    surfaceContainer = LightSurfaceElevated,
    surfaceContainerHigh = LightSurfacePanel,
    surfaceContainerHighest = LightSurfaceHighlight,
    inverseSurface = SurfaceCard,
    inverseOnSurface = TextPrimary,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    outline = LightBorderMetal,
    outlineVariant = LightBorderSubtle,
    scrim = Color(0x660B192C)
)

/**
 * Builds a complete Material 3 [ColorScheme] derived from a [DynamicThemePalette],
 * preserving full surface container elevation roles and Brushed Metallic Blue contrast.
 */
fun buildBrushedMetallicColorScheme(palette: DynamicThemePalette): ColorScheme {
    return if (palette.isDark) {
        BrushedMetallicBlueDarkColorScheme.copy(
            primary = palette.primaryNeon,
            onPrimary = palette.textOnPrimary,
            primaryContainer = palette.surfacePanel,
            onPrimaryContainer = palette.primaryNeon,
            secondary = palette.accentCyan,
            onSecondary = palette.textOnPrimary,
            secondaryContainer = palette.surfaceElevated,
            onSecondaryContainer = palette.accentCyan,
            tertiary = palette.primaryDeep,
            onTertiary = Color.White,
            background = palette.background,
            onBackground = palette.textPrimary,
            surface = palette.surfaceCard,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.surfaceElevated,
            onSurfaceVariant = palette.textSecondary,
            surfaceTint = palette.primaryNeon,
            surfaceDim = palette.surfaceDim,
            surfaceBright = palette.surfaceHighlight,
            surfaceContainerLowest = palette.background,
            surfaceContainerLow = palette.surfaceCard,
            surfaceContainer = palette.surfaceElevated,
            surfaceContainerHigh = palette.surfacePanel,
            surfaceContainerHighest = palette.surfaceHighlight,
            error = palette.dangerAccent,
            outline = palette.borderMetal,
            outlineVariant = palette.borderSubtle
        )
    } else {
        BrushedMetallicBlueLightColorScheme.copy(
            primary = palette.primaryNeon,
            onPrimary = palette.textOnPrimary,
            primaryContainer = palette.surfacePanel,
            onPrimaryContainer = palette.primaryDeep,
            secondary = palette.accentCyan,
            onSecondary = Color.White,
            secondaryContainer = palette.surfaceElevated,
            onSecondaryContainer = palette.accentCyan,
            tertiary = palette.primaryDeep,
            onTertiary = Color.White,
            background = palette.background,
            onBackground = palette.textPrimary,
            surface = palette.surfaceCard,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.surfaceElevated,
            onSurfaceVariant = palette.textSecondary,
            surfaceTint = palette.primaryNeon,
            surfaceDim = palette.surfaceDim,
            surfaceBright = palette.surfaceCard,
            surfaceContainerLowest = palette.surfaceCard,
            surfaceContainerLow = palette.background,
            surfaceContainer = palette.surfaceElevated,
            surfaceContainerHigh = palette.surfacePanel,
            surfaceContainerHighest = palette.surfaceHighlight,
            error = palette.dangerAccent,
            outline = palette.borderMetal,
            outlineVariant = palette.borderSubtle
        )
    }
}

/**
 * Primary Material 3 Theme wrapper for PROSHIFT supporting Brushed Metallic Blue Light & Dark modes,
 * optional department-aware tinting, and optional Android 12+ system dynamic color.
 */
@Composable
fun ProshiftTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    department: String = "",
    professionGroupId: String = ProfessionGroup.SAGLIK.id,
    dynamicThemeEnabled: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemDynamicScheme: ColorScheme? = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        null
    }

    DynamicTheme(
        department = department,
        professionGroupId = professionGroupId,
        enabled = dynamicThemeEnabled,
        isDarkTheme = darkTheme,
        colorSchemeOverride = systemDynamicScheme,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    department: String = "",
    professionGroupId: String = ProfessionGroup.SAGLIK.id,
    dynamicThemeEnabled: Boolean = false,
    isDarkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    ProshiftTheme(
        darkTheme = isDarkTheme,
        dynamicColor = dynamicColor,
        department = department,
        professionGroupId = professionGroupId,
        dynamicThemeEnabled = dynamicThemeEnabled,
        content = content
    )
}
