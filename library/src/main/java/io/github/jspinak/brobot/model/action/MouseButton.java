package io.github.jspinak.brobot.model.action;

/**
 * Represents the physical mouse buttons.
 *
 * <p>This enum has a single responsibility: identifying which mouse button is being used. It does
 * not concern itself with click types (single/double) or timing behaviors, following the Single
 * Responsibility Principle.
 */
public enum MouseButton {
    /** The primary (left) mouse button. Typically used for selection and primary actions. */
    LEFT,

    /**
     * The secondary (right) mouse button. Typically used for context menus and secondary actions.
     */
    RIGHT,

    /**
     * The middle mouse button or scroll wheel click. Often used for auxiliary functions like
     * opening links in new tabs.
     */
    MIDDLE
}
