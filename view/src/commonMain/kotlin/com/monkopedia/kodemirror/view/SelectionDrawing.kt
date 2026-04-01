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
package com.monkopedia.kodemirror.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextLayoutResult
import com.monkopedia.kodemirror.state.EditorState

/** Extension to select the draw-selection extension. */
val drawSelection: com.monkopedia.kodemirror.state.Extension =
    com.monkopedia.kodemirror.state.ExtensionList(emptyList())

/**
 * Draw the cursor and selection ranges as a per-line [Modifier] overlay.
 *
 * Only draws the portion of the selection that intersects the given line,
 * using coordinates local to the line's composable.
 *
 * @param state  Current editor state.
 * @param lineFrom Document offset of the start of this line.
 * @param lineTo  Document offset of the end of this line.
 * @param theme  Editor theme for colors.
 * @param textLayoutResult Layout result for accurate character positioning.
 */
@Composable
fun Modifier.drawSelectionOverlay(
    state: EditorState,
    lineFrom: Int,
    lineTo: Int,
    theme: EditorTheme,
    textLayoutResult: TextLayoutResult? = null,
    tabOffsetMap: IntArray? = null
): Modifier = this.drawWithContent {
    drawLineSelection(
        state,
        lineFrom,
        lineTo,
        theme,
        textLayoutResult,
        tabOffsetMap
    )
    drawContent()
}

/** Default block cursor color (salmon/pink, matching upstream `#ff9696`). */
private val BLOCK_CURSOR_COLOR = Color(0xFFFF9696)

/**
 * Map a document-relative offset to the expanded text offset,
 * accounting for tab expansion. Returns the offset unchanged when
 * there is no tab offset map.
 */
private fun mapOffset(docOffset: Int, lineLength: Int, tabOffsetMap: IntArray?): Int {
    if (tabOffsetMap == null) return docOffset
    return tabOffsetMap[docOffset.coerceIn(0, lineLength)]
}

private fun DrawScope.drawLineSelection(
    state: EditorState,
    lineFrom: Int,
    lineTo: Int,
    theme: EditorTheme,
    textLayoutResult: TextLayoutResult?,
    tabOffsetMap: IntArray?
) {
    val lineLength = lineTo - lineFrom
    // Check for block cursors (vim normal/visual mode)
    val blockCursors = state.facet(blockCursorProvider)
        .flatMap { it.invoke() }
        .filter { it.offset in lineFrom..lineTo }
    val hasBlockCursors = blockCursors.isNotEmpty()

    val selection = state.selection
    for (range in selection.ranges) {
        val rangeFrom = range.from.value
        val rangeTo = range.to.value
        val rangeHead = range.head.value
        if (!range.empty) {
            // Draw selection highlight if it overlaps this line
            val selFrom = maxOf(rangeFrom, lineFrom)
            val selTo = minOf(rangeTo, lineTo)
            if (selFrom < selTo || (selFrom == selTo && selFrom > lineFrom)) {
                val extendsToNextLine = rangeTo > lineTo
                val expandedLineLen = mapOffset(
                    lineLength,
                    lineLength,
                    tabOffsetMap
                )
                drawLineSelectionRange(
                    mapOffset(
                        selFrom - lineFrom,
                        lineLength,
                        tabOffsetMap
                    ),
                    mapOffset(
                        selTo - lineFrom,
                        lineLength,
                        tabOffsetMap
                    ),
                    expandedLineLen,
                    theme.selection,
                    textLayoutResult,
                    extendsToNextLine
                )
            }
        }
        // Draw thin cursor only when block cursors are NOT active
        if (!hasBlockCursors && rangeHead in lineFrom..lineTo) {
            drawLineCursor(
                mapOffset(
                    rangeHead - lineFrom,
                    lineLength,
                    tabOffsetMap
                ),
                theme.cursor,
                textLayoutResult
            )
        }
    }

    // Draw block cursors at full line height
    for (cursor in blockCursors) {
        drawBlockCursor(
            mapOffset(
                cursor.offset - lineFrom,
                lineLength,
                tabOffsetMap
            ),
            cursor.alpha,
            textLayoutResult
        )
    }
}

private fun DrawScope.drawBlockCursor(
    offsetInLine: Int,
    alpha: Float,
    textLayoutResult: TextLayoutResult?
) {
    val lineHeight = size.height
    val textLen = textLayoutResult?.layoutInput?.text?.length ?: 0

    val x: Float
    val charWidth: Float

    if (textLayoutResult != null && offsetInLine < textLen) {
        // Character position: get exact bounds from text layout
        x = textLayoutResult.getHorizontalPosition(offsetInLine, true)
        val boundingBox = textLayoutResult.getBoundingBox(offsetInLine)
        charWidth = boundingBox.width.coerceAtLeast(4f)
    } else if (textLayoutResult != null && textLen > 0) {
        // End of line: position after last character, use average char width
        x = textLayoutResult.getHorizontalPosition(textLen, true)
        charWidth = textLayoutResult.size.width.toFloat() / textLen
    } else {
        // Empty line: position at start, use fallback width
        x = 0f
        charWidth = lineHeight * 0.55f // approximate monospace char width
    }

    drawRect(
        color = BLOCK_CURSOR_COLOR.copy(alpha = alpha),
        topLeft = Offset(x, 0f),
        size = Size(charWidth, lineHeight)
    )
}

private fun DrawScope.drawLineCursor(
    offsetInLine: Int,
    cursorColor: Color,
    textLayoutResult: TextLayoutResult?
) {
    val lineHeight = size.height
    val textLen = textLayoutResult?.layoutInput?.text?.length
    val x = if (textLayoutResult != null && textLen != null && offsetInLine <= textLen) {
        textLayoutResult.getHorizontalPosition(offsetInLine, true)
    } else {
        0f
    }
    drawLine(
        color = cursorColor,
        start = Offset(x, 0f),
        end = Offset(x, lineHeight),
        strokeWidth = 2f
    )
}

private fun DrawScope.drawLineSelectionRange(
    fromOffset: Int,
    toOffset: Int,
    lineLength: Int,
    selectionColor: Color,
    textLayoutResult: TextLayoutResult?,
    extendsToNextLine: Boolean
) {
    val lineHeight = size.height
    val textLen = textLayoutResult?.layoutInput?.text?.length ?: lineLength

    val x: Float
    val endX: Float

    if (textLayoutResult != null) {
        x = textLayoutResult.getHorizontalPosition(
            fromOffset.coerceAtMost(textLen),
            true
        )
        endX = if (extendsToNextLine) {
            // Selection continues to next line — extend to full container width
            size.width
        } else {
            textLayoutResult.getHorizontalPosition(
                toOffset.coerceAtMost(textLen),
                true
            )
        }
    } else {
        // Fallback: fraction-based (inaccurate but better than nothing)
        val startFraction = if (lineLength > 0) fromOffset.toFloat() / lineLength else 0f
        val endFraction = if (lineLength > 0) toOffset.toFloat() / lineLength else 1f
        x = startFraction * size.width
        endX = if (extendsToNextLine) size.width else endFraction * size.width
    }

    val w = endX - x
    drawRect(
        color = selectionColor,
        topLeft = Offset(x, 0f),
        size = Size(w.coerceAtLeast(1f), lineHeight)
    )
}
