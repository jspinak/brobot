package io.github.jspinak.brobot.runner.events;

/**
 * Event representing an error that occurred in the system.
 */
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

    public String getErrorMessage() {
        return errorMessage;
    }

    public Exception getException() {
        return exception;
    }

    public ErrorSeverity getSeverity() {
        return severity;
    }

    public String getComponentName() {
        return componentName;
    }

    /**
     * Enum defining error severity levels
     */
    public enum ErrorSeverity {
        LOW,        // Non-critical, system can continue
        MEDIUM,     // Important but not fatal
        HIGH,       // Critical error, specific operation failed
        FATAL       // System cannot continue
    }

    /**
     * Factory method to create a low severity error event
     */
    public static ErrorEvent low(Object source, String errorMessage, Exception exception, String componentName) {
        return new ErrorEvent(source, errorMessage, exception, ErrorSeverity.LOW, componentName);
    }

    /**
     * Factory method to create a medium severity error event
     */
    public static ErrorEvent medium(Object source, String errorMessage, Exception exception, String componentName) {
        return new ErrorEvent(source, errorMessage, exception, ErrorSeverity.MEDIUM, componentName);
    }

    /**
     * Factory method to create a high severity error event
     */
    public static ErrorEvent high(Object source, String errorMessage, Exception exception, String componentName) {
        return new ErrorEvent(source, errorMessage, exception, ErrorSeverity.HIGH, componentName);
    }

    /**
     * Factory method to create a fatal error event
     */
    public static ErrorEvent fatal(Object source, String errorMessage, Exception exception, String componentName) {
        return new ErrorEvent(source, errorMessage, exception, ErrorSeverity.FATAL, componentName);
    }
}