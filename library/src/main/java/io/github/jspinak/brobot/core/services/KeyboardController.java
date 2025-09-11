package io.github.jspinak.brobot.core.services;

/**
 * Core interface for keyboard control operations.
 *
 * <p>This interface defines the contract for keyboard control implementations, completely decoupled
 * from any higher-level components like Find or Actions. It represents pure keyboard control
 * functionality with no dependencies on the Brobot action framework.
 *
 * <p>Implementations can use various technologies for keyboard control:
 *
 * <ul>
 *   <li>AWT Robot for Java-based control
 *   <li>Sikuli's keyboard control capabilities
 *   <li>Platform-specific APIs
 *   <li>Mock implementations for testing
 * </ul>
 *
 * <p>Key design principles:
 *
 * <ul>
 *   <li>No dependencies on Find, Actions, or any higher-level components
 *   <li>Pure keyboard control - no pattern matching or element finding
 *   <li>Support for text typing and special key combinations
 *   <li>Thread-safe operations
 * </ul>
 *
 * @since 2.0.0
 */
public interface KeyboardController {

    /** Special keys enumeration for keyboard operations. */
    enum SpecialKey {
        ENTER,
        TAB,
        ESC,
        BACKSPACE,
        DELETE,
        SPACE,
        SHIFT,
        CTRL,
        ALT,
        CMD, // Command key on Mac
        UP,
        DOWN,
        LEFT,
        RIGHT,
        HOME,
        END,
        PAGE_UP,
        PAGE_DOWN,
        F1,
        F2,
        F3,
        F4,
        F5,
        F6,
        F7,
        F8,
        F9,
        F10,
        F11,
        F12
    }

    /**
     * Types the specified text string.
     *
     * <p>This method simulates typing the given text as if typed on a physical keyboard. The typing
     * speed may vary based on implementation and configuration.
     *
     * @param text The text to type
     * @return true if typing was successful, false otherwise
     */
    boolean type(String text);

    /**
     * Types the specified text with a delay between characters.
     *
     * <p>This method allows control over typing speed by specifying a delay in milliseconds between
     * each character.
     *
     * @param text The text to type
     * @param delayMs Delay in milliseconds between characters
     * @return true if typing was successful, false otherwise
     */
    boolean type(String text, int delayMs);

    /**
     * Presses and releases a special key.
     *
     * <p>This method simulates pressing a special key like Enter, Tab, or function keys.
     *
     * @param key The special key to press
     * @return true if key press was successful, false otherwise
     */
    boolean pressKey(SpecialKey key);

    /**
     * Presses and holds a special key without releasing.
     *
     * <p>This method is useful for key combinations where a modifier key needs to be held while
     * other keys are pressed.
     *
     * @param key The special key to press and hold
     * @return true if key down was successful, false otherwise
     */
    boolean keyDown(SpecialKey key);

    /**
     * Releases a previously pressed key.
     *
     * <p>This method releases a key that was pressed with keyDown.
     *
     * @param key The special key to release
     * @return true if key release was successful, false otherwise
     */
    boolean keyUp(SpecialKey key);

    /**
     * Performs a keyboard shortcut with a modifier key.
     *
     * <p>This method handles common shortcuts like Ctrl+C, Ctrl+V, etc.
     *
     * @param modifier The modifier key (CTRL, ALT, SHIFT, CMD)
     * @param key The character key to press with the modifier
     * @return true if shortcut was successful, false otherwise
     */
    boolean shortcut(SpecialKey modifier, char key);

    /**
     * Performs a keyboard shortcut with multiple modifier keys.
     *
     * <p>This method handles complex shortcuts like Ctrl+Shift+S.
     *
     * @param modifiers Array of modifier keys to hold
     * @param key The character key to press with the modifiers
     * @return true if shortcut was successful, false otherwise
     */
    boolean shortcut(SpecialKey[] modifiers, char key);

    /**
     * Performs a keyboard shortcut with a modifier and special key.
     *
     * <p>This method handles shortcuts like Alt+Tab, Ctrl+Home, etc.
     *
     * @param modifier The modifier key
     * @param specialKey The special key to press with the modifier
     * @return true if shortcut was successful, false otherwise
     */
    boolean shortcut(SpecialKey modifier, SpecialKey specialKey);

    /**
     * Clears the current text selection and types new text.
     *
     * <p>This method first selects all text (Ctrl+A), then types the new text, effectively
     * replacing any existing content.
     *
     * @param text The text to type after clearing
     * @return true if operation was successful, false otherwise
     */
    boolean clearAndType(String text);

    /**
     * Performs a copy operation (Ctrl+C or Cmd+C).
     *
     * <p>Convenience method for the copy shortcut.
     *
     * @return true if copy was successful, false otherwise
     */
    boolean copy();

    /**
     * Performs a paste operation (Ctrl+V or Cmd+V).
     *
     * <p>Convenience method for the paste shortcut.
     *
     * @return true if paste was successful, false otherwise
     */
    boolean paste();

    /**
     * Performs a cut operation (Ctrl+X or Cmd+X).
     *
     * <p>Convenience method for the cut shortcut.
     *
     * @return true if cut was successful, false otherwise
     */
    boolean cut();

    /**
     * Performs a select all operation (Ctrl+A or Cmd+A).
     *
     * <p>Convenience method for the select all shortcut.
     *
     * @return true if select all was successful, false otherwise
     */
    boolean selectAll();

    /**
     * Checks if the keyboard controller is available and functional.
     *
     * <p>This method can be used to verify that keyboard control is possible in the current
     * environment.
     *
     * @return true if keyboard control is available, false otherwise
     */
    boolean isAvailable();

    /**
     * Gets the name of this keyboard controller implementation.
     *
     * <p>Used for logging and debugging to identify which controller is being used.
     *
     * @return Implementation name (e.g., "AWT Robot", "Sikuli", "Mock")
     */
    String getImplementationName();
}
