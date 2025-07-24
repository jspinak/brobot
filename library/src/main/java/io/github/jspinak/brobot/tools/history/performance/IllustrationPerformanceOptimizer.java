package io.github.jspinak.brobot.tools.history.performance;

import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.tools.history.configuration.IllustrationConfig;
import io.github.jspinak.brobot.tools.history.configuration.IllustrationContext;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Performance optimization system for illustration generation.
 * <p>
 * This component implements intelligent performance optimization strategies
 * to maintain system responsiveness while providing valuable visual feedback:
 * <ul>
 * <li>Adaptive sampling based on system load and action frequency</li>
 * <li>Smart batching to reduce I/O overhead</li>
 * <li>Quality-based prioritization to focus on meaningful illustrations</li>
 * <li>Resource monitoring and automatic throttling</li>
 * </ul>
 * <p>
 * The optimizer continuously monitors system performance and automatically
 * adjusts illustration generation to maintain optimal balance between
 * visual feedback and system performance.
 *
 * @see IllustrationConfig
 * @see IllustrationContext
 * @see PerformanceMetrics
 */
@Component
public class IllustrationPerformanceOptimizer {
    
    private final Map<ActionOptions.Action, ActionFrequencyTracker> frequencyTrackers = new ConcurrentHashMap<>();
    private final AtomicInteger illustrationsThisMinute = new AtomicInteger(0);
    private final AtomicLong lastMinuteReset = new AtomicLong(System.currentTimeMillis());
    private final Queue<IllustrationBatch> pendingBatches = new LinkedList<>();
    private final PerformanceMetrics performanceMetrics = new PerformanceMetrics();
    
    private IllustrationConfig config;
    
    /**
     * Sets the current illustration configuration.
     *
     * @param config the configuration to use
     */
    public void setConfig(IllustrationConfig config) {
        this.config = config;
    }
    
    /**
     * Determines if an illustration should be generated based on performance considerations.
     *
     * @param context the current illustration context
     * @return optimization decision with reasoning
     */
    public OptimizationDecision shouldIllustrate(IllustrationContext context) {
        // Reset per-minute counter if needed
        resetMinuteCounterIfNeeded();
        
        // Check rate limiting
        if (illustrationsThisMinute.get() >= config.getMaxIllustrationsPerMinute()) {
            return OptimizationDecision.skip("Rate limit exceeded for this minute");
        }
        
        // Check system load
        if (context.getSystemMetrics() != null && context.getSystemMetrics().isHighLoad()) {
            if (shouldSkipDueToLoad(context)) {
                return OptimizationDecision.skip("System under high load");
            }
        }
        
        // Apply sampling
        double samplingRate = calculateAdaptiveSamplingRate(context.getCurrentAction(), context);
        if (ThreadLocalRandom.current().nextDouble() > samplingRate) {
            return OptimizationDecision.skip("Filtered by adaptive sampling");
        }
        
        // Check quality threshold
        if (!meetsQualityThreshold(context)) {
            return OptimizationDecision.skip("Below quality threshold");
        }
        
        // Check if batching should be applied
        if (shouldBatch(context)) {
            return OptimizationDecision.batch("Added to batch for performance");
        }
        
        // Approve for immediate illustration
        illustrationsThisMinute.incrementAndGet();
        updateFrequencyTracker(context);
        return OptimizationDecision.proceed("Passed all optimization checks");
    }
    
