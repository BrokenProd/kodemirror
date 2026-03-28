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

class VimRegisterTest {

    @Test
    fun named_register_yank_and_paste() = testVim(value = "foo\nbar") { h ->
        h.doKeys("\"", "a", "y", "y")
        h.doKeys("j")
        h.doKeys("\"", "a", "p")
        assertEquals("foo\nbar\nfoo", h.cm.getValue())
    }

    @Test
    fun unnamed_register_after_delete() = testVim(value = "foo\nbar\nbaz") { h ->
        h.doKeys("d", "d")
        h.doKeys("p")
        assertEquals("bar\nfoo\nbaz", h.cm.getValue())
    }

    @Test
    fun yank_to_register_a_then_b() = testVim(value = "aaa\nbbb\nccc") { h ->
        h.doKeys("\"", "a", "y", "y")
        h.doKeys("j")
        h.doKeys("\"", "b", "y", "y")
        h.doKeys("G")
        h.doKeys("\"", "a", "p")
        assertEquals("aaa\nbbb\nccc\naaa", h.cm.getValue())
    }
}
