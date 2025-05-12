package io.github.jspinak.brobot.json.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bytedeco.opencv.opencv_core.Rect;

import java.io.IOException;

/**
 * Custom serializer for JavaCV Rect objects to prevent circular references.
 */
public class RectSerializer extends JsonSerializer<Rect> {
    @Override
    public void serialize(Rect rect, JsonGenerator gen, SerializerProvider provider) throws IOException {
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
