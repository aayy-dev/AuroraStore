/*
 * SPDX-FileCopyrightText: 2025 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.composable

import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.aurora.store.compose.composition.LocalUI
import com.aurora.store.compose.composition.UI
import java.util.Locale

@Composable
fun LocaleListItem(
    modifier: Modifier = Modifier,
    displayName: String,
    displayLanguage: String,
    isChecked: Boolean = false,
    onClick: () -> Unit = {}
) {
    AuroraListItem(
        modifier = modifier,
        headline = displayName,
        supporting = displayLanguage,
        onClick = { if (!isChecked) onClick() },
        trailing = {
            Checkbox(
                checked = isChecked,
                // On TV the row itself is the only focus stop for this action
                onCheckedChange = if (LocalUI.current == UI.TV) {
                    null
                } else {
                    { _ -> if (!isChecked) onClick() }
                }
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun LocaleListItemPreview() {
    LocaleListItem(
        displayName = Locale.JAPANESE.displayName,
        displayLanguage = Locale.JAPAN.getDisplayLanguage(Locale.JAPAN),
        isChecked = true
    )
}
