/*
 * SPDX-FileCopyrightText: 2026 Aurora OSS
 * SPDX-FileCopyrightText: 2025 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.ui.details.composable

import android.text.format.Formatter
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.aurora.gplayapi.model.App
import com.aurora.store.R
import com.aurora.store.compose.composable.app.hasRating
import com.aurora.store.compose.preview.AppPreviewProvider
import com.aurora.store.compose.preview.ThemePreviewProvider
import com.aurora.store.util.CommonUtil

/**
 * A single entry of the stats row: either a short value (e.g. "4.3 ★", "10M+") or an icon on top,
 * with a label underneath.
 */
private data class Stat(
    val label: String,
    val value: String? = null,
    @DrawableRes val iconRes: Int? = null,
    val showStar: Boolean = false
)

/**
 * Play Store style row of app stats (rating, downloads, size, last update, price, ads),
 * separated by vertical dividers and horizontally scrollable.
 */
@Composable
fun Tags(app: App) {
    val context = LocalContext.current

    val stats = buildList {
        if (hasRating(app.labeledRating)) {
            add(
                Stat(
                    value = app.labeledRating,
                    showStar = true,
                    label = app.rating.abbreviatedLabel.takeIf { it.isNotBlank() }
                        ?.let { stringResource(R.string.details_stat_reviews, it) }
                        ?: stringResource(R.string.action_filter_rating)
                )
            )
        }
        if (app.installs > 0) {
            add(
                Stat(
                    value = CommonUtil.addDiPrefix(app.installs),
                    label = stringResource(R.string.action_filter_downloads)
                )
            )
        }
        if (app.size > 0) {
            add(
                Stat(
                    iconRes = R.drawable.ic_download_manager,
                    label = Formatter.formatShortFileSize(context, app.size)
                )
            )
        }
        if (app.updatedOn.isNotBlank()) {
            add(Stat(iconRes = R.drawable.ic_updates, label = app.updatedOn))
        }
        add(
            Stat(
                iconRes = R.drawable.ic_paid,
                label = stringResource(
                    if (app.isFree) R.string.details_free else R.string.details_paid
                )
            )
        )
        add(
            Stat(
                iconRes = R.drawable.ic_campaign,
                label = stringResource(
                    if (app.containsAds) R.string.details_contains_ads else R.string.details_no_ads
                )
            )
        )
    }

    LazyRow(
        contentPadding = PaddingValues(horizontal = dimensionResource(R.dimen.spacing_small)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(items = stats) { index, stat ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (index > 0) {
                    VerticalDivider(
                        modifier = Modifier.height(24.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
                StatItem(stat = stat)
            }
        }
    }
}

@Composable
private fun StatItem(stat: Stat) {
    Column(
        modifier = Modifier
            .widthIn(min = 96.dp)
            .padding(horizontal = dimensionResource(R.dimen.spacing_medium)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            modifier = Modifier.height(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            when {
                stat.value != null -> {
                    Text(
                        text = stat.value,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    if (stat.showStar) {
                        Icon(
                            modifier = Modifier.size(14.dp),
                            painter = painterResource(R.drawable.ic_star_filled),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                stat.iconRes != null -> Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(stat.iconRes),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Text(
            text = stat.label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@PreviewWrapper(ThemePreviewProvider::class)
@Preview(showBackground = true)
@Composable
private fun TagsPreview(@PreviewParameter(AppPreviewProvider::class) app: App) {
    Column(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium))
    ) {
        Tags(app = app)
    }
}
