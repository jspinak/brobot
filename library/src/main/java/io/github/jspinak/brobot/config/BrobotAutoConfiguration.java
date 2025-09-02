package io.github.jspinak.brobot.config;

import io.github.jspinak.brobot.tools.logging.gui.GuiAccessConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Spring Boot auto-configuration for the Brobot framework.
 * 
 * <p>This configuration ensures that:
 * <ul>
 *   <li>Default properties are loaded from brobot-defaults.properties</li>
 *   <li>BrobotProperties is available for configuration binding</li>
 *   <li>Framework initialization happens in the correct order</li>
 *   <li>Applications can override any defaults</li>
 * </ul>
 * </p>
 * 
 * <p>This class is registered in META-INF/spring.factories for automatic
 * discovery by Spring Boot applications.</p>
 * 
 * @since 1.1.0
 */
@AutoConfiguration
@Import({BrobotConfig.class, BrobotDefaultsConfiguration.class, DiagnosticsConfiguration.class})
@EnableConfigurationProperties({BrobotProperties.class, GuiAccessConfig.class})
public class BrobotAutoConfiguration {
    
    /**
     * Provides BrobotPropertiesInitializer if not already defined by the application.
     * This ensures backward compatibility with FrameworkSettings.
     */
    @Bean
    @ConditionalOnMissingBean
    public BrobotPropertiesInitializer brobotPropertiesInitializer(BrobotProperties properties) {
        return new BrobotPropertiesInitializer(properties);
    }
    
    /**
     * Provides ExecutionEnvironment as a Spring bean.
     * This returns the singleton instance to ensure consistency.
     */
    @Bean
    @ConditionalOnMissingBean
    public ExecutionEnvironment executionEnvironment(BrobotProperties properties) {
        // Configure ExecutionEnvironment directly here based on properties
        // This ensures it's configured before being used by other beans
        ExecutionEnvironment env = ExecutionEnvironment.builder()
            .mockMode(properties.getCore().isMock())
            .forceHeadless(properties.getCore().isHeadless() ? true : null)
            .allowScreenCapture(!properties.getCore().isMock())
            .build();
        ExecutionEnvironment.setInstance(env);
        String mode = properties.getCore().isMock() ? "mock" : (env.hasDisplay() ? "display" : "headless");
        System.out.println("[Brobot] Mode: " + mode);
        return env;
    }
}