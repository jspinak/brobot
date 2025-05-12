package io.github.jspinak.brobot.json.parsing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.jspinak.brobot.json.parsing.exception.ConfigurationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JsonPathUtilsTest {
    
    @Autowired
    private JsonPathUtils jsonPathUtils;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testGetString() throws ConfigurationException {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("key", "value");

        String result = jsonPathUtils.getString(root, "key");
        assertEquals("value", result);
    }

    @Test
    void testGetStringThrowsException() {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("key", 123);

        ConfigurationException exception = assertThrows(ConfigurationException.class, () ->
                jsonPathUtils.getString(root, "key"));
        assertEquals("Value at path 'key' is not a string", exception.getMessage());
    }

    @Test
    void testGetOptionalString() {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("key", "value");

        Optional<String> result = jsonPathUtils.getOptionalString(root, "key");
        assertTrue(result.isPresent());
        assertEquals("value", result.get());
    }

    @Test
    void testGetInt() throws ConfigurationException {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("key", 42);

        int result = jsonPathUtils.getInt(root, "key");
        assertEquals(42, result);
    }

    @Test
    void testGetBoolean() throws ConfigurationException {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("key", true);

        boolean result = jsonPathUtils.getBoolean(root, "key");
        assertTrue(result);
    }

    @Test
    void testGetNode() throws ConfigurationException {
        ObjectNode root = objectMapper.createObjectNode();
        ObjectNode child = objectMapper.createObjectNode();
        child.put("key", "value");
        root.set("child", child);

        JsonNode result = jsonPathUtils.getNode(root, "child.key");
        assertEquals("value", result.asText());
    }

    @Test
    void testGetFieldNames() throws ConfigurationException {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("field1", "value1");
        root.put("field2", "value2");

        List<String> fieldNames = jsonPathUtils.getFieldNames(root, "");
        assertTrue(fieldNames.contains("field1"));
        assertTrue(fieldNames.contains("field2"));
    }

    @Test
    void testHasPath() {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("key", "value");

        assertTrue(jsonPathUtils.hasPath(root, "key"));
        assertFalse(jsonPathUtils.hasPath(root, "nonexistent"));
    }
}