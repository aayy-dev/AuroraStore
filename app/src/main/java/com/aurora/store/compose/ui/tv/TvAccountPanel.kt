/*
 * SPDX-FileCopyrightText: 2026 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.ui.tv

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.Surface
import com.aurora.store.R
import com.aurora.store.compose.composable.AccountAvatar
import com.aurora.store.compose.theme.TvBackground
import com.aurora.store.compose.theme.TvCard
import com.aurora.store.compose.theme.TvDimens
import com.aurora.store.compose.theme.TvFocusedSurface
import com.aurora.store.compose.theme.TvOnFocusedSurface
import com.aurora.store.compose.theme.TvOnSurface
import com.aurora.store.compose.theme.TvOnSurfaceMuted

private val RowShape = RoundedCornerShape(8.dp)

/**
 * One entry of the TV account page: a menu row on the left or a card on the right.
 */
data class TvMenuEntry(
    val label: String,
    @DrawableRes val iconRes: Int? = null,
    val subtitle: String? = null,
    val onClick: () -> Unit
)

/**
 * Full-screen account page for TV, modeled on the TV Play Store's account page: the account on top,
 * a list of sections on the left and shortcut cards with chevrons on the right. Back closes it.
 */
@Composable
fun TvAccountPanel(
    avatarUrl: String?,
    name: String,
    email: String,
    onManageAccounts: () -> Unit,
    menu: List<TvMenuEntry>,
    cards: List<TvMenuEntry>,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val firstItem = remember { FocusRequester() }
        RequestFocusOnce(firstItem)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(TvBackground)
                .padding(
                    horizontal = TvDimens.ScreenPaddingHorizontal,
                    vertical = TvDimens.ScreenPaddingTop
                ),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AccountAvatar(url = avatarUrl, size = 56.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = TvOnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TvOnSurfaceMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                TvButton(
                    label = stringResource(R.string.manage_account),
                    iconRes = R.drawable.ic_account_manager,
                    onClick = onManageAccounts
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(48.dp)) {
                Column(
                    modifier = Modifier
                        .width(280.dp)
                        .focusRestorer()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    menu.forEachIndexed { index, entry ->
                        MenuRow(
                            modifier = if (index == 0) {
                                Modifier.focusRequester(firstItem)
                            } else {
                                Modifier
                            },
                            entry = entry
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .focusRestorer(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    cards.forEach { entry -> CardRow(entry = entry) }
                }
            }
        }
    }
}

@Composable
private fun MenuRow(entry: TvMenuEntry, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        onClick = entry.onClick,
        shape = ClickableSurfaceDefaults.shape(shape = RowShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            contentColor = TvOnSurface,
            focusedContainerColor = TvFocusedSurface,
            focusedContentColor = TvOnFocusedSurface,
            pressedContainerColor = TvFocusedSurface,
            pressedContentColor = TvOnFocusedSurface
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (entry.iconRes != null) {
                Icon(
                    modifier = Modifier.size(22.dp),
                    painter = painterResource(entry.iconRes),
                    contentDescription = null,
                    tint = LocalContentColor.current
                )
            }
            Text(
                text = entry.label,
                style = MaterialTheme.typography.titleMedium,
                color = LocalContentColor.current,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CardRow(entry: TvMenuEntry) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (entry.subtitle != null) 72.dp else 64.dp),
        onClick = entry.onClick,
        shape = ClickableSurfaceDefaults.shape(shape = RowShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = TvCard,
            contentColor = TvOnSurface,
            focusedContainerColor = TvFocusedSurface,
            focusedContentColor = TvOnFocusedSurface,
            pressedContainerColor = TvFocusedSurface,
            pressedContentColor = TvOnFocusedSurface
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = LocalContentColor.current,
                    maxLines = 1
                )
                if (entry.subtitle != null) {
                    Text(
                        text = entry.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = LocalContentColor.current.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Icon(
                modifier = Modifier.size(22.dp),
                painter = painterResource(R.drawable.ic_arrow_forward),
                contentDescription = null,
                tint = LocalContentColor.current
            )
        }
    }
}
