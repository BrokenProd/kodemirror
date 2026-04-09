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
 * Original theme by Konstantin Pschera.
 */
package com.monkopedia.kodemirror.themeayulight

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
 * Ayu Light syntax highlighting style.
 *
 * Ported from thememirror ayu-light.ts. Light theme by Konstantin Pschera.
 */
val ayuLightHighlightStyle = HighlightStyle.define(
    listOf(
        TagStyleSpec(Tags.comment, SpanStyle(color = Color(0x99787b80))),
        TagStyleSpec(Tags.string, SpanStyle(color = Color(0xFF86b300))),
        TagStyleSpec(Tags.regexp, SpanStyle(color = Color(0xFF4cbf99))),
        TagStyleSpec(
            listOf(Tags.number, Tags.bool, Tags.`null`),
            SpanStyle(color = Color(0xFFffaa33))
        ),
        TagStyleSpec(Tags.variableName, SpanStyle(color = Color(0xFF5c6166))),
        TagStyleSpec(
            listOf(Tags.definitionKeyword, Tags.modifier),
            SpanStyle(color = Color(0xFFfa8d3e))
        ),
        TagStyleSpec(
            listOf(Tags.keyword, Tags.special(Tags.brace)),
            SpanStyle(color = Color(0xFFfa8d3e))
        ),
        TagStyleSpec(Tags.operator, SpanStyle(color = Color(0xFFed9366))),
        TagStyleSpec(Tags.separator, SpanStyle(color = Color(0xB35c6166))),
        TagStyleSpec(Tags.punctuation, SpanStyle(color = Color(0xFF5c6166))),
        TagStyleSpec(
            listOf(Tags.definition(Tags.propertyName), Tags.function(Tags.variableName)),
            SpanStyle(color = Color(0xFFf2ae49))
        ),
        TagStyleSpec(
            listOf(Tags.className, Tags.definition(Tags.typeName)),
            SpanStyle(color = Color(0xFF22a4e6))
        ),
        TagStyleSpec(
            listOf(Tags.tagName, Tags.typeName, Tags.self, Tags.labelName),
            SpanStyle(color = Color(0xFF55b4d4))
        ),
        TagStyleSpec(Tags.angleBracket, SpanStyle(color = Color(0x8055b4d4))),
        TagStyleSpec(Tags.attributeName, SpanStyle(color = Color(0xFFf2ae49)))
    )
)

/**
 * Complete Ayu Light editor theme with UI styling.
 *
 * Ported from thememirror ayu-light.ts.
 */
val ayuLightTheme: EditorTheme = EditorTheme(
    background = Color(0xFFfcfcfc),
    foreground = Color(0xFF5c6166),
    cursor = Color(0xFFffaa33),
    selection = Color(0x26036dd6),
    activeLineBackground = Color(0x1a8a9199),
    gutterBackground = Color(0xFFfcfcfc),
    gutterForeground = Color(0x668a9199),
    dark = false
)

/**
 * Extension combining [ayuLightTheme] UI styling with [ayuLightHighlightStyle] syntax highlighting.
 *
 * Drop this into an editor's extension list for a complete Ayu Light look.
 */
val ayuLight: Extension = extensionListOf(
    editorTheme.of(ayuLightTheme),
    syntaxHighlighting(ayuLightHighlightStyle)
)
