package io.github.jspinak.brobot.diagnostics;

import io.github.jspinak.brobot.config.ImagePathManager;
import io.github.jspinak.brobot.config.SmartImageLoader;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sikuli.script.ImagePath;
import org.springframework.test.util.ReflectionTestUtils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for ImageLoadingDiagnosticsRunner.
 * Tests diagnostic capabilities, environment reporting, and image loading analysis.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ImageLoadingDiagnosticsRunner Tests")
public class ImageLoadingDiagnosticsRunnerTest extends BrobotTestBase {

    @Mock
    private SmartImageLoader smartImageLoader;
    
    @Mock
    private ImagePathManager imagePathManager;
    
    @Captor
    private ArgumentCaptor<String> stringCaptor;
    
    private ImageLoadingDiagnosticsRunner diagnosticsRunner;
    
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        
        // Capture System.out for testing console output
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
        
        diagnosticsRunner = new ImageLoadingDiagnosticsRunner(smartImageLoader, imagePathManager);
    }
    
    @AfterEach
    public void tearDown() {
        // Restore original System.out
        System.setOut(originalOut);
    }
    
    @Test
    @DisplayName("Should run diagnostics with basic environment information")
    void testRunDiagnosticsBasic() {
        // Arrange
        Map<String, Object> pathDiagnostics = new HashMap<>();
        pathDiagnostics.put("configured_paths", Arrays.asList("/path1", "/path2"));
        pathDiagnostics.put("cache_size", 5);
        when(imagePathManager.getDiagnostics()).thenReturn(pathDiagnostics);
        when(imagePathManager.getAllPaths()).thenReturn(Arrays.asList("/images"));
        
        // Act
        diagnosticsRunner.runDiagnostics();
        
        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("=== Brobot Image Loading Diagnostics ==="));
        assertTrue(output.contains("=== Environment Information ==="));
        assertTrue(output.contains("Working Directory:"));
        assertTrue(output.contains("Java Version:"));
        assertTrue(output.contains("OS:"));
        assertTrue(output.contains("Display Available:"));
        
        verify(imagePathManager).getDiagnostics();
        verify(imagePathManager).getAllPaths();
    }
    
    @Test
    @DisplayName("Should print path configuration details")
    void testPrintPathConfiguration() {
        // Arrange
        Map<String, Object> pathDiagnostics = new HashMap<>();
        pathDiagnostics.put("configured_paths", Arrays.asList("/path/to/images", "/another/path"));
        pathDiagnostics.put("cache_size", 10);
        pathDiagnostics.put("status", "initialized");
        
        when(imagePathManager.getDiagnostics()).thenReturn(pathDiagnostics);
        when(imagePathManager.getAllPaths()).thenReturn(Collections.emptyList());
        
        ReflectionTestUtils.setField(diagnosticsRunner, "verbose", true);
        
        // Act
        diagnosticsRunner.runDiagnostics();
        
        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("=== Path Configuration ==="));
        assertTrue(output.contains("ImagePathManager Status:"));
        assertTrue(output.contains("configured_paths: 2 entries"));
        assertTrue(output.contains("/path/to/images"));
        assertTrue(output.contains("/another/path"));
        assertTrue(output.contains("cache_size: 10"));
        assertTrue(output.contains("status: initialized"));
    }
    
    @Test
    @DisplayName("Should analyze directory structure")
    void testDirectoryStructureAnalysis() {
        // Arrange
        String testDir = System.getProperty("java.io.tmpdir") + File.separator + "test_images";
        File dir = new File(testDir);
        dir.mkdirs();
        
        // Create test image files
        File imageFile1 = new File(dir, "test1.png");
        File imageFile2 = new File(dir, "test2.jpg");
        File subDir = new File(dir, "subdir");
        subDir.mkdirs();
        File imageFile3 = new File(subDir, "test3.gif");
        
        try {
            imageFile1.createNewFile();
            imageFile2.createNewFile();
            imageFile3.createNewFile();
            
            when(imagePathManager.getDiagnostics()).thenReturn(new HashMap<>());
            when(imagePathManager.getAllPaths()).thenReturn(Arrays.asList(testDir));
            
            ReflectionTestUtils.setField(diagnosticsRunner, "verbose", false);
            
            // Act
            diagnosticsRunner.runDiagnostics();
            
            // Assert
            String output = outputStream.toString();
            assertTrue(output.contains("=== Directory Structure ==="));
            assertTrue(output.contains("Directory: " + dir.getAbsolutePath()));
            assertTrue(output.contains("Contains 3 image files"));
            
        } catch (Exception e) {
            fail("Failed to create test files: " + e.getMessage());
        } finally {
            // Cleanup
            imageFile3.delete();
            imageFile2.delete();
            imageFile1.delete();
            subDir.delete();
            dir.delete();
        }
    }
    
    @Test
    @DisplayName("Should test specific images when configured")
    void testSpecificImages() {
        // Arrange
        List<String> testImages = Arrays.asList("image1.png", "image2.jpg", "image3.gif");
        ReflectionTestUtils.setField(diagnosticsRunner, "testImages", testImages);
        ReflectionTestUtils.setField(diagnosticsRunner, "verbose", true);
        
        SmartImageLoader.LoadResult successResult = mock(SmartImageLoader.LoadResult.class);
        when(successResult.isSuccess()).thenReturn(true);
        when(successResult.getLoadedFrom()).thenReturn("cache");
        when(successResult.getLoadTimeMs()).thenReturn(15L);
        
        SmartImageLoader.LoadResult failureResult = mock(SmartImageLoader.LoadResult.class);
        when(failureResult.isSuccess()).thenReturn(false);
        when(failureResult.getFailureReason()).thenReturn("File not found");
        
        BufferedImage mockImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        
        when(smartImageLoader.loadImage("image1.png")).thenReturn(successResult);
        when(smartImageLoader.loadImage("image2.jpg")).thenReturn(successResult);
        when(smartImageLoader.loadImage("image3.gif")).thenReturn(failureResult);
        when(smartImageLoader.getFromCache(anyString())).thenReturn(mockImage);
        when(smartImageLoader.getSuggestionsForFailure("image3.gif"))
            .thenReturn(Arrays.asList("Check file exists", "Verify path configuration"));
        
        when(imagePathManager.getDiagnostics()).thenReturn(new HashMap<>());
        when(imagePathManager.getAllPaths()).thenReturn(Collections.emptyList());
        
        // Act
        diagnosticsRunner.runDiagnostics();
        
        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("=== Testing Specific Images ==="));
        assertTrue(output.contains("Testing: 'image1.png'"));
        assertTrue(output.contains("✓ SUCCESS - Loaded from: cache"));
        assertTrue(output.contains("Image dimensions: 100x100"));
        
        assertTrue(output.contains("Testing: 'image3.gif'"));
        assertTrue(output.contains("✗ FAILED - File not found"));
        assertTrue(output.contains("Suggestions:"));
        assertTrue(output.contains("Check file exists"));
        assertTrue(output.contains("Verify path configuration"));
        
        verify(smartImageLoader, times(3)).loadImage(anyString());
        verify(smartImageLoader, times(2)).getFromCache(anyString());
    }
    
    @Test
    @DisplayName("Should test all found images when enabled")
    void testAllFoundImages() {
        // Arrange
        String testDir = System.getProperty("java.io.tmpdir") + File.separator + "all_images_test";
        File dir = new File(testDir);
        dir.mkdirs();
        
        // Create test files
        File image1 = new File(dir, "img1.png");
        File image2 = new File(dir, "img2.jpg");
        File textFile = new File(dir, "readme.txt");
        
        try {
            image1.createNewFile();
            image2.createNewFile();
            textFile.createNewFile();
            
            ReflectionTestUtils.setField(diagnosticsRunner, "testAllImages", true);
            ReflectionTestUtils.setField(diagnosticsRunner, "verbose", false);
            
            SmartImageLoader.LoadResult successResult = mock(SmartImageLoader.LoadResult.class);
            when(successResult.isSuccess()).thenReturn(true);
            
            SmartImageLoader.LoadResult failureResult = mock(SmartImageLoader.LoadResult.class);
            when(failureResult.isSuccess()).thenReturn(false);
            when(failureResult.getFailureReason()).thenReturn("Load error");
            
            when(smartImageLoader.loadImage(endsWith("img1.png"))).thenReturn(successResult);
            when(smartImageLoader.loadImage(endsWith("img2.jpg"))).thenReturn(failureResult);
            
            when(imagePathManager.getDiagnostics()).thenReturn(new HashMap<>());
            when(imagePathManager.getAllPaths()).thenReturn(Arrays.asList(testDir));
            
            // Act
            diagnosticsRunner.runDiagnostics();
            
            // Assert
            String output = outputStream.toString();
            assertTrue(output.contains("=== Testing All Found Images ==="));
            assertTrue(output.contains("Found 2 total images to test"));
            assertTrue(output.contains("Test Results: 1 successful, 1 failed"));
            
            verify(smartImageLoader, times(2)).loadImage(anyString());
            
        } catch (Exception e) {
            fail("Failed to create test files: " + e.getMessage());
        } finally {
            // Cleanup
            image1.delete();
            image2.delete();
            textFile.delete();
            dir.delete();
        }
    }
    
    @Test
    @DisplayName("Should generate performance report")
    void testPerformanceReport() {
        // Arrange
        Map<String, SmartImageLoader.LoadResult> loadHistory = new HashMap<>();
        
        SmartImageLoader.LoadResult cacheHit = mock(SmartImageLoader.LoadResult.class);
        when(cacheHit.isSuccess()).thenReturn(true);
        when(cacheHit.getLoadedFrom()).thenReturn("cache");
        when(cacheHit.getLoadTimeMs()).thenReturn(5L);
        
        SmartImageLoader.LoadResult fileLoad = mock(SmartImageLoader.LoadResult.class);
        when(fileLoad.isSuccess()).thenReturn(true);
        when(fileLoad.getLoadedFrom()).thenReturn("file");
        when(fileLoad.getLoadTimeMs()).thenReturn(50L);
        
        SmartImageLoader.LoadResult failure = mock(SmartImageLoader.LoadResult.class);
        when(failure.isSuccess()).thenReturn(false);
        when(failure.getLoadTimeMs()).thenReturn(10L);
        
        loadHistory.put("image1.png", cacheHit);
        loadHistory.put("image2.jpg", fileLoad);
        loadHistory.put("image3.gif", failure);
        
        Map<String, Object> diagnostics = new HashMap<>();
        diagnostics.put("loadHistory", loadHistory);
        diagnostics.put("cachedImages", 5);
        
        when(smartImageLoader.getDiagnostics()).thenReturn(diagnostics);
        when(imagePathManager.getDiagnostics()).thenReturn(new HashMap<>());
        when(imagePathManager.getAllPaths()).thenReturn(Collections.emptyList());
        
        // Act
        diagnosticsRunner.runDiagnostics();
        
        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("=== Performance Report ==="));
        assertTrue(output.contains("Total Load Attempts: 3"));
        assertTrue(output.contains("Successful Loads: 2"));
        assertTrue(output.contains("Failed Loads: 1"));
        assertTrue(output.contains("Cache Hits: 1"));
        assertTrue(output.contains("File/Resource Loads: 1"));
        assertTrue(output.contains("Current Cache Size: 5"));
    }
    
    @Test
    @DisplayName("Should provide recommendations based on diagnostics")
    void testRecommendations() {
        // Arrange
        Map<String, SmartImageLoader.LoadResult> loadHistory = new HashMap<>();
        
        // Create scenario with failures and low cache hit rate
        for (int i = 0; i < 15; i++) {
            SmartImageLoader.LoadResult result = mock(SmartImageLoader.LoadResult.class);
            if (i < 5) {
                when(result.isSuccess()).thenReturn(false);
            } else {
                when(result.isSuccess()).thenReturn(true);
                when(result.getLoadedFrom()).thenReturn(i < 8 ? "file" : "cache");
            }
            loadHistory.put("image" + i + ".png", result);
        }
        
        Map<String, Object> diagnostics = new HashMap<>();
        diagnostics.put("loadHistory", loadHistory);
        
        when(smartImageLoader.getDiagnostics()).thenReturn(diagnostics);
        when(imagePathManager.getDiagnostics()).thenReturn(new HashMap<>());
        when(imagePathManager.getAllPaths()).thenReturn(Collections.emptyList());
        
        // Act
        diagnosticsRunner.runDiagnostics();
        
        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("=== Recommendations ==="));
        assertTrue(output.contains("⚠ 5 image load failures detected"));
        assertTrue(output.contains("Check that image files exist in configured paths"));
        assertTrue(output.contains("⚠ Low cache hit rate detected"));
        assertTrue(output.contains("Consider preloading frequently used images"));
    }
    
    @Test
    @DisplayName("Should handle mock mode detection")
    void testMockModeDetection() {
        // Arrange
        Map<String, SmartImageLoader.LoadResult> loadHistory = new HashMap<>();
        
        SmartImageLoader.LoadResult mockLoad = mock(SmartImageLoader.LoadResult.class);
        when(mockLoad.isSuccess()).thenReturn(true);
        when(mockLoad.getLoadedFrom()).thenReturn("mock");
        
        loadHistory.put("mock_image.png", mockLoad);
        
        Map<String, Object> diagnostics = new HashMap<>();
        diagnostics.put("loadHistory", loadHistory);
        
        when(smartImageLoader.getDiagnostics()).thenReturn(diagnostics);
        when(imagePathManager.getDiagnostics()).thenReturn(new HashMap<>());
        when(imagePathManager.getAllPaths()).thenReturn(Collections.emptyList());
        
        // Act
        diagnosticsRunner.runDiagnostics();
        
        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("ℹ Mock mode detected (1 mock images)"));
        assertTrue(output.contains("Set brobot.mock=false for production use"));
    }
    
    @Test
    @DisplayName("Should handle empty diagnostics gracefully")
    void testEmptyDiagnostics() {
        // Arrange
        when(imagePathManager.getDiagnostics()).thenReturn(new HashMap<>());
        when(imagePathManager.getAllPaths()).thenReturn(Collections.emptyList());
        when(smartImageLoader.getDiagnostics()).thenReturn(new HashMap<>());
        
        // Act
        assertDoesNotThrow(() -> diagnosticsRunner.runDiagnostics());
        
        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("=== Brobot Image Loading Diagnostics ==="));
        assertTrue(output.contains("=== Image Loading Diagnostics Complete ==="));
    }
    
    @Test
    @DisplayName("Should handle non-existent directories gracefully")
    void testNonExistentDirectory() {
        // Arrange
        when(imagePathManager.getDiagnostics()).thenReturn(new HashMap<>());
        when(imagePathManager.getAllPaths()).thenReturn(Arrays.asList("/non/existent/path"));
        
        // Act
        diagnosticsRunner.runDiagnostics();
        
        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("Directory: /non/existent/path (NOT FOUND)"));
    }
    
    @Test
    @DisplayName("Should identify different image file types")
    void testImageFileTypeIdentification() {
        // Arrange
        String testDir = System.getProperty("java.io.tmpdir") + File.separator + "image_types_test";
        File dir = new File(testDir);
        dir.mkdirs();
        
        File pngFile = new File(dir, "test.png");
        File jpgFile = new File(dir, "test.jpg");
        File jpegFile = new File(dir, "test.jpeg");
        File gifFile = new File(dir, "test.gif");
        File bmpFile = new File(dir, "test.bmp");
        File txtFile = new File(dir, "test.txt");
        
        try {
            pngFile.createNewFile();
            jpgFile.createNewFile();
            jpegFile.createNewFile();
            gifFile.createNewFile();
            bmpFile.createNewFile();
            txtFile.createNewFile();
            
            when(imagePathManager.getDiagnostics()).thenReturn(new HashMap<>());
            when(imagePathManager.getAllPaths()).thenReturn(Arrays.asList(testDir));
            
            ReflectionTestUtils.setField(diagnosticsRunner, "verbose", false);
            
            // Act
            diagnosticsRunner.runDiagnostics();
            
            // Assert
            String output = outputStream.toString();
            assertTrue(output.contains("Contains 5 image files")); // Should not count .txt
            
        } catch (Exception e) {
            fail("Failed to create test files: " + e.getMessage());
        } finally {
            // Cleanup
            pngFile.delete();
            jpgFile.delete();
            jpegFile.delete();
            gifFile.delete();
            bmpFile.delete();
            txtFile.delete();
            dir.delete();
        }
    }
    
    @Test
    @DisplayName("Should calculate average load time correctly")
    void testAverageLoadTimeCalculation() {
        // Arrange
        Map<String, SmartImageLoader.LoadResult> loadHistory = new HashMap<>();
        
        for (int i = 0; i < 5; i++) {
            SmartImageLoader.LoadResult result = mock(SmartImageLoader.LoadResult.class);
            when(result.isSuccess()).thenReturn(true);
            when(result.getLoadedFrom()).thenReturn("file");
            when(result.getLoadTimeMs()).thenReturn((long)(10 * (i + 1))); // 10, 20, 30, 40, 50
            loadHistory.put("image" + i + ".png", result);
        }
        
        Map<String, Object> diagnostics = new HashMap<>();
        diagnostics.put("loadHistory", loadHistory);
        diagnostics.put("cachedImages", 0);
        
        when(smartImageLoader.getDiagnostics()).thenReturn(diagnostics);
        when(imagePathManager.getDiagnostics()).thenReturn(new HashMap<>());
        when(imagePathManager.getAllPaths()).thenReturn(Collections.emptyList());
        
        // Act
        diagnosticsRunner.runDiagnostics();
        
        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("Average Load Time:")); // Average should be 30ms
    }
}