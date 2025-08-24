package io.github.jspinak.brobot.runner.diagnostics;

import java.util.List;

/**
 * Health check result data.
 */
public record HealthCheckResult(
    HealthStatus overallStatus,
    List<HealthCheckItem> checks
) {}