# Tab Handling

By default, the Tab key moves focus to the next UI element (standard
Compose behavior). You can override this to indent code instead.

<div class="demo-embed">
<iframe src="../../showcase/index.html?demo=tab" loading="lazy"></iframe>
</div>

## Tab extension

The demo defines a function that returns either an indent-with-tab
extension or a literal-tab-insert extension:

```kotlin
--8<-- "samples/showcase/src/commonMain/kotlin/com/monkopedia/kodemirror/samples/showcase/demos/TabDemo.kt:tab-extension"
```

- **`TabMode.INDENT`** uses `indentWithTab` from the `:commands` module,
  which maps Tab to `indentMore` and Shift-Tab to `indentLess`.
- **`TabMode.INSERT`** inserts a literal tab character at the cursor.

## Accessibility note

Capturing Tab prevents keyboard-only users from tabbing out of the
editor. Consider providing an alternative escape mechanism (e.g., Escape
to release focus) if your editor is part of a larger form.

## Related API

- [`KeyBinding`](/api/view/com.monkopedia.kodemirror.view/-key-binding/) — key binding data class
- [`keymapOf`](/api/view/com.monkopedia.kodemirror.view/keymap-of.html) — create keymap extension
- [`indentWithTab`](/api/commands/com.monkopedia.kodemirror.commands/indent-with-tab.html) — tab indentation binding

---

*Based on the [CodeMirror Tab Handling example](https://codemirror.net/examples/tab/).*
