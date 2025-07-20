package io.github.jspinak.brobot.runner.ui.log;

import atlantafx.base.theme.Styles;
import io.github.jspinak.brobot.runner.events.BrobotEvent;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.ui.components.base.BrobotPanel;
import io.github.jspinak.brobot.runner.ui.log.components.LogFilterPanel;
import io.github.jspinak.brobot.runner.ui.log.components.LogTableView;
import io.github.jspinak.brobot.runner.ui.log.models.LogEntry;
import io.github.jspinak.brobot.runner.ui.log.services.LogExportService;
import io.github.jspinak.brobot.runner.ui.log.services.LogFilterService;
import io.github.jspinak.brobot.runner.ui.log.services.LogParsingService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.scene.Node;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Refactored LogViewerPanel using extracted services.
 * Coordinates between services and manages the UI.
 */
@Slf4j
@Component
public class RefactoredLogViewerPanel extends BrobotPanel {
    
    private static final int MAX_LOG_ENTRIES = 10000;
    
    private final EventBus eventBus;
    private final LogParsingService parsingService;
    private final LogFilterService filterService;
    private final LogExportService exportService;
    
    // UI Components
    private LogTableView logTableView;
    private LogFilterPanel filterPanel;
    private Label statusLabel;
    private Label countLabel;
    private ProgressBar memoryBar;
    private Button exportButton;
    private Button clearButton;
    
    // State
    private final AtomicInteger totalEntries = new AtomicInteger(0);
    
    @Autowired
    public RefactoredLogViewerPanel(
            EventBus eventBus,
            LogParsingService parsingService,
            LogFilterService filterService,
            LogExportService exportService) {
        
        super();
        this.eventBus = eventBus;
        this.parsingService = parsingService;
        this.filterService = filterService;
        this.exportService = exportService;
    }
    
    @PostConstruct
    public void postConstruct() {
        // Re-initialize with dependencies available
        initialize();
        
        // Subscribe to all event types for logging
        for (BrobotEvent.EventType eventType : BrobotEvent.EventType.values()) {
            eventBus.subscribe(eventType, this::handleEvent);
        }
        
        // Add initial log entry if logTableView is ready
        if (logTableView != null) {
            LogEntry welcomeEntry = parsingService.createSimpleEntry(
                "Log viewer initialized", 
                LogEntry.LogLevel.INFO
            );
            addLogEntry(welcomeEntry);
        }
    }
    
    @PreDestroy
    public void preDestroy() {
        // Unsubscribe from all events
        for (BrobotEvent.EventType eventType : BrobotEvent.EventType.values()) {
            eventBus.unsubscribe(eventType, this::handleEvent);
        }
    }
    
    @Override
    protected void initialize() {
        // Skip initialization if dependencies not ready
        if (eventBus == null || parsingService == null || filterService == null || exportService == null) {
            return;
        }
        
        getStyleClass().add("log-viewer-panel");
        
        // Create main layout
        BorderPane mainLayout = new BorderPane();
        
        // Top: Title and controls
        mainLayout.setTop(createTopSection());
        
        // Center: Split pane with filters and logs
        mainLayout.setCenter(createCenterSection());
        
        // Bottom: Status bar
        mainLayout.setBottom(createStatusBar());
        
        getChildren().add(mainLayout);
        VBox.setVgrow(mainLayout, Priority.ALWAYS);
    }
    
    /**
     * Creates the top section with title and controls.
     */
    private Node createTopSection() {
        HBox topSection = new HBox(12);
        topSection.setAlignment(Pos.CENTER_LEFT);
        topSection.setPadding(new Insets(8));
        topSection.getStyleClass().add("log-viewer-header");
        
        // Title
        Label titleLabel = new Label("Log Viewer");
        titleLabel.getStyleClass().addAll(Styles.TITLE_3);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Export button
        exportButton = new Button("Export");
        exportButton.getStyleClass().add(Styles.BUTTON_OUTLINED);
        exportButton.setOnAction(e -> exportLogs());
        
        // Clear button
        clearButton = new Button("Clear");
        clearButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.DANGER);
        clearButton.setOnAction(e -> clearLogs());
        
