package io.github.jspinak.brobot.json.parsing;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.jspinak.brobot.json.parsing.exception.ConfigurationException;
import io.github.jspinak.brobot.json.utils.JsonUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JsonParserTest {

    @TempDir
    Path tempDir;

    @Autowired
    private JsonParser jsonParser;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JsonUtils jsonUtils;

    @Test
    void testParseJsonString() throws ConfigurationException {
        String json = "{\"key\":\"value\"}";
        JsonNode node = jsonParser.parseJson(json);
        assertEquals("value", node.get("key").asText());
    }

    @Test
    void testToJson() throws ConfigurationException {
        // Create a simple object to serialize
        TestConfig config = new TestConfig();
        config.setName("Test");
        config.setVersion("1.0");
        config.setCount(5);
        config.setEnabled(true);

        String json = jsonParser.toJson(config);
        // Check for presence of key and value, regardless of spacing
        assertTrue(json.contains("\"name\""));
        assertTrue(json.contains("Test"));
        assertTrue(json.contains("\"version\""));
        assertTrue(json.contains("1.0"));
        assertTrue(json.contains("\"count\""));
        assertTrue(json.contains("5"));
        assertTrue(json.contains("\"enabled\""));
        assertTrue(json.contains("true"));
    }

    @Test
    void testToPrettyJson() throws ConfigurationException {
        TestConfig config = new TestConfig();
        config.setName("PrettyTest");

        String json = jsonParser.toPrettyJson(config);
        assertTrue(json.contains("\"name\""));
        assertTrue(json.contains("PrettyTest"));
        // Pretty JSON should have line breaks
        assertTrue(json.contains("\n"));
    }

    @Test
    void testJsonUtilsToJsonSafe() throws ConfigurationException {
        // Create an object with potentially problematic fields
        TestObjectWithImage obj = new TestObjectWithImage();
        obj.setName("TestImage");
        obj.setImage(new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB));

        // This should work even though it contains a BufferedImage
        String json = jsonUtils.toJsonSafe(obj);
        assertTrue(json.contains("\"name\""));
        assertTrue(json.contains("TestImage"));
    }

    @Test
    void testJsonUtilsToPrettyJsonSafe() throws ConfigurationException {
        // Create an object with potentially problematic fields
        TestObjectWithImage obj = new TestObjectWithImage();
        obj.setName("PrettyImage");
        obj.setImage(new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB));

        // This should work even though it contains a BufferedImage
        String json = jsonUtils.toPrettyJsonSafe(obj);
        assertTrue(json.contains("\"name\""));
        assertTrue(json.contains("PrettyImage"));
        assertTrue(json.contains("\n"));
    }

    @Test
    void testWriteToFile() throws ConfigurationException, IOException {
        TestConfig config = new TestConfig();
        config.setName("FileTest");

        Path file = tempDir.resolve("config.json");
        jsonParser.writeToFile(config, file);

        assertTrue(Files.exists(file));
        String content = Files.readString(file);
        assertTrue(content.contains("\"name\""));
        assertTrue(content.contains("FileTest"));
    }

    @Test
    void testJsonUtilsWriteToFileSafe() throws ConfigurationException, IOException {
        TestObjectWithImage obj = new TestObjectWithImage();
        obj.setName("SafeFileTest");
        obj.setImage(new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB));

        Path file = tempDir.resolve("image-config.json");
        jsonUtils.writeToFileSafe(obj, file);

        assertTrue(Files.exists(file));
        String content = Files.readString(file);
        assertTrue(content.contains("\"name\""));
        assertTrue(content.contains("SafeFileTest"));
    }

    @Test
    void testReadFromFile() throws ConfigurationException, IOException {
        // Create a JSON file
        Path file = tempDir.resolve("read-test.json");
        Files.writeString(file, "{\"name\":\"ReadTest\",\"version\":\"2.0\",\"count\":10,\"enabled\":false}");

        // Read it back
        TestConfig config = jsonParser.readFromFile(file, TestConfig.class);

        assertEquals("ReadTest", config.getName());
        assertEquals("2.0", config.getVersion());
        assertEquals(10, config.getCount());
        assertFalse(config.isEnabled());
    }

    @Test
    void testConvertJson() throws ConfigurationException {
        String json = "{\"name\":\"Test\",\"version\":\"1.0\",\"count\":5,\"enabled\":true}";
        TestConfig config = jsonParser.convertJson(json, TestConfig.class);
        assertEquals("Test", config.getName());
        assertEquals("1.0", config.getVersion());
        assertEquals(5, config.getCount());
        assertTrue(config.isEnabled());
    }

    /**
     * Test that circular references are handled properly using the circular reference mapper
     */
    @Test
    void testCircularReferenceHandling() throws ConfigurationException {
        // Create objects with circular references
        ObjectWithCircularReference obj1 = new ObjectWithCircularReference();
        ObjectWithCircularReference obj2 = new ObjectWithCircularReference();
        obj1.setName("Object1");
        obj2.setName("Object2");
        obj1.setReference(obj2);
        obj2.setReference(obj1);

        // This should not throw a StackOverflowError
        try {
            String json = jsonUtils.toPrettyJsonSafe(obj1);
            // At minimum, the name should be included
            assertTrue(json.contains("\"name\""));
            assertTrue(json.contains("Object1"));
        } catch (Exception e) {
            fail("Should not throw an exception: " + e.getMessage());
        }
    }

    @Test
    void testValidateSerializationCycle() throws ConfigurationException {
        // Test the validation cycle method
        TestConfig original = new TestConfig();
        original.setName("CycleTest");
        original.setVersion("3.0");

        TestConfig result = jsonUtils.validateSerializationCycle(original, TestConfig.class);

        // Verify the object made it through the cycle
        assertEquals("CycleTest", result.getName());
        assertEquals("3.0", result.getVersion());
    }

    // Test POJO class for conversion
    public static class TestConfig {
        private String name;
        private String version;
        private int count;
        private boolean enabled;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }

        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    // Class with a problematic field (BufferedImage)
    public static class TestObjectWithImage {
        private String name;
        private BufferedImage image;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public BufferedImage getImage() { return image; }
        public void setImage(BufferedImage image) { this.image = image; }
    }

    // Class with a circular reference
    public static class ObjectWithCircularReference {
        private String name;
        private ObjectWithCircularReference reference;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public ObjectWithCircularReference getReference() { return reference; }
        public void setReference(ObjectWithCircularReference reference) { this.reference = reference; }
    }
}