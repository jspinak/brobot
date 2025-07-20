package io.github.jspinak.brobot.runner.ui.config.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Service for handling JSON configuration file operations.
 * Provides safe JSON parsing, updating, and validation.
 */
@Slf4j
@Service
public class JsonConfigHandler {
    
    private final ObjectMapper objectMapper;
    
    public JsonConfigHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }
    
    /**
     * Loads JSON content from a file.
     * 
     * @param path Path to the JSON file
     * @return JSON content as string
     * @throws IOException If file cannot be read
     */
    public String loadJsonFile(Path path) throws IOException {
        log.debug("Loading JSON file: {}", path);
        return Files.readString(path);
    }
    
    /**
     * Saves JSON content to a file.
     * 
     * @param path Path to save the file
     * @param jsonContent JSON content to save
     * @throws IOException If file cannot be written
     */
    public void saveJsonFile(Path path, String jsonContent) throws IOException {
        log.debug("Saving JSON file: {}", path);
        
        // Validate JSON before saving
        if (!validateJson(jsonContent)) {
            throw new IllegalArgumentException("Invalid JSON content");
        }
        
        // Pretty print the JSON
        String prettyJson = prettyPrintJson(jsonContent);
        Files.writeString(path, prettyJson);
    }
    
    /**
     * Extracts a value from JSON by key.
     * 
     * @param json JSON content
     * @param key Key to extract
     * @return Value as string, or empty string if not found
     */
    public String extractValue(String json, String key) {
        if (json == null || json.isEmpty() || key == null || key.isEmpty()) {
            return "";
        }
        
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode node = findNode(root, key);
            
            if (node != null && !node.isNull()) {
                if (node.isTextual()) {
                    return node.asText();
                } else {
                    return node.toString();
                }
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON: {}", e.getMessage());
        }
        
        return "";
    }
    
    /**
     * Extracts a value from JSON by path (e.g., "metadata.author").
     * 
     * @param json JSON content
     * @param path Path to the value
     * @return Optional containing the value if found
     */
    public Optional<String> extractValueByPath(String json, String path) {
        if (json == null || json.isEmpty() || path == null || path.isEmpty()) {
            return Optional.empty();
        }
        
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode node = root;
            
            for (String part : path.split("\\.")) {
                node = node.get(part);
                if (node == null) {
                    return Optional.empty();
                }
            }
            
            if (!node.isNull()) {
                if (node.isTextual()) {
                    return Optional.of(node.asText());
                } else {
                    return Optional.of(node.toString());
                }
            }
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON: {}", e.getMessage());
        }
        
        return Optional.empty();
    }
    
    /**
     * Updates a value in JSON by key.
     * 
     * @param json JSON content
     * @param key Key to update
     * @param value New value
     * @return Updated JSON string
     */
    public String updateValue(String json, String key, String value) {
        if (json == null || json.isEmpty()) {
            return json;
        }
        
        try {
            JsonNode root = objectMapper.readTree(json);
            
            if (root.isObject()) {
                ObjectNode objectNode = (ObjectNode) root;
                
                if (value == null || value.isEmpty()) {
                    // Remove the key if value is empty
                    objectNode.remove(key);
                } else {
                    // Update or add the key-value pair
                    objectNode.put(key, value);
                }
                
                return objectMapper.writeValueAsString(objectNode);
            }
        } catch (JsonProcessingException e) {
            log.error("Error updating JSON: {}", e.getMessage());
        }
        
        return json;
    }
    
    /**
     * Updates a value in JSON by path (e.g., "metadata.author").
     * 
     * @param json JSON content
     * @param path Path to the value
     * @param value New value
     * @return Updated JSON string
     */
    public String updateValueByPath(String json, String path, String value) {
        if (json == null || json.isEmpty() || path == null || path.isEmpty()) {
            return json;
        }
        
        try {
            JsonNode root = objectMapper.readTree(json);
            String[] parts = path.split("\\.");
            
            // Navigate to the parent node
            JsonNode parent = root;
            for (int i = 0; i < parts.length - 1; i++) {
                parent = parent.get(parts[i]);
                if (parent == null || !parent.isObject()) {
                    // Path doesn't exist, create it
                    return createPath(json, path, value);
                }
            }
            
            // Update the value
            if (parent.isObject()) {
                ObjectNode parentObject = (ObjectNode) parent;
                String lastKey = parts[parts.length - 1];
                
                if (value == null || value.isEmpty()) {
                    parentObject.remove(lastKey);
                } else {
                    parentObject.put(lastKey, value);
                }
            }
            
            return objectMapper.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            log.error("Error updating JSON by path: {}", e.getMessage());
        }
        
        return json;
    }
    
    /**
     * Updates multiple values in JSON.
     * 
     * @param json JSON content
     * @param updates Map of key-value pairs to update
     * @return Updated JSON string
     */
    public String updateMultipleValues(String json, Map<String, String> updates) {
        if (json == null || json.isEmpty() || updates == null || updates.isEmpty()) {
            return json;
        }
        
        try {
            JsonNode root = objectMapper.readTree(json);
            
            if (root.isObject()) {
                ObjectNode objectNode = (ObjectNode) root;
                
                for (Map.Entry<String, String> entry : updates.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    
                    if (value == null || value.isEmpty()) {
                        objectNode.remove(key);
                    } else {
                        objectNode.put(key, value);
                    }
                }
                
                return objectMapper.writeValueAsString(objectNode);
            }
        } catch (JsonProcessingException e) {
            log.error("Error updating multiple JSON values: {}", e.getMessage());
        }
        
        return json;
    }
    
    /**
     * Validates if the string is valid JSON.
     * 
     * @param json JSON content to validate
     * @return true if valid JSON, false otherwise
     */
    public boolean validateJson(String json) {
        if (json == null || json.isEmpty()) {
            return false;
        }
        
        try {
            objectMapper.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            log.debug("Invalid JSON: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Parses JSON to a Map.
     * 
     * @param json JSON content
     * @return Map representation of JSON
     * @throws JsonProcessingException If parsing fails
     */
    public Map<String, Object> parseToMap(String json) throws JsonProcessingException {
        if (json == null || json.isEmpty()) {
            return new HashMap<>();
        }
        
        return objectMapper.readValue(json, Map.class);
    }
    
    /**
     * Converts a Map to JSON string.
     * 
     * @param map Map to convert
     * @return JSON string
     * @throws JsonProcessingException If conversion fails
     */
    public String mapToJson(Map<String, Object> map) throws JsonProcessingException {
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        
        return objectMapper.writeValueAsString(map);
    }
    
    /**
     * Pretty prints JSON content.
     * 
     * @param json JSON content
     * @return Pretty printed JSON
     */
    public String prettyPrintJson(String json) {
        try {
            Object jsonObject = objectMapper.readValue(json, Object.class);
            return objectMapper.writeValueAsString(jsonObject);
        } catch (JsonProcessingException e) {
            log.error("Error pretty printing JSON: {}", e.getMessage());
            return json;
        }
    }
    
    /**
     * Merges two JSON objects.
     * 
     * @param baseJson Base JSON content
     * @param updateJson JSON content to merge
     * @return Merged JSON string
     */
    public String mergeJson(String baseJson, String updateJson) {
        try {
            JsonNode baseNode = objectMapper.readTree(baseJson);
            JsonNode updateNode = objectMapper.readTree(updateJson);
            
            JsonNode merged = merge(baseNode, updateNode);
            return objectMapper.writeValueAsString(merged);
        } catch (JsonProcessingException e) {
            log.error("Error merging JSON: {}", e.getMessage());
            return baseJson;
        }
    }
    
    /**
     * Gets all keys from a JSON object.
     * 
     * @param json JSON content
     * @return Set of keys
     */
    public Set<String> getKeys(String json) {
        Set<String> keys = new HashSet<>();
        
        try {
            JsonNode root = objectMapper.readTree(json);
            if (root.isObject()) {
                root.fieldNames().forEachRemaining(keys::add);
            }
        } catch (JsonProcessingException e) {
            log.error("Error getting JSON keys: {}", e.getMessage());
        }
        
        return keys;
    }
    
    // Helper methods
    
    private JsonNode findNode(JsonNode root, String key) {
        if (root.has(key)) {
            return root.get(key);
        }
        
        // Search recursively
        Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            JsonNode value = field.getValue();
            if (value.isObject()) {
                JsonNode found = findNode(value, key);
                if (found != null) {
                    return found;
                }
            }
        }
        
        return null;
    }
    
    private String createPath(String json, String path, String value) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(json);
        String[] parts = path.split("\\.");
        
        JsonNode current = root;
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            
            if (!current.has(part)) {
                ((ObjectNode) current).putObject(part);
            }
            current = current.get(part);
        }
        
        if (current.isObject()) {
            ((ObjectNode) current).put(parts[parts.length - 1], value);
        }
        
        return objectMapper.writeValueAsString(root);
    }
    
    private JsonNode merge(JsonNode baseNode, JsonNode updateNode) {
        if (!baseNode.isObject() || !updateNode.isObject()) {
            return updateNode;
        }
        
        ObjectNode result = objectMapper.createObjectNode();
        
        // Copy all fields from base
        baseNode.fields().forEachRemaining(field -> 
            result.set(field.getKey(), field.getValue()));
        
        // Merge fields from update
        updateNode.fields().forEachRemaining(field -> {
            String key = field.getKey();
            JsonNode value = field.getValue();
            
            if (result.has(key) && result.get(key).isObject() && value.isObject()) {
                // Recursively merge objects
                result.set(key, merge(result.get(key), value));
            } else {
                // Overwrite with update value
                result.set(key, value);
            }
        });
        
        return result;
    }
}