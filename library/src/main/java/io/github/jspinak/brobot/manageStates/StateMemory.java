package io.github.jspinak.brobot.manageStates;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.primatives.enums.SpecialStateType;
import io.github.jspinak.brobot.primatives.enums.StateEnum;
import io.github.jspinak.brobot.report.Report;
import lombok.Getter;

/**
 * Maintains the runtime memory of active States in the Brobot framework.
 * 
 * <p>StateMemory is a critical component of the State Management System (μ), responsible 
 * for tracking which states are currently active in the GUI. It serves as the framework's 
 * working memory, maintaining an accurate understanding of the current GUI configuration 
 * throughout automation execution.</p>
 * 
 * <p>Key responsibilities:
 * <ul>
 *   <li><b>Active State Tracking</b>: Maintains a set of currently visible/active states</li>
 *   <li><b>State Transitions</b>: Updates active states as the GUI changes</li>
 *   <li><b>Match Integration</b>: Adjusts active states based on what is found during Find operations</li>
 *   <li><b>State Probability</b>: Manages probability values for mock testing and uncertainty handling</li>
 *   <li><b>Visit Tracking</b>: Records state visit counts for analysis and optimization</li>
 * </ul>
 * </p>
 * 
 * <p>Special state handling:
 * <ul>
 *   <li><b>PREVIOUS</b>: References states that are currently hidden but can be returned to</li>
 *   <li><b>CURRENT</b>: The set of currently active states</li>
 *   <li><b>EXPECTED</b>: States anticipated to become active after transitions</li>
 *   <li><b>NULL State</b>: Ignored in active state tracking as it represents stateless elements</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, StateMemory bridges the gap between the static state 
 * structure and the dynamic runtime behavior of the GUI. It enables the framework to 
 * maintain context awareness, recover from unexpected situations, and make intelligent 
 * decisions about navigation and action execution.</p>
 * 
 * @since 1.0
 * @see State
 * @see StateTransitions
 * @see StateFinder
 * @see AllStatesInProjectService
 */
@Component
@Getter
public class StateMemory {

    private final AllStatesInProjectService allStates;

    public enum Enum implements StateEnum {
        PREVIOUS, CURRENT, EXPECTED
    }

    private Set<Long> activeStates = new HashSet<>();

    public StateMemory(AllStatesInProjectService allStatesInProjectService) {
        this.allStates = allStatesInProjectService;
    }

    public List<State> getActiveStateList() {
        List<State> activeStateList = new ArrayList<>();
        for (Long stateId : activeStates) {
            Optional<State> stateOpt = allStates.getState(stateId);
            stateOpt.ifPresent(activeStateList::add);
        }
        return activeStateList;
    }

    public List<String> getActiveStateNames() {
        List<String> activeStateNames = new ArrayList<>();
        for (Long stateId : activeStates) {
            Optional<State> stateOpt = allStates.getState(stateId);
            stateOpt.ifPresent(state -> activeStateNames.add(state.getName()));
        }
        return activeStateNames;
    }

    public String getActiveStateNamesAsString() {
        return String.join(", ", getActiveStateNames());
    }

    public void adjustActiveStatesWithMatches(Matches matches) {
        matches.getMatchList().forEach(match -> {
            if (match.getStateObjectData() != null) {
                Long ownerStateId = match.getStateObjectData().getOwnerStateId();
                if (ownerStateId != null && ownerStateId > 0) addActiveState(ownerStateId);
            }
        });
    }

    public void addActiveState(Long activeState) {
        addActiveState(activeState, false);
    }

    private boolean isNullState(Long state) {
        return state.equals(SpecialStateType.NULL.getId());
    }

    public void addActiveState(Long activeState, boolean newLine) {
        if (activeStates.contains(activeState)) return;
        if (isNullState(activeState)) return;
        Report.print("+ add state "+allStates.getStateName(activeState)+" to active states ");
        if (newLine) Report.println();
        activeStates.add(activeState);
        allStates.getState(activeState).ifPresent(state -> {
            state.setProbabilityExists(100);
            state.addVisit();
        });
    }

    public void removeInactiveStates(Set<Long> inactiveStates) {
        inactiveStates.forEach(this::removeInactiveState);
    }

    public void removeInactiveState(Long inactiveState) {
        if (!activeStates.contains(inactiveState)) return;
        Report.println("- remove "+inactiveState+" from active states");
        activeStates.remove(inactiveState);
        allStates.getState(inactiveState).ifPresent(state -> state.setProbabilityExists(0));
    }

    public void removeAllStates() {
        activeStates = new HashSet<>();
    }

}
