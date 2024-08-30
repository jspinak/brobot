package io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.buildStateStructure;

import io.github.jspinak.brobot.app.services.StateService;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.manageStates.StateTransitions;
import io.github.jspinak.brobot.services.StateTransitionsInProjectService;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Provides support functions for the state structure.
 * - print out the state structure (especially useful when the state structure resides in memory only)
 */
@Component
public class StateStructureInfo {

    private final StateService stateService;
    private final StateTransitionsInProjectService stateTransitionsInProjectService;

    public StateStructureInfo(StateService stateService, StateTransitionsInProjectService stateTransitionsInProjectService) {
        this.stateService = stateService;
        this.stateTransitionsInProjectService = stateTransitionsInProjectService;
    }

    public void printStateStructure() {
        for (State state : stateService.getAllStates()) {
            System.out.println(state);
            System.out.println();

            Optional<StateTransitions> optTrs = stateTransitionsInProjectService.getTransitions(state.getId());
            if (optTrs.isPresent()) {
                StateTransitions trs = optTrs.get();
                System.out.println("transitions:");
                trs.getTransitions().values().forEach(tr -> {
                    System.out.print(" enter: ");
                    tr.getActivate().forEach(st -> System.out.print(st + " "));
                    System.out.println();
                    System.out.print(" exit: ");
                    tr.getExit().forEach(st -> System.out.print(st + " "));
                    System.out.println();
                    System.out.println(" this state persists after the transition (NONE = no opinion)? "
                            + tr.getStaysVisibleAfterTransition().toString());
                });
            }
        }
    }
}
