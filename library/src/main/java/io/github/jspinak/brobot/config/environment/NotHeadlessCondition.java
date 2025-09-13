package io.github.jspinak.brobot.config.environment;

import java.awt.GraphicsEnvironment;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Condition that checks if the application is NOT running in headless mode. This is used to
 * conditionally create beans that require a graphical environment.
 */
public class NotHeadlessCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        // Check if explicitly set to headless
        String headlessProperty = System.getProperty("java.awt.headless");
        if ("true".equalsIgnoreCase(headlessProperty)) {
            return false;
        }

        // Check if we're in a test environment with mock mode
        String testType = System.getProperty("brobot.test.type");
        if ("unit".equals(testType)) {
            return false;
        }

        // Check if mock mode is enabled
        String mockEnabled = context.getEnvironment().getProperty("brobot.mock");
        if ("true".equals(mockEnabled)) {
            return false;
        }

        // Check if GraphicsEnvironment reports headless
        try {
            return !GraphicsEnvironment.isHeadless();
        } catch (Exception e) {
            // If we can't determine, assume headless for safety
            return false;
        }
    }
}
