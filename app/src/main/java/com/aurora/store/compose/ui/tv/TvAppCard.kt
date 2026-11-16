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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme as M3Theme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.Surface
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.aurora.gplayapi.model.App
import com.aurora.store.R
import com.aurora.store.compose.composable.app.AppIcon
import com.aurora.store.compose.composable.app.hasRating
import com.aurora.store.compose.theme.TvBackground
import com.aurora.store.compose.theme.TvCard
import com.aurora.store.compose.theme.TvCardHigh
import com.aurora.store.compose.theme.TvDimens
import com.aurora.store.compose.theme.TvFocusedSurface
import com.aurora.store.compose.theme.TvOnFocusedSurface
import com.aurora.store.compose.theme.TvOnSurface
import com.aurora.store.compose.theme.TvOnSurfaceMuted

private val CardShape = RoundedCornerShape(8.dp)

/** Height reserved under each card for the focused app's name and rating. */
private val LabelHeight = 30.dp

/**
 * 16:9 app card for TV rows, modeled on the TV Play Store. It shows the app's feature graphic when
 * there is one, otherwise its icon and name. When focused it grows, gets a white outline and shows
 * the app name and rating underneath.
 */
@Composable
fun TvAppCard(app: App, onClick: () -> Unit, modifier: Modifier = Modifier, badge: String? = null) {
    var focused by remember { mutableStateOf(false) }

    Column(modifier = modifier.width(TvDimens.CardWidth)) {
        Surface(
            modifier = Modifier
                .size(TvDimens.CardWidth, TvDimens.CardHeight)
                .onFocusChanged { focused = it.isFocused },
            onClick = onClick,
            shape = ClickableSurfaceDefaults.shape(shape = CardShape),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = TvCardHigh,
                focusedContainerColor = TvCardHigh,
                pressedContainerColor = TvCardHigh
            ),
            scale = ClickableSurfaceDefaults.scale(focusedScale = TvDimens.FOCUSED_CARD_SCALE),
            border = ClickableSurfaceDefaults.border(
                focusedBorder = Border(
                    border = BorderStroke(3.dp, Color.White),
                    inset = 0.dp,
                    shape = CardShape
                )
            )
        ) {
            CardArt(app = app)
            if (badge != null) {
                Text(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .background(TvBackground.copy(alpha = 0.8f), CircleShape)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    text = badge,
                    style = M3Theme.typography.labelLarge,
                    color = TvOnSurface
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(LabelHeight)
                .padding(top = 10.dp)
        ) {
            if (focused) {
                Row(
                    // The label may run past the card, like on the TV Play Store
                    modifier = Modifier
                        .wrapContentWidth(Alignment.Start, unbounded = true)
                        .widthIn(max = TvDimens.CardWidth * 1.8f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(1f, fill = false),
                        text = app.displayName,
                        style = M3Theme.typography.bodyMedium,
                        color = TvOnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (hasRating(app.labeledRating)) {
                        Text(
                            text = "${app.labeledRating} ★",
                            style = M3Theme.typography.bodyMedium,
                            color = TvOnSurfaceMuted,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CardArt(app: App) {
    val coverUrl = app.coverArtwork.url
    if (coverUrl.isNotBlank()) {
        AsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = ImageRequest.Builder(LocalContext.current)
                .data(coverUrl)
                .crossfade(true)
                .build(),
            contentDescription = app.displayName,
            contentScale = ContentScale.Crop
        )
    } else {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AppIcon(url = app.iconArtwork.url, modifier = Modifier.size(44.dp))
            Text(
                text = app.displayName,
                style = M3Theme.typography.labelLarge,
                color = TvOnSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Last card of a row that opens the full list, sized like a [TvAppCard].
 */
@Composable
fun TvMoreCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.width(TvDimens.CardWidth)) {
        Surface(
            modifier = Modifier.size(TvDimens.CardWidth, TvDimens.CardHeight),
            onClick = onClick,
            shape = ClickableSurfaceDefaults.shape(shape = CardShape),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = TvCard,
                contentColor = TvOnSurface,
                focusedContainerColor = TvFocusedSurface,
                focusedContentColor = TvOnFocusedSurface,
                pressedContainerColor = TvFocusedSurface
            ),
            scale = ClickableSurfaceDefaults.scale(focusedScale = TvDimens.FOCUSED_CARD_SCALE)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.title_more),
                    style = M3Theme.typography.titleSmall,
                    color = LocalContentColor.current
                )
                Icon(
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .size(20.dp),
                    painter = painterResource(R.drawable.ic_arrow_forward),
                    contentDescription = null,
                    tint = LocalContentColor.current
                )
            }
        }
        Spacer(Modifier.height(LabelHeight))
    }
}
