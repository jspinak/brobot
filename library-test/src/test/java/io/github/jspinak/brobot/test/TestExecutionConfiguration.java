package io.github.jspinak.brobot.test;

import java.lang.annotation.*;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.platform.commons.annotation.Testable;

/**
 * Provides test execution configuration for better isolation and resource management. This includes
 * test instance lifecycle management and execution mode configuration.
 */
public class TestExecutionConfiguration {

    /**
     * Annotation for tests that require complete isolation. These tests will run sequentially and
     * have their own test instance.
     */
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    @Execution(ExecutionMode.SAME_THREAD)
    @Isolated
    @Testable
    public @interface IsolatedTest {
        String value() default "";
    }

    /**
     * Annotation for tests that can share a test instance. These tests can run in parallel and
     * share setup/teardown.
     */
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Execution(ExecutionMode.CONCURRENT)
    @Testable
    public @interface SharedInstanceTest {
        String value() default "";
    }

    /**
     * Annotation for resource-intensive tests. These tests require sequential execution to avoid
     * resource conflicts.
     */
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    @Execution(ExecutionMode.SAME_THREAD)
    @Testable
    public @interface ResourceIntensiveTest {
        String value() default "";
    }

    /**
     * Annotation for tests that manipulate static state. These must run in isolation to prevent
     * state pollution.
     */
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    @Execution(ExecutionMode.SAME_THREAD)
    @Isolated
    @Testable
    public @interface StaticStateTest {
        String value() default "";
    }
}
