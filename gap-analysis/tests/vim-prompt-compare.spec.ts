import { test } from "../drivers/vim-test-context";
import * as path from "path";

const screenshotDir = path.join(__dirname, "..", "screenshots");

test.describe("Vim Prompt Comparison", () => {
  test("ex command prompt (:) - visual compare", async ({ cm6, km }) => {
    // Focus and open the ex command prompt with :
    await cm6.focus();
    await cm6.press("Shift+;"); // :
    await cm6.page.waitForTimeout(500);
    await cm6.screenshot(path.join(screenshotDir, "cm6-vim-ex-prompt.png"));

    if (km) {
      await km.focus();
      await km.press("Shift+;"); // :
      await km.page.waitForTimeout(500);
      await km.screenshot(path.join(screenshotDir, "km-vim-ex-prompt.png"));
    }
  });

  test("ex command prompt with typed command (:set number) - visual compare", async ({
    cm6,
    km,
  }) => {
    // Open : prompt and type a command without pressing Enter
    await cm6.focus();
    await cm6.press("Shift+;"); // :
    await cm6.page.waitForTimeout(300);
    await cm6.page.keyboard.type("set number", { delay: 50 });
    await cm6.page.waitForTimeout(500);
    await cm6.screenshot(
      path.join(screenshotDir, "cm6-vim-ex-set-number.png")
    );

    if (km) {
      await km.focus();
      await km.press("Shift+;"); // :
      await km.page.waitForTimeout(300);
      await km.page.keyboard.type("set number", { delay: 50 });
      await km.page.waitForTimeout(500);
      await km.screenshot(
        path.join(screenshotDir, "km-vim-ex-set-number.png")
      );
    }
  });

  test("forward search prompt (/) with query - visual compare", async ({
    cm6,
    km,
  }) => {
    // Press / to open forward search, type a search term
    await cm6.focus();
    await cm6.press("/");
    await cm6.page.waitForTimeout(300);
    await cm6.page.keyboard.type("fibonacci", { delay: 50 });
    await cm6.page.waitForTimeout(500);
    await cm6.screenshot(
      path.join(screenshotDir, "cm6-vim-search-forward.png")
    );

    if (km) {
      await km.focus();
      await km.press("/");
      await km.page.waitForTimeout(300);
      await km.page.keyboard.type("fibonacci", { delay: 50 });
      await km.page.waitForTimeout(500);
      await km.screenshot(
        path.join(screenshotDir, "km-vim-search-forward.png")
      );
    }
  });

  test("backward search prompt (?) with query - visual compare", async ({
    cm6,
    km,
  }) => {
    // Move to end of file first so backward search has something to find
    await cm6.focus();
    await cm6.press("Shift+g"); // G - go to end
    await cm6.page.waitForTimeout(200);
    await cm6.press("Shift+/"); // ?
    await cm6.page.waitForTimeout(300);
    await cm6.page.keyboard.type("fibonacci", { delay: 50 });
    await cm6.page.waitForTimeout(500);
    await cm6.screenshot(
      path.join(screenshotDir, "cm6-vim-search-backward.png")
    );

    if (km) {
      await km.focus();
      await km.press("Shift+g"); // G - go to end
      await km.page.waitForTimeout(200);
      await km.press("Shift+/"); // ?
      await km.page.waitForTimeout(300);
      await km.page.keyboard.type("fibonacci", { delay: 50 });
      await km.page.waitForTimeout(500);
      await km.screenshot(
        path.join(screenshotDir, "km-vim-search-backward.png")
      );
    }
  });

  test("substitute prompt (:s/) - visual compare", async ({ cm6, km }) => {
    // Open : prompt and type a substitute command
    await cm6.focus();
    await cm6.press("Shift+;"); // :
    await cm6.page.waitForTimeout(300);
    await cm6.page.keyboard.type("s/foo/bar/g", { delay: 50 });
    await cm6.page.waitForTimeout(500);
    await cm6.screenshot(
      path.join(screenshotDir, "cm6-vim-substitute.png")
    );

    if (km) {
      await km.focus();
      await km.press("Shift+;"); // :
      await km.page.waitForTimeout(300);
      await km.page.keyboard.type("s/foo/bar/g", { delay: 50 });
      await km.page.waitForTimeout(500);
      await km.screenshot(
        path.join(screenshotDir, "km-vim-substitute.png")
      );
    }
  });
});
