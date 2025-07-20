package io.github.jspinak.brobot.runner.diagnostics;

import lombok.Builder;

import java.util.Map;

/**
 * Performance metrics data.
 */
@Builder
public record PerformanceMetrics(
    String performanceReport,
    long gcTotalCount,
    long gcTotalTime,
    Map<String, GCInfo> gcDetails,
    double cpuUsage
) {}