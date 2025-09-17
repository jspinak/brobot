package io.github.jspinak.brobot.action.basic.type;

import java.util.List;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.internal.text.KeyDownWrapper;
import io.github.jspinak.brobot.model.state.StateString;
import io.github.jspinak.brobot.tools.testing.wrapper.TimeWrapper;

/**
 * Presses and holds keyboard keys in the Brobot model-based GUI automation framework.
 *
 * <p>KeyDown is a low-level keyboard action in the Action Model (α) that initiates key presses
 * without releasing them. This action is essential for implementing keyboard shortcuts, modifier
 * key combinations, and sustained key presses required by certain applications or accessibility
 * features.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li><b>Multi-key Support</b>: Can press multiple keys in sequence from a single
 *       ObjectCollection
 *   <li><b>Modifier Integration</b>: Special handling for modifier keys (CTRL, SHIFT, ALT) that are
 *       pressed before other keys
 *   <li><b>State Persistence</b>: Keys remain pressed until a corresponding KeyUp action
 *   <li><b>Timing Control</b>: Configurable pauses between individual key presses
 * </ul>
 *
 * <p>Processing behavior:
 *
 * <ul>
 *   <li>Uses only the first ObjectCollection provided
 *   <li>Processes multiple StateString objects within the collection sequentially
 *   <li>Modifier keys from ActionConfig are applied to each key press
 * </ul>
 *
 * <p>Common use cases:
 *
 * <ul>
 *   <li>Creating keyboard shortcuts (e.g., Ctrl+C, Alt+Tab)
 *   <li>Holding keys for gaming or specialized applications
 *   <li>Accessibility features requiring sustained key presses
 *   <li>Complex key combinations for advanced GUI operations
 * </ul>
 *
 * <p>Important considerations:
 *
 * <ul>
 *   <li>Always pair with KeyUp to avoid leaving keys in a pressed state
 *   <li>Some applications may have different behaviors for sustained key presses
 *   <li>Platform-specific key handling may affect certain key combinations
 *   <li>Modifier keys should typically be released in reverse order of pressing
 * </ul>
 *
 * <p>In the model-based approach, KeyDown enables the decomposition of complex keyboard
 * interactions into atomic operations. This granular control is essential for accurately modeling
 * keyboard-based GUI interactions and ensuring reliable automation across different platforms and
 * applications.
 *
 * @since 1.0
 * @see KeyUp
 * @see TypeText
 * @see StateString
 * @see KeyDownOptions
 */
@Component
public class KeyDown implements ActionInterface {

    private final KeyDownWrapper keyDownWrapper;
    private final TimeWrapper timeWrapper;

    public KeyDown(KeyDownWrapper keyDownWrapper, TimeWrapper timeWrapper) {
        this.keyDownWrapper = keyDownWrapper;
        this.timeWrapper = timeWrapper;
    }

    @Override
    public Type getActionType() {
        return Type.KEY_DOWN;
    }

    @Override
    public void perform(ActionResult matches, ObjectCollection... objectCollections) {
        // Get the configuration - expecting KeyDownOptions
        if (!(matches.getActionConfig() instanceof KeyDownOptions)) {
            throw new IllegalArgumentException("KeyDown requires KeyDownOptions configuration");
        }
        KeyDownOptions options = (KeyDownOptions) matches.getActionConfig();

        if (objectCollections == null || objectCollections.length == 0) return;

        // Uses the first objectCollection, which can have multiple keys
        List<StateString> strings = objectCollections[0].getStateStrings();
        for (int i = 0; i < strings.size(); i++) {
            StateString str = strings.get(i);
            // Convert List<String> modifiers to single String for legacy API
            String modifierString =
                    options.getModifiers().isEmpty()
                            ? ""
                            : String.join("+", options.getModifiers());
            keyDownWrapper.press(str.getString(), modifierString);

            // Pause between keys (except after the last one)
            if (i < strings.size() - 1) {
                timeWrapper.wait(options.getPauseBetweenKeys());
            }
        }
    }
}
