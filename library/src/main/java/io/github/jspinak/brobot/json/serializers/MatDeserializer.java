package io.github.jspinak.brobot.json.serializers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Base64;

import org.bytedeco.opencv.opencv_core.Mat;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Custom Jackson deserializer for OpenCV Mat objects.
 *
 * <p>Reconstructs Mat from JSON representation containing matrix metadata and data. Handles both
 * complete matrix data and metadata-only representations.
 */
public class MatDeserializer extends JsonDeserializer<Mat> {

    @Override
    public Mat deserialize(JsonParser parser, DeserializationContext context) throws IOException {

        JsonNode node = parser.getCodec().readTree(parser);

        if (node == null || node.isNull()) {
            return null;
        }

        // Get matrix metadata
        int rows = node.has("rows") ? node.get("rows").asInt() : 0;
        int cols = node.has("cols") ? node.get("cols").asInt() : 0;
        int type = node.has("type") ? node.get("type").asInt() : 0;

        // Create Mat with dimensions
        if (rows > 0 && cols > 0) {
            Mat mat = new Mat(rows, cols, type);

            // Try to restore data if available
            if (node.has("data")) {
                String base64Data = node.get("data").asText();
                if (!base64Data.isEmpty()) {
                    try {
                        byte[] data = Base64.getDecoder().decode(base64Data);
                        ByteBuffer buffer = mat.createBuffer();
                        if (buffer != null && buffer.remaining() >= data.length) {
                            buffer.put(data);
                        }
                    } catch (Exception e) {
                        // Mat is already created with proper dimensions, just no data
                    }
                }
            }

            return mat;
        }

        // Return empty Mat if no valid dimensions
        return new Mat();
    }

    @Override
    public Class<Mat> handledType() {
        return Mat.class;
    }
}
