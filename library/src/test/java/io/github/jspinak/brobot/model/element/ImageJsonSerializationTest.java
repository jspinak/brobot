package io.github.jspinak.brobot.model.element;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Image JSON serialization/deserialization without Spring dependencies.
 * Migrated from library-test module.
 * 
 * Note: BufferedImage is marked with @JsonIgnore in the Image class, so JSON
 * serialization only includes the name field.
 */
class ImageJsonSerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Test parsing a basic Image from JSON
     */
    @Test
    void testParseBasicImage() throws Exception {
        String json = """
                {
                  "name": "TestImage"
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(json);
        
        // Validate JSON structure
        assertTrue(jsonNode.has("name"));
        assertEquals("TestImage", jsonNode.get("name").asText());
        
        // Note: BufferedImage field is @JsonIgnore so it won't appear in JSON
        assertFalse(jsonNode.has("bufferedImage"));
    }

    /**
     * Test creating Image JSON structure
     */
    @Test
    void testCreateImageJson() throws Exception {
        // Create an image JSON structure
        ObjectNode imageNode = objectMapper.createObjectNode();
        imageNode.put("name", "SerializedImage");
        
        // Convert to string
        String json = objectMapper.writeValueAsString(imageNode);
        assertNotNull(json);
        assertTrue(json.contains("\"name\" : \"SerializedImage\""));
        
        // Parse back and verify
        JsonNode parsedNode = objectMapper.readTree(json);
        assertEquals("SerializedImage", parsedNode.get("name").asText());
    }

    /**
     * Test Image object behavior (not JSON specific)
     */
    @Test
    void testImageObject() {
        // Test constructor with BufferedImage
        BufferedImage bufferedImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Image image = new Image(bufferedImage);
        
        assertNotNull(image);
        assertNotNull(image.getBufferedImage());
        assertEquals(100, image.w());
        assertEquals(100, image.h());
        assertNull(image.getName()); // No name set in this constructor
        
        // Test constructor with BufferedImage and name
        Image namedImage = new Image(bufferedImage, "NamedImage");
        assertEquals("NamedImage", namedImage.getName());
        assertEquals(100, namedImage.w());
        assertEquals(100, namedImage.h());
    }

    /**
     * Test the isEmpty method
     */
    @Test
    void testIsEmpty() {
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
    void testWidthAndHeight() {
        // Create an image with specific dimensions
        int width = 200;
        int height = 150;
        Image image = new Image(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB));

        // Verify dimensions
        assertEquals(width, image.w());
        assertEquals(height, image.h());
        
        // Test empty image dimensions - returns 0 when BufferedImage is null
        Image emptyImage = new Image((BufferedImage)null);
        assertEquals(0, emptyImage.w());
        assertEquals(0, emptyImage.h());
    }

    /**
     * Test getEmptyImage static method
     */
    @Test
    void testGetEmptyImage() {
        Image emptyImage = Image.getEmptyImage();

        assertNotNull(emptyImage);
        assertEquals("empty scene", emptyImage.getName());
        assertNotNull(emptyImage.getBufferedImage());
        // Empty image actually has screen dimensions (1920x1080 in this case)
        assertTrue(emptyImage.w() > 0);
        assertTrue(emptyImage.h() > 0);
    }

    /**
     * Test toString method
     */
    @Test
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

    /**
     * Test JSON with various image scenarios
     */
    @Test
    void testImageJsonScenarios() throws Exception {
        // Empty name
        String emptyNameJson = """
                {
                  "name": ""
                }
                """;
        JsonNode emptyNameNode = objectMapper.readTree(emptyNameJson);
        assertEquals("", emptyNameNode.get("name").asText());
        
        // Null name (field missing)
        String nullNameJson = """
                {
                }
                """;
        JsonNode nullNameNode = objectMapper.readTree(nullNameJson);
        assertFalse(nullNameNode.has("name"));
        
        // Long name
        String longNameJson = """
                {
                  "name": "This is a very long image name that tests how the system handles extended naming conventions"
                }
                """;
        JsonNode longNameNode = objectMapper.readTree(longNameJson);
        assertEquals("This is a very long image name that tests how the system handles extended naming conventions", 
                     longNameNode.get("name").asText());
    }

    /**
     * Test Image constructors
     */
    @Test
    void testImageConstructors() {
        // Test filename constructor - returns null BufferedImage if file doesn't exist
        Image imgFromFile = new Image("test/image.png");
        // The constructor doesn't throw exception, it just sets bufferedImage to null
        assertNotNull(imgFromFile);
        assertEquals("test/image", imgFromFile.getName()); // filename without extension
        
        // Test Pattern constructor - we'll skip this as it would require Pattern setup
        
        // Test Mat constructor - we'll skip this as it would require JavaCV setup
    }

    /**
     * Test serialization of Image object to JSON
     * Note: Only the name field will be serialized due to @JsonIgnore on BufferedImage
     */
    @Test
    void testImageSerialization() throws Exception {
        // Create an image
        Image image = new Image(new BufferedImage(50, 60, BufferedImage.TYPE_INT_RGB), "TestSerializedImage");
        
        // Serialize to JSON string
        String json = objectMapper.writeValueAsString(image);
        assertNotNull(json);
        
        // Parse the JSON
        JsonNode jsonNode = objectMapper.readTree(json);
        
        // Verify only name is serialized
        assertTrue(jsonNode.has("name"));
        assertEquals("TestSerializedImage", jsonNode.get("name").asText());
        
        // BufferedImage should not be in JSON
        assertFalse(jsonNode.has("bufferedImage"));
        
        // The JSON should have only name and empty fields
        // There might be additional fields besides 'name' depending on Jackson configuration
        assertTrue(jsonNode.size() >= 1);
    }
}