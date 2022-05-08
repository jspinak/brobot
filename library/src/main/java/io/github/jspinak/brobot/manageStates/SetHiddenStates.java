package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.primatives.enums.StateEnum;
import io.github.jspinak.brobot.services.StateService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Set the hidden States after a successful State transition.
 * Any active States that are in the newly activated State's canHide set
 * will become hidden States.
 */
@Component
public class SetHiddenStates {

    private final StateService stateService;
    private StateMemory stateMemory;

    public SetHiddenStates(StateService stateService, StateMemory stateMemory) {
        this.stateService = stateService;
        this.stateMemory = stateMemory;
    }

    /**
     * After stateToSet is active, set the hidden States and remove them from the active States.
     * @param stateToSet
     * @return true when the stateToSet is a valid State
     */
    public boolean set(StateEnum stateToSet) {
        Optional<State> optStateToSet = stateService.findByName(stateToSet);
        if (optStateToSet.isEmpty()) return false;
        State state = optStateToSet.get();
        for (StateEnum activeState : new ArrayList<>(stateMemory.getActiveStates())) {
            if (state.getCanHide().contains(activeState)) {
                state.addHiddenState(activeState);
                stateMemory.removeInactiveState(activeState);
            }
        }
        return true;
    }
}
