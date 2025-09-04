package io.github.jspinak.brobot.test.annotations;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a test as potentially flaky due to timing, concurrency, or environmental issues.
 * These tests may be excluded from regular test runs.
 * 
 * To run flaky tests, set system property: -DrunFlakyTests=true
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Test
@Tag("flaky")
@EnabledIfSystemProperty(named = "runFlakyTests", matches = "true", 
    disabledReason = "Flaky test - run with -DrunFlakyTests=true")
public @interface FlakyTest {
    
    /**
     * Reason why this test is flaky.
     */
    String reason() default "";
    
    /**
     * Number of retry attempts for this test.
     */
    int retries() default 3;
    
    /**
     * Category of flakiness.
     */
    FlakyCause cause() default FlakyCause.TIMING;
    
    enum FlakyCause {
        TIMING,           // Timing-dependent assertions
        CONCURRENCY,      // Race conditions
        ENVIRONMENT,      // Environment-specific issues
        NETWORK,          // Network-related flakiness
        FILE_SYSTEM,      // File system timing issues
        ASYNC,            // Async operation timing
        EXTERNAL_PROCESS  // Depends on external processes
    }
}