package io.github.jspinak.brobot.datatypes.primitives.image;

import com.fasterxml.jackson.databind.JsonNode;

import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;
import io.github.jspinak.brobot.runner.json.utils.JsonUtils;
import io.github.jspinak.brobot.model.element.Image;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {"java.awt.headless=false"})
public class ImageJsonParserTest {

    @Autowired
    private ConfigurationParser jsonParser;

    @Autowired
    private JsonUtils jsonUtils;

    /**
     * Test parsing a basic Image from JSON
     */
    @Test
    public void testParseBasicImage() throws ConfigurationException {
        String json = """
                {
                  "name": "TestImage"
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        Image image = jsonParser.convertJson(jsonNode, Image.class);

        assertNotNull(image);
        assertEquals("TestImage", image.getName());
        assertNull(image.getBufferedImage()); // BufferedImage can't be directly serialized in JSON
    }

    /**
     * Test serializing and deserializing an Image
     * Note: BufferedImage won't be serialized by default, so we're only testing the name
     */
    @Test
    public void testSerializeDeserializeImage() throws ConfigurationException {
        // Create an image with just a name
        Image image = new Image(new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB));
        image.setName("SerializedImage");

        // Serialize
        String json = jsonUtils.toJsonSafe(image);
        System.out.println("DEBUG: Serialized Image: " + json);

        // Deserialize
        JsonNode jsonNode = jsonParser.parseJson(json);
        Image deserializedImage = jsonParser.convertJson(jsonNode, Image.class);

        // Verify
        assertNotNull(deserializedImage);
        assertEquals("SerializedImage", deserializedImage.getName());
    }

    /**
     * Test the isEmpty method
     */
    @Test
    public void testIsEmpty() {
        // Create an empty image
        Image emptyImage = new Image((BufferedImage)null);
        assertTrue(emptyImage.isEmpty());

        // Create a non-empty image
        Image nonEmptyImage = new Image(new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB));
        assertFalse(nonEmptyImage.isEmpty());
    }

    /**
     * Test getters for width and height
     */
    @Test
    public void testWidthAndHeight() {
        // Create an image with specific dimensions
        int width = 200;
        int height = 150;
        Image image = new Image(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB));

        // Verify dimensions
        assertEquals(width, image.w());
        assertEquals(height, image.h());
    }

    /**
     * Test getEmptyImage static method
     */
    @Test
    public void testGetEmptyImage() {
        Image emptyImage = Image.getEmptyImage();

        assertNotNull(emptyImage);
        assertEquals("empty scene", emptyImage.getName());
        assertNotNull(emptyImage.getBufferedImage());
    }

    /**
     * Test toString method
     */
    @Test
    public void testToString() {
        // Create an image with name and dimensions
        Image image = new Image(new BufferedImage(100, 80, BufferedImage.TYPE_INT_RGB));
        image.setName("ToStringTest");

        String result = image.toString();

        assertNotNull(result);
        assertTrue(result.contains("ToStringTest"));
        assertTrue(result.contains("100"));
        assertTrue(result.contains("80"));
    }
}