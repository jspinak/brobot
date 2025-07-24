package io.github.jspinak.brobot.action.internal.text;

import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateString;
import io.github.jspinak.brobot.config.FrameworkSettings;

import org.springframework.stereotype.Component;

/**
 * Provides text typing functionality with support for both real and mocked operations.
 * <p>
 * This is version 2 of the TypeTextWrapper, updated to work with the new ActionConfig
 * hierarchy and TypeOptions instead of ActionOptions.
 * </p>
 * <p>
 * This wrapper abstracts Sikuli's text typing operations and integrates with Brobot's
 * action system. It supports typing plain text as well as text with keyboard modifiers
 * (e.g., Ctrl, Alt, Shift combinations). In mock mode, the text and modifiers are
 * logged but not actually typed, useful for testing.
 * </p>
 * 
 * @see StateString
 * @see TypeOptions
 * @see FrameworkSettings#mock
 * @since 2.0
 */
@Component
public class TypeTextWrapperV2 {
    
    private final TypeTextWrapper legacyWrapper;
    
    public TypeTextWrapperV2(TypeTextWrapper legacyWrapper) {
        this.legacyWrapper = legacyWrapper;
    }

    /**
     * Types the specified text into the currently focused window or input field.
     * <p>
     * This method handles both plain text typing and typing with keyboard modifiers.
     * If modifiers are specified in the type options (e.g., ["ctrl", "shift"]), they will
     * be held down while typing the text. The method creates a new {@link Region}
     * at the current focus point for the typing operation.
     * </p>
     * <p>
     * In mock mode, the operation is logged but not performed.
     * </p>
     * 
     * @param stateString Contains the text to be typed. Must not be null.
     * @param typeOptions Configuration that may contain keyboard modifiers and type delay.
     * @return {@code true} if the typing operation was successful (or mocked),
     *         {@code false} if the type operation failed in real mode.
     */
    public boolean type(StateString stateString, TypeOptions typeOptions) {
        if (FrameworkSettings.mock) {
            return mockType(stateString, typeOptions);
        }
        
        // Convert modifiers list to string for Sikuli
        String modifierString = typeOptions.getModifiers().isEmpty() ? "" : 
            String.join("+", typeOptions.getModifiers());
        
        Region region = new Region();
        if (modifierString.isEmpty()) {
            return region.sikuli().type(stateString.getString()) == 1;
        } else {
            return region.sikuli().type(stateString.getString(), modifierString) == 1;
        }
    }
    
    private boolean mockType(StateString stateString, TypeOptions typeOptions) {
        String modifierString = typeOptions.getModifiers().isEmpty() ? "" : 
            String.join("+", typeOptions.getModifiers());
        
        if (modifierString.isEmpty()) {
            System.out.println("Mock: type '" + stateString.getString() + "'");
        } else {
            System.out.println("Mock: type '" + stateString.getString() + "' with modifiers: " + modifierString);
        }
        return true;
    }
    
    /**
     * Legacy method that types text using ActionOptions.
     * <p>
     * This method is provided for backward compatibility during migration.
     * It delegates to the original TypeTextWrapper implementation.
     * </p>
     * 
     * @param stateString The text to type
     * @param actionOptions The legacy action options
     * @return true if successful
     * @deprecated Use {@link #type(StateString, TypeOptions)} instead
     */
    @Deprecated
    public boolean type(StateString stateString, ActionOptions actionOptions) {
        return legacyWrapper.type(stateString, actionOptions);
    }
}