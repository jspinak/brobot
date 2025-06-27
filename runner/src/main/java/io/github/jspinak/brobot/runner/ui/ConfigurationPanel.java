package io.github.jspinak.brobot.runner.ui;

import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.ConfigurationEvent;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.init.BrobotLibraryInitializer;
import io.github.jspinak.brobot.runner.ui.dialogs.ErrorDialog;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import net.rgielen.fxweaver.core.FxmlView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * JavaFX component for configuring the Brobot Runner
 */
@FxmlView("")
public class ConfigurationPanel extends VBox {
    private static ConfigurationPanel INSTANCE;

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationPanel.class);

    private final BrobotRunnerProperties properties;
    private final BrobotLibraryInitializer libraryInitializer;
    private final EventBus eventBus;
    private final StateService allStatesInProjectService;

    private TextField projectConfigField;
    private TextField dslConfigField;
    private TextField imagePathField;
    private Label statusLabel;

    public ConfigurationPanel(
            BrobotRunnerProperties properties,
            BrobotLibraryInitializer libraryInitializer,
            EventBus eventBus,
            StateService allStatesInProjectService) {
        this.properties = properties;
        this.libraryInitializer = libraryInitializer;
        this.eventBus = eventBus;
        this.allStatesInProjectService = allStatesInProjectService;

        setupUI(allStatesInProjectService);
    }

    @PostConstruct
    public void initialize() {
        // Set the static instance for use by event handlers
        INSTANCE = this;
    }

    private void setupUI(StateService allStatesInProjectService) {
        setPadding(new Insets(20));
        setSpacing(10);

        Label titleLabel = new Label("Brobot Runner Configuration");
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        // Project configuration file selection
        GridPane configGrid = new GridPane();
        configGrid.setHgap(10);
        configGrid.setVgap(10);

        Label projectConfigLabel = new Label("Project Configuration:");
        projectConfigField = new TextField(properties.getProjectConfigPath().toString());
        Button browseProjectButton = new Button("Browse...");
        browseProjectButton.setOnAction(e -> browseForProjectConfig());

        Label dslConfigLabel = new Label("DSL Configuration:");
        dslConfigField = new TextField(properties.getDslConfigPath().toString());
        Button browseDslButton = new Button("Browse...");
        browseDslButton.setOnAction(e -> browseForDslConfig());

        Label imagePathLabel = new Label("Images Directory:");
        imagePathField = new TextField(properties.getImagePath());
        Button browseImageButton = new Button("Browse...");
        browseImageButton.setOnAction(e -> browseForImagesDirectory());

        configGrid.add(projectConfigLabel, 0, 0);
        configGrid.add(projectConfigField, 1, 0);
        configGrid.add(browseProjectButton, 2, 0);

        configGrid.add(dslConfigLabel, 0, 1);
        configGrid.add(dslConfigField, 1, 1);
        configGrid.add(browseDslButton, 2, 1);

        configGrid.add(imagePathLabel, 0, 2);
        configGrid.add(imagePathField, 1, 2);
        configGrid.add(browseImageButton, 2, 2);

        // Load configuration button
        Button loadButton = new Button("Load Configuration");
        loadButton.setOnAction(e -> loadConfiguration());

        // Status label
        statusLabel = new Label("Status: Ready");
        statusLabel.setStyle("-fx-text-fill: blue;");

        getChildren().addAll(
                titleLabel,
                new Separator(),
                configGrid,
                loadButton,
                statusLabel
        );
    }

    private void browseForProjectConfig() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Project Configuration File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );

        // Set initial directory
        Path initialPath = Paths.get(projectConfigField.getText());
        File initialDir = initialPath.getParent() != null ?
                initialPath.getParent().toFile() :
                new File(System.getProperty("user.dir"));

        if (initialDir.exists()) {
            fileChooser.setInitialDirectory(initialDir);
        }

        File file = fileChooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            projectConfigField.setText(file.getAbsolutePath());
            updateStatus("Project configuration file selected: " + file.getName());
        }
    }

    private void browseForDslConfig() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select DSL Configuration File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );

        // Set initial directory
        Path initialPath = Paths.get(dslConfigField.getText());
        File initialDir = initialPath.getParent() != null ?
                initialPath.getParent().toFile() :
                new File(System.getProperty("user.dir"));

        if (initialDir.exists()) {
            fileChooser.setInitialDirectory(initialDir);
        }

        File file = fileChooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            dslConfigField.setText(file.getAbsolutePath());
            updateStatus("DSL configuration file selected: " + file.getName());
        }
    }

    private void browseForImagesDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Images Directory");

        // Set initial directory
        File initialDir = new File(imagePathField.getText());
        if (initialDir.exists() && initialDir.isDirectory()) {
            directoryChooser.setInitialDirectory(initialDir);
        }

        File directory = directoryChooser.showDialog(getScene().getWindow());
        if (directory != null) {
            imagePathField.setText(directory.getAbsolutePath());
            try {
                libraryInitializer.updateImagePath(directory.getAbsolutePath());
                updateStatus("Images directory updated: " + directory.getName());
            } catch (Exception ex) {
                logger.error("Failed to update image path", ex);
                ErrorDialog.show("Image Path Error",
                        "Failed to update images directory",
                        "Error: " + ex.getMessage());
                updateStatus("Error updating images directory", true);
            }
        }
    }

    private void loadConfiguration() {
        try {
            updateStatus("Loading configuration...");

            // Validate input fields first
            if (!validateInputFields()) {
                return;
            }

            Path projectConfigPath = Paths.get(projectConfigField.getText());
            Path dslConfigPath = Paths.get(dslConfigField.getText());

            // Show a progress indicator during loading
            ProgressIndicator progress = new ProgressIndicator();
            progress.setMaxSize(20, 20);
            getChildren().add(progress);

            // Use a background thread for loading
            new Thread(() -> {
                boolean success = false;
                try {
                    success = libraryInitializer.initializeWithConfig(projectConfigPath, dslConfigPath);

                    // Update UI on JavaFX thread
                    boolean finalSuccess = success;
                    javafx.application.Platform.runLater(() -> {
                        getChildren().remove(progress);

                        if (finalSuccess) {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Configuration Loaded");
                            alert.setHeaderText("Success");
                            alert.setContentText("Configuration loaded successfully!");
                            alert.showAndWait();
                            updateStatus("Configuration loaded successfully");
                            eventBus.publish(ConfigurationEvent.loaded(this, "Project Configuration",
                                    "Configuration loaded successfully"));
                        } else {
                            ErrorDialog.show("Configuration Error",
                                    "Failed to load configuration",
                                    "Please check the log for details.");
                            updateStatus("Configuration loading failed", true);
                            eventBus.publish(ConfigurationEvent.loadingFailed(this, "Project Configuration",
                                    "Failed to load configuration", null));
                        }
                    });
                } catch (Exception ex) {
                    logger.error("Failed to load configuration", ex);

                    // Update UI on JavaFX thread
                    javafx.application.Platform.runLater(() -> {
                        getChildren().remove(progress);
                        ErrorDialog.show("Configuration Error",
                                "Exception while loading configuration",
                                "Error: " + ex.getMessage());
                        updateStatus("Error loading configuration: " + ex.getMessage(), true);
                        eventBus.publish(ConfigurationEvent.loadingFailed(this, "Project Configuration",
                                "Error loading configuration: " + ex.getMessage(), ex));
                    });
                }
            }).start();

        } catch (Exception ex) {
            logger.error("Unexpected error during configuration loading", ex);
            ErrorDialog.show("Unexpected Error",
                    "An unexpected error occurred",
                    "Error: " + ex.getMessage());
            updateStatus("Unexpected error: " + ex.getMessage(), true);
        }
    }

    private boolean validateInputFields() {
        // Check project config file
        File projectFile = new File(projectConfigField.getText());
        if (!projectFile.exists() || !projectFile.isFile()) {
            ErrorDialog.show("Invalid Project File",
                    "Project configuration file not found",
                    "Please select a valid JSON file for project configuration.");
            updateStatus("Project configuration file not found", true);
            return false;
        }

        // Check DSL config file
        File dslFile = new File(dslConfigField.getText());
        if (!dslFile.exists() || !dslFile.isFile()) {
            ErrorDialog.show("Invalid DSL File",
                    "DSL configuration file not found",
                    "Please select a valid JSON file for DSL configuration.");
            updateStatus("DSL configuration file not found", true);
            return false;
        }

        // Check images directory
        File imageDir = new File(imagePathField.getText());
        if (!imageDir.exists() || !imageDir.isDirectory()) {
            ErrorDialog.show("Invalid Images Directory",
                    "Images directory not found",
                    "Please select a valid directory for images.");
            updateStatus("Images directory not found", true);
            return false;
        }

        return true;
    }

    /**
     * Gets the singleton instance of the ConfigurationPanel.
     */
    public static Optional<ConfigurationPanel> getInstance() {
        return Optional.ofNullable(INSTANCE);
    }

    /**
     * Updates the status message display.
     */
    public void updateStatus(String message) {
        updateStatus(message, false);
    }

    /**
     * Updates the status message display with optional error indication.
     */
    public void updateStatus(String message, boolean isError) {
        Platform.runLater(() -> {
            statusLabel.setText("Status: " + message);
            statusLabel.setStyle(isError ? "-fx-text-fill: red;" : "-fx-text-fill: blue;");
            logger.info(message);
        });
    }
}