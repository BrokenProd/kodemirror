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
import androidx.compose.foundation.layout.defaultMinSize
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
import com.monkopedia.kodemirror.view.LocalEditorTheme
import com.monkopedia.kodemirror.view.Panel

/**
 * Create a status panel that always shows the current vim mode and status.
 */
internal fun createStatusPanel(): Panel = Panel(top = false) {
    val prompt = virtualPrompt
    if (prompt != null) {
        VimPromptContent(prompt)
    } else {
        val session = LocalEditorSession.current
        val plugin = session.plugin(vimPlugin)
        val cm = plugin?.cm
        val vim = cm?.vim
        val modeFromField = session.state.field(vimModeField)
        VimStatusContent(vim, modeFromField)
    }
}

/**
 * Create a dialog panel shown when an ex command or search prompt is active.
 */
internal fun createVimDialogPanel(): Panel = Panel(top = false) {
    val prompt = virtualPrompt
    if (prompt != null) {
        VimPromptContent(prompt)
    } else {
        val session = LocalEditorSession.current
        val plugin = session.plugin(vimPlugin)
        val cm = plugin?.cm
        val vim = cm?.vim
        val modeFromField = session.state.field(vimModeField)
        VimStatusContent(vim, modeFromField)
    }
}

/**
 * Build a monospace text style using the theme's foreground color.
 * Must be called from a composable context to read [LocalEditorTheme].
 */
@Composable
private fun monoStyle(): TextStyle {
    val theme = LocalEditorTheme.current
    return TextStyle(fontFamily = FontFamily.Monospace, color = theme.foreground)
}

@Composable
private fun VimPromptContent(prompt: PromptOptions) {
    val style = monoStyle()
    Row(
        modifier = Modifier.fillMaxWidth()
            .defaultMinSize(minHeight = 24.dp)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicText(
            text = prompt.prefix + (prompt.value ?: ""),
            style = style
        )
    }
}

@Composable
private fun VimStatusContent(vim: VimState?, modeFromField: String? = null) {
    val style = monoStyle()
    Row(
        modifier = Modifier.fillMaxWidth()
            .defaultMinSize(minHeight = 24.dp)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val modeText = if (vim != null || modeFromField != null) {
            val mode = (modeFromField ?: vim?.mode ?: "normal").uppercase()
            val suffix = if (vim?.insertModeReturn == true) "(C-O)" else ""
            "--$mode$suffix--"
        } else {
            ""
        }
        BasicText(
            text = modeText,
            style = style
        )
        Spacer(modifier = Modifier.weight(1f))
        BasicText(
            text = vim?.status ?: "",
            style = style
        )
    }
}
