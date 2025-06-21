package io.github.jspinak.brobot.automationScripts;

import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.manageStates.StateTransitions;

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
 * @see ContinuousAutomation
 */
public interface StateHandler {
    boolean handleState(State currentState, StateTransitions stateTransitions);
    void onNoTransitionFound();
}
