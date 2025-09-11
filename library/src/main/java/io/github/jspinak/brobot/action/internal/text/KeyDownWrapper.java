package io.github.jspinak.brobot.action.internal.text;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;

/**
 * Handles keyboard key press-and-hold operations for modifier key combinations.
 *
 * <p>KeyDownWrapper provides functionality to press and hold keys without releasing them, enabling
 * the creation of keyboard shortcuts and modifier key combinations. This is essential for
 * operations like Ctrl+C, Alt+Tab, or Shift+Click where one key must be held while another action
 * occurs.
 *
 * <p><strong>Important behavior notes:</strong>
 *
 * <ul>
 *   <li>KeyDown does NOT produce repeated characters - holding 'a' won't type 'aaaa'
 *   <li>The key remains logically pressed until explicitly released with KeyUp
 *   <li>This is a state change, not a continuous action
 *   <li>Multiple keys can be held simultaneously
 *   <li>Always pair with KeyUp to avoid stuck key states
 * </ul>
 *
 * <p><strong>Technical limitations:</strong>
 *
 * <p>The underlying implementation (Java Robot/SikuliX) simulates a single key press event with the
 * key state set to "pressed". It does not continuously send key events, which is why held keys
 * don't auto-repeat. This is by design to support modifier key functionality.
 *
 * <p><strong>Common usage patterns:</strong>
 *
 * <pre>{@code
 * // Ctrl+C copy operation
 * keyDownWrapper.press("c", "CTRL");
 * keyUpWrapper.release("c");
 * keyUpWrapper.release("CTRL");
 *
 * // Alt+Tab window switching
 * keyDownWrapper.press("TAB", "ALT");
 * keyUpWrapper.release("TAB");
 * keyUpWrapper.release("ALT");
 * }</pre>
 *
 * @see KeyUpWrapper
 * @see TypeTextWrapper
 */
@Component
public class KeyDownWrapper {

    /**
     * Presses and holds a key with optional modifier keys.
     *
     * <p>The key remains in pressed state until explicitly released using {@link
     * KeyUpWrapper#release(String)}. This enables modifier key combinations where one key must be
     * held while another is pressed.
     *
     * <p><strong>Parameter format:</strong>
     *
     * <ul>
     *   <li>key: Single character ("a", "b") or special key name ("TAB", "ENTER")
     *   <li>modifiers: Modifier keys like "CTRL", "ALT", "SHIFT" or combinations "CTRL+SHIFT"
     * </ul>
     *
     * <p><strong>Side effects:</strong>
     *
     * <ul>
     *   <li>Sets the specified key(s) to pressed state in the OS
     *   <li>In mock mode: writes "hold [modifiers] [key]|" to Report
     *   <li>In real mode: writes "[key] " to Report and presses the key
     * </ul>
     *
     * <p><strong>Important:</strong> Always release pressed keys with KeyUpWrapper to prevent stuck
     * key states that can interfere with subsequent operations.
     *
     * @param key The primary key to press (e.g., "a", "TAB", "F1")
     * @param modifiers Modifier keys to hold with the primary key (e.g., "CTRL", "ALT+SHIFT")
     */
    public void press(String key, String modifiers) {
        if (FrameworkSettings.mock) {
            ConsoleReporter.format("hold %s %s| ", modifiers, key);
            return;
        }
        ConsoleReporter.print(key + " ");
        new Region().sikuli().keyDown(key + modifiers);
    }
}
