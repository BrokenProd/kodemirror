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

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.monkopedia.kodemirror.view.LocalEditorSession
import com.monkopedia.kodemirror.view.Panel

/**
 * Create a status panel that always shows the current vim mode and status.
 */
internal fun createStatusPanel(): Panel = Panel(top = false) {
    val session = LocalEditorSession.current
    val plugin = session.plugin(vimPlugin)
    val cm = plugin?.cm
    val vim = cm?.vim

    VimStatusContent(vim)
}

/**
 * Create a dialog panel shown when an ex command or search prompt is active.
 */
internal fun createVimDialogPanel(): Panel = Panel(top = false) {
    val session = LocalEditorSession.current
    val plugin = session.plugin(vimPlugin)
    val cm = plugin?.cm
    val vim = cm?.vim

    VimStatusContent(vim)
}

private val monoStyle = TextStyle(fontFamily = FontFamily.Monospace)

@Composable
private fun VimStatusContent(vim: VimState?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val modeText = if (vim != null) {
            val mode = (vim.mode ?: "normal").uppercase()
            val suffix = if (vim.insertModeReturn) "(C-O)" else ""
            "--$mode$suffix--"
        } else {
            ""
        }
        BasicText(
            text = modeText,
            style = monoStyle
        )
        Spacer(modifier = Modifier.weight(1f))
        BasicText(
            text = vim?.status ?: "",
            style = monoStyle
        )
    }
}
