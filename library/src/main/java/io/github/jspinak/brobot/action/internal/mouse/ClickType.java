package io.github.jspinak.brobot.action.internal.mouse;

import org.springframework.stereotype.Component;

import java.awt.event.InputEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps click types to Sikuli button constants.
 * <p>
 * This component provides the mapping between Brobot's button type enum
 * and the underlying Sikuli button constants used for mouse operations.
 * It serves as a bridge between the high-level action configuration and the
 * low-level mouse event generation.
 * </p>
 */
@Component
public class ClickType {

    /**
     * Enum representing mouse button types.
     * This is a temporary enum that will be replaced by MouseButton enum.
     */
    public enum Type {
        LEFT, RIGHT, MIDDLE, DOUBLE_LEFT, DOUBLE_RIGHT, DOUBLE_MIDDLE
    }

    private final Map<Type, Integer> typeToSikuliButton;

    public ClickType() {
        this.typeToSikuliButton = new HashMap<>();
        initializeButtonMapping();
    }

    private void initializeButtonMapping() {
        // Map Type to AWT InputEvent button masks
        typeToSikuliButton.put(Type.LEFT, InputEvent.BUTTON1_DOWN_MASK);
        typeToSikuliButton.put(Type.RIGHT, InputEvent.BUTTON3_DOWN_MASK);
        typeToSikuliButton.put(Type.MIDDLE, InputEvent.BUTTON2_DOWN_MASK);
        typeToSikuliButton.put(Type.DOUBLE_LEFT, InputEvent.BUTTON1_DOWN_MASK);
        typeToSikuliButton.put(Type.DOUBLE_RIGHT, InputEvent.BUTTON3_DOWN_MASK);
        typeToSikuliButton.put(Type.DOUBLE_MIDDLE, InputEvent.BUTTON2_DOWN_MASK);
    }

    /**
     * Gets the mapping from Type to Sikuli button constants.
     * 
     * @return Map of Type to button mask integers
     */
    public Map<Type, Integer> getTypeToSikuliButton() {
        return typeToSikuliButton;
    }
}