package io.github.jspinak.brobot.runner.ui.config.services;

import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.project.AutomationProject;
import io.github.jspinak.brobot.runner.project.AutomationProjectManager;
import io.github.jspinak.brobot.runner.ui.config.ConfigEntry;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for handling configuration metadata operations.
 * Responsible for loading, saving, and managing metadata.
 */
@Slf4j
public class ConfigMetadataService {
    
    private final EventBus eventBus;
    private final AutomationProjectManager projectManager;
    
    public ConfigMetadataService(EventBus eventBus, AutomationProjectManager projectManager) {
        this.eventBus = eventBus;
        this.projectManager = projectManager;
    }
    
    /**
     * Loads metadata for a configuration.
     */
    public ConfigMetadata loadMetadata(ConfigEntry config) {
        if (config == null) {
            return null;
        }
        
        AutomationProject project = projectManager.getActiveProject();
        if (project == null) {
            return createDefaultMetadata(config);
        }
        
        ConfigMetadata metadata = new ConfigMetadata();
        metadata.setProjectName(project.getName());
        metadata.setVersion(project.getVersion());
        metadata.setAuthor(project.getAuthor());
        metadata.setDescription(project.getDescription());
        
        // Load additional metadata from custom properties
        if (project.getCustomProperties() != null) {
            Map<String, String> additionalFields = new HashMap<>();
            for (Map.Entry<String, Object> entry : project.getCustomProperties().entrySet()) {
                additionalFields.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
            metadata.setAdditionalFields(additionalFields);
        }
        
        // Set file paths
        metadata.setProjectConfigPath(config.getProjectConfigPath());
        metadata.setDslConfigPath(config.getDslConfigPath());
        metadata.setImagePath(config.getImagePath());
        
        return metadata;
    }
    
    /**
     * Saves metadata for a configuration.
     */
    public boolean saveMetadata(ConfigEntry config, ConfigMetadata metadata) {
        if (config == null || metadata == null) {
            return false;
        }
        
        try {
            AutomationProject project = projectManager.getActiveProject();
            if (project == null) {
                project = new AutomationProject();
                project.setName(metadata.getProjectName());
            }
            
            // Update project metadata
            project.setName(metadata.getProjectName());
            project.setVersion(metadata.getVersion());
            project.setAuthor(metadata.getAuthor());
            project.setDescription(metadata.getDescription());
            
            // Update additional metadata
            if (metadata.getAdditionalFields() != null) {
                if (project.getCustomProperties() == null) {
                    project.setCustomProperties(new HashMap<>());
                }
                project.getCustomProperties().clear();
                for (Map.Entry<String, String> entry : metadata.getAdditionalFields().entrySet()) {
                    project.getCustomProperties().put(entry.getKey(), entry.getValue());
                }
            }
            
            // Save project
            projectManager.setActiveProject(project);
            
            // Log success
            eventBus.publish(LogEvent.info(
                this,
                String.format("Saved metadata for configuration: %s", config.getName()),
                "ConfigMetadataService"
            ));
            
            return true;
            
        } catch (Exception e) {
            log.error("Failed to save metadata for configuration: " + config.getName(), e);
            
            eventBus.publish(LogEvent.error(
                this,
                String.format("Failed to save metadata: %s", e.getMessage()),
                "ConfigMetadataService",
                e
            ));
            
            return false;
        }
    }
    
    /**
     * Validates metadata before saving.
     */
    public ValidationResult validateMetadata(ConfigMetadata metadata) {
        ValidationResult result = new ValidationResult();
        
        if (metadata == null) {
            result.addError("Metadata cannot be null");
            return result;
        }
        
        // Validate project name
        if (metadata.getProjectName() == null || metadata.getProjectName().trim().isEmpty()) {
            result.addError("Project name is required");
        }
        
        // Validate version format if provided
        if (metadata.getVersion() != null && !metadata.getVersion().trim().isEmpty()) {
            if (!isValidVersion(metadata.getVersion())) {
                result.addError("Invalid version format. Use semantic versioning (e.g., 1.0.0)");
            }
        }
        
        // Validate additional fields
        if (metadata.getAdditionalFields() != null) {
            for (Map.Entry<String, String> entry : metadata.getAdditionalFields().entrySet()) {
                if (entry.getKey() == null || entry.getKey().trim().isEmpty()) {
                    result.addError("Additional field key cannot be empty");
                }
                if (entry.getValue() != null && entry.getValue().length() > 1000) {
                    result.addError("Additional field value too long: " + entry.getKey());
                }
            }
        }
        
        return result;
    }
    
    private boolean isValidVersion(String version) {
        // Simple semantic versioning check
        return version.matches("^\\d+\\.\\d+(\\.\\d+)?(-.*)?$");
    }
    
    private ConfigMetadata createDefaultMetadata(ConfigEntry config) {
        ConfigMetadata metadata = new ConfigMetadata();
        metadata.setProjectName(config.getName());
        metadata.setVersion("1.0.0");
        metadata.setAuthor(System.getProperty("user.name"));
        metadata.setDescription("Automation configuration for " + config.getName());
        
        metadata.setProjectConfigPath(config.getProjectConfigPath());
        metadata.setDslConfigPath(config.getDslConfigPath());
        metadata.setImagePath(config.getImagePath());
        
        return metadata;
    }
    
    /**
     * Result of metadata validation.
     */
    public static class ValidationResult {
        private final Map<String, String> errors = new HashMap<>();
        
        public void addError(String error) {
            errors.put("error_" + errors.size(), error);
        }
        
        public void addError(String field, String error) {
            errors.put(field, error);
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        public Map<String, String> getErrors() {
            return new HashMap<>(errors);
        }
        
        public String getErrorMessage() {
            return String.join("\n", errors.values());
        }
    }
}