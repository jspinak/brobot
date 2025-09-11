package io.github.jspinak.brobot.test.benchmark;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Performance benchmark utility for measuring test execution times and tracking performance
 * metrics.
 */
public class PerformanceBenchmark {

    private static final Map<String, List<BenchmarkResult>> benchmarkResults =
            new ConcurrentHashMap<>();
    private static final Map<String, PerformanceThreshold> thresholds = new ConcurrentHashMap<>();

    /** Represents a single benchmark result. */
    public static class BenchmarkResult {
        private final String name;
        private final long durationNanos;
        private final Instant timestamp;
        private final Map<String, Object> metadata;

        public BenchmarkResult(String name, long durationNanos) {
            this.name = name;
            this.durationNanos = durationNanos;
            this.timestamp = Instant.now();
            this.metadata = new HashMap<>();
        }

        public long getDurationMillis() {
            return TimeUnit.NANOSECONDS.toMillis(durationNanos);
        }

        public double getDurationSeconds() {
            return durationNanos / 1_000_000_000.0;
        }

        public void addMetadata(String key, Object value) {
            metadata.put(key, value);
        }

        @Override
        public String toString() {
            return String.format("%s: %.3f ms", name, getDurationMillis() / 1000.0);
        }
    }

    /** Represents performance thresholds for a specific operation. */
    public static class PerformanceThreshold {
        private final long warningThresholdMillis;
        private final long errorThresholdMillis;

        public PerformanceThreshold(long warningMillis, long errorMillis) {
            this.warningThresholdMillis = warningMillis;
            this.errorThresholdMillis = errorMillis;
        }

        public boolean isWithinWarning(long durationMillis) {
            return durationMillis <= warningThresholdMillis;
        }

        public boolean isWithinError(long durationMillis) {
            return durationMillis <= errorThresholdMillis;
        }
    }

    /** Measures the execution time of a code block. */
    public static <T> T measure(String name, Supplier<T> operation) {
        Instant start = Instant.now();
        try {
            return operation.get();
        } finally {
            Instant end = Instant.now();
            long durationNanos = Duration.between(start, end).toNanos();
            recordResult(name, durationNanos);
        }
    }

    /** Measures the execution time of a runnable. */
    public static void measure(String name, Runnable operation) {
        measure(
                name,
                () -> {
                    operation.run();
                    return null;
                });
    }

    /** Measures execution time with warmup runs. */
    public static <T> T measureWithWarmup(String name, int warmupRuns, Supplier<T> operation) {
        // Warmup runs
        for (int i = 0; i < warmupRuns; i++) {
            operation.get();
        }

        // Actual measurement
        return measure(name, operation);
    }

    /** Records a benchmark result. */
    private static void recordResult(String name, long durationNanos) {
        BenchmarkResult result = new BenchmarkResult(name, durationNanos);
        benchmarkResults.computeIfAbsent(name, k -> new ArrayList<>()).add(result);

        // Check thresholds
        PerformanceThreshold threshold = thresholds.get(name);
        if (threshold != null) {
            long durationMillis = TimeUnit.NANOSECONDS.toMillis(durationNanos);
            if (!threshold.isWithinError(durationMillis)) {
                System.err.printf(
                        "PERFORMANCE ERROR: %s took %d ms (threshold: %d ms)%n",
                        name, durationMillis, threshold.errorThresholdMillis);
            } else if (!threshold.isWithinWarning(durationMillis)) {
                System.out.printf(
                        "PERFORMANCE WARNING: %s took %d ms (threshold: %d ms)%n",
                        name, durationMillis, threshold.warningThresholdMillis);
            }
        }
    }

    /** Sets performance thresholds for an operation. */
    public static void setThreshold(String name, long warningMillis, long errorMillis) {
        thresholds.put(name, new PerformanceThreshold(warningMillis, errorMillis));
    }

