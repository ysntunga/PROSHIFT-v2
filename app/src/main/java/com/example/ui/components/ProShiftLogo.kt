package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathNode
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DynamicTheme

/**
 * Official PROSHIFT corporate logo variants.
 * All variants render with a 100% transparent background (no box, square, circle, or <rect>)
 * directly over the host surface.
 */
enum class ProShiftLogoVariant(
    val key: String,
    val viewportWidth: Float,
    val viewportHeight: Float
) {
    /** Transparent PS monogram only (viewBox 0 0 256 256). */
    SYMBOL("symbol", 256f, 256f),

    /** Transparent Live-Shift dual-ring PS emblem (viewBox 0 0 256 256). */
    RING_SYMBOL("ring-symbol", 256f, 256f),

    /** Transparent horizontal PS emblem + font-independent vector PROSHIFT wordmark (viewBox 0 0 715 256). */
    HORIZONTAL("horizontal", 715f, 256f),

    /** Transparent vertical stacked emblem + vector PROSHIFT wordmark (viewBox 0 0 480 336). */
    STACKED("stacked", 480f, 336f);

    companion object {
        fun fromKey(value: String): ProShiftLogoVariant =
            entries.firstOrNull { it.key.equals(value.trim(), ignoreCase = true) }
                ?: HORIZONTAL
    }
}

/**
 * Official 3-part dynamic color tokens for the PROSHIFT brand logo.
 */
@Immutable
data class ProShiftLogoColors(
    /** Main P->S ribbon and "PRO" letters. */
    val coreRibbon: Color,
    /** Top-right S piece, Day Shift outer arc, and "SHIFT" letters. */
    val dayShift: Color,
    /** Bottom-left P piece and Night Shift outer arc. */
    val nightShift: Color
)

object ProShiftLogoPalette {
    val Dark = ProShiftLogoColors(
        coreRibbon = Color(0xFFFFFFFF),
        dayShift = Color(0xFF22D3EE),
        nightShift = Color(0xFF818CF8)
    )

    val Light = ProShiftLogoColors(
        coreRibbon = Color(0xFF0F172A),
        dayShift = Color(0xFF0891B2),
        nightShift = Color(0xFF4F46E5)
    )
}

/**
 * Resolves the exact official PROSHIFT logo colors for the active theme.
 */
fun proShiftLogoColors(isDark: Boolean): ProShiftLogoColors =
    if (isDark) ProShiftLogoPalette.Dark else ProShiftLogoPalette.Light

/**
 * Exact unmodified official PROSHIFT SVG path definitions and pre-parsed vector nodes.
 */
object ProShiftLogoPaths {
    // variant="symbol" (viewBox="0 0 256 256")
    const val SYMBOL_DAY_SHIFT =
        "M 132.66 34 H 236 A 40 40 0 0 1 196 74 H 159.71 A 72 72 0 0 0 132.66 34 Z"
    const val SYMBOL_NIGHT_SHIFT =
        "M 18 222 V 164 A 50 50 0 0 1 68 114 H 97.44 A 72 72 0 0 0 129.39 154 H 70 A 12 12 0 0 0 58 166 V 194 C 58 211 42 222 18 222 Z"
    const val SYMBOL_CORE_RIBBON =
        "M 18 34 H 90 A 58 58 0 0 1 148 92 A 18 18 0 0 0 166 110 H 182 A 56 56 0 0 1 182 222 H 116 A 40 40 0 0 1 156 182 H 182 A 16 16 0 0 0 182 150 H 166 A 58 58 0 0 1 108 92 A 18 18 0 0 0 90 74 H 58 A 40 40 0 0 1 18 34 Z"

