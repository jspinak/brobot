# Performance Benchmarking Guide for Brobot Runner Refactoring

## Overview

This guide provides comprehensive instructions for benchmarking performance before, during, and after refactoring to ensure no performance regressions and validate improvements.

## Benchmarking Framework

### 1. Core Benchmark Infrastructure

```java
@Component
@Slf4j
public class BenchmarkRunner {
    private final MetricsRegistry metricsRegistry;
    private final BenchmarkReportGenerator reportGenerator;
    
    public BenchmarkResults run(BenchmarkSuite suite, BenchmarkConfig config) {
        log.info("Starting benchmark suite: {}", suite.getName());
        
        // Warm-up phase
        performWarmup(suite, config.getWarmupIterations());
        
        // Measurement phase
        List<BenchmarkResult> results = new ArrayList<>();
        for (int i = 0; i < config.getMeasurementIterations(); i++) {
            results.add(measureIteration(suite, i));
        }
        
        // Analysis phase
        BenchmarkAnalysis analysis = analyzeResults(results);
        
        // Report generation
        BenchmarkReport report = reportGenerator.generate(suite, results, analysis);
        
        return BenchmarkResults.builder()
            .suite(suite)
            .results(results)
            .analysis(analysis)
            .report(report)
            .build();
    }
}
```

### 2. Benchmark Suite Definition

```java
@BenchmarkSuite(name = "Session Management Performance")
public class SessionBenchmarkSuite {
    
    @Benchmark
    @BenchmarkConfig(
        warmupIterations = 5,
        measurementIterations = 20,
        timeout = 30
    )
    public void benchmarkSessionCreation(BenchmarkContext context) {
        SessionService service = context.getService(SessionService.class);
        
        context.startTimer();
        Session session = service.startSession("benchmark-project", Map.of());
        context.stopTimer();
        
        // Cleanup
        service.endSession();
    }
    
    @Benchmark
    @BenchmarkConfig(fork = 3) // Run in 3 separate JVMs
    public void benchmarkConcurrentSessions(BenchmarkContext context) {
        SessionService service = context.getService(SessionService.class);
        int concurrentSessions = 100;
        
        CountDownLatch latch = new CountDownLatch(concurrentSessions);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        
        context.startTimer();
        
        for (int i = 0; i < concurrentSessions; i++) {
            final int sessionId = i;
            executor.submit(() -> {
                try {
                    service.startSession("project-" + sessionId, Map.of());
                    latch.countDown();
                } catch (Exception e) {
                    context.recordError(e);
                }
            });
        }
        
        latch.await();
        context.stopTimer();
        
        executor.shutdown();
    }
    
    @Benchmark
    @MemoryProfiling
    public void benchmarkMemoryUsage(BenchmarkContext context) {
        SessionService service = context.getService(SessionService.class);
        List<Session> sessions = new ArrayList<>();
        
        context.recordMemoryBefore();
        
        // Create 1000 sessions
        for (int i = 0; i < 1000; i++) {
            sessions.add(service.startSession("memory-test-" + i, 
                generateLargeContext()));
        }
        
        context.recordMemoryAfter();
        
        // Force GC and measure again
        System.gc();
        Thread.sleep(100);
        context.recordMemoryAfterGC();
    }
}
```

### 3. Performance Metrics Collection

