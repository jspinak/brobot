package io.github.jspinak.brobot.test.config;

import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.config.environment.ExecutionEnvironment;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Forces mock mode for tests that should always run in mock mode,
 * regardless of integration test settings.
 * 
 * This overrides TestConfigurationManager's decision to use live mode
 * for integration tests, ensuring tests work in headless environments.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MockOnlyTestConfiguration implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        // Force mock mode regardless of test type
        FrameworkSettings.mock = true;

        // Configure environment for mock testing
        ExecutionEnvironment environment = ExecutionEnvironment.builder()
                .mockMode(true)
                .forceHeadless(true)
                .allowScreenCapture(false)
                .build();

        ExecutionEnvironment.setInstance(environment);

        // Set system properties for headless
        System.setProperty("java.awt.headless", "true");

        // Override any conflicting properties
        System.setProperty("brobot.framework.mock", "true");
        System.setProperty("brobot.mock.enabled", "true");
    }
}