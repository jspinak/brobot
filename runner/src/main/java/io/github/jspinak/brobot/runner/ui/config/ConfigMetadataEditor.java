package io.github.jspinak.brobot.runner.ui.config;

import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.project.AutomationProject;
import io.github.jspinak.brobot.runner.project.AutomationProjectManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Editor component for configuration metadata.
 * Allows editing project information, version, author, and other metadata.
 */
public class ConfigMetadataEditor extends BorderPane {
    private static final Logger logger = LoggerFactory.getLogger(ConfigMetadataEditor.class);

    private final EventBus eventBus;
    private final AutomationProjectManager projectManager;

    private ConfigEntry currentConfig;
    private boolean hasUnsavedChanges = false;

    // Project metadata fields
    private TextField projectNameField;
    private TextField versionField;
    private TextField authorField;
    private TextArea descriptionArea;

    // Additional metadata fields
    private final Map<String, TextField> additionalFields = new HashMap<>();

    // UI components
    private Button saveButton;
    private Button resetButton;
    private Label modifiedLabel;
    private Label statusLabel;

    public ConfigMetadataEditor(EventBus eventBus, AutomationProjectManager projectManager) {
        this.eventBus = eventBus;
        this.projectManager = projectManager;

        createUI();
    }

    private void createUI() {
        // Create form
        VBox form = createForm();

        // Create toolbar
        HBox toolbar = createToolbar();

        // Create status bar
        HBox statusBar = createStatusBar();

        // Add to layout
        setTop(toolbar);
        setCenter(form);
        setBottom(statusBar);

        // Initialize with empty state
        disableForm(true);
    }

