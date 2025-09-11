package io.github.jspinak.brobot.test.config;

import org.springframework.boot.test.context.TestConfiguration;

/**
 * Test configuration for the action framework.
 *
 * <p>The circular dependency issue has been resolved architecturally: - TimeProvider no longer
 * depends on ExecutionModeController - TimeService interface breaks the circular dependency -
 * DefaultTimeService provides the implementation - MockFind and other mocks are now properly loaded
 * from component scan
 */
@TestConfiguration
public class TestActionConfig {
    // No beans needed here - the architectural fix resolves the Spring context issues
    // All required beans are now properly loaded through component scanning
}
