package io.github.jspinak.brobot.runner.performance.thread;

import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticCapable;
import io.github.jspinak.brobot.runner.common.diagnostics.DiagnosticInfo;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service responsible for optimizing thread usage based on system metrics.
 * 
 * This service analyzes thread pool performance, system resources, and
 * workload characteristics to make optimization decisions. It can adjust
 * pool sizes, recommend configuration changes, and handle resource constraints.
 * 
 * Thread Safety: This class is thread-safe.
 * 
 * @since 1.0.0
 */
@Slf4j
@Service
public class ThreadOptimizationService implements DiagnosticCapable {
    
    private final ThreadPoolManagementService poolManagement;
    private final ThreadMonitoringService monitoring;
    
    @Value("${thread.optimization.max-threads:200}")
    private int maxThreadThreshold;
    
    @Value("${thread.optimization.cpu-threshold:0.9}")
    private double cpuThreshold;
    
    @Value("${thread.optimization.memory-threshold:0.85}")
    private double memoryThreshold;
    
    // Optimization metrics
    private final AtomicLong totalOptimizations = new AtomicLong();
    private final AtomicLong successfulOptimizations = new AtomicLong();
    private final Map<String, AtomicLong> optimizationsByType = new ConcurrentHashMap<>();
    
    // Diagnostic mode
    private final AtomicBoolean diagnosticMode = new AtomicBoolean(false);
    
    @Autowired
    public ThreadOptimizationService(ThreadPoolManagementService poolManagement,
                                   ThreadMonitoringService monitoring) {
        this.poolManagement = poolManagement;
        this.monitoring = monitoring;
    }
    
    /**
     * Optimize thread usage based on current system state.
     * 
     * @param stats current thread statistics
     * @return optimization result
     */
    public OptimizationResult optimizeThreadUsage(ThreadMonitoringService.ThreadStatistics stats) {
        totalOptimizations.incrementAndGet();
        
        List<OptimizationAction> actions = new ArrayList<>();
        SystemMetrics metrics = collectSystemMetrics();
        
        // Check for high thread count
        if (stats.getCurrentThreadCount() > maxThreadThreshold) {
            actions.addAll(handleHighThreadCount(stats, metrics));
        }
        
        // Check for high CPU usage
        if (metrics.getCpuUsage() > cpuThreshold) {
            actions.addAll(handleHighCpuUsage(stats, metrics));
        }
        
        // Check for memory pressure
        if (metrics.getMemoryUsage() > memoryThreshold) {
            actions.addAll(handleMemoryPressure(stats, metrics));
        }
        
        // Check individual pool health
        stats.getPoolHealth().forEach((name, health) -> {
            if (!health.isHealthy()) {
                actions.addAll(optimizeUnhealthyPool(name, health));
            } else if (health.isUnderLoad()) {
                actions.addAll(handlePoolUnderLoad(name, health));
            } else if (health.utilization() < 0.1) {
                actions.addAll(handleIdlePool(name, health));
            }
        });
        
        // Apply optimizations
        boolean success = applyOptimizations(actions);
        if (success) {
            successfulOptimizations.incrementAndGet();
        }
        
        return OptimizationResult.builder()
                .timestamp(System.currentTimeMillis())
                .actions(actions)
                .success(success)
                .metrics(metrics)
                .build();
    }
    
    /**
     * Determine optimization strategy based on metrics.
     * 
     * @param metrics system metrics
     * @return optimization strategy
     */
    public OptimizationStrategy determineStrategy(SystemMetrics metrics) {
        if (metrics.getCpuUsage() > cpuThreshold && metrics.getMemoryUsage() > memoryThreshold) {
            return OptimizationStrategy.AGGRESSIVE;
        } else if (metrics.getCpuUsage() > cpuThreshold || metrics.getMemoryUsage() > memoryThreshold) {
            return OptimizationStrategy.MODERATE;
        } else if (metrics.getCpuUsage() < 0.3 && metrics.getMemoryUsage() < 0.3) {
            return OptimizationStrategy.RELAXED;
        } else {
            return OptimizationStrategy.BALANCED;
        }
    }
    
