package io.github.jspinak.brobot.aspects.monitoring;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.logging.unified.LogEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * Aspect that provides comprehensive performance monitoring for Brobot operations.
 *
 * <p>This aspect tracks execution times, identifies bottlenecks, and provides performance analytics
 * without requiring any changes to existing code.
 *
 * <p>Features: - Method-level performance tracking - Statistical analysis (min, max, avg,
 * percentiles) - Performance trend detection - Slow operation alerts - Periodic performance reports
 * - Memory usage correlation
 *
 * <p>The aspect can be configured to monitor specific packages or methods and provides both
 * real-time alerts and aggregated reports.
 */
@Aspect
@Component
@Slf4j
@ConditionalOnProperty(
        prefix = "brobot.aspects.performance",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class PerformanceMonitoringAspect {

    private final BrobotLogger brobotLogger;

    @Autowired
    public PerformanceMonitoringAspect(BrobotLogger brobotLogger) {
        this.brobotLogger = brobotLogger;
    }

    @Value("${brobot.aspects.performance.alert-threshold:10000}")
    private long alertThresholdMillis;

    @Value("${brobot.aspects.performance.report-interval:300}")
    private int reportIntervalSeconds;

    @Value("${brobot.aspects.performance.track-memory:true}")
    private boolean trackMemoryUsage;

    // Performance data storage
    private final ConcurrentHashMap<String, MethodPerformanceStats> performanceStats =
            new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<Long>> recentExecutions =
            new ConcurrentHashMap<>();

    // Recursion guard to prevent infinite loops
    private final ThreadLocal<Boolean> isMonitoring = ThreadLocal.withInitial(() -> false);

    // Global counters
    private final LongAdder totalMethodCalls = new LongAdder();
    private final AtomicLong totalExecutionTime = new AtomicLong();

    @PostConstruct
    public void init() {
        log.info(
                "Performance Monitoring Aspect initialized with alert threshold: {}ms",
                alertThresholdMillis);
    }

    /** Pointcut for initialization methods to exclude */
    @Pointcut(
            "execution(* *.init(..)) || execution(* *.initialize(..)) || "
                    + "execution(* *.postConstruct(..)) || execution(* *.afterPropertiesSet(..))")
    public void initializationMethods() {}

    /** Pointcut for logging and monitoring infrastructure to exclude */
    @Pointcut(
            "within(io.github.jspinak.brobot.logger..*) || "
                    + "within(io.github.jspinak.brobot.aspects..*) || "
                    + "within(io.github.jspinak.brobot.config..*) || "
                    + "within(org.springframework..*) || "
                    + "within(org.aspectj..*)")
    public void infrastructureMethods() {}

    /** Pointcut for action package methods */
    @Pointcut("execution(public * io.github.jspinak.brobot.action..*(..))")
    public void actionMethods() {}

    /** Pointcut for navigation package methods */
    @Pointcut("execution(public * io.github.jspinak.brobot.navigation..*(..))")
    public void navigationMethods() {}

    /** Pointcut for state management methods */
    @Pointcut("execution(public * io.github.jspinak.brobot.state..*(..))")
    public void stateMethods() {}

    /** Combined pointcut for all monitored methods with exclusions */
    @Pointcut(
            "(actionMethods() || navigationMethods() || stateMethods()) && "
                    + "!initializationMethods() && !infrastructureMethods()")
    public void monitoredMethods() {}

    /** Main performance monitoring advice */
    @Around("monitoredMethods()")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        // Check recursion guard
        if (isMonitoring.get()) {
            // Already monitoring in this thread, skip to avoid recursion
            return joinPoint.proceed();
        }

        String methodSignature = joinPoint.getSignature().toShortString();

        // Skip toString, hashCode, equals methods
        String methodName = joinPoint.getSignature().getName();
        if (methodName.equals("toString")
                || methodName.equals("hashCode")
                || methodName.equals("equals")) {
            return joinPoint.proceed();
        }

        // Set recursion guard
        isMonitoring.set(true);

        // Capture start metrics
        long startTime = System.currentTimeMillis();
        long startMemory = trackMemoryUsage ? getUsedMemory() : 0;

        try {
            // Execute the method
            Object result = joinPoint.proceed();

            // Capture end metrics
            long executionTime = System.currentTimeMillis() - startTime;
            long memoryDelta = trackMemoryUsage ? getUsedMemory() - startMemory : 0;

            // Record performance data
            recordPerformanceData(methodSignature, executionTime, true, memoryDelta);

            // Check for slow operations
            if (executionTime > alertThresholdMillis) {
                logSlowOperation(methodSignature, executionTime, joinPoint.getArgs());
            }

            return result;

        } catch (Throwable e) {
            // Record failure
            long executionTime = System.currentTimeMillis() - startTime;
            recordPerformanceData(methodSignature, executionTime, false, 0);
            throw e;
        } finally {
            // Clear recursion guard
            isMonitoring.set(false);
        }
    }

    /** Record performance data for a method execution */
    private void recordPerformanceData(
            String method, long executionTime, boolean success, long memoryDelta) {
        // Update statistics
        performanceStats.compute(
                method,
                (k, stats) -> {
                    if (stats == null) {
                        stats = new MethodPerformanceStats(method);
                    }
                    stats.recordExecution(executionTime, success, memoryDelta);
                    return stats;
                });

        // Keep recent executions for trend analysis
        recentExecutions.compute(
                method,
                (k, list) -> {
                    if (list == null) {
                        list = Collections.synchronizedList(new ArrayList<>());
                    }
                    list.add(executionTime);

                    // Keep only last 100 executions
                    if (list.size() > 100) {
                        list.remove(0);
                    }
                    return list;
                });

        // Update global counters
        totalMethodCalls.increment();
        totalExecutionTime.addAndGet(executionTime);
    }

    /** Log slow operation with details */
    private void logSlowOperation(String method, long executionTime, Object[] args) {
        brobotLogger
                .log()
                .type(LogEvent.Type.PERFORMANCE)
                .level(LogEvent.Level.WARNING)
                .action("SLOW_OPERATION")
                .duration(executionTime)
                .metadata("method", method)
                .metadata("threshold", alertThresholdMillis)
                .metadata("argCount", args != null ? args.length : 0)
                .observation("Operation exceeded performance threshold")
                .log();
    }

    /** Generate and log performance report */
    @Scheduled(fixedDelayString = "${brobot.aspects.performance.report-interval:300}000")
    public void generatePerformanceReport() {
        if (performanceStats.isEmpty()) {
            return;
        }

        log.info("=== Performance Report ===");
        log.info("Total method calls: {}", totalMethodCalls.sum());
        log.info("Total execution time: {}ms", totalExecutionTime.get());

        // Find top slow methods
        List<MethodPerformanceStats> slowestMethods =
                performanceStats.values().stream()
                        .sorted((a, b) -> Long.compare(b.getAverageTime(), a.getAverageTime()))
                        .limit(10)
                        .collect(Collectors.toList());

        // Log detailed stats for slowest methods
        slowestMethods.forEach(
                stats -> {
                    brobotLogger
                            .log()
                            .type(LogEvent.Type.PERFORMANCE)
                            .level(LogEvent.Level.INFO)
                            .action("PERFORMANCE_REPORT")
                            .metadata("method", stats.getMethodName())
                            .metadata("calls", stats.getTotalCalls())
                            .metadata("avgTime", stats.getAverageTime())
                            .metadata("minTime", stats.getMinTime())
                            .metadata("maxTime", stats.getMaxTime())
                            .metadata("p95Time", stats.getPercentile(95))
                            .metadata(
                                    "successRate", String.format("%.2f%%", stats.getSuccessRate()))
                            .observation("Performance statistics")
                            .log();
                });

        // Detect performance trends
        detectPerformanceTrends();
    }

    /** Detect performance degradation trends */
    private void detectPerformanceTrends() {
        recentExecutions.forEach(
                (method, executions) -> {
                    if (executions.size() < 20) {
                        return; // Not enough data for trend analysis
                    }

                    // Calculate moving averages
                    List<Long> recent =
                            executions.subList(
                                    Math.max(0, executions.size() - 10), executions.size());
                    List<Long> previous =
                            executions.subList(
                                    Math.max(0, executions.size() - 20), executions.size() - 10);

                    double recentAvg =
                            recent.stream().mapToLong(Long::longValue).average().orElse(0);
                    double previousAvg =
                            previous.stream().mapToLong(Long::longValue).average().orElse(0);

                    // Check for significant degradation (>20% slower)
                    if (recentAvg > previousAvg * 1.2) {
                        brobotLogger
                                .log()
                                .type(LogEvent.Type.PERFORMANCE)
                                .level(LogEvent.Level.WARNING)
                                .action("PERFORMANCE_DEGRADATION")
                                .metadata("method", method)
                                .metadata("recentAvg", (long) recentAvg)
                                .metadata("previousAvg", (long) previousAvg)
                                .metadata(
                                        "degradation",
                                        String.format(
                                                "%.1f%%",
                                                (recentAvg - previousAvg) / previousAvg * 100))
                                .observation("Performance degradation detected")
                                .log();
                    }
                });
    }

    /** Get current memory usage */
    private long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    /** Get performance statistics for reporting */
    public Map<String, MethodPerformanceStats> getPerformanceStats() {
        return new HashMap<>(performanceStats);
    }

    /** Reset all performance statistics */
    public void resetStatistics() {
        performanceStats.clear();
        recentExecutions.clear();
        totalMethodCalls.reset();
        totalExecutionTime.set(0);
        log.info("Performance statistics reset");
    }

    /** Inner class for tracking method performance statistics */
    public static class MethodPerformanceStats {
        private final String methodName;
        private final LongAdder totalCalls = new LongAdder();
        private final LongAdder successfulCalls = new LongAdder();
        private final AtomicLong totalTime = new AtomicLong();
        private final AtomicLong totalMemory = new AtomicLong();
        private volatile long minTime = Long.MAX_VALUE;
        private volatile long maxTime = 0;
        private final List<Long> executionTimes = Collections.synchronizedList(new ArrayList<>());

        public MethodPerformanceStats(String methodName) {
            this.methodName = methodName;
        }

        public synchronized void recordExecution(
                long executionTime, boolean success, long memoryDelta) {
            totalCalls.increment();
            if (success) {
                successfulCalls.increment();
            }
            totalTime.addAndGet(executionTime);
            totalMemory.addAndGet(memoryDelta);
            minTime = Math.min(minTime, executionTime);
            maxTime = Math.max(maxTime, executionTime);

            // Keep last 1000 execution times for percentile calculation
            executionTimes.add(executionTime);
            if (executionTimes.size() > 1000) {
                executionTimes.remove(0);
            }
        }

        public long getAverageTime() {
            long calls = totalCalls.sum();
            return calls > 0 ? totalTime.get() / calls : 0;
        }

        public long getAverageMemory() {
            long calls = totalCalls.sum();
            return calls > 0 ? totalMemory.get() / calls : 0;
        }

        public double getSuccessRate() {
            long total = totalCalls.sum();
            return total > 0 ? (double) successfulCalls.sum() / total * 100 : 0;
        }

        public long getPercentile(int percentile) {
            if (executionTimes.isEmpty()) {
                return 0;
            }

            List<Long> sorted = new ArrayList<>(executionTimes);
            Collections.sort(sorted);
            int index = (int) Math.ceil(percentile / 100.0 * sorted.size()) - 1;
            return sorted.get(Math.max(0, Math.min(index, sorted.size() - 1)));
        }

        // Getters
        public String getMethodName() {
            return methodName;
        }

        public long getTotalCalls() {
            return totalCalls.sum();
        }

        public long getSuccessfulCalls() {
            return successfulCalls.sum();
        }

        public long getTotalTime() {
            return totalTime.get();
        }

        public long getMinTime() {
            return minTime == Long.MAX_VALUE ? 0 : minTime;
        }

        public long getMaxTime() {
            return maxTime;
        }
    }
}
