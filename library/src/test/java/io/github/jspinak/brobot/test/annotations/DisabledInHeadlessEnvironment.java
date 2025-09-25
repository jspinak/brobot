package io.github.jspinak.brobot.test.annotations;

import java.lang.annotation.*;

import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;

import io.github.jspinak.brobot.test.extensions.DisplayRequirementExtension;

/**
 * Composite annotation that disables tests in all headless environments: - CI/CD environments - WSL
 * (Windows Subsystem for Linux) - Docker containers without display - Headless servers
 *
 * <p>This replaces the need to add multiple @DisabledIfEnvironmentVariable annotations
 * and @RequiresDisplay to each test class.
 *
 * <p>Usage:
 *
 * <pre>{@code
 * @DisabledInHeadlessEnvironment
 * class ScreenCaptureTest {
 *     // Test that requires real display
 * }
 * }</pre>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@DisabledIfEnvironmentVariable(
        named = "CI",
        matches = "true",
        disabledReason = "Disabled in CI - requires display")
@DisabledIfEnvironmentVariable(
        named = "GITHUB_ACTIONS",
        matches = "true",
        disabledReason = "Disabled in GitHub Actions - requires display")
@DisabledIfEnvironmentVariable(
        named = "WSL_DISTRO_NAME",
        matches = ".*",
        disabledReason = "Disabled in WSL - requires real display")
@ExtendWith(DisplayRequirementExtension.class)
@RequiresDisplay("Test requires real display for screen operations")
public @interface DisabledInHeadlessEnvironment {
    /**
     * Optional custom reason for disabling in headless environments.
     *
     * @return reason for requiring display
     */
    String value() default "Requires real display for screen capture operations";
}
