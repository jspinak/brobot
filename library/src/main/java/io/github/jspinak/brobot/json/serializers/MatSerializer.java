package io.github.jspinak.brobot.json.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bytedeco.opencv.opencv_core.Mat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Base64;

/**
 * Custom Jackson serializer for OpenCV Mat objects.
 * 
 * Converts Mat to a JSON representation that includes matrix metadata and data.
 * This prevents serialization errors when Mat objects are included in classes 
 * that need to be converted to JSON.
 * 
 * The serialized format includes:
 * - rows: Number of rows
 * - cols: Number of columns
 * - type: OpenCV type constant
 * - channels: Number of channels
 * - depth: Bit depth
 * - data: Base64-encoded matrix data
 */
public class MatSerializer extends JsonSerializer<Mat> {
    
    @Override
    public void serialize(Mat mat, JsonGenerator gen, SerializerProvider provider) 
            throws IOException {
        
        if (mat == null || mat.isNull()) {
            gen.writeNull();
            return;
        }
        
        gen.writeStartObject();
        
        // Write matrix metadata
        gen.writeNumberField("rows", mat.rows());
        gen.writeNumberField("cols", mat.cols());
        gen.writeNumberField("type", mat.type());
        gen.writeNumberField("channels", mat.channels());
        gen.writeNumberField("depth", mat.depth());
        
        // Serialize matrix data
        try {
            // Calculate buffer size
            int rows = mat.rows();
            int cols = mat.cols();
            int channels = mat.channels();
            int depth = mat.depth();
            
            if (rows > 0 && cols > 0 && channels > 0) {
                // Determine element size based on depth
                int elemSize = getElementSize(depth);
                int totalBytes = rows * cols * channels * elemSize;
                
                // Create buffer and copy data
                byte[] data = new byte[totalBytes];
                ByteBuffer buffer = mat.createBuffer();
                if (buffer != null && buffer.remaining() >= totalBytes) {
                    buffer.get(data, 0, Math.min(totalBytes, buffer.remaining()));
                    String base64 = Base64.getEncoder().encodeToString(data);
                    gen.writeStringField("data", base64);
                } else {
                    gen.writeStringField("data", "");
                }
            } else {
                gen.writeStringField("data", "");
            }
        } catch (Exception e) {
            // If we can't serialize the matrix data, at least preserve metadata
            gen.writeStringField("data", "");
            gen.writeStringField("error", "Failed to serialize Mat data: " + e.getMessage());
        }
        
        gen.writeEndObject();
    }
    
    private int getElementSize(int depth) {
        switch (depth) {
            case 0: // CV_8U
            case 1: // CV_8S
                return 1;
            case 2: // CV_16U
            case 3: // CV_16S
                return 2;
            case 4: // CV_32S
            case 5: // CV_32F
                return 4;
            case 6: // CV_64F
                return 8;
            default:
                return 1;
        }
    }
    
    @Override
    public Class<Mat> handledType() {
        return Mat.class;
    }
}