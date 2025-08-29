package io.github.jspinak.brobot.config;

import lombok.extern.slf4j.Slf4j;
import org.sikuli.script.ImagePath;
import org.springframework.stereotype.Component;

import java.io.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Centralized image path management system that handles all path resolution strategies.
 * This class provides intelligent path resolution, JAR extraction, and SikuliX integration.
 */
@Slf4j
@Component
public class ImagePathManager {
    
    private final Set<String> configuredPaths = ConcurrentHashMap.newKeySet();
    private final Map<String, Path> extractedJarPaths = new ConcurrentHashMap<>();
    private boolean initialized = false;
    private Path primaryImagePath;
    
    /**
     * Strategy pattern for different path resolution approaches
     */
    private interface PathResolver {
        Optional<Path> resolve(String basePath);
        String getName();
    }
    
    private final List<PathResolver> resolvers = Arrays.asList(
        new AbsolutePathResolver(),
        new WorkingDirectoryResolver(),
        new ClasspathResolver(),
        new JarRelativeResolver(),
        new CommonLocationResolver()
    );
    
    /**
     * Initialize the image path manager with the given base path
     */
    public synchronized void initialize(String basePath) {
        // Validate input
        if (basePath == null || basePath.isEmpty()) {
            throw new IllegalStateException("Image path cannot be null or empty");
        }
        
        if (initialized && basePath.equals(primaryImagePath != null ? primaryImagePath.toString() : null)) {
            log.debug("ImagePathManager already initialized with path: {}", basePath);
            return;
        }
        
        log.info("Initializing ImagePathManager with base path: {}", basePath);
        
        // Try each resolver strategy
        Optional<Path> resolvedPath = Optional.empty();
        for (PathResolver resolver : resolvers) {
            resolvedPath = resolver.resolve(basePath);
            if (resolvedPath.isPresent()) {
                log.info("Path resolved using {}: {}", resolver.getName(), resolvedPath.get());
                break;
            }
        }
        
        if (resolvedPath.isEmpty()) {
            log.warn("Could not resolve image path: {}. Using fallback.", basePath);
            resolvedPath = createFallbackPath(basePath);
        }
        
        primaryImagePath = resolvedPath.orElseThrow(() -> 
            new IllegalStateException("Failed to initialize image path: " + basePath));
        
        // Ensure the directory exists
        try {
            Files.createDirectories(primaryImagePath);
        } catch (IOException e) {
            log.warn("Could not create directory: {}", primaryImagePath, e);
        }
        
        // Configure SikuliX if not in mock mode
        if (!ExecutionEnvironment.getInstance().shouldSkipSikuliX()) {
            configureSikuliX();
        }
        
        initialized = true;
    }
    
    /**
     * Add an additional search path
     */
    public void addPath(String path) {
        for (PathResolver resolver : resolvers) {
            Optional<Path> resolved = resolver.resolve(path);
            if (resolved.isPresent()) {
                String pathStr = resolved.get().toString();
                if (configuredPaths.add(pathStr)) {
                    log.info("Added search path: {}", pathStr);
                    if (!ExecutionEnvironment.getInstance().shouldSkipSikuliX()) {
                        ImagePath.add(pathStr);
                    }
                }
                return;
            }
        }
        log.warn("Could not resolve additional path: {}", path);
    }
    
    /**
     * Get all configured image paths
     */
    public List<String> getConfiguredPaths() {
        // Use a LinkedHashSet to maintain order while avoiding duplicates
        Set<String> uniquePaths = new LinkedHashSet<>();
        if (primaryImagePath != null) {
            uniquePaths.add(primaryImagePath.toString());
        }
        uniquePaths.addAll(configuredPaths);
        return new ArrayList<>(uniquePaths);
    }
    
    /**
     * Get all paths including primary, configured, and extracted JAR paths
     */
    public List<String> getAllPaths() {
        List<String> allPaths = new ArrayList<>(getConfiguredPaths());
        extractedJarPaths.values().forEach(path -> allPaths.add(path.toString()));
        return allPaths;
    }
    
