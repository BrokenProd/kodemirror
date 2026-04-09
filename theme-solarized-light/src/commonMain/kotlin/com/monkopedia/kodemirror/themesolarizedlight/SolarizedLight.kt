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
package com.monkopedia.kodemirror.themesolarizedlight

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
 * Solarized Light syntax highlighting style.
 *
 * Ported from thememirror solarized-light.ts. Light theme by Ethan Schoonover.
 */
val solarizedLightHighlightStyle = HighlightStyle.define(
    listOf(
        TagStyleSpec(Tags.comment, SpanStyle(color = Color(0xFF93A1A1))),
        TagStyleSpec(Tags.string, SpanStyle(color = Color(0xFF2AA198))),
        TagStyleSpec(Tags.regexp, SpanStyle(color = Color(0xFFD30102))),
        TagStyleSpec(Tags.number, SpanStyle(color = Color(0xFFD33682))),
        TagStyleSpec(Tags.variableName, SpanStyle(color = Color(0xFF268BD2))),
        TagStyleSpec(
            listOf(Tags.keyword, Tags.operator, Tags.punctuation),
            SpanStyle(color = Color(0xFF859900))
        ),
        TagStyleSpec(
            listOf(Tags.definitionKeyword, Tags.modifier),
            SpanStyle(color = Color(0xFF073642), fontWeight = FontWeight.Bold)
        ),
        TagStyleSpec(
            listOf(Tags.className, Tags.self, Tags.definition(Tags.propertyName)),
            SpanStyle(color = Color(0xFF268BD2))
        ),
        TagStyleSpec(
            Tags.function(Tags.variableName),
            SpanStyle(color = Color(0xFF268BD2))
        ),
        TagStyleSpec(
            listOf(Tags.bool, Tags.`null`),
            SpanStyle(color = Color(0xFFB58900))
        ),
        TagStyleSpec(
            Tags.tagName,
            SpanStyle(color = Color(0xFF268BD2), fontWeight = FontWeight.Bold)
        ),
        TagStyleSpec(Tags.angleBracket, SpanStyle(color = Color(0xFF93A1A1))),
        TagStyleSpec(Tags.attributeName, SpanStyle(color = Color(0xFF93A1A1))),
        TagStyleSpec(Tags.typeName, SpanStyle(color = Color(0xFF859900)))
    )
)

/**
 * Complete Solarized Light editor theme with UI styling.
 *
 * Ported from thememirror solarized-light.ts.
 */
val solarizedLightTheme: EditorTheme = EditorTheme(
    background = Color(0xFFfef7e5),
    foreground = Color(0xFF586E75),
    cursor = Color(0xFF000000),
    selection = Color(0xFF073642),
    activeLineBackground = Color(0xFFEEE8D5),
    gutterBackground = Color(0xFFfef7e5),
    // gutterForeground: #586E7580 -> 0x80586E75
    gutterForeground = Color(0x80586E75),
    dark = false
)

/**
 * Extension combining [solarizedLightTheme] UI styling with
 * [solarizedLightHighlightStyle] syntax highlighting.
 *
 * Drop this into an editor's extension list for a complete Solarized Light look.
 */
val solarizedLight: Extension = extensionListOf(
    editorTheme.of(solarizedLightTheme),
    syntaxHighlighting(solarizedLightHighlightStyle)
)
