package io.github.jspinak.brobot.runner.ui.config;

import lombok.Getter;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import io.github.jspinak.brobot.runner.ui.components.BrobotFormGrid;
import javafx.scene.control.OverrunStyle;
import io.github.jspinak.brobot.runner.ui.components.BrobotButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

/**
 * Panel for displaying configuration details and metadata.
 * 
 * @deprecated Use {@link RefactoredConfigDetailsPanel} instead.
 *             This class uses the new LabelManager and UIUpdateManager architecture for better resource management.
 *             Will be removed in version 3.0.
 */
@Deprecated(since = "2.5", forRemoval = true)
@Slf4j
@Getter
@EqualsAndHashCode(callSuper = false)
public class ConfigDetailsPanel extends VBox {
    private static final Logger logger = LoggerFactory.getLogger(ConfigDetailsPanel.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final EventBus eventBus;

    private final ObjectProperty<ConfigEntry> configuration = new SimpleObjectProperty<>();

    private final Label nameLabel;
    private final Label projectLabel;
    private final Label projectConfigPathLabel;
    private final Label dslConfigPathLabel;
    private final Label imagePathLabel;
    private final Label lastModifiedLabel;

    private final TextArea descriptionArea;
    private final TextField authorField;
    private final TextField versionField;

    private boolean editable = false;

    public ConfigDetailsPanel(EventBus eventBus) {
        this.eventBus = eventBus;

        getStyleClass().add("configuration-details");
        setPadding(new Insets(15));
        setSpacing(15);

        // Basic info section - using BrobotFormGrid for proper spacing
        BrobotFormGrid basicInfoGrid = new BrobotFormGrid();

        nameLabel = createInfoLabel();
        projectLabel = createInfoLabel();
        projectConfigPathLabel = createInfoLabel();
        dslConfigPathLabel = createInfoLabel();
        imagePathLabel = createInfoLabel();
        lastModifiedLabel = createInfoLabel();

        // Add fields using the BrobotFormGrid methods for proper spacing
        basicInfoGrid.addField("Name", nameLabel);
        basicInfoGrid.addField("Project", projectLabel);
        basicInfoGrid.addField("Project Config", projectConfigPathLabel);
        basicInfoGrid.addField("DSL Config", dslConfigPathLabel);
        basicInfoGrid.addField("Image Path", imagePathLabel);
        basicInfoGrid.addField("Last Modified", lastModifiedLabel);

        // Metadata section
        VBox metadataBox = new VBox(10);
        metadataBox.setPadding(new Insets(10));

        Label metadataTitle = new Label("Configuration Metadata");
        metadataTitle.getStyleClass().add("subsection-title");

        Label descriptionLabel = new Label("Description:");
        descriptionArea = new TextArea();
        descriptionArea.setEditable(false);
        descriptionArea.setPrefRowCount(4);

        Label authorLabel = new Label("Author:");
        authorField = new TextField();
        authorField.setEditable(false);

        Label versionLabel = new Label("Version:");
        versionField = new TextField();
        versionField.setEditable(false);

        metadataBox.getChildren().addAll(
                metadataTitle,
                descriptionLabel, descriptionArea,
                authorLabel, authorField,
                versionLabel, versionField
        );

        // Edit actions
        BrobotButton editButton = new BrobotButton("Edit Metadata");
        editButton.setOnAction(e -> toggleEditMode());

        BrobotButton saveButton = BrobotButton.primary("Save");
        saveButton.setOnAction(e -> saveMetadata());
        saveButton.setDisable(true);

        BrobotButton cancelButton = BrobotButton.secondary("Cancel");
        cancelButton.setOnAction(e -> cancelEdit());
        cancelButton.setDisable(true);

        HBox actionsBox = new HBox(10);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);
        actionsBox.getChildren().addAll(editButton, saveButton, cancelButton);

        // Bind button enabled states to edit mode
        saveButton.disableProperty().bind(configuration.isNull().or(
                Platform.isFxApplicationThread() ?
                        new SimpleBooleanProperty(!editable) : new SimpleBooleanProperty(true)));
        cancelButton.disableProperty().bind(saveButton.disableProperty());

        // Enable/disable edit controls based on edit mode
        editable = false;
        updateEditableState();

        // Preview configuration files section
        TabPane fileTabs = new TabPane();

        Tab projectConfigTab = new Tab("Project Configuration");
        TextArea projectConfigText = new TextArea();
        projectConfigText.setEditable(false);
        projectConfigTab.setContent(projectConfigText);

        Tab dslConfigTab = new Tab("DSL Configuration");
        TextArea dslConfigText = new TextArea();
        dslConfigText.setEditable(false);
        dslConfigTab.setContent(dslConfigText);

        fileTabs.getTabs().addAll(projectConfigTab, dslConfigTab);
        fileTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        VBox.setVgrow(fileTabs, Priority.ALWAYS);

        // Load file content when tab is selected
        fileTabs.getSelectionModel().selectedItemProperty().addListener((obs, old, tab) -> {
            ConfigEntry config = configuration.get();
            if (config == null) return;

            try {
                if (tab == projectConfigTab) {
                    loadFileContent(config.getProjectConfigPath(), projectConfigText);
                } else if (tab == dslConfigTab) {
                    loadFileContent(config.getDslConfigPath(), dslConfigText);
                }
            } catch (Exception e) {
                logger.error("Error loading config file content", e);
                showErrorAlert("File Load Error", "Could not load file content", e.getMessage());
            }
        });

        // Listen for configuration changes
        configuration.addListener((obs, old, config) -> {
            if (config != null) {
                updateDetailsDisplay(config);

                // Load initial content for the selected tab
                Tab selectedTab = fileTabs.getSelectionModel().getSelectedItem();
                if (selectedTab == projectConfigTab) {
                    try {
                        loadFileContent(config.getProjectConfigPath(), projectConfigText);
                    } catch (Exception e) {
                        logger.error("Error loading project config file content", e);
                    }
                } else if (selectedTab == dslConfigTab) {
                    try {
                        loadFileContent(config.getDslConfigPath(), dslConfigText);
                    } catch (Exception e) {
                        logger.error("Error loading DSL config file content", e);
                    }
                }
            } else {
                clearDetailsDisplay();
                projectConfigText.clear();
                dslConfigText.clear();
            }
        });

        // Add sections to main layout
        getChildren().addAll(
                basicInfoGrid,
                new Separator(),
                metadataBox,
                actionsBox,
                new Separator(),
                new Label("Configuration Files"),
                fileTabs
        );
    }

