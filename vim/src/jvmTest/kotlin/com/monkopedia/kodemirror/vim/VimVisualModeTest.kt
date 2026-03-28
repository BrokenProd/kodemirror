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
import kotlin.test.assertTrue

class VimVisualModeTest {

    @Test
    fun v_enters_visual_mode() = testVim(value = "hello world") { h ->
        h.doKeys("v")
        assertTrue(h.vim.visualMode)
        assertEquals(false, h.vim.visualLine)
    }

    @Test
    fun capitalV_enters_visual_line() = testVim(value = "hello\nworld") { h ->
        h.doKeys("V")
        assertTrue(h.vim.visualMode)
        assertTrue(h.vim.visualLine)
    }

    @Test
    fun v_then_esc_exits_visual() = testVim(value = "hello") { h ->
        h.doKeys("v")
        assertTrue(h.vim.visualMode)
        h.doKeys("<Esc>")
        assertEquals(false, h.vim.visualMode)
    }

    @Test
    fun visual_d_deletes_selection() = testVim(value = "abcdef", cursor = Pos(0, 1)) { h ->
        h.doKeys("v", "l", "l", "d")
        assertEquals("aef", h.cm.getValue())
    }

    @Test
    fun visual_line_d_deletes_lines() = testVim(
        value = "foo\nbar\nbaz",
        cursor = Pos(0, 0)
    ) { h ->
        h.doKeys("V", "j", "d")
        assertEquals("baz", h.cm.getValue())
    }

    @Test
    fun visual_y_yanks_selection() = testVim(value = "abcdef") { h ->
        h.doKeys("v", "l", "l", "y")
        assertEquals(false, h.vim.visualMode)
        h.doKeys("$", "p")
        assertEquals("abcdefabc", h.cm.getValue())
    }
}
