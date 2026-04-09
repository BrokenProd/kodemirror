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
 * Original theme by Chris Kempson.
 */
package com.monkopedia.kodemirror.themetomorrow

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
 * Tomorrow syntax highlighting style.
 *
 * Ported from thememirror tomorrow.ts. Light theme by Chris Kempson.
 */
val tomorrowHighlightStyle = HighlightStyle.define(
    listOf(
        TagStyleSpec(Tags.comment, SpanStyle(color = Color(0xFF8E908C))),
        TagStyleSpec(
            listOf(Tags.variableName, Tags.self, Tags.propertyName, Tags.attributeName, Tags.regexp),
            SpanStyle(color = Color(0xFFC82829))
        ),
        TagStyleSpec(
            listOf(Tags.number, Tags.bool, Tags.`null`),
            SpanStyle(color = Color(0xFFF5871F))
        ),
        TagStyleSpec(
            listOf(Tags.className, Tags.typeName, Tags.definition(Tags.typeName)),
            SpanStyle(color = Color(0xFFC99E00))
        ),
        TagStyleSpec(
            listOf(Tags.string, Tags.special(Tags.brace)),
            SpanStyle(color = Color(0xFF718C00))
        ),
        TagStyleSpec(Tags.operator, SpanStyle(color = Color(0xFF3E999F))),
        TagStyleSpec(
            listOf(Tags.definition(Tags.propertyName), Tags.function(Tags.variableName)),
            SpanStyle(color = Color(0xFF4271AE))
        ),
        TagStyleSpec(Tags.keyword, SpanStyle(color = Color(0xFF8959A8))),
        TagStyleSpec(Tags.derefOperator, SpanStyle(color = Color(0xFF4D4D4C)))
    )
)

/**
 * Complete Tomorrow editor theme with UI styling.
 *
 * Ported from thememirror tomorrow.ts.
 */
val tomorrowTheme: EditorTheme = EditorTheme(
    background = Color(0xFFFFFFFF),
    foreground = Color(0xFF4D4D4C),
    cursor = Color(0xFFAEAFAD),
    selection = Color(0xFFD6D6D6),
    activeLineBackground = Color(0xFFEFEFEF),
    gutterBackground = Color(0xFFFFFFFF),
    // gutterForeground: #4D4D4C80 -> 0x804D4D4C
    gutterForeground = Color(0x804D4D4C),
    dark = false
)

/**
 * Extension combining [tomorrowTheme] UI styling with [tomorrowHighlightStyle] syntax highlighting.
 *
 * Drop this into an editor's extension list for a complete Tomorrow look.
 */
val tomorrow: Extension = extensionListOf(
    editorTheme.of(tomorrowTheme),
    syntaxHighlighting(tomorrowHighlightStyle)
)
