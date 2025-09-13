package io.github.jspinak.brobot.config.core;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;

import org.sikuli.script.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.config.environment.ExecutionEnvironment;

import lombok.extern.slf4j.Slf4j;

/**
 * Intelligent image loading system that works across all deployment scenarios. Provides transparent
 * JAR handling, caching, and fallback mechanisms.
 */
@Slf4j
@Component
@Order(2) // Created early, but after ImageLoadingInitializer
public class SmartImageLoader {

    private final ImagePathManager pathManager;
    private final ExecutionEnvironment environment;
    private final Map<String, BufferedImage> imageCache = new ConcurrentHashMap<>();
    private final Map<String, LoadResult> loadHistory = new ConcurrentHashMap<>();

    @Autowired
    public SmartImageLoader(ImagePathManager pathManager, ExecutionEnvironment environment) {
        this.pathManager = pathManager;
        this.environment = environment;
        // ImagePathManager will be initialized by ImageLoadingInitializer
    }

    /** Result of an image load attempt with diagnostic information */
    public static class LoadResult {
        private final boolean success;
        private final String loadedFrom;
        private final String failureReason;
        private final Long loadTimeMs;

        private LoadResult(
                boolean success, String loadedFrom, String failureReason, long loadTimeMs) {
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

        public boolean isSuccess() {
            return success;
        }

        public String getLoadedFrom() {
            return loadedFrom;
        }

        public String getFailureReason() {
            return failureReason;
        }

        public Long getLoadTimeMs() {
            return loadTimeMs;
        }
    }

    /**
     * Load an image using intelligent path resolution and caching.
     *
     * @param imageName the name or path of the image to load
     * @return LoadResult containing the result and diagnostic information
     */
    public LoadResult loadImage(String imageName) {
        BufferedImage image = loadImageInternal(imageName);
        return loadHistory.get(imageName);
    }

    /**
     * Load an image and return the BufferedImage directly. For backward compatibility with existing
     * tests.
     *
     * @param imageName the name or path of the image to load
     * @return the loaded BufferedImage, or null if loading failed
     */
    public BufferedImage loadImageDirect(String imageName) {
        return loadImageInternal(imageName);
    }

