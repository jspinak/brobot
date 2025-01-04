package io.github.jspinak.brobot.automationScripts;

import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.manageStates.StateTransitions;

public interface StateHandler {
    boolean handleState(State currentState, StateTransitions stateTransitions);
    void onNoTransitionFound();
}
