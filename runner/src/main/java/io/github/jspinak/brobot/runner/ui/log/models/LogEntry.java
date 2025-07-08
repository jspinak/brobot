package io.github.jspinak.brobot.runner.ui.log.models;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Model representing a log entry in the system.
 * This is a UI-agnostic representation of log data.
 */
@Data
@Builder
public class LogEntry {
    
    /**
     * Unique identifier for the log entry.
     */
    private final String id;
    
    /**
     * Timestamp when the log was created.
     */
    private final LocalDateTime timestamp;
    
    /**
     * The type/category of the log entry.
     */
    private final String type;
    
    /**
     * The severity level of the log.
     */
    private final LogLevel level;
    
    /**
     * The main log message.
     */
    private final String message;
    
    /**
     * The source that generated the log.
     */
    private final String source;
    
    /**
     * Additional details or context.
     */
    private final String details;
    
    /**
     * Associated exception if any.
     */
    private final Throwable exception;
    
    /**
     * Additional metadata as key-value pairs.
     */
    private final Map<String, Object> metadata;
    
    /**
     * State name if this is a state transition log.
     */
    private final String stateName;
    
    /**
     * Action name if this is an action log.
     */
    private final String actionName;
    
    /**
     * Log severity levels.
     */
    public enum LogLevel {
        TRACE("Trace", ""),
        DEBUG("Debug", ""),
        INFO("Info", ""),
        WARNING("Warning", ""),
        ERROR("Error", ""),
        FATAL("Fatal", "");
        
        private final String displayName;
        private final String icon;
        
        LogLevel(String displayName, String icon) {
            this.displayName = displayName;
            this.icon = icon;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getIcon() {
            return icon;
        }
        
        /**
         * Gets the appropriate style class for this level.
         */
        public String getStyleClass() {
            return "log-level-" + name().toLowerCase();
        }
    }
    
    /**
     * Creates a simple info log entry.
     */
    public static LogEntry info(String message) {
        return LogEntry.builder()
                .id(generateId())
                .timestamp(LocalDateTime.now())
                .type("SYSTEM")
                .level(LogLevel.INFO)
                .message(message)
                .source("System")
                .build();
    }
    
    /**
     * Creates a simple error log entry.
     */
    public static LogEntry error(String message, Throwable exception) {
        return LogEntry.builder()
                .id(generateId())
                .timestamp(LocalDateTime.now())
                .type("ERROR")
                .level(LogLevel.ERROR)
                .message(message)
                .source("System")
                .exception(exception)
                .build();
    }
    
    /**
     * Generates a unique ID for a log entry.
     */
    private static String generateId() {
        return "LOG-" + System.nanoTime();
    }
    
    /**
     * Gets a formatted timestamp string.
     */
    public String getFormattedTimestamp() {
        return timestamp.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
    }
    
    /**
     * Gets a short timestamp string.
     */
    public String getShortTimestamp() {
        return timestamp.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
    
    /**
     * Checks if this log entry has exception details.
     */
    public boolean hasException() {
        return exception != null;
    }
    
    /**
     * Checks if this log entry has additional metadata.
     */
    public boolean hasMetadata() {
        return metadata != null && !metadata.isEmpty();
    }
    
    /**
     * Gets the exception stack trace as a string.
     */
    public String getExceptionStackTrace() {
        if (exception == null) {
            return null;
        }
        
        java.io.StringWriter sw = new java.io.StringWriter();
        exception.printStackTrace(new java.io.PrintWriter(sw));
        return sw.toString();
    }
}