    /**
     * Apply optimization actions.
     * 
     * @param actions list of actions to apply
     * @return true if all actions succeeded
     */
    public boolean applyOptimizations(List<OptimizationAction> actions) {
        if (actions.isEmpty()) {
            return true;
        }
        
        log.info("Applying {} optimization actions", actions.size());
        boolean allSuccess = true;
        
        for (OptimizationAction action : actions) {
            try {
                boolean success = applyAction(action);
                if (!success) {
                    allSuccess = false;
                }
                
                optimizationsByType.computeIfAbsent(action.getType().name(), k -> new AtomicLong())
                        .incrementAndGet();
                
                if (diagnosticMode.get()) {
                    log.info("[DIAGNOSTIC] Applied optimization: {} - Success: {}", action, success);
                }
            } catch (Exception e) {
                log.error("Failed to apply optimization action: {}", action, e);
                allSuccess = false;
            }
        }
        
        return allSuccess;
    }
    
    /**
     * Adjust pool size based on optimization parameters.
     * 
     * @param poolName pool name
     * @param adjustment size adjustment
     */
    public void adjustPoolSize(String poolName, PoolAdjustment adjustment) {
        poolManagement.adjustPoolSize(poolName, 
                adjustment.getCoreSize(), adjustment.getMaxSize());
    }
    
    private List<OptimizationAction> handleHighThreadCount(
            ThreadMonitoringService.ThreadStatistics stats, SystemMetrics metrics) {
        
        List<OptimizationAction> actions = new ArrayList<>();
        
        // Find pools that can be reduced
        stats.getPoolHealth().forEach((name, health) -> {
            if (health.utilization() < 0.5 && health.poolSize() > 1) {
                int newSize = Math.max(1, health.poolSize() / 2);
                actions.add(OptimizationAction.builder()
                        .type(ActionType.REDUCE_POOL_SIZE)
                        .targetPool(name)
                        .description("Reduce pool size due to low utilization")
                        .parameters(Map.of("newSize", newSize))
                        .build());
            }
        });
        
        return actions;
    }
    
    private List<OptimizationAction> handleHighCpuUsage(
            ThreadMonitoringService.ThreadStatistics stats, SystemMetrics metrics) {
        
        List<OptimizationAction> actions = new ArrayList<>();
        
        // Reduce CPU-intensive pool sizes
        stats.getPoolHealth().forEach((name, health) -> {
            if (name.contains("cpu") && health.activeThreads() > metrics.getAvailableCores()) {
                actions.add(OptimizationAction.builder()
                        .type(ActionType.THROTTLE_POOL)
                        .targetPool(name)
                        .description("Throttle CPU-intensive pool")
                        .parameters(Map.of("maxThreads", metrics.getAvailableCores()))
                        .build());
            }
        });
        
        return actions;
    }
    
    private List<OptimizationAction> handleMemoryPressure(
            ThreadMonitoringService.ThreadStatistics stats, SystemMetrics metrics) {
        
        List<OptimizationAction> actions = new ArrayList<>();
        
        // Reduce queue sizes for pools with large queues
        stats.getPoolHealth().forEach((name, health) -> {
            if (health.queueSize() > 100) {
                actions.add(OptimizationAction.builder()
                        .type(ActionType.REDUCE_QUEUE_SIZE)
                        .targetPool(name)
                        .description("Reduce queue size due to memory pressure")
                        .parameters(Map.of("maxQueue", 50))
                        .build());
            }
        });
        
        return actions;
    }
    
    private List<OptimizationAction> optimizeUnhealthyPool(String name, ThreadPoolHealth health) {
        List<OptimizationAction> actions = new ArrayList<>();
        
        if (health.rejectedTasks() > 0) {
            // Increase pool size if tasks are being rejected
            int newMax = Math.min(health.poolSize() * 2, 50);
            actions.add(OptimizationAction.builder()
                    .type(ActionType.INCREASE_POOL_SIZE)
                    .targetPool(name)
                    .description("Increase pool size due to rejected tasks")
                    .parameters(Map.of("newMax", newMax))
                    .build());
        }
        
        return actions;
    }
    
    private List<OptimizationAction> handlePoolUnderLoad(String name, ThreadPoolHealth health) {
        List<OptimizationAction> actions = new ArrayList<>();
        
        // Temporarily increase pool size
        int newCore = Math.min(health.poolSize() + 2, health.activeThreads() + 2);
        actions.add(OptimizationAction.builder()
                .type(ActionType.SCALE_UP)
                .targetPool(name)
                .description("Scale up pool under load")
                .parameters(Map.of("newCore", newCore))
                .build());
        
        return actions;
    }
    
