package io.github.jspinak.brobot.services;

import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.primatives.enums.StateEnum;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Stream;

/**
 * Manages the State repository. Saves new States and retrieves States given StateEnums.
 */
@Component
public class StateService {

    private Map<StateEnum, State> stateRepository = new HashMap<>();

    public Set<State> findAllStates() {
        return new HashSet<>(stateRepository.values());
    }

    public Set<StateEnum> findAllStateEnums() {
        return stateRepository.keySet();
    }

    public Optional<State> findByName(StateEnum stateName) {
        if (stateName == null) return Optional.empty();
        return Optional.ofNullable(stateRepository.get(stateName));
    }

    public Set<State> findSetByName(StateEnum... stateEnums) {
        Set<State> states = new HashSet<>();
        Stream.of(stateEnums).forEach(stateEnum -> findByName(stateEnum).ifPresent(states::add));
        return states;
    }

    public Set<State> findSetByName(Set<StateEnum> stateEnums) {
        return findSetByName(stateEnums.toArray(new StateEnum[0]));
    }

    public State[] findArrayByName(Set<StateEnum> stateEnums) {
        return findArrayByName(stateEnums.toArray(new StateEnum[0]));
    }

    public State[] findArrayByName(StateEnum... stateEnums) {
        List<State> states = new ArrayList<>();
        Stream.of(stateEnums).forEach(stateEnum -> findByName(stateEnum).ifPresent(states::add));
        return states.toArray(new State[0]);
    }

    public void save(State state) {
        if (state == null) return;
        stateRepository.put(state.getName(), state);
    }

}
