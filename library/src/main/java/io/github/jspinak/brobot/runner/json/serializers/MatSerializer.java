package io.github.jspinak.brobot.runner.json.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom serializer for OpenCV {@link Mat} (Matrix) objects that provides safe JSON
 * serialization by extracting only metadata while avoiding native memory issues.
 * 
 * <p>This serializer addresses critical challenges when working with OpenCV Mat objects:</p>
 * <ul>
 *   <li><b>Native Memory Management:</b> Mat objects are wrappers around native C++
 *       OpenCV matrices that allocate memory outside the JVM heap. This native memory
 *       contains raw pixel/data arrays that cannot be directly serialized to JSON.</li>
 *   <li><b>Large Data Size:</b> Mat objects can contain millions of data points
 *       (e.g., a 1920x1080 image has 2+ million pixels). Serializing this raw data
 *       would create enormous JSON documents and severe performance issues.</li>
 *   <li><b>Platform Dependencies:</b> Native memory pointers and data layouts are
 *       platform-specific and cannot be meaningfully transferred between systems.</li>
 *   <li><b>Memory Leaks:</b> Improper handling of Mat serialization could lead to
 *       native memory leaks as the garbage collector doesn't manage native memory.</li>
 * </ul>
 * 
 * <p><b>Serialization Strategy:</b></p>
 * <p>Instead of attempting to serialize the actual matrix data, this serializer
 * extracts only the essential metadata that describes the matrix structure:</p>
 * <ul>
 *   <li>Number of rows and columns (matrix dimensions)</li>
 *   <li>Number of channels (e.g., 1 for grayscale, 3 for RGB, 4 for RGBA)</li>
 *   <li>Type indicator for identification</li>
 * </ul>
 * 
 * <p><b>Serialization Format:</b></p>
 * <pre>{@code
 * // For a valid Mat:
 * {
 *   "rows": 1080,
 *   "cols": 1920,
 *   "channels": 3,
 *   "type": "Mat"
 * }
 * 
 * // For null or invalid Mat:
 * {
 *   "type": "null"
 * }
 * }</pre>
 * 
 * <p><b>Use Cases:</b></p>
 * <p>This approach is suitable for logging, debugging, and metadata transfer.
 * If actual matrix data needs to be persisted, it should be saved using OpenCV's
 * native formats (e.g., imwrite for images) and referenced by path.</p>
 * 
 * @see org.bytedeco.opencv.opencv_core.Mat
 * @see JsonSerializer
 * @see RectSerializer
 */
@Component
public class MatSerializer extends JsonSerializer<Mat> {
    /**
     * Serializes an OpenCV Mat object to JSON format, extracting only structural
     * metadata while avoiding native memory serialization.
     * 
     * <p><b>Null Safety:</b> Checks both for null references and null native pointers
     * (mat.isNull()) since JavaCV objects can have non-null Java references but
     * null native pointers.</p>
     * 
     * <p><b>Valid Mat Serialization:</b> For valid Mat objects, writes:</p>
     * <ul>
     *   <li>{@code rows} - Number of rows in the matrix</li>
     *   <li>{@code cols} - Number of columns in the matrix</li>
     *   <li>{@code channels} - Number of channels (color components)</li>
     *   <li>{@code type} - Fixed string "Mat" for type identification</li>
     * </ul>
     * 
     * <p><b>Invalid Mat Serialization:</b> For null or invalid Mat objects,
     * writes only {@code {"type": "null"}} to indicate absence of data.</p>
     * 
     * @param mat the Mat object to serialize (may be null or have null native pointer)
     * @param gen the JsonGenerator used to write JSON content
     * @param provider the SerializerProvider (not used in this implementation)
     * @throws IOException if there's an error writing to the JsonGenerator
     */
    @Override
    public void serialize(Mat mat, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        if (mat != null && !mat.isNull()) {
            gen.writeNumberField("rows", mat.rows());
            gen.writeNumberField("cols", mat.cols());
            gen.writeNumberField("channels", mat.channels());
            gen.writeStringField("type", "Mat");
        } else {
            gen.writeStringField("type", "null");
        }
        gen.writeEndObject();
    }
}
