package io.github.jspinak.brobot.tools.history.configuration;

import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ActionConfig;
import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Context information for making intelligent illustration decisions.
 * <p>
 * This class provides comprehensive context about the current action execution
 * environment, enabling sophisticated illustration filtering and optimization
 * decisions. The context includes:
 * <ul>
 * <li>Current action details and configuration</li>
 * <li>Historical action execution data</li>
 * <li>System performance metrics</li>
 * <li>Current state information</li>
 * <li>Resource utilization data</li>
 * </ul>
 * <p>
 * Used by context filters to make intelligent decisions about when
 * illustrations should be generated based on current conditions.
 *
 * @see IllustrationConfig
 * @see io.github.jspinak.brobot.tools.history.IllustrationController
 */
@Data
@Builder(toBuilder = true)
public class IllustrationContext {
    
    /**
     * The current action being executed.
     */
    private final ActionOptions.Action currentAction;
    
    /**
     * Configuration for the current action (ActionConfig or ActionOptions).
     */
    private final Object currentActionConfig;
    
    /**
     * Result of the last action executed.
     */
    private final ActionResult lastActionResult;
    
    /**
     * History of recent action executions.
     */
    private final List<ActionExecutionRecord> recentActions;
    
    /**
     * Currently active states in the automation.
     */
    private final List<String> activeStates;
    
    /**
     * Current system performance metrics.
     */
    private final SystemMetrics systemMetrics;
    
    /**
     * Number of illustrations generated in the last minute.
     */
    private final int recentIllustrationCount;
    
    /**
     * Timestamp when this context was created.
     */
    @Builder.Default
    private final LocalDateTime timestamp = LocalDateTime.now();
    
    /**
     * Custom context data for specific filtering needs.
     */
    @lombok.Singular("contextData")
    private final Map<String, Object> contextData;
    
    /**
     * Total number of consecutive failures for the current action type.
     */
    private final int consecutiveFailures;
    
    /**
     * Duration since the last successful execution of this action type.
     */
    private final Duration timeSinceLastSuccess;
    
    /**
     * Whether this is the first execution of this action type.
     */
    private final boolean isFirstExecution;
    
    /**
     * Whether the current action is part of a retry sequence.
     */
    private final boolean isRetryAttempt;
    
    /**
     * Priority level of the current action execution.
     */
    @Builder.Default
    private final Priority priority = Priority.NORMAL;
    
    /**
     * Gets typed action configuration if it matches the expected type.
     *
     * @param configType the expected configuration type
     * @param <T> the configuration type
     * @return the typed configuration or null if types don't match
     */
    @SuppressWarnings("unchecked")
    public <T> T getActionConfig(Class<T> configType) {
        if (currentActionConfig != null && configType.isAssignableFrom(currentActionConfig.getClass())) {
            return (T) currentActionConfig;
        }
        return null;
    }
    
    /**
     * Checks if the current action is one of the specified types.
     *
     * @param actions action types to check against
     * @return true if current action matches any of the specified types
     */
    public boolean isAction(ActionOptions.Action... actions) {
        for (ActionOptions.Action action : actions) {
            if (action.equals(currentAction)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks if any of the specified states are currently active.
     *
     * @param stateNames state names to check
     * @return true if any of the states are active
     */
    public boolean hasActiveState(String... stateNames) {
        for (String stateName : stateNames) {
            if (activeStates.contains(stateName)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Gets the success rate for recent actions of the current type.
     *
     * @return success rate (0.0-1.0) or 1.0 if no recent actions
     */
    public double getRecentSuccessRate() {
        if (recentActions == null || recentActions.isEmpty()) {
            return 1.0;
        }
        
        long successCount = recentActions.stream()
            .filter(record -> record.getAction().equals(currentAction))
            .mapToLong(record -> record.isSuccess() ? 1 : 0)
            .sum();
        
        long totalCount = recentActions.stream()
            .filter(record -> record.getAction().equals(currentAction))
            .count();
        
        return totalCount > 0 ? (double) successCount / totalCount : 1.0;
    }
    
    /**
     * Gets custom context data by key.
     *
     * @param key the data key
     * @return the stored value or null
     */
    public Object getContextData(String key) {
        return contextData.get(key);
    }
    
    /**
     * Gets typed custom context data.
     *
     * @param key the data key
     * @param type the expected value type
     * @param <T> the value type
     * @return the typed value or null
     */
    @SuppressWarnings("unchecked")
    public <T> T getContextData(String key, Class<T> type) {
        Object value = contextData.get(key);
        if (value != null && type.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        return null;
    }
    
    /**
     * Priority levels for action executions.
     */
    public enum Priority {
        LOW, NORMAL, HIGH, CRITICAL
    }
    
    /**
     * Record of a single action execution for historical context.
     */
    @Data
    @Builder
    public static class ActionExecutionRecord {
        private final ActionOptions.Action action;
        private final LocalDateTime timestamp;
        private final boolean success;
        private final Duration executionTime;
        private final String stateName;
        private final int matchCount;
        private final double averageMatchScore;
    }
    
    /**
     * Current system performance metrics.
     */
    @Data
    @Builder
    public static class SystemMetrics {
        private final double cpuUsage;
        private final long memoryUsageMB;
        private final long availableMemoryMB;
        private final int activeThreadCount;
        private final Duration averageActionTime;
        private final int pendingIllustrations;
        
        /**
         * Indicates if the system is under high load.
         *
         * @return true if system load is high
         */
        public boolean isHighLoad() {
            return cpuUsage > 0.8 || 
                   (memoryUsageMB > availableMemoryMB * 0.9) || 
                   pendingIllustrations > 50;
        }
    }
}