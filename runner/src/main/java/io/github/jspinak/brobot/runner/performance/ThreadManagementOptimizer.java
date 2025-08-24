package io.github.jspinak.brobot.runner.performance;

import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticCapable;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import io.github.jspinak.brobot.runner.performance.thread.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Central thread management orchestrator that coordinates thread-related services.
 * 
 * This component acts as a thin orchestrator that delegates specific
 * responsibilities to specialized services following the Single
 * Responsibility Principle:
 * 
 * - ThreadPoolManagementService: Manages thread pool lifecycle
 * - ThreadMonitoringService: Monitors thread health and metrics
 * - ThreadPoolFactoryService: Creates thread pools with configurations
 * - ThreadOptimizationService: Optimizes thread usage
 * 
 * The orchestrator maintains backward compatibility with the original API
 * while providing improved modularity and testability.
 * 
 * Thread Safety: This class is thread-safe.
 * 
 * @see ThreadPoolManagementService
 * @see ThreadMonitoringService
 * @see ThreadPoolFactoryService
 * @see ThreadOptimizationService
 * @since 1.0.0
 */
@Slf4j
@Component
@Data
@RequiredArgsConstructor
public class ThreadManagementOptimizer implements DiagnosticCapable {

    private final ThreadPoolManagementService poolManagement;
    private final ThreadMonitoringService monitoring;
    private final ThreadPoolFactoryService factory;
    private final ThreadOptimizationService optimization;
    
    // Global shared pool (backward compatibility)
    private ForkJoinPool sharedPool;
    
    // Configuration
    private volatile int maxThreadCount = 200;
    private volatile double cpuThreshold = 0.9;
    
    // Diagnostic mode
    private final AtomicBoolean diagnosticMode = new AtomicBoolean(false);
    
    @PostConstruct
    public void initialize() {
        log.info("Initializing thread management optimizer");
        
        // Create shared pool
        sharedPool = factory.createWorkStealingPool("shared", 
                Runtime.getRuntime().availableProcessors());
        
        // Configure system properties (backward compatibility)
        configureSystemThreadPools();
        
        log.info("Thread management optimizer initialized");
    }
    
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down thread management optimizer");
        
        // Shutdown shared pool
        if (sharedPool != null) {
            sharedPool.shutdownNow();
        }
        
