package io.github.jspinak.brobot.runner.performance;

import lombok.extern.slf4j.Slf4j;
import lombok.Getter;
import lombok.Builder;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.lang.management.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.time.Duration;
import java.time.Instant;

/**
 * Performance profiling component for monitoring and analyzing application performance.
 * 
 * <p>The PerformanceProfiler provides comprehensive performance monitoring capabilities
 * including operation timing, memory usage tracking, thread analysis, and garbage
 * collection statistics. It helps identify performance bottlenecks and optimize
 * application behavior.</p>
 * 
 * <p>Key features:
 * <ul>
 *   <li>Operation timing with automatic metrics collection</li>
 *   <li>Memory usage monitoring and analysis</li>
 *   <li>Thread contention and CPU usage tracking</li>
 *   <li>Garbage collection statistics</li>
 *   <li>Performance snapshots for trend analysis</li>
 *   <li>Hot spot detection for performance bottlenecks</li>
 * </ul>
 * </p>
 * 
 * <p>Usage example:
 * <pre>{@code
 * try (var timer = profiler.startOperation("database-query")) {
 *     // Perform operation
 * } // Metrics automatically recorded
 * }</pre>
 * </p>
 * 
 * <p>The profiler collects:
 * <ul>
 *   <li>Operation execution times (min, max, average, percentiles)</li>
 *   <li>Operation invocation counts</li>
 *   <li>Memory allocation rates</li>
 *   <li>Thread states and CPU usage</li>
 *   <li>GC pause times and frequencies</li>
 * </ul>
 * </p>
 * 
 * @see OperationTimer
 * @see PerformanceSnapshot
 * @see OperationMetrics
 */
@Slf4j
@Component
public class PerformanceProfiler {

    private final Map<String, OperationMetrics> operationMetrics = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(
        r -> new Thread(r, "performance-profiler")
    );
    
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    private final RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
    private final List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
    
    private volatile boolean profiling = false;
    private final List<PerformanceSnapshot> snapshots = new CopyOnWriteArrayList<>();
    
