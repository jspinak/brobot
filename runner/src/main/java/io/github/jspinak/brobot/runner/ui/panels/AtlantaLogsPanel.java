package io.github.jspinak.brobot.runner.ui.panels;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.runner.events.BrobotEvent;
import io.github.jspinak.brobot.runner.events.EventBus;
import io.github.jspinak.brobot.runner.events.LogEntryEvent;
import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.runner.persistence.LogQueryService;
import io.github.jspinak.brobot.runner.ui.components.base.AtlantaCard;
import io.github.jspinak.brobot.tools.logging.model.LogData;
import io.github.jspinak.brobot.tools.logging.model.LogEventType;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Modern logs panel with AtlantaFX styling. Provides a clean, card-based interface for viewing and
 * filtering logs.
 */
@Slf4j
@Component
public class AtlantaLogsPanel extends VBox implements AutoCloseable {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter FULL_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final int MAX_LOG_ENTRIES = 10000;

    // Dependencies
    private final EventBus eventBus;
    private final LogQueryService logQueryService;

    // Data
    private final ObservableList<LogEntryViewModel> logEntries =
            FXCollections.observableArrayList();
    private final FilteredList<LogEntryViewModel> filteredLogs;

    // UI Components
    private TableView<LogEntryViewModel> logTable;
    private TextArea logDetailArea;
    private TextField searchField;
    private ComboBox<String> levelFilter;
    private ComboBox<String> typeFilter;
    private CheckBox autoScrollCheck;
    private Label statusLabel;
    private ProgressIndicator loadingIndicator;

    // Event handling
    private final Consumer<BrobotEvent> eventHandler;
    private final Map<String, Color> levelColors = new ConcurrentHashMap<>();

    @Autowired
    public AtlantaLogsPanel(EventBus eventBus, LogQueryService logQueryService) {
        this.eventBus = eventBus;
        this.logQueryService = logQueryService;
        this.filteredLogs = new FilteredList<>(logEntries);
        this.eventHandler = this::handleEvent;

        getStyleClass().add("logs-panel");

        initializeLevelColors();
        initialize();
        setupEventListeners();

        // Load initial logs
        Platform.runLater(this::refreshLogs);
    }

    private void initializeLevelColors() {
        levelColors.put("ERROR", Color.web("#dc3545"));
        levelColors.put("WARNING", Color.web("#ffc107"));
        levelColors.put("INFO", Color.web("#17a2b8"));
        levelColors.put("DEBUG", Color.web("#6c757d"));
        levelColors.put("SUCCESS", Color.web("#28a745"));
    }

    private void initialize() {
        // Create main content
        VBox mainContent = new VBox();
        mainContent.getChildren().addAll(createFilterBar(), createMainContent());

        getChildren().add(mainContent);
        VBox.setVgrow(mainContent, Priority.ALWAYS);
    }

