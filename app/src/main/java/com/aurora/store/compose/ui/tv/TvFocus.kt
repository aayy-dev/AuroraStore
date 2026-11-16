/*
 * SPDX-FileCopyrightText: 2026 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.ui.tv

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.aurora.store.compose.composition.LocalUI
import com.aurora.store.compose.composition.UI
import com.aurora.store.compose.theme.TvDimens
import kotlinx.coroutines.launch

/**
 * Scrolls a lazy list so the focused item's leading edge sits [leadingPx] from the list's start,
 * the way TV rows keep the focused card (and the focused row) at a fixed position instead of
 * scrolling only as far as needed. The list stops at its ends as usual.
 *
 * Compose hands the spec physical coordinates and mirrors the result for right-to-left rows, so
 * a right-to-left row pins the item's right edge instead.
 */
private class PivotBringIntoViewSpec(
    private val leadingPx: Float,
    private val pinEnd: Boolean
) : BringIntoViewSpec {
    override fun calculateScrollDistance(offset: Float, size: Float, containerSize: Float): Float =
        if (pinEnd) {
            offset + size - (containerSize - leadingPx)
        } else {
            offset - leadingPx
        }
}

/**
 * Provides a pivot scrolling behavior (see [PivotBringIntoViewSpec]) to lazy lists in [content].
 * Set [horizontal] for rows so right-to-left layouts pin to the correct side. Nested lists need
 * their own provider, since this one is inherited.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TvPivot(leading: Dp, horizontal: Boolean = false, content: @Composable () -> Unit) {
    val leadingPx = with(LocalDensity.current) { leading.toPx() }
    val pinEnd = horizontal && LocalLayoutDirection.current == LayoutDirection.Rtl
    val spec = remember(leadingPx, pinEnd) { PivotBringIntoViewSpec(leadingPx, pinEnd) }
    CompositionLocalProvider(LocalBringIntoViewSpec provides spec, content = content)
}

/**
 * Tracks whether the user is moving around a screen's header (top bar, sub-page pills), so content
 * that finishes loading doesn't pull focus away from there.
 */
@Stable
class TvFocusGuard {
    var inHeader by mutableStateOf(false)

    /** Set while the top bar is about to take focus itself (on launch, or coming back to it). */
    var topBarClaim by mutableStateOf(false)

    /** Whether content should leave focus where it is. */
    val blocked: Boolean
        get() = inHeader || topBarClaim
}

/**
 * On TV, makes the item of the list in [listState] at [index] (or with [key]) a focus stop, so
 * text that has nothing to click can still be reached and read with the D-pad. While it has focus, up/down scroll through
 * it a step at a time when it's taller than the screen, then move on as usual. Elsewhere it does
 * nothing.
 */
@Composable
fun Modifier.tvReadable(listState: LazyListState, index: Int = -1, key: Any? = null): Modifier {
    if (LocalUI.current != UI.TV) return this
    val scope = rememberCoroutineScope()
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(12.dp)
    return this
        .onFocusChanged { focused = it.isFocused }
        .then(
            if (focused) {
                Modifier
                    .background(Color.White.copy(alpha = 0.06f), shape)
                    .border(2.dp, Color.White.copy(alpha = 0.7f), shape)
            } else {
                Modifier
            }
        )
        .onKeyEvent { event ->
            if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
            val layout = listState.layoutInfo
            val item = layout.visibleItemsInfo.firstOrNull {
                if (key != null) it.key == key else it.index == index
            }
                ?: return@onKeyEvent false
            val step = (layout.viewportEndOffset - layout.viewportStartOffset) * 0.6f
            val scrollBy = when (event.key) {
                Key.DirectionDown ->
                    if (item.offset + item.size > layout.viewportEndOffset) step else null
                Key.DirectionUp ->
                    if (item.offset < layout.viewportStartOffset) -step else null
                else -> null
            } ?: return@onKeyEvent false
            scope.launch { listState.animateScrollBy(scrollBy) }
            true
        }
        .focusable()
}

/**
 * Stops the D-pad from leaving the ends of a row of [count] items sideways, where the nearest
 * thing on screen would otherwise be something in another row.
 */
fun Modifier.tvRowEnds(index: Int, count: Int): Modifier = focusProperties {
    if (index == 0) start = FocusRequester.Cancel
    if (index == count - 1) end = FocusRequester.Cancel
}

/** The [TvFocusGuard] of the current TV screen. */
val LocalTvFocusGuard = staticCompositionLocalOf { TvFocusGuard() }

/**
 * Marks this header element (top bar, pill row) for [LocalTvFocusGuard].
 */
@Composable
fun Modifier.tvHeader(): Modifier {
    val guard = LocalTvFocusGuard.current
    return onFocusChanged { guard.inHeader = it.hasFocus }
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
