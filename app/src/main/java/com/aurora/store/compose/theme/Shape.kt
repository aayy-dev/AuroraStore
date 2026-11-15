/*
 * SPDX-FileCopyrightText: 2026 Aurora OSS
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.aurora.store.compose.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Corner radii modeled on the Play Store: cards and grouped surfaces are noticeably rounder than
 * the Material baseline (review cards measure ~24dp), while buttons and chips stay fully rounded
 * through their own component defaults.
 */
val StoreShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/**
 * Shape for app icons, proportional to the icon size like the Play Store's rounded-square icons.
 */
val AppIconShape = RoundedCornerShape(percent = 22)