    /** Creates the filter bar with search and filter controls. */
    private HBox createFilterBar() {
        HBox filterBar = new HBox(16);
        filterBar.getStyleClass().add("action-bar");
        filterBar.setAlignment(Pos.CENTER_LEFT);

        // Search field
        searchField = new TextField();
        searchField.setPromptText("Search logs...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(300);
        searchField.textProperty().addListener((obs, old, text) -> applyFilters());

        // Level filter
        Label levelLabel = new Label("Level:");
        levelFilter = new ComboBox<>();
        levelFilter.getItems().addAll("All Levels", "ERROR", "WARNING", "INFO", "DEBUG");
        levelFilter.setValue("All Levels");
        levelFilter.getStyleClass().add("filter-combo");
        levelFilter.setOnAction(e -> applyFilters());

        // Type filter
        Label typeLabel = new Label("Type:");
        typeFilter = new ComboBox<>();
        typeFilter.getItems().add("All Types");
        for (LogEventType type : LogEventType.values()) {
            typeFilter.getItems().add(type.name());
        }
        typeFilter.setValue("All Types");
        typeFilter.getStyleClass().add("filter-combo");
        typeFilter.setOnAction(e -> applyFilters());

        // Auto-scroll
        autoScrollCheck = new CheckBox("Auto-scroll");
        autoScrollCheck.setSelected(true);
        autoScrollCheck.getStyleClass().add("auto-scroll-check");

        // Action buttons
        Button clearFiltersButton = new Button("Clear Filters");
        clearFiltersButton.getStyleClass().addAll("button", "secondary", "small");
        clearFiltersButton.setOnAction(e -> clearFilters());

        Button exportButton = new Button("Export");
        exportButton.getStyleClass().addAll("button", "secondary", "small");
        exportButton.setOnAction(e -> exportLogs());

        Button clearLogsButton = new Button("Clear");
        clearLogsButton.getStyleClass().addAll("button", "danger", "small");
        clearLogsButton.setOnAction(e -> clearLogs());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Status
        statusLabel = new Label("0 logs");
        statusLabel.getStyleClass().add("status-label");

        // Loading indicator
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setPrefSize(16, 16);
        loadingIndicator.setVisible(false);

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

    /** Creates the main content area with log table and details. */
    private HBox createMainContent() {
        HBox content = new HBox(24);
        content.getStyleClass().add("split-layout");

        // Left: Log table
        AtlantaCard tableCard = new AtlantaCard("Log Entries");
        tableCard.setExpand(true);

        logTable = createLogTable();
        tableCard.setContent(logTable);

        // Right: Log details
        AtlantaCard detailCard = new AtlantaCard("Log Details");
        detailCard.setMinWidth(400);
        detailCard.setPrefWidth(500);

        logDetailArea = new TextArea();
        logDetailArea.setEditable(false);
        logDetailArea.setWrapText(true);
        logDetailArea.getStyleClass().add("log-detail-area");
        logDetailArea.setPromptText("Select a log entry to view details");

        detailCard.setContent(logDetailArea);

        content.getChildren().addAll(tableCard, detailCard);
        HBox.setHgrow(tableCard, Priority.ALWAYS);

        return content;
    }

    /** Creates the log table with styled columns. */
    private TableView<LogEntryViewModel> createLogTable() {
        TableView<LogEntryViewModel> table = new TableView<>();
        table.setPlaceholder(new Label("No logs available"));
        table.getStyleClass().add("log-table");
        table.setItems(filteredLogs);

        // Time column
        TableColumn<LogEntryViewModel, String> timeColumn = new TableColumn<>("Time");
        timeColumn.setCellValueFactory(param -> param.getValue().timeProperty());
        timeColumn.setPrefWidth(100);
        timeColumn.setCellFactory(
                col ->
                        new TableCell<>() {
                            @Override
                            protected void updateItem(String time, boolean empty) {
                                super.updateItem(time, empty);
                                if (empty || time == null) {
                                    setText(null);
                                } else {
                                    setText(time);
                                    getStyleClass().add("time-cell");
                                }
                            }
                        });

        // Level column with colored indicators
        TableColumn<LogEntryViewModel, String> levelColumn = new TableColumn<>("Level");
        levelColumn.setCellValueFactory(param -> param.getValue().levelProperty());
        levelColumn.setPrefWidth(80);
        levelColumn.setCellFactory(
                col ->
                        new TableCell<>() {
                            @Override
                            protected void updateItem(String level, boolean empty) {
                                super.updateItem(level, empty);
                                if (empty || level == null) {
                                    setText(null);
                                    setGraphic(null);
                                } else {
                                    HBox content = new HBox(8);
                                    content.setAlignment(Pos.CENTER_LEFT);

                                    // Colored indicator
                                    Circle indicator = new Circle(4);
                                    indicator.setFill(levelColors.getOrDefault(level, Color.GRAY));

                                    Label label = new Label(level);
                                    label.getStyleClass().add("level-" + level.toLowerCase());

                                    content.getChildren().addAll(indicator, label);
                                    setGraphic(content);
                                    setText(null);
                                }
                            }
                        });

        // Type column
        TableColumn<LogEntryViewModel, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(param -> param.getValue().typeProperty());
        typeColumn.setPrefWidth(120);

        // Message column
        TableColumn<LogEntryViewModel, String> messageColumn = new TableColumn<>("Message");
        messageColumn.setCellValueFactory(param -> param.getValue().messageProperty());
        messageColumn.setPrefWidth(500);
        messageColumn.setCellFactory(
                col ->
                        new TableCell<>() {
                            @Override
                            protected void updateItem(String message, boolean empty) {
                                super.updateItem(message, empty);
                                if (empty || message == null) {
                                    setText(null);
                                } else {
                                    setText(message);
                                    setTooltip(new Tooltip(message));
                                }
                            }
                        });

        table.getColumns().addAll(timeColumn, levelColumn, typeColumn, messageColumn);

        // Selection listener
        table.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (obs, oldVal, newVal) -> {
                            if (newVal != null) {
                                updateDetailPanel(newVal);
                            }
                        });

        // Row styling
        table.setRowFactory(
                tv ->
                        new TableRow<>() {
                            @Override
                            protected void updateItem(LogEntryViewModel item, boolean empty) {
                                super.updateItem(item, empty);
                                getStyleClass()
                                        .removeAll(
                                                "log-row-error",
                                                "log-row-warning",
                                                "log-row-info",
                                                "log-row-debug");

                                if (!empty && item != null) {
                                    String level = item.getLevel().toLowerCase();
                                    getStyleClass().add("log-row-" + level);
                                }
                            }
                        });

        return table;
    }

    /** Updates the detail panel with log entry information. */
    private void updateDetailPanel(LogEntryViewModel entry) {
        if (entry == null) {
            logDetailArea.clear();
            return;
        }

        StringBuilder details = new StringBuilder();
        details.append("Time: ").append(entry.getFullTime()).append("\n");
        details.append("Level: ").append(entry.getLevel()).append("\n");
        details.append("Type: ").append(entry.getType()).append("\n");
        details.append("Success: ").append(entry.isSuccess() ? "Yes" : "No").append("\n");
        details.append("\n");
        details.append("Message:\n").append(entry.getMessage()).append("\n");

        if (entry.getRawLogData() != null) {
            LogData data = entry.getRawLogData();
            if (data.getErrorMessage() != null) {
                details.append("\nError: ").append(data.getErrorMessage());
            }
            if (data.getCurrentStateName() != null) {
                details.append("\nCurrent State: ").append(data.getCurrentStateName());
            }
            if (data.getPerformance() != null && data.getPerformance().getActionDuration() > 0) {
                details.append("\nDuration: ")
                        .append(data.getPerformance().getActionDuration())
                        .append(" ms");
            }
        }

        logDetailArea.setText(details.toString());
    }

    /** Applies filters to the log entries. */
    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        String selectedLevel = levelFilter.getValue();
        String selectedType = typeFilter.getValue();

        Predicate<LogEntryViewModel> predicate =
                entry -> {
                    // Search filter
                    if (!searchText.isEmpty()) {
                        boolean matchesSearch =
                                entry.getMessage().toLowerCase().contains(searchText)
                                        || entry.getType().toLowerCase().contains(searchText);
                        if (!matchesSearch) return false;
                    }

                    // Level filter
                    if (!"All Levels".equals(selectedLevel)
                            && !entry.getLevel().equals(selectedLevel)) {
                        return false;
                    }

                    // Type filter
                    if (!"All Types".equals(selectedType)
                            && !entry.getType().equals(selectedType)) {
                        return false;
                    }

                    return true;
                };

        filteredLogs.setPredicate(predicate);
        updateStatus();
    }

