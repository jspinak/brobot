package io.github.jspinak.brobot.runner.events;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Event representing a log message from the system.
 */
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class LogEvent extends BrobotEvent {
    private final String message;
    private final LogLevel level;
    private final String category;
    private final Exception exception;

    public LogEvent(EventType eventType, Object source, String message, LogLevel level,
                    String category, Exception exception) {
        super(eventType, source);
        this.message = message;
        this.level = level;
        this.category = category;
        this.exception = exception;
    }

    public String getMessage() {
        return message;
    }

    public LogLevel getLevel() {
        return level;
    }

    public String getCategory() {
        return category;
    }

    public Exception getException() {
        return exception;
    }

    /**
     * Enum defining log severity levels
     */
    public enum LogLevel {
        DEBUG,
        INFO,
        WARNING,
        ERROR,
        CRITICAL
    }

    /**
     * Factory method to create a debug log event
     */
    public static LogEvent debug(Object source, String message, String category) {
        return new LogEvent(EventType.LOG_MESSAGE, source, message, LogLevel.DEBUG, category, null);
    }

    /**
     * Factory method to create an info log event
     */
    public static LogEvent info(Object source, String message, String category) {
        return new LogEvent(EventType.LOG_MESSAGE, source, message, LogLevel.INFO, category, null);
    }

    /**
     * Factory method to create a warning log event
     */
    public static LogEvent warning(Object source, String message, String category) {
        return new LogEvent(EventType.LOG_WARNING, source, message, LogLevel.WARNING, category, null);
    }

    /**
     * Factory method to create an error log event
     */
    public static LogEvent error(Object source, String message, String category, Exception exception) {
        return new LogEvent(EventType.LOG_ERROR, source, message, LogLevel.ERROR, category, exception);
    }

    /**
     * Factory method to create a critical log event
     */
    public static LogEvent critical(Object source, String message, String category, Exception exception) {
        return new LogEvent(EventType.LOG_ERROR, source, message, LogLevel.CRITICAL, category, exception);
    }
}