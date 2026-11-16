/*
 * SPDX-FileCopyrightText: 2026 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.ui.tv

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.Surface
import com.aurora.store.compose.theme.TvFocusedSurface
import com.aurora.store.compose.theme.TvOnFocusedSurface
import com.aurora.store.compose.theme.TvOnSurface
import com.aurora.store.compose.theme.TvOnSurfaceMuted
import com.aurora.store.compose.theme.TvSelectedPill

/**
 * Pill used for filters and sub-pages on TV: transparent when idle, dark grey when selected and
 * light with dark text while focused, matching the TV Play Store's top bar pills.
 */
@Composable
fun TvChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .heightIn(min = 36.dp)
            .semantics {
                this.selected = selected
                role = Role.Tab
            },
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape = CircleShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) TvSelectedPill else Color.Transparent,
            contentColor = if (selected) TvOnSurface else TvOnSurfaceMuted,
            focusedContainerColor = TvFocusedSurface,
            focusedContentColor = TvOnFocusedSurface,
            pressedContainerColor = TvFocusedSurface,
            pressedContentColor = TvOnFocusedSurface
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f)
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = LocalContentColor.current,
            maxLines = 1
        )
    }
}
