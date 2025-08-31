package io.github.jspinak.brobot.test.config;

import io.github.jspinak.brobot.config.FrameworkSettings;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

/**
 * Optimized test configuration for integration tests.
 * Reduces initialization overhead and optimizes resource usage.
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
        // Enable mock mode for faster execution
        FrameworkSettings.mock = true;
        
        // Reduce wait times for mock operations
        FrameworkSettings.mockTimeFindFirst = 0.01;
        FrameworkSettings.mockTimeFindAll = 0.02;
        FrameworkSettings.mockTimeClick = 0.01;
        FrameworkSettings.mockTimeMove = 0.01;
        FrameworkSettings.mockTimeDrag = 0.02;
        FrameworkSettings.mockTimeFindHistogram = 0.02;
        FrameworkSettings.mockTimeFindColor = 0.02;
        FrameworkSettings.mockTimeClassify = 0.03;
        
        // Configure thread pools for optimal test performance
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", 
            String.valueOf(Runtime.getRuntime().availableProcessors()));
    }

    /**
     * Provide a shared executor service for test operations.
     * This reduces the overhead of creating new thread pools for each test.
     */
    @Bean
    @Primary
    public ExecutorService testExecutorService() {
        int cores = Runtime.getRuntime().availableProcessors();
        return Executors.newFixedThreadPool(Math.max(2, cores / 2));
    }

    /**
     * Provide an optimized ForkJoinPool for parallel operations.
     */
    @Bean
    public ForkJoinPool testForkJoinPool() {
        int parallelism = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
        return new ForkJoinPool(parallelism);
    }
}