# Read-Only Mode

There are two ways to make an editor read-only: the `readOnly` facet
on the state layer, and the `editable` facet on the view layer.

<div class="demo-embed">
<iframe src="../../showcase/index.html?demo=readonly" loading="lazy"></iframe>
</div>

## Using readOnly

The `readOnly` facet prevents the document from being modified through
transactions:

```kotlin
import com.monkopedia.kodemirror.state.*

val state = EditorState.create(EditorStateConfig(
    doc = "This text cannot be changed.".asDoc(),
    extensions = readOnly.of(true)
))
```

With `readOnly`, the document is immutable but the cursor can still
move and text can be selected and copied.

## Using editable

The `editable` facet in the `:view` module disables all interaction
with the editor — no cursor, no selection, no input:

```kotlin
import com.monkopedia.kodemirror.view.editable

val state = EditorState.create(EditorStateConfig(
    doc = "Display only.".asDoc(),
    extensions = editable.of(false)
))
```

## Toggling at runtime

The demo uses a `Compartment` to toggle the `editable` facet at
runtime:

```kotlin
--8<-- "samples/showcase/src/commonMain/kotlin/com/monkopedia/kodemirror/samples/showcase/demos/ReadOnlyDemo.kt:editable-compartment"
```

## Checking read-only status

```kotlin
val isReadOnly = state.readOnly  // shorthand for state.facet(readOnly)
```

---

*Based on the [CodeMirror Read-Only example](https://codemirror.net/examples/config/).*
