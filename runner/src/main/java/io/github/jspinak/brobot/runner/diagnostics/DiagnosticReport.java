package io.github.jspinak.brobot.runner.diagnostics;

import io.github.jspinak.brobot.runner.errorhandling.ErrorStatistics;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Comprehensive diagnostic report containing system information and metrics.
 */
@Builder
public record DiagnosticReport(
    LocalDateTime timestamp,
    SystemInfo systemInfo,
    MemoryInfo memoryInfo,
    ThreadDiagnosticInfo threadInfo,
    PerformanceMetrics performanceMetrics,
    ErrorStatistics errorStatistics,
    HealthCheckResult healthCheckResult,
    Map<String, String> environmentVariables,
    Map<String, String> systemProperties
) {}