package io.github.jspinak.brobot.runner.diagnostics;

import io.github.jspinak.brobot.runner.errorhandling.ErrorStatistics;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

/**
 * Comprehensive diagnostic report containing system information and metrics.
 */
@Builder
public record DiagnosticReport(
    LocalDateTime timestamp,
    SystemInfo systemInfo,
    MemoryInfo memoryInfo,
    ThreadInfo threadInfo,
    PerformanceMetrics performanceMetrics,
    ErrorStatistics errorStatistics,
    Map<String, String> environmentVariables,
    Map<String, String> systemProperties
) {}

@Builder
record SystemInfo(
    String osName,
    String osVersion,
    String osArch,
    String javaVersion,
    String javaVendor,
    String jvmName,
    String jvmVersion,
    int availableProcessors,
    double systemLoadAverage,
    long uptime,
    Date startTime
) {}

@Builder
record MemoryInfo(
    long heapUsed,
    long heapMax,
    long heapCommitted,
    long nonHeapUsed,
    long nonHeapMax,
    long nonHeapCommitted,
    long freeMemory,
    long totalMemory,
    long maxMemory,
    Map<String, MemoryPoolInfo> memoryPools
) {}

@Builder
record MemoryPoolInfo(
    String name,
    String type,
    long used,
    long max,
    long committed
) {}

@Builder
record ThreadInfo(
    int threadCount,
    int peakThreadCount,
    int daemonThreadCount,
    long totalStartedThreadCount,
    int deadlockedThreads,
    Map<Thread.State, Integer> threadStates
) {}

@Builder
record PerformanceMetrics(
    String performanceReport,
    long gcTotalCount,
    long gcTotalTime,
    Map<String, GCInfo> gcDetails,
    double cpuUsage
) {}

@Builder
record GCInfo(
    String name,
    long collectionCount,
    long collectionTime
) {}

record HealthCheckResult(
    HealthStatus overallStatus,
    java.util.List<HealthCheckItem> checks
) {}

record HealthCheckItem(
    String component,
    HealthStatus status,
    String message
) {}

enum HealthStatus {
    HEALTHY,
    WARNING,
    CRITICAL
}