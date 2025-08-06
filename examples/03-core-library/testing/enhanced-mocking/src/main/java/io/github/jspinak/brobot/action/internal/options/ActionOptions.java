package io.github.jspinak.brobot.action.internal.options;

/**
 * Simplified ActionOptions for the enhanced mocking example.
 * In a real Brobot project, this would come from the actual library.
 */
public class ActionOptions {
    
    public enum Action {
        FIND, CLICK, TYPE, MOVE, VANISH, HIGHLIGHT, SCROLL_MOUSE_WHEEL,
        MOUSE_DOWN, MOUSE_UP, KEY_DOWN, KEY_UP, CLASSIFY,
        CLICK_UNTIL, DRAG
    }
}