    private VBox createForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));

        // Project metadata section
        TitledPane projectMetadataPane = new TitledPane();
        projectMetadataPane.setText("Project Metadata");
        projectMetadataPane.setCollapsible(false);

        GridPane projectGrid = new GridPane();
        projectGrid.setHgap(10);
        projectGrid.setVgap(10);
        projectGrid.setPadding(new Insets(10));

        Label projectNameLabel = new Label("Project Name:");
        projectNameField = new TextField();
        projectNameField.textProperty().addListener((obs, old, val) -> markAsModified());

        Label versionLabel = new Label("Version:");
        versionField = new TextField();
        versionField.textProperty().addListener((obs, old, val) -> markAsModified());

        Label authorLabel = new Label("Author:");
        authorField = new TextField();
        authorField.textProperty().addListener((obs, old, val) -> markAsModified());

        Label descriptionLabel = new Label("Description:");
        descriptionArea = new TextArea();
        descriptionArea.setPrefRowCount(4);
        descriptionArea.setWrapText(true);
        descriptionArea.textProperty().addListener((obs, old, val) -> markAsModified());

        projectGrid.add(projectNameLabel, 0, 0);
        projectGrid.add(projectNameField, 1, 0);

        projectGrid.add(versionLabel, 0, 1);
        projectGrid.add(versionField, 1, 1);

        projectGrid.add(authorLabel, 0, 2);
        projectGrid.add(authorField, 1, 2);

        projectGrid.add(descriptionLabel, 0, 3);
        projectGrid.add(descriptionArea, 1, 3);

        GridPane.setHgrow(projectNameField, Priority.ALWAYS);
        GridPane.setHgrow(versionField, Priority.ALWAYS);
        GridPane.setHgrow(authorField, Priority.ALWAYS);
        GridPane.setHgrow(descriptionArea, Priority.ALWAYS);

        projectMetadataPane.setContent(projectGrid);

        // Additional metadata section
        TitledPane additionalMetadataPane = new TitledPane();
        additionalMetadataPane.setText("Additional Metadata");
        additionalMetadataPane.setCollapsible(true);

        GridPane additionalGrid = new GridPane();
        additionalGrid.setHgap(10);
        additionalGrid.setVgap(10);
        additionalGrid.setPadding(new Insets(10));

        // Add some common metadata fields
        addAdditionalField(additionalGrid, 0, "Organization", "organization");
        addAdditionalField(additionalGrid, 1, "Website", "website");
        addAdditionalField(additionalGrid, 2, "License", "license");
        addAdditionalField(additionalGrid, 3, "Created Date", "createdDate");

        // Add "Add Field" button
        Button addFieldButton = new Button("Add Custom Field");
        addFieldButton.setOnAction(e -> showAddFieldDialog(additionalGrid));

        VBox additionalContent = new VBox(10);
        additionalContent.getChildren().addAll(additionalGrid, addFieldButton);
        additionalMetadataPane.setContent(additionalContent);

        // Configuration files section
        TitledPane filesPane = new TitledPane();
        filesPane.setText("Configuration Files");
        filesPane.setCollapsible(true);

        GridPane filesGrid = new GridPane();
        filesGrid.setHgap(10);
        filesGrid.setVgap(10);
        filesGrid.setPadding(new Insets(10));

        Label projectConfigLabel = new Label("Project Config:");
        TextField projectConfigField = new TextField();
        projectConfigField.setEditable(false);

        Label dslConfigLabel = new Label("DSL Config:");
        TextField dslConfigField = new TextField();
        dslConfigField.setEditable(false);

        Label imagePathLabel = new Label("Image Path:");
        TextField imagePathField = new TextField();
        imagePathField.setEditable(false);

        filesGrid.add(projectConfigLabel, 0, 0);
        filesGrid.add(projectConfigField, 1, 0);

        filesGrid.add(dslConfigLabel, 0, 1);
        filesGrid.add(dslConfigField, 1, 1);

        filesGrid.add(imagePathLabel, 0, 2);
        filesGrid.add(imagePathField, 1, 2);

        GridPane.setHgrow(projectConfigField, Priority.ALWAYS);
        GridPane.setHgrow(dslConfigField, Priority.ALWAYS);
        GridPane.setHgrow(imagePathField, Priority.ALWAYS);

        filesPane.setContent(filesGrid);

        // Bind configuration file fields to current config
        setOnCurrentConfigChanged((config) -> {
            if (config != null) {
                projectConfigField.setText(config.getProjectConfigPath().toString());
                dslConfigField.setText(config.getDslConfigPath().toString());
                imagePathField.setText(config.getImagePath().toString());
            } else {
                projectConfigField.clear();
                dslConfigField.clear();
                imagePathField.clear();
            }
        });

        // Add all sections to form
        form.getChildren().addAll(
                projectMetadataPane,
                additionalMetadataPane,
                filesPane
        );

        return form;
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(10));
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("Configuration Metadata Editor");
        titleLabel.getStyleClass().add("title-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        saveButton = new Button("Save Changes");
        saveButton.getStyleClass().add("button-primary");
        saveButton.setOnAction(e -> saveChanges());
        saveButton.setDisable(true);

        resetButton = new Button("Reset");
        resetButton.setOnAction(e -> resetChanges());
        resetButton.setDisable(true);

        toolbar.getChildren().addAll(
                titleLabel,
                spacer,
                resetButton,
                saveButton
        );

        return toolbar;
    }

    private HBox createStatusBar() {
        HBox statusBar = new HBox(10);
        statusBar.setPadding(new Insets(5));
        statusBar.setAlignment(Pos.CENTER_LEFT);

        statusLabel = new Label("No configuration loaded");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        modifiedLabel = new Label("");
        modifiedLabel.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");

        statusBar.getChildren().addAll(
                statusLabel,
                spacer,
                modifiedLabel
        );

        return statusBar;
    }

    private void addAdditionalField(GridPane grid, int row, String label, String key) {
        Label fieldLabel = new Label(label + ":");
        TextField fieldValue = new TextField();
        fieldValue.textProperty().addListener((obs, old, val) -> markAsModified());

        grid.add(fieldLabel, 0, row);
        grid.add(fieldValue, 1, row);

        GridPane.setHgrow(fieldValue, Priority.ALWAYS);

        additionalFields.put(key, fieldValue);
    }

    private void showAddFieldDialog(GridPane grid) {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Add Custom Metadata Field");
        dialog.setHeaderText("Enter the name and key for the new metadata field");

        // Set the button types
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Create the field name and key labels and fields
        GridPane dialogGrid = new GridPane();
        dialogGrid.setHgap(10);
        dialogGrid.setVgap(10);
        dialogGrid.setPadding(new Insets(20, 150, 10, 10));

        TextField fieldNameField = new TextField();
        fieldNameField.setPromptText("Field Name");

        TextField fieldKeyField = new TextField();
        fieldKeyField.setPromptText("Field Key (no spaces)");

        // Update field key as user types field name
        fieldNameField.textProperty().addListener((obs, old, val) -> {
            if (fieldKeyField.getText().isEmpty() || fieldKeyField.getText().equals(normalizeKey(old))) {
                fieldKeyField.setText(normalizeKey(val));
            }
        });

        dialogGrid.add(new Label("Field Name:"), 0, 0);
        dialogGrid.add(fieldNameField, 1, 0);
        dialogGrid.add(new Label("Field Key:"), 0, 1);
        dialogGrid.add(fieldKeyField, 1, 1);

        dialog.getDialogPane().setContent(dialogGrid);

        // Request focus on the field name field by default
        Platform.runLater(fieldNameField::requestFocus);

        // Convert the result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return new String[]{fieldNameField.getText(), fieldKeyField.getText()};
            }
            return null;
        });

        // Show the dialog and process the result
        Optional<String[]> result = dialog.showAndWait();
        result.ifPresent(nameKey -> {
            if (!nameKey[0].isEmpty() && !nameKey[1].isEmpty()) {
                // Add new field to the grid
                int newRow = grid.getRowCount();
                addAdditionalField(grid, newRow, nameKey[0], nameKey[1]);

                markAsModified();
            }
        });
    }

    private String normalizeKey(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        // Convert to lowercase, replace spaces with underscores, remove special chars
        return input.toLowerCase()
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-z0-9_]", "");
    }

    /**
     * Sets the configuration to edit.
     *
     * @param config The configuration entry
     */
    public void setConfiguration(ConfigEntry config) {
        // Check for unsaved changes before switching
        if (hasUnsavedChanges) {
            if (!confirmDiscardChanges()) {
                return;
            }
        }

        currentConfig = config;

        if (config != null) {
            loadConfigData(config);
            disableForm(false);
            statusLabel.setText("Editing: " + config.getName());
        } else {
            clearForm();
            disableForm(true);
            statusLabel.setText("No configuration loaded");
        }

        resetModifiedState();

        // Notify listeners
        currentConfigChangedListeners.forEach(listener -> listener.accept(config));
    }

    private void loadConfigData(ConfigEntry config) {
        AutomationProject project = projectManager.getActiveProject();

        if (project != null) {
            // Load from active project
            projectNameField.setText(project.getName());
            versionField.setText(project.getVersion() != null ? project.getVersion() : "");
            authorField.setText(project.getAuthor() != null ? project.getAuthor() : "");
            descriptionArea.setText(project.getDescription() != null ? project.getDescription() : "");

            // Load additional metadata
            loadAdditionalMetadata(project);
        } else {
            // Try to extract data from project config file
            try {
                String configContent = Files.readString(config.getProjectConfigPath());

                // Extract data using regex (simplified approach)
                projectNameField.setText(extractJsonValue(configContent, "name"));
                versionField.setText(extractJsonValue(configContent, "version"));
                authorField.setText(extractJsonValue(configContent, "author"));
                descriptionArea.setText(extractJsonValue(configContent, "description"));

                // Extract additional metadata
                for (Map.Entry<String, TextField> entry : additionalFields.entrySet()) {
                    String key = entry.getKey();
                    TextField field = entry.getValue();
                    field.setText(extractJsonValue(configContent, key));
                }

            } catch (IOException e) {
                logger.error("Error loading configuration data", e);
                showAlert(Alert.AlertType.ERROR,
                        "Load Error",
                        "Error loading configuration data",
                        e.getMessage());
            }
        }
    }

    private void loadAdditionalMetadata(AutomationProject project) {
        // In a real implementation, you'd have access to additional metadata fields
        // Here we'll just check for common fields in the ProjectManager

        for (Map.Entry<String, TextField> entry : additionalFields.entrySet()) {
            String key = entry.getKey();
            TextField field = entry.getValue();

            // Reset field
            field.clear();

            // This is simplified - in a real implementation you'd use reflection
            // or a more robust approach to extract metadata
            switch (key) {
                case "organization":
                    field.setText(project.getOrganization() != null ? project.getOrganization() : "");
                    break;
                case "website":
                    field.setText(project.getWebsite() != null ? project.getWebsite() : "");
                    break;
                case "license":
                    field.setText(project.getLicense() != null ? project.getLicense() : "");
                    break;
                case "createdDate":
                    field.setText(project.getCreatedDate() != null ? project.getCreatedDate() : "");
                    break;
                default:
                    // Try to get from custom properties
                    if (project.getCustomProperties() != null && project.getCustomProperties().containsKey(key)) {
                        field.setText(project.getCustomProperties().get(key).toString());
                    }
            }
        }
    }

    private void saveChanges() {
        if (currentConfig == null) {
            return;
        }

        try {
            // Update configuration entry
            currentConfig.setProject(projectNameField.getText());
            currentConfig.setVersion(versionField.getText());
            currentConfig.setAuthor(authorField.getText());
            currentConfig.setDescription(descriptionArea.getText());
            currentConfig.setLastModified(LocalDateTime.now());

            // Update project config file
            updateProjectConfigFile(currentConfig);

            // Log success
            eventBus.publish(LogEvent.info(this,
                    "Saved metadata for configuration: " + currentConfig.getName(), "Configuration"));

            // Reset modified state
            resetModifiedState();

            showAlert(Alert.AlertType.INFORMATION,
                    "Save Success",
                    "Metadata saved successfully",
                    "The configuration metadata has been updated.");

        } catch (Exception e) {
            logger.error("Error saving configuration metadata", e);
            showAlert(Alert.AlertType.ERROR,
                    "Save Error",
                    "Error saving configuration metadata",
                    e.getMessage());
        }
    }

    void updateProjectConfigFile(ConfigEntry config) throws IOException {
        Path configPath = config.getProjectConfigPath();
        String content = Files.readString(configPath);

        // Update basic fields
        content = updateJsonValue(content, "name", projectNameField.getText());
        content = updateJsonValue(content, "version", versionField.getText());
        content = updateJsonValue(content, "author", authorField.getText());
        content = updateJsonValue(content, "description", descriptionArea.getText());

        // Update additional fields
        for (Map.Entry<String, TextField> entry : additionalFields.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue().getText();

            if (!value.isEmpty()) {
                content = updateJsonValue(content, key, value);
            }
        }

        // Write updated content to file
        Files.writeString(configPath, content);

        // If project is loaded, update it
        AutomationProject project = projectManager.getActiveProject();
        if (project != null) {
            project.setName(projectNameField.getText());
            project.setVersion(versionField.getText());
            project.setAuthor(authorField.getText());
            project.setDescription(descriptionArea.getText());

            // Update additional fields
            for (Map.Entry<String, TextField> entry : additionalFields.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue().getText();

                if (!value.isEmpty()) {
                    // This is simplified - in a real implementation you'd use reflection
                    // or a more robust approach to update metadata
                    switch (key) {
                        case "organization":
                            project.setOrganization(value);
                            break;
                        case "website":
                            project.setWebsite(value);
                            break;
                        case "license":
                            project.setLicense(value);
                            break;
                        case "createdDate":
                            project.setCreatedDate(value);
                            break;
                        default:
                            // Add to custom properties
                            if (project.getCustomProperties() == null) {
                                project.setCustomProperties(new HashMap<>());
                            }
                            project.getCustomProperties().put(key, value);
                    }
                }
            }
        }
    }

    private void resetChanges() {
        if (currentConfig != null) {
            loadConfigData(currentConfig);
            resetModifiedState();
        }
    }

    private void clearForm() {
        projectNameField.clear();
        versionField.clear();
        authorField.clear();
        descriptionArea.clear();

        for (TextField field : additionalFields.values()) {
            field.clear();
        }
    }

    private void disableForm(boolean disabled) {
        projectNameField.setDisable(disabled);
        versionField.setDisable(disabled);
        authorField.setDisable(disabled);
        descriptionArea.setDisable(disabled);

        for (TextField field : additionalFields.values()) {
            field.setDisable(disabled);
        }

        saveButton.setDisable(disabled || !hasUnsavedChanges);
        resetButton.setDisable(disabled || !hasUnsavedChanges);
    }

    private void markAsModified() {
        if (!hasUnsavedChanges) {
            hasUnsavedChanges = true;
            modifiedLabel.setText("Modified - Unsaved Changes");
            saveButton.setDisable(false);
            resetButton.setDisable(false);
        }
    }

    private void resetModifiedState() {
        hasUnsavedChanges = false;
        modifiedLabel.setText("");
        saveButton.setDisable(true);
        resetButton.setDisable(true);
    }

    private boolean confirmDiscardChanges() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Unsaved Changes");
        alert.setHeaderText("You have unsaved changes");
        alert.setContentText("Do you want to discard your changes?");

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Clears the editor.
     */
    public void clear() {
        currentConfig = null;
        clearForm();
        disableForm(true);
        statusLabel.setText("No configuration loaded");
        resetModifiedState();

        // Notify listeners
        currentConfigChangedListeners.forEach(listener -> listener.accept(null));
    }

    /**
     * Shows an alert dialog.
     */
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    /**
     * Extracts a value from a JSON string using regex.
     */
    private String extractJsonValue(String json, String key) {
        if (json == null || json.isEmpty()) {
            return "";
        }

        // This is a simplified approach - in a real implementation, you'd use a JSON parser
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return "";
    }

    /**
     * Updates a value in a JSON string.
     */
    private String updateJsonValue(String json, String key, String value) {
        if (json == null || json.isEmpty()) {
            return json;
        }

        if (value == null || value.isEmpty()) {
            // Remove the key-value pair
            return json.replaceAll("\\s*\"" + key + "\"\\s*:\\s*\"[^\"]*\"\\s*,?", "")
                    .replaceAll(",\\s*}", "}");
        }

        // This is a simplified approach - in a real implementation, you'd use a JSON parser
        Pattern pattern = Pattern.compile("(\"" + key + "\"\\s*:\\s*\")(.*?)(\")", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(json);

        if (matcher.find()) {
            // Key exists, update value
            return matcher.replaceFirst("$1" + value.replace("$", "\\$") + "$3");
        } else {
            // Key doesn't exist, add it
            int insertIndex = json.lastIndexOf('}');
            if (insertIndex > 0) {
                StringBuilder sb = new StringBuilder(json);
                String comma = json.substring(0, insertIndex).trim().endsWith(",") ? "" : ",";
                sb.insert(insertIndex, comma + "\"" + key + "\": \"" + value + "\"");
                return sb.toString();
            }
        }

        return json;
    }

    // Event handling for current config changes
    private final List<Consumer<ConfigEntry>> currentConfigChangedListeners = new java.util.ArrayList<>();

    /**
     * Adds a listener for current configuration changes.
     */
    public void setOnCurrentConfigChanged(java.util.function.Consumer<ConfigEntry> listener) {
        currentConfigChangedListeners.add(listener);
    }

    /**
     * Removes a listener for current configuration changes.
     */
    public void removeOnCurrentConfigChanged(java.util.function.Consumer<ConfigEntry> listener) {
        currentConfigChangedListeners.remove(listener);
    }
}