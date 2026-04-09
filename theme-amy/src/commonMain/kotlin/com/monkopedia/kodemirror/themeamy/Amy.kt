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
 * Ported from thememirror (https://github.com/vadimdemedes/thememirror)
 * by Vadim Demedes, licensed under MIT. See NOTICE file for details.
 * Original theme by William D. Neumann.
 */
package com.monkopedia.kodemirror.themeamy

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import com.monkopedia.kodemirror.language.HighlightStyle
import com.monkopedia.kodemirror.language.TagStyleSpec
import com.monkopedia.kodemirror.language.syntaxHighlighting
import com.monkopedia.kodemirror.lezer.highlight.Tags
import com.monkopedia.kodemirror.state.Extension
import com.monkopedia.kodemirror.state.extensionListOf
import com.monkopedia.kodemirror.view.EditorTheme
import com.monkopedia.kodemirror.view.editorTheme

/**
 * Amy syntax highlighting style.
 *
 * Ported from thememirror amy.ts. Dark theme by William D. Neumann.
 */
val amyHighlightStyle = HighlightStyle.define(
    listOf(
        TagStyleSpec(Tags.comment, SpanStyle(color = Color(0xFF404080))),
        TagStyleSpec(
            listOf(Tags.string, Tags.regexp),
            SpanStyle(color = Color(0xFF999999))
        ),
        TagStyleSpec(Tags.number, SpanStyle(color = Color(0xFF7090B0))),
        TagStyleSpec(
            listOf(Tags.bool, Tags.`null`),
            SpanStyle(color = Color(0xFF8080A0))
        ),
        TagStyleSpec(
            listOf(Tags.punctuation, Tags.derefOperator),
            SpanStyle(color = Color(0xFF805080))
        ),
        TagStyleSpec(Tags.keyword, SpanStyle(color = Color(0xFF60B0FF))),
        TagStyleSpec(Tags.definitionKeyword, SpanStyle(color = Color(0xFFB0FFF0))),
        TagStyleSpec(Tags.moduleKeyword, SpanStyle(color = Color(0xFF60B0FF))),
        TagStyleSpec(Tags.operator, SpanStyle(color = Color(0xFFA0A0FF))),
        TagStyleSpec(
            listOf(Tags.variableName, Tags.self),
            SpanStyle(color = Color(0xFF008080))
        ),
        TagStyleSpec(Tags.operatorKeyword, SpanStyle(color = Color(0xFFA0A0FF))),
        TagStyleSpec(Tags.controlKeyword, SpanStyle(color = Color(0xFF80A0FF))),
        TagStyleSpec(Tags.className, SpanStyle(color = Color(0xFF70E080))),
        TagStyleSpec(
            listOf(Tags.function(Tags.propertyName), Tags.propertyName),
            SpanStyle(color = Color(0xFF50A0A0))
        ),
        TagStyleSpec(Tags.tagName, SpanStyle(color = Color(0xFF009090))),
        TagStyleSpec(Tags.modifier, SpanStyle(color = Color(0xFFB0FFF0))),
        TagStyleSpec(
            listOf(Tags.squareBracket, Tags.attributeName),
            SpanStyle(color = Color(0xFFD0D0FF))
        )
    )
)

/**
 * Complete Amy editor theme with UI styling.
 *
 * Ported from thememirror amy.ts.
 */
val amyTheme: EditorTheme = EditorTheme(
    background = Color(0xFF200020),
    foreground = Color(0xFFD0D0FF),
    cursor = Color(0xFF7070FF),
    selection = Color(0x80800000),
    activeLineBackground = Color(0x40800000),
    gutterBackground = Color(0xFF200020),
    gutterForeground = Color(0xFFC080C0),
    dark = true
)

/**
 * Extension combining [amyTheme] UI styling with [amyHighlightStyle] syntax highlighting.
 *
 * Drop this into an editor's extension list for a complete Amy look.
 */
val amy: Extension = extensionListOf(
    editorTheme.of(amyTheme),
    syntaxHighlighting(amyHighlightStyle)
)
