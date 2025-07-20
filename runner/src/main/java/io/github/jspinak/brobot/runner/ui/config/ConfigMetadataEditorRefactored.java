package io.github.jspinak.brobot.runner.ui.config;

import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.project.AutomationProjectManager;
import io.github.jspinak.brobot.runner.ui.config.services.ConfigMetadata;
import io.github.jspinak.brobot.runner.ui.config.services.ConfigMetadataService;
import io.github.jspinak.brobot.runner.ui.config.services.ConfigMetadataValidator;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.extern.slf4j.Slf4j;
import atlantafx.base.theme.Styles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Refactored editor component for configuration metadata.
 * Delegates business logic to services and focuses on UI presentation.
 */
@Slf4j
public class ConfigMetadataEditorRefactored extends BorderPane {
    
    // Services
    private final ConfigMetadataService metadataService;
    private final ConfigMetadataValidator metadataValidator;
    
    // State
    private ConfigEntry currentConfig;
    private ConfigMetadata currentMetadata;
    private ConfigMetadata originalMetadata;
    
    // UI Components
    private final ConfigMetadataForm form;
    private final ConfigMetadataToolbar toolbar;
    private final ConfigMetadataStatusBar statusBar;
    
    // Listeners
    private final List<Consumer<ConfigEntry>> configChangedListeners = new ArrayList<>();
    
    public ConfigMetadataEditorRefactored(EventBus eventBus, AutomationProjectManager projectManager) {
        this.metadataService = new ConfigMetadataService(eventBus, projectManager);
        this.metadataValidator = new ConfigMetadataValidator();
        
        this.form = new ConfigMetadataForm();
        this.toolbar = new ConfigMetadataToolbar();
        this.statusBar = new ConfigMetadataStatusBar();
        
        setupUI();
        setupEventHandlers();
        
        // Initialize with empty state
        setConfiguration(null);
    }
    
    private void setupUI() {
        setTop(toolbar);
        setCenter(form);
        setBottom(statusBar);
    }
    
    private void setupEventHandlers() {
        // Form change listener
        form.setOnMetadataChanged(() -> {
            if (currentMetadata != null && originalMetadata != null) {
                boolean hasChanges = currentMetadata.differs(originalMetadata);
                toolbar.setModified(hasChanges);
                
                if (hasChanges) {
                    // Validate on change
                    var validationResult = metadataValidator.validate(currentMetadata);
                    form.showValidationErrors(validationResult);
                    toolbar.setSaveEnabled(validationResult.isValid());
                }
            }
        });
        
        // Toolbar actions
        toolbar.setOnSave(this::saveChanges);
        toolbar.setOnReset(this::resetChanges);
    }
    
    /**
     * Sets the configuration to edit.
     */
    public void setConfiguration(ConfigEntry config) {
        // Check for unsaved changes
        if (hasUnsavedChanges() && !confirmDiscardChanges()) {
            return;
        }
        
        currentConfig = config;
        
        if (config != null) {
            // Load metadata
            currentMetadata = metadataService.loadMetadata(config);
            originalMetadata = currentMetadata.copy();
            
            // Update UI
            form.setMetadata(currentMetadata);
            form.setEnabled(true);
            statusBar.setStatus("Editing: " + config.getName());
        } else {
            currentMetadata = null;
            originalMetadata = null;
            
            form.clear();
            form.setEnabled(false);
            statusBar.setStatus("No configuration loaded");
        }
        
        toolbar.setModified(false);
        toolbar.setSaveEnabled(false);
        
        // Notify listeners
        configChangedListeners.forEach(listener -> listener.accept(config));
    }
    
    private void saveChanges() {
        if (currentConfig == null || currentMetadata == null) {
            return;
        }
        
        // Get updated metadata from form
        currentMetadata = form.getMetadata();
        
        // Validate
        var validationResult = metadataValidator.validate(currentMetadata);
        if (!validationResult.isValid()) {
            form.showValidationErrors(validationResult);
            showAlert(Alert.AlertType.ERROR, "Validation Error", 
                "Please fix the validation errors before saving.", 
                validationResult.getErrorMessage());
            return;
        }
        
        // Save
        boolean success = metadataService.saveMetadata(currentConfig, currentMetadata);
        
        if (success) {
            originalMetadata = currentMetadata.copy();
            toolbar.setModified(false);
            statusBar.setStatus("Saved successfully");
            
            showAlert(Alert.AlertType.INFORMATION, "Success", 
                "Configuration metadata saved successfully.", null);
        } else {
            showAlert(Alert.AlertType.ERROR, "Save Failed", 
                "Failed to save configuration metadata.", 
                "Please check the logs for more information.");
        }
    }
    
