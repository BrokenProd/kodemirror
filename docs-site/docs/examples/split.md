# Split View

A split editor shows the same document in two side-by-side panes. Each
pane is a `KodeMirror` composable sharing the same `EditorSession`.

<div class="demo-embed">
<iframe src="../../showcase/index.html?demo=split" loading="lazy"></iframe>
</div>

## Synchronizing two editors

The demo creates two separate editor sessions (with different themes)
and uses a `syncDispatch` callback to forward changes between them via
an annotation guard:

```kotlin
--8<-- "samples/showcase/src/commonMain/kotlin/com/monkopedia/kodemirror/samples/showcase/demos/SplitDemo.kt:sync-dispatch"
```

Each editor registers this callback so that edits in one pane are
replicated to the other, while the `syncAnnotation` prevents infinite
dispatch loops.

!!! note
    For true shared-document editing with cursor awareness, the
    `:collab` module is a better fit. The sync-dispatch pattern shown
    here is simpler but does not handle concurrent edits from multiple
    users.

---

*Based on the [CodeMirror Split View example](https://codemirror.net/examples/split/).*
