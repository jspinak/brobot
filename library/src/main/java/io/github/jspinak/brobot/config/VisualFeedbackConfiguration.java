package io.github.jspinak.brobot.config;

import io.github.jspinak.brobot.tools.logging.visual.VisualFeedbackConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for visual feedback components in Brobot.
 * 
 * This configuration enables visual feedback features when the property
 * 'brobot.highlight.enabled' is set to true. It creates and configures
 * the necessary beans for highlighting found matches and search regions.
 * 
 * @see VisualFeedbackConfig
 * @see io.github.jspinak.brobot.tools.logging.visual.HighlightManager
 */
@Configuration
@ConditionalOnProperty(
    prefix = "brobot.highlight",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = false
)
@EnableConfigurationProperties(VisualFeedbackConfig.class)
public class VisualFeedbackConfiguration {
    
    /**
     * Creates the VisualFeedbackConfig bean with nested configuration classes.
     * 
     * Note: The @EnableConfigurationProperties annotation above handles the
     * creation of the VisualFeedbackConfig bean automatically from properties.
     * The nested configuration classes (FindHighlightConfig, SearchRegionHighlightConfig, etc.)
     * are created as part of the parent configuration.
     * 
     * @param config The auto-configured VisualFeedbackConfig from properties
     * @return The configured VisualFeedbackConfig bean
     */
    @Bean
    public VisualFeedbackConfig visualFeedbackConfig(VisualFeedbackConfig config) {
        // The config is already populated from properties by Spring Boot
        // We can perform additional initialization if needed
        return config;
    }
}