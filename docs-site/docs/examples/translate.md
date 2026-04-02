# Internationalization

Kodemirror supports translating UI strings through the `phrases` facet
and the `phrase()` method. This lets you localize built-in messages
(tooltips, ARIA labels, panel headings, etc.) without forking the
source.

<div class="demo-embed">
<iframe src="../../showcase/index.html?demo=translate" loading="lazy"></iframe>
</div>

## The phrases facet

Register translations with `EditorState.phrases`. The facet maps
English source strings to their translations:

```kotlin
import com.monkopedia.kodemirror.state.EditorState
import com.monkopedia.kodemirror.state.EditorStateConfig

val germanPhrases = EditorState.phrases.of(mapOf(
    "Find" to "Suchen",
    "Replace" to "Ersetzen",
    "next" to "nächste",
    "previous" to "vorherige",
    "Replace all" to "Alle ersetzen",
    "close" to "schließen"
))

val state = EditorState.create(EditorStateConfig(
    extensions = germanPhrases
))
```

## Looking up translations

Use `state.phrase()` (or `view.phrase()`) to look up a translated
string. If no translation is registered the original English string is
returned unchanged:

```kotlin
val label = state.phrase("Find")
// Returns "Suchen" with the German phrases above,
// or "Find" if no translation is registered.
```

### Variable substitution

Phrases support positional placeholders (`$1`, `$2`, etc.):

```kotlin
val msg = state.phrase("Change from $1 to $2", "tabs", "spaces")
// With a matching translation:
//   "Änderung von $1 zu $2" -> "Änderung von tabs zu spaces"
// Without:
//   "Change from tabs to spaces"
```

Use `$$` to produce a literal dollar sign in translated text.

## EditorSession.phrase

For convenience, `EditorSession` exposes the same method, delegating to the
current state:

```kotlin
val translated = view.phrase("Replace all")
```

This is useful inside view plugins and commands that have access to the
view but not directly to the state.

## Complete example

Phrase maps from the live demo:

```kotlin
--8<-- "samples/showcase/src/commonMain/kotlin/com/monkopedia/kodemirror/samples/showcase/demos/TranslateDemo.kt:phrase-maps"
```

## Key points

- **`EditorState.phrases`** is a facet mapping English keys to
  translated values.
- **`state.phrase()`** and **`view.phrase()`** perform the lookup.
- Placeholders `$1`, `$2`, ... are replaced with the extra arguments.
- Extensions that display user-facing text should use `phrase()` so
  their strings are translatable.

---

*Based on the [CodeMirror Internationalization example](https://codemirror.net/examples/translate/).*
