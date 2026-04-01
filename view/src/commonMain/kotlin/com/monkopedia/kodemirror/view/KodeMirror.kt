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

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.monkopedia.kodemirror.state.ChangeSpec
import com.monkopedia.kodemirror.state.DocPos
import com.monkopedia.kodemirror.state.EditorState
import com.monkopedia.kodemirror.state.EditorStateConfig
import com.monkopedia.kodemirror.state.Extension
import com.monkopedia.kodemirror.state.SelectionSpec
import com.monkopedia.kodemirror.state.Transaction
import com.monkopedia.kodemirror.state.TransactionSpec
import com.monkopedia.kodemirror.state.asDoc
import com.monkopedia.kodemirror.state.asInsert

/**
 * The main editor composable.
 *
 * Wires up the [EditorSession] with plugin hosting, layout caching, and
 * input handling, then renders the editor content.
 *
 * @param session  The [EditorSession] to display.
 * @param modifier Modifier applied to the outermost container.
 */
@Composable
fun KodeMirror(session: EditorSession, modifier: Modifier = Modifier) {
    val impl = session as EditorSessionImpl
    val state by session::state

    val pluginHost = remember(session) {
        ViewPluginHost(session).also { it.syncToState(state, null) }
    }
    val lineLayoutCache = remember(session) { LineLayoutCache() }
    // Deferred layouts: onGloballyPositioned fires bottom-up (children before
    // parent), so editorCoordinates is null on the first layout pass. We store
    // the child coordinates here and populate the cache once the parent fires.
    val pendingLineLayouts = remember(session) {
        mutableMapOf<Int, PendingLineLayout>()
    }
    // TextLayoutResult per line, populated from onTextLayout (always reliable).
    // Used as a fallback for tap positioning when lineLayoutCache is incomplete.
    val textLayoutResults = remember(session) {
        mutableMapOf<Int, TextLayoutResult>()
    }

    val compositionScope = rememberCoroutineScope()

    // Wire up session internals
    DisposableEffect(session) {
        impl.pluginHost = pluginHost
        impl.lineLayoutCache = lineLayoutCache
        impl.backingCoroutineScope = compositionScope
        onDispose {
            pluginHost.destroy()
            lineLayoutCache.clear()
            impl.pluginHost = null
            impl.lineLayoutCache = null
            impl.backingCoroutineScope = null
        }
    }

    // Event-driven paste handler for wasmJs (where platformClipboardGet()
    // returns null). The JS paste event listener invokes this callback directly.
    DisposableEffect(session) {
        platformRegisterPasteHandler { pasteText ->
            val sel = session.state.selection.main
            session.dispatch(
                TransactionSpec(
                    changes = ChangeSpec.Single(
                        from = sel.from,
                        to = sel.to,
                        insert = pasteText.asInsert()
                    ),
                    selection = SelectionSpec.CursorSpec(
                        DocPos(sel.from.value + pasteText.length)
                    ),
                    userEvent = "input.paste"
                )
            )
        }
        onDispose { platformUnregisterPasteHandler() }
    }

    // Derive rendering data from current state
    val theme = state.facet(editorTheme)
    val contentStyle = state.facet(editorContentStyle).let { style ->
        if (style.color == Color.Unspecified) style.copy(color = theme.foreground) else style
    }
    val allPanels = buildList {
        state.facet(showPanel)?.let { add(it) }
        addAll(state.facet(showPanels))
    }
    val topPanels = allPanels.filter { it.top }
    val bottomPanels = allPanels.filter { !it.top }
    val hasGutters = state.facet(gutters).isNotEmpty()
    val viewport = Viewport(0, state.doc.length)
    // Compute columnItems when state changes. Decorations (both from
    // facets and plugins) are derived from state, so `state` is a
    // sufficient remember key. Avoid using list concatenation results
    // as keys — the `+` operator creates a new reference every
    // composition, defeating memoization.
    val columnItems = remember(state) {
        val extensionDecos = state.facet(decorations)
        val pluginDecos = pluginHost.collectDecorations()
        buildColumnItems(state, viewport, extensionDecos + pluginDecos)
    }

    val lazyState = rememberLazyListState()

    // Track Alt key state for rectangular selection
    var altPressed by remember { mutableStateOf(false) }

    // Track editor layout coordinates for position mapping
    var editorCoordinates: LayoutCoordinates? by remember { mutableStateOf(null) }

    // Focus management
    val focusRequester = remember { FocusRequester() }

    // Auto-focus the hidden text field when the editor first appears
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        platformFocusInput()
    }

    // Track whether the platform callback already handled this keydown.
    // The document-level listener fires in capture phase BEFORE Skiko
    // processes the event. If the callback handled the key, onPreviewKeyEvent
    // should skip to avoid double-handling.
    val keyProcessedByCallback = remember { booleanArrayOf(false) }

    // Register a platform-level key handler that receives ALL keydown events.
    // This is the primary input path on wasmJs because Playwright (and some
    // headless environments) dispatch key events to BODY rather than the
    // canvas in the shadow DOM, so Skiko never generates Compose KeyEvents.
    // For real users (events targeting the canvas), both this callback AND
    // onPreviewKeyEvent fire — the flag prevents double-handling.
    DisposableEffect(session) {
        platformRegisterKeyHandler handler@{ key, ctrl, alt, meta, shift ->
            // Clear the flag at the start of each key event.
            // It will be set to true only if we handle this key.
            keyProcessedByCallback[0] = false
            val handled = handleRawKeyEvent(session, key, ctrl, alt, meta, shift)
            if (handled) {
                keyProcessedByCallback[0] = true
            }
            handled
        }
        onDispose {
            platformUnregisterKeyHandler()
        }
    }

    // Prevent tap from overriding drag selection
    var recentlyDragged by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val lineHeightDp = with(density) { contentStyle.lineHeight.toDp() }

    // Compute gutter width based on digit count + padding (5dp + 3dp)
    val configs = state.facet(gutters)
    val gutterWidthDp = if (hasGutters) {
        val maxDigits = state.doc.lines.toString().length
        val charWidthDp = with(density) {
            (contentStyle.fontSize.toPx() * 0.65f).toDp()
        }
        val lineNumberWidth = charWidthDp * maxDigits +
            theme.layout.gutterStartPadding + theme.layout.gutterEndPadding
        val extraGutterWidth =
            theme.layout.customGutterWidth *
                configs.count { it.type != GutterType.LineNumbers && it.lineMarker != null }
        lineNumberWidth + extraGutterWidth
    } else {
        0.dp
    }

    // Content height in px (top padding + items + bottom padding)
    val contentHeightPx = with(density) {
        (
            theme.layout.contentTopPadding + lineHeightDp * columnItems.size +
                theme.layout.contentBottomPadding
            ).toPx()
    }

    CompositionLocalProvider(
        LocalEditorTheme provides theme,
        LocalContentTextStyle provides contentStyle,
        LocalEditorSession provides session
    ) {
        Column(modifier = modifier.fillMaxSize()) {
            for (panel in topPanels) {
                Box(Modifier.fillMaxWidth().background(theme.panelBackground)) {
                    panel.content(this)
                }
                Box(
                    Modifier.fillMaxWidth().height(theme.layout.panelBorderWidth)
                        .background(theme.panelBorderColor)
                )
            }
            Box(
                modifier = Modifier
                    .testTag("KodeMirror")
                    .weight(1f)
                    .fillMaxWidth()
                    .onGloballyPositioned { coords ->
                        editorCoordinates = coords
                        // Process pending line layouts synchronously.
                        // onGloballyPositioned fires bottom-up: children
                        // stored their data in pendingLineLayouts because
                        // editorCoordinates was null when they fired. Now
                        // that the parent has coordinates, drain the map.
                        if (pendingLineLayouts.isNotEmpty()) {
                            val pending = pendingLineLayouts.toMap()
                            pendingLineLayouts.clear()
                            for ((_, p) in pending) {
                                if (p.coords.isAttached) {
                                    val pos = coords.localPositionOf(
                                        p.coords,
                                        Offset.Zero
                                    )
                                    lineLayoutCache.store(
                                        p.lineNumber,
                                        p.lineFrom,
                                        pos.y,
                                        pos.x,
                                        p.result
                                    )
                                }
                            }
                        }
                    }
                    .drawEditorBackground(
                        theme = theme,
                        hasGutters = hasGutters,
                        gutterWidthDp = gutterWidthDp,
                        contentHeightPx = contentHeightPx
                    )
                    .onPreviewKeyEvent { event ->
                        altPressed = event.isAltPressed
                        false
                    }
                    .pointerInput(session) {
                        detectTapGestures { offset ->
                            if (recentlyDragged) {
                                recentlyDragged = false
                                return@detectTapGestures
                            }
                            focusRequester.requestFocus()
                            platformFocusInput()
                            // Use LazyColumn layout info (always reliable) with
                            // cache-based positioning as fallback.
                            val pos = posFromVisibleItems(
                                offset,
                                lazyState,
                                columnItems,
                                textLayoutResults,
                                hasGutters,
                                gutterWidthDp,
                                density
                            )
                                ?: session.posAtCoords(offset.x, offset.y)
                                ?: return@detectTapGestures
                            session.dispatch(
                                TransactionSpec(
                                    selection = SelectionSpec.CursorSpec(DocPos(pos))
                                )
                            )
                        }
                    }
                    .pointerInput(session) {
                        var dragStart = Offset.Zero
                        var dragCurrent = Offset.Zero
                        detectDragGestures(
                            onDragStart = { offset ->
                                recentlyDragged = true
                                focusRequester.requestFocus()
                                platformFocusInput()
                                dragStart = offset
                                dragCurrent = offset
                            },
                            onDrag = { _, dragAmount ->
                                dragCurrent += dragAmount
                                if (altPressed) {
                                    handleRectangularDrag(
                                        session,
                                        dragStart,
                                        dragCurrent
                                    )
                                } else {
                                    handleDrag(
                                        session,
                                        dragStart,
                                        dragCurrent
                                    )
                                }
                                val pos = session.posAtCoords(
                                    dragCurrent.x,
                                    dragCurrent.y
                                )
                                session.plugin(dropCursorViewPlugin)
                                    ?.moveTo(pos)
                            },
                            onDragEnd = {
                                recentlyDragged = false
                                session.plugin(dropCursorViewPlugin)
                                    ?.moveTo(null)
                            },
                            onDragCancel = {
                                recentlyDragged = false
                                session.plugin(dropCursorViewPlugin)
                                    ?.moveTo(null)
                            }
                        )
                    }
                    .pointerInput(session) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent(
                                    PointerEventPass.Main
                                )
                                val pos = event.changes
                                    .firstOrNull()?.position
                                if (pos != null) {
                                    val hoverTooltips =
                                        impl.pluginHost
                                            ?.collectHoverPlugins()
                                            ?: emptyList()
                                    for (plugin in hoverTooltips) {
                                        plugin.updateHover(pos.x, pos.y)
                                    }
                                }
                            }
                        }
                    }
            ) {
                EditorContent(
                    lazyState = lazyState,
                    focusRequester = focusRequester,
                    columnItems = columnItems,
                    hasGutters = hasGutters,
                    gutterWidthDp = gutterWidthDp,
                    lineHeightDp = lineHeightDp,
                    lineLayoutCache = lineLayoutCache,
                    pendingLineLayouts = pendingLineLayouts,
                    editorCoordinates = editorCoordinates,
                    textLayoutResults = textLayoutResults,
                    keyProcessedByCallback = keyProcessedByCallback
                )
            }
            for (panel in bottomPanels) {
                Box(
                    Modifier.fillMaxWidth().height(theme.layout.panelBorderWidth)
                        .background(theme.panelBorderColor)
                )
                Box(Modifier.fillMaxWidth().background(theme.panelBackground)) {
                    panel.content(this)
                }
            }
        }
    }
}

