/*
 * SPDX-FileCopyrightText: 2026 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.ui.tv

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aurora.gplayapi.model.App
import com.aurora.gplayapi.model.StreamBundle
import com.aurora.gplayapi.model.StreamCluster
import com.aurora.store.R
import com.aurora.store.compose.composable.Placeholder
import com.aurora.store.compose.theme.TvDimens
import com.aurora.store.compose.theme.TvOnSurface
import com.aurora.store.compose.theme.TvSectionTitle
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

private const val LOAD_MORE_THRESHOLD = 2

/** Where the focused card settles below the top of the list, leaving room for its row title. */
private val RowPivot = 64.dp

/** Where the focused grid row settles below the top of the grid. */
private val GridPivot = 16.dp

/**
 * Requests focus from [requester] once its target has been laid out, retrying for a few frames
 * because lazy items attach after the first composition. It stays out of the way while the user
 * is in the screen's header (see [LocalTvFocusGuard]), e.g. if content finishes loading after
 * they moved up to the top bar.
 */
@Composable
fun RequestFocusOnce(requester: FocusRequester) {
    val guard = LocalTvFocusGuard.current
    LaunchedEffect(requester) {
        repeat(10) {
            withFrameNanos { }
            if (guard.blocked) return@LaunchedEffect
            if (runCatching { requester.requestFocus() }.getOrDefault(false)) {
                return@LaunchedEffect
            }
        }
    }
}

/**
 * Remembers which item of a TV list had focus, across navigation and process death, without
 * triggering recomposition on every D-pad move (it's only read when the list comes back).
 */
@Composable
private fun rememberFocusMemory(size: Int): IntArray = rememberSaveable(
    saver = androidx.compose.runtime.saveable.Saver(
        save = { it.toList() },
        restore = { it.toIntArray() }
    )
) { IntArray(size) }

/**
 * Calls [onReachedEnd] whenever the user scrolls within [threshold] items of the end of a laid out
 * list, but never for an empty or not yet measured one.
 */
@Composable
private fun LoadMoreEffect(state: LazyListState, threshold: Int, onReachedEnd: () -> Unit) {
    val callback by rememberUpdatedState(onReachedEnd)
    LaunchedEffect(state) {
        snapshotFlow {
            val info = state.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: return@snapshotFlow false
            info.totalItemsCount > 0 && last >= info.totalItemsCount - threshold
        }.distinctUntilChanged().filter { it }.collect { callback() }
    }
}

/**
 * TV version of the home stream: one titled row of [TvAppCard]s per cluster, scrolled so the
 * focused row and card stay in a fixed place. Loads more clusters and more apps per row as the
 * user nears the end, like the phone stream. A single cluster is shown as a grid instead.
 */
@Composable
fun TvStreamRows(
    streamBundle: StreamBundle?,
    modifier: Modifier = Modifier,
    filterSingleAppClusters: Boolean = true,
    onAppClick: (App) -> Unit = {},
    onMoreClick: (StreamCluster) -> Unit = {},
    onClusterScrolled: (StreamCluster) -> Unit = {},
    onScrolledToEnd: () -> Unit = {}
) {
    val listState = rememberLazyListState()
    // Kept above the loading states so a brief reload doesn't forget the position
    val focusMemory = rememberFocusMemory(2)

    if (streamBundle == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val clusters = remember(streamBundle, filterSingleAppClusters) {
        streamBundle.clusters
            .distinctBy { it.id }
            .map { cluster -> cluster.copy(apps = cluster.apps.distinctBy { it.packageName }) }
            .filter { cluster ->
                cluster.apps.isNotEmpty() &&
                    cluster.title.isNotBlank() &&
                    (!filterSingleAppClusters || cluster.apps.size > 1)
            }
    }

    if (clusters.isEmpty()) {
        Placeholder(
            modifier = modifier,
            painter = painterResource(R.drawable.ic_apps),
            message = stringResource(R.string.no_apps_available)
        )
        return
    }

    // A single list (a category or a "more" page) reads better as a grid
    if (clusters.size == 1) {
        val cluster = clusters.first()
        TvAppGrid(
            modifier = modifier,
            apps = cluster.apps,
            hasMore = cluster.hasNext() || streamBundle.hasNext(),
            onAppClick = onAppClick,
            onLoadMore = {
                if (cluster.hasNext()) onClusterScrolled(cluster) else onScrolledToEnd()
            }
        )
        return
    }

    LoadMoreEffect(listState, LOAD_MORE_THRESHOLD) {
        if (streamBundle.hasNext()) onScrolledToEnd()
    }

    // Focus the last focused card (the first one on a fresh start) whenever the rows come back,
    // e.g. after returning from an app's details, so the remote picks up where it left off
    val restoreRequester = remember { FocusRequester() }
    RequestFocusOnce(restoreRequester)

    TvPivot(leading = RowPivot) {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(items = clusters, key = { _, cluster -> cluster.id }) { index, cluster ->
                TvAppRow(
                    title = cluster.title,
                    apps = cluster.apps,
                    isFeatured = index == 0,
                    hasMore = cluster.hasNext(),
                    onLoadMore = { onClusterScrolled(cluster) },
                    onMoreClick = if (cluster.browseUrl.isNotBlank()) {
                        { onMoreClick(cluster) }
                    } else {
                        null
                    },
                    restoreCardIndex = if (index == focusMemory[0]) focusMemory[1] else null,
                    restoreRequester = restoreRequester,
                    onCardFocused = { card ->
                        focusMemory[0] = index
                        focusMemory[1] = card
                    },
                    onAppClick = onAppClick
                )
            }
            if (streamBundle.hasNext()) {
                item(key = "loading_footer") { LoadingFooter() }
            }
        }
    }
}

