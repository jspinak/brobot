package io.github.jspinak.brobot.runner.json.serializers;

import java.io.IOException;

import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Custom deserializer for OpenCV {@link Mat} (Matrix) objects that provides safe JSON
 * deserialization by creating placeholder Mat objects from metadata.
 *
 * <p>This deserializer complements {@link MatSerializer} and addresses the same critical challenges
 * when working with OpenCV Mat objects:
 *
 * <ul>
 *   <li><b>Native Memory Management:</b> Mat objects are wrappers around native C++ OpenCV
 *       matrices. We cannot reconstruct the actual data from JSON metadata.
 *   <li><b>Data Loss:</b> Since MatSerializer only stores metadata (rows, cols, channels), the
 *       actual matrix data is not available during deserialization.
 *   <li><b>Use Case Limitation:</b> This deserializer creates "placeholder" Mat objects that
 *       maintain structural information but contain no actual data.
 * </ul>
 *
 * <p><b>Deserialization Strategy:</b>
 *
 * <p>The deserializer reads the metadata written by MatSerializer and creates a new, empty Mat
 * object with the same dimensions. This is suitable for:
 *
 * <ul>
 *   <li>Maintaining object structure during round-trip serialization
 *   <li>Testing and debugging where actual data is not needed
 *   <li>Placeholder creation for further processing
 * </ul>
 *
 * <p><b>Important Note:</b> The deserialized Mat will have the correct dimensions but will contain
 * zero-initialized data, not the original matrix content.
 *
 * @see MatSerializer
 * @see org.bytedeco.opencv.opencv_core.Mat
 * @see JsonDeserializer
 */
@Component
public class MatDeserializer extends JsonDeserializer<Mat> {

    /**
     * Deserializes a Mat object from JSON metadata.
     *
     * <p>Reads the metadata format produced by {@link MatSerializer}:
     *
     * <pre>{@code
     * {
     *   "rows": 1080,
     *   "cols": 1920,
     *   "channels": 3,
     *   "type": "Mat"
     * }
     * }</pre>
     *
     * <p>For null Mat representations ({"type": "null"}), returns null.
     *
     * <p>For valid Mat metadata, creates a new Mat with:
     *
     * <ul>
     *   <li>Same dimensions (rows × cols)
     *   <li>Same number of channels
     *   <li>CV_8UC(channels) type (8-bit unsigned integer per channel)
     *   <li>Zero-initialized data
     * </ul>
     *
     * @param parser the JsonParser positioned at the Mat object
     * @param context the DeserializationContext
     * @return a new Mat object with the specified dimensions, or null
     * @throws IOException if there's an error reading from the JsonParser
     */
    @Override
    public Mat deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);

        // Check for null Mat
        String type = node.get("type").asText();
        if ("null".equals(type)) {
            return null;
        }

        // Extract metadata
        int rows = node.get("rows").asInt();
        int cols = node.get("cols").asInt();
        int channels = node.get("channels").asInt();

        // Create a new Mat with the same dimensions
        // CV_8UC1 = 0, CV_8UC3 = 16, CV_8UC4 = 24
        // Formula: CV_8UC(n) = (n-1) * 8
        int matType = org.bytedeco.opencv.global.opencv_core.CV_8UC(channels);

        // Create and return the Mat
        // Note: This creates a zero-initialized Mat with the correct dimensions
        return new Mat(rows, cols, matType);
    }
}
