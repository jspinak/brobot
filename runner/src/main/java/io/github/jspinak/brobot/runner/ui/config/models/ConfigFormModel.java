package io.github.jspinak.brobot.runner.ui.config.models;

import java.util.HashMap;
import java.util.Map;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import lombok.Data;

/**
 * Model class for the configuration form. Contains all the data and UI references needed by the
 * form builder.
 */
@Data
public class ConfigFormModel {

    // Basic information
    private String name;
    private String project;
    private String version;
    private String description;
    private String author;

    // Paths (read-only display)
    private String projectConfigPath;
    private String dslConfigPath;
    private String imagePath;

    // Additional custom fields
    private Map<String, String> additionalFields = new HashMap<>();

    // UI References
    private GridPane additionalFieldsGrid;
    private Label statusLabel = new Label("Ready");
    private Label modifiedLabel = new Label("");
    private Label validationLabel = new Label("");

    // Handlers
    private Runnable saveHandler = () -> {};
    private Runnable resetHandler = () -> {};
    private Runnable exportHandler = () -> {};

    // State
    private boolean modified = false;

    /** Creates a new form model with default values. */
    public static ConfigFormModel createDefault() {
        ConfigFormModel model = new ConfigFormModel();
        model.setName("New Configuration");
        model.setVersion("1.0.0");
        model.setAuthor(System.getProperty("user.name", "Unknown"));
        return model;
    }

    /** Marks the form as modified and updates UI. */
    public void markAsModified() {
        this.modified = true;
        modifiedLabel.setText("Modified");
        modifiedLabel.getStyleClass().add("text-warning");
    }

    /** Resets the modified state. */
    public void resetModified() {
        this.modified = false;
        modifiedLabel.setText("");
        modifiedLabel.getStyleClass().remove("text-warning");
    }

    /** Updates the status message. */
    public void setStatus(String status) {
        statusLabel.setText(status);
    }

    /** Updates the validation message. */
    public void setValidationMessage(String message, ValidationResult.Severity severity) {
        validationLabel.setText(message);
        validationLabel.getStyleClass().removeAll("text-success", "text-warning", "text-danger");

        switch (severity) {
            case SUCCESS:
                validationLabel.getStyleClass().add("text-success");
                break;
            case WARNING:
                validationLabel.getStyleClass().add("text-warning");
                break;
            case ERROR:
                validationLabel.getStyleClass().add("text-danger");
                break;
        }
    }

    /** Clears the validation message. */
    public void clearValidationMessage() {
        validationLabel.setText("");
        validationLabel.getStyleClass().removeAll("text-success", "text-warning", "text-danger");
    }

    /** Converts the model to a flat map for saving. */
    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();

        if (name != null) map.put("name", name);
        if (project != null) map.put("project", project);
        if (version != null) map.put("version", version);
        if (description != null) map.put("description", description);
        if (author != null) map.put("author", author);

        // Add all additional fields
        map.putAll(additionalFields);

        return map;
    }

    /** Populates the model from a map. */
    public void fromMap(Map<String, String> map) {
        this.name = map.get("name");
        this.project = map.get("project");
        this.version = map.get("version");
        this.description = map.get("description");
        this.author = map.get("author");

        // Extract additional fields (anything not in the standard set)
        additionalFields.clear();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            if (!isStandardField(key)) {
                additionalFields.put(key, entry.getValue());
            }
        }
    }

    /** Checks if a field is one of the standard fields. */
    private boolean isStandardField(String key) {
        return "name".equals(key)
                || "project".equals(key)
                || "version".equals(key)
                || "description".equals(key)
                || "author".equals(key)
                || key.endsWith("Path")
                || // Skip path fields
                key.startsWith("_"); // Skip internal fields
    }

    /** Creates a copy of this model. */
    public ConfigFormModel copy() {
        ConfigFormModel copy = new ConfigFormModel();
        copy.setName(this.name);
        copy.setProject(this.project);
        copy.setVersion(this.version);
        copy.setDescription(this.description);
        copy.setAuthor(this.author);
        copy.setProjectConfigPath(this.projectConfigPath);
        copy.setDslConfigPath(this.dslConfigPath);
        copy.setImagePath(this.imagePath);
        copy.setAdditionalFields(new HashMap<>(this.additionalFields));
        copy.setModified(this.modified);
        return copy;
    }
}
