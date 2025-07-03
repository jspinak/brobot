package io.github.jspinak.brobot.runner.json.parsing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.jspinak.brobot.runner.json.module.BrobotJsonModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrobotObjectMapperTest {

    @Mock
    private BrobotJsonModule brobotJsonModule;
    
    private BrobotObjectMapper brobotObjectMapper;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        when(brobotJsonModule.getModuleName()).thenReturn("BrobotJsonModule");
        when(brobotJsonModule.version()).thenReturn(com.fasterxml.jackson.core.Version.unknownVersion());
        brobotObjectMapper = new BrobotObjectMapper(brobotJsonModule);
    }
    
    @Test
    void testReadTree_FromString() throws Exception {
        // Setup
        String json = "{\"name\": \"test\", \"value\": 42}";
        
        // Execute
        JsonNode result = brobotObjectMapper.readTree(json);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isObject());
        assertEquals("test", result.get("name").asText());
        assertEquals(42, result.get("value").asInt());
    }
    
    @Test
    void testReadTree_FromString_WithComments() throws Exception {
        // Setup - JSON with comments should be allowed
        String jsonWithComments = "{\n" +
            "  // This is a comment\n" +
            "  \"name\": \"test\",\n" +
            "  \"value\": 42 // Another comment\n" +
            "}";
        
        // Execute
        JsonNode result = brobotObjectMapper.readTree(jsonWithComments);
        
        // Verify
        assertNotNull(result);
        assertEquals("test", result.get("name").asText());
        assertEquals(42, result.get("value").asInt());
    }
    
    @Test
    void testReadTree_FromFile() throws Exception {
        // Setup
        Path jsonFile = tempDir.resolve("test.json");
        String json = "{\"key\": \"value\", \"number\": 123}";
        Files.writeString(jsonFile, json);
        
        // Execute
        JsonNode result = brobotObjectMapper.readTree(jsonFile.toFile());
        
        // Verify
        assertNotNull(result);
        assertEquals("value", result.get("key").asText());
        assertEquals(123, result.get("number").asInt());
    }
    
    @Test
    void testReadTree_FromInputStream() throws Exception {
        // Setup
        String json = "{\"test\": true, \"count\": 5}";
        InputStream inputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        
        // Execute
        JsonNode result = brobotObjectMapper.readTree(inputStream);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.get("test").asBoolean());
        assertEquals(5, result.get("count").asInt());
    }
    
    @Test
    void testTreeToValue() throws Exception {
        // Setup
        String json = "{\"message\": \"Hello\", \"timestamp\": 1234567890}";
        JsonNode jsonNode = brobotObjectMapper.readTree(json);
        
        // Execute
        TestData result = brobotObjectMapper.treeToValue(jsonNode, TestData.class);
        
        // Verify
        assertNotNull(result);
        assertEquals("Hello", result.getMessage());
        assertEquals(1234567890L, result.getTimestamp());
    }
    
    @Test
    void testReadValue() throws Exception {
        // Setup
        String json = "{\"message\": \"Test message\", \"timestamp\": 9876543210}";
        
        // Execute
        TestData result = brobotObjectMapper.readValue(json, TestData.class);
        
        // Verify
        assertNotNull(result);
        assertEquals("Test message", result.getMessage());
        assertEquals(9876543210L, result.getTimestamp());
    }
    
    @Test
    void testReadValue_IgnoresUnknownProperties() throws Exception {
        // Setup - JSON with extra unknown field
        String json = "{\"message\": \"Test\", \"timestamp\": 123, \"unknownField\": \"ignored\"}";
        
        // Execute - Should not throw exception due to unknown field
        TestData result = brobotObjectMapper.readValue(json, TestData.class);
        
        // Verify
        assertNotNull(result);
        assertEquals("Test", result.getMessage());
        assertEquals(123L, result.getTimestamp());
    }
    
    @Test
    void testWriteValueAsString() throws Exception {
        // Setup
        TestData data = new TestData();
        data.setMessage("Hello World");
        data.setTimestamp(1234567890L);
        
        // Execute
        String result = brobotObjectMapper.writeValueAsString(data);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.contains("\"message\" : \"Hello World\""));
        assertTrue(result.contains("\"timestamp\" : 1234567890"));
        // Should be pretty printed (indented)
        assertTrue(result.contains("\n"));
        assertTrue(result.contains("  "));
    }
    
    @Test
    void testWriterWithDefaultPrettyPrinter() throws Exception {
        // Setup
        TestData data = new TestData();
        data.setMessage("Pretty");
        data.setTimestamp(999L);
        
        // Execute
        ObjectWriter writer = brobotObjectMapper.writerWithDefaultPrettyPrinter();
        String result = writer.writeValueAsString(data);
        
        // Verify
        assertNotNull(writer);
        assertNotNull(result);
        assertTrue(result.contains("\"message\" : \"Pretty\""));
        assertTrue(result.contains("\n")); // Pretty printed
    }
    
    @Test
    void testCreateObjectNode() {
        // Execute
        ObjectNode result = brobotObjectMapper.createObjectNode();
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isObject());
        assertEquals(0, result.size());
        
        // Test that we can add to it
        result.put("test", "value");
        assertEquals("value", result.get("test").asText());
    }
    
    @Test
    void testJavaTimeSupport() throws Exception {
        // Setup
        TimeData data = new TimeData();
        data.setDateTime(LocalDateTime.of(2023, 12, 25, 10, 30));
        data.setDuration(Duration.ofHours(2).plusMinutes(30));
        
        // Execute
        String json = brobotObjectMapper.writeValueAsString(data);
        TimeData result = brobotObjectMapper.readValue(json, TimeData.class);
        
        // Verify
        assertNotNull(result);
        assertEquals(data.getDateTime(), result.getDateTime());
        assertEquals(data.getDuration(), result.getDuration());
        // Should not be timestamps
        assertFalse(json.contains("1703502600"));
    }
    
    @Test
    void testInvalidJson_ThrowsException() {
        // Setup
        String invalidJson = "{invalid json}";
        
        // Execute & Verify
        assertThrows(JsonProcessingException.class, 
            () -> brobotObjectMapper.readTree(invalidJson));
    }
    
    @Test
    void testFileNotFound_ThrowsException() {
        // Setup
        File nonExistentFile = new File("nonexistent.json");
        
        // Execute & Verify
        assertThrows(IOException.class, 
            () -> brobotObjectMapper.readTree(nonExistentFile));
    }
    
    // Test helper classes
    static class TestData {
        private String message;
        private long timestamp;
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
    
    static class TimeData {
        private LocalDateTime dateTime;
        private Duration duration;
        
        public LocalDateTime getDateTime() {
            return dateTime;
        }
        
        public void setDateTime(LocalDateTime dateTime) {
            this.dateTime = dateTime;
        }
        
        public Duration getDuration() {
            return duration;
        }
        
        public void setDuration(Duration duration) {
            this.duration = duration;
        }
    }
}