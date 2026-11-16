/*
 * SPDX-FileCopyrightText: 2026 Aurora OSS
 * SPDX-FileCopyrightText: 2025 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.ui.details

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aurora.extensions.adaptiveNavigationIcon
import com.aurora.extensions.isWindowCompact
import com.aurora.gplayapi.model.Artwork
import com.aurora.store.R
import com.aurora.store.compose.composable.TopAppBar
import com.aurora.store.compose.composable.details.ScreenshotListItem
import com.aurora.store.compose.preview.ThemePreviewProvider
import com.aurora.store.compose.ui.tv.RequestFocusOnce
import com.aurora.store.viewmodel.details.AppDetailsViewModel
import kotlinx.coroutines.launch

@Composable
fun ScreenshotScreen(
    packageName: String,
    index: Int,
    viewModel: AppDetailsViewModel = hiltViewModel(key = packageName),
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfoV2()
) {
    val app by viewModel.app.collectAsStateWithLifecycle()
    val screenshots = app?.screenshots ?: emptyList()

    val topAppBarTitle = when {
        windowAdaptiveInfo.isWindowCompact -> app!!.displayName
        else -> stringResource(R.string.details_screenshots)
    }

    ScreenContent(
        topAppBarTitle = topAppBarTitle,
        screenshots = screenshots.distinctBy { it.url },
        index = index
    )
}

@Composable
private fun ScreenContent(
    topAppBarTitle: String? = null,
    screenshots: List<Artwork> = emptyList(),
    index: Int = 0,
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfoV2()
) {
    val displayMetrics = LocalResources.current.displayMetrics
    val pagerState = rememberPagerState(initialPage = index) { screenshots.size }

    LaunchedEffect(key1 = index) {
        if (pagerState.currentPage != index) pagerState.scrollToPage(index)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = topAppBarTitle,
                navigationIcon = windowAdaptiveInfo.adaptiveNavigationIcon
            )
        }
    ) { paddingValues ->
        // The pager only pages by swiping, so let the remote's left/right page it too
        val scope = rememberCoroutineScope()
        val pagerFocus = remember { FocusRequester() }
        RequestFocusOnce(pagerFocus)
        HorizontalPager(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .focusRequester(pagerFocus)
                .onKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                    val step = when (event.key) {
                        Key.DirectionRight -> 1
                        Key.DirectionLeft -> -1
                        else -> return@onKeyEvent false
                    }
                    val target = (pagerState.currentPage + step)
                        .coerceIn(0, (screenshots.size - 1).coerceAtLeast(0))
                    scope.launch { pagerState.animateScrollToPage(target) }
                    true
                }
                .focusable(),
            state = pagerState,
            key = { screenshots[it].url }
        ) { page ->
            val artwork = screenshots[page]
            ScreenshotListItem(
                modifier = Modifier.fillMaxSize(),
                url = "${artwork.url}=rw-w${displayMetrics.widthPixels}-v1-e15"
            )
        }
    }
}

@PreviewWrapper(ThemePreviewProvider::class)
@Preview
@Composable
private fun ScreenshotScreenPreview() {
    ScreenContent(
        topAppBarTitle = stringResource(R.string.app_name)
    )
}
