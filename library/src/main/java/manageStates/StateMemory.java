package manageStates;

import com.brobot.multimodule.database.primitives.match.Matches;
import com.brobot.multimodule.primatives.enums.StateEnum;
import com.brobot.multimodule.reports.Report;
import com.brobot.multimodule.services.StateService;
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
    private StateService stateService;

    public enum Enum implements StateEnum {
        PREVIOUS, CURRENT, EXPECTED
    }

    private Set<StateEnum> activeStates = new HashSet<>();

    public StateMemory(StateService stateService) {
        this.stateService = stateService;
    }

    public void adjustActiveStatesWithMatches(Matches matches) {
        matches.getMatchObjects().forEach(
                mO -> addActiveState(mO.getStateObject().getOwnerStateName()));
    }

    public void addActiveState(StateEnum activeState) {
        addActiveState(activeState, false);
    }

    public void addActiveState(StateEnum activeState, boolean newLine) {
        if (activeStates.contains(activeState)) return;
        Report.print("+ add "+activeState+" to active states ");
        if (newLine) Report.println();
        activeStates.add(activeState);
        stateService.findByName(activeState).ifPresent(state -> state.setProbabilityExists(100));
    }

    public void removeInactiveStates(Set<StateEnum> inactiveStates) {
        inactiveStates.forEach(this::removeInactiveState);
    }

    public void removeInactiveState(StateEnum inactiveState) {
        if (!activeStates.contains(inactiveState)) return;
        Report.println("- remove "+inactiveState+" from active states");
        activeStates.remove(inactiveState);
        stateService.findByName(inactiveState).ifPresent(state -> state.setProbabilityExists(0));
    }

}
