package io.github.jspinak.brobot.tools.history.performance;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import lombok.Data;

/**
 * Performance metrics for illustration system monitoring.
 *
 * <p>This class tracks various performance indicators to help optimize illustration generation and
 * identify performance bottlenecks:
 *
 * <ul>
 *   <li>Throughput metrics (illustrations per time period)
 *   <li>Processing time statistics
 *   <li>Resource utilization indicators
 *   <li>Quality and effectiveness measures
 * </ul>
 *
 * <p>Used by the performance optimizer to make intelligent decisions about when and how to generate
 * illustrations.
 *
 * @see IllustrationPerformanceOptimizer
 */
@Data
public class PerformanceMetrics {

    // Throughput metrics
    private final AtomicInteger totalIllustrationsGenerated = new AtomicInteger(0);
    private final AtomicInteger illustrationsSkipped = new AtomicInteger(0);
    private final AtomicInteger illustrationsBatched = new AtomicInteger(0);
    private final AtomicInteger illustrationsDeferred = new AtomicInteger(0);

    // Processing time metrics
    private final AtomicLong totalProcessingTimeMs = new AtomicLong(0);
    private final AtomicLong minProcessingTimeMs = new AtomicLong(Long.MAX_VALUE);
    private final AtomicLong maxProcessingTimeMs = new AtomicLong(0);

    // Resource utilization
    private final AtomicLong totalMemoryUsedMB = new AtomicLong(0);
    private final AtomicInteger activeIllustrationThreads = new AtomicInteger(0);

    // Quality metrics
    private final AtomicInteger highQualityIllustrations = new AtomicInteger(0);
    private final AtomicInteger lowQualityIllustrations = new AtomicInteger(0);

    // Timing
    private final LocalDateTime startTime = LocalDateTime.now();
    private LocalDateTime lastIllustrationTime = LocalDateTime.now();

    /**
     * Records the generation of a new illustration.
     *
     * @param processingTimeMs time taken to generate the illustration
     * @param memorySizeMB memory size of the generated illustration
     * @param highQuality whether the illustration meets high quality standards
     */
    public void recordIllustration(long processingTimeMs, long memorySizeMB, boolean highQuality) {
        totalIllustrationsGenerated.incrementAndGet();
        totalProcessingTimeMs.addAndGet(processingTimeMs);
        totalMemoryUsedMB.addAndGet(memorySizeMB);
        lastIllustrationTime = LocalDateTime.now();

        // Update min/max processing times
        minProcessingTimeMs.updateAndGet(current -> Math.min(current, processingTimeMs));
        maxProcessingTimeMs.updateAndGet(current -> Math.max(current, processingTimeMs));

        // Update quality metrics
        if (highQuality) {
            highQualityIllustrations.incrementAndGet();
        } else {
            lowQualityIllustrations.incrementAndGet();
        }
    }

    /**
     * Records a skipped illustration.
     *
     * @param reason the reason for skipping
     */
    public void recordSkipped(String reason) {
        illustrationsSkipped.incrementAndGet();
    }

    /** Records a batched illustration. */
    public void recordBatched() {
        illustrationsBatched.incrementAndGet();
    }

    /**
     * Records a deferred illustration.
     *
     * @param deferDuration how long the illustration was deferred
     */
    public void recordDeferred(Duration deferDuration) {
        illustrationsDeferred.incrementAndGet();
    }

    /** Increments the count of active illustration threads. */
    public void incrementActiveThreads() {
        activeIllustrationThreads.incrementAndGet();
    }

    /** Decrements the count of active illustration threads. */
    public void decrementActiveThreads() {
        activeIllustrationThreads.decrementAndGet();
    }

    /**
     * Calculates the average processing time per illustration.
     *
     * @return average processing time in milliseconds
     */
    public double getAverageProcessingTimeMs() {
        int total = totalIllustrationsGenerated.get();
        return total > 0 ? (double) totalProcessingTimeMs.get() / total : 0.0;
    }

