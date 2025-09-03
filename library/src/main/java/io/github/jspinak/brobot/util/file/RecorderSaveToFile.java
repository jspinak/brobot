package io.github.jspinak.brobot.util.file;

import org.apache.commons.io.FilenameUtils;
import org.sikuli.basics.Debug;
import org.sikuli.script.Image;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import io.github.jspinak.brobot.config.core.FrameworkSettings;

import javax.imageio.ImageIO;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;

/**
 * Implementation of SaveToFile for recording automation sessions to disk.
 * <p>
 * This component handles file persistence operations during Brobot's recording
 * and playback functionality. It saves screenshots, XML documents, and manages
 * folder structures with timestamp-based naming to preserve session history.
 * <p>
 * Key features:
 * <ul>
 * <li>Timestamp-based image naming using millisecond precision</li>
 * <li>Centralized recording folder from {@link FrameworkSettings}</li>
 * <li>XML formatting with proper indentation</li>
 * <li>PNG format for all saved images</li>
 * </ul>
 * <p>
 * File naming conventions:
 * <ul>
 * <li>Images: {baseFileName}-{timestamp}.png</li>
 * <li>Timestamp format: Unix epoch milliseconds</li>
 * <li>Example: "screenshot-1702654321000.png"</li>
 * </ul>
 * <p>
 * Error handling:
 * <ul>
 * <li>Image save failures return null and log errors</li>
 * <li>XML save failures throw RuntimeException</li>
 * <li>Folder creation uses placeholder implementation</li>
 * </ul>
 * <p>
 * Implementation notes:
 * <ul>
 * <li>Uses Sikuli's Image class for compatibility</li>
 * <li>Leverages Apache Commons for file operations</li>
 * <li>XML transformation uses standard Java DOM APIs</li>
 * <li>Folder creation uses standard Java File.mkdirs()</li>
 * </ul>
 *
 * @see SaveToFile
 * @see FrameworkSettings#recordingFolder
 */
@Component
public class RecorderSaveToFile implements SaveToFile {

    /**
     * Creates the default recording folder from global settings.
     * <p>
     * Uses the path configured in {@link FrameworkSettings#recordingFolder}
     * as the default location for all recording outputs.
     *
     * @return File object representing the recording folder
     */
    public File createFolder() {
        return new File(FrameworkSettings.recordingFolder);
    }

    /**
     * Creates a folder at the specified location.
     * <p>
     * Creates the directory and all necessary parent directories if they don't
     * exist.
     * This replaces the original Sikuli Commons.asFolder method that is not
     * available
     * in version 2.0.5.
     * <p>
     * If the folder already exists, returns the existing folder.
     * If folder creation fails, logs an error and returns the File object anyway.
     *
     * @param folder the folder to create
     * @return File object for the folder path (created if necessary)
     */
    public File createFolder(File folder) {
        if (!folder.exists()) {
            boolean created = folder.mkdirs();
            if (!created && !folder.exists()) {
                Debug.error("Failed to create folder: " + folder.getAbsolutePath());
            }
        }
        return folder;
    }

    /**
     * Saves an image with timestamp-based naming to prevent overwrites.
     * <p>
     * Creates a unique filename by appending the current Unix timestamp
     * in milliseconds to the base filename. The image is saved in PNG
     * format to the recording folder.
     * <p>
     * File naming: {baseFileName}-{timestamp}.png
     * Example: "click-1702654321000.png"
     * <p>
     * Error handling:
     * <ul>
     * <li>Logs successful saves at debug level 3</li>
     * <li>Logs errors with details on failure</li>
     * <li>Returns null if save operation fails</li>
     * </ul>
     *
     * @param img          the Sikuli Image to save
     * @param baseFileName base name without extension (e.g., "screenshot")
     * @return absolute path of saved image, or null on failure
     */
    public String saveImageWithDate(Image img, String baseFileName) {
        File fImage = new File(createFolder(), String.format("%s-%d.png", baseFileName, new Date().getTime()));
        try {
            ImageIO.write(img.get(), FilenameUtils.getExtension(fImage.getName()), fImage);
            Debug.log(3, "ScreenImage::saveImage: %s", fImage);
        } catch (Exception ex) {
            Debug.error("ScreenImage::saveImage: did not work: %s (%s)", fImage, ex.getMessage());
            return null;
        }
        return fImage.getAbsolutePath();
    }

    /**
     * Saves an XML document to the recording folder.
     * <p>
     * Writes the document with proper formatting and indentation
     * to improve readability. The file is saved in the configured
     * recording folder.
     * <p>
     * Error handling:
     * <ul>
     * <li>Silently returns if document is null</li>
     * <li>Throws RuntimeException on file or transformation errors</li>
     * <li>No automatic parent directory creation</li>
     * </ul>
     *
     * @param doc      the DOM Document to save; ignored if null
     * @param fileName target filename including .xml extension
     * @throws RuntimeException if file cannot be created or written
     */
    public void saveXML(Document doc, String fileName) {
        if (doc == null)
            return;
        FileOutputStream output;
        try {
            output = new FileOutputStream(new File(createFolder(), fileName));
            writeXml(doc, output);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Writes XML document to output stream with formatting.
     * <p>
     * Configures the transformer to produce human-readable XML
     * with proper indentation. Uses standard Java XML APIs for
     * maximum compatibility.
     * <p>
     * Transformer configuration:
     * <ul>
     * <li>Indentation enabled for readability</li>
     * <li>UTF-8 encoding (default)</li>
     * <li>Standard XML 1.0 output</li>
     * </ul>
     *
     * @param doc    the document to transform
     * @param output target stream for XML output
     * @throws RuntimeException if transformation fails
     */
    private void writeXml(Document doc, OutputStream output) {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = transformerFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); // makes it look nice
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(output);
        try {
            transformer.transform(source, result);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }
}