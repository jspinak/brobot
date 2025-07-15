package io.github.jspinak.brobot.util.image.io;

import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.util.image.core.BufferedImageUtilities;

import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;

/**
 * Utility component for saving images to disk with automatic filename generation.
 * <p>
 * This class provides functionality for saving screenshots, regions, and OpenCV Mat
 * objects to disk with intelligent filename management. It automatically generates
 * unique filenames to prevent overwrites and maintains counters for efficient
 * sequential naming.
 * <p>
 * Key features:
 * <ul>
 * <li>Automatic unique filename generation with sequential numbering</li>
 * <li>Support for saving regions, screenshots, BufferedImages, and Mat objects</li>
 * <li>Configurable base paths with automatic number suffixes</li>
 * <li>Mock mode support for testing without actual file writes</li>
 * <li>Batch saving operations for multiple images</li>
 * </ul>
 * <p>
 * Filename patterns:
 * <ul>
 * <li>Basic: "{basePath}.png" or "{basePath} -{number}.png"</li>
 * <li>Custom: "{prefix}{number}_{suffix}.png"</li>
 * <li>Numbers start at 0 and increment to avoid overwrites</li>
 * </ul>
 * <p>
 * Use cases:
 * <ul>
 * <li>Saving screenshots during automation runs</li>
 * <li>Creating image datasets with sequential naming</li>
 * <li>Debugging with automatic screenshot capture</li>
 * <li>Building image libraries for pattern matching</li>
 * </ul>
 * <p>
 * Thread safety: This class is NOT thread-safe due to the mutable lastFilenumber map.
 * Concurrent access may result in filename collisions or inconsistent numbering.
 *
 * @see BufferedImageUtilities
 * @see FrameworkSettings
 * @see Pattern
 * @see Mat
 */
@Component
public class ImageFileUtilities {

    private final BufferedImageUtilities bufferedImageOps;
    /**
     * Tracks the last used file number for each base path to enable
     * efficient sequential naming without filesystem checks.
     */
    private Map<String, Integer> lastFilenumber = new HashMap<>();

    public ImageFileUtilities(BufferedImageUtilities bufferedImageOps) {
        this.bufferedImageOps = bufferedImageOps;
    }

    /**
     * Saves a screen region to a PNG file with automatic unique naming.
     * <p>
     * Captures the specified region from the screen and saves it as a PNG image.
     * The filename is automatically generated to avoid overwrites by appending
     * a number suffix if needed (e.g., "screenshot.png", "screenshot -1.png").
     * <p>
     * Behavior modes:
     * <ul>
     * <li>Mock mode: Only logs the intended filename without saving</li>
     * <li>History mode: Saves without console output</li>
     * <li>Normal mode: Saves and prints the filename</li>
     * </ul>
     * <p>
     * Error handling: Returns null if IOException occurs during save.
     *
     * @param region the screen region to capture and save; empty region captures full screen
     * @param path the base path name without extension (e.g., "screenshots/capture")
     * @return the actual path used to save the file including number suffix and .png extension,
     *         or null if save failed
     */
    public String saveRegionToFile(Region region, String path) {
        try {
            String newPath = getFreePath(path) + ".png";
            if (FrameworkSettings.mock) {
                ConsoleReporter.format("Save file as %s \n", newPath);
                return newPath;
            }
            // Remove debug print - use proper logging if needed
            ImageIO.write(bufferedImageOps.getBuffImgFromScreen(region),
                    "png", new File(newPath));
            return newPath;
        } catch (IOException e) {
            ConsoleReporter.println("Error saving region to file: " + e.getMessage());
            return null;
        }
    }

