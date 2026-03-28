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
package com.monkopedia.kodemirror.vim

import kotlin.test.Test
import kotlin.test.assertEquals

class VimOperatorTest {

    @Test
    fun dd_deletes_line() = testVim(value = "foo\nbar\nbaz", cursor = LinePos(1, 0)) { h ->
        h.doKeys("d", "d")
        assertEquals("foo\nbaz", h.cm.getValue())
        h.assertCursorAt(1, 0)
    }

    @Test
    fun dd_deletes_first_line() = testVim(value = "foo\nbar\nbaz") { h ->
        h.doKeys("d", "d")
        assertEquals("bar\nbaz", h.cm.getValue())
        h.assertCursorAt(0, 0)
    }

    @Test
    fun dd_deletes_last_line() = testVim(value = "foo\nbar\nbaz", cursor = LinePos(2, 0)) { h ->
        h.doKeys("d", "d")
        assertEquals("foo\nbar", h.cm.getValue())
        h.assertCursorAt(1, 0)
    }

    @Test
    fun dd_with_count() = testVim(value = "foo\nbar\nbaz\nqux") { h ->
        h.doKeys("2", "d", "d")
        assertEquals("baz\nqux", h.cm.getValue())
        h.assertCursorAt(0, 0)
    }

    @Test
    fun dw_deletes_word() = testVim(value = "foo bar baz") { h ->
        h.doKeys("d", "w")
        assertEquals("bar baz", h.cm.getValue())
    }

    @Test
    fun dw_with_count() = testVim(value = "foo bar baz") { h ->
        h.doKeys("2", "d", "w")
        assertEquals("baz", h.cm.getValue())
    }

    @Test
    fun de_deletes_to_end_of_word() = testVim(value = "foo bar baz") { h ->
        h.doKeys("d", "e")
        assertEquals(" bar baz", h.cm.getValue())
    }

    @Test
    fun db_deletes_backward_word() = testVim(value = "foo bar baz", cursor = LinePos(0, 4)) { h ->
        h.doKeys("d", "b")
        assertEquals("bar baz", h.cm.getValue())
    }

    @Test
    fun d_dollar_deletes_to_eol() = testVim(value = "foo bar\nbaz", cursor = LinePos(0, 3)) { h ->
        h.doKeys("d", "$")
        assertEquals("foo\nbaz", h.cm.getValue())
    }

    @Test
    fun capitalD_deletes_to_eol() = testVim(value = "foo bar\nbaz", cursor = LinePos(0, 3)) { h ->
        h.doKeys("D")
        assertEquals("foo\nbaz", h.cm.getValue())
    }

    @Test
    fun x_deletes_char() = testVim(value = "abc") { h ->
        h.doKeys("x")
        assertEquals("bc", h.cm.getValue())
    }

    @Test
    fun x_with_count() = testVim(value = "abcde") { h ->
        h.doKeys("3", "x")
        assertEquals("de", h.cm.getValue())
    }

    @Test
    fun capitalX_deletes_char_before() = testVim(value = "abc", cursor = LinePos(0, 1)) { h ->
        h.doKeys("X")
        assertEquals("bc", h.cm.getValue())
    }

    @Test
    fun cc_changes_line() = testVim(value = "foo\nbar\nbaz", cursor = LinePos(1, 0)) { h ->
        h.doKeys("c", "c")
        assertEquals("foo\n\nbaz", h.cm.getValue())
        h.assertCursorAt(1, 0)
    }

    @Test
    fun cw_changes_word() = testVim(value = "foo bar baz") { h ->
        h.doKeys("c", "w")
        // After cw, should be in insert mode with "foo" deleted
        assertEquals(" bar baz", h.cm.getValue())
    }

    @Test
    fun capitalC_changes_to_eol() = testVim(
        value = "foo bar\nbaz",
        cursor = LinePos(0, 3)
    ) { h ->
        h.doKeys("C")
        assertEquals("foo\nbaz", h.cm.getValue())
    }

    @Test
    fun yy_yanks_line() = testVim(value = "foo\nbar\nbaz", cursor = LinePos(1, 0)) { h ->
        h.doKeys("y", "y")
        h.doKeys("p")
        assertEquals("foo\nbar\nbar\nbaz", h.cm.getValue())
    }

    @Test
    fun yy_with_count() = testVim(value = "foo\nbar\nbaz") { h ->
        h.doKeys("2", "y", "y")
        h.doKeys("p")
        assertEquals("foo\nfoo\nbar\nbar\nbaz", h.cm.getValue())
    }

    @Test
    fun yw_yanks_word() = testVim(value = "foo bar baz") { h ->
        h.doKeys("y", "w")
        h.doKeys("$")
        h.doKeys("p")
        assertEquals("foo bar bazfoo ", h.cm.getValue())
    }

    @Test
    fun capitalY_yanks_line() = testVim(value = "foo\nbar\nbaz") { h ->
        h.doKeys("Y")
        h.doKeys("p")
        assertEquals("foo\nfoo\nbar\nbaz", h.cm.getValue())
    }

    @Test
    fun tilde_toggles_case() = testVim(value = "aBc") { h ->
        h.doKeys("~")
        assertEquals("ABc", h.cm.getValue())
        h.assertCursorAt(0, 1)
    }

    @Test
    fun tilde_repeat() = testVim(value = "aBcDe") { h ->
        h.doKeys("3", "~")
        assertEquals("AbCDe", h.cm.getValue())
    }

    @Test
    fun indent_right() = testVim(value = "foo\nbar\nbaz") { h ->
        h.doKeys(">", ">")
        assertEquals("  foo\nbar\nbaz", h.cm.getValue())
    }

    @Test
    fun indent_left() = testVim(value = "  foo\nbar\nbaz") { h ->
        h.doKeys("<", "<")
        assertEquals("foo\nbar\nbaz", h.cm.getValue())
    }
}
