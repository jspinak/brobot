package io.github.jspinak.brobot.control;

/**
 * Exception thrown when an execution is stopped during a pause point check.
 *
 * <p>This is a control flow exception used to gracefully exit execution loops when the user or
 * system requests a stop. It should be caught at the appropriate level to perform cleanup
 * operations.
 *
 * <p>This exception is part of the normal control flow and should not be treated as an error
 * condition. It allows for clean termination of nested execution contexts.
 */
public class ExecutionStoppedException extends RuntimeException {

    /** Constructs a new ExecutionStoppedException with a default message. */
    public ExecutionStoppedException() {
        super("Execution has been stopped");
    }

    /**
     * Constructs a new ExecutionStoppedException with the specified message.
     *
     * @param message the detail message
     */
    public ExecutionStoppedException(String message) {
        super(message);
    }

    /**
     * Constructs a new ExecutionStoppedException with the specified message and cause.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public ExecutionStoppedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new ExecutionStoppedException with the specified cause.
     *
     * @param cause the cause of this exception
     */
    public ExecutionStoppedException(Throwable cause) {
        super(cause);
    }
}