```java
@Component
public class PerformanceMetricsCollector {
    
    public PerformanceMetrics collect(String operation) {
        return PerformanceMetrics.builder()
            .operation(operation)
            .timestamp(Instant.now())
            .threadMetrics(collectThreadMetrics())
            .memoryMetrics(collectMemoryMetrics())
            .cpuMetrics(collectCPUMetrics())
            .ioMetrics(collectIOMetrics())
            .customMetrics(collectCustomMetrics())
            .build();
    }
    
    private ThreadMetrics collectThreadMetrics() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        
        return ThreadMetrics.builder()
            .threadCount(threadBean.getThreadCount())
            .peakThreadCount(threadBean.getPeakThreadCount())
            .daemonThreadCount(threadBean.getDaemonThreadCount())
            .totalStartedThreadCount(threadBean.getTotalStartedThreadCount())
            .deadlockedThreads(findDeadlockedThreads())
            .build();
    }
    
    private MemoryMetrics collectMemoryMetrics() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        Runtime runtime = Runtime.getRuntime();
        
        return MemoryMetrics.builder()
            .heapUsed(memoryBean.getHeapMemoryUsage().getUsed())
            .heapMax(memoryBean.getHeapMemoryUsage().getMax())
            .nonHeapUsed(memoryBean.getNonHeapMemoryUsage().getUsed())
            .totalMemory(runtime.totalMemory())
            .freeMemory(runtime.freeMemory())
            .objectPendingFinalization(memoryBean.getObjectPendingFinalizationCount())
            .build();
    }
    
    private CPUMetrics collectCPUMetrics() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean sunOsBean = 
                (com.sun.management.OperatingSystemMXBean) osBean;
            
            return CPUMetrics.builder()
                .processCpuLoad(sunOsBean.getProcessCpuLoad())
                .systemCpuLoad(sunOsBean.getSystemCpuLoad())
                .processCpuTime(sunOsBean.getProcessCpuTime())
                .availableProcessors(osBean.getAvailableProcessors())
                .loadAverage(osBean.getSystemLoadAverage())
                .build();
        }
        
        return CPUMetrics.basic(osBean);
    }
}
```

### 4. Benchmark Scenarios

#### 4.1 Startup Performance
```java
@Benchmark
public class StartupBenchmark {
    
    @BenchmarkScenario("Cold Start")
    public StartupMetrics benchmarkColdStart() {
        // Kill any running instances
        ProcessUtils.killProcess("brobot-runner");
        
        // Clear caches
        CacheUtils.clearSystemCaches();
        
        // Measure cold start
        long startTime = System.nanoTime();
        
        Process process = new ProcessBuilder("java", "-jar", "brobot-runner.jar")
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start();
        
        // Wait for application ready signal
        waitForApplicationReady(process);
        
        long startupTime = System.nanoTime() - startTime;
        
        return StartupMetrics.builder()
            .startupTimeNanos(startupTime)
            .startupTimeMillis(TimeUnit.NANOSECONDS.toMillis(startupTime))
            .processId(process.pid())
            .build();
    }
    
    @BenchmarkScenario("Warm Start")
    public StartupMetrics benchmarkWarmStart() {
        // First start to warm up
        benchmarkColdStart();
        
        // Shutdown gracefully
        shutdownApplication();
        
        // Measure warm start
        return measureStartup();
    }
}
```

#### 4.2 Throughput Benchmark
```java
@Benchmark
public class ThroughputBenchmark {
    
    @BenchmarkScenario("Task Execution Throughput")
    public ThroughputMetrics benchmarkTaskThroughput(Duration duration) {
        ExecutionService executionService = getService(ExecutionService.class);
        
        AtomicLong completedTasks = new AtomicLong();
        AtomicLong failedTasks = new AtomicLong();
        
        long endTime = System.currentTimeMillis() + duration.toMillis();
        
        while (System.currentTimeMillis() < endTime) {
            try {
                CompletableFuture<ExecutionResult> future = 
                    executionService.execute("throughput-test", 
                        this::generateTestTask, ExecutionOptions.DEFAULT);
                
                future.whenComplete((result, error) -> {
                    if (error == null) {
                        completedTasks.incrementAndGet();
                    } else {
                        failedTasks.incrementAndGet();
                    }
                });
                
            } catch (Exception e) {
                failedTasks.incrementAndGet();
            }
        }
        
        // Wait for all tasks to complete
        waitForCompletion();
        
        return ThroughputMetrics.builder()
            .duration(duration)
            .completedTasks(completedTasks.get())
            .failedTasks(failedTasks.get())
            .tasksPerSecond(completedTasks.get() / duration.getSeconds())
            .build();
    }
}
```

