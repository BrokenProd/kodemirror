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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.monkopedia.kodemirror.autocomplete.autocompletion
import com.monkopedia.kodemirror.autocomplete.closeBrackets
import com.monkopedia.kodemirror.autocomplete.closeBracketsKeymap
import com.monkopedia.kodemirror.autocomplete.completionKeymap
import com.monkopedia.kodemirror.commands.defaultKeymap
import com.monkopedia.kodemirror.commands.emacsStyleKeymap
import com.monkopedia.kodemirror.commands.history
import com.monkopedia.kodemirror.commands.standardKeymap
import com.monkopedia.kodemirror.lang.javascript.javascript
import com.monkopedia.kodemirror.language.bracketMatching
import com.monkopedia.kodemirror.language.defaultHighlightStyle
import com.monkopedia.kodemirror.language.foldGutter
import com.monkopedia.kodemirror.language.foldKeymap
import com.monkopedia.kodemirror.language.indentOnInput
import com.monkopedia.kodemirror.language.syntaxHighlighting
import com.monkopedia.kodemirror.lint.lintKeymap
import com.monkopedia.kodemirror.samples.showcase.DemoScaffold
import com.monkopedia.kodemirror.samples.showcase.SampleDocs
import com.monkopedia.kodemirror.search.highlightSelectionMatches
import com.monkopedia.kodemirror.search.searchKeymap
import com.monkopedia.kodemirror.state.Compartment
import com.monkopedia.kodemirror.state.TransactionSpec
import com.monkopedia.kodemirror.state.allowMultipleSelections
import com.monkopedia.kodemirror.state.extensionListOf
import com.monkopedia.kodemirror.state.plus
import com.monkopedia.kodemirror.themonedark.oneDark
import com.monkopedia.kodemirror.view.KodeMirror
import com.monkopedia.kodemirror.view.KeyBinding
import com.monkopedia.kodemirror.view.crosshairCursor
import com.monkopedia.kodemirror.view.drawSelection
import com.monkopedia.kodemirror.view.dropCursor
import com.monkopedia.kodemirror.view.highlightActiveLine
import com.monkopedia.kodemirror.view.highlightActiveLineGutter
import com.monkopedia.kodemirror.view.highlightSpecialChars
import com.monkopedia.kodemirror.view.keymapOf
import com.monkopedia.kodemirror.view.lineNumbers
import com.monkopedia.kodemirror.view.rectangularSelection
import com.monkopedia.kodemirror.view.rememberEditorSession

private data class BindingInfo(
    val key: String,
    val description: String,
    val category: String
)

private enum class KeymapPreset(
    val label: String,
    val bindings: List<KeyBinding>,
    val referenceCard: List<BindingInfo>
) {
    STANDARD(
        label = "Standard",
        bindings = standardKeymap,
        referenceCard = standardBindings
    ),
    DEFAULT(
        label = "Default",
        bindings = defaultKeymap,
        referenceCard = standardBindings + defaultExtraBindings
    ),
    EMACS(
        label = "Emacs",
        bindings = standardKeymap + emacsStyleKeymap,
        referenceCard = standardBindings + emacsBindings
    )
}

private val baseExtensions = extensionListOf(
    lineNumbers,
    highlightActiveLineGutter,
    highlightSpecialChars,
    history(),
    foldGutter(),
    drawSelection,
    dropCursor,
    allowMultipleSelections.of(true),
    indentOnInput,
    syntaxHighlighting(defaultHighlightStyle, fallback = true),
    bracketMatching(),
    closeBrackets(),
    autocompletion(),
    rectangularSelection,
    crosshairCursor,
    highlightActiveLine,
    highlightSelectionMatches()
)

private val utilityKeymaps = keymapOf(
    closeBracketsKeymap + searchKeymap + foldKeymap + completionKeymap + lintKeymap
)

