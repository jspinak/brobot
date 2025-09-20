package io.github.jspinak.brobot.model.state.special;

// SpecialStateType is in the same package

/**
 * Marker class for transitions that should return to the previous (hidden) state.
 *
 * <p>This class is used with @OutgoingTransition(to = PreviousState.class) to indicate a dynamic
 * transition that returns to whatever state was covered by an overlay.
 *
 * <p>When a state overlays another (e.g., a menu opens over a game screen), the covered state is
 * tracked as "hidden". Transitions to PreviousState will dynamically resolve to return to that
 * hidden state.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @OutgoingTransition(
 *     to = PreviousState.class,
 *     pathCost = 0,
 *     description = "Close menu and return to previous state"
 * )
 * public boolean closeMenu() {
 *     return action.click(closeButton).isSuccess();
 * }
 * }</pre>
 *
 * <p>This enables flexible navigation where overlay states (menus, dialogs, popups) can be closed
 * to return to whatever state they were covering, without hardcoding specific target states.
 *
 * @since 1.0
 * @see SpecialStateType#PREVIOUS
 */
public final class PreviousState {
    /**
     * The special state ID that represents "return to previous state". This ID is resolved at
     * runtime to the actual hidden state.
     */
    public static final Long ID = SpecialStateType.PREVIOUS.getId();

    /**
     * Private constructor prevents instantiation. This is a marker class used only for type
     * references in annotations.
     */
    private PreviousState() {
        throw new UnsupportedOperationException(
                "PreviousState is a marker class and cannot be instantiated");
    }
}
