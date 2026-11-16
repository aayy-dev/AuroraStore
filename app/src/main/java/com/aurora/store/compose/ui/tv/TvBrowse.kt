/*
 * SPDX-FileCopyrightText: 2026 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.ui.tv

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.LocalContentColor
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.aurora.gplayapi.model.App
import com.aurora.gplayapi.model.Category
import com.aurora.gplayapi.model.StreamCluster
import com.aurora.store.R
import com.aurora.store.compose.composable.Placeholder
import com.aurora.store.compose.theme.TvDimens
import com.aurora.store.data.model.ViewState

private const val CATEGORY_COLUMNS = 4

/**
 * TV layout of the top charts: a row of chart pills above a grid of numbered app cards.
 */
@Composable
fun TvTopCharts(
    selectedIndex: Int,
    chartTitles: List<Int>,
    state: ViewState,
    cluster: StreamCluster?,
    onTabSelected: (Int) -> Unit,
    onRetry: () -> Unit,
    onAppClick: (App) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .focusRestorer()
                .padding(horizontal = TvDimens.ScreenPaddingHorizontal - 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            chartTitles.forEachIndexed { index, titleRes ->
                TvChip(
                    label = stringResource(titleRes),
                    selected = index == selectedIndex,
                    onClick = { onTabSelected(index) }
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when {
                state is ViewState.Error -> Placeholder(
                    modifier = Modifier.fillMaxSize(),
                    painter = painterResource(R.drawable.ic_apps),
                    message = stringResource(R.string.no_apps_available),
                    actionLabel = stringResource(R.string.action_retry),
                    onAction = onRetry
                )

                cluster == null -> Loading()

                cluster.apps.isEmpty() -> Placeholder(
                    modifier = Modifier.fillMaxSize(),
                    painter = painterResource(R.drawable.ic_apps),
                    message = stringResource(R.string.no_apps_available)
                )

                else -> TvAppGrid(
                    apps = cluster.apps,
                    badge = { index -> "${index + 1}" },
                    onAppClick = onAppClick
                )
            }
        }
    }
}

/**
 * TV layout of the categories: a grid of focusable tiles with each category's icon and name.
 */
@Composable
fun TvCategories(list: List<Category>?, onCategoryClick: (Category) -> Unit) {
    if (list == null) {
        Loading()
        return
    }

    var focusedIndex by rememberSaveable { mutableIntStateOf(0) }
    val restoreRequester = remember { FocusRequester() }
    RequestFocusOnce(restoreRequester)

    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxSize()
            .focusRestorer(),
        columns = GridCells.Fixed(CATEGORY_COLUMNS),
        contentPadding = PaddingValues(
            start = TvDimens.ScreenPaddingHorizontal,
            end = TvDimens.ScreenPaddingHorizontal,
            top = 16.dp,
            bottom = 48.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        itemsIndexed(items = list, key = { _, category -> category.title }) { index, category ->
            TvTile(
                modifier = Modifier
                    .then(
                        if (index == focusedIndex) {
                            Modifier.focusRequester(restoreRequester)
                        } else {
                            Modifier
                        }
                    )
                    .onFocusChanged { if (it.hasFocus) focusedIndex = index },
                width = 200.dp,
                height = 64.dp,
                onClick = { onCategoryClick(category) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        modifier = Modifier.size(24.dp),
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(category.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(LocalContentColor.current)
                    )
                    Text(
                        text = category.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = LocalContentColor.current,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun Loading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
