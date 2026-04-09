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
 */
package com.monkopedia.kodemirror.themebarf

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import com.monkopedia.kodemirror.language.HighlightStyle
import com.monkopedia.kodemirror.language.TagStyleSpec
import com.monkopedia.kodemirror.language.syntaxHighlighting
import com.monkopedia.kodemirror.lezer.highlight.Tags
import com.monkopedia.kodemirror.state.Extension
import com.monkopedia.kodemirror.state.extensionListOf
import com.monkopedia.kodemirror.view.EditorTheme
import com.monkopedia.kodemirror.view.editorTheme

/**
 * Barf syntax highlighting style.
 *
 * Ported from thememirror barf.ts. Dark theme.
 */
val barfHighlightStyle = HighlightStyle.define(
    listOf(
        TagStyleSpec(Tags.comment, SpanStyle(color = Color(0xFF6E6E6E))),
        TagStyleSpec(
            listOf(Tags.string, Tags.regexp, Tags.special(Tags.brace)),
            SpanStyle(color = Color(0xFF5C81B3))
        ),
        TagStyleSpec(Tags.number, SpanStyle(color = Color(0xFFC1E1B8))),
        TagStyleSpec(Tags.bool, SpanStyle(color = Color(0xFF53667D))),
        TagStyleSpec(
            listOf(Tags.definitionKeyword, Tags.modifier, Tags.function(Tags.propertyName)),
            SpanStyle(color = Color(0xFFA3D295), fontWeight = FontWeight.Bold)
        ),
        TagStyleSpec(
            listOf(Tags.keyword, Tags.moduleKeyword, Tags.operatorKeyword, Tags.operator),
            SpanStyle(color = Color(0xFF697A8E), fontWeight = FontWeight.Bold)
        ),
        TagStyleSpec(
            listOf(Tags.variableName, Tags.attributeName),
            SpanStyle(color = Color(0xFF708E67))
        ),
        TagStyleSpec(
            listOf(
                Tags.function(Tags.variableName),
                Tags.definition(Tags.propertyName),
                Tags.derefOperator
            ),
            SpanStyle(color = Color(0xFFFFFFFF))
        ),
        TagStyleSpec(Tags.tagName, SpanStyle(color = Color(0xFFA3D295)))
    )
)

/**
 * Complete Barf editor theme with UI styling.
 *
 * Ported from thememirror barf.ts.
 */
val barfTheme: EditorTheme = EditorTheme(
    // background: #15191EFA -> 0xFA15191E
    background = Color(0xFA15191E),
    foreground = Color(0xFFEEF2F7),
    cursor = Color(0xFFC4C4C4),
    // selection: #90B2D557 -> 0x5790B2D5
    selection = Color(0x5790B2D5),
    // lineHighlight: #57575712 -> 0x12575757
    activeLineBackground = Color(0x12575757),
    // gutterBackground: #15191EFA -> 0xFA15191E
    gutterBackground = Color(0xFA15191E),
    // gutterForeground: #aaaaaa95 -> 0x95aaaaaa
    gutterForeground = Color(0x95aaaaaa),
    dark = true
)

/**
 * Extension combining [barfTheme] UI styling with [barfHighlightStyle] syntax highlighting.
 *
 * Drop this into an editor's extension list for a complete Barf look.
 */
val barf: Extension = extensionListOf(
    editorTheme.of(barfTheme),
    syntaxHighlighting(barfHighlightStyle)
)
