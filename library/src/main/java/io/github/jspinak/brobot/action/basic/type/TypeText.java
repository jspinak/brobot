package io.github.jspinak.brobot.action.basic.type;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.internal.text.TypeTextWrapper;
import io.github.jspinak.brobot.model.state.StateString;
import io.github.jspinak.brobot.tools.testing.mock.time.TimeProvider;

import org.sikuli.basics.Settings;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Types text to the window in focus in the Brobot model-based GUI automation framework.
 * 
 * <p>TypeText is a fundamental action in the Action Model (α) that enables keyboard input 
 * simulation. It bridges the gap between high-level text input requirements and low-level 
 * keyboard event generation, providing a reliable way to enter text into GUI applications 
 * regardless of their underlying technology.</p>
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li><b>Focus-based Input</b>: Types to whatever window or field currently has focus, 
 *       following standard GUI interaction patterns</li>
 *   <li><b>Configurable Timing</b>: Supports custom delays between keystrokes to accommodate 
 *       different application response times</li>
 *   <li><b>Batch Processing</b>: Can type multiple strings in sequence with configurable 
 *       pauses between them</li>
 *   <li><b>State-aware</b>: Works with StateString objects that maintain context about 
 *       their owning states</li>
 * </ul>
 * 
 * <p>Common use cases:</p>
 * <ul>
 *   <li>Filling form fields during automated testing</li>
 *   <li>Entering search queries or commands</li>
 *   <li>Providing credentials during login sequences</li>
 *   <li>Interacting with text-based interfaces or terminals</li>
 * </ul>
 * 
 * <p>The type delay mechanism is particularly important for handling applications that 
 * process keystrokes asynchronously or have input validation that runs between keystrokes. 
 * By adjusting the type delay through ActionOptions, automation scripts can adapt to 
 * different application behaviors without modifying the core logic.</p>
 * 
 * <p>This action exemplifies the framework's approach to abstracting platform-specific 
 * details while providing fine-grained control when needed. The underlying implementation 
 * delegates to platform-specific wrappers while maintaining a consistent interface.</p>
 * 
 * @since 1.0
 * @see StateString
 * @see ActionOptions
 * @see TypeTextWrapper
 * @see Click
 */
@Component
public class TypeText implements ActionInterface {

    private final TypeTextWrapper typeTextWrapper;
    private final TimeProvider time;

    public TypeText(TypeTextWrapper typeTextWrapper, TimeProvider time) {
        this.typeTextWrapper = typeTextWrapper;
        this.time = time;
    }

    public void perform(ActionResult matches, ObjectCollection... objectCollections) {
        ActionOptions actionOptions = matches.getActionOptions();
        double defaultTypeDelay = Settings.TypeDelay;
        Settings.TypeDelay = actionOptions.getTypeDelay();
        List<StateString> strings = objectCollections[0].getStateStrings();
        for (StateString str : strings) {
            typeTextWrapper.type(str, actionOptions);
            if (strings.indexOf(str) < strings.size() - 1)
                time.wait(actionOptions.getPauseBetweenIndividualActions());
        }
        Settings.TypeDelay = defaultTypeDelay;
    }

}
