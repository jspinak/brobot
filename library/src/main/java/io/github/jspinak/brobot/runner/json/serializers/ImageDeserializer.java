package io.github.jspinak.brobot.runner.json.serializers;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import io.github.jspinak.brobot.model.element.Image;

/**
 * Custom deserializer for {@link Image} objects that recreates Image instances from JSON metadata,
 * generating placeholder BufferedImage objects as needed.
 *
 * <p>This deserializer complements the {@link ImageSerializer} by handling the reverse process of
 * recreating Image objects from their serialized metadata. It addresses several important
 * considerations:
 *
 * <ul>
 *   <li><b>Data Loss Acceptance:</b> Since the serializer excludes actual pixel data for
 *       efficiency, this deserializer cannot restore the original image content. Instead, it
 *       creates placeholder BufferedImage objects.
 *   <li><b>Metadata Preservation:</b> Ensures all available metadata (name, dimensions) is properly
 *       restored to maintain object identity and structure.
 *   <li><b>Memory Efficiency:</b> Creates minimal BufferedImage objects only when dimensions are
 *       available, avoiding unnecessary memory allocation.
 *   <li><b>Graceful Degradation:</b> Handles missing or incomplete JSON data by creating valid but
 *       empty Image objects.
 * </ul>
 *
 * <p><b>Deserialization Strategy:</b>
 *
 * <ol>
 *   <li>Extracts available metadata from JSON (name, width, height)
 *   <li>Creates a blank BufferedImage if dimensions are valid (>0)
 *   <li>Constructs an Image object with the BufferedImage (or null)
 *   <li>Sets additional properties like name if available
 * </ol>
 *
 * <p><b>Expected JSON Format:</b>
 *
 * <pre>{@code
 * {
 *   "name": "button_submit.png",
 *   "width": 120,
 *   "height": 40
 * }
 * }</pre>
 *
 * <p><b>Limitations:</b>
 *
 * <ul>
 *   <li>Created BufferedImages are blank (TYPE_INT_RGB) and don't contain original pixels
 *   <li>This is suitable for structural testing but not for image comparison/matching
 *   <li>For full image restoration, the actual image files must be loaded separately
 * </ul>
 *
 * @see Image
 * @see ImageSerializer
 * @see JsonDeserializer
 * @see java.awt.image.BufferedImage
 */
@Component
public class ImageDeserializer extends JsonDeserializer<Image> {

    /**
     * Deserializes JSON content into an Image object, creating a placeholder BufferedImage based on
     * the metadata provided.
     *
     * <p><b>Process Flow:</b>
     *
     * <ol>
     *   <li>Parses the JSON into a tree structure for easy field access
     *   <li>Extracts name, width, and height fields with null-safe checks
     *   <li>Creates a blank BufferedImage if valid dimensions exist
     *   <li>Constructs and configures the Image object
     * </ol>
     *
     * <p><b>Field Handling:</b>
     *
     * <ul>
     *   <li>{@code name} - Optional, set on Image if present
     *   <li>{@code width/height} - Used to create BufferedImage, must both be >0
     *   <li>Missing fields - Handled gracefully with defaults (null/0)
     * </ul>
     *
     * <p><b>BufferedImage Creation:</b>
     *
     * <p>Creates a TYPE_INT_RGB BufferedImage only if both width and height are positive values.
     * This ensures memory is allocated only for valid images.
     *
     * @param jp the JsonParser positioned at the Image JSON object
     * @param ctxt the deserialization context
     * @return a new Image object with metadata restored and placeholder BufferedImage
     * @throws IOException if there's an error reading the JSON content
     */
    @Override
    public Image deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);

        // Get properties from JSON
        String name = node.has("name") ? node.get("name").asText() : null;
        int width = node.has("width") ? node.get("width").asInt() : 0;
        int height = node.has("height") ? node.get("height").asInt() : 0;

        // Create a new empty BufferedImage if width and height are available
        BufferedImage img = null;
        if (width > 0 && height > 0) {
            img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        }

        // Create the Image object
        Image image = new Image(img);
        if (name != null) {
            image.setName(name);
        }

        return image;
    }
}
