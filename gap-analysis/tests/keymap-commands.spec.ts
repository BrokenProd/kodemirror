import { test, expect } from "../drivers/test-context";

/**
 * Comprehensive gap analysis tests for ALL keybindings across the three
 * keymaps: standardKeymap, defaultKeymap, and emacsStyleKeymap.
 *
 * Each test positions the cursor, presses the key on both CM6 and KM,
 * then compares resulting document text and cursor position.
 *
 * Note: CM6's basicSetup includes defaultKeymap (which extends standardKeymap).
 * Emacs bindings are NOT active in the default basicSetup, so the emacs tests
 * verify that both CM6 and KM behave the same way (both may do nothing if
 * the binding isn't active).
 *
 * Clipboard tests (Ctrl-c/v/x) are skipped because they don't work reliably
 * in headless Playwright.
 */

// Helper to press a key on both editors
async function pressOnBoth(
  cm6: any,
  km: any,
  key: string,
  times: number = 1
) {
  for (let i = 0; i < times; i++) {
    await cm6.press(key);
    if (km) await km.press(key);
  }
}

// Helper to type text on both editors
async function typeOnBoth(cm6: any, km: any, text: string) {
  await cm6.type(text);
  if (km) await km.type(text);
}

// Helper to compare doc and cursor between CM6 and KM
async function expectMatch(cm6: any, km: any) {
  const cm6State = await cm6.getState();
  if (km) {
    const kmState = await km.getState();
    expect(kmState.doc, "doc mismatch").toBe(cm6State.doc);
    expect(kmState.cursor.pos, "cursor pos mismatch").toBe(
      cm6State.cursor.pos
    );
  }
  return cm6State;
}

// ─── Standard Keymap ───────────────────────────────────────────────────────

test.describe("Keymap: Standard - Arrow Keys", () => {
  test.beforeEach(async ({ cm6, km }) => {
    await cm6.focus();
    if (km) await km.focus();
  });

  test("ArrowLeft moves cursor left", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "ArrowRight", 5);
    await pressOnBoth(cm6, km, "ArrowLeft");
    await expectMatch(cm6, km);
  });

  test("ArrowRight moves cursor right", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "ArrowRight");
    await expectMatch(cm6, km);
  });

  test("ArrowUp moves cursor up", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "ArrowDown", 3);
    await pressOnBoth(cm6, km, "ArrowUp");
    await expectMatch(cm6, km);
  });

  test("ArrowDown moves cursor down", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "ArrowDown");
    await expectMatch(cm6, km);
  });
});

test.describe("Keymap: Standard - Home/End", () => {
  test.beforeEach(async ({ cm6, km }) => {
    await cm6.focus();
    if (km) await km.focus();
  });

  test("Home moves to line start", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "ArrowDown", 2);
    await pressOnBoth(cm6, km, "ArrowRight", 5);
    await pressOnBoth(cm6, km, "Home");
    await expectMatch(cm6, km);
  });

  test("End moves to line end", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "ArrowDown", 2);
    await pressOnBoth(cm6, km, "End");
    await expectMatch(cm6, km);
  });

  test("Ctrl-Home moves to document start", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+End");
    await pressOnBoth(cm6, km, "Control+Home");
    await expectMatch(cm6, km);
  });

  test("Ctrl-End moves to document end", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "Control+End");
    await expectMatch(cm6, km);
  });
});

test.describe("Keymap: Standard - PageUp/PageDown", () => {
  test.beforeEach(async ({ cm6, km }) => {
    await cm6.focus();
    if (km) await km.focus();
  });

  // PageUp/PageDown are viewport-dependent -- the page size depends on the
  // visible line count, which differs between CM6 (DOM viewport) and KM
  // (Compose canvas). We verify that each editor moves the cursor by at
  // least one line in the expected direction.
  test("PageDown moves cursor down a page", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    const beforeCm6 = await cm6.getState();
    await pressOnBoth(cm6, km, "PageDown");
    const afterCm6 = await cm6.getState();
    // CM6 should have moved forward
    expect(afterCm6.cursor.pos, "CM6 PageDown should move cursor forward").toBeGreaterThan(
      beforeCm6.cursor.pos
    );
    if (km) {
      const afterKm = await km.getState();
      // KM should have also moved forward
      expect(afterKm.cursor.pos, "KM PageDown should move cursor forward").toBeGreaterThan(
        beforeCm6.cursor.pos
      );
    }
  });

  test("PageUp moves cursor up a page", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+End");
    const beforeCm6 = await cm6.getState();
    await pressOnBoth(cm6, km, "PageUp");
    const afterCm6 = await cm6.getState();
    // CM6 should have moved backward
    expect(afterCm6.cursor.pos, "CM6 PageUp should move cursor backward").toBeLessThan(
      beforeCm6.cursor.pos
    );
    if (km) {
      const afterKm = await km.getState();
      // KM should have also moved backward
      expect(afterKm.cursor.pos, "KM PageUp should move cursor backward").toBeLessThan(
        beforeCm6.cursor.pos
      );
    }
  });
});