#### 4.3 Latency Benchmark
```java
@Benchmark
public class LatencyBenchmark {
    
    @BenchmarkScenario("Request Latency Distribution")
    public LatencyMetrics benchmarkLatency(int requests) {
        List<Long> latencies = new ArrayList<>(requests);
        
        for (int i = 0; i < requests; i++) {
            long startTime = System.nanoTime();
            
            // Execute operation
            executeOperation();
            
            long latency = System.nanoTime() - startTime;
            latencies.add(latency);
            
            // Small delay to avoid overwhelming the system
            Thread.sleep(10);
        }
        
        // Calculate percentiles
        Collections.sort(latencies);
        
        return LatencyMetrics.builder()
            .min(latencies.get(0))
            .max(latencies.get(latencies.size() - 1))
            .mean(calculateMean(latencies))
            .median(calculatePercentile(latencies, 50))
            .p95(calculatePercentile(latencies, 95))
            .p99(calculatePercentile(latencies, 99))
            .p999(calculatePercentile(latencies, 99.9))
            .standardDeviation(calculateStdDev(latencies))
            .build();
    }
}
```

### 5. Comparative Analysis

```java
@Component
public class PerformanceComparator {
    
    public ComparisonReport compare(
            BenchmarkResults before, 
            BenchmarkResults after) {
        
        ComparisonReport report = new ComparisonReport();
        
        // Compare each benchmark
        for (String benchmarkName : before.getBenchmarkNames()) {
            BenchmarkResult beforeResult = before.getResult(benchmarkName);
            BenchmarkResult afterResult = after.getResult(benchmarkName);
            
            if (beforeResult != null && afterResult != null) {
                ComparisonResult comparison = compareBenchmark(
                    beforeResult, 
                    afterResult
                );
                report.addComparison(benchmarkName, comparison);
            }
        }
        
        // Generate summary
        report.setSummary(generateSummary(report));
        
        // Flag regressions
        report.setRegressions(findRegressions(report));
        
        // Flag improvements
        report.setImprovements(findImprovements(report));
        
        return report;
    }
    
    private ComparisonResult compareBenchmark(
            BenchmarkResult before, 
            BenchmarkResult after) {
        
        double improvement = calculateImprovement(
            before.getMeanTime(), 
            after.getMeanTime()
        );
        
        // Statistical significance test
        boolean significant = isStatisticallySignificant(
            before.getMeasurements(), 
            after.getMeasurements()
        );
        
        return ComparisonResult.builder()
            .beforeMean(before.getMeanTime())
            .afterMean(after.getMeanTime())
            .improvement(improvement)
            .significant(significant)
            .confidenceInterval(calculateConfidenceInterval(before, after))
            .build();
    }
}
```

### 6. Continuous Performance Monitoring

```java
@Configuration
public class PerformanceMonitoringConfig {
    
    @Bean
    public PerformanceMonitor performanceMonitor() {
        return PerformanceMonitor.builder()
            .metricsRegistry(new MicrometerRegistry())
            .alertThresholds(defineThresholds())
            .monitoringInterval(Duration.ofMinutes(5))
            .build();
    }
    
    @Bean
    @Scheduled(fixedDelay = 300000) // Every 5 minutes
    public void monitorPerformance() {
        PerformanceSnapshot snapshot = performanceMonitor.capture();
        
        // Check for anomalies
        List<PerformanceAnomaly> anomalies = 
            anomalyDetector.detect(snapshot);
        
        if (!anomalies.isEmpty()) {
            alertingService.sendAlert(new PerformanceAlert(anomalies));
        }
        
        // Store for historical analysis
        performanceRepository.save(snapshot);
    }
}
```

## Benchmark Execution Guide

