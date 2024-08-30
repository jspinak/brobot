package io.github.jspinak.brobot.app.models;

import io.github.jspinak.brobot.app.database.entities.StateEntity;
import io.github.jspinak.brobot.app.database.entities.StateTransitionsEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StateAndTransitions {
    private StateEntity state;
    private StateTransitionsEntity stateTransitions;

    public StateAndTransitions(StateEntity state, StateTransitionsEntity stateTransitions) {
        this.state = state;
        this.stateTransitions = stateTransitions;
    }
}
