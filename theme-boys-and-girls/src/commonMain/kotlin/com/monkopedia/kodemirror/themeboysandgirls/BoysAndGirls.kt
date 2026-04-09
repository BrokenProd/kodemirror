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
package com.monkopedia.kodemirror.themeboysandgirls

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
 * Boys and Girls syntax highlighting style.
 *
 * Ported from thememirror boys-and-girls.ts. Dark theme.
 */
val boysAndGirlsHighlightStyle = HighlightStyle.define(
    listOf(
        TagStyleSpec(Tags.comment, SpanStyle(color = Color(0xFF404040))),
        TagStyleSpec(
            listOf(Tags.string, Tags.special(Tags.brace), Tags.regexp),
            SpanStyle(color = Color(0xFF00D8FF))
        ),
        TagStyleSpec(Tags.number, SpanStyle(color = Color(0xFFE62286))),
        TagStyleSpec(
            listOf(Tags.variableName, Tags.attributeName, Tags.self),
            SpanStyle(color = Color(0xFFE62286), fontWeight = FontWeight.Bold)
        ),
        TagStyleSpec(
            Tags.function(Tags.variableName),
            SpanStyle(color = Color(0xFFFFFFFF), fontWeight = FontWeight.Bold)
        )
    )
)

/**
 * Complete Boys and Girls editor theme with UI styling.
 *
 * Ported from thememirror boys-and-girls.ts.
 */
val boysAndGirlsTheme: EditorTheme = EditorTheme(
    background = Color(0xFF000205),
    foreground = Color(0xFFFFFFFF),
    cursor = Color(0xFFE60065),
    // selection: #E60C6559 -> 0x59E60C65
    selection = Color(0x59E60C65),
    // lineHighlight: #4DD7FC1A -> 0x1A4DD7FC
    activeLineBackground = Color(0x1A4DD7FC),
    gutterBackground = Color(0xFF000205),
    // gutterForeground: #ffffff90 -> 0x90ffffff
    gutterForeground = Color(0x90ffffff),
    dark = true
)

/**
 * Extension combining [boysAndGirlsTheme] UI styling with
 * [boysAndGirlsHighlightStyle] syntax highlighting.
 *
 * Drop this into an editor's extension list for a complete Boys and Girls look.
 */
val boysAndGirls: Extension = extensionListOf(
    editorTheme.of(boysAndGirlsTheme),
    syntaxHighlighting(boysAndGirlsHighlightStyle)
)
