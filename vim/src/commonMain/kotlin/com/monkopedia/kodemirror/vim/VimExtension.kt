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

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import com.monkopedia.kodemirror.state.EditorState
import com.monkopedia.kodemirror.state.Extension
import com.monkopedia.kodemirror.state.RangeSet
import com.monkopedia.kodemirror.state.StateEffect
import com.monkopedia.kodemirror.state.StateEffectType
import com.monkopedia.kodemirror.state.StateField
import com.monkopedia.kodemirror.state.Transaction
import com.monkopedia.kodemirror.state.TransactionSpec
import com.monkopedia.kodemirror.state.define
import com.monkopedia.kodemirror.state.extensionListOf
import com.monkopedia.kodemirror.view.DecorationSet
import com.monkopedia.kodemirror.view.EditorSession
import com.monkopedia.kodemirror.view.KeyBinding
import com.monkopedia.kodemirror.view.PluginValue
import com.monkopedia.kodemirror.view.ViewPlugin
import com.monkopedia.kodemirror.view.ViewUpdate
import com.monkopedia.kodemirror.view.keymap
import com.monkopedia.kodemirror.view.showPanel

/**
 * Create a vim mode extension for the editor.
 *
 * @param status If true, always show a status panel displaying the current
 *   vim mode. If false (default), only show a panel when a dialog is active
 *   (e.g., ex command line, search prompt).
 */
fun vim(status: Boolean = false): Extension {
    return extensionListOf(
        vimKeymap,
        vimPlugin.asExtension(),
        if (status) showPanel.of(createStatusPanel()) else vimPanelField
    )
}

/**
 * Get the [CodeMirrorAdapter] associated with an [EditorSession], or null
 * if vim mode is not active.
 */
fun getCM(view: EditorSession): CodeMirrorAdapter? {
    return view.plugin(vimPlugin)?.cm
}

// ---------------------------------------------------------------------------
// State effect for toggling the vim panel
// ---------------------------------------------------------------------------

internal val showVimPanel: StateEffectType<Boolean> = StateEffect.define()

// ---------------------------------------------------------------------------
// State field that tracks vim panel visibility
// ---------------------------------------------------------------------------

internal val vimPanelField: StateField<Boolean> = StateField.define {
    create { _: EditorState -> false }
    update { value: Boolean, tr: Transaction ->
        var result = value
        for (effect in tr.effects) {
            val panelEffect = effect.asType(showVimPanel)
            if (panelEffect != null) {
                result = panelEffect.value
            }
        }
        result
    }
    provide { field: StateField<Boolean> ->
        showPanel.from(field) { on: Boolean ->
            if (on) createVimDialogPanel() else null
        }
    }
}

// ---------------------------------------------------------------------------
// Vim keymap: intercepts all key events and routes them to the vim engine
// ---------------------------------------------------------------------------

private val vimKeymap: Extension = keymap.of(
    listOf(
        KeyBinding(
            any = handler@{ view, event ->
                if (event.type != KeyEventType.KeyDown) return@handler false
                val plugin = view.plugin(vimPlugin) ?: return@handler false
                plugin.handleKey(event)
            }
        )
    )
)

// ---------------------------------------------------------------------------
// Vim ViewPlugin: manages the vim lifecycle
// ---------------------------------------------------------------------------

internal val vimPlugin: ViewPlugin<VimPluginValue> = ViewPlugin.define(
    create = { session -> VimPluginValue(session) },
    decorations = { plugin -> plugin.decorations }
)

internal class VimPluginValue(private val session: EditorSession) : PluginValue {
    val cm: CodeMirrorAdapter = CodeMirrorAdapter(session)
    private val vimState: VimState
    var decorations: DecorationSet = RangeSet.empty()
        private set

    init {
        Vim.enterVimMode(cm)
        cm.vimPlugin = this
        vimState = cm.vim ?: Vim.maybeInitVimState_(cm)

        cm.on("vim-command-done") {
            cm.vim?.status = ""
            rebuildDecorations()
            updateStatus()
        }
        cm.on("vim-mode-change") { args ->
            val vimSt = cm.vim ?: return@on
            val eventMap = args.getOrNull(0) as? Map<*, *>
            if (eventMap != null) {
                vimSt.mode = eventMap["mode"] as? String
                val sub = eventMap["subMode"] as? String
                if (sub != null) {
                    vimSt.mode = (vimSt.mode ?: "") +
                        if (sub == "linewise") " line" else " block"
                }
            }
            vimSt.status = ""
            rebuildDecorations()
            updateStatus()
        }
        rebuildDecorations()

        cm.on("dialog") {
            if (cm.statusbar != null) {
                updateStatus()
            } else {
                session.dispatch(
                    TransactionSpec(
                        effects = listOf(
                            showVimPanel.of(cm.dialog != null)
                        )
                    )
                )
            }
        }
    }

