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
 * Original theme by Fred LeBlanc.
 */
package com.monkopedia.kodemirror.themeclouds

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
 * Clouds syntax highlighting style.
 *
 * Ported from thememirror clouds.ts. Light theme by Fred LeBlanc.
 */
val cloudsHighlightStyle = HighlightStyle.define(
    listOf(
        TagStyleSpec(Tags.comment, SpanStyle(color = Color(0xFFBCC8BA))),
        TagStyleSpec(
            listOf(Tags.string, Tags.special(Tags.brace), Tags.regexp),
            SpanStyle(color = Color(0xFF5D90CD))
        ),
        TagStyleSpec(
            listOf(Tags.number, Tags.bool, Tags.`null`),
            SpanStyle(color = Color(0xFF46A609))
        ),
        TagStyleSpec(Tags.keyword, SpanStyle(color = Color(0xFFAF956F))),
        TagStyleSpec(
            listOf(Tags.definitionKeyword, Tags.modifier),
            SpanStyle(color = Color(0xFFC52727))
        ),
        TagStyleSpec(
            listOf(Tags.angleBracket, Tags.tagName, Tags.attributeName),
            SpanStyle(color = Color(0xFF606060))
        ),
        TagStyleSpec(Tags.self, SpanStyle(color = Color(0xFF000000)))
    )
)

/**
 * Complete Clouds editor theme with UI styling.
 *
 * Ported from thememirror clouds.ts.
 */
val cloudsTheme: EditorTheme = EditorTheme(
    background = Color(0xFFFFFFFF),
    foreground = Color(0xFF000000),
    cursor = Color(0xFF000000),
    selection = Color(0xFFBDD5FC),
    activeLineBackground = Color(0xFFFFFBD1),
    gutterBackground = Color(0xFFFFFFFF),
    // gutterForeground: #00000070 -> 0x70000000
    gutterForeground = Color(0x70000000),
    dark = false
)

/**
 * Extension combining [cloudsTheme] UI styling with [cloudsHighlightStyle] syntax highlighting.
 *
 * Drop this into an editor's extension list for a complete Clouds look.
 */
val clouds: Extension = extensionListOf(
    editorTheme.of(cloudsTheme),
    syntaxHighlighting(cloudsHighlightStyle)
)
