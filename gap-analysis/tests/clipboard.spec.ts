import { test, expect } from "../drivers/test-context";
import { EditorDriver } from "../drivers/types";

/**
 * Helper: select text from current cursor by holding Shift+Arrow.
 * Returns the selected text from the driver's state.
 */
async function selectChars(
  driver: EditorDriver,
  count: number
): Promise<string> {
  for (let i = 0; i < count; i++) {
    await driver.press("Shift+ArrowRight");
  }
  const state = await driver.getState();
  const { from, to } = state.selection.ranges[0];
  return state.doc.slice(from, to);
}

test.describe("Clipboard", () => {
  test.beforeEach(async ({ cm6, km }) => {
    await cm6.focus();
    if (km) await km.focus();
  });

  test("copy and paste single-line text", async ({ cm6, km }) => {
    // Go to start, select first 8 characters
    await cm6.press("Control+Home");
    if (km) await km.press("Control+Home");

    const cm6Selected = await selectChars(cm6, 8);
    if (km) await selectChars(km, 8);

    // Copy
    await cm6.press("Control+c");
    if (km) await km.press("Control+c");

    // Move to end of document
    await cm6.press("Control+End");
    if (km) await km.press("Control+End");

    // Paste
    await cm6.press("Control+v");
    if (km) await km.press("Control+v");

    // Verify: pasted text appears exactly once at the end
    const cm6Doc = await cm6.getDoc();
    expect(cm6Doc).toContain(cm6Selected);
    // The pasted text should be at the very end
    expect(cm6Doc.endsWith(cm6Selected)).toBe(true);

    if (km) {
      const kmDoc = await km.getDoc();
      expect(kmDoc.endsWith(cm6Selected)).toBe(true);
    }
  });

  test("paste does not duplicate text", async ({ cm6, km }) => {
    // This test specifically catches the double-paste bug.
    // Go to start, select a word
    await cm6.press("Control+Home");
    if (km) await km.press("Control+Home");

    await cm6.press("Control+Shift+ArrowRight");
    if (km) await km.press("Control+Shift+ArrowRight");

    const cm6Sel = await cm6.getSelection();
    const cm6Before = await cm6.getState();
    const selectedText = cm6Before.doc.slice(
      cm6Sel.ranges[0].from,
      cm6Sel.ranges[0].to
    );

    // Copy
    await cm6.press("Control+c");
    if (km) await km.press("Control+c");

    // Move to end, add a newline as a marker
    await cm6.press("Control+End");
    if (km) await km.press("Control+End");
    await cm6.press("Enter");
    if (km) await km.press("Enter");

    // Paste
    await cm6.press("Control+v");
    if (km) await km.press("Control+v");

    // Count occurrences of the pasted text on the last line
    if (km) {
      const kmDoc = await km.getDoc();
      const lastLine = kmDoc.split("\n").pop() ?? "";
      // The pasted text should appear exactly once on the last line
      const count = lastLine.split(selectedText).length - 1;
      expect(count).toBe(1);
    }
  });

  test("cut removes text and paste restores it", async ({ cm6, km }) => {
    await cm6.press("Control+Home");
    if (km) await km.press("Control+Home");

    // Select first word
    await cm6.press("Control+Shift+ArrowRight");
    if (km) await km.press("Control+Shift+ArrowRight");

    const cm6Before = await cm6.getState();
    const cm6SelRange = cm6Before.selection.ranges[0];
    const cutText = cm6Before.doc.slice(cm6SelRange.from, cm6SelRange.to);
    const cm6BeforeLen = cm6Before.docInfo.length;

    if (km) var kmBefore = await km.getState();

    // Cut
    await cm6.press("Control+x");
    if (km) await km.press("Control+x");

    // Verify cut removed text
    const cm6AfterCut = await cm6.getState();
    expect(cm6AfterCut.docInfo.length).toBe(cm6BeforeLen - cutText.length);

    if (km) {
      const kmAfterCut = await km.getState();
      expect(kmAfterCut.docInfo.length).toBe(
        kmBefore!.docInfo.length - cutText.length
      );
    }

    // Move to end and paste
    await cm6.press("Control+End");
    if (km) await km.press("Control+End");
    await cm6.press("Control+v");
    if (km) await km.press("Control+v");

    // Verify paste added the cut text
    const cm6AfterPaste = await cm6.getDoc();
    expect(cm6AfterPaste.endsWith(cutText)).toBe(true);

    if (km) {
      const kmAfterPaste = await km.getDoc();
      expect(kmAfterPaste.endsWith(cutText)).toBe(true);
    }
  });

  test("paste multi-line text preserves newlines", async ({ cm6, km }) => {
    // Select the first 3 lines
    await cm6.press("Control+Home");
    if (km) await km.press("Control+Home");

    for (let i = 0; i < 3; i++) {
      await cm6.press("Shift+ArrowDown");
      if (km) await km.press("Shift+ArrowDown");
    }

    const cm6Sel = await cm6.getState();
    const selectedText = cm6Sel.doc.slice(
      cm6Sel.selection.ranges[0].from,
      cm6Sel.selection.ranges[0].to
    );
    const expectedNewlines = (selectedText.match(/\n/g) || []).length;
    expect(expectedNewlines).toBeGreaterThanOrEqual(2);

    // Copy
    await cm6.press("Control+c");
    if (km) await km.press("Control+c");

    // Move to end, add marker
    await cm6.press("Control+End");
    if (km) await km.press("Control+End");
    await cm6.press("Enter");
    if (km) await km.press("Enter");

    const cm6BeforePaste = await cm6.getState();
    if (km) var kmBeforePaste = await km.getState();

    // Paste
    await cm6.press("Control+v");
    if (km) await km.press("Control+v");

    // Verify the pasted content has the same number of newlines
    const cm6After = await cm6.getState();
    const cm6NewLines =
      cm6After.docInfo.lines - cm6BeforePaste.docInfo.lines;
    expect(cm6NewLines).toBe(expectedNewlines);

    if (km) {
      const kmAfter = await km.getState();
      const kmNewLines =
        kmAfter.docInfo.lines - kmBeforePaste!.docInfo.lines;
      expect(kmNewLines).toBe(expectedNewlines);
    }
  });

  test("paste replaces selection", async ({ cm6, km }) => {
    // Select the first word from the beginning of the document, copy it,
    // then select a different word and paste over it.
    await cm6.press("Control+Home");
    if (km) await km.press("Control+Home");

    // Select the first word and copy
    await cm6.press("Control+Shift+ArrowRight");
    if (km) await km.press("Control+Shift+ArrowRight");

    const cm6Sel = await cm6.getSelection();
    const cm6State = await cm6.getState();
    const copiedText = cm6State.doc.slice(
      cm6Sel.ranges[0].from,
      cm6Sel.ranges[0].to
    );

    await cm6.press("Control+c");
    if (km) await km.press("Control+c");

    // Move to the second word and select it
    await cm6.press("ArrowRight"); // deselect, move past space
    if (km) await km.press("ArrowRight");
    await cm6.press("Control+Shift+ArrowRight");
    if (km) await km.press("Control+Shift+ArrowRight");

    const cm6BeforePaste = await cm6.getState();
    const cm6SelectedWord = cm6BeforePaste.doc.slice(
      cm6BeforePaste.selection.ranges[0].from,
      cm6BeforePaste.selection.ranges[0].to
    );

    // Paste over the selection — the second word should be replaced
    await cm6.press("Control+v");
    if (km) await km.press("Control+v");

    // Verify: the copied text now appears at least twice (original + pasted)
    const cm6After = await cm6.getDoc();
    const cm6Count = (cm6After.match(new RegExp(copiedText, "g")) || []).length;
    expect(cm6Count).toBeGreaterThanOrEqual(2);
    // The replaced word should no longer appear at its original position
    expect(cm6After).not.toContain(
      copiedText + " " + cm6SelectedWord + " "
    );

    if (km) {
      const kmAfter = await km.getDoc();
      const kmCount = (kmAfter.match(new RegExp(copiedText, "g")) || []).length;
      expect(kmCount).toBe(cm6Count);
    }
  });
});
