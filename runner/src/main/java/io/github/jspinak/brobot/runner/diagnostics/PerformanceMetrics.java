package io.github.jspinak.brobot.runner.diagnostics;

import java.util.Map;

import lombok.Builder;

/** Performance metrics data. */
@Builder
public record PerformanceMetrics(
        String performanceReport,
        long gcTotalCount,
        long gcTotalTime,
        Map<String, GCInfo> gcDetails,
        double cpuUsage) {}
