package io.github.jspinak.brobot.runner.ui.illustration.analytics;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;

import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.runner.ui.illustration.streaming.IllustrationStreamEvent;

import lombok.Getter;

/**
 * Service for collecting and analyzing illustration metrics.
 *
 * <p>This service aggregates data from the illustration system to provide insights into
 * performance, quality, and usage patterns.
 *
 * @see IllustrationAnalyticsDashboard
 * @see AnalyticsSnapshot
 */
@Service
@Getter
public class IllustrationAnalyticsService {

    // Counters
    private final AtomicInteger totalIllustrations = new AtomicInteger(0);
    private final AtomicInteger successfulIllustrations = new AtomicInteger(0);
    private final AtomicInteger skippedIllustrations = new AtomicInteger(0);
    private final AtomicInteger batchedIllustrations = new AtomicInteger(0);

    // Action-specific counters
    private final Map<String, AtomicInteger> actionCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> actionSuccesses = new ConcurrentHashMap<>();

    // Performance metrics
    private final DoubleAdder totalProcessingTime = new DoubleAdder();
    private final AtomicLong minProcessingTime = new AtomicLong(Long.MAX_VALUE);
    private final AtomicLong maxProcessingTime = new AtomicLong(0);

    // Quality metrics
    private final AtomicInteger highQualityCount = new AtomicInteger(0);
    private final DoubleAdder totalQualityScore = new DoubleAdder();

    // Time tracking
    private final LocalDateTime startTime = LocalDateTime.now();
    private volatile LocalDateTime lastUpdateTime = LocalDateTime.now();

    // Current state
    private volatile int currentQueueSize = 0;
    private volatile double currentSamplingRate = 1.0;

    /**
     * Records an illustration event.
     *
     * @param event the stream event to record
     */
    public void recordIllustrationEvent(IllustrationStreamEvent event) {
        totalIllustrations.incrementAndGet();
        lastUpdateTime = LocalDateTime.now();

        if (event.getMetadata() != null) {
            String actionType = event.getMetadata().getActionType();

            // Update action counts
            actionCounts.computeIfAbsent(actionType, k -> new AtomicInteger(0)).incrementAndGet();

            // Update success counts
            if (event.getMetadata().isSuccess()) {
                successfulIllustrations.incrementAndGet();
                actionSuccesses
                        .computeIfAbsent(actionType, k -> new AtomicInteger(0))
                        .incrementAndGet();
            }

            // Update performance metrics
            if (event.getProcessingTimeMs() > 0) {
                totalProcessingTime.add(event.getProcessingTimeMs());
                updateMinMax(event.getProcessingTimeMs());
            }

            // Update quality metrics if available
            if (event.getMetadata().getPerformanceData() != null) {
                double avgSimilarity =
                        event.getMetadata().getPerformanceData().getAverageSimilarity();
                if (avgSimilarity > 0.8) {
                    highQualityCount.incrementAndGet();
                }
                totalQualityScore.add(avgSimilarity);
            }
        }

        // Update queue size
        currentQueueSize = event.getQueueSize();
    }

    /**
     * Records a skipped illustration.
     *
     * @param reason the reason for skipping
     */
    public void recordSkipped(String reason) {
        skippedIllustrations.incrementAndGet();
    }

    /** Records a batched illustration. */
    public void recordBatched() {
        batchedIllustrations.incrementAndGet();
    }

    /**
     * Updates the current sampling rate.
     *
     * @param rate the new sampling rate
     */
    public void updateSamplingRate(double rate) {
        currentSamplingRate = rate;
    }

