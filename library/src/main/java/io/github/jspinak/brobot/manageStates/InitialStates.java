package io.github.jspinak.brobot.manageStates;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.reports.Report;
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

    private final StateFinder stateFinder;
    private final StateMemory stateMemory;
    private final AllStatesInProjectService allStatesInProjectService;

    int sumOfProbabilities = 0;
    /*
    The probability of a State existing is added to the sumOfProbabilities
    to give the Integer value here. The sumOfProbabilities can be greater than 1
    as it will be used to rescale the individual probabilities when choosing an
    initial State.
     */
    private final Map<Set<String>, Integer> potentialActiveStates = new HashMap<>();

    public InitialStates(StateFinder stateFinder, StateMemory stateMemory, AllStatesInProjectService allStatesInProjectService) {
        this.stateFinder = stateFinder;
        this.stateMemory = stateMemory;
        this.allStatesInProjectService = allStatesInProjectService;
    }

    public void addStateSet(int probability, String... stateNames) {
        if (probability <= 0) return;
        sumOfProbabilities += probability;
        potentialActiveStates.put(Set.of(stateNames), sumOfProbabilities);
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
        for (Map.Entry<Set<String>, Integer> entry : potentialActiveStates.entrySet()) {
            if (entry.getValue() >= rand) {
                Set<String> initialStates = entry.getKey();
                initialStates.forEach(state -> stateMemory.addActiveState(state, true));
                initialStates.forEach(name ->
                        allStatesInProjectService.getState(name).ifPresent(State::setProbabilityToBaseProbability));
                return;
            }
        }
    }

    private void searchForInitialStates() {
        Set<String> allPotentialStates = new HashSet<>();
        potentialActiveStates.forEach((pot, prob) -> allPotentialStates.addAll(pot));
        allPotentialStates.forEach(stateFinder::findState);
    }


}
