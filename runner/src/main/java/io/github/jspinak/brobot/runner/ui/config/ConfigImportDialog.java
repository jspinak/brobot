package io.github.jspinak.brobot.runner.ui.config;

import io.github.jspinak.brobot.json.schemaValidation.model.ValidationResult;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.init.BrobotLibraryInitializer;
import io.github.jspinak.brobot.runner.init.ProjectConfigLoader;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

/**
 * Dialog for importing configuration files into the Brobot Runner.
 */
public class ConfigImportDialog extends Dialog<ConfigEntry> {
    private static final Logger logger = LoggerFactory.getLogger(ConfigImportDialog.class);

    private final BrobotLibraryInitializer libraryInitializer;
    private final BrobotRunnerProperties properties;
    private final EventBus eventBus;

    private TextField projectConfigField;
    private TextField dslConfigField;
    private TextField imagePathField;
    private TextField configNameField;
    private TextField projectNameField;
    private CheckBox copyFilesCheckbox;

    private Button validateButton;
    private Label validationStatusLabel;
    private ValidationResultsPanel validationResultsPanel;

    private Path selectedProjectConfig;
    private Path selectedDslConfig;
    private Path selectedImagePath;

    private ValidationResult lastValidationResult;

    public ConfigImportDialog(
            BrobotLibraryInitializer libraryInitializer,
            BrobotRunnerProperties properties,
            EventBus eventBus) {

        this.libraryInitializer = libraryInitializer;
        this.properties = properties;
        this.eventBus = eventBus;

        setTitle("Import Configuration");
        setHeaderText("Import project and DSL configuration files");

        // Set buttons
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Disable OK button initially
        getDialogPane().lookupButton(ButtonType.OK).setDisable(true);

        // Create content
        VBox content = createContent();
        getDialogPane().setContent(content);

        // Set result converter
        setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return createConfigEntry();
            }
            return null;
        });

        // Set modality
        initModality(Modality.APPLICATION_MODAL);

        // Set size
        getDialogPane().setPrefSize(800, 600);
    }

    private VBox createContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Create form
        GridPane form = createForm();

        // Create validation panel
        validationResultsPanel = new ValidationResultsPanel();
        validationResultsPanel.setPrefHeight(200);
        VBox.setVgrow(validationResultsPanel, Priority.ALWAYS);

        validationStatusLabel = new Label("Validation Status: Not Validated");
        validationStatusLabel.setStyle("-fx-font-weight: bold");

        content.getChildren().addAll(
                form,
                new Separator(),
                validationStatusLabel,
                validationResultsPanel
        );

        return content;
    }

    private GridPane createForm() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(0, 0, 0, 0));

        // Project config
        Label projectConfigLabel = new Label("Project Configuration:");
        projectConfigField = new TextField();
        projectConfigField.setEditable(false);
        projectConfigField.setPrefWidth(300);

        Button browseProjectButton = new Button("Browse...");
        browseProjectButton.setOnAction(e -> browseForProjectConfig());

        grid.add(projectConfigLabel, 0, 0);
        grid.add(projectConfigField, 1, 0);
        grid.add(browseProjectButton, 2, 0);

        // DSL config
        Label dslConfigLabel = new Label("DSL Configuration:");
        dslConfigField = new TextField();
        dslConfigField.setEditable(false);

        Button browseDslButton = new Button("Browse...");
        browseDslButton.setOnAction(e -> browseForDslConfig());

        grid.add(dslConfigLabel, 0, 1);
        grid.add(dslConfigField, 1, 1);
        grid.add(browseDslButton, 2, 1);

        // Image path
        Label imagePathLabel = new Label("Images Directory:");
        imagePathField = new TextField(properties.getImagePath());
        imagePathField.setEditable(false);

        Button browseImageButton = new Button("Browse...");
        browseImageButton.setOnAction(e -> browseForImagesDirectory());

        grid.add(imagePathLabel, 0, 2);
        grid.add(imagePathField, 1, 2);
        grid.add(browseImageButton, 2, 2);

        // Config name
        Label configNameLabel = new Label("Configuration Name:");
        configNameField = new TextField();
        configNameField.setPromptText("Enter a name for this configuration");

        grid.add(configNameLabel, 0, 3);
        grid.add(configNameField, 1, 3, 2, 1);

        // Project name
        Label projectNameLabel = new Label("Project Name:");
        projectNameField = new TextField();
        projectNameField.setPromptText("Enter the project name");

        grid.add(projectNameLabel, 0, 4);
        grid.add(projectNameField, 1, 4, 2, 1);

        // Copy files checkbox
        copyFilesCheckbox = new CheckBox("Copy files to configuration directory");
        copyFilesCheckbox.setSelected(true);

        grid.add(copyFilesCheckbox, 0, 5, 3, 1);

        // Validation button
        validateButton = new Button("Validate Configuration");
        validateButton.setDisable(true);
        validateButton.setOnAction(e -> validateConfiguration());

        validateButton.getStyleClass().add("button-primary");

        HBox validationBox = new HBox(validateButton);
        validationBox.setAlignment(Pos.CENTER_RIGHT);
        grid.add(validationBox, 0, 6, 3, 1);

        // Make text fields grow horizontally
        GridPane.setHgrow(projectConfigField, Priority.ALWAYS);
        GridPane.setHgrow(dslConfigField, Priority.ALWAYS);
        GridPane.setHgrow(imagePathField, Priority.ALWAYS);
        GridPane.setHgrow(configNameField, Priority.ALWAYS);
        GridPane.setHgrow(projectNameField, Priority.ALWAYS);

        // Add listeners to update validation button state
        projectConfigField.textProperty().addListener((obs, old, val) -> updateValidateButtonState());
        dslConfigField.textProperty().addListener((obs, old, val) -> updateValidateButtonState());
        imagePathField.textProperty().addListener((obs, old, val) -> updateValidateButtonState());

        return grid;
    }

    private void browseForProjectConfig() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Project Configuration File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        File file = fileChooser.showOpenDialog(getDialogPane().getScene().getWindow());
        if (file != null) {
            selectedProjectConfig = file.toPath();
            projectConfigField.setText(selectedProjectConfig.toString());

            // Auto-fill config name from filename
            String filename = selectedProjectConfig.getFileName().toString();
            if (filename.endsWith(".json")) {
                filename = filename.substring(0, filename.length() - 5);
            }
            configNameField.setText(filename);

            // Try to extract project name
            try {
                String content = Files.readString(selectedProjectConfig);
                // Very basic extraction - in a real implementation you'd use a JSON parser
                if (content.contains("\"name\"")) {
                    int nameIndex = content.indexOf("\"name\"");
                    int valueStart = content.indexOf(":", nameIndex) + 1;
                    int valueEnd = content.indexOf(",", valueStart);
                    if (valueEnd == -1) {
                        valueEnd = content.indexOf("}", valueStart);
                    }
                    if (valueStart > 0 && valueEnd > valueStart) {
                        String projectName = content.substring(valueStart, valueEnd).trim();
                        // Remove quotes
                        if (projectName.startsWith("\"") && projectName.endsWith("\"")) {
                            projectName = projectName.substring(1, projectName.length() - 1);
                        }
                        projectNameField.setText(projectName);
                    }
                }
            } catch (IOException e) {
                logger.error("Error reading project config file", e);
            }

            // Look for DSL config in the same directory
            if (selectedDslConfig == null) {
                try {
                    Path parentDir = selectedProjectConfig.getParent();
                    Files.list(parentDir)
                            .filter(p -> p.toString().endsWith(".json") && !p.equals(selectedProjectConfig))
                            .findFirst()
                            .ifPresent(p -> {
                                selectedDslConfig = p;
                                dslConfigField.setText(selectedDslConfig.toString());
                            });
                } catch (IOException e) {
                    logger.error("Error listing directory files", e);
                }
            }
        }
    }

    private void browseForDslConfig() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select DSL Configuration File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        // Set initial directory to the project config directory if available
        if (selectedProjectConfig != null) {
            fileChooser.setInitialDirectory(selectedProjectConfig.getParent().toFile());
        }

        File file = fileChooser.showOpenDialog(getDialogPane().getScene().getWindow());
        if (file != null) {
            selectedDslConfig = file.toPath();
            dslConfigField.setText(selectedDslConfig.toString());
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

        File directory = directoryChooser.showDialog(getDialogPane().getScene().getWindow());
        if (directory != null) {
            selectedImagePath = directory.toPath();
            imagePathField.setText(selectedImagePath.toString());
        }
    }

    private void updateValidateButtonState() {
        boolean hasProjectConfig = !projectConfigField.getText().isEmpty();
        boolean hasDslConfig = !dslConfigField.getText().isEmpty();
        boolean hasImagePath = !imagePathField.getText().isEmpty();

        validateButton.setDisable(!(hasProjectConfig && hasDslConfig && hasImagePath));

        // Also update OK button
        getDialogPane().lookupButton(ButtonType.OK).setDisable(lastValidationResult == null || lastValidationResult.hasCriticalErrors());
    }

    void validateConfiguration() {
        if (selectedProjectConfig == null || selectedDslConfig == null) {
            showAlert(Alert.AlertType.ERROR,
                    "Validation Error",
                    "Missing configuration files",
                    "Please select both project and DSL configuration files.");
            return;
        }

        Path imagePath = selectedImagePath != null ?
                selectedImagePath : Paths.get(properties.getImagePath());

        try {
            ProjectConfigLoader configLoader = new ProjectConfigLoader(null); // Normally you'd get this from Spring
            lastValidationResult = configLoader.loadAndValidate(
                    selectedProjectConfig,
                    selectedDslConfig,
                    imagePath
            );

            // Update validation results panel
            validationResultsPanel.setValidationResult(lastValidationResult);

            // Update validation status
            if (lastValidationResult.hasCriticalErrors()) {
                validationStatusLabel.setText("Validation Status: Failed (Critical Errors)");
                validationStatusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: red;");
                getDialogPane().lookupButton(ButtonType.OK).setDisable(true);
            } else if (lastValidationResult.hasWarnings()) {
                validationStatusLabel.setText("Validation Status: Passed with Warnings");
                validationStatusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: orange;");
                getDialogPane().lookupButton(ButtonType.OK).setDisable(false);
            } else {
                validationStatusLabel.setText("Validation Status: Passed");
                validationStatusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: green;");
                getDialogPane().lookupButton(ButtonType.OK).setDisable(false);
            }

        } catch (Exception e) {
            logger.error("Error validating configuration", e);
            showAlert(Alert.AlertType.ERROR,
                    "Validation Error",
                    "Error validating configuration",
                    e.getMessage());

            validationStatusLabel.setText("Validation Status: Error");
            validationStatusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: red;");
            getDialogPane().lookupButton(ButtonType.OK).setDisable(true);
        }
    }

    private ConfigEntry createConfigEntry() {
        if (selectedProjectConfig == null || selectedDslConfig == null) {
            return null;
        }

        String configName = configNameField.getText();
        if (configName.isEmpty()) {
            configName = selectedProjectConfig.getFileName().toString();
        }

        String projectName = projectNameField.getText();
        if (projectName.isEmpty()) {
            projectName = "Unknown";
        }

        Path imagePath = selectedImagePath != null ?
                selectedImagePath : Paths.get(properties.getImagePath());

        // If copy files is selected, copy to config directory
        Path projectConfigPath = selectedProjectConfig;
        Path dslConfigPath = selectedDslConfig;

        if (copyFilesCheckbox.isSelected()) {
            try {
                // Create config directory if it doesn't exist
                Path configDir = Paths.get(properties.getConfigPath());
                Files.createDirectories(configDir);

                // Copy project config
                Path targetProjectConfig = configDir.resolve(selectedProjectConfig.getFileName());
                Files.copy(selectedProjectConfig, targetProjectConfig, StandardCopyOption.REPLACE_EXISTING);
                projectConfigPath = targetProjectConfig;

                // Copy DSL config
                Path targetDslConfig = configDir.resolve(selectedDslConfig.getFileName());
                Files.copy(selectedDslConfig, targetDslConfig, StandardCopyOption.REPLACE_EXISTING);
                dslConfigPath = targetDslConfig;

                // Log copy
                eventBus.publish(LogEvent.info(this,
                        "Copied configuration files to " + configDir, "Configuration"));

            } catch (IOException e) {
                logger.error("Error copying configuration files", e);
                showAlert(Alert.AlertType.ERROR,
                        "Copy Error",
                        "Error copying configuration files",
                        e.getMessage());
            }
        }

        return new ConfigEntry(
                configName,
                projectName,
                projectConfigPath,
                dslConfigPath,
                imagePath,
                LocalDateTime.now()
        );
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}