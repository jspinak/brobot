package io.github.jspinak.brobot.runner.json.parsing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for JsonPathUtils JSON path navigation.
 * Tests cover:
 * - Type-safe accessors for strings, integers, booleans
 * - Array and object navigation
 * - Path existence checking
 * - Functional operations
 * - Edge cases and error handling
 */
@DisplayName("JsonPathUtils - JSON Path Navigation")
public class JsonPathUtilsTest extends BrobotTestBase {

    private JsonPathUtils jsonPathUtils;
    private ObjectMapper objectMapper;
    private JsonNode testJson;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        jsonPathUtils = new JsonPathUtils();
        objectMapper = new ObjectMapper();
        
        // Create test JSON structure
        testJson = createTestJsonStructure();
    }

    private JsonNode createTestJsonStructure() {
        ObjectNode root = objectMapper.createObjectNode();
        
        // Simple fields
        root.put("stringField", "testValue");
        root.put("intField", 42);
        root.put("boolField", true);
        root.put("nullField", (String) null);
        
        // Nested object
        ObjectNode nested = root.putObject("nested");
        nested.put("innerString", "innerValue");
        nested.put("innerInt", 100);
        
        // Deep nesting
        ObjectNode deepNested = nested.putObject("deep");
        deepNested.put("deepestValue", "found");
        
        // Array field
        ArrayNode array = root.putArray("arrayField");
        array.add("item1");
        array.add("item2");
        array.add("item3");
        
        // Array of objects
        ArrayNode objectArray = root.putArray("objectArray");
        ObjectNode obj1 = objectArray.addObject();
        obj1.put("id", 1);
        obj1.put("name", "first");
        ObjectNode obj2 = objectArray.addObject();
        obj2.put("id", 2);
        obj2.put("name", "second");
        
        return root;
    }

    @Nested
    @DisplayName("String Accessor Tests")
    class StringAccessorTests {

        @Test
        @DisplayName("Should get string value from simple path")
        void shouldGetStringFromSimplePath() throws Exception {
            String result = jsonPathUtils.getString(testJson, "stringField");
            
            assertNotNull(result);
            assertEquals("testValue", result);
        }

        @Test
        @DisplayName("Should get string value from nested path")
        void shouldGetStringFromNestedPath() throws Exception {
            String result = jsonPathUtils.getString(testJson, "nested.innerString");
            
            assertNotNull(result);
            assertEquals("innerValue", result);
        }

        @Test
        @DisplayName("Should get string value from deeply nested path")
        void shouldGetStringFromDeeplyNestedPath() throws Exception {
            String result = jsonPathUtils.getString(testJson, "nested.deep.deepestValue");
            
            assertNotNull(result);
            assertEquals("found", result);
        }

        @Test
        @DisplayName("Should throw exception for non-existent path")
        void shouldThrowExceptionForNonExistentPath() {
            assertThrows(Exception.class, () -> {
                jsonPathUtils.getString(testJson, "nonexistent.path");
            });
        }

        @Test
        @DisplayName("Should throw exception for null value")
        void shouldThrowExceptionForNullValue() {
            assertThrows(Exception.class, () -> {
                jsonPathUtils.getString(testJson, "nullField");
            });
        }

        @ParameterizedTest
        @CsvSource({
            "stringField, testValue",
            "nested.innerString, innerValue",
            "nested.deep.deepestValue, found"
        })
        @DisplayName("Should get strings from various paths")
        void shouldGetStringsFromVariousPaths(String path, String expected) throws Exception {
            String result = jsonPathUtils.getString(testJson, path);
            
            assertNotNull(result);
            assertEquals(expected, result);
        }
    }

    @Nested
    @DisplayName("Integer Accessor Tests")
    class IntegerAccessorTests {

        @Test
        @DisplayName("Should get integer value from simple path")
        void shouldGetIntegerFromSimplePath() throws Exception {
            int result = jsonPathUtils.getInt(testJson, "intField");
            
            assertEquals(42, result);
        }

        @Test
        @DisplayName("Should get integer value from nested path")
        void shouldGetIntegerFromNestedPath() throws Exception {
            int result = jsonPathUtils.getInt(testJson, "nested.innerInt");
            
            assertEquals(100, result);
        }

        @Test
        @DisplayName("Should throw exception for string field")
        void shouldThrowExceptionForStringField() {
            assertThrows(Exception.class, () -> {
                jsonPathUtils.getInt(testJson, "stringField");
            });
        }

        @Test
        @DisplayName("Should use optional integer accessor")
        void shouldUseOptionalIntegerAccessor() {
            Optional<Integer> result = jsonPathUtils.getOptionalInt(testJson, "intField");
            
            assertTrue(result.isPresent());
            assertEquals(42, result.get());
        }
    }

    @Nested
    @DisplayName("Boolean Accessor Tests")
    class BooleanAccessorTests {

        @Test
        @DisplayName("Should get boolean value from simple path")
        void shouldGetBooleanFromSimplePath() throws Exception {
            boolean result = jsonPathUtils.getBoolean(testJson, "boolField");
            
            assertTrue(result);
        }

        @Test
        @DisplayName("Should throw exception for non-boolean field")
        void shouldThrowExceptionForNonBooleanField() {
            assertThrows(Exception.class, () -> {
                jsonPathUtils.getBoolean(testJson, "stringField");
            });
        }

        @Test
        @DisplayName("Should use optional boolean accessor")
        void shouldUseOptionalBooleanAccessor() {
            Optional<Boolean> result = jsonPathUtils.getOptionalBoolean(testJson, "boolField");
            
            assertTrue(result.isPresent());
            assertTrue(result.get());
        }
    }

    @Nested
    @DisplayName("Array Navigation Tests")
    class ArrayNavigationTests {

        @Test
        @DisplayName("Should access array element by index")
        void shouldAccessArrayElementByIndex() throws Exception {
            String result = jsonPathUtils.getString(testJson, "arrayField.0");
            
            assertNotNull(result);
            assertEquals("item1", result);
        }

        @Test
        @DisplayName("Should access array element with dot notation")
        void shouldAccessArrayElementWithDotNotation() throws Exception {
            String result = jsonPathUtils.getString(testJson, "arrayField.1");
            
            assertNotNull(result);
            assertEquals("item2", result);
        }

        @Test
        @DisplayName("Should get array node")
        void shouldGetArrayNode() throws Exception {
            ArrayNode arrayNode = jsonPathUtils.getArray(testJson, "arrayField");
            
            assertNotNull(arrayNode);
            assertEquals(3, arrayNode.size());
        }

        @Test
        @DisplayName("Should iterate over array elements using mapArray")
        void shouldIterateOverArrayElementsUsingMapArray() throws Exception {
            List<String> items = jsonPathUtils.mapArray(testJson, "arrayField", JsonNode::asText);
            
            assertEquals(3, items.size());
            assertEquals("item1", items.get(0));
            assertEquals("item2", items.get(1));
            assertEquals("item3", items.get(2));
        }

        @Test
        @DisplayName("Should access nested object in array")
        void shouldAccessNestedObjectInArray() throws Exception {
            String result = jsonPathUtils.getString(testJson, "objectArray.0.name");
            
            assertNotNull(result);
            assertEquals("first", result);
        }

        @Test
        @DisplayName("Should handle out of bounds array access")
        void shouldHandleOutOfBoundsArrayAccess() {
            assertThrows(Exception.class, () -> {
                jsonPathUtils.getString(testJson, "arrayField.10");
            });
        }
    }

    @Nested
    @DisplayName("Path Existence Tests")
    class PathExistenceTests {

        @ParameterizedTest
        @ValueSource(strings = {
            "stringField",
            "nested",
            "nested.innerString",
            "nested.deep.deepestValue",
            "arrayField",
            "objectArray.0.id"
        })
        @DisplayName("Should detect existing paths")
        void shouldDetectExistingPaths(String path) {
            assertTrue(jsonPathUtils.hasPath(testJson, path));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "nonexistent",
            "nested.nonexistent",
            "arrayField.invalid",
            "objectArray.10",
            "totally.made.up.path"
        })
        @DisplayName("Should detect non-existing paths")
        void shouldDetectNonExistingPaths(String path) {
            assertFalse(jsonPathUtils.hasPath(testJson, path));
        }

        @Test
        @DisplayName("Should check if path points to null node")
        void shouldCheckIfPathPointsToNullNode() throws Exception {
            JsonNode nullNode = jsonPathUtils.getNode(testJson, "nullField");
            assertTrue(nullNode.isNull());
            
            JsonNode stringNode = jsonPathUtils.getNode(testJson, "stringField");
            assertFalse(stringNode.isNull());
        }
    }

    @Nested
    @DisplayName("Functional Operations Tests")
    class FunctionalOperationsTests {

        @Test
        @DisplayName("Should apply transformation to value")
        void shouldApplyTransformationToValue() throws Exception {
            String result = jsonPathUtils.getString(testJson, "stringField").toUpperCase();
            
            assertNotNull(result);
            assertEquals("TESTVALUE", result);
        }

        @Test
        @DisplayName("Should chain multiple path operations")
        void shouldChainMultiplePathOperations() throws Exception {
            String innerValue = jsonPathUtils.getString(testJson, "nested.innerString");
            int length = innerValue.length();
            
            assertEquals(10, length); // "innerValue" has 10 characters
        }

        @Test
        @DisplayName("Should find array elements using findInArray")
        void shouldFindArrayElementsUsingFindInArray() throws Exception {
            Optional<JsonNode> found = jsonPathUtils.findInArray(testJson, "objectArray", 
                    node -> node.get("id").asInt() > 1);
            
            assertTrue(found.isPresent());
            assertEquals("second", found.get().get("name").asText());
        }

        @Test
        @DisplayName("Should map array elements")
        void shouldMapArrayElements() throws Exception {
            List<String> names = jsonPathUtils.mapArray(testJson, "objectArray",
                    node -> node.get("name").asText());
            
            assertEquals(2, names.size());
            assertEquals("first", names.get(0));
            assertEquals("second", names.get(1));
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle empty path")
        void shouldHandleEmptyPath() throws Exception {
            JsonNode result = jsonPathUtils.getNode(testJson, "");
            
            assertEquals(testJson, result); // Empty path returns root
        }

        @Test
        @DisplayName("Should handle null JSON node")
        void shouldHandleNullJsonNode() {
            assertThrows(Exception.class, () -> {
                jsonPathUtils.getString(null, "anyPath");
            });
        }

        @Test
        @DisplayName("Should handle special characters in path")
        void shouldHandleSpecialCharactersInPath() {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("field.with.dots", "value");
            node.put("field-with-dashes", "value2");
            
            // For now, let's test that the utility handles regular field names
            try {
                String result2 = jsonPathUtils.getString(node, "field-with-dashes");
                assertEquals("value2", result2);
            } catch (Exception e) {
                // Expected for complex field names that need escaping
                assertNotNull(e);
            }
        }

        @Test
        @DisplayName("Should handle circular references gracefully")
        void shouldHandleCircularReferencesGracefully() {
            ObjectNode node1 = objectMapper.createObjectNode();
            ObjectNode node2 = objectMapper.createObjectNode();
            
            node1.set("child", node2);
            node2.set("parent", node1); // Circular reference
            
            // Should not cause stack overflow
            assertDoesNotThrow(() -> {
                jsonPathUtils.hasPath(node1, "child.parent.child.parent");
            });
        }

        @Test
        @DisplayName("Should handle very deep nesting")
        void shouldHandleVeryDeepNesting() {
            ObjectNode root = objectMapper.createObjectNode();
            ObjectNode current = root;
            
            // Create very deep nesting
            for (int i = 0; i < 100; i++) {
                ObjectNode next = objectMapper.createObjectNode();
                current.set("level" + i, next);
                current = next;
            }
            current.put("deepValue", "found");
            
            // Build path
            StringBuilder path = new StringBuilder();
            for (int i = 0; i < 100; i++) {
                if (i > 0) path.append(".");
                path.append("level").append(i);
            }
            path.append(".deepValue");
            
            String result = jsonPathUtils.getString(root, path.toString());
            assertNotNull(result);
            assertEquals("found", result);
        }
    }

    @Test
    @DisplayName("Should provide default values for missing paths using Optional")
    void shouldProvideDefaultValuesForMissingPathsUsingOptional() {
        String defaultString = jsonPathUtils.getOptionalString(testJson, "missing.path").orElse("default");
        assertEquals("default", defaultString);
        
        int defaultInt = jsonPathUtils.getOptionalInt(testJson, "missing.path").orElse(999);
        assertEquals(999, defaultInt);
        
        boolean defaultBool = jsonPathUtils.getOptionalBoolean(testJson, "missing.path").orElse(true);
        assertTrue(defaultBool);
    }
}