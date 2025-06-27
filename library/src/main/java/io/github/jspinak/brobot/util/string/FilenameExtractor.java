package io.github.jspinak.brobot.util.string;

import java.io.File;

/**
 * Utility class for extracting and manipulating file names from paths.
 * <p>
 * This class provides static methods for common file name operations,
 * particularly focused on extracting clean file names without directory
 * paths or file extensions.
 * <p>
 * Key features:
 * <ul>
 * <li>Extracts base filename from full paths</li>
 * <li>Removes file extensions</li>
 * <li>Handles various path separators</li>
 * <li>Null-safe operations</li>
 * </ul>
 * <p>
 * Use cases:
 * <ul>
 * <li>Generating identifiers from file paths</li>
 * <li>Creating display names for files</li>
 * <li>Comparing files by name without extension</li>
 * <li>Building output filenames based on input files</li>
 * </ul>
 * <p>
 * Thread safety: All methods are stateless and thread-safe.
 *
 * @see File
 */
public class FilenameExtractor {

    /**
     * Extracts the base filename without directory path or file extension.
     * <p>
     * This method processes a file path to extract just the core filename,
     * removing both the directory structure and the file extension. It handles
     * various path formats and is platform-independent.
     * <p>
     * Processing steps:
     * <ol>
     * <li>Create File object to parse the path</li>
     * <li>Extract filename using File.getName()</li>
     * <li>Remove extension using regex</li>
     * </ol>
     * <p>
     * Extension removal:
     * <ul>
     * <li>Regex pattern: "[.][^.]+$"</li>
     * <li>Matches: Last dot and everything after it</li>
     * <li>Handles multiple dots: "file.tar.gz" → "file.tar"</li>
     * </ul>
     * <p>
     * Examples:
     * <ul>
     * <li>"/path/to/file.txt" → "file"</li>
     * <li>"C:\\folder\\image.png" → "image"</li>
     * <li>"document.pdf" → "document"</li>
     * <li>"archive.tar.gz" → "archive.tar"</li>
     * <li>"no_extension" → "no_extension"</li>
     * </ul>
     * <p>
     * Edge cases:
     * <ul>
     * <li>null input: Returns empty string</li>
     * <li>No extension: Returns filename as-is</li>
     * <li>Hidden files: ".hidden" → ".hidden" (no change)</li>
     * <li>Trailing dots: "file." → "file"</li>
     * </ul>
     *
     * @param filename the file path or name to process; may be null
     * @return the base filename without path or extension, or empty string if input is null
     */
    public static String getFilenameWithoutExtensionAndDirectory(String filename) {
        if (filename == null) return "";
        File file = new File(filename); // Create a File object from the image path
        return file.getName().replaceFirst("[.][^.]+$", "");
    }
}
