package io.github.jspinak.brobot.json.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * Custom Jackson serializer for BufferedImage objects.
 * 
 * Converts BufferedImage to a Base64-encoded PNG string for JSON serialization.
 * This prevents serialization errors when BufferedImage objects are included in 
 * classes that need to be converted to JSON.
 * 
 * The serialized format includes:
 * - width: Image width in pixels
 * - height: Image height in pixels  
 * - type: BufferedImage type constant
 * - data: Base64-encoded PNG data
 */
public class BufferedImageSerializer extends JsonSerializer<BufferedImage> {
    
    @Override
    public void serialize(BufferedImage image, JsonGenerator gen, SerializerProvider provider) 
            throws IOException {
        
        if (image == null) {
            gen.writeNull();
            return;
        }
        
        gen.writeStartObject();
        
        // Write image metadata
        gen.writeNumberField("width", image.getWidth());
        gen.writeNumberField("height", image.getHeight());
        gen.writeNumberField("type", image.getType());
        
        // Convert to Base64-encoded PNG
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            gen.writeStringField("data", base64);
        } catch (IOException e) {
            // If we can't serialize the image data, at least preserve metadata
            gen.writeStringField("data", "");
            gen.writeStringField("error", "Failed to serialize image data: " + e.getMessage());
        }
        
        gen.writeEndObject();
    }
    
    @Override
    public Class<BufferedImage> handledType() {
        return BufferedImage.class;
    }
}