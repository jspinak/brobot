package io.github.jspinak.brobot.runner.json.serializers;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import io.github.jspinak.brobot.model.element.Image;

/**
 * Custom serializer for {@link Image} objects that provides memory-efficient JSON serialization by
 * excluding the actual image data (BufferedImage) while preserving metadata.
 *
 * <p>This serializer addresses several critical challenges when serializing Image objects:
 *
 * <ul>
 *   <li><b>Memory Efficiency:</b> BufferedImage objects can be very large (megabytes for
 *       high-resolution images). Including raw pixel data in JSON would create enormous strings
 *       that consume excessive memory and bandwidth.
 *   <li><b>Binary Data in JSON:</b> BufferedImage contains binary pixel data that doesn't translate
 *       well to JSON's text format. Base64 encoding would increase size by ~33%.
 *   <li><b>Serialization Performance:</b> Converting large image data to/from JSON is
 *       computationally expensive and can cause significant performance degradation.
 *   <li><b>Use Case Optimization:</b> For most use cases (logging, debugging, state transfer), the
 *       image metadata (dimensions, name) is sufficient; the actual pixels are not needed.
 * </ul>
 *
 * <p><b>Serialization Strategy:</b>
 *
 * <p>This serializer extracts and preserves only the essential metadata about the image:
 *
 * <ul>
 *   <li>Image name/identifier
 *   <li>Width and height dimensions
 *   <li>Any other lightweight properties
 * </ul>
 *
 * <p><b>Serialization Format Example:</b>
 *
 * <pre>{@code
 * {
 *   "name": "button_submit.png",
 *   "width": 120,
 *   "height": 40
 * }
 * }</pre>
 *
 * <p><b>Note:</b> If the actual image data needs to be persisted, it should be saved separately as
 * an image file and referenced by name/path in the JSON.
 *
 * @see Image
 * @see ImageDeserializer
 * @see java.awt.image.BufferedImage
 */
@Component
public class ImageSerializer extends JsonSerializer<Image> {

    /**
     * Serializes an Image object to JSON format, including only metadata while excluding the actual
     * BufferedImage data for efficiency.
     *
     * <p><b>Null/Empty Handling:</b> If the image is null or empty (no image data), writes a JSON
     * null value and returns immediately.
     *
     * <p><b>Normal Serialization:</b> For valid Image objects, creates a JSON object containing:
     *
     * <ul>
     *   <li>{@code name} - The image name/identifier (if not null)
     *   <li>{@code width} - The image width in pixels
     *   <li>{@code height} - The image height in pixels
     * </ul>
     *
     * <p>Additional fields can be added here as needed without affecting the core serialization
     * strategy of excluding the BufferedImage data.
     *
     * @param image the Image object to serialize
     * @param gen the JsonGenerator used to write JSON content
     * @param serializers the SerializerProvider (not currently used)
     * @throws IOException if there's an error writing to the JsonGenerator
     */
    @Override
    public void serialize(Image image, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        if (image == null || image.isEmpty()) {
            gen.writeNull();
            return;
        }

        gen.writeStartObject();

        // Serialize only simple properties
        if (image.getName() != null) {
            gen.writeStringField("name", image.getName());
        }

        // Can include other simple fields here if needed
        gen.writeNumberField("width", image.w());
        gen.writeNumberField("height", image.h());

        gen.writeEndObject();
    }
}