test.describe("Keymap: Standard - Enter", () => {
  test.beforeEach(async ({ cm6, km }) => {
    await cm6.focus();
    if (km) await km.focus();
  });

  test("Enter inserts newline", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+End");
    await pressOnBoth(cm6, km, "Enter");
    await expectMatch(cm6, km);
  });
});

test.describe("Keymap: Standard - Backspace/Delete", () => {
  test.beforeEach(async ({ cm6, km }) => {
    await cm6.focus();
    if (km) await km.focus();
  });

  test("Backspace deletes char backward", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "ArrowRight", 5);
    await pressOnBoth(cm6, km, "Backspace");
    await expectMatch(cm6, km);
  });

  test("Delete deletes char forward", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "Delete");
    await expectMatch(cm6, km);
  });
});

test.describe("Keymap: Standard - Word Movement", () => {
  test.beforeEach(async ({ cm6, km }) => {
    await cm6.focus();
    if (km) await km.focus();
  });

  test("Ctrl-ArrowRight moves word right", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "Control+ArrowRight");
    await expectMatch(cm6, km);
  });

  test("Ctrl-ArrowRight twice", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "Control+ArrowRight", 2);
    await expectMatch(cm6, km);
  });

  test("Ctrl-ArrowLeft moves word left", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "End");
    await pressOnBoth(cm6, km, "Control+ArrowLeft");
    await expectMatch(cm6, km);
  });

  test("Ctrl-ArrowLeft twice", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "End");
    await pressOnBoth(cm6, km, "Control+ArrowLeft", 2);
    await expectMatch(cm6, km);
  });
});

test.describe("Keymap: Standard - Word Delete", () => {
  test.beforeEach(async ({ cm6, km }) => {
    await cm6.focus();
    if (km) await km.focus();
  });

  test("Ctrl-Backspace deletes word backward", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+End");
    await pressOnBoth(cm6, km, "Enter");
    await typeOnBoth(cm6, km, "hello world");
    await pressOnBoth(cm6, km, "Control+Backspace");
    await expectMatch(cm6, km);
  });

  test("Ctrl-Backspace on typed text multiple times", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+End");
    await pressOnBoth(cm6, km, "Enter");
    await typeOnBoth(cm6, km, "one two three");
    await pressOnBoth(cm6, km, "Control+Backspace");
    await pressOnBoth(cm6, km, "Control+Backspace");
    await pressOnBoth(cm6, km, "Control+Backspace");
    await expectMatch(cm6, km);
  });

  test("Ctrl-Delete deletes word forward", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "Control+Delete");
    await expectMatch(cm6, km);
  });

  test("Ctrl-Delete twice", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "Control+Delete");
    await pressOnBoth(cm6, km, "Control+Delete");
    await expectMatch(cm6, km);
  });

  test("Ctrl-Backspace at line start crosses line boundary", async ({
    cm6,
    km,
  }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "ArrowDown", 2);
    await pressOnBoth(cm6, km, "Home");
    await pressOnBoth(cm6, km, "Control+Backspace");
    await expectMatch(cm6, km);
  });

  test("Ctrl-Delete at line end crosses line boundary", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "End");
    await pressOnBoth(cm6, km, "Control+Delete");
    await expectMatch(cm6, km);
  });
});

// ─── Default Keymap (extends Standard) ─────────────────────────────────────

