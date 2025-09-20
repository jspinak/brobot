package io.github.jspinak.brobot.model.state.special;

// SpecialStateType is in the same package

/**
 * Marker class for transitions to an expected state determined at runtime.
 *
 * <p>This class is used with @OutgoingTransition(activate = {ExpectedState.class}) to indicate a
 * transition where the target state is determined dynamically based on the application's expected
 * behavior or user configuration.
 *
 * <p>Use cases include:
 *
 * <ul>
 *   <li>Transitions where the target depends on user settings or preferences
 *   <li>Navigation flows that change based on application state
 *   <li>Conditional routing based on external factors
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @OutgoingTransition(
 *     activate = {ExpectedState.class},
 *     pathCost = 10,
 *     description = "Navigate to user's default page"
 * )
 * public boolean goToDefault() {
 *     // The actual target is resolved based on user preferences
 *     return action.click(homeButton).isSuccess();
 * }
 * }</pre>
 *
 * <p>The framework will resolve ExpectedState to the appropriate target based on the current
 * context and configured expectations.
 *
 * @since 1.0
 * @see SpecialStateType#EXPECTED
 */
public final class ExpectedState {
    /**
     * The special state ID that represents "go to expected state". This ID is resolved at runtime
     * based on context.
     */
    public static final Long ID = SpecialStateType.EXPECTED.getId();

    /**
     * Private constructor prevents instantiation. This is a marker class used only for type
     * references in annotations.
     */
    private ExpectedState() {
        throw new UnsupportedOperationException(
                "ExpectedState is a marker class and cannot be instantiated");
    }
}
