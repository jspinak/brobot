package manageStates;

import com.brobot.multimodule.actions.BrobotSettings;
import com.brobot.multimodule.database.state.state.State;
import com.brobot.multimodule.primatives.enums.StateEnum;
import com.brobot.multimodule.reports.Report;
import com.brobot.multimodule.services.StateService;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Given sets of possible active States, this class searches for these States
 * to set active States. InitialState was designed to set the active States at
 * the start of an automation, but it can also be used to look for probable
 * active State sets in case Brobot gets lost. It is much less costly than
 * searching for all States with the StateFinder.
 */
@Component
public class InitialStates {

    private StateFinder stateFinder;
    private StateMemory stateMemory;
    private StateService stateService;

    int sumOfProbabilities = 0;
    /*
    The probability of a State existing is added to the sumOfProbabilities
    to give the Integer value here. The sumOfProbabilities can be greater than 1
    as it will be used to rescale the individual probabilities when choosing an
    initial State.
     */
    private Map<Set<StateEnum>, Integer> potentialActiveStates = new HashMap<>();

    public InitialStates(StateFinder stateFinder, StateMemory stateMemory, StateService stateService) {
        this.stateFinder = stateFinder;
        this.stateMemory = stateMemory;
        this.stateService = stateService;
    }

    public void addStateSet(int probability, StateEnum... stateEnums) {
        if (probability <= 0) return;
        sumOfProbabilities += probability;
        potentialActiveStates.put(Set.of(stateEnums), sumOfProbabilities);
    }

    public void findIntialStates() {
        Report.println();
        if (BrobotSettings.mock) {
            mockInitialStates();
            return;
        }
        searchForInitialStates();
    }

    private void mockInitialStates() {
        int rand = new Random().nextInt(sumOfProbabilities);
        for (Map.Entry<Set<StateEnum>, Integer> entry : potentialActiveStates.entrySet()) {
            if (entry.getValue() >= rand) {
                Set<StateEnum> initialStates = entry.getKey();
                initialStates.forEach(state -> stateMemory.addActiveState(state, true));
                initialStates.forEach(stateEnum ->
                        stateService.findByName(stateEnum).ifPresent(State::setProbabilityToBaseProbability));
                return;
            }
        }
    }

    private void searchForInitialStates() {
        Set<StateEnum> allPotentialStates = new HashSet<>();
        potentialActiveStates.forEach((pot, prob) -> allPotentialStates.addAll(pot));
        allPotentialStates.forEach(pot -> stateFinder.findState(pot));
    }


}
