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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.monkopedia.kodemirror.basicsetup.basicSetup
import com.monkopedia.kodemirror.lang.javascript.javascript
import com.monkopedia.kodemirror.state.Extension
import com.monkopedia.kodemirror.state.plus
import com.monkopedia.kodemirror.themedracula.dracula
import com.monkopedia.kodemirror.themesolarizedlight.solarizedLight
import com.monkopedia.kodemirror.themonedark.oneDark
import com.monkopedia.kodemirror.view.KodeMirror
import com.monkopedia.kodemirror.view.editorTheme
import com.monkopedia.kodemirror.view.lightEditorTheme
import com.monkopedia.kodemirror.view.rememberEditorSession

private fun themeExtension(themeName: String): Extension = when (themeName) {
    "onedark" -> oneDark
    "solarizedlight" -> solarizedLight
    "dracula" -> dracula
    else -> editorTheme.of(lightEditorTheme)
}

@Composable
fun ThemeTestPage(themeName: String) {
    val session = rememberEditorSession(
        doc = SampleDocs.javascript,
        extensions = basicSetup +
            javascript().extension +
            themeExtension(themeName) +
            testBridgeExtension
    )
    Box(Modifier.fillMaxSize()) {
        KodeMirror(
            session = session,
            modifier = Modifier.fillMaxSize()
        )
    }
}
