package io.github.jspinak.brobot.tools.testing.mock.state;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.tools.testing.mock.MockStatus;

/**
 * Manages state probabilities for controlled testing scenarios in the Brobot framework.
 *
 * <p>This class provides mechanisms to manipulate state existence probabilities during mock
 * testing, enabling deterministic test scenarios where state availability can be precisely
 * controlled. By setting specific probabilities, tests can simulate various application conditions
 * including normal operation, error states, and edge cases.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Dynamic probability adjustment - Modify state probabilities at runtime
 *   <li>Batch operations - Set probabilities for multiple states simultaneously
 *   <li>Test scenario control - Create specific test conditions by controlling state availability
 *   <li>Integration with StateService - Works directly with the project's state registry
 * </ul>
 *
 * <p>Common use cases:
 *
 * <ul>
 *   <li>Simulating unavailable states (probability = 0)
 *   <li>Testing probabilistic state detection (0 < probability < 100)
 *   <li>Ensuring states are always found (probability = 100)
 *   <li>Creating failure scenarios for error handling tests
 *   <li>Testing state transition logic under various conditions
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * // Make login state always visible
 * mockStateManagement.setStateProbabilities(100, "LoginPage");
 *
 * // Simulate intermittent state detection
 * mockStateManagement.setStateProbabilities(70, "Dashboard", "Settings");
 *
 * // Make error states unavailable
 * mockStateManagement.setStateProbabilities(0, "ErrorDialog", "CrashScreen");
 * }</pre>
 *
 * <p>Thread safety: This component is not thread-safe. It should be used in single-threaded test
 * scenarios or with external synchronization.
 *
 * @see StateService
 * @see io.github.jspinak.brobot.model.state.State
 * @see MockStatus
 */
@Component
public class MockStateManagement {

    private final StateService allStatesInProjectService;

    public MockStateManagement(StateService allStatesInProjectService) {
        this.allStatesInProjectService = allStatesInProjectService;
    }

    /**
     * Sets the existence probability for one or more states in the testing environment.
     *
     * <p>This method allows tests to control which states are detected during mock execution by
     * adjusting their probability of existence. The probability affects whether mock find
     * operations will successfully locate objects belonging to these states.
     *
     * <p>The method silently ignores state names that don't exist in the project, allowing tests to
     * be resilient to state name changes or optional states.
     *
     * @param probability The probability of state existence (0-100). 0 means the state will never
     *     be found, 100 means the state will always be found, values in between create
     *     probabilistic detection.
     * @param stateNames Variable number of state names to update. States are identified by their
     *     unique name in the project.
     */
    public void setStateProbabilities(int probability, String... stateNames) {
        for (String stateName : stateNames) {
            allStatesInProjectService
                    .getState(stateName)
                    .ifPresent(
                            state -> {
                                state.setMockFindStochasticModifier(probability);
                            });
        }
    }
}
