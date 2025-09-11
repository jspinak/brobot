package io.github.jspinak.brobot.annotations;

import org.springframework.context.ApplicationEvent;

/**
 * Event fired when all @State annotated classes have been processed and registered. This event
 * signals that the state structure is ready to be initialized.
 */
public class StatesRegisteredEvent extends ApplicationEvent {

    private final int stateCount;
    private final int transitionCount;

    public StatesRegisteredEvent(Object source, int stateCount, int transitionCount) {
        super(source);
        this.stateCount = stateCount;
        this.transitionCount = transitionCount;
    }

    public int getStateCount() {
        return stateCount;
    }

    public int getTransitionCount() {
        return transitionCount;
    }
}
