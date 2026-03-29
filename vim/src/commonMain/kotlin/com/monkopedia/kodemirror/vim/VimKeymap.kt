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
package com.monkopedia.kodemirror.vim

/**
 * The default Vim keymap.
 *
 * This is the Kotlin port of the `defaultKeymap` array from the upstream
 * `@replit/codemirror-vim` package.  Every entry from the JS source is
 * represented here in the same order.
 */
val defaultKeymap: List<VimKeyCommand> = mutableListOf(
    // -----------------------------------------------------------------------
    // Key-to-key mappings (must come first so they can override others)
    // -----------------------------------------------------------------------
    KeyToKeyCommand(keys = "<Left>", toKeys = "h"),
    KeyToKeyCommand(keys = "<Right>", toKeys = "l"),
    KeyToKeyCommand(keys = "<Up>", toKeys = "k"),
    KeyToKeyCommand(keys = "<Down>", toKeys = "j"),
    KeyToKeyCommand(keys = "g<Up>", toKeys = "gk"),
    KeyToKeyCommand(keys = "g<Down>", toKeys = "gj"),
    KeyToKeyCommand(keys = "<Space>", toKeys = "l"),
    KeyToKeyCommand(keys = "<BS>", toKeys = "h"),
    KeyToKeyCommand(keys = "<Del>", toKeys = "x"),
    KeyToKeyCommand(keys = "<C-Space>", toKeys = "W"),
    KeyToKeyCommand(keys = "<C-BS>", toKeys = "B"),
    KeyToKeyCommand(keys = "<S-Space>", toKeys = "w"),
    KeyToKeyCommand(keys = "<S-BS>", toKeys = "b"),
    KeyToKeyCommand(keys = "<C-n>", toKeys = "j"),
    KeyToKeyCommand(keys = "<C-p>", toKeys = "k"),
    KeyToKeyCommand(keys = "<C-[>", toKeys = "<Esc>"),
    KeyToKeyCommand(keys = "<C-c>", toKeys = "<Esc>"),
    KeyToKeyCommand(keys = "<C-[>", toKeys = "<Esc>", context = "insert"),
    KeyToKeyCommand(keys = "<C-c>", toKeys = "<Esc>", context = "insert"),
    KeyToKeyCommand(keys = "<C-Esc>", toKeys = "<Esc>"),
    KeyToKeyCommand(keys = "<C-Esc>", toKeys = "<Esc>", context = "insert"),
    KeyToKeyCommand(keys = "s", toKeys = "cl", context = "normal"),
    KeyToKeyCommand(keys = "s", toKeys = "c", context = "visual"),
    KeyToKeyCommand(keys = "S", toKeys = "cc", context = "normal"),
    KeyToKeyCommand(keys = "S", toKeys = "VdO", context = "visual"),
    KeyToKeyCommand(keys = "<Home>", toKeys = "0"),
    KeyToKeyCommand(keys = "<End>", toKeys = "\$"),
    KeyToKeyCommand(keys = "<PageUp>", toKeys = "<C-b>"),
    KeyToKeyCommand(keys = "<PageDown>", toKeys = "<C-f>"),
    KeyToKeyCommand(keys = "<CR>", toKeys = "j^", context = "normal"),
    KeyToKeyCommand(keys = "<Ins>", toKeys = "i", context = "normal"),
    ActionCommand(
        keys = "<Ins>",
        action = "toggleOverwrite",
        context = "insert"
    ),

    // -----------------------------------------------------------------------
    // Motions
    // -----------------------------------------------------------------------
    MotionCommand(
        keys = "H",
        motion = "moveToTopLine",
        motionArgs = MotionArgs(linewise = true, toJumplist = true)
    ),
    MotionCommand(
        keys = "M",
        motion = "moveToMiddleLine",
        motionArgs = MotionArgs(linewise = true, toJumplist = true)
    ),
    MotionCommand(
        keys = "L",
        motion = "moveToBottomLine",
        motionArgs = MotionArgs(linewise = true, toJumplist = true)
    ),
    MotionCommand(
        keys = "h",
        motion = "moveByCharacters",
        motionArgs = MotionArgs(forward = false)
    ),
    MotionCommand(
        keys = "l",
        motion = "moveByCharacters",
        motionArgs = MotionArgs(forward = true)
    ),
    MotionCommand(
        keys = "j",
        motion = "moveByLines",
        motionArgs = MotionArgs(forward = true, linewise = true)
    ),
    MotionCommand(
        keys = "k",
        motion = "moveByLines",
        motionArgs = MotionArgs(forward = false, linewise = true)
    ),
    MotionCommand(
        keys = "gj",
        motion = "moveByDisplayLines",
        motionArgs = MotionArgs(forward = true)
    ),
    MotionCommand(
        keys = "gk",
        motion = "moveByDisplayLines",
        motionArgs = MotionArgs(forward = false)
    ),
    MotionCommand(
        keys = "w",
        motion = "moveByWords",
        motionArgs = MotionArgs(forward = true, wordEnd = false)
    ),
    MotionCommand(
        keys = "W",
        motion = "moveByWords",
        motionArgs = MotionArgs(forward = true, wordEnd = false, bigWord = true)
    ),
    MotionCommand(
        keys = "e",
        motion = "moveByWords",
        motionArgs = MotionArgs(forward = true, wordEnd = true, inclusive = true)
    ),
    MotionCommand(
        keys = "E",
        motion = "moveByWords",
        motionArgs = MotionArgs(
            forward = true,
            wordEnd = true,
            bigWord = true,
            inclusive = true
        )
    ),
    MotionCommand(
        keys = "b",
        motion = "moveByWords",
        motionArgs = MotionArgs(forward = false, wordEnd = false)
    ),
    MotionCommand(
        keys = "B",
        motion = "moveByWords",
        motionArgs = MotionArgs(forward = false, wordEnd = false, bigWord = true)
    ),
    MotionCommand(
        keys = "ge",
        motion = "moveByWords",
        motionArgs = MotionArgs(forward = false, wordEnd = true, inclusive = true)
    ),
    MotionCommand(
        keys = "gE",
        motion = "moveByWords",
        motionArgs = MotionArgs(
            forward = false,
            wordEnd = true,
            bigWord = true,
            inclusive = true
        )
    ),
    MotionCommand(
        keys = "{",
        motion = "moveByParagraph",
        motionArgs = MotionArgs(forward = false, toJumplist = true)
    ),
    MotionCommand(
        keys = "}",
        motion = "moveByParagraph",
        motionArgs = MotionArgs(forward = true, toJumplist = true)
    ),
    MotionCommand(
        keys = "(",
        motion = "moveBySentence",
        motionArgs = MotionArgs(forward = false)
    ),
    MotionCommand(
        keys = ")",
        motion = "moveBySentence",
        motionArgs = MotionArgs(forward = true)
    ),
    MotionCommand(
        keys = "<C-f>",
        motion = "moveByPage",
        motionArgs = MotionArgs(forward = true)
    ),
    MotionCommand(
        keys = "<C-b>",
        motion = "moveByPage",
        motionArgs = MotionArgs(forward = false)
    ),
    MotionCommand(
        keys = "<C-d>",
        motion = "moveByScroll",
        motionArgs = MotionArgs(forward = true, explicitRepeat = true)
    ),
    MotionCommand(
        keys = "<C-u>",
        motion = "moveByScroll",
        motionArgs = MotionArgs(forward = false, explicitRepeat = true)
    ),
    MotionCommand(
        keys = "gg",
        motion = "moveToLineOrEdgeOfDocument",
        motionArgs = MotionArgs(
            forward = false,
            explicitRepeat = true,
            linewise = true,
            toJumplist = true
        )
    ),
    MotionCommand(
        keys = "G",
        motion = "moveToLineOrEdgeOfDocument",
        motionArgs = MotionArgs(
            forward = true,
            explicitRepeat = true,
            linewise = true,
            toJumplist = true
        )
    ),
    MotionCommand(keys = "g\$", motion = "moveToEndOfDisplayLine"),
    MotionCommand(keys = "g^", motion = "moveToStartOfDisplayLine"),
    MotionCommand(keys = "g0", motion = "moveToStartOfDisplayLine"),
    MotionCommand(keys = "0", motion = "moveToStartOfLine"),
    MotionCommand(keys = "^", motion = "moveToFirstNonWhiteSpaceCharacter"),
    MotionCommand(
        keys = "+",
        motion = "moveByLines",
        motionArgs = MotionArgs(forward = true, toFirstChar = true)
    ),
    MotionCommand(
        keys = "-",
        motion = "moveByLines",
        motionArgs = MotionArgs(forward = false, toFirstChar = true)
    ),
    MotionCommand(
        keys = "_",
        motion = "moveByLines",
        motionArgs = MotionArgs(
            forward = true,
            toFirstChar = true,
            repeatOffset = -1
        )
    ),
    MotionCommand(
        keys = "\$",
        motion = "moveToEol",
        motionArgs = MotionArgs(inclusive = true)
    ),
    MotionCommand(
        keys = "%",
        motion = "moveToMatchedSymbol",
        motionArgs = MotionArgs(inclusive = true, toJumplist = true)
    ),
    MotionCommand(
        keys = "f<character>",
        motion = "moveToCharacter",
        motionArgs = MotionArgs(forward = true, inclusive = true)
    ),
    MotionCommand(
        keys = "F<character>",
        motion = "moveToCharacter",
        motionArgs = MotionArgs(forward = false)
    ),
    MotionCommand(
        keys = "t<character>",
        motion = "moveTillCharacter",
        motionArgs = MotionArgs(forward = true, inclusive = true)
    ),
    MotionCommand(
        keys = "T<character>",
        motion = "moveTillCharacter",
        motionArgs = MotionArgs(forward = false)
    ),
    MotionCommand(
        keys = ";",
        motion = "repeatLastCharacterSearch",
        motionArgs = MotionArgs(forward = true)
    ),
    MotionCommand(
        keys = ",",
        motion = "repeatLastCharacterSearch",
        motionArgs = MotionArgs(forward = false)
    ),
    MotionCommand(
        keys = "'<register>",
        motion = "goToMark",
        motionArgs = MotionArgs(toJumplist = true, linewise = true)
    ),
    MotionCommand(
        keys = "`<register>",
        motion = "goToMark",
        motionArgs = MotionArgs(toJumplist = true)
    ),
    MotionCommand(
        keys = "]`",
        motion = "jumpToMark",
        motionArgs = MotionArgs(forward = true)
    ),
    MotionCommand(
        keys = "[`",
        motion = "jumpToMark",
        motionArgs = MotionArgs(forward = false)
    ),
    MotionCommand(
        keys = "]'",
        motion = "jumpToMark",
        motionArgs = MotionArgs(forward = true, linewise = true)
    ),
    MotionCommand(
        keys = "['",
        motion = "jumpToMark",
        motionArgs = MotionArgs(forward = false, linewise = true)
    ),
    // ]p and [p aren't motions but must come before more general motion declarations
    ActionCommand(
        keys = "]p",
        action = "paste",
        isEdit = true,
        actionArgs = ActionArgs(after = true, isEdit = true, matchIndent = true)
    ),
    ActionCommand(
        keys = "[p",
        action = "paste",
        isEdit = true,
        actionArgs = ActionArgs(after = false, isEdit = true, matchIndent = true)
    ),
    MotionCommand(
        keys = "]<character>",
        motion = "moveToSymbol",
        motionArgs = MotionArgs(forward = true, toJumplist = true)
    ),
    MotionCommand(
        keys = "[<character>",
        motion = "moveToSymbol",
        motionArgs = MotionArgs(forward = false, toJumplist = true)
    ),
    MotionCommand(keys = "|", motion = "moveToColumn"),
    MotionCommand(
        keys = "o",
        motion = "moveToOtherHighlightedEnd",
        context = "visual"
    ),
    MotionCommand(
        keys = "O",
        motion = "moveToOtherHighlightedEnd",
        motionArgs = MotionArgs(sameLine = true),
        context = "visual"
    ),

    // -----------------------------------------------------------------------
    // Operators
    // -----------------------------------------------------------------------
    OperatorCommand(keys = "d", operator = "delete"),
    OperatorCommand(keys = "y", operator = "yank"),
    OperatorCommand(keys = "c", operator = "change"),
    OperatorCommand(keys = "=", operator = "indentAuto"),
    OperatorCommand(
        keys = ">",
        operator = "indent",
        operatorArgs = OperatorArgs(indentRight = true)
    ),
    OperatorCommand(
        keys = "<",
        operator = "indent",
        operatorArgs = OperatorArgs(indentRight = false)
    ),
    OperatorCommand(keys = "g~", operator = "changeCase"),
    OperatorCommand(
        keys = "gu",
        operator = "changeCase",
        operatorArgs = OperatorArgs(toLower = true),
        isEdit = true
    ),
    OperatorCommand(
        keys = "gU",
        operator = "changeCase",
        operatorArgs = OperatorArgs(toLower = false),
        isEdit = true
    ),
    OperatorCommand(
        keys = "gc",
        operator = "toggleComment",
        isEdit = true
    ),
    MotionCommand(
        keys = "n",
        motion = "findNext",
        motionArgs = MotionArgs(forward = true, toJumplist = true)
    ),
    MotionCommand(
        keys = "N",
        motion = "findNext",
        motionArgs = MotionArgs(forward = false, toJumplist = true)
    ),
    MotionCommand(
        keys = "gn",
        motion = "findAndSelectNextInclusive",
        motionArgs = MotionArgs(forward = true)
    ),
    MotionCommand(
        keys = "gN",
        motion = "findAndSelectNextInclusive",
        motionArgs = MotionArgs(forward = false)
    ),
    OperatorCommand(keys = "gq", operator = "hardWrap"),
    OperatorCommand(
        keys = "gw",
        operator = "hardWrap",
        operatorArgs = OperatorArgs(keepCursor = true)
    ),
    OperatorCommand(keys = "g?", operator = "rot13"),

    // -----------------------------------------------------------------------
    // Operator-Motion dual commands
    // -----------------------------------------------------------------------
    OperatorMotionCommand(
        keys = "x",
        operator = "delete",
        motion = "moveByCharacters",
        motionArgs = MotionArgs(forward = true),
        operatorMotionArgs = OperatorMotionArgs(visualLine = false)
    ),
    OperatorMotionCommand(
        keys = "X",
        operator = "delete",
        motion = "moveByCharacters",
        motionArgs = MotionArgs(forward = false),
        operatorMotionArgs = OperatorMotionArgs(visualLine = true)
    ),
    OperatorMotionCommand(
        keys = "D",
        operator = "delete",
        motion = "moveToEol",
        motionArgs = MotionArgs(inclusive = true),
        context = "normal"
    ),
    OperatorCommand(
        keys = "D",
        operator = "delete",
        operatorArgs = OperatorArgs(linewise = true),
        context = "visual"
    ),
    OperatorMotionCommand(
        keys = "Y",
        operator = "yank",
        motion = "expandToLine",
        motionArgs = MotionArgs(linewise = true),
        context = "normal"
    ),
    OperatorCommand(
        keys = "Y",
        operator = "yank",
        operatorArgs = OperatorArgs(linewise = true),
        context = "visual"
    ),
    OperatorMotionCommand(
        keys = "C",
        operator = "change",
        motion = "moveToEol",
        motionArgs = MotionArgs(inclusive = true),
        context = "normal"
    ),
    OperatorCommand(
        keys = "C",
        operator = "change",
        operatorArgs = OperatorArgs(linewise = true),
        context = "visual"
    ),
    OperatorMotionCommand(
        keys = "~",
        operator = "changeCase",
        motion = "moveByCharacters",
        motionArgs = MotionArgs(forward = true),
        operatorArgs = OperatorArgs(shouldMoveCursor = true),
        context = "normal"
    ),
    OperatorCommand(
        keys = "~",
        operator = "changeCase",
        context = "visual"
    ),
    OperatorMotionCommand(
        keys = "<C-u>",
        operator = "delete",
        motion = "moveToStartOfLine",
        context = "insert"
    ),
    OperatorMotionCommand(
        keys = "<C-w>",
        operator = "delete",
        motion = "moveByWords",
        motionArgs = MotionArgs(forward = false, wordEnd = false),
        context = "insert"
    ),
    // Ignore C-w in normal mode
    IdleCommand(keys = "<C-w>", context = "normal"),

    // -----------------------------------------------------------------------
    // Actions
    // -----------------------------------------------------------------------
    ActionCommand(
        keys = "<C-i>",
        action = "jumpListWalk",
        actionArgs = ActionArgs(forward = true)
    ),
    ActionCommand(
        keys = "<C-o>",
        action = "jumpListWalk",
        actionArgs = ActionArgs(forward = false)
    ),
    ActionCommand(
        keys = "<C-e>",
        action = "scroll",
        actionArgs = ActionArgs(forward = true, linewise = true)
    ),
    ActionCommand(
        keys = "<C-y>",
        action = "scroll",
        actionArgs = ActionArgs(forward = false, linewise = true)
    ),
    ActionCommand(
        keys = "a",
        action = "enterInsertMode",
        isEdit = true,
        actionArgs = ActionArgs(insertAt = "charAfter"),
        context = "normal"
    ),
    ActionCommand(
        keys = "A",
        action = "enterInsertMode",
        isEdit = true,
        actionArgs = ActionArgs(insertAt = "eol"),
        context = "normal"
    ),
    ActionCommand(
        keys = "A",
        action = "enterInsertMode",
        isEdit = true,
        actionArgs = ActionArgs(insertAt = "endOfSelectedArea"),
        context = "visual"
    ),
    ActionCommand(
        keys = "i",
        action = "enterInsertMode",
        isEdit = true,
        actionArgs = ActionArgs(insertAt = "inplace"),
        context = "normal"
    ),
    ActionCommand(
        keys = "gi",
        action = "enterInsertMode",
        isEdit = true,
        actionArgs = ActionArgs(insertAt = "lastEdit"),
        context = "normal"
    ),
    ActionCommand(
        keys = "I",
        action = "enterInsertMode",
        isEdit = true,
        actionArgs = ActionArgs(insertAt = "firstNonBlank"),
        context = "normal"
    ),
    ActionCommand(
        keys = "gI",
        action = "enterInsertMode",
        isEdit = true,
        actionArgs = ActionArgs(insertAt = "bol"),
        context = "normal"
    ),
    ActionCommand(
        keys = "I",
        action = "enterInsertMode",
        isEdit = true,
        actionArgs = ActionArgs(insertAt = "startOfSelectedArea"),
        context = "visual"
    ),
    ActionCommand(
        keys = "o",
        action = "newLineAndEnterInsertMode",
        isEdit = true,
        interlaceInsertRepeat = true,
        actionArgs = ActionArgs(after = true),
        context = "normal"
    ),
    ActionCommand(
        keys = "O",
        action = "newLineAndEnterInsertMode",
        isEdit = true,
        interlaceInsertRepeat = true,
        actionArgs = ActionArgs(after = false),
        context = "normal"
    ),
    ActionCommand(keys = "v", action = "toggleVisualMode"),
    ActionCommand(
        keys = "V",
        action = "toggleVisualMode",
        actionArgs = ActionArgs(linewise = true)
    ),
    ActionCommand(
        keys = "<C-v>",
        action = "toggleVisualMode",
        actionArgs = ActionArgs(blockwise = true)
    ),
    ActionCommand(
        keys = "<C-q>",
        action = "toggleVisualMode",
        actionArgs = ActionArgs(blockwise = true)
    ),
    ActionCommand(keys = "gv", action = "reselectLastSelection"),
    ActionCommand(keys = "J", action = "joinLines", isEdit = true),
    ActionCommand(
        keys = "gJ",
        action = "joinLines",
        actionArgs = ActionArgs(keepSpaces = true),
        isEdit = true
    ),
    ActionCommand(
        keys = "p",
        action = "paste",
        isEdit = true,
        actionArgs = ActionArgs(after = true, isEdit = true)
    ),
    ActionCommand(
        keys = "P",
        action = "paste",
        isEdit = true,
        actionArgs = ActionArgs(after = false, isEdit = true)
    ),
    ActionCommand(keys = "r<character>", action = "replace", isEdit = true),
    ActionCommand(keys = "@<register>", action = "replayMacro"),
    ActionCommand(keys = "q<register>", action = "enterMacroRecordMode"),
    // Handle Replace-mode as a special case of insert mode.
    ActionCommand(
        keys = "R",
        action = "enterInsertMode",
        isEdit = true,
        actionArgs = ActionArgs(replace = true),
        context = "normal"
    ),
    OperatorCommand(
        keys = "R",
        operator = "change",
        operatorArgs = OperatorArgs(linewise = true, fullLine = true),
        context = "visual",
        isEdit = true
    ),
    ActionCommand(keys = "u", action = "undo", context = "normal"),
    OperatorCommand(
        keys = "u",
        operator = "changeCase",
        operatorArgs = OperatorArgs(toLower = true),
        context = "visual",
        isEdit = true
    ),
    OperatorCommand(
        keys = "U",
        operator = "changeCase",
        operatorArgs = OperatorArgs(toLower = false),
        context = "visual",
        isEdit = true
    ),
    ActionCommand(keys = "<C-r>", action = "redo"),
    ActionCommand(keys = "m<register>", action = "setMark"),
    ActionCommand(keys = "\"<register>", action = "setRegister"),
    ActionCommand(
        keys = "<C-r><register>",
        action = "insertRegister",
        context = "insert",
        isEdit = true
    ),
    ActionCommand(
        keys = "<C-o>",
        action = "oneNormalCommand",
        context = "insert"
    ),
    ActionCommand(
        keys = "zz",
        action = "scrollToCursor",
        actionArgs = ActionArgs(position = "center")
    ),
    ActionCommand(
        keys = "z.",
        action = "scrollToCursor",
        actionArgs = ActionArgs(position = "center"),
        motion = "moveToFirstNonWhiteSpaceCharacter"
    ),
    ActionCommand(
        keys = "zt",
        action = "scrollToCursor",
        actionArgs = ActionArgs(position = "top")
    ),
    ActionCommand(
        keys = "z<CR>",
        action = "scrollToCursor",
        actionArgs = ActionArgs(position = "top"),
        motion = "moveToFirstNonWhiteSpaceCharacter"
    ),
    ActionCommand(
        keys = "zb",
        action = "scrollToCursor",
        actionArgs = ActionArgs(position = "bottom")
    ),
    ActionCommand(
        keys = "z-",
        action = "scrollToCursor",
        actionArgs = ActionArgs(position = "bottom"),
        motion = "moveToFirstNonWhiteSpaceCharacter"
    ),
    ActionCommand(keys = ".", action = "repeatLastEdit"),
    ActionCommand(
        keys = "<C-a>",
        action = "incrementNumberToken",
        isEdit = true,
        actionArgs = ActionArgs(increase = true, backtrack = false)
    ),
    ActionCommand(
        keys = "<C-x>",
        action = "incrementNumberToken",
        isEdit = true,
        actionArgs = ActionArgs(increase = false, backtrack = false)
    ),
    ActionCommand(
        keys = "<C-t>",
        action = "indent",
        actionArgs = ActionArgs(indentRight = true),
        context = "insert"
    ),
    ActionCommand(
        keys = "<C-d>",
        action = "indent",
        actionArgs = ActionArgs(indentRight = false),
        context = "insert"
    ),

    // -----------------------------------------------------------------------
    // Text object motions
    // -----------------------------------------------------------------------
    MotionCommand(keys = "a<register>", motion = "textObjectManipulation"),
    MotionCommand(
        keys = "i<register>",
        motion = "textObjectManipulation",
        motionArgs = MotionArgs(textObjectInner = true)
    ),

    // -----------------------------------------------------------------------
    // Search
    // -----------------------------------------------------------------------
    SearchCommand(
        keys = "/",
        searchArgs = SearchArgs(
            forward = true,
            querySrc = "prompt",
            toJumplist = true
        )
    ),
    SearchCommand(
        keys = "?",
        searchArgs = SearchArgs(
            forward = false,
            querySrc = "prompt",
            toJumplist = true
        )
    ),
    SearchCommand(
        keys = "*",
        searchArgs = SearchArgs(
            forward = true,
            querySrc = "wordUnderCursor",
            wholeWordOnly = true,
            toJumplist = true
        )
    ),
    SearchCommand(
        keys = "#",
        searchArgs = SearchArgs(
            forward = false,
            querySrc = "wordUnderCursor",
            wholeWordOnly = true,
            toJumplist = true
        )
    ),
    SearchCommand(
        keys = "g*",
        searchArgs = SearchArgs(
            forward = true,
            querySrc = "wordUnderCursor",
            toJumplist = true
        )
    ),
    SearchCommand(
        keys = "g#",
        searchArgs = SearchArgs(
            forward = false,
            querySrc = "wordUnderCursor",
            toJumplist = true
        )
    ),

    // -----------------------------------------------------------------------
    // Ex command
    // -----------------------------------------------------------------------
    ExCommandMapping(keys = ":")
)