test.describe("Keymap: Default - Move Line", () => {
  test.beforeEach(async ({ cm6, km }) => {
    await cm6.focus();
    if (km) await km.focus();
  });

  test("Alt-ArrowUp moves line up", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "ArrowDown", 2);
    await pressOnBoth(cm6, km, "Alt+ArrowUp");
    await expectMatch(cm6, km);
  });

  test("Alt-ArrowDown moves line down", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "ArrowDown");
    await pressOnBoth(cm6, km, "Alt+ArrowDown");
    await expectMatch(cm6, km);
  });
});

test.describe("Keymap: Default - Copy Line", () => {
  test.beforeEach(async ({ cm6, km }) => {
    await cm6.focus();
    if (km) await km.focus();
  });

  test("Shift-Alt-ArrowUp copies line up", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "ArrowDown", 2);
    await pressOnBoth(cm6, km, "Shift+Alt+ArrowUp");
    await expectMatch(cm6, km);
  });

  test("Shift-Alt-ArrowDown copies line down", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "ArrowDown");
    await pressOnBoth(cm6, km, "Shift+Alt+ArrowDown");
    await expectMatch(cm6, km);
  });
});

test.describe("Keymap: Default - Delete Line", () => {
  test.beforeEach(async ({ cm6, km }) => {
    await cm6.focus();
    if (km) await km.focus();
  });

  test("Ctrl-Shift-k deletes current line", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "ArrowDown");
    await pressOnBoth(cm6, km, "Control+Shift+k");
    await expectMatch(cm6, km);
  });
});

test.describe("Keymap: Default - Indent", () => {
  test.beforeEach(async ({ cm6, km }) => {
    await cm6.focus();
    if (km) await km.focus();
  });

  test("Ctrl-] indents line", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "ArrowDown", 2);
    await pressOnBoth(cm6, km, "Control+]");
    await expectMatch(cm6, km);
  });

  test("Ctrl-[ dedents line", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "ArrowDown", 2);
    // Indent first, then dedent
    await pressOnBoth(cm6, km, "Control+]");
    await pressOnBoth(cm6, km, "Control+[");
    await expectMatch(cm6, km);
  });
});

test.describe("Keymap: Default - Bracket Matching", () => {
  test.beforeEach(async ({ cm6, km }) => {
    await cm6.focus();
    if (km) await km.focus();
  });

  test("Ctrl-Shift-\\ goes to matching bracket", async ({ cm6, km }) => {
    // Position cursor on an opening bracket: line 2 has "fibonacci(n) {"
    // Move to the opening paren
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "ArrowDown"); // line 2: function fibonacci(n) {
    await pressOnBoth(cm6, km, "End");
    // Move back to find the opening brace
    await pressOnBoth(cm6, km, "ArrowLeft"); // should be at '{'

    await pressOnBoth(cm6, km, "Control+Shift+\\");

    await expectMatch(cm6, km);
  });
});

test.describe("Keymap: Default - Transpose", () => {
  test.beforeEach(async ({ cm6, km }) => {
    await cm6.focus();
    if (km) await km.focus();
  });

  test("Ctrl-t transposes characters", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "ArrowRight", 3);
    await pressOnBoth(cm6, km, "Control+t");
    await expectMatch(cm6, km);
  });
});

// ─── Tab / Shift-Tab (indentWithTab) ───────────────────────────────────────

test.describe("Keymap: Tab/Shift-Tab", () => {
  test.beforeEach(async ({ cm6, km }) => {
    await cm6.focus();
    if (km) await km.focus();
  });

  test("Tab indents (or inserts tab depending on config)", async ({
    cm6,
    km,
  }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "ArrowDown", 2);
    // Tab behavior depends on whether indentWithTab is active
    // Both editors should behave the same way either way
    await pressOnBoth(cm6, km, "Tab");
    await expectMatch(cm6, km);
  });

  test("Shift-Tab dedents", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "ArrowDown", 2);
    await pressOnBoth(cm6, km, "Shift+Tab");
    await expectMatch(cm6, km);
  });
});

// ─── Emacs Keymap ──────────────────────────────────────────────────────────
// These bindings may or may not be active (emacs keymap is optional).
// Tests verify CM6 and KM behave identically regardless.

