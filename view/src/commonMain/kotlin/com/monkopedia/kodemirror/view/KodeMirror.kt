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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.isAltPressed
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
import androidx.compose.ui.unit.dp
import com.monkopedia.kodemirror.state.ChangeSpec
import com.monkopedia.kodemirror.state.EditorState
import com.monkopedia.kodemirror.state.EditorStateConfig
import com.monkopedia.kodemirror.state.Extension
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

    // TODO: Replace paste polling with event-driven approach.
    // Disabled: the while(true)/delay(16) loop prevents the compose test
    // recomposer from ever reaching idle, causing test hangs.

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
                    .drawWithContent {
                        // Editor background
                        drawRect(theme.background)
                        // Gutter background strip — only as tall as content
                        if (hasGutters) {
                            val w = gutterWidthDp.toPx()
                            val contentH = contentHeightPx.coerceAtMost(
                                size.height
                            )
                            drawRect(
                                color = theme.gutterBackground,
                                topLeft = Offset.Zero,
                                size = androidx.compose.ui.geometry.Size(
                                    w,
                                    contentH
                                )
                            )
                            val bc = theme.gutterBorderColor
                            if (bc != Color.Transparent) {
                                drawLine(
                                    color = bc,
                                    start = Offset(w - 0.5f, 0f),
                                    end = Offset(
                                        w - 0.5f,
                                        contentH
                                    ),
                                    strokeWidth = 1f
                                )
                            }
                        }
                        drawContent()
                    }
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
                                    selection = com.monkopedia.kodemirror.state
                                        .SelectionSpec.CursorSpec(
                                            com.monkopedia.kodemirror.state.DocPos(pos)
                                        )
                                )
                            )
                        }
                    }
                    .pointerInput(session) {
                        var dragStart = androidx.compose.ui.geometry.Offset.Zero
                        var dragCurrent = androidx.compose.ui.geometry.Offset.Zero
                        detectDragGestures(
                            onDragStart = { offset ->
                                recentlyDragged = true
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
                // Hidden text field for receiving IME/text input and key events
                var hiddenTextValue by remember {
                    mutableStateOf(TextFieldValue(""))
                }
                BasicTextField(
                    value = hiddenTextValue,
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(
                        Color.Transparent
                    ),
                    onValueChange = { newValue ->
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
                            val newCursor = com.monkopedia.kodemirror.state.DocPos(
                                from.value + inserted.length
                            )
                            session.dispatch(
                                TransactionSpec(
                                    changes = ChangeSpec.Single(
                                        from = from,
                                        to = to,
                                        insert = inserted.asInsert()
                                    ),
                                    selection = com.monkopedia.kodemirror.state.SelectionSpec
                                        .CursorSpec(newCursor),
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
                            handleKeyEvent(session, event)
                        }
                )

                LazyColumn(
                    state = lazyState,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
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
                                    mutableStateOf<TextLayoutResult?>(
                                        null
                                    )
                                }

                                val lineModifier: Modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = lineHeightDp)
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
                                            .then(contentExtraModifier)
                                            .drawSelectionOverlay(
                                                state,
                                                item.from.value,
                                                item.to.value,
                                                theme,
                                                textLayout
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
                                        BasicText(
                                            text = item.content,
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
    lazyState: androidx.compose.foundation.lazy.LazyListState,
    columnItems: List<ColumnItem>,
    textLayoutResults: Map<Int, TextLayoutResult>,
    hasGutters: Boolean,
    gutterWidthDp: androidx.compose.ui.unit.Dp,
    density: androidx.compose.ui.unit.Density
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
            val charOffset = layout.getOffsetForPosition(Offset(localX, localY))
            return item.from.value +
                charOffset.coerceIn(0, layout.layoutInput.text.length)
        }
    }
    return null
}
