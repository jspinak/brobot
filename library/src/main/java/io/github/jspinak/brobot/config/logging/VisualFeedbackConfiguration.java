package io.github.jspinak.brobot.config.logging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

// Removed old logging import that no longer exists:
// import io.github.jspinak.brobot.tools.logging.visual.VisualFeedbackConfig;

/**
 * Configuration class for visual feedback components in Brobot.
 *
 * <p>This configuration enables visual feedback features when the property
 * 'brobot.highlight.enabled' is set to true. It creates and configures the necessary beans for
 * highlighting found matches and search regions.
 *
 // * Note: VisualFeedbackConfig and HighlightManager classes have been removed // HighlightManager removed
 */
@Configuration
@ConditionalOnProperty(
        prefix = "brobot.highlight",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false)
// @EnableConfigurationProperties(VisualFeedbackConfig.class) // Class no longer exists
public class VisualFeedbackConfiguration {

    /**
     * Note: The @EnableConfigurationProperties annotation above handles the creation of the
     * configuration beans (disabled - VisualFeedbackConfig class no longer exists).
     *
     * <p>No additional @Bean method is needed as it would create a duplicate bean.
     */
}
