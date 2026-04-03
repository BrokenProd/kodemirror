# Changelog

All notable changes to Kodemirror will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

## [0.1.0] - 2026-04-03

Initial release of Kodemirror — a Kotlin Multiplatform port of CodeMirror 6.

### Highlights
- Full Compose Multiplatform code editor component
- Vim mode with 600+ ported upstream tests
- Real browser keyboard input pipeline (no JS bridges)
- 183 automated gap tests verifying CM6 parity
- 57 keymap command tests covering all standard/emacs bindings
- 20+ language modules plus 100+ legacy modes
- Live interactive documentation with embedded demos

### Platform Support
- **wasmJs**: Fully tested with automated gap tests and manual browser testing
- **JVM Desktop**: Unit tests pass, visual rendering lightly tested
- **Android**: Unit tests pass via CI
- **iOS / macOS native**: Compiles, experimental (CI on PR/manual trigger)

### Added
- `extensionListOf(vararg Extension)` factory function
- `operator fun Extension.plus(other: Extension)` for combining extensions
- `operator fun Text.get(range: IntRange)` for slicing text
- `operator fun EditorState.get(field: StateField<T>)` for field access
- `String.asInsert()` extension for converting strings to `InsertContent`
- `Int.asCursor()` extension for creating cursor `SelectionSpec`
- `EditorSelection.asSpec()` extension for converting to `SelectionSpec`
- `EditorState.create(doc: String, ...)` convenience overload
- `EditorState.currentLine`, `.selectedText`, `.cursorPosition` properties
- `Text.isEmpty`, `.isNotEmpty`, `.lineSequence()` properties
- `keymapOf(bindings: List<KeyBinding>)` overload accepting a list
- `keymapOf { }` DSL builder for key bindings
- `selectSelectionMatches` search command
- `SearchQuery.validOrNull()` factory method
- `CompletionType` enum for standard completion types
- `pickedCompletion` annotation for tracking accepted completions
- `ifIn` / `ifNotIn` context-aware completion source wrappers
- `hasNextSnippetField` / `hasPrevSnippetField` snippet state queries
- `StateField.define<T> { create { }; update { } }` DSL builder
- `HighlightStyle.define { Tags.keyword styles SpanStyle(...) }` DSL builder
- `Decoration.mark(style: SpanStyle, ...)` convenience overload
- `Decoration.line(style: SpanStyle, ...)` convenience overload
- `operator fun StateField<T>.getValue(EditorState, KProperty)` property delegate
- `editorThemeFromColors()` factory for Material Design color scheme integration
- `inputSuppressor` facet for blocking text input (used by vim normal mode)
- `blockCursorProvider` facet for overlay-based block cursor rendering
- `keyEventLayoutKey()` public API for keyboard layout-aware key names
- Tab character rendering via column-aware expansion in `buildLineContentWithTabs`
- Vim mode status bar with `StateField`-based mode tracking
- Vim `:` / `/` / `?` command prompt panel
- Polished search panel with rounded corners and proper styling

### Changed
- Renamed `tags` object to `Tags` (PascalCase convention)
- Renamed `jsHighlight` to `jsHighlighting` (consistent with other modules)
- Renamed `tagLanguage` to `jinjaTagLanguage` / `liquidTagLanguage`
- Converted `Prec` lambda properties to functions
- Made `phpHighlighting` and `angularHighlighting` public
- Block cursor rendered via selection overlay (`DrawScope`) instead of decorations
- Keymap precedence: later extensions override earlier ones (matching CM6)
- `defaultEditorFontFamily` changed to `FontFamily.Monospace` (cross-platform)

### Removed
- `StateEffect.is()` method (use `asType()` instead)

### Internal
- Made lezer-lr internal state `internal`
- Filtered `ComposableSingletons` from public API dumps
- Document-level key handler for wasmJs keyboard routing
- `platformFocusInput()` / `platformRegisterKeyHandler()` expect/actual APIs
