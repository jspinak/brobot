package io.github.jspinak.brobot.logging;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Configuration for the Brobot logging system.
 *
 * <p>Provides configuration for output formats, performance optimizations, and data enrichment.
 * Logging levels should be controlled via standard Spring Boot properties:
 *
 * <pre>
 * # Standard Spring Boot logging configuration
 * logging.level.root=INFO
 * logging.level.io.github.jspinak.brobot=DEBUG
 *
 * # Brobot-specific configuration
 * brobot.logging.output.format=STRUCTURED
 * brobot.logging.performance.async=true
 * </pre>
 */
@ConfigurationProperties(prefix = "brobot.logging")
@Data
public class LoggingConfiguration {

    /** Preset configurations for common scenarios */
    public enum LoggingPreset {
        /** Production settings: minimal logging, JSON format, async */
        PRODUCTION,
        /** Development settings: detailed logging, simple format, sync */
        DEVELOPMENT,
        /** Testing settings: balanced logging with action details */
        TESTING,
        /** Silent mode: no logging */
        SILENT
    }

    /** Output format options */
    public enum OutputFormat {
        /** Simple format: [CATEGORY] LEVEL - MESSAGE */
        SIMPLE,
        /** Structured format with timestamp and correlation ID */
        STRUCTURED,
        /** JSON format for machine processing */
        JSON
    }

    /** Configuration for output formatting */
    @Data
    public static class OutputConfiguration {
        private OutputFormat format = OutputFormat.STRUCTURED;
        private boolean includeTimestamp = true;
        private boolean includeThread = false;
        private boolean includeCorrelationId = true;
        private boolean includeStateContext = true;
    }

    /** Configuration for performance optimizations */
    @Data
    public static class PerformanceConfiguration {
        private boolean async = true;
        private int bufferSize = 8192;
    }

    /** Configuration for data enrichment */
    @Data
    public static class EnrichmentConfiguration {
        private boolean includeScreenshots = false;
        private boolean includeSimilarityScores = true;
        private boolean includeTimingBreakdown = true;
        private boolean includeMemoryUsage = false;
    }

    // Note: Logging levels are now controlled via standard Spring Boot properties
    // Use logging.level.root and logging.level.* in application.properties

    /** Output configuration */
    private OutputConfiguration output = new OutputConfiguration();

    /** Performance optimizations */
    private PerformanceConfiguration performance = new PerformanceConfiguration();

    /** Data enrichment */
    private EnrichmentConfiguration enrichment = new EnrichmentConfiguration();

    /**
     * Check if logging is enabled - delegates to SLF4J/Logback. This method is kept for backward
     * compatibility but always returns true, letting the underlying logging framework handle level
     * filtering.
     *
     * @param category The log category (unused)
     * @param level The log level to check (unused)
     * @return always true - actual filtering is done by SLF4J/Logback
     */
    public boolean isLoggingEnabled(LogCategory category, LogLevel level) {
        // Delegate all level checking to SLF4J/Logback
        // Configured via logging.level.* properties
        return true;
    }

    /**
     * Apply a preset configuration for output format and performance. Note: Logging levels should
     * be set via logging.level.* properties.
     *
     * @param preset The preset to apply
     */
    public void applyPreset(LoggingPreset preset) {
        switch (preset) {
            case PRODUCTION:
                // Use logging.level.root=WARN in application.properties
                performance.setAsync(true);
                output.setFormat(OutputFormat.JSON);
                enrichment.setIncludeScreenshots(false);
                break;
            case DEVELOPMENT:
                // Use logging.level.root=DEBUG in application.properties
                performance.setAsync(false);
                output.setFormat(OutputFormat.SIMPLE);
                enrichment.setIncludeScreenshots(true);
                break;
            case TESTING:
                // Use logging.level.* properties for fine-grained control
                performance.setAsync(false);
                break;
            case SILENT:
                // Use logging.level.root=OFF in application.properties
                break;
        }
    }

    // Category-specific logging levels are now controlled via Spring Boot properties
    // Example: logging.level.io.github.jspinak.brobot.actions=DEBUG
}
