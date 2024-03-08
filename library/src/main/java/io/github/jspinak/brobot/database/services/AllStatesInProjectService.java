package io.github.jspinak.brobot.database.services;

import io.github.jspinak.brobot.database.data.AllStatesInProject;
import io.github.jspinak.brobot.datatypes.state.state.State;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Manages the State repository.
 * Saves new states and retrieves states given the state name. StateEnums are typically used as state names
 *   for state structures built with named image files, or manually built state structures. Strings are used
 *   for state structures created at run-time or dynamically evolving state structures.
 **/
@Component
public class AllStatesInProjectService {

    private final AllStatesInProject allStatesInProject;

    public AllStatesInProjectService(AllStatesInProject allStatesInProject) {
        this.allStatesInProject = allStatesInProject;
    }

    public Optional<State> getState(String name) {
        return allStatesInProject.getState(name);
    }

    public List<State> getAllStates() {
        return allStatesInProject.getAllStates();
    }

    public Set<String> getAllStateNames() {
        return getAllStates().stream()
                .map(State::getName)
                .collect(Collectors.toSet());
    }

    public Set<State> findSetByName(String... stateNames) {
        Set<State> states = new HashSet<>();
        Stream.of(stateNames).forEach(name -> getState(name).ifPresent(states::add));
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
        Stream.of(stateNames).forEach(name -> getState(name).ifPresent(states::add));
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
        allStatesInProject.save(state);
    }

    public void resetTimesVisited() {getAllStates().forEach(state -> state.setTimesVisited(0));
    }

    public void deleteAllStates() {
        allStatesInProject.deleteAll();
    }

    public void removeState(State state) {
        allStatesInProject.delete(state);
    }

    public List<State> getAllInProject(Long projectId) {
        return getAllStates().stream()
                .filter(state -> state.getProjectId().equals(projectId))
                .collect(Collectors.toList());
    }
}