@Composable
fun KeybindingsDemo() {
    var preset by remember { mutableStateOf(KeymapPreset.DEFAULT) }
    val keymapCompartment = remember { Compartment() }

    val session = rememberEditorSession(
        doc = SampleDocs.javascript,
        extensions = baseExtensions +
            utilityKeymaps +
            javascript().extension +
            oneDark +
            keymapCompartment.of(keymapOf(preset.bindings))
    )

    DemoScaffold(
        title = "Keybindings",
        description = "Switch between keymap presets and see the active bindings.",
        controls = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KeymapPreset.entries.forEach { choice ->
                    FilterChip(
                        selected = preset == choice,
                        onClick = {
                            preset = choice
                            session.dispatch(
                                TransactionSpec(
                                    effects = listOf(
                                        keymapCompartment.reconfigure(
                                            keymapOf(choice.bindings)
                                        )
                                    )
                                )
                            )
                        },
                        label = { Text(choice.label) }
                    )
                }
            }
        }
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            ReferenceCard(
                bindings = preset.referenceCard,
                modifier = Modifier.weight(0.4f).fillMaxSize()
            )
            KodeMirror(
                session = session,
                modifier = Modifier.weight(0.6f).fillMaxSize()
            )
        }
    }
}

@Composable
private fun ReferenceCard(bindings: List<BindingInfo>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.padding(end = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        val grouped = bindings.groupBy { it.category }
        grouped.forEach { (category, items) ->
            item(key = "header-$category") {
                Text(
                    text = category,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }
            items(items, key = { "${it.category}-${it.key}" }) { binding ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = binding.key,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
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

// --- Static reference card data ---

private val standardBindings = listOf(
    // Movement
    BindingInfo("Arrow Keys", "Move cursor", "Movement"),
    BindingInfo("Ctrl-Left/Right", "Move by word", "Movement"),
    BindingInfo("Home / End", "Line start / end", "Movement"),
    BindingInfo("Ctrl-Home/End", "Document start / end", "Movement"),
    BindingInfo("PageUp / PageDown", "Page up / down", "Movement"),
    // Selection
    BindingInfo("Shift-Arrow", "Extend selection", "Selection"),
    BindingInfo("Ctrl-a", "Select all", "Selection"),
    // Deletion
    BindingInfo("Backspace", "Delete backward", "Deletion"),
    BindingInfo("Delete", "Delete forward", "Deletion"),
    BindingInfo("Ctrl-Backspace", "Delete word backward", "Deletion"),
    BindingInfo("Ctrl-Delete", "Delete word forward", "Deletion"),
    // Text
    BindingInfo("Enter", "New line & indent", "Text"),
    // Clipboard
    BindingInfo("Ctrl-c", "Copy", "Clipboard"),
    BindingInfo("Ctrl-x", "Cut", "Clipboard"),
    BindingInfo("Ctrl-v", "Paste", "Clipboard")
)

private val defaultExtraBindings = listOf(
    // Line Operations
    BindingInfo("Alt-Up/Down", "Move line up / down", "Line Operations"),
    BindingInfo("Shift-Alt-Up/Down", "Copy line up / down", "Line Operations"),
    BindingInfo("Ctrl-Shift-k", "Delete line", "Line Operations"),
    // Indentation
    BindingInfo("Ctrl-]", "Indent more", "Indentation"),
    BindingInfo("Ctrl-[", "Indent less", "Indentation"),
    // Comments
    BindingInfo("Ctrl-/", "Toggle line comment", "Comments"),
    BindingInfo("Alt-Shift-a", "Toggle block comment", "Comments"),
    // Navigation
    BindingInfo("Ctrl-Shift-\\", "Go to matching bracket", "Navigation"),
    BindingInfo("Ctrl-t", "Transpose characters", "Navigation"),
    BindingInfo("Ctrl-d", "Select next occurrence", "Navigation")
)

private val emacsBindings = listOf(
    BindingInfo("Ctrl-b / Ctrl-f", "Char left / right", "Emacs Movement"),
    BindingInfo("Ctrl-p / Ctrl-n", "Line up / down", "Emacs Movement"),
    BindingInfo("Ctrl-a / Ctrl-e", "Line start / end", "Emacs Movement"),
    BindingInfo("Ctrl-d", "Delete forward", "Emacs Editing"),
    BindingInfo("Ctrl-h", "Delete backward", "Emacs Editing"),
    BindingInfo("Ctrl-k", "Delete to line end", "Emacs Editing"),
    BindingInfo("Ctrl-t", "Transpose characters", "Emacs Editing")
)
