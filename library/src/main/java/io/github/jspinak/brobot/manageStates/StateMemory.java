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
 * StateMemory keeps track of which States are currently active.
 * The PREVIOUS enum applies to current hidden States.
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
