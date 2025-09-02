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
        void shouldGetStringFromSimplePath() {
            Optional<String> result = jsonPathUtils.getString(testJson, "stringField");
            
            assertTrue(result.isPresent());
            assertEquals("testValue", result.get());
        }

        @Test
        @DisplayName("Should get string value from nested path")
        void shouldGetStringFromNestedPath() {
            Optional<String> result = jsonPathUtils.getString(testJson, "nested.innerString");
            
            assertTrue(result.isPresent());
            assertEquals("innerValue", result.get());
        }

        @Test
        @DisplayName("Should get string value from deeply nested path")
        void shouldGetStringFromDeeplyNestedPath() {
            Optional<String> result = jsonPathUtils.getString(testJson, "nested.deep.deepestValue");
            
            assertTrue(result.isPresent());
            assertEquals("found", result.get());
        }

        @Test
        @DisplayName("Should return empty for non-existent path")
        void shouldReturnEmptyForNonExistentPath() {
            Optional<String> result = jsonPathUtils.getString(testJson, "nonexistent.path");
            
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Should return empty for null value")
        void shouldReturnEmptyForNullValue() {
            Optional<String> result = jsonPathUtils.getString(testJson, "nullField");
            
            assertFalse(result.isPresent());
        }

        @ParameterizedTest
        @CsvSource({
            "stringField, testValue",
            "nested.innerString, innerValue",
            "nested.deep.deepestValue, found"
        })
        @DisplayName("Should get strings from various paths")
        void shouldGetStringsFromVariousPaths(String path, String expected) {
            Optional<String> result = jsonPathUtils.getString(testJson, path);
            
            assertTrue(result.isPresent());
            assertEquals(expected, result.get());
        }
    }

    @Nested
    @DisplayName("Integer Accessor Tests")
    class IntegerAccessorTests {

        @Test
        @DisplayName("Should get integer value from simple path")
        void shouldGetIntegerFromSimplePath() {
            Optional<Integer> result = jsonPathUtils.getInteger(testJson, "intField");
            
            assertTrue(result.isPresent());
            assertEquals(42, result.get());
        }

        @Test
        @DisplayName("Should get integer value from nested path")
        void shouldGetIntegerFromNestedPath() {
            Optional<Integer> result = jsonPathUtils.getInteger(testJson, "nested.innerInt");
            
            assertTrue(result.isPresent());
            assertEquals(100, result.get());
        }

        @Test
        @DisplayName("Should return empty for string field")
        void shouldReturnEmptyForStringField() {
            Optional<Integer> result = jsonPathUtils.getInteger(testJson, "stringField");
            
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Should handle numeric string conversion")
        void shouldHandleNumericStringConversion() {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("numericString", "123");
            
            Optional<Integer> result = jsonPathUtils.getIntegerWithConversion(node, "numericString");
            
            assertTrue(result.isPresent());
            assertEquals(123, result.get());
        }
    }

    @Nested
    @DisplayName("Boolean Accessor Tests")
    class BooleanAccessorTests {

        @Test
        @DisplayName("Should get boolean value from simple path")
        void shouldGetBooleanFromSimplePath() {
            Optional<Boolean> result = jsonPathUtils.getBoolean(testJson, "boolField");
            
            assertTrue(result.isPresent());
            assertTrue(result.get());
        }

        @Test
        @DisplayName("Should return empty for non-boolean field")
        void shouldReturnEmptyForNonBooleanField() {
            Optional<Boolean> result = jsonPathUtils.getBoolean(testJson, "stringField");
            
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Should handle boolean string conversion")
        void shouldHandleBooleanStringConversion() {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("boolString", "true");
            
            Optional<Boolean> result = jsonPathUtils.getBooleanWithConversion(node, "boolString");
            
            assertTrue(result.isPresent());
            assertTrue(result.get());
        }
    }

    @Nested
    @DisplayName("Array Navigation Tests")
    class ArrayNavigationTests {

        @Test
        @DisplayName("Should access array element by index")
        void shouldAccessArrayElementByIndex() {
            Optional<String> result = jsonPathUtils.getString(testJson, "arrayField[0]");
            
            assertTrue(result.isPresent());
            assertEquals("item1", result.get());
        }

        @Test
        @DisplayName("Should access array element with bracket notation")
        void shouldAccessArrayElementWithBracketNotation() {
            Optional<String> result = jsonPathUtils.getString(testJson, "arrayField.[1]");
            
            assertTrue(result.isPresent());
            assertEquals("item2", result.get());
        }

        @Test
        @DisplayName("Should get array size")
        void shouldGetArraySize() {
            Optional<Integer> size = jsonPathUtils.getArraySize(testJson, "arrayField");
            
            assertTrue(size.isPresent());
            assertEquals(3, size.get());
        }

        @Test
        @DisplayName("Should iterate over array elements")
        void shouldIterateOverArrayElements() {
            List<String> items = jsonPathUtils.getArrayStrings(testJson, "arrayField");
            
            assertEquals(3, items.size());
            assertEquals("item1", items.get(0));
            assertEquals("item2", items.get(1));
            assertEquals("item3", items.get(2));
        }

        @Test
        @DisplayName("Should access nested object in array")
        void shouldAccessNestedObjectInArray() {
            Optional<String> result = jsonPathUtils.getString(testJson, "objectArray[0].name");
            
            assertTrue(result.isPresent());
            assertEquals("first", result.get());
        }

        @Test
        @DisplayName("Should handle out of bounds array access")
        void shouldHandleOutOfBoundsArrayAccess() {
            Optional<String> result = jsonPathUtils.getString(testJson, "arrayField[10]");
            
            assertFalse(result.isPresent());
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
            "objectArray[0].id"
        })
        @DisplayName("Should detect existing paths")
        void shouldDetectExistingPaths(String path) {
            assertTrue(jsonPathUtils.pathExists(testJson, path));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "nonexistent",
            "nested.nonexistent",
            "arrayField.invalid",
            "objectArray[10]",
            "totally.made.up.path"
        })
        @DisplayName("Should detect non-existing paths")
        void shouldDetectNonExistingPaths(String path) {
            assertFalse(jsonPathUtils.pathExists(testJson, path));
        }

        @Test
        @DisplayName("Should check if path is null")
        void shouldCheckIfPathIsNull() {
            assertTrue(jsonPathUtils.isNull(testJson, "nullField"));
            assertFalse(jsonPathUtils.isNull(testJson, "stringField"));
        }
    }

    @Nested
    @DisplayName("Functional Operations Tests")
    class FunctionalOperationsTests {

        @Test
        @DisplayName("Should apply transformation to value")
        void shouldApplyTransformationToValue() {
            Optional<String> result = jsonPathUtils.getString(testJson, "stringField")
                    .map(String::toUpperCase);
            
            assertTrue(result.isPresent());
            assertEquals("TESTVALUE", result.get());
        }

        @Test
        @DisplayName("Should chain multiple path operations")
        void shouldChainMultiplePathOperations() {
            Optional<Integer> result = jsonPathUtils.getString(testJson, "nested.innerString")
                    .map(String::length);
            
            assertTrue(result.isPresent());
            assertEquals(10, result.get()); // "innerValue" has 10 characters
        }

        @Test
        @DisplayName("Should filter array elements")
        void shouldFilterArrayElements() {
            List<JsonNode> filtered = jsonPathUtils.filterArray(testJson, "objectArray", 
                    node -> node.get("id").asInt() > 1);
            
            assertEquals(1, filtered.size());
            assertEquals("second", filtered.get(0).get("name").asText());
        }

        @Test
        @DisplayName("Should map array elements")
        void shouldMapArrayElements() {
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
        void shouldHandleEmptyPath() {
            Optional<String> result = jsonPathUtils.getString(testJson, "");
            
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Should handle null JSON node")
        void shouldHandleNullJsonNode() {
            Optional<String> result = jsonPathUtils.getString(null, "anyPath");
            
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Should handle special characters in path")
        void shouldHandleSpecialCharactersInPath() {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("field.with.dots", "value");
            node.put("field-with-dashes", "value2");
            
            // Should handle escaped dots
            Optional<String> result1 = jsonPathUtils.getString(node, "field\\.with\\.dots");
            assertTrue(result1.isPresent());
            assertEquals("value", result1.get());
            
            // Should handle dashes
            Optional<String> result2 = jsonPathUtils.getString(node, "field-with-dashes");
            assertTrue(result2.isPresent());
            assertEquals("value2", result2.get());
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
                jsonPathUtils.pathExists(node1, "child.parent.child.parent");
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
            
            Optional<String> result = jsonPathUtils.getString(root, path.toString());
            assertTrue(result.isPresent());
            assertEquals("found", result.get());
        }
    }

    @Test
    @DisplayName("Should provide default values for missing paths")
    void shouldProvideDefaultValuesForMissingPaths() {
        String defaultString = jsonPathUtils.getString(testJson, "missing.path", "default");
        assertEquals("default", defaultString);
        
        int defaultInt = jsonPathUtils.getInteger(testJson, "missing.path", 999);
        assertEquals(999, defaultInt);
        
        boolean defaultBool = jsonPathUtils.getBoolean(testJson, "missing.path", true);
        assertTrue(defaultBool);
    }
}