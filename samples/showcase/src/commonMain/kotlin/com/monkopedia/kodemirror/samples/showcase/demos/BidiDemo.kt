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
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.SystemFont
import com.monkopedia.kodemirror.lang.javascript.javascript
import com.monkopedia.kodemirror.samples.showcase.DemoScaffold
import com.monkopedia.kodemirror.samples.showcase.SampleDocs
import com.monkopedia.kodemirror.samples.showcase.showcaseSetup
import com.monkopedia.kodemirror.state.plus
import com.monkopedia.kodemirror.view.KodeMirror
import com.monkopedia.kodemirror.view.editorContentStyle
import com.monkopedia.kodemirror.view.perLineTextDirection
import com.monkopedia.kodemirror.view.rememberEditorSession
import kodemirror.samples.showcase.generated.resources.NotoSansArabic_Regular
import kodemirror.samples.showcase.generated.resources.NotoSansHebrew_Regular
import kodemirror.samples.showcase.generated.resources.Res
import org.jetbrains.compose.resources.Font

@OptIn(ExperimentalTextApi::class)
@Composable
fun BidiDemo() {
    val bidiFontFamily = FontFamily(
        Font(Res.font.NotoSansArabic_Regular),
        Font(Res.font.NotoSansHebrew_Regular),
        SystemFont("JetBrains Mono"),
        SystemFont("DejaVu Sans Mono"),
        SystemFont("Noto Sans Mono"),
        SystemFont("Courier New"),
        SystemFont("monospace"),
        SystemFont("sans-serif")
    )
    // Preload fonts before rendering the editor by resolving them eagerly
    var fontsReady by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        // Load the font bytes to ensure they're cached before rendering
        Res.readBytes("font/NotoSansArabic_Regular.ttf")
        Res.readBytes("font/NotoSansHebrew_Regular.ttf")
        fontsReady = true
    }
    DemoScaffold(
        title = "Bidirectional Text",
        description = "perLineTextDirection enabled for mixed LTR/RTL content."
    ) {
        if (fontsReady) {
            val session = rememberEditorSession(
                doc = SampleDocs.bidi,
                extensions = showcaseSetup + javascript().extension +
                    perLineTextDirection.of(true) +
                    editorContentStyle.of(TextStyle(fontFamily = bidiFontFamily))
            )
            KodeMirror(
                session = session,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(Modifier.fillMaxSize())
        }
    }
}
