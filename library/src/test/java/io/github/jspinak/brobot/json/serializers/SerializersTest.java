package io.github.jspinak.brobot.json.serializers;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;

import org.bytedeco.opencv.opencv_core.Mat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.jspinak.brobot.json.BrobotJacksonModule;
import io.github.jspinak.brobot.model.element.Image;
import io.github.jspinak.brobot.test.BrobotTestBase;

/** Tests for custom JSON serializers. */
@DisabledIfEnvironmentVariable(
        named = "CI",
        matches = "true",
        disabledReason = "Test incompatible with CI environment")
public class SerializersTest extends BrobotTestBase {

    private ObjectMapper mapper;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        mapper = new ObjectMapper();
        mapper.registerModule(new BrobotJacksonModule());
    }

    @Test
    public void testBufferedImageSerialization() throws Exception {
        // Create a simple BufferedImage
        BufferedImage original = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);

        // Serialize
        String json = mapper.writeValueAsString(original);
        assertNotNull(json);
        assertTrue(json.contains("\"width\":10"));
        assertTrue(json.contains("\"height\":10"));

        // Deserialize
        BufferedImage deserialized = mapper.readValue(json, BufferedImage.class);
        assertNotNull(deserialized);
        assertEquals(10, deserialized.getWidth());
        assertEquals(10, deserialized.getHeight());
    }

    @Test
    public void testMatSerialization() throws Exception {
        // Create a simple Mat
        Mat original = new Mat(5, 5, org.bytedeco.opencv.global.opencv_core.CV_8UC3);

        // Serialize
        String json = mapper.writeValueAsString(original);
        assertNotNull(json);
        assertTrue(json.contains("\"rows\":5"));
        assertTrue(json.contains("\"cols\":5"));

        // Deserialize
        Mat deserialized = mapper.readValue(json, Mat.class);
        assertNotNull(deserialized);
        assertEquals(5, deserialized.rows());
        assertEquals(5, deserialized.cols());

        // Clean up
        original.close();
        deserialized.close();
    }

    @Test
    public void testImageWithBufferedImageSerialization() throws Exception {
        // Create an Image with BufferedImage
        BufferedImage bufferedImage = new BufferedImage(20, 15, BufferedImage.TYPE_INT_RGB);
        Image original = new Image(bufferedImage, "test-image");

        // Serialize
        String json = mapper.writeValueAsString(original);
        assertNotNull(json);
        assertTrue(json.contains("\"name\":\"test-image\""));

        // Deserialize
        Image deserialized = mapper.readValue(json, Image.class);
        assertNotNull(deserialized);
        assertEquals("test-image", deserialized.getName());
        assertNotNull(deserialized.getBufferedImage());
        assertEquals(20, deserialized.getBufferedImage().getWidth());
        assertEquals(15, deserialized.getBufferedImage().getHeight());
    }

    @Test
    public void testNullHandling() throws Exception {
        // Test null BufferedImage
        BufferedImage nullImage = null;
        String json = mapper.writeValueAsString(nullImage);
        assertEquals("null", json);

        BufferedImage deserialized = mapper.readValue("null", BufferedImage.class);
        assertNull(deserialized);

        // Test null Mat
        Mat nullMat = null;
        json = mapper.writeValueAsString(nullMat);
        assertEquals("null", json);

        Mat deserializedMat = mapper.readValue("null", Mat.class);
        assertNull(deserializedMat);
    }
}
