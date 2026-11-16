/*
 * SPDX-FileCopyrightText: 2025 The Calyx Institute
 * SPDX-FileCopyrightText: 2026 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.composable.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.aurora.extensions.requiresGMS
import com.aurora.gplayapi.model.App
import com.aurora.store.R
import com.aurora.store.compose.preview.AppPreviewProvider
import com.aurora.store.compose.preview.ThemePreviewProvider
import com.aurora.store.util.CommonUtil

/**
 * Composable for displaying app details in a vertical list, modeled on Play Store list rows:
 * icon, title, developer, then a rating pill followed by the remaining details.
 * @param modifier The modifier to be applied to the composable
 * @param app [App] to display
 * @param rank Optional chart position shown before the icon
 * @param onClick Callback when the composable is clicked
 * @see AppListItem
 */
@Composable
fun LargeAppListItem(
    modifier: Modifier = Modifier,
    app: App,
    rank: Int? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = dimensionResource(R.dimen.spacing_medium),
                vertical = dimensionResource(R.dimen.spacing_small)
            ),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_large)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (rank != null) {
            Text(
                modifier = Modifier.widthIn(min = 28.dp),
                text = rank.toString(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        AppIcon(
            modifier = Modifier.requiredSize(dimensionResource(R.dimen.icon_size_medium)),
            url = app.iconArtwork.url
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = app.displayName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (app.developerName.isNotBlank()) {
                Text(
                    text = app.developerName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(
                    dimensionResource(R.dimen.spacing_small)
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasRating(app.labeledRating)) RatingPill(rating = app.labeledRating)
                Text(
                    text = buildAppExtras(app),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun buildAppExtras(app: App): String = buildList {
    add(if (app.size > 0) CommonUtil.addSiPrefix(app.size) else app.downloadString)
    add(stringResource(if (app.isFree) R.string.details_free else R.string.details_paid))
    add(
        stringResource(
            if (app.containsAds) R.string.details_contains_ads else R.string.details_no_ads
        )
    )
    if (app.requiresGMS()) add(stringResource(R.string.details_gsf_dependent))
}.filter { it.isNotBlank() }.joinToString(separator = " • ")

@PreviewWrapper(ThemePreviewProvider::class)
@Preview(showBackground = true)
@Composable
fun LargeAppListItemPreview(@PreviewParameter(AppPreviewProvider::class) app: App) {
    LargeAppListItem(app = app)
}

@PreviewWrapper(ThemePreviewProvider::class)
@Preview(showBackground = true)
@Composable
private fun LargeAppListItemRankPreview(@PreviewParameter(AppPreviewProvider::class) app: App) {
    LargeAppListItem(app = app, rank = 1)
}
