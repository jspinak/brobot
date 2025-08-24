package io.github.jspinak.brobot.action.internal.text;

import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;

import org.springframework.stereotype.Component;

/**
 * Handles keyboard key release operations in both real and mock modes.
 * <p>
 * KeyUpWrapper provides a unified interface for releasing keyboard keys, supporting
 * both individual key releases and releasing all currently pressed keys. It abstracts
 * the underlying SikuliX key release functionality while adding mock mode support
 * for testing and logging capabilities.
 * <p>
 * <strong>Key features:</strong>
 * <ul>
 * <li>Release individual keys by string name (e.g., "a", "CTRL")</li>
 * <li>Release special keys by integer code</li>
 * <li>Release all currently pressed keys</li>
 * <li>Mock mode support for testing without actual keyboard interaction</li>
 * <li>Automatic logging of all key release operations</li>
 * </ul>
 * <p>
 * <strong>Usage scenarios:</strong>
 * <ul>
 * <li>Completing keyboard shortcuts after KeyDown operations</li>
 * <li>Ensuring clean keyboard state after complex key combinations</li>
 * <li>Releasing modifier keys (Ctrl, Alt, Shift) after use</li>
 * <li>Emergency release of all keys to prevent stuck key states</li>
 * </ul>
 * <p>
 * <strong>Design rationale:</strong>
 * <p>
 * This wrapper ensures consistent key release behavior across the framework,
 * preventing common issues like stuck modifier keys. The logging functionality
 * aids in debugging keyboard interaction sequences, while mock mode enables
 * comprehensive testing of keyboard-based automation.
 *
 * @see KeyDownWrapper
 * @see TypeTextWrapper
 */
@Component
public class KeyUpWrapper {

    /**
     * Releases all currently pressed keys.
     * <p>
     * This method provides a safety mechanism to ensure no keys remain pressed
     * after automation operations. It's particularly useful for:
     * <ul>
     * <li>Cleanup after complex keyboard sequences</li>
     * <li>Recovery from interrupted operations</li>
     * <li>Ensuring clean state between test cases</li>
     * </ul>
     * <p>
     * <strong>Side effects:</strong>
     * <ul>
     * <li>Releases all keys currently held down by the framework</li>
     * <li>Writes "release all keys|" to the Report</li>
     * <li>In mock mode, only logs without actual key release</li>
     * </ul>
     */
    public void release() {
        ConsoleReporter.format("release all keys| ");
        if (FrameworkSettings.mock) return;
        new Region().sikuli().keyUp();
    }

    /**
     * Releases a specific key identified by its string representation.
     * <p>
     * Accepts both regular keys ("a", "b", "1") and special key names
     * ("CTRL", "ALT", "SHIFT", "ENTER"). The string format follows
     * SikuliX key naming conventions.
     * <p>
     * <strong>Side effects:</strong>
     * <ul>
     * <li>Releases the specified key if currently pressed</li>
     * <li>Writes "release [key]|" to the Report</li>
     * <li>In mock mode, only logs without actual key release</li>
     * </ul>
     *
     * @param key String representation of the key to release (e.g., "a", "CTRL")
     */
    public void release(String key) {
        ConsoleReporter.format("release %s| ", key);
        if (FrameworkSettings.mock) return;
        new Region().sikuli().keyUp(key);
    }

    /**
     * Releases a special key identified by its integer key code.
     * <p>
     * This method is used for special keys that are represented by integer
     * constants in the Key class, such as:
     * <ul>
     * <li>Key.CTRL - Control key</li>
     * <li>Key.ALT - Alt key</li>
     * <li>Key.SHIFT - Shift key</li>
     * <li>Key.META/Key.CMD - Windows/Command key</li>
     * <li>Function keys (Key.F1, Key.F2, etc.)</li>
     * </ul>
     * <p>
     * <strong>Side effects:</strong>
     * <ul>
     * <li>Releases the specified special key if pressed</li>
     * <li>Writes "release [keycode]|" to the Report</li>
     * <li>In mock mode, only logs without actual key release</li>
     * </ul>
     *
     * @param key Integer key code representing a special key
     * @see org.sikuli.script.Key
     */
    public void release(int key) {
        if (FrameworkSettings.mock) {
            ConsoleReporter.format("release %d| ", key);
            return;
        }
        new Region().sikuli().keyUp(key);
    }
}
