package io.github.jspinak.brobot.runner.ui.config;

import io.github.jspinak.brobot.runner.config.ApplicationConfig;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.init.BrobotLibraryInitializer;
import io.github.jspinak.brobot.runner.ui.components.base.AtlantaCard;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;
import io.github.jspinak.brobot.runner.ui.components.BrobotButton;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern configuration panel styled according to AtlantaFX design principles.
 * Provides a clean, card-based interface for managing configurations.
 */
@Slf4j
public class AtlantaConfigPanel extends VBox {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    private final EventBus eventBus;
    private final BrobotRunnerProperties runnerProperties;
    private final BrobotLibraryInitializer libraryInitializer;
    private final ApplicationConfig appConfig;
    
    private TableView<ConfigEntry> configTable;
    private ObservableList<ConfigEntry> configData;
    
    // Configuration details fields
    private Label nameValue;
    private Label projectValue;
    private Label projectConfigValue;
    private Label dslConfigValue;
    private Label imagePathValue;
    private Label lastModifiedValue;
    private TextArea descriptionArea;
    private TextField authorField;
    
    public AtlantaConfigPanel(EventBus eventBus,
                              BrobotRunnerProperties runnerProperties,
                              BrobotLibraryInitializer libraryInitializer,
                              ApplicationConfig appConfig) {
        this.eventBus = eventBus;
        this.runnerProperties = runnerProperties;
        this.libraryInitializer = libraryInitializer;
        this.appConfig = appConfig;
        
        getStyleClass().add("configuration-panel");
        
        // Create main content
        VBox mainContent = new VBox();
        mainContent.getChildren().addAll(
            createActionBar(),
            createSplitLayout()
        );
        
        getChildren().add(mainContent);
        VBox.setVgrow(mainContent, Priority.ALWAYS);
        
        // Initialize with empty data
        configData = FXCollections.observableArrayList();
        configTable.setItems(configData);
        
        // Load configurations
        loadRecentConfigurations();
    }
    
    /**
     * Creates the action bar with primary actions.
     */
    private HBox createActionBar() {
        HBox actionBar = new HBox(8);
        actionBar.getStyleClass().add("action-bar");
        actionBar.setAlignment(Pos.CENTER_LEFT);
        
        // Primary actions
        BrobotButton newConfigBtn = BrobotButton.primary("+ New Configuration");
        newConfigBtn.setOnAction(e -> createNewConfiguration());
        
        BrobotButton importBtn = BrobotButton.secondary("📁 Import");
        importBtn.setOnAction(e -> importConfiguration());
        
        BrobotButton refreshBtn = BrobotButton.secondary("🔄 Refresh");
        refreshBtn.setOnAction(e -> loadRecentConfigurations());
        
        // Config path section
        Label configPathLabel = new Label("Config Path: " + runnerProperties.getConfigPath());
        configPathLabel.getStyleClass().addAll("button", "secondary");
        
        BrobotButton changePathBtn = BrobotButton.secondary("🔧 Change...");
        changePathBtn.setOnAction(e -> changeConfigPath());
        
        BrobotButton openFolderBtn = BrobotButton.secondary("📂 Open Folder");
        openFolderBtn.setOnAction(e -> openConfigFolder());
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Right-aligned import button
        Button importConfigBtn = new Button("Import Config");
        importConfigBtn.getStyleClass().addAll("button", "primary");
        importConfigBtn.setOnAction(e -> importSelectedConfiguration());
        
        actionBar.getChildren().addAll(
            newConfigBtn, importBtn, refreshBtn,
            configPathLabel, changePathBtn, openFolderBtn,
            spacer, importConfigBtn
        );
        
        return actionBar;
    }
    
    /**
     * Creates the split layout with configurations table and details.
     */
    private HBox createSplitLayout() {
        HBox splitLayout = new HBox(24);
        splitLayout.getStyleClass().add("split-layout");
        
        // Left: Recent Configurations
        AtlantaCard configurationsCard = new AtlantaCard("Recent Configurations");
        configurationsCard.getStyleClass().add("recent-configurations-card");
        configurationsCard.setMinWidth(600);
        configurationsCard.setExpand(true);
        
        VBox tableContent = new VBox(16);
        tableContent.getChildren().addAll(
            createSearchBar(),
            createConfigurationsTable()
        );
        
        configurationsCard.setContent(tableContent);
        
        // Right: Configuration Details
        AtlantaCard detailsCard = new AtlantaCard("Configuration Details");
        detailsCard.getStyleClass().add("configuration-details-card");
        detailsCard.setMinWidth(500);
        detailsCard.setExpand(true);
        
        detailsCard.setContent(createDetailsContent());
        
        splitLayout.getChildren().addAll(configurationsCard, detailsCard);
        HBox.setHgrow(configurationsCard, Priority.ALWAYS);
        HBox.setHgrow(detailsCard, Priority.ALWAYS);
        
        return splitLayout;
    }
    
