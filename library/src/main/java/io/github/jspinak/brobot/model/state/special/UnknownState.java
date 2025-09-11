package io.github.jspinak.brobot.model.state.special;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateEnum;
import io.github.jspinak.brobot.navigation.service.StateService;

import lombok.Getter;

/**
 * Represents the initial uncertain state in Brobot's state management system.
 *
 * <p>UnknownState serves as the universal entry point and recovery mechanism in the model-based
 * automation framework. When Brobot starts or loses track of the current application state, it
 * begins in the Unknown state. This state acts as a safety net, ensuring the automation can always
 * recover from unexpected situations and navigate back to known states.
 *
 * <p>Key characteristics:
 *
 * <ul>
 *   <li><b>Universal Entry</b>: Default starting state for all automations
 *   <li><b>Recovery Point</b>: Fallback when state detection fails
 *   <li><b>No Prerequisites</b>: Always accessible regardless of application state
 *   <li><b>Transition Hub</b>: Should have paths to major application states
 *   <li><b>Error Resilient</b>: Handles unexpected application conditions
 * </ul>
 *
 * <p>Common scenarios leading to Unknown state:
 *
 * <ul>
 *   <li>Initial automation startup
 *   <li>Application crash or restart
 *   <li>Unexpected popups or dialogs
 *   <li>Network errors disrupting navigation
 *   <li>State detection confidence below threshold
 *   <li>Manual intervention during automation
 * </ul>
 *
 * <p>Best practices for Unknown state transitions:
 *
 * <ul>
 *   <li>Include paths to main menu or home screens
 *   <li>Add error dismissal actions (close dialogs, alerts)
 *   <li>Implement application restart procedures
 *   <li>Use robust visual patterns that work across contexts
 *   <li>Consider multiple recovery strategies
 * </ul>
 *
 * <p>Example recovery strategies:
 *
 * <ul>
 *   <li>ESC key to close potential dialogs
 *   <li>Alt+F4 to close unknown windows
 *   <li>Click on application icon to ensure focus
 *   <li>Navigate to known URL or home screen
 *   <li>Use keyboard shortcuts to reach main menu
 * </ul>
 *
 * <p>Integration features:
 *
 * <ul>
 *   <li>Automatically registered with state management system
 *   <li>Special enum for type-safe references
 *   <li>Singleton pattern ensures single instance
 *   <li>No visual patterns required (always accessible)
 * </ul>
 *
 * <p>In the model-based approach (State Structure Ω), the Unknown state represents the absence of
 * knowledge about the current GUI state. It's the complement to all known states and ensures the
 * state space is complete. This completeness guarantee is crucial for robust automation that can
 * handle any situation, making the system truly resilient to unexpected conditions.
 *
 * @since 1.0
 * @see State
 * @see StateEnum
 * @see StateIdResolver
 * @see StateDetector
 * @see InitialStates
 */
@Getter
@Component
public class UnknownState {

    public enum Enum implements StateEnum {
        UNKNOWN
    }

    private State state = new State.Builder("unknown").build();

    public UnknownState(StateService allStatesInProjectService) {
        allStatesInProjectService.save(state);
    }
}