    /**
     * Gets the current analytics snapshot.
     *
     * @return snapshot of current metrics
     */
    public AnalyticsSnapshot getCurrentSnapshot() {
        return AnalyticsSnapshot.builder()
                .timestamp(LocalDateTime.now())
                .totalIllustrations(totalIllustrations.get())
                .successfulIllustrations(successfulIllustrations.get())
                .skippedIllustrations(skippedIllustrations.get())
                .batchedIllustrations(batchedIllustrations.get())
                .actionCounts(new ConcurrentHashMap<>(actionCounts))
                .actionSuccesses(new ConcurrentHashMap<>(actionSuccesses))
                .overallSuccessRate(calculateOverallSuccessRate())
                .successRatesByAction(calculateSuccessRatesByAction())
                .averageProcessingTime(calculateAverageProcessingTime())
                .minProcessingTime(
                        minProcessingTime.get() == Long.MAX_VALUE ? 0 : minProcessingTime.get())
                .maxProcessingTime(maxProcessingTime.get())
                .averageTimeByAction(calculateAverageTimeByAction())
                .highQualityRate(calculateHighQualityRate())
                .averageQualityScore(calculateAverageQualityScore())
                .skipRate(calculateSkipRate())
                .batchEfficiency(calculateBatchEfficiency())
                .samplingEffectiveness(calculateSamplingEffectiveness())
                .illustrationsPerMinute(calculateIllustrationsPerMinute())
                .currentQueueSize(currentQueueSize)
                .currentSamplingRate(currentSamplingRate)
                .uptimeDuration(Duration.between(startTime, LocalDateTime.now()))
                .build();
    }

    /** Resets all metrics. */
    public void reset() {
        totalIllustrations.set(0);
        successfulIllustrations.set(0);
        skippedIllustrations.set(0);
        batchedIllustrations.set(0);

        actionCounts.clear();
        actionSuccesses.clear();

        totalProcessingTime.reset();
        minProcessingTime.set(Long.MAX_VALUE);
        maxProcessingTime.set(0);

        highQualityCount.set(0);
        totalQualityScore.reset();

        currentQueueSize = 0;
        lastUpdateTime = LocalDateTime.now();
    }

    private void updateMinMax(long processingTime) {
        minProcessingTime.updateAndGet(current -> Math.min(current, processingTime));
        maxProcessingTime.updateAndGet(current -> Math.max(current, processingTime));
    }

    private double calculateOverallSuccessRate() {
        int total = totalIllustrations.get();
        return total > 0 ? (double) successfulIllustrations.get() / total : 0.0;
    }

    private Map<String, Double> calculateSuccessRatesByAction() {
        Map<String, Double> rates = new ConcurrentHashMap<>();

        actionCounts.forEach(
                (action, count) -> {
                    int successes =
                            actionSuccesses.getOrDefault(action, new AtomicInteger(0)).get();
                    rates.put(action, count.get() > 0 ? (double) successes / count.get() : 0.0);
                });

        return rates;
    }

    private double calculateAverageProcessingTime() {
        int total = totalIllustrations.get();
        return total > 0 ? totalProcessingTime.sum() / total : 0.0;
    }

    private Map<String, Double> calculateAverageTimeByAction() {
        // This would require per-action time tracking
        // For now, return empty map
        return new ConcurrentHashMap<>();
    }

    private double calculateHighQualityRate() {
        int total = totalIllustrations.get();
        return total > 0 ? (double) highQualityCount.get() / total : 0.0;
    }

    private double calculateAverageQualityScore() {
        int total = totalIllustrations.get();
        return total > 0 ? totalQualityScore.sum() / total : 0.0;
    }

    private double calculateSkipRate() {
        int total = totalIllustrations.get() + skippedIllustrations.get();
        return total > 0 ? (double) skippedIllustrations.get() / total : 0.0;
    }

    private double calculateBatchEfficiency() {
        int processed = totalIllustrations.get();
        return processed > 0 ? (double) batchedIllustrations.get() / processed : 0.0;
    }

    private double calculateSamplingEffectiveness() {
        // Effectiveness = high quality rate at current sampling rate
        return calculateHighQualityRate() * currentSamplingRate;
    }

    private double calculateIllustrationsPerMinute() {
        Duration uptime = Duration.between(startTime, LocalDateTime.now());
        double minutes = uptime.toSeconds() / 60.0;
        return minutes > 0 ? totalIllustrations.get() / minutes : 0.0;
    }
}
