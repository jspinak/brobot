/**
 * Brobot annotation system for declarative state and transition configuration.
 *
 * <p>This package provides annotations that simplify the configuration of Brobot state machines by
 * allowing developers to declare states and transitions using annotations instead of manual
 * registration code.
 *
 * <h2>Key Annotations:</h2>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.annotations.State @State} - Marks a class as a Brobot state
 *   <li>{@link io.github.jspinak.brobot.annotations.Transition @Transition} - Marks a class as
 *       containing transition logic
 * </ul>
 *
 * <h2>Example Usage:</h2>
 *
 * <pre>
 * // Define a state
 * @State(initial = true)
 * @Getter
 * @Slf4j
 * public class LoginState {
 *     private StateObject loginButton = new StateObject.Builder()
 *         .withImage("login-button")
 *         .build();
 * }
 *
 * // Define transitions for a state
 * @TransitionSet(state = DashboardState.class)
 * @RequiredArgsConstructor
 * @Slf4j
 * public class DashboardTransitions {
 *     private final Action action;
 *     private final DashboardState dashboardState;
 *
 *     @OutgoingTransition(activate = {SettingsState.class})
 *     public boolean toSettings() {
 *         // Navigate from Dashboard to Settings
 *         return action.click(dashboardState.getSettingsButton()).isSuccess();
 *     }
 *
 *     @IncomingTransition
 *     public boolean verifyArrival() {
 *         // Verify we've arrived at the dashboard
 *         return action.find(dashboardState.getDashboardHeader()).isSuccess();
 *     }
 * }
 * </pre>
 *
 * <p>The {@link io.github.jspinak.brobot.annotations.AnnotationProcessor} automatically discovers
 * and registers all annotated states and transitions at application startup.
 *
 * @since 1.0.0
 */
package io.github.jspinak.brobot.annotations;
