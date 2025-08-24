package io.github.jspinak.brobot.util.file;

import org.sikuli.script.Image;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import java.io.File;

/**
 * Interface for file saving operations supporting various content types.
 * <p>
 * Defines a contract for saving different types of data to the filesystem,
 * including images, XML documents, and folder creation. This abstraction
 * allows for different implementations based on environment requirements
 * (e.g., local filesystem, cloud storage, mock implementations for testing).
 * <p>
 * Key operations:
 * <ul>
 * <li>Folder creation with automatic parent directory handling</li>
 * <li>Image saving with timestamp-based naming</li>
 * <li>XML document persistence</li>
 * </ul>
 * <p>
 * Implementation considerations:
 * <ul>
 * <li>Implementations should handle IOException appropriately</li>
 * <li>Folder creation should be idempotent</li>
 * <li>Image naming should prevent overwrites</li>
 * <li>XML saving should use proper encoding</li>
 * </ul>
 * <p>
 * Common use cases:
 * <ul>
 * <li>Saving screenshots during automation runs</li>
 * <li>Persisting configuration as XML</li>
 * <li>Creating directory structures for reports</li>
 * <li>Archiving visual test results</li>
 * </ul>
 *
 * @see RecorderSaveToFile
 * @see FilenameAllocator
 */
public interface SaveToFile {

    /**
     * Creates a folder at the specified location, including parent directories.
     * <p>
     * Implementations should:
     * <ul>
     * <li>Create all necessary parent directories</li>
     * <li>Return the folder even if it already exists (idempotent)</li>
     * <li>Handle permissions and access issues gracefully</li>
     * <li>Return null or throw exception on failure</li>
     * </ul>
     *
     * @param folder the folder to create, including full path
     * @return the created folder File object, or existing folder if already present
     */
    public File createFolder(File folder);

    /**
     * Saves an image with a timestamp-based filename to prevent overwrites.
     * <p>
     * The implementation should:
     * <ul>
     * <li>Append current date/time to the base filename</li>
     * <li>Use a consistent timestamp format (e.g., yyyyMMdd_HHmmss)</li>
     * <li>Ensure the filename is filesystem-safe</li>
     * <li>Save in an appropriate image format (typically PNG)</li>
     * </ul>
     * <p>
     * Example: baseFileName "screenshot" might become "screenshot_20231215_143052.png"
     *
     * @param img the Sikuli Image object to save
     * @param baseFileName base name for the file, without extension or timestamp
     * @return the full path of the saved file, or null if save failed
     */
    String saveImageWithDate(Image img, String baseFileName);

    /**
     * Persists an XML document to the specified file.
     * <p>
     * Implementations should:
     * <ul>
     * <li>Use UTF-8 encoding by default</li>
     * <li>Format with proper indentation for readability</li>
     * <li>Create parent directories if needed</li>
     * <li>Handle existing files appropriately (typically overwrite)</li>
     * </ul>
     * <p>
     * Common uses include saving:
     * <ul>
     * <li>Test configuration files</li>
     * <li>State transition definitions</li>
     * <li>Action sequence recordings</li>
     * <li>Analysis results in structured format</li>
     * </ul>
     *
     * @param doc the DOM Document to save
     * @param fileName target filename including path and .xml extension
     */
    void saveXML(Document doc, String fileName);
}
