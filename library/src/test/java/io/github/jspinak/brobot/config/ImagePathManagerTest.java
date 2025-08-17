package io.github.jspinak.brobot.config;

import org.junit.jupiter.api.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.jar.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ImagePathManager focusing on path resolution strategies.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ImagePathManagerTest {
    
    private Path tempDir;
    private ImagePathManager pathManager;
    
    @BeforeEach
    void setup() throws IOException {
        tempDir = Files.createTempDirectory("image-path-test");
        pathManager = new ImagePathManager();
    }
    
    @AfterEach
    void cleanup() throws IOException {
        deleteDirectory(tempDir);
    }
    
    @Test
    @DisplayName("Absolute path resolution")
    void testAbsolutePathResolution() throws IOException {
        // Create test directory structure
        Path imageDir = tempDir.resolve("images");
        Files.createDirectories(imageDir);
        createTestImage(imageDir.resolve("test.png"));
        
        // Initialize with absolute path
        pathManager.initialize(imageDir.toAbsolutePath().toString());
        
        // Verify
        List<String> paths = pathManager.getConfiguredPaths();
        assertEquals(1, paths.size());
        assertTrue(paths.get(0).contains(imageDir.toString()));
        assertTrue(pathManager.validatePaths());
    }
    
    @Test
    @DisplayName("Relative path resolution")
    void testRelativePathResolution() {
        // Test with common relative paths
        String[] relativePaths = {"images", "./images", "../images", "src/main/resources/images"};
        
        for (String path : relativePaths) {
            ImagePathManager pm = new ImagePathManager();
            // Should not throw exception even if path doesn't exist
            assertDoesNotThrow(() -> pm.initialize(path));
        }
    }
    
    @Test
    @DisplayName("Classpath resolution")
    void testClasspathResolution() {
        // Test classpath prefix handling
        pathManager.initialize("classpath:images");
        
        // Should handle gracefully even if not found
        assertNotNull(pathManager.getConfiguredPaths());
    }
    
    @Test
    @DisplayName("JAR extraction simulation")
    void testJarExtraction() throws IOException {
        // Create a mock JAR structure
        Path jarImages = tempDir.resolve("jar-images");
        Files.createDirectories(jarImages);
        createTestImage(jarImages.resolve("button.png"));
        createTestImage(jarImages.resolve("icon.png"));
        
        // Simulate extraction
        Path extracted = pathManager.extractImagesFromJar("images");
        
        // In real scenario, this would extract from JAR
        // For testing, we just verify the method handles the case
        // Note: Actual JAR extraction is tested in integration tests
    }
    
    @Test
    @DisplayName("Multiple path management")
    void testMultiplePathManagement() throws IOException {
        // Create multiple image directories
        Path path1 = tempDir.resolve("images1");
        Path path2 = tempDir.resolve("images2");
        Files.createDirectories(path1);
        Files.createDirectories(path2);
        
        createTestImage(path1.resolve("img1.png"));
        createTestImage(path2.resolve("img2.png"));
        
        // Add multiple paths
        pathManager.initialize(path1.toString());
        pathManager.addPath(path2.toString());
        
        // Verify both paths are configured
        List<String> paths = pathManager.getConfiguredPaths();
        assertEquals(2, paths.size());
        
        // Test duplicate prevention - adding primary path again should not increase count
        pathManager.addPath(path1.toString());
        assertEquals(2, pathManager.getConfiguredPaths().size());
    }
    
    @Test
    @DisplayName("Path validation")
    void testPathValidation() throws IOException {
        // Test with no images
        Path emptyDir = tempDir.resolve("empty");
        Files.createDirectories(emptyDir);
        pathManager.initialize(emptyDir.toString());
        assertFalse(pathManager.validatePaths());
        
        // Test with images
        Path imageDir = tempDir.resolve("with-images");
        Files.createDirectories(imageDir);
        createTestImage(imageDir.resolve("test.png"));
        
        pathManager = new ImagePathManager();
        pathManager.initialize(imageDir.toString());
        assertTrue(pathManager.validatePaths());
    }
    
    @Test
    @DisplayName("Common locations resolver")
    void testCommonLocationsResolver() {
        // Test that common locations are checked
        pathManager.initialize("images");
        
        // Should check multiple common locations
        assertNotNull(pathManager.getConfiguredPaths());
        
        // Even if not found, should create fallback
        assertFalse(pathManager.getConfiguredPaths().isEmpty());
    }
    
    @Test
    @DisplayName("Fallback path creation")
    void testFallbackPathCreation() {
        // Initialize with non-existent path
        pathManager.initialize("non/existent/path");
        
        // Should create fallback in temp directory
        List<String> paths = pathManager.getConfiguredPaths();
        assertFalse(paths.isEmpty());
        assertTrue(paths.get(0).contains("brobot-images"));
    }
    
    @Test
    @DisplayName("Diagnostics information")
    void testDiagnostics() throws IOException {
        // Setup test environment
        Path imageDir = tempDir.resolve("diagnostics-test");
        Files.createDirectories(imageDir);
        createTestImage(imageDir.resolve("test.png"));
        
        pathManager.initialize(imageDir.toString());
        
        // Get diagnostics
        var diagnostics = pathManager.getDiagnostics();
        
        assertNotNull(diagnostics);
        assertTrue((boolean) diagnostics.get("initialized"));
        assertNotNull(diagnostics.get("primaryPath"));
        assertNotNull(diagnostics.get("additionalPaths"));
        assertTrue((boolean) diagnostics.get("pathsValid"));
    }
    
    @Test
    @DisplayName("SikuliX integration - mock mode")
    void testSikuliXIntegrationMockMode() {
        // Set mock mode
        ExecutionEnvironment env = ExecutionEnvironment.builder()
            .mockMode(true)
            .build();
        ExecutionEnvironment.setInstance(env);
        
        try {
            // Initialize path manager
            pathManager.initialize("images");
            
            // Verify SikuliX is skipped
            var diagnostics = pathManager.getDiagnostics();
            assertFalse((boolean) diagnostics.get("sikulixConfigured"));
            
        } finally {
            // Reset environment
            ExecutionEnvironment.setInstance(ExecutionEnvironment.builder().build());
        }
    }
    
    // Helper methods
    
    private void createTestImage(Path path) throws IOException {
        Files.write(path, new byte[]{
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A // PNG header
        });
    }
    
    private void deleteDirectory(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walk(path)
                .sorted((a, b) -> -a.compareTo(b))
                .forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (IOException e) {
                        // Ignore
                    }
                });
        }
    }
}