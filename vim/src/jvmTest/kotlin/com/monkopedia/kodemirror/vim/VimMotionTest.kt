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

/**
 * Motion tests ported from vim_test.js.
 *
 * Uses the default code (DEFAULT_CODE) from the test harness.
 */
class VimMotionTest {

    // The default code lines for reference:
    // Line 0: " wOrd1 (#%"
    // Line 1: " word3] "
    // Line 2: "aopop pop 0 1 2 3 4"
    // Line 3: " (a) [b] {c} "
    // Line 4: "int getchar(void) {"
    // Line 5: "  static char buf[BUFSIZ];"
    // Line 6: "  static char *bufp = buf;"
    // Line 7: "  if (n == 0) {  /* buffer is empty */"
    // Line 8: "    n = read(0, buf, sizeof buf);"
    // Line 9: "    bufp = buf;"
    // Line 10: "  }"
    // Line 11: ""
    // Line 12: "  return (--n >= 0) ? (unsigned char) *bufp++ : EOF;"
    // Line 13: " "
    // Line 14: "}"

    // Line info
    private val word1Start = Pos(0, 1)
    private val word1End = Pos(0, 5)
    private val word2Start = Pos(0, 7)
    private val word2End = Pos(0, 9)
    private val word3Start = Pos(1, 1)
    private val word3End = Pos(1, 5)
    private val charLine = 2
    private val endOfDocument = Pos(14, 1)

    @Test
    fun pipe() = testMotion(listOf("|"), Pos(0, 0), Pos(0, 4))

    @Test
    fun pipe_repeat() = testMotion(listOf("3", "|"), Pos(0, 2), Pos(0, 4))

    @Test
    fun h() = testMotion(listOf("h"), Pos(0, 0), word1Start)

    @Test
    fun h_repeat() = testMotion(listOf("3", "h"), Pos(0, 2), word1End)

    @Test
    fun l() = testMotion(listOf("l"), Pos(0, 1))

    @Test
    fun l_repeat() = testMotion(listOf("2", "l"), Pos(0, 2))

    @Test
    fun j() = testMotion(listOf("j"), Pos(1, 5), word1End)

    @Test
    fun j_repeat() = testMotion(listOf("2", "j"), Pos(2, 5), word1End)

    @Test
    fun j_repeat_clip() = testMotion(listOf("1000", "j"), endOfDocument)

    @Test
    fun k() = testMotion(listOf("k"), Pos(0, 5), word3End)

    @Test
    fun k_repeat() = testMotion(listOf("2", "k"), Pos(0, 4), Pos(2, 4))

    @Test
    fun w() = testMotion(listOf("w"), word1Start)

    @Test
    fun w_repeat() = testMotion(listOf("2", "w"), word2Start)

    @Test
    fun w_wrap() = testMotion(listOf("w"), word3Start, word2Start)

    @Test
    fun w_endOfDocument() = testMotion(listOf("w"), endOfDocument, endOfDocument)

    @Test
    fun w_start_to_end() = testMotion(listOf("1000", "w"), endOfDocument, Pos(0, 0))

    @Test
    fun capitalW() = testMotion(listOf("W"), Pos(0, 1))

    @Test
    fun e() = testMotion(listOf("e"), word1End)

    @Test
    fun e_repeat() = testMotion(listOf("2", "e"), word2End)

    @Test
    fun e_wrap() = testMotion(listOf("e"), word3End, word2End)

    @Test
    fun b() = testMotion(listOf("b"), word3Start, word3End)

    @Test
    fun b_repeat() = testMotion(listOf("2", "b"), word2Start, word3End)

    @Test
    fun b_wrap() = testMotion(listOf("b"), word2Start, word3Start)

    @Test
    fun ge() = testMotion(listOf("g", "e"), word2End, word3End)

    @Test
    fun ge_repeat() = testMotion(listOf("2", "g", "e"), word1End, word3Start)

    @Test
    fun gg() = testMotion(listOf("g", "g"), Pos(0, 1), Pos(3, 1))

    @Test
    fun gg_repeat() = testMotion(listOf("3", "g", "g"), Pos(2, 0))

    @Test
    fun capitalG() = testMotion(listOf("G"), Pos(14, 0), Pos(3, 1))

    @Test
    fun zero() = testMotion(listOf("0"), Pos(0, 0), Pos(0, 8))

    @Test
    fun caret() = testMotion(listOf("^"), Pos(0, 1), Pos(0, 8))

    @Test
    fun plus() = testMotion(listOf("+"), Pos(1, 1), Pos(0, 8))

    @Test
    fun minus() = testMotion(listOf("-"), Pos(0, 1), Pos(1, 4))

    @Test
    fun dollar() = testMotion(listOf("$"), Pos(0, 9), Pos(0, 1))

    @Test
    fun dollar_repeat() = testMotion(listOf("2", "$"), Pos(1, 7), Pos(0, 3))

    @Test
    fun f() = testMotion(listOf("f", "p"), Pos(charLine, 2), Pos(charLine, 0))

    @Test
    fun f_repeat() = testMotion(
        listOf("2", "f", "p"),
        Pos(charLine, 6),
        Pos(charLine, 2)
    )

    @Test
    fun t() = testMotion(
        listOf("t", "p"),
        Pos(charLine, 1),
        Pos(charLine, 0)
    )

    @Test
    fun capitalF() = testMotion(listOf("F", "p"), Pos(charLine, 2), Pos(charLine, 4))

    @Test
    fun capitalT() = testMotion(
        listOf("T", "p"),
        Pos(charLine, 3),
        Pos(charLine, 4)
    )

    @Test
    fun percent_parens() = testMotion(listOf("%"), Pos(3, 3), Pos(3, 1))

    @Test
    fun percent_squares() = testMotion(listOf("%"), Pos(3, 7), Pos(3, 5))

    @Test
    fun percent_braces() = testMotion(listOf("%"), Pos(3, 11), Pos(3, 9))

    // -- Operator motions (d, c, y, etc.) are tested in VimOperatorTest

    @Test
    fun keepHPos() = testMotion(
        listOf("5", "j", "j", "7", "k"),
        Pos(8, 12),
        Pos(12, 12)
    )

    @Test
    fun keepHPosEol() = testMotion(
        listOf("$", "2", "j"),
        Pos(2, 18)
    )

    @Test
    fun changingLinesAfterEolOperation() = testVim { helpers ->
        helpers.cm.setCursor(0, 0)
        helpers.doKeys("$")
        helpers.doKeys("j")
        // After moving to Eol and then down, we should be at Eol of line 1
        helpers.assertCursorAt(1, 7)
        helpers.doKeys("j")
        // After moving down, we should be at Eol of line 2
        helpers.assertCursorAt(2, 18)
        helpers.doKeys("h")
        helpers.doKeys("j")
        // Line 3 is shorter, so clipped to its end
        helpers.assertCursorAt(3, 12)
        helpers.doKeys("j")
        helpers.doKeys("j")
        // Back to line 5, should restore the saved column
        helpers.assertCursorAt(5, 17)
    }
}
