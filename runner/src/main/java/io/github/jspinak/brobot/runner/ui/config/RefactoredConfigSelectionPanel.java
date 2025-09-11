package io.github.jspinak.brobot.runner.ui.config;

import java.time.LocalDateTime;
import javafx.scene.layout.VBox;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.config.ApplicationConfig;
import io.github.jspinak.brobot.runner.config.BrobotRunnerProperties;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.init.BrobotLibraryInitializer;
import io.github.jspinak.brobot.runner.ui.config.services.*;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Refactored panel for selecting recent configurations and managing configuration history. Uses
 * service-oriented architecture to separate concerns and improve maintainability.
 */
@Slf4j
@Component
@Getter
public class RefactoredConfigSelectionPanel extends VBox {

    // Services
    private final ConfigHistoryService historyService;
    private final ConfigTableService tableService;
    private final ConfigLoadingService loadingService;
    private final ConfigImportService importService;
    private final ConfigSelectionUIFactory uiFactory;

    // Dependencies
    private final EventBus eventBus;
    private final BrobotRunnerProperties runnerProperties;
    private final BrobotLibraryInitializer libraryInitializer;
    private final ApplicationConfig appConfig;

    // UI Components
    private ConfigSelectionUIFactory.AssembledUI assembledUI;

    @Autowired
    public RefactoredConfigSelectionPanel(
            ConfigHistoryService historyService,
            ConfigTableService tableService,
            ConfigLoadingService loadingService,
            ConfigImportService importService,
            ConfigSelectionUIFactory uiFactory,
            EventBus eventBus,
            BrobotRunnerProperties runnerProperties,
            BrobotLibraryInitializer libraryInitializer,
            ApplicationConfig appConfig) {

        this.historyService = historyService;
        this.tableService = tableService;
        this.loadingService = loadingService;
        this.importService = importService;
        this.uiFactory = uiFactory;
        this.eventBus = eventBus;
        this.runnerProperties = runnerProperties;
        this.libraryInitializer = libraryInitializer;
        this.appConfig = appConfig;
    }

    @PostConstruct
    public void initialize() {
        log.info("Initializing Refactored Config Selection Panel");

        // Configure services
        configureServices();

        // Set up UI
        setupUI();

        // Load recent configurations
        refreshRecentConfigurations();
    }

    /** Configures all services with appropriate settings and handlers. */
    private void configureServices() {
        // Configure history service
        historyService.setConfiguration(
                ConfigHistoryService.HistoryConfiguration.builder()
                        .maxRecentConfigs(10)
                        .autoSave(true)
                        .moveToTopOnLoad(true)
                        .build());

        // Configure table service
        tableService.setConfiguration(
                ConfigTableService.TableConfiguration.builder()
                        .showActions(true)
                        .showPath(true)
                        .showTooltips(true)
                        .autoResize(true)
                        .build());

        tableService.setLoadHandler(this::loadConfiguration);
        tableService.setDeleteHandler(this::removeConfiguration);
        tableService.setSelectionHandler(this::showConfigDetails);

        // Configure loading service
        loadingService.setConfiguration(
                ConfigLoadingService.LoadingConfiguration.builder()
                        .showSuccessAlert(true)
                        .showErrorAlert(true)
                        .publishEvents(true)
                        .asyncLoading(false)
                        .build());

        loadingService.setLoadingCompleteHandler(
                result -> {
                    if (result.isSuccess() && result.getConfigEntry() != null) {
                        // Update history
                        result.getConfigEntry().setLastModified(LocalDateTime.now());
                        historyService.updateConfigurationAccess(result.getConfigEntry());

                        // Refresh table
                        refreshTable();
                    }
                });

        // Configure import service
        importService.setConfiguration(
                ConfigImportService.ImportConfiguration.builder()
                        .autoDetectDslConfig(true)
                        .showImportDialog(true)
                        .dslFilePatterns("dsl", "automation")
                        .defaultProjectName("Unknown")
                        .build());

        importService.setImportSuccessHandler(
                entry -> {
                    // Add to history
                    historyService.addRecentConfiguration(entry);

                    // Refresh table
                    refreshTable();

                    // Select the new entry
                    tableService.selectConfiguration(
                            assembledUI.getSplitPane().getConfigTable(), entry);

                    // Show details
                    showConfigDetails(entry);
                });

        // Configure UI factory
        uiFactory.setConfiguration(
                ConfigSelectionUIFactory.UIConfiguration.builder()
                        .panelPadding(15)
                        .panelSpacing(15)
                        .headerSpacing(10)
                        .splitPaneDividerPosition(0.45)
                        .recentConfigsMinWidth(300)
                        .detailsMinWidth(400)
                        .primaryButtonClass("button-primary")
                        .build());
    }

