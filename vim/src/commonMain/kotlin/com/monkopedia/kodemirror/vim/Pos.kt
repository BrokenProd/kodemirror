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

/**
 * A position in the editor, using 0-based line numbers and character offsets.
 * This mirrors CM5's Pos(line, ch) used throughout the vim engine.
 */
data class Pos(val line: Int, val ch: Int, val sticky: String? = null) : Comparable<Pos> {
    override fun compareTo(other: Pos): Int {
        val lineCmp = line.compareTo(other.line)
        return if (lineCmp != 0) lineCmp else ch.compareTo(other.ch)
    }

    override fun toString(): String = "Pos($line, $ch)"
}

/** Create a Pos, mirroring the CM5 `Pos(line, ch)` constructor. */
fun makeCursor(line: Int, ch: Int): Pos = Pos(line, ch)

/** Returns true if [a] is before [b] in the document. */
fun cursorIsBefore(a: Pos, b: Pos): Boolean = a.line < b.line || (a.line == b.line && a.ch < b.ch)

/** Returns true if the two positions are equal (ignoring sticky). */
fun cursorEqual(a: Pos, b: Pos): Boolean = a.line == b.line && a.ch == b.ch

/** Returns the earlier of two positions. */
fun cursorMin(a: Pos, b: Pos): Pos = if (cursorIsBefore(a, b)) a else b

/** Returns the later of two positions. */
fun cursorMax(a: Pos, b: Pos): Pos = if (cursorIsBefore(a, b)) b else a

/** Copy a Pos, optionally overriding line or ch. */
fun Pos.copy(line: Int = this.line, ch: Int = this.ch): Pos = Pos(line, ch)
