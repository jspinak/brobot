package io.github.jspinak.brobot.runner.ui.config;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.project.AutomationProjectManager;
import io.github.jspinak.brobot.runner.ui.config.builders.ConfigFormBuilder;
import io.github.jspinak.brobot.runner.ui.config.models.ConfigData;
import io.github.jspinak.brobot.runner.ui.config.models.ConfigFormModel;
import io.github.jspinak.brobot.runner.ui.config.models.ValidationResult;
import io.github.jspinak.brobot.runner.ui.config.services.ConfigFileService;
import io.github.jspinak.brobot.runner.ui.config.services.ConfigJsonService;
import io.github.jspinak.brobot.runner.ui.config.services.ConfigStateManager;
import io.github.jspinak.brobot.runner.ui.config.services.ConfigValidationService;

import lombok.extern.slf4j.Slf4j;

/**
 * Refactored ConfigMetadataEditor using extracted services. Coordinates between services and
 * manages the UI.
 */
@Slf4j
@Component
public class RefactoredConfigMetadataEditor extends BorderPane {

    private final EventBus eventBus;
    private final AutomationProjectManager projectManager;
    private final ConfigJsonService jsonService;
    private final ConfigFileService fileService;
    private final ConfigValidationService validationService;
    private final ConfigFormBuilder formBuilder;
    private final ConfigStateManager stateManager;

    private ConfigFormModel formModel;

    @Autowired
    public RefactoredConfigMetadataEditor(
            EventBus eventBus,
            AutomationProjectManager projectManager,
            ConfigJsonService jsonService,
            ConfigFileService fileService,
            ConfigValidationService validationService,
            ConfigFormBuilder formBuilder,
            ConfigStateManager stateManager) {

        this.eventBus = eventBus;
        this.projectManager = projectManager;
        this.jsonService = jsonService;
        this.fileService = fileService;
        this.validationService = validationService;
        this.formBuilder = formBuilder;
        this.stateManager = stateManager;

        initializeUI();
    }

    /** Initializes the UI components. */
    private void initializeUI() {
        formModel = ConfigFormModel.createDefault();
        setupHandlers();

        // Build UI using form builder
        VBox form = formBuilder.buildConfigurationForm(formModel);
        HBox toolbar = formBuilder.buildToolbar(formModel);
        HBox statusBar = formBuilder.buildStatusBar(formModel);

        setTop(toolbar);
        setCenter(form);
        setBottom(statusBar);

        // Initially disabled until a config is loaded
        setDisable(true);
    }

    /** Sets up event handlers for the form model. */
    private void setupHandlers() {
        formModel.setSaveHandler(this::saveConfiguration);
        formModel.setResetHandler(this::resetConfiguration);
        formModel.setExportHandler(this::exportConfiguration);
    }

    /** Loads a configuration for editing. */
    public void loadConfiguration(ConfigEntry configEntry) {
        if (!stateManager.confirmDiscardChanges()) {
            return;
        }

        setDisable(true);
        formModel.setStatus("Loading configuration...");

        Task<ConfigData> loadTask =
                new Task<>() {
                    @Override
                    protected ConfigData call() throws Exception {
                        return fileService.loadConfigurationData(
                                configEntry.getProjectConfigPath());
                    }
                };

        loadTask.setOnSucceeded(
                e -> {
                    ConfigData data = loadTask.getValue();
                    populateForm(configEntry, data);
                    stateManager.setCurrentConfig(configEntry, formModel);
                    setDisable(false);
                    formModel.setStatus("Configuration loaded");
                    log.info("Loaded configuration: {}", configEntry.getName());
                });

        loadTask.setOnFailed(
                e -> {
                    Throwable error = loadTask.getException();
                    log.error("Failed to load configuration", error);
                    showError("Failed to load configuration", error.getMessage());
                    formModel.setStatus("Failed to load configuration");
                });

        new Thread(loadTask).start();
    }

