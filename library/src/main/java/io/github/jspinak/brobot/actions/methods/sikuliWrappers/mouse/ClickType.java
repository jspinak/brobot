package io.github.jspinak.brobot.actions.methods.sikuliWrappers.mouse;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.awt.event.InputEvent;
import java.util.HashMap;
import java.util.Map;

import static io.github.jspinak.brobot.actions.methods.sikuliWrappers.mouse.ClickType.Type.*;

/**
 * Converts ClickType to a Sikuli Button.
 */
@Component
@Getter
public class ClickType {

    public enum Type {
        LEFT, RIGHT, MIDDLE, DOUBLE_LEFT, DOUBLE_RIGHT, DOUBLE_MIDDLE
    }

    private Map<Type, Integer> typeToSikuliButton = new HashMap<>();
    {
        typeToSikuliButton.put(LEFT, InputEvent.BUTTON1_DOWN_MASK);
        typeToSikuliButton.put(MIDDLE, InputEvent.BUTTON2_DOWN_MASK);
        typeToSikuliButton.put(RIGHT, InputEvent.BUTTON3_DOWN_MASK);
    }
}
