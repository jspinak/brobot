package io.github.jspinak.brobot.runner.ui.log;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.function.Predicate;
import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.persistence.LogQueryService;
import io.github.jspinak.brobot.runner.ui.components.base.AtlantaCard;
import io.github.jspinak.brobot.runner.ui.log.models.LogEntry;
import io.github.jspinak.brobot.runner.ui.log.services.*;
import io.github.jspinak.brobot.tools.logging.model.LogData;
import io.github.jspinak.brobot.tools.logging.model.LogEventType;

import lombok.extern.slf4j.Slf4j;

/**
 * Refactored logs panel using service-oriented architecture. Acts as a thin orchestrator
 * coordinating between specialized services.
 */
@Slf4j
@Component
public class RefactoredAtlantaLogsPanel extends VBox implements AutoCloseable {

    // Dependencies
    private final EventBus eventBus;
    private final LogQueryService logQueryService;
    private final LogExportService exportService;
    private final LogFilterService filterService;
    private final LogTableFactory tableFactory;
    private final LogEventAdapter eventAdapter;
    private final LogDataRepository dataRepository;
    private final LogViewStateManager viewStateManager;

    // UI Components
    private TableView<LogEntry> logTable;
    private TextArea logDetailArea;
    private TextField searchField;
    private ComboBox<String> levelFilter;
    private ComboBox<String> typeFilter;
    private FilteredList<LogEntry> filteredLogs;

    // Current filter criteria
    private LogFilterService.FilterCriteria currentCriteria =
            LogFilterService.FilterCriteria.builder().build();

    @Autowired
    public RefactoredAtlantaLogsPanel(
            EventBus eventBus,
            LogQueryService logQueryService,
            LogExportService exportService,
            LogFilterService filterService,
            LogTableFactory tableFactory,
            LogEventAdapter eventAdapter,
            LogDataRepository dataRepository,
            LogViewStateManager viewStateManager) {

        this.eventBus = eventBus;
        this.logQueryService = logQueryService;
        this.exportService = exportService;
        this.filterService = filterService;
        this.tableFactory = tableFactory;
        this.eventAdapter = eventAdapter;
        this.dataRepository = dataRepository;
        this.viewStateManager = viewStateManager;

        getStyleClass().add("logs-panel");

        initialize();
        setupEventHandling();
        loadInitialLogs();
    }

    /** Initializes the UI components. */
    private void initialize() {
        // Create filtered list - manual filtering since service uses different API
        filteredLogs = new FilteredList<>(dataRepository.getLogEntries());
        updateFilter();

        // Build UI
        VBox mainContent = new VBox();
        mainContent.getChildren().addAll(createFilterBar(), createMainContent());

        getChildren().add(mainContent);
        VBox.setVgrow(mainContent, Priority.ALWAYS);
    }

    /** Updates the filter predicate based on current criteria. */
    private void updateFilter() {
        Predicate<LogEntry> predicate = filterService.createFilter(currentCriteria);
        filteredLogs.setPredicate(predicate);
    }

