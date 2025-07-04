package io.github.jspinak.brobot.runner.resources;

import lombok.Data;

import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import jakarta.annotation.PostConstruct;
import org.bytedeco.opencv.opencv_core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Data
public class ImageResourceManager implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(ImageResourceManager.class);

    private final ResourceManager resourceManager;
    private final EventBus eventBus;
    private final BrobotRunnerProperties properties;

    // Use SoftReferences to allow GC to collect when memory is low
    private final Map<String, SoftReference<BufferedImage>> imageCache = new ConcurrentHashMap<>();
    private final Map<String, Long> imageSizes = new ConcurrentHashMap<>();
    private final AtomicLong cachedMemoryUsed = new AtomicLong(0);
    private final long memoryThreshold;
    private final Set<Mat> activeMats = ConcurrentHashMap.newKeySet();

    private final ScheduledExecutorService memoryMonitor;

    public ImageResourceManager(ResourceManager resourceManager,
                                EventBus eventBus,
                                BrobotRunnerProperties properties) {
        this.resourceManager = resourceManager;
        this.eventBus = eventBus;
        this.properties = properties;

        // Set threshold to 70% of max heap or 500MB, whichever is less
        this.memoryThreshold = Math.min(
                Runtime.getRuntime().maxMemory() * 7 / 10,
                500 * 1024 * 1024
        );

        // Schedule memory monitoring
        this.memoryMonitor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Image-Memory-Monitor");
            t.setDaemon(true);
            return t;
        });

        resourceManager.registerResource(this, "ImageResourceManager");
    }

    @PostConstruct
    public void initialize() {
        // Start memory monitoring after initialization
        this.memoryMonitor.scheduleAtFixedRate(
                this::checkMemoryUsage, 30, 30, TimeUnit.SECONDS);

        eventBus.publish(LogEvent.info(this,
                "Image resource manager initialized with memory threshold: " +
                        (memoryThreshold / 1024 / 1024) + "MB",
                "Resources"));
    }

    /**
     * Retrieves a cached image or loads it from disk
     */
    public BufferedImage getBufferedImage(String imageName) {
        String key = normalizeImageName(imageName);
        SoftReference<BufferedImage> ref = imageCache.get(key);

        if (ref != null) {
            BufferedImage image = ref.get();
            if (image != null) {
                eventBus.publish(LogEvent.debug(this, "Image cache hit: " + key, "ImageCache"));
                return image;
            }
            // Reference was cleared by GC
            imageCache.remove(key);
            imageSizes.remove(key);
            eventBus.publish(LogEvent.debug(this, "Image cache reference cleared: " + key, "ImageCache"));
        }

        // Load image from disk
        BufferedImage image = loadImageFromDisk(key);
        if (image != null) {
            cacheImage(key, image);
        }

        return image;
    }

    /**
     * Caches an image and tracks its memory usage
     */
    public void cacheImage(String key, BufferedImage image) {
        if (image == null) return;

        // Estimate size - width * height * bytes per pixel (4 for RGBA)
        long estimatedSize = (long) image.getWidth() * image.getHeight() * 4;

        // Adjust the cached memory usage counter
        Long previousSize = imageSizes.put(key, estimatedSize);
        if (previousSize != null) {
            cachedMemoryUsed.addAndGet(estimatedSize - previousSize);
        } else {
            cachedMemoryUsed.addAndGet(estimatedSize);
        }

        // Cache the image with a soft reference
        imageCache.put(key, new SoftReference<>(image));

        eventBus.publish(LogEvent.debug(this,
                String.format("Image cached: %s (%.2f KB, total: %.2f MB)",
                        key, estimatedSize/1024.0, cachedMemoryUsed.get()/1024.0/1024.0),
                "ImageCache"));

        // Check if we need to clean up
        if (cachedMemoryUsed.get() > memoryThreshold) {
            triggerCleanup();
        }
    }

    /**
     * Registers a Mat for tracking and cleanup
     */
    public void registerMat(Mat mat) {
        if (mat != null && !mat.isNull()) {
            activeMats.add(mat);
            eventBus.publish(LogEvent.debug(this,
                    "Mat registered, active mats: " + activeMats.size(), "Resources"));
        }
    }

    /**
     * Releases a Mat and removes it from tracking
     */
    public void releaseMat(Mat mat) {
        if (mat != null && !mat.isNull()) {
            mat.release();
            activeMats.remove(mat);
            eventBus.publish(LogEvent.debug(this,
                    "Mat released, active mats: " + activeMats.size(), "Resources"));
        }
    }

    /**
     * Updates cached image references from a StateImage
     */
    public void updateStateImageCache(StateImage stateImage) {
        if (stateImage == null) return;

        for (Pattern pattern : stateImage.getPatterns()) {
            if (pattern.isEmpty()) continue;

            BufferedImage bImg = pattern.getBImage();
            String name = pattern.getName();

            if (bImg != null && name != null && !name.isEmpty()) {
                cacheImage(name, bImg);
            }
        }
    }

    private String normalizeImageName(String imageName) {
        if (imageName == null) return "";

        // If the name has a path, extract just the filename
        String filename = Paths.get(imageName).getFileName().toString();

        // Remove extension if present
        int extIndex = filename.lastIndexOf('.');
        if (extIndex > 0) {
            filename = filename.substring(0, extIndex);
        }

        return filename;
    }

    private BufferedImage loadImageFromDisk(String imageName) {
        try {
            Path imagePath = Paths.get(properties.getImagePath(), imageName + ".png");

            // Use reflection to access the Brobot implementation
            // Note: In a real implementation we would use a more direct approach
            // by accessing the appropriate classes
            Class<?> bufferImageOpsClass = Class.forName("io.github.jspinak.brobot.imageUtils.BufferedImageOps");
            java.lang.reflect.Method method = bufferImageOpsClass.getMethod("getBuffImgFromFile", String.class);
            BufferedImage image = (BufferedImage) method.invoke(null, imagePath.toString());

            if (image != null) {
                eventBus.publish(LogEvent.debug(this,
                        "Image loaded from disk: " + imageName, "ImageCache"));
            } else {
                eventBus.publish(LogEvent.warning(this,
                        "Failed to load image from disk: " + imageName, "ImageCache"));
            }

            return image;
        } catch (Exception e) {
            eventBus.publish(LogEvent.error(this,
                    "Error loading image: " + imageName + ", " + e.getMessage(), "ImageCache", e));
            return null;
        }
    }

    private void checkMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        double usedPercent = 100.0 * usedMemory / runtime.maxMemory();

        eventBus.publish(LogEvent.debug(this,
                String.format("Memory usage: %.1f%% (%.2f MB used, %.2f MB cached)",
                        usedPercent,
                        usedMemory / 1024.0 / 1024.0,
                        cachedMemoryUsed.get() / 1024.0 / 1024.0),
                "Memory"));

        if (usedMemory > memoryThreshold) {
            triggerCleanup();
        }
    }

    private void triggerCleanup() {
        eventBus.publish(LogEvent.warning(this,
                String.format("Memory usage high (%.2f MB cached), triggering cleanup",
                        cachedMemoryUsed.get() / 1024.0 / 1024.0),
                "Memory"));

        // Free at least 30% of the cache
        long targetReduction = cachedMemoryUsed.get() * 3 / 10;
        AtomicLong freed = new AtomicLong();

        // Clear some entries from the cache, prioritizing larger images
        Set<Map.Entry<String, Long>> entries = new HashSet<>(imageSizes.entrySet());
        entries.stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()) // Largest first
                .limit(Math.max(3, entries.size() / 5)) // Clear top 20% of large images, at least 3
                .forEach(entry -> {
                    imageCache.remove(entry.getKey());
                    freed.addAndGet(entry.getValue());
                });

        // Update memory usage tracker
        cachedMemoryUsed.addAndGet(-freed.get());

        eventBus.publish(LogEvent.info(this,
                String.format("Memory cleanup completed: %.2f MB freed", freed.get() / 1024.0 / 1024.0),
                "Memory"));

        // Request garbage collection
        System.gc();
    }

    @Override
    public void close() {
        memoryMonitor.shutdown();

        // Release all tracked Mat objects
        for (Mat mat : activeMats) {
            try {
                if (mat != null && !mat.isNull()) {
                    mat.release();
                }
            } catch (Exception e) {
                logger.error("Error releasing Mat during shutdown", e);
            }
        }
        activeMats.clear();

        // Clear cache
        imageCache.clear();
        imageSizes.clear();
        cachedMemoryUsed.set(0);

        eventBus.publish(LogEvent.info(this, "Image resource manager shutdown", "Resources"));
    }

    public long getCachedImageCount() {
        return imageCache.size();
    }

    public long getCachedMemoryUsage() {
        return cachedMemoryUsed.get();
    }

    public long getActiveMatCount() {
        return activeMats.size();
    }
}