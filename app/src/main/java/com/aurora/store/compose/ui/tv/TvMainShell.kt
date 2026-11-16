/*
 * SPDX-FileCopyrightText: 2026 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.ui.tv

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
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
                    .onFocusChanged { barFocused = it.hasFocus }
                    .focusRestorer(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val selected = item is TvTopBarItem.Section && item.index == selectedIndex
                    TopBarPill(
                        iconRes = item.iconRes,
                        label = stringResource(item.labelRes),
                        badgeCount = item.badgeCount,
                        selected = selected,
                        expanded = barFocused && item.showsLabel,
                        onClick = {
                            when (item) {
                                is TvTopBarItem.Section -> onSelect(item.index)
                                is TvTopBarItem.Action -> item.onClick()
                            }
                        }
                    )
                }
                AvatarButton(url = avatarUrl, onClick = onAvatarClick)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            content()
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
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .height(40.dp)
            .semantics { contentDescription = label },
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
private fun AvatarButton(url: String?, onClick: () -> Unit) {
    val label = stringResource(R.string.title_more)
    Surface(
        modifier = Modifier
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
