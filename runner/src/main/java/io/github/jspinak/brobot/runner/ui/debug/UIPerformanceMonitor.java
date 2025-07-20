package io.github.jspinak.brobot.runner.ui.debug;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Monitors UI performance metrics.
 * Tracks update times, render performance, and identifies bottlenecks.
 */
@Slf4j
@Component
public class UIPerformanceMonitor {
    
    private final Map<String, PerformanceMetrics> componentMetrics = new ConcurrentHashMap<>();
    private final Map<String, Long> updateTimes = new ConcurrentHashMap<>();
    
    // Thresholds for warnings (in milliseconds)
    private static final long SLOW_UPDATE_THRESHOLD = 100;
    private static final long VERY_SLOW_UPDATE_THRESHOLD = 500;
    
    /**
     * Records the start of a UI update operation.
     * 
     * @param componentId The component being updated
     * @return A token to pass to recordUpdateEnd
     */
    public long recordUpdateStart(String componentId) {
        long startTime = System.currentTimeMillis();
        updateTimes.put(componentId, startTime);
        return startTime;
    }
    
    /**
     * Records the end of a UI update operation.
     * 
     * @param componentId The component that was updated
     * @param startTime The start time returned by recordUpdateStart
     */
    public void recordUpdateEnd(String componentId, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        recordUpdate(componentId, duration);
    }
    
    /**
     * Records a UI update duration.
     * 
     * @param componentId The component ID
     * @param duration The update duration in milliseconds
     */
    public void recordUpdate(String componentId, long duration) {
        PerformanceMetrics metrics = componentMetrics.computeIfAbsent(
            componentId, k -> new PerformanceMetrics()
        );
        
        metrics.recordUpdate(duration);
        
        // Log warnings for slow updates
        if (duration > VERY_SLOW_UPDATE_THRESHOLD) {
            log.error("Very slow UI update in {}: {}ms", componentId, duration);
        } else if (duration > SLOW_UPDATE_THRESHOLD) {
            log.warn("Slow UI update in {}: {}ms", componentId, duration);
        }
    }
    
    /**
     * Gets performance metrics for a specific component.
     * 
     * @param componentId The component ID
     * @return The performance metrics, or null if not found
     */
    public PerformanceMetrics getMetrics(String componentId) {
        return componentMetrics.get(componentId);
    }
    
    /**
     * Gets all performance metrics.
     * 
     * @return Map of component IDs to their metrics
     */
    public Map<String, PerformanceMetrics> getAllMetrics() {
        return new ConcurrentHashMap<>(componentMetrics);
    }
    
    /**
     * Clears all recorded metrics.
     */
    public void clearMetrics() {
        componentMetrics.clear();
        updateTimes.clear();
        log.info("Cleared all performance metrics");
    }
    
    /**
     * Logs a performance report for all monitored components.
     */
    public void logPerformanceReport() {
        log.info("=== UI Performance Report ===");
        
        componentMetrics.entrySet().stream()
            .sorted((a, b) -> Long.compare(
                b.getValue().getAverageUpdateTime(),
                a.getValue().getAverageUpdateTime()
            ))
            .forEach(entry -> {
                String componentId = entry.getKey();
                PerformanceMetrics metrics = entry.getValue();
                
                log.info("Component: {}", componentId);
                log.info("  Updates: {}", metrics.getUpdateCount());
                log.info("  Average: {}ms", metrics.getAverageUpdateTime());
                log.info("  Min: {}ms", metrics.getMinUpdateTime());
                log.info("  Max: {}ms", metrics.getMaxUpdateTime());
                log.info("  Total: {}ms", metrics.getTotalUpdateTime());
            });
        
        log.info("=== End Performance Report ===");
    }
    
    /**
     * Gets components with slow average update times.
     * 
     * @return Map of slow components and their average update times
     */
    public Map<String, Long> getSlowComponents() {
        Map<String, Long> slowComponents = new ConcurrentHashMap<>();
        
        componentMetrics.forEach((id, metrics) -> {
            long avgTime = metrics.getAverageUpdateTime();
            if (avgTime > SLOW_UPDATE_THRESHOLD) {
                slowComponents.put(id, avgTime);
            }
        });
        
        return slowComponents;
    }
    
    /**
     * Performance metrics for a single component.
     */
    public static class PerformanceMetrics {
        private final AtomicLong updateCount = new AtomicLong(0);
        private final AtomicLong totalUpdateTime = new AtomicLong(0);
        private final AtomicLong minUpdateTime = new AtomicLong(Long.MAX_VALUE);
        private final AtomicLong maxUpdateTime = new AtomicLong(0);
        
        /**
         * Records an update duration.
         * 
         * @param duration The update duration in milliseconds
         */
        public void recordUpdate(long duration) {
            updateCount.incrementAndGet();
            totalUpdateTime.addAndGet(duration);
            
            // Update min/max
            minUpdateTime.updateAndGet(current -> Math.min(current, duration));
            maxUpdateTime.updateAndGet(current -> Math.max(current, duration));
        }
        
        public long getUpdateCount() {
            return updateCount.get();
        }
        
        public long getTotalUpdateTime() {
            return totalUpdateTime.get();
        }
        
        public long getMinUpdateTime() {
            long min = minUpdateTime.get();
            return min == Long.MAX_VALUE ? 0 : min;
        }
        
        public long getMaxUpdateTime() {
            return maxUpdateTime.get();
        }
        
        public long getAverageUpdateTime() {
            long count = updateCount.get();
            if (count == 0) return 0;
            return totalUpdateTime.get() / count;
        }
        
        @Override
        public String toString() {
            return String.format(
                "PerformanceMetrics[count=%d, avg=%dms, min=%dms, max=%dms]",
                getUpdateCount(),
                getAverageUpdateTime(),
                getMinUpdateTime(),
                getMaxUpdateTime()
            );
        }
    }
}