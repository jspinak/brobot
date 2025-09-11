package io.github.jspinak.brobot.runner.ui.config;

import java.io.File;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.config.ApplicationConfig;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.init.BrobotLibraryInitializer;
import io.github.jspinak.brobot.runner.ui.components.base.AtlantaCard;
import io.github.jspinak.brobot.runner.ui.config.AtlantaConfigPanel.ConfigEntry;
import io.github.jspinak.brobot.runner.ui.config.atlanta.services.*;

import lombok.extern.slf4j.Slf4j;

/**
 * Refactored modern configuration panel that delegates responsibilities to specialized services.
 */
@Slf4j
@Component
public class RefactoredAtlantaConfigPanel extends VBox {

    private final EventBus eventBus;
    private final BrobotRunnerProperties runnerProperties;
    private final BrobotLibraryInitializer libraryInitializer;
    private final ApplicationConfig appConfig;

    // Services
    private final ConfigTableService tableService;
    private final ConfigDetailsPanelService detailsService;
    private final ConfigFileOperationsService fileOperationsService;
    private final ConfigOperationsService operationsService;
    private final AtlantaConfigUIFactory uiFactory;

    // UI Components
    private TableView<ConfigEntry> configTable;
    private TextField searchField;
    private ComboBox<Integer> itemsPerPage;
    private Label configPathLabel;

    @Autowired
    public RefactoredAtlantaConfigPanel(
            EventBus eventBus,
            BrobotRunnerProperties runnerProperties,
            BrobotLibraryInitializer libraryInitializer,
            ApplicationConfig appConfig,
            ConfigTableService tableService,
            ConfigDetailsPanelService detailsService,
            ConfigFileOperationsService fileOperationsService,
            ConfigOperationsService operationsService,
            AtlantaConfigUIFactory uiFactory) {

        this.eventBus = eventBus;
        this.runnerProperties = runnerProperties;
        this.libraryInitializer = libraryInitializer;
        this.appConfig = appConfig;
        this.tableService = tableService;
        this.detailsService = detailsService;
        this.fileOperationsService = fileOperationsService;
        this.operationsService = operationsService;
        this.uiFactory = uiFactory;

        getStyleClass().add("configuration-panel");

        // Initialize UI
        initializeUI();

        // Load initial data
        loadRecentConfigurations();
    }

    /** Initializes the UI components. */
    private void initializeUI() {
        // Create main content
        VBox mainContent = new VBox();
        mainContent.getChildren().addAll(createActionBar(), createSplitLayout());

        getChildren().add(mainContent);
        VBox.setVgrow(mainContent, Priority.ALWAYS);

        // Set up service handlers
        setupServiceHandlers();
    }

    /** Creates the action bar with primary actions. */
    private HBox createActionBar() {
        HBox actionBar = uiFactory.createActionBar();

        // Create components
        AtlantaConfigUIFactory.ActionBarComponents components =
                uiFactory.createActionBarComponents(fileOperationsService.getConfigPath());

        // Store config path label for updates
        configPathLabel = components.getConfigPathLabel();

        // Set up action handlers
        components.getNewConfigBtn().setOnAction(e -> createNewConfiguration());
        components.getImportBtn().setOnAction(e -> importConfiguration());
        components.getRefreshBtn().setOnAction(e -> loadRecentConfigurations());
        components.getChangePathBtn().setOnAction(e -> changeConfigPath());
        components.getOpenFolderBtn().setOnAction(e -> fileOperationsService.openConfigFolder());
        components.getImportConfigBtn().setOnAction(e -> importSelectedConfiguration());

        // Add components to action bar
        actionBar
                .getChildren()
                .addAll(
                        components.getNewConfigBtn(),
                        components.getImportBtn(),
                        components.getRefreshBtn(),
                        components.getConfigPathLabel(),
                        components.getChangePathBtn(),
                        components.getOpenFolderBtn(),
                        uiFactory.createSpacer(),
                        components.getImportConfigBtn());

        return actionBar;
    }