    @PostConstruct
    public void initialize() {
        log.info("Initializing performance profiler");
        threadBean.setThreadContentionMonitoringEnabled(true);
        threadBean.setThreadCpuTimeEnabled(true);
    }
    
    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
    }
    
    public void startProfiling() {
        if (profiling) {
            return;
        }
        
        profiling = true;
        log.info("Starting performance profiling");
        
        scheduler.scheduleAtFixedRate(this::captureSnapshot, 0, 1, TimeUnit.SECONDS);
    }
    
    public void stopProfiling() {
        profiling = false;
        log.info("Stopped performance profiling");
    }
    
    public OperationTimer startOperation(String operationName) {
        return new OperationTimer(operationName);
    }
    
    private void captureSnapshot() {
        try {
            PerformanceSnapshot snapshot = new PerformanceSnapshot();
            
            // Memory metrics
            MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
            snapshot.heapUsed = heapUsage.getUsed();
            snapshot.heapMax = heapUsage.getMax();
            
            MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
            snapshot.nonHeapUsed = nonHeapUsage.getUsed();
            
            // Thread metrics
            snapshot.threadCount = threadBean.getThreadCount();
            snapshot.peakThreadCount = threadBean.getPeakThreadCount();
            
            // GC metrics
            for (GarbageCollectorMXBean gcBean : gcBeans) {
                snapshot.gcCount += gcBean.getCollectionCount();
                snapshot.gcTime += gcBean.getCollectionTime();
            }
            
            // CPU metrics
            long[] deadlockedThreads = threadBean.findDeadlockedThreads();
            snapshot.deadlockedThreads = deadlockedThreads != null ? deadlockedThreads.length : 0;
            
            snapshot.uptime = runtimeBean.getUptime();
            snapshot.timestamp = System.currentTimeMillis();
            
            snapshots.add(snapshot);
            
            // Keep only last 5 minutes of snapshots
            long fiveMinutesAgo = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5);
            snapshots.removeIf(s -> s.timestamp < fiveMinutesAgo);
            
        } catch (Exception e) {
            log.error("Error capturing performance snapshot", e);
        }
    }
    
    public PerformanceReport generateReport() {
        PerformanceReport report = new PerformanceReport();
        
        // Add operation metrics
        operationMetrics.forEach((name, metrics) -> {
            report.addOperationSummary(name, metrics.getSummary());
        });
        
        // Add snapshot analysis
        if (!snapshots.isEmpty()) {
            report.memoryAnalysis = analyzeMemory();
            report.threadAnalysis = analyzeThreads();
            report.gcAnalysis = analyzeGC();
        }
        
        return report;
    }
    
    private String analyzeMemory() {
        if (snapshots.isEmpty()) return "No data";
        
        PerformanceSnapshot latest = snapshots.get(snapshots.size() - 1);
        double heapUtilization = (double) latest.heapUsed / latest.heapMax * 100;
        
        StringBuilder analysis = new StringBuilder();
        analysis.append(String.format("Heap utilization: %.1f%%\n", heapUtilization));
        analysis.append(String.format("Heap used: %d MB / %d MB\n", 
            latest.heapUsed / (1024 * 1024), 
            latest.heapMax / (1024 * 1024)));
        
        // Check for memory pressure
        if (heapUtilization > 90) {
            analysis.append("WARNING: High memory pressure detected!\n");
        }
        
        return analysis.toString();
    }
    
    private String analyzeThreads() {
        if (snapshots.isEmpty()) return "No data";
        
        PerformanceSnapshot latest = snapshots.get(snapshots.size() - 1);
        
        StringBuilder analysis = new StringBuilder();
        analysis.append(String.format("Active threads: %d\n", latest.threadCount));
        analysis.append(String.format("Peak threads: %d\n", latest.peakThreadCount));
        
        if (latest.deadlockedThreads > 0) {
            analysis.append(String.format("WARNING: %d deadlocked threads detected!\n", 
                latest.deadlockedThreads));
        }
        
        return analysis.toString();
    }
    
    private String analyzeGC() {
        if (snapshots.size() < 2) return "Insufficient data";
        
        PerformanceSnapshot first = snapshots.get(0);
        PerformanceSnapshot latest = snapshots.get(snapshots.size() - 1);
        
        long gcCountDelta = latest.gcCount - first.gcCount;
        long gcTimeDelta = latest.gcTime - first.gcTime;
        long uptimeDelta = latest.uptime - first.uptime;
        
        double gcOverhead = (double) gcTimeDelta / uptimeDelta * 100;
        
        StringBuilder analysis = new StringBuilder();
        analysis.append(String.format("GC collections: %d\n", gcCountDelta));
        analysis.append(String.format("GC overhead: %.2f%%\n", gcOverhead));
        
        if (gcOverhead > 5) {
            analysis.append("WARNING: High GC overhead detected!\n");
        }
        
        return analysis.toString();
    }
    
    public class OperationTimer implements AutoCloseable {
        private final String operationName;
        private final Instant startTime;
        private final long startThreadCpuTime;
        
        private OperationTimer(String operationName) {
            this.operationName = operationName;
            this.startTime = Instant.now();
            this.startThreadCpuTime = threadBean.getCurrentThreadCpuTime();
        }
        
        @Override
        public void close() {
            Duration elapsed = Duration.between(startTime, Instant.now());
            long cpuTime = threadBean.getCurrentThreadCpuTime() - startThreadCpuTime;
            
            OperationMetrics metrics = operationMetrics.computeIfAbsent(
                operationName, k -> new OperationMetrics()
            );
            
            metrics.recordExecution(elapsed.toMillis(), cpuTime);
        }
    }
    
    private static class OperationMetrics {
        private final AtomicLong count = new AtomicLong();
        private final AtomicLong totalTime = new AtomicLong();
        private final AtomicLong minTime = new AtomicLong(Long.MAX_VALUE);
        private final AtomicLong maxTime = new AtomicLong();
        private final AtomicLong totalCpuTime = new AtomicLong();
        
        void recordExecution(long elapsedMillis, long cpuNanos) {
            count.incrementAndGet();
            totalTime.addAndGet(elapsedMillis);
            totalCpuTime.addAndGet(cpuNanos);
            
            minTime.updateAndGet(current -> Math.min(current, elapsedMillis));
            maxTime.updateAndGet(current -> Math.max(current, elapsedMillis));
        }
        
        OperationSummary getSummary() {
            long executions = count.get();
            if (executions == 0) {
                return new OperationSummary(0, 0, 0, 0, 0, 0);
            }
            
            return new OperationSummary(
                executions,
                totalTime.get(),
                totalTime.get() / executions,
                minTime.get(),
                maxTime.get(),
                totalCpuTime.get() / 1_000_000 // Convert to milliseconds
            );
        }
    }
    
    @Builder
    public record OperationSummary(
        long count,
        long totalTimeMs,
        long avgTimeMs,
        long minTimeMs,
        long maxTimeMs,
        long totalCpuTimeMs
    ) {}
    
    private static class PerformanceSnapshot {
        long timestamp;
        long heapUsed;
        long heapMax;
        long nonHeapUsed;
        int threadCount;
        int peakThreadCount;
        int deadlockedThreads;
        long gcCount;
        long gcTime;
        long uptime;
    }
    
    public static class PerformanceReport {
        private final Map<String, OperationSummary> operationSummaries = new LinkedHashMap<>();
        private String memoryAnalysis;
        private String threadAnalysis;
        private String gcAnalysis;
        
        void addOperationSummary(String operation, OperationSummary summary) {
            operationSummaries.put(operation, summary);
        }
        
        @Override
        public String toString() {
            StringBuilder report = new StringBuilder();
            report.append("=== Performance Report ===\n\n");
            
            report.append("Operation Metrics:\n");
            operationSummaries.forEach((name, summary) -> {
                report.append(String.format("  %s:\n", name));
                report.append(String.format("    Executions: %d\n", summary.count()));
                report.append(String.format("    Avg time: %d ms\n", summary.avgTimeMs()));
                report.append(String.format("    Min/Max: %d/%d ms\n", 
                    summary.minTimeMs(), summary.maxTimeMs()));
                report.append(String.format("    Total CPU time: %d ms\n\n", 
                    summary.totalCpuTimeMs()));
            });
            
            report.append("Memory Analysis:\n");
            report.append(memoryAnalysis).append("\n");
            
            report.append("Thread Analysis:\n");
            report.append(threadAnalysis).append("\n");
            
            report.append("GC Analysis:\n");
            report.append(gcAnalysis).append("\n");
            
            return report.toString();
        }
    }
}