/*
 * SPDX-FileCopyrightText: 2026 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.ui.commons

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.aurora.store.R
import com.aurora.store.compose.composable.AccountAvatar
import com.aurora.store.compose.composable.StoreSheet
import com.aurora.store.compose.composition.LocalUI
import com.aurora.store.compose.composition.UI
import com.aurora.store.compose.navigation.Destination
import com.aurora.store.compose.ui.tv.TvAccountPanel
import com.aurora.store.compose.ui.tv.TvMenuEntry
import com.aurora.store.viewmodel.commons.MoreViewModel

private data class MoreItem(
    @StringRes val titleRes: Int,
    @DrawableRes val iconRes: Int,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreSheet(
    onDismiss: () -> Unit,
    onNavigateTo: (Destination) -> Unit = {},
    viewModel: MoreViewModel = hiltViewModel()
) {
    val mainItems = listOf(
        MoreItem(R.string.title_apps_games, R.drawable.ic_apps) {
            onNavigateTo(Destination.Installed)
        },
        MoreItem(R.string.title_blacklist_manager, R.drawable.ic_blacklist) {
            onNavigateTo(Destination.Blacklist)
        },
        MoreItem(R.string.title_favourites_manager, R.drawable.ic_favorite_unchecked) {
            onNavigateTo(Destination.Favourite)
        },
        MoreItem(R.string.title_spoof_manager, R.drawable.ic_spoof) {
            onNavigateTo(Destination.Spoof)
        }
    )
    val extraItems = listOf(
        MoreItem(R.string.title_settings, R.drawable.ic_menu_settings) {
            onNavigateTo(Destination.Settings)
        },
        MoreItem(R.string.title_about, R.drawable.ic_menu_about) { onNavigateTo(Destination.About) }
    )

    if (LocalUI.current == UI.TV) {
        val isAnonymous = viewModel.authProvider.isAnonymous
        val profile = viewModel.authProvider.authData?.userProfile
        fun go(destination: Destination) {
            onNavigateTo(destination)
            onDismiss()
        }
        TvAccountPanel(
            avatarUrl = profile?.artwork?.url,
            name = if (isAnonymous) {
                stringResource(R.string.account_anonymous)
            } else {
                profile?.name ?: stringResource(R.string.status_unavailable)
            },
            email = profile?.email ?: stringResource(R.string.status_unavailable),
            onManageAccounts = { go(Destination.Accounts) },
            menu = (mainItems + extraItems).map { item ->
                TvMenuEntry(
                    label = stringResource(item.titleRes),
                    iconRes = item.iconRes,
                    onClick = {
                        item.onClick()
                        onDismiss()
                    }
                )
            },
            cards = listOf(
                TvMenuEntry(
                    label = stringResource(R.string.title_download_manager),
                    onClick = { go(Destination.Downloads) }
                ),
                TvMenuEntry(
                    label = stringResource(R.string.title_notifications),
                    onClick = { go(Destination.Notifications) }
                )
            ),
            onDismiss = onDismiss
        )
        return
    }

    StoreSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = dimensionResource(R.dimen.spacing_large)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerLowest
            ) {
                AccountHeader(
                    viewModel = viewModel,
                    onNavigateToAccounts = {
                        onNavigateTo(Destination.Accounts)
                        onDismiss()
                    }
                )
            }

            MenuGroup(items = mainItems, onDismiss = onDismiss)
            MenuGroup(items = extraItems, onDismiss = onDismiss)
        }

        Spacer(
            Modifier
                .padding(bottom = dimensionResource(R.dimen.spacing_large))
                .navigationBarsPadding()
        )
    }
}

/**
 * Group of menu entries drawn as one rounded card with hairline gaps between rows, like the Play
 * Store account menu.
 */
@Composable
private fun MenuGroup(items: List<MoreItem>, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier.clip(MaterialTheme.shapes.large),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items.forEach { item ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                onClick = {
                    item.onClick()
                    onDismiss()
                }
            ) {
                Row(
                    modifier = Modifier
                        .heightIn(min = 56.dp)
                        .padding(horizontal = dimensionResource(R.dimen.spacing_large)),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(
                        dimensionResource(R.dimen.spacing_large)
                    )
                ) {
                    Icon(
                        painter = painterResource(item.iconRes),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(item.titleRes),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountHeader(viewModel: MoreViewModel, onNavigateToAccounts: () -> Unit) {
    val context = LocalContext.current
    val isAnonymous = viewModel.authProvider.isAnonymous

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.spacing_large)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AccountAvatar(
            url = viewModel.authProvider.authData?.userProfile?.artwork?.url,
            size = 48.dp
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = dimensionResource(R.dimen.spacing_medium))
        ) {
            Text(
                text = if (isAnonymous) {
                    stringResource(R.string.account_anonymous)
                } else {
                    viewModel.authProvider.authData?.userProfile?.name
                        ?: stringResource(R.string.status_unavailable)
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = viewModel.authProvider.authData?.userProfile?.email
                    ?: stringResource(R.string.status_unavailable),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onNavigateToAccounts) {
            Icon(
                painter = painterResource(R.drawable.ic_account_manager),
                contentDescription = stringResource(R.string.manage_account)
            )
        }
    }
}
