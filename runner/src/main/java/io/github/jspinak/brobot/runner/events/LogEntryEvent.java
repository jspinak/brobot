package io.github.jspinak.brobot.runner.events;

import io.github.jspinak.brobot.log.entities.LogEntry;

/**
 * Event representing a log entry from the Brobot library.
 */
public class LogEntryEvent extends BrobotEvent {
    private final LogEntry logEntry;

    public LogEntryEvent(EventType eventType, Object source, LogEntry logEntry) {
        super(eventType, source);
        this.logEntry = logEntry;
    }

    public LogEntry getLogEntry() {
        return logEntry;
    }

    /**
     * Factory method to create a log entry event
     */
    public static LogEntryEvent created(Object source, LogEntry logEntry) {
        return new LogEntryEvent(EventType.LOG_MESSAGE, source, logEntry);
    }
}