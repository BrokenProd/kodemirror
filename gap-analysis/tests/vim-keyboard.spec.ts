/**
 * Vim Keyboard Integration Tests
 *
 * These tests verify vim keyboard handling via the sendKey/typeText bridge.
 * They complement the parity tests in vim.spec.ts and vim-extended.spec.ts
 * by focusing on the key-suppression contract: normal-mode keys must NOT
 * insert text, while insert-mode keys must insert text.
 *
 * NOTE: Playwright's synthetic keyboard events (page.keyboard.press) don't
 * flow through Compose for Web's event pipeline (Skiko only processes
 * trusted browser events). So we test via the bridge, which exercises the
 * same Vim.handleKey → state update path that real keyboard events use
 * through onPreviewKeyEvent.
 */
import { test as base, Page, expect } from "@playwright/test";

const hasDisplay = !!process.env.DISPLAY;
const KM_READY_TIMEOUT = parseInt(
  process.env.KM_READY_TIMEOUT ?? (hasDisplay ? "30000" : "5000"),
  10
);

interface KeyboardFixtures {
  kmPage: Page | null;
}

let kmAvailableChecked = false;
let kmAvailable = false;

const test = base.extend<KeyboardFixtures>({
  kmPage: async ({ context }, use) => {
    if (kmAvailableChecked && !kmAvailable) {
      await use(null);
      return;
    }

    let page: Page | null = null;
    try {
      page = await context.newPage();
      await page.goto("http://localhost:8081/?test=vim", {
        timeout: KM_READY_TIMEOUT,
        waitUntil: "domcontentloaded",
      });
      await page.waitForFunction(
        () => {
          const km = (globalThis as any).__kodemirror;
          return km && km.ready === true && km.state !== null;
        },
        undefined,
        { timeout: KM_READY_TIMEOUT }
      );
      await page.waitForFunction(
        () => typeof (globalThis as any).__kodemirror.sendKey === "function",
        undefined,
        { timeout: 5000 }
      );
      kmAvailable = true;
    } catch {
      if (page) {
        try {
          await page.close();
        } catch {
          // ignore
        }
      }
      page = null;
      kmAvailable = false;
    }
    kmAvailableChecked = true;
    await use(page);
    if (page) {
      try {
        await page.close();
      } catch {
        // ignore
      }
    }
  },
});

interface VimState {
  doc: string;
  cursor: { pos: number; line: number; col: number };
  vimMode?: string;
  docInfo: { lines: number; length: number };
}

async function getState(page: Page): Promise<VimState> {
  return await page.evaluate(() => (globalThis as any).__kodemirror.state);
}

async function getVersion(page: Page): Promise<number> {
  return await page.evaluate(
    () => (globalThis as any).__kodemirror?.version ?? 0
  );
}

async function waitForUpdate(page: Page, fromVer: number): Promise<void> {
  try {
    await page.waitForFunction(
      (v: number) => {
        const km = (globalThis as any).__kodemirror;
        return km && km.version > v;
      },
      fromVer,
      { timeout: 1000 }
    );
  } catch {
    // Key may not change state
  }
}

async function sendKey(page: Page, key: string): Promise<void> {
  const specialMap: Record<string, string> = {
    Escape: "<Esc>",
    Enter: "<CR>",
  };
  const vimKey = specialMap[key] ?? key;
  const ver = await getVersion(page);
  await page.evaluate(
    (k: string) => (globalThis as any).__kodemirror.sendKey(k),
    vimKey
  );
  await waitForUpdate(page, ver);
}

async function resetCursor(page: Page): Promise<void> {
  await sendKey(page, "Escape");
  await sendKey(page, "g");
  await sendKey(page, "g");
  await sendKey(page, "0");
}

