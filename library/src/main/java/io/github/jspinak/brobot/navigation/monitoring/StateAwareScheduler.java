package io.github.jspinak.brobot.navigation.monitoring;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.config.logging.LoggingVerbosityConfig;
import io.github.jspinak.brobot.statemanagement.StateDetector;
import io.github.jspinak.brobot.statemanagement.StateMemory;
// Removed old logging import: 
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Provides state-aware scheduling capabilities for automation tasks in the Brobot framework.
 *
 * <p>This component extends standard scheduling functionality with intelligent state detection and
 * management, ensuring tasks run with proper state context. It supports two checking modes:
 * checking all required states or only checking currently inactive states.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li><b>State Validation</b>: Ensures required states are active before task execution
 *   <li><b>Flexible Checking</b>: Check all states or only inactive ones for efficiency
 *   <li><b>Auto-Recovery</b>: Optionally rebuilds states when requirements aren't met
 *   <li><b>Configurable Behavior</b>: Different strategies for different scenarios
 * </ul>
 *
 * <p>This component maintains single responsibility by focusing solely on the intersection of
 * scheduling and state awareness, delegating actual state operations to the appropriate brobot
 * components.
 *
 * @since 1.1.0
 * @see MonitoringService
 * @see StateDetector
 * @see StateMemory
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StateAwareScheduler {

    private final StateDetector stateDetector;
    private final StateMemory stateMemory;

    @Autowired(required = false)
    private LoggingVerbosityConfig verbosityConfig;

    /** Configuration for state checking behavior. */
    public static class StateCheckConfiguration {
        private final List<String> requiredStates;
        private final boolean rebuildOnMismatch;
        private final boolean skipIfStatesMissing;
        private final CheckMode checkMode;
        private final int maxIterations;

        /** Defines how states should be checked. */
        public enum CheckMode {
            /**
             * Check all required states, regardless of current active status. More thorough but
             * potentially less efficient.
             */
            CHECK_ALL,

            /**
             * Only check states that are currently inactive. More efficient when some states are
             * known to be active.
             */
            CHECK_INACTIVE_ONLY
        }

        private StateCheckConfiguration(Builder builder) {
            this.requiredStates = builder.requiredStates;
            this.rebuildOnMismatch = builder.rebuildOnMismatch;
            this.skipIfStatesMissing = builder.skipIfStatesMissing;
            this.checkMode = builder.checkMode;
            this.maxIterations = builder.maxIterations;
        }

        public List<String> getRequiredStates() {
            return requiredStates;
        }

        public boolean isRebuildOnMismatch() {
            return rebuildOnMismatch;
        }

        public boolean isSkipIfStatesMissing() {
            return skipIfStatesMissing;
        }

        public CheckMode getCheckMode() {
            return checkMode;
        }

        public int getMaxIterations() {
            return maxIterations;
        }

        public static class Builder {
            private List<String> requiredStates = List.of();
            private boolean rebuildOnMismatch = true;
            private boolean skipIfStatesMissing = false;
            private CheckMode checkMode = CheckMode.CHECK_INACTIVE_ONLY;
            private int maxIterations = -1; // -1 means unlimited

            public Builder withRequiredStates(List<String> states) {
                this.requiredStates = states;
                return this;
            }

            public Builder withRebuildOnMismatch(boolean rebuild) {
                this.rebuildOnMismatch = rebuild;
                return this;
            }

            public Builder withSkipIfStatesMissing(boolean skip) {
                this.skipIfStatesMissing = skip;
                return this;
            }

            public Builder withCheckMode(CheckMode mode) {
                this.checkMode = mode;
                return this;
            }

            public Builder withMaxIterations(int maxIterations) {
                this.maxIterations = maxIterations;
                return this;
            }

            public StateCheckConfiguration build() {
                return new StateCheckConfiguration(this);
            }
        }
    }

    /**
     * Schedules a task with state checking at the beginning of each cycle. Supports optional
     * iteration limit to stop after a specified number of executions.
     *
     * @param scheduler The executor service to use for scheduling
     * @param task The task to execute after state validation
     * @param config Configuration for state checking behavior (including max iterations)
     * @param initialDelay Initial delay before first execution
     * @param period Period between executions
     * @param unit Time unit for delays
     * @return ScheduledFuture that can be used to cancel the task
     */
    public ScheduledFuture<?> scheduleWithStateCheck(
            ScheduledExecutorService scheduler,
            Runnable task,
            StateCheckConfiguration config,
            long initialDelay,
            long period,
            TimeUnit unit) {

        final AtomicInteger iterationCount = new AtomicInteger(0);
        final int maxIterations = config.getMaxIterations();

        // Log scheduling start
        if (maxIterations > 0) {
            if (verbosityConfig != null
                    && (verbosityConfig.isNormalMode() || verbosityConfig.isVerboseMode())) {
            }
        }

        return scheduler.scheduleAtFixedRate(
                () -> {
                    try {
                        // Check if we've reached the iteration limit
                        if (maxIterations > 0) {
                            int currentIteration = iterationCount.incrementAndGet();
                            if (currentIteration > maxIterations) {
                                // Always log when stopping due to max iterations
                                log.info(
                                        "Reached maximum iterations ({}) for scheduled task,"
                                                + " stopping",
                                        maxIterations);
                                throw new RuntimeException("Max iterations reached");
                            }

                            // Log iteration progress based on verbosity
                            if (verbosityConfig != null) {
                                if (verbosityConfig.isVerboseMode()) {
                                    // VERBOSE: Log every iteration
                                } else if (verbosityConfig.isNormalMode()
                                        && (currentIteration == 1
                                                || currentIteration % 5 == 0
                                                || currentIteration == maxIterations)) {
                                    // NORMAL: Log first, every 5th, and last iteration
                                }
                                // QUIET: No iteration logging
                            } else {
                                // Fallback to debug logging if config not available
                                log.debug(
                                        "Executing scheduled task iteration {}/{}",
                                        currentIteration,
                                        maxIterations);
                            }
                        }

                        performStateCheck(config);
                        task.run();
                    } catch (IllegalStateException e) {
                        if (config.isSkipIfStatesMissing()) {
                            log.warn("Skipping task execution: {}", e.getMessage());
                        } else {
                            log.error("State check failed", e);
                        }
                    } catch (RuntimeException e) {
                        if (e.getMessage() != null
                                && e.getMessage().equals("Max iterations reached")) {
                            // This will cause the scheduled task to stop
                            throw e;
                        }
                        log.error("Error in state-aware scheduled task", e);
                    } catch (Exception e) {
                        log.error("Error in state-aware scheduled task", e);
                    }
                },
                initialDelay,
                period,
                unit);
    }

    /**
     * Schedules a task with fixed delay and state checking. Supports optional iteration limit to
     * stop after a specified number of executions.
     *
     * @param scheduler The executor service to use for scheduling
     * @param task The task to execute after state validation
     * @param config Configuration for state checking behavior (including max iterations)
     * @param initialDelay Initial delay before first execution
     * @param delay Delay between end of one execution and start of next
     * @param unit Time unit for delays
     * @return ScheduledFuture that can be used to cancel the task
     */
    public ScheduledFuture<?> scheduleWithFixedDelayAndStateCheck(
            ScheduledExecutorService scheduler,
            Runnable task,
            StateCheckConfiguration config,
            long initialDelay,
            long delay,
            TimeUnit unit) {

        final AtomicInteger iterationCount = new AtomicInteger(0);
        final int maxIterations = config.getMaxIterations();

        // Log scheduling start
        if (maxIterations > 0) {
            if (verbosityConfig != null
                    && (verbosityConfig.isNormalMode() || verbosityConfig.isVerboseMode())) {
            }
        }

        return scheduler.scheduleWithFixedDelay(
                () -> {
                    try {
                        // Check if we've reached the iteration limit
                        if (maxIterations > 0) {
                            int currentIteration = iterationCount.incrementAndGet();
                            if (currentIteration > maxIterations) {
                                // Always log when stopping due to max iterations
                                log.info(
                                        "Reached maximum iterations ({}) for scheduled task,"
                                                + " stopping",
                                        maxIterations);
                                throw new RuntimeException("Max iterations reached");
                            }

                            // Log iteration progress based on verbosity
                            if (verbosityConfig != null) {
                                if (verbosityConfig.isVerboseMode()) {
                                    // VERBOSE: Log every iteration
                                } else if (verbosityConfig.isNormalMode()
                                        && (currentIteration == 1
                                                || currentIteration % 5 == 0
                                                || currentIteration == maxIterations)) {
                                    // NORMAL: Log first, every 5th, and last iteration
                                }
                                // QUIET: No iteration logging
                            } else {
                                // Fallback to debug logging if config not available
                                log.debug(
                                        "Executing scheduled task iteration {}/{}",
                                        currentIteration,
                                        maxIterations);
                            }
                        }

                        performStateCheck(config);
                        task.run();
                    } catch (IllegalStateException e) {
                        if (config.isSkipIfStatesMissing()) {
                            log.warn("Skipping task execution: {}", e.getMessage());
                        } else {
                            log.error("State check failed", e);
                        }
                    } catch (RuntimeException e) {
                        if (e.getMessage() != null
                                && e.getMessage().equals("Max iterations reached")) {
                            // This will cause the scheduled task to stop
                            throw e;
                        }
                        log.error("Error in state-aware scheduled task", e);
                    } catch (Exception e) {
                        log.error("Error in state-aware scheduled task", e);
                    }
                },
                initialDelay,
                delay,
                unit);
    }

    /**
     * Performs state checking according to the provided configuration. This method encapsulates the
     * state validation logic.
     *
     * @param config The state check configuration
     * @throws IllegalStateException if required states are not active and skipIfStatesMissing is
     *     false
     */
    private void performStateCheck(StateCheckConfiguration config) {
        log.debug(
                "Performing state check for required states: {} using mode: {}",
                config.getRequiredStates(),
                config.getCheckMode());

        List<String> activeStateNames = stateMemory.getActiveStateNames();
        Set<String> activeStateSet = new HashSet<>(activeStateNames);

        // Determine which states need to be checked
        List<String> statesToCheck;
        if (config.getCheckMode() == StateCheckConfiguration.CheckMode.CHECK_ALL) {
            // Check all required states
            statesToCheck = config.getRequiredStates();
            log.debug("Checking all required states: {}", statesToCheck);
        } else {
            // Only check states that are currently inactive
            statesToCheck =
                    config.getRequiredStates().stream()
                            .filter(state -> !activeStateSet.contains(state))
                            .collect(Collectors.toList());
            log.debug(
                    "Checking only inactive states: {} (active states: {})",
                    statesToCheck,
                    activeStateNames);
        }

        // If all required states are already active and we're in CHECK_INACTIVE_ONLY mode
        if (statesToCheck.isEmpty()
                && config.getCheckMode() == StateCheckConfiguration.CheckMode.CHECK_INACTIVE_ONLY) {
            log.debug("All required states are already active, skipping state detection");
            return;
        }

        // Perform state checking for the determined states
        boolean allRequiredStatesActive = true;
        if (!statesToCheck.isEmpty()) {
            // Perform targeted state detection
            for (String stateName : statesToCheck) {
                if (!stateDetector.findState(stateName)) {
                    allRequiredStatesActive = false;
                    log.info("Required state '{}' is not active", stateName);
                }
            }
        }

        // Re-check after targeted detection
        activeStateNames = stateMemory.getActiveStateNames();
        Set<String> updatedActiveStateSet = new HashSet<>(activeStateNames);
        allRequiredStatesActive =
                config.getRequiredStates().stream().allMatch(updatedActiveStateSet::contains);

        if (!allRequiredStatesActive) {
            log.info(
                    "Not all required states are active. Current: {}, Required: {}",
                    activeStateNames,
                    config.getRequiredStates());

            if (config.isRebuildOnMismatch()) {
                log.info("Rebuilding active states");
                stateDetector.rebuildActiveStates();

                // Re-check after rebuild
                activeStateNames = stateMemory.getActiveStateNames();
                Set<String> rebuiltActiveStateSet = new HashSet<>(activeStateNames);
                allRequiredStatesActive =
                        config.getRequiredStates().stream()
                                .allMatch(rebuiltActiveStateSet::contains);
            }

            if (!allRequiredStatesActive && config.isSkipIfStatesMissing()) {
                throw new IllegalStateException(
                        "Required states not active, skipping task execution. Required: "
                                + config.getRequiredStates()
                                + ", Active: "
                                + activeStateNames);
            }
        }

        log.debug("State check completed. All required states active: {}", allRequiredStatesActive);
    }

    /**
     * Creates a pre-execution hook that ensures specific states are active. This can be used with
     * existing schedulers as a wrapper.
     *
     * @param config State check configuration
     * @return A runnable that performs state checking
     */
    public Runnable createStateCheckHook(StateCheckConfiguration config) {
        return () -> performStateCheck(config);
    }

    /**
     * Wraps an existing task with state checking.
     *
     * @param task The original task to wrap
     * @param config State check configuration
     * @return A new runnable that performs state checking before the task
     */
    public Runnable wrapWithStateCheck(Runnable task, StateCheckConfiguration config) {
        return () -> {
            performStateCheck(config);
            task.run();
        };
    }
}
