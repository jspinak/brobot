package io.github.jspinak.brobot.runner.events;

import io.github.jspinak.brobot.tools.logging.model.LogData;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/** Event representing a log entry from the Brobot library. */
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class LogEntryEvent extends BrobotEvent {
    private final LogData logData;

    public LogEntryEvent(EventType eventType, Object source, LogData logData) {
        super(eventType, source);
        this.logData = logData;
    }

    public LogData getLogEntry() {
        return logData;
    }

    /** Factory method to create a log entry event */
    public static LogEntryEvent created(Object source, LogData logData) {
        return new LogEntryEvent(EventType.LOG_MESSAGE, source, logData);
    }
}
