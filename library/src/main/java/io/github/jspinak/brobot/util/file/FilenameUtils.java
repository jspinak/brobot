package io.github.jspinak.brobot.util.file;

/**
 * Utility class for common filename manipulation operations.
 *
 * <p>Provides static methods for handling filename extensions, particularly focused on PNG image
 * files which are the standard format for Brobot's screenshots and visual documentation. These
 * utilities ensure consistent filename handling across the framework.
 *
 * <p>Key operations:
 *
 * <ul>
 *   <li>Adding PNG extensions to extensionless filenames
 *   <li>Extracting filenames without extensions
 *   <li>Simple extension detection using dot notation
 * </ul>
 *
 * <p>Design decisions:
 *
 * <ul>
 *   <li>Uses simple string operations for performance
 *   <li>Assumes single-dot extension format (file.ext)
 *   <li>Does not validate extension validity
 *   <li>PNG is the default extension for Brobot images
 * </ul>
 *
 * <p>Limitations:
 *
 * <ul>
 *   <li>No support for compound extensions (e.g., .tar.gz)
 *   <li>No path manipulation (works with filenames only)
 *   <li>Extension detection is simplistic (any dot counts)
 *   <li>No case normalization for extensions
 * </ul>
 *
 * <p>Thread safety: All methods are stateless and thread-safe.
 *
 * @see FilenameAllocator
 * @see SaveToFile
 */
public class FilenameUtils {

    /**
     * Ensures a filename has an extension, defaulting to .png if missing.
     *
     * <p>This method supports Brobot's convention of using PNG format for all screenshots and
     * visual outputs. If the filename contains any dot, it's assumed to already have an extension
     * and is returned unchanged.
     *
     * <p>Examples:
     *
     * <ul>
     *   <li>"screenshot" → "screenshot.png"
     *   <li>"image.jpg" → "image.jpg" (unchanged)
     *   <li>"file.name.txt" → "file.name.txt" (unchanged)
     *   <li>"noextension" → "noextension.png"
     * </ul>
     *
     * <p>Note: This method uses a simple dot-detection strategy. Files with dots in their names but
     * no extension will not receive .png suffix.
     *
     * @param fileName input filename, may or may not have an extension
     * @return filename with an extension, original or with .png added
     */
    public static String addPngExtensionIfNeeded(String fileName) {
        // Check if the filename has an extension
        if (!fileName.contains(".")) {
            // If there's no extension, add ".png"
            fileName += ".png";
        }
        return fileName;
    }

    /**
     * Extracts the filename portion without its extension.
     *
     * <p>Removes the extension by finding the last dot in the filename and returning everything
     * before it. If no dot exists, returns the original filename unchanged.
     *
     * <p>Examples:
     *
     * <ul>
     *   <li>"image.png" → "image"
     *   <li>"document.pdf" → "document"
     *   <li>"archive.tar.gz" → "archive.tar"
     *   <li>"noextension" → "noextension"
     *   <li>"multiple.dots.txt" → "multiple.dots"
     * </ul>
     *
     * <p>This method correctly handles:
     *
     * <ul>
     *   <li>Multiple dots (uses last dot as extension separator)
     *   <li>Hidden files starting with dot (e.g., ".gitignore")
     *   <li>Filenames without extensions
     * </ul>
     *
     * @param filename input filename with or without extension
     * @return filename without extension, or original if no extension found
     */
    public static String getFileNameWithoutExtension(String filename) {
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex != -1) {
            return filename.substring(0, dotIndex);
        } else {
            // If there's no extension, return the full filename
            return filename;
        }
    }
}
