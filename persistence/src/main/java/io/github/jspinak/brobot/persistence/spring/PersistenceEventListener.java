package io.github.jspinak.brobot.persistence.spring;

import io.github.jspinak.brobot.model.action.ActionRecord;
import io.github.jspinak.brobot.model.state.stateObject.StateObject;
import io.github.jspinak.brobot.persistence.PersistenceProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;

/**
 * Spring event listener that bridges Brobot library events to persistence.
 * Listens for action execution events and records them.
 */
@RequiredArgsConstructor
@Slf4j
public class PersistenceEventListener {
    
    private final PersistenceProvider persistenceProvider;
    
    /**
     * Handle action executed events from the Brobot library.
     * Note: The library would need to publish these events.
     */
    @EventListener
    public void onActionExecuted(ActionExecutedEvent event) {
        if (persistenceProvider.isRecording()) {
            persistenceProvider.recordAction(event.getRecord(), event.getStateObject());
            log.trace("Recorded action: {}", event.getRecord().getActionConfig());
        }
    }
    
    /**
     * Event class for action execution.
     * This would ideally be defined in the library.
     */
    public static class ActionExecutedEvent {
        private final ActionRecord record;
        private final StateObject stateObject;
        
        public ActionExecutedEvent(ActionRecord record, StateObject stateObject) {
            this.record = record;
            this.stateObject = stateObject;
        }
        
        public ActionRecord getRecord() {
            return record;
        }
        
        public StateObject getStateObject() {
            return stateObject;
        }
    }
}