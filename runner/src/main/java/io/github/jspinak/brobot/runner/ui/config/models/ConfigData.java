package io.github.jspinak.brobot.runner.ui.config.models;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Model class representing configuration data loaded from a file.
 * Contains both the raw JSON data and a flattened key-value representation.
 */
@Data
public class ConfigData {
    
    /**
     * The path to the configuration file.
     */
    private Path configPath;
    
    /**
     * The raw JSON node representation of the configuration.
     */
    private JsonNode jsonNode;
    
    /**
     * Flattened key-value pairs from the JSON.
     * Nested objects use dot notation (e.g., "app.version").
     */
    private Map<String, String> rawData = new HashMap<>();
    
    /**
     * Timestamp of when the file was last modified.
     */
    private Instant lastModified;
    
    /**
     * Whether this configuration has unsaved changes.
     */
    private boolean hasUnsavedChanges = false;
    
    /**
     * Gets a value by key from the flattened data.
     */
    public String getValue(String key) {
        return rawData.get(key);
    }
    
    /**
     * Sets a value in the flattened data and marks as modified.
     */
    public void setValue(String key, String value) {
        rawData.put(key, value);
        hasUnsavedChanges = true;
    }
    
    /**
     * Removes a key from the flattened data and marks as modified.
     */
    public void removeValue(String key) {
        rawData.remove(key);
        hasUnsavedChanges = true;
    }
    
    /**
     * Gets all keys that start with a given prefix.
     * Useful for getting all values in a section.
     */
    public Map<String, String> getValuesWithPrefix(String prefix) {
        Map<String, String> result = new HashMap<>();
        rawData.forEach((key, value) -> {
            if (key.startsWith(prefix)) {
                result.put(key, value);
            }
        });
        return result;
    }
    
    /**
     * Resets the unsaved changes flag.
     * Should be called after successful save.
     */
    public void markAsSaved() {
        hasUnsavedChanges = false;
    }
    
    /**
     * Creates a deep copy of this configuration data.
     */
    public ConfigData copy() {
        ConfigData copy = new ConfigData();
        copy.setConfigPath(this.configPath);
        copy.setJsonNode(this.jsonNode != null ? this.jsonNode.deepCopy() : null);
        copy.setRawData(new HashMap<>(this.rawData));
        copy.setLastModified(this.lastModified);
        copy.setHasUnsavedChanges(this.hasUnsavedChanges);
        return copy;
    }
}