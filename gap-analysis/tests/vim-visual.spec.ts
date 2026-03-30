import { test, expect } from "../drivers/vim-test-context";
import * as path from "path";
import * as fs from "fs";

const outputDir = path.join(__dirname, "..", "report", "screenshots");

test.describe("Vim Visual Comparison", () => {
  test.beforeAll(() => {
    fs.mkdirSync(outputDir, { recursive: true });
  });

  test("normal mode - block cursor", async ({ cm6, km }) => {
    // Vim starts in normal mode with block cursor
    await cm6.screenshot(path.join(outputDir, "vim-normal-cm6.png"));
    const cm6Shot = await cm6.screenshot();
    expect(cm6Shot.length).toBeGreaterThan(1000);

    if (km) {
      await km.screenshot(path.join(outputDir, "vim-normal-km.png"));
      const kmShot = await km.screenshot();
      expect(kmShot.length).toBeGreaterThan(1000);
    }
  });

  test("insert mode - thin cursor", async ({ cm6, km }) => {
    // Enter insert mode with 'i'
    await cm6.press("i");
    if (km) await km.press("i");

    // Small delay for mode change to render
    await cm6.page.waitForTimeout(100);

    await cm6.screenshot(path.join(outputDir, "vim-insert-cm6.png"));
    if (km) {
      await km.screenshot(path.join(outputDir, "vim-insert-km.png"));
    }

    // Verify vim mode
    const cm6State = await cm6.getVimState();
    expect(cm6State.vimMode).toBe("insert");

    if (km) {
      const kmState = await km.getVimState();
      expect(kmState.vimMode).toBe("insert");
    }
  });

  test("visual mode - selection highlight", async ({ cm6, km }) => {
    // Enter visual mode with 'v', then select some text
    await cm6.press("v");
    if (km) await km.press("v");

    // Select a few characters with 'l'
    for (let i = 0; i < 5; i++) {
      await cm6.press("l");
      if (km) await km.press("l");
    }

    await cm6.screenshot(path.join(outputDir, "vim-visual-cm6.png"));
    if (km) {
      await km.screenshot(path.join(outputDir, "vim-visual-km.png"));
    }

    const cm6State = await cm6.getVimState();
    expect(cm6State.vimMode).toBe("visual");

    if (km) {
      const kmState = await km.getVimState();
      expect(kmState.vimMode).toBe("visual");
    }
  });

  test("visual line mode", async ({ cm6, km }) => {
    // Enter visual line mode with 'V'
    await cm6.press("Shift+v");
    if (km) await km.press("Shift+v");

    // Select another line down
    await cm6.press("j");
    if (km) await km.press("j");

    await cm6.screenshot(path.join(outputDir, "vim-visual-line-cm6.png"));
    if (km) {
      await km.screenshot(path.join(outputDir, "vim-visual-line-km.png"));
    }

    const cm6State = await cm6.getVimState();
    expect(cm6State.vimMode).toBe("visual-line");

    if (km) {
      const kmState = await km.getVimState();
      expect(kmState.vimMode).toBe("visual-line");
      // Selection should cover only 2 lines, same as CM6
      expect(kmState.selection?.ranges?.[0]?.from).toBe(cm6State.selection?.ranges?.[0]?.from);
      expect(kmState.selection?.ranges?.[0]?.to).toBe(cm6State.selection?.ranges?.[0]?.to);
    }
  });

  test("after navigation - cursor position", async ({ cm6, km }) => {
    // Navigate: go to line 5 with 5G
    await cm6.press("5");
    await cm6.press("Shift+g");
    if (km) {
      await km.press("5");
      await km.press("Shift+g");
    }

    await cm6.screenshot(path.join(outputDir, "vim-nav-cm6.png"));
    if (km) {
      await km.screenshot(path.join(outputDir, "vim-nav-km.png"));
    }

    // Both should be on the same line
    const cm6State = await cm6.getState();
    if (km) {
      const kmState = await km.getState();
      expect(kmState.cursor.line).toBe(cm6State.cursor.line);
    }
  });
});
