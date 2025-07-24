package io.github.jspinak.brobot.tools.testing.mock.scenario;

import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Context information for mock test execution, tracking state and history.
 * <p>
 * This class maintains runtime information about mock test execution including:
 * <ul>
 * <li>Action execution counts and timing</li>
 * <li>Current active states and transitions</li>
 * <li>Failure history and patterns</li>
 * <li>Custom context data for scenario-specific testing</li>
 * </ul>
 * <p>
 * Thread-safe counters ensure accurate tracking in concurrent test scenarios.
 *
 * @see MockScenarioConfig
 * @see MockScenarioManager
 */
@Data
@Builder
public class MockTestContext {
    
    /**
     * Total number of actions executed in this test session.
     */
    @Builder.Default
    private final AtomicInteger totalActions = new AtomicInteger(0);
    
    /**
     * Number of actions that have failed in this session.
     */
    @Builder.Default
    private final AtomicInteger failedActions = new AtomicInteger(0);
    
    /**
     * Count of actions by type for analysis and pattern detection.
     */
    @Singular("actionCount")
    private final Map<ActionOptions.Action, AtomicInteger> actionCounts;
    
    /**
     * Currently active states in the automation.
     */
    @Singular("activeState")
    private final Map<String, Boolean> activeStates;
    
    /**
     * Custom data for scenario-specific context tracking.
     */
    @Singular("contextData")
    private final Map<String, Object> contextData;
    
    /**
     * When this test context was created.
     */
    @Builder.Default
    private final LocalDateTime startTime = LocalDateTime.now();
    
    /**
     * Last time an action was executed.
     */
    private LocalDateTime lastActionTime;
    
    /**
     * Increments the total action count and returns the new value.
     *
     * @return new total action count
     */
    public int incrementTotalActions() {
        lastActionTime = LocalDateTime.now();
        return totalActions.incrementAndGet();
    }
    
    /**
     * Increments the failed action count and returns the new value.
     *
     * @return new failed action count
     */
    public int incrementFailedActions() {
        return failedActions.incrementAndGet();
    }
    
    /**
     * Increments the count for a specific action type.
     *
     * @param action the action type to increment
     * @return new count for this action type
     */
    public int incrementActionCount(ActionOptions.Action action) {
        return actionCounts.computeIfAbsent(action, k -> new AtomicInteger(0))
            .incrementAndGet();
    }
    
    /**
     * Gets the count for a specific action type.
     *
     * @param action the action type to query
     * @return current count for this action type
     */
    public int getActionCount(ActionOptions.Action action) {
        AtomicInteger count = actionCounts.get(action);
        return count != null ? count.get() : 0;
    }
    
    /**
     * Checks if a state is currently active.
     *
     * @param stateName name of the state to check
     * @return true if state is active
     */
    public boolean isStateActive(String stateName) {
        return activeStates.getOrDefault(stateName, false);
    }
    
    /**
     * Sets the active status of a state.
     *
     * @param stateName name of the state
     * @param active whether the state should be active
     */
    public void setStateActive(String stateName, boolean active) {
        activeStates.put(stateName, active);
    }
    
    /**
     * Gets custom context data.
     *
     * @param key the data key
     * @return the stored value or null
     */
    public Object getContextData(String key) {
        return contextData.get(key);
    }
    
    /**
     * Sets custom context data.
     *
     * @param key the data key
     * @param value the value to store
     */
    public void setContextData(String key, Object value) {
        contextData.put(key, value);
    }
    
    /**
     * Calculates the current success rate.
     *
     * @return success rate (0.0-1.0) or 1.0 if no actions executed
     */
    public double getSuccessRate() {
        int total = totalActions.get();
        if (total == 0) return 1.0;
        int failed = failedActions.get();
        return (double) (total - failed) / total;
    }
    
    /**
     * Gets the duration since the test context was created.
     *
     * @return test execution duration
     */
    public java.time.Duration getTestDuration() {
        return java.time.Duration.between(startTime, LocalDateTime.now());
    }
}