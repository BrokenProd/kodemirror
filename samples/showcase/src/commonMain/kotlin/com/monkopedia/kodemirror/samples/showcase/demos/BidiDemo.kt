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
 */
package com.monkopedia.kodemirror.samples.showcase.demos

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import com.monkopedia.kodemirror.lang.javascript.javascript
import com.monkopedia.kodemirror.samples.showcase.DemoScaffold
import com.monkopedia.kodemirror.samples.showcase.SampleDocs
import com.monkopedia.kodemirror.samples.showcase.showcaseSetup
import com.monkopedia.kodemirror.state.Extension
import com.monkopedia.kodemirror.state.RangeSet
import com.monkopedia.kodemirror.state.plus
import com.monkopedia.kodemirror.view.Decoration
import com.monkopedia.kodemirror.view.DecorationSet
import com.monkopedia.kodemirror.view.EditorSession
import com.monkopedia.kodemirror.view.KodeMirror
import com.monkopedia.kodemirror.view.MarkDecorationSpec
import com.monkopedia.kodemirror.view.MatchDecorator
import com.monkopedia.kodemirror.view.PluginValue
import com.monkopedia.kodemirror.view.ViewPlugin
import com.monkopedia.kodemirror.view.ViewUpdate
import com.monkopedia.kodemirror.view.editorContentStyle
import com.monkopedia.kodemirror.view.perLineTextDirection
import com.monkopedia.kodemirror.view.rememberEditorSession
import kodemirror.samples.showcase.generated.resources.JetBrainsMono_Regular
import kodemirror.samples.showcase.generated.resources.NotoSansArabic_Regular
import kodemirror.samples.showcase.generated.resources.NotoSansHebrew_Regular
import kodemirror.samples.showcase.generated.resources.Res
import org.jetbrains.compose.resources.Font

/**
 * Creates an extension that applies Arabic/Hebrew resource fonts to RTL character
 * ranges. Needed on wasmJs where Compose doesn't do per-character font fallback.
 */
@Composable
private fun rtlFontExtension(): Extension {
    val arabicFont = FontFamily(Font(Res.font.NotoSansArabic_Regular))
    val hebrewFont = FontFamily(Font(Res.font.NotoSansHebrew_Regular))

    val arabicMark = Decoration.mark(
        MarkDecorationSpec(style = SpanStyle(fontFamily = arabicFont))
    )
    val hebrewMark = Decoration.mark(
        MarkDecorationSpec(style = SpanStyle(fontFamily = hebrewFont))
    )

    // Match runs of Arabic or Hebrew characters (including combining marks and spaces between)
    val arabicRanges = "[\\u0600-\\u06FF\\u0750-\\u077F\\u08A0-\\u08FF\\uFB50-\\uFDFF\\uFE70-\\uFEFF]+"
    val hebrewRanges = "[\\u0590-\\u05FF\\uFB1D-\\uFB4F]+"
    val decorator = MatchDecorator(
        regexp = Regex("$arabicRanges|$hebrewRanges"),
        decorate = { add, match, _ ->
            val from = match.range.first
            val to = match.range.last + 1
            val firstChar = match.value[0]
            val mark = if (firstChar.code in 0x0590..0x05FF ||
                firstChar.code in 0xFB1D..0xFB4F
            ) {
                hebrewMark
            } else {
                arabicMark
            }
            add(from, to, mark)
        }
    )

    return ViewPlugin.define(
        create = { view -> RtlFontPlugin(view, decorator) },
        configure = {
            copy(
                decorations = { plugin ->
                    (plugin as? RtlFontPlugin)?.decos ?: RangeSet.empty()
                }
            )
        }
    ).asExtension()
}

private class RtlFontPlugin(
    view: EditorSession,
    private val decorator: MatchDecorator
) : PluginValue {
    var decos: DecorationSet = decorator.createDeco(view)

    override fun update(update: ViewUpdate) {
        decos = decorator.updateDeco(update, decos)
    }
}

@Composable
fun BidiDemo() {
    val monoFont = FontFamily(Font(Res.font.JetBrainsMono_Regular))
    val rtlExt = rtlFontExtension()
    var fontsReady by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        Res.readBytes("font/JetBrainsMono_Regular.ttf")
        Res.readBytes("font/NotoSansArabic_Regular.ttf")
        Res.readBytes("font/NotoSansHebrew_Regular.ttf")
        fontsReady = true
    }
    DemoScaffold(
        title = "Bidirectional Text",
        description = "perLineTextDirection enabled for mixed LTR/RTL content."
    ) {
        if (fontsReady) {
            // --8<-- [start:bidi-setup]
            val session = rememberEditorSession(
                doc = SampleDocs.bidi,
                extensions = showcaseSetup + javascript().extension +
                    perLineTextDirection.of(true) +
                    editorContentStyle.of(TextStyle(fontFamily = monoFont)) +
                    rtlExt
            )
            // --8<-- [end:bidi-setup]
            KodeMirror(
                session = session,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(Modifier.fillMaxSize())
        }
    }
}