    /** Creates the split layout with configurations table and details. */
    private HBox createSplitLayout() {
        HBox splitLayout = uiFactory.createSplitLayout();

        // Left: Recent Configurations
        AtlantaCard configurationsCard =
                uiFactory.createCard("Recent Configurations", 600, "recent-configurations-card");

        VBox tableContent = uiFactory.createTableContent();
        tableContent.getChildren().addAll(createSearchBar(), createConfigurationsTable());

        configurationsCard.setContent(tableContent);

        // Right: Configuration Details
        AtlantaCard detailsCard =
                uiFactory.createCard("Configuration Details", 500, "configuration-details-card");

        detailsCard.setContent(detailsService.createDetailsContent());

        splitLayout.getChildren().addAll(configurationsCard, detailsCard);

        return splitLayout;
    }

    /** Creates the search bar for filtering configurations. */
    private HBox createSearchBar() {
        searchField = uiFactory.createSearchField();
        itemsPerPage = uiFactory.createItemsPerPageCombo();

        // Set up listeners
        searchField
                .textProperty()
                .addListener((obs, oldVal, newVal) -> tableService.applyFilter(newVal));

        itemsPerPage
                .valueProperty()
                .addListener((obs, oldVal, newVal) -> tableService.setItemsPerPage(newVal));

        return uiFactory.createSearchBar(searchField, itemsPerPage);
    }

    /** Creates the configurations table. */
    private TableView<ConfigEntry> createConfigurationsTable() {
        configTable = tableService.createConfigurationsTable();

        // Add selection listener
        configTable
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (obs, oldSelection, newSelection) -> {
                            detailsService.updateDetails(newSelection);
                        });

        return configTable;
    }

    /** Sets up service handlers. */
    private void setupServiceHandlers() {
        // Table service handlers
        tableService.setLoadHandler(this::loadConfiguration);
        tableService.setDeleteHandler(this::deleteConfiguration);
    }

    /** Creates a new configuration. */
    private void createNewConfiguration() {
        eventBus.publish(LogEvent.info(this, "Creating new configuration", "Config"));
        // Implementation would show a dialog to create a new configuration
        // For now, just create a dummy entry
        ConfigEntry newEntry =
                operationsService.createConfiguration(
                        "New Config", "New Project", fileOperationsService.getConfigPath());

        if (newEntry != null) {
            tableService.addEntry(newEntry);
        }
    }

    /** Imports a configuration from file. */
    private void importConfiguration() {
        File file = fileOperationsService.showImportDialog(getScene().getWindow());
        if (file != null && fileOperationsService.validateConfigurationFile(file)) {
            ConfigEntry imported = operationsService.importConfiguration(file);
            if (imported != null) {
                tableService.addEntry(imported);
            }
        }
    }

    /** Loads recent configurations. */
    private void loadRecentConfigurations() {
        tableService.clearEntries();

        List<ConfigEntry> configurations = operationsService.loadRecentConfigurations();
        tableService.updateData(FXCollections.observableArrayList(configurations));
    }

    /** Changes the configuration path. */
    private void changeConfigPath() {
        File newPath = fileOperationsService.showChangePathDialog(getScene().getWindow());
        if (newPath != null) {
            fileOperationsService.updateConfigPath(newPath.getAbsolutePath());
            configPathLabel.setText("Config Path: " + newPath.getAbsolutePath());
            loadRecentConfigurations();
        }
    }

    /** Imports the selected configuration. */
    private void importSelectedConfiguration() {
        ConfigEntry selected = configTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            operationsService
                    .loadConfiguration(selected)
                    .thenAccept(
                            success -> {
                                if (success) {
                                    eventBus.publish(
                                            LogEvent.info(
                                                    this,
                                                    "Successfully imported configuration: "
                                                            + selected.getName(),
                                                    "Config"));
                                }
                            });
        }
    }

    /**
     * Loads a configuration.
     *
     * @param entry The configuration to load
     */
    private void loadConfiguration(ConfigEntry entry) {
        operationsService
                .loadConfiguration(entry)
                .thenAccept(
                        success -> {
                            if (success) {
                                log.info("Configuration loaded successfully: {}", entry.getName());
                            }
                        });
    }

    /**
     * Deletes a configuration.
     *
     * @param entry The configuration to delete
     */
    private void deleteConfiguration(ConfigEntry entry) {
        if (fileOperationsService.confirmDeletion(entry.getName())) {
            if (operationsService.deleteConfiguration(entry)) {
                tableService.removeEntry(entry);
            }
        }
    }
}
