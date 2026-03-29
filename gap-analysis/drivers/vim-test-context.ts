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

  /**
   * Focus the editor by clicking near the top-left corner of .cm-content.
   */
  override async focus(): Promise<void> {
    const content = this.page.locator(".cm-content");
    const box = await content.boundingBox();
    if (box) {
      await this.page.mouse.click(box.x + 5, box.y + 5);
    } else {
      await content.click();
    }
  }

  /**
   * CM6's @replit/codemirror-vim processes vim commands via the input event
   * chain, not just keydown. Playwright's press() only dispatches keydown/up
   * (no input event), so Shift+character keys don't work. Use type() for
   * character keys and press() only for special keys.
   */
  override async press(key: string): Promise<void> {
    const char = this.keyToChar(key);
    if (char !== null) {
      // Use type() to send through the full input event chain
      await this.page.keyboard.type(char);
    } else {
      // Special keys: Enter, Escape, Control+key, etc.
      await this.page.keyboard.press(key);
    }
  }

  /**
   * Convert a Playwright key spec to a character for type(), or null if it
   * must be sent via press() (special/modifier keys).
   */
  private keyToChar(key: string): string | null {
    // No + means it's a single key
    if (!key.includes("+")) {
      // Special keys that need press()
      const specialKeys = new Set([
        "Enter",
        "Escape",
        "Tab",
        "Backspace",
        "Delete",
        "ArrowLeft",
        "ArrowRight",
        "ArrowUp",
        "ArrowDown",
        "Home",
        "End",
        "PageUp",
        "PageDown",
      ]);
      if (specialKeys.has(key)) return null;
      // Single character or letter - use type()
      return key;
    }

    // Handle modifier combinations
    const parts = key.split("+");
    const modifiers = parts.slice(0, -1).map((m) => m.toLowerCase());
    const mainKey = parts[parts.length - 1];

    // Control/Alt/Meta combos must use press()
    if (
      modifiers.includes("control") ||
      modifiers.includes("alt") ||
      modifiers.includes("meta")
    ) {
      return null;
    }

    // Shift+key → shifted character
    if (modifiers.length === 1 && modifiers[0] === "shift") {
      const shiftMap: Record<string, string> = {
        "1": "!",
        "2": "@",
        "3": "#",
        "4": "$",
        "5": "%",
        "6": "^",
        "7": "&",
        "8": "*",
        "9": "(",
        "0": ")",
        "-": "_",
        "=": "+",
        "[": "{",
        "]": "}",
        "\\": "|",
        ";": ":",
        "'": '"',
        ",": "<",
        ".": ">",
        "/": "?",
        "`": "~",
      };
      if (shiftMap[mainKey]) return shiftMap[mainKey];
      // Letters: uppercase
      if (mainKey.length === 1 && mainKey >= "a" && mainKey <= "z") {
        return mainKey.toUpperCase();
      }
      // Already uppercase or unknown - just type it
      return mainKey;
    }

    // Unknown modifier combo - use press()
    return null;
  }
}

/**
 * VimKodemirrorDriver uses the sendKey/typeText bridge exposed by VimTestBridge.kt
 * instead of Playwright keyboard events.
 *
 * Compose for Web's onPreviewKeyEvent doesn't intercept Playwright's synthetic
 * keyboard events, so keys would bypass vim and be inserted as text. The bridge
 * calls Vim.multiSelectHandleKey() directly from Kotlin/WASM.
 */
class VimKodemirrorDriver extends KodemirrorDriver implements VimEditorDriver {
  async getVimState(): Promise<VimStateSnapshot> {
    return await this.page.evaluate(() => {
      return (globalThis as any).__kodemirror.state;
    });
  }

  /** Map a Playwright key string to a vim key string for the sendKey bridge. */
  private toVimKey(key: string): string {
    // Handle modifier combinations (e.g., "Control+r", "Shift+4")
    const parts = key.split("+");
    const modifiers: string[] = [];
    let mainKey = parts[parts.length - 1];

    for (let i = 0; i < parts.length - 1; i++) {
      const mod = parts[i].toLowerCase();
      if (mod === "control") modifiers.push("C");
      else if (mod === "alt") modifiers.push("A");
      else if (mod === "meta") modifiers.push("M");
      else if (mod === "shift") {
        // Shift+letter → uppercase; Shift+digit → symbol
        mainKey = this.shiftedKey(mainKey);
      }
    }

    // Map special Playwright key names to vim key names
    const specialMap: Record<string, string> = {
      Escape: "Esc",
      Enter: "CR",
      Tab: "Tab",
      Backspace: "BS",
      Delete: "Del",
      ArrowLeft: "Left",
      ArrowRight: "Right",
      ArrowUp: "Up",
      ArrowDown: "Down",
      Home: "Home",
      End: "End",
      PageUp: "PageUp",
      PageDown: "PageDown",
      " ": "Space",
    };

    const vimName = specialMap[mainKey];
    if (vimName) {
      if (modifiers.length > 0) {
        return `<${modifiers.join("-")}-${vimName}>`;
      }
      return `<${vimName}>`;
    }

    // For regular characters with Ctrl/Alt modifiers
    if (modifiers.length > 0) {
      return `<${modifiers.join("-")}-${mainKey.toLowerCase()}>`;
    }

    // Plain character key
    return mainKey;
  }

  private shiftedKey(key: string): string {
    const shiftMap: Record<string, string> = {
      "1": "!",
      "2": "@",
      "3": "#",
      "4": "$",
      "5": "%",
      "6": "^",
      "7": "&",
      "8": "*",
      "9": "(",
      "0": ")",
      "-": "_",
      "=": "+",
      "[": "{",
      "]": "}",
      "\\": "|",
      ";": ":",
      "'": '"',
      ",": "<",
      ".": ">",
      "/": "?",
      "`": "~",
    };
    if (shiftMap[key]) return shiftMap[key];
    // Letters: uppercase
    if (key.length === 1 && key >= "a" && key <= "z") return key.toUpperCase();
    // Already uppercase or special
    return key;
  }

  override async press(key: string): Promise<void> {
    const vimKey = this.toVimKey(key);
    const fromVer = await this.getVersion();
    await this.page.evaluate((k: string) => {
      (globalThis as any).__kodemirror.sendKey(k);
    }, vimKey);
    // Wait briefly for state to sync
    try {
      await this.page.waitForFunction(
        (v: number) => {
          const km = (globalThis as any).__kodemirror;
          return km && km.version > v;
        },
        fromVer,
        { timeout: 500 }
      );
    } catch {
      // Some keys may not change state
    }
  }

  override async type(text: string): Promise<void> {
    const fromVer = await this.getVersion();
    await this.page.evaluate((t: string) => {
      (globalThis as any).__kodemirror.typeText(t);
    }, text);
    try {
      await this.page.waitForFunction(
        (v: number) => {
          const km = (globalThis as any).__kodemirror;
          return km && km.version > v;
        },
        fromVer,
        { timeout: 1000 }
      );
    } catch {
      // ignore timeout
    }
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
    // Focus via JS (doesn't move cursor) then normalize to line 1 col 0
    await driver.focus();
    await driver.press("g");
    await driver.press("g");
    await driver.press("0");
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
      // Wait for sendKey bridge to be registered
      await page.waitForFunction(
        () => typeof (globalThis as any).__kodemirror.sendKey === "function",
        undefined,
        { timeout: 5000 }
      );
      driver = kmDriver;
      kmAvailable = true;

      // Normalize cursor to line 1, col 0 via the bridge
      await driver.press("Escape");
      await driver.press("g");
      await driver.press("g");
      await driver.press("0");
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
