package io.github.jspinak.brobot.runner.ui.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.ui.components.BrobotButton;
import io.github.jspinak.brobot.runner.ui.components.BrobotFormGrid;
import io.github.jspinak.brobot.runner.ui.management.LabelManager;
import io.github.jspinak.brobot.runner.ui.management.UIUpdateManager;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Refactored panel for displaying configuration details and metadata. Uses LabelManager for label
 * management and UIUpdateManager for UI updates.
 */
@Slf4j
@Component
@Getter
@EqualsAndHashCode(callSuper = false)
public class RefactoredConfigDetailsPanel extends VBox {
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String UPDATE_TASK_ID = "config-details-update";
    private static final String FILE_LOAD_TASK_ID = "config-file-load";

    // Label IDs
    private static final String LABEL_NAME = "config-name";
    private static final String LABEL_PROJECT = "config-project";
    private static final String LABEL_PROJECT_CONFIG = "config-project-path";
    private static final String LABEL_DSL_CONFIG = "config-dsl-path";
    private static final String LABEL_IMAGE_PATH = "config-image-path";
    private static final String LABEL_LAST_MODIFIED = "config-last-modified";

    private final EventBus eventBus;
    private final LabelManager labelManager;
    private final UIUpdateManager uiUpdateManager;

    private final ObjectProperty<ConfigEntry> configuration = new SimpleObjectProperty<>();

    private final TextArea descriptionArea;
    private final TextField authorField;
    private final TextField versionField;

    private TabPane fileTabs;
    private TextArea projectConfigText;
    private TextArea dslConfigText;

    private boolean editable = false;

    @Autowired
    public RefactoredConfigDetailsPanel(
            EventBus eventBus, LabelManager labelManager, UIUpdateManager uiUpdateManager) {
        this.eventBus = eventBus;
        this.labelManager = labelManager;
        this.uiUpdateManager = uiUpdateManager;

        // Initialize text components
        this.descriptionArea = new TextArea();
        this.authorField = new TextField();
        this.versionField = new TextField();

        log.info("RefactoredConfigDetailsPanel created");
    }

    @PostConstruct
    public void postConstruct() {
        initialize();
        setupConfigurationListener();
        log.info("RefactoredConfigDetailsPanel initialized");
    }

    @PreDestroy
    public void preDestroy() {
        log.info("Cleaning up RefactoredConfigDetailsPanel");

        // Clean up labels
        labelManager.removeComponentLabels(this);

        // Log performance metrics
        UIUpdateManager.UpdateMetrics updateMetrics = uiUpdateManager.getMetrics(UPDATE_TASK_ID);
        if (updateMetrics != null) {
            log.info(
                    "Config details update performance - Total: {}, Avg: {:.2f}ms",
                    updateMetrics.getTotalUpdates(),
                    updateMetrics.getAverageDurationMs());
        }

        UIUpdateManager.UpdateMetrics fileLoadMetrics =
                uiUpdateManager.getMetrics(FILE_LOAD_TASK_ID);
        if (fileLoadMetrics != null) {
            log.info(
                    "File load performance - Total: {}, Avg: {:.2f}ms",
                    fileLoadMetrics.getTotalUpdates(),
                    fileLoadMetrics.getAverageDurationMs());
        }
    }

    private void initialize() {
        getStyleClass().add("configuration-details");
        setPadding(new Insets(15));
        setSpacing(15);

        // Basic info section - using BrobotFormGrid for proper spacing
        BrobotFormGrid basicInfoGrid = new BrobotFormGrid();

        // Create labels using LabelManager
        Label nameLabel = labelManager.getOrCreateLabel(this, LABEL_NAME, "");
        Label projectLabel = labelManager.getOrCreateLabel(this, LABEL_PROJECT, "");
        Label projectConfigPathLabel =
                labelManager.getOrCreateLabel(this, LABEL_PROJECT_CONFIG, "");
        Label dslConfigPathLabel = labelManager.getOrCreateLabel(this, LABEL_DSL_CONFIG, "");
        Label imagePathLabel = labelManager.getOrCreateLabel(this, LABEL_IMAGE_PATH, "");
        Label lastModifiedLabel = labelManager.getOrCreateLabel(this, LABEL_LAST_MODIFIED, "");

        // Configure label styles
        configureInfoLabel(nameLabel);
        configureInfoLabel(projectLabel);
        configureInfoLabel(projectConfigPathLabel);
        configureInfoLabel(dslConfigPathLabel);
        configureInfoLabel(imagePathLabel);
        configureInfoLabel(lastModifiedLabel);

        // Add fields using the BrobotFormGrid methods for proper spacing
        basicInfoGrid.addField("Name", nameLabel);
        basicInfoGrid.addField("Project", projectLabel);
        basicInfoGrid.addField("Project Config", projectConfigPathLabel);
        basicInfoGrid.addField("DSL Config", dslConfigPathLabel);
        basicInfoGrid.addField("Image Path", imagePathLabel);
        basicInfoGrid.addField("Last Modified", lastModifiedLabel);

        // Metadata section
        VBox metadataBox = createMetadataSection();

        // Edit actions
        HBox actionsBox = createActionsBox();

        // Preview configuration files section
        fileTabs = createFileTabs();

        // Add sections to main layout
        getChildren()
                .addAll(
                        basicInfoGrid,
                        new Separator(),
                        metadataBox,
                        actionsBox,
                        new Separator(),
                        new Label("Configuration Files"),
                        fileTabs);
    }

