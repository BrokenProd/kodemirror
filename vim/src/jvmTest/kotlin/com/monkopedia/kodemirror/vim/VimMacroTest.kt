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

class VimMacroTest {

    @Test
    fun qq_at_q_records_and_replays() = testVim(value = "            ", cursor = Pos(0, 0)) { h ->
        h.doKeys("q", "q", "l", "l", "q")
        h.assertCursorAt(0, 2)
        h.doKeys("@", "q")
        h.assertCursorAt(0, 4)
    }

    @Test
    fun at_at_replays_last() = testVim(value = "            ", cursor = Pos(0, 0)) { h ->
        h.doKeys("q", "q", "l", "l", "q")
        h.assertCursorAt(0, 2)
        h.doKeys("@", "q")
        h.assertCursorAt(0, 4)
        h.doKeys("@", "@")
        h.assertCursorAt(0, 6)
    }

    @Test
    fun macro_with_insert() = testVim(value = "aaa\nbbb\nccc", cursor = Pos(0, 0)) { h ->
        h.doKeys("q", "a", "I", "x", "<Esc>", "j", "q")
        h.assertCursorAt(1, 0)
        h.doKeys("@", "a")
        assertEquals("xaaa\nxbbb\nccc", h.cm.getValue())
    }
}
