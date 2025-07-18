/**
 * State transition execution and orchestration.
 * 
 * <p>This package implements the transition execution layer of Brobot's navigation system.
 * It manages the complex process of changing GUI states, including action execution,
 * cascading state activation, hidden state management, and high-level navigation
 * orchestration. This is where abstract state transitions become concrete GUI
 * interactions.</p>
 * 
 * <h2>Core Components</h2>
 * 
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.navigation.transition.TransitionExecutor} - 
 *       Orchestrates complex state transitions with cascading effects</li>
 *   <li>{@link io.github.jspinak.brobot.navigation.transition.StateNavigator} - 
 *       High-level API for intelligent navigation to target states</li>
 *   <li>{@link io.github.jspinak.brobot.navigation.transition.StateTransitions} - 
 *       Manages collections of transitions with filtering and access</li>
 *   <li>{@link io.github.jspinak.brobot.navigation.transition.StateTransitionsJointTable} - 
 *       Maps state relationships and transition possibilities</li>
 *   <li>{@link io.github.jspinak.brobot.navigation.transition.TransitionFetcher} - 
 *       Retrieves appropriate transitions for state pairs</li>
 * </ul>
 * 
 * <h2>Transition Types</h2>
 * 
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.navigation.transition.TaskSequenceStateTransition} - 
 *       Declarative transitions using action definitions</li>
 *   <li>{@link io.github.jspinak.brobot.navigation.transition.JavaStateTransition} - 
 *       Code-based transitions with custom logic</li>
 *   <li>{@link io.github.jspinak.brobot.navigation.transition.TransitionConditionPackager} - 
 *       Wraps transition conditions for evaluation</li>
 * </ul>
 * 
 * <h2>Transition Execution Model</h2>
 * 
 * <p>TransitionExecutor manages multi-phase execution:</p>
 * <ol>
 *   <li><b>Validation</b> - Verify source state and transition availability</li>
 *   <li><b>FromTransition</b> - Execute actions to leave source state</li>
 *   <li><b>State Discovery</b> - Determine all states to activate</li>
 *   <li><b>ToTransitions</b> - Execute recognition for target states</li>
 *   <li><b>Cascading</b> - Activate additional related states</li>
 *   <li><b>Cleanup</b> - Deactivate exited states</li>
 * </ol>
 * 
 * <h2>Navigation Orchestration</h2>
 * 
 * <p>StateNavigator provides intelligent pathfinding and execution:</p>
 * <ol>
 *   <li><b>Path Discovery</b> - Find all routes to target</li>
 *   <li><b>Path Selection</b> - Choose optimal path</li>
 *   <li><b>Path Execution</b> - Traverse selected path</li>
 *   <li><b>Failure Recovery</b> - Try alternative paths</li>
 *   <li><b>Progress Tracking</b> - Leverage partial success</li>
 * </ol>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>High-Level Navigation</h3>
 * <pre>{@code
 * StateNavigator navigator = context.getBean(StateNavigator.class);
 * 
 * // Navigate to target state - handles everything automatically
 * boolean success = navigator.goToState(targetState);
 * 
 * // Or navigate to state by name
 * success = navigator.goToState("CheckoutPage");
 * 
 * // Framework automatically:
 * // - Finds current position
 * // - Discovers paths
 * // - Executes transitions
 * // - Recovers from failures
 * }</pre>
 * 
 * <h3>Direct Transition Execution</h3>
 * <pre>{@code
 * TransitionExecutor executor = context.getBean(TransitionExecutor.class);
 * 
 * // Execute a specific transition
 * boolean success = executor.execute(fromStateId, toStateId);
 * 
 * // Handles:
 * // - FromTransition actions
 * // - ToTransition verification
 * // - Cascading state activation
 * // - Hidden state management
 * }</pre>
 * 
 * <h3>Transition Discovery</h3>
 * <pre>{@code
 * StateTransitions transitions = context.getBean(StateTransitions.class);
 * 
 * // Find all transitions from a state
 * Set<StateTransition> fromState = transitions.getTransitionsFrom(stateId);
 * 
 * // Find transitions between specific states
 * Optional<StateTransition> direct = transitions.getTransition(fromId, toId);
 * 
 * // Get all states reachable from current
 * Set<Long> reachable = transitions.getStatesWithTransitionsFrom(stateId);
 * }</pre>
 * 
 * <h2>Complex Transitions</h2>
 * 
 * <h3>Cascading States</h3>
 * <pre>{@code
 * // Define transition with cascading effects
 * StateTransition openDialog = new StateTransition.Builder()
 *     .setFromState(mainScreen)
 *     .setToState(dialogState)
 *     .addCascadingState(overlayState)  // Also activates
 *     .addCascadingState(buttonPanel)    // Multiple cascades
 *     .build();
 * }</pre>
 * 
 * <h3>Hidden State Management</h3>
 * <pre>{@code
 * // Transition that hides underlying state
 * StateTransition showModal = new StateTransition.Builder()
 *     .setFromState(mainScreen)
 *     .setToState(modalDialog)
 *     .setHidesState(mainScreen)  // Main screen hidden, not exited
 *     .build();
 * }</pre>
 * 
 * <h3>Custom Transition Logic</h3>
 * <pre>{@code
 * public class LoginTransition extends JavaStateTransition {
 *     @Override
 *     public boolean execute() {
 *         // Custom login sequence
 *         type.perform(usernameField, credentials.getUsername());
 *         type.perform(passwordField, credentials.getPassword());
 *         click.perform(loginButton);
 *         
 *         // Wait for dashboard
 *         return wait.perform(dashboardElement);
 *     }
 * }
 * }</pre>
 * 
 * <h2>Failure Handling</h2>
 * 
 * <p>The package provides sophisticated failure recovery:</p>
 * <ul>
 *   <li><b>Transition-Level Retry</b> - Individual transitions can retry</li>
 *   <li><b>Path-Level Recovery</b> - Navigator tries alternative paths</li>
 *   <li><b>Blacklisting</b> - Failed transitions avoided in future</li>
 *   <li><b>Partial Progress</b> - Successful steps aren't repeated</li>
 * </ul>
 * 
 * <h2>State Relationship Management</h2>
 * 
 * <p>StateTransitionsJointTable tracks complex relationships:</p>
 * <ul>
 *   <li>Which states can transition to others</li>
 *   <li>Multiple transitions between state pairs</li>
 *   <li>Cascading state dependencies</li>
 *   <li>Hidden state relationships</li>
 * </ul>
 * 
 * <h2>Design Principles</h2>
 * 
 * <ol>
 *   <li><b>Atomic Transitions</b> - Core transition succeeds or fails completely</li>
 *   <li><b>Graceful Degradation</b> - Cascades can fail without breaking core</li>
 *   <li><b>State Consistency</b> - Always maintains valid state configuration</li>
 *   <li><b>Recovery Oriented</b> - Built for failure recovery from ground up</li>
 *   <li><b>Extensibility</b> - Easy to add custom transition types</li>
 * </ol>
 * 
 * <h2>Integration Points</h2>
 * 
 * <p>Transition components integrate with:</p>
 * <ul>
 *   <li>PathFinder for route discovery</li>
 *   <li>Actions for GUI interactions</li>
 *   <li>StateMemory for active state tracking</li>
 *   <li>Logging for transition history</li>
 * </ul>
 * 
 * @since 1.0
 * @see io.github.jspinak.brobot.navigation.path
 * @see io.github.jspinak.brobot.model.transition
 * @see io.github.jspinak.brobot.action
 * @see io.github.jspinak.brobot.statemanagement
 */
package io.github.jspinak.brobot.navigation.transition;