    private void configureInfoLabel(Label label) {
        label.setStyle("-fx-font-weight: normal;");
        label.setMaxWidth(Double.MAX_VALUE);
        label.setWrapText(false);
        label.setTextOverrun(OverrunStyle.ELLIPSIS);
    }

    private VBox createMetadataSection() {
        VBox metadataBox = new VBox(10);
        metadataBox.setPadding(new Insets(10));

        Label metadataTitle = new Label("Configuration Metadata");
        metadataTitle.getStyleClass().add("subsection-title");

        Label descriptionLabel = new Label("Description:");
        descriptionArea.setEditable(false);
        descriptionArea.setPrefRowCount(4);

        Label authorLabel = new Label("Author:");
        authorField.setEditable(false);

        Label versionLabel = new Label("Version:");
        versionField.setEditable(false);

        metadataBox
                .getChildren()
                .addAll(
                        metadataTitle,
                        descriptionLabel,
                        descriptionArea,
                        authorLabel,
                        authorField,
                        versionLabel,
                        versionField);

        return metadataBox;
    }

    private HBox createActionsBox() {
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
        saveButton
                .disableProperty()
                .bind(configuration.isNull().or(new SimpleBooleanProperty(!editable)));
        cancelButton.disableProperty().bind(saveButton.disableProperty());

        // Enable/disable edit controls based on edit mode
        updateEditableState();

        return actionsBox;
    }

    private TabPane createFileTabs() {
        TabPane tabs = new TabPane();

        Tab projectConfigTab = new Tab("Project Configuration");
        projectConfigText = new TextArea();
        projectConfigText.setEditable(false);
        projectConfigTab.setContent(projectConfigText);

        Tab dslConfigTab = new Tab("DSL Configuration");
        dslConfigText = new TextArea();
        dslConfigText.setEditable(false);
        dslConfigTab.setContent(dslConfigText);

        tabs.getTabs().addAll(projectConfigTab, dslConfigTab);
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        VBox.setVgrow(tabs, Priority.ALWAYS);

        // Load file content when tab is selected
        tabs.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (obs, old, tab) -> {
                            ConfigEntry config = configuration.get();
                            if (config == null) return;

                            // Use CompletableFuture for async file loading
                            if (tab == projectConfigTab) {
                                loadFileContentAsync(
                                        config.getProjectConfigPath(), projectConfigText);
                            } else if (tab == dslConfigTab) {
                                loadFileContentAsync(config.getDslConfigPath(), dslConfigText);
                            }
                        });