/**
 * Titled row of [TvAppCard]s. Keeps the focused card at the row's start, asks for more apps
 * ([onLoadMore]) near the end, and can end with a "More" card that opens the full list.
 */
@Composable
fun TvAppRow(
    title: String,
    apps: List<App>,
    onAppClick: (App) -> Unit,
    modifier: Modifier = Modifier,
    isFeatured: Boolean = false,
    hasMore: Boolean = false,
    onLoadMore: () -> Unit = {},
    onMoreClick: (() -> Unit)? = null,
    restoreCardIndex: Int? = null,
    restoreRequester: FocusRequester? = null,
    onCardFocused: (Int) -> Unit = {}
) {
    val rowState = rememberLazyListState()
    LoadMoreEffect(rowState, LOAD_MORE_THRESHOLD) { if (hasMore) onLoadMore() }

    Column(modifier = modifier) {
        TvSectionTitle(title = title, isFeatured = isFeatured)

        TvPivot(leading = TvDimens.ScreenPaddingHorizontal, horizontal = true) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRestorer(),
                state = rowState,
                contentPadding = PaddingValues(horizontal = TvDimens.ScreenPaddingHorizontal),
                horizontalArrangement = Arrangement.spacedBy(TvDimens.CardSpacing)
            ) {
                val rowApps = apps.distinctBy { it.packageName }
                val itemCount = rowApps.size + if (onMoreClick != null) 1 else 0
                itemsIndexed(
                    items = rowApps,
                    key = { _, app -> app.packageName }
                ) { index, app ->
                    TvAppCard(
                        modifier = Modifier
                            .tvRowEnds(index, itemCount)
                            .then(
                                if (index == restoreCardIndex && restoreRequester != null) {
                                    Modifier.focusRequester(restoreRequester)
                                } else {
                                    Modifier
                                }
                            )
                            .onFocusChanged { if (it.hasFocus) onCardFocused(index) },
                        app = app,
                        onClick = { onAppClick(app) }
                    )
                }
                if (onMoreClick != null) {
                    item(key = "more") {
                        TvMoreCard(
                            modifier = Modifier.tvRowEnds(itemCount - 1, itemCount),
                            onClick = onMoreClick
                        )
                    }
                }
            }
        }
    }
}

/**
 * Row or section title on TV: a large headline for the first (featured) row, a quieter grey title
 * for the rest, with sizes sampled from the TV Play Store.
 */
@Composable
fun TvSectionTitle(title: String, modifier: Modifier = Modifier, isFeatured: Boolean = false) {
    Text(
        modifier = modifier.padding(
            start = TvDimens.ScreenPaddingHorizontal,
            end = TvDimens.ScreenPaddingHorizontal,
            top = 12.dp,
            bottom = 14.dp
        ),
        text = title,
        style = if (isFeatured) {
            MaterialTheme.typography.headlineMedium
        } else {
            MaterialTheme.typography.titleMedium
        },
        color = if (isFeatured) TvOnSurface else TvSectionTitle,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

/**
 * Grid of [TvAppCard]s for a single list of apps, loading more near the end. [badge] can label
 * cards (e.g. chart positions).
 */
@Composable
fun TvAppGrid(
    apps: List<App>,
    onAppClick: (App) -> Unit,
    modifier: Modifier = Modifier,
    hasMore: Boolean = false,
    onLoadMore: () -> Unit = {},
    badge: ((index: Int) -> String?)? = null
) {
    val gridState = rememberLazyGridState()
    val distinctApps = remember(apps) { apps.distinctBy { it.packageName } }
    GridLoadMoreEffect(gridState) { if (hasMore) onLoadMore() }

    // Focus the last focused card (the first one on a fresh start)
    val focusMemory = rememberFocusMemory(1)
    val restoreRequester = remember { FocusRequester() }
    RequestFocusOnce(restoreRequester)

    TvPivot(leading = GridPivot) {
        LazyVerticalGrid(
            modifier = modifier
                .fillMaxSize()
                .focusRestorer(),
            state = gridState,
            columns = GridCells.Fixed(TvDimens.GRID_COLUMNS),
            contentPadding = PaddingValues(
                start = TvDimens.ScreenPaddingHorizontal,
                end = TvDimens.ScreenPaddingHorizontal,
                top = 16.dp,
                bottom = 48.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(TvDimens.CardSpacing),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            gridItemsIndexed(
                items = distinctApps,
                key = { _, app -> app.packageName }
            ) { index, app ->
                TvAppCard(
                    modifier = Modifier
                        .then(
                            if (index == focusMemory[0]) {
                                Modifier.focusRequester(restoreRequester)
                            } else {
                                Modifier
                            }
                        )
                        .onFocusChanged { if (it.hasFocus) focusMemory[0] = index },
                    app = app,
                    badge = badge?.invoke(index),
                    onClick = { onAppClick(app) }
                )
            }
            if (hasMore) {
                item(key = "loading", span = { GridItemSpan(maxLineSpan) }) { LoadingFooter() }
            }
        }
    }
}

/**
 * Grid counterpart of [LoadMoreEffect].
 */
@Composable
internal fun GridLoadMoreEffect(state: LazyGridState, onReachedEnd: () -> Unit) {
    val callback by rememberUpdatedState(onReachedEnd)
    LaunchedEffect(state) {
        snapshotFlow {
            val info = state.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: return@snapshotFlow false
            info.totalItemsCount > 0 && last >= info.totalItemsCount - TvDimens.GRID_COLUMNS
        }.distinctUntilChanged().filter { it }.collect { callback() }
    }
}

@Composable
private fun LoadingFooter() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(32.dp))
    }
}
