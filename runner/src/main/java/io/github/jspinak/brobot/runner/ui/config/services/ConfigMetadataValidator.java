package io.github.jspinak.brobot.runner.ui.config.services;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Validator for configuration metadata.
 * Provides validation rules and error messages.
 */
public class ConfigMetadataValidator {
    
    private static final Pattern VERSION_PATTERN = Pattern.compile("^\\d+\\.\\d+(\\.\\d+)?(-.*)?$");
    private static final Pattern KEY_PATTERN = Pattern.compile("^[a-z0-9_]+$");
    private static final int MAX_FIELD_LENGTH = 1000;
    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_VERSION_LENGTH = 20;
    
    /**
     * Validates metadata before saving.
     */
    public ValidationResult validate(ConfigMetadata metadata) {
        ValidationResult result = new ValidationResult();
        
        if (metadata == null) {
            result.addError("general", "Metadata cannot be null");
            return result;
        }
        
        // Validate project name
        validateProjectName(metadata.getProjectName(), result);
        
        // Validate version
        validateVersion(metadata.getVersion(), result);
        
        // Validate author
        validateAuthor(metadata.getAuthor(), result);
        
        // Validate description
        validateDescription(metadata.getDescription(), result);
        
        // Validate additional fields
        validateAdditionalFields(metadata.getAdditionalFields(), result);
        
        return result;
    }
    
    private void validateProjectName(String projectName, ValidationResult result) {
        if (projectName == null || projectName.trim().isEmpty()) {
            result.addError("projectName", "Project name is required");
        } else if (projectName.length() > MAX_NAME_LENGTH) {
            result.addError("projectName", 
                String.format("Project name must be less than %d characters", MAX_NAME_LENGTH));
        } else if (!isValidName(projectName)) {
            result.addError("projectName", 
                "Project name contains invalid characters");
        }
    }
    
    private void validateVersion(String version, ValidationResult result) {
        if (version != null && !version.trim().isEmpty()) {
            if (version.length() > MAX_VERSION_LENGTH) {
                result.addError("version", 
                    String.format("Version must be less than %d characters", MAX_VERSION_LENGTH));
            } else if (!VERSION_PATTERN.matcher(version).matches()) {
                result.addError("version", 
                    "Invalid version format. Use semantic versioning (e.g., 1.0.0)");
            }
        }
    }
    
    private void validateAuthor(String author, ValidationResult result) {
        if (author != null && author.length() > MAX_NAME_LENGTH) {
            result.addError("author", 
                String.format("Author name must be less than %d characters", MAX_NAME_LENGTH));
        }
    }
    
    private void validateDescription(String description, ValidationResult result) {
        if (description != null && description.length() > MAX_FIELD_LENGTH) {
            result.addError("description", 
                String.format("Description must be less than %d characters", MAX_FIELD_LENGTH));
        }
    }
    
    private void validateAdditionalFields(Map<String, String> additionalFields, ValidationResult result) {
        if (additionalFields == null) {
            return;
        }
        
        for (Map.Entry<String, String> entry : additionalFields.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            
            // Validate key
            if (key == null || key.trim().isEmpty()) {
                result.addError("additionalField", "Field key cannot be empty");
            } else if (!KEY_PATTERN.matcher(key).matches()) {
                result.addError("additionalField_" + key, 
                    "Field key '" + key + "' contains invalid characters. Use lowercase letters, numbers, and underscores only");
            }
            
            // Validate value
            if (value != null && value.length() > MAX_FIELD_LENGTH) {
                result.addError("additionalField_" + key, 
                    String.format("Field '%s' value must be less than %d characters", key, MAX_FIELD_LENGTH));
            }
        }
    }
    
    private boolean isValidName(String name) {
        // Allow alphanumeric, spaces, hyphens, underscores
        return name.matches("^[a-zA-Z0-9\\s\\-_]+$");
    }
    
    /**
     * Result of metadata validation.
     */
    public static class ValidationResult {
        private final Map<String, String> errors = new HashMap<>();
        
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
        
        public String getFieldError(String field) {
            return errors.get(field);
        }
        
        public boolean hasFieldError(String field) {
            return errors.containsKey(field);
        }
    }
}