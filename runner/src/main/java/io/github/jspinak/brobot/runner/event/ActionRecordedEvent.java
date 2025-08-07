package io.github.jspinak.brobot.runner.event;

import io.github.jspinak.brobot.runner.persistence.entity.ActionRecordEntity;
import io.github.jspinak.brobot.runner.persistence.entity.RecordingSessionEntity;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when an action is recorded.
 */
@Getter
public class ActionRecordedEvent extends ApplicationEvent {
    
    private final ActionRecordEntity record;
    private final RecordingSessionEntity session;
    
    public ActionRecordedEvent(Object source, ActionRecordEntity record, RecordingSessionEntity session) {
        super(source);
        this.record = record;
        this.session = session;
    }
    
    public ActionRecordedEvent(ActionRecordEntity record, RecordingSessionEntity session) {
        super(record);
        this.record = record;
        this.session = session;
    }
    
    /**
     * Check if the action was successful
     */
    public boolean wasSuccessful() {
        return record != null && record.isActionSuccess();
    }
    
    /**
     * Get the action type
     */
    public String getActionType() {
        return record != null ? record.getActionConfigType() : null;
    }
}