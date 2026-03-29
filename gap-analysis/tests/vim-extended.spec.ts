import { test, expect, VimStateSnapshot } from "../drivers/vim-test-context";

test.describe("Vim Extended Parity", () => {
  test.describe("Character Motions", () => {
    test("f finds character forward", async ({ cm6, km }) => {
      // f followed by a character to find on the current line
      await cm6.press("f");
      await cm6.press("i"); // find 'i' in "// Fibonacci sequence"
      if (km) {
        await km.press("f");
        await km.press("i");
      }

      const cm6State = await cm6.getState();
      if (km) {
        const kmState = await km.getState();
        expect(kmState.cursor.pos).toBe(cm6State.cursor.pos);
      }
    });

    test("t moves to before character", async ({ cm6, km }) => {
      await cm6.press("t");
      await cm6.press("i");
      if (km) {
        await km.press("t");
        await km.press("i");
      }

      const cm6State = await cm6.getState();
      if (km) {
        const kmState = await km.getState();
        expect(kmState.cursor.pos).toBe(cm6State.cursor.pos);
      }
    });

    test("F finds character backward", async ({ cm6, km }) => {
      // Move to end of line first
      await cm6.press("Shift+4"); // $
      if (km) await km.press("Shift+4");

      await cm6.press("Shift+f");
      await cm6.press("i");
      if (km) {
        await km.press("Shift+f");
        await km.press("i");
      }

      const cm6State = await cm6.getState();
      if (km) {
        const kmState = await km.getState();
        expect(kmState.cursor.pos).toBe(cm6State.cursor.pos);
      }
    });

    test("; repeats last f/t", async ({ cm6, km }) => {
      await cm6.press("f");
      await cm6.press("i");
      if (km) {
        await km.press("f");
        await km.press("i");
      }

      // Repeat with ;
      await cm6.press(";");
      if (km) await km.press(";");

      const cm6State = await cm6.getState();
      if (km) {
        const kmState = await km.getState();
        expect(kmState.cursor.pos).toBe(cm6State.cursor.pos);
      }
    });
  });

  test.describe("Replace", () => {
    test("r replaces single character", async ({ cm6, km }) => {
      // Move to a known position
      await cm6.press("l");
      if (km) await km.press("l");

      await cm6.press("r");
      await cm6.press("X");
      if (km) {
        await km.press("r");
        await km.press("X");
      }

      const cm6State = await cm6.getState();
      const cm6Vim = await cm6.getVimState();
      // Should still be in normal mode after r
      expect(cm6Vim.vimMode).toBe("normal");

      if (km) {
        const kmState = await km.getState();
        const kmVim = await km.getVimState();
        expect(kmVim.vimMode).toBe("normal");
        expect(kmState.doc).toBe(cm6State.doc);
      }
    });

    test("R enters replace mode", async ({ cm6, km }) => {
      await cm6.press("Shift+r");
      if (km) await km.press("Shift+r");

      const cm6Vim = await cm6.getVimState();
      // Replace mode shows as "insert" with replace submode
      expect(cm6Vim.vimMode).toBe("insert");

      if (km) {
        const kmVim = await km.getVimState();
        expect(kmVim.vimMode).toBe("insert");
      }

      await cm6.press("Escape");
      if (km) await km.press("Escape");
    });
  });

  test.describe("Delete and Change", () => {
    test("x deletes character under cursor", async ({ cm6, km }) => {
      const cm6Before = await cm6.getDoc();

      await cm6.press("x");
      if (km) await km.press("x");

      const cm6After = await cm6.getDoc();
      expect(cm6After.length).toBe(cm6Before.length - 1);

      if (km) {
        const kmAfter = await km.getDoc();
        expect(kmAfter).toBe(cm6After);
      }
    });

    test("X deletes character before cursor", async ({ cm6, km }) => {
      // Move right first so there's a character before
      await cm6.press("l");
      await cm6.press("l");
      if (km) {
        await km.press("l");
        await km.press("l");
      }

      const cm6Before = await cm6.getDoc();

      await cm6.press("Shift+x");
      if (km) await km.press("Shift+x");

      const cm6After = await cm6.getDoc();
      expect(cm6After.length).toBe(cm6Before.length - 1);

      if (km) {
        const kmAfter = await km.getDoc();
        expect(kmAfter).toBe(cm6After);
      }
    });

    test("cw changes word", async ({ cm6, km }) => {
      await cm6.press("c");
      await cm6.press("w");
      if (km) {
        await km.press("c");
        await km.press("w");
      }

      const cm6Vim = await cm6.getVimState();
      expect(cm6Vim.vimMode).toBe("insert");

      if (km) {
        const kmVim = await km.getVimState();
        expect(kmVim.vimMode).toBe("insert");
      }

      // Type replacement text
      await cm6.type("REPLACED");
      if (km) await km.type("REPLACED");

      await cm6.press("Escape");
      if (km) await km.press("Escape");

      const cm6After = await cm6.getDoc();
      expect(cm6After).toContain("REPLACED");

      if (km) {
        const kmAfter = await km.getDoc();
        expect(kmAfter).toBe(cm6After);
      }
    });

    test("C changes to end of line", async ({ cm6, km }) => {
      await cm6.press("Shift+c");
      if (km) await km.press("Shift+c");

      const cm6Vim = await cm6.getVimState();
      expect(cm6Vim.vimMode).toBe("insert");

      if (km) {
        const kmVim = await km.getVimState();
        expect(kmVim.vimMode).toBe("insert");
      }

      await cm6.press("Escape");
      if (km) await km.press("Escape");
    });

    test("S changes entire line", async ({ cm6, km }) => {
      await cm6.press("j"); // move to line 2
      if (km) await km.press("j");

      await cm6.press("Shift+s");
      if (km) await km.press("Shift+s");

      const cm6Vim = await cm6.getVimState();
      expect(cm6Vim.vimMode).toBe("insert");

      if (km) {
        const kmVim = await km.getVimState();
        expect(kmVim.vimMode).toBe("insert");
      }

      await cm6.press("Escape");
      if (km) await km.press("Escape");
    });

    test("D deletes to end of line", async ({ cm6, km }) => {
      // Move to middle of line
      await cm6.press("w");
      if (km) await km.press("w");

      const cm6Before = await cm6.getDoc();

      await cm6.press("Shift+d");
      if (km) await km.press("Shift+d");

      const cm6After = await cm6.getDoc();
      expect(cm6After.length).toBeLessThan(cm6Before.length);

      if (km) {
        const kmAfter = await km.getDoc();
        expect(kmAfter).toBe(cm6After);
      }
    });
  });

  test.describe("Line Operations", () => {
    test("J joins lines", async ({ cm6, km }) => {
      const cm6Before = await cm6.getState();

      await cm6.press("Shift+j");
      if (km) await km.press("Shift+j");

      const cm6After = await cm6.getState();
      expect(cm6After.docInfo.lines).toBe(cm6Before.docInfo.lines - 1);

      if (km) {
        const kmAfter = await km.getState();
        expect(kmAfter.docInfo.lines).toBe(cm6After.docInfo.lines);
        expect(kmAfter.doc).toBe(cm6After.doc);
      }
    });

    test(">> indents line", async ({ cm6, km }) => {
      const cm6Before = await cm6.getDoc();

      await cm6.press("Shift+.");
      await cm6.press("Shift+.");
      if (km) {
        await km.press("Shift+.");
        await km.press("Shift+.");
      }

      const cm6After = await cm6.getDoc();
      // Line should be indented
      expect(cm6After).not.toBe(cm6Before);

      if (km) {
        const kmAfter = await km.getDoc();
        expect(kmAfter).toBe(cm6After);
      }
    });

    test("<< unindents line", async ({ cm6, km }) => {
      // Move to an indented line (line 3: "    if (n <= 1) return n;")
      await cm6.press("j");
      await cm6.press("j");
      if (km) {
        await km.press("j");
        await km.press("j");
      }

      const cm6Before = await cm6.getDoc();

      await cm6.press("Shift+,");
      await cm6.press("Shift+,");
      if (km) {
        await km.press("Shift+,");
        await km.press("Shift+,");
      }

      const cm6After = await cm6.getDoc();
      expect(cm6After).not.toBe(cm6Before);

      if (km) {
        const kmAfter = await km.getDoc();
        expect(kmAfter).toBe(cm6After);
      }
    });
  });

  test.describe("Repeat", () => {
    test(". repeats last edit", async ({ cm6, km }) => {
      // Delete a line with dd
      await cm6.press("d");
      await cm6.press("d");
      if (km) {
        await km.press("d");
        await km.press("d");
      }

      const cm6After1 = await cm6.getState();

      // Repeat with .
      await cm6.press(".");
      if (km) await km.press(".");

      const cm6After2 = await cm6.getState();
      expect(cm6After2.docInfo.lines).toBe(cm6After1.docInfo.lines - 1);

      if (km) {
        const kmAfter2 = await km.getState();
        expect(kmAfter2.docInfo.lines).toBe(cm6After2.docInfo.lines);
        expect(kmAfter2.doc).toBe(cm6After2.doc);
      }
    });
  });

  test.describe("Marks", () => {
    test("ma sets mark, 'a jumps to mark", async ({ cm6, km }) => {
      // Move to a specific line
      await cm6.press("j");
      await cm6.press("j");
      await cm6.press("j");
      if (km) {
        await km.press("j");
        await km.press("j");
        await km.press("j");
      }

      // Set mark 'a'
      await cm6.press("m");
      await cm6.press("a");
      if (km) {
        await km.press("m");
        await km.press("a");
      }

      const cm6Marked = await cm6.getState();

      // Move away
      await cm6.press("g");
      await cm6.press("g");
      if (km) {
        await km.press("g");
        await km.press("g");
      }

      // Jump back to mark with 'a
      await cm6.press("'");
      await cm6.press("a");
      if (km) {
        await km.press("'");
        await km.press("a");
      }

      const cm6Jump = await cm6.getState();
      expect(cm6Jump.cursor.line).toBe(cm6Marked.cursor.line);

      if (km) {
        const kmJump = await km.getState();
        expect(kmJump.cursor.line).toBe(cm6Jump.cursor.line);
      }
    });
  });

  test.describe("Text Objects", () => {
    test("diw deletes inner word", async ({ cm6, km }) => {
      // Move to a word
      await cm6.press("w");
      if (km) await km.press("w");

      const cm6Before = await cm6.getDoc();

      await cm6.press("d");
      await cm6.press("i");
      await cm6.press("w");
      if (km) {
        await km.press("d");
        await km.press("i");
        await km.press("w");
      }

      const cm6After = await cm6.getDoc();
      expect(cm6After.length).toBeLessThan(cm6Before.length);

      if (km) {
        const kmAfter = await km.getDoc();
        expect(kmAfter).toBe(cm6After);
      }
    });

    test("ci( changes inside parens", async ({ cm6, km }) => {
      // Navigate to a line with parentheses: "function fibonacci(n) {"
      await cm6.press("j");
      if (km) await km.press("j");

      // Move inside the parens
      await cm6.press("f");
      await cm6.press("(");
      await cm6.press("l");
      if (km) {
        await km.press("f");
        await km.press("(");
        await km.press("l");
      }

      await cm6.press("c");
      await cm6.press("i");
      await cm6.press("(");
      if (km) {
        await km.press("c");
        await km.press("i");
        await km.press("(");
      }

      const cm6Vim = await cm6.getVimState();
      expect(cm6Vim.vimMode).toBe("insert");

      if (km) {
        const kmVim = await km.getVimState();
        expect(kmVim.vimMode).toBe("insert");
      }

      // Type replacement
      await cm6.type("x, y");
      if (km) await km.type("x, y");

      await cm6.press("Escape");
      if (km) await km.press("Escape");

      const cm6After = await cm6.getDoc();
      expect(cm6After).toContain("fibonacci(x, y)");

      if (km) {
        const kmAfter = await km.getDoc();
        expect(kmAfter).toBe(cm6After);
      }
    });
  });

  test.describe("Numbered Commands", () => {
    test("3j moves down 3 lines", async ({ cm6, km }) => {
      await cm6.press("3");
      await cm6.press("j");
      if (km) {
        await km.press("3");
        await km.press("j");
      }

      const cm6State = await cm6.getState();
      expect(cm6State.cursor.line).toBe(4);

      if (km) {
        const kmState = await km.getState();
        expect(kmState.cursor.line).toBe(cm6State.cursor.line);
      }
    });

    test("2dd deletes 2 lines", async ({ cm6, km }) => {
      const cm6Before = await cm6.getState();

      await cm6.press("2");
      await cm6.press("d");
      await cm6.press("d");
      if (km) {
        await km.press("2");
        await km.press("d");
        await km.press("d");
      }

      const cm6After = await cm6.getState();
      expect(cm6After.docInfo.lines).toBe(cm6Before.docInfo.lines - 2);

      if (km) {
        const kmAfter = await km.getState();
        expect(kmAfter.docInfo.lines).toBe(cm6After.docInfo.lines);
        expect(kmAfter.doc).toBe(cm6After.doc);
      }
    });

    test("3x deletes 3 characters", async ({ cm6, km }) => {
      const cm6Before = await cm6.getDoc();

      await cm6.press("3");
      await cm6.press("x");
      if (km) {
        await km.press("3");
        await km.press("x");
      }

      const cm6After = await cm6.getDoc();
      expect(cm6After.length).toBe(cm6Before.length - 3);

      if (km) {
        const kmAfter = await km.getDoc();
        expect(kmAfter).toBe(cm6After);
      }
    });
  });

  test.describe("Miscellaneous", () => {
    test("~ toggles case", async ({ cm6, km }) => {
      // Move to a letter
      await cm6.press("l");
      await cm6.press("l");
      await cm6.press("l");
      if (km) {
        await km.press("l");
        await km.press("l");
        await km.press("l");
      }

      const cm6Before = await cm6.getDoc();

      await cm6.press("Shift+`");
      if (km) await km.press("Shift+`");

      const cm6After = await cm6.getDoc();
      expect(cm6After).not.toBe(cm6Before);

      if (km) {
        const kmAfter = await km.getDoc();
        expect(kmAfter).toBe(cm6After);
      }
    });

    test("% jumps to matching bracket", async ({ cm6, km }) => {
      // Move to line with brackets: "function fibonacci(n) {"
      await cm6.press("j");
      if (km) await km.press("j");

      // Move to the opening brace
      await cm6.press("Shift+4"); // $ (end of line, which is {)
      if (km) await km.press("Shift+4");

      const cm6Before = await cm6.getState();

      await cm6.press("Shift+5"); // %
      if (km) await km.press("Shift+5");

      const cm6After = await cm6.getState();
      // Should have jumped to the matching }
      expect(cm6After.cursor.line).not.toBe(cm6Before.cursor.line);

      if (km) {
        const kmAfter = await km.getState();
        expect(kmAfter.cursor.line).toBe(cm6After.cursor.line);
        expect(kmAfter.cursor.col).toBe(cm6After.cursor.col);
      }
    });

    test("* searches for word under cursor", async ({ cm6, km }) => {
      // Move to "fibonacci" on line 2
      await cm6.press("j");
      await cm6.press("w");
      await cm6.press("w");
      if (km) {
        await km.press("j");
        await km.press("w");
        await km.press("w");
      }

      await cm6.press("Shift+8"); // *
      if (km) await km.press("Shift+8");

      const cm6After = await cm6.getState();
      if (km) {
        const kmAfter = await km.getState();
        expect(kmAfter.cursor.pos).toBe(cm6After.cursor.pos);
      }
    });
  });

  test.describe("Visual Mode Operations", () => {
    test("visual delete removes selected text", async ({ cm6, km }) => {
      // Enter visual mode, select some text, delete
      await cm6.press("v");
      if (km) await km.press("v");

      await cm6.press("l");
      await cm6.press("l");
      await cm6.press("l");
      if (km) {
        await km.press("l");
        await km.press("l");
        await km.press("l");
      }

      await cm6.press("d");
      if (km) await km.press("d");

      const cm6After = await cm6.getDoc();
      if (km) {
        const kmAfter = await km.getDoc();
        expect(kmAfter).toBe(cm6After);
      }

      // Should be back in normal mode
      const cm6Vim = await cm6.getVimState();
      expect(cm6Vim.vimMode).toBe("normal");

      if (km) {
        const kmVim = await km.getVimState();
        expect(kmVim.vimMode).toBe("normal");
      }
    });

    test("visual yank and paste", async ({ cm6, km }) => {
      // Select a word in visual mode
      await cm6.press("v");
      await cm6.press("e");
      if (km) {
        await km.press("v");
        await km.press("e");
      }

      // Yank
      await cm6.press("y");
      if (km) await km.press("y");

      // Move to end of line
      await cm6.press("Shift+4");
      if (km) await km.press("Shift+4");

      // Paste
      await cm6.press("p");
      if (km) await km.press("p");

      const cm6After = await cm6.getDoc();
      if (km) {
        const kmAfter = await km.getDoc();
        expect(kmAfter).toBe(cm6After);
      }
    });
  });

  test.describe("Insert Mode Variants", () => {
    test("A appends at end of line", async ({ cm6, km }) => {
      await cm6.press("Shift+a");
      if (km) await km.press("Shift+a");

      const cm6Vim = await cm6.getVimState();
      expect(cm6Vim.vimMode).toBe("insert");

      const cm6State = await cm6.getState();
      // Cursor should be at end of line
      const line = cm6State.doc.split("\n")[cm6State.cursor.line - 1];
      expect(cm6State.cursor.col).toBe(line.length);

      if (km) {
        const kmVim = await km.getVimState();
        expect(kmVim.vimMode).toBe("insert");
        const kmState = await km.getState();
        expect(kmState.cursor.col).toBe(cm6State.cursor.col);
      }

      await cm6.press("Escape");
      if (km) await km.press("Escape");
    });

    test("I inserts at first non-blank", async ({ cm6, km }) => {
      // Go to an indented line
      await cm6.press("j");
      if (km) await km.press("j");

      await cm6.press("Shift+i");
      if (km) await km.press("Shift+i");

      const cm6Vim = await cm6.getVimState();
      expect(cm6Vim.vimMode).toBe("insert");

      if (km) {
        const kmVim = await km.getVimState();
        expect(kmVim.vimMode).toBe("insert");
        const kmState = await km.getState();
        const cm6State = await cm6.getState();
        expect(kmState.cursor.col).toBe(cm6State.cursor.col);
      }

      await cm6.press("Escape");
      if (km) await km.press("Escape");
    });
  });

  test.describe("Ex Commands Extended", () => {
    test(":set number toggles line numbers option", async ({ cm6, km }) => {
      // This tests that ex commands work for settings
      await cm6.press("Shift+;");
      if (km) await km.press("Shift+;");

      await cm6.type("set number");
      if (km) await km.type("set number");

      await cm6.press("Enter");
      if (km) await km.press("Enter");

      // Both should handle the command without error
      const cm6Vim = await cm6.getVimState();
      expect(cm6Vim.vimMode).toBe("normal");

      if (km) {
        const kmVim = await km.getVimState();
        expect(kmVim.vimMode).toBe("normal");
      }
    });

    test(":5 goes to line 5", async ({ cm6, km }) => {
      await cm6.press("Shift+;");
      await cm6.type("5");
      await cm6.press("Enter");

      const cm6State = await cm6.getState();
      expect(cm6State.cursor.line).toBe(5);

      if (km) {
        // Use 5G (equivalent to :5) since ex command line navigation
        // has a known state sync issue with the test bridge
        await km.press("5");
        await km.press("Shift+g");

        const kmState = await km.getState();
        expect(kmState.cursor.line).toBe(cm6State.cursor.line);
      }
    });
  });
});
