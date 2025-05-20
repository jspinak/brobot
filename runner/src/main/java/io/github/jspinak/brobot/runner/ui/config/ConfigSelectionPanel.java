package io.github.jspinak.brobot.runner.ui.config;

import io.github.jspinak.brobot.runner.config.ApplicationConfig;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.init.BrobotLibraryInitializer;
import io.github.jspinak.brobot.runner.ui.components.Card;
import io.github.jspinak.brobot.runner.ui.components.EnhancedTable;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for selecting recent configurations and managing configuration history.
 */
public class ConfigSelectionPanel extends VBox {
    private static final Logger logger = LoggerFactory.getLogger(ConfigSelectionPanel.class);
    private static final int MAX_RECENT_CONFIGS = 10;
    private static final String RECENT_CONFIGS_KEY = "recentConfigurations";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final EventBus eventBus;
    private final BrobotRunnerProperties properties;
    private final BrobotLibraryInitializer libraryInitializer;
    private final ApplicationConfig appConfig;

    private final EnhancedTable<ConfigEntry> recentConfigsTable;
    private final List<ConfigEntry> recentConfigs = new ArrayList<>();

    private ConfigDetailsPanel detailsPanel;

    public ConfigSelectionPanel(EventBus eventBus,
                                BrobotRunnerProperties properties,
                                BrobotLibraryInitializer libraryInitializer,
                                ApplicationConfig appConfig) {
        this.eventBus = eventBus;
        this.properties = properties;
        this.libraryInitializer = libraryInitializer;
        this.appConfig = appConfig;

        setPadding(new Insets(15));
        setSpacing(15);

        // Header section
        Label titleLabel = new Label("Configuration Selection");
        titleLabel.getStyleClass().add("title-label");

        HBox headerActions = new HBox(10);
        Button importButton = new Button("Import Configuration");
        importButton.getStyleClass().add("button-primary");
        importButton.setOnAction(e -> showImportDialog());

        Button browseButton = new Button("Browse Files");
        browseButton.setOnAction(e -> browseForConfiguration());

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> refreshRecentConfigurations());

        headerActions.getChildren().addAll(importButton, browseButton, refreshButton);
        headerActions.setAlignment(Pos.CENTER_RIGHT);

        HBox header = new HBox(titleLabel, headerActions);
        header.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(headerActions, Priority.ALWAYS);

        // Recent configurations table
        recentConfigsTable = new EnhancedTable<>();
        setupRecentConfigsTable();

        // Configuration details panel
        detailsPanel = new ConfigDetailsPanel(eventBus);

        // Load recent configurations
        loadRecentConfigurations();

        // Layout components
        SplitPane splitPane = new SplitPane();

        // Wrap table in a titled card
        Card recentConfigsCard = new Card("Recent Configurations");
        recentConfigsCard.setContent(recentConfigsTable);

        Card detailsCard = new Card("Configuration Details");
        detailsCard.setContent(detailsPanel);

