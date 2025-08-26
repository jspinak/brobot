package io.github.jspinak.brobot.test;

import io.github.jspinak.brobot.config.BrobotConfiguration;
import io.github.jspinak.brobot.config.BrobotProperties;
import io.github.jspinak.brobot.config.LoggingVerbosityConfig;
import io.github.jspinak.brobot.startup.BrobotStartupConfiguration;
import io.github.jspinak.brobot.tools.logging.console.ConsoleActionConfig;
import io.github.jspinak.brobot.tools.logging.visual.VisualFeedbackConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Explicitly configures @ConfigurationProperties beans for tests.
 * This prevents Spring Boot from auto-creating duplicate beans.
 * 
 * Note: We don't include LoggingVerbosityConfig here as it's already
 * enabled by ActionLoggingConfig which is imported via BrobotConfig.
 */
@Configuration
@EnableConfigurationProperties({
    // LoggingVerbosityConfig is enabled by ActionLoggingConfig
    // VisualFeedbackConfig is enabled by ActionLoggingConfig
    BrobotConfiguration.class,
    BrobotProperties.class,
    BrobotStartupConfiguration.class
    // ConsoleActionConfig is enabled by ActionLoggingConfig
    // GuiAccessConfig is handled by MockGuiAccessConfig
})
public class TestConfigurationPropertiesConfig {
    
    // LoggingVerbosityConfig is provided by TestLoggingConfig
    
    @Bean
    @Primary
    public VisualFeedbackConfig visualFeedbackConfig() {
        VisualFeedbackConfig config = new VisualFeedbackConfig();
        config.setEnabled(false); // Disable visual feedback in tests
        config.setAutoHighlightFinds(false);
        return config;
    }
    
    @Bean
    @Primary 
    public BrobotConfiguration brobotConfiguration() {
        return new BrobotConfiguration();
    }
    
    @Bean
    @Primary
    public BrobotProperties brobotProperties() {
        return new BrobotProperties();
    }
    
    @Bean
    @Primary
    public BrobotStartupConfiguration brobotStartupConfiguration() {
        return new BrobotStartupConfiguration();
    }
    
    @Bean
    @Primary
    public ConsoleActionConfig consoleActionConfig() {
        ConsoleActionConfig config = new ConsoleActionConfig();
        config.setEnabled(false); // Disable console output in tests
        config.setLevel(ConsoleActionConfig.Level.QUIET); // Set explicit level
        return config;
    }
    
    // GuiAccessConfig is provided by MockGuiAccessConfig - don't duplicate it here
}