    // variant="ring-symbol" (viewBox="0 0 256 256")
    const val RING_SYMBOL_DAY_SHIFT =
        "M 130.7 73.48 H 190.64 A 23.2 23.2 0 0 1 167.44 96.68 H 146.39 A 41.76 41.76 0 0 0 130.7 73.48 Z M 80.24 25.59 A 113 113 0 0 1 214.56 200.63 A 5 5 0 0 1 206.9 194.21 A 103 103 0 0 0 84.47 34.65 A 5 5 0 0 1 80.24 25.59 Z"
    const val RING_SYMBOL_NIGHT_SHIFT =
        "M 64.2 182.52 V 148.88 A 29 29 0 0 1 93.2 119.88 H 110.28 A 41.76 41.76 0 0 0 128.81 143.08 H 94.36 A 6.96 6.96 0 0 0 87.4 150.04 V 166.28 C 87.4 176.14 78.12 182.52 64.2 182.52 Z M 175.76 230.41 A 113 113 0 0 1 41.44 55.37 A 5 5 0 0 1 49.1 61.79 A 103 103 0 0 0 171.53 221.35 A 5 5 0 0 1 175.76 230.41 Z"
    const val RING_SYMBOL_CORE_RIBBON =
        "M 64.2 73.48 H 105.96 A 33.64 33.64 0 0 1 139.6 107.12 A 10.44 10.44 0 0 0 150.04 117.56 H 159.32 A 32.48 32.48 0 0 1 159.32 182.52 H 121.04 A 23.2 23.2 0 0 1 144.24 159.32 H 159.32 A 9.28 9.28 0 0 0 159.32 140.76 H 150.04 A 33.64 33.64 0 0 1 116.4 107.12 A 10.44 10.44 0 0 0 105.96 96.68 H 87.4 A 23.2 23.2 0 0 1 64.2 73.48 Z"

    // Font-independent PROSHIFT vector wordmark (fill-rule="evenodd", horizontal baseline)
    const val WORDMARK_PRO =
        "M 268 162 V 94 H 296 A 18 18 0 0 1 296 130 H 281 V 162 Z M 281 107 V 117 H 296 A 5 5 0 0 0 296 107 Z M 326 162 V 94 H 354 A 18 18 0 0 1 364 128 L 374 162 H 359 L 352 130 H 339 V 162 Z M 339 107 V 117 H 354 A 5 5 0 0 0 354 107 Z M 408 94 H 416 A 22 22 0 0 1 438 116 V 140 A 22 22 0 0 1 416 162 H 408 A 22 22 0 0 1 386 140 V 116 A 22 22 0 0 1 408 94 Z M 408 107 A 9 9 0 0 0 399 116 V 140 A 9 9 0 0 0 408 149 H 416 A 9 9 0 0 0 425 140 V 116 A 9 9 0 0 0 416 107 Z"
    const val WORDMARK_SHIFT =
        "M 472.25 94 H 498 A 13 13 0 0 1 485 107 H 472.25 A 7.25 7.25 0 0 0 472.25 121.5 H 477.75 A 20.25 20.25 0 0 1 477.75 162 H 452 A 13 13 0 0 1 465 149 H 477.75 A 7.25 7.25 0 0 0 477.75 134.5 H 472.25 A 20.25 20.25 0 0 1 472.25 94 Z M 510 162 V 94 H 523 V 121.5 H 545 V 94 H 558 V 162 H 545 V 134.5 H 523 V 162 Z M 570 162 V 94 H 583 V 162 Z M 595 162 V 94 H 637 A 13 13 0 0 1 624 107 H 608 V 121.5 H 631 V 134.5 H 608 V 162 Z M 647 94 H 695 V 107 H 677.5 V 162 H 664.5 V 107 H 647 Z"

    private fun parse(pathData: String): List<PathNode> =
        PathParser().parsePathString(pathData).toNodes()

    internal val symbolDayShiftNodes: List<PathNode> by lazy { parse(SYMBOL_DAY_SHIFT) }
    internal val symbolNightShiftNodes: List<PathNode> by lazy { parse(SYMBOL_NIGHT_SHIFT) }
    internal val symbolCoreRibbonNodes: List<PathNode> by lazy { parse(SYMBOL_CORE_RIBBON) }

