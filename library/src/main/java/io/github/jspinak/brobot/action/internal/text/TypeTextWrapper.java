package io.github.jspinak.brobot.action.internal.text;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateString;
import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;

import org.springframework.stereotype.Component;

/**
 * Provides text typing functionality with support for both real and mocked operations.
 * <p>
 * This wrapper abstracts Sikuli's text typing operations and integrates with Brobot's
 * action system. It supports typing plain text as well as text with keyboard modifiers
 * (e.g., Ctrl, Alt, Shift combinations). In mock mode, the text and modifiers are
 * logged but not actually typed, useful for testing.
 * <p>
 * The class creates a new {@link Region} instance for each typing operation, which
 * represents the current focus location where the text will be typed.
 * 
 * @see StateString
 * @see ActionOptions#getModifiers()
 * @see FrameworkSettings#mock
 */
@Component
public class TypeTextWrapper {

    /**
     * Types the specified text into the currently focused window or input field.
     * <p>
     * This method handles both plain text typing and typing with keyboard modifiers.
     * If modifiers are specified in the action options (e.g., "ctrl+shift"), they will
     * be held down while typing the text. The method creates a new {@link Region}
     * at the current focus point for the typing operation.
     * <p>
     * In mock mode, the operation is logged but not performed. The Sikuli type method
     * returns 1 on success and 0 on failure, which this method converts to a boolean.
     * 
     * @param stateString Contains the text to be typed. Must not be null.
     * @param actionConfig Configuration that may contain keyboard modifiers.
     *                      An empty string for modifiers results in plain text typing.
     * @return {@code true} if the typing operation was successful (or mocked),
     *         {@code false} if the Sikuli type operation failed in real mode.
     * 
     * @see Region#sikuli()
     */
    public boolean type(StateString stateString, ActionConfig actionConfig) {
        if (FrameworkSettings.mock) return mockType(stateString, actionConfig);
        
        // Get modifiers from TypeOptions if available
        String modifiers = "";
        if (actionConfig instanceof io.github.jspinak.brobot.action.basic.type.TypeOptions) {
            io.github.jspinak.brobot.action.basic.type.TypeOptions typeOptions = 
                (io.github.jspinak.brobot.action.basic.type.TypeOptions) actionConfig;
            modifiers = typeOptions.getModifiers();
        }
        
        if (modifiers.equals(""))
            return new Region().sikuli().type(stateString.getString()) != 0;
        else return new Region().sikuli().type(stateString.getString(), modifiers) != 0;
    }

    /**
     * Simulates typing operation in mock mode by logging the action details.
     * <p>
     * This method is used when {@link FrameworkSettings#mock} is enabled. It logs
     * both the modifiers and the text that would be typed without performing
     * the actual typing operation.
     * 
     * @param stateString Contains the text that would be typed
     * @param actionConfig Contains any keyboard modifiers that would be applied
     * @return Always returns {@code true} to simulate successful typing
     */
    private boolean mockType(StateString stateString, ActionConfig actionConfig) {
        // Get modifiers from TypeOptions if available
        if (actionConfig instanceof io.github.jspinak.brobot.action.basic.type.TypeOptions) {
            io.github.jspinak.brobot.action.basic.type.TypeOptions typeOptions = 
                (io.github.jspinak.brobot.action.basic.type.TypeOptions) actionConfig;
            ConsoleReporter.print(typeOptions.getModifiers());
        }
        ConsoleReporter.print(stateString.getString());
        return true;
    }
}