        // Delegate to pool management service
        poolManagement.shutdownAll();
    }
    
    /**
     * Create an optimized thread pool for a specific purpose.
     * 
     * @param name pool name
     * @param config pool configuration
     * @return executor service
     */
    public ExecutorService createOptimizedPool(String name, ThreadPoolConfig config) {
        return poolManagement.createPool(name, config);
    }
    
    /**
     * Get the shared ForkJoinPool for parallel operations.
     * 
     * @return shared fork join pool
     */
    public ForkJoinPool getSharedPool() {
        return sharedPool;
    }
    
    /**
     * Execute a parallel computation with optimal thread usage.
     * 
     * @param task task to execute
     * @param <T> result type
     * @return completable future
     */
    public <T> CompletableFuture<T> executeParallel(Callable<T> task) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, sharedPool);
    }
    
    /**
     * Execute multiple tasks in parallel with controlled concurrency.
     * 
     * @param tasks list of tasks
     * @param maxConcurrency maximum concurrent executions
     * @param <T> result type
     * @return list of completable futures
     */
    public <T> List<CompletableFuture<T>> executeAllParallel(
            List<Callable<T>> tasks, int maxConcurrency) {
        
        Semaphore semaphore = new Semaphore(maxConcurrency);
        
        return tasks.stream()
            .map(task -> CompletableFuture.supplyAsync(() -> {
                try {
                    semaphore.acquire();
                    try {
                        return task.call();
                    } finally {
                        semaphore.release();
                    }
                } catch (Exception e) {
                    throw new CompletionException(e);
                }
            }, sharedPool))
            .toList();
    }
    
    /**
     * Monitor threads periodically and apply optimizations.
     */
    @Scheduled(fixedDelay = 5000) // Every 5 seconds
    private void monitorThreads() {
        try {
            // Get current statistics
            ThreadMonitoringService.ThreadStatistics stats = monitoring.getCurrentStatistics();
            
            // Check for high thread count
            if (stats.getCurrentThreadCount() > maxThreadCount) {
                log.warn("High thread count detected: {} threads", stats.getCurrentThreadCount());
                optimizeThreadUsage();
            }
            
            // Detect contention
            List<ThreadMonitoringService.ThreadContentionInfo> contentions = monitoring.detectContention();
            if (!contentions.isEmpty()) {
                log.warn("Thread contention detected in {} threads", contentions.size());
            }
            
            // Check pool health
            Map<String, ThreadPoolHealth> poolHealth = poolManagement.getAllPoolHealth();
            poolHealth.forEach((name, health) -> {
                if (!health.isHealthy()) {
                    log.warn("Thread pool '{}' unhealthy: {}", name, health);
                }
            });
            
        } catch (Exception e) {
            log.error("Error during thread monitoring", e);
        }
    }
    
    /**
     * Apply thread usage optimizations.
     */
    private void optimizeThreadUsage() {
        ThreadMonitoringService.ThreadStatistics stats = monitoring.getCurrentStatistics();
        ThreadOptimizationService.OptimizationResult result = optimization.optimizeThreadUsage(stats);
        
        if (result.isSuccess()) {
            log.info("Successfully applied {} thread optimizations", result.getActions().size());
        } else {
            log.warn("Failed to apply some thread optimizations");
        }
        
        if (diagnosticMode.get()) {
            log.info("[DIAGNOSTIC] Optimization result: {}", result);
        }
    }
    
    /**
     * Report thread statistics periodically.
     */
    @Scheduled(fixedDelay = 30000) // Every 30 seconds
    public void reportThreadStatistics() {
        if (!log.isDebugEnabled()) {
            return;
        }
        
        ThreadMonitoringService.ThreadStatistics stats = monitoring.getCurrentStatistics();
        
        StringBuilder report = new StringBuilder("Thread Statistics:\n");
        report.append(String.format("  Total threads: %d\n", stats.getCurrentThreadCount()));
        report.append(String.format("  Peak threads: %d\n", stats.getPeakThreadCount()));
        report.append(String.format("  Daemon threads: %d\n", stats.getDaemonThreadCount()));
        report.append(String.format("  Pool threads: %d\n", stats.getTotalPoolThreads()));
        report.append(String.format("  Active pool threads: %d\n", stats.getTotalActivePoolThreads()));
        
        stats.getPoolHealth().forEach((name, health) -> {
            report.append(String.format("  Pool '%s': %s\n", name, health));
        });
        
        log.debug(report.toString());
    }
    
    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        Map<String, Object> states = new HashMap<>();
        
        // Configuration
        states.put("maxThreadCount", maxThreadCount);
        states.put("cpuThreshold", cpuThreshold);
        
        // Current state
        ThreadMonitoringService.ThreadStatistics stats = monitoring.getCurrentStatistics();
        states.put("currentThreads", stats.getCurrentThreadCount());
        states.put("managedPools", poolManagement.getPoolNames().size());
        
        // Service diagnostics
        states.put("services.poolManagement", poolManagement.getDiagnosticInfo());
        states.put("services.monitoring", monitoring.getDiagnosticInfo());
        states.put("services.optimization", optimization.getDiagnosticInfo());
        
        return DiagnosticInfo.builder()
                .component("ThreadManagementOptimizer")
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
        
        // Propagate to all services
        poolManagement.enableDiagnosticMode(enabled);
        monitoring.enableDiagnosticMode(enabled);
        optimization.enableDiagnosticMode(enabled);
        
        log.info("Diagnostic mode {} for ThreadManagementOptimizer and all services", 
                enabled ? "enabled" : "disabled");
    }
    
    private void configureSystemThreadPools() {
        // Configure ForkJoinPool common pool
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism",
            String.valueOf(Runtime.getRuntime().availableProcessors()));
    }
    
    // Backward compatibility methods
    
    /**
     * Create a default configuration (backward compatibility).
     * 
     * @return default thread pool configuration
     */
    public static ThreadPoolConfig.ThreadPoolConfigBuilder defaultConfig() {
        return ThreadPoolConfig.builder()
                .corePoolSize(Runtime.getRuntime().availableProcessors())
                .maximumPoolSize(Runtime.getRuntime().availableProcessors() * 2)
                .keepAliveTime(60L)
                .keepAliveUnit(TimeUnit.SECONDS)
                .queueCapacity(100)
                .allowCoreThreadTimeout(true)
                .threadNamePrefix("default");
    }
}