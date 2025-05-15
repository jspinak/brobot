package io.github.jspinak.brobot.json.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Custom deserializer for Image class that creates an Image with an empty BufferedImage.
 */
@Component
public class ImageDeserializer extends JsonDeserializer<Image> {

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