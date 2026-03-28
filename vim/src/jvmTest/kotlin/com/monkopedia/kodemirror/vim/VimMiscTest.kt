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

class VimMiscTest {

    @Test
    fun mark_and_jump() = testVim(value = "foo\nbar\nbaz") { h ->
        h.doKeys("j") // go to line 1
        h.doKeys("m", "a") // set mark 'a'
        h.doKeys("G") // go to last line
        h.doKeys("'", "a") // jump to mark 'a'
        h.assertCursorAt(1, 0)
    }

    @Test
    fun backtick_mark_preserves_column() = testVim(
        value = "foo\nbar\nbaz",
        cursor = Pos(1, 2)
    ) { h ->
        h.doKeys("m", "a")
        h.doKeys("G")
        h.doKeys("`", "a")
        h.assertCursorAt(1, 2)
    }

    @Test
    fun dot_repeats_last_edit() = testVim(value = "foo\nbar\nbaz") { h ->
        h.doKeys("d", "d") // delete line
        assertEquals("bar\nbaz", h.cm.getValue())
        h.doKeys(".") // repeat
        assertEquals("baz", h.cm.getValue())
    }

    @Test
    fun u_undoes_last_change() = testVim(value = "foo bar") { h ->
        h.doKeys("d", "w")
        assertEquals("bar", h.cm.getValue())
        h.doKeys("u")
        assertEquals("foo bar", h.cm.getValue())
    }

    @Test
    fun ctrl_r_redoes() = testVim(value = "foo bar") { h ->
        h.doKeys("d", "w")
        assertEquals("bar", h.cm.getValue())
        h.doKeys("u")
        assertEquals("foo bar", h.cm.getValue())
        h.doKeys("<C-r>")
        assertEquals("bar", h.cm.getValue())
    }

    @Test
    fun J_joins_lines() = testVim(value = "foo\nbar\nbaz") { h ->
        h.doKeys("J")
        assertEquals("foo bar\nbaz", h.cm.getValue())
    }

    @Test
    fun J_with_count() = testVim(value = "foo\nbar\nbaz\nqux") { h ->
        h.doKeys("3", "J")
        assertEquals("foo bar baz\nqux", h.cm.getValue())
    }

    @Test
    fun r_replaces_char() = testVim(value = "abc") { h ->
        h.doKeys("r", "x")
        assertEquals("xbc", h.cm.getValue())
    }

    @Test
    fun r_with_count() = testVim(value = "abcde") { h ->
        h.doKeys("3", "r", "x")
        assertEquals("xxxde", h.cm.getValue())
    }

    @Test
    fun p_pastes_after() = testVim(value = "abc", cursor = Pos(0, 0)) { h ->
        h.doKeys("x") // delete 'a', yanked to unnamed register
        h.doKeys("p") // paste after
        assertEquals("bac", h.cm.getValue())
    }

    @Test
    fun capitalP_pastes_before() = testVim(value = "abc", cursor = Pos(0, 1)) { h ->
        h.doKeys("x") // delete 'b'
        h.doKeys("P") // paste before cursor
        assertEquals("abc", h.cm.getValue())
    }

    @Test
    fun ctrl_a_increments_number() = testVim(value = "foo 42 bar", cursor = Pos(0, 4)) { h ->
        h.doKeys("<C-a>")
        assertEquals("foo 43 bar", h.cm.getValue())
    }

    @Test
    fun ctrl_x_decrements_number() = testVim(value = "foo 42 bar", cursor = Pos(0, 4)) { h ->
        h.doKeys("<C-x>")
        assertEquals("foo 41 bar", h.cm.getValue())
    }
}