    private void resetChanges() {
        if (originalMetadata != null) {
            currentMetadata = originalMetadata.copy();
            form.setMetadata(currentMetadata);
            toolbar.setModified(false);
            form.clearValidationErrors();
        }
    }
    
    private boolean hasUnsavedChanges() {
        return currentMetadata != null && originalMetadata != null && 
               currentMetadata.differs(originalMetadata);
    }
    
    private boolean confirmDiscardChanges() {
        if (!hasUnsavedChanges()) {
            return true;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Unsaved Changes");
        alert.setHeaderText("You have unsaved changes.");
        alert.setContentText("Do you want to discard your changes?");
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(header);
            if (content != null) {
                alert.setContentText(content);
            }
            alert.showAndWait();
        });
    }
    
    /**
     * Clears the editor.
     */
    public void clear() {
        setConfiguration(null);
    }
    
    /**
     * Adds a listener for configuration changes.
     */
    public void addConfigChangedListener(Consumer<ConfigEntry> listener) {
        configChangedListeners.add(listener);
    }
    
    /**
     * Removes a configuration change listener.
     */
    public void removeConfigChangedListener(Consumer<ConfigEntry> listener) {
        configChangedListeners.remove(listener);
    }
}

/**
 * Form component for editing metadata.
 */
@Slf4j
class ConfigMetadataForm extends VBox {
    
    // Core fields
    private TextField projectNameField;
    private TextField versionField;
    private TextField authorField;
    private TextArea descriptionArea;
    
    // Additional fields grid
    private GridPane additionalFieldsGrid;
    private final List<AdditionalFieldRow> additionalFieldRows = new ArrayList<>();
    
    // File paths (read-only)
    private TextField projectConfigField;
    private TextField dslConfigField;
    private TextField imagePathField;
    
    // Change listener
    private Runnable onMetadataChanged;
    
    public ConfigMetadataForm() {
        super(15);
        setPadding(new Insets(20));
        createUI();
    }
    
    private void createUI() {
        // Project metadata section
        TitledPane projectPane = createProjectMetadataPane();
        
        // Additional metadata section
        TitledPane additionalPane = createAdditionalMetadataPane();
        
        // Configuration files section
        TitledPane filesPane = createFilesPane();
        
        getChildren().addAll(projectPane, additionalPane, filesPane);
    }
    
    private TitledPane createProjectMetadataPane() {
        TitledPane pane = new TitledPane();
        pane.setText("Project Metadata");
        pane.setCollapsible(false);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        
        // Create fields
        projectNameField = createTextField("Project Name:");
        versionField = createTextField("Version:");
        authorField = createTextField("Author:");
        
        Label descriptionLabel = new Label("Description:");
        descriptionArea = new TextArea();
        descriptionArea.setPrefRowCount(4);
        descriptionArea.setWrapText(true);
        descriptionArea.textProperty().addListener((obs, old, val) -> notifyChange());
        
        // Add to grid
        int row = 0;
        grid.add(new Label("Project Name:"), 0, row);
        grid.add(projectNameField, 1, row++);
        
        grid.add(new Label("Version:"), 0, row);
        grid.add(versionField, 1, row++);
        
        grid.add(new Label("Author:"), 0, row);
        grid.add(authorField, 1, row++);
        
        grid.add(descriptionLabel, 0, row);
        grid.add(descriptionArea, 1, row);
        
        // Set constraints
        GridPane.setHgrow(projectNameField, Priority.ALWAYS);
        GridPane.setHgrow(versionField, Priority.ALWAYS);
        GridPane.setHgrow(authorField, Priority.ALWAYS);
        GridPane.setHgrow(descriptionArea, Priority.ALWAYS);
        
        pane.setContent(grid);
        return pane;
    }
    
    private TitledPane createAdditionalMetadataPane() {
        TitledPane pane = new TitledPane();
        pane.setText("Additional Metadata");
        pane.setCollapsible(true);
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        additionalFieldsGrid = new GridPane();
        additionalFieldsGrid.setHgap(10);
        additionalFieldsGrid.setVgap(10);
        
        Button addFieldButton = new Button("Add Custom Field");
        addFieldButton.setOnAction(e -> showAddFieldDialog());
        
        content.getChildren().addAll(additionalFieldsGrid, addFieldButton);
        pane.setContent(content);
        return pane;
    }
    
