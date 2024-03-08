package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.primatives.enums.StateEnum;
import io.github.jspinak.brobot.reports.Report;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * StateMemory keeps track of which States are currently active.
 * The PREVIOUS enum applies to current hidden States.
 */
@Component
@Getter
public class StateMemory {

    private final AllStatesInProjectService allStatesInProjectService;

    public enum Enum implements StateEnum {
        PREVIOUS, CURRENT, EXPECTED
    }

    private Set<String> activeStates = new HashSet<>();

    public StateMemory(AllStatesInProjectService allStatesInProjectService) {
        this.allStatesInProjectService = allStatesInProjectService;
    }

    public void adjustActiveStatesWithMatches(Matches matches) {
        matches.getMatchList().forEach(match -> {
            if (match.getStateObjectData() != null) addActiveState(match.getStateObjectData().getOwnerStateName());
        });
    }

    public void addActiveState(String activeState) {
        addActiveState(activeState, false);
    }

    private boolean isNullState(String state) {
        if (state.equals("null")) return true;
        if (state.equals("NULL")) return true;
        return false;
    }

    public void addActiveState(String activeState, boolean newLine) {
        if (activeStates.contains(activeState)) return;
        if (isNullState(activeState)) return;
        Report.print("+ add "+activeState+" to active states ");
        if (newLine) Report.println();
        activeStates.add(activeState);
        allStatesInProjectService.getState(activeState).ifPresent(state -> {
            state.setProbabilityExists(100);
            state.addVisit();
        });
    }

    public void removeInactiveStates(Set<String> inactiveStates) {
        inactiveStates.forEach(this::removeInactiveState);
    }

    public void removeInactiveState(String inactiveState) {
        if (!activeStates.contains(inactiveState)) return;
        Report.println("- remove "+inactiveState+" from active states");
        activeStates.remove(inactiveState);
        allStatesInProjectService.getState(inactiveState).ifPresent(state -> state.setProbabilityExists(0));
    }

    public void removeAllStates() {
        activeStates = new HashSet<>();
    }

}
