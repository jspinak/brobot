package io.github.jspinak.brobot.action.basic.type;
import io.github.jspinak.brobot.action.ActionType;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.internal.text.KeyDownWrapper;
import io.github.jspinak.brobot.model.state.StateString;
import io.github.jspinak.brobot.tools.testing.mock.time.TimeProvider;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Presses and holds keyboard keys in the Brobot model-based GUI automation framework.
 * 
 * <p>KeyDown is a low-level keyboard action in the Action Model (α) that initiates key 
 * presses without releasing them. This action is essential for implementing keyboard 
 * shortcuts, modifier key combinations, and sustained key presses required by certain 
 * applications or accessibility features.</p>
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li><b>Multi-key Support</b>: Can press multiple keys in sequence from a single 
 *       ObjectCollection</li>
 *   <li><b>Modifier Integration</b>: Special handling for modifier keys (CTRL, SHIFT, ALT) 
 *       that are pressed before other keys</li>
 *   <li><b>State Persistence</b>: Keys remain pressed until a corresponding KeyUp action</li>
 *   <li><b>Timing Control</b>: Configurable pauses between individual key presses</li>
 * </ul>
 * 
 * <p>Processing behavior:</p>
 * <ul>
 *   <li>Uses only the first ObjectCollection provided</li>
 *   <li>Processes multiple StateString objects within the collection sequentially</li>
 *   <li>Modifier keys from ActionOptions are applied to each key press</li>
 * </ul>
 * 
 * <p>Common use cases:</p>
 * <ul>
 *   <li>Creating keyboard shortcuts (e.g., Ctrl+C, Alt+Tab)</li>
 *   <li>Holding keys for gaming or specialized applications</li>
 *   <li>Accessibility features requiring sustained key presses</li>
 *   <li>Complex key combinations for advanced GUI operations</li>
 * </ul>
 * 
 * <p>Important considerations:</p>
 * <ul>
 *   <li>Always pair with KeyUp to avoid leaving keys in a pressed state</li>
 *   <li>Some applications may have different behaviors for sustained key presses</li>
 *   <li>Platform-specific key handling may affect certain key combinations</li>
 *   <li>Modifier keys should typically be released in reverse order of pressing</li>
 * </ul>
 * 
 * <p>In the model-based approach, KeyDown enables the decomposition of complex keyboard 
 * interactions into atomic operations. This granular control is essential for accurately 
 * modeling keyboard-based GUI interactions and ensuring reliable automation across 
 * different platforms and applications.</p>
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
    private final TimeProvider time;

    public KeyDown(KeyDownWrapper keyDownWrapper, TimeProvider time) {
        this.keyDownWrapper = keyDownWrapper;
        this.time = time;
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
            String modifierString = options.getModifiers().isEmpty() ? "" : 
                String.join("+", options.getModifiers());
            keyDownWrapper.press(str.getString(), modifierString);
            
            // Pause between keys (except after the last one)
            if (i < strings.size() - 1) {
                time.wait(options.getPauseBetweenKeys());
            }
        }
    }

}
