package io.github.jspinak.brobot.runner.ui.config;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import io.github.jspinak.brobot.runner.config.ApplicationConfig;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.init.BrobotLibraryInitializer;
import io.github.jspinak.brobot.runner.ui.components.base.AtlantaBasePanel;
import io.github.jspinak.brobot.runner.ui.components.base.AtlantaCard;

import lombok.extern.slf4j.Slf4j;

/**
 * Improved configuration panel with better spacing and layout. Extends AtlantaBasePanel for
 * consistent styling.
 */
@Slf4j
public class ImprovedAtlantaConfigPanel extends AtlantaBasePanel {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final EventBus eventBus;
    private final BrobotRunnerProperties runnerProperties;
    private final BrobotLibraryInitializer libraryInitializer;
    private final ApplicationConfig appConfig;

    private TableView<ConfigEntry> configTable;
    private ObservableList<ConfigEntry> configData;
    private TextField searchField;

    // Configuration details fields
    private Label nameValue;
    private Label projectValue;
    private Label projectConfigValue;
    private Label dslConfigValue;
    private Label imagePathValue;
    private Label lastModifiedValue;
    private TextArea descriptionArea;
    private TextField authorField;

    // Config path display
    private Label configPathValue;

    public ImprovedAtlantaConfigPanel(
            EventBus eventBus,
            BrobotRunnerProperties runnerProperties,
            BrobotLibraryInitializer libraryInitializer,
            ApplicationConfig appConfig) {
        super("Configuration Management");

        this.eventBus = eventBus;
        this.runnerProperties = runnerProperties;
        this.libraryInitializer = libraryInitializer;
        this.appConfig = appConfig;

        getStyleClass().add("config-panel");

        initialize();
    }

    private void initialize() {
        // Set up action bar
        setupActionBar();

        // Add main content
        addContent(createMainContent());

        // Initialize data
        configData = FXCollections.observableArrayList();
        configTable.setItems(configData);

        // Load configurations
        loadRecentConfigurations();
    }

    /** Sets up the action bar with improved button grouping. */
    private void setupActionBar() {
        // Primary action buttons
        Button newConfigBtn = new Button("+ New Configuration");
        newConfigBtn.getStyleClass().addAll("button", "primary");
        newConfigBtn.setOnAction(e -> createNewConfiguration());

        Button importBtn = new Button("📁 Import");
        importBtn.getStyleClass().addAll("button", "secondary");
        importBtn.setOnAction(e -> importConfiguration());

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.getStyleClass().addAll("button", "secondary");
        refreshBtn.setOnAction(e -> loadRecentConfigurations());

        HBox primaryActions = createButtonGroup(newConfigBtn, importBtn, refreshBtn);

        // Config path section
        Label configPathLabel = new Label("Config Path:");
        configPathLabel.getStyleClass().add("config-path-label");

        configPathValue = new Label(runnerProperties.getConfigPath());
        configPathValue.getStyleClass().add("config-path-value");
        configPathValue.setMaxWidth(300);
        Tooltip.install(configPathValue, new Tooltip(runnerProperties.getConfigPath()));

        Button changePathBtn = new Button("Change");
        changePathBtn.getStyleClass().addAll("button", "secondary", "small");
        changePathBtn.setOnAction(e -> changeConfigPath());

        Button openFolderBtn = new Button("📂 Open");
        openFolderBtn.getStyleClass().addAll("button", "secondary", "small");
        openFolderBtn.setOnAction(e -> openConfigFolder());

        HBox configPathGroup =
                createButtonGroup(configPathLabel, configPathValue, changePathBtn, openFolderBtn);

        // Secondary actions
        Button exportBtn = new Button("Export Config");
        exportBtn.getStyleClass().addAll("button", "accent");
        exportBtn.setOnAction(e -> exportSelectedConfiguration());

        // Add all groups to action bar with proper spacing
        addToActionBar(
                primaryActions, createSeparator(), configPathGroup, createSpacer(), exportBtn);
    }

