package io.github.jspinak.brobot.runner.ui.config.services;

import io.github.jspinak.brobot.runner.ui.config.models.ValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Service for validating configuration data and user inputs.
 * Provides validation rules for different configuration fields.
 */
@Slf4j
@Service
public class ConfigValidationService {
    
    // Validation patterns
    private static final Pattern VERSION_PATTERN = Pattern.compile("^\\d+\\.\\d+(\\.\\d+)?(-\\w+)?$");
    private static final Pattern PROJECT_NAME_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_-]*$");
    private static final Pattern KEY_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_.]*$");
    private static final Pattern PACKAGE_NAME_PATTERN = Pattern.compile("^[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)*$");
    
    // Common reserved words that shouldn't be used as keys
    private static final List<String> RESERVED_WORDS = List.of(
        "class", "interface", "enum", "package", "import", "extends", "implements",
        "public", "private", "protected", "static", "final", "abstract", "synchronized",
        "volatile", "transient", "native", "strictfp", "void", "boolean", "byte",
        "char", "short", "int", "long", "float", "double", "if", "else", "switch",
        "case", "default", "for", "while", "do", "break", "continue", "return",
        "throw", "throws", "try", "catch", "finally", "new", "this", "super",
        "instanceof", "true", "false", "null"
    );
    
    /**
     * Validates a version string.
     * Expected format: major.minor[.patch][-qualifier]
     * Examples: 1.0, 1.0.0, 1.0.0-SNAPSHOT
     */
    public ValidationResult validateVersion(String version) {
        if (version == null || version.trim().isEmpty()) {
            return ValidationResult.error("Version cannot be empty");
        }
        
        version = version.trim();
        if (!VERSION_PATTERN.matcher(version).matches()) {
            return ValidationResult.error(
                "Invalid version format. Expected: major.minor[.patch][-qualifier] (e.g., 1.0, 1.0.0, 1.0.0-SNAPSHOT)"
            );
        }
        
        return ValidationResult.success();
    }
    
    /**
     * Validates a project name.
     * Must start with a letter and contain only letters, numbers, hyphens, and underscores.
     */
    public ValidationResult validateProjectName(String projectName) {
        if (projectName == null || projectName.trim().isEmpty()) {
            return ValidationResult.error("Project name cannot be empty");
        }
        
        projectName = projectName.trim();
        if (!PROJECT_NAME_PATTERN.matcher(projectName).matches()) {
            return ValidationResult.error(
                "Invalid project name. Must start with a letter and contain only letters, numbers, hyphens, and underscores"
            );
        }
        
        if (projectName.length() > 50) {
            return ValidationResult.warning("Project name is very long. Consider using a shorter name");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * Validates a configuration key.
     * Must be a valid identifier that can be used in code.
     */
    public ValidationResult validateKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            return ValidationResult.error("Key cannot be empty");
        }
        
        key = key.trim();
        if (!KEY_PATTERN.matcher(key).matches()) {
            return ValidationResult.error(
                "Invalid key format. Must start with a letter and contain only letters, numbers, underscores, and dots"
            );
        }
        
        if (RESERVED_WORDS.contains(key.toLowerCase())) {
            return ValidationResult.error("'" + key + "' is a reserved word and cannot be used as a key");
        }
        
        if (key.length() > 100) {
            return ValidationResult.error("Key is too long. Maximum length is 100 characters");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * Normalizes a key by converting to camelCase and removing invalid characters.
     */
    public String normalizeKey(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }
        
        // Remove leading/trailing whitespace
        input = input.trim();
        
        // Replace spaces and special characters with underscores
        input = input.replaceAll("[^a-zA-Z0-9_.]", "_");
        
        // Remove consecutive underscores
        input = input.replaceAll("_+", "_");
        
        // Remove leading/trailing underscores
        input = input.replaceAll("^_+|_+$", "");
        
        // Convert to camelCase if contains underscores
        if (input.contains("_")) {
            String[] parts = input.split("_");
            StringBuilder result = new StringBuilder(parts[0].toLowerCase());
            for (int i = 1; i < parts.length; i++) {
                if (!parts[i].isEmpty()) {
                    result.append(Character.toUpperCase(parts[i].charAt(0)));
                    if (parts[i].length() > 1) {
                        result.append(parts[i].substring(1).toLowerCase());
                    }
                }
            }
            input = result.toString();
        }
        
        // Ensure first character is lowercase
        if (!input.isEmpty() && Character.isUpperCase(input.charAt(0))) {
            input = Character.toLowerCase(input.charAt(0)) + input.substring(1);
        }
        
        return input;
    }
    
    /**
     * Validates a file path.
     */
    public ValidationResult validateFilePath(String pathStr) {
        if (pathStr == null || pathStr.trim().isEmpty()) {
            return ValidationResult.error("File path cannot be empty");
        }
        
        try {
            Path path = Paths.get(pathStr.trim());
            
            if (!Files.exists(path)) {
                return ValidationResult.warning("File does not exist: " + path);
            }
            
            if (!Files.isReadable(path)) {
                return ValidationResult.error("File is not readable: " + path);
            }
            
            if (!pathStr.endsWith(".json")) {
                return ValidationResult.warning("File does not have .json extension");
            }
            
            return ValidationResult.success();
        } catch (Exception e) {
            return ValidationResult.error("Invalid file path: " + e.getMessage());
        }
    }
    
    /**
     * Validates a package name.
     */
    public ValidationResult validatePackageName(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) {
            return ValidationResult.error("Package name cannot be empty");
        }
        
        packageName = packageName.trim();
        if (!PACKAGE_NAME_PATTERN.matcher(packageName).matches()) {
            return ValidationResult.error(
                "Invalid package name. Must be lowercase letters separated by dots (e.g., com.example.app)"
            );
        }
        
        return ValidationResult.success();
    }
    
    /**
     * Validates a URL.
     */
    public ValidationResult validateUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return ValidationResult.error("URL cannot be empty");
        }
        
        url = url.trim();
        try {
            new java.net.URL(url);
            return ValidationResult.success();
        } catch (Exception e) {
            return ValidationResult.error("Invalid URL format: " + e.getMessage());
        }
    }
    
    /**
     * Validates an integer value within a range.
     */
    public ValidationResult validateIntegerRange(String value, int min, int max) {
        if (value == null || value.trim().isEmpty()) {
            return ValidationResult.error("Value cannot be empty");
        }
        
        try {
            int intValue = Integer.parseInt(value.trim());
            if (intValue < min || intValue > max) {
                return ValidationResult.error(
                    String.format("Value must be between %d and %d", min, max)
                );
            }
            return ValidationResult.success();
        } catch (NumberFormatException e) {
            return ValidationResult.error("Value must be a valid integer");
        }
    }
    
    /**
     * Validates a boolean value.
     */
    public ValidationResult validateBoolean(String value) {
        if (value == null || value.trim().isEmpty()) {
            return ValidationResult.error("Value cannot be empty");
        }
        
        String trimmed = value.trim().toLowerCase();
        if (!trimmed.equals("true") && !trimmed.equals("false")) {
            return ValidationResult.error("Value must be either 'true' or 'false'");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * Validates a list of configuration entries.
     * Returns all validation errors found.
     */
    public List<ValidationResult> validateConfiguration(java.util.Map<String, String> config) {
        List<ValidationResult> results = new ArrayList<>();
        
        // Validate required fields
        if (!config.containsKey("name") || config.get("name").trim().isEmpty()) {
            results.add(ValidationResult.error("Configuration name is required"));
        }
        
        if (!config.containsKey("version") || config.get("version").trim().isEmpty()) {
            results.add(ValidationResult.error("Version is required"));
        } else {
            ValidationResult versionResult = validateVersion(config.get("version"));
            if (!versionResult.isValid()) {
                results.add(versionResult);
            }
        }
        
        // Validate optional fields if present
        if (config.containsKey("project")) {
            ValidationResult projectResult = validateProjectName(config.get("project"));
            if (!projectResult.isValid()) {
                results.add(projectResult);
            }
        }
        
        if (config.containsKey("package")) {
            ValidationResult packageResult = validatePackageName(config.get("package"));
            if (!packageResult.isValid()) {
                results.add(packageResult);
            }
        }
        
        return results;
    }
    
    /**
     * Checks if a configuration has any validation errors.
     */
    public boolean hasValidationErrors(java.util.Map<String, String> config) {
        return !validateConfiguration(config).stream()
            .filter(result -> result.getSeverity() == ValidationResult.Severity.ERROR)
            .toList()
            .isEmpty();
    }
}