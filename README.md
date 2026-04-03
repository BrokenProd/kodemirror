# KodeMirror

A Kotlin Multiplatform port of [CodeMirror 6](https://codemirror.net/), providing a Compose Multiplatform code editor component with syntax highlighting, vim mode, search/replace, autocompletion, and more.

[Documentation](https://monkopedia.github.io/kodemirror/) · [Live Demo](https://monkopedia.github.io/kodemirror/showcase/) · [API Reference](https://monkopedia.github.io/kodemirror/reference/)

## Platform Support

| Platform | Status |
|----------|--------|
| wasmJs (Browser) | ✅ Tested — automated gap tests + manual browser testing |
| JVM Desktop | 🔶 Unit tests pass, visual rendering lightly tested |
| Android | 🔶 Unit tests pass, untested on devices |
| iOS (arm64, simulatorArm64, x64) | 🔶 Compiles, experimental |
| macOS native (arm64, x64) | 🔶 Compiles, experimental |

## Quick Start

Add the BOM and modules you need:

```kotlin
// build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(platform("com.monkopedia.kodemirror:kodemirror-bom:0.1.0"))
            implementation("com.monkopedia.kodemirror:view")
            implementation("com.monkopedia.kodemirror:commands")
            implementation("com.monkopedia.kodemirror:basic-setup")
            // Language support (pick what you need)
            implementation("com.monkopedia.kodemirror:lang-javascript")
        }
    }
}
```

## Usage

```kotlin
@Composable
fun Editor() {
    val session = rememberEditorSession(
        doc = "function hello() {\n    return 'world';\n}",
        extensions = basicSetup + javascript().extension
    )
    KodeMirror(session = session, modifier = Modifier.fillMaxSize())
}
```

## Modules

### Core
- **state** — Editor state, transactions, selections, facets
- **view** — Compose UI component, decorations, key handling
- **language** — Language support infrastructure, syntax highlighting
- **commands** — Standard editor commands (cursor movement, delete, indent, undo/redo)
- **basic-setup** — Convenience bundle combining common extensions

### Features
- **search** — Find/replace panel, search commands
- **autocomplete** — Completion popup and sources
- **lint** — Diagnostic display and linting
- **collab** — Collaborative editing support
- **merge** — Side-by-side diff view
- **vim** — Vim keybindings and modal editing

### Language Support
JavaScript, TypeScript, HTML, CSS, Python, Java, Go, Rust, Markdown, JSON, YAML, XML, SQL, C++, PHP, and more — 20+ languages via dedicated modules plus 100+ via `legacy-modes`.

### Themes
- **theme-one-dark** — One Dark theme
- **theme-dracula** — Dracula theme
- **theme-github-light** — GitHub Light theme
- **material-theme** — Material Design integration

## Known Issues

See [open issues](https://github.com/Monkopedia/kodemirror/issues) for known bugs and planned improvements.

## License

```
Copyright 2026 Jason Monk

Licensed under the Apache License, Version 2.0
```

Originally based on CodeMirror 6 by Marijn Haverbeke, licensed under MIT. See [NOTICE](NOTICE) for details.
