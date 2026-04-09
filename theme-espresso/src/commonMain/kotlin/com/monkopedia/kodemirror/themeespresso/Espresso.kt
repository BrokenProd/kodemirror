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
 * Original theme by TextMate.
 */
package com.monkopedia.kodemirror.themeespresso

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
 * Espresso syntax highlighting style.
 *
 * Ported from thememirror espresso.ts. Light theme by TextMate.
 */
val espressoHighlightStyle = HighlightStyle.define(
    listOf(
        TagStyleSpec(Tags.comment, SpanStyle(color = Color(0xFFAAAAAA))),
        TagStyleSpec(
            listOf(Tags.keyword, Tags.operator, Tags.typeName, Tags.tagName, Tags.propertyName),
            SpanStyle(color = Color(0xFF2F6F9F), fontWeight = FontWeight.Bold)
        ),
        TagStyleSpec(
            listOf(Tags.attributeName, Tags.definition(Tags.propertyName)),
            SpanStyle(color = Color(0xFF4F9FD0))
        ),
        TagStyleSpec(
            listOf(Tags.className, Tags.string, Tags.special(Tags.brace)),
            SpanStyle(color = Color(0xFFCF4F5F))
        ),
        TagStyleSpec(
            Tags.number,
            SpanStyle(color = Color(0xFFCF4F5F), fontWeight = FontWeight.Bold)
        ),
        TagStyleSpec(Tags.variableName, SpanStyle(fontWeight = FontWeight.Bold))
    )
)

/**
 * Complete Espresso editor theme with UI styling.
 *
 * Ported from thememirror espresso.ts.
 */
val espressoTheme: EditorTheme = EditorTheme(
    background = Color(0xFFFFFFFF),
    foreground = Color(0xFF000000),
    cursor = Color(0xFF000000),
    selection = Color(0xFF80C7FF),
    activeLineBackground = Color(0xFFC1E2F8),
    gutterBackground = Color(0xFFFFFFFF),
    // gutterForeground: #00000070 -> 0x70000000
    gutterForeground = Color(0x70000000),
    dark = false
)

/**
 * Extension combining [espressoTheme] UI styling with [espressoHighlightStyle] syntax highlighting.
 *
 * Drop this into an editor's extension list for a complete Espresso look.
 */
val espresso: Extension = extensionListOf(
    editorTheme.of(espressoTheme),
    syntaxHighlighting(espressoHighlightStyle)
)
