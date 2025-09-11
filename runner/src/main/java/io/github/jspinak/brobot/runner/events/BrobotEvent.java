package io.github.jspinak.brobot.runner.events;

import java.time.Instant;

/**
 * Base class for all events in the Brobot Runner. All events extend from this class and provide
 * specific event data.
 */
public abstract class BrobotEvent {
    private final Instant timestamp;
    private final EventType eventType;
    private final Object source;

    public BrobotEvent(EventType eventType, Object source) {
        this.timestamp = Instant.now();
        this.eventType = eventType;
        this.source = source;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public EventType getEventType() {
        return eventType;
    }

    public Object getSource() {
        return source;
    }

    /** Enum defining the types of events in the system. New event types should be added here. */
    public enum EventType {
        // Execution events
        EXECUTION_STARTED,
        EXECUTION_PROGRESS,
        EXECUTION_COMPLETED,
        EXECUTION_FAILED,
        EXECUTION_PAUSED,
        EXECUTION_RESUMED,
        EXECUTION_STOPPED,

        // Automation events
        AUTOMATION_STARTED,
        AUTOMATION_COMPLETED,
        AUTOMATION_FAILED,
        AUTOMATION_PROGRESS,
        AUTOMATION_PAUSED,
        AUTOMATION_RESUMED,
        AUTOMATION_STOPPED,
        STATE_TRANSITION,
        ACTION_STARTED,
        ACTION_COMPLETED,
        ACTION_FAILED,

        // Configuration events
        CONFIG_LOADED,
        CONFIG_LOADING_FAILED,

        // Log events
        LOG_MESSAGE,
        LOG_WARNING,
        LOG_ERROR,
        LOG_ENTRY,

        // UI events
        UI_STATE_CHANGED,

        // Error events
        ERROR_OCCURRED,

        // System events
        SYSTEM_STARTUP,
        SYSTEM_SHUTDOWN
    }
}
