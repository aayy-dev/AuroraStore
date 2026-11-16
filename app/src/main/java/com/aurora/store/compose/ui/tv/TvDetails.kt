/*
 * SPDX-FileCopyrightText: 2026 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.ui.tv

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.Surface
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.aurora.gplayapi.model.App
import com.aurora.gplayapi.model.Artwork
import com.aurora.gplayapi.model.Rating
import com.aurora.gplayapi.model.Review
import com.aurora.store.compose.composable.app.AppIcon
import com.aurora.store.compose.composable.app.StarRating
import com.aurora.store.compose.composable.app.hasRating
import com.aurora.store.compose.theme.TvCard
import com.aurora.store.compose.theme.TvCardHigh
import com.aurora.store.compose.theme.TvDimens
import com.aurora.store.compose.theme.TvFocusedSurface
import com.aurora.store.compose.theme.TvOnFocusedSurface
import com.aurora.store.compose.theme.TvOnSurface
import com.aurora.store.compose.theme.TvOnSurfaceMuted

private val ArtShape = RoundedCornerShape(12.dp)
private val TileShape = RoundedCornerShape(12.dp)

/** Banner on the right of the details header (16:9). */
private val BannerWidth = 320.dp
private val BannerHeight = 180.dp

/** Height of screenshot thumbnails; their width follows each image's aspect ratio. */
private val ScreenshotHeight = 160.dp

/**
 * Top of the TV app details page, modeled on the TV Play Store: the app name, a line with
 * developer, category and content rating, the star rating, a two-line description and the
 * actions on the left, and the app's feature graphic (or icon) on the right.
 */
@Composable
fun TvDetailsHeader(
    app: App,
    actions: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    status: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = TvDimens.ScreenPaddingHorizontal,
                end = TvDimens.ScreenPaddingHorizontal,
                top = 36.dp,
                bottom = 16.dp
            ),
        horizontalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = app.displayName,
                style = MaterialTheme.typography.headlineLarge,
                color = TvOnSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = listOf(app.developerName, app.categoryName, app.contentRating.title)
                    .filter { it.isNotBlank() }
                    .joinToString("  •  "),
                style = MaterialTheme.typography.bodyLarge,
                color = TvOnSurfaceMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (hasRating(app.labeledRating)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StarRating(rating = app.rating.average, starSize = 16.dp)
                    Text(
                        text = app.labeledRating,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TvOnSurfaceMuted
                    )
                }
            }
            if (app.shortDescription.isNotBlank()) {
                Text(
                    text = app.shortDescription,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TvOnSurfaceMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (status != null) {
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TvOnSurfaceMuted,
                    maxLines = 1
                )
            }
            Box(modifier = Modifier.padding(top = 12.dp)) { actions() }
        }

        Box(
            modifier = Modifier
                .size(BannerWidth, BannerHeight)
                .clip(ArtShape)
                .background(TvCardHigh),
            contentAlignment = Alignment.Center
        ) {
            if (app.coverArtwork.url.isNotBlank()) {
                AsyncImage(
                    modifier = Modifier.fillMaxSize(),
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(app.coverArtwork.url)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            } else {
                AppIcon(url = app.iconArtwork.url, modifier = Modifier.size(96.dp))
            }
        }
    }
}

/**
 * Row of screenshot thumbnails that open the full-screen viewer.
 */
