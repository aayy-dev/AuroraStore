/*
 * SPDX-FileCopyrightText: 2026 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.composable.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.aurora.store.R
import com.aurora.store.compose.preview.ThemePreviewProvider

/**
 * Whether [rating] (as shown by [com.aurora.gplayapi.model.App.labeledRating]) is worth
 * displaying.
 */
fun hasRating(rating: String?): Boolean = !rating.isNullOrBlank() && rating != "0.0"

/**
 * Small "4.3 ★" pill used next to apps in lists, modeled on the Play Store.
 */
@Composable
fun RatingPill(
    rating: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = rating,
            style = style,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Icon(
            modifier = Modifier.size(12.dp),
            painter = painterResource(R.drawable.ic_star_filled),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@PreviewWrapper(ThemePreviewProvider::class)
@Preview(showBackground = true)
@Composable
private fun RatingPillPreview() {
    RatingPill(rating = "4.3")
}
