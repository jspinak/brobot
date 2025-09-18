package io.github.jspinak.brobot.action.basic.type;

import java.util.List;

import org.sikuli.basics.Settings;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.Click;
import io.github.jspinak.brobot.action.internal.text.TextTyper;
import io.github.jspinak.brobot.model.state.StateString;
import io.github.jspinak.brobot.tools.testing.wrapper.TimeWrapper;

/**
 * Types text to the window in focus in the Brobot model-based GUI automation framework.
 *
 * <p>TypeText is a fundamental action in the Action Model (α) that enables keyboard input
 * simulation. It bridges the gap between high-level text input requirements and low-level keyboard
 * event generation, providing a reliable way to enter text into GUI applications regardless of
 * their underlying technology.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li><b>Focus-based Input</b>: Types to whatever window or field currently has focus, following
 *       standard GUI interaction patterns
 *   <li><b>Configurable Timing</b>: Supports custom delays between keystrokes to accommodate
 *       different application response times
 *   <li><b>Batch Processing</b>: Can type multiple strings in sequence with configurable pauses
 *       between them
 *   <li><b>State-aware</b>: Works with StateString objects that maintain context about their owning
 *       states
 * </ul>
 *
 * <p>Common use cases:
 *
 * <ul>
 *   <li>Filling form fields during automated testing
 *   <li>Entering search queries or commands
 *   <li>Providing credentials during login sequences
 *   <li>Interacting with text-based interfaces or terminals
 * </ul>
 *
 * <p>The type delay mechanism is particularly important for handling applications that process
 * keystrokes asynchronously or have input validation that runs between keystrokes. By adjusting the
 * type delay through ActionConfig, automation scripts can adapt to different application behaviors
 * without modifying the core logic.
 *
 * <p>This action exemplifies the framework's approach to abstracting platform-specific details
 * while providing fine-grained control when needed. The underlying implementation delegates to
 * platform-specific wrappers while maintaining a consistent interface.
 *
 * @since 1.0
 * @see StateString
 * @see TypeOptions
 * @see TextTyper
 * @see Click
 */
@Component
public class TypeText implements ActionInterface {

    @Override
    public Type getActionType() {
        return Type.TYPE;
    }

    private final TextTyper textTyper;
    private final TimeWrapper timeWrapper;

    public TypeText(TextTyper textTyper, TimeWrapper timeWrapper) {
        this.textTyper = textTyper;
        this.timeWrapper = timeWrapper;
    }

    @Override
    public void perform(ActionResult matches, ObjectCollection... objectCollections) {
        // Get the configuration - expecting TypeOptions
        if (!(matches.getActionConfig() instanceof TypeOptions)) {
            throw new IllegalArgumentException("TypeText requires TypeOptions configuration");
        }
        TypeOptions typeOptions = (TypeOptions) matches.getActionConfig();

        // Save and set the type delay
        double defaultTypeDelay = Settings.TypeDelay;
        Settings.TypeDelay = typeOptions.getTypeDelay();

        List<StateString> strings = objectCollections[0].getStateStrings();
        if (strings == null || strings.isEmpty()) {
            // Nothing to type, restore settings and return
            Settings.TypeDelay = defaultTypeDelay;
            return;
        }

        for (int i = 0; i < strings.size(); i++) {
            StateString str = strings.get(i);
            textTyper.type(str, typeOptions);

            // Pause between typing different strings (except after the last one)
            if (i < strings.size() - 1 && typeOptions.getPauseAfterEnd() > 0) {
                timeWrapper.wait(typeOptions.getPauseAfterEnd());
            }
        }

        // Restore the default type delay
        Settings.TypeDelay = defaultTypeDelay;
    }
}
