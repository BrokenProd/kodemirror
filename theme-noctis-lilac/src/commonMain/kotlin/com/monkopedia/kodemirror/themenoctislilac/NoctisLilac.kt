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
package com.monkopedia.kodemirror.themenoctislilac

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
 * Noctis Lilac syntax highlighting style.
 *
 * Ported from thememirror noctis-lilac.ts. Light theme by Liviu Schera.
 */
val noctisLilacHighlightStyle = HighlightStyle.define(
    listOf(
        TagStyleSpec(Tags.comment, SpanStyle(color = Color(0xFF9995b7))),
        TagStyleSpec(
            Tags.keyword,
            SpanStyle(color = Color(0xFFff5792), fontWeight = FontWeight.Bold)
        ),
        TagStyleSpec(
            listOf(Tags.definitionKeyword, Tags.modifier),
            SpanStyle(color = Color(0xFFff5792))
        ),
        TagStyleSpec(
            listOf(Tags.className, Tags.tagName, Tags.definition(Tags.typeName)),
            SpanStyle(color = Color(0xFF0094f0))
        ),
        TagStyleSpec(
            listOf(Tags.number, Tags.bool, Tags.`null`, Tags.special(Tags.brace)),
            SpanStyle(color = Color(0xFF5842ff))
        ),
        TagStyleSpec(
            listOf(Tags.definition(Tags.propertyName), Tags.function(Tags.variableName)),
            SpanStyle(color = Color(0xFF0095a8))
        ),
        TagStyleSpec(Tags.typeName, SpanStyle(color = Color(0xFFb3694d))),
        TagStyleSpec(
            listOf(Tags.propertyName, Tags.variableName),
            SpanStyle(color = Color(0xFFfa8900))
        ),
        TagStyleSpec(Tags.operator, SpanStyle(color = Color(0xFFff5792))),
        TagStyleSpec(Tags.self, SpanStyle(color = Color(0xFFe64100))),
        TagStyleSpec(
            listOf(Tags.string, Tags.regexp),
            SpanStyle(color = Color(0xFF00b368))
        ),
        TagStyleSpec(
            listOf(Tags.paren, Tags.bracket),
            SpanStyle(color = Color(0xFF0431fa))
        ),
        TagStyleSpec(Tags.labelName, SpanStyle(color = Color(0xFF00bdd6))),
        TagStyleSpec(Tags.attributeName, SpanStyle(color = Color(0xFFe64100))),
        TagStyleSpec(Tags.angleBracket, SpanStyle(color = Color(0xFF9995b7)))
    )
)

/**
 * Complete Noctis Lilac editor theme with UI styling.
 *
 * Ported from thememirror noctis-lilac.ts.
 */
val noctisLilacTheme: EditorTheme = EditorTheme(
    background = Color(0xFFf2f1f8),
    foreground = Color(0xFF0c006b),
    cursor = Color(0xFF5c49e9),
    selection = Color(0xFFd5d1f2),
    activeLineBackground = Color(0xFFe1def3),
    gutterBackground = Color(0xFFf2f1f8),
    // gutterForeground: #0c006b70 -> 0x700c006b
    gutterForeground = Color(0x700c006b),
    dark = false
)

/**
 * Extension combining [noctisLilacTheme] UI styling with
 * [noctisLilacHighlightStyle] syntax highlighting.
 *
 * Drop this into an editor's extension list for a complete Noctis Lilac look.
 */
val noctisLilac: Extension = extensionListOf(
    editorTheme.of(noctisLilacTheme),
    syntaxHighlighting(noctisLilacHighlightStyle)
)
