/*
 * SPDX-FileCopyrightText: 2025 The Calyx Institute
 * SPDX-FileCopyrightText: 2026 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Whether the active [MaterialTheme] is dark.
 *
 * Derived from the resolved color scheme rather than [androidx.compose.foundation.isSystemInDarkTheme]
 * so it stays correct when the user forces a light/dark theme that differs from the system setting.
 */
@Composable
@ReadOnlyComposable
private fun isAppInDarkTheme(): Boolean = MaterialTheme.colorScheme.surface.luminance() < 0.5f

/**
 * Amber used to flag warnings/caveats. Lightened in dark theme for adequate contrast.
 */
val warningColor: Color
    @Composable @ReadOnlyComposable
    get() = if (isAppInDarkTheme()) Color(0xFFFFB74D) else Color(0xFFFF7600)

/**
 * Green used to flag positive/success states. Lightened in dark theme for adequate contrast.
 */
val successColor: Color
    @Composable @ReadOnlyComposable
    get() = if (isAppInDarkTheme()) Color(0xFF5BD27A) else Color(0xFF1B8738)

val colorGreen: Color
    @Composable @ReadOnlyComposable
    get() = if (isAppInDarkTheme()) Color(0xFF81C784) else Color(0xFF388E3C)

val colorRed: Color
    @Composable @ReadOnlyComposable
    get() = if (isAppInDarkTheme()) Color(0xFFE57373) else Color(0xFFD32F2F)

/*
 * Store color schemes modeled on the current Google Play Store look (neutral surfaces with blue
 * accents). These are applied on every device; wallpaper-based dynamic color is intentionally not
 * used so the app looks the same everywhere.
 *
 * Roles marked "sampled" were measured from screenshots of the Play Store (v54.1) on an Android 17
 * emulator; the remaining roles follow the same blue/neutral tonal palette so that components which
 * use them (menus, snackbars, error states, etc.) stay consistent.
 */

val StoreLightColorScheme = lightColorScheme(
    primary = Color(0xFF0B57D0), // sampled: Install button, links, stars
    onPrimary = Color(0xFFFFFFFF), // sampled
    primaryContainer = Color(0xFFD3E3FD),
    onPrimaryContainer = Color(0xFF041E49),
    inversePrimary = Color(0xFFA8C7FA),
    secondary = Color(0xFF00639B), // sampled: selected navigation label
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFC2E7FF), // sampled: selected nav pill, selected filter chip
    onSecondaryContainer = Color(0xFF004A77), // sampled
    tertiary = Color(0xFF146C2E),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFC4EED0),
    onTertiaryContainer = Color(0xFF072711),
    background = Color(0xFFFFFFFF), // sampled
    onBackground = Color(0xFF1F1F1F), // sampled
    surface = Color(0xFFFFFFFF), // sampled
    onSurface = Color(0xFF1F1F1F), // sampled: headings
    surfaceVariant = Color(0xFFE1E3E1),
    onSurfaceVariant = Color(0xFF444746), // sampled: list titles, secondary text, icons
    surfaceTint = Color(0xFF0B57D0),
    inverseSurface = Color(0xFF303030),
    inverseOnSurface = Color(0xFFF2F2F2),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    outline = Color(0xFF747775),
    outlineVariant = Color(0xFFC4C7C5), // sampled: outlined buttons/chips, dividers
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFFD3DBE5),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF8FAFD),
    surfaceContainer = Color(0xFFF0F4F9), // sampled: navigation bar, chips, rating pills
    surfaceContainerHigh = Color(0xFFE9EEF6), // sampled: search bar, review cards, arrow buttons
    surfaceContainerHighest = Color(0xFFDDE3EA) // sampled: developer reply box
)

val StoreDarkColorScheme = darkColorScheme(
    primary = Color(0xFFA8C7FA), // sampled
    onPrimary = Color(0xFF062E6F), // sampled
    primaryContainer = Color(0xFF0842A0),
    onPrimaryContainer = Color(0xFFD3E3FD),
    inversePrimary = Color(0xFF0B57D0),
    secondary = Color(0xFF7FCFFF), // sampled
    onSecondary = Color(0xFF003355),
    secondaryContainer = Color(0xFF004A77), // sampled
    onSecondaryContainer = Color(0xFFC2E7FF), // sampled
    tertiary = Color(0xFF6DD58C),
    onTertiary = Color(0xFF0A3818),
    tertiaryContainer = Color(0xFF0F5223),
    onTertiaryContainer = Color(0xFFC4EED0),
    background = Color(0xFF131314), // sampled
    onBackground = Color(0xFFE3E3E3), // sampled
    surface = Color(0xFF131314), // sampled
    onSurface = Color(0xFFE3E3E3), // sampled
    surfaceVariant = Color(0xFF444746),
    onSurfaceVariant = Color(0xFFC4C7C5), // sampled
    surfaceTint = Color(0xFFA8C7FA),
    inverseSurface = Color(0xFFE3E3E3),
    inverseOnSurface = Color(0xFF303030),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    outline = Color(0xFF8E918F),
    outlineVariant = Color(0xFF444746), // sampled
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFF37393B),
    surfaceDim = Color(0xFF131314),
    surfaceContainerLowest = Color(0xFF0E0E0E),
    surfaceContainerLow = Color(0xFF1B1B1B),
    surfaceContainer = Color(0xFF1E1F20), // sampled
    surfaceContainerHigh = Color(0xFF282A2C), // sampled
    surfaceContainerHighest = Color(0xFF333537)
)