    /**
     * Calculates the illustrations per minute rate.
     *
     * @return illustrations per minute
     */
    public double getIllustrationsPerMinute() {
        Duration elapsed = Duration.between(startTime, LocalDateTime.now());
        if (elapsed.toMinutes() == 0) {
            return 0.0;
        }
        return (double) totalIllustrationsGenerated.get() / elapsed.toMinutes();
    }

    /**
     * Calculates the skip rate (percentage of illustrations skipped).
     *
     * @return skip rate (0.0-1.0)
     */
    public double getSkipRate() {
        int total = totalIllustrationsGenerated.get() + illustrationsSkipped.get();
        return total > 0 ? (double) illustrationsSkipped.get() / total : 0.0;
    }

    /**
     * Calculates the high quality rate (percentage of high-quality illustrations).
     *
     * @return high quality rate (0.0-1.0)
     */
    public double getHighQualityRate() {
        int total = highQualityIllustrations.get() + lowQualityIllustrations.get();
        return total > 0 ? (double) highQualityIllustrations.get() / total : 0.0;
    }

    /**
     * Gets the average memory usage per illustration.
     *
     * @return average memory usage in MB
     */
    public double getAverageMemoryUsageMB() {
        int total = totalIllustrationsGenerated.get();
        return total > 0 ? (double) totalMemoryUsedMB.get() / total : 0.0;
    }

    /**
     * Gets the time since the last illustration was generated.
     *
     * @return duration since last illustration
     */
    public Duration getTimeSinceLastIllustration() {
        return Duration.between(lastIllustrationTime, LocalDateTime.now());
    }

    /**
     * Gets the total uptime of the metrics system.
     *
     * @return total uptime duration
     */
    public Duration getTotalUptime() {
        return Duration.between(startTime, LocalDateTime.now());
    }

    /** Resets all metrics to their initial values. */
    public void reset() {
        totalIllustrationsGenerated.set(0);
        illustrationsSkipped.set(0);
        illustrationsBatched.set(0);
        illustrationsDeferred.set(0);
        totalProcessingTimeMs.set(0);
        minProcessingTimeMs.set(Long.MAX_VALUE);
        maxProcessingTimeMs.set(0);
        totalMemoryUsedMB.set(0);
        activeIllustrationThreads.set(0);
        highQualityIllustrations.set(0);
        lowQualityIllustrations.set(0);
        lastIllustrationTime = LocalDateTime.now();
    }

    /**
     * Creates a snapshot of current metrics.
     *
     * @return immutable snapshot of current metrics
     */
    public MetricsSnapshot snapshot() {
        return MetricsSnapshot.builder()
                .totalIllustrationsGenerated(totalIllustrationsGenerated.get())
                .illustrationsSkipped(illustrationsSkipped.get())
                .illustrationsBatched(illustrationsBatched.get())
                .illustrationsDeferred(illustrationsDeferred.get())
                .averageProcessingTimeMs(getAverageProcessingTimeMs())
                .minProcessingTimeMs(minProcessingTimeMs.get())
                .maxProcessingTimeMs(maxProcessingTimeMs.get())
                .illustrationsPerMinute(getIllustrationsPerMinute())
                .skipRate(getSkipRate())
                .highQualityRate(getHighQualityRate())
                .averageMemoryUsageMB(getAverageMemoryUsageMB())
                .activeThreads(activeIllustrationThreads.get())
                .timeSinceLastIllustration(getTimeSinceLastIllustration())
                .totalUptime(getTotalUptime())
                .build();
    }

    /** Immutable snapshot of performance metrics at a point in time. */
    @Data
    @lombok.Builder
    public static class MetricsSnapshot {
        private final int totalIllustrationsGenerated;
        private final int illustrationsSkipped;
        private final int illustrationsBatched;
        private final int illustrationsDeferred;
        private final double averageProcessingTimeMs;
        private final long minProcessingTimeMs;
        private final long maxProcessingTimeMs;
        private final double illustrationsPerMinute;
        private final double skipRate;
        private final double highQualityRate;
        private final double averageMemoryUsageMB;
        private final int activeThreads;
        private final Duration timeSinceLastIllustration;
        private final Duration totalUptime;
    }
}