@Composable
fun TvScreenshotsRow(
    title: String,
    screenshots: List<Artwork>,
    onClick: (index: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (screenshots.isEmpty()) return
    Column(modifier = modifier) {
        TvSectionTitle(title = title)
        TvPivot(leading = TvDimens.ScreenPaddingHorizontal) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRestorer(),
                contentPadding = PaddingValues(
                    start = TvDimens.ScreenPaddingHorizontal,
                    end = TvDimens.ScreenPaddingHorizontal,
                    bottom = 16.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(TvDimens.CardSpacing)
            ) {
                itemsIndexed(screenshots) { index, artwork ->
                    val ratio = if (artwork.width > 0 && artwork.height > 0) {
                        artwork.width.toFloat() / artwork.height
                    } else {
                        9f / 16f
                    }
                    TvTile(
                        width = ScreenshotHeight * ratio.coerceIn(0.4f, 2f),
                        height = ScreenshotHeight,
                        onClick = { onClick(index) }
                    ) {
                        AsyncImage(
                            modifier = Modifier.fillMaxSize(),
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(artwork.url)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

/**
 * Ratings summary card followed by featured review cards; each opens the full reviews.
 */
@Composable
fun TvRatingsRow(
    title: String,
    rating: Rating,
    reviews: List<Review>,
    ratingsCountLabel: String,
    onOpenReviews: () -> Unit,
    modifier: Modifier = Modifier
) {
    val total = listOf(
        rating.oneStar,
        rating.twoStar,
        rating.threeStar,
        rating.fourStar,
        rating.fiveStar
    ).sumOf { it.toLong() }
    if (total <= 0L && reviews.isEmpty()) return

    Column(modifier = modifier) {
        TvSectionTitle(title = title)
        TvPivot(leading = TvDimens.ScreenPaddingHorizontal) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRestorer(),
                contentPadding = PaddingValues(
                    start = TvDimens.ScreenPaddingHorizontal,
                    end = TvDimens.ScreenPaddingHorizontal,
                    bottom = 16.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(TvDimens.CardSpacing)
            ) {
                if (total > 0L) {
                    item(key = "summary") {
                        TvTile(width = 300.dp, height = 150.dp, onClick = onOpenReviews) {
                            RatingSummary(rating = rating, total = total, label = ratingsCountLabel)
                        }
                    }
                }
                items(reviews, key = { it.commentId.ifBlank { it.userName + it.timeStamp } }) {
                    TvTile(width = 300.dp, height = 150.dp, onClick = onOpenReviews) {
                        ReviewTile(review = it)
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingSummary(rating: Rating, total: Long, label: String) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = String.format(LocalLocale.current.platformLocale, "%.1f", rating.average),
                style = MaterialTheme.typography.displaySmall,
                color = LocalContentColor.current
            )
            StarRating(
                rating = rating.average,
                starSize = 14.dp,
                color = LocalContentColor.current,
                emptyColor = LocalContentColor.current.copy(alpha = 0.3f)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = LocalContentColor.current,
                maxLines = 1
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                5 to rating.fiveStar,
                4 to rating.fourStar,
                3 to rating.threeStar,
                2 to rating.twoStar,
                1 to rating.oneStar
            ).forEach { (stars, count) ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$stars",
                        style = MaterialTheme.typography.labelSmall,
                        color = LocalContentColor.current
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(LocalContentColor.current.copy(alpha = 0.2f))
                    ) {
                        val share = (count.toFloat() / total).coerceIn(0f, 1f)
                        if (share > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(share)
                                    .height(4.dp)
                                    .clip(CircleShape)
                                    .background(LocalContentColor.current)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewTile(review: Review) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
                model = ImageRequest.Builder(LocalContext.current)
                    .data(review.userPhotoUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
            Column {
                Text(
                    text = review.userName,
                    style = MaterialTheme.typography.titleSmall,
                    color = LocalContentColor.current,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                StarRating(
                    rating = review.rating.toFloat(),
                    starSize = 12.dp,
                    color = LocalContentColor.current,
                    emptyColor = LocalContentColor.current.copy(alpha = 0.3f)
                )
            }
        }
        Text(
            text = review.comment,
            style = MaterialTheme.typography.bodySmall,
            color = LocalContentColor.current,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * One entry of [TvInfoRow].
 */
data class TvInfo(val title: String, val subtitle: String, val onClick: () -> Unit)

/**
 * Row of focusable cards that open the details' secondary pages (about, permissions, privacy,
 * developer and so on).
 */
@Composable
fun TvInfoRow(title: String, items: List<TvInfo>, modifier: Modifier = Modifier) {
    if (items.isEmpty()) return
    Column(modifier = modifier) {
        TvSectionTitle(title = title)
        TvPivot(leading = TvDimens.ScreenPaddingHorizontal) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRestorer(),
                contentPadding = PaddingValues(
                    start = TvDimens.ScreenPaddingHorizontal,
                    end = TvDimens.ScreenPaddingHorizontal,
                    bottom = 16.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(TvDimens.CardSpacing)
            ) {
                items(items, key = { it.title }) { info ->
                    TvTile(width = 240.dp, height = 104.dp, onClick = info.onClick) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = info.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = LocalContentColor.current,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = info.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = LocalContentColor.current.copy(alpha = 0.8f),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Focusable dark card used on TV for screenshots, reviews and info: light with dark content and a
 * slight zoom while focused.
 */
@Composable
fun TvTile(
    width: Dp,
    height: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .width(width)
            .height(height),
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape = TileShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = TvCard,
            contentColor = TvOnSurface,
            focusedContainerColor = TvFocusedSurface,
            focusedContentColor = TvOnFocusedSurface,
            pressedContainerColor = TvFocusedSurface,
            pressedContentColor = TvOnFocusedSurface
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(2.dp, Color.White),
                shape = TileShape
            )
        )
    ) {
        content()
    }
}