    internal val ringSymbolDayShiftNodes: List<PathNode> by lazy { parse(RING_SYMBOL_DAY_SHIFT) }
    internal val ringSymbolNightShiftNodes: List<PathNode> by lazy { parse(RING_SYMBOL_NIGHT_SHIFT) }
    internal val ringSymbolCoreRibbonNodes: List<PathNode> by lazy { parse(RING_SYMBOL_CORE_RIBBON) }

    internal val wordmarkProNodes: List<PathNode> by lazy { parse(WORDMARK_PRO) }
    internal val wordmarkShiftNodes: List<PathNode> by lazy { parse(WORDMARK_SHIFT) }
}

/**
 * Builds an exact, 100% transparent [ImageVector] for the requested PROSHIFT logo variant
 * and active Light/Dark theme palette (or explicit [colors] tokens).
 */
fun buildProShiftLogoImageVector(
    variant: ProShiftLogoVariant,
    withRing: Boolean = false,
    isDarkTheme: Boolean = true,
    colors: ProShiftLogoColors = proShiftLogoColors(isDarkTheme)
): ImageVector {
    val useRing = when (variant) {
        ProShiftLogoVariant.SYMBOL -> false
        ProShiftLogoVariant.RING_SYMBOL -> true
        ProShiftLogoVariant.HORIZONTAL, ProShiftLogoVariant.STACKED -> withRing
    }

    val dayNodes = if (useRing) ProShiftLogoPaths.ringSymbolDayShiftNodes else ProShiftLogoPaths.symbolDayShiftNodes
    val nightNodes = if (useRing) ProShiftLogoPaths.ringSymbolNightShiftNodes else ProShiftLogoPaths.symbolNightShiftNodes
    val ribbonNodes = if (useRing) ProShiftLogoPaths.ringSymbolCoreRibbonNodes else ProShiftLogoPaths.symbolCoreRibbonNodes

    val dayBrush = SolidColor(colors.dayShift)
    val nightBrush = SolidColor(colors.nightShift)
    val ribbonBrush = SolidColor(colors.coreRibbon)

    val builder = ImageVector.Builder(
        name = "ProShiftLogo_${variant.key}_${if (useRing) "ring" else "plain"}_${colors.hashCode()}",
        defaultWidth = variant.viewportWidth.dp,
        defaultHeight = variant.viewportHeight.dp,
        viewportWidth = variant.viewportWidth,
        viewportHeight = variant.viewportHeight
    )

    when (variant) {
        ProShiftLogoVariant.SYMBOL,
        ProShiftLogoVariant.RING_SYMBOL -> {
            builder.addPath(pathData = dayNodes, fill = dayBrush)
            builder.addPath(pathData = nightNodes, fill = nightBrush)
            builder.addPath(pathData = ribbonNodes, fill = ribbonBrush)
        }

        ProShiftLogoVariant.HORIZONTAL -> {
            builder.addPath(pathData = dayNodes, fill = dayBrush)
            builder.addPath(pathData = nightNodes, fill = nightBrush)
            builder.addPath(pathData = ribbonNodes, fill = ribbonBrush)
            builder.addPath(
                pathData = ProShiftLogoPaths.wordmarkProNodes,
                pathFillType = PathFillType.EvenOdd,
                fill = ribbonBrush
            )
            builder.addPath(
                pathData = ProShiftLogoPaths.wordmarkShiftNodes,
                pathFillType = PathFillType.EvenOdd,
                fill = dayBrush
            )
        }

        ProShiftLogoVariant.STACKED -> {
            // Centered emblem on top: translate(135.04, 12) scale(0.82)
            builder.group(
                name = "symbol",
                translationX = 135.04f,
                translationY = 12f,
                scaleX = 0.82f,
                scaleY = 0.82f
            ) {
                addPath(pathData = dayNodes, fill = dayBrush)
                addPath(pathData = nightNodes, fill = nightBrush)
                addPath(pathData = ribbonNodes, fill = ribbonBrush)
            }
            // Centered PROSHIFT wordmark below (268 - 241.5 = 26.5, 94 + 146 = 240 -> centered in 480w)
            builder.group(
                name = "wordmark",
                translationX = -241.5f,
                translationY = 146f
            ) {
                addPath(
                    pathData = ProShiftLogoPaths.wordmarkProNodes,
                    pathFillType = PathFillType.EvenOdd,
                    fill = ribbonBrush
                )
                addPath(
                    pathData = ProShiftLogoPaths.wordmarkShiftNodes,
                    pathFillType = PathFillType.EvenOdd,
                    fill = dayBrush
                )
            }
        }
    }

    return builder.build()
}

