/*
 * SPDX-FileCopyrightText: 2026 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.ui.tv

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import com.aurora.store.R
import com.aurora.store.compose.composable.AccountAvatar
import com.aurora.store.compose.theme.TvBackground
import com.aurora.store.compose.theme.TvDimens
import com.aurora.store.compose.theme.TvFocusedSurface
import com.aurora.store.compose.theme.TvOnFocusedSurface
import com.aurora.store.compose.theme.TvOnSurface
import com.aurora.store.compose.theme.TvOnSurfaceMuted
import com.aurora.store.compose.theme.TvSelectedPill

private const val BAR_FOCUS_NONE = -1
private const val BAR_FOCUS_SELECTED = -2
private const val BAR_FOCUS_AVATAR = -3

/**
 * A section of the TV top bar. [Section] entries switch the page shown under the bar, [Action]
 * entries open another screen.
 */
sealed class TvTopBarItem {
    abstract val labelRes: Int
    abstract val iconRes: Int
    abstract val badgeCount: Int

    /** Whether the label shows next to the icon while the top bar has focus. */
    val showsLabel: Boolean
        get() = this is Section || (this is Action && showLabel)

    data class Section(
        val index: Int,
        override val labelRes: Int,
        @DrawableRes override val iconRes: Int,
        override val badgeCount: Int = 0
    ) : TvTopBarItem()

    data class Action(
        override val labelRes: Int,
        @DrawableRes override val iconRes: Int,
        override val badgeCount: Int = 0,
        val showLabel: Boolean = false,
        val onClick: () -> Unit
    ) : TvTopBarItem()
}

/**
 * Main screen layout for TV, modeled on the TV Play Store: the app logo on the left and the
 * sections (Search, Apps, Games, Updates) plus account on the right of a top bar, with the
 * selected section's page underneath. Section labels only show while the bar has focus.
 */
@Composable
fun TvMainShell(
    items: List<TvTopBarItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    avatarUrl: String?,
    onAvatarClick: () -> Unit,
    content: @Composable () -> Unit
) {
    var barFocused by remember { mutableStateOf(false) }
    val itemFocus = remember(items.size) { List(items.size) { FocusRequester() } }
    val avatarFocus = remember { FocusRequester() }
    val selectedItem = items.indexOfFirst {
        it is TvTopBarItem.Section && it.index == selectedIndex
    }

    // What the top bar should focus when this screen shows: the selected section on launch, or
    // the entry that opened another screen, so Back from that screen comes back to it
    var pendingFocus by rememberSaveable { mutableIntStateOf(BAR_FOCUS_SELECTED) }
    val guard = remember { TvFocusGuard().apply { topBarClaim = pendingFocus != BAR_FOCUS_NONE } }
    LaunchedEffect(Unit) {
        val target = when (val pending = pendingFocus) {
            BAR_FOCUS_NONE -> null
            BAR_FOCUS_SELECTED -> itemFocus.getOrNull(selectedItem)
            BAR_FOCUS_AVATAR -> avatarFocus
            else -> itemFocus.getOrNull(pending)
        }
        if (target != null) {
            repeat(10) {
                withFrameNanos { }
                if (runCatching { target.requestFocus() }.getOrDefault(false)) {
                    pendingFocus = BAR_FOCUS_NONE
                    guard.topBarClaim = false
                    return@LaunchedEffect
                }
            }
        }
        pendingFocus = BAR_FOCUS_NONE
        guard.topBarClaim = false
    }

    // Like the TV Play Store, Back from the page first goes up to the top bar; Back from the top
    // bar leaves the app
    BackHandler(enabled = !barFocused) {
        itemFocus.getOrNull(selectedItem)?.let { runCatching { it.requestFocus() } }
    }

    CompositionLocalProvider(LocalTvFocusGuard provides guard) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(TvBackground)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = TvDimens.ScreenPaddingHorizontal,
                        end = TvDimens.ScreenPaddingHorizontal,
                        top = TvDimens.ScreenPaddingTop,
                        bottom = 12.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    modifier = Modifier.size(32.dp),
                    painter = painterResource(R.drawable.ic_logo_mark),
                    contentDescription = null
                )
                Spacer(Modifier.size(12.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall,
                    color = TvOnSurface
                )
                Spacer(Modifier.weight(1f))
                Row(
                    modifier = Modifier
                        .onFocusChanged {
                            barFocused = it.hasFocus
                            guard.inHeader = it.hasFocus
                        }
                        // Going up from the page lands on the selected section, not on whatever
                        // happens to be above the focused item
                        .focusProperties {
                            onEnter = {
                                if (requestedFocusDirection == FocusDirection.Up) {
                                    itemFocus.getOrNull(selectedItem)?.requestFocus()
                                }
                            }
                        }
                        .focusGroup(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEachIndexed { position, item ->
                        val selected = item is TvTopBarItem.Section && item.index == selectedIndex
                        TopBarPill(
                            modifier = Modifier
                                .focusRequester(itemFocus[position])
                                // The avatar closes the row on the other side
                                .tvRowEnds(position, items.size + 1),
                            iconRes = item.iconRes,
                            label = stringResource(item.labelRes),
                            badgeCount = item.badgeCount,
                            selected = selected,
                            expanded = barFocused && item.showsLabel,
                            onClick = {
                                when (item) {
                                    is TvTopBarItem.Section -> onSelect(item.index)
                                    is TvTopBarItem.Action -> {
                                        pendingFocus = position
                                        item.onClick()
                                    }
                                }
                            }
                        )
                    }
                    AvatarButton(
                        modifier = Modifier
                            .focusRequester(avatarFocus)
                            .tvRowEnds(items.size, items.size + 1),
                        url = avatarUrl,
                        onClick = {
                            // The account panel opens Settings, Downloads, Blacklist...
                            pendingFocus = BAR_FOCUS_AVATAR
                            onAvatarClick()
                        }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    // Once the user is in the page, coming back should restore the page's focus
                    .onFocusChanged { if (it.hasFocus) pendingFocus = BAR_FOCUS_NONE }
            ) {
                content()
            }
        }
    }
}

@Composable
private fun TopBarPill(
    @DrawableRes iconRes: Int,
    label: String,
    badgeCount: Int,
    selected: Boolean,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val description = if (badgeCount > 0) "$label ($badgeCount)" else label
    Surface(
        modifier = modifier
            .height(40.dp)
            .semantics {
                contentDescription = description
                this.selected = selected
                role = Role.Tab
            },
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape = CircleShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) TvSelectedPill else Color.Transparent,
            contentColor = if (selected) TvOnSurface else TvOnSurfaceMuted,
            focusedContainerColor = TvFocusedSurface,
            focusedContentColor = TvOnFocusedSurface,
            pressedContainerColor = TvFocusedSurface,
            pressedContentColor = TvOnFocusedSurface
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BadgedBox(
                badge = {
                    if (badgeCount > 0) Badge { Text("$badgeCount") }
                }
            ) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = androidx.tv.material3.LocalContentColor.current
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                Text(
                    modifier = Modifier.padding(start = 8.dp, end = 4.dp),
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    color = androidx.tv.material3.LocalContentColor.current,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun AvatarButton(url: String?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val label = stringResource(R.string.title_more)
    Surface(
        modifier = modifier
            .size(44.dp)
            .semantics { contentDescription = label },
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape = CircleShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = TvFocusedSurface,
            pressedContainerColor = TvFocusedSurface
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AccountAvatar(url = url, size = 34.dp)
        }
    }
}
