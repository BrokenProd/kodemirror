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

/**
 * A bookmark marker that tracks a position through document changes.
 */
interface Marker {
    fun find(): LinePos?
    fun clear()
}

/**
 * A line handle reference.
 */
interface LineHandle {
    val lineNo: Int
    val text: String
}

/**
 * A selection range with anchor and head, using [LinePos] coordinates.
 */
data class LinePosRange(
    var anchor: LinePos,
    var head: LinePos
) {
    fun from(): LinePos = cursorMin(anchor, head)
    fun empty(): Boolean = cursorEqual(anchor, head)
}

// ---------------------------------------------------------------------------
// Search args
// ---------------------------------------------------------------------------

data class SearchArgs(
    val forward: Boolean? = null,
    val toJumplist: Boolean? = null,
    val wholeWordOnly: Boolean? = null,
    val querySrc: String? = null
)

// ---------------------------------------------------------------------------
// Operator args
// ---------------------------------------------------------------------------

data class OperatorArgs(
    var repeat: Int = 0,
    var forward: Boolean? = null,
    var linewise: Boolean? = null,
    var fullLine: Boolean? = null,
    var registerName: String? = null,
    var indentRight: Boolean? = null,
    var toLower: Boolean? = null,
    var shouldMoveCursor: Boolean? = null,
    var selectedCharacter: String? = null,
    var lastSel: LastSelection? = null,
    var keepCursor: Boolean? = null
)

data class LastSelection(
    val head: LinePos,
    val anchor: LinePos,
    val visualLine: Boolean,
    val visualBlock: Boolean
)

// ---------------------------------------------------------------------------
// Action args
// ---------------------------------------------------------------------------

data class ActionArgs(
    var repeat: Int = 1,
    var forward: Boolean? = null,
    var head: LinePos? = null,
    var position: String? = null,
    var backtrack: Boolean? = null,
    var increase: Boolean? = null,
    var repeatIsExplicit: Boolean? = null,
    var indentRight: Boolean? = null,
    var selectedCharacter: String? = null,
    var after: Boolean? = null,
    var matchIndent: Boolean? = null,
    var registerName: String? = null,
    var isEdit: Boolean? = null,
    var linewise: Boolean? = null,
    var insertAt: String? = null,
    var blockwise: Boolean? = null,
    var keepSpaces: Boolean? = null,
    var replace: Boolean? = null,
    var keepCursor: Boolean? = null
)

// ---------------------------------------------------------------------------
// Motion args
// ---------------------------------------------------------------------------

data class MotionArgs(
    var repeat: Int = 1,
    var forward: Boolean? = null,
    var selectedCharacter: String? = null,
    var linewise: Boolean? = null,
    var textObjectInner: Boolean? = null,
    var sameLine: Boolean? = null,
    var repeatOffset: Int? = null,
    var toJumplist: Boolean? = null,
    var inclusive: Boolean? = null,
    var wordEnd: Boolean? = null,
    var toFirstChar: Boolean? = null,
    var explicitRepeat: Boolean? = null,
    var bigWord: Boolean? = null,
    var repeatIsExplicit: Boolean? = null,
    var noRepeat: Boolean? = null
)

// ---------------------------------------------------------------------------
// Command types (the keymap entries)
// ---------------------------------------------------------------------------

sealed class VimKeyCommand {
    abstract val keys: String
    abstract val context: String?
    open val interlaceInsertRepeat: Boolean? = null
    open val exitVisualBlock: Boolean? = null
    open val isEdit: Boolean? = null
    open val repeatOverride: Int? = null
    open val noremap: Boolean? = null
}

data class MotionCommand(
    override val keys: String,
    val motion: String,
    val motionArgs: MotionArgs? = null,
    override val repeatOverride: Int? = null,
    override val context: String? = null,
    override val noremap: Boolean? = null
) : VimKeyCommand()

data class OperatorCommand(
    override val keys: String,
    val operator: String,
    val operatorArgs: OperatorArgs? = null,
    override val context: String? = null,
    override val isEdit: Boolean? = null,
    override val noremap: Boolean? = null
) : VimKeyCommand()

data class ActionCommand(
    override val keys: String,
    val action: String,
    val actionArgs: ActionArgs? = null,
    val motion: String? = null,
    val operator: String? = null,
    override val interlaceInsertRepeat: Boolean? = null,
    override val exitVisualBlock: Boolean? = null,
    override val isEdit: Boolean? = null,
    override val context: String? = null,
    override val noremap: Boolean? = null
) : VimKeyCommand()

data class SearchCommand(
    override val keys: String,
    val searchArgs: SearchArgs,
    override val context: String? = null,
    override val noremap: Boolean? = null
) : VimKeyCommand()

