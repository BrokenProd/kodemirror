package com.monkopedia.kodemirror.view

import com.monkopedia.kodemirror.state.DocPos
import com.monkopedia.kodemirror.state.EditorState
import com.monkopedia.kodemirror.state.EditorStateConfig
import com.monkopedia.kodemirror.state.asDoc
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertContains

class TabExpansionTest {
    @Test
    fun tab_at_column_0_expands_to_tabSize_spaces() {
        val state = EditorState.create(EditorStateConfig(doc = "\tx".asDoc()))
        val line = state.doc.line(com.monkopedia.kodemirror.state.LineNumber(1))
        val result = buildLineContentWithTabs(
            line.from, line.to, line.text, emptyList(), tabSize = 4
        )
        assertEquals("    x", result.content.text)
    }

    @Test
    fun tab_at_column_3_expands_to_1_space() {
        val state = EditorState.create(EditorStateConfig(doc = "abc\tx".asDoc()))
        val line = state.doc.line(com.monkopedia.kodemirror.state.LineNumber(1))
        val result = buildLineContentWithTabs(
            line.from, line.to, line.text, emptyList(), tabSize = 4
        )
        assertEquals("abc x", result.content.text)
    }

    @Test
    fun tab_at_column_6_expands_to_2_spaces() {
        val state = EditorState.create(EditorStateConfig(doc = "before\tafter".asDoc()))
        val line = state.doc.line(com.monkopedia.kodemirror.state.LineNumber(1))
        val result = buildLineContentWithTabs(
            line.from, line.to, line.text, emptyList(), tabSize = 4
        )
        // "before" is 6 chars, tab at col 6, next stop is 8, so 2 spaces
        assertEquals("before  after", result.content.text)
    }

    @Test
    fun no_tabs_returns_original_text() {
        val state = EditorState.create(EditorStateConfig(doc = "no tabs here".asDoc()))
        val line = state.doc.line(com.monkopedia.kodemirror.state.LineNumber(1))
        val result = buildLineContentWithTabs(
            line.from, line.to, line.text, emptyList(), tabSize = 4
        )
        assertEquals("no tabs here", result.content.text)
    }
}