    /** Sets up the UI components and layout. */
    private void setupUI() {
        // Create main panel
        VBox mainPanel = uiFactory.createMainPanel();

        // Create header
        var header = uiFactory.createHeaderSection();

        // Create split pane
        var splitPane = uiFactory.createSplitPaneSection(eventBus);

        // Set up table
        tableService.setupTable(splitPane.getConfigTable());

        // Set up button actions
        header.getImportButton().setOnAction(e -> showImportDialog());
        header.getBrowseButton().setOnAction(e -> browseForConfiguration());
        header.getRefreshButton().setOnAction(e -> refreshRecentConfigurations());

        // Assemble UI
        assembledUI = uiFactory.assembleUI(mainPanel, header, splitPane);

        // Add to this panel
        getChildren().add(mainPanel);
    }

    /**
     * Gets the currently selected configuration from the table.
     *
     * @return The selected configuration entry or null if none is selected
     */
    public ConfigEntry getSelectedConfiguration() {
        return tableService.getSelectedConfiguration(assembledUI.getSplitPane().getConfigTable());
    }

    /** Shows the import dialog. */
    private void showImportDialog() {
        importService.showImportDialog(getScene().getWindow());
    }

    /** Browses for a configuration file. */
    private void browseForConfiguration() {
        importService.browseForConfiguration(getScene().getWindow());
    }

    /**
     * Shows configuration details in the details panel.
     *
     * @param entry The configuration to show
     */
    private void showConfigDetails(ConfigEntry entry) {
        if (entry != null) {
            assembledUI.getSplitPane().getDetailsPanel().setConfiguration(entry);
        }
    }

    /**
     * Loads a configuration.
     *
     * @param entry The configuration to load
     */
    private void loadConfiguration(ConfigEntry entry) {
        if (entry != null) {
            loadingService.loadConfiguration(entry);
        }
    }

    /**
     * Removes a configuration from the recent list.
     *
     * @param entry The configuration to remove
     */
    private void removeConfiguration(ConfigEntry entry) {
        if (entry != null) {
            loadingService
                    .confirmRemoval(entry)
                    .ifPresent(
                            confirmed -> {
                                if (confirmed) {
                                    // Remove from history
                                    historyService.removeConfiguration(entry);

                                    // Refresh table
                                    refreshTable();

                                    // Clear details if the removed entry was selected
                                    if (assembledUI
                                                    .getSplitPane()
                                                    .getDetailsPanel()
                                                    .getConfiguration()
                                            == entry) {
                                        assembledUI
                                                .getSplitPane()
                                                .getDetailsPanel()
                                                .clearConfiguration();
                                    }

                                    // Log removal
                                    String message =
                                            "Removed configuration from recent list: "
                                                    + entry.getName();
                                    eventBus.publish(LogEvent.info(this, message, "Configuration"));
                                }
                            });
        }
    }

    /** Refreshes the recent configurations list. */
    void refreshRecentConfigurations() {
        historyService.loadRecentConfigurations();
        refreshTable();
    }

    /** Refreshes the table with current data from history service. */
    private void refreshTable() {
        tableService.updateTableData(
                assembledUI.getSplitPane().getConfigTable(),
                historyService.getRecentConfigurations());
    }

    /**
     * Adds a configuration to the recent list.
     *
     * @param entry The configuration to add
     */
    void addRecentConfiguration(ConfigEntry entry) {
        historyService.addRecentConfiguration(entry);
        refreshTable();
    }
}
