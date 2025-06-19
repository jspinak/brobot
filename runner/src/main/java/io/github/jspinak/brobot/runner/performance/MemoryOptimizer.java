package io.github.jspinak.brobot.runner.performance;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.lang.management.*;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class MemoryOptimizer {

    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
    
    private final Map<String, MemoryPool> memoryPools = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(
        r -> new Thread(r, "memory-optimizer")
    );
    
    private volatile double memoryPressureThreshold = 0.85; // 85% heap usage
    private volatile double emergencyThreshold = 0.95; // 95% heap usage
    private volatile boolean aggressiveGcEnabled = false;
    
    private final List<WeakReference<IMemoryReleasable>> releasables = new CopyOnWriteArrayList<>();
    private final AtomicLong lastGcTime = new AtomicLong(System.currentTimeMillis());
    
    @PostConstruct
    public void initialize() {
        log.info("Initializing memory optimizer");
        
        // Start monitoring memory pressure
        scheduler.scheduleAtFixedRate(this::checkMemoryPressure, 5, 5, TimeUnit.SECONDS);
        
        // Configure JVM for better memory management
        configureJvmMemorySettings();
    }
    
    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
    }
    
    private void configureJvmMemorySettings() {
        try {
            // Enable aggressive memory reclamation when needed
            System.setProperty("java.awt.headless", "false"); // Required for JavaFX
            
            // Log current memory settings
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            
            log.info("JVM Memory Configuration:");
            log.info("  Max heap: {} MB", maxMemory / (1024 * 1024));
            log.info("  Total heap: {} MB", totalMemory / (1024 * 1024));
            log.info("  Available processors: {}", runtime.availableProcessors());
            
        } catch (Exception e) {
            log.error("Error configuring JVM memory settings", e);
        }
    }
    
    public void registerMemoryPool(String name, long maxSize) {
        memoryPools.put(name, new MemoryPool(name, maxSize));
    }
    
    public void registerReleasable(IMemoryReleasable releasable) {
        releasables.add(new WeakReference<>(releasable));
        cleanupDeadReferences();
    }
    
    private void cleanupDeadReferences() {
        releasables.removeIf(ref -> ref.get() == null);
    }
    
    private void checkMemoryPressure() {
        try {
            MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
            double usageRatio = (double) heapUsage.getUsed() / heapUsage.getMax();
            
            if (usageRatio > emergencyThreshold) {
                handleEmergencyMemoryPressure();
            } else if (usageRatio > memoryPressureThreshold) {
                handleHighMemoryPressure();
            } else if (usageRatio < 0.5 && aggressiveGcEnabled) {
                // Disable aggressive GC when memory pressure is low
                aggressiveGcEnabled = false;
                log.info("Memory pressure reduced, disabling aggressive GC");
            }
            
        } catch (Exception e) {
            log.error("Error checking memory pressure", e);
        }
    }
    
    private void handleHighMemoryPressure() {
        log.warn("High memory pressure detected, initiating memory optimization");
        
        if (!aggressiveGcEnabled) {
            aggressiveGcEnabled = true;
            log.info("Enabling aggressive GC due to memory pressure");
        }
        
        // Release memory from registered components
        int released = releaseMemory(MemoryPriority.LOW);
        
        // Request garbage collection if we haven't done so recently
        long timeSinceLastGc = System.currentTimeMillis() - lastGcTime.get();
        if (timeSinceLastGc > TimeUnit.SECONDS.toMillis(30)) {
            suggestGarbageCollection();
        }
        
        log.info("Released memory from {} components", released);
    }
    
    private void handleEmergencyMemoryPressure() {
        log.error("Emergency memory pressure detected!");
        
        // Release all possible memory
        int released = releaseMemory(MemoryPriority.CRITICAL);
        
        // Force garbage collection
        System.gc();
        lastGcTime.set(System.currentTimeMillis());
        
        // Clear all memory pools
        memoryPools.values().forEach(MemoryPool::clear);
        
        log.info("Emergency memory release completed, freed {} components", released);
    }
    
    private int releaseMemory(MemoryPriority minPriority) {
        int releasedCount = 0;
        
        for (WeakReference<IMemoryReleasable> ref : releasables) {
            IMemoryReleasable releasable = ref.get();
            if (releasable != null) {
                try {
                    if (releasable.getMemoryPriority().ordinal() >= minPriority.ordinal()) {
                        long freedBytes = releasable.releaseMemory();
                        if (freedBytes > 0) {
                            releasedCount++;
                            log.debug("Released {} bytes from {}", 
                                freedBytes, releasable.getClass().getSimpleName());
                        }
                    }
                } catch (Exception e) {
                    log.error("Error releasing memory from component", e);
                }
            }
        }
        
        return releasedCount;
    }
    
    private void suggestGarbageCollection() {
        log.debug("Suggesting garbage collection");
        System.gc();
        lastGcTime.set(System.currentTimeMillis());
    }
    
    @Scheduled(fixedDelay = 60000) // Every minute
    public void reportMemoryStatus() {
        if (log.isDebugEnabled()) {
            MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
            double usagePercent = (double) heapUsage.getUsed() / heapUsage.getMax() * 100;
            
            StringBuilder report = new StringBuilder();
            report.append(String.format("Memory Status: %.1f%% heap used", usagePercent));
            
            // GC statistics
            long totalGcTime = 0;
            long totalGcCount = 0;
            for (GarbageCollectorMXBean gcBean : gcBeans) {
                totalGcTime += gcBean.getCollectionTime();
                totalGcCount += gcBean.getCollectionCount();
            }
            
            report.append(String.format(" | GC: %d collections, %d ms total", 
                totalGcCount, totalGcTime));
            
            log.debug(report.toString());
        }
    }
    
    public MemoryAllocation allocate(String poolName, long size) {
        MemoryPool pool = memoryPools.get(poolName);
        if (pool == null) {
            throw new IllegalArgumentException("Unknown memory pool: " + poolName);
        }
        
        return pool.allocate(size);
    }
    
    public interface IMemoryReleasable {
        /**
         * Release memory held by this component.
         * @return The approximate number of bytes released
         */
        long releaseMemory();
        
        /**
         * Get the priority of this component's memory.
         * Lower priority memory will be released first.
         */
        MemoryPriority getMemoryPriority();
    }
    
    public enum MemoryPriority {
        LOW,      // Can be released anytime (e.g., caches)
        NORMAL,   // Should be kept if possible (e.g., preloaded data)
        HIGH,     // Should only be released under pressure (e.g., active data)
        CRITICAL  // Only release in emergency (e.g., core functionality)
    }
    
    private static class MemoryPool {
        private final String name;
        private final long maxSize;
        private final AtomicLong currentSize = new AtomicLong();
        private final List<WeakReference<MemoryAllocation>> allocations = new CopyOnWriteArrayList<>();
        
        MemoryPool(String name, long maxSize) {
            this.name = name;
            this.maxSize = maxSize;
        }
        
        MemoryAllocation allocate(long size) {
            if (currentSize.get() + size > maxSize) {
                // Try to free up space
                freeUnusedAllocations();
                
                if (currentSize.get() + size > maxSize) {
                    throw new OutOfMemoryError("Memory pool " + name + " exhausted");
                }
            }
            
            currentSize.addAndGet(size);
            MemoryAllocation allocation = new MemoryAllocation(this, size);
            allocations.add(new WeakReference<>(allocation));
            return allocation;
        }
        
        void release(long size) {
            currentSize.addAndGet(-size);
        }
        
        void clear() {
            allocations.clear();
            currentSize.set(0);
        }
        
        private void freeUnusedAllocations() {
            allocations.removeIf(ref -> {
                MemoryAllocation alloc = ref.get();
                if (alloc == null) {
                    return true;
                }
                if (!alloc.isInUse()) {
                    alloc.release();
                    return true;
                }
                return false;
            });
        }
    }
    
    public static class MemoryAllocation implements AutoCloseable {
        private final MemoryPool pool;
        private final long size;
        private volatile boolean released = false;
        private volatile boolean inUse = true;
        
        MemoryAllocation(MemoryPool pool, long size) {
            this.pool = pool;
            this.size = size;
        }
        
        public void markUnused() {
            inUse = false;
        }
        
        public boolean isInUse() {
            return inUse && !released;
        }
        
        public void release() {
            if (!released) {
                released = true;
                pool.release(size);
            }
        }
        
        @Override
        public void close() {
            release();
        }
    }
}