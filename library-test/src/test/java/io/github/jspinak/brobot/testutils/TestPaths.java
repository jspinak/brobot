package io.github.jspinak.brobot.testutils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class for managing test resource paths.
 * Provides absolute paths to test images and screenshots.
 */
public class TestPaths {
    
    private static final String SCREENSHOTS_PATH;
    private static final String IMAGES_PATH;
    
    static {
        // Determine the correct paths based on current working directory
        String currentDir = System.getProperty("user.dir");
        File imageDir = new File(currentDir, "images");
        File screenshotDir = new File(currentDir, "screenshots");
        
        // If running from project root, adjust paths
        if (!imageDir.exists()) {
            imageDir = new File(currentDir, "library-test/images");
            screenshotDir = new File(currentDir, "library-test/screenshots");
        }
        
        IMAGES_PATH = imageDir.getAbsolutePath();
        SCREENSHOTS_PATH = screenshotDir.getAbsolutePath();
    }
    
    /**
     * Get the absolute path to the screenshots directory.
     */
    public static String getScreenshotsPath() {
        return SCREENSHOTS_PATH;
    }
    
    /**
     * Get the absolute path to the images directory.
     */
    public static String getImagesPath() {
        return IMAGES_PATH;
    }
    
    /**
     * Get the absolute path to a screenshot file.
     * @param filename The screenshot filename (with or without extension)
     * @return Absolute path to the screenshot file
     */
    public static String getScreenshotPath(String filename) {
        String fileWithExt = ensurePngExtension(filename);
        return Paths.get(SCREENSHOTS_PATH, fileWithExt).toString();
    }
    
    /**
     * Get the absolute path to an image file.
     * @param filename The image filename (with or without extension)
     * @return Absolute path to the image file
     */
    public static String getImagePath(String filename) {
        String fileWithExt = ensurePngExtension(filename);
        return Paths.get(IMAGES_PATH, fileWithExt).toString();
    }
    
    /**
     * Get a Path object for a screenshot file.
     * @param filename The screenshot filename (with or without extension)
     * @return Path object to the screenshot file
     */
    public static Path getScreenshotPathObject(String filename) {
        String fileWithExt = ensurePngExtension(filename);
        return Paths.get(SCREENSHOTS_PATH, fileWithExt);
    }
    
    /**
     * Get a Path object for an image file.
     * @param filename The image filename (with or without extension)
     * @return Path object to the image file
     */
    public static Path getImagePathObject(String filename) {
        String fileWithExt = ensurePngExtension(filename);
        return Paths.get(IMAGES_PATH, fileWithExt);
    }
    
    /**
     * Ensure filename has .png extension.
     * @param filename The filename with or without extension
     * @return Filename with .png extension
     */
    private static String ensurePngExtension(String filename) {
        if (filename == null) {
            return null;
        }
        // Remove any leading path separators or relative path indicators
        filename = filename.replaceAll("^(\\.\\./)+", "");
        filename = filename.replaceAll("^screenshots/", "");
        filename = filename.replaceAll("^images/", "");
        
        // Add .png extension if not present
        if (!filename.toLowerCase().endsWith(".png")) {
            return filename + ".png";
        }
        return filename;
    }
}