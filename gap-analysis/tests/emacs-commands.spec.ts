import { test, expect } from "../drivers/test-context";

/**
 * Gap analysis tests for standard editor commands.
 *
 * These tests run the same key sequence on both CM6 and KodeMirror,
 * then verify the resulting document text and cursor position match.
 * A failing KM assertion points to a real behavioral gap.
 *
 * Note: Emacs bindings (Ctrl-a/e/f/b/p/n/d/h/k/t) may or may not be
 * active depending on the editor configuration. These tests compare
 * CM6 vs KM behavior — if neither handles the key, both should be
 * unchanged and the test still passes.
 */

test.describe("Standard Editor Commands", () => {
  test.beforeEach(async ({ cm6, km }) => {
    await cm6.focus();
    if (km) await km.focus();
  });

  test("Ctrl-Backspace deletes word backward", async ({ cm6, km }) => {
    // Move to end of first line
    await cm6.press("End");
    if (km) await km.press("End");

    const cm6Before = await cm6.getState();

    await cm6.press("Control+Backspace");
    if (km) await km.press("Control+Backspace");

    const cm6After = await cm6.getState();
    // Verify CM6 actually did something
    expect(cm6After.cursor.pos).toBeLessThan(cm6Before.cursor.pos);

    if (km) {
      const kmAfter = await km.getState();
      expect(kmAfter.doc).toBe(cm6After.doc);
      expect(kmAfter.cursor.pos).toBe(cm6After.cursor.pos);
    }
  });

  test("Ctrl-Backspace on typed text", async ({ cm6, km }) => {
    // Go to end, add a new line, type known text
    await cm6.press("Control+End");
    if (km) await km.press("Control+End");
    await cm6.press("Enter");
    if (km) await km.press("Enter");
    await cm6.type("hello world");
    if (km) await km.type("hello world");

    await cm6.press("Control+Backspace");
    if (km) await km.press("Control+Backspace");

    const cm6After = await cm6.getDoc();
    if (km) {
      const kmAfter = await km.getDoc();
      expect(kmAfter).toBe(cm6After);
    }
  });

  test("Ctrl-Delete deletes word forward", async ({ cm6, km }) => {
    await cm6.press("Home");
    if (km) await km.press("Home");

    const cm6Before = await cm6.getState();

    await cm6.press("Control+Delete");
    if (km) await km.press("Control+Delete");

    const cm6After = await cm6.getState();

    if (km) {
      const kmAfter = await km.getState();
      expect(kmAfter.doc).toBe(cm6After.doc);
      expect(kmAfter.cursor.pos).toBe(cm6After.cursor.pos);
    }
  });

  test("Ctrl-z undoes last change", async ({ cm6, km }) => {
    // Type something first
    await cm6.press("End");
    if (km) await km.press("End");
    await cm6.type("X");
    if (km) await km.type("X");

    const cm6WithX = await cm6.getDoc();
    expect(cm6WithX).toContain("X");

    // Undo
    await cm6.press("Control+z");
    if (km) await km.press("Control+z");

    const cm6After = await cm6.getDoc();
    if (km) {
      const kmAfter = await km.getDoc();
      expect(kmAfter).toBe(cm6After);
    }
  });

  test("Ctrl-Shift-z redoes after undo", async ({ cm6, km }) => {
    await cm6.press("End");
    if (km) await km.press("End");
    await cm6.type("Y");
    if (km) await km.type("Y");

    await cm6.press("Control+z");
    if (km) await km.press("Control+z");

    await cm6.press("Control+Shift+z");
    if (km) await km.press("Control+Shift+z");

    const cm6After = await cm6.getDoc();
    if (km) {
      const kmAfter = await km.getDoc();
      expect(kmAfter).toBe(cm6After);
    }
  });

  test("Ctrl-Backspace multiple times", async ({ cm6, km }) => {
    await cm6.press("Control+End");
    if (km) await km.press("Control+End");
    await cm6.press("Enter");
    if (km) await km.press("Enter");
    await cm6.type("one two three");
    if (km) await km.type("one two three");

    // Delete 3 words backward
    await cm6.press("Control+Backspace");
    if (km) await km.press("Control+Backspace");
    await cm6.press("Control+Backspace");
    if (km) await km.press("Control+Backspace");
    await cm6.press("Control+Backspace");
    if (km) await km.press("Control+Backspace");

    const cm6After = await cm6.getDoc();
    if (km) {
      const kmAfter = await km.getDoc();
      expect(kmAfter).toBe(cm6After);
    }
  });
});

test.describe("Emacs Keybindings (if active)", () => {
  test.beforeEach(async ({ cm6, km }) => {
    await cm6.focus();
    if (km) await km.focus();
  });

  // These tests compare CM6 vs KM behavior.
  // If the emacs binding isn't active on either, both should be unchanged.

  test("Ctrl-a / Ctrl-e line navigation", async ({ cm6, km }) => {
    // Move somewhere in the middle of a line
    await cm6.press("End");
    if (km) await km.press("End");
    await cm6.press("ArrowLeft");
    await cm6.press("ArrowLeft");
    if (km) {
      await km.press("ArrowLeft");
      await km.press("ArrowLeft");
    }

    // Ctrl-a should go to line start
    await cm6.press("Control+a");
    if (km) await km.press("Control+a");

    const cm6After = await cm6.getCursor();
    if (km) {
      const kmAfter = await km.getCursor();
      expect(kmAfter.col).toBe(cm6After.col);
    }

    // Ctrl-e should go to line end
    await cm6.press("Control+e");
    if (km) await km.press("Control+e");

    const cm6End = await cm6.getCursor();
    if (km) {
      const kmEnd = await km.getCursor();
      expect(kmEnd.col).toBe(cm6End.col);
    }
  });

  test("Ctrl-d deletes character forward", async ({ cm6, km }) => {
    await cm6.press("Home");
    if (km) await km.press("Home");

    const cm6Before = await cm6.getDoc();

    await cm6.press("Control+d");
    if (km) await km.press("Control+d");

    const cm6After = await cm6.getDoc();
    if (km) {
      const kmAfter = await km.getDoc();
      expect(kmAfter).toBe(cm6After);
    }
  });

  test("Ctrl-k kills to end of line", async ({ cm6, km }) => {
    await cm6.press("Home");
    if (km) await km.press("Home");

    await cm6.press("Control+k");
    if (km) await km.press("Control+k");

    const cm6After = await cm6.getDoc();
    if (km) {
      const kmAfter = await km.getDoc();
      expect(kmAfter).toBe(cm6After);
    }
  });

  test("Ctrl-t transposes characters", async ({ cm6, km }) => {
    // Position cursor after second character on line 1
    await cm6.press("Home");
    await cm6.press("ArrowRight");
    await cm6.press("ArrowRight");
    if (km) {
      await km.press("Home");
      await km.press("ArrowRight");
      await km.press("ArrowRight");
    }

    await cm6.press("Control+t");
    if (km) await km.press("Control+t");

    const cm6After = await cm6.getDoc();
    if (km) {
      const kmAfter = await km.getDoc();
      expect(kmAfter).toBe(cm6After);
    }
  });
});
