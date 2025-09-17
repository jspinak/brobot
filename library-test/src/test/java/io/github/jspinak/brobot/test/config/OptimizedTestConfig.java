package io.github.jspinak.brobot.test.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Optimized test configuration for integration tests. Reduces initialization overhead and optimizes
 * resource usage.
 */
@TestConfiguration
public class OptimizedTestConfig implements BeforeAllCallback {

    private static boolean initialized = false;
    private static final Object INIT_LOCK = new Object();

    @Override
    public void beforeAll(ExtensionContext context) {
        synchronized (INIT_LOCK) {
            if (!initialized) {
                optimizeTestSettings();
                initialized = true;
            }
        }
    }

    private void optimizeTestSettings() {
        // Note: BrobotProperties is now immutable and configured via Spring
        // Mock mode and timing settings should be configured in application-test.properties
        // Example properties:
        // brobot.core.mock=true
        // brobot.mock.time-find-first=0.01
        // brobot.mock.time-find-all=0.02
        // etc.

        // Configure thread pools for optimal test performance
        System.setProperty(
                "java.util.concurrent.ForkJoinPool.common.parallelism",
                String.valueOf(Runtime.getRuntime().availableProcessors()));
    }

    /**
     * Provide a shared executor service for test operations. This reduces the overhead of creating
     * new thread pools for each test.
     */
    @Bean
    @Primary
    public ExecutorService testExecutorService() {
        int cores = Runtime.getRuntime().availableProcessors();
        return Executors.newFixedThreadPool(Math.max(2, cores / 2));
    }

    /** Provide an optimized ForkJoinPool for parallel operations. */
    @Bean
    public ForkJoinPool testForkJoinPool() {
        int parallelism = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
        return new ForkJoinPool(parallelism);
    }
}