    /**
     * Load an image using intelligent path resolution and caching.
     *
     * @param imageName the name or path of the image to load
     * @return the loaded BufferedImage, or a placeholder in mock mode
     */
    private BufferedImage loadImageInternal(String imageName) {
        long startTime = System.currentTimeMillis();

        // Check cache first
        BufferedImage cached = imageCache.get(imageName);
        if (cached != null) {
            log.debug("Image loaded from cache: {}", imageName);
            loadHistory.put(imageName, LoadResult.success("cache", 0));
            return cached;
        }

        // In mock mode, log and return null
        if (environment.isMockMode()) {
            log.warn("⚠️ Mock mode active - Image loading skipped: {}", imageName);
            log.warn("   To load real images, run without mock profile");
            loadHistory.put(
                    imageName,
                    LoadResult.failure(
                            "Mock mode - no real images", System.currentTimeMillis() - startTime));
            return null;
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
                result =
                        LoadResult.success(
                                "configured_path", System.currentTimeMillis() - startTime);
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
        if (image == null
                && (imageName.startsWith("http://") || imageName.startsWith("https://"))) {
            image = tryLoadFromURL(imageName);
            if (image != null) {
                result = LoadResult.success("url", System.currentTimeMillis() - startTime);
            }
        }

        // 5. Try JAR extraction
        if (image == null) {
            image = tryLoadFromJar(imageName);
            if (image != null) {
                result =
                        LoadResult.success(
                                "jar_extraction", System.currentTimeMillis() - startTime);
            }
        }

        // If all strategies failed, log error and return null
        if (image == null) {
            log.error("❌ IMAGE NOT FOUND: {}", imageName);
            log.error("   Searched in:");
            for (String path : pathManager.getConfiguredPaths()) {
                log.error("     - {}", path);
            }
            String resolvedPath = System.getProperty("brobot.resolved.image.path");
            if (resolvedPath != null) {
                log.error("     - {} (resolved path)", resolvedPath);
            }
            log.error(
                    "   Please ensure the image file exists with .png, .jpg, .jpeg, .gif, or .bmp"
                            + " extension");

            // Return null instead of placeholder - let the caller handle the missing image
            result =
                    LoadResult.failure(
                            "Image not found: " + imageName,
                            System.currentTimeMillis() - startTime);
            loadHistory.put(imageName, result);
            return null;
        }

        // Cache the result
        imageCache.put(imageName, image);
        loadHistory.put(imageName, result);

        log.debug(
                "Image '{}' loaded from {} in {}ms",
                imageName,
                result.getLoadedFrom(),
                result.getLoadTimeMs());
        return image;
    }

    /**
     * Load an image and wrap it in a SikuliX Image object.
     *
     * @param imageName the name or path of the image to load
     * @return SikuliX Image object, or null if image not found
     */
    public Image loadSikuliImage(String imageName) {
        BufferedImage bufferedImage = loadImageInternal(imageName);
        if (bufferedImage == null) {
            log.error("Cannot create SikuliX Image for missing file: {}", imageName);
            return null;
        }
        return new Image(bufferedImage, imageName);
    }

    /** Clear the image cache */
    public void clearCache() {
        imageCache.clear();
        log.info("Image cache cleared");
    }

    /**
     * Get an image from cache if it exists
     *
     * @param imageName the name of the image
     * @return the cached BufferedImage or null if not cached
     */
    public BufferedImage getFromCache(String imageName) {
        return imageCache.get(imageName);
    }

    /** Get diagnostic information about image loading */
    public Map<String, Object> getDiagnostics() {
        Map<String, Object> diagnostics = new ConcurrentHashMap<>();
        diagnostics.put("cachedImages", imageCache.size());
        diagnostics.put("loadHistory", loadHistory);
        diagnostics.put("pathManagerDiagnostics", pathManager.getDiagnostics());
        return diagnostics;
    }

    /** Provide suggestions for fixing image loading issues */
    public List<String> getSuggestionsForFailure(String imageName) {
        List<String> suggestions = new ArrayList<>();
        LoadResult result = loadHistory.get(imageName);

        if (result == null || result.isSuccess()) {
            suggestions.add("No failure recorded for image: " + imageName);
            return suggestions;
        }

        suggestions.add("Image loading failed for: " + imageName);
        suggestions.add("Reason: " + result.getFailureReason());

        // Check if image paths are configured
        if (pathManager.getConfiguredPaths().isEmpty()) {
            suggestions.add(
                    "No image paths configured. Set brobot.core.image-path in"
                            + " application.properties");
        } else {
            suggestions.add("Configured paths: " + pathManager.getConfiguredPaths());
            suggestions.add("Ensure your image exists in one of these locations.");
        }

        suggestions.add("Check image filename and extension (png, jpg, jpeg, gif, bmp supported)");
        suggestions.add("If running from JAR, ensure images are in external directory");
        suggestions.add("Try using absolute path to test: /full/path/to/image.png");

        return suggestions;
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
        // First, try the resolved path if available
        String resolvedPath = System.getProperty("brobot.resolved.image.path");
        if (resolvedPath != null) {
            log.info("Trying resolved path: {} for image: {}", resolvedPath, imageName);
            BufferedImage img = tryLoadFromSpecificPath(resolvedPath, imageName);
            if (img != null) {
                log.info("✅ Loaded {} from resolved path: {}", imageName, resolvedPath);
                return img;
            }
        }

        // Then try other configured paths
        List<String> paths = pathManager.getConfiguredPaths();
        log.info("Trying {} configured paths for image: {}", paths.size(), imageName);
        for (String configuredPath : paths) {
            log.info("Trying configured path: {} for image: {}", configuredPath, imageName);
            BufferedImage img = tryLoadFromSpecificPath(configuredPath, imageName);
            if (img != null) {
                log.info("✅ Loaded {} from configured path: {}", imageName, configuredPath);
                return img;
            }
        }

        return null;
    }

    private BufferedImage tryLoadFromSpecificPath(String basePath, String imageName) {
        try {
            Path fullPath = Paths.get(basePath, imageName);
            log.trace("Trying direct path: {}", fullPath);
            if (Files.exists(fullPath) && Files.isRegularFile(fullPath)) {
                log.debug("Found image at: {}", fullPath);
                return ImageIO.read(fullPath.toFile());
            }

            // Also try without extension and common extensions
            String nameWithoutExt = removeExtension(imageName);
            for (String ext : new String[] {".png", ".jpg", ".jpeg", ".gif", ".bmp"}) {
                fullPath = Paths.get(basePath, nameWithoutExt + ext);
                log.trace("Trying path with extension: {}", fullPath);
                if (Files.exists(fullPath) && Files.isRegularFile(fullPath)) {
                    log.debug("Found image at: {}", fullPath);
                    return ImageIO.read(fullPath.toFile());
                }
            }
        } catch (Exception e) {
            log.debug("Could not load from path: {} - {}", basePath, e.getMessage());
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
            URL url = new java.net.URI(urlString).toURL();
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

    private String removeExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0) {
            return filename.substring(0, lastDot);
        }
        return filename;
    }
}
