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
 * VimKodemirrorDriver uses REAL browser keyboard events to interact with
 * Kodemirror's vim mode — the same path that a real user's keystrokes follow.
 *
 * Compose for Web renders into a shadow DOM with a canvas and a hidden textarea.
 * The keyboard event flow is:
 *   1. page.keyboard.press(key) → browser dispatches KeyboardEvent
 *   2. Document-level capture keydown listener (Platform.wasmJs.kt) intercepts it
 *   3. KodeMirror's onPreviewKeyEvent processes it for vim
 *   4. For insert-mode text input, page.keyboard.type(ch) goes through the
 *      textarea → DomInputStrategy → Compose text input pipeline
 *
 * Any test failures from this approach point to REAL input handling bugs in
 * the Compose/Skiko/KodeMirror pipeline.
 */
class VimKodemirrorDriver extends KodemirrorDriver implements VimEditorDriver {
  async getVimState(): Promise<VimStateSnapshot> {
    return await this.page.evaluate(() => {
      return (globalThis as any).__kodemirror.state;
    });
  }

  /**
   * Determine if the editor is currently in insert mode.
   */
  private async isInsertMode(): Promise<boolean> {
    const state = await this.getVimState();
    return state.vimMode === "insert";
  }

  /**
   * Convert a Playwright key spec to a character for type(), or null if it
   * must be sent via press() (special/modifier keys).
   */
  private keyToChar(key: string): string | null {
    // No + means it's a single key
    if (!key.includes("+")) {
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
      if (mainKey.length === 1 && mainKey >= "a" && mainKey <= "z") {
        return mainKey.toUpperCase();
      }
      return mainKey;
    }

    return null;
  }

  /**
   * Press a key using real browser events.
   *
   * In insert mode, character keys are sent via type() (through the textarea
   * input pipeline). In normal mode, all keys go through press() (keyboard
   * events intercepted by the document-level listener).
   *
   * Special keys (Escape, Enter, arrows, etc.) always use press().
   */
  /**
   * Press a key using the real browser input path.
   *
   * - Normal mode: focus canvas → press() → Skiko → Compose KeyEvent → vim
   * - Insert mode chars: focus textarea → type() → Compose text input
   * - Special keys always: focus canvas → press() → Skiko → Compose KeyEvent
   */
  override async press(key: string): Promise<void> {
    const ver = await this.getVersion();

    const char = this.keyToChar(key);
    const insertMode = await this.isInsertMode();

    if (char !== null && insertMode) {
      // Insert mode text input goes through the textarea
      await this.focusTextarea();
      await this.page.keyboard.type(char);
    } else {
      // Normal mode keys and special keys go through the canvas → Skiko
      await this.focusCanvas();
      await this.page.keyboard.press(key);
    }

    await this.waitForUpdate(ver);
  }

  override async type(text: string): Promise<void> {
    await this.focusTextarea();
    for (const ch of text) {
      const ver = await this.getVersion();
      await this.page.keyboard.type(ch);
      await this.waitForUpdate(ver);
    }
  }

  /**
   * Focus the canvas for keyboard events. Skiko routes key events from
   * the canvas through Compose's onPreviewKeyEvent pipeline.
   */
  private async focusCanvas(): Promise<void> {
    await this.page.evaluate(() => {
      const shadow = document.body.shadowRoot;
      if (shadow) {
        const canvas = shadow.querySelector("canvas");
        if (canvas) (canvas as HTMLElement).focus();
      }
    });
  }

  /**
   * Focus the textarea for text input. In insert mode, typed characters
   * need to go through the textarea → Compose text input pipeline.
   */
  private async focusTextarea(): Promise<void> {
    await this.page.evaluate(() => {
      const shadow = document.body.shadowRoot;
      if (shadow) {
        const ta = shadow.querySelector("textarea");
        if (ta) ta.focus();
      }
    });
  }

  private async waitForUpdate(fromVersion: number): Promise<void> {
    try {
      await this.page.waitForFunction(
        (v: number) => {
          const km = (globalThis as any).__kodemirror;
          return km && km.version > v;
        },
        fromVersion,
        { timeout: 500 }
      );
    } catch {
      // Key press may not change state
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
