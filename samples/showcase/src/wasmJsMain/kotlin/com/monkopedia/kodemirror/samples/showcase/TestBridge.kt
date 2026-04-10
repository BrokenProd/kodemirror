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
 */
package com.monkopedia.kodemirror.samples.showcase

import com.monkopedia.kodemirror.language.defaultHighlightStyle
import com.monkopedia.kodemirror.language.oneDarkHighlightStyle
import com.monkopedia.kodemirror.language.syntaxTree
import com.monkopedia.kodemirror.lezer.highlight.classHighlighter
import com.monkopedia.kodemirror.lezer.highlight.highlightTree
import com.monkopedia.kodemirror.search.searchPanelOpen
import com.monkopedia.kodemirror.state.DocPos
import com.monkopedia.kodemirror.state.Extension
import com.monkopedia.kodemirror.view.EditorSession
import com.monkopedia.kodemirror.view.PluginValue
import com.monkopedia.kodemirror.view.ViewPlugin
import com.monkopedia.kodemirror.view.ViewUpdate
import com.monkopedia.kodemirror.view.getPanel
import kotlin.JsFun

@JsFun(
    """() => {
    globalThis.__kodemirror = { ready: false, version: 0, state: null };
}"""
)
private external fun initBridge()

@JsFun(
    """(json, version) => {
    globalThis.__kodemirror.state = JSON.parse(json);
    globalThis.__kodemirror.version = version;
    globalThis.__kodemirror.ready = true;
}"""
)
private external fun syncState(json: String, version: Int)

@JsFun(
    """(json) => {
    globalThis.__kodemirror.highlights = JSON.parse(json);
}"""
)
private external fun syncHighlights(json: String)

private var bridgeVersion = 0

private fun serializeState(session: EditorSession): String {
    val state = session.state
    val doc = state.doc
    val sel = state.selection
    val main = sel.main
    val cursorPos = main.head
    val cursorLine = doc.lineAt(cursorPos)
    val cursorCol = cursorPos.value - cursorLine.from.value

    val rangesJson = buildString {
        append('[')
        sel.ranges.forEachIndexed { i, r ->
            if (i > 0) append(',')
            append("{\"from\":")
            append(r.from.value)
            append(",\"to\":")
            append(r.to.value)
            append(",\"anchor\":")
            append(r.anchor.value)
            append(",\"head\":")
            append(r.head.value)
            append('}')
        }
        append(']')
    }

    val panelCount = getPanel(session).size
    val isSearchOpen = searchPanelOpen(state)

    return buildString {
        append("{\"doc\":")
        append(escapeJsonString(doc.toString()))
        append(",\"cursor\":{\"pos\":")
        append(cursorPos.value)
        append(",\"line\":")
        append(cursorLine.number.value)
        append(",\"col\":")
        append(cursorCol)
        append("},\"selection\":{\"anchor\":")
        append(main.anchor.value)
        append(",\"head\":")
        append(main.head.value)
        append(",\"empty\":")
        append(main.empty)
        append(",\"ranges\":")
        append(rangesJson)
        append("},\"docInfo\":{\"lines\":")
        append(doc.lines)
        append(",\"length\":")
        append(doc.length)
        append("},\"panelCount\":")
        append(panelCount)
        append(",\"searchPanelOpen\":")
        append(isSearchOpen)
        append('}')
    }
}

private fun escapeJsonString(s: String): String = buildString {
    append('"')
    for (c in s) {
        when (c) {
            '"' -> append("\\\"")
            '\\' -> append("\\\\")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> if (c.code < 0x20) {
                append("\\u")
                append(c.code.toString(16).padStart(4, '0'))
            } else {
                append(c)
            }
        }
    }
    append('"')
}

private fun serializeHighlights(session: EditorSession): String {
    val tree = syntaxTree(session.state)
    val doc = session.state.doc

    // Walk with classHighlighter
    val classSpans = mutableListOf<Triple<Int, Int, String>>()
    highlightTree(tree, classHighlighter, { from, to, cls ->
        classSpans.add(Triple(from, to, cls))
    })

    // Walk with defaultHighlightStyle
    val defaultSpans = mutableListOf<Triple<Int, Int, String>>()
    highlightTree(tree, defaultHighlightStyle, { from, to, cls ->
        defaultSpans.add(Triple(from, to, cls))
    })

    // Walk with oneDarkHighlightStyle
    val oneDarkSpans = mutableListOf<Triple<Int, Int, String>>()
    highlightTree(tree, oneDarkHighlightStyle, { from, to, cls ->
        oneDarkSpans.add(Triple(from, to, cls))
    })

    // Build a map from (from,to) -> style class for quick lookup
    val defaultMap = defaultSpans.associate { (f, t, c) -> "$f-$t" to c }
    val oneDarkMap = oneDarkSpans.associate { (f, t, c) -> "$f-$t" to c }

    return buildString {
        append('[')
        classSpans.forEachIndexed { i, (from, to, cls) ->
            if (i > 0) append(',')
            val key = "$from-$to"
            append("{\"f\":")
            append(from)
            append(",\"t\":")
            append(to)
            append(",\"c\":")
            append(escapeJsonString(cls))
            append(",\"x\":")
            append(escapeJsonString(doc.sliceString(DocPos(from), DocPos(to)).take(40)))
            append(",\"dc\":")
            append(escapeJsonString(defaultMap[key] ?: ""))
            append(",\"odc\":")
            append(escapeJsonString(oneDarkMap[key] ?: ""))
            append('}')
        }
        append(']')
    }
}

private class TestBridgePlugin(session: EditorSession) : PluginValue {
    init {
        bridgeVersion++
        syncState(serializeState(session), bridgeVersion)
        syncHighlights(serializeHighlights(session))
    }

    override fun update(update: ViewUpdate) {
        bridgeVersion++
        syncState(serializeState(update.session), bridgeVersion)
        syncHighlights(serializeHighlights(update.session))
    }
}

val testBridgeExtension: Extension by lazy {
    initBridge()
    ViewPlugin.define(create = { session: EditorSession ->
        TestBridgePlugin(session)
    }).asExtension()
}
