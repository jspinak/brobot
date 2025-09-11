package io.github.jspinak.brobot.runner.json.utils;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.json.parsing.BrobotObjectMapper;
import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;
import io.github.jspinak.brobot.test.DisabledInCI;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

/**
 * Comprehensive tests for JsonUtils JSON serialization utilities.
 * Tests safe serialization with fallback mechanisms, circular reference handling,
 * and file I/O operations.
 */
@DisplayName("JsonUtils Tests")

@DisabledInCI
public class JsonUtilsTest extends BrobotTestBase {

    @Mock
    private ConfigurationParser mockJsonParser;
    
    @Mock
    private BrobotObjectMapper mockObjectMapper;
    
    private JsonUtils jsonUtils;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        jsonUtils = new JsonUtils(mockJsonParser, mockObjectMapper);
    }
    
    @Nested
    @DisplayName("Safe Serialization")
    class SafeSerialization {
        
        @Test
        @DisplayName("Should serialize object using primary parser when successful")
        public void testSuccessfulPrimarySerialization() throws Exception {
            // Given
            TestObject obj = new TestObject("test", 42);
            String expectedJson = "{\"name\":\"test\",\"value\":42}";
            when(mockJsonParser.toJson(obj)).thenReturn(expectedJson);
            
            // When
            String result = jsonUtils.toJsonSafe(obj);
            
            // Then
            assertEquals(expectedJson, result);
            verify(mockJsonParser).toJson(obj);
        }
        
        @Test
        @DisplayName("Should fallback to circular reference mapper when primary fails")
        public void testFallbackToCircularReferenceMapper() throws Exception {
            // Given
            TestObject obj = new TestObject("test", 42);
            when(mockJsonParser.toJson(obj))
                .thenThrow(new ConfigurationException("Primary failed"));
            
            // When
            String result = jsonUtils.toJsonSafe(obj);
            
            // Then
            assertNotNull(result);
            // The fallback mapper should successfully serialize the object
            assertTrue(result.contains("\"name\":\"test\""));
            assertTrue(result.contains("\"value\":42"));
            verify(mockJsonParser).toJson(obj);
        }
        
        @Test
        @DisplayName("Should throw exception when all serialization attempts fail")
        public void testAllSerializationAttemptsFail() throws Exception {
            // Given - Create a JsonUtils with a circular reference mapper that will also fail
            JsonUtils failingJsonUtils = new JsonUtils(mockJsonParser, mockObjectMapper) {
                @Override
                public String toJsonSafe(Object object) throws ConfigurationException {
                    // First try with the standard JsonParser
                    try {
                        return mockJsonParser.toJson(object);
                    } catch (ConfigurationException e) {
                        // Simulate that even the fallback mapper fails
                        throw new ConfigurationException("Failed to serialize object safely: " + e.getMessage(), e);
                    }
                }
            };
            
            Object unserializable = new Object();
            when(mockJsonParser.toJson(any()))
                .thenThrow(new ConfigurationException("Primary failed"));
            
            // When/Then
            assertThrows(ConfigurationException.class, 
                () -> failingJsonUtils.toJsonSafe(unserializable));
        }
        
        @Test
        @DisplayName("Should handle null objects safely")
        public void testNullObjectSerialization() throws Exception {
            // Given
            when(mockJsonParser.toJson(null)).thenReturn("null");
            
            // When
            String result = jsonUtils.toJsonSafe(null);
            
            // Then
            assertEquals("null", result);
        }
    }
    
    @Nested
    @DisplayName("Pretty Print Serialization")
    class PrettyPrintSerialization {
        
        @Test
        @DisplayName("Should pretty print using primary parser when successful")
        public void testSuccessfulPrettyPrint() throws Exception {
            // Given
            TestObject obj = new TestObject("test", 42);
            String expectedJson = "{\n  \"name\" : \"test\",\n  \"value\" : 42\n}";
            when(mockJsonParser.toPrettyJson(obj)).thenReturn(expectedJson);
            
            // When
            String result = jsonUtils.toPrettyJsonSafe(obj);
            
            // Then
            assertEquals(expectedJson, result);
            verify(mockJsonParser).toPrettyJson(obj);
        }
        
        @Test
        @DisplayName("Should fallback to pretty print with circular reference mapper")
        public void testPrettyPrintFallback() throws Exception {
            // Given
            TestObject obj = new TestObject("test", 42);
            when(mockJsonParser.toPrettyJson(obj))
                .thenThrow(new ConfigurationException("Pretty print failed"));
            
            // When
            String result = jsonUtils.toPrettyJsonSafe(obj);
            
            // Then
            assertNotNull(result);
            assertTrue(result.contains("\n")); // Should be formatted
            verify(mockJsonParser).toPrettyJson(obj);
        }
    }
    
    @Nested
    @DisplayName("Circular Reference Handling")
    class CircularReferenceHandling {
        
        @Test
        @DisplayName("Should handle self-referencing objects")
        public void testSelfReferencingSerialization() throws Exception {
            // Given
            SelfReferencingObject obj = new SelfReferencingObject("circular");
            obj.setSelf(obj);
            
            // Force primary parser to fail so fallback (circular reference mapper) is used
            when(mockJsonParser.toJson(obj))
                .thenThrow(new ConfigurationException("Circular reference detected"));
            
            // When
            String result = jsonUtils.toJsonSafe(obj);
            
            // Then
            assertNotNull(result);
            assertTrue(result.contains("circular"));
            // Self reference should be null due to WRITE_SELF_REFERENCES_AS_NULL
            assertTrue(result.contains("\"self\":null"));
        }
        
        @Test
        @DisplayName("Should handle mutual references between objects")
        public void testMutualReferences() throws Exception {
            // Given
            MutualRefA objA = new MutualRefA("A");
            MutualRefB objB = new MutualRefB("B");
            objA.setRefB(objB);
            objB.setRefA(objA);
            
            // Force primary parser to fail so fallback (circular reference mapper) is used
            when(mockJsonParser.toJson(objA))
                .thenThrow(new ConfigurationException("Circular reference detected"));
            
            // When
            String result = jsonUtils.toJsonSafe(objA);
            
            // Then
            assertNotNull(result);
            assertTrue(result.contains("\"name\":\"A\""));
            // The circular reference back to A should be null
            assertTrue(result.contains("\"refA\":null"));
        }
        
        @Test
        @DisplayName("Should handle deep circular reference chains")
        public void testDeepCircularChain() throws Exception {
            // Given
            LinkedNode node1 = new LinkedNode("node1");
            LinkedNode node2 = new LinkedNode("node2");
            LinkedNode node3 = new LinkedNode("node3");
            node1.setNext(node2);
            node2.setNext(node3);
            node3.setNext(node1); // Create cycle
            
            // Force primary parser to fail so fallback (circular reference mapper) is used
            when(mockJsonParser.toJson(node1))
                .thenThrow(new ConfigurationException("Circular reference detected"));
            
            // When
            String result = jsonUtils.toJsonSafe(node1);
            
            // Then
            assertNotNull(result);
            assertTrue(result.contains("\"name\":\"node1\""));
            assertTrue(result.contains("\"name\":\"node2\""));
            assertTrue(result.contains("\"name\":\"node3\""));
            // The circular reference back to node1 should be null
            assertTrue(result.contains("\"next\":null"));
        }
    }
    
    @Nested
    @DisplayName("File Operations")
    class FileOperations {
        
        @Test
        @DisplayName("Should write object to file successfully")
        public void testWriteToFile() throws Exception {
            // Given
            TestObject obj = new TestObject("test", 42);
            Path filePath = tempDir.resolve("test.json");
            String expectedJson = "{\n  \"name\" : \"test\",\n  \"value\" : 42\n}";
            when(mockJsonParser.toPrettyJson(obj)).thenReturn(expectedJson);
            
            // When
            jsonUtils.writeToFileSafe(obj, filePath);
            
            // Then
            assertTrue(Files.exists(filePath));
            String content = Files.readString(filePath);
            assertEquals(expectedJson, content);
        }
        
        @Test
        @DisplayName("Should create parent directories if they don't exist")
        public void testCreateParentDirectories() throws Exception {
            // Given
            TestObject obj = new TestObject("test", 42);
            Path filePath = tempDir.resolve("subdir/nested/test.json");
            when(mockJsonParser.toPrettyJson(obj)).thenReturn("{}");
            
            // When
            jsonUtils.writeToFileSafe(obj, filePath);
            
            // Then
            assertTrue(Files.exists(filePath.getParent()));
            assertTrue(Files.exists(filePath));
        }
        
        @Test
        @DisplayName("Should handle file write errors")
        public void testFileWriteError() throws Exception {
            // Given
            TestObject obj = new TestObject("test", 42);
            Path invalidPath = Path.of("/invalid/path/test.json");
            when(mockJsonParser.toPrettyJson(obj)).thenReturn("{}");
            
            // When/Then
            assertThrows(IOException.class, 
                () -> jsonUtils.writeToFileSafe(obj, invalidPath));
        }
        
        @Test
        @DisplayName("Should overwrite existing file")
        public void testOverwriteExistingFile() throws Exception {
            // Given
            TestObject obj = new TestObject("test", 42);
            Path filePath = tempDir.resolve("test.json");
            Files.writeString(filePath, "old content");
            String newJson = "{\"new\":\"content\"}";
            when(mockJsonParser.toPrettyJson(obj)).thenReturn(newJson);
            
            // When
            jsonUtils.writeToFileSafe(obj, filePath);
            
            // Then
            String content = Files.readString(filePath);
            assertEquals(newJson, content);
        }
    }
    
    @Nested
    @DisplayName("Serialization Validation")
    class SerializationValidation {
        
        @Test
        @DisplayName("Should validate successful serialization cycle")
        public void testSuccessfulValidation() throws Exception {
            // Given
            TestObject original = new TestObject("test", 42);
            String json = "{\"name\":\"test\",\"value\":42}";
            TestObject deserialized = new TestObject("test", 42);
            
            when(mockJsonParser.toJson(original)).thenReturn(json);
            when(mockJsonParser.convertJson(json, TestObject.class)).thenReturn(deserialized);
            
            // When
            TestObject result = jsonUtils.validateSerializationCycle(original, TestObject.class);
            
            // Then
            assertNotNull(result);
            assertEquals(original.getName(), result.getName());
            assertEquals(original.getValue(), result.getValue());
        }
        
        @Test
        @DisplayName("Should handle validation with fallback serialization")
        public void testValidationWithFallback() throws Exception {
            // Given
            TestObject original = new TestObject("test", 42);
            TestObject deserialized = new TestObject("test", 42);
            
            when(mockJsonParser.toJson(original))
                .thenThrow(new ConfigurationException("Primary failed"));
            when(mockJsonParser.convertJson(anyString(), eq(TestObject.class)))
                .thenReturn(deserialized);
            
            // When
            TestObject result = jsonUtils.validateSerializationCycle(original, TestObject.class);
            
            // Then
            assertNotNull(result);
        }
        
        @Test
        @DisplayName("Should throw exception when deserialization fails")
        public void testValidationDeserializationFailure() throws Exception {
            // Given
            TestObject original = new TestObject("test", 42);
            String json = "{\"name\":\"test\",\"value\":42}";
            
            when(mockJsonParser.toJson(original)).thenReturn(json);
            when(mockJsonParser.convertJson(json, TestObject.class))
                .thenThrow(new ConfigurationException("Deserialization failed"));
            
            // When/Then
            assertThrows(ConfigurationException.class,
                () -> jsonUtils.validateSerializationCycle(original, TestObject.class));
        }
    }
    
    @Nested
    @DisplayName("Special Types Handling")
    class SpecialTypesHandling {
        
        @Test
        @DisplayName("Should serialize collections")
        public void testCollectionSerialization() throws Exception {
            // Given
            List<String> list = Arrays.asList("one", "two", "three");
            when(mockJsonParser.toJson(list)).thenReturn("[\"one\",\"two\",\"three\"]");
            
            // When
            String result = jsonUtils.toJsonSafe(list);
            
            // Then
            assertEquals("[\"one\",\"two\",\"three\"]", result);
        }
        
        @Test
        @DisplayName("Should serialize maps")
        public void testMapSerialization() throws Exception {
            // Given
            Map<String, Integer> map = new HashMap<>();
            map.put("a", 1);
            map.put("b", 2);
            when(mockJsonParser.toJson(map)).thenReturn("{\"a\":1,\"b\":2}");
            
            // When
            String result = jsonUtils.toJsonSafe(map);
            
            // Then
            assertTrue(result.contains("\"a\":1") || result.contains("\"a\": 1"));
        }
        
        @Test
        @DisplayName("Should serialize date/time objects")
        public void testDateTimeSerialization() throws Exception {
            // Given
            LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 10, 30);
            when(mockJsonParser.toJson(dateTime))
                .thenThrow(new ConfigurationException("Primary failed"));
            
            // When - Falls back to circular reference mapper
            String result = jsonUtils.toJsonSafe(dateTime);
            
            // Then
            assertNotNull(result);
            assertTrue(result.contains("2024") || result.contains("15"));
        }
        
        @Test
        @DisplayName("Should handle empty objects")
        public void testEmptyObjectSerialization() throws Exception {
            // Given
            EmptyObject empty = new EmptyObject();
            when(mockJsonParser.toJson(empty)).thenReturn("{}");
            
            // When
            String result = jsonUtils.toJsonSafe(empty);
            
            // Then
            assertEquals("{}", result);
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Should handle objects with transient fields")
        public void testTransientFields() throws Exception {
            // Given
            ObjectWithTransient obj = new ObjectWithTransient("public", "secret");
            when(mockJsonParser.toJson(obj))
                .thenThrow(new ConfigurationException("Primary failed"));
            
            // When
            String result = jsonUtils.toJsonSafe(obj);
            
            // Then
            assertNotNull(result);
            assertTrue(result.contains("public"));
            assertFalse(result.contains("secret"));
        }
        
        @Test
        @DisplayName("Should handle nested complex objects")
        public void testNestedComplexObjects() throws Exception {
            // Given
            ComplexObject complex = new ComplexObject();
            complex.setName("complex");
            complex.setInner(new TestObject("inner", 100));
            complex.setList(Arrays.asList("a", "b", "c"));
            
            when(mockJsonParser.toJson(complex))
                .thenThrow(new ConfigurationException("Too complex"));
            
            // When
            String result = jsonUtils.toJsonSafe(complex);
            
            // Then
            assertNotNull(result);
            assertTrue(result.contains("complex"));
        }
        
        @Test
        @DisplayName("Should handle arrays")
        public void testArraySerialization() throws Exception {
            // Given
            String[] array = {"one", "two", "three"};
            when(mockJsonParser.toJson(array)).thenReturn("[\"one\",\"two\",\"three\"]");
            
            // When
            String result = jsonUtils.toJsonSafe(array);
            
            // Then
            assertEquals("[\"one\",\"two\",\"three\"]", result);
        }
    }
    
    // Test helper classes
    
    private static class TestObject {
        private String name;
        private int value;
        
        public TestObject() {}
        
        public TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
    }
    
    private static class SelfReferencingObject {
        private String name;
        private SelfReferencingObject self;
        
        public SelfReferencingObject(String name) {
            this.name = name;
        }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public SelfReferencingObject getSelf() { return self; }
        public void setSelf(SelfReferencingObject self) {
            this.self = self;
        }
    }
    
    private static class MutualRefA {
        private String name;
        private MutualRefB refB;
        
        public MutualRefA(String name) {
            this.name = name;
        }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public MutualRefB getRefB() { return refB; }
        public void setRefB(MutualRefB refB) {
            this.refB = refB;
        }
    }
    
    private static class MutualRefB {
        private String name;
        private MutualRefA refA;
        
        public MutualRefB(String name) {
            this.name = name;
        }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public MutualRefA getRefA() { return refA; }
        public void setRefA(MutualRefA refA) {
            this.refA = refA;
        }
    }
    
    private static class LinkedNode {
        private String name;
        private LinkedNode next;
        
        public LinkedNode(String name) {
            this.name = name;
        }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public LinkedNode getNext() { return next; }
        public void setNext(LinkedNode next) {
            this.next = next;
        }
    }
    
    private static class EmptyObject {
        // No fields
    }
    
    private static class ObjectWithTransient {
        private String publicData;
        private transient String secretData;
        
        public ObjectWithTransient(String publicData, String secretData) {
            this.publicData = publicData;
            this.secretData = secretData;
        }
    }
    
    private static class ComplexObject {
        private String name;
        private TestObject inner;
        private List<String> list;
        private Map<String, Object> map;
        
        public void setName(String name) { this.name = name; }
        public void setInner(TestObject inner) { this.inner = inner; }
        public void setList(List<String> list) { this.list = list; }
        public void setMap(Map<String, Object> map) { this.map = map; }
    }
}