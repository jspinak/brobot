package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.primatives.enums.SpecialStateType;
import io.github.jspinak.brobot.services.StateTransitionsInProjectService;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AdjacentStates {

    private final AllStatesInProjectService allStatesInProjectService;
    private final StateMemory stateMemory;
    private final StateTransitionsInProjectService stateTransitionsInProjectService;

    public AdjacentStates(AllStatesInProjectService allStatesInProjectService, StateMemory stateMemory,
                          StateTransitionsInProjectService stateTransitionsInProjectService) {
        this.allStatesInProjectService = allStatesInProjectService;
        this.stateMemory = stateMemory;
        this.stateTransitionsInProjectService = stateTransitionsInProjectService;
    }

    public Set<Long> getAdjacentStates(Long stateId) {
        Set<Long> adjacent = new HashSet<>();
        Optional<StateTransitions> trsOpt = stateTransitionsInProjectService.getTransitions(stateId);
        if (trsOpt.isEmpty()) return adjacent;
        Set<Long> statesWithStaticTransitions = trsOpt.get().getTransitions().stream()
                .filter(t -> t.getActivate() != null && !t.getActivate().isEmpty())
                .flatMap(t -> t.getActivate().stream())
                .collect(Collectors.toSet());
        adjacent.addAll(statesWithStaticTransitions);
        if (!statesWithStaticTransitions.contains(SpecialStateType.PREVIOUS.getId())) return adjacent;
        adjacent.remove(SpecialStateType.PREVIOUS.getId());
        Optional<State> currentState = allStatesInProjectService.getState(stateId);
        if (currentState.isEmpty() || currentState.get().getHiddenStateNames().isEmpty()) return adjacent;
        adjacent.addAll(currentState.get().getHiddenStateIds());
        return adjacent;
    }

    public Set<Long> getAdjacentStates(Set<Long> stateIds) {
        Set<Long> adjacent = new HashSet<>();
        stateIds.forEach(sE -> adjacent.addAll(getAdjacentStates(sE)));
        return adjacent;
    }

    public Set<Long> getAdjacentStates() {
        return getAdjacentStates(stateMemory.getActiveStates());
    }
}
