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
package com.monkopedia.kodemirror.view

import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.isShiftPressed
import kotlin.JsFun
import kotlin.js.JsString

@JsFun(
    """() => {
    var ua = navigator.userAgent.toLowerCase();
    if (ua.indexOf('mac') !== -1) return 'Mac';
    if (ua.indexOf('win') !== -1) return 'Windows';
    return 'Linux';
}"""
)
private external fun detectOsFromBrowser(): String

@JsFun("(text) => { navigator.clipboard.writeText(text); }")
private external fun jsClipboardWrite(text: String)

// Capture the browser's layout-aware event.key on every keydown.
// Installed eagerly at module load so it's ready before the first key event.
// Uses capture phase on document so it fires before Skiko/Compose processes
// the event on the canvas.
//
// Routes ALL keys through __kodeKeyCallback because Playwright (and some
// headless environments) dispatch key events to BODY rather than the canvas
// in the shadow DOM, so Skiko never generates Compose KeyEvents for them.
// The callback receives (key, ctrlKey, altKey, metaKey, shiftKey) and should
// return true if the key was handled (to prevent default browser behavior).
// For real users (events targeting the canvas), both this callback AND
// onPreviewKeyEvent may fire; a Kotlin-side flag prevents double-handling.
@JsFun(
    """() => {
    globalThis.__kodeKey = '';
    globalThis.__kodeKeyCallback = null;
    var special = ['Home','End','Tab','Backspace','Delete','Enter','Escape',
        'ArrowUp','ArrowDown','ArrowLeft','ArrowRight',
        'PageUp','PageDown','F1','F2','F3','F4','F5','F6',
        'F7','F8','F9','F10','F11','F12'];
    document.addEventListener('keydown', function(e) {
        globalThis.__kodeKey = e.key;
        var isModified = e.ctrlKey || e.metaKey || e.altKey;
        if ((e.ctrlKey || e.metaKey) && e.key === 'v') {
            e.stopPropagation();
            return;
        }
        if (isModified || special.indexOf(e.key) !== -1) {
            e.preventDefault();
        }
        // Route all keys through the Kotlin callback. This is necessary
        // because Playwright (and some headless environments) dispatch key
        // events to BODY rather than the canvas in the shadow DOM, so Skiko
        // never converts them to Compose KeyEvents. The Kotlin callback
        // sets a flag so onPreviewKeyEvent skips if Skiko also processes
        // the same event (preventing double-handling).
        if (globalThis.__kodeKeyCallback) {
            var handled = globalThis.__kodeKeyCallback(
                e.key, e.ctrlKey, e.altKey, e.metaKey, e.shiftKey
            );
            if (handled) {
                e.preventDefault();
            }
        }
    }, true);
    document.addEventListener('paste', function(e) {
        var text = (e.clipboardData || window.clipboardData).getData('text');
        if (text && globalThis.__kodePasteCallback) {
            globalThis.__kodePasteCallback(text);
        }
        e.stopPropagation();
        e.preventDefault();
    }, true);
}"""
)
private external fun installKeyCapture()

@JsFun(
    """(cb) => {
    globalThis.__kodeKeyCallback = function(key, ctrl, alt, meta, shift) {
        return cb(key, ctrl, alt, meta, shift);
    };
}"""
)
private external fun jsSetKeyCallback(
    callback: (JsString, Boolean, Boolean, Boolean, Boolean) -> Boolean
)

@JsFun("() => { globalThis.__kodeKeyCallback = null; }")
private external fun jsClearKeyCallback()

@JsFun("() => globalThis.__kodeKey || ''")
private external fun readCapturedKey(): String

/**
 * Focus the canvas in the shadow DOM so keyboard events reach Skiko's
 * event handler. Skiko processes key events from the canvas element
 * and converts them to Compose KeyEvents that flow through
 * onPreviewKeyEvent.
 */
@JsFun(
    """() => {
    var shadow = document.body.shadowRoot;
    if (shadow) {
        var canvas = shadow.querySelector('canvas');
        if (canvas) canvas.focus();
    }
}"""
)
private external fun platformFocusCanvas()

internal actual fun platformRegisterKeyHandler(
    handler: (key: String, ctrl: Boolean, alt: Boolean, meta: Boolean, shift: Boolean) -> Boolean
) {
    jsSetKeyCallback { key, ctrl, alt, meta, shift ->
        handler(key.toString(), ctrl, alt, meta, shift)
    }
}

internal actual fun platformUnregisterKeyHandler() {
    jsClearKeyCallback()
}

internal actual fun platformFocusInput() {
    platformFocusCanvas()
}

@JsFun("(cb) => { globalThis.__kodePasteCallback = cb; }")
private external fun jsSetPasteCallback(callback: (JsString) -> Unit)

@JsFun("() => { globalThis.__kodePasteCallback = null; }")
private external fun jsClearPasteCallback()

// Eagerly install the capture listener when this file is first loaded.
// platformOsName() is called during currentOs initialization (before any
// key events), which triggers loading of this file and runs this initializer.
@Suppress("unused")
private val keyCaptureInstalled: Boolean = run {
    installKeyCapture()
    true
}

// US-layout shift map for Playwright compatibility.
// Playwright sends the unshifted e.key for Shift+digit/symbol combinations.
// A real browser would send the shifted character (e.g., "$" for Shift+4).
private val SHIFT_MAP = mapOf(
    '1' to '!', '2' to '@', '3' to '#', '4' to '$', '5' to '%',
    '6' to '^', '7' to '&', '8' to '*', '9' to '(', '0' to ')',
    '-' to '_', '=' to '+', '[' to '{', ']' to '}', '\\' to '|',
    ';' to ':', '\'' to '"', ',' to '<', '.' to '>', '/' to '?',
    '`' to '~'
)

internal actual fun platformOsName(): String = detectOsFromBrowser()

internal actual fun keyEventCharacter(event: KeyEvent): Char? {
    // On wasmJs, character input flows through BasicTextField's onValueChange
    return null
}

actual fun keyEventLayoutKey(event: KeyEvent): String? {
    // Reference keyCaptureInstalled to prevent dead-code elimination of the
    // property initializer that installs the document keydown listener.
    keyCaptureInstalled
    val key = readCapturedKey()
    // Browser's event.key is a single character for printable keys ("x", "z")
    // and a longer string for special keys ("Enter", "Tab").
    if (key.length != 1) return null
    // Playwright's keyboard.press("Shift+j") sends e.key="j" (not "J") and
    // keyboard.press("Shift+4") sends e.key="4" (not "$"). A real browser
    // would give the shifted character. Use Compose's isShiftPressed + a
    // shift map to produce the correct character.
    if (event.isShiftPressed) {
        return SHIFT_MAP[key[0]]?.toString() ?: key.uppercase()
    }
    return key
}

internal actual fun platformClipboardGet(): String? {
    // Clipboard API on web is async; not supported in synchronous context.
    // Returns null so clipboardPaste returns false, letting the browser's
    // native paste event flow through to the hidden BasicTextField's
    // onValueChange handler.
    return null
}

internal actual fun platformClipboardSet(text: String) {
    try {
        jsClipboardWrite(text)
    } catch (_: Throwable) {
        // Clipboard API may not be available in all contexts
    }
}

internal actual fun platformRegisterPasteHandler(handler: (String) -> Unit) {
    jsSetPasteCallback { jsText -> handler(jsText.toString()) }
}

internal actual fun platformUnregisterPasteHandler() {
    jsClearPasteCallback()
}
