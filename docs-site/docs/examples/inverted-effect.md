# Inverted Effects

Some extensions use [state effects](../guide/data-model.md) alongside
document changes. For undo/redo to work correctly with these effects, the
history needs to know how to invert them. The `invertedEffects` facet
lets you register functions that produce the inverse of a transaction's
effects.

<div class="demo-embed">
<iframe src="../../showcase/index.html?demo=inverted-effect" loading="lazy"></iframe>
</div>

## When to use

Use `invertedEffects` when your extension dispatches `StateEffect`s that
change meaningful state and you want undo/redo to reverse those effects
alongside the document changes.

## Example: effect-based counter

Suppose you track a counter that increments on certain edits:

```kotlin
--8<-- "samples/showcase/src/commonMain/kotlin/com/monkopedia/kodemirror/samples/showcase/demos/InvertedEffectDemo.kt:counter-field"
```

Now register an inverted-effects provider so that undoing a transaction
that added +1 will add -1:

```kotlin
--8<-- "samples/showcase/src/commonMain/kotlin/com/monkopedia/kodemirror/samples/showcase/demos/InvertedEffectDemo.kt:counter-extension"
```

When the history undoes a transaction that carried `addToCounter.of(1)`,
it will now also apply `addToCounter.of(-1)`, keeping the counter in
sync with the document state.

## How it works

The `invertedEffects` facet accepts functions of type
`(Transaction) -> List<StateEffect<*>>`. When the history records a
transaction, it calls all registered functions to collect the inverse
effects. These are stored alongside the inverted document changes and
replayed on undo/redo.

```kotlin
val invertedEffects: Facet<
    (Transaction) -> List<StateEffect<*>>,
    List<(Transaction) -> List<StateEffect<*>>>
> = Facet.define()
```

## Key points

- Register one function per effect type you want to make undoable.
- The function receives the original transaction and should return
  effects that reverse the original effects.
- Only effects that are not already handled by document-change inversion
  need this treatment. Document changes are inverted automatically.

---

*Based on the [CodeMirror Inverted Effects example](https://codemirror.net/examples/inverted-effect/).*
