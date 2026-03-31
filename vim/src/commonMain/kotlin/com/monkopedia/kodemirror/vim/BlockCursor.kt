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

import com.monkopedia.kodemirror.state.DocPos
import com.monkopedia.kodemirror.state.EditorState
import com.monkopedia.kodemirror.state.SelectionRange
import com.monkopedia.kodemirror.view.BlockCursorSpec
import kotlin.math.max

/**
 * Compute block cursor positions for the given editor state and vim state.
 *
 * Returns a list of [BlockCursorSpec] with document offsets and alpha values.
 * The actual rendering is handled by the selection overlay in SelectionDrawing.kt
 * which draws filled rectangles at full line height.
 *
 * Returns an empty list in insert mode (unless overwrite mode is active).
 */
internal fun computeBlockCursorPositions(state: EditorState, cm: VimEditor): List<BlockCursorSpec> {
    val vim = cm.vim ?: return emptyList()

    // No block cursor in insert mode (unless overwrite)
    if (vim.insertMode && !vim.overwrite) return emptyList()

    val result = mutableListOf<BlockCursorSpec>()
    val docLength = state.doc.length

    for (range in state.selection.ranges) {
        val isPrimary = range == state.selection.main
        val spec = measureBlockCursorPosition(vim, docLength, range, isPrimary, state)
        if (spec != null) result.add(spec)
    }

    return result
}

/**
 * Determine the cursor position for a single selection range in block-cursor mode.
 *
 * Matches the upstream `measureCursor` logic from `block-cursor.ts`.
 */
private fun measureBlockCursorPosition(
    vim: VimState,
    docLength: Int,
    cursor: SelectionRange,
    primary: Boolean,
    state: EditorState
): BlockCursorSpec? {
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

    // Determine cursor opacity
    val alpha = when {
        vim.overwrite -> 0.2f
        vim.status.isNotEmpty() -> 0.5f
        else -> 1.0f
    }

    return BlockCursorSpec(
        offset = max(head.value, 0),
        alpha = alpha
    )
}
