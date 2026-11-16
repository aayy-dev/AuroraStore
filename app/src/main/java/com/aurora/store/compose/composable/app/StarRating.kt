/*
 * SPDX-FileCopyrightText: 2026 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.composable.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.aurora.store.R
import com.aurora.store.compose.preview.ThemePreviewProvider
import kotlin.math.roundToInt

/**
 * Row of five stars filled up to [rating] in half-star steps, as shown next to ratings and
 * reviews on the Play Store. Read by accessibility services as "Rated x out of 5 stars".
 */
@Composable
fun StarRating(rating: Float, modifier: Modifier = Modifier, starSize: Dp = 14.dp) {
    val safeRating = if (rating.isNaN()) 0f else rating.coerceIn(0f, 5f)
    val halfSteps = (safeRating * 2).roundToInt()
    val description = stringResource(
        R.string.rating_content_description,
        String.format(LocalLocale.current.platformLocale, "%.1f", safeRating)
    )

    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        repeat(5) { index ->
            val fill = ((halfSteps - index * 2).coerceIn(0, 2)) / 2f
            Box(modifier = Modifier.size(starSize)) {
                Icon(
                    modifier = Modifier.size(starSize),
                    painter = painterResource(R.drawable.ic_star_filled),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline
                )
                if (fill > 0f) {
                    Icon(
                        modifier = Modifier
                            .size(starSize)
                            .drawWithContent {
                                val width = size.width * fill
                                if (layoutDirection == LayoutDirection.Rtl) {
                                    clipRect(left = size.width - width) {
                                        this@drawWithContent.drawContent()
                                    }
                                } else {
                                    clipRect(right = width) {
                                        this@drawWithContent.drawContent()
                                    }
                                }
                            },
                        painter = painterResource(R.drawable.ic_star_filled),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@PreviewWrapper(ThemePreviewProvider::class)
@Preview(showBackground = true)
@Composable
private fun StarRatingPreview() {
    StarRating(rating = 3.6f)
}
