/*
 * SPDX-FileCopyrightText: 2026 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.ui.tv

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

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
