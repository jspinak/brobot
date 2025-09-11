/**
 * State transition definitions and transition management.
 *
 * <p>This package implements the transition component T of Brobot's formal model <b>Ω = (E, S,
 * T)</b>. Transitions define the edges in the state graph, specifying how the application moves
 * between different states through user interactions or system events.
 *
 * <h2>Core Components</h2>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.model.transition.StateTransition} - Defines connections
 *       between states with activation conditions
 *   <li>{@link io.github.jspinak.brobot.model.transition.TransitionFunction} - Encapsulates the
 *       logic for executing state transitions
 *   <li>{@link io.github.jspinak.brobot.model.transition.TransitionMetadata} - Properties and
 *       constraints for transitions
 * </ul>
 *
 * <h2>Transition Model</h2>
 *
 * <p>A StateTransition represents a directed edge in the state graph:
 *
 * <ul>
 *   <li><b>From State</b> - The source state where transition originates
 *   <li><b>To State</b> - The target state after transition completes
 *   <li><b>Activation</b> - The element to interact with (click, type, etc.)
 *   <li><b>Verification</b> - Elements confirming successful transition
 *   <li><b>Function</b> - Custom logic for complex transitions
 * </ul>
 *
 * <h2>Basic Transitions</h2>
 *
 * <h3>Simple Click Transition</h3>
 *
 * <pre>{@code
 * StateTransition loginTransition = new StateTransition.Builder()
 *     .setFromState("LoginScreen")
 *     .setToState("Dashboard")
 *     .setTransitionImage(loginButton)  // Click this to transition
 *     .addToStateImage(dashboardLogo)   // Verify this appears
 *     .setScore(0.95)                   // Reliability score
 *     .build();
 * }</pre>
 *
 * <h3>Multi-Step Transition</h3>
 *
 * <pre>{@code
 * StateTransition formSubmit = new StateTransition.Builder()
 *     .setFromState("FormPage")
 *     .setToState("Confirmation")
 *     .setTransitionFunction(new TransitionFunction() {
 *         @Override
 *         public boolean execute() {
 *             type.perform(nameField, "John Doe");
 *             type.perform(emailField, "john@example.com");
 *             click.perform(submitButton);
 *             return wait.perform(confirmationMessage);
 *         }
 *     })
 *     .build();
 * }</pre>
 *
 * <h2>Transition Functions</h2>
 *
 * <p>Complex transitions use TransitionFunction for custom logic:
 *
 * <pre>{@code
 * public class LoginTransitionFunction implements TransitionFunction {
 *     private final String username;
 *     private final String password;
 *
 *     @Override
 *     public boolean execute() {
 *         // Type credentials
 *         if (!type.perform(usernameField, username)) return false;
 *         if (!type.perform(passwordField, password)) return false;
 *
 *         // Click login
 *         if (!click.perform(loginButton)) return false;
 *
 *         // Wait for dashboard
 *         return wait.perform(dashboardElement);
 *     }
 *
 *     @Override
 *     public boolean canExecute(State currentState) {
 *         return currentState.getName().equals("LoginScreen");
 *     }
 * }
 * }</pre>
 *
 * <h2>Transition Properties</h2>
 *
 * <h3>Activation Types</h3>
 *
 * <ul>
 *   <li><b>CLICK</b> - Click an element to trigger transition
 *   <li><b>TYPE</b> - Text input triggers transition
 *   <li><b>DRAG</b> - Drag operation causes transition
 *   <li><b>APPEAR</b> - Automatic when element appears
 *   <li><b>VANISH</b> - Automatic when element disappears
 *   <li><b>CUSTOM</b> - Defined by TransitionFunction
 * </ul>
 *
 * <h3>Timing Properties</h3>
 *
 * <pre>{@code
 * StateTransition timedTransition = new StateTransition.Builder()
 *     .setFromState("SplashScreen")
 *     .setToState("MainMenu")
 *     .setActivationType(ActivationType.APPEAR)
 *     .setPauseBeforeTransition(2.0)    // Wait 2 seconds
 *     .setPauseAfterTransition(0.5)     // Pause after complete
 *     .setMaxWaitTime(10.0)              // Timeout after 10 seconds
 *     .build();
 * }</pre>
 *
 * <h2>Transition Verification</h2>
 *
 * <p>Transitions can include verification steps:
 *
 * <pre>{@code
 * StateTransition verifiedTransition = new StateTransition.Builder()
 *     .setFromState("Cart")
 *     .setToState("Checkout")
 *     .setTransitionImage(checkoutButton)
 *     // Multiple verification points
 *     .addToStateImage(checkoutHeader)
 *     .addToStateImage(paymentSection)
 *     .addFinishStateImage(orderSummary)
 *     .setVerificationTimeout(5.0)
 *     .build();
 * }</pre>
 *
 * <h2>Conditional Transitions</h2>
 *
 * <p>Transitions can have conditions:
 *
 * <pre>{@code
 * StateTransition conditionalTransition = new StateTransition.Builder()
 *     .setFromState("ProductPage")
 *     .setToState("Cart")
 *     .setTransitionImage(addToCartButton)
 *     .setCondition(() -> inventory.isAvailable(productId))
 *     .setFallbackTransition(outOfStockTransition)
 *     .build();
 * }</pre>
 *
 * <h2>Transition Execution</h2>
 *
 * <p>The framework executes transitions automatically:
 *
 * <pre>{@code
 * // Manual execution
 * boolean success = transitionManager.executeTransition(loginTransition);
 *
 * // Automatic pathfinding
 * Path path = pathFinder.findPath("CurrentState", "TargetState");
 * for (StateTransition transition : path.getTransitions()) {
 *     transitionManager.executeTransition(transition);
 * }
 * }</pre>
 *
 * <h2>Error Handling</h2>
 *
 * <p>Transitions include error recovery:
 *
 * <pre>{@code
 * StateTransition robustTransition = new StateTransition.Builder()
 *     .setFromState("Form")
 *     .setToState("Success")
 *     .setTransitionImage(submitButton)
 *     .setMaxAttempts(3)
 *     .setRetryDelay(2.0)
 *     .setErrorHandler((attempt, error) -> {
 *         logger.warn("Transition attempt {} failed", attempt);
 *         clearForm();
 *         return attempt < 3;  // Retry if under 3 attempts
 *     })
 *     .build();
 * }</pre>
 *
 * <h2>Best Practices</h2>
 *
 * <ol>
 *   <li>Include verification elements to confirm successful transitions
 *   <li>Set appropriate timeouts based on application responsiveness
 *   <li>Use TransitionFunction for complex multi-step transitions
 *   <li>Document preconditions and postconditions
 *   <li>Test transitions under various system loads
 *   <li>Implement fallback transitions for error cases
 * </ol>
 *
 * <h2>Integration</h2>
 *
 * <p>Transitions integrate with other framework components:
 *
 * <ul>
 *   <li>State detection uses transitions to update current state
 *   <li>Path finding algorithms traverse transition graph
 *   <li>Action chains can trigger transitions implicitly
 *   <li>History tracking records transition execution
 * </ul>
 *
 * @since 1.0
 * @see io.github.jspinak.brobot.model.state.State
 * @see io.github.jspinak.brobot.state.stateStructure.StateStructure
 * @see io.github.jspinak.brobot.navigation.PathFinder
 */
package io.github.jspinak.brobot.model.transition;
