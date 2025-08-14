package io.github.jspinak.brobot.action.internal.text;

import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.model.state.StateString;

/**
 * Interface for text typing operations.
 * 
 * Phase 2 Refactoring: Extract interface from TypeTextWrapper
 * to enable profile-based implementation selection.
 */
public interface TextTyper {
    
    /**
     * Types the specified text into the currently focused window or input field.
     * 
     * @param stateString Contains the text to be typed. Must not be null.
     * @param actionOptions Configuration that may contain keyboard modifiers.
     * @return true if the typing operation was successful, false otherwise.
     */
    boolean type(StateString stateString, ActionOptions actionOptions);
}