    /**
     * Saves a BufferedImage to a PNG file with automatic unique naming.
     * <p>
     * Similar to saveRegionToFile but works with an existing BufferedImage
     * instead of capturing from screen. Useful for saving processed images
     * or images loaded from other sources.
     *
     * @param bufferedImage the image to save
     * @param path the base path name without extension
     * @return the actual path used to save the file including suffix and extension,
     *         or null if save failed
     */
    public String saveBuffImgToFile(BufferedImage bufferedImage, String path) {
        try {
            String newPath = getFreePath(path) + ".png";
            ImageIO.write(bufferedImage,"png", new File(newPath));
            return newPath;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Saves a full screenshot to file with automatic unique naming.
     * <p>
     * Convenience method that captures the entire screen. Equivalent to
     * calling saveRegionToFile with an empty Region.
     *
     * @param path the base path name without extension
     * @return the actual path used to save the file, or null if save failed
     */
    public String saveScreenshotToFile(String path) {
        return saveRegionToFile(new Region(), path);
    }

    /**
     * Generates a unique filename by appending a number suffix if needed.
     * <p>
     * This method efficiently finds the next available filename by:
     * <ol>
     * <li>Checking the cached last number for this base path</li>
     * <li>Incrementing from that number to find the first non-existent file</li>
     * <li>Caching the new number for future calls</li>
     * </ol>
     * <p>
     * Filename format:
     * <ul>
     * <li>First file: "{path}" (no suffix)</li>
     * <li>Subsequent: "{path} -{number}" (space-dash-number)</li>
     * </ul>
     * <p>
     * This approach minimizes filesystem checks by starting from the last
     * known free number rather than always starting from 0.
     *
     * @param path the base path name without extension
     * @return the path with number suffix if needed (no extension added)
     */
    public String getFreePath(String path) {
        int i = lastFilenumber.containsKey(path) ? lastFilenumber.get(path) + 1 : 0;
        String addToEnd = i==0 ? "" : " -" + i;
        // there may be files existing before the program was run, check to make sure it's unique
        while (fileExists(path + addToEnd + ".png")) {
            i++;
            addToEnd = i==0 ? "" : " -" + i;
        }
        lastFilenumber.put(path, i);
        return path + addToEnd;
    }

    /**
     * Generates a unique filename with custom prefix and suffix pattern.
     * <p>
     * Creates filenames in the format: "{prefix}{number}_{suffix}.png"
     * This variant is useful when you need more control over the filename
     * structure, such as including timestamps or categories.
     * <p>
     * Note: Unlike the single-parameter getFreePath, this method returns
     * the complete filename including the .png extension.
     *
     * @param prefix the filename prefix (e.g., "screenshot_")
     * @param suffix additional identifier (e.g., "login_screen")
     * @return complete unique filename with .png extension
     */
    public String getFreePath(String prefix, String suffix) {
        int i = lastFilenumber.containsKey(prefix) ? lastFilenumber.get(prefix) + 1 : 0;
        String filename = prefix + i + "_" + suffix + ".png";
        while (fileExists(filename)) {
            i++;
            filename = prefix + i + "_" + suffix + ".png";
        }
        return filename;
    }

    /**
     * Gets a unique path using the default history settings.
     * <p>
     * Convenience method that uses BrobotSettings.historyPath and
     * BrobotSettings.historyFilename as the base path.
     *
     * @return unique path based on history settings
     */
    public String getFreePath() {
        return getFreePath(FrameworkSettings.historyPath + FrameworkSettings.historyFilename);
    }

    /**
     * Checks if a file exists at the specified path.
     * <p>
     * Verifies both that the path exists and that it's a regular file
     * (not a directory). Used internally to ensure unique filenames.
     *
     * @param filePath the full path to check
     * @return true if a regular file exists at the path, false otherwise
     */
    public boolean fileExists(String filePath) {
        File f = new File(filePath);
        return f.exists() && !f.isDirectory();
    }

    /**
     * Gets the next available number for the default history path.
     * <p>
     * Useful for external components that need to know what number
     * will be used for the next file save.
     *
     * @return the next free number for the history path
     */
    public int getFreeNumber() {
        return getFreeNumber(FrameworkSettings.historyPath + FrameworkSettings.historyFilename);
    }

    /**
     * Gets the next available number for a specific base path.
     * <p>
     * Triggers a path check to update the cached number, then returns
     * the number that will be used for the next save with this base path.
     * <p>
     * Side effect: Updates the internal lastFilenumber cache.
     *
     * @param path the base path to check
     * @return the next free number for this path
     */
    public int getFreeNumber(String path) {
        getFreePath(path);
        return lastFilenumber.get(path);
    }

    /**
     * Saves an OpenCV Mat as a PNG file and creates a Pattern object.
     * <p>
     * This method directly saves the Mat without unique naming, potentially
     * overwriting existing files. It's useful when you want predictable
     * filenames for pattern matching templates.
     * <p>
     * The created Pattern object references the saved file, making it
     * immediately usable for image matching operations.
     * <p>
     * Warning: This method will overwrite existing files with the same name.
     *
     * @param mat the OpenCV Mat to save
     * @param name filename without extension (e.g., "template" saves as "template.png")
     * @return a Pattern object referencing the saved file
     */
    public Pattern matToPattern(Mat mat, String name) {
        String filename = name + ".png";
        imwrite(filename, mat);
        return new Pattern(filename);
    }

    /**
     * Saves an OpenCV Mat with automatic unique naming.
     * <p>
     * Unlike matToPattern, this method ensures unique filenames by
     * appending number suffixes as needed. Useful for saving multiple
     * variations or iterations of processed images.
     *
     * @param mat the OpenCV Mat to save
     * @param nameWithoutFiletype base filename without extension
     * @return true if save successful, false otherwise
     */
    public boolean writeWithUniqueFilename(Mat mat, String nameWithoutFiletype) {
        nameWithoutFiletype = getFreePath(nameWithoutFiletype) + ".png";
        return imwrite(nameWithoutFiletype, mat);
    }

    /**
     * Batch saves multiple Mat objects with unique filenames.
     * <p>
     * Saves a collection of Mat objects, automatically skipping null entries
     * and ensuring unique filenames for each. The method maintains the
     * correspondence between mats and filenames by index.
     * <p>
     * Features:
     * <ul>
     * <li>Automatic null filtering (silently skips null Mats)</li>
     * <li>Validates list size equality before processing</li>
     * <li>Each file gets a unique name based on its base filename</li>
     * <li>Atomic failure: returns false if any save fails</li>
     * </ul>
     * <p>
     * Example usage:
     * <pre>
     * List&lt;Mat&gt; processedImages = processImages();
     * List&lt;String&gt; names = Arrays.asList("edge", "blur", "threshold");
     * imageUtils.writeAllWithUniqueFilename(processedImages, names);
     * // Saves as: edge.png, blur.png, threshold.png (or with numbers if needed)
     * </pre>
     *
     * @param mats list of OpenCV Mat objects to save (may contain nulls)
     * @param filenames corresponding base filenames without extensions
     * @return true if all non-null mats saved successfully, false if any save failed
     *         or lists have different sizes
     */
    public boolean writeAllWithUniqueFilename(List<Mat> mats, List<String> filenames) {
        if (mats.size() != filenames.size()) {
            ConsoleReporter.println("Error: number of mats and filenames must be equal.");
            return false;
        }
        List<Mat> nonNullMats = new ArrayList<>();
        List<String> nonNullFilenames = new ArrayList<>();
        for (int i=0; i<mats.size(); i++) {
            if (mats.get(i) != null) {
                nonNullMats.add(mats.get(i));
                nonNullFilenames.add(filenames.get(i));
            }
            //else Report.println("Mat at index " + i + " is null.");
        }
        for (int i=0; i<nonNullMats.size(); i++) {
            if (!writeWithUniqueFilename(nonNullMats.get(i), nonNullFilenames.get(i))) {
                return false;
            }
        }
        return true;
    }
}