    var status: String = ""
        private set

    override fun update(update: ViewUpdate) {
        if (update.docChanged) {
            cm.onChange(update)
        }
        if (update.selectionSet) {
            cm.onSelectionChange()
        }
        if (cm.curOp != null && cm.curOp?.isVimOp != true) {
            cm.onBeforeEndOperation()
        }
        rebuildDecorations()
    }

    private fun rebuildDecorations() {
        decorations = buildBlockCursorDecorations(session.state, cm)
    }

    fun handleKey(event: KeyEvent): Boolean {
        val vim = cm.vim ?: return false
        val key = composeKeyToVimKey(event, vim) ?: return false

        // Clear search highlight on Esc in normal mode
        if (key == "<Esc>" && !vim.insertMode && !vim.visualMode) {
            val searchState = vim.searchState_
            if (searchState != null) {
                cm.removeOverlay(searchState.getOverlay())
                searchState.setOverlay(null)
            }
        }

        vim.status = (vim.status) + key
        val result = Vim.multiSelectHandleKey(cm, key, "user")
        // The vim state object can change if there is an exception in handleKey
        val currentVim = Vim.maybeInitVimState_(cm)

        // Overwrite mode handling
        if (!result && currentVim.insertMode && currentVim.overwrite) {
            val charKey = extractCharFromEvent(event)
            if (charKey != null && charKey.length == 1 && !charKey.contains('\n')) {
                cm.overWriteSelection(charKey)
                rebuildDecorations()
                updateStatus()
                return true
            } else if (event.key == Key.Backspace) {
                com.monkopedia.kodemirror.commands.cursorCharLeft(cm.session)
                rebuildDecorations()
                updateStatus()
                return true
            }
        }

        if (result) {
            rebuildDecorations()
            updateStatus()
        }

        return result
    }

    private fun updateStatus() {
        val vim = cm.vim ?: return
        status = vim.status
    }

    override fun destroy() {
        Vim.leaveVimMode(cm)
    }
}

// ---------------------------------------------------------------------------
// Key conversion: Compose KeyEvent → vim key string
// ---------------------------------------------------------------------------

/**
 * Convert a Compose [KeyEvent] to a vim key string (e.g., "j", "<Esc>",
 * "<C-r>").
 *
 * Returns null if the key should be ignored (modifier-only keys, etc.).
 */
internal fun composeKeyToVimKey(event: KeyEvent, vim: VimState?): String? {
    val key = composeKeyName(event)
    return vimKeyFromEvent(
        key = key,
        ctrlKey = event.isCtrlPressed,
        altKey = event.isAltPressed,
        metaKey = event.isMetaPressed,
        shiftKey = event.isShiftPressed,
        vim = vim
    )
}

/**
 * Extract the DOM-style key name from a Compose [KeyEvent].
 *
 * Maps Compose [Key] constants to the same strings that the browser
 * KeyboardEvent.key property would produce, since [vimKeyFromEvent]
 * expects those names.
 */
private fun composeKeyName(event: KeyEvent): String {
    // For modifier-only keys, return the modifier name
    return when (event.key) {
        Key.ShiftLeft, Key.ShiftRight -> "Shift"
        Key.CtrlLeft, Key.CtrlRight -> "Control"
        Key.AltLeft, Key.AltRight -> "Alt"
        Key.MetaLeft, Key.MetaRight -> "Meta"
        Key.CapsLock -> "CapsLock"

        // Special keys → DOM-style names (will be mapped by specialKeyMap)
        Key.Escape -> "Escape"
        Key.Enter -> "Enter"
        Key.Tab -> "Tab"
        Key.Spacebar -> " "
        Key.Backspace -> "Backspace"
        Key.Delete -> "Delete"
        Key.Insert -> "Insert"
        Key.DirectionLeft -> "ArrowLeft"
        Key.DirectionRight -> "ArrowRight"
        Key.DirectionUp -> "ArrowUp"
        Key.DirectionDown -> "ArrowDown"
        Key.Home -> "Home"
        Key.MoveEnd -> "End"
        Key.PageUp -> "PageUp"
        Key.PageDown -> "PageDown"

        // Function keys
        Key.F1 -> "F1"
        Key.F2 -> "F2"
        Key.F3 -> "F3"
        Key.F4 -> "F4"
        Key.F5 -> "F5"
        Key.F6 -> "F6"
        Key.F7 -> "F7"
        Key.F8 -> "F8"
        Key.F9 -> "F9"
        Key.F10 -> "F10"
        Key.F11 -> "F11"
        Key.F12 -> "F12"

        // Letter and digit keys: use the typed character
        else -> extractCharFromEvent(event) ?: event.key.toString()
    }
}

/**
 * Extract a printable character from a KeyEvent, accounting for shift state
 * and keyboard layout.
 */
