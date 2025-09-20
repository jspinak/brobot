package io.github.jspinak.brobot.model.state.special;

// SpecialStateType is in the same package

/**
 * Marker class for transitions targeting the current active state.
 *
 * <p>This class is used with @OutgoingTransition(to = CurrentState.class) to indicate a
 * self-transition or a transition that targets the currently active state. This is particularly
 * useful when an action modifies the current state without navigating away, or when transitioning
 * between overlapping states where both remain active.
 *
 * <p>Use cases include:
 *
 * <ul>
 *   <li>Self-transitions that perform an action but stay in the same state
 *   <li>Refresh operations that reload the current state
 *   <li>Pagination, sorting, or filtering that modifies the view
 *   <li>Capturing items in games where the overlay state remains active
 *   <li>Re-entering an already active state from another active state
 * </ul>
 *
 * <p>Example from tutorial-basics (island capture):
 *
 * <pre>{@code
 * // In World-to-Island transition scenario where Island overlays World
 * // and both states remain active. To capture multiple islands:
 *
 * @TransitionSet(state = IslandState.class)
 * public class IslandTransitions {
 *
 *     // Traditional approach - directly calling transition method
 *     public void captureNewIsland() {
 *         // This bypasses the state management system
 *         fromWorld();  // Direct method call
 *     }
 *
 *     // Better approach - using CurrentState for re-entry
 *     @OutgoingTransition(
 *         to = CurrentState.class,  // Re-enter Island state
 *         pathCost = 0,
 *         description = "Capture new island (re-enter Island from World)"
 *     )
 *     public boolean captureNewIsland() {
 *         // Since both World and Island are active (Island overlays World),
 *         // this transition from World back to Island to capture another island
 *         return action.click(worldState.getNextIsland()).isSuccess();
 *     }
 * }
 * }</pre>
 *
 * <p>Other common examples:
 *
 * <pre>{@code
 * // Data refresh
 * @OutgoingTransition(
 *     to = CurrentState.class,
 *     pathCost = 5,
 *     description = "Refresh current page"
 * )
 * public boolean refresh() {
 *     return action.type("{F5}").isSuccess();
 * }
 *
 * // Pagination
 * @OutgoingTransition(
 *     to = CurrentState.class,
 *     pathCost = 2,
 *     description = "Load next page of results"
 * )
 * public boolean nextPage() {
 *     return action.click(nextButton).isSuccess();
 * }
 * }</pre>
 *
 * @since 1.0
 * @see SpecialStateType#CURRENT
 * @see PreviousState
 */
public final class CurrentState {
    /**
     * The special state ID that represents "stay in current state". This ID is resolved at runtime
     * to the actual current state.
     */
    public static final Long ID = SpecialStateType.CURRENT.getId();

    /**
     * Private constructor prevents instantiation. This is a marker class used only for type
     * references in annotations.
     */
    private CurrentState() {
        throw new UnsupportedOperationException(
                "CurrentState is a marker class and cannot be instantiated");
    }
}
