package io.github.jspinak.brobot.statemanagement;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.config.core.BrobotProperties;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.navigation.service.StateService;

import lombok.extern.slf4j.Slf4j;

/**
 * Manages probabilistic initial state discovery for automation startup and recovery.
 *
 * <p>InitialStates implements a sophisticated probabilistic approach to establishing the starting
 * position in the GUI state space. Rather than assuming a fixed starting point, it maintains a
 * weighted set of possible initial state configurations and uses intelligent search strategies to
 * determine which states are actually active when automation begins.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li><b>Probabilistic State Sets</b>: Associates probability weights with potential state
 *       combinations
 *   <li><b>Intelligent Search</b>: Searches only for likely states, avoiding costly full scans
 *   <li><b>Mock Support</b>: Simulates initial state selection for testing without GUI interaction
 *   <li><b>Fallback Strategy</b>: Searches all states if no predefined sets are found
 *   <li><b>Recovery Capability</b>: Can re-establish position when automation gets lost
 * </ul>
 *
 * <p>State selection process:
 *
 * <ol>
 *   <li>Define potential initial state sets with probability weights
 *   <li>In mock mode: Randomly select based on probability distribution
 *   <li>In normal mode: Search for states in order of likelihood
 *   <li>If no states found: Fall back to searching all known states
 *   <li>Update StateMemory with discovered active states
 * </ol>
 *
 * <p>Probability management:
 *
 * <ul>
 *   <li>Each state set has an integer probability weight (not percentage)
 *   <li>Weights are cumulative for random selection algorithm
 *   <li>Higher weights indicate more likely initial configurations
 *   <li>Sum can exceed 100 as these are relative weights, not percentages
 * </ul>
 *
 * <p>Use cases:
 *
 * <ul>
 *   <li>Starting automation from unknown GUI position
 *   <li>Handling multiple possible application entry points
 *   <li>Recovering from navigation failures or unexpected states
 *   <li>Testing automation logic with simulated state configurations
 *   <li>Supporting applications with variable startup sequences
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre>
 * // Define possible initial states with probabilities
 * initialStates.addStateSet(70, "LoginPage"); // 70% chance
 * initialStates.addStateSet(20, "Dashboard"); // 20% chance
 * initialStates.addStateSet(10, "MainMenu"); // 10% chance
 *
 * // Find which states are actually active
 * initialStates.findInitialStates();
 * </pre>
 *
 * <p>In the model-based approach, InitialStates embodies the framework's adaptability to uncertain
 * starting conditions. Unlike rigid scripts that assume fixed entry points, this component enables
 * automation that can begin from various GUI positions and intelligently determine its location
 * before proceeding with tasks.
 *
 * <p>This probabilistic approach is essential for:
 *
 * <ul>
 *   <li>Web applications with session-based navigation
 *   <li>Desktop applications with persistent state
 *   <li>Mobile apps with deep linking or notifications
 *   <li>Any GUI where the starting position varies
 * </ul>
 *
 * @since 1.0
 * @see StateDetector
 * @see StateMemory
 * @see State
 * @see BrobotProperties
 */
@Slf4j
@Component
public class InitialStates {

    private final BrobotProperties brobotProperties;
    private final StateDetector stateFinder;
    private final StateMemory stateMemory;
    private final StateService allStatesInProjectService;

    /**
     * Running total of all probability weights for normalization. Used in random selection to map
     * random values to state sets.
     */
    int sumOfProbabilities = 0;

    /**
     * Maps potential state sets to their cumulative probability thresholds.
     *
     * <p>Each entry maps a set of state IDs to an integer representing the upper bound of its
     * probability range. When selecting randomly, a value between 1 and sumOfProbabilities is
     * chosen, and the first entry with a threshold greater than or equal to this value is selected.
     *
     * <p>Example: If three sets have weights 50, 30, 20:
     *
     * <ul>
     *   <li>Set 1: threshold = 50 (range 1-50)
     *   <li>Set 2: threshold = 80 (range 51-80)
     *   <li>Set 3: threshold = 100 (range 81-100)
     * </ul>
     */
    private final Map<Set<Long>, Integer> potentialActiveStates = new HashMap<>();

    public InitialStates(
            BrobotProperties brobotProperties,
            StateDetector stateFinder,
            StateMemory stateMemory,
            StateService allStatesInProjectService) {
        this.brobotProperties = brobotProperties;
        this.stateFinder = stateFinder;
        this.stateMemory = stateMemory;
        this.allStatesInProjectService = allStatesInProjectService;
    }

    /**
     * Adds a potential initial state set with its probability weight.
     *
     * <p>Registers a combination of states that might be active when automation begins, along with
     * a weight indicating how likely this combination is. Higher weights make the state set more
     * likely to be selected in mock mode or searched first in normal mode.
     *
     * @param probability Weight for this state set (must be positive)
     * @param states Variable number of State objects forming the set
     */
    public void addStateSet(int probability, State... states) {
        if (probability <= 0) return;
        sumOfProbabilities += probability;
        Set<Long> stateIds = Arrays.stream(states).map(State::getId).collect(Collectors.toSet());
        potentialActiveStates.put(stateIds, sumOfProbabilities);
    }

