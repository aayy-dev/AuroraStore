/*
 * SPDX-FileCopyrightText: 2026 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.ui.tv

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aurora.store.compose.composition.LocalUI
import com.aurora.store.compose.composition.UI
import com.aurora.store.compose.theme.TvDimens

/**
 * Scrolls a lazy list so the focused item's leading edge sits [leading] from the list's start,
 * the way TV rows keep the focused card (and the focused row) at a fixed position instead of
 * scrolling only as far as needed. The list stops at its ends as usual.
 */
private class PivotBringIntoViewSpec(private val leadingPx: Float) : BringIntoViewSpec {
    override fun calculateScrollDistance(offset: Float, size: Float, containerSize: Float): Float =
        offset - leadingPx
}

/**
 * Provides a pivot scrolling behavior (see [PivotBringIntoViewSpec]) to lazy lists in [content].
 * Nested lists need their own provider, since this one is inherited.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TvPivot(leading: Dp, content: @Composable () -> Unit) {
    val leadingPx = with(LocalDensity.current) { leading.toPx() }
    val spec = remember(leadingPx) { PivotBringIntoViewSpec(leadingPx) }
    CompositionLocalProvider(LocalBringIntoViewSpec provides spec, content = content)
}

/**
 * Scrolls only as far as needed to show the focused item, keeping [margin] from the edges. This
 * replaces Compose's default on TV (which centers the focused item) for pages such as app
 * details, whose header must stay in view while its buttons have focus.
 */
private class MinimalBringIntoViewSpec(private val marginPx: Float) : BringIntoViewSpec {
    override fun calculateScrollDistance(offset: Float, size: Float, containerSize: Float): Float {
        val leading = offset
        val trailing = offset + size
        return when {
            leading >= marginPx && trailing <= containerSize - marginPx -> 0f
            leading < marginPx || size > containerSize - 2 * marginPx -> leading - marginPx
            else -> trailing - (containerSize - marginPx)
        }
    }
}

/**
 * Provides [MinimalBringIntoViewSpec] to lazy lists in [content].
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TvMinimalScroll(margin: Dp, content: @Composable () -> Unit) {
    val marginPx = with(LocalDensity.current) { margin.toPx() }
    val spec = remember(marginPx) { MinimalBringIntoViewSpec(marginPx) }
    CompositionLocalProvider(LocalBringIntoViewSpec provides spec, content = content)
}

/**
 * On TV, keeps a phone-style screen's content away from the screen edges, as TV layouts expect.
 * Elsewhere it shows [content] as is.
 */
@Composable
fun TvInset(content: @Composable () -> Unit) {
    if (LocalUI.current != UI.TV) {
        content()
        return
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = TvDimens.ScreenPaddingHorizontal - 24.dp)
    ) {
        content()
    }
}
