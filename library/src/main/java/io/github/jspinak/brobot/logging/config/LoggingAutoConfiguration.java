package io.github.jspinak.brobot.logging.config;

import io.github.jspinak.brobot.logging.*;
import io.github.jspinak.brobot.logging.correlation.CorrelationContext;
import io.github.jspinak.brobot.logging.formatter.*;
// Removed old logging import that no longer exists:
// import io.github.jspinak.brobot.logging.integration.ActionLoggingIntegration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for the Brobot logging system.
 *
 * <p>This configuration class sets up all the necessary beans for the new structured
 * logging system. It provides default implementations that can be overridden by
 * application-specific configurations.
 *
 * <p>The configuration includes:
 * <ul>
 *   <li>Logger implementation
 *   <li>Formatters for different output formats
 *   <li>Correlation context management
 *   <li>Integration with existing ActionConfig
 * </ul>
 */
@Configuration
@EnableConfigurationProperties(LoggingConfiguration.class)
public class LoggingAutoConfiguration {

    /**
     * Creates the correlation context bean for tracking sessions and operations.
     */
    @Bean
    @ConditionalOnMissingBean
    public CorrelationContext correlationContext() {
        return new CorrelationContext();
    }

    /**
     * Creates the simple log formatter for human-readable output.
     */
    @Bean
    @ConditionalOnMissingBean(name = "simpleLogFormatter")
    public SimpleLogFormatter simpleLogFormatter() {
        return new SimpleLogFormatter();
    }

    /**
     * Creates the structured log formatter for key-value output.
     */
    @Bean
    @ConditionalOnMissingBean(name = "structuredLogFormatter")
    public StructuredLogFormatter structuredLogFormatter() {
        return new StructuredLogFormatter();
    }

    /**
     * Creates the JSON log formatter for machine-readable output.
     */
    @Bean
    @ConditionalOnMissingBean(name = "jsonLogFormatter")
    public JsonLogFormatter jsonLogFormatter() {
        return new JsonLogFormatter();
    }

    /**
     * Creates the main logger implementation.
     *
     * <p>The formatter is selected based on the output format configuration.
     * This bean can be overridden to provide custom logger implementations.
     */
    @Bean
    @ConditionalOnMissingBean
    public BrobotLogger brobotLogger(
            LoggingConfiguration config,
            CorrelationContext correlationContext) {

        // Select formatter based on configuration
        LogFormatter formatter;
        switch (config.getOutput().getFormat()) {
            case JSON:
                formatter = jsonLogFormatter();
                break;
            case STRUCTURED:
                formatter = structuredLogFormatter();
                break;
            case SIMPLE:
            default:
                formatter = simpleLogFormatter();
                break;
        }

        return new BrobotLoggerImpl(config, correlationContext,
                simpleLogFormatter(), structuredLogFormatter(), jsonLogFormatter());
    }

    /**
     * Creates the action logging integration for backward compatibility.
     *
     * <p>This bean bridges the gap between the existing ActionConfig.LoggingOptions
     * and the new structured logging system.
     */
    // Commented out - ActionLoggingIntegration class no longer exists
    // @Bean
    // @ConditionalOnMissingBean
    // public ActionLoggingIntegration actionLoggingIntegration(
    //         BrobotLogger brobotLogger,
    //         CorrelationContext correlationContext) {
    //     return new ActionLoggingIntegration(brobotLogger, correlationContext);
    // }

    /**
     * Creates a logging preset manager for easy configuration.
     */
    @Bean
    @ConditionalOnMissingBean
    public LoggingPresetManager loggingPresetManager(LoggingConfiguration config) {
        return new LoggingPresetManager(config);
    }

    /**
     * Manages logging presets for different environments.
     */
    public static class LoggingPresetManager {
        private final LoggingConfiguration config;

        public LoggingPresetManager(LoggingConfiguration config) {
            this.config = config;
        }

        /**
         * Apply a preset based on the active Spring profile.
         */
        public void applyProfilePreset(String profile) {
            switch (profile.toLowerCase()) {
                case "production":
                case "prod":
                    config.applyPreset(LoggingConfiguration.LoggingPreset.PRODUCTION);
                    break;
                case "development":
                case "dev":
                    config.applyPreset(LoggingConfiguration.LoggingPreset.DEVELOPMENT);
                    break;
                case "test":
                    config.applyPreset(LoggingConfiguration.LoggingPreset.TESTING);
                    break;
                case "silent":
                    config.applyPreset(LoggingConfiguration.LoggingPreset.SILENT);
                    break;
                default:
                    // Keep configured values
                    break;
            }
        }

        /**
         * Set logging level for all categories.
         */
        public void setGlobalLevel(LogLevel level) {
            config.setGlobalLevel(level);
        }

        /**
         * Set logging level for a specific category.
         */
        public void setCategoryLevel(LogCategory category, LogLevel level) {
            config.getCategories().put(category, level);
        }

        /**
         * Enable or disable specific enrichment features.
         */
        public void setEnrichment(boolean screenshots, boolean similarity, boolean timing, boolean memory) {
            config.getEnrichment().setIncludeScreenshots(screenshots);
            config.getEnrichment().setIncludeSimilarityScores(similarity);
            config.getEnrichment().setIncludeTimingBreakdown(timing);
            config.getEnrichment().setIncludeMemoryUsage(memory);
        }
    }
}