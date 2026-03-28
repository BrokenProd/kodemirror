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
package com.monkopedia.kodemirror.vim

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import com.monkopedia.kodemirror.state.DocPos
import com.monkopedia.kodemirror.state.EditorState
import com.monkopedia.kodemirror.state.RangeSet
import com.monkopedia.kodemirror.state.RangeSetBuilder
import com.monkopedia.kodemirror.state.SelectionRange
import com.monkopedia.kodemirror.view.Decoration
import com.monkopedia.kodemirror.view.DecorationSet
import com.monkopedia.kodemirror.view.MarkDecoration
import kotlin.math.max

/**
 * Default block cursor background color (salmon/pink, matching upstream
 * `#ff9696`).
 */
private val BLOCK_CURSOR_COLOR = Color(0xFFFF9696)

/**
 * Build block cursor decorations for the given editor state and vim state.
 *
 * Returns a [DecorationSet] containing mark decorations that highlight the
 * character(s) under the cursor in normal/visual/overwrite mode. Returns an
 * empty set in insert mode.
 */
internal fun buildBlockCursorDecorations(state: EditorState, cm: CodeMirrorAdapter): DecorationSet {
    val vim = cm.vim ?: return RangeSet.empty()

    // No block cursor in insert mode (unless overwrite)
    if (vim.insertMode && !vim.overwrite) return RangeSet.empty()

    val builder = RangeSetBuilder<Decoration>()
    val doc = state.doc

    for (range in state.selection.ranges) {
        val isPrimary = range == state.selection.main
        val piece = measureBlockCursor(vim, cm, doc.length, range, isPrimary, state)
            ?: continue
        if (piece.from < piece.to) {
            builder.add(piece.from, piece.to, piece.decoration)
        }
    }

    return builder.finish()
}

/**
 * A measured block cursor piece: the range to highlight and its decoration.
 */
private data class CursorPiece(
    val from: DocPos,
    val to: DocPos,
    val decoration: MarkDecoration
)

/**
 * Determine the cursor position and decoration for a single selection range
 * in block-cursor mode.
 *
 * Matches the upstream `measureCursor` logic from `block-cursor.ts`.
 */
private fun measureBlockCursor(
    vim: VimState,
    cm: CodeMirrorAdapter,
    docLength: Int,
    cursor: SelectionRange,
    primary: Boolean,
    state: EditorState
): CursorPiece? {
    var head = cursor.head

    // In visual block mode, only show the primary cursor
    if (vim.visualBlock && !primary) return null

    // When the cursor has a forward selection (anchor < head),
    // the cursor character is the one *before* head
    if (cursor.anchor < cursor.head) {
        val letterPos = head.value
        if (letterPos < docLength) {
            val letter = state.sliceDoc(DocPos(letterPos), DocPos(letterPos + 1))
            if (letter != "\n") {
                head = DocPos(head.value - 1)
            }
        }
    }

    // Determine cursor opacity/size factor
    val hCoeff = when {
        vim.overwrite -> 0.2f
        vim.status.isNotEmpty() -> 0.5f
        else -> 1.0f
    }

    // Compute the range: one character from head
    val from = head
    val charEnd = if (head.value < docLength) {
        val nextChar = state.sliceDoc(head, DocPos(head.value + 1))
        // Step past surrogate pairs
        if (nextChar.isNotEmpty() && nextChar[0].isHighSurrogate() && head.value + 1 < docLength) {
            DocPos(head.value + 2)
        } else if (nextChar == "\n" || nextChar == "\r" || nextChar.isEmpty()) {
            // At end-of-line or end-of-document: cover one "virtual" space
            // by marking a zero-width range (the decoration won't be visible,
            // but the cursor layer in the composable can handle this)
            head
        } else {
            DocPos(head.value + 1)
        }
    } else {
        head
    }

    // If from == charEnd, there's no character to highlight
    if (from == charEnd) return null

    // Create the mark decoration
    val alpha = (hCoeff * 255).toInt().coerceIn(0, 255)
    val bgColor = BLOCK_CURSOR_COLOR.copy(alpha = alpha / 255f)
    val decoration = Decoration.mark(
        style = SpanStyle(background = bgColor),
        cssClass = if (primary) {
            "cm-fat-cursor cm-cursor-primary"
        } else {
            "cm-fat-cursor cm-cursor-secondary"
        }
    )

    return CursorPiece(
        from = DocPos(max(from.value, 0)),
        to = charEnd,
        decoration = decoration
    )
}
