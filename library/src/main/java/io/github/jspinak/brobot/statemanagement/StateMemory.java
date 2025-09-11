package io.github.jspinak.brobot.statemanagement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.StateEnum;
import io.github.jspinak.brobot.model.state.special.SpecialStateType;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.navigation.transition.StateTransitions;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;

import lombok.Getter;

/**
 * Maintains the runtime memory of active States in the Brobot framework.
 *
 * <p>StateMemory is a critical component of the State Management System (μ), responsible for
 * tracking which states are currently active in the GUI. It serves as the framework's working
 * memory, maintaining an accurate understanding of the current GUI configuration throughout
 * automation execution.
 *
 * <p>Key responsibilities:
 *
 * <ul>
 *   <li><b>Active State Tracking</b>: Maintains a set of currently visible/active states
 *   <li><b>State Transitions</b>: Updates active states as the GUI changes
 *   <li><b>Match Integration</b>: Adjusts active states based on what is found during Find
 *       operations
 *   <li><b>State Probability</b>: Manages probability values for mock testing and uncertainty
 *       handling
 *   <li><b>Visit Tracking</b>: Records state visit counts for analysis and optimization
 * </ul>
 *
 * <p>Special state handling:
 *
 * <ul>
 *   <li><b>PREVIOUS</b>: References states that are currently hidden but can be returned to
 *   <li><b>CURRENT</b>: The set of currently active states
 *   <li><b>EXPECTED</b>: States anticipated to become active after transitions
 *   <li><b>NULL State</b>: Ignored in active state tracking as it represents stateless elements
 * </ul>
 *
 * <p>In the model-based approach, StateMemory bridges the gap between the static state structure
 * and the dynamic runtime behavior of the GUI. It enables the framework to maintain context
 * awareness, recover from unexpected situations, and make intelligent decisions about navigation
 * and action execution.
 *
 * @since 1.0
 * @see State
 * @see StateTransitions
 * @see StateDetector
 * @see StateService
 */
@Component
@Getter
public class StateMemory {

    private final StateService stateService;

    public enum Enum implements StateEnum {
        PREVIOUS,
        CURRENT,
        EXPECTED
    }

    private Set<Long> activeStates = new HashSet<>();

    public StateMemory(StateService stateService) {
        this.stateService = stateService;
    }

    /**
     * Retrieves all active states as State objects.
     *
     * <p>Converts the internal set of active state IDs to a list of State objects by looking up
     * each ID in the StateService. Invalid IDs are silently skipped.
     *
     * @return List of currently active State objects, empty if no states active
     * @see StateService#getState(Long)
     */
    public List<State> getActiveStateList() {
        List<State> activeStateList = new ArrayList<>();
        for (Long stateId : activeStates) {
            Optional<State> stateOpt = stateService.getState(stateId);
            stateOpt.ifPresent(activeStateList::add);
        }
        return activeStateList;
    }

    /**
     * Retrieves names of all active states.
     *
     * <p>Provides a human-readable list of active state names for debugging, logging, and display
     * purposes. Names are extracted from State objects via StateService lookup.
     *
     * @return List of active state names, empty if no states active
     */
    public List<String> getActiveStateNames() {
        List<String> activeStateNames = new ArrayList<>();
        for (Long stateId : activeStates) {
            Optional<State> stateOpt = stateService.getState(stateId);
            stateOpt.ifPresent(state -> activeStateNames.add(state.getName()));
        }
        return activeStateNames;
    }

    /**
     * Formats active state names as a comma-separated string.
     *
     * <p>Convenience method for displaying active states in logs and reports. Useful for concise
     * status messages and debugging output.
     *
     * @return Comma-separated string of active state names
     */
    public String getActiveStateNamesAsString() {
        return String.join(", ", getActiveStateNames());
    }

    /**
     * Updates active states based on matches found during Find operations.
     *
     * <p>When the Find action discovers state objects on screen, this method ensures their owning
     * states are marked as active. This automatic state discovery helps maintain accurate state
     * tracking even when states change unexpectedly.
     *
     * <p>Side effects:
     *
     * <ul>
     *   <li>Adds states to active list based on found matches
     *   <li>Updates state probabilities and visit counts
     *   <li>Ignores matches without valid state ownership data
     * </ul>
     *
     * @param matches ActionResult containing found state objects
     * @see ActionResult#getMatchList()
     */
    public void adjustActiveStatesWithMatches(ActionResult matches) {
        matches.getMatchList()
                .forEach(
                        match -> {
                            if (match.getStateObjectData() != null) {
                                Long ownerStateId = match.getStateObjectData().getOwnerStateId();
                                if (ownerStateId != null && ownerStateId > 0)
                                    addActiveState(ownerStateId);
                            }
                        });
    }

