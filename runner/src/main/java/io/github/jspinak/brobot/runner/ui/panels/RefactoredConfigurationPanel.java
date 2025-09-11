package io.github.jspinak.brobot.runner.ui.panels;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.BrobotEvent;
import io.github.jspinak.brobot.runner.events.ConfigurationEvent;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.init.BrobotLibraryInitializer;
import io.github.jspinak.brobot.runner.ui.components.base.BasePanel;
import io.github.jspinak.brobot.runner.ui.dialogs.ErrorDialog;

import lombok.extern.slf4j.Slf4j;

/**
 * Refactored configuration panel using the new architecture. Extends BasePanel for standardized
 * lifecycle management and prevents label duplication.
 */
@Slf4j
@Component
public class RefactoredConfigurationPanel extends BasePanel {

    @Autowired private BrobotRunnerProperties runnerProperties;
    @Autowired private BrobotLibraryInitializer libraryInitializer;
    @Autowired private EventBus eventBus;

    // UI Components
    private TextField projectConfigField;
    private TextField dslConfigField;
    private TextField imagePathField;
    private Label statusLabel;
    private Button loadButton;
    private CheckBox autoLoadCheckbox;

    public RefactoredConfigurationPanel() {
        super("ConfigurationPanel");
    }

    @Override
    protected void doInitialize() {
        log.info("Initializing RefactoredConfigurationPanel");

        setPadding(new Insets(20));
        setSpacing(10);
        getStyleClass().add("config-panel");

        // Title using LabelManager
        Label titleLabel =
                labelManager.getOrCreateLabel(
                        "config_title", "Brobot Runner Configuration", "config-label");
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        // Create configuration grid
        GridPane configGrid = createConfigurationGrid();

        // Create control buttons
        loadButton = new Button("Load Configuration");
        loadButton.setOnAction(e -> loadConfiguration());
        loadButton.getStyleClass().add("primary-button");

        Button saveButton = new Button("Save Configuration");
        saveButton.setOnAction(e -> saveConfiguration());
        saveButton.getStyleClass().add("secondary-button");

        // Status label using LabelManager
        statusLabel = labelManager.getOrCreateLabel("config_status", "Ready", "status-label");

        // Auto-load checkbox
        autoLoadCheckbox = new CheckBox("Auto-start automation on startup");
        autoLoadCheckbox.setSelected(runnerProperties.isAutoStartAutomation());
        autoLoadCheckbox
                .selectedProperty()
                .addListener(
                        (obs, oldVal, newVal) -> runnerProperties.setAutoStartAutomation(newVal));

        // Add all components
        getChildren()
                .addAll(
                        titleLabel,
                        new Separator(),
                        configGrid,
                        new Separator(),
                        statusLabel,
                        loadButton,
                        saveButton,
                        autoLoadCheckbox);

        // Set initial values
        refreshFieldValues();

        // Subscribe to configuration events
        eventBus.subscribe(
                BrobotEvent.EventType.CONFIG_LOADED, evt -> Platform.runLater(this::refresh));
        eventBus.subscribe(
                BrobotEvent.EventType.CONFIG_LOADING_FAILED,
                evt -> Platform.runLater(this::refresh));

        log.info("RefactoredConfigurationPanel initialized successfully");
    }

    @Override
    protected void doRefresh() {
        log.debug("Refreshing configuration panel");
        refreshFieldValues();
        updateStatus("Configuration refreshed");
    }

    @Override
    protected void doCleanup() {
        log.info("Cleaning up RefactoredConfigurationPanel");
        // Additional cleanup if needed
    }

    private GridPane createConfigurationGrid() {
        GridPane configGrid = new GridPane();
        configGrid.setHgap(10);
        configGrid.setVgap(10);
        configGrid.getStyleClass().add("config-grid");

        // Project configuration
        Label projectConfigLabel =
                labelManager.getOrCreateLabel(
                        "project_config_label", "Project Configuration:", "config-field-label");
        projectConfigField = new TextField();
        projectConfigField.setPromptText("Select project configuration file...");
        Button browseProjectButton = new Button("Browse...");
        browseProjectButton.setOnAction(e -> browseForProjectConfig());

        // DSL configuration
        Label dslConfigLabel =
                labelManager.getOrCreateLabel(
                        "dsl_config_label", "DSL Configuration:", "config-field-label");
        dslConfigField = new TextField();
        dslConfigField.setPromptText("Select DSL configuration file...");
        Button browseDslButton = new Button("Browse...");
        browseDslButton.setOnAction(e -> browseForDslConfig());

        // Images directory
        Label imagePathLabel =
                labelManager.getOrCreateLabel(
                        "image_path_label", "Images Directory:", "config-field-label");
        imagePathField = new TextField();
        imagePathField.setPromptText("Select images directory...");
        Button browseImageButton = new Button("Browse...");
        browseImageButton.setOnAction(e -> browseForImagesDirectory());

        // Add to grid
        configGrid.add(projectConfigLabel, 0, 0);
        configGrid.add(projectConfigField, 1, 0);
        configGrid.add(browseProjectButton, 2, 0);

        configGrid.add(dslConfigLabel, 0, 1);
        configGrid.add(dslConfigField, 1, 1);
        configGrid.add(browseDslButton, 2, 1);

        configGrid.add(imagePathLabel, 0, 2);
        configGrid.add(imagePathField, 1, 2);
        configGrid.add(browseImageButton, 2, 2);

        // Make text fields expand
        projectConfigField.setPrefWidth(300);
        dslConfigField.setPrefWidth(300);
        imagePathField.setPrefWidth(300);

        return configGrid;
    }

