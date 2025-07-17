package io.github.jspinak.brobot.config;

import lombok.extern.slf4j.Slf4j;
import org.sikuli.script.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Intelligent image loading system that works across all deployment scenarios.
 * Provides transparent JAR handling, caching, and fallback mechanisms.
 */
@Slf4j
@Component
public class SmartImageLoader {
    
    private final ImagePathManager pathManager;
    private final ExecutionEnvironment environment;
    private final Map<String, BufferedImage> imageCache = new ConcurrentHashMap<>();
    private final Map<String, LoadResult> loadHistory = new ConcurrentHashMap<>();
    
    @Autowired
    public SmartImageLoader(ImagePathManager pathManager, ExecutionEnvironment environment) {
        this.pathManager = pathManager;
        this.environment = environment;
    }
    
    /**
     * Result of an image load attempt with diagnostic information
     */
    public static class LoadResult {
        public final boolean success;
        public final String loadedFrom;
        public final String failureReason;
        public final long loadTimeMs;
        
        private LoadResult(boolean success, String loadedFrom, String failureReason, long loadTimeMs) {
            this.success = success;
            this.loadedFrom = loadedFrom;
            this.failureReason = failureReason;
            this.loadTimeMs = loadTimeMs;
        }
        
        public static LoadResult success(String loadedFrom, long loadTimeMs) {
            return new LoadResult(true, loadedFrom, null, loadTimeMs);
        }
        
        public static LoadResult failure(String reason, long loadTimeMs) {
            return new LoadResult(false, null, reason, loadTimeMs);
        }
    }
    
    /**
     * Load an image using intelligent path resolution and caching.
     * 
     * @param imageName the name or path of the image to load
     * @return the loaded BufferedImage, or a placeholder in mock mode
     */
    public BufferedImage loadImage(String imageName) {
        long startTime = System.currentTimeMillis();
        
        // Check cache first
        BufferedImage cached = imageCache.get(imageName);
        if (cached != null) {
            log.debug("Image loaded from cache: {}", imageName);
            loadHistory.put(imageName, LoadResult.success("cache", 0));
            return cached;
        }
        
        // In mock mode, return placeholder
        if (environment.isMockMode()) {
            BufferedImage placeholder = createPlaceholder(imageName);
            imageCache.put(imageName, placeholder);
            loadHistory.put(imageName, LoadResult.success("mock", System.currentTimeMillis() - startTime));
            return placeholder;
        }
        
        // Try loading strategies in order
        BufferedImage image = null;
        LoadResult result = null;
        
        // 1. Try direct file path
        image = tryLoadFromFile(imageName);
        if (image != null) {
            result = LoadResult.success("file", System.currentTimeMillis() - startTime);
        }
        
        // 2. Try configured paths
        if (image == null) {
            image = tryLoadFromConfiguredPaths(imageName);
            if (image != null) {
                result = LoadResult.success("configured_path", System.currentTimeMillis() - startTime);
            }
        }
        
        // 3. Try classpath
        if (image == null) {
            image = tryLoadFromClasspath(imageName);
            if (image != null) {
                result = LoadResult.success("classpath", System.currentTimeMillis() - startTime);
            }
        }
        
        // 4. Try URL if it looks like one
        if (image == null && (imageName.startsWith("http://") || imageName.startsWith("https://"))) {
            image = tryLoadFromURL(imageName);
            if (image != null) {
                result = LoadResult.success("url", System.currentTimeMillis() - startTime);
            }
        }
        
        // 5. Try JAR extraction
        if (image == null) {
            image = tryLoadFromJar(imageName);
            if (image != null) {
                result = LoadResult.success("jar_extraction", System.currentTimeMillis() - startTime);
            }
        }
        
        // If all strategies failed, create placeholder and log error
        if (image == null) {
            log.debug("Image not found: {} - using placeholder", imageName);
            image = createPlaceholder(imageName);
            result = LoadResult.failure("All load strategies failed", System.currentTimeMillis() - startTime);
        }
        
        // Cache the result
        imageCache.put(imageName, image);
        loadHistory.put(imageName, result);
        
        log.debug("Image '{}' loaded from {} in {}ms", imageName, result.loadedFrom, result.loadTimeMs);
        return image;
    }
    
    /**
     * Load an image and wrap it in a SikuliX Image object.
     * 
     * @param imageName the name or path of the image to load
     * @return SikuliX Image object
     */
    public Image loadSikuliImage(String imageName) {
        BufferedImage bufferedImage = loadImage(imageName);
        return new Image(bufferedImage, imageName);
    }
    
    /**
     * Clear the image cache
     */
    public void clearCache() {
        imageCache.clear();
        log.info("Image cache cleared");
    }
    
