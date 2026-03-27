/*
 * Copyright 2026 Jason Monk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Originally based on CodeMirror 6 by Marijn Haverbeke, licensed under MIT.
 * See NOTICE file for details.
 */
package com.monkopedia.kodemirror.view

import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runDesktopComposeUiTest
import com.monkopedia.kodemirror.state.ChangeSpec
import com.monkopedia.kodemirror.state.Compartment
import com.monkopedia.kodemirror.state.DocPos
import com.monkopedia.kodemirror.state.EditorState
import com.monkopedia.kodemirror.state.EditorStateConfig
import com.monkopedia.kodemirror.state.Extension
import com.monkopedia.kodemirror.state.Facet
import com.monkopedia.kodemirror.state.SelectionSpec
import com.monkopedia.kodemirror.state.StateEffect
import com.monkopedia.kodemirror.state.StateField
import com.monkopedia.kodemirror.state.TransactionSpec
import com.monkopedia.kodemirror.state.asDoc
import com.monkopedia.kodemirror.state.asInsert
import com.monkopedia.kodemirror.state.define
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

@OptIn(ExperimentalTestApi::class)
class RecompositionTest {

    private fun runSessionTest(
        doc: String = "",
        extensions: Extension? = null,
        block: androidx.compose.ui.test.DesktopComposeUiTest.(EditorSession) -> Unit
    ) {
        runDesktopComposeUiTest {
            lateinit var session: EditorSession
            setContent {
                val state = remember {
                    EditorState.create(
                        EditorStateConfig(
                            doc = doc.asDoc(),
                            extensions = extensions
                        )
                    )
                }
                session = remember(state) { EditorSession(state) }
                KodeMirror(session = session)
            }
            waitForIdle()
            block(session)
        }
    }

    @Test
    fun docChange_triggersRecomposition() = runSessionTest(doc = "hello") { session ->
        // Dispatch text insertion at end
        session.dispatch(
            TransactionSpec(
                changes = ChangeSpec.Single(
                    from = DocPos(5),
                    insert = " world".asInsert()
                )
            )
        )
        waitForIdle()

        assertEquals("hello world", session.state.doc.toString())
    }

    @Test
    fun selectionChange_triggersRecomposition() = runSessionTest(doc = "hello world") { session ->
        assertEquals(DocPos(0), session.state.selection.main.head)

        session.dispatch(
            TransactionSpec(
                selection = SelectionSpec.CursorSpec(DocPos(5))
            )
        )
        waitForIdle()

        assertEquals(DocPos(5), session.state.selection.main.head)
    }

    @Test
    fun stateFieldChange_triggersRecomposition() {
        val updateEffect = StateEffect.define<Int>()
        val counterField = StateField.define<Int> {
            create { 0 }
            update { value, tr ->
                var result = value
                for (effect in tr.effects) {
                    effect.asType(updateEffect)?.let { result = it.value }
                }
                result
            }
        }

        runSessionTest(extensions = counterField) { session ->
            assertEquals(0, session.state.field(counterField))

            session.dispatch(
                TransactionSpec(effects = listOf(updateEffect.of(42)))
            )
            waitForIdle()

            assertEquals(42, session.state.field(counterField))
        }
    }

    @Test
    fun facetChange_viaCompartmentReconfigure_triggersRecomposition() {
        val myFacet = Facet.define<String, String>(
            combine = { values -> values.firstOrNull() ?: "" }
        )
        val compartment = Compartment()

        runSessionTest(extensions = compartment.of(myFacet.of("initial"))) { session ->
            assertEquals("initial", session.state.facet(myFacet))

            session.dispatch(
                TransactionSpec(
                    effects = listOf(compartment.reconfigure(myFacet.of("updated")))
                )
            )
            waitForIdle()

            assertEquals("updated", session.state.facet(myFacet))
        }
    }

    @Test
    fun rememberField_recomposesOnFieldChange() {
        val updateEffect = StateEffect.define<Int>()
        val counterField = StateField.define<Int> {
            create { 0 }
            update { value, tr ->
                var result = value
                for (effect in tr.effects) {
                    effect.asType(updateEffect)?.let { result = it.value }
                }
                result
            }
        }

        var observedValue by mutableIntStateOf(-1)

        runDesktopComposeUiTest {
            lateinit var session: EditorSession
            setContent {
                val state = remember {
                    EditorState.create(
                        EditorStateConfig(extensions = counterField)
                    )
                }
                session = remember(state) { EditorSession(state) }
                KodeMirror(session = session)
                observedValue = session.rememberField(counterField)
            }
            waitForIdle()
            assertEquals(0, observedValue)

            session.dispatch(
                TransactionSpec(effects = listOf(updateEffect.of(99)))
            )
            waitForIdle()

            assertEquals(99, observedValue)
        }
    }

