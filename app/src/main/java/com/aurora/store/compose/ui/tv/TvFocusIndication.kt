/*
 * SPDX-FileCopyrightText: 2026 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.ui.tv

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Indication for phone-style components shown on TV (lists, Material buttons, chips): a white
 * outline and light wash while focused, so the remote's position is obvious from across the room.
 * Material's own focus state layer is too faint for that.
 */
object TvFocusIndication : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode =
        TvFocusIndicationNode(interactionSource)

    override fun equals(other: Any?): Boolean = other === this

    override fun hashCode(): Int = javaClass.hashCode()
}

private class TvFocusIndicationNode(private val interactionSource: InteractionSource) :
    Modifier.Node(),
    DrawModifierNode {

    private var focusCount = 0
    private var pressCount = 0

    override fun onAttach() {
        coroutineScope.launch {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is FocusInteraction.Focus -> focusCount++
                    is FocusInteraction.Unfocus -> focusCount--
                    is PressInteraction.Press -> pressCount++
                    is PressInteraction.Release, is PressInteraction.Cancel -> pressCount--
                }
                focusCount = focusCount.coerceAtLeast(0)
                pressCount = pressCount.coerceAtLeast(0)
                invalidateDraw()
            }
        }
    }

    override fun ContentDrawScope.draw() {
        drawContent()
        if (focusCount == 0 && pressCount == 0) return

        val radius = CornerRadius(12.dp.toPx())
        drawRoundRect(
            color = Color.White.copy(alpha = if (pressCount > 0) 0.24f else 0.12f),
            cornerRadius = radius
        )
        if (focusCount > 0) {
            val stroke = 2.dp.toPx()
            drawRoundRect(
                color = Color.White,
                cornerRadius = radius,
                style = Stroke(width = stroke)
            )
        }
    }
}
