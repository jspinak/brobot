package io.github.jspinak.brobot.runner.events;

import lombok.Getter;

/**
 * Event published when an error occurs in the application.
 */
@Getter
public class ErrorEvent extends BrobotEvent {
    private final String errorMessage;
    private final Exception exception;
    private final ErrorSeverity severity;
    private final String componentName;
    
    public ErrorEvent(Object source, String errorMessage, Exception exception, 
                     ErrorSeverity severity, String componentName) {
        super(EventType.ERROR_OCCURRED, source);
        this.errorMessage = errorMessage;
        this.exception = exception;
        this.severity = severity;
        this.componentName = componentName;
    }
    
    /**
     * Factory method for low severity errors.
     */
    public static ErrorEvent low(Object source, String errorMessage, Exception exception, String componentName) {
        return new ErrorEvent(source, errorMessage, exception, ErrorSeverity.LOW, componentName);
    }
    
    /**
     * Factory method for medium severity errors.
     */
    public static ErrorEvent medium(Object source, String errorMessage, Exception exception, String componentName) {
        return new ErrorEvent(source, errorMessage, exception, ErrorSeverity.MEDIUM, componentName);
    }
    
    /**
     * Factory method for high severity errors.
     */
    public static ErrorEvent high(Object source, String errorMessage, Exception exception, String componentName) {
        return new ErrorEvent(source, errorMessage, exception, ErrorSeverity.HIGH, componentName);
    }
    
    /**
     * Factory method for fatal errors.
     */
    public static ErrorEvent fatal(Object source, String errorMessage, Exception exception, String componentName) {
        return new ErrorEvent(source, errorMessage, exception, ErrorSeverity.FATAL, componentName);
    }
    
    /**
     * Error severity levels.
     */
    public enum ErrorSeverity {
        LOW,      // Minor errors that don't affect functionality
        MEDIUM,   // Errors that affect some functionality
        HIGH,     // Serious errors that affect major functionality
        FATAL     // Critical errors that require application restart
    }
}