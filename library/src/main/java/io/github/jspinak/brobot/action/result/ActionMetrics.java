package io.github.jspinak.brobot.action.result;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;

/**
 * Captures metrics and performance data for action execution. Provides detailed performance
 * tracking and analysis.
 *
 * <p>This class encapsulates metrics functionality that was previously embedded in ActionResult.
 *
 * @since 2.0
 */
@Data
public class ActionMetrics {
    private long executionTimeMs;
    private int matchCount;
    private double bestMatchConfidence = 0.0;
    private String threadName;
    private String actionId;
    private int retryCount = 0;
    private long retryTimeMs = 0;
    private Map<String, Long> phaseTimings = new HashMap<>();
    private Map<String, Integer> phaseCounts = new HashMap<>();

    /** Creates ActionMetrics with current thread name. */
    public ActionMetrics() {
        this.threadName = Thread.currentThread().getName();
    }

    /**
     * Creates ActionMetrics with specified action ID.
     *
     * @param actionId Unique identifier for the action
     */
    public ActionMetrics(String actionId) {
        this();
        this.actionId = actionId;
    }

    /**
     * Records the execution time.
     *
     * @param duration The execution duration
     */
    public void recordExecutionTime(Duration duration) {
        if (duration != null) {
            this.executionTimeMs = duration.toMillis();
        }
    }

    /**
     * Records the execution time in milliseconds.
     *
     * @param timeMs The execution time in milliseconds
     */
    public void recordExecutionTime(long timeMs) {
        this.executionTimeMs = timeMs;
    }

    /**
     * Records the execution time from start to end.
     *
     * @param start Start instant
     * @param end End instant
     */
    public void recordExecutionTime(Instant start, Instant end) {
        if (start != null && end != null) {
            this.executionTimeMs = Duration.between(start, end).toMillis();
        }
    }

    /**
     * Records a retry attempt.
     *
     * @param retryDuration Duration of the retry
     */
    public void recordRetry(Duration retryDuration) {
        retryCount++;
        if (retryDuration != null) {
            retryTimeMs += retryDuration.toMillis();
        }
    }

    /**
     * Records timing for a specific phase.
     *
     * @param phaseName Name of the phase
     * @param duration Duration of the phase
     */
    public void recordPhase(String phaseName, Duration duration) {
        if (phaseName != null && duration != null) {
            phaseTimings.merge(phaseName, duration.toMillis(), Long::sum);
            phaseCounts.merge(phaseName, 1, Integer::sum);
        }
    }

    /**
     * Records timing for a specific phase.
     *
     * @param phaseName Name of the phase
     * @param timeMs Duration in milliseconds
     */
    public void recordPhase(String phaseName, long timeMs) {
        if (phaseName != null) {
            phaseTimings.merge(phaseName, timeMs, Long::sum);
            phaseCounts.merge(phaseName, 1, Integer::sum);
        }
    }

    /**
     * Gets the average time for a phase.
     *
     * @param phaseName Name of the phase
     * @return Average time in milliseconds, or 0 if phase not found
     */
    public long getAveragePhaseTime(String phaseName) {
        Long totalTime = phaseTimings.get(phaseName);
        Integer count = phaseCounts.get(phaseName);

        if (totalTime != null && count != null && count > 0) {
            return totalTime / count;
        }
        return 0;
    }

    /**
     * Gets the total time for a phase.
     *
     * @param phaseName Name of the phase
     * @return Total time in milliseconds, or 0 if phase not found
     */
    public long getTotalPhaseTime(String phaseName) {
        return phaseTimings.getOrDefault(phaseName, 0L);
    }

    /**
     * Gets the count of phase executions.
     *
     * @param phaseName Name of the phase
     * @return Number of times the phase was executed
     */
    public int getPhaseCount(String phaseName) {
        return phaseCounts.getOrDefault(phaseName, 0);
    }

