package io.github.jspinak.brobot.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

/**
 * Composite annotation to disable tests in CI environments. Tests marked with this annotation will
 * be skipped when running in CI/CD pipelines.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@DisabledIfEnvironmentVariable(
        named = "CI",
        matches = "true",
        disabledReason = "Test disabled in CI environment")
@DisabledIfEnvironmentVariable(
        named = "GITHUB_ACTIONS",
        matches = "true",
        disabledReason = "Test disabled in GitHub Actions")
public @interface DisabledInCI {
    String value() default "Test requires display or non-CI environment";
}