        topSection.getChildren().addAll(titleLabel, spacer, exportButton, clearButton);
        return topSection;
    }
    
    /**
     * Creates the center section with filters and log table.
     */
    private Node createCenterSection() {
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        
        // Right: Log table - create first
        logTableView = new LogTableView();
        
        // Left: Filter panel
        filterPanel = new LogFilterPanel(filterService);
        filterPanel.setMinWidth(300);
        filterPanel.setPrefWidth(350);
        
        // Bind filter to table
        filterPanel.getFilterProperty().addListener((obs, old, filter) -> {
            if (logTableView != null) {
                logTableView.setFilter(filter);
                updateStatusBar();
            }
        });
        
        // Apply initial filters
        filterPanel.applyFilters();
        
        // Create scroll pane for filter panel
        ScrollPane filterScroll = new ScrollPane(filterPanel);
        filterScroll.setFitToWidth(true);
        filterScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        splitPane.getItems().addAll(filterScroll, logTableView);
        splitPane.setDividerPositions(0.25);
        
        return splitPane;
    }
    
    /**
     * Creates the status bar.
     */
    private Node createStatusBar() {
        HBox statusBar = new HBox(12);
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setPadding(new Insets(4, 8, 4, 8));
        statusBar.getStyleClass().add("status-bar");
        
        // Status label
        statusLabel = new Label("Ready");
        statusLabel.getStyleClass().add(Styles.TEXT_MUTED);
        
        // Count label
        countLabel = new Label("0 / 0 entries");
        countLabel.getStyleClass().add(Styles.TEXT_MUTED);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Memory usage
        Label memoryLabel = new Label("Memory:");
        memoryLabel.getStyleClass().add(Styles.TEXT_MUTED);
        
        memoryBar = new ProgressBar();
        memoryBar.setPrefWidth(100);
        memoryBar.getStyleClass().add(Styles.SMALL);
        updateMemoryUsage();
        
        statusBar.getChildren().addAll(statusLabel, countLabel, spacer, memoryLabel, memoryBar);
        return statusBar;
    }
    
    /**
     * Handles incoming events.
     */
    private void handleEvent(BrobotEvent event) {
        try {
            LogEntry entry = parsingService.parseEvent(event);
            if (entry != null) {
                Platform.runLater(() -> addLogEntry(entry));
            }
        } catch (Exception e) {
            log.error("Failed to parse event: {}", event, e);
        }
    }
    
    /**
     * Adds a log entry to the table.
     */
    private void addLogEntry(LogEntry entry) {
        // Check if we've reached the limit
        if (totalEntries.get() >= MAX_LOG_ENTRIES) {
            // Remove oldest entry
            logTableView.getAllEntries().remove(0);
        } else {
            totalEntries.incrementAndGet();
        }
        
        logTableView.addEntry(entry);
        updateStatusBar();
    }
    
    /**
     * Updates the status bar.
     */
    private void updateStatusBar() {
        // Check if UI components are initialized
        if (countLabel == null || statusLabel == null || logTableView == null) {
            return;
        }
        
        int filtered = logTableView.getFilteredEntries();
        int total = logTableView.getTotalEntries();
        
        countLabel.setText(String.format("%d / %d entries", filtered, total));
        
        if (filtered < total) {
            statusLabel.setText(String.format("Showing %d of %d entries", filtered, total));
        } else {
            statusLabel.setText("Ready");
        }
        
        updateMemoryUsage();
    }
    
    /**
     * Updates memory usage indicator.
     */
    private void updateMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        double usage = (double) usedMemory / maxMemory;
        memoryBar.setProgress(usage);
        
        // Change color based on usage
        if (usage > 0.9) {
            memoryBar.getStyleClass().add(Styles.DANGER);
        } else if (usage > 0.7) {
            memoryBar.getStyleClass().add(Styles.WARNING);
        } else {
            memoryBar.getStyleClass().removeAll(Styles.DANGER, Styles.WARNING);
        }
    }
    
    /**
     * Exports logs to file.
     */
    private void exportLogs() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Logs");
        fileChooser.setInitialFileName(exportService.getDefaultFilename(LogExportService.ExportFormat.TEXT));
        
        // Add filters for each format
        for (LogExportService.ExportFormat format : LogExportService.ExportFormat.values()) {
            FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(
                format.getDescription(), 
                format.getExtension()
            );
            fileChooser.getExtensionFilters().add(filter);
        }
        
        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            exportLogsToFile(file);
        }
    }
    
    /**
     * Exports logs to the specified file.
     */
    private void exportLogsToFile(File file) {
        // Determine format from extension
        String filename = file.getName().toLowerCase();
        final LogExportService.ExportFormat format;
        
        if (filename.endsWith(".csv")) {
            format = LogExportService.ExportFormat.CSV;
        } else if (filename.endsWith(".json")) {
            format = LogExportService.ExportFormat.JSON;
        } else if (filename.endsWith(".html")) {
            format = LogExportService.ExportFormat.HTML;
        } else if (filename.endsWith(".md")) {
            format = LogExportService.ExportFormat.MARKDOWN;
        } else {
            format = LogExportService.ExportFormat.TEXT;
        }
        
        // Create export options
        LogExportService.ExportOptions options = LogExportService.ExportOptions.builder()
                .includeHeaders(true)
                .includeTimestamps(true)
                .includeStackTraces(true)
                .build();
        
        // Export in background
        Task<Void> exportTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                exportService.exportLogs(
                    logTableView.getAllEntries(),
                    file.toPath(),
                    format,
                    options
                );
                return null;
            }
        };
        
        exportTask.setOnSucceeded(e -> {
            statusLabel.setText("Export completed: " + file.getName());
            showSuccessAlert("Export Successful", "Logs exported to: " + file.getPath());
        });
        
        exportTask.setOnFailed(e -> {
            Throwable error = exportTask.getException();
            log.error("Export failed", error);
            showErrorAlert("Export Failed", "Failed to export logs: " + error.getMessage());
        });
        
        statusLabel.setText("Exporting...");
        new Thread(exportTask).start();
    }
    
    /**
     * Clears all logs.
     */
    private void clearLogs() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Clear Logs");
        confirm.setHeaderText("Clear all log entries?");
        confirm.setContentText("This action cannot be undone.");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                logTableView.clearEntries();
                totalEntries.set(0);
                updateStatusBar();
                
                // Add cleared message
                LogEntry clearedEntry = parsingService.createSimpleEntry(
                    "Log entries cleared", 
                    LogEntry.LogLevel.INFO
                );
                addLogEntry(clearedEntry);
            }
        });
    }
    
    /**
     * Shows a success alert.
     */
    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Shows an error alert.
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Programmatically adds a log message.
     */
    public void log(String message, LogEntry.LogLevel level) {
        LogEntry entry = parsingService.createSimpleEntry(message, level);
        Platform.runLater(() -> addLogEntry(entry));
    }
    
    /**
     * Gets the current filter from the filter panel.
     */
    public void setSearchFilter(String searchText) {
        filterPanel.setSearchText(searchText);
    }
}