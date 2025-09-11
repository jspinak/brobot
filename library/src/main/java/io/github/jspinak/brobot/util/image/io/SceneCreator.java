package io.github.jspinak.brobot.util.image.io;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.model.element.Scene;

/**
 * Creates Scene objects from screenshot files in a configured directory.
 *
 * <p>This component scans the screenshot directory and creates Scene objects for each PNG file
 * found. It's useful for batch processing of screenshots, creating test scenarios from captured
 * images, or loading pre-recorded UI states for analysis.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Automatic discovery of PNG screenshots
 *   <li>Scene naming based on filename (without extension)
 *   <li>Configurable screenshot directory via BrobotSettings
 *   <li>Support for filtering by file type
 * </ul>
 *
 * <p>Directory structure:
 *
 * <ul>
 *   <li>Reads from: BrobotSettings.screenshotPath
 *   <li>Processes: All .png files in the directory
 *   <li>Scene paths: "../screenshotPath/filename" (relative path format)
 * </ul>
 *
 * <p>Use cases:
 *
 * <ul>
 *   <li>Loading test screenshots for batch analysis
 *   <li>Creating scenes from UI capture sessions
 *   <li>Building state models from screenshot collections
 *   <li>Automated scene generation for testing
 * </ul>
 *
 * <p>Limitations:
 *
 * <ul>
 *   <li>Only processes PNG files (though isImageFile method supports more)
 *   <li>No recursive directory scanning
 *   <li>Hardcoded relative path prefix "../"
 *   <li>No validation of image content or dimensions
 * </ul>
 *
 * @see Scene
 * @see FrameworkSettings#screenshotPath
 */
@Component
public class SceneCreator {

    /**
     * Creates Scene objects from all PNG files in the screenshot directory.
     *
     * <p>Scans the configured screenshot directory for PNG files and creates a Scene object for
     * each one. The Scene name is derived from the filename without extension.
     *
     * <p>Path construction:
     *
     * <ul>
     *   <li>Input: "screenshot1.png" in screenshotPath
     *   <li>Scene path: "../screenshotPath/screenshot1"
     *   <li>The "../" prefix assumes relative navigation from working directory
     * </ul>
     *
     * <p>Error handling:
     *
     * <ul>
     *   <li>Missing directory: Returns empty list
     *   <li>IO errors: Prints stack trace and continues
     *   <li>Invalid files: Silently skipped
     * </ul>
     *
     * @return list of Scene objects, one per PNG file found
     */
    public List<Scene> createScenesFromScreenshots() {
        List<Scene> scenes = new ArrayList<>();
        Path screenshotPath = Paths.get(FrameworkSettings.screenshotPath);
        if (Files.exists(screenshotPath) && Files.isDirectory(screenshotPath)) {
            System.out.println(screenshotPath.toAbsolutePath());
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(screenshotPath)) {
            for (Path entry : stream) {
                if (Files.isRegularFile(entry) && isPngFile(entry)) {
                    String filename = entry.getFileName().toString();
                    // Remove file extension using regex that matches last dot and everything after
                    String nameWithoutSuffix = filename.replaceFirst("[.][^.]+$", "");
                    // Construct scene with relative path format
                    Scene scene =
                            new Scene("../" + FrameworkSettings.screenshotPath + nameWithoutSuffix);
                    scenes.add(scene);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return scenes;
    }

    /**
     * Checks if a file path represents a PNG image.
     *
     * <p>Case-insensitive check for .png extension.
     *
     * @param path file path to check
     * @return true if the file has a .png extension
     */
    private static boolean isPngFile(Path path) {
        String filename = path.getFileName().toString().toLowerCase();
        return filename.endsWith(".png");
    }

    /**
     * Checks if a file path represents any supported image format.
     *
     * <p>Currently unused but available for future enhancements. Supports common image formats:
     * PNG, JPG, JPEG, BMP, GIF.
     *
     * <p>Note: This method could be used to extend createScenesFromScreenshots to support multiple
     * image formats beyond just PNG.
     *
     * @param path file path to check
     * @return true if the file has a supported image extension
     */
    private static boolean isImageFile(Path path) {
        String filename = path.getFileName().toString().toLowerCase();
        return filename.endsWith(".png")
                || filename.endsWith(".jpg")
                || filename.endsWith(".jpeg")
                || filename.endsWith(".bmp")
                || filename.endsWith(".gif");
    }
}
