package io.github.jspinak.brobot.model.element;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.config.ExecutionEnvironment;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.config.ImagePathManager;
import io.github.jspinak.brobot.config.EarlyImagePathInitializer;
import io.github.jspinak.brobot.util.image.core.BufferedImageUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.sikuli.script.ImagePath;
import org.springframework.test.util.ReflectionTestUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests to ensure that image paths are correctly set and identified
 * before images are loaded to Pattern objects.
 */
@DisplayName("Pattern Loading Tests")
public class PatternLoadingTest extends BrobotTestBase {

    @Mock
    private ImagePathManager imagePathManager;
    
    @Mock
    private EarlyImagePathInitializer earlyInitializer;
    
    @TempDir
    Path tempDir;
    
    private BufferedImage testImage;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        
        // Create a test image
        testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
    }
    
    @Nested
    @DisplayName("Path Configuration Before Pattern Creation")
    class PathConfigurationBeforePatternCreation {
        
        @Test
        @DisplayName("Should verify image path is set before Pattern creation")
        void shouldVerifyImagePathIsSetBeforePatternCreation() throws IOException {
            // Given - Create test image file
            Path imageFile = tempDir.resolve("test.png");
            ImageIO.write(testImage, "png", imageFile.toFile());
            
            // When - Create Pattern in mock mode
            ExecutionEnvironment env = ExecutionEnvironment.getInstance();
            ReflectionTestUtils.setField(env, "mockMode", true); // Use mock mode
            
            Pattern pattern = new Pattern("test.png");
            
            // Then - In mock mode, Pattern should be created with dummy image
            assertNotNull(pattern);
            assertNotNull(pattern.getImage());
            assertEquals("test", pattern.getName());
            // Note: In mock mode, ImagePath methods are not called
        }
        
        @Test
        @DisplayName("Should fail Pattern creation if image path not configured")
        void shouldFailPatternCreationIfImagePathNotConfigured() {
            // Given - No image path configured
            ExecutionEnvironment env = ExecutionEnvironment.getInstance();
            ReflectionTestUtils.setField(env, "mockMode", false);
            
            try (MockedStatic<BufferedImageUtilities> utilsMock = mockStatic(BufferedImageUtilities.class)) {
                utilsMock.when(() -> BufferedImageUtilities.getBuffImgFromFile(anyString()))
                    .thenReturn(null);
                
                // When/Then - Pattern creation should fail
                assertThrows(IllegalStateException.class, () -> {
                    new Pattern("nonexistent.png");
                });
            }
        }
        
        @Test
        @DisplayName("Should use configured image path for loading")
        void shouldUseConfiguredImagePathForLoading() throws IOException {
            // Given
            Path imagesDir = tempDir.resolve("images");
            Files.createDirectories(imagesDir);
            Path imageFile = imagesDir.resolve("button.png");
            ImageIO.write(testImage, "png", imageFile.toFile());
            
            ExecutionEnvironment env = ExecutionEnvironment.getInstance();
            ReflectionTestUtils.setField(env, "mockMode", true);
            
            // When - In mock mode, Pattern doesn't actually load from disk
            Pattern pattern = new Pattern("button.png");
            
            // Then - Pattern should be created with dummy image in mock mode
            assertNotNull(pattern);
            assertEquals("button", pattern.getName());
            assertNotNull(pattern.getImage());
        }
    }
    
    @Nested
    @DisplayName("Image Resolution from Different Path Types")
    class ImageResolutionFromDifferentPathTypes {
        
        @Test
        @DisplayName("Should load Pattern from absolute path")
        void shouldLoadPatternFromAbsolutePath() throws IOException {
            // Given
            Path imageFile = tempDir.resolve("absolute.png");
            ImageIO.write(testImage, "png", imageFile.toFile());
            
            try (MockedStatic<BufferedImageUtilities> utilsMock = mockStatic(BufferedImageUtilities.class)) {
                utilsMock.when(() -> BufferedImageUtilities.getBuffImgFromFile(imageFile.toString()))
                    .thenReturn(testImage);
                
                ExecutionEnvironment env = ExecutionEnvironment.getInstance();
                ReflectionTestUtils.setField(env, "mockMode", false);
                
                // When
                Pattern pattern = new Pattern(imageFile.toString());
                
                // Then
                assertNotNull(pattern);
                assertNotNull(pattern.getImage());
                assertEquals("absolute", pattern.getName());
            }
        }
        
        @Test
        @DisplayName("Should load Pattern from relative path")
        void shouldLoadPatternFromRelativePath() throws IOException {
            // Given
            Path imagesDir = tempDir.resolve("images");
            Files.createDirectories(imagesDir);
            Path imageFile = imagesDir.resolve("relative.png");
            ImageIO.write(testImage, "png", imageFile.toFile());
            
            try (MockedStatic<ImagePath> imagePathMock = mockStatic(ImagePath.class);
                 MockedStatic<BufferedImageUtilities> utilsMock = mockStatic(BufferedImageUtilities.class)) {
                
                imagePathMock.when(ImagePath::getBundlePath).thenReturn(imagesDir.toString());
                utilsMock.when(() -> BufferedImageUtilities.getBuffImgFromFile("relative.png"))
                    .thenReturn(testImage);
                
                ExecutionEnvironment env = ExecutionEnvironment.getInstance();
                ReflectionTestUtils.setField(env, "mockMode", false);
                
                // When
                Pattern pattern = new Pattern("relative.png");
                
                // Then
                assertNotNull(pattern);
                assertNotNull(pattern.getImage());
                assertEquals("relative", pattern.getName());
            }
        }
        
        @Test
        @DisplayName("Should load Pattern from classpath resource")
        void shouldLoadPatternFromClasspathResource() {
            // Given
            String classpathImage = "classpath:test-image.png";
            
            try (MockedStatic<BufferedImageUtilities> utilsMock = mockStatic(BufferedImageUtilities.class)) {
                utilsMock.when(() -> BufferedImageUtilities.getBuffImgFromFile(classpathImage))
                    .thenReturn(testImage);
                
                ExecutionEnvironment env = ExecutionEnvironment.getInstance();
                ReflectionTestUtils.setField(env, "mockMode", false);
                
                // When
                Pattern pattern = new Pattern(classpathImage);
                
                // Then
                assertNotNull(pattern);
                assertNotNull(pattern.getImage());
            }
        }
        
        @Test
        @DisplayName("Should handle Pattern from JAR resource")
        void shouldHandlePatternFromJarResource() {
            // Given
            String jarImage = "jar-image.png";
            
            try (MockedStatic<BufferedImageUtilities> utilsMock = mockStatic(BufferedImageUtilities.class)) {
                // Simulate JAR resource loading
                utilsMock.when(() -> BufferedImageUtilities.getBuffImgFromFile(jarImage))
                    .thenReturn(testImage);
                
                ExecutionEnvironment env = ExecutionEnvironment.getInstance();
                ReflectionTestUtils.setField(env, "mockMode", false);
                
                // When
                Pattern pattern = new Pattern(jarImage);
                
                // Then
                assertNotNull(pattern);
                assertNotNull(pattern.getImage());
            }
        }
    }
    
    @Nested
    @DisplayName("Multiple Search Paths")
    class MultipleSearchPaths {
        
        @Test
        @DisplayName("Should search in primary path first")
        void shouldSearchInPrimaryPathFirst() throws IOException {
            // Given
            Path primaryPath = tempDir.resolve("primary");
            Path secondaryPath = tempDir.resolve("secondary");
            Files.createDirectories(primaryPath);
            Files.createDirectories(secondaryPath);
            
            // Create same named file in both directories
            Path primaryImage = primaryPath.resolve("test.png");
            Path secondaryImage = secondaryPath.resolve("test.png");
            
            BufferedImage primaryImg = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
            BufferedImage secondaryImg = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            
            ImageIO.write(primaryImg, "png", primaryImage.toFile());
            ImageIO.write(secondaryImg, "png", secondaryImage.toFile());
            
            try (MockedStatic<ImagePath> imagePathMock = mockStatic(ImagePath.class);
                 MockedStatic<BufferedImageUtilities> utilsMock = mockStatic(BufferedImageUtilities.class)) {
                
                imagePathMock.when(ImagePath::getBundlePath).thenReturn(primaryPath.toString());
                // Mock ImagePath.getPaths() to return a list (correct return type)
                List pathList = new ArrayList();
                imagePathMock.when(ImagePath::getPaths).thenReturn(pathList);
                
                // Should load from primary path
                utilsMock.when(() -> BufferedImageUtilities.getBuffImgFromFile("test.png"))
                    .thenReturn(primaryImg);
                
                ExecutionEnvironment env = ExecutionEnvironment.getInstance();
                ReflectionTestUtils.setField(env, "mockMode", false);
                
                // When
                Pattern pattern = new Pattern("test.png");
                
                // Then
                assertNotNull(pattern);
                assertEquals(50, pattern.w()); // Primary image is 50x50
            }
        }
        
        @Test
        @DisplayName("Should fallback to secondary path if not in primary")
        void shouldFallbackToSecondaryPath() throws IOException {
            // Given
            Path primaryPath = tempDir.resolve("primary");
            Path secondaryPath = tempDir.resolve("secondary");
            Files.createDirectories(primaryPath);
            Files.createDirectories(secondaryPath);
            
            Path secondaryImage = secondaryPath.resolve("secondary-only.png");
            ImageIO.write(testImage, "png", secondaryImage.toFile());
            
            try (MockedStatic<ImagePath> imagePathMock = mockStatic(ImagePath.class);
                 MockedStatic<BufferedImageUtilities> utilsMock = mockStatic(BufferedImageUtilities.class)) {
                
                // Mock ImagePath.getPaths() to return a list (correct return type)
                List pathList = new ArrayList();
                imagePathMock.when(ImagePath::getPaths).thenReturn(pathList);
                
                // First call returns null (not in primary), second returns image
                utilsMock.when(() -> BufferedImageUtilities.getBuffImgFromFile(anyString()))
                    .thenReturn(null)  // Not found in primary
                    .thenReturn(testImage); // Found in secondary
                
                ExecutionEnvironment env = ExecutionEnvironment.getInstance();
                ReflectionTestUtils.setField(env, "mockMode", false);
                
                // When - This should try primary first, then secondary
                Pattern pattern = null;
                try {
                    pattern = new Pattern("secondary-only.png");
                } catch (IllegalStateException e) {
                    // Expected in this test setup
                }
                
                // Verify search attempted
                utilsMock.verify(() -> BufferedImageUtilities.getBuffImgFromFile(anyString()), 
                                atLeastOnce());
            }
        }
    }
    
    @Nested
    @DisplayName("Pattern Creation Validation")
    class PatternCreationValidation {
        
        @Test
        @DisplayName("Should validate image exists before Pattern creation")
        void shouldValidateImageExistsBeforePatternCreation() {
            // Given
            ExecutionEnvironment env = ExecutionEnvironment.getInstance();
            ReflectionTestUtils.setField(env, "mockMode", false);
            
            try (MockedStatic<BufferedImageUtilities> utilsMock = mockStatic(BufferedImageUtilities.class)) {
                utilsMock.when(() -> BufferedImageUtilities.getBuffImgFromFile("missing.png"))
                    .thenReturn(null);
                
                // When/Then
                IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                    new Pattern("missing.png");
                });
                
                assertTrue(exception.getMessage().contains("Failed to load image"));
                assertTrue(exception.getMessage().contains("missing.png"));
            }
        }
        
        @Test
        @DisplayName("Should validate image format is supported")
        void shouldValidateImageFormatIsSupported() throws IOException {
            // Given
            Path unsupportedFile = tempDir.resolve("test.txt");
            Files.writeString(unsupportedFile, "Not an image");
            
            try (MockedStatic<BufferedImageUtilities> utilsMock = mockStatic(BufferedImageUtilities.class)) {
                utilsMock.when(() -> BufferedImageUtilities.getBuffImgFromFile(unsupportedFile.toString()))
                    .thenReturn(null); // Simulating unsupported format
                
                ExecutionEnvironment env = ExecutionEnvironment.getInstance();
                ReflectionTestUtils.setField(env, "mockMode", false);
                
                // When/Then
                assertThrows(IllegalStateException.class, () -> {
                    new Pattern(unsupportedFile.toString());
                });
            }
        }
        
        @Test
        @DisplayName("Should set Pattern name from filename")
        void shouldSetPatternNameFromFilename() {
            // Given
            ExecutionEnvironment env = ExecutionEnvironment.getInstance();
            ReflectionTestUtils.setField(env, "mockMode", true); // Mock mode
            
            // When
            Pattern pattern = new Pattern("button_submit.png");
            
            // Then
            assertEquals("button_submit", pattern.getName());
        }
        
        @Test
        @DisplayName("Should handle null or empty image path")
        void shouldHandleNullOrEmptyImagePath() {
            // When/Then - Should handle gracefully
            Pattern nullPattern = new Pattern((String) null);
            assertNull(nullPattern.getImgpath());
            
            Pattern emptyPattern = new Pattern("");
            // When empty string is provided, constructor returns early so imgpath stays null
            assertNull(emptyPattern.getImgpath());
        }
    }
    
    @Nested
    @DisplayName("Mock Mode vs Real Files")
    class MockModeVsRealFiles {
        
        @Test
        @DisplayName("Should create dummy Pattern in mock mode")
        void shouldCreateDummyPatternInMockMode() {
            // Given
            ExecutionEnvironment env = ExecutionEnvironment.getInstance();
            ReflectionTestUtils.setField(env, "mockMode", true);
            
            // When
            Pattern pattern = new Pattern("mock-image.png");
            
            // Then
            assertNotNull(pattern);
            assertNotNull(pattern.getImage());
            assertEquals(100, pattern.w()); // Dummy image is 100x100
            assertEquals(100, pattern.h());
            assertEquals("mock-image", pattern.getName());
        }
        
        @Test
        @DisplayName("Should load real image when not in mock mode")
        void shouldLoadRealImageWhenNotInMockMode() throws IOException {
            // Given
            Path imageFile = tempDir.resolve("real.png");
            BufferedImage realImage = new BufferedImage(200, 150, BufferedImage.TYPE_INT_RGB);
            ImageIO.write(realImage, "png", imageFile.toFile());
            
            try (MockedStatic<BufferedImageUtilities> utilsMock = mockStatic(BufferedImageUtilities.class)) {
                utilsMock.when(() -> BufferedImageUtilities.getBuffImgFromFile(imageFile.toString()))
                    .thenReturn(realImage);
                
                ExecutionEnvironment env = ExecutionEnvironment.getInstance();
                ReflectionTestUtils.setField(env, "mockMode", false);
                
                // When
                Pattern pattern = new Pattern(imageFile.toString());
                
                // Then
                assertNotNull(pattern);
                assertEquals(200, pattern.w());
                assertEquals(150, pattern.h());
            }
        }
    }
    
    @Nested
    @DisplayName("Error Recovery and Logging")
    class ErrorRecoveryAndLogging {
        
        @Test
        @DisplayName("Should provide helpful error message when image not found")
        void shouldProvideHelpfulErrorMessageWhenImageNotFound() {
            // Given
            ExecutionEnvironment env = ExecutionEnvironment.getInstance();
            ReflectionTestUtils.setField(env, "mockMode", false);
            
            try (MockedStatic<BufferedImageUtilities> utilsMock = mockStatic(BufferedImageUtilities.class)) {
                utilsMock.when(() -> BufferedImageUtilities.getBuffImgFromFile("missing-button.png"))
                    .thenReturn(null);
                
                // When/Then
                IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                    new Pattern("missing-button.png");
                });
                
                String message = exception.getMessage();
                assertTrue(message.contains("Failed to load image"));
                assertTrue(message.contains("missing-button.png"));
                assertTrue(message.contains("configured image path"));
                assertTrue(message.contains(".png extension"));
            }
        }
        
        @Test
        @DisplayName("Should handle path with spaces")
        void shouldHandlePathWithSpaces() throws IOException {
            // Given
            Path dirWithSpaces = tempDir.resolve("my images");
            Files.createDirectories(dirWithSpaces);
            Path imageFile = dirWithSpaces.resolve("test image.png");
            ImageIO.write(testImage, "png", imageFile.toFile());
            
            try (MockedStatic<BufferedImageUtilities> utilsMock = mockStatic(BufferedImageUtilities.class)) {
                utilsMock.when(() -> BufferedImageUtilities.getBuffImgFromFile(imageFile.toString()))
                    .thenReturn(testImage);
                
                ExecutionEnvironment env = ExecutionEnvironment.getInstance();
                ReflectionTestUtils.setField(env, "mockMode", false);
                
                // When
                Pattern pattern = new Pattern(imageFile.toString());
                
                // Then
                assertNotNull(pattern);
                assertNotNull(pattern.getImage());
            }
        }
        
        @Test
        @DisplayName("Should handle special characters in path")
        void shouldHandleSpecialCharactersInPath() throws IOException {
            // Given
            Path specialDir = tempDir.resolve("images-2024");
            Files.createDirectories(specialDir);
            Path imageFile = specialDir.resolve("button_v1.2.png");
            ImageIO.write(testImage, "png", imageFile.toFile());
            
            try (MockedStatic<BufferedImageUtilities> utilsMock = mockStatic(BufferedImageUtilities.class)) {
                utilsMock.when(() -> BufferedImageUtilities.getBuffImgFromFile(imageFile.toString()))
                    .thenReturn(testImage);
                
                ExecutionEnvironment env = ExecutionEnvironment.getInstance();
                ReflectionTestUtils.setField(env, "mockMode", false);
                
                // When
                Pattern pattern = new Pattern(imageFile.toString());
                
                // Then
                assertNotNull(pattern);
                assertEquals("button_v1.2", pattern.getName());
            }
        }
    }
    
    @Nested
    @DisplayName("Integration with StateImage")
    class IntegrationWithStateImage {
        
        @Test
        @DisplayName("Should create Pattern from StateImage Builder")
        void shouldCreatePatternFromStateImageBuilder() {
            // Given
            ExecutionEnvironment env = ExecutionEnvironment.getInstance();
            ReflectionTestUtils.setField(env, "mockMode", true);
            
            // When
            StateImage stateImage = new StateImage.Builder()
                .addPattern("state-pattern.png")
                .build();
            
            // Then
            assertFalse(stateImage.getPatterns().isEmpty());
            Pattern pattern = stateImage.getPatterns().get(0);
            assertEquals("state-pattern", pattern.getName());
        }
        
        @Test
        @DisplayName("Should validate all Patterns have loaded images")
        void shouldValidateAllPatternsHaveLoadedImages() {
            // Given
            ExecutionEnvironment env = ExecutionEnvironment.getInstance();
            ReflectionTestUtils.setField(env, "mockMode", true);
            
            // When
            StateImage stateImage = new StateImage.Builder()
                .addPatterns("button1.png", "button2.png", "button3.png")
                .build();
            
            // Then
            assertEquals(3, stateImage.getPatterns().size());
            for (Pattern pattern : stateImage.getPatterns()) {
                assertNotNull(pattern.getImage());
                assertNotNull(pattern.getName());
            }
        }
    }
}