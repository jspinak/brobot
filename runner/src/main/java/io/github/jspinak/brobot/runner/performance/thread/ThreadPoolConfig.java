package io.github.jspinak.brobot.runner.performance.thread;

import java.util.concurrent.TimeUnit;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Configuration for creating thread pools.
 *
 * <p>This class provides a fluent builder API for configuring thread pools with appropriate
 * settings for different workload types.
 *
 * @since 1.0.0
 */
@Getter
@Builder
@ToString
public class ThreadPoolConfig {

    private final int corePoolSize;
    private final int maximumPoolSize;
    private final long keepAliveTime;
    private final TimeUnit keepAliveUnit;
    private final int queueCapacity;
    private final boolean allowCoreThreadTimeout;
    private final String threadNamePrefix;

    /**
     * Create a default configuration suitable for general purpose use.
     *
     * @return default thread pool configuration
     */
    public static ThreadPoolConfig defaultConfig() {
        int cores = Runtime.getRuntime().availableProcessors();
        return ThreadPoolConfig.builder()
                .corePoolSize(cores)
                .maximumPoolSize(cores * 2)
                .keepAliveTime(60L)
                .keepAliveUnit(TimeUnit.SECONDS)
                .queueCapacity(100)
                .allowCoreThreadTimeout(true)
                .threadNamePrefix("default")
                .build();
    }

    /**
     * Create a configuration optimized for I/O intensive tasks.
     *
     * @return I/O optimized thread pool configuration
     */
    public static ThreadPoolConfig ioIntensiveConfig() {
        int cores = Runtime.getRuntime().availableProcessors();
        return ThreadPoolConfig.builder()
                .corePoolSize(cores * 2)
                .maximumPoolSize(cores * 4)
                .keepAliveTime(120L)
                .keepAliveUnit(TimeUnit.SECONDS)
                .queueCapacity(500)
                .allowCoreThreadTimeout(true)
                .threadNamePrefix("io-intensive")
                .build();
    }

    /**
     * Create a configuration optimized for CPU intensive tasks.
     *
     * @return CPU optimized thread pool configuration
     */
    public static ThreadPoolConfig cpuIntensiveConfig() {
        int cores = Runtime.getRuntime().availableProcessors();
        return ThreadPoolConfig.builder()
                .corePoolSize(cores)
                .maximumPoolSize(cores)
                .keepAliveTime(0L)
                .keepAliveUnit(TimeUnit.SECONDS)
                .queueCapacity(50)
                .allowCoreThreadTimeout(false)
                .threadNamePrefix("cpu-intensive")
                .build();
    }

    /**
     * Create a configuration for single-threaded executor.
     *
     * @return single-threaded configuration
     */
    public static ThreadPoolConfig singleThreadConfig() {
        return ThreadPoolConfig.builder()
                .corePoolSize(1)
                .maximumPoolSize(1)
                .keepAliveTime(0L)
                .keepAliveUnit(TimeUnit.SECONDS)
                .queueCapacity(Integer.MAX_VALUE)
                .allowCoreThreadTimeout(false)
                .threadNamePrefix("single-thread")
                .build();
    }

    /**
     * Create a configuration for scheduled tasks.
     *
     * @param corePoolSize number of threads to keep in the pool
     * @return scheduled task configuration
     */
    public static ThreadPoolConfig scheduledConfig(int corePoolSize) {
        return ThreadPoolConfig.builder()
                .corePoolSize(corePoolSize)
                .maximumPoolSize(corePoolSize)
                .keepAliveTime(10L)
                .keepAliveUnit(TimeUnit.SECONDS)
                .queueCapacity(Integer.MAX_VALUE)
                .allowCoreThreadTimeout(false)
                .threadNamePrefix("scheduled")
                .build();
    }

    /**
     * Validate the configuration.
     *
     * @throws IllegalStateException if configuration is invalid
     */
    public void validate() {
        if (corePoolSize < 0) {
            throw new IllegalStateException("Core pool size must be non-negative");
        }
        if (maximumPoolSize <= 0) {
            throw new IllegalStateException("Maximum pool size must be positive");
        }
        if (maximumPoolSize < corePoolSize) {
            throw new IllegalStateException("Maximum pool size must be >= core pool size");
        }
        if (keepAliveTime < 0) {
            throw new IllegalStateException("Keep alive time must be non-negative");
        }
        if (queueCapacity <= 0) {
            throw new IllegalStateException("Queue capacity must be positive");
        }
        if (threadNamePrefix == null || threadNamePrefix.trim().isEmpty()) {
            throw new IllegalStateException("Thread name prefix must not be empty");
        }
    }
}
