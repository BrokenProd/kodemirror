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

class VimInsertModeTest {

    @Test
    fun i_enters_insert_mode() = testVim(value = "abc") { h ->
        h.doKeys("i")
        assertEquals(true, h.vim.insertMode)
    }

    @Test
    fun a_enters_insert_after() = testVim(value = "abc") { h ->
        h.doKeys("a")
        assertEquals(true, h.vim.insertMode)
        h.assertCursorAt(0, 1)
    }

    @Test
    fun capitalI_inserts_at_first_nonblank() = testVim(
        value = "  abc",
        cursor = LinePos(0, 4)
    ) { h ->
        h.doKeys("I")
        assertEquals(true, h.vim.insertMode)
        h.assertCursorAt(0, 2)
    }

    @Test
    fun capitalA_inserts_at_eol() = testVim(value = "abc") { h ->
        h.doKeys("A")
        assertEquals(true, h.vim.insertMode)
        h.assertCursorAt(0, 3)
    }

    @Test
    fun o_opens_line_below() = testVim(value = "foo\nbar") { h ->
        h.doKeys("o")
        assertEquals("foo\n\nbar", h.cm.getValue())
        assertEquals(true, h.vim.insertMode)
        h.assertCursorAt(1, 0)
    }

    @Test
    fun capitalO_opens_line_above() = testVim(value = "foo\nbar") { h ->
        h.doKeys("O")
        assertEquals("\nfoo\nbar", h.cm.getValue())
        assertEquals(true, h.vim.insertMode)
        h.assertCursorAt(0, 0)
    }

    @Test
    fun esc_exits_insert_mode() = testVim(value = "abc") { h ->
        h.doKeys("i")
        assertEquals(true, h.vim.insertMode)
        h.doKeys("<Esc>")
        assertEquals(false, h.vim.insertMode)
    }
}
