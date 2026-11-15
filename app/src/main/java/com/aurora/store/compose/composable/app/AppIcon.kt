/*
 * SPDX-FileCopyrightText: 2026 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.composable.app

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.aurora.store.compose.preview.ThemePreviewProvider
import com.aurora.store.compose.theme.AppIconShape

/**
 * App icon clipped to the store's rounded-square [AppIconShape], with a hairline outline so light
 * icons stay distinguishable from the background (as on the Play Store).
 *
 * @param url Icon URL to load
 * @param modifier Modifier; callers set the size
 */
@Composable
fun AppIcon(url: String?, modifier: Modifier = Modifier) {
    AsyncImage(
        modifier = modifier
            .clip(AppIconShape)
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                shape = AppIconShape
            ),
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(true)
            .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop
    )
}

@PreviewWrapper(ThemePreviewProvider::class)
@Preview(showBackground = true)
@Composable
private fun AppIconPreview() {
    AppIcon(url = null, modifier = Modifier.requiredSize(56.dp))
}
