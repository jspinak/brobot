package io.github.jspinak.brobot.runner.ui;

import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import javafx.application.Platform;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        // Add other auto-configurations to exclude if needed
})
public class TestConfig {

    // Initialize JavaFX platform for tests
    static {
        try {
            if (!Platform.isFxApplicationThread()) {
                Platform.startup(() -> {});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates test properties with temporary paths
     */
    @Bean
    @Primary
    public BrobotRunnerProperties testProperties() {
        BrobotRunnerProperties properties = new BrobotRunnerProperties();
        // Default values will be overridden in test setup
        return properties;
    }
}