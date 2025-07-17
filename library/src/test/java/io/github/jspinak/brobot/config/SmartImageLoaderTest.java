package io.github.jspinak.brobot.config;

import org.junit.jupiter.api.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;
import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SmartImageLoader focusing on loading strategies and caching.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SmartImageLoaderTest {
    
    private Path tempDir;
    private ImagePathManager pathManager;
    private SmartImageLoader imageLoader;
    
    @BeforeEach
    void setup() throws IOException {
        tempDir = Files.createTempDirectory("smart-loader-test");
        pathManager = new ImagePathManager();
        imageLoader = new SmartImageLoader(pathManager, ExecutionEnvironment.getInstance());
    }
    
    @AfterEach
    void cleanup() throws IOException {
        imageLoader.clearCache();
        deleteDirectory(tempDir);
    }
    
    @Test
    @DisplayName("Load image from file path")
    void testLoadFromFilePath() throws IOException {
        // Create test image
        Path imagePath = tempDir.resolve("test-image.png");
        createRealPngImage(imagePath);
        
        // Load using absolute path
        BufferedImage image = imageLoader.loadImage(imagePath.toString());
        
        assertNotNull(image);
        assertEquals(100, image.getWidth());
        assertEquals(100, image.getHeight());
        
        // Check load history
        var diagnostics = imageLoader.getDiagnostics();
        assertNotNull(diagnostics.get("loadHistory"));
    }
    
    @Test
    @DisplayName("Load image from configured paths")
    void testLoadFromConfiguredPaths() throws IOException {
        // Setup path manager
        Path imageDir = tempDir.resolve("images");
        Files.createDirectories(imageDir);
        createRealPngImage(imageDir.resolve("button.png"));
        
        pathManager.initialize(imageDir.toString());
        
        // Load using just filename
        BufferedImage image = imageLoader.loadImage("button.png");
        
        assertNotNull(image);
        assertEquals(100, image.getWidth());
    }
    
    @Test
    @DisplayName("Image caching functionality")
    void testImageCaching() throws IOException {
        // Create test image
        Path imagePath = tempDir.resolve("cached.png");
        createRealPngImage(imagePath);
        
        // First load - from file
        long start1 = System.currentTimeMillis();
        BufferedImage image1 = imageLoader.loadImage(imagePath.toString());
        long time1 = System.currentTimeMillis() - start1;
        
        // Second load - from cache
        long start2 = System.currentTimeMillis();
        BufferedImage image2 = imageLoader.loadImage(imagePath.toString());
        long time2 = System.currentTimeMillis() - start2;
        
        assertNotNull(image1);
        assertNotNull(image2);
        assertTrue(time2 < time1 || time2 < 5); // Cache should be faster
        
        // Verify cache size
        var diagnostics = imageLoader.getDiagnostics();
        assertEquals(1, diagnostics.get("cachedImages"));
        
        // Clear cache and verify
        imageLoader.clearCache();
        diagnostics = imageLoader.getDiagnostics();
        assertEquals(0, diagnostics.get("cachedImages"));
    }
    
    @Test
    @DisplayName("Mock mode placeholder images")
    void testMockModePlaceholder() {
        // Set mock mode
        ExecutionEnvironment env = ExecutionEnvironment.builder()
            .mockMode(true)
            .build();
        
        SmartImageLoader mockLoader = new SmartImageLoader(pathManager, env);
        
        // Load non-existent image
        BufferedImage placeholder = mockLoader.loadImage("non-existent.png");
        
        assertNotNull(placeholder);
        assertEquals(100, placeholder.getWidth());
        assertEquals(100, placeholder.getHeight());
        
        // Verify it's a placeholder pattern
        int color1 = placeholder.getRGB(0, 0);
        int color2 = placeholder.getRGB(10, 10);
        assertNotEquals(color1, color2); // Should have pattern
    }
    
    @Test
    @DisplayName("Fallback strategies for missing images")
    void testFallbackStrategies() {
        // Load non-existent image
        BufferedImage fallback = imageLoader.loadImage("missing-image.png");
        
        assertNotNull(fallback); // Should return placeholder
        assertEquals(100, fallback.getWidth());
        assertEquals(100, fallback.getHeight());
        
        // Check load history shows failure
        var diagnostics = imageLoader.getDiagnostics();
        @SuppressWarnings("unchecked")
        var loadHistory = (java.util.Map<String, SmartImageLoader.LoadResult>) diagnostics.get("loadHistory");
        
        var result = loadHistory.get("missing-image.png");
        assertNotNull(result);
        assertFalse(result.success);
        assertNotNull(result.failureReason);
    }
    
    @Test
    @DisplayName("Load without extension")
    void testLoadWithoutExtension() throws IOException {
        // Create image with extension
        Path imageDir = tempDir.resolve("ext-test");
        Files.createDirectories(imageDir);
        createRealPngImage(imageDir.resolve("icon.png"));
        
        pathManager.initialize(imageDir.toString());
        
        // Try loading without extension
        BufferedImage image = imageLoader.loadImage("icon");
        
        // Should find icon.png
        assertNotNull(image);
        assertEquals(100, image.getWidth());
    }
    
    @Test
    @DisplayName("Failure suggestions")
    void testFailureSuggestions() {
        // Try to load non-existent image
        imageLoader.loadImage("does-not-exist.png");
        
        // Get suggestions
        String suggestions = imageLoader.getSuggestionsForFailure("does-not-exist.png");
        
        assertNotNull(suggestions);
        assertTrue(suggestions.contains("Image loading failed"));
        assertTrue(suggestions.contains("Suggestions:"));
        
        // Suggestions for successful load
        String noFailure = imageLoader.getSuggestionsForFailure("not-attempted.png");
        assertTrue(noFailure.contains("No failure recorded"));
    }
    
    @Test
    @DisplayName("SikuliX Image wrapper")
    void testSikuliImageWrapper() throws IOException {
        // Create test image
        Path imagePath = tempDir.resolve("sikuli-test.png");
        createRealPngImage(imagePath);
        
        // Load as SikuliX Image
        org.sikuli.script.Image sikuliImage = imageLoader.loadSikuliImage(imagePath.toString());
        
        assertNotNull(sikuliImage);
        assertEquals(imagePath.toString(), sikuliImage.getName());
        assertNotNull(sikuliImage.get()); // BufferedImage inside
    }
    
    @Test
    @DisplayName("Concurrent loading")
    void testConcurrentLoading() throws Exception {
        // Create test images
        Path imageDir = tempDir.resolve("concurrent");
        Files.createDirectories(imageDir);
        
        for (int i = 0; i < 5; i++) {
            createRealPngImage(imageDir.resolve("img" + i + ".png"));
        }
        
        pathManager.initialize(imageDir.toString());
        
        // Load images concurrently
        var futures = new java.util.ArrayList<java.util.concurrent.Future<BufferedImage>>();
        var executor = java.util.concurrent.Executors.newFixedThreadPool(5);
        
        for (int i = 0; i < 5; i++) {
            final int index = i;
            futures.add(executor.submit(() -> imageLoader.loadImage("img" + index + ".png")));
        }
        
        // Verify all loaded successfully
        for (var future : futures) {
            BufferedImage img = future.get();
            assertNotNull(img);
            assertEquals(100, img.getWidth());
        }
        
        executor.shutdown();
        
        // Verify cache has all images
        var diagnostics = imageLoader.getDiagnostics();
        assertEquals(5, diagnostics.get("cachedImages"));
    }
    
    // Helper methods
    
    private void createRealPngImage(Path path) throws IOException {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        
        // Draw something simple
        var g = image.getGraphics();
        g.setColor(java.awt.Color.BLUE);
        g.fillRect(0, 0, 100, 100);
        g.setColor(java.awt.Color.WHITE);
        g.drawString("TEST", 30, 50);
        g.dispose();
        
        ImageIO.write(image, "PNG", path.toFile());
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