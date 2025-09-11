package io.github.jspinak.brobot.util.string;

import java.io.File;

/**
 * Utility class for extracting and manipulating file names from paths.
 *
 * <p>This class provides static methods for common file name operations, particularly focused on
 * extracting clean file names without directory paths or file extensions.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Extracts base filename from full paths
 *   <li>Removes file extensions
 *   <li>Handles various path separators
 *   <li>Null-safe operations
 * </ul>
 *
 * <p>Use cases:
 *
 * <ul>
 *   <li>Generating identifiers from file paths
 *   <li>Creating display names for files
 *   <li>Comparing files by name without extension
 *   <li>Building output filenames based on input files
 * </ul>
 *
 * <p>Thread safety: All methods are stateless and thread-safe.
 *
 * @see File
 */
public class FilenameExtractor {

    /**
     * Extracts the base filename without directory path or file extension.
     *
     * <p>This method processes a file path to extract just the core filename, removing both the
     * directory structure and the file extension. It handles various path formats and is
     * platform-independent.
     *
     * <p>Processing steps:
     *
     * <ol>
     *   <li>Create File object to parse the path
     *   <li>Extract filename using File.getName()
     *   <li>Remove extension using regex
     * </ol>
     *
     * <p>Extension removal:
     *
     * <ul>
     *   <li>Regex pattern: "[.][^.]+$"
     *   <li>Matches: Last dot and everything after it
     *   <li>Handles multiple dots: "file.tar.gz" → "file.tar"
     * </ul>
     *
     * <p>Examples:
     *
     * <ul>
     *   <li>"/path/to/file.txt" → "file"
     *   <li>"C:\\folder\\image.png" → "image"
     *   <li>"document.pdf" → "document"
     *   <li>"archive.tar.gz" → "archive.tar"
     *   <li>"no_extension" → "no_extension"
     * </ul>
     *
     * <p>Edge cases:
     *
     * <ul>
     *   <li>null input: Returns empty string
     *   <li>No extension: Returns filename as-is
     *   <li>Hidden files: ".hidden" → ".hidden" (no change)
     *   <li>Trailing dots: "file." → "file"
     * </ul>
     *
     * @param filename the file path or name to process; may be null
     * @return the base filename without path or extension, or empty string if input is null
     */
    public static String getFilenameWithoutExtensionAndDirectory(String filename) {
        if (filename == null) return "";

        // Normalize path separators - replace backslashes with forward slashes
        String normalizedPath = filename.replace('\\', '/');

        // Handle trailing slashes (indicates directory, not file)
        if (normalizedPath.endsWith("/")) {
            return "";
        }

        // Extract filename from path
        int lastSeparator = normalizedPath.lastIndexOf('/');
        String name =
                (lastSeparator >= 0) ? normalizedPath.substring(lastSeparator + 1) : normalizedPath;

        // Handle special dots-only names first
        if (name.equals(".") || name.equals("..") || name.equals("...")) {
            return name;
        }

        // Handle trailing dots (but not for dots-only names)
        if (name.endsWith(".") && !name.matches("^\\.+$")) {
            return name.substring(0, name.length() - 1);
        }

        // Remove extension using regex - matches last dot and everything after it
        // But preserve hidden files that start with dot
        if (name.startsWith(".")) {
            // For hidden files, check if there's a second dot (which would be the extension)
            int secondDot = name.indexOf('.', 1);
            if (secondDot > 0) {
                // Has extension after the hidden file dot - remove it
                return name.replaceFirst("[.][^.]+$", "");
            }
            // No extension, return as-is (e.g., ".hidden")
            return name;
        }

        // For regular files, remove extension
        return name.replaceFirst("[.][^.]+$", "");
    }
}
