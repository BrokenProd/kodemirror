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

import com.monkopedia.kodemirror.search.searchPanelOpen
import com.monkopedia.kodemirror.state.Extension
import com.monkopedia.kodemirror.view.EditorSession
import com.monkopedia.kodemirror.view.PluginValue
import com.monkopedia.kodemirror.view.ViewPlugin
import com.monkopedia.kodemirror.view.ViewUpdate
import com.monkopedia.kodemirror.view.getPanel
import com.monkopedia.kodemirror.vim.getCM
import kotlin.JsFun

@JsFun(
    """() => {
    globalThis.__kodemirror = { ready: false, version: 0, state: null };
}"""
)
private external fun initVimBridge()

@JsFun(
    """(json, version) => {
    globalThis.__kodemirror.state = JSON.parse(json);
    globalThis.__kodemirror.version = version;
    globalThis.__kodemirror.ready = true;
}"""
)
private external fun syncVimState(json: String, version: Int)

private var vimBridgeVersion = 0

private fun escapeJsonStr(s: String): String = buildString {
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

private fun serializeVimState(session: EditorSession): String {
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

    // Get vim mode info
    val cm = getCM(session)
    val vimState = cm?.state?.vim
    val vimMode = when {
        vimState == null -> "normal"
        vimState.insertMode -> "insert"
        vimState.visualMode -> when {
            vimState.visualLine -> "visual-line"
            vimState.visualBlock -> "visual-block"
            else -> "visual"
        }
        else -> "normal"
    }
    val vimStatus = vimState?.status ?: ""

    return buildString {
        append("{\"doc\":")
        append(escapeJsonStr(doc.toString()))
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
        append(",\"vimMode\":")
        append(escapeJsonStr(vimMode))
        append(",\"vimStatus\":")
        append(escapeJsonStr(vimStatus))
        append('}')
    }
}

private class VimTestBridgePlugin(session: EditorSession) : PluginValue {
    init {
        vimBridgeVersion++
        syncVimState(serializeVimState(session), vimBridgeVersion)
    }

    override fun update(update: ViewUpdate) {
        vimBridgeVersion++
        syncVimState(serializeVimState(update.session), vimBridgeVersion)
    }
}

val vimTestBridgeExtension: Extension by lazy {
    initVimBridge()
    ViewPlugin.define(create = { session: EditorSession ->
        VimTestBridgePlugin(session)
    }).asExtension()
}
