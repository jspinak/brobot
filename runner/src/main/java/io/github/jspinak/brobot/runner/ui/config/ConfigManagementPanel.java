package io.github.jspinak.brobot.runner.ui.config;

import io.github.jspinak.brobot.database.services.AllStatesInProjectService;
import io.github.jspinak.brobot.runner.config.ApplicationConfig;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.init.BrobotLibraryInitializer;
import io.github.jspinak.brobot.services.ProjectManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import net.rgielen.fxweaver.core.FxmlView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Main configuration management panel that integrates all configuration UI components.
 */
@FxmlView("")
public class ConfigManagementPanel extends BorderPane {
    private static final Logger logger = LoggerFactory.getLogger(ConfigManagementPanel.class);

    private final EventBus eventBus;
    private final BrobotRunnerProperties properties;
    private final BrobotLibraryInitializer libraryInitializer;
    private final ApplicationConfig appConfig;
    private final ProjectManager projectManager;
    private final AllStatesInProjectService allStatesService;

    private TabPane tabPane;
    private ConfigSelectionPanel selectionPanel;
    private ConfigBrowserPanel browserPanel;
    private ConfigMetadataEditor metadataEditor;

    public ConfigManagementPanel(
            EventBus eventBus,
            BrobotRunnerProperties properties,
            BrobotLibraryInitializer libraryInitializer,
            ApplicationConfig appConfig,
            ProjectManager projectManager,
            AllStatesInProjectService allStatesService) {

        this.eventBus = eventBus;
        this.properties = properties;
        this.libraryInitializer = libraryInitializer;
        this.appConfig = appConfig;
        this.projectManager = projectManager;
        this.allStatesService = allStatesService;

        createUI();
    }

    private void createUI() {
        // Create toolbar
        ToolBar toolbar = createToolbar();

        // Create component panels
        selectionPanel = new ConfigSelectionPanel(eventBus, properties, libraryInitializer, appConfig);
        browserPanel = new ConfigBrowserPanel(eventBus, projectManager, allStatesService); // Pass allStatesService
        metadataEditor = new ConfigMetadataEditor(eventBus, projectManager);

        // Create tab pane for different views
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Create component panels
        selectionPanel = new ConfigSelectionPanel(eventBus, properties, libraryInitializer, appConfig);
        browserPanel = new ConfigBrowserPanel(eventBus, projectManager, allStatesService);
        metadataEditor = new ConfigMetadataEditor(eventBus, projectManager);

        // Create tabs
        Tab selectionTab = new Tab("Configurations");
        selectionTab.setContent(selectionPanel);

        Tab browserTab = new Tab("Browser");
        browserTab.setContent(browserPanel);

        Tab metadataTab = new Tab("Metadata");
        metadataTab.setContent(metadataEditor);

        tabPane.getTabs().addAll(selectionTab, browserTab, metadataTab);

        // Add components to layout
        setTop(toolbar);
        setCenter(tabPane);

        // Set up status bar
        HBox statusBar = createStatusBar();
        setBottom(statusBar);
    }

    private ToolBar createToolbar() {
        ToolBar toolbar = new ToolBar();

        Button newConfigButton = new Button("New Configuration");
        newConfigButton.setOnAction(e -> createNewConfiguration());

        Button importButton = new Button("Import");
        importButton.setOnAction(e -> importConfiguration());

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> refreshConfiguration());

        Separator separator = new Separator();
        separator.setOrientation(javafx.geometry.Orientation.VERTICAL);

        Label pathLabel = new Label("Config Path: " + properties.getConfigPath());

        Button changePathButton = new Button("Change...");
        changePathButton.setOnAction(e -> changeConfigPath());

        Button openFolderButton = new Button("Open Folder");
        openFolderButton.setOnAction(e -> openConfigFolder());

        toolbar.getItems().addAll(
                newConfigButton,
                importButton,
                refreshButton,
                separator,
                pathLabel,
                changePathButton,
                openFolderButton
        );

