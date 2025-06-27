package io.github.jspinak.brobot.action.internal.mouse;

import lombok.Getter;
import org.springframework.stereotype.Component;

import static io.github.jspinak.brobot.action.internal.mouse.ClickType.Type.*;

import java.awt.event.InputEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps different mouse click types to their corresponding Java AWT button constants.
 * <p>
 * This utility class provides a translation layer between Brobot's click type
 * enumeration and the Java AWT {@link InputEvent} button constants that Sikuli
 * expects. The mapping handles both single and double clicks for all three
 * mouse buttons (left, middle, right).
 * <p>
 * Note that double clicks use the same button constants as single clicks;
 * the distinction between single and double clicks is handled by the calling
 * code through the number of click repetitions.
 * 
 * @see InputEvent#BUTTON1_DOWN_MASK
 * @see InputEvent#BUTTON2_DOWN_MASK
 * @see InputEvent#BUTTON3_DOWN_MASK
 */
@Component
@Getter
public class ClickType {

    /**
     * Enumerates all supported mouse click types.
     * <p>
     * The enum includes both single and double click variants for each mouse button:
     * <ul>
     * <li>LEFT/DOUBLE_LEFT - Primary mouse button (typically left button)</li>
     * <li>MIDDLE/DOUBLE_MIDDLE - Middle mouse button (scroll wheel click)</li>
     * <li>RIGHT/DOUBLE_RIGHT - Secondary mouse button (typically right button)</li>
     * </ul>
     */
    public enum Type {
        LEFT, RIGHT, MIDDLE, DOUBLE_LEFT, DOUBLE_RIGHT, DOUBLE_MIDDLE
    }

    private Map<Type, Integer> typeToSikuliButton = new HashMap<>();
    {
        typeToSikuliButton.put(LEFT, InputEvent.BUTTON1_DOWN_MASK);
        typeToSikuliButton.put(MIDDLE, InputEvent.BUTTON2_DOWN_MASK);
        typeToSikuliButton.put(RIGHT, InputEvent.BUTTON3_DOWN_MASK);
        typeToSikuliButton.put(DOUBLE_LEFT, InputEvent.BUTTON1_DOWN_MASK);
        typeToSikuliButton.put(DOUBLE_MIDDLE, InputEvent.BUTTON2_DOWN_MASK);
        typeToSikuliButton.put(DOUBLE_RIGHT, InputEvent.BUTTON3_DOWN_MASK);
    }
}
