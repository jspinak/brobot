package io.github.jspinak.brobot.core.services;

/**
 * Core interface for mouse control operations.
 *
 * <p>This interface defines the contract for mouse control implementations, completely decoupled
 * from any higher-level components like Find or Actions. It represents pure mouse control
 * functionality with no dependencies on the Brobot action framework.
 *
 * <p>Implementations can use various technologies for mouse control:
 *
 * <ul>
 *   <li>AWT Robot for Java-based control
 *   <li>Sikuli's mouse control capabilities
 *   <li>Platform-specific APIs
 *   <li>Mock implementations for testing
 * </ul>
 *
 * <p>Key design principles:
 *
 * <ul>
 *   <li>No dependencies on Find, Actions, or any higher-level components
 *   <li>Pure mouse control - no pattern matching or element finding
 *   <li>Support for all standard mouse operations
 *   <li>Thread-safe operations
 * </ul>
 *
 * @since 2.0.0
 */
public interface MouseController {

    /** Mouse button enumeration for click operations. */
    enum MouseButton {
        LEFT,
        RIGHT,
        MIDDLE
    }

    /**
     * Moves the mouse cursor to the specified coordinates.
     *
     * <p>This method moves the mouse cursor to the absolute screen coordinates specified. The
     * movement may be instantaneous or animated depending on the implementation.
     *
     * @param x X coordinate to move to
     * @param y Y coordinate to move to
     * @return true if the movement was successful, false otherwise
     */
    boolean moveTo(int x, int y);

    /**
     * Performs a mouse click at the specified coordinates.
     *
     * <p>This method moves the mouse to the specified location and performs a click with the
     * specified button.
     *
     * @param x X coordinate to click at
     * @param y Y coordinate to click at
     * @param button Which mouse button to click
     * @return true if the click was successful, false otherwise
     */
    boolean click(int x, int y, MouseButton button);

    /**
     * Performs a left mouse click at the specified coordinates.
     *
     * <p>Convenience method for left-clicking, which is the most common mouse operation.
     *
     * @param x X coordinate to click at
     * @param y Y coordinate to click at
     * @return true if the click was successful, false otherwise
     */
    default boolean click(int x, int y) {
        return click(x, y, MouseButton.LEFT);
    }

    /**
     * Performs a double-click at the specified coordinates.
     *
     * <p>This method performs two rapid clicks at the specified location with the specified button.
     *
     * @param x X coordinate to double-click at
     * @param y Y coordinate to double-click at
     * @param button Which mouse button to click
     * @return true if the double-click was successful, false otherwise
     */
    boolean doubleClick(int x, int y, MouseButton button);

    /**
     * Performs a left mouse double-click at the specified coordinates.
     *
     * <p>Convenience method for left double-clicking.
     *
     * @param x X coordinate to double-click at
     * @param y Y coordinate to double-click at
     * @return true if the double-click was successful, false otherwise
     */
    default boolean doubleClick(int x, int y) {
        return doubleClick(x, y, MouseButton.LEFT);
    }

    /**
     * Performs a right-click (context menu click) at the specified coordinates.
     *
     * <p>Convenience method for right-clicking.
     *
     * @param x X coordinate to right-click at
     * @param y Y coordinate to right-click at
     * @return true if the right-click was successful, false otherwise
     */
    default boolean rightClick(int x, int y) {
        return click(x, y, MouseButton.RIGHT);
    }

    /**
     * Presses and holds the specified mouse button at the current location.
     *
     * <p>This method presses the button without releasing it, useful for drag operations.
     *
     * @param button Which mouse button to press
     * @return true if the press was successful, false otherwise
     */
    boolean mouseDown(MouseButton button);

    /**
     * Releases the specified mouse button.
     *
     * <p>This method releases a previously pressed button.
     *
     * @param button Which mouse button to release
     * @return true if the release was successful, false otherwise
     */
    boolean mouseUp(MouseButton button);

    /**
     * Performs a drag operation from one location to another.
     *
     * <p>This method presses the button at the start location, moves to the end location, and
     * releases the button.
     *
     * @param startX Starting X coordinate
     * @param startY Starting Y coordinate
     * @param endX Ending X coordinate
     * @param endY Ending Y coordinate
     * @param button Which mouse button to use for dragging
     * @return true if the drag was successful, false otherwise
     */
    boolean drag(int startX, int startY, int endX, int endY, MouseButton button);

    /**
     * Performs a left-button drag operation.
     *
     * <p>Convenience method for dragging with the left button.
     *
     * @param startX Starting X coordinate
     * @param startY Starting Y coordinate
     * @param endX Ending X coordinate
     * @param endY Ending Y coordinate
     * @return true if the drag was successful, false otherwise
     */
    default boolean drag(int startX, int startY, int endX, int endY) {
        return drag(startX, startY, endX, endY, MouseButton.LEFT);
    }

    /**
     * Scrolls the mouse wheel.
     *
     * <p>Positive values scroll up/forward, negative values scroll down/backward.
     *
     * @param wheelAmt Amount to scroll (positive for up, negative for down)
     * @return true if the scroll was successful, false otherwise
     */
    boolean scroll(int wheelAmt);

    /**
     * Gets the current mouse cursor position.
     *
     * <p>Returns the current screen coordinates of the mouse cursor.
     *
     * @return Array with [x, y] coordinates, or null if unavailable
     */
    int[] getPosition();

    /**
     * Checks if the mouse controller is available and functional.
     *
     * <p>This method can be used to verify that mouse control is possible in the current
     * environment.
     *
     * @return true if mouse control is available, false otherwise
     */
    boolean isAvailable();

    /**
     * Gets the name of this mouse controller implementation.
     *
     * <p>Used for logging and debugging to identify which controller is being used.
     *
     * @return Implementation name (e.g., "AWT Robot", "Sikuli", "Mock")
     */
    String getImplementationName();
}
