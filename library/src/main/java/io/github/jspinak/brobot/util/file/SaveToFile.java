package io.github.jspinak.brobot.util.file;

import java.io.File;

import org.sikuli.script.Image;
import org.w3c.dom.Document;

/**
 * Interface for file saving operations supporting various content types.
 *
 * <p>Defines a contract for saving different types of data to the filesystem, including images, XML
 * documents, and folder creation. This abstraction allows for different implementations based on
 * environment requirements (e.g., local filesystem, cloud storage, mock implementations for
 * testing).
 *
 * <p>Key operations:
 *
 * <ul>
 *   <li>Folder creation with automatic parent directory handling
 *   <li>Image saving with timestamp-based naming
 *   <li>XML document persistence
 * </ul>
 *
 * <p>Implementation considerations:
 *
 * <ul>
 *   <li>Implementations should handle IOException appropriately
 *   <li>Folder creation should be idempotent
 *   <li>Image naming should prevent overwrites
 *   <li>XML saving should use proper encoding
 * </ul>
 *
 * <p>Common use cases:
 *
 * <ul>
 *   <li>Saving screenshots during automation runs
 *   <li>Persisting configuration as XML
 *   <li>Creating directory structures for reports
 *   <li>Archiving visual test results
 * </ul>
 *
 * @see RecorderSaveToFile
 * @see FilenameAllocator
 */
public interface SaveToFile {

    /**
     * Creates a folder at the specified location, including parent directories.
     *
     * <p>Implementations should:
     *
     * <ul>
     *   <li>Create all necessary parent directories
     *   <li>Return the folder even if it already exists (idempotent)
     *   <li>Handle permissions and access issues gracefully
     *   <li>Return null or throw exception on failure
     * </ul>
     *
     * @param folder the folder to create, including full path
     * @return the created folder File object, or existing folder if already present
     */
    public File createFolder(File folder);

    /**
     * Saves an image with a timestamp-based filename to prevent overwrites.
     *
     * <p>The implementation should:
     *
     * <ul>
     *   <li>Append current date/time to the base filename
     *   <li>Use a consistent timestamp format (e.g., yyyyMMdd_HHmmss)
     *   <li>Ensure the filename is filesystem-safe
     *   <li>Save in an appropriate image format (typically PNG)
     * </ul>
     *
     * <p>Example: baseFileName "screenshot" might become "screenshot_20231215_143052.png"
     *
     * @param img the Sikuli Image object to save
     * @param baseFileName base name for the file, without extension or timestamp
     * @return the full path of the saved file, or null if save failed
     */
    String saveImageWithDate(Image img, String baseFileName);

    /**
     * Persists an XML document to the specified file.
     *
     * <p>Implementations should:
     *
     * <ul>
     *   <li>Use UTF-8 encoding by default
     *   <li>Format with proper indentation for readability
     *   <li>Create parent directories if needed
     *   <li>Handle existing files appropriately (typically overwrite)
     * </ul>
     *
     * <p>Common uses include saving:
     *
     * <ul>
     *   <li>Test configuration files
     *   <li>State transition definitions
     *   <li>Action sequence recordings
     *   <li>Analysis results in structured format
     * </ul>
     *
     * @param doc the DOM Document to save
     * @param fileName target filename including path and .xml extension
     */
    void saveXML(Document doc, String fileName);
}
