/*
 * Copyright 2026 Jason Monk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Originally based on CodeMirror 6 by Marijn Haverbeke, licensed under MIT.
 * See NOTICE file for details.
 */
package com.monkopedia.kodemirror.merge

import androidx.compose.ui.graphics.Color

/**
 * Color definitions for merge view decorations.
 */
object MergeColors {
    // Semi-transparent overlays (work on any background)
    val changedLineA = Color(0x14A08064)
    val changedTextA = Color(0x66FFAA99)
    val changedLineB = Color(0x1464A080)
    val changedTextB = Color(0x66AAFFAA)
    val deletedText = Color(0x66FFAAAA)
    val spacerBackground = Color(0x08000000)

    // Gutter markers (red/green are universal)
    val deletedLineGutter = Color(0xFFF44336)
    val changedLineGutter = Color(0xFF4CAF50)

    // Opaque colors — dark/light pairs
    fun deletedChunkBackground(dark: Boolean) = if (dark) Color(0xFF3D2020) else Color(0xFFFBE9E7)

    fun collapsedBackground(dark: Boolean) = if (dark) Color(0xFF2D2D2D) else Color(0xFFF5F5F5)

    fun collapsedBorder(dark: Boolean) = if (dark) Color(0xFF555555) else Color(0xFFBDBDBD)
}
