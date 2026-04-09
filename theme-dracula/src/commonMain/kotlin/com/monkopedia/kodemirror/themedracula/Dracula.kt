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
package com.monkopedia.kodemirror.themedracula

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
 * Named color constants from the thememirror Dracula palette.
 *
 * Original theme by Zeno Rocha, ported from thememirror.
 */
object DraculaColors {
    val background = Color(0xFF2d2f3f)
    val foreground = Color(0xFFf8f8f2)
    val caret = Color(0xFFf8f8f0)
    val selection = Color(0xFF44475a)
    val gutterBackground = Color(0xFF282a36)
    val gutterForeground = Color(0xFF909194)
    val lineHighlight = Color(0xFF44475a)
    val comment = Color(0xFF6272a4)
    val string = Color(0xFFf1fa8c)
    val numberSelfBoolNull = Color(0xFFbd93f9)
    val keywordOperator = Color(0xFFff79c6)
    val definitionKeywordTypeName = Color(0xFF8be9fd)
    val typeDefinition = Color(0xFFf8f8f2)
    val classPropertyFunctionAttribute = Color(0xFF50fa7b)
}

/**
 * Dracula syntax highlighting style.
 *
 * Ported from thememirror dracula.ts.
 */
val draculaHighlightStyle = HighlightStyle.define(
    listOf(
        TagStyleSpec(Tags.comment, SpanStyle(color = DraculaColors.comment)),
        TagStyleSpec(
            listOf(Tags.string, Tags.special(Tags.brace)),
            SpanStyle(color = DraculaColors.string)
        ),
        TagStyleSpec(
            listOf(Tags.number, Tags.self, Tags.bool, Tags.`null`),
            SpanStyle(color = DraculaColors.numberSelfBoolNull)
        ),
        TagStyleSpec(
            listOf(Tags.keyword, Tags.operator),
            SpanStyle(color = DraculaColors.keywordOperator)
        ),
        TagStyleSpec(
            listOf(Tags.definitionKeyword, Tags.typeName),
            SpanStyle(color = DraculaColors.definitionKeywordTypeName)
        ),
        TagStyleSpec(
            Tags.definition(Tags.typeName),
            SpanStyle(color = DraculaColors.typeDefinition)
        ),
        TagStyleSpec(
            listOf(
                Tags.className,
                Tags.definition(Tags.propertyName),
                Tags.function(Tags.variableName),
                Tags.attributeName
            ),
            SpanStyle(color = DraculaColors.classPropertyFunctionAttribute)
        )
    )
)

/**
 * Complete Dracula editor theme with UI styling.
 *
 * Ported from thememirror dracula.ts.
 */
val draculaTheme: EditorTheme = EditorTheme(
    background = DraculaColors.background,
    foreground = DraculaColors.foreground,
    cursor = DraculaColors.caret,
    selection = DraculaColors.selection,
    activeLineBackground = DraculaColors.lineHighlight,
    gutterBackground = DraculaColors.gutterBackground,
    gutterForeground = DraculaColors.gutterForeground,
    dark = true
)

/**
 * Extension combining [draculaTheme] UI styling with
 * [draculaHighlightStyle] syntax highlighting.
 *
 * Drop this into an editor's extension list for a complete Dracula look.
 */
val dracula: Extension = extensionListOf(
    editorTheme.of(draculaTheme),
    syntaxHighlighting(draculaHighlightStyle)
)
