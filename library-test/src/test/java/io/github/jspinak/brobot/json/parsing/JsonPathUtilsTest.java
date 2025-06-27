package io.github.jspinak.brobot.json.parsing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.json.parsing.JsonPathUtils;
import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JsonPathUtilsTest {

    @Autowired
    private ConfigurationParser jsonParser;

    @Autowired
    private JsonPathUtils jsonPathUtils;

    private JsonNode testJson;

    @BeforeEach
    void setUp() throws ConfigurationException {
        String json = """
                {
                  "name": "Test Project",
                  "version": "1.0.0",
                  "settings": {
                    "debug": true,
                    "maxRetries": 3,
                    "timeout": 5000
                  },
                  "items": [
                    {
                      "id": 1,
                      "name": "Item 1",
                      "tags": ["test", "sample"]
                    },
                    {
                      "id": 2,
                      "name": "Item 2",
                      "tags": ["prod"]
                    },
                    {
                      "id": 3,
                      "name": "Item 3"
                    }
                  ],
                  "emptyArray": []
                }
                """;
        testJson = jsonParser.parseJson(json);
    }

    // Group 1: Basic value retrieval tests
    @Test
    void testGetString() throws ConfigurationException {
        assertEquals("Test Project", jsonPathUtils.getString(testJson, "name"));
        assertEquals("Item 1", jsonPathUtils.getString(testJson, "items.0.name"));
    }

    @Test
    void testGetOptionalString() {
        assertEquals("1.0.0", jsonPathUtils.getOptionalString(testJson, "version").orElse(null));
        assertFalse(jsonPathUtils.getOptionalString(testJson, "nonexistent").isPresent());
    }

    @Test
    void testGetInt() throws ConfigurationException {
        assertEquals(3, jsonPathUtils.getInt(testJson, "settings.maxRetries"));
        assertEquals(2, jsonPathUtils.getInt(testJson, "items.1.id"));
    }

    @Test
    void testGetOptionalInt() {
        assertEquals(5000, jsonPathUtils.getOptionalInt(testJson, "settings.timeout").orElse(null));
        assertFalse(jsonPathUtils.getOptionalInt(testJson, "nonexistent").isPresent());
    }

    @Test
    void testGetBoolean() throws ConfigurationException {
        assertTrue(jsonPathUtils.getBoolean(testJson, "settings.debug"));
    }

    @Test
    void testGetOptionalBoolean() {
        assertTrue(jsonPathUtils.getOptionalBoolean(testJson, "settings.debug").orElse(false));
        assertFalse(jsonPathUtils.getOptionalBoolean(testJson, "nonexistent").isPresent());
    }

    // Group 2: Node and structure retrieval tests
    @Test
    void testGetNode() throws ConfigurationException {
        JsonNode settings = jsonPathUtils.getNode(testJson, "settings");
        assertTrue(settings.isObject());
        assertEquals(3, settings.size());
    }

    @Test
    void testGetArray() throws ConfigurationException {
        ArrayNode items = jsonPathUtils.getArray(testJson, "items");
        assertEquals(3, items.size());
    }

    @Test
    void testGetOptionalArray() {
        assertTrue(jsonPathUtils.getOptionalArray(testJson, "items").isPresent());
        assertFalse(jsonPathUtils.getOptionalArray(testJson, "nonexistent").isPresent());
    }

    @Test
    void testGetObject() throws ConfigurationException {
        ObjectNode settings = jsonPathUtils.getObject(testJson, "settings");
        assertEquals(3, settings.size());
    }

    @Test
    void testGetOptionalObject() {
        assertTrue(jsonPathUtils.getOptionalObject(testJson, "settings").isPresent());
        assertFalse(jsonPathUtils.getOptionalObject(testJson, "nonexistent").isPresent());
    }

    // Group 3: Path existence and validation tests
    @Test
    void testHasPath() {
        assertTrue(jsonPathUtils.hasPath(testJson, "name"));
        assertFalse(jsonPathUtils.hasPath(testJson, "nonexistent"));
    }

    @Test
    void testInvalidPath() {
        ConfigurationException exception = assertThrows(ConfigurationException.class, () ->
                jsonPathUtils.getString(testJson, "nonexistent.field"));
        assertTrue(exception.getMessage().contains("does not exist"));
    }

    @Test
    void testInvalidType() {
        ConfigurationException exception = assertThrows(ConfigurationException.class, () ->
                jsonPathUtils.getString(testJson, "settings.maxRetries"));
        assertTrue(exception.getMessage().contains("is not a string"));
    }

    // Group 4: Array processing tests
    @Test
    void testForEachInArray() throws ConfigurationException {
        AtomicInteger count = new AtomicInteger();
        jsonPathUtils.forEachInArray(testJson, "items", item -> count.incrementAndGet());
        assertEquals(3, count.get());
    }

    @Test
    void testMapArray() throws ConfigurationException {
        List<String> itemNames = jsonPathUtils.mapArray(testJson, "items", item -> item.get("name").asText());
        assertEquals(List.of("Item 1", "Item 2", "Item 3"), itemNames);
    }

    @Test
    void testFindInArray() throws ConfigurationException {
        Optional<JsonNode> item = jsonPathUtils.findInArray(testJson, "items", node -> node.get("id").asInt() == 2);
        assertTrue(item.isPresent());
        assertEquals("Item 2", item.get().get("name").asText());
    }

    // Group 5: Field name retrieval tests
    @Test
    void testGetFieldNames() throws ConfigurationException {
        List<String> fieldNames = jsonPathUtils.getFieldNames(testJson, "settings");
        assertTrue(fieldNames.containsAll(List.of("debug", "maxRetries", "timeout")));
    }
}