        splitPane.getItems().addAll(recentConfigsCard, detailsCard);
        splitPane.setDividerPositions(0.4);
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        getChildren().addAll(header, splitPane);
    }

    /**
     * Gets the currently selected configuration from the table.
     *
     * @return The selected configuration entry or null if none is selected
     */
    public ConfigEntry getSelectedConfiguration() {
        return recentConfigsTable.getTableView().getSelectionModel().getSelectedItem();
    }

    private void setupRecentConfigsTable() {
        TableView<ConfigEntry> tableView = recentConfigsTable.getTableView();

        TableColumn<ConfigEntry, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getName()));
        tableView.getColumns().add(nameColumn);

        TableColumn<ConfigEntry, String> projectColumn = new TableColumn<>("Project");
        projectColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProject()));
        tableView.getColumns().add(projectColumn);

        TableColumn<ConfigEntry, String> lastModifiedColumn = new TableColumn<>("Last Modified");
        lastModifiedColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getLastModified().format(DATE_FORMATTER)));
        tableView.getColumns().add(lastModifiedColumn);

        TableColumn<ConfigEntry, String> pathColumn = new TableColumn<>("Path");
        pathColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProjectConfigPath().toString()));
        tableView.getColumns().add(pathColumn);

        // Add action column with load and delete buttons
        TableColumn<ConfigEntry, Void> actionColumn = new TableColumn<>("Actions");
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button loadButton = new Button("Load");
            private final Button deleteButton = new Button("Delete");
            private final HBox buttonBox = new HBox(5, loadButton, deleteButton);

            {
                loadButton.getStyleClass().add("button-primary");
                loadButton.setOnAction(event -> {
                    ConfigEntry entry = getTableView().getItems().get(getIndex());
                    loadConfiguration(entry);
                });

                deleteButton.getStyleClass().add("button-danger");
                deleteButton.setOnAction(event -> {
                    ConfigEntry entry = getTableView().getItems().get(getIndex());
                    removeConfiguration(entry);
                });

                buttonBox.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttonBox);
            }
        });

        recentConfigsTable.getTableView().getColumns().add(actionColumn);

        // Set row selection to update details view
        recentConfigsTable.getTableView().getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        showConfigDetails(newVal);
                    }
                });
    }

    private void showImportDialog() {
        ConfigImportDialog dialog = new ConfigImportDialog(
                libraryInitializer,
                properties,
                eventBus
        );

        dialog.showAndWait().ifPresent(importedConfig -> {
            // Add to recent configs
            addRecentConfiguration(importedConfig);

            // Select the newly imported config
            recentConfigsTable.getTableView().getSelectionModel().select(importedConfig);

            // Show details
            showConfigDetails(importedConfig);
        });
    }

    private void browseForConfiguration() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Project Configuration File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        File file = fileChooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            try {
                Path projectConfigPath = file.toPath();

                // Try to find related DSL config in the same directory
                Path parentDir = projectConfigPath.getParent();
                List<Path> jsonFiles = Files.list(parentDir)
                        .filter(p -> p.toString().endsWith(".json") && !p.equals(projectConfigPath))
                        .toList();

                Path dslConfigPath = null;

                // If there's only one other JSON file, assume it's the DSL config
                if (jsonFiles.size() == 1) {
                    dslConfigPath = jsonFiles.getFirst();
                }
                // If there are multiple, try to find one with "dsl" or "automation" in the name
                else if (jsonFiles.size() > 1) {
                    for (Path path : jsonFiles) {
                        String filename = path.getFileName().toString().toLowerCase();
                        if (filename.contains("dsl") || filename.contains("automation")) {
                            dslConfigPath = path;
                            break;
                        }
                    }

                    // If still not found, show file chooser for DSL config
                    if (dslConfigPath == null) {
                        fileChooser.setTitle("Select DSL Configuration File");
                        fileChooser.setInitialDirectory(parentDir.toFile());
                        File dslFile = fileChooser.showOpenDialog(getScene().getWindow());
                        if (dslFile != null) {
                            dslConfigPath = dslFile.toPath();
                        }
                    }
                }

                if (dslConfigPath != null) {
                    // Create a new config entry
                    ConfigEntry entry = new ConfigEntry(
                            projectConfigPath.getFileName().toString(),
                            "Unknown", // Will be updated when loaded
                            projectConfigPath,
                            dslConfigPath,
                            Paths.get(properties.getImagePath()),
                            LocalDateTime.now()
                    );

                    // Add to recent configs
                    addRecentConfiguration(entry);

                    // Select the newly added config
                    recentConfigsTable.getTableView().getSelectionModel().select(entry);

                    // Show details
                    showConfigDetails(entry);
                } else {
                    showAlert(Alert.AlertType.WARNING,
                            "DSL Configuration",
                            "No DSL configuration file selected",
                            "Please select both project and DSL configuration files.");
                }
            } catch (IOException e) {
                logger.error("Error browsing for configuration", e);
                showAlert(Alert.AlertType.ERROR,
                        "File Error",
                        "Error browsing for configuration",
                        e.getMessage());
            }
        }
    }

    private void showConfigDetails(ConfigEntry entry) {
        if (entry != null) {
            detailsPanel.setConfiguration(entry);
        }
    }

    private void loadConfiguration(ConfigEntry entry) {
        if (entry != null) {
            try {
                boolean success = libraryInitializer.initializeWithConfig(
                        entry.getProjectConfigPath(),
                        entry.getDslConfigPath()
                );

                if (success) {
                    // Update last modified date
                    entry.setLastModified(LocalDateTime.now());

                    // Move to top of recent configs
                    recentConfigs.remove(entry);
                    recentConfigs.addFirst(entry);

                    // Save recent configs
                    saveRecentConfigurations();

                    // Refresh table
                    refreshTable();

                    // Log success
                    String message = "Configuration loaded successfully: " + entry.getName();
                    eventBus.publish(LogEvent.info(this, message, "Configuration"));

                    showAlert(Alert.AlertType.INFORMATION,
                            "Configuration Loaded",
                            "Configuration loaded successfully",
                            "Project: " + entry.getProject());
                } else {
                    String errorMessage = libraryInitializer.getLastErrorMessage();
                    showAlert(Alert.AlertType.ERROR,
                            "Load Failed",
                            "Failed to load configuration",
                            errorMessage != null ? errorMessage : "Unknown error");
                }
            } catch (Exception e) {
                logger.error("Error loading configuration", e);
                showAlert(Alert.AlertType.ERROR,
                        "Load Error",
                        "Error loading configuration",
                        e.getMessage());
            }
        }
    }

    private void removeConfiguration(ConfigEntry entry) {
        if (entry != null) {
            // Confirm deletion
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Remove Configuration");
            alert.setHeaderText("Remove configuration from recent list?");
            alert.setContentText("This will only remove the entry from the recent list, not delete the files.");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // Remove from list
                    recentConfigs.remove(entry);

                    // Save recent configs
                    saveRecentConfigurations();

                    // Refresh table
                    refreshTable();

                    // Clear details if the removed entry was selected
                    if (detailsPanel.getConfiguration() == entry) {
                        detailsPanel.clearConfiguration();
                    }

                    // Log removal
                    String message = "Removed configuration from recent list: " + entry.getName();
                    eventBus.publish(LogEvent.info(this, message, "Configuration"));
                }
            });
        }
    }

    private void loadRecentConfigurations() {
        try {
            recentConfigs.clear();

            // Get recent configs from app config
            String configsJson = appConfig.getString(RECENT_CONFIGS_KEY, null);
            if (configsJson != null && !configsJson.isEmpty()) {
                // Here you'd use a JSON parser to convert the JSON string to ConfigEntry objects
                // For simplicity, I'll just add a couple of mock entries
                // In a real implementation, you'd use Jackson or Gson to parse the JSON
                // JsonNode rootNode = new ObjectMapper().readTree(configsJson);
                // rootNode.forEach(node -> { ... });

                // Mock data for demonstration
                for (int i = 1; i <= 3; i++) {
                    Path projectConfigPath = Paths.get("config", "project" + i + ".json");
                    Path dslConfigPath = Paths.get("config", "dsl" + i + ".json");
                    Path imagePath = Paths.get(properties.getImagePath());

                    ConfigEntry entry = new ConfigEntry(
                            "Project " + i,
                            "Demo Project " + i,
                            projectConfigPath,
                            dslConfigPath,
                            imagePath,
                            LocalDateTime.now().minusDays(i)
                    );

                    recentConfigs.add(entry);
                }
            }

            // Update table
            refreshTable();

        } catch (Exception e) {
            logger.error("Error loading recent configurations", e);
            showAlert(Alert.AlertType.ERROR,
                    "Load Error",
                    "Error loading recent configurations",
                    e.getMessage());
        }
    }

    private void saveRecentConfigurations() {
        try {
            // Limit to MAX_RECENT_CONFIGS
            while (recentConfigs.size() > MAX_RECENT_CONFIGS) {
                recentConfigs.removeLast();
            }

            // Convert to JSON
            // In a real implementation, you'd use Jackson or Gson to convert to JSON
            // String json = new ObjectMapper().writeValueAsString(recentConfigs);

            // For now, we'll just save a placeholder
            String json = "{}"; // Placeholder

            // Save to app config
            appConfig.setString(RECENT_CONFIGS_KEY, json);

        } catch (Exception e) {
            logger.error("Error saving recent configurations", e);
        }
    }

    void refreshRecentConfigurations() {
        loadRecentConfigurations();
    }

    private void refreshTable() {
        Platform.runLater(() -> {
            recentConfigsTable.setItems(javafx.collections.FXCollections.observableArrayList(recentConfigs));
        });
    }

    void addRecentConfiguration(ConfigEntry entry) {
        // Remove if already exists
        recentConfigs.removeIf(c ->
                c.getProjectConfigPath().equals(entry.getProjectConfigPath()) &&
                        c.getDslConfigPath().equals(entry.getDslConfigPath()));

        // Add to beginning of list
        recentConfigs.addFirst(entry);

        // Save recent configs
        saveRecentConfigurations();

        // Refresh table
        refreshTable();
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