### 1. Pre-Refactoring Baseline
```bash
# Run full benchmark suite
./gradlew benchmark -Psuite=full -Piterations=100

# Run specific benchmarks
./gradlew benchmark -Pbenchmark=SessionBenchmark

# Generate baseline report
./gradlew benchmarkReport -Pbaseline=true
```

### 2. During Refactoring
```bash
# Quick validation benchmarks
./gradlew benchmark -Psuite=quick -Pcompare=baseline

# Continuous monitoring
./gradlew benchmark:monitor
```

### 3. Post-Refactoring Validation
```bash
# Full comparison suite
./gradlew benchmark -Psuite=full -Pcompare=baseline -Preport=html

# Statistical analysis
./gradlew benchmark:analyze -Pconfidence=0.95
```

## Performance Regression Prevention

### 1. CI/CD Integration
```yaml
# .github/workflows/performance.yml
name: Performance Benchmarks

on:
  pull_request:
    types: [opened, synchronize]

jobs:
  benchmark:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup JDK
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      
      - name: Run Benchmarks
        run: ./gradlew benchmark -Psuite=ci
      
      - name: Compare with Baseline
        run: ./gradlew benchmark:compare
      
      - name: Comment PR
        uses: actions/github-script@v6
        with:
          script: |
            const report = require('./build/benchmark-report.json');
            github.issues.createComment({
              issue_number: context.issue.number,
              owner: context.repo.owner,
              repo: context.repo.repo,
              body: generatePRComment(report)
            });
```

### 2. Performance Gates
```java
@Component
public class PerformanceGatekeeper {
    
    public boolean checkPerformanceGates(ComparisonReport report) {
        // Define acceptance criteria
        PerformanceCriteria criteria = PerformanceCriteria.builder()
            .maxRegressionPercent(5.0) // Allow 5% regression
            .requiredConfidence(0.95)   // 95% confidence
            .minimumSamples(20)         // At least 20 samples
            .build();
        
        // Check each benchmark
        for (ComparisonResult result : report.getComparisons()) {
            if (!meetsGate(result, criteria)) {
                log.error("Performance gate failed for: {}", 
                    result.getBenchmarkName());
                return false;
            }
        }
        
        return true;
    }
}
```

## Optimization Opportunities

### 1. Profiling Integration
```java
@Benchmark
@Profile("cpu")
public void profiledBenchmark(BenchmarkContext context) {
    // CPU profiling enabled
    try (Profiler profiler = context.startProfiler()) {
        // Benchmark code
        executeOperation();
    }
    
    // Profiler data automatically attached to results
}
```

### 2. Memory Analysis
```java
@Benchmark
@HeapDump(condition = "AFTER_GC")
public void memoryIntensiveBenchmark(BenchmarkContext context) {
    // Heap dump will be captured after GC
    performMemoryIntensiveOperation();
}
```

## Reporting

### 1. HTML Report Generation
```java
public class HtmlBenchmarkReporter {
    public void generateReport(BenchmarkResults results, Path outputPath) {
        // Generate interactive HTML report with charts
        // Include flame graphs, histograms, timelines
    }
}
```

### 2. Slack/Email Notifications
```java
@Component
public class BenchmarkNotifier {
    public void notifyResults(ComparisonReport report) {
        if (report.hasRegressions()) {
            slackClient.sendMessage(formatRegressionAlert(report));
        }
    }
}
```

## Best Practices

1. **Isolate Benchmarks**: Run in dedicated environment
2. **Multiple Runs**: Use statistical analysis
3. **Warm-up**: Always include warm-up phase
4. **Real Data**: Use production-like data sets
5. **Monitor Resources**: Track CPU, memory, I/O
6. **Version Control**: Tag benchmark baselines
7. **Document Changes**: Note configuration changes

This comprehensive benchmarking approach ensures refactoring maintains or improves performance while providing clear metrics for decision making.