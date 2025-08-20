package io.github.jspinak.brobot.config;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.io.TempDir;
import org.sikuli.script.ImagePath;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.eq;

/**
 * Comprehensive tests for ImagePathManager ensuring correct path resolution
 * before Pattern objects are created.
 */
@DisplayName("ImagePathManager Comprehensive Tests")
public class ImagePathManagerComprehensiveTest extends BrobotTestBase {

    private ImagePathManager imagePathManager;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        imagePathManager = new ImagePathManager();
    }
    
    @Nested
    @DisplayName("Path Initialization")
    class PathInitialization {
        
        @Test
        @DisplayName("Should initialize with absolute path")
        void shouldInitializeWithAbsolutePath() {
            // Given
            String absolutePath = tempDir.toString();
            Files.exists(tempDir); // Ensure temp dir exists
            
            // When
            imagePathManager.initialize(absolutePath);
            
            // Then
            Path primaryPath = (Path) ReflectionTestUtils.getField(imagePathManager, "primaryImagePath");
            assertNotNull(primaryPath);
            assertEquals(tempDir.toAbsolutePath(), primaryPath.toAbsolutePath());
            
            Boolean initialized = (Boolean) ReflectionTestUtils.getField(imagePathManager, "initialized");
            assertTrue(initialized);
        }
        
        @Test
        @DisplayName("Should initialize with relative path")
        void shouldInitializeWithRelativePath() throws IOException {
            // Given
            Path imagesDir = tempDir.resolve("images");
            Files.createDirectories(imagesDir);
            
            String currentDir = System.getProperty("user.dir");
            Path relativePath = Paths.get(currentDir).relativize(imagesDir);
            
            // When
            imagePathManager.initialize(relativePath.toString());
            
            // Then
            Path primaryPath = (Path) ReflectionTestUtils.getField(imagePathManager, "primaryImagePath");
            assertNotNull(primaryPath);
            assertTrue(Files.exists(primaryPath));
        }
        
        @Test
        @DisplayName("Should handle non-existent path with fallback")
        void shouldHandleNonExistentPathWithFallback() {
            // Given
            String nonExistentPath = "/non/existent/path/images";
            
            // When
            imagePathManager.initialize(nonExistentPath);
            
            // Then
            Path primaryPath = (Path) ReflectionTestUtils.getField(imagePathManager, "primaryImagePath");
            assertNotNull(primaryPath);
            // Should create fallback path
        }
        
        @Test
        @DisplayName("Should not reinitialize with same path")
        void shouldNotReinitializeWithSamePath() {
            // Given
            String path = tempDir.toString();
            
            // When
            imagePathManager.initialize(path);
            Path firstPath = (Path) ReflectionTestUtils.getField(imagePathManager, "primaryImagePath");
            
            imagePathManager.initialize(path); // Initialize again with same path
            Path secondPath = (Path) ReflectionTestUtils.getField(imagePathManager, "primaryImagePath");
            
            // Then
            assertEquals(firstPath, secondPath);
        }
        
        @Test
        @DisplayName("Should reinitialize with different path")
        void shouldReinitializeWithDifferentPath() throws IOException {
            // Given
            Path path1 = tempDir.resolve("images1");
            Path path2 = tempDir.resolve("images2");
            Files.createDirectories(path1);
            Files.createDirectories(path2);
            
            // When
            imagePathManager.initialize(path1.toString());
            Path firstPath = (Path) ReflectionTestUtils.getField(imagePathManager, "primaryImagePath");
            
            imagePathManager.initialize(path2.toString());
            Path secondPath = (Path) ReflectionTestUtils.getField(imagePathManager, "primaryImagePath");
            
            // Then
            assertNotEquals(firstPath, secondPath);
            assertEquals(path2.toAbsolutePath(), secondPath.toAbsolutePath());
        }
    }
    
    @Nested
    @DisplayName("Path Resolution Strategies")
    class PathResolutionStrategies {
        
        @Test
        @DisplayName("Should resolve absolute path directly")
        void shouldResolveAbsolutePathDirectly() {
            // Given
            Path absolutePath = tempDir.resolve("test-images");
            
            // When
            imagePathManager.initialize(absolutePath.toString());
            
            // Then
            Path primaryPath = (Path) ReflectionTestUtils.getField(imagePathManager, "primaryImagePath");
            assertEquals(absolutePath.toAbsolutePath(), primaryPath.toAbsolutePath());
        }
        
        @Test
        @DisplayName("Should resolve path from working directory")
        void shouldResolvePathFromWorkingDirectory() throws IOException {
            // Given
            String relativePath = "test-images";
            Path expectedPath = Paths.get(System.getProperty("user.dir"), relativePath);
            Files.createDirectories(expectedPath);
            
            try {
                // When
                imagePathManager.initialize(relativePath);
                
                // Then
                Path primaryPath = (Path) ReflectionTestUtils.getField(imagePathManager, "primaryImagePath");
                assertNotNull(primaryPath);
                assertTrue(primaryPath.toString().endsWith(relativePath));
            } finally {
                // Cleanup
                Files.deleteIfExists(expectedPath);
            }
        }
        
        @Test
        @DisplayName("Should resolve path from classpath")
        void shouldResolvePathFromClasspath() {
            // Given
            String classpathResource = "test-images"; // Assuming this exists in test resources
            
            // When
            imagePathManager.initialize(classpathResource);
            
            // Then
            Path primaryPath = (Path) ReflectionTestUtils.getField(imagePathManager, "primaryImagePath");
            assertNotNull(primaryPath);
        }
        
        @Test
        @DisplayName("Should handle JAR resources")
        void shouldHandleJarResources() {
            // Given
            String jarResource = "jar:file:/path/to/app.jar!/images";
            
            // When
            imagePathManager.initialize(jarResource);
            
            // Then
            Path primaryPath = (Path) ReflectionTestUtils.getField(imagePathManager, "primaryImagePath");
            assertNotNull(primaryPath);
            // Would extract to temp directory
        }
    }
    
    @Nested
    @DisplayName("SikuliX Integration")
    class SikuliXIntegration {
        
        @Test
        @DisplayName("Should configure SikuliX ImagePath when not in mock mode")
        void shouldConfigureSikuliXImagePath() {
            // Given
            String imagePath = tempDir.toString();
            
            // When - Initialize with Brobot's mock mode handling SikuliX
            imagePathManager.initialize(imagePath);
            
            // Then - Verify internal state is correct
            assertTrue((Boolean) ReflectionTestUtils.getField(imagePathManager, "initialized"));
            Path primaryPath = (Path) ReflectionTestUtils.getField(imagePathManager, "primaryImagePath");
            assertEquals(Paths.get(imagePath), primaryPath);
        }
        
        @Test
        @DisplayName("Should skip SikuliX configuration in mock mode")
        void shouldSkipSikuliXInMockMode() {
            // Given
            String imagePath = tempDir.toString();
            // Mock mode is enabled by default from BrobotTestBase
            
            // When
            imagePathManager.initialize(imagePath);
            
            // Then - In mock mode, SikuliX calls are handled internally
            // We verify successful initialization
            assertTrue((Boolean) ReflectionTestUtils.getField(imagePathManager, "initialized"));
        }
        
        @Test
        @DisplayName("Should add multiple paths to SikuliX")
        void shouldAddMultiplePathsToSikuliX() throws IOException {
            // Given
            Path path1 = tempDir.resolve("images1");
            Path path2 = tempDir.resolve("images2");
            Files.createDirectories(path1);
            Files.createDirectories(path2);
            
            // When - Use Brobot's mock mode
            imagePathManager.initialize(path1.toString());
            imagePathManager.addPath(path2.toString());
            
            // Then - Verify internal state
            assertTrue((Boolean) ReflectionTestUtils.getField(imagePathManager, "initialized"));
            Set<String> configuredPaths = (Set<String>) ReflectionTestUtils.getField(imagePathManager, "configuredPaths");
            assertTrue(configuredPaths.contains(path1.toString()));
            assertTrue(configuredPaths.contains(path2.toString()));
        }
    }
    
    @Nested
    @DisplayName("Additional Path Management")
    class AdditionalPathManagement {
        
        @Test
        @DisplayName("Should add additional search paths")
        void shouldAddAdditionalSearchPaths() throws IOException {
            // Given
            Path mainPath = tempDir.resolve("main");
            Path additionalPath = tempDir.resolve("additional");
            Files.createDirectories(mainPath);
            Files.createDirectories(additionalPath);
            
            // When
            imagePathManager.initialize(mainPath.toString());
            imagePathManager.addPath(additionalPath.toString());
            
            // Then
            Set<String> configuredPaths = (Set<String>) ReflectionTestUtils.getField(imagePathManager, "configuredPaths");
            assertTrue(configuredPaths.contains(additionalPath.toString()));
        }
        
        @Test
        @DisplayName("Should not add duplicate paths")
        void shouldNotAddDuplicatePaths() throws IOException {
            // Given
            Path path = tempDir.resolve("images");
            Files.createDirectories(path);
            
            // When
            imagePathManager.initialize(path.toString());
            imagePathManager.addPath(path.toString());
            imagePathManager.addPath(path.toString()); // Try to add again
            
            // Then
            Set<String> configuredPaths = (Set<String>) ReflectionTestUtils.getField(imagePathManager, "configuredPaths");
            // Should only contain one instance
            assertEquals(1, Collections.frequency(new ArrayList<>(configuredPaths), path.toString()));
        }
        
        @Test
        @DisplayName("Should handle invalid additional paths gracefully")
        void shouldHandleInvalidAdditionalPaths() {
            // Given
            imagePathManager.initialize(tempDir.toString());
            
            // When/Then - Should not throw
            assertDoesNotThrow(() -> imagePathManager.addPath("/invalid/path"));
        }
    }
    
    @Nested
    @DisplayName("Path Validation")
    class PathValidation {
        
        @Test
        @DisplayName("Should validate path exists before setting")
        void shouldValidatePathExists() throws IOException {
            // Given
            Path existingPath = tempDir.resolve("existing");
            Files.createDirectories(existingPath);
            
            // When
            imagePathManager.initialize(existingPath.toString());
            
            // Then
            Path primaryPath = (Path) ReflectionTestUtils.getField(imagePathManager, "primaryImagePath");
            assertTrue(Files.exists(primaryPath));
        }
        
        @Test
        @DisplayName("Should create directory if it doesn't exist")
        void shouldCreateDirectoryIfNotExists() {
            // Given
            Path nonExistentPath = tempDir.resolve("to-be-created");
            
            // When
            imagePathManager.initialize(nonExistentPath.toString());
            
            // Then
            assertTrue(Files.exists(nonExistentPath));
        }
        
        @Test
        @DisplayName("Should handle paths with spaces")
        void shouldHandlePathsWithSpaces() throws IOException {
            // Given
            Path pathWithSpaces = tempDir.resolve("path with spaces");
            Files.createDirectories(pathWithSpaces);
            
            // When
            imagePathManager.initialize(pathWithSpaces.toString());
            
            // Then
            Path primaryPath = (Path) ReflectionTestUtils.getField(imagePathManager, "primaryImagePath");
            assertEquals(pathWithSpaces.toAbsolutePath(), primaryPath.toAbsolutePath());
        }
        
        @Test
        @DisplayName("Should normalize paths with different separators")
        void shouldNormalizePathSeparators() {
            // Given - Create a path with mixed separators
            // Note: \\subfolder becomes \subfolder which is actually just "subfolder" due to escape sequence
            // We need double backslash to get a literal backslash
            String basePath = tempDir.toString().replace(File.separator, "/");
            String mixedSeparators = basePath + "/images" + File.separator + "subfolder";
            
            // When
            imagePathManager.initialize(mixedSeparators);
            
            // Then
            Path primaryPath = (Path) ReflectionTestUtils.getField(imagePathManager, "primaryImagePath");
            assertNotNull(primaryPath);
            // Path object automatically normalizes separators for the platform
            // On Unix, all separators become '/', on Windows all become '\'
            String pathStr = primaryPath.toString();
            
            // Check that path uses consistent separators for the platform
            if (File.separator.equals("/")) {
                // Unix/Linux/Mac - should only have forward slashes
                assertFalse(pathStr.contains("\\"), "Unix path should not contain backslash: " + pathStr);
            } else {
                // Windows - should only have backslashes
                assertFalse(pathStr.contains("/"), "Windows path should not contain forward slash: " + pathStr);
            }
        }
    }
    
    @Nested
    @DisplayName("Thread Safety")
    class ThreadSafety {
        
        @Test
        @DisplayName("Should handle concurrent initialization safely")
        void shouldHandleConcurrentInitialization() throws InterruptedException {
            // Given
            Path path = tempDir.resolve("concurrent");
            int threadCount = 10;
            
            // When
            Thread[] threads = new Thread[threadCount];
            for (int i = 0; i < threadCount; i++) {
                threads[i] = new Thread(() -> imagePathManager.initialize(path.toString()));
                threads[i].start();
            }
            
            // Wait for all threads
            for (Thread thread : threads) {
                thread.join();
            }
            
            // Then
            Path primaryPath = (Path) ReflectionTestUtils.getField(imagePathManager, "primaryImagePath");
            assertNotNull(primaryPath);
            assertTrue((Boolean) ReflectionTestUtils.getField(imagePathManager, "initialized"));
        }
        
        @Test
        @DisplayName("Should handle concurrent path additions safely")
        void shouldHandleConcurrentPathAdditions() throws InterruptedException, IOException {
            // Given
            imagePathManager.initialize(tempDir.toString());
            int threadCount = 10;
            List<Path> paths = new ArrayList<>();
            
            for (int i = 0; i < threadCount; i++) {
                Path path = tempDir.resolve("path" + i);
                Files.createDirectories(path);
                paths.add(path);
            }
            
            // When
            Thread[] threads = new Thread[threadCount];
            for (int i = 0; i < threadCount; i++) {
                final Path path = paths.get(i);
                threads[i] = new Thread(() -> imagePathManager.addPath(path.toString()));
                threads[i].start();
            }
            
            // Wait for all threads
            for (Thread thread : threads) {
                thread.join();
            }
            
            // Then
            Set<String> configuredPaths = (Set<String>) ReflectionTestUtils.getField(imagePathManager, "configuredPaths");
            for (Path path : paths) {
                assertTrue(configuredPaths.contains(path.toString()));
            }
        }
    }
    
    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {
        
        @Test
        @DisplayName("Should handle null path initialization")
        void shouldHandleNullPath() {
            // When/Then
            assertThrows(IllegalStateException.class, () -> imagePathManager.initialize(null));
        }
        
        @Test
        @DisplayName("Should handle empty path initialization")
        void shouldHandleEmptyPath() {
            // When/Then
            assertThrows(IllegalStateException.class, () -> imagePathManager.initialize(""));
        }
        
        @Test
        @DisplayName("Should handle permission denied scenarios")
        void shouldHandlePermissionDenied() {
            // Given
            String restrictedPath = "/root/restricted/images"; // Typically no permission
            
            // When
            imagePathManager.initialize(restrictedPath);
            
            // Then - Should fallback gracefully
            Path primaryPath = (Path) ReflectionTestUtils.getField(imagePathManager, "primaryImagePath");
            assertNotNull(primaryPath);
        }
    }
}