    private TitledPane createFilesPane() {
        TitledPane pane = new TitledPane();
        pane.setText("Configuration Files");
        pane.setCollapsible(true);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        
        projectConfigField = createReadOnlyField("Project Config:");
        dslConfigField = createReadOnlyField("DSL Config:");
        imagePathField = createReadOnlyField("Image Path:");
        
        int row = 0;
        grid.add(new Label("Project Config:"), 0, row);
        grid.add(projectConfigField, 1, row++);
        
        grid.add(new Label("DSL Config:"), 0, row);
        grid.add(dslConfigField, 1, row++);
        
        grid.add(new Label("Image Path:"), 0, row);
        grid.add(imagePathField, 1, row);
        
        GridPane.setHgrow(projectConfigField, Priority.ALWAYS);
        GridPane.setHgrow(dslConfigField, Priority.ALWAYS);
        GridPane.setHgrow(imagePathField, Priority.ALWAYS);
        
        pane.setContent(grid);
        return pane;
    }
    
    private TextField createTextField(String label) {
        TextField field = new TextField();
        field.textProperty().addListener((obs, old, val) -> notifyChange());
        return field;
    }
    
    private TextField createReadOnlyField(String label) {
        TextField field = new TextField();
        field.setEditable(false);
        field.getStyleClass().add("read-only-field");
        return field;
    }
    
    private void showAddFieldDialog() {
        TextInputDialog keyDialog = new TextInputDialog();
        keyDialog.setTitle("Add Custom Field");
        keyDialog.setHeaderText("Enter the key for the new field");
        keyDialog.setContentText("Key (lowercase, no spaces):");
        
        Optional<String> keyResult = keyDialog.showAndWait();
        keyResult.ifPresent(key -> {
            if (!key.trim().isEmpty()) {
                String normalizedKey = key.toLowerCase().replaceAll("\\s+", "_").replaceAll("[^a-z0-9_]", "");
                addAdditionalField(normalizedKey, "");
                notifyChange();
            }
        });
    }
    
    private void addAdditionalField(String key, String value) {
        AdditionalFieldRow row = new AdditionalFieldRow(key, value);
        row.setOnChange(this::notifyChange);
        row.setOnRemove(() -> {
            additionalFieldRows.remove(row);
            updateAdditionalFieldsGrid();
            notifyChange();
        });
        
        additionalFieldRows.add(row);
        updateAdditionalFieldsGrid();
    }
    
    private void updateAdditionalFieldsGrid() {
        additionalFieldsGrid.getChildren().clear();
        
        int row = 0;
        for (AdditionalFieldRow fieldRow : additionalFieldRows) {
            additionalFieldsGrid.add(fieldRow.getKeyLabel(), 0, row);
            additionalFieldsGrid.add(fieldRow.getValueField(), 1, row);
            additionalFieldsGrid.add(fieldRow.getRemoveButton(), 2, row);
            
            GridPane.setHgrow(fieldRow.getValueField(), Priority.ALWAYS);
            row++;
        }
    }
    
    public void setMetadata(ConfigMetadata metadata) {
        if (metadata == null) {
            clear();
            return;
        }
        
        projectNameField.setText(metadata.getProjectName() != null ? metadata.getProjectName() : "");
        versionField.setText(metadata.getVersion() != null ? metadata.getVersion() : "");
        authorField.setText(metadata.getAuthor() != null ? metadata.getAuthor() : "");
        descriptionArea.setText(metadata.getDescription() != null ? metadata.getDescription() : "");
        
        // Additional fields
        additionalFieldRows.clear();
        if (metadata.getAdditionalFields() != null) {
            metadata.getAdditionalFields().forEach(this::addAdditionalField);
        }
        
        // File paths
        if (metadata.getProjectConfigPath() != null) {
            projectConfigField.setText(metadata.getProjectConfigPath().toString());
        }
        if (metadata.getDslConfigPath() != null) {
            dslConfigField.setText(metadata.getDslConfigPath().toString());
        }
        if (metadata.getImagePath() != null) {
            imagePathField.setText(metadata.getImagePath().toString());
        }
    }
    
    public ConfigMetadata getMetadata() {
        ConfigMetadata metadata = new ConfigMetadata();
        
        metadata.setProjectName(projectNameField.getText());
        metadata.setVersion(versionField.getText());
        metadata.setAuthor(authorField.getText());
        metadata.setDescription(descriptionArea.getText());
        
        // Collect additional fields
        for (AdditionalFieldRow row : additionalFieldRows) {
            metadata.getAdditionalFields().put(row.getKey(), row.getValue());
        }
        
        return metadata;
    }
    
    public void clear() {
        projectNameField.clear();
        versionField.clear();
        authorField.clear();
        descriptionArea.clear();
        
        additionalFieldRows.clear();
        updateAdditionalFieldsGrid();
        
        projectConfigField.clear();
        dslConfigField.clear();
        imagePathField.clear();
    }
    
    public void setEnabled(boolean enabled) {
        projectNameField.setDisable(!enabled);
        versionField.setDisable(!enabled);
        authorField.setDisable(!enabled);
        descriptionArea.setDisable(!enabled);
        
        additionalFieldRows.forEach(row -> row.setEnabled(enabled));
    }
    
