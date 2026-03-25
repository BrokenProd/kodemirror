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
 */
package com.monkopedia.kodemirror.samples.showcase.demos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.monkopedia.kodemirror.basicsetup.basicSetup
import com.monkopedia.kodemirror.lang.javascript.javascript
import com.monkopedia.kodemirror.samples.showcase.DemoScaffold
import com.monkopedia.kodemirror.samples.showcase.SampleDocs
import com.monkopedia.kodemirror.state.Annotation
import com.monkopedia.kodemirror.state.ChangeSpec
import com.monkopedia.kodemirror.state.Transaction
import com.monkopedia.kodemirror.state.TransactionSpec
import com.monkopedia.kodemirror.state.plus
import com.monkopedia.kodemirror.themedracula.dracula
import com.monkopedia.kodemirror.themonedark.oneDark
import com.monkopedia.kodemirror.view.EditorSession
import com.monkopedia.kodemirror.view.KodeMirror
import com.monkopedia.kodemirror.view.rememberEditorSession

private val syncAnnotation = Annotation.define<Boolean>()

@Composable
fun SplitDemo() {
    DemoScaffold(
        title = "Split View",
        description = "Two editors sharing a document. " +
            "Edits in one are reflected in the other."
    ) {
        val sessions = remember { arrayOfNulls<EditorSession>(2) }

        fun syncDispatch(tr: Transaction, otherIndex: Int) {
            val other = sessions[otherIndex] ?: return
            if (!tr.changes.empty && tr.annotation(syncAnnotation) == null) {
                val annotations = buildList {
                    add(syncAnnotation.of(true))
                    tr.annotation(Transaction.userEvent)?.let {
                        add(Transaction.userEvent.of(it))
                    }
                }
                other.dispatch(
                    TransactionSpec(
                        changes = ChangeSpec.Set(tr.changes),
                        annotations = annotations
                    )
                )
            }
        }

        val sessionLeft = rememberEditorSession(
            doc = SampleDocs.javascript,
            extensions = basicSetup + javascript().extension + oneDark
        ) { tr -> syncDispatch(tr, 1) }

        val sessionRight = rememberEditorSession(
            doc = SampleDocs.javascript,
            extensions = basicSetup + javascript().extension + dracula
        ) { tr -> syncDispatch(tr, 0) }

        sessions[0] = sessionLeft
        sessions[1] = sessionRight

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KodeMirror(
                session = sessionLeft,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            KodeMirror(
                session = sessionRight,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
    }
}
