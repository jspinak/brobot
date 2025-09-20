package io.github.jspinak.brobot.annotations;

import java.lang.annotation.*;

/**
 * Marks a method as an OutgoingTransition - a transition FROM the state defined in the
 * enclosing @TransitionSet class TO a specific target state.
 *
 * <p>This annotation represents the cleaner pattern where a state's transition class contains its
 * incoming transition (IncomingTransition) and all its outgoing transitions. This is more cohesive
 * since outgoing transitions use the current state's images.
 *
 * <p><b>Special State Markers:</b> The 'to' parameter can accept special marker classes for dynamic
 * transitions:
 *
 * <ul>
 *   <li>{@code PreviousState.class} - Returns to the most recently hidden state (for overlays,
 *       dialogs, menus)
 *   <li>{@code CurrentState.class} - Self-transition that stays in or re-enters the current state
 *   <li>{@code ExpectedState.class} - (Future) Runtime-determined target state
 * </ul>
 *
 * <p><b>Hidden States:</b> When a state overlays another (e.g., a menu opening over a page), the
 * covered state is automatically tracked as "hidden". Transitions to {@code PreviousState.class}
 * will dynamically resolve to return to whatever state was hidden.
 *
 * <p>The annotated method should:
 *
 * <ul>
 *   <li>Return boolean (true if transition succeeds, false otherwise)
 *   <li>Contain the actions needed to navigate TO the target state
 *   <li>Be a member of a class annotated with @TransitionSet
 *   <li>Use images from the current state (defined in @TransitionSet)
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * // Simple transition to a specific state
 * @OutgoingTransition(activate = {PricingState.class})  // pathCost defaults to 1
 * public boolean toPricing() {
 *     log.info("Navigating from Homepage to Pricing");
 *     return action.click(homepageState.getPricingLink()).isSuccess();
 * }
 *
 * // Return to hidden state (dynamic transition)
 * @OutgoingTransition(
 *     activate = {PreviousState.class},  // Returns to whatever state was covered
 *     pathCost = 0,
 *     description = "Close modal and return to previous state"
 * )
 * public boolean closeModal() {
 *     return action.click(modalState.getCloseButton()).isSuccess();
 * }
 *
 * // Self-transition (stays in current state)
 * @OutgoingTransition(
 *     activate = {CurrentState.class},  // Stay in or re-enter current state
 *     pathCost = 2,
 *     description = "Load more results"
 * )
 * public boolean loadMore() {
 *     return action.click(loadMoreButton).isSuccess();
 * }
 *
 * // Modal overlay - keep origin visible
 * @OutgoingTransition(
 *     activate = {SettingsModalState.class},
 *     staysVisible = true,  // dashboard remains visible behind modal
 *     pathCost = 2
 * )
 * public boolean toSettingsModal() {
 *     return action.click(dashboardState.getSettingsButton()).isSuccess();
 * }
 *
 * // Complex transition with multiple state changes
 * @OutgoingTransition(
 *     activate = {DashboardState.class, SidebarState.class, HeaderState.class},
 *     exit = {LoginState.class},
 *     staysVisible = false,  // originating state is deactivated (default)
 *     pathCost = 0  // preferred path (lower cost)
 * )
 * public boolean toDashboard() {
 *     return action.click(loginState.getLoginButton()).isSuccess();
 * }
 * }</pre>
 *
 * @since 1.3.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OutgoingTransition {

    /**
     * States to activate during this transition. This transition will navigate FROM the state
     * defined in @TransitionSet and activate all states in this array.
     *
     * <p>Can include:
     *
     * <ul>
     *   <li>Regular state classes (e.g., {@code HomeState.class})
     *   <li>{@code PreviousState.class} - Returns to the most recently hidden state
     *   <li>{@code CurrentState.class} - Self-transition or re-enters current state
     *   <li>{@code ExpectedState.class} - (Future) Runtime-determined state
     * </ul>
     *
     * <p>For single-target transitions, use a single-element array: {@code activate =
     * {TargetState.class}}
     *
     * <p>For multi-state transitions, include all states to activate: {@code activate =
     * {MainState.class, SidePanel.class, Header.class}}
     *
     * @return array of state classes to activate (required, must have at least one)
     */
    Class<?>[] activate();

    /**
     * States to exit/deactivate during this transition. Note: The originating state's deactivation
     * is controlled by staysVisible.
     *
     * @return array of state classes to exit
     */
    Class<?>[] exit() default {};

    /**
     * Whether the originating state remains visible after transition. Default is false (originating
     * state is deactivated).
     *
     * @return true if originating state should remain visible, false otherwise
     */
    boolean staysVisible() default false;

    /**
     * Path-finding cost for this transition. The total cost of a path is the sum of all state costs
     * and transition costs in that path. Lower costs are preferred when multiple paths exist.
     * Higher costs discourage using this transition in pathfinding. Default is 1.
     *
     * <p>Example uses:
     *
     * <ul>
     *   <li>0 - Free transition (no cost)
     *   <li>1 - Normal transition (default)
     *   <li>5 - Slightly expensive transition
     *   <li>10+ - Expensive/fallback transition
     * </ul>
     *
     * @return the path cost for this transition
     */
    int pathCost() default 1;

    /**
     * Optional description of this transition. Useful for documentation and debugging.
     *
     * @return the transition description
     */
    String description() default "";
}
