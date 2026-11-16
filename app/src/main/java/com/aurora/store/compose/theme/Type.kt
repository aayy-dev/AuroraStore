/*
 * SPDX-FileCopyrightText: 2026 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.theme

import android.icu.util.ULocale
import android.os.Build
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.aurora.store.R
import java.util.Locale

/**
 * Builds one weight of the bundled Google Sans Flex variable font by setting its `wght` axis.
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
 * Latin-script languages that need letters the bundled font doesn't have (Kabyle ɛ ɣ ḍ, Esperanto
 * ĉ ĝ ŝ, Asturian ḥ ḷ), so mixing it with system fallback glyphs would look broken.
 */
private val unsupportedLatinLanguages = setOf("kab", "eo", "ast")

/**
 * Whether [StoreFontFamily] can render [locale] properly.
 *
 * The font only covers Latin script, and its weights come from variation axes that Android
 * ignores before 8.0 (every weight would render as regular), so everything else keeps the system
 * font, which has real glyphs and weights for every script.
 */
fun supportsStoreFont(locale: Locale): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
    if (locale.language in unsupportedLatinLanguages) return false
    return ULocale.addLikelySubtags(ULocale.forLocale(locale)).script == "Latn"
}

/**
 * Material 3 type scale (baseline and emphasized styles) set in [StoreFontFamily] where the
 * current locale and Android version support it, otherwise in the system font.
 */
@Composable
fun rememberStoreTypography(): Typography {
    val locale = LocalLocale.current.platformLocale
    return remember(locale) {
        if (supportsStoreFont(locale)) Typography(StoreFontFamily) else Typography()
    }
}
