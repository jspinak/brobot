package io.github.jspinak.brobot.runner.ui.log.services;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.runner.events.*;
import io.github.jspinak.brobot.runner.ui.log.models.LogEntry;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for parsing various log sources into a unified LogEntry format. Handles conversion from
 * different event types and log formats.
 */
@Slf4j
@Service
public class LogParsingService {

    /** Parses a BrobotEvent into a LogEntry. */
    public LogEntry parseEvent(BrobotEvent event) {
        if (event == null) {
            return null;
        }

        LogEntry.LogEntryBuilder builder =
                LogEntry.builder()
                        .id(generateId())
                        .timestamp(
                                LocalDateTime.ofInstant(
                                        event.getTimestamp(), ZoneId.systemDefault()));

        // Parse based on event type
        BrobotEvent.EventType eventType = event.getEventType();

        // Set common properties
        builder.type(eventType.name())
                .source(
                        event.getSource() != null
                                ? event.getSource().getClass().getSimpleName()
                                : "Unknown");

        // Handle specific event types
        if (eventType.name().startsWith("EXECUTION_")) {
            return parseExecutionEvent(event, builder);
        } else if (eventType.name().equals("STATE_TRANSITION")) {
            return parseStateEvent(event, builder);
        } else if (eventType.name().startsWith("ACTION_")) {
            return parseActionEvent(event, builder);
        } else if (eventType.name().contains("ERROR")) {
            return parseErrorEvent(event, builder);
        } else if (eventType.name().contains("LOG")) {
            return parseLogEvent(event, builder);
        } else {
            return parseGenericEvent(event, builder);
        }
    }

    /** Parses execution-related events. */
    private LogEntry parseExecutionEvent(BrobotEvent event, LogEntry.LogEntryBuilder builder) {
        String eventTypeName = event.getEventType().name();

        builder.type("EXECUTION");

        switch (eventTypeName) {
            case "EXECUTION_STARTED":
                builder.level(LogEntry.LogLevel.INFO).message("Execution started");
                break;
            case "EXECUTION_COMPLETED":
                builder.level(LogEntry.LogLevel.INFO).message("Execution completed");
                break;
            case "EXECUTION_FAILED":
                builder.level(LogEntry.LogLevel.ERROR).message("Execution failed");
                break;
            case "EXECUTION_PAUSED":
                builder.level(LogEntry.LogLevel.WARNING).message("Execution paused");
                break;
            case "EXECUTION_RESUMED":
                builder.level(LogEntry.LogLevel.INFO).message("Execution resumed");
                break;
            default:
                builder.level(LogEntry.LogLevel.INFO).message("Execution event: " + eventTypeName);
        }

        return builder.build();
    }

    /** Parses state-related events. */
    private LogEntry parseStateEvent(BrobotEvent event, LogEntry.LogEntryBuilder builder) {
        builder.type("STATE")
                .level(LogEntry.LogLevel.INFO)
                .message("State transition occurred")
                .source("StateManager");

        return builder.build();
    }

    /** Parses action-related events. */
    private LogEntry parseActionEvent(BrobotEvent event, LogEntry.LogEntryBuilder builder) {
        String eventTypeName = event.getEventType().name();

        builder.type("ACTION");

        switch (eventTypeName) {
            case "ACTION_STARTED":
                builder.level(LogEntry.LogLevel.DEBUG).message("Action started");
                break;
            case "ACTION_COMPLETED":
                builder.level(LogEntry.LogLevel.INFO).message("Action completed");
                break;
            case "ACTION_FAILED":
                builder.level(LogEntry.LogLevel.ERROR).message("Action failed");
                break;
            default:
                builder.level(LogEntry.LogLevel.INFO).message("Action event: " + eventTypeName);
        }

        return builder.build();
    }

    /** Parses error events. */
    private LogEntry parseErrorEvent(BrobotEvent event, LogEntry.LogEntryBuilder builder) {
        builder.type("ERROR").level(LogEntry.LogLevel.ERROR).message("An error occurred");

        // Check if this is an ErrorEvent with additional details
        if (event instanceof ErrorEvent) {
            ErrorEvent errorEvent = (ErrorEvent) event;
            builder.message(errorEvent.getErrorMessage()).exception(errorEvent.getException());

            if (errorEvent.getSeverity() != null) {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("severity", errorEvent.getSeverity());
                metadata.put("component", errorEvent.getComponentName());
                builder.metadata(metadata);
            }
        }

        return builder.build();
    }

