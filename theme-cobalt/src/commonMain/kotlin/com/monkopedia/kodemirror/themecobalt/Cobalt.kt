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
package com.monkopedia.kodemirror.themecobalt

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
 * Cobalt syntax highlighting style.
 *
 * Ported from thememirror cobalt.ts. Dark theme by Jacob Rus.
 */
val cobaltHighlightStyle = HighlightStyle.define(
    listOf(
        TagStyleSpec(Tags.comment, SpanStyle(color = Color(0xFF0088FF))),
        TagStyleSpec(Tags.string, SpanStyle(color = Color(0xFF3AD900))),
        TagStyleSpec(Tags.regexp, SpanStyle(color = Color(0xFF80FFC2))),
        TagStyleSpec(
            listOf(Tags.number, Tags.bool, Tags.`null`),
            SpanStyle(color = Color(0xFFFF628C))
        ),
        TagStyleSpec(
            listOf(Tags.definitionKeyword, Tags.modifier),
            SpanStyle(color = Color(0xFFFFEE80))
        ),
        TagStyleSpec(Tags.variableName, SpanStyle(color = Color(0xFFCCCCCC))),
        TagStyleSpec(Tags.self, SpanStyle(color = Color(0xFFFF80E1))),
        TagStyleSpec(
            listOf(
                Tags.className,
                Tags.definition(Tags.propertyName),
                Tags.function(Tags.variableName),
                Tags.definition(Tags.typeName),
                Tags.labelName
            ),
            SpanStyle(color = Color(0xFFFFDD00))
        ),
        TagStyleSpec(
            listOf(Tags.keyword, Tags.operator),
            SpanStyle(color = Color(0xFFFF9D00))
        ),
        TagStyleSpec(
            listOf(Tags.propertyName, Tags.typeName),
            SpanStyle(color = Color(0xFF80FFBB))
        ),
        TagStyleSpec(Tags.special(Tags.brace), SpanStyle(color = Color(0xFFEDEF7D))),
        TagStyleSpec(Tags.attributeName, SpanStyle(color = Color(0xFF9EFFFF))),
        TagStyleSpec(Tags.derefOperator, SpanStyle(color = Color(0xFFFFFFFF)))
    )
)

/**
 * Complete Cobalt editor theme with UI styling.
 *
 * Ported from thememirror cobalt.ts.
 */
val cobaltTheme: EditorTheme = EditorTheme(
    background = Color(0xFF00254b),
    foreground = Color(0xFFFFFFFF),
    cursor = Color(0xFFFFFFFF),
    // selection: #B36539BF -> 0xBFB36539
    selection = Color(0xBFB36539),
    // lineHighlight: #00000059 -> 0x59000000
    activeLineBackground = Color(0x59000000),
    gutterBackground = Color(0xFF00254b),
    // gutterForeground: #FFFFFF70 -> 0x70FFFFFF
    gutterForeground = Color(0x70FFFFFF),
    dark = true
)

/**
 * Extension combining [cobaltTheme] UI styling with [cobaltHighlightStyle] syntax highlighting.
 *
 * Drop this into an editor's extension list for a complete Cobalt look.
 */
val cobalt: Extension = extensionListOf(
    editorTheme.of(cobaltTheme),
    syntaxHighlighting(cobaltHighlightStyle)
)
