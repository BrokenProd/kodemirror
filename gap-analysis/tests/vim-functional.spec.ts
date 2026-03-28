import { test, expect, VimStateSnapshot } from "../drivers/vim-test-context";

test.describe("Vim Functional Parity", () => {
  test.describe("Basic Motions", () => {
    test("hjkl navigation", async ({ cm6, km }) => {
      // Move down 2 lines with j
      await cm6.press("j");
      await cm6.press("j");
      if (km) {
        await km.press("j");
        await km.press("j");
      }

      const cm6State = await cm6.getState();
      expect(cm6State.cursor.line).toBe(3);

      if (km) {
        const kmState = await km.getState();
        expect(kmState.cursor.line).toBe(cm6State.cursor.line);
      }

      // Move right with l
      await cm6.press("l");
      await cm6.press("l");
      if (km) {
        await km.press("l");
        await km.press("l");
      }

      const cm6After = await cm6.getState();
      if (km) {
        const kmAfter = await km.getState();
        expect(kmAfter.cursor.col).toBe(cm6After.cursor.col);
      }

      // Move up with k
      await cm6.press("k");
      if (km) await km.press("k");

      const cm6Up = await cm6.getState();
      if (km) {
        const kmUp = await km.getState();
        expect(kmUp.cursor.line).toBe(cm6Up.cursor.line);
      }

      // Move left with h
      await cm6.press("h");
      if (km) await km.press("h");

      const cm6Left = await cm6.getState();
      if (km) {
        const kmLeft = await km.getState();
        expect(kmLeft.cursor.col).toBe(cm6Left.cursor.col);
      }
    });

    test("w/b word navigation", async ({ cm6, km }) => {
      // Move to next word with w
      await cm6.press("w");
      if (km) await km.press("w");

      const cm6W = await cm6.getState();
      if (km) {
        const kmW = await km.getState();
        expect(kmW.cursor.pos).toBe(cm6W.cursor.pos);
      }

      // Move forward more
      await cm6.press("w");
      await cm6.press("w");
      if (km) {
        await km.press("w");
        await km.press("w");
      }

      const cm6W3 = await cm6.getState();
      if (km) {
        const kmW3 = await km.getState();
        expect(kmW3.cursor.pos).toBe(cm6W3.cursor.pos);
      }

      // Move back with b
      await cm6.press("b");
      if (km) await km.press("b");

      const cm6B = await cm6.getState();
      if (km) {
        const kmB = await km.getState();
        expect(kmB.cursor.pos).toBe(cm6B.cursor.pos);
      }
    });

    test("0/$ line start/end", async ({ cm6, km }) => {
      // Move to second line first
      await cm6.press("j");
      if (km) await km.press("j");

      // Move to end of line with $
      await cm6.press("Shift+4"); // $
      if (km) await km.press("Shift+4");

      const cm6End = await cm6.getState();
      if (km) {
        const kmEnd = await km.getState();
        expect(kmEnd.cursor.col).toBe(cm6End.cursor.col);
      }

      // Move to start of line with 0
      await cm6.press("0");
      if (km) await km.press("0");

      const cm6Start = await cm6.getState();
      expect(cm6Start.cursor.col).toBe(0);
      if (km) {
        const kmStart = await km.getState();
        expect(kmStart.cursor.col).toBe(0);
      }
    });

    test("gg/G first/last line", async ({ cm6, km }) => {
      // Go to last line with G
      await cm6.press("Shift+g");
      if (km) await km.press("Shift+g");

      const cm6Last = await cm6.getState();
      expect(cm6Last.cursor.line).toBe(cm6Last.docInfo.lines);
      if (km) {
        const kmLast = await km.getState();
        expect(kmLast.cursor.line).toBe(cm6Last.cursor.line);
      }

      // Go to first line with gg
      await cm6.press("g");
      await cm6.press("g");
      if (km) {
        await km.press("g");
        await km.press("g");
      }

      const cm6First = await cm6.getState();
      expect(cm6First.cursor.line).toBe(1);
      if (km) {
        const kmFirst = await km.getState();
        expect(kmFirst.cursor.line).toBe(1);
      }
    });
  });

  test.describe("Operators", () => {
    test("dd deletes line", async ({ cm6, km }) => {
      // Move to line 2
      await cm6.press("j");
      if (km) await km.press("j");

      const cm6Before = await cm6.getState();
      const linesBefore = cm6Before.docInfo.lines;

      // Delete line with dd
      await cm6.press("d");
      await cm6.press("d");
      if (km) {
        await km.press("d");
        await km.press("d");
      }

      const cm6After = await cm6.getState();
      expect(cm6After.docInfo.lines).toBe(linesBefore - 1);

      if (km) {
        const kmAfter = await km.getState();
        expect(kmAfter.docInfo.lines).toBe(cm6After.docInfo.lines);
        expect(kmAfter.doc).toBe(cm6After.doc);
      }
    });

    test("yy/p yank and paste", async ({ cm6, km }) => {
      // Yank first line
      await cm6.press("y");
      await cm6.press("y");
      if (km) {
        await km.press("y");
        await km.press("y");
      }

      // Paste below
      await cm6.press("p");
      if (km) await km.press("p");

      const cm6After = await cm6.getState();
      if (km) {
        const kmAfter = await km.getState();
        expect(kmAfter.doc).toBe(cm6After.doc);
        expect(kmAfter.cursor.line).toBe(cm6After.cursor.line);
      }
    });

    test("cc changes line", async ({ cm6, km }) => {
      // Move to line 2
      await cm6.press("j");
      if (km) await km.press("j");

      // Change line with cc (enters insert mode)
      await cm6.press("c");
      await cm6.press("c");
      if (km) {
        await km.press("c");
        await km.press("c");
      }

      // Should be in insert mode
      const cm6Vim = await cm6.getVimState();
      expect(cm6Vim.vimMode).toBe("insert");

      if (km) {
        const kmVim = await km.getVimState();
        expect(kmVim.vimMode).toBe("insert");
      }

      // Exit insert mode
      await cm6.press("Escape");
      if (km) await km.press("Escape");
    });

    test("dw deletes word", async ({ cm6, km }) => {
      const cm6Before = await cm6.getDoc();

      // Delete word with dw
      await cm6.press("d");
      await cm6.press("w");
      if (km) {
        await km.press("d");
        await km.press("w");
      }

      const cm6After = await cm6.getDoc();
      expect(cm6After.length).toBeLessThan(cm6Before.length);

      if (km) {
        const kmAfter = await km.getDoc();
        expect(kmAfter).toBe(cm6After);
      }
    });
  });

  test.describe("Insert Mode", () => {
    test("i enters insert mode before cursor", async ({ cm6, km }) => {
      await cm6.press("i");
      if (km) await km.press("i");

      const cm6Vim = await cm6.getVimState();
      expect(cm6Vim.vimMode).toBe("insert");

      if (km) {
        const kmVim = await km.getVimState();
        expect(kmVim.vimMode).toBe("insert");
      }

      // Escape back to normal
      await cm6.press("Escape");
      if (km) await km.press("Escape");
    });

    test("a enters insert mode after cursor", async ({ cm6, km }) => {
      // Move to a known position first
      await cm6.press("l");
      if (km) await km.press("l");

      const cm6Before = await cm6.getState();

      await cm6.press("a");
      if (km) await km.press("a");

      const cm6After = await cm6.getState();
      // Cursor should move one right when entering insert with 'a'
      expect(cm6After.cursor.pos).toBe(cm6Before.cursor.pos + 1);

      if (km) {
        const kmAfter = await km.getState();
        expect(kmAfter.cursor.pos).toBe(cm6After.cursor.pos);
      }

      await cm6.press("Escape");
      if (km) await km.press("Escape");
    });

    test("o opens line below", async ({ cm6, km }) => {
      const cm6Before = await cm6.getState();

      await cm6.press("o");
      if (km) await km.press("o");

      const cm6After = await cm6.getState();
      expect(cm6After.docInfo.lines).toBe(cm6Before.docInfo.lines + 1);
      expect(cm6After.cursor.line).toBe(cm6Before.cursor.line + 1);

      if (km) {
        const kmAfter = await km.getState();
        expect(kmAfter.docInfo.lines).toBe(cm6After.docInfo.lines);
        expect(kmAfter.cursor.line).toBe(cm6After.cursor.line);
      }

      await cm6.press("Escape");
      if (km) await km.press("Escape");
    });

    test("O opens line above", async ({ cm6, km }) => {
      // Move down first so we're not at line 1
      await cm6.press("j");
      await cm6.press("j");
      if (km) {
        await km.press("j");
        await km.press("j");
      }

      const cm6Before = await cm6.getState();

      await cm6.press("Shift+o");
      if (km) await km.press("Shift+o");

      const cm6After = await cm6.getState();
      expect(cm6After.docInfo.lines).toBe(cm6Before.docInfo.lines + 1);
      // Cursor line stays the same (new line inserted above)
      expect(cm6After.cursor.line).toBe(cm6Before.cursor.line);

      if (km) {
        const kmAfter = await km.getState();
        expect(kmAfter.docInfo.lines).toBe(cm6After.docInfo.lines);
        expect(kmAfter.cursor.line).toBe(cm6After.cursor.line);
      }

      await cm6.press("Escape");
      if (km) await km.press("Escape");
    });
  });

  test.describe("Visual Mode", () => {
    test("v enters character visual mode", async ({ cm6, km }) => {
      await cm6.press("v");
      if (km) await km.press("v");

      const cm6Vim = await cm6.getVimState();
      expect(cm6Vim.vimMode).toBe("visual");

      if (km) {
        const kmVim = await km.getVimState();
        expect(kmVim.vimMode).toBe("visual");
      }

      // Select some text
      await cm6.press("l");
      await cm6.press("l");
      await cm6.press("l");
      if (km) {
        await km.press("l");
        await km.press("l");
        await km.press("l");
      }

      const cm6Sel = await cm6.getSelection();
      expect(cm6Sel.empty).toBe(false);

      if (km) {
        const kmSel = await km.getSelection();
        expect(kmSel.empty).toBe(false);
        // Selection ranges should match
        expect(kmSel.ranges[0].from).toBe(cm6Sel.ranges[0].from);
        expect(kmSel.ranges[0].to).toBe(cm6Sel.ranges[0].to);
      }

      // Exit visual mode
      await cm6.press("Escape");
      if (km) await km.press("Escape");
    });

    test("V enters line visual mode", async ({ cm6, km }) => {
      await cm6.press("Shift+v");
      if (km) await km.press("Shift+v");

      const cm6Vim = await cm6.getVimState();
      expect(cm6Vim.vimMode).toBe("visual-line");

      if (km) {
        const kmVim = await km.getVimState();
        expect(kmVim.vimMode).toBe("visual-line");
      }

      await cm6.press("Escape");
      if (km) await km.press("Escape");
    });
  });

  test.describe("Search", () => {
    test("/ searches forward", async ({ cm6, km }) => {
      // Type / to start search
      await cm6.press("/");
      if (km) await km.press("/");

      // Type search pattern
      await cm6.type("fibonacci");
      if (km) await km.type("fibonacci");

      // Press Enter to execute search
      await cm6.press("Enter");
      if (km) await km.press("Enter");

      const cm6After = await cm6.getState();
      if (km) {
        const kmAfter = await km.getState();
        expect(kmAfter.cursor.pos).toBe(cm6After.cursor.pos);
      }
    });

    test("n finds next match", async ({ cm6, km }) => {
      // Search for something first
      await cm6.press("/");
      if (km) await km.press("/");
      await cm6.type("n");
      if (km) await km.type("n");
      await cm6.press("Enter");
      if (km) await km.press("Enter");

      const cm6First = await cm6.getState();

      // Press n to find next
      await cm6.press("n");
      if (km) await km.press("n");

      const cm6Next = await cm6.getState();
      // Should have moved to a different position
      expect(cm6Next.cursor.pos).not.toBe(cm6First.cursor.pos);

      if (km) {
        const kmNext = await km.getState();
        expect(kmNext.cursor.pos).toBe(cm6Next.cursor.pos);
      }
    });
  });

  test.describe("Undo/Redo", () => {
    test("u undoes last change", async ({ cm6, km }) => {
      const cm6Before = await cm6.getDoc();

      // Make a change: delete a line
      await cm6.press("d");
      await cm6.press("d");
      if (km) {
        await km.press("d");
        await km.press("d");
      }

      const cm6Changed = await cm6.getDoc();
      expect(cm6Changed).not.toBe(cm6Before);

      // Undo with u
      await cm6.press("u");
      if (km) await km.press("u");

      const cm6Undone = await cm6.getDoc();
      expect(cm6Undone).toBe(cm6Before);

      if (km) {
        const kmUndone = await km.getDoc();
        expect(kmUndone).toBe(cm6Before);
      }
    });

    test("Ctrl+r redoes", async ({ cm6, km }) => {
      const cm6Before = await cm6.getDoc();

      // Change, undo, then redo
      await cm6.press("d");
      await cm6.press("d");
      if (km) {
        await km.press("d");
        await km.press("d");
      }

      const cm6Changed = await cm6.getDoc();

      await cm6.press("u");
      if (km) await km.press("u");

      // Redo
      await cm6.press("Control+r");
      if (km) await km.press("Control+r");

      const cm6Redone = await cm6.getDoc();
      expect(cm6Redone).toBe(cm6Changed);

      if (km) {
        const kmRedone = await km.getDoc();
        expect(kmRedone).toBe(cm6Changed);
      }
    });
  });

  test.describe("Ex Commands", () => {
    test(":s/old/new/ substitution", async ({ cm6, km }) => {
      // Open ex command line
      await cm6.press("Shift+;"); // :
      if (km) await km.press("Shift+;");

      // Type substitution command
      await cm6.type("s/Fibonacci/FIBONACCI/");
      if (km) await km.type("s/Fibonacci/FIBONACCI/");

      await cm6.press("Enter");
      if (km) await km.press("Enter");

      const cm6After = await cm6.getDoc();
      expect(cm6After).toContain("FIBONACCI");

      if (km) {
        const kmAfter = await km.getDoc();
        expect(kmAfter).toContain("FIBONACCI");
        expect(kmAfter).toBe(cm6After);
      }
    });
  });
});