    /** Gets statistics for a named operation. */
    public static BenchmarkStatistics getStatistics(String name) {
        List<BenchmarkResult> results = benchmarkResults.get(name);
        if (results == null || results.isEmpty()) {
            return null;
        }
        return new BenchmarkStatistics(name, results);
    }

    /** Gets all benchmark statistics. */
    public static Map<String, BenchmarkStatistics> getAllStatistics() {
        Map<String, BenchmarkStatistics> stats = new HashMap<>();
        for (Map.Entry<String, List<BenchmarkResult>> entry : benchmarkResults.entrySet()) {
            stats.put(entry.getKey(), new BenchmarkStatistics(entry.getKey(), entry.getValue()));
        }
        return stats;
    }

    /** Clears all benchmark results. */
    public static void clear() {
        benchmarkResults.clear();
    }

    /** Clears results for a specific operation. */
    public static void clear(String name) {
        benchmarkResults.remove(name);
    }

    /** Prints a summary of all benchmarks. */
    public static void printSummary() {
        System.out.println("\n=== Performance Benchmark Summary ===");
        Map<String, BenchmarkStatistics> stats = getAllStatistics();

        if (stats.isEmpty()) {
            System.out.println("No benchmarks recorded");
            return;
        }

        for (BenchmarkStatistics stat : stats.values()) {
            System.out.println(stat);
        }
        System.out.println("=====================================\n");
    }

    /** Statistics for benchmark results. */
    public static class BenchmarkStatistics {
        private final String name;
        private final int count;
        private final double avgMillis;
        private final double minMillis;
        private final double maxMillis;
        private final double stdDevMillis;
        private final double medianMillis;

        public BenchmarkStatistics(String name, List<BenchmarkResult> results) {
            this.name = name;
            this.count = results.size();

            List<Long> durations = results.stream().map(r -> r.durationNanos).sorted().toList();

            // Calculate statistics
            long sum = durations.stream().mapToLong(Long::longValue).sum();
            this.avgMillis = TimeUnit.NANOSECONDS.toMillis(sum) / (double) count;
            this.minMillis = TimeUnit.NANOSECONDS.toMillis(durations.get(0)) / 1000.0;
            this.maxMillis = TimeUnit.NANOSECONDS.toMillis(durations.get(count - 1)) / 1000.0;

            // Median
            if (count % 2 == 0) {
                long median = (durations.get(count / 2 - 1) + durations.get(count / 2)) / 2;
                this.medianMillis = TimeUnit.NANOSECONDS.toMillis(median) / 1000.0;
            } else {
                this.medianMillis =
                        TimeUnit.NANOSECONDS.toMillis(durations.get(count / 2)) / 1000.0;
            }

            // Standard deviation
            double variance = 0;
            for (long duration : durations) {
                double millis = TimeUnit.NANOSECONDS.toMillis(duration) / 1000.0;
                variance += Math.pow(millis - avgMillis / 1000.0, 2);
            }
            this.stdDevMillis = Math.sqrt(variance / count);
        }

        @Override
        public String toString() {
            return String.format(
                    "%s: count=%d, avg=%.3fs, min=%.3fs, max=%.3fs, median=%.3fs, stdDev=%.3fs",
                    name,
                    count,
                    avgMillis / 1000.0,
                    minMillis,
                    maxMillis,
                    medianMillis,
                    stdDevMillis);
        }

        public String toDetailedString() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Benchmark: %s%n", name));
            sb.append(String.format("  Executions: %d%n", count));
            sb.append(String.format("  Average:    %.3f ms%n", avgMillis));
            sb.append(String.format("  Minimum:    %.3f ms%n", minMillis * 1000));
            sb.append(String.format("  Maximum:    %.3f ms%n", maxMillis * 1000));
            sb.append(String.format("  Median:     %.3f ms%n", medianMillis * 1000));
            sb.append(String.format("  Std Dev:    %.3f ms%n", stdDevMillis * 1000));
            return sb.toString();
        }
    }
}
