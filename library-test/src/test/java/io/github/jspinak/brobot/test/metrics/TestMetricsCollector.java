package io.github.jspinak.brobot.test.metrics;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * Collects detailed metrics about test execution for performance analysis. Thread-safe for parallel
 * test execution.
 */
public class TestMetricsCollector implements TestExecutionListener {

    private final Map<TestIdentifier, TestMetrics> testMetrics = new ConcurrentHashMap<>();
    private final Map<String, CategoryMetrics> categoryMetrics = new ConcurrentHashMap<>();
    private final AtomicInteger totalTests = new AtomicInteger(0);
    private final AtomicInteger passedTests = new AtomicInteger(0);
    private final AtomicInteger failedTests = new AtomicInteger(0);
    private final AtomicInteger skippedTests = new AtomicInteger(0);
    private final AtomicLong totalDuration = new AtomicLong(0);

    private Instant suiteStartTime;
    private Instant suiteEndTime;

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        suiteStartTime = Instant.now();
        System.out.println("Test suite started at " + suiteStartTime);
        System.out.println(
                "Total test count: " + testPlan.countTestIdentifiers(TestIdentifier::isTest));
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        suiteEndTime = Instant.now();
        printFinalReport();
    }

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        if (testIdentifier.isTest()) {
            TestMetrics metrics = new TestMetrics(testIdentifier);
            metrics.startTime = Instant.now();
            testMetrics.put(testIdentifier, metrics);
            totalTests.incrementAndGet();
        }
    }

    @Override
    public void executionFinished(
            TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        if (testIdentifier.isTest()) {
            TestMetrics metrics = testMetrics.get(testIdentifier);
            if (metrics != null) {
                metrics.endTime = Instant.now();
                metrics.duration = Duration.between(metrics.startTime, metrics.endTime);
                metrics.status = testExecutionResult.getStatus();
                metrics.throwable = testExecutionResult.getThrowable().orElse(null);

                // Update counters
                switch (testExecutionResult.getStatus()) {
                    case SUCCESSFUL:
                        passedTests.incrementAndGet();
                        break;
                    case FAILED:
                        failedTests.incrementAndGet();
                        break;
                    case ABORTED:
                        skippedTests.incrementAndGet();
                        break;
                }

                totalDuration.addAndGet(metrics.duration.toMillis());

                // Update category metrics
                updateCategoryMetrics(testIdentifier, metrics);
            }
        }
    }

    private void updateCategoryMetrics(TestIdentifier testIdentifier, TestMetrics metrics) {
        Set<String> tags =
                testIdentifier.getTags().stream()
                        .map(tag -> tag.getName())
                        .collect(Collectors.toSet());

        tags.forEach(
                tag -> {
                    CategoryMetrics catMetrics =
                            categoryMetrics.computeIfAbsent(tag, k -> new CategoryMetrics(k));
                    catMetrics.addTest(metrics);
                });
    }

    private void printFinalReport() {
        Duration totalSuiteDuration = Duration.between(suiteStartTime, suiteEndTime);

        System.out.println("\n" + "=".repeat(80));
        System.out.println("TEST EXECUTION METRICS REPORT");
        System.out.println("=".repeat(80));

        // Overall statistics
        System.out.println("\nOVERALL STATISTICS:");
        System.out.println(String.format("  Total Tests:      %d", totalTests.get()));
        System.out.println(
                String.format(
                        "  Passed:           %d (%.1f%%)",
                        passedTests.get(), (passedTests.get() * 100.0) / totalTests.get()));
        System.out.println(
                String.format(
                        "  Failed:           %d (%.1f%%)",
                        failedTests.get(), (failedTests.get() * 100.0) / totalTests.get()));
        System.out.println(
                String.format(
                        "  Skipped:          %d (%.1f%%)",
                        skippedTests.get(), (skippedTests.get() * 100.0) / totalTests.get()));
        System.out.println(
                String.format("  Suite Duration:   %.2fs", totalSuiteDuration.toMillis() / 1000.0));
        System.out.println(
                String.format(
                        "  Throughput:       %.1f tests/sec",
                        totalTests.get() / (totalSuiteDuration.toMillis() / 1000.0)));
        System.out.println(
                String.format(
                        "  Avg Test Time:    %.0fms",
                        totalDuration.get() / (double) totalTests.get()));

        // Category breakdown
        if (!categoryMetrics.isEmpty()) {
            System.out.println("\nCATEGORY BREAKDOWN:");
            categoryMetrics.values().stream()
                    .sorted(Comparator.comparing(CategoryMetrics::getTestCount).reversed())
                    .forEach(
                            cat -> {
                                System.out.println(String.format("  %s:", cat.categoryName));
                                System.out.println(
                                        String.format(
                                                "    Tests: %d, Passed: %d, Failed: %d, Avg Time:"
                                                        + " %.0fms",
                                                cat.getTestCount(),
                                                cat.getPassed(),
                                                cat.getFailed(),
                                                cat.getAverageDuration()));
                            });
        }

        // Slowest tests
        System.out.println("\nSLOWEST TESTS:");
        testMetrics.values().stream()
                .sorted(Comparator.comparing((TestMetrics m) -> m.duration).reversed())
                .limit(10)
                .forEach(
                        metrics -> {
                            String testName = metrics.testIdentifier.getDisplayName();
                            System.out.println(
                                    String.format(
                                            "  %s: %.2fs",
                                            testName, metrics.duration.toMillis() / 1000.0));
                        });

        // Failed tests
        if (failedTests.get() > 0) {
            System.out.println("\nFAILED TESTS:");
            testMetrics.values().stream()
                    .filter(m -> m.status == TestExecutionResult.Status.FAILED)
                    .forEach(
                            metrics -> {
                                String testName = metrics.testIdentifier.getDisplayName();
                                System.out.println(String.format("  %s", testName));
                                if (metrics.throwable != null) {
                                    System.out.println(
                                            String.format(
                                                    "    Cause: %s",
                                                    metrics.throwable.getMessage()));
                                }
                            });
        }

        System.out.println("\n" + "=".repeat(80));
    }

    /** Get metrics for analysis */
    public Map<TestIdentifier, TestMetrics> getTestMetrics() {
        return new HashMap<>(testMetrics);
    }

    public Map<String, CategoryMetrics> getCategoryMetrics() {
        return new HashMap<>(categoryMetrics);
    }

    /** Identifies potential flaky tests based on historical data */
    public List<String> identifyFlakyTests(List<TestExecutionHistory> history) {
        Map<String, Integer> failureCount = new HashMap<>();
        Map<String, Integer> totalRuns = new HashMap<>();

        history.forEach(
                run -> {
                    run.getTestResults()
                            .forEach(
                                    (testName, passed) -> {
                                        totalRuns.merge(testName, 1, Integer::sum);
                                        if (!passed) {
                                            failureCount.merge(testName, 1, Integer::sum);
                                        }
                                    });
                });

        return failureCount.entrySet().stream()
                .filter(
                        entry -> {
                            int failures = entry.getValue();
                            int total = totalRuns.get(entry.getKey());
                            double failureRate = failures / (double) total;
                            // Consider flaky if fails 20-80% of the time
                            return failureRate > 0.2 && failureRate < 0.8;
                        })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /** Inner class for test metrics */
    public static class TestMetrics {
        private final TestIdentifier testIdentifier;
        private Instant startTime;
        private Instant endTime;
        private Duration duration;
        private TestExecutionResult.Status status;
        private Throwable throwable;

        public TestMetrics(TestIdentifier testIdentifier) {
            this.testIdentifier = testIdentifier;
        }

        // Getters
        public Duration getDuration() {
            return duration;
        }

        public TestExecutionResult.Status getStatus() {
            return status;
        }

        public Throwable getThrowable() {
            return throwable;
        }
    }

    /** Inner class for category metrics */
    public static class CategoryMetrics {
        private final String categoryName;
        private final AtomicInteger testCount = new AtomicInteger(0);
        private final AtomicInteger passed = new AtomicInteger(0);
        private final AtomicInteger failed = new AtomicInteger(0);
        private final AtomicLong totalDuration = new AtomicLong(0);

        public CategoryMetrics(String categoryName) {
            this.categoryName = categoryName;
        }

        public void addTest(TestMetrics metrics) {
            testCount.incrementAndGet();
            if (metrics.status == TestExecutionResult.Status.SUCCESSFUL) {
                passed.incrementAndGet();
            } else if (metrics.status == TestExecutionResult.Status.FAILED) {
                failed.incrementAndGet();
            }
            totalDuration.addAndGet(metrics.duration.toMillis());
        }

        public int getTestCount() {
            return testCount.get();
        }

        public int getPassed() {
            return passed.get();
        }

        public int getFailed() {
            return failed.get();
        }

        public double getAverageDuration() {
            return testCount.get() > 0 ? totalDuration.get() / (double) testCount.get() : 0;
        }
    }

    /** Class for tracking test execution history */
    public static class TestExecutionHistory {
        private final Map<String, Boolean> testResults = new HashMap<>();
        private final Instant executionTime;

        public TestExecutionHistory(Instant executionTime) {
            this.executionTime = executionTime;
        }

        public void addResult(String testName, boolean passed) {
            testResults.put(testName, passed);
        }

        public Map<String, Boolean> getTestResults() {
            return testResults;
        }
    }
}
