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

import com.monkopedia.kodemirror.commands.history
import com.monkopedia.kodemirror.state.DocPos
import com.monkopedia.kodemirror.state.EditorState
import com.monkopedia.kodemirror.state.EditorStateConfig
import com.monkopedia.kodemirror.state.SelectionSpec
import com.monkopedia.kodemirror.state.asDoc
import com.monkopedia.kodemirror.view.EditorSession
import kotlin.test.assertEquals

/**
 * Default code used in vim tests, matching the upstream vim_test.js `code` variable.
 */
val DEFAULT_CODE = buildString {
    appendLine(" wOrd1 (#%")
    appendLine(" word3] ")
    appendLine("aopop pop 0 1 2 3 4")
    appendLine(" (a) [b] {c} ")
    appendLine("int getchar(void) {")
    appendLine("  static char buf[BUFSIZ];")
    appendLine("  static char *bufp = buf;")
    appendLine("  if (n == 0) {  /* buffer is empty */")
    appendLine("    n = read(0, buf, sizeof buf);")
    appendLine("    bufp = buf;")
    appendLine("  }")
    appendLine()
    appendLine("  return (--n >= 0) ? (unsigned char) *bufp++ : EOF;")
    appendLine(" ")
    appendLine("}")
}

/**
 * Helper class providing vim test utilities, mirroring the upstream `helpers` object.
 */
class VimHelpers(
    val cm: CodeMirrorAdapter,
    val vim: VimState
) {
    /**
     * Simulate pressing vim keys. Each argument is a single key or a special
     * key in angle brackets like `<Esc>`, `<C-r>`, `<CR>`.
     */
    fun doKeys(vararg keys: String) {
        for (key in keys) {
            // Pass keys directly in vim notation — handleKey expects <C-r>, not Ctrl-r
            typeKey(cm, key)
        }
    }

    /**
     * Execute an ex command (e.g., `doEx("s/foo/bar/g")`).
     * This simulates typing `:command\n`.
     */
    fun doEx(command: String) {
        doKeys(":")
        // Feed the command characters and Enter
        for (ch in command) {
            typeKey(cm, ch.toString())
        }
        typeKey(cm, "Enter")
    }

    /**
     * Assert the cursor is at the given position.
     */
    fun assertCursorAt(line: Int, ch: Int) {
        val actual = cm.getCursor()
        assertEquals(
            LinePos(line, ch),
            LinePos(actual.line, actual.ch),
            "Expected cursor at ($line, $ch) but was at (${actual.line}, ${actual.ch})"
        )
    }

    /**
     * Assert the cursor is at the given LinePos.
     */
    fun assertCursorAt(pos: LinePos) = assertCursorAt(pos.line, pos.ch)

    /**
     * Get the register controller for inspecting register contents.
     */
    internal fun getRegisterController() = Vim.getRegisterController()
}

/**
 * Convert vim key notation to a key name.
 * e.g., "C-r" → "Ctrl-r", "CR" → "Return", "BS" → "Backspace"
 */
private fun vimKeyToKeyName(key: String): String {
    return key.replace(Regex("[CS]-|CR|BS")) { match ->
        when (match.value) {
            "C-" -> "Ctrl-"
            "S-" -> "Shift-"
            "CR" -> "Return"
            "BS" -> "Backspace"
            else -> match.value
        }
    }
}

/**
 * Simulate typing a key in vim mode.
 * When in insert mode and the key is a printable character that vim doesn't handle,
 * insert it into the document (simulating what EditorView would do).
 */
internal fun typeKey(cm: CodeMirrorAdapter, key: String) {
    val handled = Vim.handleKey(cm, key, "user")
    if (!handled) {
        val vim = cm.state.vim
        if (vim != null && vim.insertMode && key.length == 1) {
            // Simulate text input: insert the character at the cursor
            val cursor = cm.getCursor()
            cm.replaceRange(key, cursor, cursor)
            // Record the change for macro/repeat tracking
            onChange(cm, Change(text = listOf(key)))
        }
    }
}

/**
 * Run a vim test with the given initial document and cursor position.
 *
 * Usage:
 * ```kotlin
 * @Test fun myTest() = testVim(value = "hello\nworld", cursor = LinePos(0, 0)) { helpers ->
 *     helpers.doKeys("d", "d")
 *     assertEquals("world", helpers.cm.getValue())
 * }
 * ```
 */
fun testVim(value: String = DEFAULT_CODE, cursor: LinePos? = null, fn: (VimHelpers) -> Unit) {
    // Create editor state
    val cursorOffset = if (cursor != null) {
        val lines = value.split("\n")
        var offset = 0
        for (i in 0 until cursor.line.coerceAtMost(lines.size - 1)) {
            offset += lines[i].length + 1
        }
        offset + cursor.ch.coerceAtMost(
            lines.getOrElse(cursor.line) { "" }.length
        )
    } else {
        0
    }

    val state = EditorState.create(
        EditorStateConfig(
            doc = value.asDoc(),
            selection = SelectionSpec.CursorSpec(DocPos(cursorOffset)),
            extensions = history()
        )
    )
    val session = EditorSession(state)
    val cm = CodeMirrorAdapter(session)

    // Initialize vim mode
    val vim = Vim.maybeInitVimState_(cm)
    Vim.resetVimGlobalState_()

    val helpers = VimHelpers(cm, vim)

    fn(helpers)
}

/**
 * Shorthand for testing a motion: press keys, assert cursor position.
 */
fun testMotion(keys: List<String>, endPos: LinePos, startPos: LinePos = LinePos(0, 0)) =
    testVim(cursor = startPos) { helpers ->
        helpers.doKeys(*keys.toTypedArray())
        helpers.assertCursorAt(endPos)
    }
