package com.monkopedia.kodemirror.view

import com.monkopedia.kodemirror.state.ChangeSpec
import com.monkopedia.kodemirror.state.DocPos
import com.monkopedia.kodemirror.state.EditorState
import com.monkopedia.kodemirror.state.EditorStateConfig
import com.monkopedia.kodemirror.state.LineNumber
import com.monkopedia.kodemirror.state.SelectionSpec
import com.monkopedia.kodemirror.state.TransactionSpec
import com.monkopedia.kodemirror.state.asDoc
import com.monkopedia.kodemirror.state.asInsert
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TabExpansionTest {

    @Test
    fun tab_inserted_via_dispatch_appears_in_document() {
        val state = EditorState.create(
            EditorStateConfig(doc = "hello\nworld".asDoc())
        )
        val tr = state.update(
            TransactionSpec(
                changes = ChangeSpec.Single(
                    from = DocPos(6),
                    insert = "\t".asInsert()
                ),
                selection = SelectionSpec.CursorSpec(DocPos(7))
            )
        )
        val newState = tr.state
        val line2 = newState.doc.line(LineNumber(2))
        assertTrue('\t' in line2.text, "Line 2 should contain a tab character")
        assertEquals("\tworld", line2.text)
    }

    @Test
    fun tab_inserted_via_dispatch_expands_in_annotated_string() {
        val state = EditorState.create(
            EditorStateConfig(doc = "hello\nworld".asDoc())
        )
        val tr = state.update(
            TransactionSpec(
                changes = ChangeSpec.Single(
                    from = DocPos(6),
                    insert = "\t".asInsert()
                ),
                selection = SelectionSpec.CursorSpec(DocPos(7))
            )
        )
        val newState = tr.state
        val line2 = newState.doc.line(LineNumber(2))
        val result = buildLineContentWithTabs(
            line2.from, line2.to, line2.text, emptyList(), tabSize = 4
        )
        assertEquals("    world", result.content.text)
        assertNotNull(result.offsetMap)
    }

    @Test
    fun tab_at_column_0_expands_to_tabSize_spaces() {
        val state = EditorState.create(EditorStateConfig(doc = "\tx".asDoc()))
        val line = state.doc.line(LineNumber(1))
        val result = buildLineContentWithTabs(
            line.from, line.to, line.text, emptyList(), tabSize = 4
        )
        assertEquals("    x", result.content.text)
    }

    @Test
    fun tab_at_column_3_expands_to_1_space() {
        val state = EditorState.create(EditorStateConfig(doc = "abc\tx".asDoc()))
        val line = state.doc.line(LineNumber(1))
        val result = buildLineContentWithTabs(
            line.from, line.to, line.text, emptyList(), tabSize = 4
        )
        assertEquals("abc x", result.content.text)
    }

    @Test
    fun tab_at_column_6_expands_to_2_spaces() {
        val state = EditorState.create(
            EditorStateConfig(doc = "before\tafter".asDoc())
        )
        val line = state.doc.line(LineNumber(1))
        val result = buildLineContentWithTabs(
            line.from, line.to, line.text, emptyList(), tabSize = 4
        )
        // "before" is 6 chars, tab at col 6, next stop is 8, so 2 spaces
        assertEquals("before  after", result.content.text)
    }

    @Test
    fun no_tabs_returns_original_text() {
        val state = EditorState.create(
            EditorStateConfig(doc = "no tabs here".asDoc())
        )
        val line = state.doc.line(LineNumber(1))
        val result = buildLineContentWithTabs(
            line.from, line.to, line.text, emptyList(), tabSize = 4
        )
        assertEquals("no tabs here", result.content.text)
    }
}
