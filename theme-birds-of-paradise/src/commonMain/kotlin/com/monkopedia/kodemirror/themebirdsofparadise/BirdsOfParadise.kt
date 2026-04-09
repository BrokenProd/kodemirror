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
 * Original theme by Joe Bergantine.
 */
package com.monkopedia.kodemirror.themebirdsofparadise

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
 * Birds of Paradise syntax highlighting style.
 *
 * Ported from thememirror birds-of-paradise.ts. Dark theme by Joe Bergantine.
 */
val birdsOfParadiseHighlightStyle = HighlightStyle.define(
    listOf(
        TagStyleSpec(Tags.comment, SpanStyle(color = Color(0xFF6B4E32))),
        TagStyleSpec(
            listOf(Tags.keyword, Tags.operator, Tags.derefOperator),
            SpanStyle(color = Color(0xFFEF5D32))
        ),
        TagStyleSpec(
            Tags.className,
            SpanStyle(color = Color(0xFFEFAC32), fontWeight = FontWeight.Bold)
        ),
        TagStyleSpec(
            listOf(
                Tags.typeName,
                Tags.propertyName,
                Tags.function(Tags.variableName),
                Tags.definition(Tags.variableName)
            ),
            SpanStyle(color = Color(0xFFEFAC32))
        ),
        TagStyleSpec(
            Tags.definition(Tags.typeName),
            SpanStyle(color = Color(0xFFEFAC32), fontWeight = FontWeight.Bold)
        ),
        TagStyleSpec(
            Tags.labelName,
            SpanStyle(color = Color(0xFFEFAC32), fontWeight = FontWeight.Bold)
        ),
        TagStyleSpec(
            listOf(Tags.number, Tags.bool),
            SpanStyle(color = Color(0xFF6C99BB))
        ),
        TagStyleSpec(
            listOf(Tags.variableName, Tags.self),
            SpanStyle(color = Color(0xFF7DAF9C))
        ),
        TagStyleSpec(
            listOf(Tags.string, Tags.special(Tags.brace), Tags.regexp),
            SpanStyle(color = Color(0xFFD9D762))
        ),
        TagStyleSpec(
            listOf(Tags.angleBracket, Tags.tagName, Tags.attributeName),
            SpanStyle(color = Color(0xFFEFCB43))
        )
    )
)

/**
 * Complete Birds of Paradise editor theme with UI styling.
 *
 * Ported from thememirror birds-of-paradise.ts.
 */
val birdsOfParadiseTheme: EditorTheme = EditorTheme(
    background = Color(0xFF3b2627),
    foreground = Color(0xFFE6E1C4),
    cursor = Color(0xFFE6E1C4),
    selection = Color(0xFF16120E),
    activeLineBackground = Color(0xFF1F1611),
    gutterBackground = Color(0xFF3b2627),
    // gutterForeground: #E6E1C490 -> 0x90E6E1C4
    gutterForeground = Color(0x90E6E1C4),
    dark = true
)

/**
 * Extension combining [birdsOfParadiseTheme] UI styling with
 * [birdsOfParadiseHighlightStyle] syntax highlighting.
 *
 * Drop this into an editor's extension list for a complete Birds of Paradise look.
 */
val birdsOfParadise: Extension = extensionListOf(
    editorTheme.of(birdsOfParadiseTheme),
    syntaxHighlighting(birdsOfParadiseHighlightStyle)
)
