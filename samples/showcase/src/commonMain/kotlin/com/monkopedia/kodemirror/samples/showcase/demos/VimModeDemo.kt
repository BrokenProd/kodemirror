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
package com.monkopedia.kodemirror.samples.showcase.demos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.monkopedia.kodemirror.lang.javascript.javascript
import com.monkopedia.kodemirror.samples.showcase.DemoScaffold
import com.monkopedia.kodemirror.samples.showcase.SampleDocs
import com.monkopedia.kodemirror.samples.showcase.showcaseSetup
import com.monkopedia.kodemirror.state.Compartment
import com.monkopedia.kodemirror.state.Extension
import com.monkopedia.kodemirror.state.ExtensionList
import com.monkopedia.kodemirror.state.TransactionSpec
import com.monkopedia.kodemirror.state.plus
import com.monkopedia.kodemirror.view.KodeMirror
import com.monkopedia.kodemirror.view.rememberEditorSession
import com.monkopedia.kodemirror.vim.vim

@Composable
fun VimModeDemo() {
    var vimEnabled by remember { mutableStateOf(true) }
    val vimCompartment = remember { Compartment() }

    val session = rememberEditorSession(
        doc = SampleDocs.javascript,
        extensions = showcaseSetup + javascript().extension +
            vimCompartment.of(if (vimEnabled) vim(status = true) else emptyExtension)
    )

    DemoScaffold(
        title = "Vim Mode",
        description = "Modal editing with vim keybindings. " +
            "Toggle vim mode on/off with the switch.",
        controls = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Vim Mode", modifier = Modifier.padding(end = 8.dp))
                Switch(
                    checked = vimEnabled,
                    onCheckedChange = {
                        vimEnabled = it
                        session.dispatch(
                            TransactionSpec(
                                effects = listOf(
                                    vimCompartment.reconfigure(
                                        if (it) vim(status = true) else emptyExtension
                                    )
                                )
                            )
                        )
                    }
                )
            }
        }
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            VimReferenceCard(modifier = Modifier.weight(0.35f))
            KodeMirror(
                session = session,
                modifier = Modifier.weight(0.65f)
            )
        }
    }
}

private val emptyExtension: Extension = ExtensionList(emptyList())

private data class VimBindingInfo(
    val key: String,
    val description: String,
    val category: String
)

private val vimBindings = listOf(
    // Movement
    VimBindingInfo("h / j / k / l", "Move left / down / up / right", "Movement"),
    VimBindingInfo("w / b", "Next / previous word", "Movement"),
    VimBindingInfo("e", "End of word", "Movement"),
    VimBindingInfo("0 / $", "Start / end of line", "Movement"),
    VimBindingInfo("^", "First non-blank character", "Movement"),
    VimBindingInfo("gg / G", "Go to first / last line", "Movement"),
    VimBindingInfo("f{c} / t{c}", "Find / till character", "Movement"),
    VimBindingInfo("% ", "Matching bracket", "Movement"),

    // Editing
    VimBindingInfo("i / a", "Insert before / after cursor", "Editing"),
    VimBindingInfo("I / A", "Insert at line start / end", "Editing"),
    VimBindingInfo("o / O", "Open line below / above", "Editing"),
    VimBindingInfo("x / X", "Delete char forward / backward", "Editing"),
    VimBindingInfo("r{c}", "Replace character", "Editing"),
    VimBindingInfo("dd", "Delete line", "Editing"),
    VimBindingInfo("cc", "Change line", "Editing"),
    VimBindingInfo("yy", "Yank (copy) line", "Editing"),
    VimBindingInfo("p / P", "Paste after / before", "Editing"),
    VimBindingInfo("u / Ctrl-r", "Undo / redo", "Editing"),
    VimBindingInfo(".", "Repeat last change", "Editing"),

    // Operators
    VimBindingInfo("d{motion}", "Delete", "Operators"),
    VimBindingInfo("c{motion}", "Change", "Operators"),
    VimBindingInfo("y{motion}", "Yank (copy)", "Operators"),
    VimBindingInfo("> / <", "Indent / unindent", "Operators"),

    // Visual mode
    VimBindingInfo("v", "Visual (character)", "Visual Mode"),
    VimBindingInfo("V", "Visual line", "Visual Mode"),
    VimBindingInfo("Ctrl-v", "Visual block", "Visual Mode"),

    // Search
    VimBindingInfo("/{pattern}", "Search forward", "Search"),
    VimBindingInfo("?{pattern}", "Search backward", "Search"),
    VimBindingInfo("n / N", "Next / previous match", "Search"),
    VimBindingInfo("* / #", "Search word under cursor", "Search"),

    // Ex commands
    VimBindingInfo(":w", "Write (save)", "Ex Commands"),
    VimBindingInfo(":s/old/new/g", "Substitute", "Ex Commands"),
    VimBindingInfo(":{n}", "Go to line n", "Ex Commands"),
    VimBindingInfo(":noh", "Clear search highlight", "Ex Commands"),
)

@Composable
private fun VimReferenceCard(modifier: Modifier = Modifier) {
    val grouped = vimBindings.groupBy { it.category }

    LazyColumn(modifier = modifier.padding(end = 8.dp)) {
        grouped.forEach { (category, items) ->
            item(key = "header-$category") {
                Text(
                    text = category,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                )
            }
            items(items, key = { "binding-${it.key}" }) { binding ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = binding.key,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(0.4f)
                    )
                    Text(
                        text = binding.description,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(0.6f)
                    )
                }
            }
        }
    }
}
