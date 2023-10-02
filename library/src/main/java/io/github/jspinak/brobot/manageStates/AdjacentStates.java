package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.primatives.enums.StateEnum;
import io.github.jspinak.brobot.services.StateService;
import io.github.jspinak.brobot.services.StateTransitionsService;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static io.github.jspinak.brobot.manageStates.StateMemory.Enum.PREVIOUS;

@Component
public class AdjacentStates {

    private StateService stateService;
    private StateMemory stateMemory;
    private StateTransitionsService stateTransitionsService;

    public AdjacentStates(StateService stateService, StateMemory stateMemory,
                          StateTransitionsService stateTransitionsService) {
        this.stateService = stateService;
        this.stateMemory = stateMemory;
        this.stateTransitionsService = stateTransitionsService;
    }

    public Set<StateEnum> getAdjacentStates(StateEnum stateEnum) {
        Set<StateEnum> adjacent = new HashSet<>();
        Optional<StateTransitions> trsOpt = stateTransitionsService.getTransitions(stateEnum);
        if (trsOpt.isEmpty()) return adjacent;
        Set<StateEnum> statesWithStaticTransitions = trsOpt.get().getTransitions().keySet();
        adjacent.addAll(statesWithStaticTransitions);
        if (!statesWithStaticTransitions.contains(PREVIOUS)) return adjacent;
        adjacent.remove(PREVIOUS);
        Optional<State> currentState = stateService.findByName(stateEnum);
        if (currentState.isEmpty() || currentState.get().getHidden().isEmpty()) return adjacent;
        adjacent.addAll(currentState.get().getHidden());
        return adjacent;
    }

    public Set<StateEnum> getAdjacentStates(Set<StateEnum> stateEnums) {
        Set<StateEnum> adjacent = new HashSet<>();
        stateEnums.forEach(sE -> adjacent.addAll(getAdjacentStates(sE)));
        return adjacent;
    }

    public Set<StateEnum> getAdjacentStates() {
        return getAdjacentStates(stateMemory.getActiveStates());
    }
}
