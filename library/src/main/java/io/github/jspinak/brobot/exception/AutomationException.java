package io.github.jspinak.brobot.exception;

/**
 * Exception thrown when automation sequences fail.
 *
 * <p>This exception provides structured information about automation failures, allowing
 * applications to handle them appropriately based on configuration.
 *
 * <p>The exception can be caught and handled by applications to:
 *
 * <ul>
 *   <li>Log detailed failure information
 *   <li>Trigger recovery mechanisms
 *   <li>Continue with alternative automation paths
 *   <li>Perform cleanup operations
 * </ul>
 *
 * @since 1.0
 */
public class AutomationException extends RuntimeException {

    private final String stateName;
    private final String operation;
    private final boolean recoverable;

    /**
     * Creates an automation exception with a message.
     *
     * @param message Error message
     */
    public AutomationException(String message) {
        super(message);
        this.stateName = null;
        this.operation = null;
        this.recoverable = false;
    }

    /**
     * Creates an automation exception with a message and cause.
     *
     * @param message Error message
     * @param cause Underlying cause
     */
    public AutomationException(String message, Throwable cause) {
        super(message, cause);
        this.stateName = null;
        this.operation = null;
        this.recoverable = false;
    }

    /**
     * Creates an automation exception with detailed context.
     *
     * @param message Error message
     * @param stateName Name of the state where failure occurred
     * @param operation Operation that failed (e.g., "transition", "click", "find")
     * @param recoverable Whether the error is potentially recoverable
     */
    public AutomationException(
            String message, String stateName, String operation, boolean recoverable) {
        super(message);
        this.stateName = stateName;
        this.operation = operation;
        this.recoverable = recoverable;
    }

    /**
     * Creates an automation exception with detailed context and cause.
     *
     * @param message Error message
     * @param cause Underlying cause
     * @param stateName Name of the state where failure occurred
     * @param operation Operation that failed
     * @param recoverable Whether the error is potentially recoverable
     */
    public AutomationException(
            String message,
            Throwable cause,
            String stateName,
            String operation,
            boolean recoverable) {
        super(message, cause);
        this.stateName = stateName;
        this.operation = operation;
        this.recoverable = recoverable;
    }

    /**
     * Gets the name of the state where the failure occurred.
     *
     * @return State name, or null if not specified
     */
    public String getStateName() {
        return stateName;
    }

    /**
     * Gets the operation that failed.
     *
     * @return Operation name, or null if not specified
     */
    public String getOperation() {
        return operation;
    }

    /**
     * Checks if the error is potentially recoverable.
     *
     * @return true if recoverable, false otherwise
     */
    public boolean isRecoverable() {
        return recoverable;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("AutomationException: ");
        sb.append(getMessage());
        if (stateName != null) {
            sb.append(" [State: ").append(stateName).append("]");
        }
        if (operation != null) {
            sb.append(" [Operation: ").append(operation).append("]");
        }
        if (recoverable) {
            sb.append(" [Recoverable]");
        }
        return sb.toString();
    }
}
