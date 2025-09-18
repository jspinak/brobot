package io.github.jspinak.brobot.config.core;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for ImagePathManager to ensure it uses simple directory paths without JAR extraction
 * complexity.
 */
class ImagePathManagerTest {

    private ImagePathManager pathManager;

    @TempDir Path tempDir;

    @BeforeEach
    void setUp() {
        pathManager = new ImagePathManager();
    }

    @Test
    void testInitializeWithRelativePath() {
        // Test with relative path "images"
        pathManager.initialize("images");

        Path primaryPath = pathManager.getPrimaryImagePath();
        assertNotNull(primaryPath);
        assertTrue(primaryPath.isAbsolute(), "Path should be resolved to absolute");
        assertTrue(primaryPath.toString().endsWith("images"), "Path should end with 'images'");
    }

    @Test
    void testInitializeWithAbsolutePath() {
        // Test with absolute path
        Path absolutePath = tempDir.resolve("screenshots");
        pathManager.initialize(absolutePath.toString());

        Path primaryPath = pathManager.getPrimaryImagePath();
        assertNotNull(primaryPath);
        assertEquals(absolutePath, primaryPath, "Absolute path should be used as-is");
    }

    @Test
    void testDirectoryCreation() throws Exception {
        // Test that directory is created if it doesn't exist
        Path newDir = tempDir.resolve("new-images");
        assertFalse(Files.exists(newDir), "Directory should not exist initially");

        pathManager.initialize(newDir.toString());

        assertTrue(Files.exists(newDir), "Directory should be created");
        assertTrue(Files.isDirectory(newDir), "Should be a directory");
    }

    @Test
    void testNoJarExtraction() {
        // Test that JAR extraction returns null (deprecated)
        Path result = pathManager.extractImagesFromJar("images");

        assertNull(result, "JAR extraction should return null (deprecated)");
    }

    @Test
    void testGetConfiguredPaths() {
        pathManager.initialize("images");

        List<String> paths = pathManager.getConfiguredPaths();
        assertNotNull(paths);
        assertFalse(paths.isEmpty(), "Should have at least the primary path");
    }

    @Test
    void testAddPath() throws Exception {
        pathManager.initialize("images");

        // Create another directory
        Path additionalDir = tempDir.resolve("additional-images");
        Files.createDirectories(additionalDir);

        // Add it as additional path
        pathManager.addPath(additionalDir.toString());

        List<String> paths = pathManager.getConfiguredPaths();
        assertTrue(paths.size() >= 2, "Should have primary and additional path");
    }

    @Test
    void testReinitializeWithSamePath() {
        String imagePath = "images";

        // Initialize once
        pathManager.initialize(imagePath);
        Path firstPath = pathManager.getPrimaryImagePath();

        // Initialize again with same path - should be no-op
        pathManager.initialize(imagePath);
        Path secondPath = pathManager.getPrimaryImagePath();

        assertEquals(
                firstPath,
                secondPath,
                "Re-initialization with same path should not change primary path");
    }
}
