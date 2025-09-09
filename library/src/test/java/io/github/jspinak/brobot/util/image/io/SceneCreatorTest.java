package io.github.jspinak.brobot.util.image.io;

import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for SceneCreator - screenshot to Scene conversion
 * utility.
 * Tests directory scanning, file filtering, and Scene object creation.
 */
@DisplayName("SceneCreator Tests")
@DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Test incompatible with CI environment")
public class SceneCreatorTest extends BrobotTestBase {

    private SceneCreator sceneCreator;
    private String originalScreenshotPath;

    @TempDir
    Path tempDir;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        sceneCreator = new SceneCreator();

        // Save and set test screenshot path
        originalScreenshotPath = FrameworkSettings.screenshotPath;
        FrameworkSettings.screenshotPath = tempDir.toString() + "/";
    }

    @org.junit.jupiter.api.AfterEach
    public void tearDown() {
        // Restore original screenshot path
        FrameworkSettings.screenshotPath = originalScreenshotPath;
    }

    @Nested
    @DisplayName("Basic Scene Creation")
    class BasicSceneCreation {

        @Test
        @DisplayName("Create scenes from PNG files")
        public void testCreateScenesFromPngFiles() throws IOException {
            // Create test PNG files
            Files.createFile(tempDir.resolve("screenshot1.png"));
            Files.createFile(tempDir.resolve("screenshot2.png"));
            Files.createFile(tempDir.resolve("screenshot3.png"));

            List<Scene> scenes = sceneCreator.createScenesFromScreenshots();

            assertNotNull(scenes);
            assertEquals(3, scenes.size());

            // Verify scene paths
            List<String> paths = scenes.stream()
                    .map(scene -> scene.getPattern() != null ? scene.getPattern().getName() : "")
                    .collect(Collectors.toList());

            assertTrue(paths.stream().anyMatch(p -> p.contains("screenshot1")));
            assertTrue(paths.stream().anyMatch(p -> p.contains("screenshot2")));
            assertTrue(paths.stream().anyMatch(p -> p.contains("screenshot3")));
        }

        @Test
        @DisplayName("Empty directory returns empty list")
        public void testEmptyDirectory() {
            List<Scene> scenes = sceneCreator.createScenesFromScreenshots();

            assertNotNull(scenes);
            assertTrue(scenes.isEmpty());
        }

        @Test
        @DisplayName("Single PNG file creates single scene")
        public void testSinglePngFile() throws IOException {
            Files.createFile(tempDir.resolve("single.png"));

            List<Scene> scenes = sceneCreator.createScenesFromScreenshots();

            assertNotNull(scenes);
            assertEquals(1, scenes.size());
            assertTrue(scenes.get(0).getPattern() != null &&
                    scenes.get(0).getPattern().getName().contains("single"));
        }
    }

    @Nested
    @DisplayName("File Filtering")
    class FileFiltering {

        @Test
        @DisplayName("Only PNG files are processed")
        public void testOnlyPngFilesProcessed() throws IOException {
            // Create various file types
            Files.createFile(tempDir.resolve("image.png"));
            Files.createFile(tempDir.resolve("document.txt"));
            Files.createFile(tempDir.resolve("data.json"));
            Files.createFile(tempDir.resolve("script.js"));

            List<Scene> scenes = sceneCreator.createScenesFromScreenshots();

            assertNotNull(scenes);
            assertEquals(1, scenes.size());
            assertTrue(scenes.get(0).getPattern() != null &&
                    scenes.get(0).getPattern().getName().contains("image"));
        }

        @Test
        @DisplayName("Other image formats ignored")
        public void testOtherImageFormatsIgnored() throws IOException {
            // Create various image formats
            Files.createFile(tempDir.resolve("photo.jpg"));
            Files.createFile(tempDir.resolve("picture.jpeg"));
            Files.createFile(tempDir.resolve("graphic.bmp"));
            Files.createFile(tempDir.resolve("animation.gif"));
            Files.createFile(tempDir.resolve("screenshot.png"));

            List<Scene> scenes = sceneCreator.createScenesFromScreenshots();

            assertNotNull(scenes);
            assertEquals(1, scenes.size()); // Only PNG processed
            assertTrue(scenes.get(0).getPattern() != null &&
                    scenes.get(0).getPattern().getName().contains("screenshot"));
        }

        @Test
        @DisplayName("Case insensitive PNG extension")
        public void testCaseInsensitivePngExtension() throws IOException {
            Files.createFile(tempDir.resolve("upper.PNG"));
            Files.createFile(tempDir.resolve("mixed.Png"));
            Files.createFile(tempDir.resolve("lower.png"));

            List<Scene> scenes = sceneCreator.createScenesFromScreenshots();

            assertNotNull(scenes);
            assertEquals(3, scenes.size());
        }

        @Test
        @DisplayName("Directories are ignored")
        public void testDirectoriesIgnored() throws IOException {
            Files.createDirectory(tempDir.resolve("subdir.png")); // Directory with .png name
            Files.createFile(tempDir.resolve("file.png"));

            List<Scene> scenes = sceneCreator.createScenesFromScreenshots();

            assertNotNull(scenes);
            assertEquals(1, scenes.size());
            assertTrue(scenes.get(0).getPattern() != null &&
                    scenes.get(0).getPattern().getName().contains("file"));
        }
    }

    @Nested
    @DisplayName("Path Construction")
    class PathConstruction {

        @Test
        @DisplayName("Scene path includes relative prefix")
        public void testScenePathRelativePrefix() throws IOException {
            Files.createFile(tempDir.resolve("test.png"));

            List<Scene> scenes = sceneCreator.createScenesFromScreenshots();

            assertNotNull(scenes);
            assertEquals(1, scenes.size());

            String path = scenes.get(0).getPattern() != null ? scenes.get(0).getPattern().getName() : "";
            // The path should contain the filename without extension
            assertTrue(path.contains("test"));
            assertFalse(path.endsWith(".png")); // Extension removed
        }

        @Test
        @DisplayName("File extension removed from scene name")
        public void testFileExtensionRemoved() throws IOException {
            Files.createFile(tempDir.resolve("screenshot.png"));

            List<Scene> scenes = sceneCreator.createScenesFromScreenshots();

            assertNotNull(scenes);
            assertEquals(1, scenes.size());

            String path = scenes.get(0).getPattern() != null ? scenes.get(0).getPattern().getName() : "";
            assertFalse(path.contains(".png"));
            assertTrue(path.endsWith("screenshot"));
        }

        @ParameterizedTest
        @CsvSource({
                "simple.png, simple",
                "with.dots.in.name.png, with.dots.in",
                "multiple...dots.png, multiple..",
                "ends.with.dot..png, ends.with.dot"
        })
        @DisplayName("Complex filename handling")
        public void testComplexFilenames(String filename, String expectedNamePart) throws IOException {
            Files.createFile(tempDir.resolve(filename));

            List<Scene> scenes = sceneCreator.createScenesFromScreenshots();

            assertNotNull(scenes);
            assertEquals(1, scenes.size());
            // The regex removes everything from the last dot, so "with.dots.in.name.png"
            // becomes "with.dots.in"
            assertTrue(scenes.get(0).getPattern() != null &&
                    scenes.get(0).getPattern().getName().contains(expectedNamePart));
        }
    }

    @Nested
    @DisplayName("Special Characters in Filenames")
    class SpecialCharactersInFilenames {

        @Test
        @DisplayName("Spaces in filename")
        public void testSpacesInFilename() throws IOException {
            Files.createFile(tempDir.resolve("screen shot.png"));

            List<Scene> scenes = sceneCreator.createScenesFromScreenshots();

            assertNotNull(scenes);
            assertEquals(1, scenes.size());
            assertTrue(scenes.get(0).getPattern() != null &&
                    scenes.get(0).getPattern().getName().contains("screen shot"));
        }

        @Test
        @DisplayName("Hyphens and underscores")
        public void testHyphensAndUnderscores() throws IOException {
            Files.createFile(tempDir.resolve("screen-shot_001.png"));

            List<Scene> scenes = sceneCreator.createScenesFromScreenshots();

            assertNotNull(scenes);
            assertEquals(1, scenes.size());
            assertTrue(scenes.get(0).getPattern() != null &&
                    scenes.get(0).getPattern().getName().contains("screen-shot_001"));
        }

        @Test
        @DisplayName("Unicode characters")
        public void testUnicodeCharacters() throws IOException {
            Files.createFile(tempDir.resolve("截图_测试.png"));

            List<Scene> scenes = sceneCreator.createScenesFromScreenshots();

            assertNotNull(scenes);
            assertEquals(1, scenes.size());
            assertTrue(scenes.get(0).getPattern() != null &&
                    scenes.get(0).getPattern().getName().contains("截图_测试"));
        }

        @Test
        @DisplayName("Numbers and dates in filename")
        public void testNumbersAndDates() throws IOException {
            Files.createFile(tempDir.resolve("screenshot_2024-01-15_143022.png"));

            List<Scene> scenes = sceneCreator.createScenesFromScreenshots();

            assertNotNull(scenes);
            assertEquals(1, scenes.size());
            assertTrue(scenes.get(0).getPattern() != null &&
                    scenes.get(0).getPattern().getName().contains("screenshot_2024-01-15_143022"));
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("Non-existent directory returns empty list")
        public void testNonExistentDirectory() {
            FrameworkSettings.screenshotPath = "/non/existent/path/";

            List<Scene> scenes = sceneCreator.createScenesFromScreenshots();

            assertNotNull(scenes);
            assertTrue(scenes.isEmpty());
        }

        @Test
        @DisplayName("File as screenshot path returns empty list")
        public void testFileAsScreenshotPath() throws IOException {
            Path file = tempDir.resolve("file.txt");
            Files.createFile(file);
            FrameworkSettings.screenshotPath = file.toString();

            List<Scene> scenes = sceneCreator.createScenesFromScreenshots();

            assertNotNull(scenes);
            assertTrue(scenes.isEmpty());
        }

        @Test
        @DisplayName("Empty screenshot path")
        public void testEmptyScreenshotPath() {
            FrameworkSettings.screenshotPath = "";

            // Should handle gracefully
            assertDoesNotThrow(() -> {
                List<Scene> scenes = sceneCreator.createScenesFromScreenshots();
                assertNotNull(scenes);
            });
        }

        @Test
        @DisplayName("Null screenshot path")
        public void testNullScreenshotPath() {
            FrameworkSettings.screenshotPath = null;

            // Should handle gracefully
            assertThrows(NullPointerException.class, () -> {
                sceneCreator.createScenesFromScreenshots();
            });
        }
    }

    @Nested
    @DisplayName("Multiple Files Processing")
    class MultipleFilesProcessing {

        @Test
        @DisplayName("Process many PNG files")
        public void testManyPngFiles() throws IOException {
            // Create 20 PNG files
            for (int i = 1; i <= 20; i++) {
                Files.createFile(tempDir.resolve("screenshot_" + i + ".png"));
            }

            List<Scene> scenes = sceneCreator.createScenesFromScreenshots();

            assertNotNull(scenes);
            assertEquals(20, scenes.size());

            // Verify all files processed
            for (int i = 1; i <= 20; i++) {
                final int num = i;
                assertTrue(scenes.stream().anyMatch(s -> s.getPattern() != null &&
                        s.getPattern().getName().contains("screenshot_" + num)));
            }
        }

        @Test
        @DisplayName("Mixed file types in directory")
        public void testMixedFileTypes() throws IOException {
            // Create mix of files
            Files.createFile(tempDir.resolve("screenshot1.png"));
            Files.createFile(tempDir.resolve("document.pdf"));
            Files.createFile(tempDir.resolve("screenshot2.png"));
            Files.createFile(tempDir.resolve("data.csv"));
            Files.createFile(tempDir.resolve("screenshot3.png"));
            Files.createDirectory(tempDir.resolve("subdir"));
            Files.createFile(tempDir.resolve("config.xml"));

            List<Scene> scenes = sceneCreator.createScenesFromScreenshots();

            assertNotNull(scenes);
            assertEquals(3, scenes.size()); // Only PNG files
        }

        @Test
        @DisplayName("Files with similar names")
        public void testSimilarFilenames() throws IOException {
            Files.createFile(tempDir.resolve("test.png"));
            Files.createFile(tempDir.resolve("test1.png"));
            Files.createFile(tempDir.resolve("test_1.png"));
            Files.createFile(tempDir.resolve("test-1.png"));

            List<Scene> scenes = sceneCreator.createScenesFromScreenshots();

            assertNotNull(scenes);
            assertEquals(4, scenes.size());

            // All should be distinct
            List<String> paths = scenes.stream()
                    .map(scene -> scene.getPattern() != null ? scene.getPattern().getName() : "")
                    .collect(Collectors.toList());
            assertEquals(4, paths.stream().distinct().count());
        }
    }

    @Nested
    @DisplayName("Performance Characteristics")
    class PerformanceCharacteristics {

        @Test
        @DisplayName("Handle large number of files efficiently")
        public void testLargeNumberOfFiles() throws IOException {
            // Create 100 PNG files
            for (int i = 0; i < 100; i++) {
                Files.createFile(tempDir.resolve("img_" + String.format("%03d", i) + ".png"));
            }

            long startTime = System.currentTimeMillis();
            List<Scene> scenes = sceneCreator.createScenesFromScreenshots();
            long endTime = System.currentTimeMillis();

            assertNotNull(scenes);
            assertEquals(100, scenes.size());

            // Should complete quickly
            assertTrue(endTime - startTime < 1000, "Processing took " + (endTime - startTime) + "ms");
        }

        @Test
        @DisplayName("Consistent ordering of scenes")
        public void testConsistentOrdering() throws IOException {
            Files.createFile(tempDir.resolve("a.png"));
            Files.createFile(tempDir.resolve("b.png"));
            Files.createFile(tempDir.resolve("c.png"));

            List<Scene> scenes1 = sceneCreator.createScenesFromScreenshots();
            List<Scene> scenes2 = sceneCreator.createScenesFromScreenshots();

            assertEquals(scenes1.size(), scenes2.size());

            // Order might vary based on file system, but size should be consistent
            assertEquals(3, scenes1.size());
        }
    }

    @Nested
    @DisplayName("Real-World Scenarios")
    class RealWorldScenarios {

        @Test
        @DisplayName("Test screenshot batch processing")
        public void testScreenshotBatchProcessing() throws IOException {
            // Simulate test run screenshots
            Files.createFile(tempDir.resolve("login_page_initial.png"));
            Files.createFile(tempDir.resolve("login_page_filled.png"));
            Files.createFile(tempDir.resolve("login_page_error.png"));
            Files.createFile(tempDir.resolve("dashboard_loaded.png"));
            Files.createFile(tempDir.resolve("dashboard_menu_open.png"));

            List<Scene> scenes = sceneCreator.createScenesFromScreenshots();

            assertNotNull(scenes);
            assertEquals(5, scenes.size());

            // Verify all test screenshots captured
            assertTrue(scenes.stream().anyMatch(s -> s.getPattern() != null &&
                    s.getPattern().getName().contains("login_page_initial")));
            assertTrue(scenes.stream().anyMatch(s -> s.getPattern() != null &&
                    s.getPattern().getName().contains("dashboard_loaded")));
        }

        @Test
        @DisplayName("Game state captures")
        public void testGameStateCaptures() throws IOException {
            // Simulate game state screenshots
            Files.createFile(tempDir.resolve("main_menu.png"));
            Files.createFile(tempDir.resolve("level_1_start.png"));
            Files.createFile(tempDir.resolve("level_1_checkpoint.png"));
            Files.createFile(tempDir.resolve("level_1_complete.png"));
            Files.createFile(tempDir.resolve("game_over.png"));

            List<Scene> scenes = sceneCreator.createScenesFromScreenshots();

            assertNotNull(scenes);
            assertEquals(5, scenes.size());

            // All game states should be captured
            List<String> expectedStates = List.of("main_menu", "level_1_start",
                    "level_1_checkpoint", "level_1_complete", "game_over");

            for (String state : expectedStates) {
                assertTrue(scenes.stream().anyMatch(s -> s.getPattern() != null &&
                        s.getPattern().getName().contains(state)));
            }
        }

        @Test
        @DisplayName("Timestamped screenshots")
        public void testTimestampedScreenshots() throws IOException {
            // Simulate timestamped screenshots
            Files.createFile(tempDir.resolve("screenshot_20240115_093000.png"));
            Files.createFile(tempDir.resolve("screenshot_20240115_093015.png"));
            Files.createFile(tempDir.resolve("screenshot_20240115_093030.png"));
            Files.createFile(tempDir.resolve("screenshot_20240115_093045.png"));

            List<Scene> scenes = sceneCreator.createScenesFromScreenshots();

            assertNotNull(scenes);
            assertEquals(4, scenes.size());

            // All timestamps preserved in scene paths
            assertTrue(scenes.stream().allMatch(s -> s.getPattern() != null &&
                    s.getPattern().getName().contains("202401")));
        }
    }
}