/*
 * SPDX-FileCopyrightText: 2026 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.ui.tv

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button as TvMaterialButton
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.LocalContentColor
import com.aurora.store.compose.theme.TvFocusedSurface
import com.aurora.store.compose.theme.TvOnFocusedSurface
import com.aurora.store.compose.theme.TvOnSurface
import com.aurora.store.compose.theme.TvOnSurfaceMuted
import com.aurora.store.compose.theme.TvSelectedPill

/**
 * Pill button for TV, like the TV Play Store's Install button: dark grey with light text, light
 * with dark text while focused. Give it a [label] or an icon only ([iconRes] plus
 * [contentDescription]).
 */
@Composable
fun TvButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    @DrawableRes iconRes: Int? = null,
    contentDescription: String? = null,
    enabled: Boolean = true,
    stateDescription: String? = null
) {
    // A disabled button stays focusable (it just doesn't act), so focus isn't dropped when an
    // action becomes unavailable under the remote, e.g. "Open" while an install downloads
    TvMaterialButton(
        modifier = modifier
            .heightIn(min = 44.dp)
            .semantics {
                if (contentDescription != null) this.contentDescription = contentDescription
                if (stateDescription != null) this.stateDescription = stateDescription
                if (!enabled) disabled()
            },
        onClick = { if (enabled) onClick() },
        shape = ButtonDefaults.shape(shape = CircleShape),
        colors = if (enabled) {
            ButtonDefaults.colors(
                containerColor = TvSelectedPill,
                contentColor = TvOnSurface,
                focusedContainerColor = TvFocusedSurface,
                focusedContentColor = TvOnFocusedSurface,
                pressedContainerColor = TvFocusedSurface,
                pressedContentColor = TvOnFocusedSurface
            )
        } else {
            ButtonDefaults.colors(
                containerColor = TvSelectedPill.copy(alpha = 0.5f),
                contentColor = TvOnSurfaceMuted,
                focusedContainerColor = TvFocusedSurface.copy(alpha = 0.4f),
                focusedContentColor = TvOnSurface,
                pressedContainerColor = TvFocusedSurface.copy(alpha = 0.4f),
                pressedContentColor = TvOnSurface
            )
        },
        scale = ButtonDefaults.scale(focusedScale = 1.05f),
        contentPadding = if (label == null) {
            PaddingValues(horizontal = 12.dp, vertical = 10.dp)
        } else {
            PaddingValues(horizontal = 24.dp, vertical = 10.dp)
        }
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (iconRes != null) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = LocalContentColor.current
                )
            }
            if (label != null) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    color = LocalContentColor.current,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
