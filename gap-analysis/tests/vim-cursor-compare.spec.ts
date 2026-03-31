import { test, expect } from "../drivers/vim-test-context";
import * as path from "path";

const screenshotDir = path.join(__dirname, "..", "screenshots");

test.describe("Vim Cursor Comparison", () => {
  test("normal cursor on character vs empty line", async ({ cm6, km }) => {
    // First: cursor on a normal character (line 1, col 0)
    await cm6.press("g");
    await cm6.press("g");
    await cm6.press("0");
    if (km) {
      await km.press("g");
      await km.press("g");
      await km.press("0");
    }
    await cm6.page.waitForTimeout(200);
    if (km) await km.page.waitForTimeout(200);

    await cm6.screenshot(path.join(screenshotDir, "cm6-cursor-on-char.png"));
    if (km) await km.screenshot(path.join(screenshotDir, "km-cursor-on-char.png"));

    // Now move to the empty line (line 6 in the fibonacci code)
    await cm6.press("j"); await cm6.press("j"); await cm6.press("j");
    await cm6.press("j"); await cm6.press("j"); // should be on line 6 (empty)
    if (km) {
      await km.press("j"); await km.press("j"); await km.press("j");
      await km.press("j"); await km.press("j");
    }
    await cm6.page.waitForTimeout(200);
    if (km) await km.page.waitForTimeout(200);

    const cm6State = await cm6.getState();
    console.log("CM6 cursor:", cm6State.cursor);
    if (km) {
      const kmState = await km.getState();
      console.log("KM cursor:", kmState.cursor);
    }

    await cm6.screenshot(path.join(screenshotDir, "cm6-cursor-empty-line.png"));
    if (km) await km.screenshot(path.join(screenshotDir, "km-cursor-empty-line.png"));
  });
});
