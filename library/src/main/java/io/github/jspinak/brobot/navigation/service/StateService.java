package io.github.jspinak.brobot.navigation.service;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateStore;
import io.github.jspinak.brobot.runner.project.AutomationProjectManager;

import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service layer for managing states within the active project.
 * 
 * <p>StateService provides a comprehensive API for state management, serving as the primary 
 * interface between the automation framework and the underlying state repository. It handles 
 * all CRUD operations for states, supports multiple query patterns, and maintains the integrity 
 * of the state structure that forms the foundation of model-based GUI automation.</p>
 * 
 * <p>Key responsibilities:
 * <ul>
 *   <li><b>State Retrieval</b>: Query states by ID, name, or in bulk collections</li>
 *   <li><b>State Persistence</b>: Save new states and manage the state lifecycle</li>
 *   <li><b>Name/ID Resolution</b>: Convert between human-readable names and internal IDs</li>
 *   <li><b>Bulk Operations</b>: Handle sets and arrays of states efficiently</li>
 *   <li><b>State Validation</b>: Check for special conditions like unknown-only state</li>
 * </ul>
 * </p>
 * 
 * <p>State identification patterns:
 * <ul>
 *   <li><b>StateEnums</b>: Used for compile-time safe references in code-based definitions</li>
 *   <li><b>String Names</b>: Support dynamic state creation and runtime flexibility</li>
 *   <li><b>Long IDs</b>: Internal identifiers for efficient processing and relationships</li>
 * </ul>
 * </p>
 * 
 * <p>Common usage patterns:
 * <ul>
 *   <li>Retrieve states during navigation decisions</li>
 *   <li>Save states during project initialization or dynamic creation</li>
 *   <li>Query state collections for pathfinding algorithms</li>
 *   <li>Reset visit counters for fresh automation runs</li>
 * </ul>
 * </p>
 * 
 * <p>Performance considerations:
 * <ul>
 *   <li>Delegates to StateStore for actual storage operations</li>
 *   <li>Returns Optional to handle missing states gracefully</li>
 *   <li>Supports bulk operations to minimize repository calls</li>
 *   <li>Caches results where appropriate in StateStore implementation</li>
 * </ul>
 * </p>
 * 
 * <p>State lifecycle notes:
 * <ul>
 *   <li>States are typically created during project initialization</li>
 *   <li>Image processing happens separately in Init class due to bean loading order</li>
 *   <li>States can be dynamically added during runtime for adaptive automation</li>
 *   <li>Deletion is supported but rarely used in production scenarios</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, StateService is the guardian of the state structure (S) 
 * that defines all possible GUI configurations. It ensures consistent access to state 
 * information across all framework components, enabling the navigation, transition, and 
 * action systems to work with a unified view of the application model.</p>
 * 
 * @since 1.0
 * @see State
 * @see StateStore
 * @see StateTransitionService
 * @see AutomationProjectManager
 */
@Component
public class StateService {

    private final StateStore stateStore;

    public StateService(StateStore allStatesInProject) {
        this.stateStore = allStatesInProject;
    }

    /**
     * Retrieves a state by its name.
     *
     * @param name The state name to search for
     * @return Optional containing the state if found, empty otherwise
     */
    public Optional<State> getState(String name) {
        return stateStore.getState(name);
    }

    /**
     * Retrieves a state by its ID.
     *
     * @param id The state ID to search for
     * @return Optional containing the state if found, empty otherwise
     */
    public Optional<State> getState(Long id) {
        return stateStore.getState(id);
    }

    /**
     * Retrieves a state name by its ID.
     * <p>
     * Convenience method for when only the name is needed.
     *
     * @param id The state ID
     * @return State name if found, null otherwise
     */
    public String getStateName(Long id) {
        return stateStore.getState(id).map(State::getName).orElse(null);
    }

    /**
     * Retrieves a state ID by its name.
     * <p>
     * Useful for converting human-readable names to internal IDs.
     *
     * @param name The state name
     * @return State ID if found, null otherwise
     */
    public Long getStateId(String name) {
        return stateStore.getState(name).map(State::getId).orElse(null);
    }

    public List<State> getAllStates() {
        return stateStore.getAllStates();
    }

    /**
     * Checks if only the unknown state exists in the repository.
     * <p>
     * This condition typically indicates an uninitialized or empty project.
     * The unknown state is a special state representing undefined GUI positions.
     *
     * @return true if only the unknown state exists
     */
    public boolean onlyTheUnknownStateExists() {
        List<State> allStates = stateStore.getAllStates();
        return allStates.size() == 1 &&
                allStates.getFirst().getName().equals("unknown");
    }

    public List<Long> getAllStateIds() {
        return stateStore.getAllStates().stream()
                .map(State::getId)
                .collect(Collectors.toList());
    }

    public Set<String> getAllStateNames() {
        return getAllStates().stream()
                .map(State::getName)
                .collect(Collectors.toSet());
    }

    public Set<State> findSetById(Long... stateId) {
        Set<State> states = new HashSet<>();
        if (stateId != null) {
            Stream.of(stateId).forEach(name -> getState(name).ifPresent(states::add));
        }
        return states;
    }

    public Set<State> findSetById(Set<Long> stateIds) {
        return findSetById(stateIds.toArray(new Long[0]));
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
     * <p>
     * Saves a new state or updates an existing one. Note that initial image
     * processing cannot occur here due to bean loading order - the bundle path
     * must be set first. Image initialization happens later in the Init class.
     * <p>
     * Side effects:
     * <ul>
     *   <li>Adds state to persistent storage</li>
     *   <li>May overwrite existing state with same ID</li>
     *   <li>Does not process state images</li>
     * </ul>
     *
     * @param state The State to add (ignored if null)
     * @see Init
     */
    public void save(State state) {
        if (state == null) return;
        stateStore.save(state);
    }

    /**
     * Resets visit counters for all states.
     * <p>
     * Clears the visit history, useful for starting fresh automation runs
     * or resetting statistics. Side effect: modifies all states in repository.
     */
    public void resetTimesVisited() {
        getAllStates().forEach(state -> state.setTimesVisited(0));
    }

    public void deleteAllStates() {
        stateStore.deleteAll();
    }

    public void removeState(State state) {
        stateStore.delete(state);
    }

    /**
     * Prints all state names to console for debugging.
     * <p>
     * Useful for verifying project loading and state structure.
     */
    public void printAllStates() {
        System.out.println("All States in Project:");
        getAllStates().forEach(state -> System.out.println(state.getName()));
    }

}