/** Inner content of the editor: hidden text field, line list, and tooltip layer. */
@Composable
private fun EditorContent(
    lazyState: LazyListState,
    focusRequester: FocusRequester,
    columnItems: List<ColumnItem>,
    hasGutters: Boolean,
    gutterWidthDp: Dp,
    lineHeightDp: Dp,
    lineLayoutCache: LineLayoutCache,
    pendingLineLayouts: MutableMap<Int, PendingLineLayout>,
    editorCoordinates: LayoutCoordinates?,
    textLayoutResults: MutableMap<Int, TextLayoutResult>,
    keyProcessedByCallback: BooleanArray
) {
    val session = LocalEditorSession.current
    val impl = session as EditorSessionImpl
    val state by session::state
    val theme = LocalEditorTheme.current
    val contentStyle = LocalContentTextStyle.current

    // Hidden text field for receiving IME/text input and key events
    var hiddenTextValue by remember {
        mutableStateOf(TextFieldValue(""))
    }
    // Flag to suppress onValueChange when onPreviewKeyEvent consumed the key.
    // On wasmJs, returning true from onPreviewKeyEvent does NOT call
    // preventDefault() on the DOM event, so the browser still generates an
    // input event that triggers onValueChange. This flag bridges the gap.
    val suppressInput = remember { booleanArrayOf(false) }
    BasicTextField(
        value = hiddenTextValue,
        cursorBrush = SolidColor(Color.Transparent),
        onValueChange = { newValue ->
            if (suppressInput[0]) {
                suppressInput[0] = false
                hiddenTextValue = TextFieldValue("")
                return@BasicTextField
            }
            // Block text input when editor is read-only
            if (!session.editable) {
                hiddenTextValue = TextFieldValue("")
                return@BasicTextField
            }
            // Check if a vim-like extension wants to suppress text input.
            val shouldSuppress = session.state.facet(inputSuppressor)
                .any { it.invoke() }
            if (shouldSuppress) {
                hiddenTextValue = TextFieldValue("")
                return@BasicTextField
            }
            // Filter control characters (Tab, etc.) that leak through
            // when their key events aren't consumed by the keymap.
            // Preserve newlines — they are valid input (e.g. paste).
            val inserted = newValue.text.filter {
                it == '\n' || !it.isISOControl()
            }
            if (inserted.isNotEmpty()) {
                val sel = session.state.selection.main
                val from = sel.from
                val to = sel.to
                val newCursor = DocPos(from.value + inserted.length)
                session.dispatch(
                    TransactionSpec(
                        changes = ChangeSpec.Single(
                            from = from,
                            to = to,
                            insert = inserted.asInsert()
                        ),
                        selection = SelectionSpec.CursorSpec(newCursor),
                        userEvent = "input.type"
                    )
                )
            }
            // Reset to empty for next input
            hiddenTextValue = TextFieldValue("")
        },
        modifier = Modifier
            .testTag("KodeMirror_input")
            .size(1.dp)
            .alpha(0f)
            .focusRequester(focusRequester)
            .onFocusChanged { focusState ->
                impl.hasFocus = focusState.isFocused
            }
            .onPreviewKeyEvent { event ->
                // If the document-level callback already handled this
                // keydown, skip to avoid double-handling.
                if (event.type == KeyEventType.KeyDown &&
                    keyProcessedByCallback[0]
                ) {
                    keyProcessedByCallback[0] = false
                    suppressInput[0] = true
                    return@onPreviewKeyEvent true
                }
                val consumed = handleKeyEvent(session, event)
                if (consumed) {
                    suppressInput[0] = true
                } else if (event.type == KeyEventType.KeyDown && session.editable) {
                    // When the keymap doesn't consume a printable character,
                    // insert it directly. On wasmJs with canvas focus, the
                    // browser doesn't generate a text input event on the
                    // BasicTextField's backing element, so onValueChange
                    // won't fire. This bridges the gap.
                    // Don't insert for modified keys (Ctrl+A, Alt+X, etc.)
                    // — those are shortcuts, not text input.
                    val char = keyEventLayoutKey(event)
                        ?: keyEventCharacter(event)?.toString()
                    if (char != null && char.length == 1 &&
                        !char[0].isISOControl() &&
                        !event.isCtrlPressed &&
                        !event.isMetaPressed &&
                        !event.isAltPressed
                    ) {
                        val sel = session.state.selection.main
                        val newCursor = DocPos(sel.from.value + char.length)
                        session.dispatch(
                            TransactionSpec(
                                changes = ChangeSpec.Single(
                                    from = sel.from,
                                    to = sel.to,
                                    insert = char.asInsert()
                                ),
                                selection = SelectionSpec.CursorSpec(newCursor),
                                userEvent = "input.type"
                            )
                        )
                        suppressInput[0] = true
                        return@onPreviewKeyEvent true
                    }
                }
                consumed
            }
    )

    LazyColumn(
        state = lazyState,
        contentPadding = PaddingValues(
            top = theme.layout.contentTopPadding,
            bottom = theme.layout.contentBottomPadding
        )
    ) {
        items(
            items = columnItems,
            key = { item ->
                when (item) {
                    is ColumnItem.TextLine -> "line-${item.lineNumber}"
                    is ColumnItem.BlockWidgetItem -> "widget-${item.from}-${item.type}"
                }
            }
        ) { item ->
            when (item) {
                is ColumnItem.TextLine -> {
                    val capturedLineNum = item.lineNumber
                    val capturedFrom = item.from
                    var textLayout by remember {
                        mutableStateOf<TextLayoutResult?>(null)
                    }

                    val lineModifier: Modifier = Modifier
                        .fillMaxWidth()
                        .height(lineHeightDp)
                    var contentExtraModifier: Modifier = Modifier
                        .padding(start = 6.dp, end = 2.dp)
                    for (deco in item.lineDecorations) {
                        val bg = deco.spec.style?.background
                        if (bg != null && bg != Color.Unspecified) {
                            contentExtraModifier = contentExtraModifier.background(bg)
                        }
                    }
                    Row(
                        modifier = lineModifier,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (hasGutters) {
                            GutterView(
                                session = session,
                                lineNumber = item.lineNumber.value,
                                modifier = Modifier.width(gutterWidthDp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .then(contentExtraModifier)
                                .drawSelectionOverlay(
                                    state,
                                    item.from.value,
                                    item.to.value,
                                    theme,
                                    textLayout,
                                    item.tabOffsetMap
                                )
                                .onGloballyPositioned { contentCoords ->
                                    val layout = textLayout
                                        ?: return@onGloballyPositioned
                                    val editorCoords = editorCoordinates
                                    if (editorCoords != null &&
                                        editorCoords.isAttached
                                    ) {
                                        val pos = editorCoords.localPositionOf(
                                            contentCoords,
                                            Offset.Zero
                                        )
                                        lineLayoutCache.store(
                                            capturedLineNum.value,
                                            capturedFrom.value,
                                            pos.y,
                                            pos.x,
                                            layout
                                        )
                                        pendingLineLayouts
                                            .remove(capturedLineNum.value)
                                    } else {
                                        pendingLineLayouts[capturedLineNum.value] =
                                            PendingLineLayout(
                                                capturedLineNum.value,
                                                capturedFrom.value,
                                                contentCoords,
                                                layout
                                            )
                                    }
                                }
                        ) {
                            val renderContent = item.content
                            if ('\t' in renderContent.text || item.tabOffsetMap != null) {
                                println("[TAB-RENDER] Line ${item.lineNumber}: content='${renderContent.text}' len=${renderContent.text.length} hasTabMap=${item.tabOffsetMap != null}")
                            }
                            BasicText(
                                text = renderContent,
                                style = contentStyle,
                                onTextLayout = { result: TextLayoutResult ->
                                    textLayout = result
                                    textLayoutResults[capturedLineNum.value] =
                                        result
                                }
                            )
                            for (widget in item.inlineWidgets) {
                                widget.spec.widget.Content()
                            }
                        }
                    }
                }

                is ColumnItem.BlockWidgetItem -> {
                    item.widget.spec.widget.Content()
                }
            }
        }
    }

    // Sync viewport/height tracking for ViewUpdate flags.
    // Evict stale cache entries for scrolled-off lines.
    // IMPORTANT: lazyState.layoutInfo must NOT be read during
    // composition — it produces a new object every layout pass,
    // creating an infinite recomposition/layout cycle. Use
    // snapshotFlow to observe it outside of composition.
    val firstVisible = lazyState.firstVisibleItemIndex
    LaunchedEffect(firstVisible) {
        impl.lastFirstVisibleItem = firstVisible
    }
    LaunchedEffect(session) {
        snapshotFlow {
            lazyState.layoutInfo.visibleItemsInfo.let { items ->
                items.firstOrNull()?.index to items.size
            }
        }.collect { (startIdx, count) ->
            val startIndex = startIdx ?: 0
            val visibleLineNumbers = columnItems
                .drop(startIndex)
                .take(count.coerceAtLeast(1))
                .filterIsInstance<ColumnItem.TextLine>()
                .map { it.lineNumber.value }
                .toSet()
            if (visibleLineNumbers.isNotEmpty()) {
                lineLayoutCache.evict(visibleLineNumbers)
            }
        }
    }

    // Tooltip layer
    TooltipLayer(session = session)
}

/** Draw editor and gutter backgrounds behind content. */
private fun Modifier.drawEditorBackground(
    theme: EditorTheme,
    hasGutters: Boolean,
    gutterWidthDp: Dp,
    contentHeightPx: Float
): Modifier = drawWithContent {
    drawRect(theme.background)
    if (hasGutters) {
        val w = gutterWidthDp.toPx()
        val contentH = contentHeightPx.coerceAtMost(size.height)
        drawRect(
            color = theme.gutterBackground,
            topLeft = Offset.Zero,
            size = Size(w, contentH)
        )
        val bc = theme.gutterBorderColor
        if (bc != Color.Transparent) {
            drawLine(
                color = bc,
                start = Offset(w - 0.5f, 0f),
                end = Offset(w - 0.5f, contentH),
                strokeWidth = 1f
            )
        }
    }
    drawContent()
}

/**
 * Create and remember an [EditorSession] with the given document text and extensions.
 */
@Composable
fun rememberEditorSession(
    doc: String = "",
    extensions: Extension? = null,
    onUpdate: (Transaction) -> Unit = {}
): EditorSession {
    return remember {
        val config = EditorStateConfig(
            doc = doc.asDoc(),
            extensions = extensions
        )
        EditorSession(EditorState.create(config), onUpdate)
    }
}

/**
 * Create and remember an [EditorSession] from an [EditorStateConfig].
 */
@Composable
fun rememberEditorSession(
    config: EditorStateConfig,
    onUpdate: (Transaction) -> Unit = {}
): EditorSession {
    return remember { EditorSession(EditorState.create(config), onUpdate) }
}

/** Layout info saved when [onGloballyPositioned] fires before the parent. */
private class PendingLineLayout(
    val lineNumber: Int,
    val lineFrom: Int,
    val coords: LayoutCoordinates,
    val result: TextLayoutResult
)

/**
 * Compute a document position from a tap offset using the [LazyColumn]'s own
 * layout info. This is reliable even when [LineLayoutCache] hasn't been
 * populated yet (first render / page switch).
 */
private fun posFromVisibleItems(
    offset: Offset,
    lazyState: LazyListState,
    columnItems: List<ColumnItem>,
    textLayoutResults: Map<Int, TextLayoutResult>,
    hasGutters: Boolean,
    gutterWidthDp: Dp,
    density: Density
): Int? {
    val contentStartPx = with(density) {
        (if (hasGutters) gutterWidthDp.toPx() else 0f) + 6.dp.toPx()
    }
    for (info in lazyState.layoutInfo.visibleItemsInfo) {
        val item = columnItems.getOrNull(info.index)
            as? ColumnItem.TextLine ?: continue
        val itemTop = info.offset.toFloat()
        val itemBottom = itemTop + info.size.toFloat()
        if (offset.y >= itemTop && offset.y < itemBottom) {
            val layout = textLayoutResults[item.lineNumber.value]
                ?: return item.from.value // no layout yet, return line start
            val localY = offset.y - itemTop
            val localX = (offset.x - contentStartPx).coerceAtLeast(0f)
            val expandedOffset = layout.getOffsetForPosition(
                Offset(localX, localY)
            )
            val charOffset = unmapTabOffset(
                expandedOffset,
                item.tabOffsetMap
            )
            return item.from.value +
                charOffset.coerceIn(0, item.to.value - item.from.value)
        }
    }
    return null
}
