package io.github.jspinak.brobot.runner.json.serializers;

import java.io.IOException;

import org.bytedeco.opencv.opencv_core.Rect;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Custom serializer for JavaCV {@link Rect} objects that provides safe JSON serialization for
 * OpenCV/JavaCV rectangle structures.
 *
 * <p>This serializer addresses critical challenges when working with JavaCV Rect objects:
 *
 * <ul>
 *   <li><b>Native Memory References:</b> JavaCV Rect objects are wrappers around native C++ OpenCV
 *       structures that contain pointers to native memory. Direct serialization would attempt to
 *       serialize these native pointers, causing errors or crashes.
 *   <li><b>Circular References:</b> Rect objects may contain internal references to parent
 *       structures or native memory contexts that create circular dependencies during
 *       serialization.
 *   <li><b>Platform Dependencies:</b> Native memory addresses and structures are platform-specific
 *       and cannot be meaningfully serialized or deserialized across different systems or sessions.
 * </ul>
 *
 * <p><b>Serialization Format:</b>
 *
 * <p>Converts the Rect to a simple JSON object containing only the geometric properties:
 *
 * <pre>{@code
 * {
 *   "x": 100,
 *   "y": 200,
 *   "width": 300,
 *   "height": 400
 * }
 * }</pre>
 *
 * <p>This format preserves all the meaningful data (position and dimensions) while avoiding any
 * native memory references or complex internal structures.
 *
 * @see org.bytedeco.opencv.opencv_core.Rect
 * @see JsonSerializer
 * @see com.fasterxml.jackson.databind.module.SimpleModule
 */
@Component
public class RectSerializer extends JsonSerializer<Rect> {
    /**
     * Serializes a JavaCV Rect object to JSON format, extracting only the geometric properties and
     * avoiding native memory references.
     *
     * <p><b>Null Handling:</b> If the rect parameter is null, writes a JSON null value.
     *
     * <p><b>Normal Serialization:</b> For non-null Rect objects, creates a JSON object with the
     * following properties:
     *
     * <ul>
     *   <li>{@code x} - The x-coordinate of the rectangle's top-left corner
     *   <li>{@code y} - The y-coordinate of the rectangle's top-left corner
     *   <li>{@code width} - The width of the rectangle
     *   <li>{@code height} - The height of the rectangle
     * </ul>
     *
     * @param rect the Rect object to serialize (may be null)
     * @param gen the JsonGenerator used to write JSON content
     * @param provider the SerializerProvider (not used in this implementation)
     * @throws IOException if there's an error writing to the JsonGenerator
     */
    @Override
    public void serialize(Rect rect, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        if (rect == null) {
            gen.writeNull();
            return;
        }

        gen.writeStartObject();
        gen.writeNumberField("x", rect.x());
        gen.writeNumberField("y", rect.y());
        gen.writeNumberField("width", rect.width());
        gen.writeNumberField("height", rect.height());
        gen.writeEndObject();
    }
}
