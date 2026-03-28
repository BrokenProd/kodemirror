import { test as base, Page } from "@playwright/test";
import { CM6Driver } from "./cm6-driver";
import { KodemirrorDriver } from "./kodemirror-driver";
import { EditorDriver, EditorStateSnapshot } from "./types";
import * as path from "path";

export interface VimStateSnapshot extends EditorStateSnapshot {
  vimMode?: string;
  vimStatus?: string;
}

export interface VimEditorDriver extends EditorDriver {
  getVimState(): Promise<VimStateSnapshot>;
}

class VimCM6Driver extends CM6Driver implements VimEditorDriver {
  async getVimState(): Promise<VimStateSnapshot> {
    return await this.page.evaluate(() => (window as any).getState());
  }
}

class VimKodemirrorDriver extends KodemirrorDriver implements VimEditorDriver {
  async getVimState(): Promise<VimStateSnapshot> {
    return await this.page.evaluate(() => {
      return (globalThis as any).__kodemirror.state;
    });
  }
}

export interface VimTestFixtures {
  cm6: VimEditorDriver;
  km: VimEditorDriver | null;
}

const cm6VimFixturePath = path.join(
  __dirname,
  "..",
  "fixtures",
  "cm6-vim-test.html"
);

const hasDisplay = !!process.env.DISPLAY;
const KM_READY_TIMEOUT = parseInt(
  process.env.KM_READY_TIMEOUT ?? (hasDisplay ? "30000" : "5000"),
  10
);

let kmAvailableChecked = false;
let kmAvailable = false;

export const test = base.extend<VimTestFixtures>({
  cm6: async ({ context }, use) => {
    await context.grantPermissions(["clipboard-read", "clipboard-write"]);
    const page = await context.newPage();
    await page.goto(`file://${cm6VimFixturePath}`);
    const driver = new VimCM6Driver(page);
    await driver.waitForReady();
    await driver.focus();
    await use(driver);
    await page.close();
  },

  km: async ({ context }, use) => {
    if (kmAvailableChecked && !kmAvailable) {
      await use(null);
      return;
    }

    await context.grantPermissions(["clipboard-read", "clipboard-write"]);
    let driver: VimKodemirrorDriver | null = null;
    let page: Page | null = null;
    try {
      page = await context.newPage();
      // Load KM with vim test mode
      await page.goto("http://localhost:8081/?test=vim", {
        timeout: KM_READY_TIMEOUT,
        waitUntil: "domcontentloaded",
      });
      const kmDriver = new VimKodemirrorDriver(page);
      await page.waitForFunction(
        () => {
          const km = (globalThis as any).__kodemirror;
          return km && km.ready === true && km.state !== null;
        },
        undefined,
        { timeout: KM_READY_TIMEOUT }
      );
      driver = kmDriver;
      kmAvailable = true;
    } catch {
      if (page) {
        try {
          await page.close();
        } catch {
          // ignore close errors
        }
      }
      page = null;
      kmAvailable = false;
    }
    kmAvailableChecked = true;
    await use(driver);
    if (driver && page) {
      try {
        await page.close();
      } catch {
        // ignore close errors
      }
    }
  },
});

export { expect } from "@playwright/test";