    /**
     * Extract images from JAR if running from JAR file
     */
    public Path extractImagesFromJar(String resourcePath) {
        String cacheKey = resourcePath;
        if (extractedJarPaths.containsKey(cacheKey)) {
            return extractedJarPaths.get(cacheKey);
        }
        
        try {
            // Get the JAR file containing this class
            URL jarUrl = getClass().getProtectionDomain().getCodeSource().getLocation();
            if (!jarUrl.toString().endsWith(".jar")) {
                log.debug("Not running from JAR, skipping extraction");
                return null;
            }
            
            Path jarPath = Paths.get(jarUrl.toURI());
            Path tempDir = Files.createTempDirectory("brobot-images-");
            tempDir.toFile().deleteOnExit();
            
            log.info("Extracting images from JAR to: {}", tempDir);
            
            try (JarFile jarFile = new JarFile(jarPath.toFile())) {
                Enumeration<JarEntry> entries = jarFile.entries();
                int extractedCount = 0;
                
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String entryName = entry.getName();
                    
                    if (entryName.startsWith(resourcePath) && !entry.isDirectory() 
                        && isImageFile(entryName)) {
                        Path destFile = tempDir.resolve(entryName.substring(resourcePath.length()));
                        Files.createDirectories(destFile.getParent());
                        
                        try (InputStream is = jarFile.getInputStream(entry)) {
                            Files.copy(is, destFile, StandardCopyOption.REPLACE_EXISTING);
                            extractedCount++;
                        }
                    }
                }
                
                log.info("Extracted {} images from JAR", extractedCount);
                extractedJarPaths.put(cacheKey, tempDir);
                return tempDir;
            }
            
        } catch (Exception e) {
            log.error("Failed to extract images from JAR: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Validate that the configured paths contain images
     */
    public boolean validatePaths() {
        boolean hasImages = false;
        
        for (String pathStr : getConfiguredPaths()) {
            Path path = Paths.get(pathStr);
            if (Files.exists(path) && Files.isDirectory(path)) {
                try {
                    boolean hasImagesInPath = Files.walk(path, 1)
                        .filter(Files::isRegularFile)
                        .anyMatch(p -> isImageFile(p.toString()));
                    
                    if (hasImagesInPath) {
                        hasImages = true;
                        log.debug("Found images in: {}", path);
                    }
                } catch (IOException e) {
                    log.warn("Error checking path {}: {}", path, e.getMessage());
                }
            }
        }
        
        return hasImages;
    }
    
    /**
     * Get diagnostic information about current configuration
     */
    public Map<String, Object> getDiagnostics() {
        Map<String, Object> diagnostics = new HashMap<>();
        diagnostics.put("initialized", initialized);
        diagnostics.put("primaryPath", primaryImagePath != null ? primaryImagePath.toString() : null);
        diagnostics.put("additionalPaths", new ArrayList<>(configuredPaths));
        diagnostics.put("extractedJars", new ArrayList<>(extractedJarPaths.keySet()));
        diagnostics.put("pathsValid", validatePaths());
        diagnostics.put("sikulixConfigured", !ExecutionEnvironment.getInstance().shouldSkipSikuliX());
        return diagnostics;
    }
    
    /**
     * Verify image setup and provide detailed diagnostics.
     * This method checks for expected directories and images, logging
     * helpful information for troubleshooting missing resources.
     * 
     * @param expectedDirs List of expected directory paths (relative to project root)
     * @param expectedImages List of expected image file paths
     * @return true if all expected resources are found, false otherwise
     */
    public boolean verifyImageSetup(List<String> expectedDirs, List<String> expectedImages) {
        log.info("=== IMAGE SETUP VERIFICATION ===");
        
        boolean allPresent = true;
        
        // Check ImagePath configuration
        String bundlePath = ImagePath.getBundlePath();
        log.info("SikuliX Bundle Path: {}", bundlePath != null ? bundlePath : "NOT SET");
        log.info("Primary Image Path: {}", primaryImagePath != null ? primaryImagePath : "NOT SET");
        
        // Check expected directories if provided
        if (expectedDirs != null && !expectedDirs.isEmpty()) {
            for (String dir : expectedDirs) {
                File dirFile = new File(dir);
                if (dirFile.exists()) {
                    log.info("✓ Directory exists: {}", dirFile.getAbsolutePath());
                    
                    // List image files in directory
                    File[] imageFiles = dirFile.listFiles((d, name) -> isImageFile(name));
                    if (imageFiles != null && imageFiles.length > 0) {
                        log.info("  Found {} image files:", imageFiles.length);
                        for (File img : imageFiles) {
                            log.debug("    - {}", img.getName());
                        }
                    } else {
                        log.warn("  No image files found in {}", dir);
                    }
                } else {
                    log.warn("✗ Directory missing: {} (absolute: {})", dir, dirFile.getAbsolutePath());
                    allPresent = false;
                }
            }
        }
        
        // Check specific expected images if provided
        if (expectedImages != null && !expectedImages.isEmpty()) {
            log.info("Checking for expected images:");
            for (String imagePath : expectedImages) {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    log.debug("  ✓ {}", imagePath);
                } else {
                    log.warn("  ✗ MISSING: {} (looking in: {})", imagePath, imageFile.getAbsolutePath());
                    allPresent = false;
                    
                    // Try alternative locations
                    checkAlternativeImageLocations(imagePath);
                }
            }
        }
        
        // Provide setup guidance if images are missing
        if (!allPresent) {
            log.warn("\n=== IMAGE SETUP REQUIRED ===");
            log.warn("Some expected images or directories are missing. Please ensure:");
            log.warn("1. Image directories exist in the correct location");
            log.warn("2. Image files are properly named and in PNG format");
            log.warn("3. The image path configuration points to the correct location");
            log.warn("\nCurrent working directory: {}", System.getProperty("user.dir"));
            log.warn("Primary image path: {}", primaryImagePath);
        } else {
            log.info("✓ All expected images and directories verified successfully");
        }
        
        log.info("=== END IMAGE VERIFICATION ===");
        return allPresent;
    }
    
    /**
     * Simple verification that just checks if the configured paths have images
     */
    public boolean verifyImageSetup() {
        return verifyImageSetup(null, null);
    }
    
    private void checkAlternativeImageLocations(String imagePath) {
        // Try without common prefixes
        String[] prefixes = {"images/", "src/main/resources/images/", "resources/images/"};
        for (String prefix : prefixes) {
            if (imagePath.startsWith(prefix)) {
                String withoutPrefix = imagePath.substring(prefix.length());
                File altFile = new File(withoutPrefix);
                if (altFile.exists()) {
                    log.info("    Found at alternative location: {}", altFile.getAbsolutePath());
                    return;
                }
            }
        }
        
        // Try in primary image path
        if (primaryImagePath != null) {
            Path inPrimary = primaryImagePath.resolve(Paths.get(imagePath).getFileName());
            if (Files.exists(inPrimary)) {
                log.info("    Found in primary image path: {}", inPrimary);
            }
        }
    }
    
    private void configureSikuliX() {
        if (primaryImagePath == null) {
            log.warn("Cannot configure SikuliX - no primary path set");
            return;
        }
        
        String pathStr = primaryImagePath.toString();
        log.info("Configuring SikuliX with bundle path: {}", pathStr);
        
        try {
            ImagePath.setBundlePath(pathStr);
            configuredPaths.add(pathStr);
            
            // Add any additional paths
            for (String additionalPath : configuredPaths) {
                if (!additionalPath.equals(pathStr)) {
                    ImagePath.add(additionalPath);
                }
            }
        } catch (Exception e) {
            log.error("Failed to configure SikuliX paths: {}", e.getMessage());
        }
    }
    
    private Optional<Path> createFallbackPath(String basePath) {
        if (basePath == null || basePath.isEmpty()) {
            return Optional.empty();
        }
        try {
            // Create directory in temp if it doesn't exist
            Path fallback = Paths.get(System.getProperty("java.io.tmpdir"), "brobot-images", basePath);
            Files.createDirectories(fallback);
            log.info("Created fallback image directory: {}", fallback);
            return Optional.of(fallback);
        } catch (Exception e) {
            log.error("Failed to create fallback directory: {}", e.getMessage());
            return Optional.empty();
        }
    }
    
    private boolean isImageFile(String filename) {
        String lower = filename.toLowerCase();
        return lower.endsWith(".png") || lower.endsWith(".jpg") || 
               lower.endsWith(".jpeg") || lower.endsWith(".gif") || 
               lower.endsWith(".bmp");
    }
    
    // Path resolver implementations
    
    private static class AbsolutePathResolver implements PathResolver {
        @Override
        public Optional<Path> resolve(String basePath) {
            if (basePath == null || basePath.isEmpty()) {
                return Optional.empty();
            }
            try {
                Path path = Paths.get(basePath);
                if (path.isAbsolute()) {
                    // For absolute paths, we can try to create if it doesn't exist
                    if (!Files.exists(path)) {
                        try {
                            Files.createDirectories(path);
                            return Optional.of(path);
                        } catch (IOException e) {
                            // Could not create, fall through
                        }
                    } else if (Files.isDirectory(path)) {
                        return Optional.of(path);
                    }
                }
            } catch (Exception e) {
                // Invalid path
            }
            return Optional.empty();
        }
        
        @Override
        public String getName() {
            return "AbsolutePathResolver";
        }
    }
    
    private static class WorkingDirectoryResolver implements PathResolver {
        @Override
        public Optional<Path> resolve(String basePath) {
            if (basePath == null || basePath.isEmpty()) {
                return Optional.empty();
            }
            try {
                Path workingDir = Paths.get(System.getProperty("user.dir"));
                Path resolved = workingDir.resolve(basePath);
                if (Files.exists(resolved) && Files.isDirectory(resolved)) {
                    return Optional.of(resolved);
                }
            } catch (Exception e) {
                // Invalid path
            }
            return Optional.empty();
        }
        
        @Override
        public String getName() {
            return "WorkingDirectoryResolver";
        }
    }
    
    private static class ClasspathResolver implements PathResolver {
        @Override
        public Optional<Path> resolve(String basePath) {
            if (basePath == null || basePath.isEmpty()) {
                return Optional.empty();
            }
            String resourcePath = basePath;
            if (basePath.startsWith("classpath:")) {
                resourcePath = basePath.substring("classpath:".length());
            }
            
            URL resource = getClass().getClassLoader().getResource(resourcePath);
            if (resource != null) {
                try {
                    Path path = Paths.get(resource.toURI());
                    if (Files.exists(path) && Files.isDirectory(path)) {
                        return Optional.of(path);
                    }
                } catch (URISyntaxException | FileSystemNotFoundException e) {
                    // Might be in JAR - will handle separately
                }
            }
            return Optional.empty();
        }
        
        @Override
        public String getName() {
            return "ClasspathResolver";
        }
    }
    
    private class JarRelativeResolver implements PathResolver {
        @Override
        public Optional<Path> resolve(String basePath) {
            if (basePath == null || basePath.isEmpty()) {
                return Optional.empty();
            }
            try {
                URL jarUrl = getClass().getProtectionDomain().getCodeSource().getLocation();
                if (jarUrl.toString().endsWith(".jar")) {
                    Path jarPath = Paths.get(jarUrl.toURI());
                    Path jarDir = jarPath.getParent();
                    Path resolved = jarDir.resolve(basePath);
                    
                    if (Files.exists(resolved) && Files.isDirectory(resolved)) {
                        return Optional.of(resolved);
                    }
                    
                    // Try extracting from JAR
                    Path extracted = extractImagesFromJar(basePath);
                    if (extracted != null) {
                        return Optional.of(extracted);
                    }
                }
            } catch (Exception e) {
                log.debug("JarRelativeResolver failed: {}", e.getMessage());
            }
            return Optional.empty();
        }
        
        @Override
        public String getName() {
            return "JarRelativeResolver";
        }
    }
    
    private static class CommonLocationResolver implements PathResolver {
        private final List<String> commonLocations = Arrays.asList(
            "src/main/resources/images",
            "src/test/resources/images",
            "resources/images",
            "images",
            "../images",
            "../../images"
        );
        
        @Override
        public Optional<Path> resolve(String basePath) {
            if (basePath == null || basePath.isEmpty()) {
                return Optional.empty();
            }
            // First try the base path in common locations
            for (String location : commonLocations) {
                Path candidate = Paths.get(location, basePath);
                if (Files.exists(candidate) && Files.isDirectory(candidate)) {
                    return Optional.of(candidate);
                }
            }
            
            // Then try common locations themselves if basePath is "images" or similar
            if (basePath.equals("images") || basePath.endsWith("/images")) {
                for (String location : commonLocations) {
                    Path candidate = Paths.get(location);
                    if (Files.exists(candidate) && Files.isDirectory(candidate)) {
                        return Optional.of(candidate);
                    }
                }
            }
            
            return Optional.empty();
        }
        
        @Override
        public String getName() {
            return "CommonLocationResolver";
        }
    }
}