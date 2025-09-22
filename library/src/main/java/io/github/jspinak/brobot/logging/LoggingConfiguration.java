package io.github.jspinak.brobot.logging;

import java.util.EnumMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * Configuration for the Brobot logging system.
 *
 * <p>Provides hierarchical configuration where category-specific levels override global settings.
 * Supports multiple output formats and performance optimizations.
 *
 * <p>Configuration can be set via application.properties:
 *
 * <pre>
 * brobot.logging.global-level=INFO
 * brobot.logging.categories.actions=DEBUG
 * brobot.logging.output.format=STRUCTURED
 * brobot.logging.performance.async=true
 * </pre>
 */
@Component
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

    /** Global level - overrides all categories if set */
    private LogLevel globalLevel = LogLevel.INFO;

    /** Category-specific levels (optional - inherit from global if not set) */
    private Map<LogCategory, LogLevel> categories = new EnumMap<>(LogCategory.class);

    /** Output configuration */
    private OutputConfiguration output = new OutputConfiguration();

    /** Performance optimizations */
    private PerformanceConfiguration performance = new PerformanceConfiguration();

    /** Data enrichment */
    private EnrichmentConfiguration enrichment = new EnrichmentConfiguration();

    /**
     * Get effective level for a category (category-specific or global).
     *
     * @param category The log category to check
     * @return The effective log level for this category
     */
    public LogLevel getEffectiveLevel(LogCategory category) {
        return categories.getOrDefault(category, globalLevel);
    }

    /**
     * Check if logging is enabled for the given category and level.
     *
     * @param category The log category
     * @param level The log level to check
     * @return true if logging is enabled for this category/level combination
     */
    public boolean isLoggingEnabled(LogCategory category, LogLevel level) {
        LogLevel effectiveLevel = getEffectiveLevel(category);
        return effectiveLevel != LogLevel.OFF && level.ordinal() <= effectiveLevel.ordinal();
    }

    /**
     * Apply a preset configuration.
     *
     * @param preset The preset to apply
     */
    public void applyPreset(LoggingPreset preset) {
        switch (preset) {
            case PRODUCTION:
                globalLevel = LogLevel.WARN;
                performance.setAsync(true);
                output.setFormat(OutputFormat.JSON);
                enrichment.setIncludeScreenshots(false);
                break;
            case DEVELOPMENT:
                globalLevel = LogLevel.DEBUG;
                performance.setAsync(false);
                output.setFormat(OutputFormat.SIMPLE);
                enrichment.setIncludeScreenshots(true);
                break;
            case TESTING:
                globalLevel = LogLevel.INFO;
                categories.put(LogCategory.ACTIONS, LogLevel.DEBUG);
                categories.put(LogCategory.MATCHING, LogLevel.TRACE);
                performance.setAsync(false);
                break;
            case SILENT:
                globalLevel = LogLevel.OFF;
                break;
        }
    }

    /**
     * Set logging level for a specific category.
     *
     * @param category The category to configure
     * @param level The level to set
     */
    public void setCategoryLevel(LogCategory category, LogLevel level) {
        categories.put(category, level);
    }

    /**
     * Remove category-specific configuration, falling back to global level.
     *
     * @param category The category to reset
     */
    public void resetCategoryLevel(LogCategory category) {
        categories.remove(category);
    }
}
