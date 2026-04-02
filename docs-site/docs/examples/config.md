# Dynamic Configuration

Extensions are normally fixed when you create an `EditorState`. To
change configuration at runtime — switch languages, toggle features,
change themes — use `Compartment`.

<div class="demo-embed">
<iframe src="../../showcase/index.html?demo=config" loading="lazy"></iframe>
</div>

## Compartments

A `Compartment` wraps part of your extension configuration so it can be
replaced later via an effect. Here is the demo's compartment setup:

```kotlin
--8<-- "samples/showcase/src/commonMain/kotlin/com/monkopedia/kodemirror/samples/showcase/demos/ConfigDemo.kt:compartment-setup"
```

## Switching configuration

To reconfigure at runtime, dispatch a transaction with the compartment's
`reconfigure` effect:

```kotlin
--8<-- "samples/showcase/src/commonMain/kotlin/com/monkopedia/kodemirror/samples/showcase/demos/ConfigDemo.kt:reconfigure-lang"
```

The editor re-parses the document with the new language and updates
highlighting immediately.

## Toggle example

A compartment can also wrap a feature you want to enable/disable:

```kotlin
val lineNumberCompartment = Compartment()

// Start with line numbers on
val session = rememberEditorSession(
    doc = "...",
    extensions = lineNumberCompartment.of(lineNumbers) + // ...
)

// Toggle off — replace with an empty extension list
fun toggleLineNumbers(session: EditorSession, enabled: Boolean) {
    session.dispatch(TransactionSpec(
        effects = listOf(
            lineNumberCompartment.reconfigure(
                if (enabled) lineNumbers
                else ExtensionList(emptyList())
            )
        )
    ))
}
```

## Theme switching

Themes work the same way:

```kotlin
import com.monkopedia.kodemirror.view.*
import com.monkopedia.kodemirror.themonedark.oneDark

val themeCompartment = Compartment()

val session = rememberEditorSession(
    doc = "...",
    extensions = themeCompartment.of(oneDark) + // ...
)

// Switch to light theme
fun switchToLight(session: EditorSession) {
    session.dispatch(TransactionSpec(
        effects = listOf(
            themeCompartment.reconfigure(editorTheme.of(lightEditorTheme))
        )
    ))
}
```

## Reading current configuration

You can query a compartment's current content:

```kotlin
val currentLanguage = languageCompartment.get(state)
```

## Related API

- [`Compartment`](/api/state/com.monkopedia.kodemirror.state/-compartment/) — dynamic extension reconfiguration
- [`Extension`](/api/state/com.monkopedia.kodemirror.state/-extension/) — base extension type
- [`Facet`](/api/state/com.monkopedia.kodemirror.state/-facet/) — facet system for extension composition
- [`StateEffect`](/api/state/com.monkopedia.kodemirror.state/-state-effect/) — state effect for reconfiguration

---

*Based on the [CodeMirror Configuration example](https://codemirror.net/examples/config/).*