    /**
     * Creates a label for displaying configuration information.
     *
     * @return The created label
     */
    private Label createInfoLabel() {
        Label label = new Label();
        label.setStyle("-fx-font-weight: normal;");
        label.setMaxWidth(Double.MAX_VALUE);
        label.setWrapText(false);
        label.setTextOverrun(OverrunStyle.ELLIPSIS);
        return label;
    }

    /**
     * Sets the configuration to display details for.
     *
     * @param config The configuration entry
     */
    public void setConfiguration(ConfigEntry config) {
        configuration.set(config);
    }

    /**
     * Gets the current configuration.
     *
     * @return The configuration entry
     */
    public ConfigEntry getConfiguration() {
        return configuration.get();
    }

    /**
     * Clears the configuration details.
     */
    public void clearConfiguration() {
        configuration.set(null);
    }

    /**
     * Updates the display with configuration details.
     *
     * @param config The configuration entry
     */
    private void updateDetailsDisplay(ConfigEntry config) {
        nameLabel.setText(config.getName());
        projectLabel.setText(config.getProject());
        projectConfigPathLabel.setText(config.getProjectConfigPath().toString());
        dslConfigPathLabel.setText(config.getDslConfigPath().toString());
        imagePathLabel.setText(config.getImagePath().toString());
        lastModifiedLabel.setText(config.getLastModified().format(DATE_FORMATTER));

        descriptionArea.setText(config.getDescription() != null ? config.getDescription() : "");
        authorField.setText(config.getAuthor() != null ? config.getAuthor() : "");
        versionField.setText(config.getVersion() != null ? config.getVersion() : "");
    }

    /**
     * Clears the details display.
     */
    private void clearDetailsDisplay() {
        nameLabel.setText("");
        projectLabel.setText("");
        projectConfigPathLabel.setText("");
        dslConfigPathLabel.setText("");
        imagePathLabel.setText("");
        lastModifiedLabel.setText("");

        descriptionArea.setText("");
        authorField.setText("");
        versionField.setText("");
    }

    /**
     * Loads file content into a text area.
     *
     * @param path The file path
     * @param textArea The text area to load content into
     */
    private void loadFileContent(Path path, TextArea textArea) throws IOException {
        if (path != null && Files.exists(path)) {
            String content = Files.readString(path);
            Platform.runLater(() -> textArea.setText(content));
        } else {
            Platform.runLater(() -> textArea.setText("File not found: " + path));
        }
    }

    /**
     * Toggles edit mode for metadata.
     */
    private void toggleEditMode() {
        editable = !editable;
        updateEditableState();
    }

    /**
     * Updates the editable state of metadata fields.
     */
    private void updateEditableState() {
        Platform.runLater(() -> {
            descriptionArea.setEditable(editable);
            authorField.setEditable(editable);
            versionField.setEditable(editable);

            if (editable) {
                descriptionArea.setStyle("-fx-control-inner-background: #f8f8f8;");
                authorField.setStyle("-fx-control-inner-background: #f8f8f8;");
                versionField.setStyle("-fx-control-inner-background: #f8f8f8;");
            } else {
                descriptionArea.setStyle("");
                authorField.setStyle("");
                versionField.setStyle("");
            }
        });
    }

    /**
     * Saves metadata changes.
     */
    private void saveMetadata() {
        ConfigEntry config = configuration.get();
        if (config == null) return;

        config.setDescription(descriptionArea.getText());
        config.setAuthor(authorField.getText());
        config.setVersion(versionField.getText());

        // In a real implementation, you would save these changes to the config file
        // For this implementation, we'll just log that the changes were made
        eventBus.publish(LogEvent.info(this,
                "Updated metadata for configuration: " + config.getName(), "Configuration"));

        editable = false;
        updateEditableState();
    }

    /**
     * Cancels metadata edits.
     */
    private void cancelEdit() {
        ConfigEntry config = configuration.get();
        if (config != null) {
            // Reset fields to original values
            descriptionArea.setText(config.getDescription() != null ? config.getDescription() : "");
            authorField.setText(config.getAuthor() != null ? config.getAuthor() : "");
            versionField.setText(config.getVersion() != null ? config.getVersion() : "");
        }

        editable = false;
        updateEditableState();
    }

    /**
     * Shows an error alert dialog.
     *
     * @param title The alert title
     * @param header The alert header
     * @param content The alert content
     */
    private void showErrorAlert(String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}