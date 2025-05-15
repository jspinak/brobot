package io.github.jspinak.brobot.json.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom serializer for Mat objects
 */
@Component
public class MatSerializer extends JsonSerializer<Mat> {
    @Override
    public void serialize(Mat mat, JsonGenerator gen, SerializerProvider provider) throws IOException {
        // Instead of trying to serialize the full Mat, just output dimensions
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
