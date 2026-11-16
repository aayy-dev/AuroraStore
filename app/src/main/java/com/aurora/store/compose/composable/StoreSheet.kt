/*
 * SPDX-FileCopyrightText: 2026 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aurora.store.compose.composition.LocalUI
import com.aurora.store.compose.composition.UI
import com.aurora.store.compose.theme.TvCard
import com.aurora.store.compose.ui.tv.RequestFocusOnce

private val TvPanelShape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)

/**
 * Modal sheet for the app's option and action sheets. Phones get a [ModalBottomSheet]; TV gets a
 * full-height panel on the right, the usual TV pattern for options, with focus on its first entry.
 * Back (or selecting outside the panel) dismisses either.
 */
@Composable
fun StoreSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    containerColor: Color = BottomSheetDefaults.ContainerColor,
    content: @Composable ColumnScope.() -> Unit
) {
    if (LocalUI.current != UI.TV) {
        ModalBottomSheet(
            modifier = modifier,
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
            containerColor = containerColor,
            content = content
        )
        return
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val firstEntry = remember { FocusRequester() }
        RequestFocusOnce(firstEntry)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                // Taps (not remote focus) outside the panel dismiss it
                .pointerInput(Unit) { detectTapGestures { onDismissRequest() } },
            contentAlignment = Alignment.CenterEnd
        ) {
            Column(
                modifier = modifier
                    .fillMaxHeight()
                    .width(460.dp)
                    .background(TvCard, TvPanelShape)
                    // Swallow taps so tapping inside the panel doesn't dismiss it
                    .pointerInput(Unit) { detectTapGestures { } }
                    .focusRequester(firstEntry)
                    // Like the bottom sheet, the panel doesn't scroll; sheets with long content
                    // scroll it themselves
                    .padding(vertical = 24.dp),
                content = content
            )
        }
    }
}