    /** Creates the main content area with improved responsive layout. */
    private Region createMainContent() {
        // Left side - Configuration list
        AtlantaCard listCard = new AtlantaCard("Recent Configurations");

        VBox listContent = new VBox(12);

        // Search bar
        searchField = new TextField();
        searchField.setPromptText("Search configurations...");
        searchField.getStyleClass().add("search-field");
        searchField.textProperty().addListener((obs, old, text) -> filterConfigurations(text));

        // Configuration table
        configTable = createConfigurationTable();
        VBox.setVgrow(configTable, Priority.ALWAYS);

        listContent.getChildren().addAll(searchField, configTable);
        listCard.setContent(listContent);

        // Right side - Configuration details
        AtlantaCard detailCard = new AtlantaCard("Configuration Details");
        detailCard.setExpand(true);

        VBox detailContent = createDetailContent();
        detailCard.setContent(detailContent);

        // Use responsive split layout from base class
        return createResponsiveSplitLayout(listCard, detailCard);
    }

    /** Creates the configuration table with proper styling. */
    private TableView<ConfigEntry> createConfigurationTable() {
        TableView<ConfigEntry> table = new TableView<>();
        table.getStyleClass().add("config-table");
        table.setPlaceholder(new Label("No configurations found"));

        // Name column
        TableColumn<ConfigEntry, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> data.getValue().nameProperty());
        nameCol.setPrefWidth(200);

