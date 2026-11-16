/*
 * SPDX-FileCopyrightText: 2026 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.ui.tv

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.aurora.gplayapi.api.SearchSuggestion
import com.aurora.gplayapi.model.App
import com.aurora.store.R
import com.aurora.store.compose.composable.Placeholder
import com.aurora.store.compose.theme.TvBackground
import com.aurora.store.compose.theme.TvCard
import com.aurora.store.compose.theme.TvDimens
import com.aurora.store.compose.theme.TvOnSurface
import com.aurora.store.compose.theme.TvOnSurfaceMuted

/** Where the focused grid row settles below the top of the results. */
private val ResultsPivot = 16.dp

/**
 * TV search page, modeled on the TV Play Store: a pill-shaped search box at the top, then either
 * search suggestions (while typing) or the filters and a grid of result cards.
 */
@Composable
fun TvSearch(
    textFieldState: TextFieldState,
    suggestions: List<SearchSuggestion>,
    results: LazyPagingItems<App>,
    isSearching: Boolean,
    onSearch: (String) -> Unit,
    onAppClick: (App) -> Unit,
    filters: @Composable () -> Unit
) {
    // Start in the search box; once there are results (e.g. coming back from an app), the
    // results grid restores focus to the last focused card instead
    val fieldFocus = remember { FocusRequester() }
    if (!isSearching) RequestFocusOnce(fieldFocus)

    // Close the on-screen keyboard once a search runs so the results are visible
    val keyboard = LocalSoftwareKeyboardController.current
    val search: (String) -> Unit = { query ->
        keyboard?.hide()
        onSearch(query)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TvBackground)
            .padding(top = TvDimens.ScreenPaddingTop)
    ) {
        TvSearchField(
            modifier = Modifier
                .padding(horizontal = TvDimens.ScreenPaddingHorizontal)
                .focusRequester(fieldFocus),
            state = textFieldState,
            onSearch = { search(textFieldState.text.toString()) }
        )

        if (suggestions.isNotEmpty()) {
            TvPivot(leading = TvDimens.ScreenPaddingHorizontal, horizontal = true) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRestorer(),
                    contentPadding = PaddingValues(
                        horizontal = TvDimens.ScreenPaddingHorizontal - 16.dp,
                        vertical = 16.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(suggestions.distinctBy { it.title }, key = { it.title }) { suggestion ->
                        TvChip(
                            label = suggestion.title,
                            selected = false,
                            onClick = { search(suggestion.title) }
                        )
                    }
                }
            }
        } else {
            Box(modifier = Modifier.height(16.dp))
        }

        if (isSearching) {
            Box(
                modifier = Modifier.padding(horizontal = TvDimens.ScreenPaddingHorizontal - 16.dp)
            ) {
                filters()
            }
            TvSearchResults(results = results, onAppClick = onAppClick)
        }
    }
}

@Composable
private fun TvSearchField(
    state: TextFieldState,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    var focused by remember { mutableStateOf(false) }
    BasicTextField(
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focused = it.isFocused },
        state = state,
        lineLimits = TextFieldLineLimits.SingleLine,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        onKeyboardAction = { onSearch() },
        textStyle = MaterialTheme.typography.titleMedium.copy(color = TvOnSurface),
        cursorBrush = SolidColor(TvOnSurface),
        decorator = { innerTextField ->
            Row(
                modifier = Modifier
                    .height(52.dp)
                    .background(TvCard, CircleShape)
                    .border(
                        BorderStroke(2.dp, if (focused) Color.White else TvCard),
                        CircleShape
                    )
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(22.dp),
                    painter = painterResource(R.drawable.ic_round_search),
                    contentDescription = null,
                    tint = TvOnSurfaceMuted
                )
                Box(modifier = Modifier.weight(1f)) {
                    if (state.text.isEmpty()) {
                        Text(
                            text = stringResource(R.string.search_hint),
                            style = MaterialTheme.typography.titleMedium,
                            color = TvOnSurfaceMuted,
                            maxLines = 1
                        )
                    }
                    innerTextField()
                }
            }
        }
    )
}

@Composable
private fun TvSearchResults(results: LazyPagingItems<App>, onAppClick: (App) -> Unit) {
    when {
        results.loadState.refresh is LoadState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        results.loadState.refresh is LoadState.Error -> Placeholder(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(R.drawable.ic_refresh),
            message = stringResource(R.string.error),
            actionLabel = stringResource(R.string.action_retry),
            onAction = { results.retry() }
        )

        results.itemCount == 0 -> Placeholder(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(R.drawable.ic_disclaimer),
            message = stringResource(R.string.no_apps_available)
        )

        else -> TvPagedAppGrid(apps = results, onAppClick = onAppClick)
    }
}

/**
 * Grid of [TvAppCard]s for a paged list of apps (search results, "more" pages), restoring focus
 * to the last focused card when it comes back.
 */
@Composable
fun TvPagedAppGrid(
    apps: LazyPagingItems<App>,
    onAppClick: (App) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusMemory = rememberSaveable(
        saver = androidx.compose.runtime.saveable.Saver(
            save = { it.toList() },
            restore = { it.toIntArray() }
        )
    ) { IntArray(1) }
    val restoreRequester = remember { FocusRequester() }
    RequestFocusOnce(restoreRequester)

    TvPivot(leading = ResultsPivot) {
        LazyVerticalGrid(
            modifier = modifier
                .fillMaxSize()
                .focusRestorer(),
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
            items(count = apps.itemCount) { index ->
                apps[index]?.let { app ->
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
                        onClick = { onAppClick(app) }
                    )
                }
            }
        }
    }
}
