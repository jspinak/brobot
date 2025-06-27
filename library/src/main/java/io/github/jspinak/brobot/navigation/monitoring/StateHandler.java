package io.github.jspinak.brobot.navigation.monitoring;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;

/**
 * Interface for implementing custom state handling logic in automation scripts.
 * 
 * <p>StateHandler defines the contract for processing states and their transitions during 
 * automation execution. It serves as the bridge between the automation framework's state 
 * discovery mechanisms and user-defined business logic, enabling customizable responses 
 * to different GUI states.</p>
 * 
 * <p>Key responsibilities:
 * <ul>
 *   <li><b>State Processing</b>: Decide how to handle each active state encountered</li>
 *   <li><b>Transition Selection</b>: Choose which transitions to execute from available options</li>
 *   <li><b>Error Handling</b>: Define behavior when no transitions are available</li>
 *   <li><b>Flow Control</b>: Return success/failure to influence automation continuation</li>
 * </ul>
 * </p>
 * 
 * <p>Method contracts:
 * <ul>
 *   <li>{@code handleState()}: Process a state with its available transitions
 *       <ul>
 *         <li>Returns true to indicate successful handling</li>
 *         <li>Returns false to signal issues (logged but doesn't stop automation)</li>
 *         <li>Can execute zero, one, or multiple transitions</li>
 *       </ul>
 *   </li>
 *   <li>{@code onNoTransitionFound()}: Called when active states have no transitions
 *       <ul>
 *         <li>Can log warnings, wait, or take corrective action</li>
 *         <li>Useful for handling rest states or error conditions</li>
 *       </ul>
 *   </li>
 * </ul>
 * </p>
 * 
 * <p>Common implementation patterns:
 * <ul>
 *   <li><b>Sequential</b>: Execute first available transition</li>
 *   <li><b>Conditional</b>: Choose transitions based on application state</li>
 *   <li><b>Prioritized</b>: Select transitions by score or preference</li>
 *   <li><b>Random</b>: Choose randomly for testing or exploration</li>
 *   <li><b>Learning</b>: Select based on historical success rates</li>
 * </ul>
 * </p>
 * 
 * <p>Integration points:
 * <ul>
 *   <li>Used by BaseAutomation and its subclasses</li>
 *   <li>Called during automation loops after state discovery</li>
 *   <li>Can access application context for decision making</li>
 *   <li>May integrate with external systems or APIs</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, StateHandler embodies the strategy pattern for state 
 * processing. It allows separation of the mechanical aspects of state discovery and 
 * transition execution from the business logic of how to respond to different application 
 * states, making automation scripts more modular and testable.</p>
 * 
 * @since 1.0
 * @see State
 * @see StateTransitions
 * @see BaseAutomation
 * @see MonitoringService
 */
public interface StateHandler {
    /**
     * Processes the current state and executes appropriate transitions.
     * <p>
     * Called when an active state is found that has defined transitions.
     * Implementations should analyze the state and available transitions,
     * then decide which actions to take. The return value indicates
     * whether the state was handled successfully.
     * <p>
     * Implementation considerations:
     * <ul>
     *   <li>May execute zero, one, or multiple transitions</li>
     *   <li>Should handle null or empty transition lists gracefully</li>
     *   <li>Can access external context for decision making</li>
     *   <li>Should log significant decisions or errors</li>
     * </ul>
     *
     * @param currentState The active state to process
     * @param stateTransitions Available transitions from this state
     * @return true if state was handled successfully, false if issues occurred
     */
    boolean handleState(State currentState, StateTransitions stateTransitions);
    
    /**
     * Handles the scenario when no active state with transitions is found.
     * <p>
     * Called during automation loops when state discovery finds active
     * states but none have defined transitions. This often indicates
     * the automation is in an unexpected state or a rest state.
     * <p>
     * Common implementations:
     * <ul>
     *   <li>Log a warning and continue searching</li>
     *   <li>Wait briefly for states to stabilize</li>
     *   <li>Attempt recovery by navigating to known states</li>
     *   <li>Trigger state discovery to find new states</li>
     * </ul>
     */
    void onNoTransitionFound();
}