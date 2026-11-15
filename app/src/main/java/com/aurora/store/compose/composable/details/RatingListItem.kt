/*
 * SPDX-FileCopyrightText: 2025 The Calyx Institute
 * SPDX-FileCopyrightText: 2026 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.composable.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.aurora.store.R
import com.aurora.store.compose.preview.ThemePreviewProvider
import com.aurora.store.compose.theme.ratingBarTrackColor

/**
 * Composable to display a single star-rating distribution bar, modeled on the Play Store
 * @param modifier The modifier to be applied to the composable
 * @param label Label for the bar (number of stars)
 * @param rating Share of ratings for this bar, from 0 to 1
 */
@Composable
fun RatingListItem(modifier: Modifier = Modifier, label: String, rating: Float) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.spacing_small)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium))
    ) {
        Text(
            modifier = Modifier.widthIn(min = 8.dp),
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(CircleShape)
                .background(ratingBarTrackColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(rating.coerceIn(0f, 1f).coerceAtLeast(0.03f))
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@PreviewWrapper(ThemePreviewProvider::class)
@Preview(showBackground = true)
@Composable
private fun RatingListItemPreview() {
    RatingListItem(label = "5", rating = 0.5F)
}
