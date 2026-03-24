# Kodemirror Gap Analysis Report

Generated: 2026-03-24

## Summary

| Category | Pass | Fail | Timeout | Skip | Total |
|----------|------|------|---------|------|-------|
| Editing | 6 | 0 | 0 | 0 | 6 |
| Features | 4 | 0 | 0 | 0 | 4 |
| Navigation | 11 | 0 | 0 | 0 | 11 |
| Selection | 9 | 0 | 0 | 0 | 9 |
| Typing | 4 | 2 | 0 | 0 | 6 |
| Visual Comparison | 3 | 0 | 0 | 0 | 3 |
| **Total** | **37** | **2** | **0** | **0** | **39** |

## Failures

### Typing > special characters - quotes

- **Status:** fail
- **Duration:** 1577ms
- **Error:**
  ```
  Error: [2mexpect([22m[31mreceived[39m[2m).[22mtoBe[2m([22m[32mexpected[39m[2m) // Object.is equality[22m
  
  Expected: [32mfalse[39m
  Received: [31mtrue[39m
  
    82 |       const kmDoubleQuote =
    83 |         kmState.doc.split("\n").pop()?.includes('""') ?? false;
  > 84 |       expect(kmDoubleQuote).toBe(cm6DoubleQuote);
       |                             ^
    85 |     }
    86 |   });
    87 |
      at /home/jmonk/git/kodemirror/gap-analysis/tests/typing.spec.ts:84:29
  ```

### Typing > Enter key and auto-indent

- **Status:** fail
- **Duration:** 2695ms
- **Error:**
  ```
  Error: [2mexpect([22m[31mreceived[39m[2m).[22mtoBe[2m([22m[32mexpected[39m[2m) // Object.is equality[22m
  
  Expected: [32m2[39m
  Received: [31m0[39m
  
    104 |     if (km) {
    105 |       const kmState = await km.getState();
  > 106 |       expect(kmState.cursor.col).toBe(cm6State.cursor.col);
        |                                  ^
    107 |     }
    108 |   });
    109 |
      at /home/jmonk/git/kodemirror/gap-analysis/tests/typing.spec.ts:106:34
  ```

## Suggested TODO Items

Add these to `docs/TODO.md` for tracking and resolution:

### 1. Fix gap: Typing - special characters - quotes
- Gap analysis test `special characters - quotes` in category `Typing` failed.
- Error: Error: [2mexpect([22m[31mreceived[39m[2m).[22mtoBe[2m([22m[32mexpected[39m[2m) // Object.is equality[22m

### 2. Fix gap: Typing - Enter key and auto-indent
- Gap analysis test `Enter key and auto-indent` in category `Typing` failed.
- Error: Error: [2mexpect([22m[31mreceived[39m[2m).[22mtoBe[2m([22m[32mexpected[39m[2m) // Object.is equality[22m

## Passing Tests

- [x] Editing > Backspace (1224ms)
- [x] Editing > Delete key (1043ms)
- [x] Editing > Ctrl+Backspace - delete word backward (1057ms)
- [x] Editing > Ctrl+Z - undo (1791ms)
- [x] Editing > Ctrl+Y - redo (1798ms)
- [x] Editing > bracket auto-delete (1618ms)
- [x] Features > Ctrl+F opens search (1614ms)
- [x] Features > initial document matches (978ms)
- [x] Features > initial cursor position (1005ms)
- [x] Features > bracket matching - cursor next to bracket (1373ms)
- [x] Navigation > arrow keys - right (1049ms)
- [x] Navigation > arrow keys - left (1007ms)
- [x] Navigation > arrow keys - down (1030ms)
- [x] Navigation > arrow keys - up (1497ms)
- [x] Navigation > Home key (1084ms)
- [x] Navigation > End key (1063ms)
- [x] Navigation > Ctrl+Home - go to document start (1499ms)
- [x] Navigation > Ctrl+End - go to document end (1049ms)
- [x] Navigation > Ctrl+Right - word movement forward (1049ms)
- [x] Navigation > Ctrl+Left - word movement backward (1033ms)
- [x] Navigation > column memory across lines (1076ms)
- [x] Selection > Shift+Right - extend selection right (1020ms)
- [x] Selection > Shift+Left - extend selection left (1079ms)
- [x] Selection > Shift+Down - extend selection down (1042ms)
- [x] Selection > Ctrl+Shift+Right - select word right (1040ms)
- [x] Selection > Ctrl+Shift+Left - select word left (1023ms)
- [x] Selection > Shift+Home - select to line start (1230ms)
- [x] Selection > Shift+End - select to line end (1042ms)
- [x] Selection > Ctrl+A - select all (1010ms)
- [x] Selection > typing replaces selection (1223ms)
- [x] Typing > single character (1637ms)
- [x] Typing > word input (1646ms)
- [x] Typing > special characters - brackets (1574ms)
- [x] Typing > Tab key (2058ms)
- [x] Visual Comparison > initial render - side by side (969ms)
- [x] Visual Comparison > after typing - visual state (2677ms)
- [x] Visual Comparison > with selection - visual state (1067ms)
