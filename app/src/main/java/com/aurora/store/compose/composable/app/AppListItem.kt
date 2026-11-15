/*
 * SPDX-FileCopyrightText: 2025 The Calyx Institute
 * SPDX-FileCopyrightText: 2026 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.composable.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.aurora.gplayapi.model.App
import com.aurora.store.R
import com.aurora.store.compose.preview.AppPreviewProvider
import com.aurora.store.compose.preview.ThemePreviewProvider

/**
 * Composable for displaying minimal app details in a horizontal-scrollable list, modeled on the
 * Play Store's icon tiles: icon, up to two lines of title and a rating pill.
 * @param modifier The modifier to be applied to the composable
 * @param app [App] to display
 * @param onClick Callback when the composable is clicked
 * @see LargeAppListItem
 */
@Composable
fun AppListItem(modifier: Modifier = Modifier, app: App, onClick: () -> Unit = {}) {
    Column(
        modifier = modifier
            .width(dimensionResource(R.dimen.icon_size_large))
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(vertical = dimensionResource(R.dimen.spacing_xsmall)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_xsmall))
    ) {
        AppIcon(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            url = app.iconArtwork.url
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            text = app.displayName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        if (hasRating(app.labeledRating)) {
            RatingPill(rating = app.labeledRating, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@PreviewWrapper(ThemePreviewProvider::class)
@Preview(showBackground = true)
@Composable
private fun AppListItemPreview(@PreviewParameter(AppPreviewProvider::class) app: App) {
    AppListItem(app = app)
}