    /** Creates the filter bar. */
    private HBox createFilterBar() {
        HBox filterBar = new HBox(16);
        filterBar.getStyleClass().add("action-bar");
        filterBar.setAlignment(Pos.CENTER_LEFT);

        // Search field
        searchField = new TextField();
        searchField.setPromptText("Search logs...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(300);
        searchField
                .textProperty()
                .addListener(
                        (obs, old, text) -> {
                            currentCriteria =
                                    LogFilterService.FilterCriteria.builder()
                                            .searchText(text)
                                            .logType(typeFilter.getValue())
                                            .build();
                            updateFilter();
                        });

        // Level filter
        Label levelLabel = new Label("Level:");
        levelFilter = new ComboBox<>();
        levelFilter.getItems().addAll("All Levels", "ERROR", "WARNING", "INFO", "DEBUG");
        levelFilter.setValue("All Levels");
        levelFilter.getStyleClass().add("filter-combo");
        levelFilter.setOnAction(
                e -> {
                    String level = levelFilter.getValue();
                    LogEntry.LogLevel minLevel = null;
                    if (!"All Levels".equals(level)) {
                        try {
                            minLevel = LogEntry.LogLevel.valueOf(level);
                        } catch (Exception ex) {
                            // ignore
                        }
                    }
                    currentCriteria =
                            LogFilterService.FilterCriteria.builder()
                                    .searchText(searchField.getText())
                                    .logType(typeFilter.getValue())
                                    .minLevel(minLevel)
                                    .build();
                    updateFilter();
                });

        // Type filter
        Label typeLabel = new Label("Type:");
        typeFilter = new ComboBox<>();
        typeFilter.getItems().add("All Types");
        for (LogEventType type : LogEventType.values()) {
            typeFilter.getItems().add(type.name());
        }
        typeFilter.setValue("All Types");
        typeFilter.getStyleClass().add("filter-combo");
        typeFilter.setOnAction(
                e -> {
                    String type = typeFilter.getValue();
                    currentCriteria =
                            LogFilterService.FilterCriteria.builder()
                                    .searchText(searchField.getText())
                                    .logType("All Types".equals(type) ? null : type)
                                    .build();
                    updateFilter();
                });

        // Auto-scroll
        CheckBox autoScrollCheck = new CheckBox("Auto-scroll");
        autoScrollCheck.selectedProperty().bindBidirectional(viewStateManager.autoScrollProperty());
        autoScrollCheck.getStyleClass().add("auto-scroll-check");

        // Action buttons
        Button clearFiltersButton = new Button("Clear Filters");
        clearFiltersButton.getStyleClass().addAll("button", "secondary", "small");
        clearFiltersButton.setOnAction(
                e -> {
                    searchField.clear();
                    levelFilter.setValue("All Levels");
                    typeFilter.setValue("All Types");
                    currentCriteria = LogFilterService.FilterCriteria.builder().build();
                    updateFilter();
                });

        Button exportButton = new Button("Export");
        exportButton.getStyleClass().addAll("button", "secondary", "small");
        exportButton.setOnAction(e -> exportLogs());

        Button clearLogsButton = new Button("Clear");
        clearLogsButton.getStyleClass().addAll("button", "danger", "small");
        clearLogsButton.setOnAction(e -> clearLogs());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Status
        Label statusLabel = new Label();
        statusLabel.textProperty().bind(viewStateManager.statusProperty());
        statusLabel.getStyleClass().add("status-label");

        // Loading indicator
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setPrefSize(16, 16);
        loadingIndicator.visibleProperty().bind(viewStateManager.loadingProperty());

        filterBar
                .getChildren()
                .addAll(
                        searchField,
                        new Separator(javafx.geometry.Orientation.VERTICAL),
                        levelLabel,
                        levelFilter,
                        typeLabel,
                        typeFilter,
                        new Separator(javafx.geometry.Orientation.VERTICAL),
                        autoScrollCheck,
                        clearFiltersButton,
                        exportButton,
                        clearLogsButton,
                        spacer,
                        statusLabel,
                        loadingIndicator);

        return filterBar;
    }

    /** Creates the main content area. */
    private HBox createMainContent() {
        HBox content = new HBox(24);
        content.getStyleClass().add("split-layout");

        // Left: Log table
        AtlantaCard tableCard = new AtlantaCard("Log Entries");
        tableCard.setExpand(true);

        // Create table with configuration
        LogTableFactory.TableConfiguration tableConfig =
                LogTableFactory.TableConfiguration.builder()
                        .enableRowStyling(true)
                        .enableTooltips(true)
                        .enableLevelIndicators(true)
                        .selectionHandler(
                                entry -> viewStateManager.selectedEntryProperty().set(entry))
                        .build();

        logTable = tableFactory.createLogTable(tableConfig);
        logTable.setItems(filteredLogs);

        // Setup context menu
        logTable.setContextMenu(tableFactory.createTableContextMenu(logTable));

        tableCard.setContent(logTable);

        // Right: Log details
        AtlantaCard detailCard = new AtlantaCard("Log Details");
        detailCard.setMinWidth(400);
        detailCard.setPrefWidth(500);

        logDetailArea = new TextArea();
        logDetailArea.setEditable(false);
        logDetailArea.wrapTextProperty().bind(viewStateManager.wrapTextProperty());
        logDetailArea.getStyleClass().add("log-detail-area");
        logDetailArea.setPromptText("Select a log entry to view details");

        detailCard.setContent(logDetailArea);

        // Bind detail display to selection
        viewStateManager
                .selectedEntryProperty()
                .addListener(
                        (obs, oldVal, newVal) -> {
                            if (newVal != null) {
                                logDetailArea.setText(viewStateManager.formatDetails(newVal));
                            } else {
                                logDetailArea.clear();
                            }
                        });

        content.getChildren().addAll(tableCard, detailCard);
        HBox.setHgrow(tableCard, Priority.ALWAYS);

        return content;
    }

    /** Sets up event handling. */
    private void setupEventHandling() {
        // Subscribe to log events
        eventAdapter.subscribe(eventBus, this::addLogEntry);

        // Update status when data changes
        filteredLogs.addListener(
                (javafx.collections.ListChangeListener<LogEntry>)
                        c ->
                                viewStateManager.updateStatus(
                                        dataRepository.size(), filteredLogs.size()));

        // Auto-scroll handling
        viewStateManager
                .autoScrollProperty()
                .addListener(
                        (obs, oldVal, newVal) -> {
                            if (newVal && filteredLogs.size() > 0) {
                                logTable.scrollTo(0);
                            }
                        });
    }

    /** Loads initial logs from the query service. */
    private void loadInitialLogs() {
        viewStateManager.setLoading(true);

        Task<List<LogData>> loadTask =
                new Task<>() {
                    @Override
                    protected List<LogData> call() throws Exception {
                        return logQueryService.getRecentLogs(200);
                    }
                };

        loadTask.setOnSucceeded(
                e -> {
                    List<LogData> logData = loadTask.getValue();
                    List<LogEntry> entries = logData.stream().map(this::convertLogData).toList();

                    dataRepository.addLogEntries(entries);
                    viewStateManager.setLoading(false);
                    viewStateManager.updateStatus(entries.size(), filteredLogs.size());

                    log.info("Loaded {} initial log entries", entries.size());
                });

        loadTask.setOnFailed(
                e -> {
                    Throwable error = loadTask.getException();
                    log.error("Failed to load initial logs", error);
                    viewStateManager.setLoading(false);
                    viewStateManager.updateStatus("Failed to load logs");
                });

        new Thread(loadTask).start();
    }

    /** Converts LogData to LogEntry. */
    private LogEntry convertLogData(LogData logData) {
        LocalDateTime timestamp =
                LocalDateTime.ofInstant(logData.getTimestamp(), ZoneId.systemDefault());

        LogEntry.LogLevel level;
        if (logData.getType() == LogEventType.ERROR) {
            level = LogEntry.LogLevel.ERROR;
        } else if (!logData.isSuccess()) {
            level = LogEntry.LogLevel.WARNING;
        } else {
            level = LogEntry.LogLevel.INFO;
        }

        return LogEntry.builder()
                .id("LOG-" + System.nanoTime())
                .timestamp(timestamp)
                .type(logData.getType() != null ? logData.getType().toString() : "SYSTEM")
                .level(level)
                .message(logData.getDescription())
                .source(
                        logData.getCurrentStateName() != null
                                ? logData.getCurrentStateName()
                                : "System")
                .build();
    }

    /** Adds a new log entry. */
    private void addLogEntry(LogEntry entry) {
        dataRepository.addLogEntry(entry);

        // Auto-scroll if enabled
        if (viewStateManager.autoScrollProperty().get() && logTable.getItems().size() > 0) {
            Platform.runLater(() -> logTable.scrollTo(0));
        }
    }

    /** Clears all logs. */
    private void clearLogs() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Clear Logs");
        confirm.setHeaderText("Clear all log entries?");
        confirm.setContentText("This action cannot be undone.");

        confirm.showAndWait()
                .ifPresent(
                        response -> {
                            if (response == ButtonType.OK) {
                                dataRepository.clearLogs();
                                logDetailArea.clear();
                                viewStateManager.clearSelection();
                                log.info("Cleared all log entries");
                            }
                        });
    }

