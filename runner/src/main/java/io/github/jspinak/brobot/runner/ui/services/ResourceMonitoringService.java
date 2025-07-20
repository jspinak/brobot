package io.github.jspinak.brobot.runner.ui.services;

import io.github.jspinak.brobot.runner.cache.CacheManager;
import io.github.jspinak.brobot.runner.resources.ImageResourceManager;
import io.github.jspinak.brobot.runner.resources.ResourceManager;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Service responsible for monitoring system resources.
 * Extracted from ResourceMonitorPanel to follow Single Responsibility Principle.
 */
@Slf4j
public class ResourceMonitoringService {
    
    private final ResourceManager resourceManager;
    private final ImageResourceManager imageResourceManager;
    private final CacheManager cacheManager;
    
    private ScheduledExecutorService executor;
    private static final int REFRESH_INTERVAL_SECONDS = 30;
    
    public ResourceMonitoringService(ResourceManager resourceManager,
                                     ImageResourceManager imageResourceManager,
                                     CacheManager cacheManager) {
        this.resourceManager = resourceManager;
        this.imageResourceManager = imageResourceManager;
        this.cacheManager = cacheManager;
    }
    
    /**
     * Start monitoring resources and calling the callback with updates.
     */
    public void startMonitoring(Consumer<ResourceData> updateCallback) {
        if (executor != null && !executor.isShutdown()) {
            return; // Already monitoring
        }
        
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "resource-monitor");
            t.setDaemon(true);
            return t;
        });
        
        executor.scheduleAtFixedRate(() -> {
            try {
                ResourceData data = collectResourceData();
                updateCallback.accept(data);
            } catch (Exception e) {
                log.error("Error collecting resource data", e);
            }
        }, 0, REFRESH_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }
    
    /**
     * Stop monitoring resources.
     */
    public void stopMonitoring() {
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Collect current resource data.
     */
    private ResourceData collectResourceData() {
        ResourceData data = new ResourceData();
        
        // Collect resource counts
        data.setTotalResources(resourceManager.getResourceCount());
        data.setCachedImages((int) imageResourceManager.getCachedImageCount());
        data.setActiveMats((int) imageResourceManager.getActiveMatCount());
        
        // Calculate memory usage
        long memoryCached = imageResourceManager.getCachedMemoryUsage();
        data.setMemoryMB(memoryCached / 1024.0 / 1024.0);
        
        // Collect cache statistics
        data.setCacheStats(cacheManager.getAllCacheStats());
        
        return data;
    }
    
    /**
     * Data class containing current resource state.
     */
    @Data
    public static class ResourceData {
        private int totalResources;
        private int cachedImages;
        private int activeMats;
        private double memoryMB;
        private Map<String, Map<String, Long>> cacheStats;
    }
}