    private List<OptimizationAction> handleIdlePool(String name, ThreadPoolHealth health) {
        List<OptimizationAction> actions = new ArrayList<>();
        
        if (health.poolSize() > 1) {
            actions.add(OptimizationAction.builder()
                    .type(ActionType.SCALE_DOWN)
                    .targetPool(name)
                    .description("Scale down idle pool")
                    .parameters(Map.of("newCore", 1))
                    .build());
        }
        
        return actions;
    }
    
    private boolean applyAction(OptimizationAction action) {
        switch (action.getType()) {
            case REDUCE_POOL_SIZE:
            case INCREASE_POOL_SIZE:
            case SCALE_UP:
            case SCALE_DOWN:
                Integer newSize = (Integer) action.getParameters().get("newCore");
                Integer newMax = (Integer) action.getParameters().get("newMax");
                if (newSize == null) newSize = newMax;
                if (newMax == null) newMax = newSize;
                return poolManagement.adjustPoolSize(action.getTargetPool(), newSize, newMax);
                
            case THROTTLE_POOL:
                Integer maxThreads = (Integer) action.getParameters().get("maxThreads");
                return poolManagement.adjustPoolSize(action.getTargetPool(), maxThreads, maxThreads);
                
            case REDUCE_QUEUE_SIZE:
                // Queue size adjustment would require pool recreation
                log.warn("Queue size adjustment not implemented for pool: {}", action.getTargetPool());
                return false;
                
            default:
                log.warn("Unknown optimization action type: {}", action.getType());
                return false;
        }
    }
    
    private SystemMetrics collectSystemMetrics() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        double memoryUsage = (double) usedMemory / maxMemory;
        double cpuUsage = getCpuUsage();
        
        return SystemMetrics.builder()
                .cpuUsage(cpuUsage)
                .memoryUsage(memoryUsage)
                .availableCores(runtime.availableProcessors())
                .freeMemory(freeMemory)
                .maxMemory(maxMemory)
                .build();
    }
    
    private double getCpuUsage() {
        try {
            return ((com.sun.management.OperatingSystemMXBean) 
                ManagementFactory.getOperatingSystemMXBean()).getProcessCpuLoad();
        } catch (Exception e) {
            return -1;
        }
    }
    
    @Override
    public DiagnosticInfo getDiagnosticInfo() {
        Map<String, Object> states = new HashMap<>();
        states.put("totalOptimizations", totalOptimizations.get());
        states.put("successfulOptimizations", successfulOptimizations.get());
        states.put("successRate", totalOptimizations.get() > 0 ? 
                (successfulOptimizations.get() * 100.0 / totalOptimizations.get()) : 100.0);
        states.put("maxThreadThreshold", maxThreadThreshold);
        states.put("cpuThreshold", cpuThreshold);
        states.put("memoryThreshold", memoryThreshold);
        
        // Optimization type breakdown
        optimizationsByType.forEach((type, count) -> {
            states.put("optimizations." + type, count.get());
        });
        
        return DiagnosticInfo.builder()
                .component("ThreadOptimizationService")
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
        log.info("Diagnostic mode {} for ThreadOptimizationService", 
                enabled ? "enabled" : "disabled");
    }
    
    /**
     * Optimization result.
     */
    @Data
    @Builder
    public static class OptimizationResult {
        private final long timestamp;
        private final List<OptimizationAction> actions;
        private final boolean success;
        private final SystemMetrics metrics;
    }
    
    /**
     * Optimization action.
     */
    @Data
    @Builder
    public static class OptimizationAction {
        private final ActionType type;
        private final String targetPool;
        private final String description;
        private final Map<String, Object> parameters;
    }
    
    /**
     * System metrics.
     */
    @Data
    @Builder
    public static class SystemMetrics {
        private final double cpuUsage;
        private final double memoryUsage;
        private final int availableCores;
        private final long freeMemory;
        private final long maxMemory;
    }
    
    /**
     * Pool size adjustment.
     */
    @Data
    public static class PoolAdjustment {
        private final int coreSize;
        private final int maxSize;
    }
    
    /**
     * Optimization strategy.
     */
    public enum OptimizationStrategy {
        AGGRESSIVE,  // Reduce resources aggressively
        MODERATE,    // Balanced optimization
        BALANCED,    // Normal operation
        RELAXED      // Allow more resources
    }
    
    /**
     * Action type.
     */
    public enum ActionType {
        REDUCE_POOL_SIZE,
        INCREASE_POOL_SIZE,
        THROTTLE_POOL,
        REDUCE_QUEUE_SIZE,
        SCALE_UP,
        SCALE_DOWN
    }
}