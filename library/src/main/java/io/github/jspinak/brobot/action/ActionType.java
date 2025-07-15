package io.github.jspinak.brobot.action;

/**
 * Enumeration of action types for convenient action execution.
 * <p>
 * This enum provides a type-safe way to specify actions when using
 * the convenience methods in the Action class. It enables simple,
 * discoverable API calls like:
 * {@code action.perform(CLICK, location)}
 * </p>
 * 
 * @since 2.0
 * @see Action#perform(ActionType, Object...)
 */
public enum ActionType {
    // Mouse actions
    CLICK("Click on a location or region"),
    DOUBLE_CLICK("Double-click on a location or region"),
    RIGHT_CLICK("Right-click on a location or region"),
    MIDDLE_CLICK("Middle-click on a location or region"),
    
    // Visual actions
    HIGHLIGHT("Highlight a region"),
    
    // Keyboard actions
    TYPE("Type text at current location or after clicking"),
    KEY_DOWN("Press and hold a key"),
    KEY_UP("Release a key"),
    
    // Mouse movement
    HOVER("Move mouse to location without clicking"),
    DRAG("Drag from one location to another"),
    MOUSE_DOWN("Press mouse button without releasing"),
    MOUSE_UP("Release mouse button"),
    
    // Scroll actions
    SCROLL_UP("Scroll up at location"),
    SCROLL_DOWN("Scroll down at location"),
    
    // Wait actions
    WAIT("Wait for a specified duration"),
    WAIT_VANISH("Wait for element to disappear"),
    
    // Verification actions
    EXISTS("Check if element exists"),
    FIND("Find elements on screen");
    
    private final String description;
    
    ActionType(String description) {
        this.description = description;
    }
    
    /**
     * Gets a human-readable description of the action type.
     * 
     * @return the action description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Determines if this action type requires a location or region.
     * 
     * @return true if the action needs a location/region, false otherwise
     */
    public boolean requiresLocation() {
        switch (this) {
            case CLICK:
            case DOUBLE_CLICK:
            case RIGHT_CLICK:
            case MIDDLE_CLICK:
            case HOVER:
            case SCROLL_UP:
            case SCROLL_DOWN:
            case MOUSE_DOWN:
            case MOUSE_UP:
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Determines if this action type requires a region (not just a location).
     * 
     * @return true if the action needs a region with bounds, false otherwise
     */
    public boolean requiresRegion() {
        return this == HIGHLIGHT;
    }
    
    /**
     * Determines if this is a keyboard action type.
     * 
     * @return true if this is a keyboard action, false otherwise
     */
    public boolean isKeyboardAction() {
        switch (this) {
            case TYPE:
            case KEY_DOWN:
            case KEY_UP:
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Determines if this is a mouse action type.
     * 
     * @return true if this is a mouse action, false otherwise
     */
    public boolean isMouseAction() {
        switch (this) {
            case CLICK:
            case DOUBLE_CLICK:
            case RIGHT_CLICK:
            case MIDDLE_CLICK:
            case HOVER:
            case DRAG:
            case MOUSE_DOWN:
            case MOUSE_UP:
            case SCROLL_UP:
            case SCROLL_DOWN:
                return true;
            default:
                return false;
        }
    }
}