package io.github.jspinak.brobot.runner.ui.config.services;

import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Factory service for creating metadata form UI components.
 * Provides consistent styling and layout for form elements.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetadataFormFactory {
    
    private static final double LABEL_MIN_WIDTH = 120;
    private static final double FIELD_PREF_WIDTH = 300;
    private static final double SPACING = 10;
    private static final Insets PADDING = new Insets(15);
    
    /**
     * Creates the main form container.
     * 
     * @return VBox containing all form sections
     */
    public VBox createMainForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.getStyleClass().add("metadata-form");
        return form;
    }
    
    /**
     * Creates a titled pane section.
     * 
     * @param title Section title
     * @param content Section content
     * @param collapsible Whether the section is collapsible
     * @return Configured TitledPane
     */
    public TitledPane createSection(String title, Region content, boolean collapsible) {
        TitledPane pane = new TitledPane();
        pane.setText(title);
        pane.setContent(content);
        pane.setCollapsible(collapsible);
        pane.setExpanded(true);
        pane.getStyleClass().add("metadata-section");
        return pane;
    }
    
    /**
     * Creates a grid pane for form fields.
     * 
     * @return Configured GridPane
     */
    public GridPane createFieldGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(SPACING);
        grid.setVgap(SPACING);
        grid.setPadding(PADDING);
        grid.getStyleClass().add("field-grid");
        
        // Configure column constraints
        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setMinWidth(LABEL_MIN_WIDTH);
        labelColumn.setHalignment(javafx.geometry.HPos.RIGHT);
        
        ColumnConstraints fieldColumn = new ColumnConstraints();
        fieldColumn.setHgrow(Priority.ALWAYS);
        
        grid.getColumnConstraints().addAll(labelColumn, fieldColumn);
        
        return grid;
    }
    
    /**
     * Creates the project metadata section.
     * 
     * @return GridPane containing project metadata fields
     */
    public GridPane createProjectMetadataGrid() {
        GridPane grid = createFieldGrid();
        grid.getStyleClass().add("project-metadata-grid");
        return grid;
    }
    
    /**
     * Creates the additional metadata section.
     * 
     * @return GridPane containing additional metadata fields
     */
    public GridPane createAdditionalMetadataGrid() {
        GridPane grid = createFieldGrid();
        grid.getStyleClass().add("additional-metadata-grid");
        return grid;
    }
    
    /**
     * Creates the file paths section.
     * 
     * @return GridPane containing file path fields
     */
    public GridPane createFilePathsGrid() {
        GridPane grid = createFieldGrid();
        grid.getStyleClass().add("file-paths-grid");
        return grid;
    }
    
    /**
     * Creates a text field with label.
     * 
     * @param label Field label
     * @param prompt Prompt text
     * @param required Whether field is required
     * @return HBox containing label and field
     */
    public TextField createTextField(String label, String prompt, boolean required) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefWidth(FIELD_PREF_WIDTH);
        
        if (required) {
            field.getStyleClass().add("required-field");
        }
        
        return field;
    }
    
    /**
     * Creates a text area with label.
     * 
     * @param label Field label
     * @param prompt Prompt text
     * @param rows Number of rows
     * @return TextArea
     */
    public TextArea createTextArea(String label, String prompt, int rows) {
        TextArea area = new TextArea();
        area.setPromptText(prompt);
        area.setPrefRowCount(rows);
        area.setPrefWidth(FIELD_PREF_WIDTH);
        area.setWrapText(true);
        return area;
    }
    
    /**
     * Creates a label for a form field.
     * 
     * @param text Label text
     * @param required Whether the field is required
     * @return Configured Label
     */
    public Label createFieldLabel(String text, boolean required) {
        Label label = new Label(text + ":");
        label.getStyleClass().add("field-label");
        
        if (required) {
            label.setText(text + " *:");
            label.getStyleClass().add("required-label");
        }
        
        return label;
    }
    
    /**
     * Adds a field to a grid at the specified row.
     * 
     * @param grid Target grid
     * @param row Row index
     * @param label Label text
     * @param field Field control
     * @param required Whether field is required
     */
    public void addFieldToGrid(GridPane grid, int row, String label, Control field, boolean required) {
        Label fieldLabel = createFieldLabel(label, required);
        grid.add(fieldLabel, 0, row);
        grid.add(field, 1, row);
        GridPane.setHgrow(field, Priority.ALWAYS);
    }
    
    /**
     * Creates a read-only text field.
     * 
     * @param value Initial value
     * @return Read-only TextField
     */
    public TextField createReadOnlyField(String value) {
        TextField field = new TextField(value);
        field.setEditable(false);
        field.getStyleClass().add("read-only-field");
        return field;
    }
    
    /**
     * Creates an add field button.
     * 
     * @param onAction Action to perform when clicked
     * @return Configured Button
     */
    public Button createAddFieldButton(Consumer<Void> onAction) {
        Button button = new Button("Add Field");
        button.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.SMALL);
        button.setOnAction(e -> onAction.accept(null));
        return button;
    }
    
    /**
     * Creates a remove field button.
     * 
     * @param onAction Action to perform when clicked
     * @return Configured Button
     */
    public Button createRemoveFieldButton(Consumer<Void> onAction) {
        Button button = new Button("×");
        button.getStyleClass().addAll(Styles.DANGER, Styles.BUTTON_CIRCLE);
        button.setTooltip(new Tooltip("Remove field"));
        button.setOnAction(e -> onAction.accept(null));
        return button;
    }
    
    /**
     * Creates a custom field row with remove button.
     * 
     * @param label Field label
     * @param field Field control
     * @param onRemove Remove action
     * @return HBox containing the field row
     */
    public HBox createCustomFieldRow(String label, Control field, Consumer<Void> onRemove) {
        HBox row = new HBox(SPACING);
        row.setAlignment(Pos.CENTER_LEFT);
        
        Label fieldLabel = createFieldLabel(label, false);
        fieldLabel.setMinWidth(LABEL_MIN_WIDTH);
        
        HBox.setHgrow(field, Priority.ALWAYS);
        
        Button removeButton = createRemoveFieldButton(onRemove);
        
        row.getChildren().addAll(fieldLabel, field, removeButton);
        return row;
    }
    
    /**
     * Creates a toolbar for form actions.
     * 
     * @param title Form title
     * @return HBox toolbar
     */
    public HBox createFormToolbar(String title) {
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(10));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.getStyleClass().add("form-toolbar");
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("title-label");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        toolbar.getChildren().addAll(titleLabel, spacer);
        return toolbar;
    }
    
    /**
     * Creates a status bar for the form.
     * 
     * @return HBox status bar
     */
    public HBox createStatusBar() {
        HBox statusBar = new HBox(10);
        statusBar.setPadding(new Insets(5));
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.getStyleClass().add("form-status-bar");
        return statusBar;
    }
    
    /**
     * Creates save and reset buttons.
     * 
     * @param onSave Save action
     * @param onReset Reset action
     * @return HBox containing buttons
     */
    public HBox createActionButtons(Consumer<Void> onSave, Consumer<Void> onReset) {
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        
        Button resetButton = new Button("Reset");
        resetButton.getStyleClass().add(Styles.BUTTON_OUTLINED);
        resetButton.setOnAction(e -> onReset.accept(null));
        
        Button saveButton = new Button("Save Changes");
        saveButton.getStyleClass().add(Styles.ACCENT);
        saveButton.setOnAction(e -> onSave.accept(null));
        
        buttons.getChildren().addAll(resetButton, saveButton);
        return buttons;
    }
    
    /**
     * Creates a separator.
     * 
     * @return Configured Separator
     */
    public Separator createSeparator() {
        Separator separator = new Separator();
        separator.getStyleClass().add("form-separator");
        VBox.setMargin(separator, new Insets(10, 0, 10, 0));
        return separator;
    }
    
    /**
     * Shows an add field dialog.
     * 
     * @return Optional array with [fieldName, fieldKey] or empty
     */
    public Optional<String[]> showAddFieldDialog() {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Add Custom Metadata Field");
        dialog.setHeaderText("Enter the name and key for the new metadata field");
        
        // Set the button types
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        
        // Create the field name and key fields
        GridPane grid = createFieldGrid();
        
        TextField nameField = createTextField("Field Name", "Display name", true);
        TextField keyField = createTextField("Field Key", "Internal key (letters, numbers, underscore)", true);
        
        // Add validation
        keyField.textProperty().addListener((obs, old, val) -> {
            if (val != null) {
                // Remove invalid characters
                String filtered = val.replaceAll("[^a-zA-Z0-9_]", "");
                if (!filtered.equals(val)) {
                    keyField.setText(filtered);
                }
            }
        });
        
        addFieldToGrid(grid, 0, "Field Name", nameField, true);
        addFieldToGrid(grid, 1, "Field Key", keyField, true);
        
        dialog.getDialogPane().setContent(grid);
        
        // Enable/Disable add button depending on whether fields are filled
        Button addButton = (Button) dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);
        
        // Validation
        Runnable validation = () -> {
            boolean valid = !nameField.getText().trim().isEmpty() && 
                           !keyField.getText().trim().isEmpty() &&
                           keyField.getText().matches("^[a-zA-Z][a-zA-Z0-9_]*$");
            addButton.setDisable(!valid);
        };
        
        nameField.textProperty().addListener((obs, old, val) -> validation.run());
        keyField.textProperty().addListener((obs, old, val) -> validation.run());
        
        // Convert the result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return new String[]{nameField.getText().trim(), keyField.getText().trim()};
            }
            return null;
        });
        
        return dialog.showAndWait();
    }
    
    /**
     * Applies consistent styling to a form component.
     * 
     * @param component Component to style
     */
    public void applyFormStyling(Region component) {
        component.getStyleClass().add("metadata-form-component");
    }
}