package io.github.jspinak.brobot.runner.event;

import io.github.jspinak.brobot.runner.persistence.entity.RecordingSessionEntity;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a recording session starts.
 */
@Getter
public class RecordingStartedEvent extends ApplicationEvent {
    
    private final RecordingSessionEntity session;
    
    public RecordingStartedEvent(Object source, RecordingSessionEntity session) {
        super(source);
        this.session = session;
    }
    
    public RecordingStartedEvent(RecordingSessionEntity session) {
        super(session);
        this.session = session;
    }
}