        // Type column
        TableColumn<ConfigEntry, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> data.getValue().typeProperty());
        typeCol.setPrefWidth(100);

        // Modified column
        TableColumn<ConfigEntry, String> modifiedCol = new TableColumn<>("Modified");
        modifiedCol.setCellValueFactory(data -> data.getValue().modifiedProperty());
        modifiedCol.setPrefWidth(150);

        // Status column
        TableColumn<ConfigEntry, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> data.getValue().statusProperty());
        statusCol.setPrefWidth(100);

        table.getColumns().addAll(nameCol, typeCol, modifiedCol, statusCol);

        // Selection listener
        table.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (obs, oldVal, newVal) -> {
                            if (newVal != null) {
                                updateDetailView(newVal);
                            }
                        });

        return table;
    }

    /** Creates the detail content area. */
    private VBox createDetailContent() {
        VBox content = new VBox(16);
        content.getStyleClass().add("detail-content");

        // Basic info section
        GridPane basicInfo = new GridPane();
        basicInfo.setHgap(12);
        basicInfo.setVgap(8);
        basicInfo.getStyleClass().add("info-grid");

        int row = 0;

        // Name
        basicInfo.add(createLabel("Name:"), 0, row);
        nameValue = createValueLabel("");
        basicInfo.add(nameValue, 1, row++);

        // Project
        basicInfo.add(createLabel("Project:"), 0, row);
        projectValue = createValueLabel("");
        basicInfo.add(projectValue, 1, row++);

        // Project Config
        basicInfo.add(createLabel("Project Config:"), 0, row);
        projectConfigValue = createValueLabel("");
        basicInfo.add(projectConfigValue, 1, row++);

        // DSL Config
        basicInfo.add(createLabel("DSL Config:"), 0, row);
        dslConfigValue = createValueLabel("");
        basicInfo.add(dslConfigValue, 1, row++);

        // Image Path
        basicInfo.add(createLabel("Image Path:"), 0, row);
        imagePathValue = createValueLabel("");
        basicInfo.add(imagePathValue, 1, row++);

        // Last Modified
        basicInfo.add(createLabel("Last Modified:"), 0, row);
        lastModifiedValue = createValueLabel("");
        basicInfo.add(lastModifiedValue, 1, row++);

        // Description section
        Label descLabel = new Label("Description:");
        descLabel.getStyleClass().add("section-label");

        descriptionArea = new TextArea();
        descriptionArea.setPrefRowCount(3);
        descriptionArea.setWrapText(true);
        descriptionArea.getStyleClass().add("description-area");

        // Author section
        HBox authorBox = new HBox(8);
        authorBox.setAlignment(Pos.CENTER_LEFT);
        Label authorLabel = new Label("Author:");
        authorLabel.getStyleClass().add("field-label");

        authorField = new TextField();
        authorField.getStyleClass().add("author-field");
        authorField.setPrefWidth(200);

        authorBox.getChildren().addAll(authorLabel, authorField);

        // Action buttons
        HBox actions = new HBox(8);
        actions.getStyleClass().add("detail-actions");
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button saveBtn = new Button("Save Changes");
        saveBtn.getStyleClass().addAll("button", "primary");
        saveBtn.setDisable(true);

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().addAll("button", "danger");

        actions.getChildren().addAll(saveBtn, deleteBtn);

        // Add all sections
        content.getChildren()
                .addAll(
                        basicInfo,
                        new Separator(),
                        descLabel,
                        descriptionArea,
                        authorBox,
                        new Region(), // spacer
                        actions);

        VBox.setVgrow(content.getChildren().get(content.getChildren().size() - 2), Priority.ALWAYS);

        return content;
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("field-label");
        return label;
    }

    private Label createValueLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("field-value");
        return label;
    }

    private void updateDetailView(ConfigEntry entry) {
        nameValue.setText(entry.getName());
        projectValue.setText("Default Project");
        projectConfigValue.setText("project-config.json");
        dslConfigValue.setText("dsl-config.yaml");
        imagePathValue.setText("/images/project/");
        lastModifiedValue.setText(entry.getModified());
        descriptionArea.setText("Configuration for " + entry.getName());
        authorField.setText("User");
    }

    private void filterConfigurations(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            configTable.setItems(configData);
        } else {
            ObservableList<ConfigEntry> filtered = FXCollections.observableArrayList();
            String lower = searchText.toLowerCase();
            for (ConfigEntry entry : configData) {
                if (entry.getName().toLowerCase().contains(lower)
                        || entry.getType().toLowerCase().contains(lower)) {
                    filtered.add(entry);
                }
            }
            configTable.setItems(filtered);
        }
    }

    private void loadRecentConfigurations() {
        configData.clear();

        // Add sample data
        configData.addAll(
                new ConfigEntry(
                        "production-config",
                        "Production",
                        LocalDateTime.now().minusDays(1),
                        "Active"),
                new ConfigEntry(
                        "staging-config", "Staging", LocalDateTime.now().minusDays(3), "Active"),
                new ConfigEntry(
                        "test-config", "Test", LocalDateTime.now().minusDays(7), "Inactive"),
                new ConfigEntry(
                        "dev-config", "Development", LocalDateTime.now().minusDays(14), "Active"),
                new ConfigEntry(
                        "backup-config", "Backup", LocalDateTime.now().minusDays(30), "Archived"));

        log.info("Loaded {} configurations", configData.size());
    }

    private void createNewConfiguration() {
        log.info("Creating new configuration");
        log.info("Creating new configuration");
    }

    private void importConfiguration() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Configuration");
        fileChooser
                .getExtensionFilters()
                .addAll(
                        new FileChooser.ExtensionFilter("JSON Files", "*.json"),
                        new FileChooser.ExtensionFilter("YAML Files", "*.yaml", "*.yml"),
                        new FileChooser.ExtensionFilter("All Files", "*.*"));

        File file = fileChooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            log.info("Importing configuration from: {}", file.getAbsolutePath());
            log.info("Importing configuration: {}", file.getName());
        }
    }

    private void changeConfigPath() {
        // Implementation for changing config path
        log.info("Changing configuration path");
    }

    private void openConfigFolder() {
        // Implementation for opening config folder
        log.info("Opening configuration folder: {}", runnerProperties.getConfigPath());
    }

    private void exportSelectedConfiguration() {
        ConfigEntry selected = configTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            log.info("Exporting configuration: {}", selected.getName());
            log.info("Exporting configuration: {}", selected.getName());
        }
    }

    /** Configuration entry model for the table. */
    public static class ConfigEntry {
        private final SimpleStringProperty name;
        private final SimpleStringProperty type;
        private final SimpleStringProperty modified;
        private final SimpleStringProperty status;

        public ConfigEntry(String name, String type, LocalDateTime modified, String status) {
            this.name = new SimpleStringProperty(name);
            this.type = new SimpleStringProperty(type);
            this.modified = new SimpleStringProperty(modified.format(DATE_FORMATTER));
            this.status = new SimpleStringProperty(status);
        }

        public String getName() {
            return name.get();
        }

        public SimpleStringProperty nameProperty() {
            return name;
        }

        public String getType() {
            return type.get();
        }

        public SimpleStringProperty typeProperty() {
            return type;
        }

        public String getModified() {
            return modified.get();
        }

        public SimpleStringProperty modifiedProperty() {
            return modified;
        }

        public String getStatus() {
            return status.get();
        }

        public SimpleStringProperty statusProperty() {
            return status;
        }
    }
}
