package io.github.jspinak.brobot.runner.performance;

import lombok.Data;

import io.github.jspinak.brobot.runner.performance.MemoryOptimizer.MemoryPriority;
import io.github.jspinak.brobot.runner.performance.MemoryOptimizer.MemoryReleasable;
import javafx.scene.image.Image;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Component
@Primary
@RequiredArgsConstructor
@Data
public class OptimizedImageResourceManager implements MemoryReleasable {

    private final MemoryOptimizer memoryOptimizer;
    private final PerformanceProfiler profiler;
    
    // Multi-level cache system
    private final Map<String, Image> strongCache = new ConcurrentHashMap<>();
    private final Map<String, SoftReference<Image>> softCache = new ConcurrentHashMap<>();
    private final Map<String, ImageMetadata> metadataCache = new ConcurrentHashMap<>();
    
    // LRU tracking for strong cache
    private final LinkedHashMap<String, Long> accessOrder = new LinkedHashMap<>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Long> eldest) {
            return size() > maxStrongCacheSize;
        }
    };
    
    // Async loading
    private final ExecutorService loadingExecutor = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "image-loader");
        t.setDaemon(true);
        return t;
    });
    
    // Configuration
    private int maxStrongCacheSize = 50;
    private long maxCacheMemoryBytes = 100 * 1024 * 1024; // 100MB
    private final AtomicLong currentCacheSize = new AtomicLong();
    
    // Statistics
    private final AtomicLong cacheHits = new AtomicLong();
    private final AtomicLong cacheMisses = new AtomicLong();
    
    @PostConstruct
    public void initialize() {
        memoryOptimizer.registerReleasable(this);
        log.info("Initialized optimized image resource manager with {}MB cache", 
            maxCacheMemoryBytes / (1024 * 1024));
    }
    
    public CompletableFuture<Image> getImageAsync(String path) {
        return CompletableFuture.supplyAsync(() -> getImage(path), loadingExecutor);
    }
    
    public Image getImage(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        
        try (var timer = profiler.startOperation("image-load-" + getFileName(path))) {
            // Check strong cache first
            Image image = strongCache.get(path);
            if (image != null) {
                cacheHits.incrementAndGet();
                updateAccessOrder(path);
                return image;
            }
            
            // Check soft cache
            SoftReference<Image> softRef = softCache.get(path);
            if (softRef != null) {
                image = softRef.get();
                if (image != null) {
                    cacheHits.incrementAndGet();
                    promoteToStrongCache(path, image);
                    return image;
                } else {
                    // Reference was cleared
                    softCache.remove(path);
                }
            }
            
            // Cache miss - load image
            cacheMisses.incrementAndGet();
            return loadImage(path);
            
        } catch (Exception e) {
            log.error("Error loading image: {}", path, e);
            return null;
        }
    }
    
    private Image loadImage(String path) {
        try {
            // Check metadata cache for size optimization
            ImageMetadata metadata = getOrLoadMetadata(path);
            
            // Load image with appropriate settings
            boolean backgroundLoading = metadata.estimatedSize > 1024 * 1024; // 1MB
            Image image = new Image("file:" + path, 
                0, 0,        // No forced size
                true,        // Preserve ratio
                true,        // Smooth
                backgroundLoading);
            
            if (backgroundLoading) {
                // For large images, wait for loading to complete
                image.progressProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal.doubleValue() >= 1.0) {
                        cacheImage(path, image, metadata);
                    }
                });
            } else {
                cacheImage(path, image, metadata);
            }
            
            return image;
            
        } catch (Exception e) {
            log.error("Failed to load image: {}", path, e);
            return null;
        }
    }
    
    private void cacheImage(String path, Image image, ImageMetadata metadata) {
        long imageSize = estimateImageMemorySize(image);
        
        // Check if we have space in cache
        if (currentCacheSize.get() + imageSize > maxCacheMemoryBytes) {
            evictLeastRecentlyUsed(imageSize);
        }
        
        // Add to appropriate cache
        if (metadata.accessCount > 2 || metadata.isPinned) {
            // Frequently accessed or pinned - use strong cache
            addToStrongCache(path, image, imageSize);
        } else {
            // Infrequently accessed - use soft cache
            softCache.put(path, new SoftReference<>(image));
            currentCacheSize.addAndGet(imageSize);
        }
        
        // Update metadata
        metadata.lastAccess = System.currentTimeMillis();
        metadata.accessCount++;
        metadata.cachedSize = imageSize;
    }
    
    private void addToStrongCache(String path, Image image, long size) {
        strongCache.put(path, image);
        updateAccessOrder(path);
        currentCacheSize.addAndGet(size);
        
        // Check if we need to evict oldest entries
        if (strongCache.size() > maxStrongCacheSize) {
            evictOldestStrongCacheEntry();
        }
    }
    
    private void promoteToStrongCache(String path, Image image) {
        ImageMetadata metadata = metadataCache.get(path);
        if (metadata != null) {
            softCache.remove(path);
            addToStrongCache(path, image, metadata.cachedSize);
        }
    }
    
    private synchronized void updateAccessOrder(String path) {
        accessOrder.put(path, System.currentTimeMillis());
    }
    
    private synchronized void evictOldestStrongCacheEntry() {
        if (accessOrder.isEmpty()) return;
        
        String oldestPath = accessOrder.keySet().iterator().next();
        Image removed = strongCache.remove(oldestPath);
        accessOrder.remove(oldestPath);
        
        if (removed != null) {
            ImageMetadata metadata = metadataCache.get(oldestPath);
            if (metadata != null) {
                currentCacheSize.addAndGet(-metadata.cachedSize);
                // Move to soft cache
                softCache.put(oldestPath, new SoftReference<>(removed));
            }
        }
    }
    
    private void evictLeastRecentlyUsed(long requiredSpace) {
        long freedSpace = 0;
        
        // First, clear soft references
        Iterator<Map.Entry<String, SoftReference<Image>>> softIter = softCache.entrySet().iterator();
        while (softIter.hasNext() && freedSpace < requiredSpace) {
            Map.Entry<String, SoftReference<Image>> entry = softIter.next();
            ImageMetadata metadata = metadataCache.get(entry.getKey());
            
            if (metadata != null && entry.getValue().get() == null) {
                softIter.remove();
                freedSpace += metadata.cachedSize;
                currentCacheSize.addAndGet(-metadata.cachedSize);
            }
        }
        
        // If still need space, evict from strong cache
        if (freedSpace < requiredSpace) {
            List<String> sortedPaths = new ArrayList<>(accessOrder.keySet());
            for (String path : sortedPaths) {
                if (freedSpace >= requiredSpace) break;
                
                ImageMetadata metadata = metadataCache.get(path);
                if (metadata != null && !metadata.isPinned) {
                    strongCache.remove(path);
                    accessOrder.remove(path);
                    freedSpace += metadata.cachedSize;
                    currentCacheSize.addAndGet(-metadata.cachedSize);
                }
            }
        }
    }
    
    private ImageMetadata getOrLoadMetadata(String path) {
        return metadataCache.computeIfAbsent(path, p -> {
            try {
                Path filePath = Path.of(p);
                long fileSize = Files.size(filePath);
                return new ImageMetadata(p, fileSize);
            } catch (IOException e) {
                return new ImageMetadata(p, 0);
            }
        });
    }
    
    private long estimateImageMemorySize(Image image) {
        // Estimate: width * height * 4 bytes per pixel (RGBA)
        return (long) (image.getWidth() * image.getHeight() * 4);
    }
    
    private String getFileName(String path) {
        int lastSlash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    }
    
    public void pinImage(String path) {
        ImageMetadata metadata = metadataCache.get(path);
        if (metadata != null) {
            metadata.isPinned = true;
        }
    }
    
    public void unpinImage(String path) {
        ImageMetadata metadata = metadataCache.get(path);
        if (metadata != null) {
            metadata.isPinned = false;
        }
    }
    
    public void preloadImages(List<String> paths) {
        CompletableFuture<?>[] futures = paths.stream()
            .map(this::getImageAsync)
            .toArray(CompletableFuture[]::new);
            
        CompletableFuture.allOf(futures).thenRun(() -> 
            log.info("Preloaded {} images", paths.size())
        );
    }
    
    @Override
    public long releaseMemory() {
        long releasedBytes = 0;
        
        // Clear soft cache
        for (var entry : softCache.entrySet()) {
            ImageMetadata metadata = metadataCache.get(entry.getKey());
            if (metadata != null) {
                releasedBytes += metadata.cachedSize;
            }
        }
        softCache.clear();
        
        // Clear unpinned entries from strong cache
        List<String> unpinnedPaths = strongCache.keySet().stream()
            .filter(path -> {
                ImageMetadata metadata = metadataCache.get(path);
                return metadata == null || !metadata.isPinned;
            })
            .collect(Collectors.toList());
            
        for (String path : unpinnedPaths) {
            strongCache.remove(path);
            ImageMetadata metadata = metadataCache.get(path);
            if (metadata != null) {
                releasedBytes += metadata.cachedSize;
            }
        }
        
        currentCacheSize.set(0);
        
        log.info("Released {} MB of image cache memory", releasedBytes / (1024 * 1024));
        return releasedBytes;
    }
    
    @Override
    public MemoryPriority getMemoryPriority() {
        return MemoryPriority.LOW; // Image cache can be cleared when needed
    }
    
    public CacheStatistics getStatistics() {
        long hits = cacheHits.get();
        long misses = cacheMisses.get();
        double hitRate = (hits + misses) > 0 ? (double) hits / (hits + misses) * 100 : 0;
        
        return new CacheStatistics(
            strongCache.size(),
            softCache.size(),
            currentCacheSize.get(),
            hits,
            misses,
            hitRate
        );
    }
    
    private static class ImageMetadata {
        final String path;
        final long estimatedSize;
        long lastAccess;
        int accessCount;
        long cachedSize;
        boolean isPinned;
        
        ImageMetadata(String path, long estimatedSize) {
            this.path = path;
            this.estimatedSize = estimatedSize;
            this.lastAccess = System.currentTimeMillis();
            this.accessCount = 0;
            this.cachedSize = 0;
            this.isPinned = false;
        }
    }
    
    public record CacheStatistics(
        int strongCacheSize,
        int softCacheSize,
        long totalCacheBytes,
        long cacheHits,
        long cacheMisses,
        double hitRate
    ) {
        @Override
        public String toString() {
            return String.format(
                "Cache Stats: Strong=%d, Soft=%d, Size=%dMB, Hit Rate=%.1f%% (Hits=%d, Misses=%d)",
                strongCacheSize, softCacheSize, totalCacheBytes / (1024 * 1024),
                hitRate, cacheHits, cacheMisses
            );
        }
    }
}