package io.github.jspinak.brobot.manageStates;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.reports.Report;

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
    The probability of a set of States existing is added to the sumOfProbabilities
    to give the Integer value here. The sumOfProbabilities can be greater than 1
    as it will be used to rescale the individual probabilities when choosing an
    initial State.
     */
    private final Map<Set<Long>, Integer> potentialActiveStates = new HashMap<>();

    public InitialStates(StateFinder stateFinder, StateMemory stateMemory, AllStatesInProjectService allStatesInProjectService) {
        this.stateFinder = stateFinder;
        this.stateMemory = stateMemory;
        this.allStatesInProjectService = allStatesInProjectService;
    }

    public void addStateSet(int probability, State... states) {
        if (probability <= 0) return;
        sumOfProbabilities += probability;
        Set<Long> stateIds = Arrays.stream(states).map(State::getId).collect(Collectors.toSet());
        potentialActiveStates.put(stateIds, sumOfProbabilities);
    }

    public void addStateSet(int probability, String... stateNames) {
        if (probability <= 0) return;
        sumOfProbabilities += probability;
        Set<Long> stateIds = new HashSet<>();
        for (String name : stateNames) {
            allStatesInProjectService.getState(name).ifPresent(state -> stateIds.add(state.getId()));
        }
        potentialActiveStates.put(stateIds, sumOfProbabilities);
    }

    public void findIntialStates() {
        Report.println("find initial states");
        if (BrobotSettings.mock) {
            mockInitialStates();
            return;
        }
        searchForInitialStates();
    }

    private void mockInitialStates() {
        if (potentialActiveStates.isEmpty()) {
            Report.println("No potential active states defined");
            return;
        }
        
        // Generate a random number between 1 and sumOfProbabilities
        int randomValue = new Random().nextInt(sumOfProbabilities) + 1;
        Report.println("Randomly selected value: " + randomValue + " out of " + sumOfProbabilities);
        
        // Find the state set whose probability range contains the random value
        for (Map.Entry<Set<Long>, Integer> entry : potentialActiveStates.entrySet()) {
            if (randomValue <= entry.getValue()) {
                Set<Long> selectedStates = entry.getKey();
                
                // Activate the selected states
                Report.println("Selected " + selectedStates.size() + " initial states");
                selectedStates.forEach(stateId -> {
                    stateMemory.addActiveState(stateId, true);
                    allStatesInProjectService.getState(stateId).ifPresent(state -> {
                        state.setProbabilityToBaseProbability();
                        Report.println("Activated state: " + state.getName() + " (ID: " + stateId + ")");
                    });
                });
                Report.print("Initial States are ");
                stateMemory.getActiveStateNames().forEach(state -> System.out.println(state + ", "));
                return;
            }
        }
        
        // This should never happen if potentialActiveStates is properly populated
        Report.println("Failed to select any initial states");
    }

    private void searchForInitialStates() {
        searchForStates(potentialActiveStates);
        if (stateMemory.getActiveStates().isEmpty()) {
            Map<Set<Long>, Integer> potential = new HashMap<>();
            allStatesInProjectService.getAllStateIds().forEach(id -> potential.put(Collections.singleton(id), 1));
            searchForStates(potential);
        }
    }

    private void searchForStates(Map<Set<Long>, Integer> potentialActiveStatesAndProbabilities) {
        Set<Long> allPotentialStates = new HashSet<>();
        potentialActiveStatesAndProbabilities.forEach((pot, prob) -> allPotentialStates.addAll(pot));
        allPotentialStates.forEach(stateFinder::findState);
    }

}
