package io.github.jspinak.brobot.config.dpi;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Spring ApplicationContextInitializer that ensures DPI awareness is disabled before any Spring
 * beans are created or AWT classes are loaded.
 *
 * <p>This initializer runs very early in the Spring Boot startup process, before the application
 * context is refreshed and before any beans are instantiated.
 *
 * <p>To use this initializer, it must be registered in one of these ways:
 *
 * <ul>
 *   <li>In META-INF/spring.factories
 *   <li>Via SpringApplication.addInitializers()
 *   <li>Via application.properties: context.initializer.classes
 * </ul>
 *
 * @since 1.1.0
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DPIApplicationContextInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        // This runs VERY early, before any beans are created
        System.out.println("[Brobot] Initializing DPI settings...");

        // Ensure DPI awareness is configured before any AWT classes are loaded
        DPIAwarenessDisabler.ensureInitialized();

        // Log the status for debugging
        System.out.println("[Brobot] " + DPIAwarenessDisabler.getDPIStatus());
    }
}