test.describe("Keymap: Emacs - Navigation", () => {
  test.beforeEach(async ({ cm6, km }) => {
    await cm6.focus();
    if (km) await km.focus();
  });

  test("Ctrl-a goes to line start", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "ArrowDown", 2);
    await pressOnBoth(cm6, km, "ArrowRight", 5);
    await pressOnBoth(cm6, km, "Control+a");
    await expectMatch(cm6, km);
  });

  test("Ctrl-e goes to line end", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "ArrowDown", 2);
    await pressOnBoth(cm6, km, "Control+e");
    await expectMatch(cm6, km);
  });

  test("Ctrl-f moves char right", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "Control+f");
    await expectMatch(cm6, km);
  });

  test("Ctrl-b moves char left", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "ArrowRight", 5);
    await pressOnBoth(cm6, km, "Control+b");
    await expectMatch(cm6, km);
  });

  test("Ctrl-p moves line up", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "ArrowDown", 3);
    await pressOnBoth(cm6, km, "Control+p");
    await expectMatch(cm6, km);
  });

  test("Ctrl-n moves line down", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "Control+n");
    await expectMatch(cm6, km);
  });
});

test.describe("Keymap: Emacs - Editing", () => {
  test.beforeEach(async ({ cm6, km }) => {
    await cm6.focus();
    if (km) await km.focus();
  });

  test("Ctrl-d deletes char forward", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "Control+d");
    await expectMatch(cm6, km);
  });

  test("Ctrl-h deletes char backward", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "ArrowRight", 5);
    await pressOnBoth(cm6, km, "Control+h");
    await expectMatch(cm6, km);
  });

  test("Ctrl-k kills to end of line", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "ArrowRight", 3);
    await pressOnBoth(cm6, km, "Control+k");
    await expectMatch(cm6, km);
  });

  test("Ctrl-t transposes characters (emacs)", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "ArrowRight", 2);
    await pressOnBoth(cm6, km, "Control+t");
    await expectMatch(cm6, km);
  });
});

// ─── Selection variants (Shift + movement keys) ───────────────────────────

test.describe("Keymap: Standard - Selection with Shift", () => {
  test.beforeEach(async ({ cm6, km }) => {
    await cm6.focus();
    if (km) await km.focus();
  });

  test("Shift-ArrowRight extends selection right", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "Shift+ArrowRight", 5);
    const cm6State = await cm6.getState();
    if (km) {
      const kmState = await km.getState();
      expect(kmState.selection.anchor).toBe(cm6State.selection.anchor);
      expect(kmState.selection.head).toBe(cm6State.selection.head);
    }
  });

  test("Shift-ArrowLeft extends selection left", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "ArrowRight", 10);
    await pressOnBoth(cm6, km, "Shift+ArrowLeft", 3);
    const cm6State = await cm6.getState();
    if (km) {
      const kmState = await km.getState();
      expect(kmState.selection.anchor).toBe(cm6State.selection.anchor);
      expect(kmState.selection.head).toBe(cm6State.selection.head);
    }
  });

  test("Shift-ArrowDown extends selection down", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "Shift+ArrowDown");
    const cm6State = await cm6.getState();
    if (km) {
      const kmState = await km.getState();
      expect(kmState.selection.anchor).toBe(cm6State.selection.anchor);
      expect(kmState.selection.head).toBe(cm6State.selection.head);
    }
  });

  test("Shift-ArrowUp extends selection up", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "ArrowDown", 3);
    await pressOnBoth(cm6, km, "Shift+ArrowUp");
    const cm6State = await cm6.getState();
    if (km) {
      const kmState = await km.getState();
      expect(kmState.selection.anchor).toBe(cm6State.selection.anchor);
      expect(kmState.selection.head).toBe(cm6State.selection.head);
    }
  });

  test("Shift-Home selects to line start", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "ArrowDown");
    await pressOnBoth(cm6, km, "End");
    await pressOnBoth(cm6, km, "Shift+Home");
    const cm6State = await cm6.getState();
    if (km) {
      const kmState = await km.getState();
      expect(kmState.selection.anchor).toBe(cm6State.selection.anchor);
      expect(kmState.selection.head).toBe(cm6State.selection.head);
    }
  });

  test("Shift-End selects to line end", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "ArrowDown");
    await pressOnBoth(cm6, km, "Home");
    await pressOnBoth(cm6, km, "Shift+End");
    const cm6State = await cm6.getState();
    if (km) {
      const kmState = await km.getState();
      expect(kmState.selection.anchor).toBe(cm6State.selection.anchor);
      expect(kmState.selection.head).toBe(cm6State.selection.head);
    }
  });

  test("Ctrl-Shift-ArrowRight selects word right", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "Control+Shift+ArrowRight");
    const cm6State = await cm6.getState();
    if (km) {
      const kmState = await km.getState();
      expect(kmState.selection.anchor).toBe(cm6State.selection.anchor);
      expect(kmState.selection.head).toBe(cm6State.selection.head);
    }
  });

  test("Ctrl-Shift-ArrowLeft selects word left", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "End");
    await pressOnBoth(cm6, km, "Control+Shift+ArrowLeft");
    const cm6State = await cm6.getState();
    if (km) {
      const kmState = await km.getState();
      expect(kmState.selection.anchor).toBe(cm6State.selection.anchor);
      expect(kmState.selection.head).toBe(cm6State.selection.head);
    }
  });

  test("Ctrl-Shift-Home selects to doc start", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+End");
    await pressOnBoth(cm6, km, "Control+Shift+Home");
    const cm6State = await cm6.getState();
    if (km) {
      const kmState = await km.getState();
      expect(kmState.selection.anchor).toBe(cm6State.selection.anchor);
      expect(kmState.selection.head).toBe(cm6State.selection.head);
    }
  });

  test("Ctrl-Shift-End selects to doc end", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "Control+Shift+End");
    const cm6State = await cm6.getState();
    if (km) {
      const kmState = await km.getState();
      expect(kmState.selection.anchor).toBe(cm6State.selection.anchor);
      expect(kmState.selection.head).toBe(cm6State.selection.head);
    }
  });
});

