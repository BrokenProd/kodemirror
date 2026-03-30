import { test, expect } from "../drivers/test-context";
import * as path from "path";

const screenshotDir = path.join(__dirname, "..", "screenshots");

test.describe("Search Panel Comparison", () => {
  test("search panel open - visual compare", async ({ cm6, km }) => {
    // Focus both editors
    await cm6.focus();
    if (km) await km.focus();

    // Open search panel in CM6
    await cm6.press("Control+f");
    // Wait a moment for panel to render
    await cm6.page.waitForTimeout(500);
    await cm6.screenshot(path.join(screenshotDir, "cm6-search-panel.png"));

    if (km) {
      // Open search panel in KM
      await km.press("Control+f");
      await km.page.waitForTimeout(1000);
      await km.screenshot(path.join(screenshotDir, "km-search-panel.png"));
    }
  });

  test("find and replace panel - visual compare", async ({ cm6, km }) => {
    await cm6.focus();
    if (km) await km.focus();

    // Open find-and-replace panel in CM6 with Ctrl+H
    await cm6.press("Control+h");
    await cm6.page.waitForTimeout(500);
    await cm6.screenshot(
      path.join(screenshotDir, "cm6-find-replace-panel.png")
    );

    if (km) {
      // Open find-and-replace panel in KM with Ctrl+H
      await km.press("Control+h");
      await km.page.waitForTimeout(1000);
      await km.screenshot(
        path.join(screenshotDir, "km-find-replace-panel.png")
      );
    }
  });

  test("search panel with query - visual compare", async ({ cm6, km }) => {
    await cm6.focus();
    if (km) await km.focus();

    // Open search and type a query in CM6
    await cm6.press("Control+f");
    await cm6.page.waitForTimeout(300);
    await cm6.page.locator(".cm-search input[main-field]").fill("fibonacci");
    await cm6.page.waitForTimeout(500);
    await cm6.screenshot(
      path.join(screenshotDir, "cm6-search-with-query.png")
    );

    if (km) {
      // Open search in KM and type query
      await km.press("Control+f");
      await km.page.waitForTimeout(1000);
      await km.page.keyboard.type("fibonacci", { delay: 50 });
      await km.page.waitForTimeout(500);
      await km.screenshot(
        path.join(screenshotDir, "km-search-with-query.png")
      );
    }
  });
});
