package io.github.jspinak.brobot.exception;

/**
 * Base runtime exception for all Brobot framework exceptions.
 * <p>
 * This is the root of the Brobot exception hierarchy, providing a common base
 * for all framework-specific runtime exceptions. Using runtime exceptions allows
 * the framework to propagate errors up through multiple layers without forcing
 * intermediate code to handle them, enabling centralized error handling at
 * appropriate orchestration points.
 * </p>
 * 
 * @since 1.0
 */
public class BrobotRuntimeException extends RuntimeException {
    
    /**
     * Constructs a new runtime exception with the specified detail message.
     *
     * @param message the detail message
     */
    public BrobotRuntimeException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new runtime exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public BrobotRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructs a new runtime exception with the specified cause.
     *
     * @param cause the cause of the exception
     */
    public BrobotRuntimeException(Throwable cause) {
        super(cause);
    }
}