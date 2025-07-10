package io.github.jspinak.brobot.runner.diagnostics;

/**
 * Individual health check item data.
 */
public record HealthCheckItem(
    String component,
    HealthStatus status,
    String message
) {}