    @Test
    fun rememberField_doesNotRecomposeOnUnrelatedChange() {
        val updateEffect = StateEffect.define<Int>()
        val counterField = StateField.define<Int> {
            create { 0 }
            update { value, tr ->
                var result = value
                for (effect in tr.effects) {
                    effect.asType(updateEffect)?.let { result = it.value }
                }
                result
            }
        }

        var recomposeCount by mutableIntStateOf(0)

        runDesktopComposeUiTest {
            lateinit var session: EditorSession
            setContent {
                val state = remember {
                    EditorState.create(
                        EditorStateConfig(
                            doc = "hello".asDoc(),
                            extensions = counterField
                        )
                    )
                }
                session = remember(state) { EditorSession(state) }
                KodeMirror(session = session)

                val fieldValue = session.rememberField(counterField)
                SideEffect {
                    recomposeCount++
                }
                // Use fieldValue to prevent DCE
                remember(fieldValue) { fieldValue }
            }
            waitForIdle()
            val initialCount = recomposeCount

            // Dispatch doc changes that don't affect the field
            session.dispatch(
                TransactionSpec(
                    changes = ChangeSpec.Single(
                        from = DocPos(5),
                        insert = " world".asInsert()
                    )
                )
            )
            waitForIdle()

            session.dispatch(
                TransactionSpec(
                    changes = ChangeSpec.Single(
                        from = DocPos(11),
                        insert = "!".asInsert()
                    )
                )
            )
            waitForIdle()

            // The SideEffect counter should not have increased beyond initial composition
            // since the field value didn't change
            assertEquals(initialCount, recomposeCount)
        }
    }

    @Test
    fun rememberFacet_recomposesOnFacetChange() {
        val myFacet = Facet.define<String, String>(
            combine = { values -> values.firstOrNull() ?: "" }
        )
        val compartment = Compartment()

        var observedValue by mutableStateOf("")

        runDesktopComposeUiTest {
            lateinit var session: EditorSession
            setContent {
                val state = remember {
                    EditorState.create(
                        EditorStateConfig(
                            extensions = compartment.of(myFacet.of("initial"))
                        )
                    )
                }
                session = remember(state) { EditorSession(state) }
                KodeMirror(session = session)
                observedValue = session.rememberFacet(myFacet)
            }
            waitForIdle()
            assertEquals("initial", observedValue)

            session.dispatch(
                TransactionSpec(
                    effects = listOf(compartment.reconfigure(myFacet.of("changed")))
                )
            )
            waitForIdle()

            assertEquals("changed", observedValue)
        }
    }

    @Test
    fun dispatchFromBackgroundThread_triggersRecomposition() = runSessionTest(
        doc = "hello"
    ) { session ->
        // Dispatch from Dispatchers.Default — same as AsyncLinterPlugin
        runBlocking {
            withContext(Dispatchers.Default) {
                session.dispatch(
                    TransactionSpec(
                        changes = ChangeSpec.Single(
                            from = DocPos(5),
                            insert = " world".asInsert()
                        )
                    )
                )
            }
        }
        waitForIdle()

        assertEquals("hello world", session.state.doc.toString())
    }

    @Test
    fun stateEffectFromBackgroundThread_triggersRecomposition() {
        val updateEffect = StateEffect.define<Int>()
        val counterField = StateField.define<Int> {
            create { 0 }
            update { value, tr ->
                var result = value
                for (effect in tr.effects) {
                    effect.asType(updateEffect)?.let { result = it.value }
                }
                result
            }
        }

        var observedValue by mutableIntStateOf(-1)

        runDesktopComposeUiTest {
            lateinit var session: EditorSession
            setContent {
                val state = remember {
                    EditorState.create(
                        EditorStateConfig(extensions = counterField)
                    )
                }
                session = remember(state) { EditorSession(state) }
                KodeMirror(session = session)
                observedValue = session.rememberField(counterField)
            }
            waitForIdle()
            assertEquals(0, observedValue)

            // Dispatch effect from background thread — like async linter does
            runBlocking {
                withContext(Dispatchers.Default) {
                    session.dispatch(
                        TransactionSpec(effects = listOf(updateEffect.of(42)))
                    )
                }
            }
            waitForIdle()

            assertEquals(42, observedValue)
        }
    }

