package io.github.jspinak.brobot.runner.json.parsing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.runner.json.module.BrobotJsonModule;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for ConfigurationParser JSON parsing and error handling.
 * Tests cover:
 * - Standard and safe serialization with automatic fallback
 * - Circular reference handling through fallback mapper
 * - File I/O operations for JSON persistence
 * - Schema validation integration
 * - Error handling and recovery
 */
@DisplayName("ConfigurationParser - JSON Parsing and Error Handling")
public class ConfigurationParserTest extends BrobotTestBase {

    private ConfigurationParser configurationParser;
    
    @Mock
    private BrobotObjectMapper mockObjectMapper;
    
    @Mock
    private SchemaManager mockSchemaManager;
    
    @TempDir
    Path tempDir;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        configurationParser = new ConfigurationParser(mockObjectMapper, mockSchemaManager);
    }

    @Nested
    @DisplayName("Standard Serialization Tests")
    class StandardSerializationTests {

        @Test
        @DisplayName("Should serialize simple objects to JSON")
        void shouldSerializeSimpleObjects() throws JsonProcessingException {
            Map<String, Object> testObject = new HashMap<>();
            testObject.put("key1", "value1");
            testObject.put("key2", 42);
            
            when(mockObjectMapper.writeValueAsString(testObject))
                    .thenReturn("{\"key1\":\"value1\",\"key2\":42}");
            
            String json = configurationParser.serialize(testObject);
            
            assertNotNull(json);
            assertTrue(json.contains("key1"));
            assertTrue(json.contains("value1"));
            assertTrue(json.contains("42"));
            verify(mockObjectMapper).writeValueAsString(testObject);
        }

        @Test
        @DisplayName("Should deserialize JSON to objects")
        void shouldDeserializeJsonToObjects() throws JsonProcessingException {
            String json = "{\"x\":100,\"y\":200}";
            Location expectedLocation = new Location(100, 200);
            
            when(mockObjectMapper.readValue(json, Location.class))
                    .thenReturn(expectedLocation);
            
            Location result = configurationParser.deserialize(json, Location.class);
            
            assertNotNull(result);
            assertEquals(100, result.getX());
            assertEquals(200, result.getY());
            verify(mockObjectMapper).readValue(json, Location.class);
        }

        @Test
        @DisplayName("Should handle null input gracefully")
        void shouldHandleNullInput() throws JsonProcessingException {
            when(mockObjectMapper.writeValueAsString(null)).thenReturn("null");
            
            String json = configurationParser.serialize(null);
            
            assertEquals("null", json);
            verify(mockObjectMapper).writeValueAsString(null);
        }
    }

    @Nested
    @DisplayName("Safe Serialization with Fallback Tests")
    class SafeSerializationTests {

        @Test
        @DisplayName("Should use fallback mapper on circular reference")
        void shouldUseFallbackMapperOnCircularReference() throws JsonProcessingException {
            Map<String, Object> circularRef = new HashMap<>();
            circularRef.put("self", circularRef);
            
            // First attempt throws due to circular reference
            when(mockObjectMapper.writeValueAsString(circularRef))
                    .thenThrow(new JsonProcessingException("Circular reference") {});
            
            String json = configurationParser.safeSerialize(circularRef);
            
            assertNotNull(json);
            // Should have attempted with main mapper first
            verify(mockObjectMapper).writeValueAsString(circularRef);
        }

        @Test
        @DisplayName("Should serialize complex Brobot objects safely")
        void shouldSerializeComplexBrobotObjectsSafely() throws JsonProcessingException {
            PatternFindOptions options = new PatternFindOptions.Builder()
                    .setDoOnEach(PatternFindOptions.DoOnEach.FIRST)
                    .build();
            
            when(mockObjectMapper.writeValueAsString(options))
                    .thenReturn("{\"@type\":\"FIND\",\"doOnEach\":\"FIRST\"}");
            
            String json = configurationParser.safeSerialize(options);
            
            assertNotNull(json);
            assertTrue(json.contains("FIRST"));
            verify(mockObjectMapper).writeValueAsString(options);
        }

        @Test
        @DisplayName("Should handle serialization errors gracefully")
        void shouldHandleSerializationErrorsGracefully() throws JsonProcessingException {
            Object problematicObject = new Object() {
                @SuppressWarnings("unused")
                public String getError() {
                    throw new RuntimeException("Serialization error");
                }
            };
            
            when(mockObjectMapper.writeValueAsString(problematicObject))
                    .thenThrow(new JsonProcessingException("Error") {});
            
            String json = configurationParser.safeSerialize(problematicObject);
            
            assertNotNull(json);
            // Should have attempted serialization
            verify(mockObjectMapper).writeValueAsString(problematicObject);
        }
    }

    @Nested
    @DisplayName("File I/O Operations Tests")
    class FileIOOperationsTests {

        @Test
        @DisplayName("Should write JSON to file")
        void shouldWriteJsonToFile() throws IOException {
            Map<String, Object> data = new HashMap<>();
            data.put("test", "value");
            
            when(mockObjectMapper.writeValueAsString(data))
                    .thenReturn("{\"test\":\"value\"}");
            
            File outputFile = tempDir.resolve("test.json").toFile();
            
            configurationParser.writeToFile(data, outputFile);
            
            assertTrue(outputFile.exists());
            String content = Files.readString(outputFile.toPath());
            assertTrue(content.contains("test"));
            assertTrue(content.contains("value"));
        }

        @Test
        @DisplayName("Should read JSON from file")
        void shouldReadJsonFromFile() throws IOException {
            File inputFile = tempDir.resolve("input.json").toFile();
            String jsonContent = "{\"x\":10,\"y\":20,\"width\":100,\"height\":200}";
            Files.writeString(inputFile.toPath(), jsonContent);
            
            Region expectedRegion = new Region(10, 20, 100, 200);
            when(mockObjectMapper.readValue(jsonContent, Region.class))
                    .thenReturn(expectedRegion);
            
            Region result = configurationParser.readFromFile(inputFile, Region.class);
            
            assertNotNull(result);
            assertEquals(10, result.getX());
            assertEquals(20, result.getY());
            assertEquals(100, result.getW());
            assertEquals(200, result.getH());
        }

        @Test
        @DisplayName("Should handle file not found gracefully")
        void shouldHandleFileNotFoundGracefully() {
            File nonExistentFile = tempDir.resolve("nonexistent.json").toFile();
            
            assertThrows(IOException.class, () -> {
                configurationParser.readFromFile(nonExistentFile, Map.class);
            });
        }

        @Test
        @DisplayName("Should create parent directories when writing file")
        void shouldCreateParentDirectoriesWhenWritingFile() throws IOException {
            Map<String, Object> data = new HashMap<>();
            data.put("test", "value");
            
            when(mockObjectMapper.writeValueAsString(data))
                    .thenReturn("{\"test\":\"value\"}");
            
            File nestedFile = tempDir.resolve("nested/dir/test.json").toFile();
            
            configurationParser.writeToFile(data, nestedFile);
            
            assertTrue(nestedFile.exists());
            assertTrue(nestedFile.getParentFile().exists());
        }
    }

    @Nested
    @DisplayName("Schema Validation Tests")
    class SchemaValidationTests {

        @Test
        @DisplayName("Should validate JSON against schema")
        void shouldValidateJsonAgainstSchema() throws Exception {
            String json = "{\"type\":\"FIND\",\"similarity\":0.95}";
            String schemaName = "pattern-find-options";
            
            when(mockSchemaManager.validate(json, schemaName)).thenReturn(true);
            
            boolean isValid = configurationParser.validateAgainstSchema(json, schemaName);
            
            assertTrue(isValid);
            verify(mockSchemaManager).validate(json, schemaName);
        }

        @Test
        @DisplayName("Should handle validation errors")
        void shouldHandleValidationErrors() throws Exception {
            String invalidJson = "{\"type\":\"INVALID\"}";
            String schemaName = "pattern-find-options";
            
            when(mockSchemaManager.validate(invalidJson, schemaName))
                    .thenReturn(false);
            
            boolean isValid = configurationParser.validateAgainstSchema(invalidJson, schemaName);
            
            assertFalse(isValid);
            verify(mockSchemaManager).validate(invalidJson, schemaName);
        }

        @Test
        @DisplayName("Should validate before serialization if schema provided")
        void shouldValidateBeforeSerializationIfSchemaProvided() throws Exception {
            ClickOptions options = new ClickOptions.Builder().build();
            String schemaName = "click-options";
            
            String expectedJson = "{\"@type\":\"CLICK\"}";
            when(mockObjectMapper.writeValueAsString(options))
                    .thenReturn(expectedJson);
            when(mockSchemaManager.validate(expectedJson, schemaName))
                    .thenReturn(true);
            
            String json = configurationParser.serializeWithValidation(options, schemaName);
            
            assertNotNull(json);
            verify(mockSchemaManager).validate(expectedJson, schemaName);
            verify(mockObjectMapper).writeValueAsString(options);
        }
    }

    @Nested
    @DisplayName("JSON Tree Operations Tests")
    class JsonTreeOperationsTests {

        @Test
        @DisplayName("Should parse JSON to tree structure")
        void shouldParseJsonToTreeStructure() throws JsonProcessingException {
            String json = "{\"key\":\"value\",\"nested\":{\"inner\":\"data\"}}";
            JsonNode mockNode = mock(JsonNode.class);
            
            when(mockObjectMapper.readTree(json)).thenReturn(mockNode);
            
            JsonNode result = configurationParser.parseToTree(json);
            
            assertNotNull(result);
            assertEquals(mockNode, result);
            verify(mockObjectMapper).readTree(json);
        }

        @Test
        @DisplayName("Should convert tree to object")
        void shouldConvertTreeToObject() throws JsonProcessingException {
            JsonNode mockNode = mock(JsonNode.class);
            Location expectedLocation = new Location(50, 100);
            
            when(mockObjectMapper.treeToValue(mockNode, Location.class))
                    .thenReturn(expectedLocation);
            
            Location result = configurationParser.treeToObject(mockNode, Location.class);
            
            assertNotNull(result);
            assertEquals(expectedLocation, result);
            verify(mockObjectMapper).treeToValue(mockNode, Location.class);
        }

        @Test
        @DisplayName("Should create object node")
        void shouldCreateObjectNode() {
            var mockObjectNode = mock(com.fasterxml.jackson.databind.node.ObjectNode.class);
            
            when(mockObjectMapper.createObjectNode()).thenReturn(mockObjectNode);
            
            var result = configurationParser.createObjectNode();
            
            assertNotNull(result);
            assertEquals(mockObjectNode, result);
            verify(mockObjectMapper).createObjectNode();
        }
    }

    @Nested
    @DisplayName("Error Recovery Tests")
    class ErrorRecoveryTests {

        @Test
        @DisplayName("Should recover from partial JSON corruption")
        void shouldRecoverFromPartialJsonCorruption() throws JsonProcessingException {
            String corruptedJson = "{\"key\":\"value\",\"bad\":}";
            String fixedJson = "{\"key\":\"value\"}";
            
            // First attempt fails
            when(mockObjectMapper.readValue(corruptedJson, Map.class))
                    .thenThrow(new JsonProcessingException("Unexpected character") {});
            
            // Attempt recovery
            Map<String, Object> recovered = new HashMap<>();
            recovered.put("key", "value");
            when(mockObjectMapper.readValue(fixedJson, Map.class))
                    .thenReturn(recovered);
            
            // Test recovery logic
            Map<String, Object> result = configurationParser.tryRecover(corruptedJson, fixedJson, Map.class);
            
            assertNotNull(result);
            assertEquals("value", result.get("key"));
        }

        @Test
        @DisplayName("Should provide detailed error messages")
        void shouldProvideDetailedErrorMessages() {
            String invalidJson = "not json at all";
            
            ConfigurationException exception = assertThrows(ConfigurationException.class, () -> {
                configurationParser.parseWithExceptionHandling(invalidJson, Map.class);
            });
            
            assertNotNull(exception.getMessage());
            assertTrue(exception.getMessage().contains("Failed to parse"));
        }

        @Test
        @DisplayName("Should handle deeply nested errors")
        void shouldHandleDeeplyNestedErrors() throws JsonProcessingException {
            Map<String, Object> deeplyNested = new HashMap<>();
            Map<String, Object> level1 = new HashMap<>();
            Map<String, Object> level2 = new HashMap<>();
            level2.put("error", new Object() {
                @Override
                public String toString() {
                    throw new RuntimeException("Deep error");
                }
            });
            level1.put("level2", level2);
            deeplyNested.put("level1", level1);
            
            when(mockObjectMapper.writeValueAsString(deeplyNested))
                    .thenThrow(new JsonProcessingException("Deep nested error") {});
            
            String result = configurationParser.safeSerialize(deeplyNested);
            
            assertNotNull(result);
            // Should have attempted and handled the error
            verify(mockObjectMapper).writeValueAsString(deeplyNested);
        }
    }

    @Test
    @DisplayName("Should handle concurrent serialization")
    void shouldHandleConcurrentSerialization() throws InterruptedException {
        int threadCount = 10;
        List<Thread> threads = new ArrayList<>();
        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());
        
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            Thread thread = new Thread(() -> {
                try {
                    Map<String, Object> data = new HashMap<>();
                    data.put("thread", index);
                    
                    when(mockObjectMapper.writeValueAsString(data))
                            .thenReturn("{\"thread\":" + index + "}");
                    
                    String json = configurationParser.serialize(data);
                    assertNotNull(json);
                    assertTrue(json.contains(String.valueOf(index)));
                } catch (Exception e) {
                    exceptions.add(e);
                }
            });
            threads.add(thread);
            thread.start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        assertTrue(exceptions.isEmpty(), "Concurrent serialization failed: " + exceptions);
    }

    // Helper method for testing
    private static class TestData {
        public String field1;
        public int field2;
        
        public TestData(String field1, int field2) {
            this.field1 = field1;
            this.field2 = field2;
        }
    }
}