    /** Parses log events. */
    private LogEntry parseLogEvent(BrobotEvent event, LogEntry.LogEntryBuilder builder) {
        builder.type("LOG");

        // Check if this is a LogEvent or LogEntryEvent
        if (event instanceof LogEvent) {
            LogEvent logEvent = (LogEvent) event;
            builder.message(logEvent.getMessage())
                    .level(convertLogLevel(logEvent.getLevel()))
                    .source(logEvent.getCategory() != null ? logEvent.getCategory() : "System");

            if (logEvent.getException() != null) {
                builder.exception(logEvent.getException());
            }
        } else if (event instanceof LogEntryEvent) {
            LogEntryEvent logEntryEvent = (LogEntryEvent) event;
            if (logEntryEvent.getLogData() != null) {
                // Extract message from LogData if available
                builder.message("Log entry from " + event.getSource())
                        .level(LogEntry.LogLevel.INFO)
                        .source(
                                event.getSource() != null
                                        ? event.getSource().toString()
                                        : "Unknown");

                // Add LogData info to metadata
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("logData", logEntryEvent.getLogData().toString());
                builder.metadata(metadata);
            }
        } else {
            builder.level(LogEntry.LogLevel.INFO).message("Log event");
        }

        return builder.build();
    }

    /** Parses generic events. */
    private LogEntry parseGenericEvent(BrobotEvent event, LogEntry.LogEntryBuilder builder) {
        builder.level(LogEntry.LogLevel.INFO).message(event.getEventType().name());

        return builder.build();
    }

    /** Converts LogEvent.LogLevel to our LogEntry.LogLevel. */
    private LogEntry.LogLevel convertLogLevel(LogEvent.LogLevel logLevel) {
        if (logLevel == null) {
            return LogEntry.LogLevel.INFO;
        }

        switch (logLevel) {
            case DEBUG:
                return LogEntry.LogLevel.DEBUG;
            case INFO:
                return LogEntry.LogLevel.INFO;
            case WARNING:
                return LogEntry.LogLevel.WARNING;
            case ERROR:
                return LogEntry.LogLevel.ERROR;
            default:
                return LogEntry.LogLevel.INFO;
        }
    }

    /**
     * Parses a raw log line (for importing logs from files). Expected format: [TIMESTAMP] [LEVEL]
     * [SOURCE] MESSAGE
     */
    public LogEntry parseLogLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        try {
            // Simple regex-based parsing
            String trimmed = line.trim();

            // Try to extract timestamp
            LocalDateTime timestamp = LocalDateTime.now();
            String timestampPattern = "\\[(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})\\]";
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(timestampPattern);
            java.util.regex.Matcher matcher = pattern.matcher(trimmed);

            if (matcher.find()) {
                try {
                    timestamp =
                            LocalDateTime.parse(
                                    matcher.group(1),
                                    java.time.format.DateTimeFormatter.ofPattern(
                                            "yyyy-MM-dd HH:mm:ss"));
                } catch (Exception e) {
                    // Keep default timestamp
                }
            }

            // Try to extract level
            LogEntry.LogLevel level = LogEntry.LogLevel.INFO;
            for (LogEntry.LogLevel l : LogEntry.LogLevel.values()) {
                if (trimmed.contains("[" + l.name() + "]")
                        || trimmed.contains("[" + l.getDisplayName() + "]")) {
                    level = l;
                    break;
                }
            }

            // Extract message (everything after common patterns)
            String message = trimmed;
            int messageStart = Math.max(trimmed.lastIndexOf(']') + 1, trimmed.indexOf('-') + 1);
            if (messageStart > 0 && messageStart < trimmed.length()) {
                message = trimmed.substring(messageStart).trim();
            }

            return LogEntry.builder()
                    .id(generateId())
                    .timestamp(timestamp)
                    .type("IMPORTED")
                    .level(level)
                    .message(message)
                    .source("Import")
                    .build();

        } catch (Exception e) {
            log.warn("Failed to parse log line: {}", line, e);
            // Return a basic entry with the raw line
            return LogEntry.builder()
                    .id(generateId())
                    .timestamp(LocalDateTime.now())
                    .type("IMPORTED")
                    .level(LogEntry.LogLevel.INFO)
                    .message(line)
                    .source("Import")
                    .details("Failed to parse: " + e.getMessage())
                    .build();
        }
    }

    /** Generates a unique ID for a log entry. */
    private String generateId() {
        return UUID.randomUUID().toString();
    }

    /** Creates a simple log entry from a message. */
    public LogEntry createSimpleEntry(String message, LogEntry.LogLevel level) {
        return LogEntry.builder()
                .id(generateId())
                .timestamp(LocalDateTime.now())
                .type("SYSTEM")
                .level(level)
                .message(message)
                .source("System")
                .build();
    }
}
