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

class VimExCommandTest {

    @Test
    fun substitute_basic() = testVim(value = "foo bar foo") { h ->
        h.doEx("s/foo/baz/")
        assertEquals("baz bar foo", h.cm.getValue())
    }

    @Test
    fun substitute_global() = testVim(value = "foo bar foo") { h ->
        h.doEx("s/foo/baz/g")
        assertEquals("baz bar baz", h.cm.getValue())
    }

    @Test
    fun substitute_with_range() = testVim(value = "foo\nfoo\nfoo") { h ->
        h.doEx("1,2s/foo/bar/")
        assertEquals("bar\nbar\nfoo", h.cm.getValue())
    }

    @Test
    fun sort_lines() = testVim(value = "cherry\napple\nbanana") { h ->
        h.doEx("%sort")
        assertEquals("apple\nbanana\ncherry", h.cm.getValue())
    }

    @Test
    fun delete_lines() = testVim(value = "foo\nbar\nbaz") { h ->
        h.doEx("2d")
        assertEquals("foo\nbaz", h.cm.getValue())
    }

    @Test
    fun join_lines() = testVim(value = "foo\nbar\nbaz") { h ->
        h.doEx("1,2join")
        assertEquals("foo bar\nbaz", h.cm.getValue())
    }
}
