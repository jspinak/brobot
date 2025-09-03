package io.github.jspinak.brobot.config.core;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Loads default Brobot properties from the library's resources.
 * 
 * <p>This configuration ensures that Brobot has sensible defaults even if
 * the application doesn't provide any configuration. Applications can override
 * any of these defaults by providing their own values in application.properties
 * or application.yml.</p>
 * 
 * <p>The {@code ignoreResourceNotFound = false} ensures that the defaults file
 * must exist in the library, preventing silent failures if it's accidentally
 * removed during refactoring.</p>
 * 
 * @since 1.1.0
 */
@Configuration
@PropertySource(value = "classpath:brobot-defaults.properties", ignoreResourceNotFound = false)
public class BrobotDefaultsConfiguration {
    // This class just loads the default properties
    // The actual configuration is handled by BrobotProperties
}