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
package com.monkopedia.kodemirror.samples.showcase

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import com.monkopedia.kodemirror.basicsetup.basicSetup
import com.monkopedia.kodemirror.state.Extension
import com.monkopedia.kodemirror.state.plus
import com.monkopedia.kodemirror.themonedark.oneDark
import com.monkopedia.kodemirror.view.editorContentStyle
import kodemirror.samples.showcase.generated.resources.JetBrainsMono_Regular
import kodemirror.samples.showcase.generated.resources.Res
import org.jetbrains.compose.resources.Font

/**
 * Base showcase setup without font override: [basicSetup] + [oneDark].
 */
private val showcaseBase: Extension = basicSetup + oneDark

/**
 * Showcase-specific setup: [basicSetup] plus the [oneDark] theme and
 * JetBrains Mono font loaded from bundled resources.
 *
 * This is a `@Composable` property because [Font] from Compose Resources
 * requires a composable context. All showcase demo composables access this
 * to get monospace rendering on wasmJs where [SystemFont] names don't
 * resolve through CanvasKit's font manager.
 */
val showcaseSetup: Extension
    @Composable get() {
        val monoFont = FontFamily(Font(Res.font.JetBrainsMono_Regular))
        return showcaseBase + editorContentStyle.of(
            TextStyle(fontFamily = monoFont)
        )
    }
