package io.github.jspinak.brobot.runner.ui.illustration.analytics;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Immutable snapshot of analytics data at a point in time.
 * <p>
 * This class provides a consistent view of all analytics metrics
 * for display and analysis purposes.
 *
 * @see IllustrationAnalyticsService
 * @see IllustrationAnalyticsDashboard
 */
@Data
@Builder
public class AnalyticsSnapshot {
    
    /**
     * Timestamp when this snapshot was created.
     */
    private final LocalDateTime timestamp;
    
    // Count metrics
    private final int totalIllustrations;
    private final int successfulIllustrations;
    private final int skippedIllustrations;
    private final int batchedIllustrations;
    
    // Action-specific metrics
    private final Map<String, AtomicInteger> actionCounts;
    private final Map<String, AtomicInteger> actionSuccesses;
    
    // Calculated rates
    private final double overallSuccessRate;
    private final Map<String, Double> successRatesByAction;
    
    // Performance metrics
    private final double averageProcessingTime;
    private final long minProcessingTime;
    private final long maxProcessingTime;
    private final Map<String, Double> averageTimeByAction;
    
    // Quality metrics
    private final double highQualityRate;
    private final double averageQualityScore;
    
    // Efficiency metrics
    private final double skipRate;
    private final double batchEfficiency;
    private final double samplingEffectiveness;
    
    // Throughput metrics
    private final double illustrationsPerMinute;
    
    // Current state
    private final int currentQueueSize;
    private final double currentSamplingRate;
    
    // System metrics
    private final Duration uptimeDuration;
}