package io.github.jspinak.brobot.diagnostics;

import io.github.jspinak.brobot.config.ImagePathManager;
import io.github.jspinak.brobot.config.SmartImageLoader;
import io.github.jspinak.brobot.model.element.Image;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sikuli.script.ImagePath;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Diagnostic runner for testing image loading capabilities.
 * Enabled via property: brobot.diagnostics.image-loading.enabled=true
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "brobot.diagnostics.image-loading.enabled", havingValue = "true")
public class ImageLoadingDiagnosticsRunner {

    private final SmartImageLoader smartImageLoader;
    private final ImagePathManager imagePathManager;
    
    @Value("${brobot.diagnostics.image-loading.test-images:}")
    private List<String> testImages = new ArrayList<>();
    
    @Value("${brobot.diagnostics.image-loading.verbose:true}")
    private boolean verbose;
    
    @Value("${brobot.diagnostics.image-loading.test-all-images:false}")
    private boolean testAllImages;

    @PostConstruct
    public void runDiagnostics() {
        log.info("=== Brobot Image Loading Diagnostics ===");
        
        // 1. Environment Information
        printEnvironmentInfo();
        
        // 2. Path Configuration
        printPathConfiguration();
        
        // 3. Directory Structure
        printDirectoryStructure();
        
        // 4. Test Specific Images
        if (!testImages.isEmpty()) {
            testSpecificImages();
        }
        
        // 5. Test All Found Images (if enabled)
        if (testAllImages) {
            testAllFoundImages();
        }
        
        // 6. Performance Report
        printPerformanceReport();
        
        // 7. Recommendations
        printRecommendations();
        
        log.info("=== Image Loading Diagnostics Complete ===");
    }
    
    private void printEnvironmentInfo() {
        log.info("\n=== Environment Information ===");
        log.info("Working Directory: {}", System.getProperty("user.dir"));
        log.info("Java Version: {}", System.getProperty("java.version"));
        log.info("OS: {} {}", System.getProperty("os.name"), System.getProperty("os.version"));
        log.info("Display Available: {}", !java.awt.GraphicsEnvironment.isHeadless());
    }
    
    private void printPathConfiguration() {
        log.info("\n=== Path Configuration ===");
        
        // SmartImageLoader paths
        Map<String, Object> pathDiagnostics = imagePathManager.getDiagnostics();
        log.info("ImagePathManager Status:");
        pathDiagnostics.forEach((key, value) -> {
            if (value instanceof List) {
                List<?> list = (List<?>) value;
                log.info("  {}: {} entries", key, list.size());
                if (verbose && !list.isEmpty()) {
                    list.forEach(item -> log.info("    - {}", item));
                }
            } else {
                log.info("  {}: {}", key, value);
            }
        });
        
        // SikuliX paths
        log.info("\nSikuliX ImagePath:");
        log.info("  Bundle Path: {}", ImagePath.getBundlePath());
        List<ImagePath.PathEntry> sikuliPaths = ImagePath.getPaths();
        log.info("  Configured Paths: {} total", sikuliPaths.size());
        if (verbose) {
            sikuliPaths.forEach(entry -> log.info("    - {}", entry.getPath()));
        }
    }
    
    private void printDirectoryStructure() {
        log.info("\n=== Directory Structure ===");
        
        List<String> imageDirs = imagePathManager.getAllPaths();
        for (String dirPath : imageDirs) {
            File dir = new File(dirPath);
            if (dir.exists() && dir.isDirectory()) {
                log.info("Directory: {}", dir.getAbsolutePath());
                if (verbose) {
                    listImageFiles(dir, "  ");
                } else {
                    int imageCount = countImageFiles(dir);
                    log.info("  Contains {} image files", imageCount);
                }
            } else {
                log.info("Directory: {} (NOT FOUND)", dirPath);
            }
        }
    }
    
