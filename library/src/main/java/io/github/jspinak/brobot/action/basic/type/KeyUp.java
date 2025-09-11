package io.github.jspinak.brobot.action.basic.type;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.internal.text.KeyUpWrapper;
import io.github.jspinak.brobot.model.state.StateString;

/**
 * Releases previously pressed keyboard keys in the Brobot model-based GUI automation framework.
 *
 * <p>KeyUp is a low-level keyboard action in the Action Model (α) that releases keys previously
 * pressed by KeyDown actions. It completes keyboard interactions by ensuring proper key state
 * management and preventing keys from remaining in a pressed state, which could interfere with
 * subsequent automation or user interactions.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li><b>Selective Release</b>: Can release specific keys or all pressed keys
 *   <li><b>Modifier Handling</b>: Special handling ensures modifier keys (CTRL, SHIFT, ALT) are
 *       released last to maintain proper key combination semantics
 *   <li><b>Batch Processing</b>: Releases multiple keys from a single ObjectCollection
 *   <li><b>Safety Mechanism</b>: Can release all keys when no specific keys are provided
 * </ul>
 *
 * <p>Release behavior:
 *
 * <ul>
 *   <li>If no keys are specified: Releases all currently pressed keys
 *   <li>If keys are specified: Releases each key in the ObjectCollection sequentially
 *   <li>Modifier keys from ActionOptions are always released last
 *   <li>Uses only the first ObjectCollection provided
 * </ul>
 *
 * <p>Common use cases:
 *
 * <ul>
 *   <li>Completing keyboard shortcuts after KeyDown (e.g., releasing Ctrl+C)
 *   <li>Ending sustained key presses for gaming or specialized applications
 *   <li>Cleaning up keyboard state after complex key combinations
 *   <li>Ensuring proper state before subsequent keyboard actions
 * </ul>
 *
 * <p>Important considerations:
 *
 * <ul>
 *   <li>Must be paired with preceding KeyDown actions for proper key management
 *   <li>Releasing an already-released key is typically harmless
 *   <li>The order of key releases can be important for certain applications
 *   <li>Platform-specific behaviors may affect key release timing
 * </ul>
 *
 * <p>In the model-based approach, KeyUp ensures that the framework maintains accurate keyboard
 * state throughout automation execution. This state management is crucial for preventing
 * interference between actions and ensuring that the GUI behaves predictably in response to
 * keyboard inputs.
 *
 * @since 1.0
 * @see KeyDown
 * @see TypeText
 * @see StateString
 * @see KeyUpOptions
 */
@Component
public class KeyUp implements ActionInterface {

    private final KeyUpWrapper keyUpWrapper;

    public KeyUp(KeyUpWrapper keyUpWrapper) {
        this.keyUpWrapper = keyUpWrapper;
    }

    @Override
    public Type getActionType() {
        return Type.KEY_UP;
    }

    @Override
    public void perform(ActionResult matches, ObjectCollection... objectCollections) {
        // Get the configuration - expecting KeyUpOptions
        if (!(matches.getActionConfig() instanceof KeyUpOptions)) {
            throw new IllegalArgumentException("KeyUp requires KeyUpOptions configuration");
        }
        KeyUpOptions options = (KeyUpOptions) matches.getActionConfig();

        if (nothingToRelease(options, objectCollections)) {
            keyUpWrapper.release(); // releases all keys
        } else {
            // Release specific keys from the collection
            if (objectCollections != null && objectCollections.length > 0) {
                for (StateString stateString : objectCollections[0].getStateStrings()) {
                    keyUpWrapper.release(stateString.getString());
                }
            }

            // Release modifier keys last
            if (!options.getModifiers().isEmpty()) {
                String modifierString = String.join("+", options.getModifiers());
                keyUpWrapper.release(modifierString);
            }
        }
    }

    private boolean nothingToRelease(KeyUpOptions options, ObjectCollection... objectCollections) {
        if (objectCollections == null || objectCollections.length == 0) return true;
        return objectCollections[0].getStateStrings().isEmpty() && options.getModifiers().isEmpty();
    }
}