    /**
     * Calculates adaptive sampling rate based on current conditions.
     *
     * @param context the current context
     * @return sampling rate (0.0-1.0)
     */
    private double calculateAdaptiveSamplingRate(ActionOptions.Action action, IllustrationContext context) {
        double baseSamplingRate = config.getSamplingRate(action);
        
        if (!config.isAdaptiveSampling()) {
            return baseSamplingRate;
        }
        
        // Adjust based on frequency
        ActionFrequencyTracker tracker = frequencyTrackers.get(action);
        if (tracker != null) {
            double frequency = tracker.getActionsPerSecond();
            
            // Reduce sampling for high-frequency actions
            if (frequency > 10) { // More than 10 actions per second
                baseSamplingRate *= 0.1; // Sample only 10%
            } else if (frequency > 5) { // More than 5 actions per second
                baseSamplingRate *= 0.3; // Sample 30%
            } else if (frequency > 1) { // More than 1 action per second
                baseSamplingRate *= 0.7; // Sample 70%
            }
        }
        
        // Adjust based on system load
        if (context.getSystemMetrics() != null) {
            double loadFactor = 1.0;
            
            if (context.getSystemMetrics().getCpuUsage() > 0.8) {
                loadFactor *= 0.2; // Drastically reduce during high CPU
            } else if (context.getSystemMetrics().getCpuUsage() > 0.6) {
                loadFactor *= 0.5; // Moderately reduce during medium CPU
            }
            
            baseSamplingRate *= loadFactor;
        }
        
        return Math.max(0.01, Math.min(1.0, baseSamplingRate)); // Keep between 1% and 100%
    }
    
    /**
     * Determines if illustration should be skipped due to system load.
     *
     * @param context the current context
     * @return true if illustration should be skipped
     */
    private boolean shouldSkipDueToLoad(IllustrationContext context) {
        if (context.getPriority() == IllustrationContext.Priority.CRITICAL) {
            return false; // Never skip critical illustrations
        }
        
        IllustrationContext.SystemMetrics metrics = context.getSystemMetrics();
        
        // Skip for non-critical actions under extreme load
        if (metrics.getCpuUsage() > 0.9 && 
            context.getPriority() == IllustrationContext.Priority.LOW) {
            return true;
        }
        
        // Skip if memory is very low
        if (metrics.getMemoryUsageMB() > metrics.getAvailableMemoryMB() * 0.95) {
            return context.getPriority() != IllustrationContext.Priority.HIGH;
        }
        
        return false;
    }
    
    /**
     * Checks if the action result meets quality thresholds.
     *
     * @param context the current context
     * @return true if quality requirements are met
     */
    private boolean meetsQualityThreshold(IllustrationContext context) {
        if (context.getLastActionResult() == null || config.getQualityMetrics() == null) {
            return true; // No quality filtering if no result or config
        }
        
        IllustrationConfig.QualityMetrics qualityMetrics = config.getQualityMetrics();
        ActionResult result = context.getLastActionResult();
        
        // Check custom quality calculator first
        if (qualityMetrics.getCustomQualityCalculator() != null) {
            double quality = qualityMetrics.getCustomQualityCalculator().apply(context);
            return quality >= config.getQualityThreshold();
        }
        
        // Use built-in quality metrics
        double quality = calculateQualityScore(result, qualityMetrics);
        return quality >= config.getQualityThreshold();
    }
    
    /**
     * Calculates quality score for an action result.
     *
     * @param result the action result
     * @param qualityMetrics quality calculation settings
     * @return quality score (0.0-1.0)
     */
    private double calculateQualityScore(ActionResult result, IllustrationConfig.QualityMetrics qualityMetrics) {
        double totalScore = 0.0;
        int factorCount = 0;
        
        // Factor in similarity if matches are present
        if (!result.getMatchList().isEmpty()) {
            double avgSimilarity = result.getMatchList().stream()
                .mapToDouble(match -> match.getScore())
                .average()
                .orElse(0.0);
            
            if (avgSimilarity >= qualityMetrics.getMinSimilarity()) {
                totalScore += avgSimilarity;
                factorCount++;
            } else {
                return 0.0; // Below minimum threshold
            }
        }
        
        // Factor in success/failure
        totalScore += result.isSuccess() ? 1.0 : 0.0;
        factorCount++;
        
        // Factor in region size if enabled
        if (qualityMetrics.isUseRegionSize() && !result.getMatchList().isEmpty()) {
            int totalArea = result.getMatchList().stream()
                .mapToInt(match -> match.w() * match.h())
                .sum();
            
            // Normalize area score (larger matches generally indicate better quality)
            double areaScore = Math.min(1.0, totalArea / 10000.0); // Normalize against 100x100 area
            totalScore += areaScore;
            factorCount++;
        }
        
        return factorCount > 0 ? totalScore / factorCount : 0.0;
    }
    