        return tabs;
    }

    private void setupConfigurationListener() {
        // Listen for configuration changes
        configuration.addListener(
                (obs, old, config) -> {
                    if (config != null) {
                        // Use UIUpdateManager for thread-safe UI updates
                        uiUpdateManager.executeUpdate(
                                UPDATE_TASK_ID, () -> updateDetailsDisplay(config));

                        // Load initial content for the selected tab
                        Tab selectedTab = fileTabs.getSelectionModel().getSelectedItem();
                        if (selectedTab != null
                                && selectedTab.getText().equals("Project Configuration")) {
                            loadFileContentAsync(config.getProjectConfigPath(), projectConfigText);
                        } else if (selectedTab != null
                                && selectedTab.getText().equals("DSL Configuration")) {
                            loadFileContentAsync(config.getDslConfigPath(), dslConfigText);
                        }
                    } else {
                        uiUpdateManager.executeUpdate(UPDATE_TASK_ID, this::clearDetailsDisplay);
                        projectConfigText.clear();
                        dslConfigText.clear();
                    }
                });
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

    /** Clears the configuration details. */
    public void clearConfiguration() {
        configuration.set(null);
    }

    /**
     * Updates the display with configuration details. This method is guaranteed to run on the
     * JavaFX thread via UIUpdateManager.
     *
     * @param config The configuration entry
     */
    private void updateDetailsDisplay(ConfigEntry config) {
        // Update labels using LabelManager
        labelManager.updateLabel(this, LABEL_NAME, config.getName());
        labelManager.updateLabel(this, LABEL_PROJECT, config.getProject());
        labelManager.updateLabel(
                this, LABEL_PROJECT_CONFIG, config.getProjectConfigPath().toString());
        labelManager.updateLabel(this, LABEL_DSL_CONFIG, config.getDslConfigPath().toString());
        labelManager.updateLabel(this, LABEL_IMAGE_PATH, config.getImagePath().toString());
        labelManager.updateLabel(
                this, LABEL_LAST_MODIFIED, config.getLastModified().format(DATE_FORMATTER));

        descriptionArea.setText(config.getDescription() != null ? config.getDescription() : "");
        authorField.setText(config.getAuthor() != null ? config.getAuthor() : "");
        versionField.setText(config.getVersion() != null ? config.getVersion() : "");

        log.debug("Updated config details display for: {}", config.getName());
    }

    /**
     * Clears the details display. This method is guaranteed to run on the JavaFX thread via
     * UIUpdateManager.
     */
    private void clearDetailsDisplay() {
        // Clear labels using LabelManager
        labelManager.updateLabel(this, LABEL_NAME, "");
        labelManager.updateLabel(this, LABEL_PROJECT, "");
        labelManager.updateLabel(this, LABEL_PROJECT_CONFIG, "");
        labelManager.updateLabel(this, LABEL_DSL_CONFIG, "");
        labelManager.updateLabel(this, LABEL_IMAGE_PATH, "");
        labelManager.updateLabel(this, LABEL_LAST_MODIFIED, "");

        descriptionArea.setText("");
        authorField.setText("");
        versionField.setText("");

        log.debug("Cleared config details display");
    }

    /**
     * Asynchronously loads file content into a text area.
     *
     * @param path The file path
     * @param textArea The text area to load content into
     */
    private void loadFileContentAsync(Path path, TextArea textArea) {
        CompletableFuture.supplyAsync(
                        () -> {
                            try {
                                if (path != null && Files.exists(path)) {
                                    return Files.readString(path);
                                } else {
                                    return "File not found: " + path;
                                }
                            } catch (IOException e) {
                                log.error("Error loading file content from: {}", path, e);
                                return "Error loading file: " + e.getMessage();
                            }
                        })
                .thenAccept(
                        content -> {
                            // Use UIUpdateManager to update UI thread-safely
                            uiUpdateManager.executeUpdate(
                                    FILE_LOAD_TASK_ID, () -> textArea.setText(content));
                        });
    }

    /** Toggles edit mode for metadata. */
    private void toggleEditMode() {
        editable = !editable;
        updateEditableState();
        log.debug("Edit mode toggled to: {}", editable);
    }

    /** Updates the editable state of metadata fields. */
    private void updateEditableState() {
        uiUpdateManager.executeUpdate(
                UPDATE_TASK_ID,
                () -> {
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

    /** Saves metadata changes. */
    private void saveMetadata() {
        ConfigEntry config = configuration.get();
        if (config == null) return;

        config.setDescription(descriptionArea.getText());
        config.setAuthor(authorField.getText());
        config.setVersion(versionField.getText());

        // In a real implementation, you would save these changes to the config file
        // For this implementation, we'll just log that the changes were made
        eventBus.publish(
                LogEvent.info(
                        this,
                        "Updated metadata for configuration: " + config.getName(),
                        "Configuration"));

        editable = false;
        updateEditableState();

        log.info("Saved metadata for configuration: {}", config.getName());
    }

    /** Cancels metadata edits. */
    private void cancelEdit() {
        ConfigEntry config = configuration.get();
        if (config != null) {
            // Reset fields to original values using UIUpdateManager
            uiUpdateManager.executeUpdate(
                    UPDATE_TASK_ID,
                    () -> {
                        descriptionArea.setText(
                                config.getDescription() != null ? config.getDescription() : "");
                        authorField.setText(config.getAuthor() != null ? config.getAuthor() : "");
                        versionField.setText(
                                config.getVersion() != null ? config.getVersion() : "");
                    });
        }

        editable = false;
        updateEditableState();

        log.debug("Cancelled metadata edit");
    }

    /**
     * Shows an error alert dialog.
     *
     * @param title The alert title
     * @param header The alert header
     * @param content The alert content
     */
    private void showErrorAlert(String title, String header, String content) {
        uiUpdateManager.executeUpdate(
                UPDATE_TASK_ID,
                () -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle(title);
                    alert.setHeaderText(header);
                    alert.setContentText(content);
                    alert.showAndWait();
                });
    }

    /** Get performance summary for this panel's operations. */
    public String getPerformanceSummary() {
        StringBuilder summary = new StringBuilder("Config Details Panel Performance:\n");

        UIUpdateManager.UpdateMetrics updateMetrics = uiUpdateManager.getMetrics(UPDATE_TASK_ID);
        if (updateMetrics != null) {
            summary.append(
                    String.format(
                            "  UI Updates: %d total, %.2f ms avg\n",
                            updateMetrics.getTotalUpdates(), updateMetrics.getAverageDurationMs()));
        }

        UIUpdateManager.UpdateMetrics fileLoadMetrics =
                uiUpdateManager.getMetrics(FILE_LOAD_TASK_ID);
        if (fileLoadMetrics != null) {
            summary.append(
                    String.format(
                            "  File Loads: %d total, %.2f ms avg\n",
                            fileLoadMetrics.getTotalUpdates(),
                            fileLoadMetrics.getAverageDurationMs()));
        }

        summary.append(String.format("  Labels managed: %d\n", 6));

        return summary.toString();
    }
}
