package io.github.jspinak.brobot.statemanagement;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.special.SpecialStateType;
import io.github.jspinak.brobot.navigation.service.StateService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Discovers active states through visual pattern matching in the Brobot framework.
 *
 * <p>StateDetector provides mechanisms to identify which states are currently active in the GUI by
 * searching for their associated visual patterns. This is essential for recovering from lost
 * context, initializing automation, or maintaining awareness of the current application state
 * during long-running automation sessions.
 *
 * <p>Key operations:
 *
 * <ul>
 *   <li><b>Check Active States</b>: Verify if currently tracked states are still active
 *   <li><b>Rebuild Active States</b>: Full discovery when context is lost
 *   <li><b>Search All States</b>: Comprehensive scan of all defined states
 *   <li><b>Find Specific State</b>: Check if a particular state is active
 *   <li><b>Refresh States</b>: Complete reset and rediscovery
 * </ul>
 *
 * <p>Performance considerations:
 *
 * <ul>
 *   <li>Full state search is computationally expensive (O(n) with n = total images)
 *   <li>Checking existing active states is more efficient than full search
 *   <li>Future optimization could use machine learning for instant state recognition
 *   <li>Static images in states make ML training data generation feasible
 * </ul>
 *
 * <p>State discovery strategy:
 *
 * <ol>
 *   <li>First checks if known active states are still visible
 *   <li>If no active states remain, performs comprehensive search
 *   <li>Falls back to UNKNOWN state if no states are found
 *   <li>Updates StateMemory with discovered active states
 * </ol>
 *
 * <p>Common use cases:
 *
 * <ul>
 *   <li>Initializing automation when starting state is unknown
 *   <li>Recovering after application crashes or unexpected navigation
 *   <li>Periodic state validation in long-running automation
 *   <li>Debugging state detection issues
 * </ul>
 *
 * <p>Future enhancements:
 *
 * <ul>
 *   <li>Neural network-based instant state recognition from screenshots
 *   <li>Probabilistic state detection based on partial matches
 *   <li>Hierarchical search starting with likely states
 *   <li>Caching and optimization for frequently checked states
 * </ul>
 *
 * <p>In the model-based approach, StateDetector serves as the sensory system that connects the
 * abstract state model to the concrete visual reality of the GUI. It enables the framework to
 * maintain situational awareness and recover gracefully from unexpected situations, which is
 * crucial for robust automation.
 *
 * @since 1.0
 * @see State
 * @see StateMemory
 * @see StateService
 * @see Action
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StateDetector {

    private final StateService allStatesInProjectService;
    private final StateMemory stateMemory;
    private final Action action;

    /**
     * Verifies that currently active states are still visible on screen.
     *
     * <p>Iterates through all states marked as active in StateMemory and checks if they can still
     * be found on the screen. States that are no longer visible are removed from the active state
     * list. Uses a HashSet copy to avoid concurrent modification while removing states.
     *
     * <p>Side effects:
     *
     * <ul>
     *   <li>Removes states from StateMemory that are no longer visible
     *   <li>May result in empty active state list if no states remain
     * </ul>
     */
    public void checkForActiveStates() {
        Set<Long> currentActiveStates = new HashSet<>(stateMemory.getActiveStates());
        log.debug("Checking {} active states for visibility", currentActiveStates.size());

        Set<Long> statesNoLongerVisible =
                currentActiveStates.stream()
                        .filter(stateId -> !findState(stateId))
                        .collect(Collectors.toSet());

        statesNoLongerVisible.forEach(stateMemory::removeInactiveState);

        if (!statesNoLongerVisible.isEmpty()) {
            log.info(
                    "Removed {} states that are no longer visible: {}",
                    statesNoLongerVisible.size(),
                    statesNoLongerVisible.stream()
                            .map(allStatesInProjectService::getStateName)
                            .collect(Collectors.joining(", ")));
        }
    }

    /**
     * Rebuilds the active state list when context is lost or uncertain.
     *
     * <p>First attempts to verify existing active states. If no states remain active after
     * verification, performs a comprehensive search of all defined states. If still no states are
     * found, falls back to the UNKNOWN state to prevent an empty active state list.
     *
     * <p>This method implements a three-tier recovery strategy:
     *
     * <ol>
     *   <li>Verify known active states (fast)
     *   <li>Search all states if needed (slow)
     *   <li>Default to UNKNOWN if nothing found
     * </ol>
     *
     * @see #checkForActiveStates()
     * @see #searchAllImagesForCurrentStates()
     */
    public void rebuildActiveStates() {
        log.info("Rebuilding active states");
        checkForActiveStates();

        if (!stateMemory.getActiveStates().isEmpty()) {
            log.info(
                    "Active states still present after verification: {}",
                    stateMemory.getActiveStateNames());
            return;
        }

        log.info("No active states found, performing comprehensive search");
        searchAllImagesForCurrentStates();

        if (stateMemory.getActiveStates().isEmpty()) {
            log.warn("No states found after comprehensive search, defaulting to UNKNOWN state");
            stateMemory.addActiveState(SpecialStateType.UNKNOWN.getId());
        } else {
            log.info("Rebuilt active states: {}", stateMemory.getActiveStateNames());
        }
    }

    /**
     * Performs comprehensive search for all defined states on the current screen.
     *
     * <p>Searches for every state in the project (except UNKNOWN) to build a complete picture of
     * which states are currently active. This is a computationally expensive operation that should
     * be used sparingly.
     *
     * <p>Side effects:
     *
     * <ul>
     *   <li>Updates StateMemory with all found states
     *   <li>Prints progress dots to Report for monitoring
     *   <li>May take significant time with many states/images
     * </ul>
     *
     * <p>Performance note: O(n*m) where n = number of states, m = images per state
     */
    public void searchAllImagesForCurrentStates() {
        log.info("Starting comprehensive state search");
        Set<String> allStateEnums = allStatesInProjectService.getAllStateNames();
        allStateEnums.remove(SpecialStateType.UNKNOWN.toString());

        int totalStates = allStateEnums.size();
        int found = 0;

        for (String stateName : allStateEnums) {
            if (findState(stateName)) {
                found++;
            }
        }

        log.info("Comprehensive search complete: found {} of {} states", found, totalStates);
    }

    /**
     * Searches for a specific state by name on the current screen.
     *
     * <p>Attempts to find visual patterns associated with the named state. If found, the state is
     * automatically added to StateMemory's active list by the Action framework. Progress is
     * reported for debugging.
     *
     * @param stateName Name of the state to search for
     * @return true if the state was found on screen, false otherwise
     * @see Action#perform(ActionType, ObjectCollection)
     */
    public boolean findState(String stateName) {
        log.debug("Searching for state: {}", stateName);
        Optional<State> state = allStatesInProjectService.getState(stateName);

        if (state.isEmpty()) {
            log.warn("State '{}' not found in service", stateName);
            return false;
        }

        // The state will be automatically activated in StateMemory by ActionExecution
        // when patterns are found
        boolean found =
                action.find(new ObjectCollection.Builder().withNonSharedImages(state.get()).build())
                        .isSuccess();

        if (found) {
            log.debug("State '{}' found and activated", stateName);
        }

        return found;
    }

    /**
     * Searches for a specific state by ID on the current screen.
     *
     * <p>Attempts to find visual patterns associated with the state ID. If found, the state is
     * automatically added to StateMemory's active list by the Action framework. State name is
     * printed for debugging.
     *
     * @param stateId ID of the state to search for
     * @return true if the state was found on screen, false otherwise
     * @see Action#perform(ActionType, ObjectCollection)
     */
    public boolean findState(Long stateId) {
        String stateName = allStatesInProjectService.getStateName(stateId);
        log.debug("Searching for state by ID: {} ({})", stateId, stateName);

        Optional<State> state = allStatesInProjectService.getState(stateId);

        if (state.isEmpty()) {
            log.warn("State with ID {} not found in service", stateId);
            return false;
        }

        // The state will be automatically activated in StateMemory by ActionExecution
        // when patterns are found
        boolean found =
                action.find(new ObjectCollection.Builder().withNonSharedImages(state.get()).build())
                        .isSuccess();

        if (found) {
            log.debug("State '{}' (ID: {}) found and activated", stateName, stateId);
        }

        return found;
    }

    /**
     * Completely resets and rediscovers all active states.
     *
     * <p>Clears all existing active state information and performs a fresh comprehensive search.
     * This provides a clean slate when the automation needs to re-establish its position from
     * scratch.
     *
     * <p>Side effects:
     *
     * <ul>
     *   <li>Clears all states from StateMemory
     *   <li>Performs full state search (expensive)
     *   <li>Rebuilds active state list from scratch
     * </ul>
     *
     * @return Set of state IDs that were found to be active
     */
    public Set<Long> refreshActiveStates() {
        log.info("Refreshing all active states - clearing and rediscovering");
        stateMemory.removeAllStates();
        searchAllImagesForCurrentStates();

        Set<Long> activeStates = stateMemory.getActiveStates();
        log.info(
                "Refresh complete. Active states: {}",
                activeStates.stream()
                        .map(allStatesInProjectService::getStateName)
                        .collect(Collectors.joining(", ")));

        return activeStates;
    }
}
