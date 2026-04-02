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

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import com.monkopedia.kodemirror.basicsetup.basicSetup
import com.monkopedia.kodemirror.state.Extension
import com.monkopedia.kodemirror.state.plus
import com.monkopedia.kodemirror.themonedark.oneDark
import com.monkopedia.kodemirror.view.editorContentStyle

/**
 * Showcase-specific setup: [basicSetup] plus the [oneDark] theme,
 * matching how a CodeMirror project uses `basicSetup` + a theme extension.
 *
 * Explicitly sets [FontFamily.Monospace] to ensure monospace rendering
 * on all platforms. The [defaultEditorFontFamily] uses [SystemFont] names
 * that don't reliably resolve on wasmJs (Skiko's canvas rendering needs
 * fonts loaded through CSS or Compose resources).
 */
val showcaseSetup: Extension = basicSetup + oneDark +
    editorContentStyle.of(TextStyle(fontFamily = FontFamily.Monospace))