    /** Populates the form with configuration data. */
    private void populateForm(ConfigEntry configEntry, ConfigData data) {
        formModel.fromMap(data.getRawData());

        // Set paths
        formModel.setProjectConfigPath(configEntry.getProjectConfigPath().toString());
        formModel.setDslConfigPath(configEntry.getDslConfigPath().toString());
        formModel.setImagePath(configEntry.getImagePath().toString());

        formModel.resetModified();
    }

    /** Saves the current configuration. */
    private void saveConfiguration() {
        // Validate all fields
        List<ValidationResult> errors = validationService.validateConfiguration(formModel.toMap());
        if (!errors.isEmpty()) {
            showValidationErrors(errors);
            return;
        }

        formModel.setStatus("Saving configuration...");
        ConfigEntry currentConfig = stateManager.getCurrentConfig();

        Task<Void> saveTask =
                new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        Map<String, String> updates = formModel.toMap();
                        fileService.saveProjectConfigurationData(currentConfig, updates);
                        return null;
                    }
                };

        saveTask.setOnSucceeded(
                e -> {
                    stateManager.resetModifiedState();
                    formModel.setStatus("Configuration saved successfully");
                    formModel.setValidationMessage("Saved", ValidationResult.Severity.SUCCESS);

                    // Notify project manager of configuration change
                    projectManager.getCurrentProject();

                    log.info("Configuration saved successfully: {}", currentConfig.getName());
                });

        saveTask.setOnFailed(
                e -> {
                    Throwable error = saveTask.getException();
                    log.error("Failed to save configuration", error);
                    showError("Failed to save configuration", error.getMessage());
                    formModel.setStatus("Failed to save configuration");
                });

        new Thread(saveTask).start();
    }

    /** Resets the configuration to original values. */
    private void resetConfiguration() {
        if (!stateManager.confirmDiscardChanges()) {
            return;
        }

        stateManager.revertToOriginal();
        formModel.setStatus("Configuration reset to original values");
        log.info("Reset configuration to original values");
    }

    /** Exports the configuration to a properties file. */
    private void exportConfiguration() {
        ConfigEntry currentConfig = stateManager.getCurrentConfig();
        if (currentConfig == null) {
            return;
        }

        try {
            Path exportPath =
                    Paths.get(currentConfig.getProjectConfigPath().toString() + ".properties");
            fileService.exportAsProperties(currentConfig.getProjectConfigPath(), exportPath);

            formModel.setStatus("Exported to: " + exportPath.getFileName());
            log.info("Exported configuration to: {}", exportPath);

        } catch (IOException e) {
            log.error("Failed to export configuration", e);
            showError("Export Failed", e.getMessage());
        }
    }

    /** Shows validation errors in a dialog. */
    private void showValidationErrors(List<ValidationResult> errors) {
        StringBuilder message = new StringBuilder("Please fix the following errors:\n\n");

        for (ValidationResult error : errors) {
            if (error.isError()) {
                message.append("• ").append(error.getMessage()).append("\n");
            }
        }

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Errors");
        alert.setHeaderText("Configuration validation failed");
        alert.setContentText(message.toString());
        alert.showAndWait();
    }

    /** Shows an error dialog. */
    private void showError(String title, String message) {
        Platform.runLater(
                () -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle(title);
                    alert.setHeaderText(null);
                    alert.setContentText(message);
                    alert.showAndWait();
                });
    }

    /** Clears the editor. */
    public void clear() {
        if (!stateManager.confirmDiscardChanges()) {
            return;
        }

        formModel = ConfigFormModel.createDefault();
        setupHandlers();
        stateManager.setCurrentConfig(null, formModel);
        setDisable(true);

        log.debug("Cleared configuration editor");
    }

    /** Gets the current configuration entry being edited. */
    public ConfigEntry getCurrentConfig() {
        return stateManager.getCurrentConfig();
    }

    /** Checks if there are unsaved changes. */
    public boolean hasUnsavedChanges() {
        return stateManager.hasUnsavedChanges();
    }
}