private fun extractCharFromEvent(event: KeyEvent): String? {
    // For keys with Ctrl/Alt/Meta modifiers, return the base letter
    if (event.isCtrlPressed || event.isMetaPressed || event.isAltPressed) {
        return when (event.key) {
            Key.A -> "a"
            Key.B -> "b"
            Key.C -> "c"
            Key.D -> "d"
            Key.E -> "e"
            Key.F -> "f"
            Key.G -> "g"
            Key.H -> "h"
            Key.I -> "i"
            Key.J -> "j"
            Key.K -> "k"
            Key.L -> "l"
            Key.M -> "m"
            Key.N -> "n"
            Key.O -> "o"
            Key.P -> "p"
            Key.Q -> "q"
            Key.R -> "r"
            Key.S -> "s"
            Key.T -> "t"
            Key.U -> "u"
            Key.V -> "v"
            Key.W -> "w"
            Key.X -> "x"
            Key.Y -> "y"
            Key.Z -> "z"
            Key.Zero -> "0"
            Key.One -> "1"
            Key.Two -> "2"
            Key.Three -> "3"
            Key.Four -> "4"
            Key.Five -> "5"
            Key.Six -> "6"
            Key.Seven -> "7"
            Key.Eight -> "8"
            Key.Nine -> "9"
            else -> null
        }
    }

    // For normal key presses, try to get the actual typed character
    // When Shift is held, letters should be uppercase
    return when (event.key) {
        Key.A -> if (event.isShiftPressed) "A" else "a"
        Key.B -> if (event.isShiftPressed) "B" else "b"
        Key.C -> if (event.isShiftPressed) "C" else "c"
        Key.D -> if (event.isShiftPressed) "D" else "d"
        Key.E -> if (event.isShiftPressed) "E" else "e"
        Key.F -> if (event.isShiftPressed) "F" else "f"
        Key.G -> if (event.isShiftPressed) "G" else "g"
        Key.H -> if (event.isShiftPressed) "H" else "h"
        Key.I -> if (event.isShiftPressed) "I" else "i"
        Key.J -> if (event.isShiftPressed) "J" else "j"
        Key.K -> if (event.isShiftPressed) "K" else "k"
        Key.L -> if (event.isShiftPressed) "L" else "l"
        Key.M -> if (event.isShiftPressed) "M" else "m"
        Key.N -> if (event.isShiftPressed) "N" else "n"
        Key.O -> if (event.isShiftPressed) "O" else "o"
        Key.P -> if (event.isShiftPressed) "P" else "p"
        Key.Q -> if (event.isShiftPressed) "Q" else "q"
        Key.R -> if (event.isShiftPressed) "R" else "r"
        Key.S -> if (event.isShiftPressed) "S" else "s"
        Key.T -> if (event.isShiftPressed) "T" else "t"
        Key.U -> if (event.isShiftPressed) "U" else "u"
        Key.V -> if (event.isShiftPressed) "V" else "v"
        Key.W -> if (event.isShiftPressed) "W" else "w"
        Key.X -> if (event.isShiftPressed) "X" else "x"
        Key.Y -> if (event.isShiftPressed) "Y" else "y"
        Key.Z -> if (event.isShiftPressed) "Z" else "z"
        Key.Zero -> if (event.isShiftPressed) ")" else "0"
        Key.One -> if (event.isShiftPressed) "!" else "1"
        Key.Two -> if (event.isShiftPressed) "@" else "2"
        Key.Three -> if (event.isShiftPressed) "#" else "3"
        Key.Four -> if (event.isShiftPressed) "$" else "4"
        Key.Five -> if (event.isShiftPressed) "%" else "5"
        Key.Six -> if (event.isShiftPressed) "^" else "6"
        Key.Seven -> if (event.isShiftPressed) "&" else "7"
        Key.Eight -> if (event.isShiftPressed) "*" else "8"
        Key.Nine -> if (event.isShiftPressed) "(" else "9"
        Key.Minus -> if (event.isShiftPressed) "_" else "-"
        Key.Equals -> if (event.isShiftPressed) "+" else "="
        Key.LeftBracket -> if (event.isShiftPressed) "{" else "["
        Key.RightBracket -> if (event.isShiftPressed) "}" else "]"
        Key.Backslash -> if (event.isShiftPressed) "|" else "\\"
        Key.Semicolon -> if (event.isShiftPressed) ":" else ";"
        Key.Apostrophe -> if (event.isShiftPressed) "\"" else "'"
        Key.Comma -> if (event.isShiftPressed) "<" else ","
        Key.Period -> if (event.isShiftPressed) ">" else "."
        Key.Slash -> if (event.isShiftPressed) "?" else "/"
        Key.Grave -> if (event.isShiftPressed) "~" else "`"
        else -> null
    }
}
