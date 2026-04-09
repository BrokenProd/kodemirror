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
package com.monkopedia.kodemirror.themecoolglow

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
 * Cool Glow syntax highlighting style.
 *
 * Ported from thememirror cool-glow.ts. Dark theme.
 */
val coolGlowHighlightStyle = HighlightStyle.define(
    listOf(
        TagStyleSpec(Tags.comment, SpanStyle(color = Color(0xFFAEAEAE))),
        TagStyleSpec(
            listOf(Tags.string, Tags.special(Tags.brace), Tags.regexp),
            SpanStyle(color = Color(0xFF8DFF8E))
        ),
        TagStyleSpec(
            listOf(
                Tags.className,
                Tags.definition(Tags.propertyName),
                Tags.function(Tags.variableName),
                Tags.function(Tags.definition(Tags.variableName)),
                Tags.definition(Tags.typeName)
            ),
            SpanStyle(color = Color(0xFFA3EBFF))
        ),
        TagStyleSpec(
            listOf(Tags.number, Tags.bool, Tags.`null`),
            SpanStyle(color = Color(0xFF62E9BD))
        ),
        TagStyleSpec(
            listOf(Tags.keyword, Tags.operator),
            SpanStyle(color = Color(0xFF2BF1DC))
        ),
        TagStyleSpec(
            listOf(Tags.definitionKeyword, Tags.modifier),
            SpanStyle(color = Color(0xFFF8FBB1))
        ),
        TagStyleSpec(
            listOf(Tags.variableName, Tags.self),
            SpanStyle(color = Color(0xFFB683CA))
        ),
        TagStyleSpec(
            listOf(Tags.angleBracket, Tags.tagName, Tags.typeName, Tags.propertyName),
            SpanStyle(color = Color(0xFF60A4F1))
        ),
        TagStyleSpec(Tags.derefOperator, SpanStyle(color = Color(0xFFE0E0E0))),
        TagStyleSpec(Tags.attributeName, SpanStyle(color = Color(0xFF7BACCA)))
    )
)

/**
 * Complete Cool Glow editor theme with UI styling.
 *
 * Ported from thememirror cool-glow.ts.
 */
val coolGlowTheme: EditorTheme = EditorTheme(
    background = Color(0xFF060521),
    foreground = Color(0xFFE0E0E0),
    // caret: #FFFFFFA6 -> 0xA6FFFFFF
    cursor = Color(0xA6FFFFFF),
    selection = Color(0xFF122BBB),
    // lineHighlight: #FFFFFF0F -> 0x0FFFFFFF
    activeLineBackground = Color(0x0FFFFFFF),
    gutterBackground = Color(0xFF060521),
    // gutterForeground: #E0E0E090 -> 0x90E0E0E0
    gutterForeground = Color(0x90E0E0E0),
    dark = true
)

/**
 * Extension combining [coolGlowTheme] UI styling with [coolGlowHighlightStyle] syntax highlighting.
 *
 * Drop this into an editor's extension list for a complete Cool Glow look.
 */
val coolGlow: Extension = extensionListOf(
    editorTheme.of(coolGlowTheme),
    syntaxHighlighting(coolGlowHighlightStyle)
)