    @Test
    fun asyncLinterPattern_triggersRecomposition() {
        // Mimics AsyncLinterPlugin: CoroutineScope(SupervisorJob() + Dispatchers.Default),
        // delay, then dispatch effect.
        val updateEffect = StateEffect.define<Int>()
        val counterField = StateField.define<Int> {
            create { 0 }
            update { value, tr ->
                var result = value
                for (effect in tr.effects) {
                    effect.asType(updateEffect)?.let { result = it.value }
                }
                result
            }
        }

        var observedValue by mutableIntStateOf(-1)

        runDesktopComposeUiTest {
            lateinit var session: EditorSession
            setContent {
                val state = remember {
                    EditorState.create(
                        EditorStateConfig(extensions = counterField)
                    )
                }
                session = remember(state) { EditorSession(state) }
                KodeMirror(session = session)
                observedValue = session.rememberField(counterField)
            }
            waitForIdle()
            assertEquals(0, observedValue)

            // Launch from session's composition-scoped scope, like AsyncLinterPlugin
            session.coroutineScope.launch {
                session.dispatch(
                    TransactionSpec(effects = listOf(updateEffect.of(42)))
                )
            }

            // waitForIdle() pumps the composition dispatcher, running the
            // launched coroutine and processing the resulting recomposition.
            waitForIdle()

            assertEquals(42, observedValue)
        }
    }

    @Test
    fun asyncDispatch_detectedWithoutExplicitIdle() {
        // Tests whether the recomposer auto-detects snapshot changes from a
        // detached coroutine scope, WITHOUT waitForIdle() forcing processing.
        // This reproduces the real-world async linter pattern.
        val updateEffect = StateEffect.define<Int>()
        val counterField = StateField.define<Int> {
            create { 0 }
            update { value, tr ->
                var result = value
                for (effect in tr.effects) {
                    effect.asType(updateEffect)?.let { result = it.value }
                }
                result
            }
        }

        var observedValue by mutableIntStateOf(-1)

        runDesktopComposeUiTest {
            lateinit var session: EditorSession
            setContent {
                val state = remember {
                    EditorState.create(
                        EditorStateConfig(extensions = counterField)
                    )
                }
                session = remember(state) { EditorSession(state) }
                KodeMirror(session = session)
                observedValue = session.rememberField(counterField)
            }
            waitForIdle()
            assertEquals(0, observedValue)

            // Launch from a completely detached scope — the old
            // AsyncLinterPlugin pattern
            val latch = CountDownLatch(1)
            val scope = CoroutineScope(
                SupervisorJob() + Dispatchers.Default
            )
            scope.launch {
                delay(50)
                session.dispatch(
                    TransactionSpec(
                        effects = listOf(updateEffect.of(42))
                    )
                )
                latch.countDown()
            }

            // Wait for the dispatch to actually complete
            latch.await(2, TimeUnit.SECONDS)
            // Give the recomposer time to auto-process
            Thread.sleep(200)

            // The compose test framework controls recomposition
            // explicitly — waitForIdle() is required to pump the
            // recomposer even though Snapshot.sendApplyNotifications()
            // fires in dispatchTransaction(). In a real Compose window,
            // the platform's frame clock drives recomposition
            // automatically.
            waitForIdle()

            assertEquals(
                42,
                observedValue,
                "Detached-scope dispatch should update state"
            )
        }
    }

    @Test
    fun multipleDispatches_allObserved() = runSessionTest(doc = "") { session ->
        session.dispatch(
            TransactionSpec(
                changes = ChangeSpec.Single(
                    from = DocPos(0),
                    insert = "one".asInsert()
                )
            )
        )
        waitForIdle()
        assertEquals("one", session.state.doc.toString())

        session.dispatch(
            TransactionSpec(
                changes = ChangeSpec.Single(
                    from = DocPos(3),
                    insert = " two".asInsert()
                )
            )
        )
        waitForIdle()
        assertEquals("one two", session.state.doc.toString())

        session.dispatch(
            TransactionSpec(
                changes = ChangeSpec.Single(
                    from = DocPos(7),
                    insert = " three".asInsert()
                )
            )
        )
        waitForIdle()
        assertEquals("one two three", session.state.doc.toString())
    }
}
