package io.github.jspinak.brobot.config.logging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import io.github.jspinak.brobot.tools.logging.visual.VisualFeedbackConfig;

/**
 * Configuration class for visual feedback components in Brobot.
 *
 * <p>This configuration enables visual feedback features when the property
 * 'brobot.highlight.enabled' is set to true. It creates and configures the necessary beans for
 * highlighting found matches and search regions.
 *
 * @see VisualFeedbackConfig
 * @see io.github.jspinak.brobot.tools.logging.visual.HighlightManager
 */
@Configuration
@ConditionalOnProperty(
        prefix = "brobot.highlight",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false)
@EnableConfigurationProperties(VisualFeedbackConfig.class)
public class VisualFeedbackConfiguration {

    /**
     * Note: The @EnableConfigurationProperties annotation above handles the creation of the
     * VisualFeedbackConfig bean automatically from properties. The nested configuration classes
     * (FindHighlightConfig, SearchRegionHighlightConfig, etc.) are created as part of the parent
     * configuration.
     *
     * <p>No additional @Bean method is needed as it would create a duplicate bean.
     */
}
