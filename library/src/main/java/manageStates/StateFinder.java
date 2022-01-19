package manageStates;

import com.brobot.multimodule.actions.actionExecution.Action;
import com.brobot.multimodule.database.state.ObjectCollection;
import com.brobot.multimodule.database.state.state.State;
import com.brobot.multimodule.primatives.enums.StateEnum;
import com.brobot.multimodule.reports.Report;
import com.brobot.multimodule.services.StateService;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.brobot.multimodule.actions.actionOptions.ActionOptions.Action.FIND;
import static com.brobot.multimodule.manageStates.UnknownState.Enum.UNKNOWN;

/**
 * This class finds the active States after Brobot is lost.
 * It is a costly operation since all Images in all States are searched.
 * In a future version of Brobot I hope to run a screenshot through a
 * neural net to get immediately the set of active States. One problem
 * with this approach is that it may take a long time before an
 * automation can produce enough labeled data (screenshots with labeled States)
 * in order to achieve an effective network, although the amount of data needed
 * may not be that large given that States have static images associated with them.
 */
@Component
public class StateFinder {

    private StateService stateService;
    private StateMemory stateMemory;
    private Action action;

    public StateFinder(StateService stateService, StateMemory stateMemory, Action action) {
        this.stateService = stateService;
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
        if (stateMemory.getActiveStates().isEmpty()) stateMemory.addActiveState(UNKNOWN);
    }

    private void searchAllImagesForCurrentStates() {
        System.out.println("StateFinder: search all states| ");
        Set<StateEnum> allStateEnums = stateService.findAllStateEnums();
        allStateEnums.remove(UNKNOWN);
        allStateEnums.forEach(this::findState);
        Report.println("");
    }

    public boolean findState(StateEnum stateEnum) {
        Report.print(stateEnum + ".");
        Optional<State> state = stateService.findByName(stateEnum);
        if (state.isEmpty()) return false;
        return action.perform(FIND, new ObjectCollection.Builder().withNonSharedImages(state.get()).build())
                .isSuccess();
    }

}