    public void showValidationErrors(ConfigMetadataValidator.ValidationResult result) {
        // Clear previous error styles
        clearValidationErrors();
        
        // Apply error styles to fields with errors
        if (result.hasFieldError("projectName")) {
            projectNameField.getStyleClass().add("error");
        }
        if (result.hasFieldError("version")) {
            versionField.getStyleClass().add("error");
        }
        if (result.hasFieldError("author")) {
            authorField.getStyleClass().add("error");
        }
        if (result.hasFieldError("description")) {
            descriptionArea.getStyleClass().add("error");
        }
    }
    
    public void clearValidationErrors() {
        projectNameField.getStyleClass().remove("error");
        versionField.getStyleClass().remove("error");
        authorField.getStyleClass().remove("error");
        descriptionArea.getStyleClass().remove("error");
    }
    
    public void setOnMetadataChanged(Runnable listener) {
        this.onMetadataChanged = listener;
    }
    
    private void notifyChange() {
        if (onMetadataChanged != null) {
            onMetadataChanged.run();
        }
    }
}

/**
 * Row for additional metadata field.
 */
class AdditionalFieldRow {
    private final String key;
    private final Label keyLabel;
    private final TextField valueField;
    private final Button removeButton;
    
    private Runnable onChange;
    private Runnable onRemove;
    
    public AdditionalFieldRow(String key, String value) {
        this.key = key;
        this.keyLabel = new Label(formatKey(key) + ":");
        
        this.valueField = new TextField(value);
        this.valueField.textProperty().addListener((obs, old, val) -> {
            if (onChange != null) onChange.run();
        });
        
        this.removeButton = new Button("×");
        this.removeButton.getStyleClass().add("small-button");
        this.removeButton.setOnAction(e -> {
            if (onRemove != null) onRemove.run();
        });
    }
    
    private String formatKey(String key) {
        // Convert snake_case to Title Case
        String[] parts = key.split("_");
        StringBuilder formatted = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                formatted.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    formatted.append(part.substring(1));
                }
                formatted.append(" ");
            }
        }
        return formatted.toString().trim();
    }
    
    public String getKey() { return key; }
    public String getValue() { return valueField.getText(); }
    public Label getKeyLabel() { return keyLabel; }
    public TextField getValueField() { return valueField; }
    public Button getRemoveButton() { return removeButton; }
    
    public void setEnabled(boolean enabled) {
        valueField.setDisable(!enabled);
        removeButton.setDisable(!enabled);
    }
    
    public void setOnChange(Runnable listener) { this.onChange = listener; }
    public void setOnRemove(Runnable listener) { this.onRemove = listener; }
}

/**
 * Toolbar for metadata editor actions.
 */
class ConfigMetadataToolbar extends HBox {
    
    private final Label titleLabel;
    private final Button saveButton;
    private final Button resetButton;
    private final Label modifiedLabel;
    
    private Runnable onSave;
    private Runnable onReset;
    
    public ConfigMetadataToolbar() {
        super(10);
        setPadding(new Insets(10));
        setAlignment(Pos.CENTER_LEFT);
        
        titleLabel = new Label("Configuration Metadata Editor");
        titleLabel.getStyleClass().add("title-label");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        modifiedLabel = new Label("");
        modifiedLabel.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
        
        resetButton = new Button("Reset");
        resetButton.setOnAction(e -> { if (onReset != null) onReset.run(); });
        resetButton.setDisable(true);
        
        saveButton = new Button("Save Changes");
        saveButton.getStyleClass().add(Styles.ACCENT);
        saveButton.setOnAction(e -> { if (onSave != null) onSave.run(); });
        saveButton.setDisable(true);
        
        getChildren().addAll(titleLabel, spacer, modifiedLabel, resetButton, saveButton);
    }
    
    public void setModified(boolean modified) {
        modifiedLabel.setText(modified ? "Modified" : "");
        resetButton.setDisable(!modified);
        if (!modified) {
            saveButton.setDisable(true);
        }
    }
    
    public void setSaveEnabled(boolean enabled) {
        saveButton.setDisable(!enabled);
    }
    
    public void setOnSave(Runnable listener) { this.onSave = listener; }
    public void setOnReset(Runnable listener) { this.onReset = listener; }
}

/**
 * Status bar for metadata editor.
 */
class ConfigMetadataStatusBar extends HBox {
    
    private final Label statusLabel;
    
    public ConfigMetadataStatusBar() {
        super(10);
        setPadding(new Insets(5));
        setAlignment(Pos.CENTER_LEFT);
        
        statusLabel = new Label("No configuration loaded");
        
        getChildren().add(statusLabel);
    }
    
    public void setStatus(String status) {
        statusLabel.setText(status);
    }
}