    /**
     * Gets the total overhead time (execution time minus phase times).
     *
     * @return Overhead time in milliseconds
     */
    public long getOverheadTimeMs() {
        long totalPhaseTime = phaseTimings.values().stream().mapToLong(Long::longValue).sum();
        return Math.max(0, executionTimeMs - totalPhaseTime);
    }

    /**
     * Gets the retry overhead percentage.
     *
     * @return Percentage of time spent in retries
     */
    public double getRetryOverheadPercentage() {
        if (executionTimeMs == 0) {
            return 0.0;
        }
        return (retryTimeMs * 100.0) / executionTimeMs;
    }

    /**
     * Calculates the efficiency score. Based on match confidence and retry overhead.
     *
     * @return Efficiency score between 0.0 and 1.0
     */
    public double getEfficiencyScore() {
        double confidenceScore = bestMatchConfidence;
        double retryPenalty = 1.0 - (getRetryOverheadPercentage() / 100.0);
        return (confidenceScore + retryPenalty) / 2.0;
    }

    /**
     * Checks if the action had retries.
     *
     * @return true if retries occurred
     */
    public boolean hasRetries() {
        return retryCount > 0;
    }

    /**
     * Checks if phase timings are recorded.
     *
     * @return true if phase timings exist
     */
    public boolean hasPhaseTimings() {
        return !phaseTimings.isEmpty();
    }

    /**
     * Merges metrics from another instance.
     *
     * @param other The ActionMetrics to merge
     */
    public void merge(ActionMetrics other) {
        if (other != null) {
            executionTimeMs += other.executionTimeMs;
            matchCount += other.matchCount;
            bestMatchConfidence = Math.max(bestMatchConfidence, other.bestMatchConfidence);
            retryCount += other.retryCount;
            retryTimeMs += other.retryTimeMs;

            // Merge phase timings
            other.phaseTimings.forEach((phase, time) -> phaseTimings.merge(phase, time, Long::sum));
            other.phaseCounts.forEach(
                    (phase, count) -> phaseCounts.merge(phase, count, Integer::sum));
        }
    }

    /**
     * Formats the metrics as a performance summary.
     *
     * @return Formatted performance summary
     */
    public String formatPerformance() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Execution: %dms", executionTimeMs));

        if (matchCount > 0) {
            sb.append(String.format(", Matches: %d (best: %.2f)", matchCount, bestMatchConfidence));
        }

        if (hasRetries()) {
            sb.append(
                    String.format(
                            ", Retries: %d (%dms, %.1f%%)",
                            retryCount, retryTimeMs, getRetryOverheadPercentage()));
        }

        if (hasPhaseTimings()) {
            sb.append(", Phases: ").append(phaseTimings.size());
            long overhead = getOverheadTimeMs();
            if (overhead > 0) {
                sb.append(String.format(" (overhead: %dms)", overhead));
            }
        }

        sb.append(String.format(", Efficiency: %.1f%%", getEfficiencyScore() * 100));

        return sb.toString();
    }

    /**
     * Formats detailed phase breakdown.
     *
     * @return Formatted phase timing details
     */
    public String formatPhases() {
        if (!hasPhaseTimings()) {
            return "No phase timings recorded";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Phase Breakdown:\n");

        phaseTimings.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(
                        entry -> {
                            String phase = entry.getKey();
                            long time = entry.getValue();
                            int count = phaseCounts.get(phase);
                            double percentage = (time * 100.0) / executionTimeMs;

                            sb.append(
                                    String.format(
                                            "  %s: %dms (%.1f%%) x%d",
                                            phase, time, percentage, count));

                            if (count > 1) {
                                sb.append(String.format(" avg: %dms", time / count));
                            }
                            sb.append("\n");
                        });

        long overhead = getOverheadTimeMs();
        if (overhead > 0) {
            double overheadPercentage = (overhead * 100.0) / executionTimeMs;
            sb.append(String.format("  Overhead: %dms (%.1f%%)\n", overhead, overheadPercentage));
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return formatPerformance();
    }
}
