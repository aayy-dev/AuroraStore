/*
 * SPDX-FileCopyrightText: 2026 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.composable

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import com.aurora.store.R
import com.aurora.store.compose.preview.ThemePreviewProvider

/**
 * Visual style of a [StoreChip]
 */
enum class StoreChipStyle {
    /** Filled pill without border, as used for the home screen shortcuts */
    TONAL,

    /** Outlined pill, as used for filters (e.g. chart types) */
    OUTLINED
}

/**
 * Pill-shaped chip modeled on the Play Store's home shortcuts and filter chips.
 *
 * Selected chips use the secondary container color; unselected chips are either filled with the
 * surface container color ([StoreChipStyle.TONAL]) or outlined ([StoreChipStyle.OUTLINED]).
 *
 * @param label Text to display
 * @param selected Whether the chip is currently selected
 * @param onClick Callback when the chip is clicked
 * @param iconRes Optional leading icon
 * @param showCheckWhenSelected Replace the leading icon with a check mark while selected
 * @param style Visual style when not selected
 */
@Composable
fun StoreChip(
    modifier: Modifier = Modifier,
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit = {},
    @DrawableRes iconRes: Int? = null,
    @DrawableRes trailingIconRes: Int? = null,
    showCheckWhenSelected: Boolean = false,
    style: StoreChipStyle = StoreChipStyle.TONAL
) {
    val colors = MaterialTheme.colorScheme
    val containerColor = when {
        selected -> colors.secondaryContainer
        style == StoreChipStyle.TONAL -> colors.surfaceContainer
        else -> colors.surface
    }
    val contentColor = if (selected) colors.onSecondaryContainer else colors.onSurfaceVariant
    val iconColor = if (selected) colors.onSecondaryContainer else colors.primary
    val leadingIcon = if (selected && showCheckWhenSelected) R.drawable.ic_check else iconRes

    Surface(
        modifier = modifier
            .heightIn(min = 40.dp)
            .semantics {
                role = Role.Tab
                this.selected = selected
            },
        onClick = onClick,
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
        border = if (!selected && style == StoreChipStyle.OUTLINED) {
            BorderStroke(1.dp, colors.outlineVariant)
        } else {
            null
        }
    ) {
        Row(
            modifier = Modifier.padding(
                start = if (leadingIcon != null) 12.dp else 16.dp,
                end = if (trailingIconRes != null) 8.dp else 16.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(leadingIcon),
                    contentDescription = null,
                    tint = iconColor
                )
            }
            Text(
                modifier = Modifier.padding(vertical = 10.dp),
                text = label,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (trailingIconRes != null) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(trailingIconRes),
                    contentDescription = null
                )
            }
        }
    }
}

@PreviewWrapper(ThemePreviewProvider::class)
@Preview(showBackground = true)
@Composable
private fun StoreChipPreview() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StoreChip(label = "For you", selected = true, iconRes = R.drawable.ic_chip_for_you)
        StoreChip(label = "Top charts", iconRes = R.drawable.ic_chip_top_charts)
        StoreChip(
            label = "Top free",
            selected = true,
            showCheckWhenSelected = true,
            style = StoreChipStyle.OUTLINED
        )
        StoreChip(label = "Trending", style = StoreChipStyle.OUTLINED)
    }
}
