package io.github.jspinak.brobot.runner.json.parsing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigurationParserTest {

    @Mock
    private SchemaManager schemaManager;
    
    @Mock
    private BrobotObjectMapper objectMapper;
    
    @Mock
    private com.fasterxml.jackson.databind.ObjectWriter objectWriter;
    
    private ConfigurationParser configurationParser;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        configurationParser = new ConfigurationParser(schemaManager, objectMapper);
    }
    
    @Test
    void testParseJson_ValidJson() throws Exception {
        // Setup
        String json = "{\"name\": \"test\", \"value\": 123}";
        JsonNode expectedNode = new ObjectMapper().readTree(json);
        when(objectMapper.readTree(json)).thenReturn(expectedNode);
        
        // Execute
        JsonNode result = configurationParser.parseJson(json);
        
        // Verify
        assertNotNull(result);
        assertEquals(expectedNode, result);
        verify(objectMapper).readTree(json);
    }
    
    @Test
    void testParseJson_InvalidJson() throws Exception {
        // Setup
        String invalidJson = "{invalid json}";
        when(objectMapper.readTree(invalidJson)).thenThrow(new JsonProcessingException("Invalid JSON") {});
        
        // Execute & Verify
        ConfigurationException exception = assertThrows(ConfigurationException.class, 
            () -> configurationParser.parseJson(invalidJson));
        assertTrue(exception.getMessage().contains("Failed to parse JSON"));
        assertTrue(exception.getCause() instanceof JsonProcessingException);
    }
    
    @Test
    void testConvertJson_FromNode() throws Exception {
        // Setup
        JsonNode jsonNode = new ObjectMapper().readTree("{\"name\": \"test\"}");
        TestObject expectedObject = new TestObject("test");
        when(objectMapper.treeToValue(jsonNode, TestObject.class)).thenReturn(expectedObject);
        
        // Execute
        TestObject result = configurationParser.convertJson(jsonNode, TestObject.class);
        
        // Verify
        assertNotNull(result);
        assertEquals(expectedObject, result);
        verify(objectMapper).treeToValue(jsonNode, TestObject.class);
    }
    
    @Test
    void testConvertJson_FromString() throws Exception {
        // Setup
        String json = "{\"name\": \"test\"}";
        TestObject expectedObject = new TestObject("test");
        when(objectMapper.readValue(json, TestObject.class)).thenReturn(expectedObject);
        
        // Execute
        TestObject result = configurationParser.convertJson(json, TestObject.class);
        
        // Verify
        assertNotNull(result);
        assertEquals(expectedObject, result);
        verify(objectMapper).readValue(json, TestObject.class);
    }
    
    @Test
    void testConvertJson_ConversionError() throws Exception {
        // Setup
        String json = "{\"wrongField\": \"value\"}";
        when(objectMapper.readValue(json, TestObject.class))
            .thenThrow(new JsonProcessingException("Unrecognized field") {});
        
        // Execute & Verify
        ConfigurationException exception = assertThrows(ConfigurationException.class,
            () -> configurationParser.convertJson(json, TestObject.class));
        assertTrue(exception.getMessage().contains("Failed to convert JSON"));
    }
    
    @Test
    void testToJson_Success() throws Exception {
        // Setup
        TestObject object = new TestObject("test");
        String expectedJson = "{\"name\":\"test\"}";
        when(objectMapper.writeValueAsString(object)).thenReturn(expectedJson);
        
        // Execute
        String result = configurationParser.toJson(object);
        
        // Verify
        assertEquals(expectedJson, result);
        verify(objectMapper).writeValueAsString(object);
    }
    
    @Test
    void testToJson_FallbackToSafe() throws Exception {
        // Setup
        TestObject object = new TestObject("test");
        String expectedJson = "{\"name\":\"test\"}";
        
        // First attempt fails
        when(objectMapper.writeValueAsString(object))
            .thenThrow(new JsonProcessingException("Circular reference") {});
        
        // Execute
        String result = configurationParser.toJson(object);
        
        // Verify - fallback mapper handles it
        assertNotNull(result);
        assertTrue(result.contains("test"));
        verify(objectMapper).writeValueAsString(object);
    }
    
    @Test
    void testToPrettyJson_Success() throws Exception {
        // Setup
        TestObject object = new TestObject("test");
        String expectedJson = "{\n  \"name\" : \"test\"\n}";
        when(objectMapper.writerWithDefaultPrettyPrinter()).thenReturn(objectWriter);
        when(objectWriter.writeValueAsString(object)).thenReturn(expectedJson);
        
        // Execute
        String result = configurationParser.toPrettyJson(object);
        
        // Verify
        assertEquals(expectedJson, result);
        verify(objectMapper).writerWithDefaultPrettyPrinter();
    }
    
    @Test
    void testToJsonSafe_DirectCall() throws Exception {
        // Setup
        ProblematicObject object = new ProblematicObject();
        object.name = "test";
        object.image = "should be included"; // Filter only works with @JsonFilter annotation
        
        // Execute
        String result = configurationParser.toJsonSafe(object);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.contains("test"));
        // Since ProblematicObject is not annotated with @JsonFilter, all fields are included
        assertTrue(result.contains("image"));
    }
    
    @Test
    void testToPrettyJsonSafe_DirectCall() throws Exception {
        // Setup
        ProblematicObject object = new ProblematicObject();
        object.name = "test";
        object.bufferedImage = "should be included"; // Filter only works with @JsonFilter annotation
        
        // Execute
        String result = configurationParser.toPrettyJsonSafe(object);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.contains("test"));
        // Since ProblematicObject is not annotated with @JsonFilter, all fields are included
        assertTrue(result.contains("bufferedImage"));
        assertTrue(result.contains("\n")); // Pretty printed
    }
    
    @Test
    void testWriteToFile_Success() throws Exception {
        // Setup
        TestObject object = new TestObject("test");
        Path filePath = tempDir.resolve("test.json");
        String expectedJson = "{\n  \"name\" : \"test\"\n}";
        
        when(objectMapper.writerWithDefaultPrettyPrinter()).thenReturn(objectWriter);
        when(objectWriter.writeValueAsString(object)).thenReturn(expectedJson);
        
        // Execute
        configurationParser.writeToFile(object, filePath);
        
        // Verify
        assertTrue(Files.exists(filePath));
        String content = Files.readString(filePath);
        assertEquals(expectedJson, content);
    }
    
    @Test
    void testWriteToFileSafe_Success() throws Exception {
        // Setup
        ProblematicObject object = new ProblematicObject();
        object.name = "test";
        object.mat = "should be included"; // Filter only works with @JsonFilter annotation
        Path filePath = tempDir.resolve("test-safe.json");
        
        // Execute
        configurationParser.writeToFileSafe(object, filePath);
        
        // Verify
        assertTrue(Files.exists(filePath));
        String content = Files.readString(filePath);
        assertTrue(content.contains("test"));
        // Since ProblematicObject is not annotated with @JsonFilter, all fields are included
        assertTrue(content.contains("mat"));
    }
    
    @Test
    void testReadFromFile_Success() throws Exception {
        // Setup
        String json = "{\"name\":\"test\"}";
        Path filePath = tempDir.resolve("test-read.json");
        Files.writeString(filePath, json);
        
        TestObject expectedObject = new TestObject("test");
        when(objectMapper.readValue(json, TestObject.class)).thenReturn(expectedObject);
        
        // Execute
        TestObject result = configurationParser.readFromFile(filePath, TestObject.class);
        
        // Verify
        assertNotNull(result);
        assertEquals(expectedObject, result);
        verify(objectMapper).readValue(json, TestObject.class);
    }
    
    @Test
    void testReadFromFile_FileNotFound() {
        // Setup
        Path nonExistentFile = tempDir.resolve("nonexistent.json");
        
        // Execute & Verify
        assertThrows(IOException.class, 
            () -> configurationParser.readFromFile(nonExistentFile, TestObject.class));
    }
    
    @Test
    void testReadFromFile_InvalidJson() throws Exception {
        // Setup
        String invalidJson = "{invalid}";
        Path filePath = tempDir.resolve("invalid.json");
        Files.writeString(filePath, invalidJson);
        
        when(objectMapper.readValue(invalidJson, TestObject.class))
            .thenThrow(new JsonProcessingException("Invalid JSON") {});
        
        // Execute & Verify
        ConfigurationException exception = assertThrows(ConfigurationException.class,
            () -> configurationParser.readFromFile(filePath, TestObject.class));
        assertTrue(exception.getMessage().contains("Failed to convert JSON"));
    }
    
    // Test helper classes
    static class TestObject {
        private String name;
        
        public TestObject() {}
        
        public TestObject(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            TestObject that = (TestObject) obj;
            return name != null ? name.equals(that.name) : that.name == null;
        }
    }
    
    static class ProblematicObject {
        public String name;
        public String image;
        public String bufferedImage;
        public String mat;
        public String patterns;
        public String raster;
    }
}