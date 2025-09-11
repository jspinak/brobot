package io.github.jspinak.brobot.runner.ui.config.services;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;

/** Data class representing configuration metadata. */
@Data
public class ConfigMetadata {
    // Core metadata
    private String projectName;
    private String version;
    private String author;
    private String description;

    // Additional custom fields
    private Map<String, String> additionalFields = new HashMap<>();

    // File paths (read-only)
    private Path projectConfigPath;
    private Path dslConfigPath;
    private Path imagePath;

    /** Creates a copy of this metadata. */
    public ConfigMetadata copy() {
        ConfigMetadata copy = new ConfigMetadata();
        copy.setProjectName(this.projectName);
        copy.setVersion(this.version);
        copy.setAuthor(this.author);
        copy.setDescription(this.description);
        copy.setAdditionalFields(new HashMap<>(this.additionalFields));
        copy.setProjectConfigPath(this.projectConfigPath);
        copy.setDslConfigPath(this.dslConfigPath);
        copy.setImagePath(this.imagePath);
        return copy;
    }

    /** Checks if this metadata differs from another. */
    public boolean differs(ConfigMetadata other) {
        if (other == null) return true;

        return !equals(other);
    }
}