/**
 * The default Ex command map.
 *
 * This is the Kotlin port of the `defaultExCommandMap` array from the upstream
 * `@replit/codemirror-vim` package.
 */
val defaultExCommandMap: List<ExCommandDefinition> = listOf(
    ExCommandDefinition(name = "colorscheme", shortName = "colo"),
    ExCommandDefinition(name = "map"),
    ExCommandDefinition(name = "imap", shortName = "im"),
    ExCommandDefinition(name = "nmap", shortName = "nm"),
    ExCommandDefinition(name = "vmap", shortName = "vm"),
    ExCommandDefinition(name = "omap", shortName = "om"),
    ExCommandDefinition(name = "noremap", shortName = "no"),
    ExCommandDefinition(name = "nnoremap", shortName = "nn"),
    ExCommandDefinition(name = "vnoremap", shortName = "vn"),
    ExCommandDefinition(name = "inoremap", shortName = "ino"),
    ExCommandDefinition(name = "onoremap", shortName = "ono"),
    ExCommandDefinition(name = "unmap"),
    ExCommandDefinition(name = "mapclear", shortName = "mapc"),
    ExCommandDefinition(name = "nmapclear", shortName = "nmapc"),
    ExCommandDefinition(name = "vmapclear", shortName = "vmapc"),
    ExCommandDefinition(name = "imapclear", shortName = "imapc"),
    ExCommandDefinition(name = "omapclear", shortName = "omapc"),
    ExCommandDefinition(name = "write", shortName = "w"),
    ExCommandDefinition(name = "undo", shortName = "u"),
    ExCommandDefinition(name = "redo", shortName = "red"),
    ExCommandDefinition(name = "set", shortName = "se"),
    ExCommandDefinition(name = "setlocal", shortName = "setl"),
    ExCommandDefinition(name = "setglobal", shortName = "setg"),
    ExCommandDefinition(name = "sort", shortName = "sor"),
    ExCommandDefinition(name = "substitute", shortName = "s", possiblyAsync = true),
    ExCommandDefinition(name = "startinsert", shortName = "start"),
    ExCommandDefinition(name = "nohlsearch", shortName = "noh"),
    ExCommandDefinition(name = "yank", shortName = "y"),
    ExCommandDefinition(name = "put", shortName = "pu"),
    ExCommandDefinition(name = "delmarks", shortName = "delm"),
    ExCommandDefinition(name = "marks", excludeFromCommandHistory = true),
    ExCommandDefinition(
        name = "registers",
        shortName = "reg",
        excludeFromCommandHistory = true
    ),
    ExCommandDefinition(name = "vglobal", shortName = "v"),
    ExCommandDefinition(name = "delete", shortName = "d"),
    ExCommandDefinition(name = "join", shortName = "j"),
    ExCommandDefinition(name = "normal", shortName = "norm"),
    ExCommandDefinition(name = "global", shortName = "g")
)
