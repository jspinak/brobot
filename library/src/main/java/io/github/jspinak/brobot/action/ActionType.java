package io.github.jspinak.brobot.action;
import io.github.jspinak.brobot.action.ActionType;

/**
 * Enumerates all available action types in the Brobot framework.
 * <p>
 * Actions are categorized into:
 * <ul>
 *   <li><b>Basic Actions</b>: Simple, atomic operations like finding patterns or clicking</li>
 *   <li><b>Composite Actions</b>: Complex operations that combine multiple basic actions</li>
 * </ul>
 *
 * @since 2.0
 */
public enum ActionType {
    // Basic Actions
    /** Finds patterns, text, or regions on the screen */
    FIND,
    /** Performs single left mouse click on found elements */
    CLICK,
    /** Performs double click on found elements */
    DOUBLE_CLICK,
    /** Performs right mouse click on found elements */
    RIGHT_CLICK,
    /** Performs middle mouse click on found elements */
    MIDDLE_CLICK,
    /** Defines regions based on found elements or criteria */
    DEFINE,
    /** Types text or key combinations */
    TYPE,
    /** Moves the mouse cursor */
    MOVE,
    /** Hovers mouse over an element (same as MOVE) */
    HOVER,
    /** Waits for elements to disappear */
    VANISH,
    /** Waits for elements to disappear (alias for VANISH) */
    WAIT_VANISH,
    /** Highlights elements on screen */
    HIGHLIGHT,
    /** Scrolls using the mouse wheel */
    SCROLL_MOUSE_WHEEL,
    /** Scrolls up using the mouse wheel */
    SCROLL_UP,
    /** Scrolls down using the mouse wheel */
    SCROLL_DOWN,
    /** Presses and holds mouse button */
    MOUSE_DOWN,
    /** Releases mouse button */
    MOUSE_UP,
    /** Presses and holds keyboard key */
    KEY_DOWN,
    /** Releases keyboard key */
    KEY_UP,
    /** Classifies images using machine learning */
    CLASSIFY,
    
    // Composite Actions
    /** Repeatedly clicks until a condition is met */
    CLICK_UNTIL,
    /** Drags from one location to another */
    DRAG
}