        return toolbar;
    }

    private HBox createStatusBar() {
        HBox statusBar = new HBox(10);
        statusBar.setPadding(new Insets(5));

        Label statusLabel = new Label("Ready");

        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setVisible(false);

        Label configPathLabel = new Label("Config Path: " + properties.getConfigPath());
        Label imagePathLabel = new Label("Image Path: " + properties.getImagePath());

        // Add items to status bar
        statusBar.getChildren().addAll(
                statusLabel,
                progressBar,
                new javafx.scene.layout.Region(),  // Spacer
                configPathLabel,
                imagePathLabel
        );

        HBox.setHgrow(statusBar.getChildren().get(2), Priority.ALWAYS);

        return statusBar;
    }

    void createNewConfiguration() {
        // Show new configuration dialog
        Dialog<ConfigEntry> dialog = new Dialog<>();
        dialog.setTitle("Create New Configuration");
        dialog.setHeaderText("Create a new Brobot configuration");

        // Set buttons
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Create content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField projectNameField = new TextField();
        projectNameField.setPromptText("Project name");

        TextField configNameField = new TextField();
        configNameField.setPromptText("Configuration name");

        TextField versionField = new TextField();
        versionField.setPromptText("Version (optional)");

        TextField authorField = new TextField();
        authorField.setPromptText("Author (optional)");

        grid.add(new Label("Project Name:"), 0, 0);
        grid.add(projectNameField, 1, 0);
        grid.add(new Label("Configuration Name:"), 0, 1);
        grid.add(configNameField, 1, 1);
        grid.add(new Label("Version:"), 0, 2);
        grid.add(versionField, 1, 2);
        grid.add(new Label("Author:"), 0, 3);
        grid.add(authorField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Request focus on first field
        Platform.runLater(projectNameField::requestFocus);

        // Convert result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                // Create basic configuration template files
                try {
                    String projectName = projectNameField.getText().trim();
                    String configName = configNameField.getText().trim();

                    if (projectName.isEmpty() || configName.isEmpty()) {
                        showAlert(Alert.AlertType.ERROR,
                                "Validation Error",
                                "Project and configuration names are required",
                                "Please enter both project and configuration names.");
                        return null;
                    }

                    // Create template files
                    String projectConfig = createProjectConfigTemplate(
                            projectName,
                            versionField.getText().trim(),
                            authorField.getText().trim()
                    );

                    String dslConfig = createDslConfigTemplate(projectName);

                    // Save files to disk
                    Path configDir = Paths.get(properties.getConfigPath());
                    Path projectConfigPath = configDir.resolve(configName + "_project.json");
                    Path dslConfigPath = configDir.resolve(configName + "_dsl.json");

                    java.nio.file.Files.writeString(projectConfigPath, projectConfig);
                    java.nio.file.Files.writeString(dslConfigPath, dslConfig);

                    // Create and return config entry
                    return new ConfigEntry(
                            configName,
                            projectName,
                            projectConfigPath,
                            dslConfigPath,
                            Paths.get(properties.getImagePath()),
                            java.time.LocalDateTime.now()
                    );

                } catch (Exception e) {
                    logger.error("Error creating new configuration", e);
                    showAlert(Alert.AlertType.ERROR,
                            "Creation Error",
                            "Error creating new configuration",
                            e.getMessage());
                    return null;
                }
            }
            return null;
        });

        // Show dialog and handle result
        dialog.showAndWait().ifPresent(config -> {
            // Add to recent configs and show in selection panel
            selectionPanel.addRecentConfiguration(config);

            eventBus.publish(LogEvent.info(this,
                    "Created new configuration: " + config.getName(), "Configuration"));

            // Switch to selection tab
            tabPane.getSelectionModel().select(0);
        });
    }

    private String createProjectConfigTemplate(String projectName, String version, String author) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"name\": \"").append(projectName).append("\",\n");

        if (!version.isEmpty()) {
            sb.append("  \"version\": \"").append(version).append("\",\n");
        }

        if (!author.isEmpty()) {
            sb.append("  \"author\": \"").append(author).append("\",\n");
        }

        sb.append("  \"states\": [],\n");
        sb.append("  \"automation\": {\n");
        sb.append("    \"buttons\": []\n");
        sb.append("  }\n");
        sb.append("}");

        return sb.toString();
    }

    private String createDslConfigTemplate(String projectName) {
        return "{\n  \"project\": \"" + projectName + "\",\n  \"actions\": []\n}";
    }

    void importConfiguration() {
        ConfigImportDialog dialog = new ConfigImportDialog(
                libraryInitializer,
                properties,
                eventBus
        );

        dialog.showAndWait().ifPresent(importedConfig -> {
            // Add to recent configs
            selectionPanel.addRecentConfiguration(importedConfig);

            eventBus.publish(LogEvent.info(this,
                    "Imported configuration: " + importedConfig.getName(), "Configuration"));

            // Switch to selection tab
            tabPane.getSelectionModel().select(0);
        });
    }

    void refreshConfiguration() {
        // Refresh all panels
        selectionPanel.refreshRecentConfigurations();

        // If a project is loaded, refresh browser
        if (projectManager.getActiveProject() != null) {
            ConfigEntry currentConfig = selectionPanel.getSelectedConfiguration();
            if (currentConfig != null) {
                browserPanel.setConfiguration(currentConfig);
                metadataEditor.setConfiguration(currentConfig);
            }
        } else {
            browserPanel.clear();
            metadataEditor.clear();
        }

        eventBus.publish(LogEvent.info(this, "Configuration view refreshed", "Configuration"));
    }

    private void changeConfigPath() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Configuration Directory");

        // Set initial directory to current config path
        File initialDir = new File(properties.getConfigPath());
        if (initialDir.exists() && initialDir.isDirectory()) {
            directoryChooser.setInitialDirectory(initialDir);
        }

        // Show dialog
        File directory = directoryChooser.showDialog(getScene().getWindow());
        if (directory != null) {
            // Update config path
            properties.setConfigPath(directory.getAbsolutePath());

            // Log change
            eventBus.publish(LogEvent.info(this,
                    "Configuration path changed to: " + directory.getAbsolutePath(), "Configuration"));

            // Refresh view
            refreshConfiguration();
        }
    }

    private void openConfigFolder() {
        try {
            java.awt.Desktop.getDesktop().open(new File(properties.getConfigPath()));
        } catch (Exception e) {
            logger.error("Error opening config folder", e);
            showAlert(Alert.AlertType.ERROR,
                    "Folder Error",
                    "Error opening configuration folder",
                    e.getMessage());
        }
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