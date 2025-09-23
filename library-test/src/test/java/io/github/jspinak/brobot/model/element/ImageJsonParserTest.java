package io.github.jspinak.brobot.model.element;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;
import io.github.jspinak.brobot.test.TestEnvironmentInitializer;
import io.github.jspinak.brobot.test.mock.MockScreenConfig;

/**
 * Tests for Image JSON parsing with Spring context. Uses Spring Boot's configured ObjectMapper with
 * all necessary Jackson modules.
 *
 * <p>Key points: - Image is a wrapper for BufferedImage - BufferedImage is marked @JsonIgnore, so
 * only name is serialized - Image can be created from BufferedImage, Mat, Pattern, or file
 */
@DisplayName("Image JSON Parser Tests")
@SpringBootTest(
        classes = BrobotTestApplication.class,
        properties = {
            "spring.main.lazy-initialization=true",
            "brobot.mock.enabled=true",
            "brobot.illustration.disabled=true",
            "brobot.scene.analysis.disabled=true",
            "brobot.gui-access.continue-on-error=true",
            "brobot.gui-access.check-on-startup=false",
            "java.awt.headless=true",
            "spring.main.allow-bean-definition-overriding=true",
            "brobot.test.type=unit",
            "brobot.capture.physical-resolution=false"
        })
@Import({
    MockScreenConfig.class,
    io.github.jspinak.brobot.test.config.TestApplicationConfiguration.class
})
@ContextConfiguration(initializers = TestEnvironmentInitializer.class)
class ImageJsonParserTest {

    @Autowired private ConfigurationParser jsonParser;

    @Autowired private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should only serialize Image name (BufferedImage is @JsonIgnore)")
    void testImageSerializationOnly() throws ConfigurationException {
        // Image cannot be deserialized from JSON as it has no default constructor
        // and BufferedImage is @JsonIgnore. We can only test serialization.

        Image image = new Image(new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB));
        image.setName("TestImage");

        String json = jsonParser.toJson(image);

        assertNotNull(json);
        assertTrue(json.contains("\"name\""));
        assertTrue(json.contains("\"TestImage\""));
        assertFalse(json.contains("bufferedImage")); // Should not serialize @JsonIgnore field
    }

    @Test
    @DisplayName("Should serialize Image to JSON (deserialization not supported)")
    void testSerializeImage() throws ConfigurationException {
        // Create an image with a BufferedImage and name
        Image image = new Image(new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB));
        image.setName("SerializedImage");

        // Serialize
        String json = jsonParser.toJson(image);
        assertNotNull(json);
        assertTrue(json.contains("\"name\" :") && json.contains("\"SerializedImage\""));
        assertFalse(json.contains("bufferedImage")); // Should not serialize @JsonIgnore field

        // Note: Cannot deserialize back to Image as it lacks a default constructor
        // and all constructors require BufferedImage which is @JsonIgnore
    }

    @Test
    @DisplayName("Should test isEmpty method")
    void testIsEmpty() {
        // Create an empty image
        Image emptyImage = new Image((BufferedImage) null);
        assertTrue(emptyImage.isEmpty());

        // Create a non-empty image
        Image nonEmptyImage = new Image(new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB));
        assertFalse(nonEmptyImage.isEmpty());
    }

    @Test
    @DisplayName("Should test width and height methods")
    void testWidthAndHeight() {
        // Create an image with specific dimensions
        int width = 200;
        int height = 150;
        Image image = new Image(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB));

        // Verify dimensions
        assertEquals(width, image.w());
        assertEquals(height, image.h());
    }

    @Test
    @DisplayName("Should test getEmptyImage static method")
    void testGetEmptyImage() {
        Image emptyImage = Image.getEmptyImage();

        assertNotNull(emptyImage);
        assertEquals("empty scene", emptyImage.getName());
        assertNotNull(emptyImage.getBufferedImage());

        // Verify it's actually a valid image
        assertTrue(emptyImage.getBufferedImage().getWidth() > 0);
        assertTrue(emptyImage.getBufferedImage().getHeight() > 0);
    }

    @Test
    @DisplayName("Should test toString method")
    void testToString() {
        // Create an image with name and dimensions
        Image image = new Image(new BufferedImage(100, 80, BufferedImage.TYPE_INT_RGB));
        image.setName("ToStringTest");

        String result = image.toString();

        assertNotNull(result);
        assertTrue(result.contains("ToStringTest"));
        assertTrue(result.contains("100"));
        assertTrue(result.contains("80"));
    }

    @Test
    @DisplayName("Should verify Image JSON structure")
    void testImageJsonStructure() throws Exception {
        // Create an image and verify its JSON structure
        Image image = new Image(new BufferedImage(100, 80, BufferedImage.TYPE_INT_RGB));
        image.setName("StructureTest");

        String json = objectMapper.writeValueAsString(image);

        // Parse as generic JSON to verify structure
        @SuppressWarnings("unchecked")
        Map<String, Object> jsonMap = objectMapper.readValue(json, Map.class);

        // With BrobotObjectMapper, Image may have additional fields (like empty, w, h)
        // but BufferedImage should still be @JsonIgnore
        assertTrue(jsonMap.containsKey("name"));
        assertEquals("StructureTest", jsonMap.get("name"));
        assertFalse(json.contains("bufferedImage"));
    }

    @Test
    @DisplayName("Should serialize Image without name")
    void testSerializeImageWithoutName() throws Exception {
        // Create image without setting name
        Image image = new Image(new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB));

        String json = objectMapper.writeValueAsString(image);

        assertNotNull(json);
        // Parse as generic JSON to verify structure
        @SuppressWarnings("unchecked")
        Map<String, Object> jsonMap = objectMapper.readValue(json, Map.class);

        // Name should be null or not present depending on ObjectMapper configuration
        // BrobotObjectMapper might include additional fields
        if (jsonMap.containsKey("name")) {
            assertNull(jsonMap.get("name"));
        }
        assertFalse(json.contains("bufferedImage"));
    }

    @Test
    @DisplayName("Should create Image from Pattern constructor")
    void testImageFromPattern() {
        // Create a Pattern with a BufferedImage
        BufferedImage buffImg = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
        Pattern pattern = new Pattern(buffImg);
        pattern.setNameWithoutExtension("PatternName");

        // Create Image from Pattern
        Image image = new Image(pattern);

        assertNotNull(image);
        assertEquals("PatternName", image.getName());
        assertNotNull(image.getBufferedImage());
        assertEquals(50, image.w());
        assertEquals(50, image.h());
    }
}