test.describe("Vim Keyboard Integration", () => {
  test.describe("Normal mode: motion keys do not insert text", () => {
    test("j moves cursor down", async ({ kmPage }) => {
      test.skip(!kmPage, "KM not available");
      const page = kmPage!;
      await resetCursor(page);

      const before = await getState(page);
      await sendKey(page, "j");
      const after = await getState(page);

      expect(after.cursor.line).toBe(before.cursor.line + 1);
      expect(after.doc).toBe(before.doc);
    });

    test("k moves cursor up", async ({ kmPage }) => {
      test.skip(!kmPage, "KM not available");
      const page = kmPage!;
      await resetCursor(page);
      await sendKey(page, "j");
      await sendKey(page, "j");

      const before = await getState(page);
      await sendKey(page, "k");
      const after = await getState(page);

      expect(after.cursor.line).toBe(before.cursor.line - 1);
      expect(after.doc).toBe(before.doc);
    });

    test("l moves cursor right", async ({ kmPage }) => {
      test.skip(!kmPage, "KM not available");
      const page = kmPage!;
      await resetCursor(page);

      const before = await getState(page);
      await sendKey(page, "l");
      const after = await getState(page);

      expect(after.cursor.col).toBe(before.cursor.col + 1);
      expect(after.doc).toBe(before.doc);
    });

    test("h moves cursor left", async ({ kmPage }) => {
      test.skip(!kmPage, "KM not available");
      const page = kmPage!;
      await resetCursor(page);
      await sendKey(page, "l");
      await sendKey(page, "l");

      const before = await getState(page);
      await sendKey(page, "h");
      const after = await getState(page);

      expect(after.cursor.col).toBe(before.cursor.col - 1);
      expect(after.doc).toBe(before.doc);
    });

    test("w moves to next word", async ({ kmPage }) => {
      test.skip(!kmPage, "KM not available");
      const page = kmPage!;
      await resetCursor(page);

      const before = await getState(page);
      await sendKey(page, "w");
      const after = await getState(page);

      expect(after.cursor.pos).toBeGreaterThan(before.cursor.pos);
      expect(after.doc).toBe(before.doc);
    });

    test("b moves to previous word", async ({ kmPage }) => {
      test.skip(!kmPage, "KM not available");
      const page = kmPage!;
      await resetCursor(page);
      await sendKey(page, "w");
      await sendKey(page, "w");

      const before = await getState(page);
      await sendKey(page, "b");
      const after = await getState(page);

      expect(after.cursor.pos).toBeLessThan(before.cursor.pos);
      expect(after.doc).toBe(before.doc);
    });
  });

  test.describe("Normal mode: editing keys modify doc correctly", () => {
    test("x deletes one character", async ({ kmPage }) => {
      test.skip(!kmPage, "KM not available");
      const page = kmPage!;
      await resetCursor(page);

      const before = await getState(page);
      await sendKey(page, "x");
      const after = await getState(page);

      expect(after.doc.length).toBe(before.doc.length - 1);
      // No 'x' character was inserted
      expect(after.doc.startsWith("x")).toBe(false);
    });

    test("dd deletes a line", async ({ kmPage }) => {
      test.skip(!kmPage, "KM not available");
      const page = kmPage!;
      await resetCursor(page);

      const before = await getState(page);
      await sendKey(page, "d");
      await sendKey(page, "d");
      const after = await getState(page);

      expect(after.docInfo.lines).toBe(before.docInfo.lines - 1);
    });

    test("r replaces character without inserting 'r'", async ({ kmPage }) => {
      test.skip(!kmPage, "KM not available");
      const page = kmPage!;
      await resetCursor(page);

      const before = await getState(page);
      await sendKey(page, "r");
      await sendKey(page, "Z");
      const after = await getState(page);

      // Same length (replaced, not inserted)
      expect(after.doc.length).toBe(before.doc.length);
      expect(after.doc[0]).toBe("Z");
      expect(after.vimMode).toBe("normal");
    });
  });

  test.describe("Insert mode: text entry works", () => {
    test("i enters insert mode", async ({ kmPage }) => {
      test.skip(!kmPage, "KM not available");
      const page = kmPage!;
      await resetCursor(page);

      await sendKey(page, "i");
      const state = await getState(page);
      expect(state.vimMode).toBe("insert");
    });

    // Insert-mode text insertion is tested in vim.spec.ts and
    // vim-extended.spec.ts (which get fresh pages per test).
    // Sending chars in insert mode on a shared, mutated page can
    // trigger a pre-existing lezer parser crash.

    test("Escape exits insert mode without modifying doc", async ({
      kmPage,
    }) => {
      test.skip(!kmPage, "KM not available");
      const page = kmPage!;
      await resetCursor(page);
      await sendKey(page, "i");

      const before = await getState(page);
      expect(before.vimMode).toBe("insert");

      await sendKey(page, "Escape");
      const after = await getState(page);

      expect(after.vimMode).toBe("normal");
      expect(after.doc).toBe(before.doc);
    });
  });

  test.describe("Mode transitions", () => {
    // Inserting text on a shared, mutated page can trigger a
    // pre-existing lezer parser crash. The normal→insert→normal
    // flow is tested in vim.spec.ts and vim-extended.spec.ts.

    test("visual mode yank preserves document", async ({ kmPage }) => {
      test.skip(!kmPage, "KM not available");
      const page = kmPage!;
      await resetCursor(page);

      const before = await getState(page);
      await sendKey(page, "v");
      await sendKey(page, "e");
      await sendKey(page, "y");

      const after = await getState(page);
      expect(after.vimMode).toBe("normal");
      expect(after.doc).toBe(before.doc);
    });

    test("cc changes line and enters insert mode", async ({ kmPage }) => {
      test.skip(!kmPage, "KM not available");
      const page = kmPage!;
      await resetCursor(page);

      await sendKey(page, "c");
      await sendKey(page, "c");

      const state = await getState(page);
      expect(state.vimMode).toBe("insert");
      // First line content should be cleared
      const firstLine = state.doc.split("\n")[0];
      expect(firstLine.trim()).toBe("");
    });

    test("o opens line below and enters insert mode", async ({ kmPage }) => {
      test.skip(!kmPage, "KM not available");
      const page = kmPage!;
      await resetCursor(page);

      const before = await getState(page);
      await sendKey(page, "o");

      const after = await getState(page);
      expect(after.vimMode).toBe("insert");
      expect(after.docInfo.lines).toBe(before.docInfo.lines + 1);
      expect(after.cursor.line).toBe(before.cursor.line + 1);
    });
  });
});
