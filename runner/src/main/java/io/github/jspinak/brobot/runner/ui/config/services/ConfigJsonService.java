package io.github.jspinak.brobot.runner.ui.config.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for handling JSON operations in configuration files. Uses Jackson for proper JSON parsing
 * instead of regex.
 */
@Slf4j
@Service
public class ConfigJsonService {

    private final ObjectMapper objectMapper;

    public ConfigJsonService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);
    }

    /** Reads JSON from a file and returns it as a JsonNode. */
    public JsonNode readJsonFile(Path path) throws IOException {
        String content = Files.readString(path);
        return objectMapper.readTree(content);
    }

    /** Writes a JsonNode to a file with proper formatting. */
    public void writeJsonFile(Path path, JsonNode node) throws IOException {
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        Files.writeString(path, json);
    }

    /**
     * Extracts a value from JSON by key path (supports nested keys with dot notation). Example:
     * "app.version" would get the version field from the app object.
     */
    public String extractValue(JsonNode root, String keyPath) {
        String[] keys = keyPath.split("\\.");
        JsonNode current = root;

        for (String key : keys) {
            if (current == null || !current.has(key)) {
                return null;
            }
            current = current.get(key);
        }

        return current != null ? current.asText() : null;
    }

    /** Updates a value in JSON by key path. Creates missing intermediate objects as needed. */
    public JsonNode updateValue(JsonNode root, String keyPath, String value) {
        ObjectNode rootNode = root.isObject() ? (ObjectNode) root : objectMapper.createObjectNode();
        String[] keys = keyPath.split("\\.");

        ObjectNode current = rootNode;

        // Navigate to the parent of the target key
        for (int i = 0; i < keys.length - 1; i++) {
            String key = keys[i];
            if (!current.has(key) || !current.get(key).isObject()) {
                current.set(key, objectMapper.createObjectNode());
            }
            current = (ObjectNode) current.get(key);
        }

        // Set the value
        String finalKey = keys[keys.length - 1];
        if (value == null || value.isEmpty()) {
            current.remove(finalKey);
        } else {
            // Try to parse as number or boolean, otherwise store as string
            try {
                if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                    current.put(finalKey, Boolean.parseBoolean(value));
                } else if (value.matches("-?\\d+")) {
                    current.put(finalKey, Long.parseLong(value));
                } else if (value.matches("-?\\d+\\.\\d+")) {
                    current.put(finalKey, Double.parseDouble(value));
                } else {
                    current.put(finalKey, value);
                }
            } catch (NumberFormatException e) {
                current.put(finalKey, value);
            }
        }

        return rootNode;
    }

    /**
     * Converts a JsonNode to a flat map of key-value pairs. Nested objects are flattened with dot
     * notation.
     */
    public Map<String, String> flattenToMap(JsonNode node) {
        Map<String, String> result = new HashMap<>();
        flattenToMap(node, "", result);
        return result;
    }

    private void flattenToMap(JsonNode node, String prefix, Map<String, String> result) {
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String key = prefix.isEmpty() ? field.getKey() : prefix + "." + field.getKey();
                flattenToMap(field.getValue(), key, result);
            }
        } else if (node.isArray()) {
            // For arrays, store as comma-separated values
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < node.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(node.get(i).asText());
            }
            result.put(prefix, sb.toString());
        } else {
            result.put(prefix, node.asText());
        }
    }

    /** Merges two JSON objects, with values from the second overriding the first. */
    public JsonNode merge(JsonNode base, JsonNode updates) {
        if (!base.isObject() || !updates.isObject()) {
            return updates;
        }

        ObjectNode result = base.deepCopy();
        Iterator<Map.Entry<String, JsonNode>> fields = updates.fields();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String key = field.getKey();
            JsonNode updateValue = field.getValue();

            if (result.has(key) && result.get(key).isObject() && updateValue.isObject()) {
                // Recursively merge objects
                result.set(key, merge(result.get(key), updateValue));
            } else {
                // Replace the value
                result.set(key, updateValue.deepCopy());
            }
        }

        return result;
    }

    /** Validates JSON against expected structure. Returns true if valid, false otherwise. */
    public boolean isValidJson(String json) {
        try {
            objectMapper.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            log.debug("Invalid JSON: {}", e.getMessage());
            return false;
        }
    }

    /** Pretty prints JSON for display. */
    public String prettyPrint(JsonNode node) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        } catch (JsonProcessingException e) {
            log.error("Failed to pretty print JSON", e);
            return node.toString();
        }
    }
}