    /** Clears all filters. */
    private void clearFilters() {
        searchField.clear();
        levelFilter.setValue("All Levels");
        typeFilter.setValue("All Types");
    }

    /** Updates the status label. */
    private void updateStatus() {
        Platform.runLater(
                () -> {
                    int total = logEntries.size();
                    int filtered = filteredLogs.size();

                    if (total == filtered) {
                        statusLabel.setText(total + " logs");
                    } else {
                        statusLabel.setText(filtered + " of " + total + " logs");
                    }
                });
    }

    /** Refreshes logs from the query service. */
    private void refreshLogs() {
        loadingIndicator.setVisible(true);

        Platform.runLater(
                () -> {
                    try {
                        List<LogData> recentLogs = logQueryService.getRecentLogs(200);
                        List<LogEntryViewModel> viewModels = new ArrayList<>();

                        for (LogData logData : recentLogs) {
                            viewModels.add(new LogEntryViewModel(logData));
                        }

                        logEntries.clear();
                        logEntries.addAll(viewModels);
                        updateStatus();
                    } catch (Exception e) {
                        log.error("Failed to refresh logs", e);
                    } finally {
                        loadingIndicator.setVisible(false);
                    }
                });
    }

    /** Adds a new log entry. */
    private void addLogEntry(LogEntryViewModel entry) {
        Platform.runLater(
                () -> {
                    logEntries.add(0, entry);

                    // Remove oldest if exceeding max
                    if (logEntries.size() > MAX_LOG_ENTRIES) {
                        logEntries.remove(logEntries.size() - 1);
                    }

                    updateStatus();

                    // Auto-scroll to top
                    if (autoScrollCheck.isSelected() && logTable.getItems().size() > 0) {
                        logTable.scrollTo(0);
                    }
                });
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
                                logEntries.clear();
                                logDetailArea.clear();
                                updateStatus();
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
                        new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName(
                "logs_"
                        + LocalDateTime.now()
                                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));

        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            try {
                if (file.getName().endsWith(".csv")) {
                    exportAsCSV(file);
                } else {
                    exportAsText(file);
                }

                showExportSuccess(file);
            } catch (IOException e) {
                showExportError(e);
            }
        }
    }

    private void exportAsText(File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            for (LogEntryViewModel entry : filteredLogs) {
                writer.println("=".repeat(80));
                writer.println("Time: " + entry.getFullTime());
                writer.println("Level: " + entry.getLevel());
                writer.println("Type: " + entry.getType());
                writer.println("Message: " + entry.getMessage());
                writer.println();
            }
        }
    }

    private void exportAsCSV(File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("Time,Level,Type,Message");

            for (LogEntryViewModel entry : filteredLogs) {
                writer.printf(
                        "\"%s\",\"%s\",\"%s\",\"%s\"%n",
                        entry.getFullTime(),
                        entry.getLevel(),
                        entry.getType(),
                        entry.getMessage().replace("\"", "\"\""));
            }
        }
    }

    private void showExportSuccess(File file) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export Successful");
        alert.setHeaderText("Logs exported successfully");
        alert.setContentText("File saved to: " + file.getAbsolutePath());
        alert.showAndWait();
    }

    private void showExportError(Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Export Failed");
        alert.setHeaderText("Failed to export logs");
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }

    /** Sets up event listeners. */
    private void setupEventListeners() {
        eventBus.subscribe(BrobotEvent.EventType.LOG_MESSAGE, eventHandler);
        eventBus.subscribe(BrobotEvent.EventType.LOG_WARNING, eventHandler);
        eventBus.subscribe(BrobotEvent.EventType.LOG_ERROR, eventHandler);

        // Update status when entries change
        logEntries.addListener(
                (javafx.collections.ListChangeListener<LogEntryViewModel>) c -> updateStatus());
    }

    /** Handles incoming log events. */
    private void handleEvent(BrobotEvent event) {
        if (event instanceof LogEntryEvent logEntryEvent) {
            if (logEntryEvent.getLogEntry() != null) {
                addLogEntry(new LogEntryViewModel(logEntryEvent.getLogEntry()));
            }
        } else if (event instanceof LogEvent logEvent) {
            addLogEntry(new LogEntryViewModel(logEvent));
        }
    }

    @Override
    public void close() {
        eventBus.unsubscribe(BrobotEvent.EventType.LOG_MESSAGE, eventHandler);
        eventBus.unsubscribe(BrobotEvent.EventType.LOG_WARNING, eventHandler);
        eventBus.unsubscribe(BrobotEvent.EventType.LOG_ERROR, eventHandler);

        logEntries.clear();
        log.info("AtlantaLogsPanel closed");
    }

    /** View model for log entries. */
    @Getter
    @Setter
    public static class LogEntryViewModel {
        private final SimpleStringProperty time = new SimpleStringProperty();
        private final SimpleStringProperty fullTime = new SimpleStringProperty();
        private final SimpleStringProperty level = new SimpleStringProperty();
        private final SimpleStringProperty type = new SimpleStringProperty();
        private final SimpleStringProperty message = new SimpleStringProperty();
        private final SimpleBooleanProperty success = new SimpleBooleanProperty();
        private LogData rawLogData;

        public LogEntryViewModel(LogData logData) {
            this.rawLogData = logData;

            LocalDateTime timestamp =
                    LocalDateTime.ofInstant(logData.getTimestamp(), ZoneId.systemDefault());

            this.time.set(timestamp.format(TIME_FORMATTER));
            this.fullTime.set(timestamp.format(FULL_TIME_FORMATTER));
            this.type.set(logData.getType() != null ? logData.getType().toString() : "SYSTEM");
            this.message.set(logData.getDescription());
            this.success.set(logData.isSuccess());

            // Determine level
            if (logData.getType() == LogEventType.ERROR) {
                this.level.set("ERROR");
            } else if (!logData.isSuccess()) {
                this.level.set("WARNING");
            } else {
                this.level.set("INFO");
            }
        }

        public LogEntryViewModel(LogEvent logEvent) {
            LocalDateTime timestamp =
                    LocalDateTime.ofInstant(logEvent.getTimestamp(), ZoneId.systemDefault());

            this.time.set(timestamp.format(TIME_FORMATTER));
            this.fullTime.set(timestamp.format(FULL_TIME_FORMATTER));
            this.level.set(logEvent.getLevel().name());
            this.message.set(logEvent.getMessage());
            this.success.set(
                    logEvent.getLevel() == LogEvent.LogLevel.INFO
                            || logEvent.getLevel() == LogEvent.LogLevel.DEBUG);

            // Try to determine type
            try {
                this.type.set(
                        LogEventType.valueOf(logEvent.getCategory().toUpperCase()).toString());
            } catch (Exception e) {
                this.type.set("SYSTEM");
            }

            // Create minimal LogData for consistency
            LogData tempData = new LogData();
            tempData.setSuccess(isSuccess());
            tempData.setDescription(getMessage());
            tempData.setTimestamp(logEvent.getTimestamp());
            if (logEvent.getException() != null) {
                tempData.setErrorMessage(logEvent.getException().toString());
            }
            this.rawLogData = tempData;
        }

        public String getTime() {
            return time.get();
        }

        public SimpleStringProperty timeProperty() {
            return time;
        }

        public String getFullTime() {
            return fullTime.get();
        }

        public SimpleStringProperty fullTimeProperty() {
            return fullTime;
        }

        public String getLevel() {
            return level.get();
        }

        public SimpleStringProperty levelProperty() {
            return level;
        }

        public String getType() {
            return type.get();
        }

        public SimpleStringProperty typeProperty() {
            return type;
        }

        public String getMessage() {
            return message.get();
        }

        public SimpleStringProperty messageProperty() {
            return message;
        }

        public boolean isSuccess() {
            return success.get();
        }

        public SimpleBooleanProperty successProperty() {
            return success;
        }
    }
}
