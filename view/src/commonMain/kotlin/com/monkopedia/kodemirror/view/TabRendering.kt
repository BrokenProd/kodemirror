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

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import com.monkopedia.kodemirror.state.DocPos
import com.monkopedia.kodemirror.state.EditorState
import com.monkopedia.kodemirror.state.Extension
import com.monkopedia.kodemirror.state.LineNumber
import com.monkopedia.kodemirror.state.RangeSet
import com.monkopedia.kodemirror.state.RangeSetBuilder

/**
 * Extension that renders tab characters at the correct width based on
 * the editor's `tabSize` setting. Each `\t` is replaced with a widget
 * that renders the appropriate number of spaces to reach the next tab
 * stop column.
 *
 * This extension is included in `minimalSetup` / `basicSetup` so that
 * tab characters always display at the correct width in Compose text.
 */
val tabRendering: Extension = ViewPlugin.define(
    create = { view -> TabRenderingPlugin(view) },
    configure = {
        copy(
            decorations = { plugin ->
                (plugin as? TabRenderingPlugin)?.decos
                    ?: RangeSet.empty()
            }
        )
    }
).asExtension()

private class TabRenderingPlugin(view: EditorSession) : PluginValue {
    var decos: DecorationSet = buildTabDecos(view.state)

    override fun update(update: ViewUpdate) {
        if (update.docChanged) {
            decos = buildTabDecos(update.state)
        }
    }
}

/**
 * Scan every line in the document, find tab characters, and build
 * replace decorations that substitute each tab with a space-widget
 * of the correct width.
 */
private fun buildTabDecos(state: EditorState): DecorationSet {
    val doc = state.doc
    val tabSize = state.tabSize
    val builder = RangeSetBuilder<Decoration>()

    for (lineIdx in 1..doc.lines) {
        val line = doc.line(LineNumber(lineIdx))
        val text = line.text
        if ('\t' !in text) continue

        val lineStart = line.from.value
        var col = 0
        for (i in text.indices) {
            val ch = text[i]
            if (ch == '\t') {
                val spaces = tabSize - (col % tabSize)
                val pos = DocPos(lineStart + i)
                builder.add(
                    pos,
                    pos + 1,
                    Decoration.replace(
                        ReplaceDecorationSpec(
                            widget = TabWidget(spaces)
                        )
                    )
                )
                col += spaces
            } else {
                col++
            }
        }
    }

    return builder.finish()
}

private class TabWidget(val spaces: Int) : WidgetType() {
    @Composable
    override fun Content() {
        BasicText(
            text = " ".repeat(spaces),
            style = LocalContentTextStyle.current
        )
    }

    override fun equals(other: Any?): Boolean = other is TabWidget && spaces == other.spaces

    override fun hashCode(): Int = spaces
}
