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
 * Original theme by Michael Diolosa.
 */
package com.monkopedia.kodemirror.themebespin

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
 * Bespin syntax highlighting style.
 *
 * Ported from thememirror bespin.ts. Dark theme by Michael Diolosa.
 */
val bespinHighlightStyle = HighlightStyle.define(
    listOf(
        TagStyleSpec(Tags.comment, SpanStyle(color = Color(0xFF666666))),
        TagStyleSpec(
            listOf(Tags.string, Tags.special(Tags.brace)),
            SpanStyle(color = Color(0xFF54BE0D))
        ),
        TagStyleSpec(Tags.regexp, SpanStyle(color = Color(0xFFE9C062))),
        TagStyleSpec(Tags.number, SpanStyle(color = Color(0xFFCF6A4C))),
        TagStyleSpec(
            listOf(Tags.keyword, Tags.operator),
            SpanStyle(color = Color(0xFF5EA6EA))
        ),
        TagStyleSpec(Tags.variableName, SpanStyle(color = Color(0xFF7587A6))),
        TagStyleSpec(
            listOf(Tags.definitionKeyword, Tags.modifier),
            SpanStyle(color = Color(0xFFF9EE98))
        ),
        TagStyleSpec(
            listOf(Tags.propertyName, Tags.function(Tags.variableName)),
            SpanStyle(color = Color(0xFF937121))
        ),
        TagStyleSpec(
            listOf(Tags.typeName, Tags.angleBracket, Tags.tagName),
            SpanStyle(color = Color(0xFF9B859D))
        )
    )
)

/**
 * Complete Bespin editor theme with UI styling.
 *
 * Ported from thememirror bespin.ts.
 */
val bespinTheme: EditorTheme = EditorTheme(
    background = Color(0xFF2e241d),
    foreground = Color(0xFFBAAE9E),
    cursor = Color(0xFFA7A7A7),
    // selection: #DDF0FF33 -> 0x33DDF0FF
    selection = Color(0x33DDF0FF),
    // lineHighlight: #FFFFFF08 -> 0x08FFFFFF
    activeLineBackground = Color(0x08FFFFFF),
    gutterBackground = Color(0xFF28211C),
    // gutterForeground: #BAAE9E90 -> 0x90BAAE9E
    gutterForeground = Color(0x90BAAE9E),
    dark = true
)

/**
 * Extension combining [bespinTheme] UI styling with [bespinHighlightStyle] syntax highlighting.
 *
 * Drop this into an editor's extension list for a complete Bespin look.
 */
val bespin: Extension = extensionListOf(
    editorTheme.of(bespinTheme),
    syntaxHighlighting(bespinHighlightStyle)
)
