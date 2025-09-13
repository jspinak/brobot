package io.github.jspinak.brobot.config.core;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Resolver for mock mode configuration.
 * 
 * The single property is: brobot.mock
 * 
 * @since 1.1.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MockModeResolver {

    private final Environment environment;

    /**
     * Resolves the mock mode setting.
     * 
     * @return true if mock mode should be enabled, false otherwise
     */
    public boolean isMockMode() {
        return environment.getProperty("brobot.mock", Boolean.class, false);
    }

    /**
     * Logs the current mock mode configuration for debugging.
     */
    public void logConfiguration() {
        boolean mockMode = isMockMode();
        log.info("Mock Mode: {}", mockMode ? "ENABLED" : "DISABLED");
    }
}