package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.datatypes.state.state.State;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class StateManagementService {

    private final AllStatesInProjectService allStatesInProjectService;

    public StateManagementService(AllStatesInProjectService allStatesInProjectService) {
        this.allStatesInProjectService = allStatesInProjectService;
    }

    // Method to convert all StateTransitions
    public void convertAllStateTransitions(List<StateTransitions> allStateTransitions) {
        for (StateTransitions stateTransitions : allStateTransitions) {
            convertNamesToIds(stateTransitions);
        }
    }

    /**
     * Converts state names to IDs in the given StateTransitions object.
     * This method assumes that the state names are unique and that the
     * stateIdToNameMap is already populated.
     *
     * @param stateTransitions The StateTransitions object to convert.
     */
    public void convertNamesToIds(StateTransitions stateTransitions) {
        Long stateId = allStatesInProjectService.getStateId(stateTransitions.getStateName());
        if (stateTransitions.getStateId() == null) {
            stateTransitions.setStateId(stateId);
        }
        for (IStateTransition transition : stateTransitions.getTransitions()) {
            if (transition instanceof JavaStateTransition javaTransition) {
                for (String stateToActivate : javaTransition.getActivateNames()) {
                    Long stateToActivateId = allStatesInProjectService.getStateId(stateToActivate);
                    if (stateToActivateId != null) {
                        javaTransition.getActivate().add(stateToActivateId);
                    }
                }
            }
        }
    }
}
