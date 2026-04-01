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
package com.monkopedia.kodemirror.commands

import com.monkopedia.kodemirror.language.getIndentUnit
import com.monkopedia.kodemirror.language.indentString
import com.monkopedia.kodemirror.state.ChangeSpec
import com.monkopedia.kodemirror.state.DocPos
import com.monkopedia.kodemirror.state.EditorSelection
import com.monkopedia.kodemirror.state.InsertContent
import com.monkopedia.kodemirror.state.LineNumber
import com.monkopedia.kodemirror.state.SelectionSpec
import com.monkopedia.kodemirror.state.Transaction
import com.monkopedia.kodemirror.state.TransactionSpec
import com.monkopedia.kodemirror.state.countColumn
import com.monkopedia.kodemirror.view.EditorSession

/**
 * Add one level of indentation to each line in the selection.
 */
val indentMore: (EditorSession) -> Boolean = { view ->
    changeIndent(view, add = true)
}

/**
 * Remove one level of indentation from each line in the selection.
 */
val indentLess: (EditorSession) -> Boolean = { view ->
    changeIndent(view, add = false)
}

private fun changeIndent(view: EditorSession, add: Boolean): Boolean {
    val state = view.state
    if (state.readOnly) return false

    val indentSize = getIndentUnit(state)
    val indent = " ".repeat(indentSize)
    val sel = state.selection.main
    val startLine = state.doc.lineAt(sel.from)
    // When the selection ends at the very start of a line and is non-empty,
    // don't include that line (matches CM6 indentMore behavior).
    val endLine = if (sel.to > sel.from) {
        val candidateLine = state.doc.lineAt(sel.to)
        if (sel.to == candidateLine.from) {
            state.doc.lineAt(DocPos(sel.to.value - 1))
        } else {
            candidateLine
        }
    } else {
        state.doc.lineAt(sel.to)
    }

    val changes = mutableListOf<ChangeSpec>()

    for (lineNum in startLine.number.value..endLine.number.value) {
        val line = state.doc.line(LineNumber(lineNum))
        if (add) {
            changes.add(
                ChangeSpec.Single(
                    line.from,
                    line.from,
                    InsertContent.StringContent(indent)
                )
            )
        } else {
            // Match CM6's indentLess: compute the current column width of
            // leading whitespace, then generate the new (shorter) indent
            // string. Only replace the differing suffix so that the cursor
            // stays in place when it's within the common prefix.
            val text = line.text
            val space = text.takeWhile { it == ' ' || it == '\t' }
            if (space.isEmpty()) continue
            val col = countColumn(space, state.tabSize)
            val newCol = (col - indentSize).coerceAtLeast(0)
            val newIndent = indentString(state, newCol)
            // Find common prefix length
            var keep = 0
            while (
                keep < space.length && keep < newIndent.length &&
                space[keep] == newIndent[keep]
            ) {
                keep++
            }
            changes.add(
                ChangeSpec.Single(
                    line.from + keep,
                    line.from + space.length,
                    if (keep < newIndent.length) {
                        InsertContent.StringContent(newIndent.substring(keep))
                    } else {
                        null
                    }
                )
            )
        }
    }

    if (changes.isEmpty()) return false

    // CM6's changeBySelectedLine maps selection with assoc=1, so the
    // cursor moves right on insertions and stays in place when the
    // deletion is after the cursor (as with the suffix-only dedent).
    val changeDesc = state.changes(ChangeSpec.Multi(changes))
    val newSel = EditorSelection.create(
        state.selection.ranges.map { r ->
            EditorSelection.range(
                changeDesc.mapPos(r.anchor, 1),
                changeDesc.mapPos(r.head, 1)
            )
        },
        state.selection.mainIndex
    )

    view.dispatch(
        TransactionSpec(
            changes = ChangeSpec.Multi(changes),
            selection = SelectionSpec.EditorSelectionSpec(newSel),
            scrollIntoView = true,
            userEvent = if (add) "indent.more" else "indent.less",
            annotations = listOf(Transaction.addToHistory.of(true))
        )
    )
    return true
}
