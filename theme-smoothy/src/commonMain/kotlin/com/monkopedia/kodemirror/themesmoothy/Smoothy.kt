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
package com.monkopedia.kodemirror.themesmoothy

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
 * Smoothy syntax highlighting style.
 *
 * Ported from thememirror smoothy.ts. Light theme by Kenneth Reitz.
 */
val smoothyHighlightStyle = HighlightStyle.define(
    listOf(
        TagStyleSpec(Tags.comment, SpanStyle(color = Color(0xFFCFCFCF))),
        TagStyleSpec(
            listOf(Tags.number, Tags.bool, Tags.`null`),
            SpanStyle(color = Color(0xFFE66C29))
        ),
        TagStyleSpec(
            listOf(
                Tags.className,
                Tags.definition(Tags.propertyName),
                Tags.function(Tags.variableName),
                Tags.labelName,
                Tags.definition(Tags.typeName)
            ),
            SpanStyle(color = Color(0xFF2EB43B))
        ),
        TagStyleSpec(Tags.keyword, SpanStyle(color = Color(0xFFD8B229))),
        TagStyleSpec(
            Tags.operator,
            SpanStyle(color = Color(0xFF4EA44E), fontWeight = FontWeight.Bold)
        ),
        TagStyleSpec(
            listOf(Tags.definitionKeyword, Tags.modifier),
            SpanStyle(color = Color(0xFF925A47))
        ),
        TagStyleSpec(Tags.string, SpanStyle(color = Color(0xFF704D3D))),
        TagStyleSpec(Tags.typeName, SpanStyle(color = Color(0xFF2F8996))),
        TagStyleSpec(
            listOf(Tags.variableName, Tags.propertyName),
            SpanStyle(color = Color(0xFF77ACB0))
        ),
        TagStyleSpec(
            Tags.self,
            SpanStyle(color = Color(0xFF77ACB0), fontWeight = FontWeight.Bold)
        ),
        TagStyleSpec(Tags.regexp, SpanStyle(color = Color(0xFFE3965E))),
        TagStyleSpec(
            listOf(Tags.tagName, Tags.angleBracket),
            SpanStyle(color = Color(0xFFBAA827))
        ),
        TagStyleSpec(Tags.attributeName, SpanStyle(color = Color(0xFFB06520))),
        TagStyleSpec(Tags.derefOperator, SpanStyle(color = Color(0xFF000000)))
    )
)

/**
 * Complete Smoothy editor theme with UI styling.
 *
 * Ported from thememirror smoothy.ts.
 */
val smoothyTheme: EditorTheme = EditorTheme(
    background = Color(0xFFFFFFFF),
    foreground = Color(0xFF000000),
    cursor = Color(0xFF000000),
    // selection: #FFFD0054 -> 0x54FFFD00
    selection = Color(0x54FFFD00),
    // lineHighlight: #00000008 -> 0x08000000
    activeLineBackground = Color(0x08000000),
    gutterBackground = Color(0xFFFFFFFF),
    // gutterForeground: #00000070 -> 0x70000000
    gutterForeground = Color(0x70000000),
    dark = false
)

/**
 * Extension combining [smoothyTheme] UI styling with [smoothyHighlightStyle] syntax highlighting.
 *
 * Drop this into an editor's extension list for a complete Smoothy look.
 */
val smoothy: Extension = extensionListOf(
    editorTheme.of(smoothyTheme),
    syntaxHighlighting(smoothyHighlightStyle)
)
