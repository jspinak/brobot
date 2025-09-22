package io.github.jspinak.brobot.logging.events;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import io.github.jspinak.brobot.logging.LogLevel;

import lombok.Builder;
import lombok.Value;

/**
 * Event representing performance metrics for Brobot operations.
 *
 * <p>Captures detailed timing and resource usage information for operations, including memory usage
 * and timing breakdowns. This is essential for performance monitoring and optimization.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * PerformanceEvent event = PerformanceEvent.builder()
 *     .operation("findAndClick")
 *     .duration(Duration.ofMillis(250))
 *     .memoryUsed(1024 * 1024) // 1MB
 *     .breakdown(Map.of(
 *         "find", Duration.ofMillis(200),
 *         "click", Duration.ofMillis(50)
 *     ))
 *     .build();
 * }</pre>
 */
@Value
@Builder(toBuilder = true)
public class PerformanceEvent {

    /** Timestamp when the operation was initiated */
    @Builder.Default Instant timestamp = Instant.now();

    /** Name/description of the operation */
    String operation;

    /** Total duration of the operation */
    Duration duration;

    /** Memory used by the operation in bytes */
    long memoryUsed;

    /** Detailed timing breakdown of sub-operations */
    @Builder.Default Map<String, Duration> breakdown = java.util.Collections.emptyMap();

    /** Additional performance metadata */
    @Builder.Default Map<String, Object> metadata = java.util.Collections.emptyMap();

    /** Correlation ID for tracking related operations */
    String correlationId;

    /** Current state when operation was performed */
    String currentState;

    /** Thread name where operation was executed */
    String threadName;

    /** CPU time used (if available) */
    Duration cpuTime;

    /** Number of method calls or iterations */
    long operationCount;

    /** Whether this operation was considered successful */
    boolean success;

    /** Error message if operation failed */
    String errorMessage;

    /** Peak memory usage during operation */
    long peakMemoryUsed;

    /** Memory usage before operation started */
    long memoryBefore;

    /** Memory usage after operation completed */
    long memoryAfter;

    /**
     * Create a PerformanceEvent for a successful operation.
     *
     * @param operation The operation name
     * @param duration The total duration
     * @return A new PerformanceEvent for a successful operation
     */
    public static PerformanceEvent success(String operation, Duration duration) {
        return PerformanceEvent.builder()
                .operation(operation)
                .duration(duration)
                .success(true)
                .build();
    }

    /**
     * Create a PerformanceEvent for a failed operation.
     *
     * @param operation The operation name
     * @param duration The duration before failure
     * @param errorMessage The error message
     * @return A new PerformanceEvent for a failed operation
     */
    public static PerformanceEvent failure(
            String operation, Duration duration, String errorMessage) {
        return PerformanceEvent.builder()
                .operation(operation)
                .duration(duration)
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * Get memory delta (difference between before and after).
     *
     * @return Memory delta in bytes (positive = memory increased)
     */
    public long getMemoryDelta() {
        return memoryAfter - memoryBefore;
    }

    /**
     * Get memory usage in megabytes.
     *
     * @return Memory used in MB
     */
    public double getMemoryUsedMB() {
        return memoryUsed / (1024.0 * 1024.0);
    }

    /**
     * Get peak memory usage in megabytes.
     *
     * @return Peak memory used in MB
     */
    public double getPeakMemoryUsedMB() {
        return peakMemoryUsed / (1024.0 * 1024.0);
    }

    /**
     * Get operations per second rate.
     *
     * @return Operations per second, or 0 if duration is zero
     */
    public double getOperationsPerSecond() {
        if (duration.isZero() || operationCount == 0) {
            return 0.0;
        }
        return operationCount / (duration.toMillis() / 1000.0);
    }

    /**
     * Get the percentage of time spent in each sub-operation.
     *
     * @return Map of operation names to percentage of total time
     */
    public Map<String, Double> getBreakdownPercentages() {
        if (breakdown.isEmpty() || duration.isZero()) {
            return java.util.Collections.emptyMap();
        }

        long totalMs = duration.toMillis();
        return breakdown.entrySet().stream()
                .collect(
                        java.util.stream.Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> (entry.getValue().toMillis() * 100.0) / totalMs));
    }

