package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.datatypes.state.state.State;
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

    private final AllStatesInProjectService allStatesInProjectService;
    private final StateMemory stateMemory;

    public SetHiddenStates(AllStatesInProjectService allStatesInProjectService, StateMemory stateMemory) {
        this.allStatesInProjectService = allStatesInProjectService;
        this.stateMemory = stateMemory;
    }

    /**
     * After stateToSet is active, set the hidden States and remove them from the active States.
     * @param stateToSet the State to add to the list of active States.
     * @return true when the stateToSet is a valid State
     */
    public boolean set(String stateToSet) {
        Optional<State> optStateToSet = allStatesInProjectService.getState(stateToSet);
        if (optStateToSet.isEmpty()) return false;
        State state = optStateToSet.get();
        for (String activeState : new ArrayList<>(stateMemory.getActiveStates())) {
            if (state.getCanHide().contains(activeState)) {
                state.addHiddenState(activeState);
                stateMemory.removeInactiveState(activeState);
            }
        }
        return true;
    }
}
