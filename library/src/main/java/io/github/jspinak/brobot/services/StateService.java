package io.github.jspinak.brobot.services;

import io.github.jspinak.brobot.datatypes.state.state.State;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Stream;

/**
 * Manages the State repository.
 * Saves new States and retrieves States given the state name. StateEnums are typically used for state structures
 *   built with named image files, or manually built state structures. Strings are used for state structures
 *   created at run-time or dynamically evolving state structures.
 * Assigns index values to each StateImageObject that are unique to the entire application.
 *   These indices are used with sparse matrices for classification.
 *   If a StateImageObject is not added to the State variable, it will not receive an index and not be
 *   available for classification.
 */
@Component
public class StateService {

    private Map<String, State> stateRepository = new HashMap<>();

    public Set<State> findAllStates() {
        return new HashSet<>(stateRepository.values());
    }

    public Set<String> findAllStateNames() {
        return stateRepository.keySet();
    }

    public Optional<State> findByName(String stateName) {
        if (stateName == null) return Optional.empty();
        return Optional.ofNullable(stateRepository.get(stateName));
    }

    public Set<State> findSetByName(String... stateNames) {
        Set<State> states = new HashSet<>();
        Stream.of(stateNames).forEach(name -> findByName(name).ifPresent(states::add));
        return states;
    }

    public Set<State> findSetByName(Set<String> stateNames) {
        return findSetByName(stateNames.toArray(new String[0]));
    }

    public State[] findArrayByName(Set<String> stateNames) {
        return findArrayByName(stateNames.toArray(new String[0]));
    }

    public State[] findArrayByName(String... stateNames) {
        List<State> states = new ArrayList<>();
        Stream.of(stateNames).forEach(name -> findByName(name).ifPresent(states::add));
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
        stateRepository.put(state.getNameAsString(), state);
    }

    public void resetTimesVisited() {
        stateRepository.values().forEach(state -> state.setTimesVisited(0));
    }

    public void deleteAllStates() {
        stateRepository = new HashMap<>();
    }

}
