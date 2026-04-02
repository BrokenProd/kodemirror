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

class DebugEx2Test {
    @Test
    fun debug_ex_go_to_line() = testVim(value = "a\nb\nc\nd\ne\n") { h ->
        h.cm.setCursor(0, 0)
        // Directly call processCommand
        exCommandDispatcher.processCommand(h.cm, "4")
        println("After direct processCommand('4'): cursor=${h.cm.getCursor()}")
        assertEquals(3, h.cm.getCursor().line)
    }
}
