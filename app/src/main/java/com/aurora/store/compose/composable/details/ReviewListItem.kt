/*
 * SPDX-FileCopyrightText: 2025 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.composable.details

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.aurora.gplayapi.model.Review
import com.aurora.store.R
import com.aurora.store.compose.composable.app.StarRating
import com.aurora.store.compose.preview.ReviewPreviewProvider
import com.aurora.store.compose.preview.ThemePreviewProvider

/**
 * Composable for viewing a review about an app
 * @param modifier The modifier to be applied to the composable
 * @param review [Review] about an app
 */
@Composable
fun ReviewListItem(modifier: Modifier = Modifier, review: Review, showReply: Boolean = false) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(R.dimen.spacing_medium),
                vertical = dimensionResource(R.dimen.spacing_small)
            )
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(review.userPhotoUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .requiredSize(40.dp)
                .clip(CircleShape)
        )
        Column(
            modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.spacing_medium)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_xsmall))
        ) {
            Text(
                text = review.userName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(
                    dimensionResource(R.dimen.spacing_small)
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StarRating(rating = review.rating.toFloat(), starSize = 12.dp)
                Text(
                    text = DateUtils.formatDateTime(
                        LocalContext.current,
                        review.timeStamp,
                        DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = review.comment,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                overflow = TextOverflow.Ellipsis
            )
            if (showReply && review.replyText.isNotBlank()) {
                DeveloperReply(
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.spacing_small)),
                    text = review.replyText,
                    timeStamp = review.replyTimeStamp
                )
            }
        }
    }
}

@Composable
private fun DeveloperReply(modifier: Modifier = Modifier, text: String, timeStamp: Long) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(dimensionResource(R.dimen.spacing_medium))
    ) {
        Text(
            text = stringResource(R.string.review_developer_reply),
            style = MaterialTheme.typography.labelMedium
        )
        if (timeStamp > 0) {
            Text(
                text = DateUtils.formatDateTime(
                    LocalContext.current,
                    timeStamp,
                    DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@PreviewWrapper(ThemePreviewProvider::class)
@Preview(showBackground = true)
@Composable
private fun ReviewListItemPreview(@PreviewParameter(ReviewPreviewProvider::class) review: Review) {
    ReviewListItem(review = review)
}