    /** Exports logs to file. */
    private void exportLogs() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Logs");
        fileChooser
                .getExtensionFilters()
                .addAll(
                        new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                        new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                        new FileChooser.ExtensionFilter("JSON Files", "*.json"),
                        new FileChooser.ExtensionFilter("HTML Files", "*.html"));

        String defaultFilename =
                exportService.getDefaultFilename(LogExportService.ExportFormat.TEXT);
        fileChooser.setInitialFileName(defaultFilename);

        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            LogExportService.ExportFormat format = determineFormat(file.getName());
            LogExportService.ExportOptions options =
                    LogExportService.ExportOptions.builder()
                            .includeHeaders(true)
                            .includeTimestamps(true)
                            .includeStackTraces(true)
                            .build();

            Task<Void> exportTask =
                    new Task<>() {
                        @Override
                        protected Void call() throws Exception {
                            exportService.exportLogs(filteredLogs, file.toPath(), format, options);
                            return null;
                        }
                    };

            exportTask.setOnSucceeded(
                    e -> {
                        showExportSuccess(file);
                        log.info("Exported {} logs to {}", filteredLogs.size(), file);
                    });

            exportTask.setOnFailed(
                    e -> {
                        showExportError(exportTask.getException());
                        log.error("Failed to export logs", exportTask.getException());
                    });

            new Thread(exportTask).start();
        }
    }

    /** Determines export format from filename. */
    private LogExportService.ExportFormat determineFormat(String filename) {
        if (filename.endsWith(".csv")) {
            return LogExportService.ExportFormat.CSV;
        } else if (filename.endsWith(".json")) {
            return LogExportService.ExportFormat.JSON;
        } else if (filename.endsWith(".html")) {
            return LogExportService.ExportFormat.HTML;
        } else {
            return LogExportService.ExportFormat.TEXT;
        }
    }

    private void showExportSuccess(File file) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export Successful");
        alert.setHeaderText("Logs exported successfully");
        alert.setContentText("File saved to: " + file.getAbsolutePath());
        alert.showAndWait();
    }

    private void showExportError(Throwable e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Export Failed");
        alert.setHeaderText("Failed to export logs");
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }

    @Override
    public void close() {
        eventAdapter.close();
        dataRepository.clearLogs();
        log.info("RefactoredAtlantaLogsPanel closed");
    }
}
