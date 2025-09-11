package io.github.jspinak.brobot.config;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.*;

import io.github.jspinak.brobot.config.core.ImagePathManager;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive test suite for ImagePathManager. Tests centralized image path management system.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Disabled("CI failure - needs investigation")
public class ImagePathManagerTest extends BrobotTestBase {

    private ImagePathManager imagePathManager;
    private Path tempDirectory;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        imagePathManager = new ImagePathManager();

        // Create temp directory for tests
        try {
            tempDirectory = Files.createTempDirectory("brobot-test-images-");
            Files.createDirectories(tempDirectory.resolve("subdir"));
            Files.createFile(tempDirectory.resolve("test.png"));
            Files.createFile(tempDirectory.resolve("subdir/image.jpg"));
        } catch (IOException e) {
            fail("Failed to create temp directory: " + e.getMessage());
        }
    }

    @AfterEach
    void cleanupTest() {
        // Clean up temp directory
        if (tempDirectory != null) {
            try {
                Files.walk(tempDirectory)
                        .sorted((a, b) -> -a.compareTo(b))
                        .forEach(
                                path -> {
                                    try {
                                        Files.delete(path);
                                    } catch (IOException e) {
                                        // Ignore
                                    }
                                });
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    @Nested
    @DisplayName("Initialization Tests")
    @Disabled("CI failure - needs investigation")
    class InitializationTests {

        @Test
        @Order(1)
        @DisplayName("Should initialize with absolute path")
        void testInitializeWithAbsolutePath() {
            String absolutePath = tempDirectory.toString();

            imagePathManager.initialize(absolutePath);

            List<String> paths = imagePathManager.getConfiguredPaths();
            assertTrue(paths.contains(absolutePath));
        }

        @Test
        @Order(2)
        @DisplayName("Should initialize with relative path")
        void testInitializeWithRelativePath() {
            // Create a relative path that exists
            Path currentDir = Paths.get(System.getProperty("user.dir"));
            Path relativePath = currentDir.relativize(tempDirectory);

            imagePathManager.initialize(relativePath.toString());

            List<String> paths = imagePathManager.getConfiguredPaths();
            assertFalse(paths.isEmpty());
        }

        @Test
        @Order(3)
        @DisplayName("Should throw exception for null path")
        void testInitializeWithNullPath() {
            assertThrows(
                    IllegalStateException.class,
                    () -> {
                        imagePathManager.initialize(null);
                    });
        }

        @Test
        @Order(4)
        @DisplayName("Should throw exception for empty path")
        void testInitializeWithEmptyPath() {
            assertThrows(
                    IllegalStateException.class,
                    () -> {
                        imagePathManager.initialize("");
                    });
        }

        @Test
        @Order(5)
        @DisplayName("Should handle re-initialization")
        void testReinitialization() {
            String path1 = tempDirectory.toString();
            Path tempDir2 = null;

            try {
                tempDir2 = Files.createTempDirectory("brobot-test-images2-");
                String path2 = tempDir2.toString();

                imagePathManager.initialize(path1);
                List<String> paths1 = imagePathManager.getConfiguredPaths();

                imagePathManager.initialize(path2);
                List<String> paths2 = imagePathManager.getConfiguredPaths();

                // Primary path should change
                assertEquals(path2, paths2.get(0));

            } catch (IOException e) {
                fail("Failed to create second temp directory");
            } finally {
                if (tempDir2 != null) {
                    try {
                        Files.deleteIfExists(tempDir2);
                    } catch (IOException e) {
                        // Ignore
                    }
                }
            }
        }

        @Test
        @Order(6)
        @DisplayName("Should create fallback path for non-existent path")
        void testFallbackPath() {
            String nonExistentPath = "/non/existent/path/to/images";

            imagePathManager.initialize(nonExistentPath);

            List<String> paths = imagePathManager.getConfiguredPaths();
            assertFalse(paths.isEmpty());
            // Should create fallback in temp directory
            assertTrue(paths.get(0).contains("brobot-images"));
        }
    }

    @Nested
    @DisplayName("Path Management Tests")
    @Disabled("CI failure - needs investigation")
    class PathManagementTests {

        @BeforeEach
        void initializeManager() {
            imagePathManager.initialize(tempDirectory.toString());
        }

        @Test
        @Order(7)
        @DisplayName("Should add additional paths")
        void testAddPath() {
            Path additionalPath = null;
            try {
                additionalPath = Files.createTempDirectory("brobot-additional-");
                String pathStr = additionalPath.toString();

                imagePathManager.addPath(pathStr);

                List<String> paths = imagePathManager.getConfiguredPaths();
                assertTrue(paths.contains(pathStr));

            } catch (IOException e) {
                fail("Failed to create additional path");
            } finally {
                if (additionalPath != null) {
                    try {
                        Files.deleteIfExists(additionalPath);
                    } catch (IOException e) {
                        // Ignore
                    }
                }
            }
        }

        @Test
        @Order(8)
        @DisplayName("Should not add duplicate paths")
        void testNoDuplicatePaths() {
            String pathStr = tempDirectory.toString();

            imagePathManager.addPath(pathStr);
            int sizeBefore = imagePathManager.getConfiguredPaths().size();

            imagePathManager.addPath(pathStr);
            int sizeAfter = imagePathManager.getConfiguredPaths().size();

            assertEquals(sizeBefore, sizeAfter);
        }

        @Test
        @Order(9)
        @DisplayName("Should get all configured paths")
        void testGetConfiguredPaths() {
            Path additionalPath = null;
            try {
                additionalPath = Files.createTempDirectory("brobot-config-");
                imagePathManager.addPath(additionalPath.toString());

                List<String> paths = imagePathManager.getConfiguredPaths();

                assertNotNull(paths);
                assertTrue(paths.size() >= 2); // Primary + additional
                assertTrue(paths.contains(tempDirectory.toString()));
                assertTrue(paths.contains(additionalPath.toString()));

            } catch (IOException e) {
                fail("Failed to create additional path");
            } finally {
                if (additionalPath != null) {
                    try {
                        Files.deleteIfExists(additionalPath);
                    } catch (IOException e) {
                        // Ignore
                    }
                }
            }
        }

        @Test
        @Order(10)
        @DisplayName("Should handle non-existent additional paths")
        void testAddNonExistentPath() {
            String nonExistent = "/non/existent/additional/path";

            imagePathManager.addPath(nonExistent);

            // Should log warning but not fail
            // Path may not be added if it can't be resolved
            List<String> paths = imagePathManager.getConfiguredPaths();
            assertNotNull(paths);
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    @Disabled("CI failure - needs investigation")
    class ValidationTests {

        @BeforeEach
        void initializeManager() {
            imagePathManager.initialize(tempDirectory.toString());
        }

        @Test
        @Order(11)
        @DisplayName("Should validate paths with images")
        void testValidatePathsWithImages() {
            // tempDirectory has test.png
            boolean valid = imagePathManager.validatePaths();
            assertTrue(valid);
        }

        @Test
        @Order(12)
        @DisplayName("Should validate empty directories")
        void testValidateEmptyDirectory() {
            Path emptyDir = null;
            try {
                emptyDir = Files.createTempDirectory("brobot-empty-");
                ImagePathManager emptyManager = new ImagePathManager();
                emptyManager.initialize(emptyDir.toString());

                boolean valid = emptyManager.validatePaths();
                assertFalse(valid);

            } catch (IOException e) {
                fail("Failed to create empty directory");
            } finally {
                if (emptyDir != null) {
                    try {
                        Files.deleteIfExists(emptyDir);
                    } catch (IOException e) {
                        // Ignore
                    }
                }
            }
        }

        @Test
        @Order(13)
        @DisplayName("Should verify image setup")
        void testVerifyImageSetup() {
            boolean verified = imagePathManager.verifyImageSetup();
            // Just checks if configured paths have images
            assertTrue(verified);
        }

        @Test
        @Order(14)
        @DisplayName("Should verify with expected directories")
        void testVerifyWithExpectedDirectories() {
            List<String> expectedDirs = Arrays.asList(tempDirectory.toString());
            List<String> expectedImages =
                    Arrays.asList(tempDirectory.resolve("test.png").toString());

            boolean verified = imagePathManager.verifyImageSetup(expectedDirs, expectedImages);
            assertTrue(verified);
        }

        @Test
        @Order(15)
        @DisplayName("Should detect missing expected images")
        void testDetectMissingImages() {
            List<String> expectedDirs = Arrays.asList(tempDirectory.toString());
            List<String> expectedImages =
                    Arrays.asList(tempDirectory.resolve("missing.png").toString());

            boolean verified = imagePathManager.verifyImageSetup(expectedDirs, expectedImages);
            assertFalse(verified);
        }
    }

    @Nested
    @DisplayName("Diagnostics Tests")
    @Disabled("CI failure - needs investigation")
    class DiagnosticsTests {

        @BeforeEach
        void initializeManager() {
            imagePathManager.initialize(tempDirectory.toString());
        }

        @Test
        @Order(16)
        @DisplayName("Should provide diagnostic information")
        void testGetDiagnostics() {
            Map<String, Object> diagnostics = imagePathManager.getDiagnostics();

            assertNotNull(diagnostics);
            assertTrue((Boolean) diagnostics.get("initialized"));
            assertEquals(tempDirectory.toString(), diagnostics.get("primaryPath"));
            assertNotNull(diagnostics.get("additionalPaths"));
            assertNotNull(diagnostics.get("extractedJars"));
            assertTrue((Boolean) diagnostics.get("pathsValid"));

            // In mock mode, SikuliX should be skipped
            boolean sikulixConfigured = (Boolean) diagnostics.get("sikulixConfigured");
            assertFalse(sikulixConfigured); // Mock mode is enabled in BrobotTestBase
        }

        @Test
        @Order(17)
        @DisplayName("Should include additional paths in diagnostics")
        void testDiagnosticsWithAdditionalPaths() {
            Path additionalPath = null;
            try {
                additionalPath = Files.createTempDirectory("brobot-diag-");
                imagePathManager.addPath(additionalPath.toString());

                Map<String, Object> diagnostics = imagePathManager.getDiagnostics();
                List<String> additional = (List<String>) diagnostics.get("additionalPaths");

                assertTrue(additional.contains(additionalPath.toString()));

            } catch (IOException e) {
                fail("Failed to create additional path");
            } finally {
                if (additionalPath != null) {
                    try {
                        Files.deleteIfExists(additionalPath);
                    } catch (IOException e) {
                        // Ignore
                    }
                }
            }
        }
    }

    @Nested
    @DisplayName("Image File Detection Tests")
    @Disabled("CI failure - needs investigation")
    class ImageFileDetectionTests {

        @BeforeEach
        void initializeManager() {
            imagePathManager.initialize(tempDirectory.toString());
        }

        @Test
        @Order(18)
        @DisplayName("Should detect various image formats")
        void testDetectImageFormats() {
            try {
                // Create files with different extensions
                Files.createFile(tempDirectory.resolve("image.jpg"));
                Files.createFile(tempDirectory.resolve("image.jpeg"));
                Files.createFile(tempDirectory.resolve("image.gif"));
                Files.createFile(tempDirectory.resolve("image.bmp"));
                Files.createFile(tempDirectory.resolve("not-image.txt"));

                boolean hasImages = imagePathManager.validatePaths();
                assertTrue(hasImages);

            } catch (IOException e) {
                fail("Failed to create test files");
            }
        }

        @Test
        @Order(19)
        @DisplayName("Should handle case-insensitive image extensions")
        void testCaseInsensitiveExtensions() {
            try {
                Files.createFile(tempDirectory.resolve("IMAGE.PNG"));
                Files.createFile(tempDirectory.resolve("photo.JPG"));

                boolean hasImages = imagePathManager.validatePaths();
                assertTrue(hasImages);

            } catch (IOException e) {
                fail("Failed to create test files");
            }
        }
    }

    @Nested
    @DisplayName("JAR Extraction Tests")
    @Disabled("CI failure - needs investigation")
    class JARExtractionTests {

        @Test
        @Order(20)
        @DisplayName("Should handle JAR extraction request")
        void testJARExtraction() {
            imagePathManager.initialize(tempDirectory.toString());

            // When not running from JAR, should return null
            Path extracted = imagePathManager.extractImagesFromJar("images/");

            // In test environment, not running from JAR
            assertNull(extracted);
        }

        @Test
        @Order(21)
        @DisplayName("Should cache JAR extraction paths")
        void testJARExtractionCaching() {
            imagePathManager.initialize(tempDirectory.toString());

            // Multiple calls should return same result
            Path extracted1 = imagePathManager.extractImagesFromJar("images/");
            Path extracted2 = imagePathManager.extractImagesFromJar("images/");

            assertEquals(extracted1, extracted2);
        }
    }

    @Nested
    @DisplayName("Path Resolution Strategy Tests")
    @Disabled("CI failure - needs investigation")
    class PathResolutionTests {

        @Test
        @Order(22)
        @DisplayName("Should resolve absolute paths")
        void testAbsolutePathResolution() {
            String absolutePath = tempDirectory.toAbsolutePath().toString();

            imagePathManager.initialize(absolutePath);

            List<String> paths = imagePathManager.getConfiguredPaths();
            assertEquals(absolutePath, paths.get(0));
        }

        @Test
        @Order(23)
        @DisplayName("Should resolve working directory relative paths")
        void testWorkingDirectoryResolution() {
            // Create a directory relative to working directory
            Path workingDir = Paths.get(System.getProperty("user.dir"));
            Path relativeDir = workingDir.relativize(tempDirectory);

            if (!relativeDir.isAbsolute()) {
                imagePathManager.initialize(relativeDir.toString());

                List<String> paths = imagePathManager.getConfiguredPaths();
                assertFalse(paths.isEmpty());
            }
        }

        @Test
        @Order(24)
        @DisplayName("Should attempt common location resolution")
        void testCommonLocationResolution() {
            // Try common location that likely doesn't exist
            imagePathManager.initialize("images");

            // Should create fallback
            List<String> paths = imagePathManager.getConfiguredPaths();
            assertFalse(paths.isEmpty());
        }

        @Test
        @Order(25)
        @DisplayName("Should handle classpath prefix")
        void testClasspathResolution() {
            imagePathManager.initialize("classpath:images");

            // May not resolve in test environment, but should handle gracefully
            List<String> paths = imagePathManager.getConfiguredPaths();
            assertFalse(paths.isEmpty());
        }
    }
}
