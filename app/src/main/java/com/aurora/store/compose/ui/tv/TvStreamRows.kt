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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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

private const val LOAD_MORE_THRESHOLD = 2

/** Where the focused card settles below the top of the list, leaving room for its row title. */
private val RowPivot = 64.dp

/**
 * TV version of the home stream: one titled row of [TvAppCard]s per cluster, scrolled so the
 * focused row and card stay in a fixed place. Loads more clusters and more apps per row as the
 * user nears the end, like the phone stream.
 */
@Composable
fun TvStreamRows(
    streamBundle: StreamBundle?,
    modifier: Modifier = Modifier,
    onAppClick: (App) -> Unit = {},
    onClusterScrolled: (StreamCluster) -> Unit = {},
    onScrolledToEnd: () -> Unit = {}
) {
    val listState = rememberLazyListState()
    val bundleLoaded = streamBundle != null

    LaunchedEffect(listState, bundleLoaded) {
        snapshotFlow {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            last >= listState.layoutInfo.totalItemsCount - LOAD_MORE_THRESHOLD
        }.distinctUntilChanged().collect { reachedEnd ->
            if (reachedEnd && bundleLoaded) onScrolledToEnd()
        }
    }

    if (streamBundle == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val clusters = streamBundle.clusters
        .distinctBy { it.id }
        .map { cluster -> cluster.copy(apps = cluster.apps.distinctBy { it.packageName }) }
        .filter { it.apps.isNotEmpty() && it.title.isNotBlank() }

    if (clusters.isEmpty()) {
        Placeholder(
            modifier = modifier,
            painter = painterResource(R.drawable.ic_apps),
            message = stringResource(R.string.no_apps_available)
        )
        return
    }

    // Focus the last focused card (the first one on a fresh start) whenever the rows come back,
    // e.g. after returning from an app's details, so the remote picks up where it left off
    var focusedRow by rememberSaveable { mutableIntStateOf(0) }
    var focusedCard by rememberSaveable { mutableIntStateOf(0) }
    val restoreRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        // The card only attaches once the lists have laid out, so retry for a few frames
        repeat(10) {
            withFrameNanos { }
            if (runCatching { restoreRequester.requestFocus() }.getOrDefault(false)) {
                return@LaunchedEffect
            }
        }
    }

    TvPivot(leading = RowPivot) {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(items = clusters, key = { _, cluster -> cluster.id }) { index, cluster ->
                TvClusterRow(
                    cluster = cluster,
                    isFeatured = index == 0,
                    restoreCardIndex = if (index == focusedRow) focusedCard else null,
                    restoreRequester = restoreRequester,
                    onCardFocused = { card ->
                        focusedRow = index
                        focusedCard = card
                    },
                    onAppClick = onAppClick,
                    onClusterScrolled = onClusterScrolled
                )
            }
            if (streamBundle.hasNext()) {
                item(key = "loading_footer") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TvClusterRow(
    cluster: StreamCluster,
    isFeatured: Boolean,
    restoreCardIndex: Int?,
    restoreRequester: FocusRequester,
    onCardFocused: (Int) -> Unit,
    onAppClick: (App) -> Unit,
    onClusterScrolled: (StreamCluster) -> Unit
) {
    val rowState = rememberLazyListState()
    val reachedEnd by remember {
        derivedStateOf {
            val last = rowState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            last >= rowState.layoutInfo.totalItemsCount - LOAD_MORE_THRESHOLD
        }
    }
    LaunchedEffect(reachedEnd) {
        if (reachedEnd && cluster.hasNext()) onClusterScrolled(cluster)
    }

    Column {
        Text(
            modifier = Modifier.padding(
                start = TvDimens.ScreenPaddingHorizontal,
                end = TvDimens.ScreenPaddingHorizontal,
                top = 12.dp,
                bottom = 14.dp
            ),
            text = cluster.title,
            // The first row gets a large headline, later rows a quieter title (sampled sizes)
            style = if (isFeatured) {
                MaterialTheme.typography.headlineMedium
            } else {
                MaterialTheme.typography.titleMedium
            },
            color = if (isFeatured) TvOnSurface else TvSectionTitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        TvPivot(leading = TvDimens.ScreenPaddingHorizontal) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRestorer(),
                state = rowState,
                contentPadding = PaddingValues(horizontal = TvDimens.ScreenPaddingHorizontal),
                horizontalArrangement = Arrangement.spacedBy(TvDimens.CardSpacing)
            ) {
                itemsIndexed(
                    items = cluster.apps,
                    key = { _, app -> app.packageName }
                ) { index, app ->
                    TvAppCard(
                        modifier = Modifier
                            .then(
                                if (index == restoreCardIndex) {
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
            }
        }
    }
}
