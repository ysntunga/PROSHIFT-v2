package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.example.R
import com.example.ui.components.ProShiftLogo
import com.example.ui.components.ProShiftLogoVariant
import com.example.ui.components.proShiftLogoColors
import com.example.ui.i18n.LocalIsEnglish
import kotlinx.coroutines.delay

/**
 * Initial launch splash screen with a smooth fade-in and subtle scale animation.
 * Renders the new Material 3 theme-aware [ProShiftLogo] (stacked variant with Live-Shift ring)
 * directly over the active Light/Dark surface with a 100% transparent logo background.
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier,
    fadeInDurationMillis: Int = 800,
    holdDurationMillis: Long = 650L
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.background.luminance() < 0.5f
    val logoColors = remember(isDark) { proShiftLogoColors(isDark) }
    val accentBrush = remember(logoColors) {
        Brush.horizontalGradient(listOf(logoColors.nightShift, logoColors.dayShift))
    }
    val isEnglish = LocalIsEnglish.current

    val revealProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        revealProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = fadeInDurationMillis,
                easing = FastOutSlowInEasing
            )
        )
        delay(holdDurationMillis)
        onSplashFinished()
    }

    val logoAlpha = revealProgress.value
    val logoScale = lerp(0.92f, 1f, logoAlpha)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .testTag("splash_screen_root"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 32.dp)
                .graphicsLayer {
                    alpha = logoAlpha
                    scaleX = logoScale
                    scaleY = logoScale
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ProShiftLogo(
                variant = ProShiftLogoVariant.STACKED,
                withRing = true,
                height = 148.dp,
                colors = logoColors,
                contentDescription = stringResource(id = R.string.splash_logo_content_description),
                modifier = Modifier.testTag("splash_app_logo")
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(3.dp)
                    .background(
                        brush = accentBrush,
                        shape = RoundedCornerShape(2.dp)
                    )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(
                    id = if (isEnglish) R.string.splash_tagline_en else R.string.splash_tagline_tr
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("splash_app_tagline")
            )
        }
    }
}
