# Basic Setup

A minimal Kodemirror editor with common features enabled.

<div class="demo-embed">
<iframe src="../../showcase/index.html?demo=basic" loading="lazy"></iframe>
</div>

## Minimal editor

The simplest possible editor needs `rememberEditorSession` and the
`KodeMirror` composable:

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.monkopedia.kodemirror.view.*

@Composable
fun MinimalEditor() {
    val session = rememberEditorSession(doc = "Hello, world!")
    KodeMirror(session = session, modifier = Modifier.fillMaxSize())
}
```

This gives you an editable text area, but without line numbers, syntax
highlighting, or keybindings.

## Adding common extensions

A more useful editor adds line numbers, a keymap, undo history,
bracket matching, and syntax highlighting. Here is the live demo's
setup code:

```kotlin
--8<-- "samples/showcase/src/commonMain/kotlin/com/monkopedia/kodemirror/samples/showcase/demos/BasicDemo.kt:basic-setup"
```

!!! note
    The demo uses `showcaseSetup`, a pre-built extension bundle similar
    to upstream's `basicSetup`. See `showcaseSetup` for what it includes.

Each function call returns an `Extension` that plugs into the editor's
configuration. Extensions compose freely — you can add or remove any of
them independently.

## What each extension does

| Extension | Module | Purpose |
|-----------|--------|---------|
| `lineNumbers` | `:view` | Shows line numbers in the gutter |
| `highlightActiveLine` | `:view` | Highlights the line the cursor is on |
| `highlightSpecialChars` | `:view` | Makes control characters visible |
| `history()` | `:commands` | Enables undo/redo (Ctrl-Z / Ctrl-Shift-Z) |
| `bracketMatching()` | `:language` | Highlights matching brackets |
| `highlightSelectionMatches()` | `:search` | Highlights other occurrences of the selected text |
| `defaultKeymapExtension()` | `:commands` | Standard cursor movement and editing bindings |
| `keymapOf(*indentWithTab.toTypedArray())` | `:commands` | Tab/Shift-Tab for indentation |
| `javascript()` | `:lang-javascript` | JavaScript language support (parsing, highlighting, indentation) |
| `syntaxHighlighting(defaultHighlightStyle)` | `:language` | Applies colors to syntax tokens |

---

*Based on the [CodeMirror Basic Setup example](https://codemirror.net/examples/bundle/).*
