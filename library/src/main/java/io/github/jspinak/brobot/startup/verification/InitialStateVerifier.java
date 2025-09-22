package io.github.jspinak.brobot.startup.verification;

import java.util.*;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.config.core.BrobotProperties;
import io.github.jspinak.brobot.model.state.StateEnum;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.statemanagement.StateDetector;
import io.github.jspinak.brobot.statemanagement.StateMemory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Modern approach to initial state verification for Brobot applications.
 *
 * <p>This component provides a clean API for verifying and establishing initial states when
 * automation begins. It supports both real execution and mock testing scenarios.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Simple, fluent API for state verification
 *   <li>Type-safe StateEnum support
 *   <li>Automatic state activation upon successful verification
 *   <li>Mock support with optional probability weights
 *   <li>Fallback to comprehensive state search if needed
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * // Simple verification of expected states
 * initialStateVerifier.verify(HomePage.Name.HOME, LoginPage.Name.LOGIN);
 *
 * // With custom configuration
 * initialStateVerifier.builder()
 *         .withStates(HomePage.Name.HOME, Dashboard.Name.MAIN)
 *         .withFallbackSearch(true)
 *         .verify();
 *
 * // For mock testing with probabilities
 * initialStateVerifier.builder()
 *         .withState(HomePage.Name.HOME, 70) // 70% probability
 *         .withState(LoginPage.Name.LOGIN, 30) // 30% probability
 *         .verify();
 * }</pre>
 *
 * @since 1.1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InitialStateVerifier {

    private final StateDetector stateDetector;
    private final StateMemory stateMemory;
    private final BrobotProperties brobotProperties;
    private final StateService stateService;

    /**
     * Verifies that currently active states are still visible. This is a convenience method that
     * delegates to StateDetector.
     *
     * @return Set of state IDs that remain active
     */
    public Set<Long> verifyActiveStates() {
        log.info("Verifying currently active states");
        stateDetector.checkForActiveStates();
        return stateMemory.getActiveStates();
    }

    /**
     * Rebuilds the active state list when context is uncertain. This is a convenience method that
     * delegates to StateDetector.
     *
     * @return Set of state IDs that are now active
     */
    public Set<Long> rebuildActiveStates() {
        log.info("Rebuilding active states");
        stateDetector.rebuildActiveStates();
        return stateMemory.getActiveStates();
    }

    /**
     * Performs a complete refresh of active states. Clears all states and searches from scratch.
     *
     * @return Set of state IDs that were found
     */
    public Set<Long> refreshActiveStates() {
        log.info("Refreshing all active states");
        return stateDetector.refreshActiveStates();
    }

    /**
     * Verifies that one of the specified states is currently active.
     *
     * <p>In normal mode, searches for the states on screen in the order provided. In mock mode,
     * randomly selects one state with equal probability.
     *
     * @param stateEnums Variable number of state enums to verify
     * @return true if at least one state was found and activated
     */
    public boolean verify(StateEnum... stateEnums) {
        return builder().withStates(stateEnums).verify();
    }

    /**
     * Verifies that one of the specified states is currently active.
     *
     * @param stateNames Variable number of state names to verify
     * @return true if at least one state was found and activated
     */
    public boolean verify(String... stateNames) {
        return builder().withStates(stateNames).verify();
    }

    /**
     * Creates a builder for more complex verification scenarios.
     *
     * @return A new verification builder
     */
    public VerificationBuilder builder() {
        return new VerificationBuilder();
    }

    /** Fluent builder for configuring state verification. */
    public class VerificationBuilder {
        private final Map<Long, Integer> stateProbabilities = new LinkedHashMap<>();
        private boolean fallbackToAllStates = false;
        private boolean activateFirstOnly = false;
        private int totalProbability = 0;

        /**
         * Adds states to verify with equal probability.
         *
         * @param stateEnums States to verify
         * @return This builder for chaining
         */
        public VerificationBuilder withStates(StateEnum... stateEnums) {
            for (StateEnum stateEnum : stateEnums) {
                withState(stateEnum, 1);
            }
            return this;
        }

        /**
         * Adds states to verify with equal probability.
         *
         * @param stateNames State names to verify
         * @return This builder for chaining
         */
        public VerificationBuilder withStates(String... stateNames) {
            for (String stateName : stateNames) {
                withState(stateName, 1);
            }
            return this;
        }

        /**
         * Adds a state with specific probability weight (for mock mode).
         *
         * @param stateEnum State to verify
         * @param probability Probability weight (relative to other states)
         * @return This builder for chaining
         */
        public VerificationBuilder withState(StateEnum stateEnum, int probability) {
            if (stateEnum == null) {
                return this;
            }
            return withState(stateEnum.toString(), probability);
        }

        /**
         * Adds a state with specific probability weight (for mock mode).
         *
         * @param stateName State name to verify
         * @param probability Probability weight (relative to other states)
         * @return This builder for chaining
         */
        public VerificationBuilder withState(String stateName, int probability) {
            stateService
                    .getState(stateName)
                    .ifPresent(
                            state -> {
                                if (probability > 0) {
                                    totalProbability += probability;
                                    stateProbabilities.put(state.getId(), totalProbability);
                                }
                            });
            return this;
        }

        /**
         * Enables fallback search of all registered states if none of the specified states are
         * found.
         *
         * @param enabled Whether to enable fallback search
         * @return This builder for chaining
         */
        public VerificationBuilder withFallbackSearch(boolean enabled) {
            this.fallbackToAllStates = enabled;
            return this;
        }

        /**
         * When multiple states are found, activate only the first one. Default is to activate all
         * found states.
         *
         * @param firstOnly Whether to activate only the first found state
         * @return This builder for chaining
         */
        public VerificationBuilder activateFirstOnly(boolean firstOnly) {
            this.activateFirstOnly = firstOnly;
            return this;
        }

        /**
         * Executes the verification with the configured options.
         *
         * @return true if at least one state was verified and activated
         */
        public boolean verify() {
            log.info("Starting initial state verification");

            if (stateProbabilities.isEmpty()) {
                log.warn("No states specified for verification");
                return false;
            }

            boolean result = brobotProperties.getCore().isMock() ? verifyMock() : verifyReal();

            if (!result && fallbackToAllStates) {
                log.info("Primary verification failed, attempting fallback search");
                result = searchAllStates();
            }

            logVerificationResult(result);
            return result;
        }

        private boolean verifyMock() {
            // Random selection based on probabilities
            int randomValue = new Random().nextInt(totalProbability) + 1;

            for (Map.Entry<Long, Integer> entry : stateProbabilities.entrySet()) {
                if (randomValue <= entry.getValue()) {
                    Long stateId = entry.getKey();
                    stateService
                            .getState(stateId)
                            .ifPresent(
                                    state -> {
                                        stateMemory.addActiveState(stateId);
                                        state.setProbabilityToBaseProbability();
                                        log.info(
                                                "Mock: Activated state '{}' (ID: {})",
                                                state.getName(),
                                                stateId);
                                    });
                    return true;
                }
            }
            return false;
        }

        private boolean verifyReal() {
            Set<Long> foundStates = new HashSet<>();

            // Search for each state
            for (Long stateId : stateProbabilities.keySet()) {
                if (stateDetector.findState(stateId)) {
                    foundStates.add(stateId);
                    if (activateFirstOnly) {
                        break;
                    }
                }
            }

            // Activate found states
            foundStates.forEach(
                    stateId -> {
                        stateMemory.addActiveState(stateId);
                        stateService
                                .getState(stateId)
                                .ifPresent(
                                        state ->
                                                log.info(
                                                        "Verified and activated state '{}' (ID:"
                                                                + " {})",
                                                        state.getName(),
                                                        stateId));
                    });

            return !foundStates.isEmpty();
        }

        private boolean searchAllStates() {
            Set<Long> foundStates = new HashSet<>();

            for (Long stateId : stateService.getAllStateIds()) {
                if (stateDetector.findState(stateId)) {
                    foundStates.add(stateId);
                    if (activateFirstOnly) {
                        break;
                    }
                }
            }

            foundStates.forEach(
                    stateId -> {
                        stateMemory.addActiveState(stateId);
                        stateService
                                .getState(stateId)
                                .ifPresent(
                                        state ->
                                                log.info(
                                                        "Found state '{}' during fallback search",
                                                        state.getName()));
                    });

            return !foundStates.isEmpty();
        }

        private void logVerificationResult(boolean success) {
            if (success) {
                List<String> activeStates = stateMemory.getActiveStateNames();
                log.info(
                        "Initial state verification complete. Active states: {}",
                        String.join(", ", activeStates));
            } else {
                log.warn("Initial state verification failed - no states found");
            }
        }
    }
}
