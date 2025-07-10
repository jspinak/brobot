package io.github.jspinak.brobot.runner.ui.config.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JsonConfigHandler.
 */
class JsonConfigHandlerTest {
    
    private JsonConfigHandler handler;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        handler = new JsonConfigHandler();
    }
    
    @Test
    @DisplayName("Should load JSON file successfully")
    void testLoadJsonFile() throws IOException {
        // Given
        Path jsonFile = tempDir.resolve("test.json");
        String jsonContent = "{\"name\": \"test\", \"version\": \"1.0.0\"}";
        Files.writeString(jsonFile, jsonContent);
        
        // When
        String loaded = handler.loadJsonFile(jsonFile);
        
        // Then
        assertNotNull(loaded);
        assertTrue(loaded.contains("\"name\": \"test\""));
    }
    
    @Test
    @DisplayName("Should save JSON file with pretty printing")
    void testSaveJsonFile() throws IOException {
        // Given
        Path jsonFile = tempDir.resolve("output.json");
        String jsonContent = "{\"name\":\"test\",\"version\":\"1.0.0\"}";
        
        // When
        handler.saveJsonFile(jsonFile, jsonContent);
        
        // Then
        assertTrue(Files.exists(jsonFile));
        String saved = Files.readString(jsonFile);
        assertTrue(saved.contains("  \"name\" : \"test\""), "Should be pretty printed");
    }
    
    @Test
    @DisplayName("Should extract value by key")
    void testExtractValue() {
        // Given
        String json = "{\"name\": \"test\", \"version\": \"1.0.0\"}";
        
        // When
        String name = handler.extractValue(json, "name");
        String version = handler.extractValue(json, "version");
        String missing = handler.extractValue(json, "missing");
        
        // Then
        assertEquals("test", name);
        assertEquals("1.0.0", version);
        assertEquals("", missing);
    }
    
    @Test
    @DisplayName("Should extract value by path")
    void testExtractValueByPath() {
        // Given
        String json = "{\"metadata\": {\"author\": \"John Doe\", \"license\": \"MIT\"}}";
        
        // When
        Optional<String> author = handler.extractValueByPath(json, "metadata.author");
        Optional<String> license = handler.extractValueByPath(json, "metadata.license");
        Optional<String> missing = handler.extractValueByPath(json, "metadata.missing");
        
        // Then
        assertTrue(author.isPresent());
        assertEquals("John Doe", author.get());
        assertTrue(license.isPresent());
        assertEquals("MIT", license.get());
        assertFalse(missing.isPresent());
    }
    
    @Test
    @DisplayName("Should update value by key")
    void testUpdateValue() {
        // Given
        String json = "{\"name\": \"test\", \"version\": \"1.0.0\"}";
        
        // When
        String updated = handler.updateValue(json, "version", "2.0.0");
        
        // Then
        assertTrue(updated.contains("\"version\" : \"2.0.0\""));
        assertTrue(updated.contains("\"name\" : \"test\""));
    }
    
    @Test
    @DisplayName("Should add new key-value pair")
    void testAddNewValue() {
        // Given
        String json = "{\"name\": \"test\"}";
        
        // When
        String updated = handler.updateValue(json, "version", "1.0.0");
        
        // Then
        assertTrue(updated.contains("\"version\" : \"1.0.0\""));
        assertTrue(updated.contains("\"name\" : \"test\""));
    }
    
    @Test
    @DisplayName("Should remove key when value is empty")
    void testRemoveValue() {
        // Given
        String json = "{\"name\": \"test\", \"version\": \"1.0.0\"}";
        
        // When
        String updated = handler.updateValue(json, "version", "");
        
        // Then
        assertTrue(updated.contains("\"name\" : \"test\""));
        assertFalse(updated.contains("version"));
    }
    
    @Test
    @DisplayName("Should update multiple values")
    void testUpdateMultipleValues() {
        // Given
        String json = "{\"name\": \"test\"}";
        Map<String, String> updates = new HashMap<>();
        updates.put("version", "1.0.0");
        updates.put("author", "John Doe");
        updates.put("name", "updated-test");
        
        // When
        String updated = handler.updateMultipleValues(json, updates);
        
        // Then
        assertTrue(updated.contains("\"name\" : \"updated-test\""));
        assertTrue(updated.contains("\"version\" : \"1.0.0\""));
        assertTrue(updated.contains("\"author\" : \"John Doe\""));
    }
    
    @Test
    @DisplayName("Should validate JSON")
    void testValidateJson() {
        // Given
        String validJson = "{\"name\": \"test\"}";
        String invalidJson = "{name: test}";
        String emptyJson = "";
        String nullJson = null;
        
        // Then
        assertTrue(handler.validateJson(validJson));
        assertFalse(handler.validateJson(invalidJson));
        assertFalse(handler.validateJson(emptyJson));
        assertFalse(handler.validateJson(nullJson));
    }
    
    @Test
    @DisplayName("Should parse JSON to map")
    void testParseToMap() throws Exception {
        // Given
        String json = "{\"name\": \"test\", \"version\": \"1.0.0\"}";
        
        // When
        Map<String, Object> map = handler.parseToMap(json);
        
        // Then
        assertEquals("test", map.get("name"));
        assertEquals("1.0.0", map.get("version"));
    }
    
    @Test
    @DisplayName("Should convert map to JSON")
    void testMapToJson() throws Exception {
        // Given
        Map<String, Object> map = new HashMap<>();
        map.put("name", "test");
        map.put("version", "1.0.0");
        
        // When
        String json = handler.mapToJson(map);
        
        // Then
        assertTrue(json.contains("\"name\" : \"test\""));
        assertTrue(json.contains("\"version\" : \"1.0.0\""));
    }
    
    @Test
    @DisplayName("Should merge JSON objects")
    void testMergeJson() {
        // Given
        String baseJson = "{\"name\": \"test\", \"version\": \"1.0.0\"}";
        String updateJson = "{\"version\": \"2.0.0\", \"author\": \"John Doe\"}";
        
        // When
        String merged = handler.mergeJson(baseJson, updateJson);
        
        // Then
        assertTrue(merged.contains("\"name\" : \"test\""));
        assertTrue(merged.contains("\"version\" : \"2.0.0\""));
        assertTrue(merged.contains("\"author\" : \"John Doe\""));
    }
    
    @Test
    @DisplayName("Should get all keys from JSON")
    void testGetKeys() {
        // Given
        String json = "{\"name\": \"test\", \"version\": \"1.0.0\", \"author\": \"John\"}";
        
        // When
        Set<String> keys = handler.getKeys(json);
        
        // Then
        assertEquals(3, keys.size());
        assertTrue(keys.contains("name"));
        assertTrue(keys.contains("version"));
        assertTrue(keys.contains("author"));
    }
    
    @Test
    @DisplayName("Should handle nested JSON updates")
    void testUpdateValueByPath() {
        // Given
        String json = "{\"metadata\": {\"author\": \"John\"}}";
        
        // When
        String updated = handler.updateValueByPath(json, "metadata.author", "Jane");
        
        // Then
        assertTrue(updated.contains("\"author\" : \"Jane\""));
    }
    
    @Test
    @DisplayName("Should create path if not exists")
    void testCreatePath() {
        // Given
        String json = "{}";
        
        // When
        String updated = handler.updateValueByPath(json, "metadata.author", "John");
        
        // Then
        assertTrue(updated.contains("metadata"));
        assertTrue(updated.contains("\"author\" : \"John\""));
    }
    
    @Test
    @DisplayName("Should handle invalid JSON gracefully")
    void testHandleInvalidJson() {
        // Given
        String invalidJson = "not json";
        
        // When
        String result = handler.extractValue(invalidJson, "key");
        String updated = handler.updateValue(invalidJson, "key", "value");
        
        // Then
        assertEquals("", result);
        assertEquals(invalidJson, updated); // Should return original on error
    }
    
    @Test
    @DisplayName("Should pretty print JSON")
    void testPrettyPrintJson() {
        // Given
        String compactJson = "{\"name\":\"test\",\"nested\":{\"key\":\"value\"}}";
        
        // When
        String pretty = handler.prettyPrintJson(compactJson);
        
        // Then
        assertTrue(pretty.contains("\n"));
        assertTrue(pretty.contains("  "));
    }
    
}