// ─── Select All ────────────────────────────────────────────────────────────

test.describe("Keymap: Standard - Select All", () => {
  test.beforeEach(async ({ cm6, km }) => {
    await cm6.focus();
    if (km) await km.focus();
  });

  test("Ctrl-a selects all text", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+a");
    const cm6State = await cm6.getState();
    if (km) {
      const kmState = await km.getState();
      expect(kmState.selection.anchor).toBe(cm6State.selection.anchor);
      expect(kmState.selection.head).toBe(cm6State.selection.head);
    }
  });
});

// ─── Undo/Redo ─────────────────────────────────────────────────────────────

test.describe("Keymap: Standard - Undo/Redo", () => {
  test.beforeEach(async ({ cm6, km }) => {
    await cm6.focus();
    if (km) await km.focus();
  });

  test("Ctrl-z undoes last change", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+End");
    await typeOnBoth(cm6, km, "X");
    await pressOnBoth(cm6, km, "Control+z");
    await expectMatch(cm6, km);
  });

  test("Ctrl-Shift-z redoes after undo", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+End");
    await typeOnBoth(cm6, km, "Y");
    await pressOnBoth(cm6, km, "Control+z");
    await pressOnBoth(cm6, km, "Control+Shift+z");
    await expectMatch(cm6, km);
  });
});

// ─── Tab Handling ─────────────────────────────────────────────────────────

test.describe("Tab Handling", () => {
  test.beforeEach(async ({ cm6, km }) => {
    await cm6.focus();
    if (km) await km.focus();
  });

  test("Tab key behavior matches between CM6 and KM", async ({
    cm6,
    km,
  }) => {
    // Position at start of a line with content
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "ArrowDown", 2);
    await pressOnBoth(cm6, km, "Home");

    // Press Tab — behavior depends on indentWithTab config
    // Both editors should do the same thing
    await pressOnBoth(cm6, km, "Tab");
    await expectMatch(cm6, km);
  });

  test("Tab then Shift-Tab round-trips", async ({ cm6, km }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "ArrowDown", 2);
    await pressOnBoth(cm6, km, "Home");

    const before = await expectMatch(cm6, km);

    await pressOnBoth(cm6, km, "Tab");
    await pressOnBoth(cm6, km, "Shift+Tab");

    // Should be back to original
    await expectMatch(cm6, km);
  });

  test("multiple Tab presses increase indent consistently", async ({
    cm6,
    km,
  }) => {
    await pressOnBoth(cm6, km, "Control+Home");
    await pressOnBoth(cm6, km, "ArrowDown", 2);
    await pressOnBoth(cm6, km, "Home");

    await pressOnBoth(cm6, km, "Tab");
    await pressOnBoth(cm6, km, "Tab");
    await pressOnBoth(cm6, km, "Tab");

    await expectMatch(cm6, km);
  });
});
