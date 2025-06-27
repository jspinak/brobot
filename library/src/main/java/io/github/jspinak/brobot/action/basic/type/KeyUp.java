package io.github.jspinak.brobot.action.basic.type;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.internal.text.KeyUpWrapper;
import io.github.jspinak.brobot.model.state.StateString;
import org.springframework.stereotype.Component;

/**
 * Releases previously pressed keyboard keys in the Brobot model-based GUI automation framework.
 * 
 * <p>KeyUp is a low-level keyboard action in the Action Model (α) that releases keys 
 * previously pressed by KeyDown actions. It completes keyboard interactions by ensuring 
 * proper key state management and preventing keys from remaining in a pressed state, 
 * which could interfere with subsequent automation or user interactions.</p>
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li><b>Selective Release</b>: Can release specific keys or all pressed keys</li>
 *   <li><b>Modifier Handling</b>: Special handling ensures modifier keys (CTRL, SHIFT, ALT) 
 *       are released last to maintain proper key combination semantics</li>
 *   <li><b>Batch Processing</b>: Releases multiple keys from a single ObjectCollection</li>
 *   <li><b>Safety Mechanism</b>: Can release all keys when no specific keys are provided</li>
 * </ul>
 * 
 * <p>Release behavior:</p>
 * <ul>
 *   <li>If no keys are specified: Releases all currently pressed keys</li>
 *   <li>If keys are specified: Releases each key in the ObjectCollection sequentially</li>
 *   <li>Modifier keys from ActionOptions are always released last</li>
 *   <li>Uses only the first ObjectCollection provided</li>
 * </ul>
 * 
 * <p>Common use cases:</p>
 * <ul>
 *   <li>Completing keyboard shortcuts after KeyDown (e.g., releasing Ctrl+C)</li>
 *   <li>Ending sustained key presses for gaming or specialized applications</li>
 *   <li>Cleaning up keyboard state after complex key combinations</li>
 *   <li>Ensuring proper state before subsequent keyboard actions</li>
 * </ul>
 * 
 * <p>Important considerations:</p>
 * <ul>
 *   <li>Must be paired with preceding KeyDown actions for proper key management</li>
 *   <li>Releasing an already-released key is typically harmless</li>
 *   <li>The order of key releases can be important for certain applications</li>
 *   <li>Platform-specific behaviors may affect key release timing</li>
 * </ul>
 * 
 * <p>In the model-based approach, KeyUp ensures that the framework maintains accurate 
 * keyboard state throughout automation execution. This state management is crucial for 
 * preventing interference between actions and ensuring that the GUI behaves predictably 
 * in response to keyboard inputs.</p>
 * 
 * @since 1.0
 * @see KeyDown
 * @see TypeText
 * @see StateString
 * @see ActionOptions
 */
@Component
public class KeyUp implements ActionInterface {

    private final KeyUpWrapper keyUpWrapper;

    public KeyUp(KeyUpWrapper keyUpWrapper) {
        this.keyUpWrapper = keyUpWrapper;
    }

    public void perform(ActionResult matches, ObjectCollection... objectCollections) {
        ActionOptions actionOptions = matches.getActionOptions();
        if (nothingToRelease(actionOptions, objectCollections)) keyUpWrapper.release(); // releases all keys
        else {
            for (StateString stateString : objectCollections[0].getStateStrings()) {
                keyUpWrapper.release(stateString.getString());
            }
            if (!actionOptions.getModifiers().isEmpty()) keyUpWrapper.release(actionOptions.getModifiers());
        }
    }

    private boolean nothingToRelease(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        if (objectCollections == null) return true;
        return objectCollections[0].getStateStrings().isEmpty() &&
                actionOptions.getModifiers().isEmpty();
    }

}