    private void refreshFieldValues() {
        projectConfigField.setText(runnerProperties.getProjectConfigPath().toString());
        dslConfigField.setText(runnerProperties.getDslConfigPath().toString());
        imagePathField.setText(runnerProperties.getImagePath());
    }

    private void browseForProjectConfig() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Project Configuration File");
        fileChooser
                .getExtensionFilters()
                .addAll(
                        new FileChooser.ExtensionFilter("JSON Files", "*.json"),
                        new FileChooser.ExtensionFilter("All Files", "*.*"));

        // Set initial directory
        File currentFile = new File(projectConfigField.getText());
        if (currentFile.getParentFile() != null && currentFile.getParentFile().exists()) {
            fileChooser.setInitialDirectory(currentFile.getParentFile());
        }

        File selectedFile = fileChooser.showOpenDialog(getScene().getWindow());
        if (selectedFile != null) {
            projectConfigField.setText(selectedFile.getAbsolutePath());
            updateStatus("Selected project config: " + selectedFile.getName());
        }
    }

    private void browseForDslConfig() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select DSL Configuration File");
        fileChooser
                .getExtensionFilters()
                .addAll(
                        new FileChooser.ExtensionFilter("JSON Files", "*.json"),
                        new FileChooser.ExtensionFilter("All Files", "*.*"));

        // Set initial directory
        File currentFile = new File(dslConfigField.getText());
        if (currentFile.getParentFile() != null && currentFile.getParentFile().exists()) {
            fileChooser.setInitialDirectory(currentFile.getParentFile());
        }

        File selectedFile = fileChooser.showOpenDialog(getScene().getWindow());
        if (selectedFile != null) {
            dslConfigField.setText(selectedFile.getAbsolutePath());
            updateStatus("Selected DSL config: " + selectedFile.getName());
        }
    }

    private void browseForImagesDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Images Directory");

        // Set initial directory
        File currentDir = new File(imagePathField.getText());
        if (currentDir.exists() && currentDir.isDirectory()) {
            directoryChooser.setInitialDirectory(currentDir);
        }

        File selectedDirectory = directoryChooser.showDialog(getScene().getWindow());
        if (selectedDirectory != null) {
            imagePathField.setText(selectedDirectory.getAbsolutePath());
            updateStatus("Selected images directory: " + selectedDirectory.getName());
        }
    }

    private void loadConfiguration() {
        try {
            updateStatus("Loading configuration...", true);
            loadButton.setDisable(true);

            // Update properties from fields
            Path projectPath = Paths.get(projectConfigField.getText());
            Path dslPath = Paths.get(dslConfigField.getText());
            runnerProperties.setImagePath(imagePathField.getText());

            // Execute loading in background
            updateManager.executeUpdate(
                    "config-load",
                    () -> {
                        try {
                            boolean success =
                                    libraryInitializer.initializeWithConfig(projectPath, dslPath);
                            if (success) {
                                Platform.runLater(
                                        () -> {
                                            updateStatus(
                                                    "Configuration loaded successfully", false);
                                            loadButton.setDisable(false);

                                            // Publish success event
                                            ConfigurationEvent event =
                                                    ConfigurationEvent.loaded(
                                                            this,
                                                            "Project Configuration",
                                                            "Successfully loaded configuration from"
                                                                    + " "
                                                                    + projectConfigField.getText());
                                            eventBus.publish(event);
                                        });
                            } else {
                                throw new RuntimeException("Failed to initialize configuration");
                            }
                        } catch (Exception e) {
                            log.error("Failed to load configuration", e);
                            Platform.runLater(
                                    () -> {
                                        updateStatus(
                                                "Failed to load configuration: " + e.getMessage(),
                                                true);
                                        loadButton.setDisable(false);

                                        // Show error dialog
                                        ErrorDialog.show(
                                                "Configuration Error",
                                                "Failed to load configuration",
                                                e.getMessage());

                                        // Publish failure event
                                        ConfigurationEvent event =
                                                ConfigurationEvent.loadingFailed(
                                                        this,
                                                        "Project Configuration",
                                                        e.getMessage(),
                                                        e);
                                        eventBus.publish(event);
                                    });
                        }
                    });
        } catch (Exception e) {
            log.error("Error in loadConfiguration", e);
            updateStatus("Error: " + e.getMessage(), true);
            loadButton.setDisable(false);
        }
    }

    private void saveConfiguration() {
        try {
            // Update properties from fields
            // Note: BrobotRunnerProperties doesn't have setters for config paths
            // These are typically set via application properties or command line
            runnerProperties.setImagePath(imagePathField.getText());

            // Store the current configuration for next startup
            updateStatus("Configuration saved");

            log.info(
                    "Configuration saved: project={}, dsl={}, images={}",
                    projectConfigField.getText(),
                    dslConfigField.getText(),
                    imagePathField.getText());

            // TODO: Implement persistence mechanism if needed
            // Could write to a properties file or use Spring's configuration externalization
        } catch (Exception e) {
            log.error("Failed to save configuration", e);
            updateStatus("Failed to save configuration: " + e.getMessage(), true);
            ErrorDialog.show("Save Error", "Failed to save configuration", e.getMessage());
        }
    }

    private void updateStatus(String message) {
        updateStatus(message, false);
    }

    private void updateStatus(String message, boolean isError) {
        Platform.runLater(
                () -> {
                    labelManager.updateLabel("config_status", message);

                    if (isError) {
                        statusLabel.getStyleClass().add("error");
                    } else {
                        statusLabel.getStyleClass().remove("error");
                    }

                    log.debug("Status updated: {}", message);
                });
    }
}