    private void listImageFiles(File dir, String indent) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    log.info("{}[{}]", indent, file.getName());
                    listImageFiles(file, indent + "  ");
                } else if (isImageFile(file)) {
                    log.info("{}{}", indent, file.getName());
                }
            }
        }
    }
    
    private int countImageFiles(File dir) {
        int count = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    count += countImageFiles(file);
                } else if (isImageFile(file)) {
                    count++;
                }
            }
        }
        return count;
    }
    
    private boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".png") || name.endsWith(".jpg") || 
               name.endsWith(".jpeg") || name.endsWith(".gif") || 
               name.endsWith(".bmp");
    }
    
    private void testSpecificImages() {
        log.info("\n=== Testing Specific Images ===");
        
        for (String imageName : testImages) {
            testImageLoading(imageName);
        }
    }
    
    private void testAllFoundImages() {
        log.info("\n=== Testing All Found Images ===");
        
        List<String> allImages = new ArrayList<>();
        for (String dirPath : imagePathManager.getAllPaths()) {
            File dir = new File(dirPath);
            if (dir.exists() && dir.isDirectory()) {
                collectImageFiles(dir, dirPath, allImages);
            }
        }
        
        log.info("Found {} total images to test", allImages.size());
        int successful = 0;
        int failed = 0;
        
        for (String imagePath : allImages) {
            SmartImageLoader.LoadResult result = testImageLoadingQuietly(imagePath);
            if (result.isSuccess()) {
                successful++;
            } else {
                failed++;
                if (verbose) {
                    log.warn("  Failed: {} - {}", imagePath, result.getFailureReason());
                }
            }
        }
        
        log.info("Test Results: {} successful, {} failed", successful, failed);
    }
    
    private void collectImageFiles(File dir, String basePath, List<String> images) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    collectImageFiles(file, basePath, images);
                } else if (isImageFile(file)) {
                    String relativePath = file.getAbsolutePath().substring(basePath.length());
                    if (relativePath.startsWith(File.separator)) {
                        relativePath = relativePath.substring(1);
                    }
                    images.add(relativePath);
                }
            }
        }
    }
    
    private void testImageLoading(String imageName) {
        log.info("\nTesting: '{}'", imageName);
        
        long startTime = System.currentTimeMillis();
        SmartImageLoader.LoadResult result = smartImageLoader.loadImage(imageName);
        long loadTime = System.currentTimeMillis() - startTime;
        
        if (result.isSuccess()) {
            log.info("  ✓ SUCCESS - Loaded from: {} in {}ms", result.getLoadedFrom(), loadTime);
            
            if (verbose) {
                BufferedImage image = smartImageLoader.getFromCache(imageName);
                if (image != null) {
                    log.info("  Image dimensions: {}x{}", image.getWidth(), image.getHeight());
                }
            }
        } else {
            log.warn("  ✗ FAILED - {}", result.getFailureReason());
            
            // Print suggestions
            List<String> suggestions = smartImageLoader.getSuggestionsForFailure(imageName);
            if (!suggestions.isEmpty()) {
                log.info("  Suggestions:");
                suggestions.forEach(s -> log.info("    - {}", s));
            }
        }
    }
    
    private SmartImageLoader.LoadResult testImageLoadingQuietly(String imageName) {
        return smartImageLoader.loadImage(imageName);
    }
    
    private void printPerformanceReport() {
        log.info("\n=== Performance Report ===");
        
        Map<String, Object> diagnostics = smartImageLoader.getDiagnostics();
        Map<String, SmartImageLoader.LoadResult> loadHistory = 
            (Map<String, SmartImageLoader.LoadResult>) diagnostics.get("loadHistory");
        
        if (loadHistory.isEmpty()) {
            log.info("No images loaded yet");
            return;
        }
        
        // Calculate statistics
        long totalLoadTime = 0;
        long cacheHits = 0;
        long fileLoads = 0;
        long failures = 0;
        
        for (SmartImageLoader.LoadResult result : loadHistory.values()) {
            if (result.getLoadTimeMs() != null) {
                totalLoadTime += result.getLoadTimeMs();
            }
            
            if (result.isSuccess()) {
                switch (result.getLoadedFrom()) {
                    case "cache":
                        cacheHits++;
                        break;
                    case "file":
                    case "configured_path":
                    case "classpath":
                    case "jar_extraction":
                        fileLoads++;
                        break;
                }
            } else {
                failures++;
            }
        }
        
        log.info("Total Load Attempts: {}", loadHistory.size());
        log.info("Successful Loads: {}", loadHistory.size() - failures);
        log.info("Failed Loads: {}", failures);
        log.info("Cache Hits: {}", cacheHits);
        log.info("File/Resource Loads: {}", fileLoads);
        
        if (loadHistory.size() > 0) {
            double avgLoadTime = (double) totalLoadTime / loadHistory.size();
            log.info("Average Load Time: {:.2f}ms", avgLoadTime);
        }
        
        log.info("Current Cache Size: {}", diagnostics.get("cachedImages"));
    }
    
    private void printRecommendations() {
        log.info("\n=== Recommendations ===");
        
        Map<String, Object> diagnostics = smartImageLoader.getDiagnostics();
        Map<String, SmartImageLoader.LoadResult> loadHistory = 
            (Map<String, SmartImageLoader.LoadResult>) diagnostics.get("loadHistory");
        
        // Count failures
        long failureCount = loadHistory.values().stream()
            .filter(r -> !r.isSuccess())
            .count();
        
        if (failureCount > 0) {
            log.info("⚠ {} image load failures detected", failureCount);
            log.info("  - Check that image files exist in configured paths");
            log.info("  - Verify file extensions match (.png, .jpg, etc.)");
            log.info("  - For JAR deployments, ensure images are in resources");
        }
        
        // Check cache efficiency
        long cacheHits = loadHistory.values().stream()
            .filter(r -> r.isSuccess() && "cache".equals(r.getLoadedFrom()))
            .count();
        
        if (loadHistory.size() > 10 && cacheHits < loadHistory.size() / 2) {
            log.info("⚠ Low cache hit rate detected");
            log.info("  - Consider preloading frequently used images");
            log.info("  - Check if cache is being cleared too frequently");
        }
        
        // Check for mock mode
        long mockLoads = loadHistory.values().stream()
            .filter(r -> r.isSuccess() && "mock".equals(r.getLoadedFrom()))
            .count();
        
        if (mockLoads > 0) {
            log.info("ℹ Mock mode detected ({} mock images)", mockLoads);
            log.info("  - Set brobot.mock=false for production use");
        }
        
        if (failureCount == 0 && loadHistory.size() > 0) {
            log.info("✓ All image loads successful!");
        }
    }
}