    /**
     * Determines if illustration should be batched rather than processed immediately.
     *
     * @param context the current context
     * @return true if batching is recommended
     */
    private boolean shouldBatch(IllustrationContext context) {
        if (config.getBatchConfig() == null) {
            return false;
        }
        
        // Don't batch critical or high-priority illustrations
        if (context.getPriority().ordinal() >= IllustrationContext.Priority.HIGH.ordinal()) {
            return false;
        }
        
        // Don't batch if system is idle
        if (context.getSystemMetrics() != null && !context.getSystemMetrics().isHighLoad()) {
            return false;
        }
        
        // Batch high-frequency actions
        ActionFrequencyTracker tracker = frequencyTrackers.get(context.getCurrentAction());
        if (tracker != null && tracker.getActionsPerSecond() > 2) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Updates frequency tracking for the current action.
     *
     * @param context the current context
     */
    private void updateFrequencyTracker(IllustrationContext context) {
        frequencyTrackers.computeIfAbsent(context.getCurrentAction(), 
            k -> new ActionFrequencyTracker()).recordAction();
    }
    
    /**
     * Resets the per-minute counter if a minute has passed.
     */
    private void resetMinuteCounterIfNeeded() {
        long currentTime = System.currentTimeMillis();
        long lastReset = lastMinuteReset.get();
        
        if (currentTime - lastReset >= 60000) { // 1 minute
            if (lastMinuteReset.compareAndSet(lastReset, currentTime)) {
                illustrationsThisMinute.set(0);
            }
        }
    }
    
    /**
     * Gets current performance metrics.
     *
     * @return performance metrics snapshot
     */
    public PerformanceMetrics getPerformanceMetrics() {
        return performanceMetrics;
    }
    
    /**
     * Tracks action frequency for adaptive sampling.
     */
    @Data
    private static class ActionFrequencyTracker {
        private final Queue<Long> recentTimestamps = new LinkedList<>();
        private static final long WINDOW_SIZE_MS = 10000; // 10 second window
        
        public void recordAction() {
            long now = System.currentTimeMillis();
            recentTimestamps.offer(now);
            
            // Remove old timestamps
            while (!recentTimestamps.isEmpty() && 
                   now - recentTimestamps.peek() > WINDOW_SIZE_MS) {
                recentTimestamps.poll();
            }
        }
        
        public double getActionsPerSecond() {
            if (recentTimestamps.size() < 2) {
                return 0.0;
            }
            
            long timeSpan = recentTimestamps.stream()
                .mapToLong(Long::longValue)
                .max().orElse(0L) - 
                recentTimestamps.stream()
                .mapToLong(Long::longValue)
                .min().orElse(0L);
            
            if (timeSpan == 0) {
                return 0.0;
            }
            
            return (recentTimestamps.size() - 1) * 1000.0 / timeSpan;
        }
    }
    
    /**
     * Represents a batch of illustrations for performance optimization.
     */
    @Data
    private static class IllustrationBatch {
        private final List<IllustrationContext> contexts = new ArrayList<>();
        private final LocalDateTime createdAt = LocalDateTime.now();
        
        public boolean shouldFlush(IllustrationConfig.BatchConfig batchConfig) {
            return contexts.size() >= batchConfig.getMaxBatchSize() ||
                   Duration.between(createdAt, LocalDateTime.now())
                       .compareTo(batchConfig.getFlushInterval()) >= 0;
        }
    }
}