data class OperatorMotionCommand(
    override val keys: String,
    val motion: String,
    val operator: String,
    val motionArgs: MotionArgs? = null,
    val operatorArgs: OperatorArgs? = null,
    val operatorMotionArgs: OperatorMotionArgs? = null,
    override val context: String? = null,
    override val isEdit: Boolean? = null,
    override val exitVisualBlock: Boolean? = null,
    override val noremap: Boolean? = null
) : VimKeyCommand()

data class OperatorMotionArgs(
    val visualLine: Boolean? = null
)

data class IdleCommand(
    override val keys: String,
    override val context: String? = null,
    override val noremap: Boolean? = null
) : VimKeyCommand()

data class ExCommandMapping(
    override val keys: String,
    override val context: String? = null,
    override val noremap: Boolean? = null
) : VimKeyCommand()

data class KeyToExCommand(
    override val keys: String,
    val exArgs: ExParams,
    override val context: String? = null,
    override val noremap: Boolean? = null
) : VimKeyCommand()

data class KeyToKeyCommand(
    override val keys: String,
    val toKeys: String,
    override val context: String? = null,
    override val noremap: Boolean? = null
) : VimKeyCommand()

// ---------------------------------------------------------------------------
// Input state
// ---------------------------------------------------------------------------

class InputState {
    val prefixRepeat: MutableList<String> = mutableListOf()
    val motionRepeat: MutableList<String> = mutableListOf()
    var operator: String? = null
    var operatorArgs: OperatorArgs? = null
    var motion: String? = null
    var motionArgs: MotionArgs? = null
    val keyBuffer: MutableList<String> = mutableListOf()
    var registerName: String? = null
    var changeQueue: ChangeQueue? = null
    var operatorShortcut: String? = null
    var selectedCharacter: String? = null
    var repeatOverride: Int? = null
    var changeQueueList: MutableList<ChangeQueue?>? = null

    fun pushRepeatDigit(n: String) {
        if (operator != null) {
            motionRepeat.add(n)
        } else {
            prefixRepeat.add(n)
        }
    }

    fun getRepeat(): Int {
        var repeat = 0
        if (prefixRepeat.isNotEmpty() || motionRepeat.isNotEmpty()) {
            repeat = 1
            if (prefixRepeat.isNotEmpty()) {
                repeat *= prefixRepeat.joinToString("").toIntOrNull() ?: 1
            }
            if (motionRepeat.isNotEmpty()) {
                repeat *= motionRepeat.joinToString("").toIntOrNull() ?: 1
            }
        }
        return repeat
    }

    fun reset() {
        prefixRepeat.clear()
        motionRepeat.clear()
        operator = null
        operatorArgs = null
        motion = null
        motionArgs = null
        keyBuffer.clear()
        registerName = null
        changeQueue = null
        operatorShortcut = null
        selectedCharacter = null
        repeatOverride = null
        changeQueueList = null
    }
}

data class ChangeQueue(
    var inserted: String = "",
    val removed: MutableList<String> = mutableListOf()
)

// ---------------------------------------------------------------------------
// Search state
// ---------------------------------------------------------------------------

class SearchState {
    private var reversed: Boolean = false
    private var query: Regex? = null
    var highlightTimeout: Int? = null
    private var overlay: SearchOverlay? = null
    private var scrollbarAnnotate: Regex? = null

    fun setReversed(reversed: Boolean) {
        this.reversed = reversed
    }

    fun isReversed(): Boolean = reversed

    fun getQuery(): Regex? = query

    fun setQuery(query: Regex?) {
        this.query = query
    }

    fun setQuery(query: String) {
        this.query = Regex(Regex.escape(query))
    }

    fun getOverlay(): SearchOverlay? = overlay

    fun setOverlay(overlay: SearchOverlay?) {
        this.overlay = overlay
    }

    fun getScrollbarAnnotate(): Regex? = scrollbarAnnotate

    fun setScrollbarAnnotate(query: Regex?) {
        this.scrollbarAnnotate = query
    }
}

data class SearchOverlay(val query: Regex)

// ---------------------------------------------------------------------------
// Insert mode changes
// ---------------------------------------------------------------------------

class InsertModeChanges {
    val changes: MutableList<Any> = mutableListOf() // String | InsertModeKey | Pair<String,Int?>
    var expectCursorActivityForChange: Boolean = false
    var visualBlock: Int? = null
    var maybeReset: Boolean? = null
    var ignoreCount: Int? = null
    var repeatOverride: Int? = null
}

data class InsertModeKey(val keyName: String)

// ---------------------------------------------------------------------------
// Vim state (per-editor)
// ---------------------------------------------------------------------------

