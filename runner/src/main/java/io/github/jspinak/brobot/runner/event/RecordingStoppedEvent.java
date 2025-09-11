package io.github.jspinak.brobot.runner.event;

import org.springframework.context.ApplicationEvent;

import io.github.jspinak.brobot.runner.persistence.entity.RecordingSessionEntity;

import lombok.Getter;

/** Event published when a recording session stops. */
@Getter
public class RecordingStoppedEvent extends ApplicationEvent {

    private final RecordingSessionEntity session;

    public RecordingStoppedEvent(Object source, RecordingSessionEntity session) {
        super(source);
        this.session = session;
    }

    public RecordingStoppedEvent(RecordingSessionEntity session) {
        super(session);
        this.session = session;
    }

    /** Get total actions recorded in the session */
    public int getTotalRecorded() {
        return session != null ? session.getTotalActions() : 0;
    }

    /** Get success rate of the session */
    public double getSuccessRate() {
        return session != null ? session.getSuccessRate() : 0.0;
    }
}
