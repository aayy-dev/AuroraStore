/*
 * SPDX-FileCopyrightText: 2026 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.aurora.store.R

/**
 * Builds one weight of the bundled Google Sans Flex variable font.
 *
 * On API 26+ the `wght` axis is set to match [weight]; older releases ignore variation settings
 * and render the font's default (regular) instance.
 */
private fun googleSansFlex(weight: FontWeight) = Font(
    resId = R.font.google_sans_flex,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight))
)

/**
 * Google Sans Flex (SIL OFL 1.1), the open release of the typeface used across the Play Store.
 */
val StoreFontFamily = FontFamily(
    googleSansFlex(FontWeight.Normal),
    googleSansFlex(FontWeight.Medium),
    googleSansFlex(FontWeight.SemiBold),
    googleSansFlex(FontWeight.Bold)
)

/**
 * Material 3 type scale (baseline and emphasized styles) set in [StoreFontFamily].
 */
val StoreTypography = Typography(StoreFontFamily)
