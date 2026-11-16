/*
 * SPDX-FileCopyrightText: 2026 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.ui.apps

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.aurora.gplayapi.api.TopChartType
import com.aurora.gplayapi.model.ContentType
import com.aurora.store.R
import com.aurora.store.compose.composable.StoreChip
import com.aurora.store.compose.composition.LocalUI
import com.aurora.store.compose.composition.UI
import com.aurora.store.compose.navigation.Destination
import com.aurora.store.compose.theme.TvDimens
import com.aurora.store.compose.ui.tv.TvChip
import com.aurora.store.compose.ui.tv.tvHeader
import com.aurora.store.compose.ui.tv.tvRowEnds
import com.aurora.store.util.Preferences
import com.aurora.store.viewmodel.category.CategoryViewModel
import com.aurora.store.viewmodel.homestream.StreamViewModel
import com.aurora.store.viewmodel.topchart.TopChartViewModel
import kotlinx.coroutines.launch

internal fun category(pageType: Int): ContentType =
    if (pageType == 1) ContentType.GAME else ContentType.APPLICATION

private enum class AppsTab(@StringRes val titleRes: Int, @DrawableRes val iconRes: Int) {
    FOR_YOU(R.string.tab_for_you, R.drawable.ic_chip_for_you),
    TOP_CHARTS(R.string.tab_top_charts, R.drawable.ic_chip_top_charts),
    CATEGORIES(R.string.tab_categories, R.drawable.ic_chip_categories)
}

@Composable
fun AppsGamesScreen(
    pageType: Int,
    streamViewModel: StreamViewModel = hiltViewModel(key = "stream_$pageType"),
    topChartViewModel: TopChartViewModel = hiltViewModel(key = "topChart_$pageType"),
    categoryViewModel: CategoryViewModel = hiltViewModel(key = "category_$pageType"),
    onNavigateTo: (Destination) -> Unit = {}
) {
    val context = LocalContext.current

    // Prefetch once per screen: on TV this screen leaves composition on every section switch, and
    // a second prefetch could land after (and overwrite) the chart the user picked
    var topChartPrefetched by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!topChartPrefetched) {
            topChartPrefetched = true
            topChartViewModel.getStreamCluster(category(pageType), TopChartType.TOP_SELLING_FREE)
        }
    }

    val isForYouEnabled = Preferences.getBoolean(context, Preferences.PREFERENCE_FOR_YOU)
    val tabs = buildList {
        if (isForYouEnabled) add(AppsTab.FOR_YOU)
        add(AppsTab.TOP_CHARTS)
        add(AppsTab.CATEGORIES)
    }
    if (LocalUI.current == UI.TV) {
        TvAppsGamesContent(
            pageType = pageType,
            tabs = tabs,
            streamViewModel = streamViewModel,
            topChartViewModel = topChartViewModel,
            categoryViewModel = categoryViewModel,
            onNavigateTo = onNavigateTo
        )
        return
    }

    val pagerState = rememberPagerState { tabs.size }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(
                    horizontal = dimensionResource(R.dimen.spacing_medium),
                    vertical = dimensionResource(R.dimen.spacing_small)
                ),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
        ) {
            tabs.forEachIndexed { index, tab ->
                StoreChip(
                    label = stringResource(tab.titleRes),
                    iconRes = tab.iconRes,
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    }
                )
            }
        }

        HorizontalPager(
            modifier = Modifier.fillMaxSize(),
            state = pagerState,
            verticalAlignment = Alignment.Top,
            userScrollEnabled = false
        ) { page ->
            when (tabs[page]) {
                AppsTab.FOR_YOU -> ForYouContent(
                    pageType = pageType,
                    viewModel = streamViewModel,
                    onAppClick = { onNavigateTo(Destination.AppDetails(it.packageName)) },
                    onHeaderClick = { onNavigateTo(Destination.StreamBrowse(it)) },
                    onClusterScrolled = { cluster ->
                        streamViewModel.observeCluster(category(pageType), cluster)
                    },
                    onScrolledToEnd = {
                        streamViewModel.observe(category(pageType))
                    }
                )

                AppsTab.TOP_CHARTS -> TopChartsContent(
                    pageType = pageType,
                    viewModel = topChartViewModel,
                    onAppClick = { onNavigateTo(Destination.AppDetails(it.packageName)) }
                )

                AppsTab.CATEGORIES -> CategoriesContent(
                    pageType = pageType,
                    viewModel = categoryViewModel,
                    onCategoryClick = { onNavigateTo(Destination.CategoryBrowse(it)) }
                )
            }
        }
    }
}

/**
 * TV layout of the Apps and Games pages: a row of pills for the sub-pages (For you, Top charts,
 * Categories) above the selected page.
 */
@Composable
private fun TvAppsGamesContent(
    pageType: Int,
    tabs: List<AppsTab>,
    streamViewModel: StreamViewModel,
    topChartViewModel: TopChartViewModel,
    categoryViewModel: CategoryViewModel,
    onNavigateTo: (Destination) -> Unit
) {
    // Saved by name, so turning the For you page on or off doesn't shift the selection
    var selectedName by rememberSaveable { mutableStateOf(tabs.first().name) }
    val tab = tabs.firstOrNull { it.name == selectedName } ?: tabs.first()
    val saveableStateHolder = rememberSaveableStateHolder()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .tvHeader()
                .focusRestorer()
                .padding(horizontal = TvDimens.ScreenPaddingHorizontal - 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
        ) {
            tabs.forEachIndexed { index, item ->
                TvChip(
                    modifier = Modifier.tvRowEnds(index, tabs.size),
                    label = stringResource(item.titleRes),
                    selected = item == tab,
                    onClick = { selectedName = item.name }
                )
            }
        }

        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            saveableStateHolder.SaveableStateProvider(tab.name) {
                when (tab) {
                    AppsTab.FOR_YOU -> TvForYouContent(
                        pageType = pageType,
                        viewModel = streamViewModel,
                        onAppClick = { onNavigateTo(Destination.AppDetails(it.packageName)) },
                        onMoreClick = { onNavigateTo(Destination.StreamBrowse(it)) },
                        onClusterScrolled = { cluster ->
                            streamViewModel.observeCluster(category(pageType), cluster)
                        },
                        onScrolledToEnd = { streamViewModel.observe(category(pageType)) }
                    )

                    AppsTab.TOP_CHARTS -> TopChartsContent(
                        pageType = pageType,
                        viewModel = topChartViewModel,
                        onAppClick = { onNavigateTo(Destination.AppDetails(it.packageName)) }
                    )

                    AppsTab.CATEGORIES -> CategoriesContent(
                        pageType = pageType,
                        viewModel = categoryViewModel,
                        onCategoryClick = { onNavigateTo(Destination.CategoryBrowse(it)) }
                    )
                }
            }
        }
    }
}
