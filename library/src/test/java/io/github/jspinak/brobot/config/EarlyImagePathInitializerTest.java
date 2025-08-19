package io.github.jspinak.brobot.config;

import io.github.jspinak.brobot.test.BrobotTestBase;
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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for EarlyImagePathInitializer ensuring image paths are set
 * before Pattern objects are created.
 */
@DisplayName("EarlyImagePathInitializer Tests")
public class EarlyImagePathInitializerTest extends BrobotTestBase {

    private EarlyImagePathInitializer initializer;
    
    @Mock
    private BrobotProperties brobotProperties;
    
    @Mock
    private BrobotProperties.Core coreConfig;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        
        initializer = new EarlyImagePathInitializer();
        ReflectionTestUtils.setField(initializer, "brobotProperties", brobotProperties);
    }
    
    @Nested
    @DisplayName("Initialization Timing")
    class InitializationTiming {
        
        @Test
        @DisplayName("Should initialize paths in @PostConstruct")
        void shouldInitializePathsInPostConstruct() {
            // Given
            String imagePath = tempDir.toString();
            ReflectionTestUtils.setField(initializer, "primaryImagePath", imagePath);
            
            try (MockedStatic<ImagePath> imagePathMock = mockStatic(ImagePath.class)) {
                // When
                initializer.initializeImagePaths();
                
                // Then
                imagePathMock.verify(() -> ImagePath.setBundlePath(imagePath), times(1));
                imagePathMock.verify(() -> ImagePath.add(imagePath), times(1));
            }
        }
        
        @Test
        @DisplayName("Should run before state processing")
        void shouldRunBeforeStateProcessing() {
            // Given
            String imagePath = "early-init-images";
            ReflectionTestUtils.setField(initializer, "primaryImagePath", imagePath);
            
            try (MockedStatic<ImagePath> imagePathMock = mockStatic(ImagePath.class)) {
                // When
                initializer.initializeImagePaths();
                
                // Then - Path should be set immediately
                imagePathMock.verify(() -> ImagePath.setBundlePath(anyString()), times(1));
            }
        }
    }
    
    @Nested
    @DisplayName("Configuration Priority")
    class ConfigurationPriority {
        
        @Test
        @DisplayName("Should prefer BrobotProperties over @Value")
        void shouldPreferBrobotPropertiesOverValue() {
            // Given
            String valueImagePath = "value-images";
            String propertiesImagePath = tempDir.resolve("properties-images").toString();
            
            ReflectionTestUtils.setField(initializer, "primaryImagePath", valueImagePath);
            when(brobotProperties.getCore()).thenReturn(coreConfig);
            when(coreConfig.getImagePath()).thenReturn(propertiesImagePath);
            
            try (MockedStatic<ImagePath> imagePathMock = mockStatic(ImagePath.class)) {
                // When
                initializer.initializeImagePaths();
                
                // Then
                imagePathMock.verify(() -> ImagePath.setBundlePath(propertiesImagePath), times(1));
            }
        }
        
        @Test
        @DisplayName("Should use @Value when BrobotProperties not available")
        void shouldUseValueWhenPropertiesNotAvailable() {
            // Given
            String valueImagePath = tempDir.toString();
            ReflectionTestUtils.setField(initializer, "primaryImagePath", valueImagePath);
            ReflectionTestUtils.setField(initializer, "brobotProperties", null);
            
            try (MockedStatic<ImagePath> imagePathMock = mockStatic(ImagePath.class)) {
                // When
                initializer.initializeImagePaths();
                
                // Then
                imagePathMock.verify(() -> ImagePath.setBundlePath(valueImagePath), times(1));
            }
        }
        
        @Test
        @DisplayName("Should default to 'images' when no configuration")
        void shouldDefaultToImagesWhenNoConfiguration() {
            // Given
            ReflectionTestUtils.setField(initializer, "primaryImagePath", null);
            ReflectionTestUtils.setField(initializer, "brobotProperties", null);
            
            try (MockedStatic<ImagePath> imagePathMock = mockStatic(ImagePath.class)) {
                // When
                initializer.initializeImagePaths();
                
                // Then
                imagePathMock.verify(() -> ImagePath.setBundlePath("images"), times(1));
            }
        }
    }
    
    @Nested
    @DisplayName("Path Normalization")
    class PathNormalization {
        
        @Test
        @DisplayName("Should remove trailing slash")
        void shouldRemoveTrailingSlash() {
            // Given
            String pathWithSlash = tempDir.toString() + "/";
            ReflectionTestUtils.setField(initializer, "primaryImagePath", pathWithSlash);
            
            try (MockedStatic<ImagePath> imagePathMock = mockStatic(ImagePath.class)) {
                // When
                initializer.initializeImagePaths();
                
                // Then
                String expectedPath = tempDir.toString();
                imagePathMock.verify(() -> ImagePath.setBundlePath(expectedPath), times(1));
            }
        }
        
        @Test
        @DisplayName("Should remove trailing backslash")
        void shouldRemoveTrailingBackslash() {
            // Given
            String pathWithBackslash = tempDir.toString() + "\\";
            ReflectionTestUtils.setField(initializer, "primaryImagePath", pathWithBackslash);
            
            try (MockedStatic<ImagePath> imagePathMock = mockStatic(ImagePath.class)) {
                // When
                initializer.initializeImagePaths();
                
                // Then
                String expectedPath = tempDir.toString();
                imagePathMock.verify(() -> ImagePath.setBundlePath(expectedPath), times(1));
            }
        }
        
        @Test
        @DisplayName("Should handle empty path")
        void shouldHandleEmptyPath() {
            // Given
            ReflectionTestUtils.setField(initializer, "primaryImagePath", "");
            
            try (MockedStatic<ImagePath> imagePathMock = mockStatic(ImagePath.class)) {
                // When
                initializer.initializeImagePaths();
                
                // Then - Should use default
                imagePathMock.verify(() -> ImagePath.setBundlePath("images"), times(1));
            }
        }
    }
    
    @Nested
    @DisplayName("Directory Creation")
    class DirectoryCreation {
        
        @Test
        @DisplayName("Should create directory if not exists")
        void shouldCreateDirectoryIfNotExists() {
            // Given
            Path nonExistentPath = tempDir.resolve("to-be-created");
            ReflectionTestUtils.setField(initializer, "primaryImagePath", nonExistentPath.toString());
            
            try (MockedStatic<ImagePath> imagePathMock = mockStatic(ImagePath.class)) {
                // When
                initializer.initializeImagePaths();
                
                // Then
                assertTrue(Files.exists(nonExistentPath));
            }
        }
        
        @Test
        @DisplayName("Should not recreate existing directory")
        void shouldNotRecreateExistingDirectory() throws Exception {
            // Given
            Path existingPath = tempDir.resolve("existing");
            Files.createDirectories(existingPath);
            
            // Create a test file in the directory
            Path testFile = existingPath.resolve("test.png");
            Files.createFile(testFile);
            
            ReflectionTestUtils.setField(initializer, "primaryImagePath", existingPath.toString());
            
            try (MockedStatic<ImagePath> imagePathMock = mockStatic(ImagePath.class)) {
                // When
                initializer.initializeImagePaths();
                
                // Then - Test file should still exist
                assertTrue(Files.exists(testFile));
            }
        }
    }
    
    @Nested
    @DisplayName("SikuliX Configuration")
    class SikuliXConfiguration {
        
        @Test
        @DisplayName("Should set both bundle path and add path")
        void shouldSetBothBundlePathAndAddPath() {
            // Given
            String imagePath = tempDir.toString();
            ReflectionTestUtils.setField(initializer, "primaryImagePath", imagePath);
            
            try (MockedStatic<ImagePath> imagePathMock = mockStatic(ImagePath.class)) {
                // When
                initializer.initializeImagePaths();
                
                // Then
                imagePathMock.verify(() -> ImagePath.setBundlePath(imagePath), times(1));
                imagePathMock.verify(() -> ImagePath.add(imagePath), times(1));
            }
        }
        
        @Test
        @DisplayName("Should verify bundle path is set")
        void shouldVerifyBundlePathIsSet() {
            // Given
            String imagePath = tempDir.toString();
            ReflectionTestUtils.setField(initializer, "primaryImagePath", imagePath);
            
            try (MockedStatic<ImagePath> imagePathMock = mockStatic(ImagePath.class)) {
                imagePathMock.when(ImagePath::getBundlePath).thenReturn(imagePath);
                
                // When
                initializer.initializeImagePaths();
                
                // Then
                imagePathMock.verify(ImagePath::getBundlePath, atLeastOnce());
            }
        }
        
        @Test
        @DisplayName("Should get all configured paths")
        void shouldGetAllConfiguredPaths() {
            // Given
            String imagePath = tempDir.toString();
            ReflectionTestUtils.setField(initializer, "primaryImagePath", imagePath);
            
            try (MockedStatic<ImagePath> imagePathMock = mockStatic(ImagePath.class)) {
                String[] paths = {imagePath, "/other/path"};
                imagePathMock.when(ImagePath::getPaths).thenReturn(paths);
                
                // When
                initializer.initializeImagePaths();
                
                // Then
                imagePathMock.verify(ImagePath::getPaths, atLeastOnce());
            }
        }
    }
    
    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {
        
        @Test
        @DisplayName("Should handle SikuliX exceptions gracefully")
        void shouldHandleSikuliXExceptions() {
            // Given
            String imagePath = tempDir.toString();
            ReflectionTestUtils.setField(initializer, "primaryImagePath", imagePath);
            
            try (MockedStatic<ImagePath> imagePathMock = mockStatic(ImagePath.class)) {
                imagePathMock.when(() -> ImagePath.setBundlePath(anyString()))
                    .thenThrow(new RuntimeException("SikuliX error"));
                
                // When/Then - Should not throw
                assertDoesNotThrow(() -> initializer.initializeImagePaths());
            }
        }
        
        @Test
        @DisplayName("Should handle directory creation failures")
        void shouldHandleDirectoryCreationFailures() {
            // Given
            String invalidPath = "\0invalid\0path"; // Invalid characters
            ReflectionTestUtils.setField(initializer, "primaryImagePath", invalidPath);
            
            try (MockedStatic<ImagePath> imagePathMock = mockStatic(ImagePath.class)) {
                // When/Then - Should handle gracefully
                assertDoesNotThrow(() -> initializer.initializeImagePaths());
            }
        }
    }
    
    @Nested
    @DisplayName("Integration with Spring Context")
    class SpringIntegration {
        
        @Test
        @DisplayName("Should handle missing optional dependencies")
        void shouldHandleMissingOptionalDependencies() {
            // Given
            ReflectionTestUtils.setField(initializer, "brobotProperties", null);
            ReflectionTestUtils.setField(initializer, "primaryImagePath", tempDir.toString());
            
            try (MockedStatic<ImagePath> imagePathMock = mockStatic(ImagePath.class)) {
                // When
                initializer.initializeImagePaths();
                
                // Then - Should still work
                imagePathMock.verify(() -> ImagePath.setBundlePath(anyString()), times(1));
            }
        }
        
        @Test
        @DisplayName("Should log initialization details")
        void shouldLogInitializationDetails() {
            // Given
            String imagePath = tempDir.toString();
            ReflectionTestUtils.setField(initializer, "primaryImagePath", imagePath);
            
            try (MockedStatic<ImagePath> imagePathMock = mockStatic(ImagePath.class)) {
                imagePathMock.when(ImagePath::getBundlePath).thenReturn(imagePath);
                imagePathMock.when(ImagePath::getPaths).thenReturn(new String[]{imagePath});
                
                // When
                initializer.initializeImagePaths();
                
                // Then - Would log various details
                imagePathMock.verify(ImagePath::getBundlePath, atLeastOnce());
                imagePathMock.verify(ImagePath::getPaths, atLeastOnce());
            }
        }
    }
}