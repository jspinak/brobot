package io.github.jspinak.brobot.services;

import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.SetAllProfiles;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.SetKMeansProfiles;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import io.github.jspinak.brobot.primatives.enums.StateEnum;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Stream;

/**
 * Manages the State repository.
 * Saves new States and retrieves States given StateEnums.
 * Assigns index values to each StateImageObject that are unique to the entire application.
 *   These indices are used with sparse matrices for classification.
 *   If a StateImageObject is not added to the State variable, it will not receive an index and not be
 *   available for classification.
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

    /**
     * Adds a State to the repository.
     * Initial image processing can't take place here because, in the client app, the beans
     * are loaded before the bundle path can be set. Instead, initialization takes place in the
     * class "Init".
     *
     * @param state The State to add.
     */
    public void save(State state) {
        if (state == null) return;
        stateRepository.put(state.getName(), state);
    }

}
