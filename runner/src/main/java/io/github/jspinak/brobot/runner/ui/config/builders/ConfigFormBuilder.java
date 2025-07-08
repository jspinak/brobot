package io.github.jspinak.brobot.runner.ui.config.builders;

import atlantafx.base.theme.Styles;
import io.github.jspinak.brobot.runner.ui.components.base.BrobotCard;
import io.github.jspinak.brobot.runner.ui.components.base.GridBuilder;
import io.github.jspinak.brobot.runner.ui.config.models.ConfigFormModel;
import io.github.jspinak.brobot.runner.ui.config.models.ValidationResult;
import io.github.jspinak.brobot.runner.ui.config.services.ConfigValidationService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Builder for creating configuration forms with consistent styling.
 * Extracts UI building logic from ConfigMetadataEditor.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConfigFormBuilder {
    
    private final ConfigValidationService validationService;
    
    /**
     * Builds the complete configuration form.
     */
    public VBox buildConfigurationForm(ConfigFormModel model) {
        VBox formContainer = new VBox(16);
        formContainer.getStyleClass().add("config-form-container");
        
        // Create sections
        Node basicSection = createBasicInfoSection(model);
        Node pathsSection = createPathsSection(model);
        Node additionalSection = createAdditionalFieldsSection(model);
        
        formContainer.getChildren().addAll(basicSection, pathsSection, additionalSection);
        return formContainer;
    }
    
    /**
     * Creates the basic information section.
     */
    private Node createBasicInfoSection(ConfigFormModel model) {
        BrobotCard card = new BrobotCard("Basic Information");
        
        GridPane grid = new GridBuilder()
                .withLabelValueColumns()
                .withGap(12, 8)
                .build();
        
        int row = 0;
        
        // Name field
        TextField nameField = createTextField(model.getName(), model::setName);
        nameField.setPromptText("Configuration name");
        addFormRow(grid, row++, "Name:", nameField, true);
        
        // Project field
        TextField projectField = createTextField(model.getProject(), model::setProject);
        projectField.setPromptText("Project identifier");
        projectField.textProperty().addListener((obs, old, value) -> {
            ValidationResult result = validationService.validateProjectName(value);
            updateFieldValidation(projectField, result);
            model.setProject(value);
        });
        addFormRow(grid, row++, "Project:", projectField, true);
        
        // Version field
        TextField versionField = createTextField(model.getVersion(), model::setVersion);
        versionField.setPromptText("e.g., 1.0.0");
        versionField.textProperty().addListener((obs, old, value) -> {
            ValidationResult result = validationService.validateVersion(value);
            updateFieldValidation(versionField, result);
            model.setVersion(value);
        });
        addFormRow(grid, row++, "Version:", versionField, true);
        
        // Description field
        TextArea descriptionArea = new TextArea(model.getDescription());
        descriptionArea.setPrefRowCount(3);
        descriptionArea.setWrapText(true);
        descriptionArea.textProperty().addListener((obs, old, value) -> model.setDescription(value));
        addFormRow(grid, row++, "Description:", descriptionArea, false);
        
        // Author field
        TextField authorField = createTextField(model.getAuthor(), model::setAuthor);
        authorField.setPromptText("Author name");
        addFormRow(grid, row++, "Author:", authorField, false);
        
        card.addContent(grid);
        return card;
    }
    
    /**
     * Creates the paths section.
     */
    private Node createPathsSection(ConfigFormModel model) {
        BrobotCard card = new BrobotCard("Configuration Paths");
        
        GridPane grid = new GridBuilder()
                .withLabelValueColumns()
                .withGap(12, 8)
                .build();
        
        int row = 0;
        
        // Project config path (read-only)
        TextField projectPathField = new TextField(model.getProjectConfigPath());
        projectPathField.setEditable(false);
        projectPathField.getStyleClass().add(Styles.TEXT_MUTED);
        addFormRow(grid, row++, "Project Config:", projectPathField, false);
        
        // DSL config path (read-only)
        TextField dslPathField = new TextField(model.getDslConfigPath());
        dslPathField.setEditable(false);
        dslPathField.getStyleClass().add(Styles.TEXT_MUTED);
        addFormRow(grid, row++, "DSL Config:", dslPathField, false);
        
        // Image path (read-only)
        TextField imagePathField = new TextField(model.getImagePath());
        imagePathField.setEditable(false);
        imagePathField.getStyleClass().add(Styles.TEXT_MUTED);
        addFormRow(grid, row++, "Image Path:", imagePathField, false);
        
        card.addContent(grid);
        return card;
    }
    
    /**
     * Creates the additional fields section.
     */
    private Node createAdditionalFieldsSection(ConfigFormModel model) {
        BrobotCard card = new BrobotCard("Additional Fields");
        
        VBox content = new VBox(12);
        
        // Dynamic fields grid
        GridPane fieldsGrid = new GridBuilder()
                .withLabelValueColumns()
                .withGap(12, 8)
                .build();
        fieldsGrid.getStyleClass().add("additional-fields-grid");
        
        // Add existing additional fields
        int row = 0;
        for (Map.Entry<String, String> entry : model.getAdditionalFields().entrySet()) {
            addDynamicField(fieldsGrid, row++, entry.getKey(), entry.getValue(), model);
        }
        
        // Store reference for adding new fields
        model.setAdditionalFieldsGrid(fieldsGrid);
        
        // Add field button
        Button addFieldButton = new Button("Add Field");
        addFieldButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ACCENT);
        addFieldButton.setOnAction(e -> showAddFieldDialog(model));
        
        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.getChildren().add(addFieldButton);
        
        content.getChildren().addAll(fieldsGrid, buttonBox);
        card.addContent(content);
        return card;
    }
    
    /**
     * Creates the toolbar with action buttons.
     */
    public HBox buildToolbar(ConfigFormModel model) {
        HBox toolbar = new HBox(8);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(8));
        toolbar.getStyleClass().add("config-toolbar");
        
        Button saveButton = new Button("Save");
        saveButton.getStyleClass().addAll(Styles.ACCENT, Styles.SUCCESS);
        saveButton.setOnAction(e -> model.getSaveHandler().run());
        
        Button resetButton = new Button("Reset");
        resetButton.getStyleClass().add(Styles.BUTTON_OUTLINED);
        resetButton.setOnAction(e -> model.getResetHandler().run());
        
        Button exportButton = new Button("Export");
        exportButton.getStyleClass().add(Styles.BUTTON_OUTLINED);
        exportButton.setOnAction(e -> model.getExportHandler().run());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button helpButton = new Button("?");
        helpButton.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.BUTTON_CIRCLE);
        helpButton.setOnAction(e -> showHelpDialog());
        
        toolbar.getChildren().addAll(saveButton, resetButton, exportButton, spacer, helpButton);
        return toolbar;
    }
    
    /**
     * Creates the status bar.
     */
    public HBox buildStatusBar(ConfigFormModel model) {
        HBox statusBar = new HBox(16);
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setPadding(new Insets(8));
        statusBar.getStyleClass().add("config-status-bar");
        
        Label statusLabel = model.getStatusLabel();
        statusLabel.getStyleClass().add(Styles.TEXT_MUTED);
        
        Label modifiedLabel = model.getModifiedLabel();
        modifiedLabel.getStyleClass().add(Styles.TEXT_MUTED);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label validationLabel = model.getValidationLabel();
        
        statusBar.getChildren().addAll(statusLabel, modifiedLabel, spacer, validationLabel);
        return statusBar;
    }
    
    /**
     * Shows dialog to add a new field.
     */
    private void showAddFieldDialog(ConfigFormModel model) {
        Dialog<Map.Entry<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Add Custom Field");
        dialog.setHeaderText("Add a new configuration field");
        
        // Create form
        GridPane grid = new GridBuilder()
                .withLabelValueColumns()
                .withGap(12, 8)
                .build();
        
        TextField keyField = new TextField();
        keyField.setPromptText("Field key (e.g., apiKey)");
        
        TextField valueField = new TextField();
        valueField.setPromptText("Field value");
        
        Label validationLabel = new Label();
        validationLabel.getStyleClass().add(Styles.TEXT_SUBTLE);
        
        // Add validation
        keyField.textProperty().addListener((obs, old, value) -> {
            String normalized = validationService.normalizeKey(value);
            if (!normalized.equals(value)) {
                validationLabel.setText("Will be normalized to: " + normalized);
                validationLabel.getStyleClass().removeAll(Styles.DANGER);
                validationLabel.getStyleClass().add(Styles.WARNING);
            } else {
                ValidationResult result = validationService.validateKey(value);
                if (!result.isValid()) {
                    validationLabel.setText(result.getMessage());
                    validationLabel.getStyleClass().removeAll(Styles.WARNING);
                    validationLabel.getStyleClass().add(Styles.DANGER);
                } else {
                    validationLabel.setText("");
                }
            }
        });
        
        grid.add(new Label("Key:"), 0, 0);
        grid.add(keyField, 1, 0);
        grid.add(new Label("Value:"), 0, 1);
        grid.add(valueField, 1, 1);
        grid.add(validationLabel, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        // Add buttons
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        
        // Enable/disable add button based on validation
        Node addButton = dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);
        
        keyField.textProperty().addListener((obs, old, value) -> {
            ValidationResult result = validationService.validateKey(value);
            addButton.setDisable(!result.isValid() || value.trim().isEmpty());
        });
        
        // Convert result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                String key = validationService.normalizeKey(keyField.getText());
                return Map.entry(key, valueField.getText());
            }
            return null;
        });
        
        // Show dialog and add field if confirmed
        dialog.showAndWait().ifPresent(entry -> {
            model.getAdditionalFields().put(entry.getKey(), entry.getValue());
            int nextRow = model.getAdditionalFieldsGrid().getRowCount();
            addDynamicField(model.getAdditionalFieldsGrid(), nextRow, 
                          entry.getKey(), entry.getValue(), model);
            model.markAsModified();
        });
    }
    
    /**
     * Adds a form row to the grid.
     */
    private void addFormRow(GridPane grid, int row, String labelText, Node field, boolean required) {
        Label label = new Label(labelText);
        if (required) {
            label.setText(labelText + " *");
            label.getStyleClass().add("required-field");
        }
        
        GridPane.setConstraints(label, 0, row);
        GridPane.setConstraints(field, 1, row);
        GridPane.setHgrow(field, Priority.ALWAYS);
        
        grid.getChildren().addAll(label, field);
    }
    
    /**
     * Adds a dynamic field that can be removed.
     */
    private void addDynamicField(GridPane grid, int row, String key, String value, ConfigFormModel model) {
        Label label = new Label(key + ":");
        TextField field = createTextField(value, newValue -> {
            model.getAdditionalFields().put(key, newValue);
            model.markAsModified();
        });
        
        Button removeButton = new Button("✕");
        removeButton.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.BUTTON_CIRCLE, Styles.SMALL, Styles.DANGER);
        removeButton.setOnAction(e -> {
            model.getAdditionalFields().remove(key);
            grid.getChildren().removeAll(label, field, removeButton);
            model.markAsModified();
        });
        
        GridPane.setConstraints(label, 0, row);
        GridPane.setConstraints(field, 1, row);
        GridPane.setConstraints(removeButton, 2, row);
        GridPane.setHgrow(field, Priority.ALWAYS);
        
        grid.getChildren().addAll(label, field, removeButton);
    }
    
    /**
     * Creates a text field with a change listener.
     */
    private TextField createTextField(String initialValue, Consumer<String> changeHandler) {
        TextField field = new TextField(initialValue != null ? initialValue : "");
        field.textProperty().addListener((obs, old, value) -> changeHandler.accept(value));
        return field;
    }
    
    /**
     * Updates field styling based on validation result.
     */
    private void updateFieldValidation(TextField field, ValidationResult result) {
        field.getStyleClass().removeAll(Styles.DANGER, Styles.WARNING, Styles.SUCCESS);
        
        if (result.isError()) {
            field.getStyleClass().add(Styles.DANGER);
            field.setTooltip(new Tooltip(result.getMessage()));
        } else if (result.isWarning()) {
            field.getStyleClass().add(Styles.WARNING);
            field.setTooltip(new Tooltip(result.getMessage()));
        } else {
            field.setTooltip(null);
        }
    }
    
    /**
     * Shows help dialog.
     */
    private void showHelpDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Configuration Editor Help");
        alert.setHeaderText("How to use the Configuration Editor");
        alert.setContentText(
            "This editor allows you to modify configuration metadata.\n\n" +
            "• Required fields are marked with *\n" +
            "• Version format: major.minor[.patch][-qualifier]\n" +
            "• Project names must start with a letter\n" +
            "• Use the Add Field button to add custom properties\n" +
            "• Changes are validated in real-time\n\n" +
            "Click Save to persist your changes."
        );
        alert.showAndWait();
    }
}