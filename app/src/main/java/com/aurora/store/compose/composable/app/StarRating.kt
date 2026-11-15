/*
 * SPDX-FileCopyrightText: 2026 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.composable.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aurora.store.R
import com.aurora.store.compose.preview.ThemePreviewProvider
import kotlin.math.roundToInt

/**
 * Row of five stars filled up to [rating] (rounded to the nearest whole star), as shown next to
 * ratings and reviews on the Play Store.
 */
@Composable
fun StarRating(rating: Float, modifier: Modifier = Modifier, starSize: Dp = 14.dp) {
    val filled = rating.roundToInt().coerceIn(0, 5)
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(1.dp)) {
        repeat(5) { index ->
            Icon(
                modifier = Modifier.size(starSize),
                painter = painterResource(R.drawable.ic_star_filled),
                contentDescription = null,
                tint = if (index < filled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHighest
                }
            )
        }
    }
}

@PreviewWrapper(ThemePreviewProvider::class)
@Preview(showBackground = true)
@Composable
private fun StarRatingPreview() {
    StarRating(rating = 3.8f)
}
