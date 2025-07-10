package io.github.jspinak.brobot.runner.ui.config.services;

import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Registry service for metadata field types.
 * Manages field definitions and provides field creation functionality.
 */
@Slf4j
@Service
public class MetadataFieldRegistry {
    
    private final Map<String, MetadataFieldDefinition> fieldTypes = new ConcurrentHashMap<>();
    private final Map<String, Control> fieldInstances = new ConcurrentHashMap<>();
    
    // Common validators
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"
    );
    private static final Pattern URL_PATTERN = Pattern.compile(
        "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$"
    );
    private static final Pattern VERSION_PATTERN = Pattern.compile(
        "^\\d+\\.\\d+\\.\\d+(-.*)?$"
    );
    
    @PostConstruct
    public void initialize() {
        registerDefaultFieldTypes();
    }
    
    /**
     * Registers default field types.
     */
    private void registerDefaultFieldTypes() {
        // Text field
        registerFieldType(MetadataFieldDefinition.builder()
            .type("text")
            .displayName("Text")
            .description("Single line text field")
            .controlFactory(config -> {
                TextField field = new TextField();
                field.setPromptText(config.getPrompt());
                if (config.getInitialValue() != null) {
                    field.setText(config.getInitialValue());
                }
                return field;
            })
            .validator(value -> true)
            .valueExtractor(control -> ((TextField) control).getText())
            .valueSetter((control, value) -> ((TextField) control).setText(value))
            .defaultValue("")
            .multiValue(false)
            .maxLength(255)
            .build());
        
        // Text area
        registerFieldType(MetadataFieldDefinition.builder()
            .type("textarea")
            .displayName("Text Area")
            .description("Multi-line text field")
            .controlFactory(config -> {
                TextArea area = new TextArea();
                area.setPromptText(config.getPrompt());
                area.setPrefRowCount(3);
                area.setWrapText(true);
                if (config.getInitialValue() != null) {
                    area.setText(config.getInitialValue());
                }
                return area;
            })
            .validator(value -> true)
            .valueExtractor(control -> ((TextArea) control).getText())
            .valueSetter((control, value) -> ((TextArea) control).setText(value))
            .defaultValue("")
            .multiValue(false)
            .maxLength(5000)
            .build());
        
        // Number field
        registerFieldType(MetadataFieldDefinition.builder()
            .type("number")
            .displayName("Number")
            .description("Numeric input field")
            .controlFactory(config -> {
                TextField field = new TextField();
                field.setPromptText(config.getPrompt());
                
                // Add number validation
                field.textProperty().addListener((obs, old, val) -> {
                    if (val != null && !val.matches("\\d*\\.?\\d*")) {
                        field.setText(old);
                    }
                });
                
                if (config.getInitialValue() != null) {
                    field.setText(config.getInitialValue());
                }
                return field;
            })
            .validator(value -> {
                if (value == null) return true;
                try {
                    Double.parseDouble(value.toString());
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            })
            .valueExtractor(control -> ((TextField) control).getText())
            .valueSetter((control, value) -> ((TextField) control).setText(value))
            .defaultValue("0")
            .multiValue(false)
            .maxLength(20)
            .build());
        
        // Email field
        registerFieldType(MetadataFieldDefinition.builder()
            .type("email")
            .displayName("Email")
            .description("Email address field")
            .controlFactory(config -> {
                TextField field = new TextField();
                field.setPromptText(config.getPrompt() != null ? config.getPrompt() : "user@example.com");
                if (config.getInitialValue() != null) {
                    field.setText(config.getInitialValue());
                }
                return field;
            })
            .validator(value -> {
                if (value == null || value.toString().isEmpty()) return true;
                return EMAIL_PATTERN.matcher(value.toString()).matches();
            })
            .valueExtractor(control -> ((TextField) control).getText())
            .valueSetter((control, value) -> ((TextField) control).setText(value))
            .defaultValue("")
            .multiValue(false)
            .maxLength(255)
            .build());
        
        // URL field
        registerFieldType(MetadataFieldDefinition.builder()
            .type("url")
            .displayName("URL")
            .description("Web URL field")
            .controlFactory(config -> {
                TextField field = new TextField();
                field.setPromptText(config.getPrompt() != null ? config.getPrompt() : "https://example.com");
                if (config.getInitialValue() != null) {
                    field.setText(config.getInitialValue());
                }
                return field;
            })
            .validator(value -> {
                if (value == null || value.toString().isEmpty()) return true;
                return URL_PATTERN.matcher(value.toString()).matches();
            })
            .valueExtractor(control -> ((TextField) control).getText())
            .valueSetter((control, value) -> ((TextField) control).setText(value))
            .defaultValue("")
            .multiValue(false)
            .maxLength(500)
            .build());
        
        // Version field
        registerFieldType(MetadataFieldDefinition.builder()
            .type("version")
            .displayName("Version")
            .description("Semantic version field")
            .controlFactory(config -> {
                TextField field = new TextField();
                field.setPromptText(config.getPrompt() != null ? config.getPrompt() : "1.0.0");
                if (config.getInitialValue() != null) {
                    field.setText(config.getInitialValue());
                }
                return field;
            })
            .validator(value -> {
                if (value == null || value.toString().isEmpty()) return true;
                return VERSION_PATTERN.matcher(value.toString()).matches();
            })
            .valueExtractor(control -> ((TextField) control).getText())
            .valueSetter((control, value) -> ((TextField) control).setText(value))
            .defaultValue("1.0.0")
            .multiValue(false)
            .maxLength(50)
            .build());
        
        // Date field
        registerFieldType(MetadataFieldDefinition.builder()
            .type("date")
            .displayName("Date")
            .description("Date picker field")
            .controlFactory(config -> {
                DatePicker picker = new DatePicker();
                picker.setPromptText(config.getPrompt() != null ? config.getPrompt() : "Select date");
                
                if (config.getInitialValue() != null) {
                    try {
                        LocalDate date = LocalDate.parse(config.getInitialValue());
                        picker.setValue(date);
                    } catch (Exception e) {
                        log.warn("Invalid date value: {}", config.getInitialValue());
                    }
                }
                return picker;
            })
            .validator(value -> true)
            .valueExtractor(control -> {
                LocalDate date = ((DatePicker) control).getValue();
                return date != null ? date.toString() : "";
            })
            .valueSetter((control, value) -> {
                if (value != null && !value.isEmpty()) {
                    try {
                        LocalDate date = LocalDate.parse(value);
                        ((DatePicker) control).setValue(date);
                    } catch (Exception e) {
                        log.warn("Invalid date value: {}", value);
                    }
                }
            })
            .defaultValue("")
            .multiValue(false)
            .maxLength(10)
            .build());
        
        // Choice field
        registerFieldType(MetadataFieldDefinition.builder()
            .type("choice")
            .displayName("Choice")
            .description("Dropdown selection field")
            .controlFactory(config -> {
                ComboBox<String> combo = new ComboBox<>();
                combo.setPromptText(config.getPrompt());
                
                // Add choices from metadata
                if (config.getMetadata() instanceof List<?>) {
                    List<?> choices = (List<?>) config.getMetadata();
                    for (Object choice : choices) {
                        combo.getItems().add(choice.toString());
                    }
                }
                
                if (config.getInitialValue() != null) {
                    combo.setValue(config.getInitialValue());
                }
                return combo;
            })
            .validator(value -> true)
            .valueExtractor(control -> {
                Object value = ((ComboBox<?>) control).getValue();
                return value != null ? value.toString() : "";
            })
            .valueSetter((control, value) -> ((ComboBox<String>) control).setValue(value))
            .defaultValue("")
            .multiValue(false)
            .maxLength(255)
            .build());
        
        // Boolean field
        registerFieldType(MetadataFieldDefinition.builder()
            .type("boolean")
            .displayName("Boolean")
            .description("Checkbox field")
            .controlFactory(config -> {
                CheckBox checkBox = new CheckBox(config.getLabel());
                
                if (config.getInitialValue() != null) {
                    checkBox.setSelected(Boolean.parseBoolean(config.getInitialValue()));
                }
                return checkBox;
            })
            .validator(value -> true)
            .valueExtractor(control -> String.valueOf(((CheckBox) control).isSelected()))
            .valueSetter((control, value) -> 
                ((CheckBox) control).setSelected(Boolean.parseBoolean(value)))
            .defaultValue("false")
            .multiValue(false)
            .maxLength(5)
            .build());
    }
    
    /**
     * Registers a new field type.
     * 
     * @param definition Field type definition
     */
    public void registerFieldType(MetadataFieldDefinition definition) {
        if (definition == null || definition.getType() == null) {
            throw new IllegalArgumentException("Field definition and type cannot be null");
        }
        
        log.debug("Registering field type: {}", definition.getType());
        fieldTypes.put(definition.getType(), definition);
    }
    
    /**
     * Creates a field control based on type and configuration.
     * 
     * @param type Field type
     * @param config Field configuration
     * @return Created control
     */
    public Control createField(String type, MetadataFieldDefinition.FieldConfig config) {
        MetadataFieldDefinition definition = fieldTypes.get(type);
        if (definition == null) {
            log.warn("Unknown field type: {}, using default text field", type);
            definition = fieldTypes.get("text");
        }
        
        Control control = definition.getControlFactory().apply(config);
        
        // Store instance if key provided
        if (config.getKey() != null) {
            fieldInstances.put(config.getKey(), control);
        }
        
        return control;
    }
    
    /**
     * Validates a field value.
     * 
     * @param type Field type
     * @param value Value to validate
     * @return true if valid
     */
    public boolean validateField(String type, Object value) {
        MetadataFieldDefinition definition = fieldTypes.get(type);
        if (definition == null) {
            return true; // Unknown type, allow
        }
        
        return definition.getValidator().test(value);
    }
    
    /**
     * Gets the value from a field control.
     * 
     * @param type Field type
     * @param control Field control
     * @return Extracted value
     */
    public String extractValue(String type, Control control) {
        MetadataFieldDefinition definition = fieldTypes.get(type);
        if (definition == null) {
            return "";
        }
        
        return definition.getValueExtractor().apply(control);
    }
    
    /**
     * Sets the value on a field control.
     * 
     * @param type Field type
     * @param control Field control
     * @param value Value to set
     */
    public void setValue(String type, Control control, String value) {
        MetadataFieldDefinition definition = fieldTypes.get(type);
        if (definition != null) {
            definition.getValueSetter().setValue(control, value);
        }
    }
    
    /**
     * Gets all available field types.
     * 
     * @return List of field definitions
     */
    public List<MetadataFieldDefinition> getAvailableFieldTypes() {
        return new ArrayList<>(fieldTypes.values());
    }
    
    /**
     * Gets a specific field definition.
     * 
     * @param type Field type
     * @return Field definition or null
     */
    public MetadataFieldDefinition getFieldDefinition(String type) {
        return fieldTypes.get(type);
    }
    
    /**
     * Checks if a field type is registered.
     * 
     * @param type Field type
     * @return true if registered
     */
    public boolean hasFieldType(String type) {
        return fieldTypes.containsKey(type);
    }
    
    /**
     * Gets a field instance by key.
     * 
     * @param key Field key
     * @return Control instance or null
     */
    public Control getFieldInstance(String key) {
        return fieldInstances.get(key);
    }
    
    /**
     * Removes a field instance.
     * 
     * @param key Field key
     */
    public void removeFieldInstance(String key) {
        fieldInstances.remove(key);
    }
    
    /**
     * Clears all field instances.
     */
    public void clearFieldInstances() {
        fieldInstances.clear();
    }
    
    /**
     * Normalizes a field key.
     * 
     * @param key Original key
     * @return Normalized key
     */
    public String normalizeKey(String key) {
        if (key == null) return "";
        
        // Convert to lowercase, replace spaces with underscores
        return key.toLowerCase()
            .replaceAll("\\s+", "_")
            .replaceAll("[^a-z0-9_]", "");
    }
}