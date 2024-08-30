package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.datatypes.state.state.State;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StateManagementService {
    private final Map<String, Long> stateNameToIdMap = new HashMap<>();
    private final Map<Long, String> stateIdToNameMap = new HashMap<>();

    // Method to initialize state mappings
    public void initializeStateMappings(List<State> states) {
        for (State state : states) {
            stateNameToIdMap.put(state.getName(), state.getId());
            stateIdToNameMap.put(state.getId(), state.getName());
        }
    }

    // Method to convert names to IDs
    public Long getStateId(String stateName) {
        return stateNameToIdMap.get(stateName);
    }

    // Method to convert IDs to names
    public String getStateName(Long stateId) {
        return stateIdToNameMap.get(stateId);
    }

    // Method to convert all StateTransitions
    public void convertAllStateTransitions(List<StateTransitions> allStateTransitions) {
        for (StateTransitions stateTransitions : allStateTransitions) {
            stateTransitions.convertNamesToIds(this::getStateId);
        }
    }
}