    /**
     * Check if this operation was slow compared to a threshold.
     *
     * @param thresholdMs The threshold in milliseconds
     * @return true if the operation was slower than the threshold
     */
    public boolean isSlow(long thresholdMs) {
        return duration.toMillis() > thresholdMs;
    }

    /**
     * Check if this operation used excessive memory.
     *
     * @param thresholdBytes The threshold in bytes
     * @return true if memory usage exceeded the threshold
     */
    public boolean isMemoryIntensive(long thresholdBytes) {
        return memoryUsed > thresholdBytes || peakMemoryUsed > thresholdBytes;
    }

    /**
     * Get a human-readable description of this performance event.
     *
     * @return A formatted description
     */
    public String getDescription() {
        if (success) {
            String memInfo = memoryUsed > 0 ? String.format(" mem:%.1fMB", getMemoryUsedMB()) : "";
            String countInfo = operationCount > 0 ? String.format(" ops:%d", operationCount) : "";

            return String.format(
                    "PERF %s [%dms]%s%s", operation, duration.toMillis(), memInfo, countInfo);
        } else {
            return String.format(
                    "PERF %s FAILED [%dms]%s",
                    operation, duration.toMillis(), errorMessage != null ? " " + errorMessage : "");
        }
    }

    /**
     * Get a detailed breakdown description.
     *
     * @return A formatted breakdown description
     */
    public String getBreakdownDescription() {
        if (breakdown.isEmpty()) {
            return "";
        }

        Map<String, Double> percentages = getBreakdownPercentages();
        return breakdown.entrySet().stream()
                .map(
                        entry ->
                                String.format(
                                        "%s:%dms(%.1f%%)",
                                        entry.getKey(),
                                        entry.getValue().toMillis(),
                                        percentages.getOrDefault(entry.getKey(), 0.0)))
                .collect(java.util.stream.Collectors.joining(", "));
    }

    /**
     * Get the log level for this performance event. Successful operations log at INFO level,
     * failures at ERROR level, slow operations log at WARN level.
     *
     * @return The appropriate log level
     */
    public LogLevel getLevel() {
        if (!success) {
            return LogLevel.ERROR;
        } else if (isSlow(5000)) { // Consider > 5 seconds as slow
            return LogLevel.WARN;
        } else {
            return LogLevel.INFO;
        }
    }

    /**
     * Get a message describing this performance event.
     *
     * @return A formatted message
     */
    public String getMessage() {
        if (success) {
            String perfInfo = "";
            if (memoryUsed > 0) {
                perfInfo += String.format(", %.1fMB memory", getMemoryUsedMB());
            }
            if (operationCount > 0) {
                perfInfo += String.format(", %d operations", operationCount);
            }
            return String.format(
                    "Operation %s completed in %dms%s", operation, duration.toMillis(), perfInfo);
        } else {
            return String.format(
                    "Operation %s failed after %dms%s",
                    operation,
                    duration.toMillis(),
                    errorMessage != null ? ": " + errorMessage : "");
        }
    }

    /**
     * Get the memory usage for this performance event.
     *
     * @return Memory usage in bytes, or null if not measured
     */
    public Long getMemoryUsage() {
        return memoryUsed > 0 ? memoryUsed : null;
    }

    /**
     * Get the operation count for this performance event.
     *
     * @return The number of operations performed
     */
    public Long getOperationCount() {
        return operationCount > 0 ? operationCount : null;
    }

    /**
     * Get any error associated with this performance event.
     *
     * @return null since performance events only have error messages
     */
    public Throwable getError() {
        return null; // PerformanceEvent only has error messages, not exceptions
    }
}
