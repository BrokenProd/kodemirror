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

import com.monkopedia.kodemirror.state.StateField
import com.monkopedia.kodemirror.state.define

/**
 * Per-editor vim context that holds all mutable state previously stored
 * in the global [VimGlobalState] singleton. Each editor instance gets
 * its own [VimContext] via the [vimContextField] [StateField], so
 * multiple editors no longer share registers, macro state, jump lists,
 * search history, etc.
 */
internal class VimContext {
    val jumpList = CircularJumpList()
    var registerController = RegisterController()
    var macroModeState = MacroModeState()
    var lastCharacterSearch = LastCharacterSearch()
    var lastSubstituteReplacePart: String? = null
    var query: Regex? = null
    var isReversed: Boolean = false
    val searchHistoryController = HistoryController()
    val exCommandHistoryController = HistoryController()

    /** Pending key-to-key mapping stack (was top-level `keyToKeyStack`). */
    val keyToKeyStack: MutableList<VimKeyCommand> = mutableListOf()

    /** Whether key-to-key mapping is in noremap mode (was top-level `noremap`). */
    var noremap: Boolean = false

    /** Active virtual prompt for test/headless mode (was top-level `virtualPrompt`). */
    var virtualPrompt: PromptOptions? = null
}

/**
 * [StateField] that stores the per-editor [VimContext]. The context is
 * mutable state that persists across transactions (the update function
 * simply passes it through unchanged).
 *
 * Must be included in the vim extension bundle so it is always present
 * when vim mode is active.
 */
internal val vimContextField: StateField<VimContext> = StateField.define<VimContext> {
    create { VimContext() }
    update { value, _ -> value }
}

/**
 * Convenience extension to retrieve the [VimContext] from a [VimEditor].
 */
internal val VimEditor.vimContext: VimContext
    get() = session.state.field(vimContextField)
