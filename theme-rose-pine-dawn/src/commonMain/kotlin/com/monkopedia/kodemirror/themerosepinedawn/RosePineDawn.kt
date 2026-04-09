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
package com.monkopedia.kodemirror.themerosepinedawn

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
 * Rosé Pine Dawn syntax highlighting style.
 *
 * Ported from thememirror rose-pine-dawn.ts. Light theme by Rosé Pine.
 */
val rosePineDawnHighlightStyle = HighlightStyle.define(
    listOf(
        TagStyleSpec(Tags.comment, SpanStyle(color = Color(0xFF9893a5))),
        TagStyleSpec(
            listOf(Tags.bool, Tags.`null`),
            SpanStyle(color = Color(0xFF286983))
        ),
        TagStyleSpec(Tags.number, SpanStyle(color = Color(0xFFd7827e))),
        TagStyleSpec(Tags.className, SpanStyle(color = Color(0xFFd7827e))),
        TagStyleSpec(
            listOf(Tags.angleBracket, Tags.tagName, Tags.typeName),
            SpanStyle(color = Color(0xFF56949f))
        ),
        TagStyleSpec(Tags.attributeName, SpanStyle(color = Color(0xFF907aa9))),
        TagStyleSpec(Tags.punctuation, SpanStyle(color = Color(0xFF797593))),
        TagStyleSpec(
            listOf(Tags.keyword, Tags.modifier),
            SpanStyle(color = Color(0xFF286983))
        ),
        TagStyleSpec(
            listOf(Tags.string, Tags.regexp),
            SpanStyle(color = Color(0xFFea9d34))
        ),
        TagStyleSpec(Tags.variableName, SpanStyle(color = Color(0xFFd7827e)))
    )
)

/**
 * Complete Rosé Pine Dawn editor theme with UI styling.
 *
 * Ported from thememirror rose-pine-dawn.ts.
 */
val rosePineDawnTheme: EditorTheme = EditorTheme(
    background = Color(0xFFfaf4ed),
    foreground = Color(0xFF575279),
    cursor = Color(0xFF575279),
    // selection: #6e6a8614 -> 0x146e6a86
    selection = Color(0x146e6a86),
    // lineHighlight: #6e6a860d -> 0x0d6e6a86
    activeLineBackground = Color(0x0d6e6a86),
    gutterBackground = Color(0xFFfaf4ed),
    // gutterForeground: #57527970 -> 0x70575279
    gutterForeground = Color(0x70575279),
    dark = false
)

/**
 * Extension combining [rosePineDawnTheme] UI styling with
 * [rosePineDawnHighlightStyle] syntax highlighting.
 *
 * Drop this into an editor's extension list for a complete Rosé Pine Dawn look.
 */
val rosePineDawn: Extension = extensionListOf(
    editorTheme.of(rosePineDawnTheme),
    syntaxHighlighting(rosePineDawnHighlightStyle)
)
