package io.github.jspinak.brobot.runner.session;

import java.time.LocalDateTime;

import lombok.Data;

/** Represents an event that occurred during a session */
@Data
public class SessionEvent {
    private String type;
    private LocalDateTime timestamp;
    private String description;
    private String details;

    public SessionEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public SessionEvent(String type, String description) {
        this();
        this.type = type;
        this.description = description;
    }

    public SessionEvent(String type, String description, String details) {
        this(type, description);
        this.details = details;
    }
}
