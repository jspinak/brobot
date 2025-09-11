package io.github.jspinak.brobot.runner.ui.config.model;

import java.util.HashMap;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

/** Represents metadata for a configuration. Contains both standard fields and custom metadata. */
@Data
@Builder
public class ConfigMetadata {

    // Standard metadata fields
    private String projectName;
    private String version;
    private String author;
    private String description;

    // File paths
    private String projectConfigPath;
    private String dslConfigPath;
    private String imagePath;

    // Additional custom metadata
    @Builder.Default private Map<String, String> customFields = new HashMap<>();

    /**
     * Adds a custom metadata field.
     *
     * @param key Field key
     * @param value Field value
     */
    public void addCustomField(String key, String value) {
        if (customFields == null) {
            customFields = new HashMap<>();
        }
        customFields.put(key, value);
    }

    /**
     * Gets a custom metadata field value.
     *
     * @param key Field key
     * @return Field value or null if not found
     */
    public String getCustomField(String key) {
        if (customFields == null) {
            return null;
        }
        return customFields.get(key);
    }

    /**
     * Removes a custom metadata field.
     *
     * @param key Field key
     */
    public void removeCustomField(String key) {
        if (customFields != null) {
            customFields.remove(key);
        }
    }

    /**
     * Checks if a custom field exists.
     *
     * @param key Field key
     * @return true if field exists
     */
    public boolean hasCustomField(String key) {
        return customFields != null && customFields.containsKey(key);
    }

    /**
     * Creates a copy of this metadata.
     *
     * @return Copy of metadata
     */
    public ConfigMetadata copy() {
        return ConfigMetadata.builder()
                .projectName(projectName)
                .version(version)
                .author(author)
                .description(description)
                .projectConfigPath(projectConfigPath)
                .dslConfigPath(dslConfigPath)
                .imagePath(imagePath)
                .customFields(new HashMap<>(customFields != null ? customFields : new HashMap<>()))
                .build();
    }
}
