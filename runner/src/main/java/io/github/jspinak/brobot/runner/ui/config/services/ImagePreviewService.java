package io.github.jspinak.brobot.runner.ui.config.services;

import io.github.jspinak.brobot.runner.ui.config.ConfigEntry;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Service for loading and caching image previews.
 */
@Service
public class ImagePreviewService {
    private static final Logger logger = LoggerFactory.getLogger(ImagePreviewService.class);
    
    private final Map<String, Image> imageCache = new ConcurrentHashMap<>();
    private static final int MAX_CACHE_SIZE = 50;
    
    /**
     * Loads an image for preview.
     *
     * @param imageName The name of the image (without extension)
     * @param config The current configuration
     * @return The loaded image, or null if not found
     */
    public Image loadImagePreview(String imageName, ConfigEntry config) {
        if (imageName == null || imageName.isEmpty() || config == null) {
            return null;
        }
        
        // Check cache first
        String cacheKey = config.getName() + ":" + imageName;
        Image cachedImage = imageCache.get(cacheKey);
        if (cachedImage != null) {
            return cachedImage;
        }
        
        // Try to load the image
        try {
            Path imagePath = config.getImagePath().resolve(imageName + ".png");
            if (Files.exists(imagePath)) {
                Image image = new Image(imagePath.toUri().toString());
                
                // Cache the image
                cacheImage(cacheKey, image);
                
                return image;
            } else {
                // Try without extension if the name already includes it
                if (imageName.endsWith(".png")) {
                    imagePath = config.getImagePath().resolve(imageName);
                    if (Files.exists(imagePath)) {
                        Image image = new Image(imagePath.toUri().toString());
                        cacheImage(cacheKey, image);
                        return image;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error loading image preview: " + imageName, e);
        }
        
        return null;
    }
    
    /**
     * Caches an image with size limit management.
     *
     * @param key The cache key
     * @param image The image to cache
     */
    private void cacheImage(String key, Image image) {
        // Simple cache management - clear if too large
        if (imageCache.size() >= MAX_CACHE_SIZE) {
            // Remove oldest entries (simple strategy)
            imageCache.clear();
        }
        
        imageCache.put(key, image);
    }
    
    /**
     * Clears the image cache.
     */
    public void clearCache() {
        imageCache.clear();
    }
    
    /**
     * Removes cached images for a specific configuration.
     *
     * @param configName The configuration name
     */
    public void clearConfigCache(String configName) {
        imageCache.entrySet().removeIf(entry -> entry.getKey().startsWith(configName + ":"));
    }
    
    /**
     * Gets the current cache size.
     *
     * @return The number of cached images
     */
    public int getCacheSize() {
        return imageCache.size();
    }
}