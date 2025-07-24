package io.github.jspinak.brobot.tools.history.configuration;

import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.action.ActionConfig;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Advanced configuration for illustration generation with context-aware control.
 * <p>
 * This configuration system provides granular control over when and how
 * action illustrations are generated, enabling:
 * <ul>
 * <li>Context-aware illustration decisions based on action history</li>
 * <li>Performance optimization through intelligent sampling</li>
 * <li>Quality-based filtering to focus on meaningful illustrations</li>
 * <li>Resource management for high-frequency actions</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>{@code
 * IllustrationConfig config = IllustrationConfig.builder()
 *     .globalEnabled(true)
 *     .actionEnabled(ActionOptions.Action.FIND, true)
 *     .contextFilter("find_failures_only", (context) -> 
 *         context.getLastActionResult() != null && !context.getLastActionResult().isSuccess())
 *     .samplingRate(ActionOptions.Action.MOVE, 0.1) // Sample 10% of move actions
 *     .qualityThreshold(0.8) // Only illustrate high-quality matches
 *     .batchConfig(BatchConfig.builder()
 *         .maxBatchSize(50)
 *         .flushInterval(Duration.ofSeconds(30))
 *         .build())
 *     .build();
 * }</pre>
 *
 * @see IllustrationContext
 * @see BatchConfig
 * @see QualityMetrics
 */
@Data
@Builder(toBuilder = true, builderClassName = "IllustrationConfigBuilder")
public class IllustrationConfig {
    
    /**
     * Global master switch for all illustrations.
     */
    @Builder.Default
    private final boolean globalEnabled = true;
    
    /**
     * Per-action type enablement settings.
     */
    @Singular("actionEnabled")
    private final Map<ActionOptions.Action, Boolean> actionEnabledMap;
    
    /**
     * Context-based filters that determine illustration eligibility.
     * Filters are AND-ed together - all must pass for illustration to proceed.
     */
    @Singular("contextFilter")
    private final Map<String, Predicate<IllustrationContext>> contextFilters;
    
    /**
     * Sampling rates for high-frequency actions (0.0-1.0).
     * Used to reduce illustration volume while maintaining representative coverage.
     */
    @Singular("samplingRate")
    private final Map<ActionOptions.Action, Double> samplingRates;
    
    /**
     * Minimum quality threshold for matches to be illustrated.
     * Based on similarity scores, match confidence, or other quality metrics.
     */
    @Builder.Default
    private final double qualityThreshold = 0.0;
    
    /**
     * Maximum number of illustrations to generate per minute.
     * Prevents resource exhaustion during high-activity periods.
     */
    @Builder.Default
    private final int maxIllustrationsPerMinute = Integer.MAX_VALUE;
    
    /**
     * States for which illustrations should always be generated, regardless of other settings.
     */
    @Singular("alwaysIllustrateState")
    private final Set<String> alwaysIllustrateStates;
    
    /**
     * Actions that should never be illustrated, overriding all other settings.
     */
    @Singular("neverIllustrateAction")
    private final Set<ActionOptions.Action> neverIllustrateActions;
    
    /**
     * Configuration for batching illustrations to improve performance.
     */
    private final BatchConfig batchConfig;
    
    /**
     * Advanced quality metrics configuration.
     */
    private final QualityMetrics qualityMetrics;
    
    /**
     * Duration to cache illustration decisions to avoid repeated computation.
     */
    @Builder.Default
    private final Duration decisionCacheDuration = Duration.ofSeconds(1);
    
    /**
     * Whether to use adaptive sampling based on system load.
     */
    @Builder.Default
    private final boolean adaptiveSampling = false;
    
    /**
     * Custom properties for extension and integration.
     */
    @Singular("property")
    private final Map<String, Object> customProperties;
    
    /**
     * Checks if illustrations are enabled for a specific action type.
     *
     * @param action the action type to check
     * @return true if illustrations are enabled for this action
     */
    public boolean isActionEnabled(ActionOptions.Action action) {
        if (!globalEnabled || neverIllustrateActions.contains(action)) {
            return false;
        }
        
        return actionEnabledMap.getOrDefault(action, true);
    }
    
    /**
     * Gets the sampling rate for a specific action type.
     *
     * @param action the action type
     * @return sampling rate (0.0-1.0) or 1.0 if not configured
     */
    public double getSamplingRate(ActionOptions.Action action) {
        return samplingRates.getOrDefault(action, 1.0);
    }
    
    /**
     * Checks if all context filters pass for the given context.
     *
     * @param context the illustration context
     * @return true if all filters pass or no filters are configured
     */
    public boolean passesContextFilters(IllustrationContext context) {
        return contextFilters.values().stream()
            .allMatch(filter -> filter.test(context));
    }
    
    /**
     * Checks if a state should always be illustrated.
     *
     * @param stateName the state name to check
     * @return true if this state should always be illustrated
     */
    public boolean shouldAlwaysIllustrate(String stateName) {
        return alwaysIllustrateStates.contains(stateName);
    }
    
    /**
     * Gets a custom property value.
     *
     * @param propertyName the property name
     * @return the property value or null
     */
    public Object getCustomProperty(String propertyName) {
        return customProperties.get(propertyName);
    }
    
    /**
     * Creates a copy of this config with updated settings.
     *
     * @return new config builder with current settings
     */
    public IllustrationConfig.IllustrationConfigBuilder modify() {
        return this.toBuilder();
    }
    
    /**
     * Configuration for batching illustrations to improve performance.
     */
    @Data
    @Builder
    public static class BatchConfig {
        /**
         * Maximum number of illustrations to batch before flushing.
         */
        @Builder.Default
        private final int maxBatchSize = 10;
        
        /**
         * Maximum time to wait before flushing a batch.
         */
        @Builder.Default
        private final Duration flushInterval = Duration.ofSeconds(5);
        
        /**
         * Whether to flush batches on state transitions.
         */
        @Builder.Default
        private final boolean flushOnStateTransition = true;
        
        /**
         * Maximum memory usage before forcing flush (in MB).
         */
        @Builder.Default
        private final int maxMemoryUsageMB = 100;
    }
    
    /**
     * Advanced quality metrics for illustration filtering.
     */
    @Data
    @Builder
    public static class QualityMetrics {
        /**
         * Minimum similarity score for image matches.
         */
        @Builder.Default
        private final double minSimilarity = 0.7;
        
        /**
         * Minimum match confidence score.
         */
        @Builder.Default
        private final double minConfidence = 0.5;
        
        /**
         * Whether to consider match region size in quality calculations.
         */
        @Builder.Default
        private final boolean useRegionSize = true;
        
        /**
         * Whether to consider action execution time in quality calculations.
         */
        @Builder.Default
        private final boolean useExecutionTime = false;
        
        /**
         * Custom quality calculation function.
         */
        private final java.util.function.Function<IllustrationContext, Double> customQualityCalculator;
    }
}