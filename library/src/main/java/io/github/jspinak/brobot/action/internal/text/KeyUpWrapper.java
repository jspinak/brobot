package io.github.jspinak.brobot.action.internal.text;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.config.core.BrobotProperties;
import io.github.jspinak.brobot.model.element.Region;
// Removed old logging import: 
/**
 * Handles keyboard key release operations in both real and mock modes.
 *
 * <p>KeyUpWrapper provides a unified interface for releasing keyboard keys, supporting both
 * individual key releases and releasing all currently pressed keys. It abstracts the underlying
 * SikuliX key release functionality while adding mock mode support for testing and logging
 * capabilities.
 *
 * <p><strong>Key features:</strong>
 *
 * <ul>
 *   <li>Release individual keys by string name (e.g., "a", "CTRL")
 *   <li>Release special keys by integer code
 *   <li>Release all currently pressed keys
 *   <li>Mock mode support for testing without actual keyboard interaction
 *   <li>Automatic logging of all key release operations
 * </ul>
 *
 * <p><strong>Usage scenarios:</strong>
 *
 * <ul>
 *   <li>Completing keyboard shortcuts after KeyDown operations
 *   <li>Ensuring clean keyboard state after complex key combinations
 *   <li>Releasing modifier keys (Ctrl, Alt, Shift) after use
 *   <li>Emergency release of all keys to prevent stuck key states
 * </ul>
 *
 * <p><strong>Design rationale:</strong>
 *
 * <p>This wrapper ensures consistent key release behavior across the framework, preventing common
 * issues like stuck modifier keys. The logging functionality aids in debugging keyboard interaction
 * sequences, while mock mode enables comprehensive testing of keyboard-based automation.
 *
 * @see KeyDownWrapper
 * @see TypeTextWrapper
 */
@Component
public class KeyUpWrapper {

    private final BrobotProperties brobotProperties;

    @Autowired
    public KeyUpWrapper(BrobotProperties brobotProperties) {
        this.brobotProperties = brobotProperties;
    }

    /**
     * Releases all currently pressed keys.
     *
     * <p>This method provides a safety mechanism to ensure no keys remain pressed after automation
     * operations. It's particularly useful for:
     *
     * <ul>
     *   <li>Cleanup after complex keyboard sequences
     *   <li>Recovery from interrupted operations
     *   <li>Ensuring clean state between test cases
     * </ul>
     *
     * <p><strong>Side effects:</strong>
     *
     * <ul>
     *   <li>Releases all keys currently held down by the framework
     *   <li>Writes "release all keys|" to the Report
     *   <li>In mock mode, only logs without actual key release
     * </ul>
     */
    public void release() {
        if (brobotProperties.getCore().isMock()) return;
        new Region().sikuli().keyUp();
    }

    /**
     * Releases a specific key identified by its string representation.
     *
     * <p>Accepts both regular keys ("a", "b", "1") and special key names ("CTRL", "ALT", "SHIFT",
     * "ENTER"). The string format follows SikuliX key naming conventions.
     *
     * <p><strong>Side effects:</strong>
     *
     * <ul>
     *   <li>Releases the specified key if currently pressed
     *   <li>Writes "release [key]|" to the Report
     *   <li>In mock mode, only logs without actual key release
     * </ul>
     *
     * @param key String representation of the key to release (e.g., "a", "CTRL")
     */
    public void release(String key) {
        if (brobotProperties.getCore().isMock()) return;
        new Region().sikuli().keyUp(key);
    }

    /**
     * Releases a special key identified by its integer key code.
     *
     * <p>This method is used for special keys that are represented by integer constants in the Key
     * class, such as:
     *
     * <ul>
     *   <li>Key.CTRL - Control key
     *   <li>Key.ALT - Alt key
     *   <li>Key.SHIFT - Shift key
     *   <li>Key.META/Key.CMD - Windows/Command key
     *   <li>Function keys (Key.F1, Key.F2, etc.)
     * </ul>
     *
     * <p><strong>Side effects:</strong>
     *
     * <ul>
     *   <li>Releases the specified special key if pressed
     *   <li>Writes "release [keycode]|" to the Report
     *   <li>In mock mode, only logs without actual key release
     * </ul>
     *
     * @param key Integer key code representing a special key
     * @see org.sikuli.script.Key
     */
    public void release(int key) {
        if (brobotProperties.getCore().isMock()) {
            return;
        }
        new Region().sikuli().keyUp(key);
    }
}