    /**
     * Adds a state to the active state list.
     *
     * <p>Public convenience method that delegates to the private implementation without forcing a
     * newline in the report output.
     *
     * @param activeState ID of the state to mark as active
     */
    public void addActiveState(Long activeState) {
        addActiveState(activeState, false);
    }

    /**
     * Checks if a state ID represents the NULL state.
     *
     * <p>NULL states are special states representing stateless objects and should not be tracked in
     * the active state list.
     *
     * @param state State ID to check
     * @return true if this is the NULL state ID
     */
    private boolean isNullState(Long state) {
        return state.equals(SpecialStateType.NULL.getId());
    }

    /**
     * Adds a state to the active state list with reporting options.
     *
     * <p>Marks a state as currently active in the GUI. Performs several operations:
     *
     * <ul>
     *   <li>Checks for duplicate additions (idempotent)
     *   <li>Filters out NULL states
     *   <li>Reports the addition to logs
     *   <li>Sets state probability to 100%
     *   <li>Increments state visit counter
     * </ul>
     *
     * <p>Side effects:
     *
     * <ul>
     *   <li>Modifies active state set
     *   <li>Updates state probability to 100
     *   <li>Increments state visit count
     *   <li>Prints to Report
     * </ul>
     *
     * @param activeState ID of the state to mark as active
     * @param newLine Whether to print a newline after the report message
     */
    public void addActiveState(Long activeState, boolean newLine) {
        if (activeStates.contains(activeState)) return;
        if (isNullState(activeState)) return;
        ConsoleReporter.print(
                "+ add state " + stateService.getStateName(activeState) + " to active states ");
        if (newLine) ConsoleReporter.println();
        activeStates.add(activeState);
        stateService
                .getState(activeState)
                .ifPresent(
                        state -> {
                            state.setProbabilityExists(100);
                            state.addVisit();
                        });
    }

    /**
     * Removes multiple states from the active state list.
     *
     * <p>Batch operation for removing states that are no longer active. Delegates to
     * removeInactiveState for each state in the set.
     *
     * @param inactiveStates Set of state IDs to remove from active list
     * @see #removeInactiveState(Long)
     */
    public void removeInactiveStates(Set<Long> inactiveStates) {
        inactiveStates.forEach(this::removeInactiveState);
    }

    /**
     * Removes a state from the active state list.
     *
     * <p>Marks a state as no longer active. Updates the state's probability to 0 and reports the
     * removal. Idempotent - removing an already inactive state has no effect.
     *
     * <p>Side effects:
     *
     * <ul>
     *   <li>Removes state from active set
     *   <li>Sets state probability to 0
     *   <li>Prints removal to Report
     * </ul>
     *
     * @param inactiveState ID of the state to mark as inactive
     */
    public void removeInactiveState(Long inactiveState) {
        if (!activeStates.contains(inactiveState)) return;
        ConsoleReporter.println("- remove " + inactiveState + " from active states");
        activeStates.remove(inactiveState);
        stateService.getState(inactiveState).ifPresent(state -> state.setProbabilityExists(0));
    }

    /**
     * Removes a state from the active state list by name.
     *
     * <p>Convenience method that retrieves the state ID by name and calls removeInactiveState with
     * it. If the state does not exist, it reports that the removal was attempted but the state was
     * not found.
     *
     * @param inactiveStateName Name of the state to remove from active states
     */
    public void removeInactiveState(String inactiveStateName) {
        Long inactiveStateId = stateService.getStateId(inactiveStateName);
        if (inactiveStateId != null) {
            removeInactiveState(inactiveStateId);
        } else {
            ConsoleReporter.println(
                    "- remove " + inactiveStateName + " from active states (not found)");
        }
    }

    /**
     * Clears all active states from memory.
     *
     * <p>Completely resets the active state tracking by creating a new empty set. Used when the
     * automation needs to start fresh or when all states become invalid. Does not update individual
     * state probabilities.
     *
     * <p>Side effects:
     *
     * <ul>
     *   <li>Replaces active state set with empty set
     *   <li>Does NOT update state probabilities
     * </ul>
     */
    public void removeAllStates() {
        activeStates = new HashSet<>();
    }
}