    /**
     * Adds a potential initial state set using state names.
     *
     * <p>Convenience method that accepts state names instead of State objects. Names are resolved
     * to states through StateService. Invalid names are silently ignored, allowing flexible
     * configuration.
     *
     * @param probability Weight for this state set (must be positive)
     * @param stateNames Variable number of state names forming the set
     * @see StateService#getState(String)
     */
    public void addStateSet(int probability, String... stateNames) {
        if (probability <= 0) return;
        sumOfProbabilities += probability;
        Set<Long> stateIds = new HashSet<>();
        for (String name : stateNames) {
            allStatesInProjectService
                    .getState(name)
                    .ifPresent(
                            state -> {
                                stateIds.add(state.getId());
                                log.debug(
                                        "Added state to initial set: {} (ID: {})",
                                        name,
                                        state.getId());
                            });
            if (!allStatesInProjectService.getState(name).isPresent()) {
                log.warn("State not found for initial state: {}", name);
            }
        }
        if (!stateIds.isEmpty()) {
            potentialActiveStates.put(stateIds, sumOfProbabilities);
            log.info(
                    "Registered initial state set with {} states, probability: {}",
                    stateIds.size(),
                    probability);
        } else {
            log.warn("No states found for initial state names: {}", Arrays.toString(stateNames));
        }
    }

    /**
     * Discovers and activates the initial states for automation.
     *
     * <p>Main entry point that determines which states are currently active. Behavior depends on
     * BrobotProperties configuration:
     *
     * <ul>
     *   <li>Mock mode: Randomly selects from defined state sets
     *   <li>Normal mode: Searches screen for actual states
     * </ul>
     *
     * <p>Side effects:
     *
     * <ul>
     *   <li>Updates StateMemory with discovered active states
     *   <li>Resets state probabilities to base values
     *   <li>Prints results to Report for debugging
     * </ul>
     *
     * @see #mockInitialStates()
     * @see #searchForInitialStates()
     */
    public void findInitialStates() {
        log.info("Finding initial states (mock mode: {})", brobotProperties.getCore().isMock());
        if (brobotProperties.getCore().isMock()) {
            mockInitialStates();
            return;
        }
        searchForInitialStates();
    }

    /**
     * Simulates initial state selection for testing without GUI interaction.
     *
     * <p>Uses weighted random selection to choose a state set from the defined potential states.
     * Selected states are activated in StateMemory without actual screen verification, enabling
     * unit testing and development.
     *
     * <p>Selection algorithm:
     *
     * <ol>
     *   <li>Generate random number between 1 and sumOfProbabilities
     *   <li>Find first state set with threshold >= random value
     *   <li>Activate all states in the selected set
     *   <li>Reset their probabilities to base values
     * </ol>
     */
    private void mockInitialStates() {
        if (potentialActiveStates.isEmpty()) {
            log.warn("No potential active states defined for mock mode");
            return;
        }

        // Generate a random number between 1 and sumOfProbabilities
        int randomValue = new Random().nextInt(sumOfProbabilities) + 1;
        log.info(
                "Mock mode: randomly selected value {} out of {}", randomValue, sumOfProbabilities);

        // Find the state set whose probability range contains the random value
        for (Map.Entry<Set<Long>, Integer> entry : potentialActiveStates.entrySet()) {
            if (randomValue <= entry.getValue()) {
                Set<Long> selectedStates = entry.getKey();

                // Activate the selected states
                log.info("Mock mode: selected {} initial states", selectedStates.size());
                selectedStates.forEach(
                        stateId -> {
                            stateMemory.addActiveState(stateId);
                            allStatesInProjectService
                                    .getState(stateId)
                                    .ifPresent(
                                            state -> {
                                                state.setProbabilityToBaseProbability();
                                                log.info(
                                                        "Activated state: {} (ID: {})",
                                                        state.getName(),
                                                        stateId);
                                            });
                        });
                log.info("Initial states activated: {}", stateMemory.getActiveStateNames());
                stateMemory
                        .getActiveStateNames()
                        .forEach(state -> System.out.println(state + ", "));
                return;
            }
        }

        // This should never happen if potentialActiveStates is properly populated
        log.error("Failed to select any initial states in mock mode");
    }

    /**
     * Searches for initial states on the actual screen.
     *
     * <p>Attempts to find active states by searching for predefined potential state sets. If no
     * states are found, falls back to searching for all known states individually. This two-phase
     * approach balances efficiency with completeness.
     */
    private void searchForInitialStates() {
        searchForStates(potentialActiveStates);
        if (stateMemory.getActiveStates().isEmpty()) {
            Map<Set<Long>, Integer> potential = new HashMap<>();
            allStatesInProjectService
                    .getAllStateIds()
                    .forEach(id -> potential.put(Collections.singleton(id), 1));
            searchForStates(potential);
        }
    }

    /**
     * Executes state searches for all states in the provided sets.
     *
     * <p>Collects all unique states from the potential state sets and uses StateFinder to search
     * for each one on the screen. Found states are automatically added to StateMemory by
     * StateFinder.
     *
     * @param potentialActiveStatesAndProbabilities Map of state sets to search
     * @see StateDetector#findState(Long)
     */
    private void searchForStates(Map<Set<Long>, Integer> potentialActiveStatesAndProbabilities) {
        Set<Long> allPotentialStates = new HashSet<>();
        potentialActiveStatesAndProbabilities.forEach(
                (pot, prob) -> allPotentialStates.addAll(pot));
        allPotentialStates.forEach(stateFinder::findState);
    }

    /**
     * Gets the names of all states that have been registered as initial states. This includes
     * states added via @State(initial = true) annotations.
     *
     * @return List of state names registered as initial states
     */
    public List<String> getRegisteredInitialStates() {
        return potentialActiveStates.keySet().stream()
                .flatMap(Set::stream)
                .map(allStatesInProjectService::getState)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(io.github.jspinak.brobot.model.state.State::getName)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Checks if any initial states have been registered.
     *
     * @return true if initial states are registered, false otherwise
     */
    public boolean hasRegisteredInitialStates() {
        return !potentialActiveStates.isEmpty();
    }
}
