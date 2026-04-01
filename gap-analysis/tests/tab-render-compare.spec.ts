import { test, expect } from "../drivers/vim-test-context";
import * as path from "path";

const screenshotDir = path.join(__dirname, "..", "screenshots");

test.describe("Tab Character Rendering", () => {
  test("tab character renders at proper width", async ({ cm6, km }) => {
    // Insert "before\tafter" on a new line via dispatch on both editors.
    const tabLine = "before\tafter";

    // CM6: insert via view.dispatch
    await cm6.page.evaluate((text: string) => {
      const view = (window as any).cmView;
      if (view) {
        const end = view.state.doc.length;
        view.dispatch({
          changes: { from: end, insert: "\n" + text },
        });
      }
    }, tabLine);

    // KM: insert via the typeText bridge in insert mode
    if (km) {
      await km.page.evaluate((text: string) => {
        const kmObj = (globalThis as any).__kodemirror;
        if (kmObj && kmObj.sendKey) {
          kmObj.sendKey("<Esc>");
          kmObj.sendKey("G");
          kmObj.sendKey("o");
          kmObj.typeText(text);
          kmObj.sendKey("<Esc>");
        }
      }, tabLine);
      await km.page.waitForTimeout(300);
    }

    const cm6Doc = await cm6.getDoc();
    expect(cm6Doc).toContain("before\tafter");

    if (km) {
      const kmDoc = await km.getDoc();
      expect(kmDoc).toContain("before\tafter");
    }

    // Screenshot for visual comparison of tab rendering
    await cm6.screenshot(path.join(screenshotDir, "cm6-tab-render.png"));
    if (km) await km.screenshot(path.join(screenshotDir, "km-tab-render.png"));
  });

  test("tabs at different columns align to tab stops", async ({
    cm6,
    km,
  }) => {
    // Insert lines with tabs at column 0, 1, 2, 3.
    // With tabSize=4, the "x" after each tab should visually align at column 4.
    const lines = "\n\tx\na\tx\nab\tx\nabc\tx";

    await cm6.page.evaluate((text: string) => {
      const view = (window as any).cmView;
      if (view) {
        const end = view.state.doc.length;
        view.dispatch({ changes: { from: end, insert: text } });
      }
    }, lines);

    if (km) {
      await km.page.evaluate((text: string) => {
        const kmObj = (globalThis as any).__kodemirror;
        if (kmObj && kmObj.sendKey) {
          kmObj.sendKey("<Esc>");
          kmObj.sendKey("G");
          kmObj.sendKey("o");
          // Skip leading \n since 'o' already opens a new line
          kmObj.typeText(text.slice(1));
          kmObj.sendKey("<Esc>");
        }
      }, lines);
      await km.page.waitForTimeout(300);
    }

    await cm6.screenshot(path.join(screenshotDir, "cm6-tab-columns.png"));
    if (km) await km.screenshot(path.join(screenshotDir, "km-tab-columns.png"));

    const cm6Doc = await cm6.getDoc();
    if (km) {
      const kmDoc = await km.getDoc();
      expect(kmDoc.includes("\tx")).toBe(true);
    }
  });
});
