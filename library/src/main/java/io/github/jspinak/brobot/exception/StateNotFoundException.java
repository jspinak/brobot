package io.github.jspinak.brobot.exception;

/**
 * Thrown when a requested state cannot be found in the state management system.
 *
 * <p>This exception indicates that the framework attempted to access or transition to a state that
 * doesn't exist in the current state model. This is a critical error in model-based automation as
 * it suggests either a configuration problem or that the application is in an unexpected state.
 *
 * @since 1.0
 */
public class StateNotFoundException extends BrobotRuntimeException {

    private final String stateName;

    /**
     * Constructs a new state not found exception.
     *
     * @param stateName the name of the state that could not be found
     */
    public StateNotFoundException(String stateName) {
        super(String.format("State '%s' not found in the state model", stateName));
        this.stateName = stateName;
    }

    /**
     * Constructs a new state not found exception with additional context.
     *
     * @param stateName the name of the state that could not be found
     * @param context additional context about where the state was expected
     */
    public StateNotFoundException(String stateName, String context) {
        super(String.format("State '%s' not found in %s", stateName, context));
        this.stateName = stateName;
    }

    /**
     * Gets the name of the state that could not be found.
     *
     * @return the state name
     */
    public String getStateName() {
        return stateName;
    }
}
