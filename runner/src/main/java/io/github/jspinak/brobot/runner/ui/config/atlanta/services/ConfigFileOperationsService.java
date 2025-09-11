package io.github.jspinak.brobot.runner.ui.config.atlanta.services;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;

/** Service for handling file operations related to configurations. */
@Service
public class ConfigFileOperationsService {

    private final EventBus eventBus;
    private final BrobotRunnerProperties runnerProperties;

    @Autowired
    public ConfigFileOperationsService(EventBus eventBus, BrobotRunnerProperties runnerProperties) {
        this.eventBus = eventBus;
        this.runnerProperties = runnerProperties;
    }

    /**
     * Shows a file chooser for importing configurations.
     *
     * @param ownerWindow The owner window
     * @return The selected file, or null if cancelled
     */
    public File showImportDialog(Window ownerWindow) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Configuration");
        fileChooser
                .getExtensionFilters()
                .addAll(
                        new FileChooser.ExtensionFilter(
                                "Configuration Files", "*.json", "*.yml", "*.yaml"),
                        new FileChooser.ExtensionFilter("JSON Files", "*.json"),
                        new FileChooser.ExtensionFilter("YAML Files", "*.yml", "*.yaml"),
                        new FileChooser.ExtensionFilter("All Files", "*.*"));

        // Set initial directory
        Path configPath = Paths.get(runnerProperties.getConfigPath());
        if (configPath.toFile().exists()) {
            fileChooser.setInitialDirectory(configPath.toFile());
        }

        File selectedFile = fileChooser.showOpenDialog(ownerWindow);
        if (selectedFile != null) {
            eventBus.publish(
                    LogEvent.info(
                            this,
                            "Selected configuration file: " + selectedFile.getName(),
                            "Config"));
        }

        return selectedFile;
    }

    /**
     * Shows a directory chooser for changing the configuration path.
     *
     * @param ownerWindow The owner window
     * @return The selected directory, or null if cancelled
     */
    public File showChangePathDialog(Window ownerWindow) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Configuration Directory");

        // Set initial directory
        Path configPath = Paths.get(runnerProperties.getConfigPath());
        if (configPath.toFile().exists()) {
            directoryChooser.setInitialDirectory(configPath.toFile());
        }

        File selectedDirectory = directoryChooser.showDialog(ownerWindow);
        if (selectedDirectory != null) {
            eventBus.publish(
                    LogEvent.info(
                            this,
                            "Selected configuration directory: "
                                    + selectedDirectory.getAbsolutePath(),
                            "Config"));
        }

        return selectedDirectory;
    }

    /** Opens the configuration folder in the system file explorer. */
    public void openConfigFolder() {
        Path configPath = Paths.get(runnerProperties.getConfigPath());
        File configDir = configPath.toFile();

        if (!configDir.exists()) {
            showAlert(
                    Alert.AlertType.ERROR,
                    "Directory Not Found",
                    "Configuration directory does not exist",
                    "Path: " + configPath);
            return;
        }

        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(configDir);
                eventBus.publish(
                        LogEvent.info(
                                this, "Opened configuration folder: " + configPath, "Config"));
            } else {
                // Fallback for systems where Desktop is not supported
                String os = System.getProperty("os.name").toLowerCase();
                ProcessBuilder pb;

                if (os.contains("win")) {
                    pb = new ProcessBuilder("explorer.exe", configDir.getAbsolutePath());
                } else if (os.contains("mac")) {
                    pb = new ProcessBuilder("open", configDir.getAbsolutePath());
                } else {
                    // Assume Linux/Unix
                    pb = new ProcessBuilder("xdg-open", configDir.getAbsolutePath());
                }

                pb.start();
                eventBus.publish(
                        LogEvent.info(
                                this,
                                "Opened configuration folder using system command",
                                "Config"));
            }
        } catch (IOException e) {
            showAlert(
                    Alert.AlertType.ERROR,
                    "Error Opening Folder",
                    "Could not open configuration folder",
                    e.getMessage());
            eventBus.publish(
                    LogEvent.error(
                            this,
                            "Failed to open configuration folder: " + e.getMessage(),
                            "Config",
                            e));
        }
    }

    /**
     * Shows a confirmation dialog for deletion.
     *
     * @param configName The name of the configuration to delete
     * @return True if confirmed, false otherwise
     */
    public boolean confirmDeletion(String configName) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Configuration");
        confirm.setHeaderText("Delete configuration: " + configName + "?");
        confirm.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = confirm.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Validates a configuration file.
     *
     * @param file The file to validate
     * @return True if valid, false otherwise
     */
    public boolean validateConfigurationFile(File file) {
        if (file == null || !file.exists()) {
            return false;
        }

        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".json")
                || fileName.endsWith(".yml")
                || fileName.endsWith(".yaml");
    }

    /**
     * Gets the current configuration path.
     *
     * @return The configuration path
     */
    public String getConfigPath() {
        return runnerProperties.getConfigPath();
    }

    /**
     * Updates the configuration path.
     *
     * @param newPath The new path
     */
    public void updateConfigPath(String newPath) {
        // This would update the properties
        // For now, just log the action
        eventBus.publish(
                LogEvent.info(this, "Configuration path updated to: " + newPath, "Config"));
    }

    /** Shows an alert dialog. */
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