    /**
     * Creates the search bar for filtering configurations.
     */
    private HBox createSearchBar() {
        HBox searchBar = new HBox(12);
        searchBar.getStyleClass().add("search-bar");
        searchBar.setAlignment(Pos.CENTER_LEFT);
        
        TextField searchField = new TextField();
        searchField.getStyleClass().add("search-input");
        searchField.setPromptText("Search configurations...");
        searchField.setPrefWidth(300);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        
        Label itemsLabel = new Label("Items per page:");
        itemsLabel.getStyleClass().add("form-label");
        
        ComboBox<Integer> itemsPerPage = new ComboBox<>();
        itemsPerPage.getItems().addAll(25, 50, 100);
        itemsPerPage.setValue(25);
        itemsPerPage.getStyleClass().add("select");
        
        searchBar.getChildren().addAll(searchField, itemsLabel, itemsPerPage);
        
        return searchBar;
    }
    
    /**
     * Creates the configurations table.
     */
    private TableView<ConfigEntry> createConfigurationsTable() {
        configTable = new TableView<>();
        configTable.getStyleClass().add("table");
        configTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Name column
        TableColumn<ConfigEntry, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        
        // Project column
        TableColumn<ConfigEntry, String> projectCol = new TableColumn<>("Project");
        projectCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProject()));
        
        // Last Modified column
        TableColumn<ConfigEntry, String> modifiedCol = new TableColumn<>("Last Modified");
        modifiedCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getLastModified().format(DATE_FORMATTER)));
        
        // Path column
        TableColumn<ConfigEntry, String> pathCol = new TableColumn<>("Path");
        pathCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPath()));
        
        // Actions column
        TableColumn<ConfigEntry, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button loadBtn = new Button("Load");
            private final Button deleteBtn = new Button("Delete");
            
            {
                loadBtn.getStyleClass().addAll("button", "small", "accent");
                deleteBtn.getStyleClass().addAll("button", "small", "danger");
                
                loadBtn.setOnAction(e -> {
                    ConfigEntry entry = getTableView().getItems().get(getIndex());
                    loadConfiguration(entry);
                });
                
                deleteBtn.setOnAction(e -> {
                    ConfigEntry entry = getTableView().getItems().get(getIndex());
                    deleteConfiguration(entry);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(8, loadBtn, deleteBtn);
                    buttons.getStyleClass().add("action-buttons");
                    setGraphic(buttons);
                }
            }
        });
        
        configTable.getColumns().addAll(nameCol, projectCol, modifiedCol, pathCol, actionsCol);
        
        // Add placeholder for empty table
        Label placeholder = new Label("No configurations found");
        placeholder.getStyleClass().addAll("empty-state-title");
        configTable.setPlaceholder(placeholder);
        
        // Selection listener
        configTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                updateDetailsPanel(newSelection);
            } else {
                clearDetailsPanel();
            }
        });
        
        return configTable;
    }
    
    /**
     * Creates the configuration details content.
     */
    private VBox createDetailsContent() {
        VBox detailsContent = new VBox(16);
        detailsContent.getStyleClass().add("configuration-details");
        
        // Create detail fields
        nameValue = createDetailValue();
        projectValue = createDetailValue();
        projectConfigValue = createDetailValue();
        dslConfigValue = createDetailValue();
        imagePathValue = createDetailValue();
        lastModifiedValue = createDetailValue();
        
        detailsContent.getChildren().addAll(
            createDetailRow("Name:", nameValue),
            createDetailRow("Project:", projectValue),
            createDetailRow("Project Config:", projectConfigValue),
            createDetailRow("DSL Config:", dslConfigValue),
            createDetailRow("Image Path:", imagePathValue),
            createDetailRow("Last Modified:", lastModifiedValue)
        );
        
        // Metadata section
        VBox metadataSection = new VBox(16);
        metadataSection.getStyleClass().add("metadata-section");
        
        Label metadataTitle = new Label("Configuration Metadata");
        metadataTitle.getStyleClass().add("metadata-title");
        
        VBox descriptionRow = createDetailRow("Description:", null);
        descriptionArea = new TextArea();
        descriptionArea.getStyleClass().add("metadata-input");
        descriptionArea.setPromptText("No description available");
        descriptionArea.setPrefRowCount(3);
        descriptionRow.getChildren().add(descriptionArea);
        
        VBox authorRow = createDetailRow("Author:", null);
        authorField = new TextField();
        authorField.getStyleClass().add("form-control");
        authorField.setPromptText("—");
        authorRow.getChildren().add(authorField);
        
        metadataSection.getChildren().addAll(metadataTitle, descriptionRow, authorRow);
        
        detailsContent.getChildren().add(metadataSection);
        
        return detailsContent;
    }
    
    /**
     * Creates a detail row with label and value.
     */
    private VBox createDetailRow(String label, Label value) {
        VBox row = new VBox(4);
        row.getStyleClass().add("detail-row");
        
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("form-label");
        row.getChildren().add(labelNode);
        
        if (value != null) {
            row.getChildren().add(value);
        }
        
        return row;
    }
    
    /**
     * Creates a styled detail value label.
     */
    private Label createDetailValue() {
        Label value = new Label("—");
        value.getStyleClass().add("detail-value");
        return value;
    }
    
    /**
     * Updates the details panel with the selected configuration.
     */
    private void updateDetailsPanel(ConfigEntry entry) {
        nameValue.setText(entry.getName());
        projectValue.setText(entry.getProject());
        projectConfigValue.setText(entry.getProjectConfig());
        dslConfigValue.setText(entry.getDslConfig());
        imagePathValue.setText(entry.getImagePath());
        lastModifiedValue.setText(entry.getLastModified().format(DATE_FORMATTER));
        
        // Load metadata if available
        descriptionArea.setText(entry.getDescription() != null ? entry.getDescription() : "");
        authorField.setText(entry.getAuthor() != null ? entry.getAuthor() : "");
    }
    
    /**
     * Clears the details panel.
     */
    private void clearDetailsPanel() {
        nameValue.setText("—");
        projectValue.setText("—");
        projectConfigValue.setText("—");
        dslConfigValue.setText("—");
        imagePathValue.setText("—");
        lastModifiedValue.setText("—");
        descriptionArea.clear();
        authorField.clear();
    }
    
    // Action methods
    
    private void createNewConfiguration() {
        eventBus.publish(LogEvent.info(this, "Creating new configuration", "Config"));
        // Implementation would show a dialog to create a new configuration
    }
    
    private void importConfiguration() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Configuration");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Configuration Files", "*.json", "*.yml", "*.yaml")
        );
        
        File file = fileChooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            eventBus.publish(LogEvent.info(this, "Importing configuration: " + file.getName(), "Config"));
            // Implementation would import the configuration
        }
    }
    
    private void loadRecentConfigurations() {
        // Clear and reload configurations
        configData.clear();
        
        // This would load from actual storage
        // For now, just publish an event
        eventBus.publish(LogEvent.info(this, "Refreshing configurations", "Config"));
    }
    
    private void changeConfigPath() {
        // Implementation would show a directory chooser
        eventBus.publish(LogEvent.info(this, "Changing configuration path", "Config"));
    }
    
    private void openConfigFolder() {
        // Implementation would open the file explorer
        eventBus.publish(LogEvent.info(this, "Opening configuration folder", "Config"));
    }
    
    private void importSelectedConfiguration() {
        ConfigEntry selected = configTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            eventBus.publish(LogEvent.info(this, "Importing configuration: " + selected.getName(), "Config"));
            // Implementation would import the selected configuration
        }
    }
    
    private void loadConfiguration(ConfigEntry entry) {
        eventBus.publish(LogEvent.info(this, "Loading configuration: " + entry.getName(), "Config"));
        // Implementation would load the configuration
    }
    
    private void deleteConfiguration(ConfigEntry entry) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Configuration");
        confirm.setHeaderText("Delete configuration: " + entry.getName() + "?");
        confirm.setContentText("This action cannot be undone.");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                configData.remove(entry);
                eventBus.publish(LogEvent.info(this, "Deleted configuration: " + entry.getName(), "Config"));
            }
        });
    }
    
    /**
     * Configuration entry model class.
     */
    public static class ConfigEntry {
        private final String name;
        private final String project;
        private final String projectConfig;
        private final String dslConfig;
        private final String imagePath;
        private final String path;
        private final LocalDateTime lastModified;
        private String description;
        private String author;
        
        public ConfigEntry(String name, String project, String path) {
            this.name = name;
            this.project = project;
            this.path = path;
            this.projectConfig = path + "/project.config";
            this.dslConfig = path + "/dsl.config";
            this.imagePath = path + "/images";
            this.lastModified = LocalDateTime.now();
        }
        
        // Getters
        public String getName() { return name; }
        public String getProject() { return project; }
        public String getProjectConfig() { return projectConfig; }
        public String getDslConfig() { return dslConfig; }
        public String getImagePath() { return imagePath; }
        public String getPath() { return path; }
        public LocalDateTime getLastModified() { return lastModified; }
        public String getDescription() { return description; }
        public String getAuthor() { return author; }
        
        // Setters
        public void setDescription(String description) { this.description = description; }
        public void setAuthor(String author) { this.author = author; }
    }
}