/**
 * Centralized, reusable PROSHIFT corporate vector logo (Jetpack Compose equivalent of ProShiftLogo).
 *
 * - 100% transparent background (no container box, circle, square, border, or <rect>).
 * - Automatically switches between official Dark and Light mode `coreRibbon`, `dayShift`, and
 *   `nightShift` colors immediately upon theme changes.
 * - Dynamically renders `SYMBOL`, `RING_SYMBOL`, `HORIZONTAL`, and `STACKED` variants using
 *   unmodified official SVG path coordinates and EvenOdd vector wordmark paths.
 */
@Composable
fun ProShiftLogo(
    variant: ProShiftLogoVariant = ProShiftLogoVariant.HORIZONTAL,
    modifier: Modifier = Modifier,
    withRing: Boolean = false,
    height: Dp? = null,
    size: Dp? = null,
    isDarkTheme: Boolean = DynamicTheme.colors.isDark,
    colors: ProShiftLogoColors = proShiftLogoColors(isDarkTheme),
    contentDescription: String? = "PROSHIFT Logo"
) {
    val imageVector = remember(variant, withRing, colors) {
        buildProShiftLogoImageVector(
            variant = variant,
            withRing = withRing,
            isDarkTheme = isDarkTheme,
            colors = colors
        )
    }

    val resolvedHeight = height ?: size
    val sizedModifier = if (resolvedHeight != null) {
        val aspectRatio = variant.viewportWidth / variant.viewportHeight
        modifier.size(width = resolvedHeight * aspectRatio, height = resolvedHeight)
    } else {
        modifier
    }

    Image(
        imageVector = imageVector,
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        modifier = sizedModifier
    )
}

/**
 * String-key overload supporting `"symbol"`, `"ring-symbol"`, `"horizontal"`, and `"stacked"`.
 */
@Composable
fun ProShiftLogo(
    variant: String,
    modifier: Modifier = Modifier,
    withRing: Boolean = false,
    height: Dp? = null,
    size: Dp? = null,
    isDarkTheme: Boolean = DynamicTheme.colors.isDark,
    colors: ProShiftLogoColors = proShiftLogoColors(isDarkTheme),
    contentDescription: String? = "PROSHIFT Logo"
) {
    ProShiftLogo(
        variant = ProShiftLogoVariant.fromKey(variant),
        modifier = modifier,
        withRing = withRing,
        height = height,
        size = size,
        isDarkTheme = isDarkTheme,
        colors = colors,
        contentDescription = contentDescription
    )
}

/**
 * Reusable transparent corporate Top AppBar brand lockup for PROSHIFT main screens.
 * Displays the official horizontal PROSHIFT vector logo directly over the header surface
 * with an optional section subtitle beside the wordmark (zero background boxes).
 */
@Composable
fun ProShiftBrandHeader(
    sectionLabel: String,
    modifier: Modifier = Modifier,
    withRing: Boolean = true,
    logoHeight: Dp = 36.dp,
    isDarkTheme: Boolean = DynamicTheme.colors.isDark
) {
    val dyn = DynamicTheme.colors
    val logoColors = proShiftLogoColors(isDarkTheme)
    Row(
        modifier = modifier.testTag("proshift_brand_header"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        ProShiftLogo(
            variant = ProShiftLogoVariant.HORIZONTAL,
            withRing = withRing,
            height = logoHeight,
            isDarkTheme = isDarkTheme,
            contentDescription = "PROSHIFT Logo"
        )
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(14.dp)
                .background(dyn.borderMetal, RoundedCornerShape(1.dp))
        )
        Text(
            text = sectionLabel,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.6.sp,
            color = logoColors.dayShift,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
