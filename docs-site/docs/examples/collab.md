# Collaborative Editing

The `:collab` module implements the client side of collaborative editing
using operational transformation.

<div class="demo-embed">
<iframe src="../../showcase/index.html?demo=collab" loading="lazy"></iframe>
</div>

## Setup

Install the `collab()` extension with your starting document version.
Here the demo creates two editors that will synchronize:

```kotlin
--8<-- "samples/showcase/src/commonMain/kotlin/com/monkopedia/kodemirror/samples/showcase/demos/CollabDemo.kt:collab-setup"
```

## CollabConfig

```kotlin
data class CollabConfig(
    val startVersion: Int = 0,
    val clientID: String? = null,
    val sharedEffects: ((Transaction) -> List<StateEffect<*>>)? = null
)
```

| Property | Description |
|----------|-------------|
| `startVersion` | The document version when the editor was initialized |
| `clientID` | Unique client identifier (auto-generated if `null`) |
| `sharedEffects` | Extract effects from transactions to share with other clients |

## Synchronization logic

The demo uses a shared update list as a simple "server". Each client
sends its local changes and receives remote ones:

```kotlin
--8<-- "samples/showcase/src/commonMain/kotlin/com/monkopedia/kodemirror/samples/showcase/demos/CollabDemo.kt:sync-logic"
```

<!-- Manual code blocks below document the API types -->

## SendableUpdate

Each `SendableUpdate` contains:

```kotlin
data class SendableUpdate(
    val changes: ChangeSet,
    val clientID: String,
    val effects: List<StateEffect<*>> = emptyList(),
    val origin: Transaction
)
```

## Update

Each `Update` from the server:

```kotlin
data class Update(
    val changes: ChangeSet,
    val clientID: String,
    val effects: List<StateEffect<*>> = emptyList()
)
```

## Querying state

```kotlin
// Current synced version
val version = getSyncedVersion(state)

// This client's ID
val id = getClientID(state)
```

## Rebasing

If the server rejects an update because it was based on a stale version,
use `rebaseUpdates` to transform it over the accepted updates:

```kotlin
val rebased = rebaseUpdates(rejectedUpdates, acceptedUpdates)
```

## Full example: polling architecture

A simple polling-based collaboration setup:

```kotlin
class CollabClient(
    private val view: EditorSession,
    private val serverUrl: String
) {
    suspend fun push() {
        val updates = sendableUpdates(view.state)
        if (updates.isNotEmpty()) {
            // Serialize and send to server
            pushToServer(serverUrl, updates, getSyncedVersion(view.state))
        }
    }

    suspend fun pull() {
        val version = getSyncedVersion(view.state)
        val updates = pullFromServer(serverUrl, version)
        if (updates.isNotEmpty()) {
            val spec = receiveUpdates(view.state, updates)
            view.dispatch(spec)
        }
    }
}
```

## API summary

| Function | Description |
|----------|-------------|
| `collab(config)` | Install collaborative editing extension |
| `sendableUpdates(state)` | Get pending local updates to send |
| `receiveUpdates(state, updates)` | Build a `TransactionSpec` from remote updates |
| `getSyncedVersion(state)` | Get the current synced version number |
| `getClientID(state)` | Get this client's ID |
| `rebaseUpdates(updates, over)` | Rebase out-of-date updates |

---

*Based on the [CodeMirror Collaborative Editing example](https://codemirror.net/examples/collab/).*