    /**
     * Get diagnostic information about image loading
     */
    public Map<String, Object> getDiagnostics() {
        Map<String, Object> diagnostics = new ConcurrentHashMap<>();
        diagnostics.put("cachedImages", imageCache.size());
        diagnostics.put("loadHistory", loadHistory);
        diagnostics.put("pathManagerDiagnostics", pathManager.getDiagnostics());
        return diagnostics;
    }
    
    /**
     * Provide suggestions for fixing image loading issues
     */
    public String getSuggestionsForFailure(String imageName) {
        LoadResult result = loadHistory.get(imageName);
        if (result == null || result.success) {
            return "No failure recorded for image: " + imageName;
        }
        
        StringBuilder suggestions = new StringBuilder();
        suggestions.append("Image loading failed for: ").append(imageName).append("\n");
        suggestions.append("Reason: ").append(result.failureReason).append("\n\n");
        suggestions.append("Suggestions:\n");
        
        // Check if image paths are configured
        if (pathManager.getConfiguredPaths().isEmpty()) {
            suggestions.append("1. No image paths configured. Set brobot.core.image-path in application.properties\n");
        } else {
            suggestions.append("1. Configured paths: ").append(pathManager.getConfiguredPaths()).append("\n");
            suggestions.append("   Ensure your image exists in one of these locations.\n");
        }
        
        suggestions.append("2. Check image filename and extension (png, jpg, jpeg, gif, bmp supported)\n");
        suggestions.append("3. If running from JAR, ensure images are in external directory\n");
        suggestions.append("4. Try using absolute path to test: /full/path/to/image.png\n");
        
        return suggestions.toString();
    }
    
    private BufferedImage tryLoadFromFile(String imageName) {
        try {
            Path path = Paths.get(imageName);
            if (Files.exists(path) && Files.isRegularFile(path)) {
                return ImageIO.read(path.toFile());
            }
        } catch (Exception e) {
            log.debug("Failed to load as direct file: {}", e.getMessage());
        }
        return null;
    }
    
    private BufferedImage tryLoadFromConfiguredPaths(String imageName) {
        for (String configuredPath : pathManager.getConfiguredPaths()) {
            try {
                Path fullPath = Paths.get(configuredPath, imageName);
                if (Files.exists(fullPath) && Files.isRegularFile(fullPath)) {
                    return ImageIO.read(fullPath.toFile());
                }
                
                // Also try without extension and common extensions
                String nameWithoutExt = removeExtension(imageName);
                for (String ext : new String[]{".png", ".jpg", ".jpeg", ".gif", ".bmp"}) {
                    fullPath = Paths.get(configuredPath, nameWithoutExt + ext);
                    if (Files.exists(fullPath) && Files.isRegularFile(fullPath)) {
                        return ImageIO.read(fullPath.toFile());
                    }
                }
            } catch (Exception e) {
                log.debug("Failed to load from configured path {}: {}", configuredPath, e.getMessage());
            }
        }
        return null;
    }
    
    private BufferedImage tryLoadFromClasspath(String imageName) {
        try {
            // Try with leading slash
            InputStream stream = getClass().getResourceAsStream("/" + imageName);
            if (stream == null) {
                // Try without leading slash
                stream = getClass().getResourceAsStream(imageName);
            }
            if (stream == null) {
                // Try with images prefix
                stream = getClass().getResourceAsStream("/images/" + imageName);
            }
            
            if (stream != null) {
                try (InputStream is = stream) {
                    return ImageIO.read(is);
                }
            }
        } catch (Exception e) {
            log.debug("Failed to load from classpath: {}", e.getMessage());
        }
        return null;
    }
    
    private BufferedImage tryLoadFromURL(String urlString) {
        try {
            URL url = new URL(urlString);
            return ImageIO.read(url);
        } catch (Exception e) {
            log.debug("Failed to load from URL: {}", e.getMessage());
        }
        return null;
    }
    
    private BufferedImage tryLoadFromJar(String imageName) {
        try {
            // Extract images from JAR if needed
            Path extracted = pathManager.extractImagesFromJar("images");
            if (extracted != null) {
                Path imagePath = extracted.resolve(imageName);
                if (Files.exists(imagePath)) {
                    return ImageIO.read(imagePath.toFile());
                }
            }
        } catch (Exception e) {
            log.debug("Failed to load from JAR extraction: {}", e.getMessage());
        }
        return null;
    }
    
    private BufferedImage createPlaceholder(String imageName) {
        // Create a simple placeholder image
        int width = 100;
        int height = 100;
        BufferedImage placeholder = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        // Fill with a pattern to make it obvious it's a placeholder
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int color = ((x + y) % 20 < 10) ? 0xCCCCCC : 0x999999;
                placeholder.setRGB(x, y, color);
            }
        }
        
        log.debug("Created placeholder for: {}", imageName);
        return placeholder;
    }
    
    private String removeExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0) {
            return filename.substring(0, lastDot);
        }
        return filename;
    }
}