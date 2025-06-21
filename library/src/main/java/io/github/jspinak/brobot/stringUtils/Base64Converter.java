package io.github.jspinak.brobot.stringUtils;

import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

/**
 * Utility for Base64 encoding of files and byte arrays in the Brobot framework.
 * 
 * <p>Base64Converter provides convenient methods for converting binary data to Base64-encoded 
 * strings, which is essential for data serialization, transmission, and storage in text-based 
 * formats. This utility is particularly important for handling image data and other binary 
 * content within the framework's JSON-based configuration and communication systems.</p>
 * 
 * <p>Key features:
 * <ul>
 *   <li><b>File Encoding</b>: Converts files directly to Base64 strings by path</li>
 *   <li><b>Byte Array Encoding</b>: Encodes raw byte arrays for in-memory data</li>
 *   <li><b>Error Handling</b>: Gracefully handles file reading errors</li>
 *   <li><b>Static Methods</b>: Utility methods accessible without instantiation</li>
 * </ul>
 * </p>
 * 
 * <p>Common use cases:
 * <ul>
 *   <li>Encoding images for storage in JSON configurations</li>
 *   <li>Preparing binary data for network transmission</li>
 *   <li>Converting screenshots for inclusion in reports</li>
 *   <li>Serializing binary patterns for mock data</li>
 *   <li>Embedding resources in text-based formats</li>
 * </ul>
 * </p>
 * 
 * <p>Implementation notes:
 * <ul>
 *   <li>Uses standard Java Base64 encoder for compatibility</li>
 *   <li>Reads entire file into memory - suitable for typical image sizes</li>
 *   <li>Returns null on error rather than throwing exceptions</li>
 *   <li>Thread-safe due to stateless design</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, Base64Converter enables the framework to work seamlessly 
 * with binary data in text-oriented environments. This is crucial for features like 
 * configuration-based test scenarios, where image patterns need to be embedded in JSON 
 * files, and for transmitting visual data through text-based protocols.</p>
 * 
 * @since 1.0
 * @see Image
 * @see Pattern
 */
@Component
public class Base64Converter {

    public static String convert(String filePath) {
        try {
            // Load the file as bytes
            Path file = Path.of(filePath);
            byte[] fileData = Files.readAllBytes(file);

            // Encode the file data to Base64
            return Base64.getEncoder().encodeToString(fileData);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String convert(byte[] byteArray) {
        return Base64.getEncoder().encodeToString(byteArray);
    }
}
