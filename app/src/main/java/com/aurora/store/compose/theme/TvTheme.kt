/*
 * SPDX-FileCopyrightText: 2026 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.theme

import androidx.compose.foundation.LocalIndication
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.RippleConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme as TvMaterialTheme
import androidx.tv.material3.darkColorScheme as tvDarkColorScheme
import com.aurora.store.compose.ui.tv.TvFocusIndication

/*
 * Palette sampled from the Play Store app on Google TV (Android 16, Nov 2026). The TV store is
 * dark only: near-black background, dark grey cards and a light "focused" surface with dark text.
 */
val TvBackground = Color(0xFF0E0E0F) // sampled
val TvCard = Color(0xFF1B1B1C) // sampled
val TvCardHigh = Color(0xFF262627) // inferred, one step above TvCard
val TvOnSurface = Color(0xFFE8EAED) // sampled
val TvOnSurfaceMuted = Color(0xFF969CA2) // sampled
val TvFocusedSurface = Color(0xFFE8EAED) // sampled
val TvOnFocusedSurface = Color(0xFF1F1F1F) // inferred
val TvSelectedPill = Color(0xFF303031) // sampled
val TvSectionTitle = Color(0xFF919294) // sampled

/**
 * Store dark scheme with the TV surfaces swapped in, so phone screens reused on TV sit on the same
 * background as the TV-specific ones.
 */
val StoreTvColorScheme: ColorScheme = StoreDarkColorScheme.copy(
    // The TV store is monochrome: light accents (stars, filled buttons, progress) on dark
    primary = TvOnSurface,
    onPrimary = TvOnFocusedSurface,
    background = TvBackground,
    surface = TvBackground,
    surfaceDim = TvBackground,
    onBackground = TvOnSurface,
    onSurface = TvOnSurface,
    onSurfaceVariant = TvOnSurfaceMuted,
    surfaceContainerLowest = Color(0xFF0A0A0B),
    surfaceContainerLow = Color(0xFF151516),
    surfaceContainer = TvCard,
    surfaceContainerHigh = TvCardHigh,
    surfaceContainerHighest = Color(0xFF313132)
)

private val TvMaterialColorScheme = tvDarkColorScheme(
    primary = StoreDarkColorScheme.primary,
    onPrimary = StoreDarkColorScheme.onPrimary,
    secondary = StoreDarkColorScheme.secondary,
    onSecondary = StoreDarkColorScheme.onSecondary,
    background = TvBackground,
    onBackground = TvOnSurface,
    surface = TvBackground,
    onSurface = TvOnSurface,
    surfaceVariant = TvCard,
    onSurfaceVariant = TvOnSurfaceMuted,
    inverseSurface = TvFocusedSurface,
    inverseOnSurface = TvOnFocusedSurface,
    border = TvOnSurface
)

private val TvRippleConfiguration = RippleConfiguration(
    color = Color.White,
    rippleAlpha = RippleAlpha(
        draggedAlpha = 0.16f,
        focusedAlpha = 0.32f,
        hoveredAlpha = 0.08f,
        pressedAlpha = 0.4f
    )
)

/**
 * Supplies the Compose for TV theme (used by its focusable surfaces, buttons and cards) on top of
 * the app's Material 3 theme, plus a TV focus indication for the Material components.
 */
@Composable
fun AuroraTvTheme(content: @Composable () -> Unit) {
    TvMaterialTheme(colorScheme = TvMaterialColorScheme) {
        // Phone-style components reused on TV get a clearly visible focus: an outline for plain
        // clickable rows, and a much stronger focus layer for Material buttons and chips
        CompositionLocalProvider(
            LocalIndication provides TvFocusIndication,
            LocalRippleConfiguration provides TvRippleConfiguration,
            // Text and icons outside a Material surface default to light, not black
            LocalContentColor provides TvOnSurface,
            content = content
        )
    }
}

/**
 * Layout measurements of the TV Play Store at 1080p (320 dpi, so 960 x 540 dp).
 */
object TvDimens {
    /** Side margin of rows and the top bar (112 px sampled). */
    val ScreenPaddingHorizontal = 56.dp

    /** Top margin of the top bar. */
    val ScreenPaddingTop = 28.dp

    /** 16:9 app card (298 x 168 px sampled). */
    val CardWidth = 150.dp
    val CardHeight = 84.dp

    /** Gap between cards in a row (44 px sampled). */
    val CardSpacing = 22.dp

    /** Cards per grid row: five 150 dp cards and their gaps fit the 848 dp between margins. */
    const val GRID_COLUMNS = 5

    /** Focused card scale (362 px focused vs 298 px, about 1.2x). */
    const val FOCUSED_CARD_SCALE = 1.2f
}
