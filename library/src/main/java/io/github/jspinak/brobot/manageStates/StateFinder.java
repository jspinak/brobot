package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.primatives.enums.SpecialStateType;
import io.github.jspinak.brobot.report.Report;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static io.github.jspinak.brobot.actions.actionOptions.ActionOptions.Action.FIND;

/**
 * Discovers active states through visual pattern matching in the Brobot framework.
 * 
 * <p>StateFinder provides mechanisms to identify which states are currently active in the GUI 
 * by searching for their associated visual patterns. This is essential for recovering from 
 * lost context, initializing automation, or maintaining awareness of the current application 
 * state during long-running automation sessions.</p>
 * 
 * <p>Key operations:
 * <ul>
 *   <li><b>Check Active States</b>: Verify if currently tracked states are still active</li>
 *   <li><b>Rebuild Active States</b>: Full discovery when context is lost</li>
 *   <li><b>Search All States</b>: Comprehensive scan of all defined states</li>
 *   <li><b>Find Specific State</b>: Check if a particular state is active</li>
 *   <li><b>Refresh States</b>: Complete reset and rediscovery</li>
 * </ul>
 * </p>
 * 
 * <p>Performance considerations:
 * <ul>
 *   <li>Full state search is computationally expensive (O(n) with n = total images)</li>
 *   <li>Checking existing active states is more efficient than full search</li>
 *   <li>Future optimization could use machine learning for instant state recognition</li>
 *   <li>Static images in states make ML training data generation feasible</li>
 * </ul>
 * </p>
 * 
 * <p>State discovery strategy:
 * <ol>
 *   <li>First checks if known active states are still visible</li>
 *   <li>If no active states remain, performs comprehensive search</li>
 *   <li>Falls back to UNKNOWN state if no states are found</li>
 *   <li>Updates StateMemory with discovered active states</li>
 * </ol>
 * </p>
 * 
 * <p>Common use cases:
 * <ul>
 *   <li>Initializing automation when starting state is unknown</li>
 *   <li>Recovering after application crashes or unexpected navigation</li>
 *   <li>Periodic state validation in long-running automation</li>
 *   <li>Debugging state detection issues</li>
 * </ul>
 * </p>
 * 
 * <p>Future enhancements:
 * <ul>
 *   <li>Neural network-based instant state recognition from screenshots</li>
 *   <li>Probabilistic state detection based on partial matches</li>
 *   <li>Hierarchical search starting with likely states</li>
 *   <li>Caching and optimization for frequently checked states</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, StateFinder serves as the sensory system that connects 
 * the abstract state model to the concrete visual reality of the GUI. It enables the 
 * framework to maintain situational awareness and recover gracefully from unexpected 
 * situations, which is crucial for robust automation.</p>
 * 
 * @since 1.0
 * @see State
 * @see StateMemory
 * @see AllStatesInProjectService
 * @see Action
 */
@Component
public class StateFinder {

    private final AllStatesInProjectService allStatesInProjectService;
    private final StateMemory stateMemory;
    private final Action action;

    public StateFinder(AllStatesInProjectService allStatesInProjectService, StateMemory stateMemory, Action action) {
        this.allStatesInProjectService = allStatesInProjectService;
        this.stateMemory = stateMemory;
        this.action = action;
    }

    public void checkForActiveStates() {
        new HashSet<>(stateMemory.getActiveStates()).forEach(state -> {
            if (!findState(state)) stateMemory.removeInactiveState(state);
        });
    }

    public void rebuildActiveStates() {
        checkForActiveStates();
        if (!stateMemory.getActiveStates().isEmpty()) return;
        searchAllImagesForCurrentStates();
        if (stateMemory.getActiveStates().isEmpty()) stateMemory.addActiveState(SpecialStateType.UNKNOWN.getId());
    }

    public void searchAllImagesForCurrentStates() {
        System.out.println("StateFinder: search all states. ");
        Set<String> allStateEnums = allStatesInProjectService.getAllStateNames();
        allStateEnums.remove(SpecialStateType.UNKNOWN.toString());
        allStateEnums.forEach(this::findState);
        Report.println("");
    }

    public boolean findState(String stateName) {
        Report.print(stateName + ".");
        Optional<State> state = allStatesInProjectService.getState(stateName);
        return state.filter(value -> action.perform(FIND, new ObjectCollection.Builder().withNonSharedImages(value).build())
                .isSuccess()).isPresent();
    }

    public boolean findState(Long stateId) {
        Report.print(allStatesInProjectService.getStateName(stateId));
        Optional<State> state = allStatesInProjectService.getState(stateId);
        return state.filter(value -> action.perform(FIND, new ObjectCollection.Builder().withNonSharedImages(value).build())
                .isSuccess()).isPresent();
    }

    public Set<Long> refreshActiveStates() {
        stateMemory.removeAllStates();
        searchAllImagesForCurrentStates();
        return stateMemory.getActiveStates();
    }
}