class VimState {
    var onPasteFn: (() -> Unit)? = null
    var sel: LinePosRange = LinePosRange(LinePos(0, 0), LinePos(0, 0))
    var insertModeReturn: Boolean = false
    var visualBlock: Boolean = false
    val marks: MutableMap<String, Marker> = mutableMapOf()
    var visualMode: Boolean = false
    var insertMode: Boolean = false
    var pasteFn: Any? = null
    var lastSelection: VimLastSelection? = null

    @Suppress("ktlint:standard:property-naming")
    var searchState_: SearchState? = null
    var lastEditActionCommand: ActionCommand? = null
    var lastPastedText: String? = null
    var lastMotion: MotionFn? = null
    val options: MutableMap<String, VimOption> = mutableMapOf()
    var lastEditInputState: InputState? = null
    var inputState: InputState = InputState()
    var visualLine: Boolean = false
    var insertModeRepeat: Int? = null
    var lastHSPos: Int = 0
    var lastHPos: Int = 0
    var wasInVisualBlock: Boolean? = null
    var insertEnd: Marker? = null
    var status: String = ""
    var exMode: Boolean = false
    var mode: String? = null
    var expectLiteralNext: Boolean = false
    var overwrite: Boolean = false
    var keyMap: String? = null
    var textwidth: Int? = null
}

data class VimLastSelection(
    var anchorMark: Marker,
    var headMark: Marker,
    var visualLine: Boolean,
    var visualBlock: Boolean,
    var visualMode: Boolean,
    var anchor: LinePos,
    var head: LinePos
)

// ---------------------------------------------------------------------------
// Ex command params
// ---------------------------------------------------------------------------

data class ExParams(
    var commandName: String = "",
    var argString: String = "",
    var input: String = "",
    var args: MutableList<String>? = null,
    var line: Int = 0,
    var lineEnd: Int? = null,
    var selectionLine: Int = 0,
    var selectionLineEnd: Int? = null,
    var setCfg: MutableMap<String, String>? = null,
    var callback: (() -> Unit)? = null
)

// ---------------------------------------------------------------------------
// Ex command definition
// ---------------------------------------------------------------------------

data class ExCommandDefinition(
    val name: String,
    val shortName: String? = null,
    val possiblyAsync: Boolean? = null,
    val excludeFromCommandHistory: Boolean? = null,
    val argDelimiter: String? = null,
    val type: String? = null,
    val toKeys: String? = null,
    val toInput: String? = null,
    val user: Boolean? = null,
    val noremap: Boolean? = null
)

// ---------------------------------------------------------------------------
// Vim option
// ---------------------------------------------------------------------------

data class VimOption(
    val type: String? = null,
    var defaultValue: Any? = null,
    var callback: ((Any?, CodeMirrorAdapter?) -> Any?)? = null,
    var value: Any? = null
)

// ---------------------------------------------------------------------------
// Prompt options (for ex command line)
// ---------------------------------------------------------------------------

data class PromptOptions(
    val onClose: ((String?) -> Unit)? = null,
    val prefix: String = "",
    val desc: String? = null,
    val onKeyUp: ((event: VimKeyEvent) -> Boolean)? = null,
    val onKeyDown: ((event: VimKeyEvent) -> Boolean)? = null,
    var value: String? = null,
    val selectValueOnOpen: Boolean? = null
)

data class VimKeyEvent(
    val keyCode: Int = 0,
    val key: String = ""
)

// ---------------------------------------------------------------------------
// Function type aliases
// ---------------------------------------------------------------------------

typealias MotionFn = (
    cm: CodeMirrorAdapter,
    head: LinePos,
    motionArgs: MotionArgs,
    vim: VimState,
    inputState: InputState
) -> MotionResult?

typealias OperatorFn = (
    cm: CodeMirrorAdapter,
    args: OperatorArgs,
    ranges: List<LinePosRange>,
    oldAnchor: LinePos,
    newHead: LinePos?
) -> LinePos?

typealias ActionFn = (
    cm: CodeMirrorAdapter,
    actionArgs: ActionArgs,
    vim: VimState
) -> Unit

typealias ExFn = (
    cm: CodeMirrorAdapter,
    params: ExParams
) -> Unit

/**
 * Result of a motion: either a single position or a pair (anchor, head).
 */
sealed class MotionResult {
    data class SinglePos(val pos: LinePos) : MotionResult()
    data class Range(val anchor: LinePos, val head: LinePos) : MotionResult()

    companion object {
        fun from(pos: LinePos): MotionResult = SinglePos(pos)
        fun from(anchor: LinePos, head: LinePos): MotionResult = Range(anchor, head)
    }
}

fun MotionResult?.toPos(): LinePos? = when (this) {
    is MotionResult.SinglePos -> pos
    is MotionResult.Range -> head
    null -> null
}
