package io.github.jspinak.brobot.config.core;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import io.github.jspinak.brobot.config.environment.ExecutionEnvironment;

// import io.github.jspinak.brobot.tools.logging.gui.GuiAccessConfig;

/**
 * Spring Boot auto-configuration for the Brobot framework.
 *
 * <p>This configuration ensures that:
 *
 * <ul>
 *   <li>Default properties are loaded from brobot-defaults.properties
 *   <li>BrobotProperties is available for configuration binding
 *   <li>Framework initialization happens in the correct order
 *   <li>Applications can override any defaults
 * </ul>
 *
 * <p>This class is registered in META-INF/spring.factories for automatic discovery by Spring Boot
 * applications.
 *
 * @since 1.1.0
 */
@AutoConfiguration
@Import({
    BrobotConfig.class,
    BrobotDefaultsConfiguration.class,
    io.github.jspinak.brobot.config.environment.DiagnosticsConfiguration.class,
    io.github.jspinak.brobot.annotations.AnnotationConfiguration.class
})
@EnableConfigurationProperties({
    BrobotProperties.class,
    io.github.jspinak.brobot.debug.ImageDebugConfig.class
})
public class BrobotAutoConfiguration {

    // REMOVED static initializer that was causing early GraphicsEnvironment initialization
    // The ForceNonHeadlessInitializer was being called too early and triggering
    // GraphicsEnvironment.isHeadless() before JVM arguments could take effect.
    // Now initialization happens lazily when components actually need it.

    /**
     * Provides BrobotPropertiesInitializer if not already defined by the application. This ensures
     * backward compatibility with BrobotProperties.
     */
    @Bean
    @ConditionalOnMissingBean
    public BrobotPropertiesInitializer brobotPropertiesInitializer(
            BrobotProperties properties,
            MockModeResolver mockModeResolver,
            ApplicationContext applicationContext) {
        return new BrobotPropertiesInitializer(properties, mockModeResolver, applicationContext);
    }

    /**
     * Provides ExecutionEnvironment as a Spring bean. This returns the singleton instance to ensure
     * consistency.
     */
    @Bean
    @ConditionalOnMissingBean
    public ExecutionEnvironment executionEnvironment(
            BrobotProperties properties, MockModeResolver mockModeResolver) {
        // Configure ExecutionEnvironment directly here based on properties
        // This ensures it's configured before being used by other beans
        boolean mockMode = mockModeResolver.isMockMode();
        ExecutionEnvironment env =
                ExecutionEnvironment.builder()
                        .mockMode(mockMode)
                        .forceHeadless(properties.getCore().isHeadless() ? true : null)
                        .allowScreenCapture(!mockMode)
                        .build();
        ExecutionEnvironment.setInstance(env);
        String mode = mockMode ? "mock" : (env.hasDisplay() ? "display" : "headless");
        System.out.println("[Brobot] Mode: " + mode);
        return env;
    }
}
