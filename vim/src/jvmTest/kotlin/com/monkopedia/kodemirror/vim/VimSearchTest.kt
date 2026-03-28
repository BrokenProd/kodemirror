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

class VimSearchTest {

    @Test
    fun star_searches_word_under_cursor() = testVim(
        value = "foo bar foo baz foo",
        cursor = LinePos(0, 0)
    ) { h ->
        h.doKeys("*")
        h.assertCursorAt(0, 8)
    }

    @Test
    fun star_wraps_around() = testVim(
        value = "foo bar foo",
        cursor = LinePos(0, 8)
    ) { h ->
        h.doKeys("*")
        h.assertCursorAt(0, 0)
    }

    @Test
    fun hash_searches_backward() = testVim(
        value = "foo bar foo",
        cursor = LinePos(0, 8)
    ) { h ->
        h.doKeys("#")
        h.assertCursorAt(0, 0)
    }

    @Test
    fun n_repeats_search_forward() = testVim(
        value = "foo bar foo baz foo",
        cursor = LinePos(0, 0)
    ) { h ->
        h.doKeys("*")
        h.assertCursorAt(0, 8)
        h.doKeys("n")
        h.assertCursorAt(0, 16)
    }

    @Test
    fun capitalN_repeats_search_backward() = testVim(
        value = "foo bar foo baz foo",
        cursor = LinePos(0, 0)
    ) { h ->
        h.doKeys("*")
        h.assertCursorAt(0, 8)
        h.doKeys("N")
        h.assertCursorAt(0, 0)
    }
}
