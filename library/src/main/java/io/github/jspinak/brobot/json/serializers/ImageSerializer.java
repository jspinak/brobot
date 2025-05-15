package io.github.jspinak.brobot.json.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom serializer for Image class that omits the BufferedImage.
 */
@Component
public class ImageSerializer extends JsonSerializer<Image> {

    @Override
    public void serialize(Image image, JsonGenerator gen, SerializerProvider serializers) throws IOException {
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