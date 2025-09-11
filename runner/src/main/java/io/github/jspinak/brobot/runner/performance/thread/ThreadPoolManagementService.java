package io.github.jspinak.brobot.runner.performance.thread;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import jakarta.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticCapable;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;

import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for managing the lifecycle of thread pools.
 *
 * <p>This service handles creation, registration, monitoring, and shutdown of thread pools used
 * throughout the application. It maintains a registry of all managed pools and provides health
 * monitoring capabilities.
 *
 * <p>Thread Safety: This class is thread-safe.
 *
 * @since 1.0.0
 */
@Slf4j
@Service
public class ThreadPoolManagementService implements DiagnosticCapable {

    private final Map<String, ManagedThreadPool> pools = new ConcurrentHashMap<>();
    private final ThreadPoolFactoryService factoryService;

    // Diagnostic mode
    private final AtomicBoolean diagnosticMode = new AtomicBoolean(false);

    @Autowired
    public ThreadPoolManagementService(ThreadPoolFactoryService factoryService) {
        this.factoryService = factoryService;
    }

    /**
     * Create and register a new thread pool.
     *
     * @param name unique name for the pool
     * @param config pool configuration
     * @return the created executor service
     * @throws IllegalArgumentException if name is already in use
     */
    public ExecutorService createPool(String name, ThreadPoolConfig config) {
        if (pools.containsKey(name)) {
            throw new IllegalArgumentException(
                    "Thread pool with name '" + name + "' already exists");
        }

        ManagedThreadPool pool = factoryService.createManagedPool(name, config);
        pools.put(name, pool);

        log.info("Created thread pool '{}' with config: {}", name, config);

        if (diagnosticMode.get()) {
            log.info(
                    "[DIAGNOSTIC] Pool '{}' created - Core: {}, Max: {}, Queue: {}",
                    name,
                    config.getCorePoolSize(),
                    config.getMaximumPoolSize(),
                    config.getQueueCapacity());
        }

        return pool;
    }

    /**
     * Get an existing thread pool by name.
     *
     * @param name pool name
     * @return Optional containing the pool if found
     */
    public Optional<ExecutorService> getPool(String name) {
        return Optional.ofNullable(pools.get(name));
    }

    /**
     * Get all registered pool names.
     *
     * @return unmodifiable set of pool names
     */
    public Set<String> getPoolNames() {
        return Collections.unmodifiableSet(pools.keySet());
    }

    /**
     * Shutdown a specific thread pool.
     *
     * @param name pool name
     * @param awaitTermination whether to wait for termination
     * @param timeoutSeconds timeout in seconds if awaiting
     * @return true if pool was found and shutdown initiated
     */
    public boolean shutdownPool(String name, boolean awaitTermination, long timeoutSeconds) {
        ManagedThreadPool pool = pools.remove(name);
        if (pool == null) {
            log.warn("Attempted to shutdown non-existent pool: {}", name);
            return false;
        }

        log.info("Shutting down thread pool: {}", name);
        pool.shutdown();

        if (awaitTermination) {
            try {
                if (!pool.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
                    log.warn(
                            "Pool '{}' did not terminate within {} seconds, forcing shutdown",
                            name,
                            timeoutSeconds);
                    pool.shutdownNow();
                }
            } catch (InterruptedException e) {
                log.error("Interrupted while waiting for pool '{}' termination", name, e);
                pool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        return true;
    }

    /** Shutdown all managed thread pools. */
    @PreDestroy
    public void shutdownAll() {
        log.info("Shutting down all {} managed thread pools", pools.size());

        // Initiate orderly shutdown for all pools
        pools.forEach(
                (name, pool) -> {
                    log.debug("Initiating shutdown for pool: {}", name);
                    pool.shutdown();
                });

        // Wait for termination
        pools.forEach(
                (name, pool) -> {
                    try {
                        if (!pool.awaitTermination(30, TimeUnit.SECONDS)) {
                            log.warn(
                                    "Pool '{}' did not terminate gracefully, forcing shutdown",
                                    name);
                            pool.shutdownNow();
                        }
                    } catch (InterruptedException e) {
                        log.error("Interrupted while shutting down pool '{}'", name, e);
                        pool.shutdownNow();
                    }
                });

        pools.clear();
        log.info("All thread pools shutdown complete");
    }

    /**
     * Get health information for all pools.
     *
     * @return map of pool names to health info
     */
    public Map<String, ThreadPoolHealth> getAllPoolHealth() {
        return pools.entrySet().stream()
                .collect(
                        Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getHealth()));
    }

    /**
     * Get health information for a specific pool.
     *
     * @param name pool name
     * @return Optional containing health info if pool exists
     */
    public Optional<ThreadPoolHealth> getPoolHealth(String name) {
        ManagedThreadPool pool = pools.get(name);
        return pool != null ? Optional.of(pool.getHealth()) : Optional.empty();
    }

    /**
     * Adjust the size of a thread pool.
     *
     * @param name pool name
     * @param coreSize new core pool size
     * @param maxSize new maximum pool size
     * @return true if pool was found and adjusted
     */
    public boolean adjustPoolSize(String name, int coreSize, int maxSize) {
        ManagedThreadPool pool = pools.get(name);
        if (pool == null) {
            return false;
        }

        if (coreSize > maxSize) {
            throw new IllegalArgumentException("Core size cannot be greater than max size");
        }

        pool.setCorePoolSize(coreSize);
        pool.setMaximumPoolSize(maxSize);

        log.info("Adjusted pool '{}' size - Core: {}, Max: {}", name, coreSize, maxSize);

        return true;
    }

    /**
     * Check if a pool exists.
     *
     * @param name pool name
     * @return true if pool exists
     */
    public boolean poolExists(String name) {
        return pools.containsKey(name);
    }

    /**
     * Get the total number of active threads across all pools.
     *
     * @return total active thread count
     */
    public int getTotalActiveThreads() {
        return pools.values().stream().mapToInt(ThreadPoolExecutor::getActiveCount).sum();
    }

    /**
     * Get the total number of queued tasks across all pools.
     *
     * @return total queued task count
     */
    public int getTotalQueuedTasks() {
        return pools.values().stream().mapToInt(pool -> pool.getQueue().size()).sum();
    }

    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        Map<String, Object> states = new HashMap<>();
        states.put("totalPools", pools.size());
        states.put("totalActiveThreads", getTotalActiveThreads());
        states.put("totalQueuedTasks", getTotalQueuedTasks());

        // Per-pool statistics
        pools.forEach(
                (name, pool) -> {
                    ThreadPoolHealth health = pool.getHealth();
                    states.put("pool." + name + ".size", health.poolSize());
                    states.put("pool." + name + ".active", health.activeThreads());
                    states.put("pool." + name + ".queued", health.queueSize());
                    states.put("pool." + name + ".completed", health.completedTasks());
                    states.put("pool." + name + ".utilization", health.utilization());
                });

        return DiagnosticInfo.builder()
                .component("ThreadPoolManagementService")
                .states(states)
                .build();
    }

    @Override
    public boolean isDiagnosticModeEnabled() {
        return diagnosticMode.get();
    }

    @Override
    public void enableDiagnosticMode(boolean enabled) {
        diagnosticMode.set(enabled);
        log.info(
                "Diagnostic mode {} for ThreadPoolManagementService",
                enabled ? "enabled" : "disabled");
    }
}
