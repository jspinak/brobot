package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.services.StateTransitionsService;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static io.github.jspinak.brobot.manageStates.StateMemory.Enum.PREVIOUS;

@Component
public class AdjacentStates {

    private final AllStatesInProjectService allStatesInProjectService;
    private final StateMemory stateMemory;
    private final StateTransitionsService stateTransitionsService;

    public AdjacentStates(AllStatesInProjectService allStatesInProjectService, StateMemory stateMemory,
                          StateTransitionsService stateTransitionsService) {
        this.allStatesInProjectService = allStatesInProjectService;
        this.stateMemory = stateMemory;
        this.stateTransitionsService = stateTransitionsService;
    }

    public Set<String> getAdjacentStates(String stateName) {
        Set<String> adjacent = new HashSet<>();
        Optional<StateTransitions> trsOpt = stateTransitionsService.getTransitions(stateName);
        if (trsOpt.isEmpty()) return adjacent;
        Set<String> statesWithStaticTransitions = trsOpt.get().getTransitions().keySet();
        adjacent.addAll(statesWithStaticTransitions);
        if (!statesWithStaticTransitions.contains(PREVIOUS.toString())) return adjacent;
        adjacent.remove(PREVIOUS.toString());
        Optional<State> currentState = allStatesInProjectService.getState(stateName);
        if (currentState.isEmpty() || currentState.get().getHidden().isEmpty()) return adjacent;
        adjacent.addAll(currentState.get().getHidden());
        return adjacent;
    }

    public Set<String> getAdjacentStates(Set<String> stateNames) {
        Set<String> adjacent = new HashSet<>();
        stateNames.forEach(sE -> adjacent.addAll(getAdjacentStates(sE)));
        return adjacent;
    }

    public Set<String> getAdjacentStates() {
        return getAdjacentStates(stateMemory.getActiveStates());
    }
}
